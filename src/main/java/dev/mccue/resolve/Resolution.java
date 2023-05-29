package dev.mccue.resolve;

import dev.mccue.resolve.util.LL;

import java.io.PrintStream;
import java.util.*;

public final class Resolution {
    private final VersionMap versionMap;
    private final Trace trace;

    private Resolution(VersionMap versionMap, Trace trace) {
        this.versionMap = versionMap;
        this.trace = trace;
    }

    VersionMap versionMap() {
        return versionMap;
    }

    static Exclusions updateExclusions(
            Library library,
            InclusionDecision inclusionDecision,
            CoordinateId coordinateId,
            LL<DependencyId> usePath,
            HashMap<DependencyId, Exclusions> cut,
            Exclusions exclusions
    ) {
        if (inclusionDecision.included()) {
            cut.put(new DependencyId(library, coordinateId), exclusions);
            return exclusions;
        }
        else if (inclusionDecision == InclusionDecision.SAME_VERSION) {
            var key = new DependencyId(library, coordinateId);
            var cutCoord = cut.get(key);
            var newCut = cutCoord.meet(exclusions);
            cut.put(key, newCut);
            return newCut;
        }
        else {
            return exclusions;
        }
    }

    /**
     * @param initialDependencies  each dependency is defined as a lib (symbol) and coordinate (maven, git, local, etc)
     * @param overrideDependencies a map of lib to coord to use if lib is found
     * @param cache                cache for files.
     */
    static Resolution expandDependencies(
            Map<Library, Dependency> initialDependencies,
            Map<Library, Dependency> overrideDependencies,
            Cache cache
    ) {
        var cut = new HashMap<DependencyId, Exclusions>();
        record QueueEntry(
                Dependency dependency,
                LL<DependencyId> path
        ) {
        }

        Queue<QueueEntry> q = new ArrayDeque<>();
        initialDependencies.forEach((library, dependency) -> q.add(
                new QueueEntry(
                        new Dependency(library, dependency.coordinate(), dependency.exclusions()),
                        new LL.Nil<>()
                )
        ));


        var versionMap = new VersionMap();
        var trace = new Trace();

        while (!q.isEmpty()) {
            var queueEntry = q.poll();

            var library = queueEntry.dependency.library();
            var dependency = overrideDependencies.getOrDefault(
                    library,
                    queueEntry.dependency
            );

            var coordinate = dependency.coordinate();
            var coordinateId = coordinate.id();

            var decision = versionMap.includeCoordinate(
                    dependency,
                    coordinateId,
                    queueEntry.path
            );

            trace.add(new Trace.Entry(
                    queueEntry.path.reverse().toJavaList(),
                    dependency.library(),
                    dependency.coordinate().id(),
                    decision
            ));

            var exclusions = updateExclusions(
                    library,
                    decision,
                    coordinateId,
                    queueEntry.path,
                    cut,
                    dependency.exclusions()
            );

            if (decision.included()) {
                var coordinateManifest = coordinate.getManifest(library, cache);
                var afterExclusions = coordinateManifest
                        .dependencies()
                        .stream()
                        .filter(dep -> exclusions.shouldInclude(dep.library()))
                        .map(dep -> dep.withExclusions(dep.exclusions().join(exclusions)))
                        .toList();

                for (var manifestDep : afterExclusions) {
                    q.add(new QueueEntry(
                            manifestDep,
                            queueEntry.path.prepend(new DependencyId(queueEntry.dependency))
                    ));
                }
            }
        }


        return new Resolution(
                versionMap,
                trace
        );
    }

    public List<Dependency> selectedDependencies() {
        return versionMap.selectedDependencies();
    }

    public void printTree(PrintStream out, List<Library> hideLibraries) {
        var keyedEntries = new HashMap<List<DependencyId>, ArrayList<Trace.Entry>>();
        for (var entry : trace) {
            keyedEntries.put(entry.path(), keyedEntries.getOrDefault(entry.path(), new ArrayList<>()));
            keyedEntries.get(entry.path()).add(entry);
        }

        var roots = trace.stream()
                .filter(entry -> entry.path().isEmpty())
                .toList();

        var q = new ArrayDeque<>(roots);

        int depth;
        while (!q.isEmpty()) {
            var entry = q.pollFirst();
            depth = entry.path().size();

            if (entry.inclusionDecision() != InclusionDecision.NEW_TOP_DEP &&
                    hideLibraries.contains(entry.library())) {
                continue;
            }

            boolean superseded = Set.of(InclusionDecision.SAME_VERSION, InclusionDecision.NEW_DEP)
                    .contains(entry.inclusionDecision())
                    && !versionMap
                            .selectedVersion(entry.library())
                            .equals(Optional.of(entry.coordinateId()));


            if (depth != 0) {
                out.print("  ".repeat(depth));


                if (!entry.inclusionDecision().included() && !Set.of(InclusionDecision.SAME_VERSION, InclusionDecision.NEW_DEP)
                        .contains(entry.inclusionDecision())) {
                    out.print("X ");
                }
                else if (superseded) {
                    out.print("X ");
                }
                else {
                    out.print(". ");
                }
            }

            out.print(entry.library().group().value());
            out.print("/");
            out.print(entry.library().artifact().value());

            out.print(" ");
            out.print(entry.coordinateId());
            if (!entry.inclusionDecision().included() && !Set.of(
                    InclusionDecision.SAME_VERSION,
                    InclusionDecision.NEW_DEP
            ).contains(entry.inclusionDecision())) {
                out.print(" " + entry.inclusionDecision());
            }
            else if (superseded) {
                out.print(" " + "SUPERSEDED");
            }
            out.println();

            var nextPath = new ArrayList<>(entry.path());
            nextPath.add(new DependencyId(entry.library(), entry.coordinateId()));

            var next = keyedEntries.get(nextPath);
            if (next != null) {
                for (int i = next.size() - 1; i >= 0; i--) {
                    q.addFirst(next.get(i));
                }
            }
        }
    }

    public void printTree() {
        printTree(System.out, List.of());
    }

    public void printTree(List<Library> hideLibraries) {
        printTree(System.out, hideLibraries);
    }
}

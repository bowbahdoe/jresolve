package dev.mccue.resolve;

import dev.mccue.resolve.util.LL;

import java.io.PrintStream;
import java.io.PrintWriter;
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
                null
        );
    }

    public List<Dependency> selectedDependencies() {
        return versionMap.selectedDependencies();
    }

    public void printTree(PrintStream out) {
        int depth = 0;
        for (var entry : trace) {

            if (depth != 0) {
                System.out.print("  ".repeat(depth));
                System.out.print(". ");
            }
            System.out.print(entry.library().group().value());
            System.out.print("/");
            System.out.print(entry.library().artifact().value());
            System.out.print(entry.coordinateId());
            if (!entry.inclusionDecision().included()) {
                System.out.print(" " + entry.inclusionDecision());
            }
            depth++;
            System.out.println();
        }
        out.println("");
    }

    public void printTree() {
        printTree(System.out);
    }
}

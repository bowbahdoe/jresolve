package dev.mccue.resolve;

import dev.mccue.guava.graph.Graph;
import dev.mccue.guava.graph.GraphBuilder;
import dev.mccue.guava.graph.Graphs;
import dev.mccue.guava.graph.MutableGraph;
import dev.mccue.resolve.maven.MavenCoordinateId;
import dev.mccue.resolve.util.LL;
import org.jspecify.annotations.Nullable;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class Resolve {
    private final LinkedHashMap<Library, Dependency> dependencies;
    private final LinkedHashMap<Library, Dependency> dependencyOverrides;
    ExecutorService executorService;
    Cache cache;

    public Resolve() {
        this.dependencies = new LinkedHashMap<>();
        this.dependencyOverrides = new LinkedHashMap<>();
        this.cache = Cache.standard();
        var count = new AtomicInteger();
        this.executorService = Executors.newFixedThreadPool(8, (r) -> {
            var t = new Thread(r);
            t.setName("resolve-" + count.getAndIncrement());
            t.setDaemon(true);
            return t;
        });
    }

    public Resolve addDependency(Dependency dependency) {
        this.dependencies.put(dependency.library(), dependency);
        return this;
    }

    public Resolve addDependencies(List<Dependency> dependencies) {
        dependencies.forEach(this::addDependency);
        return this;
    }

    public Resolve addDependencyOverride(Dependency dependency) {
        this.dependencyOverrides.put(dependency.library(), dependency);
        return this;
    }

    public Resolve addDependencyOverride(Library library, Dependency dependency) {
        this.dependencyOverrides.put(library, dependency);
        return this;
    }

    public Resolve addDependencyOverrides(List<Dependency> dependencies) {
        dependencies.forEach(this::addDependencyOverride);
        return this;
    }

    public Resolve withCache(Cache cache) {
        this.cache = cache;
        return this;
    }

    public Resolve withExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    public Result run() {
        return Result.expandDependencies(
                dependencies,
                dependencyOverrides,
                cache,
                executorService
        );
    }

    public Fetch fetch() {
        return new Fetch(this);
    }

    public static final class Result {
        private final VersionMap versionMap;
        private final Trace trace;
        private final MutableGraph<Library> libraryGraph;

        private Result(VersionMap versionMap, Trace trace, MutableGraph<Library> libraryGraph) {
            this.versionMap = versionMap;
            this.trace = trace;
            this.libraryGraph = libraryGraph;
        }

        VersionMap versionMap() {
            return versionMap;
        }

        public Map<Usage, Set<Library>> librariesForUsage(
                Map<Library, Set<Usage>> knownUsages,
                @Nullable Usage defaultUsage
        ) {
            knownUsages = new HashMap<>(knownUsages);
            knownUsages.entrySet().removeIf(entry -> entry.getValue().isEmpty());

            Map<Usage, Set<Library>> usageToLibrary = new LinkedHashMap<>();

            knownUsages.forEach((library, usages) -> {
                usages.forEach(usage -> {
                    usageToLibrary.putIfAbsent(usage, new LinkedHashSet<>());
                    usageToLibrary.get(usage).add(library);

                    libraryGraph.addNode(library);
                    var successors = Graphs.reachableNodes(libraryGraph, library);
                    for (var successor : successors) {
                        usageToLibrary.get(usage).add(successor);
                    }
                });
            });

            var librariesWithNoKnownUsage = new LinkedHashSet<>(versionMap.selectedLibraries());
            usageToLibrary.values().forEach(librariesWithNoKnownUsage::removeAll);

            if (!librariesWithNoKnownUsage.isEmpty() && defaultUsage == null) {
                throw new IllegalArgumentException("Libraries not given a usage: " + librariesWithNoKnownUsage);
            }

            for (var library : librariesWithNoKnownUsage) {
                usageToLibrary.putIfAbsent(defaultUsage, new LinkedHashSet<>());
                usageToLibrary.get(defaultUsage).add(library);
            }

            for (var entry : usageToLibrary.entrySet()) {
                entry.setValue(Collections.unmodifiableSet(entry.getValue()));
            }

            return Collections.unmodifiableMap(usageToLibrary);
        }

        record ExclusionsUpdate(
                Exclusions newExclusions,
                boolean wasUpdated
        ) {

        }

        static ExclusionsUpdate updateExclusions(
                Library library,
                InclusionDecision inclusionDecision,
                CoordinateId coordinateId,
                HashMap<DependencyId, Exclusions> cut,
                Exclusions exclusions
        ) {
            if (inclusionDecision.included()) {
                cut.put(new DependencyId(library, coordinateId), exclusions);
                return new ExclusionsUpdate(exclusions, false);
            }
            else if (inclusionDecision == InclusionDecision.SAME_VERSION) {
                var key = new DependencyId(library, coordinateId);
                var cutCoord = cut.get(key);
                var newCut = cutCoord.meet(exclusions);
                cut.put(key, newCut);
                return new ExclusionsUpdate(newCut, !newCut.equals(cutCoord));
            }
            else {
                return new ExclusionsUpdate(exclusions, false);
            }
        }

        /**
         * @param initialDependencies  each dependency is defined as a lib (symbol) and coordinate (maven, git, local, etc.)
         * @param overrideDependencies a map of lib to coord to use if lib is found
         * @param cache                cache for files.
         */
        static Result expandDependencies(
                Map<Library, Dependency> initialDependencies,
                Map<Library, Dependency> overrideDependencies,
                Cache cache,
                ExecutorService executorService
        ) {
            MutableGraph<Library> libraryGraph = GraphBuilder.directed()
                    .allowsSelfLoops(true)
                    .build();
            var cut = new HashMap<DependencyId, Exclusions>();
            record QueueEntry(
                    Dependency dependency,
                    LL<DependencyId> path,
                    Future<Manifest> manifestPrefetch
            ) {
            }

            Queue<QueueEntry> q = new ArrayDeque<>();
            initialDependencies.forEach((library, dependency) -> {
                q.add(
                        new QueueEntry(
                                new Dependency(library, dependency.coordinate(), dependency.exclusions()),
                                new LL.Nil<>(),
                                executorService.submit(() -> dependency.coordinate().getManifest(cache))
                        )
                );
            });


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

                var exclusionsUpdate = updateExclusions(
                        library,
                        decision,
                        coordinateId,
                        cut,
                        dependency.exclusions()
                );

                var exclusions = exclusionsUpdate.newExclusions;


                if (decision.included() || exclusionsUpdate.wasUpdated) {
                    Manifest coordinateManifest;
                    try {
                        coordinateManifest = queueEntry.manifestPrefetch.get();
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e.getCause());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }


                    var afterExclusions = coordinateManifest
                            .dependencies()
                            .stream()
                            .filter(dep -> exclusions.shouldInclude(dep.library()))
                            .map(dep -> dep
                                    .withExclusions(dep.exclusions().join(exclusions)))
                            .toList();

                    for (var manifestDep : afterExclusions) {
                        libraryGraph.putEdge(library, manifestDep.library());
                        q.add(new QueueEntry(
                                manifestDep,
                                queueEntry.path.prepend(new DependencyId(queueEntry.dependency)),
                                executorService.submit(() -> manifestDep.coordinate().getManifest(cache))
                        ));
                    }
                }
            }


            return new Result(
                    versionMap,
                    trace,
                    libraryGraph
            );
        }

        public List<Dependency> selectedDependencies() {
            return versionMap.selectedDependencies();
        }

        public void printTree(PrintWriter out, List<Library> hideLibraries) {
            var keyedEntries = new HashMap<List<DependencyId>, ArrayList<Trace.Entry>>();
            for (var entry : trace) {
                keyedEntries.put(entry.path(), keyedEntries.getOrDefault(entry.path(), new ArrayList<>()));
                keyedEntries.get(entry.path()).add(entry);
            }

            var roots = trace.stream()
                    .filter(entry -> entry.path().isEmpty())
                    .sorted(Comparator.comparing((Trace.Entry entry) -> entry.library().group())
                            .thenComparing((Trace.Entry entry) -> entry.library().artifact()))
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

                boolean superseded = Set
                                .of(InclusionDecision.SAME_VERSION, InclusionDecision.NEW_DEP)
                                .contains(entry.inclusionDecision())
                        &&
                                !versionMap
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

                if (!"".equals(entry.library().group().value())) {
                    out.print(entry.library().group().value());
                    out.print("/");
                }
                out.print(entry.library().artifact().value());

                out.print(" ");
                if (entry.coordinateId() instanceof MavenCoordinateId mavenCoordinateId) {
                    out.print(mavenCoordinateId.version());
                }
                else if (!(
                        entry.coordinateId() instanceof HttpsCoordinate
                                || entry.coordinateId() instanceof URICoordinate
                                || entry.coordinateId() instanceof PathCoordinate
                )) {
                    out.print(entry.coordinateId());
                }

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

        public void printTree(PrintStream out, List<Library> hideLibraries) {
            var writer = new PrintWriter(out);
            printTree(writer, hideLibraries);
            writer.flush();
        }

        public void printTree(PrintStream out) {
            printTree(out, List.of());
        }

        public void printTree(PrintWriter out) {
            printTree(out, List.of());
        }

        public void printTree() {
            printTree(System.out, List.of());
        }

        public void printTree(List<Library> hideLibraries) {
            printTree(System.out, hideLibraries);
        }

        public Fetch fetch() {
            return new Fetch(this);
        }
    }
}

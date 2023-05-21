package dev.mccue.resolve;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Gold;
import dev.mccue.resolve.util.LL;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

@Gold
@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L18-L26")
public record Artifact(String value) implements Comparable<Artifact> {
    public static final Artifact ALL = new Artifact("*");
    public Artifact {
        Objects.requireNonNull(value, "value must not be null");
    }

    public Artifact map(Function<String, String> f) {
        return new Artifact(f.apply(this.value));
    }

    @Override
    public int compareTo(Artifact o) {
        return this.value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return value;
    }

    public static record OldResolution(
            VersionMap versionMap,
            Trace trace
    ) {
        private record CutKey(Library library, CoordinateId coordinateId) {}

        static Exclusions updateExclusions(
                Library library,
                InclusionDecision inclusionDecision,
                CoordinateId coordinateId,
                LL<Library> usePath,
                HashMap<CutKey, Exclusions> cut,
                Exclusions exclusions
        ) {
            if (inclusionDecision.included()) {
                cut.put(new CutKey(library, coordinateId), exclusions);
                return exclusions;
            }
            else if (inclusionDecision == InclusionDecision.SAME_VERSION) {
                var key = new CutKey(library, coordinateId);
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
         * @param executorService      an executor to use for the task of downloading individual files
         * @param cache                cache for files.
         */
        static OldResolution expandDependencies(
                Map<Library, Dependency> initialDependencies,
                Map<Library, Dependency> overrideDependencies,
                ExecutorService executorService,
                Cache cache
        ) {


            var cut = new HashMap<CutKey, Exclusions>();
            record QueueEntry(
                    Dependency dependency,
                    LL<Library> path
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

                Manifest manifest = () -> coordinate.getManifest(library, cache)
                        .dependencies()
                        .stream()
                        .filter(dep -> exclusions.shouldInclude(dep.library()))
                        .map(dep -> dep.withExclusions(dep.exclusions().join(exclusions)))
                        .toList();

                for (var manifestDep : manifest.dependencies()) {
                    q.add(new QueueEntry(
                            manifestDep,
                            queueEntry.path.append(queueEntry.dependency.library())
                    ));
                }
            }


            return new OldResolution(
                    versionMap,
                    null
            );
        }
    }

    static final class VersionMap {
        private final HashMap<Library, Entry> value;

        public VersionMap() {
            this.value = new HashMap<>();
        }

        public record Entry(
                HashMap<CoordinateId, Coordinate> versions,
                HashMap<CoordinateId, HashSet<LL<Library>>> paths,
                CoordinateId currentSelection,
                boolean topDep
        ) {
            public Entry() {
                this(new HashMap<>(), new HashMap<>(), null, false);
            }

            public Entry(
                    HashMap<CoordinateId, Coordinate> versions,
                    HashMap<CoordinateId, HashSet<LL<Library>>> paths
            ) {
                this(versions, paths, null, false);
            }

            Entry asTopDep() {
                return new Entry(this.versions, this.paths, this.currentSelection, true);
            }

            Entry withSelection(CoordinateId selection) {
                return new Entry(this.versions, this.paths, selection, this.topDep);
            }
        }

        public void addVersion(
                Library library,
                Coordinate coordinate,
                LL<Library> dependencyPath,
                CoordinateId coordinateId
        ) {
            var entry = this.value.getOrDefault(library, new Entry());

            entry.versions.put(coordinateId, coordinate);
            entry.paths.computeIfAbsent(coordinateId, k -> new HashSet<>());
            entry.paths.get(coordinateId).add(dependencyPath);

            this.value.put(library, entry);
        }

        public void selectVersion(Library library, CoordinateId coordinateId, boolean isTop) {
            var entry = this.value.computeIfAbsent(library, (__) -> new Entry());
            entry = entry.withSelection(coordinateId);
            if (isTop) {
                entry = entry.asTopDep();
            }
            this.value.put(library, entry);
        }

        public Optional<CoordinateId> selectedVersion(Library library) {
            var entry = this.value.get(library);
            if (entry == null) {
                return Optional.empty();
            }
            else {
                return Optional.ofNullable(entry.currentSelection);
            }
        }

        public Optional<Coordinate> selectedDep(Library library) {
            return selectedVersion(library)
                    .map(this.value.get(library).versions::get);
        }

        public Map<Library, CoordinateId> selectedCoordinateIds() {
            return value.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().currentSelection
                    ));
        }

        public Optional<HashSet<LL<Library>>> selectedPaths(Library library) {
            var entry = this.value.get(library);
            if (entry == null) {
                return Optional.empty();
            }
            else {
                var selectedVersion = this.selectedVersion(library).orElse(null);
                if (selectedVersion == null) {
                    return Optional.empty();
                }
                else {
                    return Optional.ofNullable(entry.paths.get(selectedVersion));
                }
            }
        }

        public boolean parentMissing(
                LL<Library> parentDependencyPath
        ) {
            if (parentDependencyPath.isEmpty()) {
                return false;
            }

            var path = parentDependencyPath;
            while (path instanceof LL.Cons<Library> consPath) {
                var lib = consPath.head();
                var checkPath = consPath.tail();
                var vmapEntry = value.get(lib);
                if (vmapEntry != null && vmapEntry.paths()
                        .getOrDefault(vmapEntry.currentSelection, new HashSet<>())
                        .contains(checkPath)) {
                    path = checkPath;
                    continue;
                }

                return true;
            }

            return false;
        }

        // For the given paths, deselect any libs whose only selected version paths are in omitted-paths
        public void deselectOrphans(
                List<LL<Library>> omittedDependencyPaths
        ) {

        }

        public InclusionDecision includeCoordinate(
                Dependency dependency,
                CoordinateId coordinateId,
                LL<Library> dependencyPath
        ) {
            var library = dependency.library();
            var coordinate = dependency.coordinate();
            if (dependencyPath.isEmpty()) {
                this.addVersion(library, coordinate, dependencyPath, coordinateId);
                this.selectVersion(library, coordinateId, true);
                return InclusionDecision.NEW_TOP_DEP;
            }
            else if (!dependency.exclusions().shouldInclude(library)) {
                return InclusionDecision.EXCLUDED;
            }
            else if (this.value.get(library) != null && this.value.get(library).topDep()) {
                return InclusionDecision.USE_TOP;
            }
            else if (parentMissing(dependencyPath)) {
                return InclusionDecision.PARENT_OMITTED;
            }
            else if (selectedVersion(library).isEmpty()) {
                this.addVersion(library, coordinate, dependencyPath, coordinateId);
                this.selectVersion(library, coordinateId, false);
                return InclusionDecision.NEW_DEP;
            }
            else if (Objects.equals(selectedVersion(library).orElse(null), coordinateId)) {
                this.addVersion(library, coordinate, dependencyPath, coordinateId);
                return InclusionDecision.SAME_VERSION;
            }
            else {
                System.out.println(selectedVersion(library));
                var selectedDep = selectedDep(library)
                        .orElseThrow();
                Objects.requireNonNull(selectedDep, "selectedVersion");

                var comparison = coordinate.compareVersions(selectedDep);
                if (comparison == Coordinate.VersionOrdering.INCOMPARABLE) {
                    throw new RuntimeException("Incomparable coordinates: " + coordinate.getClass() + ", " + selectedDep.getClass());
                }
                else if (comparison == Coordinate.VersionOrdering.GREATER_THAN) {
                    addVersion(library, coordinate, dependencyPath, coordinateId);
                    // TODO

                    deselectOrphans(List.of());

                    //
                    selectVersion(library, coordinateId, false);
                    return InclusionDecision.NEWER_VERSION;
                }
                else {
                    return InclusionDecision.OLDER_VERSION;
                }
            }
        }

        @Override
        public String toString() {
            return this.value.toString();
        }


        public void printPrettyString() {
            value.forEach((library, entry) -> {
                System.out.println(library);
                System.out.println("-".repeat(40));


                System.out.print(" ".repeat(4));
                System.out.println("TOP_DEP");
                System.out.print(" ".repeat(8));
                System.out.println(entry.topDep);

                System.out.print(" ".repeat(4));
                System.out.println("CURRENT_SELECTION");
                System.out.print(" ".repeat(8));
                System.out.println(entry.currentSelection);

                System.out.print(" ".repeat(4));
                System.out.println("VERSIONS");
                entry.versions.forEach(((coordinateId, coordinate) -> {
                    System.out.print(" ".repeat(8));
                    System.out.print(coordinateId);
                    System.out.print("  ->  ");
                    System.out.println(coordinate);
                }));

                System.out.print(" ".repeat(4));
                System.out.println("PATHS");
                entry.paths.forEach((coordinateId, dependencyPaths) -> {
                    System.out.print(" ".repeat(8));
                    System.out.println(coordinateId);
                    dependencyPaths.forEach(path -> {
                        System.out.print(" ".repeat(12));
                        System.out.println(path);
                    });
                });
                System.out.println();
            });
        }


    }
}

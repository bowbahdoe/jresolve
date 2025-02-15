package dev.mccue.resolve;


import dev.mccue.resolve.util.LL;

import java.util.*;
import java.util.stream.Collectors;


final class VersionMap {
    private final LinkedHashMap<Library, Entry> value;

    public VersionMap() {
        this.value = new LinkedHashMap<>();
    }

    public record Entry(
            LinkedHashMap<CoordinateId, Coordinate> versions,
            LinkedHashMap<CoordinateId, HashSet<LL<DependencyId>>> paths,
            CoordinateId currentSelection,
            boolean topDep
    ) {
        public Entry() {
            this(new LinkedHashMap<>(), new LinkedHashMap<>(), null, false);
        }

        public Entry(
                LinkedHashMap<CoordinateId, Coordinate> versions,
                LinkedHashMap<CoordinateId, HashSet<LL<DependencyId>>> paths
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
            LL<DependencyId> dependencyPath,
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
                .filter(entry -> entry.getValue().currentSelection != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().currentSelection
                ));
    }

    public List<Dependency> selectedDependencies() {
        return value.entrySet()
                .stream()
                .filter(entry -> entry.getValue().currentSelection != null)
                .map(entry -> new Dependency(
                        entry.getKey(),
                        entry.getValue().versions.get(
                                entry.getValue().currentSelection
                        ))
                )
                .toList();
    }

    List<Library> selectedLibraries() {
        return selectedDependencies()
                .stream()
                .map(Dependency::library)
                .toList();
    }

    public Optional<List<LL<DependencyId>>> selectedPaths(Library library) {
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
                return Optional.ofNullable(List.copyOf(entry.paths.get(selectedVersion)));
            }
        }
    }

    public boolean parentMissing(
            LL<DependencyId> parentDependencyPath
    ) {
        if (parentDependencyPath.isEmpty()) {
            return false;
        }

        var path = parentDependencyPath;
        while (path instanceof LL.Cons<DependencyId> consPath) {
            var dependencyId = consPath.head();
            var checkPath = consPath.tail();
            var vmapEntry = value.get(dependencyId.library());
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

    public void deselectOrphans(
            List<LL<DependencyId>> omittedDependencyPaths
    ) {
        var toPut = new ArrayList<Map.Entry<Library, Entry>>();
        for (var kv : this.value.entrySet()) {
            var library = kv.getKey();
            var entry = kv.getValue();

            var allPaths = this.selectedPaths(library)
                    .orElse(List.of());

            boolean deselect = allPaths.stream()
                    .allMatch(path -> omittedDependencyPaths.stream()
                            .anyMatch(omittedPath -> omittedPath.isSuffix(path)));

            if (deselect) {
                var newEntry = entry.withSelection(null);
                toPut.add(Map.entry(library, newEntry));
            }
        }

        toPut.forEach(kv -> this.value.put(kv.getKey(), kv.getValue()));
    }

    public InclusionDecision includeCoordinate(
            Dependency dependency,
            CoordinateId coordinateId,
            LL<DependencyId> dependencyPath
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
            var selectedDep = selectedDep(library)
                    .orElseThrow();
            Objects.requireNonNull(selectedDep, "selectedVersion");

            var comparison = coordinate.compareVersions(selectedDep);
            if (comparison == Coordinate.VersionOrdering.INCOMPARABLE) {
                throw new RuntimeException("Incomparable coordinates: " + coordinate.getClass() + ", " + selectedDep.getClass());
            }
            else if (comparison == Coordinate.VersionOrdering.GREATER_THAN) {
                addVersion(library, coordinate, dependencyPath, coordinateId);

                List<LL<DependencyId>> paths = selectedPaths(library)
                        .orElseThrow()
                        .stream()
                        .map(path -> (LL<DependencyId>) path
                                .prepend(new DependencyId(library, selectedVersion(library).orElseThrow())))
                        .toList();
                deselectOrphans(paths);

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

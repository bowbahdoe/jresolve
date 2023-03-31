package dev.mccue.resolve;


import dev.mccue.resolve.util.LL;

import java.util.*;


final class VersionMap {
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
            var selectedVersion = selectedVersion(library)
                    .map(version -> value.get(library))
                    .map(entry -> entry.versions.get(coordinateId))
                    .orElse(null);

            var comparison = coordinate.compareVersions(selectedVersion);
            if (comparison == Coordinate.VersionComparison.INCOMPARABLE) {
                throw new RuntimeException("Incomparable coordinates");
            }
            else if (comparison == Coordinate.VersionComparison.GREATER_THAN) {
                addVersion(library, coordinate, dependencyPath, coordinateId);
                // TODO
                deselectOrphans(List.of());
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

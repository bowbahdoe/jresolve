package dev.mccue.resolve;


import dev.mccue.resolve.maven.PomDependency;
import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.StackOverflow;
import dev.mccue.resolve.util.LL;
import dev.mccue.resolve.util.Tuple2;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class VersionMap {
    private final HashMap<Library, Entry> value;

    public VersionMap() {
        this.value = new HashMap<>();
    }

    VersionMap(HashMap<Library, Entry> value) {
        this.value = value;
    }

    public record Entry(
            HashMap<CoordinateId, Coordinate> versions,
            HashMap<CoordinateId, HashSet<Path>> paths,
            CoordinateId currentSelection,
            boolean topDep
    ) {
        public Entry() {
            this(new HashMap<>(), new HashMap<>(), null, false);
        }

        public Entry(
                HashMap<CoordinateId, Coordinate> versions,
                HashMap<CoordinateId, HashSet<Path>> paths
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
            Path path,
            CoordinateId coordinateId
    ) {
        var entry = this.value.getOrDefault(library, new Entry());

        entry.versions.put(coordinateId, coordinate);
        entry.paths.computeIfAbsent(coordinateId, k -> new HashSet<>());
        entry.paths.get(coordinateId).add(path);

        this.value.put(library, entry);
    }

    public void selectVersion(Library library, CoordinateId coordinateId, boolean isTop) {
        var entry = this.value.get(library);
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

    public Optional<HashSet<Path>> selectedPaths(Library library) {
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
            Path parentPath
    ) {
        if (parentPath.isEmpty()) {
            return false;
        }

        var path = parentPath.value();
        while (path instanceof LL.Cons<Library> consPath) {
            var lib = consPath.head();
            var checkPath = consPath.tail();
            var vmapEntry = value.get(lib);
            if (vmapEntry != null && vmapEntry.paths()
                    .getOrDefault(vmapEntry.currentSelection, new HashSet<>())
                    .contains(new Path(checkPath))) {
                path = checkPath;
                continue;
            }

            return true;
        }

        return false;
    }

    public void deselectOrphans(
            List<Path> omittedPaths
    ) {

    }

    public InclusionDecision includeCoordinate(
            Library library,
            Coordinate coordinate,
            CoordinateId coordinateId,
            Path path,
            Object exclusions,
            Object config
    ) {
        if (path.isEmpty()) {
            this.addVersion(library, coordinate, path, coordinateId);
            this.selectVersion(library, coordinateId, true);
            return new InclusionDecision(true, Reason.NEW_TOP_DEP);
        }
        // else if (excluded) {}
        else if (this.value.get(library) != null && this.value.get(library).topDep()) {
            return new InclusionDecision(false, Reason.USE_TOP);
        }
        else if (parentMissing(path)) {
            return new InclusionDecision(false, Reason.PARENT_OMITTED);
        }
        else if (selectedVersion(library).isEmpty()) {
            this.addVersion(library, coordinate, path, coordinateId);
            this.selectVersion(library, coordinateId, false);
            return new InclusionDecision(true, Reason.NEW_DEP);
        }
        else if (Objects.equals(selectedVersion(library).orElse(null), coordinateId)) {
            this.addVersion(library, coordinate, path, coordinateId);
            return new InclusionDecision(false, Reason.SAME_VERSION);
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
                addVersion(library, coordinate, path, coordinateId);
                // TODO: deselectOrphans(List.of());
                selectVersion(library, coordinateId, false);

                return new InclusionDecision(true, Reason.NEWER_VERSION);
            }
            else {
                return new InclusionDecision(false, Reason.OLDER_VERSION);
            }
        }
    }

    @Override
    public String toString() {
        return this.value.toString();
    }




}

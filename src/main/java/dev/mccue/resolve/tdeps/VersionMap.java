package dev.mccue.resolve.tdeps;

import java.util.*;

public final class VersionMap {
    private final HashMap<Lib, VersionMap.Entry> value;

    public VersionMap() {
        this.value = new HashMap<>();
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
            Lib lib,
            Coordinate coordinate,
            Path path,
            CoordinateId coordinateId
    ) {
        var entry = this.value.getOrDefault(lib, new Entry());

        entry.versions.put(coordinateId, coordinate);
        entry.paths.computeIfAbsent(coordinateId, k -> new HashSet<>());
        entry.paths.get(coordinateId).add(path);

        this.value.put(lib, entry);
    }

    public void selectVersion(Lib lib, CoordinateId coordinateId, boolean isTop) {
        var entry = this.value.get(lib);
        entry = entry.withSelection(coordinateId);
        if (isTop) {
            entry = entry.asTopDep();
        }
        this.value.put(lib, entry);
    }

    public Optional<CoordinateId> selectedVersion(Lib lib) {
        var entry = this.value.get(lib);
        if (entry == null) {
            return Optional.empty();
        }
        else {
            return Optional.ofNullable(entry.currentSelection);
        }
    }

    public Optional<HashSet<Path>> selectedPaths(Lib lib) {
        var entry = this.value.get(lib);
        if (entry == null) {
            return Optional.empty();
        }
        else {
            var selectedVersion = this.selectedVersion(lib).orElse(null);
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


        // TODO;
        return false;
    }

    public void deselectOrphans(
            List<Path> omittedPaths
    ) {

    }

    public InclusionDecision includeCoord(
            Lib lib,
            Coordinate coordinate,
            CoordinateId coordinateId,
            Path path,
            Object exclusions,
            Object config
    ) {
        return new InclusionDecision(false, Reason.EXCLUDED);
    }

    @Override
    public String toString() {
        return this.value.toString();
    }
}

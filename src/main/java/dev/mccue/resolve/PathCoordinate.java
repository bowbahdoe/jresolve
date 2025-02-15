package dev.mccue.resolve;

import java.nio.file.Path;

record PathCoordinate(Path path)
    implements Coordinate, CoordinateId {
    @Override
    public VersionOrdering compareVersions(Coordinate coordinate) {
        return VersionOrdering.INCOMPARABLE;
    }

    @Override
    public CoordinateId id() {
        return this;
    }

    @Override
    public Manifest getManifest(Cache cache) {
        return Manifest.EMPTY;
    }

    @Override
    public Path getLibraryLocation(Cache cache) {
        return path;
    }

    @Override
    public String toString() {
        return path.toString();
    }
}

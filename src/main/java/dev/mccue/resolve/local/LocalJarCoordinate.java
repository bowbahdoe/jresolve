package dev.mccue.resolve.local;

import dev.mccue.resolve.*;

import java.nio.file.Path;

public record LocalJarCoordinate(Path path) implements Coordinate {
    @Override
    public VersionOrdering compareVersions(Coordinate coordinate) {
        return VersionOrdering.INCOMPARABLE;
    }

    @Override
    public CoordinateId id() {
        return new LocalJarCoordinateId(path);
    }

    @Override
    public Manifest getManifest(Cache cache) {
        return Manifest.EMPTY;
    }

    @Override
    public Path getLibraryLocation(Cache cache) {
        return path;
    }
}

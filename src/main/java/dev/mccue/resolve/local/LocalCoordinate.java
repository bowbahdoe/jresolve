package dev.mccue.resolve.local;

import dev.mccue.resolve.*;

import java.nio.file.Path;

public record LocalCoordinate(Path root) implements Coordinate {
    @Override
    public VersionComparison compareVersions(Coordinate coordinate) {
        return VersionComparison.INCOMPARABLE;
    }

    @Override
    public CoordinateId id() {
        return new LocalCoordinateId(root);
    }

    @Override
    public Manifest getManifest(Library library, Cache cache) {
        return null;
    }
}

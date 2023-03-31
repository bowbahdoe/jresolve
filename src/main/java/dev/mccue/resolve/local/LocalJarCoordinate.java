package dev.mccue.resolve.local;

import dev.mccue.resolve.*;

import java.nio.file.Path;
import java.util.List;

public record LocalJarCoordinate(Path path) implements Coordinate {
    @Override
    public VersionComparison compareVersions(Coordinate coordinate) {
        return VersionComparison.INCOMPARABLE;
    }

    @Override
    public CoordinateId id() {
        return new LocalJarCoordinateId(path);
    }

    @Override
    public Manifest getManifest(Library library, Cache cache) {
        return Manifest.EMPTY;
    }

    @Override
    public Path getLibraryLocation(Library library, Cache cache) {
        return path;
    }
}

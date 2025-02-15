package dev.mccue.resolve;

import java.net.URI;
import java.nio.file.Path;

record URICoordinate(URI uri)
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
        return Path.of(uri);
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}

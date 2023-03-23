package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;

public record MavenCoordinate(
        VersionNumber version,
        MavenRepository repository
) implements Coordinate {

    public MavenCoordinate(String version) {
        this(
                VersionNumber.parse(version).orElseThrow(() -> new IllegalArgumentException("Invalid version: " + version)),
                MavenRepository.MAVEN_CENTRAL
        );
    }

    @Override
    public VersionComparison compareVersions(Coordinate coordinate) {
        if (!(coordinate instanceof MavenCoordinate mavenCoordinate)) {
            return VersionComparison.INCOMPARABLE;
        }
        else {
            return VersionComparison.fromInt(
                    this.version.compareTo(mavenCoordinate.version)
            );
        }
    }

    @Override
    public CoordinateId id() {
        return new MavenCoordinateId(version);
    }

    @Override
    public Manifest getManifest(Library library, Cache cache) {
        var poms = repository.getAllPoms(library, version);

        return null;
    }
}

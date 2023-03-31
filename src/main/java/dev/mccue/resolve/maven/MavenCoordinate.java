package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;

import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public record MavenCoordinate(
        Version version,
        MavenRepository repository
) implements Coordinate {

    public MavenCoordinate(String version) {
        this(version, MavenRepository.MAVEN_CENTRAL
        );
    }

    public MavenCoordinate(String version, MavenRepository repository) {
        this(
                new Version(version),
                repository
        );
    }

    public MavenCoordinate(Version version, Exclusions exclusions) {
        this(
                version,
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
    public MavenCoordinateId id() {
        return new MavenCoordinateId(version);
    }

    List<String> artifactKey(Library library) {
        var uri = repository.getArtifactUri(library, version, Classifier.EMPTY, Extension.JAR);
        return artifactKey(uri);
    }

    static List<String> artifactKey(URI artifactUri) {
        return List.copyOf(Arrays.asList(artifactUri.toASCIIString().split("((:)*/)+")));
    }

    @Override
    public Manifest getManifest(Library library, Cache cache) {
        return repository.getManifest(library, version, cache);
    }

    @Override
    public Path getLibraryLocation(Library library, Cache cache) {
        var key = artifactKey(library);
        return cache.fetchIfAbsent(key, () ->
                repository.getJar(library, version, HttpResponse.BodyHandlers.ofInputStream()));
    }
}

package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;

import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public record MavenCoordinate(
        Version version,
        List<MavenRepository> repositories,
        List<Scope> scopes
) implements Coordinate {

    public MavenCoordinate(
            Version version,
            List<MavenRepository> repositories,
            List<Scope> scopes
    ) {
        this.version = version;
        this.repositories = List.copyOf(repositories);
        if (scopes.isEmpty()) {
            this.scopes = List.of(Scope.COMPILE);
        }
        else {
            this.scopes = List.copyOf(scopes);
        }
    }

    public MavenCoordinate(String version) {
        this(version, RemoteMavenRepository.MAVEN_CENTRAL);
    }

    public MavenCoordinate(String version, MavenRepository repository) {
        this(
                new Version(version),
                List.of(repository),
                List.of()
        );
    }

    public MavenCoordinate(Version version, MavenRepository repository) {
        this(version, List.of(repository), List.of());
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

    List<String> artifactKey(MavenRepository repository, Library library, Classifier classifier, Extension extension) {
        var uri = repository.getArtifactUri(library, version, classifier, extension);
        return artifactKey(uri);
    }

    static List<String> artifactKey(URI artifactUri) {
        return List.copyOf(Arrays.asList(artifactUri.toASCIIString().split("((:)*/)+")));
    }

    @Override
    public Manifest getManifest(Library library, Cache cache) {
        for (var repository : repositories) {
            try {
                repository.getManifest(library, version, cache);
            } catch (LibraryNotFound ignored) {
            }
        }

        throw new LibraryNotFound(library, version);
    }

    @Override
    public Path getLibraryLocation(Library library, Cache cache) {
        for (var repository : repositories) {
            try {
                var key = artifactKey(repository, library, Classifier.EMPTY, Extension.JAR);
                return cache.fetchIfAbsent(key, () -> repository.getFile(
                        library,
                        version,
                        Classifier.EMPTY,
                        Extension.JAR
                ));
            } catch (LibraryNotFound ignored) {
            }
        }

        throw new LibraryNotFound(library, version);
    }

    @Override
    public Optional<Path> getLibrarySourcesLocation(Library library, Cache cache) {
        for (var repository : repositories) {
            try {
                var key = artifactKey(repository, library, Classifier.EMPTY, Extension.JAR);
                return Optional.of(cache.fetchIfAbsent(key, () -> repository.getFile(
                        library,
                        version,
                        Classifier.EMPTY,
                        Extension.JAR
                )));
            } catch (LibraryNotFound ignored) {
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Path> getLibraryDocumentationLocation(Library library, Cache cache) {
        for (var repository : repositories) {
            try {
                var key = artifactKey(repository, library, Classifier.EMPTY, Extension.JAR);
                return Optional.of(cache.fetchIfAbsent(key, () -> repository.getFile(
                        library,
                        version,
                        Classifier.JAVADOC,
                        Extension.JAR
                )));
            } catch (LibraryNotFound ignored) {
            }
        }
        return Optional.empty();
    }
}

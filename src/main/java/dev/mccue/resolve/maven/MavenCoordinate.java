package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;
import dev.mccue.resolve.doc.ToolsDeps;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public record MavenCoordinate(
        Version version,
        List<MavenRepository> repositories,
        List<Scope> scopes,
        @ToolsDeps(
                value = "https://clojurians.slack.com/archives/C0H28NMAS/p1680365565612789?thread_ts=1680362691.333169&cid=C0H28NMAS",
                details = "TDeps makes classifier part of the artifact group/artifact[$classifier]"
        )
        Classifier classifier,
        Classifier sourceClassifier,
        Classifier documentationClassifier
) implements Coordinate {

    public MavenCoordinate(
            Version version,
            List<MavenRepository> repositories,
            List<Scope> scopes,
            Classifier classifier,
            Classifier sourceClassifier,
            Classifier documentationClassifier
    ) {
        this.version = version;
        this.repositories = List.copyOf(repositories);
        if (scopes.isEmpty()) {
            this.scopes = List.of(Scope.COMPILE);
        }
        else {
            this.scopes = List.copyOf(scopes);
        }
        this.classifier = classifier;
        this.sourceClassifier = sourceClassifier;
        this.documentationClassifier = documentationClassifier;
    }

    public MavenCoordinate(
            Version version,
            List<MavenRepository> repositories,
            List<Scope> scopes
    ) {
        this(version, repositories, scopes, Classifier.EMPTY, Classifier.SOURCES, Classifier.JAVADOC);
    }

    public MavenCoordinate(String version) {
        this(version, RemoteMavenRepository.MAVEN_CENTRAL);
    }

    public MavenCoordinate(String version, MavenRepository repository) {
        this(new Version(version), repository);
    }

    public MavenCoordinate(Version version, MavenRepository repository) {
        this(version, List.of(repository), List.of());
    }

    @Override
    public VersionOrdering compareVersions(Coordinate coordinate) {
        if (!(coordinate instanceof MavenCoordinate mavenCoordinate)) {
            return VersionOrdering.INCOMPARABLE;
        }
        else {
            return VersionOrdering.fromInt(
                    this.version.compareTo(mavenCoordinate.version)
            );
        }
    }

    @Override
    public MavenCoordinateId id() {
        return new MavenCoordinateId(version);
    }

    CacheKey artifactKey(MavenRepository repository, Library library, Classifier classifier, Extension extension) {
        var uri = repository.getArtifactUri(library, version, classifier, extension);
        return artifactKey(uri);
    }

    static CacheKey artifactKey(URI artifactUri) {
        return new CacheKey(artifactUri.toASCIIString().split("((:)*/)+"));
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
                        classifier,
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
                var key = artifactKey(repository, library, Classifier.SOURCES, Extension.JAR);
                return Optional.of(cache.fetchIfAbsent(key, () -> repository.getFile(
                        library,
                        version,
                        sourceClassifier,
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
                var key = artifactKey(repository, library, Classifier.JAVADOC, Extension.JAR);
                return Optional.of(cache.fetchIfAbsent(key, () -> repository.getFile(
                        library,
                        version,
                        documentationClassifier,
                        Extension.JAR
                )));
            } catch (LibraryNotFound ignored) {
            }
        }
        return Optional.empty();
    }
}

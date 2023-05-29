package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;
import dev.mccue.resolve.doc.ToolsDeps;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import java.lang.System.Logger.Level;

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
        Classifier documentationClassifier,
        Runtime.Version jdkVersion,
        Os os
) implements Coordinate {
    private static final System.Logger LOG = System.getLogger(MavenCoordinate.class.getName());

    public MavenCoordinate(
            Version version,
            List<MavenRepository> repositories,
            List<Scope> scopes,
            Classifier classifier,
            Classifier sourceClassifier,
            Classifier documentationClassifier,
            Runtime.Version jdkVersion,
            Os os
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
        this.jdkVersion = jdkVersion;
        this.os = os;
    }

    public MavenCoordinate(
            Version version,
            List<MavenRepository> repositories,
            List<Scope> scopes,
            Classifier classifier,
            Classifier sourceClassifier,
            Classifier documentationClassifier
    ) {
        this(
                version,
                repositories,
                scopes,
                classifier,
                sourceClassifier,
                documentationClassifier,
                Runtime.Version.parse(System.getProperty("java.version")),
                new Os()
        );
    }

    public MavenCoordinate(
            Version version,
            List<MavenRepository> repositories,
            List<Scope> scopes
    ) {
        this(
                version,
                repositories,
                scopes,
                Classifier.EMPTY,
                Classifier.SOURCES,
                Classifier.JAVADOC
        );
    }

    public MavenCoordinate(String version) {
        this(version, MavenRepository.MAVEN_CENTRAL);
    }

    public MavenCoordinate(String version, MavenRepository repository) {
        this(new Version(version), List.of(repository));
    }

    public MavenCoordinate(String version, List<MavenRepository> repositories) {
        this(new Version(version), repositories);
    }

    public MavenCoordinate(Version version, MavenRepository repository) {
        this(version, List.of(repository));
    }

    public MavenCoordinate(Version version, List<MavenRepository> repositories) {
        this(version, repositories, List.of());
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
    public CoordinateId id() {
        return new MavenCoordinateId(version);
    }

    @Override
    public Manifest getManifest(Library library, Cache cache) {
        for (var repository : repositories) {
            try {
                return repository.getManifest(library, version, cache, scopes, repositories);
            } catch (LibraryNotFound ignored) {
            }
        }

        throw new LibraryNotFound(library, version);
    }

    @Override
    public Path getLibraryLocation(Library library, Cache cache) {
        for (var repository : repositories) {
            try {
                var key = repository.cacheKey(library, version, classifier, Extension.JAR);
                return cache.fetchIfAbsent(key, () -> repository.getArtifact(
                        library,
                        version,
                        classifier,
                        Extension.JAR
                ));
            } catch (LibraryNotFound e) {
                LOG.log(
                        Level.TRACE,
                        () -> "Could not find artifact in repository. repository=" + repository
                                + ", library=" + library
                                + ", version=" + version
                                + ", classifier=" + classifier
                                + ", cache=" + cache,
                        e
                );
            }
        }

        LOG.log(
                Level.TRACE,
                () -> "Could not find artifact in any checked repository. repositories=" + repositories
                        + ", library=" + library
                        + ", version=" + version
                        + ", classifier=" + classifier
                        + ", cache=" + cache
        );

        throw new LibraryNotFound(library, version);
    }

    @Override
    public Optional<Path> getLibrarySourcesLocation(Library library, Cache cache) {
        for (var repository : repositories) {
            try {
                var key = repository.cacheKey(library, version, sourceClassifier, Extension.JAR);
                return Optional.of(cache.fetchIfAbsent(key, () -> repository.getArtifact(
                        library,
                        version,
                        sourceClassifier,
                        Extension.JAR
                )));
            } catch (LibraryNotFound e) {
                LOG.log(
                        Level.TRACE,
                        () -> "Could not find sources in repository. repository=" + repository
                                + ", library=" + library
                                + ", version=" + version
                                + ", sourceClassifier=" + sourceClassifier
                                + ", cache=" + cache,
                        e
                );
            }
        }

        LOG.log(
                Level.TRACE,
                () -> "Could not find sources in any checked repository. repositories=" + repositories
                        + ", library=" + library
                        + ", version=" + version
                        + ", sourceClassifier=" + sourceClassifier
                        + ", cache=" + cache
        );

        return Optional.empty();
    }

    @Override
    public Optional<Path> getLibraryDocumentationLocation(Library library, Cache cache) {
        for (var repository : repositories) {
            try {
                var key = repository.cacheKey(library, version, documentationClassifier, Extension.JAR);
                return Optional.of(cache.fetchIfAbsent(key, () -> repository.getArtifact(
                        library,
                        version,
                        documentationClassifier,
                        Extension.JAR
                )));
            } catch (LibraryNotFound e) {
                LOG.log(
                        Level.TRACE,
                        () -> "Could not find documentation in repository. repository=" + repository
                                + ", library=" + library
                                + ", version=" + version
                                + ", documentationClassifier=" + documentationClassifier
                                + ", cache=" + cache,
                        e
                );
            }
        }

        LOG.log(
                Level.TRACE,
                () -> "Could not find documentation in any checked repository. repositories=" + repositories
                        + ", library=" + library
                        + ", version=" + version
                        + ", documentationClassifier=" + documentationClassifier
                        + ", cache=" + cache
        );

        return Optional.empty();
    }
}

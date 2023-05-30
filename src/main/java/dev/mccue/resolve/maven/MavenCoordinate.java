package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;
import dev.mccue.resolve.doc.ToolsDeps;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import java.lang.System.Logger.Level;

/**
 *
 * @param version
 * @param repositories
 * @param scopes
 * @param classifier The classifier under which the runtime artifact for the library will be found.
 * @param sourceClassifier
 * @param documentationClassifier
 */
public record MavenCoordinate(
        Group group,
        Artifact artifact,
        Version version,
        List<MavenRepository> repositories,
        List<Scope> scopes,
        Classifier classifier,
        Classifier sourceClassifier,
        Classifier documentationClassifier
) implements Coordinate {
    private static final System.Logger LOG = System.getLogger(MavenCoordinate.class.getName());

    public MavenCoordinate(
            Group group,
            Artifact artifact,
            Version version,
            List<MavenRepository> repositories,
            List<Scope> scopes,
            Classifier classifier,
            Classifier sourceClassifier,
            Classifier documentationClassifier
    ) {
        this.group = group;
        this.artifact = artifact;
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
            Group group,
            Artifact artifact,
            Version version,
            List<MavenRepository> repositories
    ) {
        this(
                group,
                artifact,
                version,
                repositories,
                List.of(),
                Classifier.EMPTY,
                Classifier.SOURCES,
                Classifier.JAVADOC
        );
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
        return new MavenCoordinateId(group, artifact, version);
    }

    @Override
    public Manifest getManifest(Cache cache) {
        for (var repository : repositories) {
            try {
                return repository.getManifest(group, artifact, version, cache, scopes, repositories);
            } catch (ArtifactNotFound ignored) {
            }
        }

        throw new ArtifactNotFound(new Library(group, artifact), version);
    }

    @Override
    public Path getLibraryLocation(Cache cache) {
        for (var repository : repositories) {
            try {
                var key = repository.cacheKey(group, artifact, version, classifier, Extension.JAR);
                return cache.fetchIfAbsent(key, () -> repository.getArtifact(
                        group,
                        artifact,
                        version,
                        classifier,
                        Extension.JAR
                ));
            } catch (ArtifactNotFound e) {
                LOG.log(
                        Level.TRACE,
                        () -> "Could not find artifact in repository. repository=" + repository
                                + ", library=" + new Library(group, artifact)
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
                        + ", library=" + new Library(group, artifact)
                        + ", version=" + version
                        + ", classifier=" + classifier
                        + ", cache=" + cache
        );

        throw new ArtifactNotFound(new Library(group, artifact), version);
    }

    @Override
    public Optional<Path> getLibrarySourcesLocation(Cache cache) {
        for (var repository : repositories) {
            try {
                var key = repository.cacheKey(group, artifact, version, sourceClassifier, Extension.JAR);
                return Optional.of(cache.fetchIfAbsent(key, () -> repository.getArtifact(
                        group,
                        artifact,
                        version,
                        sourceClassifier,
                        Extension.JAR
                )));
            } catch (ArtifactNotFound e) {
                LOG.log(
                        Level.TRACE,
                        () -> "Could not find sources in repository. repository=" + repository
                                + ", library=" + new Library(group, artifact)
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
                        + ", library=" + new Library(group, artifact)
                        + ", version=" + version
                        + ", sourceClassifier=" + sourceClassifier
                        + ", cache=" + cache
        );

        return Optional.empty();
    }

    @Override
    public Optional<Path> getLibraryDocumentationLocation(Cache cache) {
        for (var repository : repositories) {
            try {
                var key = repository.cacheKey(group, artifact, version, documentationClassifier, Extension.JAR);
                return Optional.of(cache.fetchIfAbsent(key, () -> repository.getArtifact(
                        group,
                        artifact,
                        version,
                        documentationClassifier,
                        Extension.JAR
                )));
            } catch (ArtifactNotFound e) {
                LOG.log(
                        Level.TRACE,
                        () -> "Could not find documentation in repository. repository=" + repository
                                + ", library=" + new Library(group, artifact)
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
                        + ", library=" + new Library(group, artifact)
                        + ", version=" + version
                        + ", documentationClassifier=" + documentationClassifier
                        + ", cache=" + cache
        );

        return Optional.empty();
    }
}

package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.lang.System.Logger.Level;

public final class MavenRepository {
    private static final System.Logger LOG =
            System.getLogger(MavenRepository.class.getName());

    public static MavenRepository central() {
        return MavenRepository.MAVEN_CENTRAL;
    }

    public static MavenRepository remote(String url) {
        return new MavenRepository(url);
    }

    public static MavenRepository remote(String url, Supplier<HttpClient> httpClient) {
        return new MavenRepository(url, httpClient);
    }

    public static MavenRepository remote(String url, Consumer<HttpRequest.Builder> enrichRequest) {
        return new MavenRepository(url, enrichRequest);
    }

    public static MavenRepository remote(
            String url,
            Supplier<HttpClient> httpClient,
            Consumer<HttpRequest.Builder> enrichRequest
    ) {
        return new MavenRepository(url, httpClient, enrichRequest);
    }

    public static MavenRepository local() {
        return new MavenRepository(new FileTransport(Path.of(System.getProperty("user.home"), ".m2")));
    }

    public static MavenRepository local(Path path) {
        return new MavenRepository(new FileTransport(path));
    }

    /**
     * Whether this should be considered a "snapshot" repository.
     *
     * <p>
     *     Snapshot repos are distinguished by their contents not being stable. During resolution
     *     and fetching, snapshot repos should have relevant artifacts re-downloaded.
     * </p>
     *
     * <p>
     *     Implementations are allowed to use standard metadata and other such tricks to
     *     avoid re-downloads.
     * </p>
     */

    final boolean isSnapshot;

    static final MavenRepository MAVEN_CENTRAL =
            new MavenRepository("https://repo1.maven.org/maven2/");

    private final Transport transport;

    MavenRepository(String url) {
        this(url, request -> {});
    }

    MavenRepository(String url, Supplier<HttpClient> httpClient) {
        this(url, httpClient, request -> {});
    }

    MavenRepository(String url, Consumer<HttpRequest.Builder> enrichRequest) {
        this(url, HttpClient::newHttpClient, enrichRequest);
    }

    MavenRepository(
            String url,
            Supplier<HttpClient> httpClient,
            Consumer<HttpRequest.Builder> enrichRequest
    ) {
        this.transport = new HttpTransport(url, httpClient, enrichRequest);
        this.isSnapshot = false;
    }

    MavenRepository(Transport transport) {
        this.transport = transport;
        this.isSnapshot = false;
    }


    CacheKey cacheKey(Group group, Artifact artifact, Version version, Classifier classifier, Extension extension) {
        var key = new ArrayList<>(
                this.transport.cachePrefix()
        );

        key.addAll(getArtifactPath(group, artifact, version, classifier, extension));

        return new CacheKey(key);
    }


    InputStream getArtifact(
            Group group,
            Artifact artifact,
            Version version,
            Classifier classifier,
            Extension extension
    ) throws ArtifactNotFound {
        LOG.log(
                Level.TRACE,
                () -> "About to get artifact. group=" + group +
                        ", artifact=" + artifact +
                        ", version=" + version +
                        ", classifier=" + classifier +
                        ", extension=" + extension +
                        ", transport=" + this.transport
        );

        var getFileResult = transport.getFile(
                getArtifactPath(group, artifact, version, classifier, extension)
        );

        switch (getFileResult) {
            case Transport.GetFileResult.Success success -> {
                var inputStream = success.inputStream();
                LOG.log(
                        Level.TRACE,
                        () -> "Successfully got file for artifact. group=" + group +
                              ", artifact=" + artifact +
                              ", version=" + version +
                              ", classifier=" + classifier +
                              ", extension=" + extension +
                              ", transport=" + this.transport
                );
                return inputStream;
            }
            case Transport.GetFileResult.NotFound notFound -> {
                LOG.log(
                        Level.TRACE,
                        () -> "Did not find file for artifact. group=" + group +
                              ", artifact=" + artifact +
                              ", version=" + version +
                              ", classifier=" + classifier +
                              ", extension=" + extension +
                              ", transport=" + this.transport
                );
                throw new ArtifactNotFound(group, artifact, version);
            }
            case Transport.GetFileResult.Error error -> {
                var e = error.throwable();
                LOG.log(
                        Level.TRACE,
                        () -> "Encountered error getting file for metadata. group=" + group +
                              ", artifact=" + artifact +
                              ", version=" + version +
                              ", classifier=" + classifier +
                              ", extension=" + extension +
                              ", transport=" + this.transport,
                        e
                );
                throw new RuntimeException(e);
            }
            case null, default -> throw new IllegalStateException();
        }
    }

    InputStream getMetadata(Group group, Artifact artifact) {
        LOG.log(
                Level.TRACE,
                () -> "About to get metadata. group=" + group +
                        ", artifact=" + artifact +
                        ", transport=" + this.transport
        );

        var getFileResult = transport.getFile(
                getMetadataPath(group, artifact)
        );

        switch (getFileResult) {
            case Transport.GetFileResult.Success success -> {
                var inputStream = success.inputStream();
                LOG.log(
                        Level.TRACE,
                        () -> "Successfully got file for metadata. group=" + group +
                              ", artifact=" + artifact +
                              ", transport=" + this.transport
                );

                return inputStream;
            }
            case Transport.GetFileResult.NotFound __ -> {
                LOG.log(
                        Level.TRACE,
                        () -> "Did not find file for metadata. group=" + group +
                              ", artifact=" + artifact +
                              ", transport=" + this.transport
                );

                throw new ArtifactNotFound(group, artifact);
            }
            case Transport.GetFileResult.Error error -> {
                var e = error.throwable();
                LOG.log(
                        Level.TRACE,
                        () -> "Encountered error getting file for metadata. group=" + group +
                              ", artifact=" + artifact +
                              ", transport=" + this.transport,
                        e
                );

                throw new RuntimeException(e);
            }
        }
    }

    static List<String> getArtifactPath(
            Group group,
            Artifact artifact,
            Version version,
            Classifier classifier,
            Extension extension
    ) {

        var path = new ArrayList<>(Arrays.asList(group
                .value().split("\\.")));

        path.add(artifact.value());

        path.add(version.toString());

        path.add(
                artifact
                        + "-"
                        + version
                        + (!classifier.equals(Classifier.EMPTY) ? ("-" + classifier.value()) : "")
                        + ((!extension.equals(Extension.EMPTY)) ? "." + extension : "")
        );

        return List.copyOf(path);
    }


    static List<String> getMetadataPath(Group group, Artifact artifact) {
        var path = new ArrayList<>(Arrays.asList(group
                .value().split("\\.")));

        path.add(artifact.value());
        path.add("maven-metadata.xml");

        return List.copyOf(path);
    }

    PomInfo getPomInfo(Group group, Artifact artifact, Version version, Cache cache) throws ArtifactNotFound {
        LOG.log(
                Level.TRACE,
                () -> "About to fetch pom file. group=" + group +
                        ", artifact=" + artifact +
                        ", version=" + version +
                        ", cache=" + cache
        );

        var key = cacheKey(group, artifact, version, Classifier.EMPTY, Extension.POM);
        try {
            if (cache == null) {
                try (var data = getArtifact(group, artifact, version, Classifier.EMPTY, Extension.POM)) {
                    return PomParser.parse(new String(data.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
            else {
                var pomPath = cache.fetchIfAbsent(key, () ->
                        getArtifact(group, artifact, version, Classifier.EMPTY, Extension.POM)
                );

                try (var data = Files.newInputStream(pomPath)) {
                    return PomParser.parse(new String(data.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    Optional<PomInfo> getParentPomInfo(PomInfo pomInfo, Cache cache) {
        if (pomInfo.parent() instanceof PomParent.Declared declaredPom) {
            return Optional.of(
                    getPomInfo(
                            new Group(declaredPom.groupId().value()),
                            new Artifact(declaredPom.artifactId().value()),
                            new Version(declaredPom.version().value()),
                            cache
                    )
            );
        }
        else {
            return Optional.empty();
        }
    }

    ChildHavingPomInfo getAllPoms(Group group, Artifact artifact, Version version, Cache cache) {
        var poms = new ArrayList<PomInfo>();
        var pom = getPomInfo(group, artifact, version, cache);
        poms.add(pom);
        while (true) {
            var parentPom = getParentPomInfo(poms.get(poms.size() - 1), cache).orElse(null);
            if (parentPom == null) {
                break;
            }
            poms.add(parentPom);
        }

        var iterator = poms.iterator();
        var currentPom = iterator.next();
        ChildHavingPomInfo childHavingPomInfo = new ChildHavingPomInfo(
                currentPom.groupId(),
                currentPom.artifactId(),
                currentPom.version(),
                currentPom.dependencies(),
                currentPom.dependencyManagement(),
                currentPom.properties(),
                currentPom.packaging(),
                Optional.empty()
        );

        while (iterator.hasNext()) {
            var parentPom = iterator.next();
            childHavingPomInfo = new ChildHavingPomInfo(
                    parentPom.groupId(),
                    parentPom.artifactId(),
                    parentPom.version(),
                    parentPom.dependencies(),
                    parentPom.dependencyManagement(),
                    parentPom.properties(),
                    parentPom.packaging(),
                    Optional.of(childHavingPomInfo)
            );
        }

        return childHavingPomInfo;
    }

    PomManifest getManifest(
            Group group,
            Artifact artifact,
            Version version,
            Cache cache,
            List<Scope> scopes,
            List<MavenRepository> childRepositories,
            Runtime.Version jdkVersion,
            Os os
    ) {
        var effectivePom = EffectivePomInfo.from(
                getAllPoms(group, artifact, version, cache),
                jdkVersion,
                os
        );
        return PomManifest.from(
                effectivePom.resolveImports(this, cache, jdkVersion, os),
                scopes,
                (depGroup, depArtifact, depVersion, defaultClassifier) -> new MavenCoordinate(
                        depGroup,
                        depArtifact,
                        depVersion,
                        childRepositories,
                        scopes,
                        defaultClassifier,
                        Classifier.SOURCES,
                        Classifier.JAVADOC,
                        jdkVersion,
                        os

                )
        ).normalize(cache);
    }

    MavenMetadata getMavenMetadata(Group group, Artifact artifact) throws IOException {
        return MavenMetadata.parseXml(new String(getMetadata(group, artifact).readAllBytes(), StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        return "MavenRepository[transport=" + transport + "]";
    }
}

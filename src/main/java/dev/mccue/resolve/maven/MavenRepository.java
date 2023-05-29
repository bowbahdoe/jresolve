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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class MavenRepository {
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
        return new MavenRepository(new FileTransport(Path.of(System.getProperty("user.dir"), ".m2")));
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
        this(url, HttpClient::newHttpClient);
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


    CacheKey cacheKey(Library library, Version version, Classifier classifier, Extension extension) {
        var key = new ArrayList<>(
                this.transport.cachePrefix()
        );

        key.addAll(getArtifactPath(library, version, classifier, extension));

        return new CacheKey(key);
    }


    InputStream getArtifact(
            Library library,
            Version version,
            Classifier classifier,
            Extension extension
    ) throws LibraryNotFound {
        return switch (transport.getFile(
                getArtifactPath(library, version, classifier, extension)
        )) {
            case Transport.GetFileResult.Success(InputStream inputStream) -> inputStream;
            case Transport.GetFileResult.NotFound __ -> throw new LibraryNotFound(library, version);
            case Transport.GetFileResult.Error(Throwable e) -> throw new RuntimeException(e);
        };
    }


    InputStream getMetadata(Library library) {
        return switch (transport.getFile(
                getMetadataPath(library)
        )) {
            case Transport.GetFileResult.Success(InputStream inputStream) -> inputStream;
            case Transport.GetFileResult.NotFound __ -> throw new LibraryNotFound(library);
            case Transport.GetFileResult.Error(Throwable e) -> throw new RuntimeException(e);
        };
    }

    static List<String> getArtifactPath(
            Library library,
            Version version,
            Classifier classifier,
            Extension extension
    ) {

        var path = new ArrayList<>(Arrays.asList(library.group()
                .value().split("\\.")));

        path.add(library.artifact().value());

        path.add(version.toString());

        path.add(
                library.artifact()
                        + (!classifier.equals(Classifier.EMPTY) ? ("-" + classifier.value()) : "")
                        + "-"
                        + version
                        + ((!extension.equals(Extension.EMPTY)) ? "." + extension : "")
        );

        return List.copyOf(path);
    }


    static List<String> getMetadataPath(Library library) {
        var path = new ArrayList<>(Arrays.asList(library.group()
                .value().split("\\.")));

        path.add(library.artifact().value());
        path.add("maven-metadata.xml");

        return List.copyOf(path);
    }

    final PomInfo getPomInfo(Library library, Version version, Cache cache) throws LibraryNotFound {
        var key = cacheKey(library, version, Classifier.EMPTY, Extension.POM);
        var pomPath = cache.fetchIfAbsent(key, () ->
                getArtifact(library, version, Classifier.EMPTY, Extension.POM)
        );

        try (var data = Files.newInputStream(pomPath)) {
            return PomParser.parse(new String(data.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    Optional<PomInfo> getParentPomInfo(PomInfo pomInfo, Cache cache) {
        if (pomInfo.parent() instanceof PomParent.Declared declaredPom) {
            return Optional.of(
                    getPomInfo(
                            new Library(
                                    new Group(declaredPom.groupId().value()),
                                    new Artifact(declaredPom.artifactId().value())
                            ),
                            new Version(declaredPom.version().value()),
                            cache
                    )
            );
        }
        else {
            return Optional.empty();
        }
    }

    ChildHavingPomInfo getAllPoms(Library library, Version version, Cache cache) {
        var poms = new ArrayList<PomInfo>();
        var pom = getPomInfo(library, version, cache);
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
            Library library,
            Version version,
            Cache cache,
            List<Scope> scopes,
            List<MavenRepository> childRepositories
    ) {
        var effectivePom = EffectivePomInfo.from(getAllPoms(library, version, cache));
        return PomManifest.from(
                effectivePom.resolveImports(this, cache),
                scopes,
                (depVersion, defaultClassifier) -> new MavenCoordinate(
                        depVersion,
                        childRepositories,
                        scopes,
                        defaultClassifier,
                        Classifier.SOURCES,
                        Classifier.JAVADOC
                )
        ).normalize(cache);
    }

    PomManifest getManifest(Library library,
                                  Version version,
                                  Cache cache,
                                  List<MavenRepository> childRepositories) {
        return getManifest(library, version, cache, List.of(Scope.COMPILE), childRepositories);
    }

    PomManifest getManifest(Library library,
                                  Version version,
                                  Cache cache) {
        return getManifest(library, version, cache, List.of(Scope.COMPILE), List.of(this));
    }


    MavenMetadata getMavenMetadata(Library library) throws IOException {
        return MavenMetadata.parseXml(new String(getMetadata(library).readAllBytes(), StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        return "MavenRepository[transport=" + transport + "]";
    }
}

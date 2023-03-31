package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;
import dev.mccue.resolve.doc.Rife;
import dev.mccue.resolve.util.Lazy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Rife(
        value = "https://github.com/rife2/rife2/blob/main/lib/src/main/java/rife/bld/dependencies/Repository.java",
        details = """
        Renamed from Repository to MavenRepository.
        Changed from record to class
        """
)
public final class MavenRepository {
    public static final MavenRepository MAVEN_CENTRAL =
            new MavenRepository("https://repo1.maven.org/maven2/");
    public static final MavenRepository SONATYPE_SNAPSHOTS =
            new MavenRepository("https://s01.oss.sonatype.org/content/repositories/snapshots/");
    public static final MavenRepository CLOJARS =
            new MavenRepository("https://clojars.org/repo");

    private final String url;
    private final Lazy<HttpClient> httpClient;
    private final Consumer<HttpRequest.Builder> enrichRequest;

    public MavenRepository(String url) {
        this(url, HttpClient::newHttpClient);
    }

    public MavenRepository(String url, Supplier<HttpClient> httpClient) {
        this(url, httpClient, request -> {});
    }

    public MavenRepository(String url, Consumer<HttpRequest.Builder> enrichRequest) {
        this(url, HttpClient::newHttpClient, enrichRequest);
    }

    public MavenRepository(
            String url,
            Supplier<HttpClient> httpClient,
            Consumer<HttpRequest.Builder> enrichRequest
    ) {
        this.url = url;
        this.httpClient = new Lazy<>(httpClient);
        this.enrichRequest = enrichRequest;
    }


    public URI getArtifactUri(
            Library library,
            Version version,
            Classifier classifier,
            Extension extension
    ) {
        var groupPath = library
                .group()
                .toString()
                .replace(".", "/");
        var result = new StringBuilder(url);
        if (!url.endsWith("/")) {
            result.append("/");
        }
        result
                .append(groupPath)
                .append("/")
                .append(library.artifact())
                .append("/")
                .append(version)
                .append("/")
                .append(library.artifact());

        if (!classifier.equals(Classifier.EMPTY)) {
            result.append("-");
            result.append(classifier.value());
        }

        result
                .append("-")
                .append(version);

        if (!extension.equals(Extension.EMPTY)) {
            result.append(".");
            result.append(extension);
        }

        return URI.create(result.toString());
    }

    public PomInfo getPomInfo(Library library, Version version, Cache cache) {
        var uri = getArtifactUri(library, version, Classifier.EMPTY, Extension.POM);

        var key = MavenCoordinate.artifactKey(uri);

        var pomPath = cache.fetchIfAbsent(key, () -> {
            var requestBuilder =
                    HttpRequest.newBuilder()
                            .GET()
                            .uri(uri);
            this.enrichRequest.accept(requestBuilder);
            var httpClient = this.httpClient.get();

            try {
                var response = httpClient.send(
                        requestBuilder.build(),
                        HttpResponse.BodyHandlers.ofInputStream()
                );
                return response.body();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        try (var data = Files.newInputStream(pomPath)) {
            return PomParser.parse(new String(data.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Optional<PomInfo> getParentPomInfo(PomInfo pomInfo, Cache cache) {
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

    public ChildHavingPomInfo getAllPoms(Library library, Version version, Cache cache) {
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

    public PomManifest getManifest(Library library, Version version, Cache cache, List<Scope> scopes) {
        var effectivePom = EffectivePomInfo.from(getAllPoms(library, version, cache));
        return PomManifest.from(
                effectivePom,
                scopes,
                (depVersion, depExclusions) -> new MavenCoordinate(depVersion, this)
        );
    }

    public PomManifest getManifest(Library library, Version version, Cache cache) {
        return getManifest(library, version, cache, List.of());
    }

    public <T> T getJar(
            Library library,
            Version version,
            HttpResponse.BodyHandler<T> bodyHandler
    ) {
        var requestBuilder =
                HttpRequest.newBuilder()
                        .GET()
                        .uri(getArtifactUri(library, version, Classifier.EMPTY, Extension.POM));
        this.enrichRequest.accept(requestBuilder);
        var httpClient = this.httpClient.get();

        try {
            var response = httpClient.send(
                    requestBuilder.build(),
                    bodyHandler
            );
            return response.body();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "MavenRepository[" + url + "]";
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        StandardCache cache = new StandardCache(Path.of("./libs"));
        var pomInfo = MAVEN_CENTRAL
                .getPomInfo(
                        new Library("org.clojure", "clojure"),
                        new Version("1.11.1"),
                        cache
                );

        var batik = MAVEN_CENTRAL.getAllPoms(
                new Library("org.apache.xmlgraphics", "batik-transcoder"),
                new Version("1.7"),
                cache
        );

        System.out.println(batik);

        // has parent
        var vaadin = MAVEN_CENTRAL.getAllPoms(
                new Library("com.vaadin", "vaadin"),
                new Version("23.3.7"),
                cache
        );

        System.out.println(vaadin);

        /*
                var mavenCore = MAVEN_CENTRAL.getManifest(
                new Library("org.apache.maven", "maven-core"),
                new Version("3.9.0")
        );

        System.out.println(MAVEN_CENTRAL.getManifest(
                new Library("com.vaadin", "vaadin"),
                new Version("23.3.7")
        ));

        System.out.println(MAVEN_CENTRAL.getManifest(
                new Library("com.vaadin", "vaadin"),
                new Version("23.3.7"),
                List.of(Scope.COMPILE)
        ));

        System.out.println(MAVEN_CENTRAL.getManifest(
                new Library("org.apache.maven", "maven-core"),
                new Version("3.9.0")
        ));
         */



    }
}

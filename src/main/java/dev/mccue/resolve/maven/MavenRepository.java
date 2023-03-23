package dev.mccue.resolve.maven;

import dev.mccue.resolve.Extension;
import dev.mccue.resolve.Library;
import dev.mccue.resolve.VersionNumber;
import dev.mccue.resolve.doc.Rife;
import dev.mccue.resolve.util.Lazy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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
            VersionNumber versionNumber,
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
                .append(versionNumber)
                .append("/")
                .append(library.artifact())
                .append("-")
                .append(versionNumber);

        if (!extension.equals(Extension.EMPTY)) {
            result.append(".");
            result.append(extension);
        }

        return URI.create(result.toString());
    }

    public PomInfo getPomInfo(Library library, VersionNumber versionNumber) {
        var requestBuilder =
                HttpRequest.newBuilder()
                        .GET()
                        .uri(getArtifactUri(library, versionNumber, Extension.POM));
        this.enrichRequest.accept(requestBuilder);
        var httpClient = this.httpClient.get();

        try {
            var response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            return PomParser.parse(response.body());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<PomInfo> getParentPomInfo(PomInfo pomInfo) {
        var parent = pomInfo.parent().orElse(null);
        if (parent == null) {
            return Optional.empty();
        }
        else {
            return Optional.of(
                    getPomInfo(parent.first(), VersionNumber.parse(parent.second()).orElseThrow())
            );
        }
    }

    public List<PomInfo> getAllPoms(Library library, VersionNumber versionNumber) {
        var poms = new ArrayList<PomInfo>();
        var pom = getPomInfo(library, versionNumber);
        poms.add(pom);
        while (true) {
            var parentPom = getParentPomInfo(poms.get(poms.size() - 1)).orElse(null);
            if (parentPom == null) {
                break;
            }
            poms.add(parentPom);
        }
        return List.copyOf(poms);
    }

    public <T> T getJar(
            Library library,
            VersionNumber versionNumber,
            HttpResponse.BodyHandler<T> bodyHandler
    ) {
        var requestBuilder =
                HttpRequest.newBuilder()
                        .GET()
                        .uri(getArtifactUri(library, versionNumber, Extension.POM));
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

    public static void main(String[] args) throws IOException, InterruptedException {
        var pomInfo = MAVEN_CENTRAL
                .getPomInfo(
                        new Library("org.clojure", "clojure"),
                        VersionNumber.parse("1.11.1").orElseThrow()
                );

        var response = MAVEN_CENTRAL.getAllPoms(
                new Library("org.clojure", "clojure"),
                VersionNumber.parse("1.11.1").orElseThrow()
        );

        System.out.println(response.size());
    }
}

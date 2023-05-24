package dev.mccue.resolve.maven;

import dev.mccue.resolve.Library;
import dev.mccue.resolve.Version;
import dev.mccue.resolve.doc.Rife;
import dev.mccue.resolve.util.Lazy;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Rife(
        value = "https://github.com/rife2/rife2/blob/main/lib/src/main/java/rife/bld/dependencies/Repository.java",
        details = """
        Renamed from Repository to MavenRepository.
        Changed from record to class
        """
)
final class RemoteMavenRepository extends MavenRepository {
    static final RemoteMavenRepository MAVEN_CENTRAL =
            new RemoteMavenRepository("https://repo1.maven.org/maven2/");

    private final String url;
    private final Lazy<HttpClient> httpClient;
    private final Consumer<HttpRequest.Builder> enrichRequest;

    public RemoteMavenRepository(String url) {
        this(url, HttpClient::newHttpClient);
    }

    public RemoteMavenRepository(String url, Supplier<HttpClient> httpClient) {
        this(url, httpClient, request -> {});
    }

    public RemoteMavenRepository(String url, Consumer<HttpRequest.Builder> enrichRequest) {
        this(url, HttpClient::newHttpClient, enrichRequest);
    }

    public RemoteMavenRepository(
            String url,
            Supplier<HttpClient> httpClient,
            Consumer<HttpRequest.Builder> enrichRequest
    ) {
        this.url = url;
        this.httpClient = new Lazy<>(httpClient);
        this.enrichRequest = enrichRequest;
    }

    URI getArtifactUri(
            Library library,
            Version version,
            Classifier classifier,
            Extension extension
    ) {
        var result = new StringBuilder(url);
        if (!url.endsWith("/")) {
            result.append("/");
        }

        return getArtifactUri(result, library, version, classifier, extension);
    }

    @Override
    URI getMetadataUri(
            Library library
    ) {
        var result = new StringBuilder(url);
        if (!url.endsWith("/")) {
            result.append("/");
        }

        return getMetadataUri(result, library);
    }


    @Override
    InputStream getFile(Library library, Version version, Classifier classifier, Extension extension) throws LibraryNotFound {
        var requestBuilder =
                HttpRequest.newBuilder()
                        .GET()
                        .uri(getArtifactUri(library, version, classifier, extension));
        this.enrichRequest.accept(requestBuilder);
        var httpClient = this.httpClient.get();

        try {
            var response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream()
            );
            System.out.println(library + " ---- " + response.statusCode());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                if (response.statusCode() == 404) {
                    throw new LibraryNotFound(library, version);
                }
                System.out.println(response.statusCode());
                throw new RuntimeException();
            }
            return response.body();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    InputStream getMetadata(Library library) {
        var requestBuilder =
                HttpRequest.newBuilder()
                        .GET()
                        .uri(getMetadataUri(library));
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
    }

    @Override
    public String toString() {
        return "MavenRepository[" + url + "]";
    }
}

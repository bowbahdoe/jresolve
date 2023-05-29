package dev.mccue.resolve.maven;

import dev.mccue.resolve.util.Lazy;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class HttpTransport implements Transport {
    private static final System.Logger LOG =
            System.getLogger(HttpTransport.class.getName());

    private final String url;
    private final Lazy<HttpClient> httpClient;
    private final Consumer<HttpRequest.Builder> enrichRequest;

    public HttpTransport(String url) {
        this(url, HttpClient::newHttpClient);
    }

    public HttpTransport(String url, Supplier<HttpClient> httpClient) {
        this(url, httpClient, request -> {});
    }

    public HttpTransport(String url, Consumer<HttpRequest.Builder> enrichRequest) {
        this(url, HttpClient::newHttpClient, enrichRequest);
    }

    public HttpTransport(
            String url,
            Supplier<HttpClient> httpClient,
            Consumer<HttpRequest.Builder> enrichRequest
    ) {
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        this.url = url;
        this.httpClient = new Lazy<>(httpClient);
        this.enrichRequest = enrichRequest;
    }

    @Override
    public List<String> cachePrefix() {
        var url = this.url;
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        return List.copyOf(Arrays.asList(url.split("((:)*/)+")));
    }

    @Override
    public GetFileResult getFile(List<String> pathElements) {
        var path = this.url + String.join("/", pathElements);

        var requestBuilder =
                HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(path));
        this.enrichRequest.accept(requestBuilder);

        var httpClient = this.httpClient.get();

        LOG.log(
                System.Logger.Level.TRACE,
                () -> "About to download file. path="
                        + path
                        + ", repository="
                        + this
        );

        try {
            var response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            LOG.log(
                    System.Logger.Level.TRACE,
                    () -> "Got response for file. statusCode="
                            + response.statusCode()
                            + ", path="
                            + path
                            + ", repository="
                            + this
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                if (response.statusCode() == 404) {
                    return new GetFileResult.NotFound();
                }
                new GetFileResult.Error(new IOException("Bad status code: statusCode=" + response.statusCode()));
            }
            return new GetFileResult.Success(response.body());
        } catch (IOException | InterruptedException e) {
            return new GetFileResult.Error(e);
        }
    }

    @Override
    public String toString() {
        return "HttpTransport[" + url + "]";
    }
}

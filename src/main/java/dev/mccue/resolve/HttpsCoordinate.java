package dev.mccue.resolve;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Arrays;

record HttpsCoordinate(HttpClient httpClient, URI httpsUrl)
        implements Coordinate, CoordinateId {

    HttpsCoordinate {
        if (!(httpsUrl.getScheme().equals("https"))) {
            throw new IllegalArgumentException("Must be an https url");
        }
    }

    @Override
    public VersionOrdering compareVersions(Coordinate coordinate) {
        return VersionOrdering.INCOMPARABLE;
    }

    @Override
    public CoordinateId id() {
        return this;
    }

    @Override
    public Manifest getManifest(Cache cache) {
        return Manifest.EMPTY;
    }

    private CacheKey uriToCacheKey(URI uri) {
        var url = uri.toString();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        return new CacheKey(Arrays.asList(url.split("((:)*/)+")));
    }

    @Override
    public Path getLibraryLocation(Cache cache) {
        var cacheKey = uriToCacheKey(httpsUrl);
        return cache.fetch(cacheKey, () -> {
            try {
                var response = httpClient.send(
                        HttpRequest.newBuilder(httpsUrl)
                                .build(),
                        HttpResponse.BodyHandlers.ofInputStream());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("Bad status code: " + response.statusCode());
                }
                return response.body();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

    }

    @Override
    public String toString() {
        return httpsUrl.toString();
    }
}

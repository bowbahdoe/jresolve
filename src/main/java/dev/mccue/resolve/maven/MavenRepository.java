package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public sealed abstract class MavenRepository
        permits RemoteMavenRepository, LocalMavenRepository {
    public static MavenRepository central() {
        return RemoteMavenRepository.MAVEN_CENTRAL;
    }

    public static MavenRepository clojars() {
        return RemoteMavenRepository.CLOJARS;
    }

    public static MavenRepository remote(String url) {
        return new RemoteMavenRepository(url);
    }

    public static MavenRepository remote(String url, Supplier<HttpClient> httpClient) {
        return new RemoteMavenRepository(url, httpClient);
    }

    public static MavenRepository remote(String url, Consumer<HttpRequest.Builder> enrichRequest) {
        return new RemoteMavenRepository(url, enrichRequest);
    }

    public static MavenRepository remote(
            String url,
            Supplier<HttpClient> httpClient,
            Consumer<HttpRequest.Builder> enrichRequest
    ) {
        return new RemoteMavenRepository(url, httpClient, enrichRequest);
    }

    public static MavenRepository local() {
        return new LocalMavenRepository();
    }

    public static MavenRepository local(Path path) {
        return new LocalMavenRepository(path);
    }

    abstract URI getArtifactUri(
            Library library,
            Version version,
            Classifier classifier,
            Extension extension
    );

    final URI getArtifactUri(
            StringBuilder result,
            Library library,
            Version version,
            Classifier classifier,
            Extension extension
    ) {

        var groupPath = library
                .group()
                .toString()
                .replace(".", "/");

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

    abstract URI getMetadataUri(
            Library library
    );

    final URI getMetadataUri(
            StringBuilder result,
            Library library
    ) {

        var groupPath = library
                .group()
                .toString()
                .replace(".", "/");

        result
                .append(groupPath)
                .append("/")
                .append(library.artifact())
                .append("/")
                .append("maven-metadata.xml");

        return URI.create(result.toString());
    }

    final PomInfo getPomInfo(Library library, Version version, Cache cache) throws LibraryNotFound {
        var uri = getArtifactUri(library, version, Classifier.EMPTY, Extension.POM);
        var key = MavenCoordinate.artifactKey(uri);
        var pomPath = cache.fetchIfAbsent(key, () ->
                getFile(library, version, Classifier.EMPTY, Extension.POM)
        );

        try (var data = Files.newInputStream(pomPath)) {
            return PomParser.parse(new String(data.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    final Optional<PomInfo> getParentPomInfo(PomInfo pomInfo, Cache cache) {
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

    final ChildHavingPomInfo getAllPoms(Library library, Version version, Cache cache) {
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

    final PomManifest getManifest(Library library, Version version, Cache cache, List<Scope> scopes) {
        var effectivePom = EffectivePomInfo.from(getAllPoms(library, version, cache));
        return PomManifest.from(
                effectivePom,
                scopes,
                (depVersion, depExclusions) -> new MavenCoordinate(depVersion, this)
        ).normalize(cache);
    }

    final PomManifest getManifest(Library library, Version version, Cache cache) {
        return getManifest(library, version, cache, List.of());
    }

    abstract InputStream getFile(
            Library library,
            Version version,
            Classifier classifier,
            Extension extension
    ) throws LibraryNotFound;

    abstract InputStream getMetadata(
            Library library
    );

    MavenMetadata getMavenMetadata(Library library) throws IOException {
        return MavenMetadata.parseXml(new String(getMetadata(library).readAllBytes(), StandardCharsets.UTF_8));
    }
}

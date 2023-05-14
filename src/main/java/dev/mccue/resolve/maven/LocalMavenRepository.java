package dev.mccue.resolve.maven;

import dev.mccue.resolve.Classifier;
import dev.mccue.resolve.Library;
import dev.mccue.resolve.Version;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LocalMavenRepository extends MavenRepository {
    private final Path root;

    public LocalMavenRepository(Path root) {
        this.root = root;
    }

    public LocalMavenRepository() {
        this(Path.of(System.getProperty("user.home"), ".m2"));
    }

    @Override
    URI getArtifactUri(
            Library library,
            Version version,
            Classifier classifier,
            Extension extension
    ) {
        var result = new StringBuilder("file:/");
        return getArtifactUri(result, library, version, classifier, extension);
    }

    @Override
    URI getMetadataUri(Library library) {
        var result = new StringBuilder("file:/");
        return getMetadataUri(result, library);
    }

    @Override
    InputStream getFile(Library library, Version version, Classifier classifier, Extension extension) throws LibraryNotFound {
        try {
            return Files.newInputStream(Path.of(getArtifactUri(library, version, classifier, extension)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    InputStream getMetadata(Library library) {
        try {
            return Files.newInputStream(Path.of(getMetadataUri(library)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public String toString() {
        return "MavenRepository[" + this.root + "]";
    }
}

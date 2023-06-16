package dev.mccue.resolve.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalLong;

public final class FileTransport implements Transport {
    private final Path root;

    public FileTransport(Path root) {
        this.root = root;
    }


    @Override
    public List<String> cachePrefix() {
        return List.copyOf(Arrays.asList(this.root.toString().split(File.pathSeparator)));
    }

    @Override
    public GetFileResult getFile(List<String> pathElements) {
        try {
            return new GetFileResult.Success(
                    Files.newInputStream(Path.of(
                            root.toString(),
                            pathElements.toArray(String[]::new)
                    )),
                    OptionalLong.empty()
            );
        } catch (NoSuchFileException e) {
            return new GetFileResult.NotFound();
        } catch (IOException e) {
            return new GetFileResult.Error(e);
        }
    }
}

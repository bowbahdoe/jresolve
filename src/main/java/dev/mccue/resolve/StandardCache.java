package dev.mccue.resolve;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public record StandardCache(Path root) implements Cache {
    public StandardCache() {
        this(java.nio.file.Path.of(System.getProperty("user.home"), ".jresolve"));
    }

    private Path keyPath(List<String> key) {
        return java.nio.file.Path.of(
                root.toString(),
                key.toArray(String[]::new)
        );
    }

    @Override
    public String toString() {
        return "StandardCache";
    }

    @Override
    public Path fetchIfAbsent(List<String> key, Supplier<InputStream> data) {
        var filePath = keyPath(key);
        if (!Files.exists(filePath)) {
            try {
                Files.createDirectories(filePath.getParent());

                try (var outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW)) {
                    data.get().transferTo(outputStream);
                    return filePath;
                } catch (FileAlreadyExistsException e) {
                    return filePath;
                }

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        return filePath;
    }
}

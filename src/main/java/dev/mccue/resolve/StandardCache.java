package dev.mccue.resolve;

import dev.mccue.resolve.doc.Gold;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Gold
final class StandardCache implements Cache {
    private final Path root;

    public StandardCache(Path root) {
        Objects.requireNonNull(root);
        this.root = root;
    }

    public StandardCache() {
        this(Path.of(System.getProperty("user.home"), ".jresolve"));
    }

    private Path keyPath(List<String> key) {
        return Path.of(
                root.toString(),
                key.toArray(String[]::new)
        );
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StandardCache standardCache
                && this.root.equals(standardCache.root);
    }

    @Override
    public int hashCode() {
        return this.root.hashCode();
    }

    @Override
    public String toString() {
        return "StandardCache[root=" + root + "]";
    }

    @Override
    public Path fetchIfAbsent(CacheKey key, Supplier<InputStream> data) {
        var filePath = keyPath(key.components());
        if (!Files.exists(filePath)) {
            try {
                Files.createDirectories(filePath.getParent());

                try (var outputStream = Files.newOutputStream(
                        filePath,
                        StandardOpenOption.CREATE_NEW
                )) {
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

    @Override
    public Path fetch(CacheKey key, Supplier<InputStream> data) {
        return null;
    }
}

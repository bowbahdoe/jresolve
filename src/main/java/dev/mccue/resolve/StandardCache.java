package dev.mccue.resolve;

import dev.mccue.resolve.doc.Gold;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.System.Logger.Level;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.function.Supplier;

@Gold
final class StandardCache implements Cache {
    private static final System.Logger LOG = System.getLogger(StandardCache.class.getName());

    private final Path root;

    public StandardCache(Path root) {
        Objects.requireNonNull(root);
        this.root = root;
    }

    public StandardCache() {
        this(Path.of(System.getProperty("user.home"), ".jresolve"));
    }

    private Path keyPath(CacheKey key) {
        return Path.of(
                root.toString(),
                key.components().toArray(String[]::new)
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
        var filePath = keyPath(key);
        LOG.log(Level.TRACE, () -> "About to check if file exists. filePath=" + filePath);
        if (!Files.exists(filePath)) {
            LOG.log(Level.TRACE, () -> "File does not exist. filePath=" + filePath);
            try {
                LOG.log(Level.TRACE, () -> "Creating parent directories for file. filePath=" + filePath);
                Files.createDirectories(filePath.getParent());

                LOG.log(Level.TRACE, () -> "About to get data from input source. data=" + data);

                try (var inputStream = data.get();
                     var outputStream = Files.newOutputStream(
                        filePath,
                        StandardOpenOption.CREATE_NEW
                )) {
                    LOG.log(Level.TRACE, () -> "Transferring contents to file. filePath=" + filePath);
                    inputStream.transferTo(outputStream);
                    return filePath;
                } catch (FileAlreadyExistsException e) {
                    LOG.log(Level.TRACE, () -> "File already exists in cache. filePath=" + filePath);
                    return filePath;
                }

            } catch (IOException e) {
                LOG.log(Level.TRACE, () -> "Error getting data. filePath=" + filePath, e);
                throw new UncheckedIOException(e);
            }
        }
        LOG.log(Level.TRACE, () -> "File exists. filePath=" + filePath);

        return filePath;
    }

    @Override
    public Path fetch(CacheKey key, Supplier<InputStream> data) {
        return null;
    }
}

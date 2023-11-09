package dev.mccue.resolve;

import dev.mccue.resolve.doc.Coursier;
import org.jspecify.annotations.NullMarked;

import java.io.*;
import java.lang.System.Logger.Level;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@NullMarked
@Coursier("""
        https://github.com/coursier/coursier/blob/929301cd078b6ba13ea78d5065cb07130576839a/modules/paths/src/main/java/coursier/paths/CachePath.java#L117
        """)
final class StandardCache implements Cache {
    private static final System.Logger LOG = System.getLogger(StandardCache.class.getName());

    // Trying to limit the calls to String.intern via this map (https://shipilev.net/jvm/anatomy-quarks/10-string-intern/)
    private static final ConcurrentHashMap<String, String> INTERNED_STRINGS = new ConcurrentHashMap<>();

    // Even if two versions of that code end up in the same JVM (say one via a shaded coursier, the other via a
    // non-shaded coursier), they will rely on the exact same object for locking here (via String.intern), so that the
    // locks are actually JVM-wide.
    private static Object lockFor(Path cachePath) {
        String key = "jresolve-jvm-lock-" + cachePath.toString();
        Object lock0 = INTERNED_STRINGS.get(key);
        if (lock0 == null) {
            String internedKey = key.intern();
            INTERNED_STRINGS.putIfAbsent(internedKey, internedKey);
            lock0 = internedKey;
        }
        return lock0;
    }
    private final Path root;

    public StandardCache(Path root) {
        Objects.requireNonNull(root);
        this.root = root;
    }

    public StandardCache() {
        this(Path.of(System.getProperty("user.home"), ".jresolve", "cache"));
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
        synchronized (lockFor(filePath)) {
            LOG.log(Level.TRACE, () -> "About to check if file exists. filePath=" + filePath);
            boolean noFile = !Files.exists(filePath);
            if (noFile) {
                LOG.log(Level.TRACE, () -> "File does not exist. filePath=" + filePath);
                try {
                    LOG.log(Level.TRACE, () -> "Acquiring structural lock. root=" + root);

                    withStructureLock(this.root, () -> {
                        try {
                            LOG.log(Level.TRACE, () -> "Creating parent directories for file. filePath=" + filePath);
                            Files.createDirectories(filePath.getParent());
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                    LOG.log(Level.TRACE, () -> "Released structural lock. root=" + root);

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

    }

    @Coursier("https://github.com/coursier/coursier/blob/929301cd078b6ba13ea78d5065cb07130576839a/modules/paths/src/main/java/coursier/paths/CachePath.java#L176")
    static void withStructureLock(Path cache, Runnable runnable) {
        try {
            synchronized (lockFor(cache)) {
                Path lockFile = cache.resolve(".structure.lock");
                Files.createDirectories(lockFile.getParent());
                try(FileChannel channel = FileChannel.open(
                        lockFile,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.DELETE_ON_CLOSE
                )) {
                    FileLock lock = null;
                    try {
                        lock = channel.lock();

                        try {
                            runnable.run();
                        }
                        finally {
                            lock.release();
                            lock = null;
                        }
                    }
                    finally {
                        if (lock != null) lock.release();
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    @Override
    public Path fetch(CacheKey key, Supplier<InputStream> data) {
        var filePath = keyPath(key);
        synchronized (lockFor(filePath)) {
            LOG.log(Level.TRACE, () -> "About to check if file exists. filePath=" + filePath);
            boolean noFile = !Files.exists(filePath);
            if (noFile) {
                LOG.log(Level.TRACE, () -> "File does not exist. filePath=" + filePath);

                LOG.log(Level.TRACE, () -> "Acquiring structural lock. root=" + root);
                withStructureLock(root, () -> {
                    try {
                        LOG.log(Level.TRACE, () -> "Creating parent directories for file. filePath=" + filePath);
                        Files.createDirectories(filePath.getParent());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
                LOG.log(Level.TRACE, () -> "Released structural lock. root=" + root);
            }
            else {
                LOG.log(Level.TRACE, () -> "File exists. filePath=" + filePath);
            }

            LOG.log(Level.TRACE, () -> "About to get data from input source. data=" + data);
            try {
                try (
                        var inputStream = data.get();
                        var outputStream = Files.newOutputStream(
                             filePath,
                             StandardOpenOption.CREATE,
                             StandardOpenOption.WRITE
                        )
                ) {
                    LOG.log(Level.TRACE, () -> "Transferring contents to file. filePath=" + filePath);
                    inputStream.transferTo(outputStream);
                    return filePath;
                }
            } catch (IOException e) {
                LOG.log(Level.TRACE, () -> "Error getting data. filePath=" + filePath, e);
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public boolean probablyContains(CacheKey key) {
        var filePath = keyPath(key);
        return Files.exists(filePath);
    }
}

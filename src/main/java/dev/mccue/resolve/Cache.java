package dev.mccue.resolve;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public interface Cache {
    static Cache standard() {
        return new StandardCache();
    }

    static Cache standard(Path path) {
        return new StandardCache(path);
    }

    /**
     * Saves data inside the cache, but re-uses it if it is already present.
     * @param key A unique key for the data, split into path fragments.
     * @param data A supplier for the data to store in the cache.
     * @return A {@link Path} containing the data.
     */
    Path fetchIfAbsent(CacheKey key, Supplier<InputStream> data);

    /**
     * Saves data inside the cache unconditionally, invalidating any old values.
     * @param key A unique key for the data, split into path fragments.
     * @param data A supplier for the data to store in the cache.
     * @return A {@link Path} containing the data.
     */
    Path fetch(CacheKey key, Supplier<InputStream> data);

    /**
     * Returns if the cache likely has a value for the given cache key.
     *
     * <p>
     *     Because file systems can be wack, this can't say for sure whether
     *     a call to {@link Cache#fetchIfAbsent(CacheKey, Supplier)} will
     *     use a cached value. This should just be considered a hint.
     * </p>
     * @param cacheKey A unique key for the data, split into path fragments.
     * @return true if there is a high likely-hood the cache has a value for the given key.
     */
    default boolean probablyContains(CacheKey cacheKey) {
        return false;
    }
}

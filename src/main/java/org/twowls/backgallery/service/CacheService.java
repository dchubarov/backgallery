package org.twowls.backgallery.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.twowls.backgallery.utils.Equipped;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.twowls.backgallery.utils.Named.compositeName;
import static org.twowls.backgallery.utils.Named.normalizedName;

/**
 * Represents simple in-memory cache for data entities.
 *
 * @author Dmitry Chubarov
 */
@Service
public class CacheService {
    static final String CREATE_TIME_PROP = "create-time";
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private final ConcurrentMap<String, Equipped<?>> data = new ConcurrentHashMap<>();

    /**
     * Retrieves an object from cache or creates a new one if key does not exist.
     *
     * @param key the key of the object being cached
     * @param bareClass the type of payload of a cached {@link Equipped}, not {@code null}
     * @param factory a function that creates a new instance if no object exists in cache.
     * @param <T> the bare type of cached object.
     * @return an {@link Equipped} instance wrapping cached object.
     */
    <T> Equipped<T>
    getOrCreate(String key, Class<? extends T> bareClass, Function<String, ? extends T> factory) {
        return getOrCreate(key, null, bareClass, factory);
    }

    /**
     * Retrieves an object from cache or creates a new one if key does not exist.
     *
     * @param key the key of the object being cached
     * @param instanceName specifies instance name if different from {@code key}.
     *                     May be {@code null} to use {@code key} as instance name.
     *                     If object already cached this argument is not used.
     * @param bareClass the type of payload of a cached {@link Equipped}, not {@code null}
     * @param factory a function that creates a new instance if no object exists in cache.
     * @param <T> the bare type of cached object.
     * @return an {@link Equipped} instance wrapping cached object.
     */
    @SuppressWarnings("unchecked")
    <T> Equipped<T>
    getOrCreate(String key, String instanceName, Class<? extends T> bareClass, Function<String, ? extends T> factory) {
        String normalizedKey = normalizedName(key);
        requireNonNull(factory);

        // create a new cache entry for given key and class, invoking factory function is key is missing
        Equipped<?> entry = data.computeIfAbsent(internalKey(normalizedKey, bareClass), (fullKey) -> {
            T instance = factory.apply(normalizedKey);
            logger.debug("Created new cache entry with key \"{}\" --> \"{}\".", fullKey, instance);
            return Equipped.of(instance, StringUtils.defaultIfEmpty(normalizedName(instanceName), normalizedKey))
                    .with(CREATE_TIME_PROP, System.nanoTime());
        });

        // check whether cached object is compatible with requested class
        if (entry != null && entry.bare() != null && !bareClass.isAssignableFrom(entry.bare().getClass())) {
            throw new IllegalStateException("Requested class " + bareClass + " is not compatible with cached entry " +
                    "which contains instance of class " + entry.bare().getClass() + ".");
        }

        return (Equipped<T>) entry;
    }

    /**
     * Evicts an individual entry from the cache.
     *
     * @param key the key to evict.
     * @return an object specified by key or {@code null} if {@code key} does not exist.
     */
    Equipped<?>
    evict(String key, Class<?> bareClass) {
        String fullKey = internalKey(normalizedName(key), bareClass);
        Equipped<?> entry = data.remove(fullKey);
        if (entry == null) {
            logger.debug("Eviction of non-existent key {} failed.", fullKey);
        } else {
            logger.debug("Evicted cache entry with key: {}.", fullKey);
        }
        return entry;
    }

    /**
     * Evicts all entries from the cache.
     */
    void evictAll() {
        data.clear();
    }

    private String internalKey(String normalizedKey, Class<?> bareClass) {
        return compositeName(bareClass.getName(), normalizedKey);
    }
}

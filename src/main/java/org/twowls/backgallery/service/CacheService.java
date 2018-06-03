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
    private static final String CREATE_TIME = "create-time";
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private final ConcurrentMap<String, Equipped<?>> data = new ConcurrentHashMap<>();

    <T> Equipped<T>
    getOrCreate(String key, Class<? extends T> bareClass, Function<String, ? extends T> factory) {
        return getOrCreate(key, key, bareClass, factory);
    }

    @SuppressWarnings("unchecked")
    <T> Equipped<T>
    getOrCreate(String key, String instanceName, Class<? extends T> bareClass, Function<String, ? extends T> factory) {
        String normalizedKey = normalizedName(key);
        requireNonNull(factory);

        // create a new cache entry for given key and class, invoking factory function is key is missing
        Equipped<?> entry = data.computeIfAbsent(compositeName(bareClass.getName(), normalizedKey), (fullKey) -> {
            T instance = factory.apply(normalizedKey);
            logger.debug("Created new cache entry with key \"{}\" --> \"{}\".", fullKey, instance);
            return Equipped.of(instance, StringUtils.defaultString(normalizedName(instanceName), normalizedKey))
                    .with(CREATE_TIME, System.nanoTime());
        });

        // check whether cached object is compatible with requested class
        if (entry != null && entry.bare() != null && !bareClass.isAssignableFrom(entry.bare().getClass())) {
            throw new IllegalStateException("Requested class " + bareClass + " is not compatible with cached entry " +
                    "which contains instance of class " + entry.bare().getClass() + ".");
        }

        return (Equipped<T>) entry;
    }

    Equipped<?> evict(String key) {
        Equipped<?> entry = data.remove(normalizedName(key));
        if (entry != null) {
            logger.debug("Evicted cache entry with key: {}", key);
        } else {
            logger.debug("Eviction failed for key: {}, key does not exists.", key);
        }
        return entry;
    }
}

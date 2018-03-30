package org.twowls.backgallery.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.twowls.backgallery.utils.Equipped;
import org.twowls.backgallery.utils.Named;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
@Service
public class CacheService {
    private static final String CREATE_TIME = "create-time";
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private final ConcurrentMap<String, Equipped<?>> data = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Equipped<T> getOrCreate(String key, Class<? extends T> bareClass, Function<String, ? extends T> factory) {
        String normalizedKey = Named.normalize(key);
        Objects.requireNonNull(factory);

        // create a new cache entry for given key and class, invoking factory function is key is missing
        Equipped<?> entry = data.computeIfAbsent(bareClass.getName() + "$" + normalizedKey, (compoundKey) -> {
            T instance = factory.apply(normalizedKey);
            logger.debug("Created new cache entry with key \"{}\" --> \"{}\".", compoundKey, instance);
            return Equipped.of(instance, normalizedKey).with(CREATE_TIME, System.nanoTime());
        });

        // check whether cached object is compatible with requested class
        if (entry != null && entry.bare() != null && !bareClass.isAssignableFrom(entry.bare().getClass())) {
            throw new IllegalStateException("Requested class " + bareClass + " is not compatible with cached entry " +
                    "which contains instance of class " + entry.bare().getClass() + ".");
        }

        return (Equipped<T>) entry;
    }

    public Equipped<?> evict(String key) {
        return data.remove(Named.normalize(key));
    }
}

package org.twowls.backgallery.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents an entity equipped with additional properties that
 * can anytime be added but never replaced or removed.
 *
 * TODO consider immutable properties
 *
 * @param <T> the type of bare object.
 * @author Dmitry Chubarov
 */
public class Equipped<T> implements Named<T> {
    private static final String NAME = "$";
    private final ConcurrentMap<String, Object> properties = new ConcurrentHashMap<>();
    private final T obj;

    private Equipped(T obj, String name) {
        properties.put(NAME, Named.normalize(name));
        this.obj = obj;
    }

    @Override
    public String name() {
        return (String) properties.get(NAME);
    }

    @Override
    public T bare() {
        return this.obj;
    }

    public Equipped<T> with(String key, Object value) {
        String normalizedKey = Named.normalize(key);

        properties.compute(normalizedKey, (k, v) -> {
            if (v != null) throw new IllegalStateException("Property already exists: " + k);
            return value;
        });

        return this;
    }

    public static <T> Equipped<T> of(T obj, String name) {
        return new Equipped<>(obj, name);
    }
}

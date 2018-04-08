package org.twowls.backgallery.utils;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents an entity equipped with additional properties.
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
        properties.putIfAbsent(Named.normalize(key), value);
        return this;
    }

    public Object prop(String key) {
        return properties.get(Named.normalize(key));
    }

    public <U> U prop(String key, Class<U> target) {
        return Objects.requireNonNull(target).cast(prop(key));
    }

    @SuppressWarnings("unchecked")
    public <V> Equipped<V> equippedProp(String key, Class<V> target) {
        Objects.requireNonNull(target);
        Equipped<?> e = (Equipped<?>) prop(key);
        Class<?> bareClass = (e != null && e.bare() != null ? e.bare().getClass() : null);
        if (bareClass == null || !target.isAssignableFrom(bareClass)) {
            throw new ClassCastException("Expected equipped property " + key + " with payload class " +
                    target + ", while real class is " + bareClass);
        }
        return (Equipped<V>) e;
    }

    public static <T> Equipped<T> of(T obj, String name) {
        return new Equipped<>(obj, name);
    }
}

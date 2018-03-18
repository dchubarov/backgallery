package org.twowls.backgallery.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * Represents an entity identified by its name.
 *
 * @author Dmitry Chubarov
 */
public class Named<T> {
    private final String name;
    private final T payload;

    private Named(String name, T payload) {
        this.name = normalize(name);
        this.payload = payload;
    }

    public String name() {
        return this.name;
    }

    public T payload() {
        return this.payload;
    }

    public static String normalize(String name) {
        return Objects.requireNonNull(StringUtils.trimToEmpty(name));
    }

    public static <T> Named<T> of(String name, T payload) {
        return new Named<>(name, payload);
    }
}

package org.twowls.backgallery.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * Represents an entity identified by its name.
 *
 * @param <T> type of bare object
 * @author Dmitry Chubarov
 */
public interface Named<T> {

    /**
     * @return the name of this object.
     */
    String name();

    /**
     * @return the bare object, contract does not restrict usage of {@code null}s.
     */
    T bare();

    /**
     * Creates and returns normalized name based on given name, may throw unchecked exceptions.
     *
     * @param name the source name.
     * @return the normalized name.
     */
    static String normalize(String name) {
        return Objects.requireNonNull(StringUtils.trimToEmpty(name));
    }
}

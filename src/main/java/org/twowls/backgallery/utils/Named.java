package org.twowls.backgallery.utils;

import org.apache.commons.lang3.StringUtils;

import static java.util.Objects.requireNonNull;

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
     * Returns bare object cast to desired type.
     *
     * @param <U> the target type.
     * @param target the target class.
     * @return the bare object cast to {@code targetClass}.
     * @throws ClassCastException if the bare object is not assignable to the type {@code U}.
     */
    default <U> U bare(Class<U> target) {
        return requireNonNull(target).cast(bare());
    }

    /**
     * Creates and returns normalized name based on given name, may throw unchecked exceptions.
     *
     * @param name the source name.
     * @return the normalized name.
     */
    static String normalize(String name) {
        return requireNonNull(StringUtils.trimToEmpty(name));
    }
}

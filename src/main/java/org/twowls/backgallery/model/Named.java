package org.twowls.backgallery.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * Represents an entity identified by its name.
 *
 * @author Dmitry Chubarov
 */
public interface Named {

    String name();

    static String normalize(String name) {
        return Objects.requireNonNull(StringUtils.trimToEmpty(name));
    }
}

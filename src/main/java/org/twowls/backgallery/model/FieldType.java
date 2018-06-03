package org.twowls.backgallery.model;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * Contains constants for field types, and their possible textual representations in config files.
 *
 * @author Dmitry Chubarov
 */
public enum FieldType {
    /**
     * Default data type (string).
     */
    DEFAULT,

    /**
     * Comma-separated list of values (e.g. "value1, value2, value3").
     */
    LIST,

    /**
     * Hierarchical value (e.g. "value1/value2/value3").
     */
    HIERARCHY,

    /**
     * Rating value (zero to five stars).
     */
    RATING("rank"),

    /**
     * Flag value (yes or no).
     */
    FLAG("boolean", "bool"),

    /**
     * Geo-location value (coordinates).
     */
    LOCATION("geo", "coordinates");

    private Set<String> altNames = new HashSet<>();

    FieldType(String... altNames) {
        this.altNames.addAll(Arrays.asList(altNames));
    }

    /**
     * Finds a constant for given {@code name} which is either constant's name or
     * one of alternative names (all case-insensitive).
     *
     * @param name the sought name
     * @return a value of {@link FieldType} for given name of {@code null} if name was {@code null}.
     */
    public static FieldType decode(String name) {
        return (name != null ? Stream.of(values()).filter(o -> Stream.concat(Stream.of(o.name()), o.altNames.stream())
                .anyMatch(s -> StringUtils.equalsIgnoreCase(s, name))).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There is no [" + FieldType.class.getName() +
                        "] constant for value '" + name + "'.")) : null);
    }
}

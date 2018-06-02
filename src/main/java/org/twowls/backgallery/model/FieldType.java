package org.twowls.backgallery.model;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
public enum FieldType {
    DEFAULT,
    LIST,
    HIERARCHY,
    RATING("rank"),
    FLAG("boolean", "bool"),
    LOCATION("geo", "coordinates");

    private Set<String> altNames = new HashSet<>();

    FieldType(String... altNames) {
        this.altNames.addAll(Arrays.asList(altNames));
    }

    public static FieldType decode(String name) {
        return (name != null ? Stream.of(values()).filter(o -> Stream.concat(Stream.of(o.name()), o.altNames.stream())
                .anyMatch(s -> StringUtils.equalsIgnoreCase(s, name))).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There is no [" + FieldType.class.getName() +
                        "] constant for value '" + name + "'.")) : null);
    }
}

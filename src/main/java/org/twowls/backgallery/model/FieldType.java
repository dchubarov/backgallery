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

    private Set<String> typeNames = new HashSet<>();

    FieldType(String... names) {
        typeNames.addAll(Arrays.asList(names));
    }

    public static FieldType forName(String name) {
        return (name != null ? Stream.of(values()).filter(o -> Stream.concat(Stream.of(o.name()), o.typeNames.stream())
                .anyMatch(s -> StringUtils.equalsIgnoreCase(s, name))).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There is no [" + FieldType.class.getName() +
                        "] constant for value '" + name + "'.")) : null);
    }
}

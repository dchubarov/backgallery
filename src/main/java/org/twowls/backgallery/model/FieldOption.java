package org.twowls.backgallery.model;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * Contains constants for the field options.
 *
 * @author Dmitry Chubarov
 */
public enum FieldOption {
    /**
     * The field value should be localized.
     */
    LOCALIZED,

    /**
     * The field value is indexed.
     */
    INDEXED;

    private final Set<String> altNames = new HashSet<>();

    FieldOption(String... altNames) {
        this.altNames.addAll(Arrays.asList(altNames));
    }

    /**
     * Returns a constant for a given name.
     *
     * @param name the sought name.
     * @return a {@link FieldOption} value or {@code null} if {@code name} was {@code null}.
     * @throws IllegalArgumentException if {@code name} is not valid.
     */
    public static FieldOption decode(String name) {
        return (name != null ? Stream.of(values()).filter(o -> Stream.concat(Stream.of(o.name()), o.altNames.stream())
                .anyMatch(s -> StringUtils.equalsIgnoreCase(s, name))).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There is no [" + FieldOption.class.getName() +
                        "] constant for value '" + name + "'.")) : null);
    }

    /**
     * Converts a comma-separated list of option names into set of options.
     *
     * @param multipleOptions a list of option names separated with whitespace, colon, or semicolon.
     *                        Each name in the list is either a constant name or one of alternative
     *                        names (all case insensitive).
     * @return a set of options.
     * @throws IllegalArgumentException if one or more names in the list are not valid.
     */
    public static EnumSet<FieldOption> decodeMultiple(String multipleOptions) {
        EnumSet<FieldOption> result = EnumSet.noneOf(FieldOption.class);
        if (multipleOptions != null) {
            Arrays.stream(multipleOptions.split("[\\s;,]"))
                    .map(StringUtils::trimToNull)
                    .filter(Objects::nonNull)
                    .forEach(s -> result.add(decode(s)));
        }
        return result;
    }
}

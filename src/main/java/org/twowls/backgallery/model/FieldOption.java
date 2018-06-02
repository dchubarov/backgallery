package org.twowls.backgallery.model;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
public enum FieldOption {
    LOCALIZED,
    INDEXED;

    private final Set<String> altNames = new HashSet<>();

    FieldOption(String... altNames) {
        this.altNames.addAll(Arrays.asList(altNames));
    }

    public static FieldOption decode(String name) {
        return (name != null ? Stream.of(values()).filter(o -> Stream.concat(Stream.of(o.name()), o.altNames.stream())
                .anyMatch(s -> StringUtils.equalsIgnoreCase(s, name))).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There is no [" + FieldOption.class.getName() +
                        "] constant for value '" + name + "'.")) : null);
    }

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

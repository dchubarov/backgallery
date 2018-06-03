package org.twowls.backgallery.model.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.twowls.backgallery.model.FieldDescriptor;
import org.twowls.backgallery.model.FieldOption;
import org.twowls.backgallery.model.FieldType;

import java.util.stream.Collectors;

/**
 * A {@link FieldDescriptor} specialization equipped with JSON serialization/deserialization rules.
 *
 * @author Dmitry Chubarov
 */
public class FieldDescriptorJson extends FieldDescriptor {
    private static final String FIELD_TYPE_JSON = "type";
    private static final String FIELD_OPTIONS_JSON = "options";

    @JsonCreator
    @SuppressWarnings("unused")
    FieldDescriptorJson(
            @JsonProperty(FIELD_TYPE_JSON) String fieldTypeName,
            @JsonProperty(FIELD_OPTIONS_JSON) String fieldOptions) {
        super(FieldType.decode(fieldTypeName), FieldOption.decodeMultiple(fieldOptions));
    }

    @JsonProperty(FIELD_TYPE_JSON)
    @SuppressWarnings("unused")
    public String typeName() {
        return StringUtils.lowerCase(type().name());
    }

    @JsonProperty(FIELD_OPTIONS_JSON)
    @SuppressWarnings("unused")
    public String optionNames() {
        return options().stream().map(o -> StringUtils.lowerCase(o.name()))
                .collect(Collectors.joining(", "));
    }
}

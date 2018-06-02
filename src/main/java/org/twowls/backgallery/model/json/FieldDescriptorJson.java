package org.twowls.backgallery.model.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.twowls.backgallery.model.FieldDescriptor;
import org.twowls.backgallery.model.FieldType;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
public class FieldDescriptorJson extends FieldDescriptor {
    private static final String FIELD_TYPE_JSON = "type";
    private static final String LOCALIZED_JSON = "localized";
    private static final String INDEXED_JSON = "indexed";

    @JsonCreator
    @SuppressWarnings("unused")
    FieldDescriptorJson(
            @JsonProperty(FIELD_TYPE_JSON) String fieldTypeName,
            @JsonProperty(LOCALIZED_JSON) boolean localized,
            @JsonProperty(INDEXED_JSON) boolean indexed) {
        super(FieldType.forName(fieldTypeName), localized, indexed);
    }

    @JsonProperty(FIELD_TYPE_JSON)
    public String typeName() {
        return StringUtils.lowerCase(type().name());
    }

    @Override
    @JsonProperty(LOCALIZED_JSON)
    public boolean localized() {
        return super.localized();
    }

    @Override
    @JsonProperty(INDEXED_JSON)
    public boolean indexed() {
        return super.indexed();
    }
}

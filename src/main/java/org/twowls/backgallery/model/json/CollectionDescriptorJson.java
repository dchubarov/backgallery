package org.twowls.backgallery.model.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.twowls.backgallery.model.CollectionDescriptor;
import org.twowls.backgallery.model.FieldDescriptor;

import java.util.Map;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
public class CollectionDescriptorJson extends CollectionDescriptor {
    private static final String DESCRIPTION_JSON = "description";
    private static final String FIELDS_JSON = "fields";
    private static final String SIZES_JSON = "sizes";

    @JsonCreator
    @SuppressWarnings("unused")
    CollectionDescriptorJson(@JsonProperty(DESCRIPTION_JSON) String description,
            @JsonProperty(FIELDS_JSON)
            @JsonDeserialize(contentAs = FieldDescriptorJson.class) Map<String, FieldDescriptor> fields,
            @JsonProperty(SIZES_JSON) Map<String, Integer> sizes) {
        super(description, fields, sizes);
    }

    @Override
    @JsonProperty(DESCRIPTION_JSON)
    public String description() {
        return super.description();
    }

    @Override
    @JsonProperty(FIELDS_JSON)
    public Map<String, FieldDescriptor> fields() {
        return super.fields();
    }

    @Override
    @JsonProperty(SIZES_JSON)
    public Map<String, Integer> sizes() {
        return super.sizes();
    }
}

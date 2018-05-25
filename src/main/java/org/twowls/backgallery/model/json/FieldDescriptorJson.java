package org.twowls.backgallery.model.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.twowls.backgallery.model.FieldDescriptor;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
public class FieldDescriptorJson extends FieldDescriptor {
    private static final String LOCALIZED_JSON = "localized";
    private static final String INDEXED_JSON = "indexed";

    @JsonCreator
    @SuppressWarnings("unused")
    FieldDescriptorJson(@JsonProperty(LOCALIZED_JSON) boolean localized,
            @JsonProperty(INDEXED_JSON) boolean indexed) {
        super(localized, indexed);
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

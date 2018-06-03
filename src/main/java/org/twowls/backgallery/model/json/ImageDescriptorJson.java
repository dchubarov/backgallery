package org.twowls.backgallery.model.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.twowls.backgallery.model.ImageDescriptor;

/**
 * An {@link ImageDescriptor} specialization equipped with JSON serialization/deserialization rules.
 *
 * @author Dmitry Chubarov
 */
public class ImageDescriptorJson extends ImageDescriptor {
    private static final String ID_JSON = "id";

    @JsonCreator
    @SuppressWarnings("unused")
    ImageDescriptorJson(@JsonProperty(ID_JSON) String id) {
        super(id);
    }

    @Override
    @JsonProperty(ID_JSON)
    public String id() {
        return super.id();
    }
}

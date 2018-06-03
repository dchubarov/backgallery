package org.twowls.backgallery.model.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.twowls.backgallery.model.RealmDescriptor;

/**
 * A {@link RealmDescriptor} specialization equipped with JSON serialization/deserialization rules.
 *
 * @author Dmitry Chubarov
 */
public class RealmDescriptorJson extends RealmDescriptor {
    private static final String DESCRIPTION_JSON = "description";
    private static final String SECURITY_TOKEN_JSON = "security-token";

    @JsonCreator
    @SuppressWarnings("unused")
    RealmDescriptorJson(@JsonProperty(DESCRIPTION_JSON) String description,
            @JsonProperty(SECURITY_TOKEN_JSON) String securityToken) {
        super(description, securityToken);
    }

    @Override
    @JsonProperty(DESCRIPTION_JSON)
    public String description() {
        return super.description();
    }

    @Override
    @JsonProperty(SECURITY_TOKEN_JSON)
    public String securityToken() {
        return super.securityToken();
    }
}

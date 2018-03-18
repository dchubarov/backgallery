package org.twowls.backgallery.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an individual realm in a backgallery database.
 *
 * @author Dmitry Chubarov
 */
public class RealmDescriptor {
    public static final String CONFIG = "realm.yml";

    @JsonProperty(value = "description")
    private String description;

    @JsonProperty(value = "security-token")
    private String securityToken;

    public String description() {
        return this.description;
    }

    public String securityToken() {
        return this.securityToken;
    }
}

package org.twowls.backgallery.model;

/**
 * Represents an individual realm in a backgallery database.
 *
 * @author Dmitry Chubarov
 */
public class RealmDescriptor implements Descriptor {
    public static final String CONFIG = "realm.yml";
    private String description;
    private String securityToken;

    protected RealmDescriptor(String description, String securityToken) {
        this.description = description;
        this.securityToken = securityToken;
    }

    public String description() {
        return this.description;
    }

    public String securityToken() {
        return this.securityToken;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{ " +
                "description=" + description +
                ", securityToken=" + (securityToken == null ? null : "***") +
                "}";
    }
}

package org.twowls.backgallery.model;

/**
 * Represents an individual image in a collection.
 *
 * @author Dmitry Chubarov
 */
public class ImageDescriptor implements Descriptor {
    private final String id;

    protected ImageDescriptor(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{ " +
                "id=" + id +
                "}";
    }
}

package org.twowls.backgallery.model;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
public class ImageDescriptor {
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

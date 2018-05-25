package org.twowls.backgallery.model;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
public class FieldDescriptor implements Descriptor {
    private final boolean localized;
    private final boolean indexed;

    protected FieldDescriptor(boolean localized, boolean indexed) {
        this.localized = localized;
        this.indexed = indexed;
    }

    public boolean localized() {
        return this.localized;
    }

    public boolean indexed() {
        return this.indexed;
    }

    public String toString() {
        return getClass().getSimpleName() + "{ " +
                "localized=" + localized +
                "indexed=" + indexed +
                "}";
    }
}

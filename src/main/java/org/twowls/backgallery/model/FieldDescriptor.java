package org.twowls.backgallery.model;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
public class FieldDescriptor implements Descriptor {
    private final FieldType type;
    private final boolean localized;
    private final boolean indexed;

    protected FieldDescriptor(FieldType type, boolean localized, boolean indexed) {
        this.type = type == null ? FieldType.DEFAULT : type;
        this.localized = localized;
        this.indexed = indexed;
    }

    public FieldType type() {
        return this.type;
    }

    public boolean localized() {
        return this.localized;
    }

    public boolean indexed() {
        return this.indexed;
    }

    public String toString() {
        return getClass().getSimpleName() + "{ " +
                "type=" + type + ", " +
                "localized=" + localized + ", " +
                "indexed=" + indexed +
                "}";
    }
}

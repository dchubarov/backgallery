package org.twowls.backgallery.model;

import java.util.EnumSet;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
public class FieldDescriptor implements Descriptor {
    private final FieldType type;
    private final EnumSet<FieldOption> options;

    protected FieldDescriptor(FieldType type, EnumSet<FieldOption> options) {
        this.type = (type == null ? FieldType.DEFAULT : type);
        this.options = (options == null ? EnumSet.noneOf(FieldOption.class) : EnumSet.copyOf(options));
    }

    public FieldType type() {
        return this.type;
    }

    public EnumSet<FieldOption> options() {
        return EnumSet.copyOf(this.options);
    }

    public String toString() {
        return getClass().getSimpleName() + "{ " +
                "type=" + type + ", " +
                "options=" + options +
                " }";
    }
}

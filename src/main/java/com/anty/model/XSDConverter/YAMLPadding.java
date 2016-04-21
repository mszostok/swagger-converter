package com.anty.model.XSDConverter;

/**
 * Created by indianer on 15.04.2016.
 */
public enum YAMLPadding {
    REF_DEF(8),
    ENUM_PREFIX(8),
    ENUM_BODY(6),
    MODEL_BODY(4),
    MODEL_VAR(6),
    MODEL_ATTR(8);


    private final String name;

    YAMLPadding(int n) {
        name = String.format("%1$" + n + "s","");
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}

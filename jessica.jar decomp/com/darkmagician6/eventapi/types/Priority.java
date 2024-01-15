/*
 * Decompiled with CFR 0.152.
 */
package com.darkmagician6.eventapi.types;

public final class Priority {
    public static final byte HIGHEST = 0;
    public static final byte HIGH = 1;
    public static final byte MEDIUM = 2;
    public static final byte LOW = 3;
    public static final byte LOWEST = 4;
    public static final byte[] VALUE_ARRAY;

    static {
        byte[] byArray = new byte[5];
        byArray[1] = 1;
        byArray[2] = 2;
        byArray[3] = 3;
        byArray[4] = 4;
        VALUE_ARRAY = byArray;
    }
}


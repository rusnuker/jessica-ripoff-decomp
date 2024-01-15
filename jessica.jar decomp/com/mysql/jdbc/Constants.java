/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Messages;

public class Constants {
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final String MILLIS_I18N = Messages.getString("Milliseconds");
    public static final byte[] SLASH_STAR_SPACE_AS_BYTES = new byte[]{47, 42, 32};
    public static final byte[] SPACE_STAR_SLASH_SPACE_AS_BYTES = new byte[]{32, 42, 47, 32};

    private Constants() {
    }
}


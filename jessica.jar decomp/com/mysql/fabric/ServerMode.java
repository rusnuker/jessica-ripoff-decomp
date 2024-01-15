/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public enum ServerMode {
    OFFLINE,
    READ_ONLY,
    WRITE_ONLY,
    READ_WRITE;


    public static ServerMode getFromConstant(Integer constant) {
        return ServerMode.values()[constant];
    }
}


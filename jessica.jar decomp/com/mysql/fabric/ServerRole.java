/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public enum ServerRole {
    FAULTY,
    SPARE,
    SECONDARY,
    PRIMARY,
    CONFIGURING;


    public static ServerRole getFromConstant(Integer constant) {
        return ServerRole.values()[constant];
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

public interface Logger {
    public static final int DEBUG = 0;
    public static final int INFO = 1;
    public static final int WARN = 2;
    public static final int ERROR = 3;
    public static final int FATAL = 4;

    public boolean isEnabled(int var1);

    public void log(int var1, String var2);
}


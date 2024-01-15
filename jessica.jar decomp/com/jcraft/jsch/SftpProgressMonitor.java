/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

public interface SftpProgressMonitor {
    public static final int PUT = 0;
    public static final int GET = 1;
    public static final long UNKNOWN_SIZE = -1L;

    public void init(int var1, String var2, String var3, long var4);

    public boolean count(long var1);

    public void end();
}


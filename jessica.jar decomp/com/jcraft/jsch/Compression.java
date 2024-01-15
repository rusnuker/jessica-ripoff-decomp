/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

public interface Compression {
    public static final int INFLATER = 0;
    public static final int DEFLATER = 1;

    public void init(int var1, int var2);

    public byte[] compress(byte[] var1, int var2, int[] var3);

    public byte[] uncompress(byte[] var1, int var2, int[] var3);
}


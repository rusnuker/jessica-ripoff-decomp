/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

public interface MAC {
    public String getName();

    public int getBlockSize();

    public void init(byte[] var1) throws Exception;

    public void update(byte[] var1, int var2, int var3);

    public void update(int var1);

    public void doFinal(byte[] var1, int var2);
}


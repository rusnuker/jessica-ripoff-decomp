/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

public interface HASH {
    public void init() throws Exception;

    public int getBlockSize();

    public void update(byte[] var1, int var2, int var3) throws Exception;

    public byte[] digest() throws Exception;
}


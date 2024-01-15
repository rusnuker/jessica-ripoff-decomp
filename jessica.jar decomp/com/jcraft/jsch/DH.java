/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

public interface DH {
    public void init() throws Exception;

    public void setP(byte[] var1);

    public void setG(byte[] var1);

    public byte[] getE() throws Exception;

    public void setF(byte[] var1);

    public byte[] getK() throws Exception;

    public void checkRange() throws Exception;
}


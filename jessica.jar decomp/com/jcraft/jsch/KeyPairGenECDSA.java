/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

public interface KeyPairGenECDSA {
    public void init(int var1) throws Exception;

    public byte[] getD();

    public byte[] getR();

    public byte[] getS();
}


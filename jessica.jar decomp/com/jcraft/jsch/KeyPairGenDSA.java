/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

public interface KeyPairGenDSA {
    public void init(int var1) throws Exception;

    public byte[] getX();

    public byte[] getY();

    public byte[] getP();

    public byte[] getQ();

    public byte[] getG();
}


/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

public interface KeyPairGenRSA {
    public void init(int var1) throws Exception;

    public byte[] getD();

    public byte[] getE();

    public byte[] getN();

    public byte[] getC();

    public byte[] getEP();

    public byte[] getEQ();

    public byte[] getP();

    public byte[] getQ();
}


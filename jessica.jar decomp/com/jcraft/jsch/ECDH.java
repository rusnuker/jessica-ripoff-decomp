/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

public interface ECDH {
    public void init(int var1) throws Exception;

    public byte[] getSecret(byte[] var1, byte[] var2) throws Exception;

    public byte[] getQ() throws Exception;

    public boolean validate(byte[] var1, byte[] var2) throws Exception;
}


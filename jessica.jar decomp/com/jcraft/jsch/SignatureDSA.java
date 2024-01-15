/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Signature;

public interface SignatureDSA
extends Signature {
    public void setPubKey(byte[] var1, byte[] var2, byte[] var3, byte[] var4) throws Exception;

    public void setPrvKey(byte[] var1, byte[] var2, byte[] var3, byte[] var4) throws Exception;
}


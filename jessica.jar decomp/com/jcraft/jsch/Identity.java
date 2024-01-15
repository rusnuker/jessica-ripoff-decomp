/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.JSchException;

public interface Identity {
    public boolean setPassphrase(byte[] var1) throws JSchException;

    public byte[] getPublicKeyBlob();

    public byte[] getSignature(byte[] var1);

    public boolean decrypt();

    public String getAlgName();

    public String getName();

    public boolean isEncrypted();

    public void clear();
}


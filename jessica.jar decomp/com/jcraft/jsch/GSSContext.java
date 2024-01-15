/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.JSchException;

public interface GSSContext {
    public void create(String var1, String var2) throws JSchException;

    public boolean isEstablished();

    public byte[] init(byte[] var1, int var2, int var3) throws JSchException;

    public byte[] getMIC(byte[] var1, int var2, int var3);

    public void dispose();
}


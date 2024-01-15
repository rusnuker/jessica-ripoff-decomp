/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jcraft;

import com.jcraft.jsch.jcraft.HMACSHA1;

public class HMACSHA196
extends HMACSHA1 {
    private static final String name = "hmac-sha1-96";
    private static final int BSIZE = 12;
    private final byte[] _buf16 = new byte[20];

    public int getBlockSize() {
        return 12;
    }

    public void doFinal(byte[] buf, int offset) {
        super.doFinal(this._buf16, 0);
        System.arraycopy(this._buf16, 0, buf, offset, 12);
    }

    public String getName() {
        return name;
    }
}


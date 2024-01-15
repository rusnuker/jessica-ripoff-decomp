/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jce;

import com.jcraft.jsch.jce.HMACSHA1;

public class HMACSHA196
extends HMACSHA1 {
    private final byte[] _buf20 = new byte[20];

    public HMACSHA196() {
        this.name = "hmac-sha1-96";
    }

    public int getBlockSize() {
        return 12;
    }

    public void doFinal(byte[] buf, int offset) {
        super.doFinal(this._buf20, 0);
        System.arraycopy(this._buf20, 0, buf, offset, 12);
    }
}


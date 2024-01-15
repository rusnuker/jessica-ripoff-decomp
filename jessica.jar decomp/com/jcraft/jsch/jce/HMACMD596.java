/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jce;

import com.jcraft.jsch.jce.HMACMD5;

public class HMACMD596
extends HMACMD5 {
    private final byte[] _buf16 = new byte[16];

    public HMACMD596() {
        this.name = "hmac-md5-96";
    }

    public int getBlockSize() {
        return 12;
    }

    public void doFinal(byte[] buf, int offset) {
        super.doFinal(this._buf16, 0);
        System.arraycopy(this._buf16, 0, buf, offset, 12);
    }
}


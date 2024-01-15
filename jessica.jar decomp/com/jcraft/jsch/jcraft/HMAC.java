/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jcraft;

import java.security.MessageDigest;

class HMAC {
    private static final int B = 64;
    private byte[] k_ipad = null;
    private byte[] k_opad = null;
    private MessageDigest md = null;
    private int bsize = 0;
    private final byte[] tmp = new byte[4];

    HMAC() {
    }

    protected void setH(MessageDigest md) {
        this.md = md;
        this.bsize = md.getDigestLength();
    }

    public int getBlockSize() {
        return this.bsize;
    }

    public void init(byte[] key) throws Exception {
        this.md.reset();
        if (key.length > this.bsize) {
            byte[] tmp = new byte[this.bsize];
            System.arraycopy(key, 0, tmp, 0, this.bsize);
            key = tmp;
        }
        if (key.length > 64) {
            this.md.update(key, 0, key.length);
            key = this.md.digest();
        }
        this.k_ipad = new byte[64];
        System.arraycopy(key, 0, this.k_ipad, 0, key.length);
        this.k_opad = new byte[64];
        System.arraycopy(key, 0, this.k_opad, 0, key.length);
        int i = 0;
        while (i < 64) {
            int n = i;
            this.k_ipad[n] = (byte)(this.k_ipad[n] ^ 0x36);
            int n2 = i++;
            this.k_opad[n2] = (byte)(this.k_opad[n2] ^ 0x5C);
        }
        this.md.update(this.k_ipad, 0, 64);
    }

    public void update(int i) {
        this.tmp[0] = (byte)(i >>> 24);
        this.tmp[1] = (byte)(i >>> 16);
        this.tmp[2] = (byte)(i >>> 8);
        this.tmp[3] = (byte)i;
        this.update(this.tmp, 0, 4);
    }

    public void update(byte[] foo, int s, int l) {
        this.md.update(foo, s, l);
    }

    public void doFinal(byte[] buf, int offset) {
        byte[] result = this.md.digest();
        this.md.update(this.k_opad, 0, 64);
        this.md.update(result, 0, this.bsize);
        try {
            this.md.digest(buf, offset, this.bsize);
        }
        catch (Exception e) {
            // empty catch block
        }
        this.md.update(this.k_ipad, 0, 64);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jce;

import com.jcraft.jsch.MAC;
import javax.crypto.Mac;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

abstract class HMAC
implements MAC {
    protected String name;
    protected int bsize;
    protected String algorithm;
    private Mac mac;
    private final byte[] tmp = new byte[4];

    HMAC() {
    }

    public int getBlockSize() {
        return this.bsize;
    }

    public void init(byte[] key) throws Exception {
        if (key.length > this.bsize) {
            byte[] tmp = new byte[this.bsize];
            System.arraycopy(key, 0, tmp, 0, this.bsize);
            key = tmp;
        }
        SecretKeySpec skey = new SecretKeySpec(key, this.algorithm);
        this.mac = Mac.getInstance(this.algorithm);
        this.mac.init(skey);
    }

    public void update(int i) {
        this.tmp[0] = (byte)(i >>> 24);
        this.tmp[1] = (byte)(i >>> 16);
        this.tmp[2] = (byte)(i >>> 8);
        this.tmp[3] = (byte)i;
        this.update(this.tmp, 0, 4);
    }

    public void update(byte[] foo, int s, int l) {
        this.mac.update(foo, s, l);
    }

    public void doFinal(byte[] buf, int offset) {
        try {
            this.mac.doFinal(buf, offset);
        }
        catch (ShortBufferException e) {
            System.err.println(e);
        }
    }

    public String getName() {
        return this.name;
    }
}


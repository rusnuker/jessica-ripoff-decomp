/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jce;

import java.security.SecureRandom;

public class Random
implements com.jcraft.jsch.Random {
    private byte[] tmp = new byte[16];
    private SecureRandom random = new SecureRandom();

    public void fill(byte[] foo, int start, int len) {
        if (len > this.tmp.length) {
            this.tmp = new byte[len];
        }
        this.random.nextBytes(this.tmp);
        System.arraycopy(this.tmp, 0, foo, start, len);
    }
}


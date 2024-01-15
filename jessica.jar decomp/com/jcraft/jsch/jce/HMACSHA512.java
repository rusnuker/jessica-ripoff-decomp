/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jce;

import com.jcraft.jsch.jce.HMAC;

public class HMACSHA512
extends HMAC {
    public HMACSHA512() {
        this.name = "hmac-sha2-512";
        this.bsize = 64;
        this.algorithm = "HmacSHA512";
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jce;

import com.jcraft.jsch.jce.HMAC;

public class HMACSHA256
extends HMAC {
    public HMACSHA256() {
        this.name = "hmac-sha2-256";
        this.bsize = 32;
        this.algorithm = "HmacSHA256";
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jce;

import com.jcraft.jsch.jce.HMAC;

public class HMACMD5
extends HMAC {
    public HMACMD5() {
        this.name = "hmac-md5";
        this.bsize = 16;
        this.algorithm = "HmacMD5";
    }
}


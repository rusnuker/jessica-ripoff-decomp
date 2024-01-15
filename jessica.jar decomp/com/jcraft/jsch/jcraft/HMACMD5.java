/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jcraft;

import com.jcraft.jsch.MAC;
import com.jcraft.jsch.jcraft.HMAC;
import java.security.MessageDigest;

public class HMACMD5
extends HMAC
implements MAC {
    private static final String name = "hmac-md5";

    public HMACMD5() {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        }
        catch (Exception e) {
            System.err.println(e);
        }
        this.setH(md);
    }

    public String getName() {
        return name;
    }
}


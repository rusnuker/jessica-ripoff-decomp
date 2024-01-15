/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jcraft;

import com.jcraft.jsch.MAC;
import com.jcraft.jsch.jcraft.HMAC;
import java.security.MessageDigest;

public class HMACSHA1
extends HMAC
implements MAC {
    private static final String name = "hmac-sha1";

    public HMACSHA1() {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
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


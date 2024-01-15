/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.JSchException;

class JSchAuthCancelException
extends JSchException {
    String method;

    JSchAuthCancelException() {
    }

    JSchAuthCancelException(String s) {
        super(s);
        this.method = s;
    }

    public String getMethod() {
        return this.method;
    }
}


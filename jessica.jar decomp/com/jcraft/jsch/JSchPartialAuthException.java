/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.JSchException;

class JSchPartialAuthException
extends JSchException {
    String methods;

    public JSchPartialAuthException() {
    }

    public JSchPartialAuthException(String s) {
        super(s);
        this.methods = s;
    }

    public String getMethods() {
        return this.methods;
    }
}


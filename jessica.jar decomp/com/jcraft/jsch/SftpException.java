/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

public class SftpException
extends Exception {
    public int id;
    private Throwable cause = null;

    public SftpException(int id, String message) {
        super(message);
        this.id = id;
    }

    public SftpException(int id, String message, Throwable e) {
        super(message);
        this.id = id;
        this.cause = e;
    }

    public String toString() {
        return this.id + ": " + this.getMessage();
    }

    public Throwable getCause() {
        return this.cause;
    }
}


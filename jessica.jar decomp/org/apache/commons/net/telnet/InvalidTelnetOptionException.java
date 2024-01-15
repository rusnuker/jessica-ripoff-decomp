/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.telnet;

public class InvalidTelnetOptionException
extends Exception {
    private int optionCode = -1;
    private String msg;

    public InvalidTelnetOptionException(String message, int optcode) {
        this.optionCode = optcode;
        this.msg = message;
    }

    public String getMessage() {
        return this.msg + ": " + this.optionCode;
    }
}


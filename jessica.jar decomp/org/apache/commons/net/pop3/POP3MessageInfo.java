/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.pop3;

public final class POP3MessageInfo {
    public int number;
    public int size;
    public String identifier;

    public POP3MessageInfo() {
        this.size = 0;
        this.number = 0;
        this.identifier = null;
    }

    public POP3MessageInfo(int num, int octets) {
        this.number = num;
        this.size = octets;
        this.identifier = null;
    }

    public POP3MessageInfo(int num, String uid) {
        this.number = num;
        this.size = -1;
        this.identifier = uid;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.xmlrpc.base;

import com.mysql.fabric.xmlrpc.base.Value;

public class Param {
    protected Value value;

    public Param() {
    }

    public Param(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return this.value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("<param>");
        sb.append(this.value.toString());
        sb.append("</param>");
        return sb.toString();
    }
}


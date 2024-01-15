/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.xmlrpc.base;

import com.mysql.fabric.xmlrpc.base.Value;

public class Member {
    protected String name;
    protected Value value;

    public Member() {
    }

    public Member(String name, Value value) {
        this.setName(name);
        this.setValue(value);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public Value getValue() {
        return this.value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<member>");
        sb.append("<name>" + this.name + "</name>");
        sb.append(this.value.toString());
        sb.append("</member>");
        return sb.toString();
    }
}


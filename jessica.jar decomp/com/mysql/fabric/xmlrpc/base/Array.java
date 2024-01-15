/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.xmlrpc.base;

import com.mysql.fabric.xmlrpc.base.Data;
import com.mysql.fabric.xmlrpc.base.Value;

public class Array {
    protected Data data;

    public Data getData() {
        return this.data;
    }

    public void setData(Data value) {
        this.data = value;
    }

    public void addValue(Value v) {
        if (this.data == null) {
            this.data = new Data();
        }
        this.data.addValue(v);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("<array>");
        sb.append(this.data.toString());
        sb.append("</array>");
        return sb.toString();
    }
}


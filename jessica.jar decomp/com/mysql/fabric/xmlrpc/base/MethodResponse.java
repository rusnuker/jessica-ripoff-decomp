/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.xmlrpc.base;

import com.mysql.fabric.xmlrpc.base.Fault;
import com.mysql.fabric.xmlrpc.base.Params;

public class MethodResponse {
    protected Params params;
    protected Fault fault;

    public Params getParams() {
        return this.params;
    }

    public void setParams(Params value) {
        this.params = value;
    }

    public Fault getFault() {
        return this.fault;
    }

    public void setFault(Fault value) {
        this.fault = value;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<methodResponse>");
        if (this.params != null) {
            sb.append(this.params.toString());
        }
        if (this.fault != null) {
            sb.append(this.fault.toString());
        }
        sb.append("</methodResponse>");
        return sb.toString();
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.xmlrpc.base;

import com.mysql.fabric.xmlrpc.base.Params;

public class MethodCall {
    protected String methodName;
    protected Params params;

    public String getMethodName() {
        return this.methodName;
    }

    public void setMethodName(String value) {
        this.methodName = value;
    }

    public Params getParams() {
        return this.params;
    }

    public void setParams(Params value) {
        this.params = value;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<methodCall>");
        sb.append("\t<methodName>" + this.methodName + "</methodName>");
        if (this.params != null) {
            sb.append(this.params.toString());
        }
        sb.append("</methodCall>");
        return sb.toString();
    }
}


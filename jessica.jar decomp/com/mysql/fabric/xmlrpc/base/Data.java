/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.xmlrpc.base;

import com.mysql.fabric.xmlrpc.base.Value;
import java.util.ArrayList;
import java.util.List;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Data {
    protected List<Value> value;

    public List<Value> getValue() {
        if (this.value == null) {
            this.value = new ArrayList<Value>();
        }
        return this.value;
    }

    public void addValue(Value v) {
        this.getValue().add(v);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.value != null) {
            sb.append("<data>");
            for (int i = 0; i < this.value.size(); ++i) {
                sb.append(this.value.get(i).toString());
            }
            sb.append("</data>");
        }
        return sb.toString();
    }
}


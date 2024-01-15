/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.xmlrpc.base;

import com.mysql.fabric.xmlrpc.base.Param;
import java.util.ArrayList;
import java.util.List;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Params {
    protected List<Param> param;

    public List<Param> getParam() {
        if (this.param == null) {
            this.param = new ArrayList<Param>();
        }
        return this.param;
    }

    public void addParam(Param p) {
        this.getParam().add(p);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.param != null) {
            sb.append("<params>");
            for (int i = 0; i < this.param.size(); ++i) {
                sb.append(this.param.get(i).toString());
            }
            sb.append("</params>");
        }
        return sb.toString();
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.xmlrpc.base;

import com.mysql.fabric.xmlrpc.base.Member;
import java.util.ArrayList;
import java.util.List;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Struct {
    protected List<Member> member;

    public List<Member> getMember() {
        if (this.member == null) {
            this.member = new ArrayList<Member>();
        }
        return this.member;
    }

    public void addMember(Member m) {
        this.getMember().add(m);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.member != null) {
            sb.append("<struct>");
            for (int i = 0; i < this.member.size(); ++i) {
                sb.append(this.member.get(i).toString());
            }
            sb.append("</struct>");
        }
        return sb.toString();
    }
}


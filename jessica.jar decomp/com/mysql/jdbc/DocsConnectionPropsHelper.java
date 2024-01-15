/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.ConnectionPropertiesImpl;

public class DocsConnectionPropsHelper
extends ConnectionPropertiesImpl {
    static final long serialVersionUID = -1580779062220390294L;

    public static void main(String[] args) throws Exception {
        System.out.println(new DocsConnectionPropsHelper().exposeAsXml());
    }
}


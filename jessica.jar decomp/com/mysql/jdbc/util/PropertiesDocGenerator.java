/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.util;

import com.mysql.jdbc.ConnectionPropertiesImpl;
import java.sql.SQLException;

public class PropertiesDocGenerator
extends ConnectionPropertiesImpl {
    static final long serialVersionUID = -4869689139143855383L;

    public static void main(String[] args) throws SQLException {
        System.out.println(new PropertiesDocGenerator().exposeAsXml());
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.util;

import com.mysql.jdbc.SQLError;

public class ErrorMappingsDocGenerator {
    public static void main(String[] args) throws Exception {
        SQLError.dumpSqlStatesMappingsAsXml();
    }
}


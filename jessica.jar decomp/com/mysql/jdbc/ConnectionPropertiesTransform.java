/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.Properties;

public interface ConnectionPropertiesTransform {
    public Properties transformProperties(Properties var1) throws SQLException;
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.NonRegisteringDriver;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class NonRegisteringReplicationDriver
extends NonRegisteringDriver {
    public Connection connect(String url, Properties info) throws SQLException {
        return this.connectReplicationConnection(url, info);
    }
}


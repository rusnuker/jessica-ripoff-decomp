/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.MySQLConnection;
import java.sql.SQLException;

public interface LoadBalancedConnection
extends MySQLConnection {
    public boolean addHost(String var1) throws SQLException;

    public void removeHost(String var1) throws SQLException;

    public void removeHostWhenNotInUse(String var1) throws SQLException;

    public void ping(boolean var1) throws SQLException;
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlPooledConnection;
import java.sql.SQLException;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

public class MysqlConnectionPoolDataSource
extends MysqlDataSource
implements ConnectionPoolDataSource {
    static final long serialVersionUID = -7767325445592304961L;

    public synchronized PooledConnection getPooledConnection() throws SQLException {
        java.sql.Connection connection = this.getConnection();
        MysqlPooledConnection mysqlPooledConnection = MysqlPooledConnection.getInstance((Connection)connection);
        return mysqlPooledConnection;
    }

    public synchronized PooledConnection getPooledConnection(String s, String s1) throws SQLException {
        java.sql.Connection connection = this.getConnection(s, s1);
        MysqlPooledConnection mysqlPooledConnection = MysqlPooledConnection.getInstance((Connection)connection);
        return mysqlPooledConnection;
    }
}


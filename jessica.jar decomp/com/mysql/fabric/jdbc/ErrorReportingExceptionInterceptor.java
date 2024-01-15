/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.jdbc;

import com.mysql.fabric.FabricCommunicationException;
import com.mysql.fabric.jdbc.FabricMySQLConnectionProxy;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.SQLError;
import java.sql.SQLException;
import java.util.Properties;

public class ErrorReportingExceptionInterceptor
implements ExceptionInterceptor {
    private String hostname;
    private String port;
    private String fabricHaGroup;

    public SQLException interceptException(SQLException sqlEx, Connection conn) {
        MySQLConnection mysqlConn = (MySQLConnection)conn;
        if (ConnectionImpl.class.isAssignableFrom(mysqlConn.getMultiHostSafeProxy().getClass())) {
            return null;
        }
        FabricMySQLConnectionProxy fabricProxy = (FabricMySQLConnectionProxy)mysqlConn.getMultiHostSafeProxy();
        try {
            return fabricProxy.interceptException(sqlEx, conn, this.fabricHaGroup, this.hostname, this.port);
        }
        catch (FabricCommunicationException ex) {
            return SQLError.createSQLException("Failed to report error to Fabric.", "08S01", ex, null);
        }
    }

    public void init(Connection conn, Properties props) throws SQLException {
        this.hostname = props.getProperty("HOST");
        this.port = props.getProperty("PORT");
        String connectionAttributes = props.getProperty("connectionAttributes");
        this.fabricHaGroup = connectionAttributes.replaceAll("^.*\\bfabricHaGroup:(.+)\\b.*$", "$1");
    }

    public void destroy() {
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.JDBC4PreparedStatementHelper;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.PreparedStatement;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;

public class JDBC4PreparedStatement
extends PreparedStatement {
    public JDBC4PreparedStatement(MySQLConnection conn, String catalog) throws SQLException {
        super(conn, catalog);
    }

    public JDBC4PreparedStatement(MySQLConnection conn, String sql, String catalog) throws SQLException {
        super(conn, sql, catalog);
    }

    public JDBC4PreparedStatement(MySQLConnection conn, String sql, String catalog, PreparedStatement.ParseInfo cachedParseInfo) throws SQLException {
        super(conn, sql, catalog, cachedParseInfo);
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        JDBC4PreparedStatementHelper.setRowId(this, parameterIndex, x);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        JDBC4PreparedStatementHelper.setNClob((PreparedStatement)this, parameterIndex, value);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        JDBC4PreparedStatementHelper.setSQLXML(this, parameterIndex, xmlObject);
    }
}


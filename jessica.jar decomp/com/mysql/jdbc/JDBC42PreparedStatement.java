/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.JDBC42Helper;
import com.mysql.jdbc.JDBC4PreparedStatement;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLType;

public class JDBC42PreparedStatement
extends JDBC4PreparedStatement {
    public JDBC42PreparedStatement(MySQLConnection conn, String catalog) throws SQLException {
        super(conn, catalog);
    }

    public JDBC42PreparedStatement(MySQLConnection conn, String sql, String catalog) throws SQLException {
        super(conn, sql, catalog);
    }

    public JDBC42PreparedStatement(MySQLConnection conn, String sql, String catalog, PreparedStatement.ParseInfo cachedParseInfo) throws SQLException {
        super(conn, sql, catalog, cachedParseInfo);
    }

    private int checkSqlType(int sqlType) throws SQLException {
        return JDBC42Helper.checkSqlType(sqlType, this.getExceptionInterceptor());
    }

    private int translateAndCheckSqlType(SQLType sqlType) throws SQLException {
        return JDBC42Helper.translateAndCheckSqlType(sqlType, this.getExceptionInterceptor());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            super.setObject(parameterIndex, JDBC42Helper.convertJavaTimeToJavaSql(x));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            super.setObject(parameterIndex, JDBC42Helper.convertJavaTimeToJavaSql(x), this.checkSqlType(targetSqlType));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            super.setObject(parameterIndex, JDBC42Helper.convertJavaTimeToJavaSql(x), this.checkSqlType(targetSqlType), scaleOrLength);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            super.setObject(parameterIndex, JDBC42Helper.convertJavaTimeToJavaSql(x), this.translateAndCheckSqlType(targetSqlType));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            super.setObject(parameterIndex, JDBC42Helper.convertJavaTimeToJavaSql(x), this.translateAndCheckSqlType(targetSqlType), scaleOrLength);
        }
    }
}


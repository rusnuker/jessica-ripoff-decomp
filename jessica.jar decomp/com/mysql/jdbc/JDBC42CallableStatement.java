/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.CallableStatement;
import com.mysql.jdbc.JDBC42Helper;
import com.mysql.jdbc.JDBC4CallableStatement;
import com.mysql.jdbc.MySQLConnection;
import java.sql.SQLException;
import java.sql.SQLType;

public class JDBC42CallableStatement
extends JDBC4CallableStatement {
    public JDBC42CallableStatement(MySQLConnection conn, CallableStatement.CallableStatementParamInfo paramInfo) throws SQLException {
        super(conn, paramInfo);
    }

    public JDBC42CallableStatement(MySQLConnection conn, String sql, String catalog, boolean isFunctionCall) throws SQLException {
        super(conn, sql, catalog, isFunctionCall);
    }

    private int checkSqlType(int sqlType) throws SQLException {
        return JDBC42Helper.checkSqlType(sqlType, this.getExceptionInterceptor());
    }

    private int translateAndCheckSqlType(SQLType sqlType) throws SQLException {
        return JDBC42Helper.translateAndCheckSqlType(sqlType, this.getExceptionInterceptor());
    }

    @Override
    public void registerOutParameter(int parameterIndex, SQLType sqlType) throws SQLException {
        super.registerOutParameter(parameterIndex, this.translateAndCheckSqlType(sqlType));
    }

    @Override
    public void registerOutParameter(int parameterIndex, SQLType sqlType, int scale) throws SQLException {
        super.registerOutParameter(parameterIndex, this.translateAndCheckSqlType(sqlType), scale);
    }

    @Override
    public void registerOutParameter(int parameterIndex, SQLType sqlType, String typeName) throws SQLException {
        super.registerOutParameter(parameterIndex, this.translateAndCheckSqlType(sqlType), typeName);
    }

    @Override
    public void registerOutParameter(String parameterName, SQLType sqlType) throws SQLException {
        super.registerOutParameter(parameterName, this.translateAndCheckSqlType(sqlType));
    }

    @Override
    public void registerOutParameter(String parameterName, SQLType sqlType, int scale) throws SQLException {
        super.registerOutParameter(parameterName, this.translateAndCheckSqlType(sqlType), scale);
    }

    @Override
    public void registerOutParameter(String parameterName, SQLType sqlType, String typeName) throws SQLException {
        super.registerOutParameter(parameterName, this.translateAndCheckSqlType(sqlType), typeName);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setObject(String parameterName, Object x, SQLType targetSqlType) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            super.setObject(parameterName, JDBC42Helper.convertJavaTimeToJavaSql(x), this.translateAndCheckSqlType(targetSqlType));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setObject(String parameterName, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            super.setObject(parameterName, JDBC42Helper.convertJavaTimeToJavaSql(x), this.translateAndCheckSqlType(targetSqlType), scaleOrLength);
        }
    }
}


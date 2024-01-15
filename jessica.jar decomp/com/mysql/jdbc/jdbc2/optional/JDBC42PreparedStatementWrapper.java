/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.jdbc2.optional.ConnectionWrapper;
import com.mysql.jdbc.jdbc2.optional.JDBC4PreparedStatementWrapper;
import com.mysql.jdbc.jdbc2.optional.MysqlPooledConnection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLType;

public class JDBC42PreparedStatementWrapper
extends JDBC4PreparedStatementWrapper {
    public JDBC42PreparedStatementWrapper(ConnectionWrapper c, MysqlPooledConnection conn, PreparedStatement toWrap) {
        super(c, conn, toWrap);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setObject(parameterIndex, x, targetSqlType);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setObject(parameterIndex, x, targetSqlType, scaleOrLength);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
}


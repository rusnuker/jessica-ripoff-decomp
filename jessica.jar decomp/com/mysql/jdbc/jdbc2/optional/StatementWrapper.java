/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StatementImpl;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.jdbc2.optional.ConnectionWrapper;
import com.mysql.jdbc.jdbc2.optional.MysqlPooledConnection;
import com.mysql.jdbc.jdbc2.optional.WrapperBase;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;

public class StatementWrapper
extends WrapperBase
implements java.sql.Statement {
    private static final Constructor<?> JDBC_4_STATEMENT_WRAPPER_CTOR;
    protected java.sql.Statement wrappedStmt;
    protected ConnectionWrapper wrappedConn;

    protected static StatementWrapper getInstance(ConnectionWrapper c, MysqlPooledConnection conn, java.sql.Statement toWrap) throws SQLException {
        if (!Util.isJdbc4()) {
            return new StatementWrapper(c, conn, toWrap);
        }
        return (StatementWrapper)Util.handleNewInstance(JDBC_4_STATEMENT_WRAPPER_CTOR, new Object[]{c, conn, toWrap}, conn.getExceptionInterceptor());
    }

    public StatementWrapper(ConnectionWrapper c, MysqlPooledConnection conn, java.sql.Statement toWrap) {
        super(conn);
        this.wrappedStmt = toWrap;
        this.wrappedConn = c;
    }

    public Connection getConnection() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedConn;
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    public void setCursorName(String name) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
            }
            this.wrappedStmt.setCursorName(name);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
            }
            this.wrappedStmt.setEscapeProcessing(enable);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setFetchDirection(int direction) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
            }
            this.wrappedStmt.setFetchDirection(direction);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public int getFetchDirection() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getFetchDirection();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 1000;
        }
    }

    public void setFetchSize(int rows) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
            }
            this.wrappedStmt.setFetchSize(rows);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public int getFetchSize() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getFetchSize();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getGeneratedKeys();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    public void setMaxFieldSize(int max) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
            }
            this.wrappedStmt.setMaxFieldSize(max);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public int getMaxFieldSize() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getMaxFieldSize();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }

    public void setMaxRows(int max) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
            }
            this.wrappedStmt.setMaxRows(max);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public int getMaxRows() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getMaxRows();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }

    public boolean getMoreResults() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getMoreResults();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return false;
        }
    }

    public boolean getMoreResults(int current) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getMoreResults(current);
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return false;
        }
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
            }
            this.wrappedStmt.setQueryTimeout(seconds);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public int getQueryTimeout() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getQueryTimeout();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }

    public ResultSet getResultSet() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                ResultSet rs = this.wrappedStmt.getResultSet();
                if (rs != null) {
                    ((ResultSetInternalMethods)rs).setWrapperStatement(this);
                }
                return rs;
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    public int getResultSetConcurrency() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getResultSetConcurrency();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }

    public int getResultSetHoldability() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getResultSetHoldability();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 1;
        }
    }

    public int getResultSetType() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getResultSetType();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 1003;
        }
    }

    public int getUpdateCount() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getUpdateCount();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return -1;
        }
    }

    public SQLWarning getWarnings() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getWarnings();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    public void addBatch(String sql) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.addBatch(sql);
            }
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void cancel() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.cancel();
            }
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void clearBatch() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.clearBatch();
            }
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void clearWarnings() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.clearWarnings();
            }
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() throws SQLException {
        try {
            block4: {
                try {
                    if (this.wrappedStmt == null) break block4;
                    this.wrappedStmt.close();
                }
                catch (SQLException sqlEx) {
                    this.checkAndFireConnectionError(sqlEx);
                    Object var3_2 = null;
                    this.wrappedStmt = null;
                    this.pooledConnection = null;
                }
            }
            Object var3_1 = null;
            this.wrappedStmt = null;
            this.pooledConnection = null;
        }
        catch (Throwable throwable) {
            Object var3_3 = null;
            this.wrappedStmt = null;
            this.pooledConnection = null;
            throw throwable;
        }
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.execute(sql, autoGeneratedKeys);
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return false;
        }
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.execute(sql, columnIndexes);
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return false;
        }
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.execute(sql, columnNames);
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return false;
        }
    }

    public boolean execute(String sql) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.execute(sql);
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return false;
        }
    }

    public int[] executeBatch() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.executeBatch();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        ResultSet rs = null;
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
            }
            rs = this.wrappedStmt.executeQuery(sql);
            ((ResultSetInternalMethods)rs).setWrapperStatement(this);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
        return rs;
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.executeUpdate(sql, autoGeneratedKeys);
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return -1;
        }
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.executeUpdate(sql, columnIndexes);
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return -1;
        }
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.executeUpdate(sql, columnNames);
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return -1;
        }
    }

    public int executeUpdate(String sql) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.executeUpdate(sql);
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return -1;
        }
    }

    public void enableStreamingResults() throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((Statement)this.wrappedStmt).enableStreamingResults();
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public long[] executeLargeBatch() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((StatementImpl)this.wrappedStmt).executeLargeBatch();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    public long executeLargeUpdate(String sql) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((StatementImpl)this.wrappedStmt).executeLargeUpdate(sql);
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return -1L;
        }
    }

    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((StatementImpl)this.wrappedStmt).executeLargeUpdate(sql, autoGeneratedKeys);
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return -1L;
        }
    }

    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((StatementImpl)this.wrappedStmt).executeLargeUpdate(sql, columnIndexes);
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return -1L;
        }
    }

    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((StatementImpl)this.wrappedStmt).executeLargeUpdate(sql, columnNames);
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return -1L;
        }
    }

    public long getLargeMaxRows() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((StatementImpl)this.wrappedStmt).getLargeMaxRows();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0L;
        }
    }

    public long getLargeUpdateCount() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((StatementImpl)this.wrappedStmt).getLargeUpdateCount();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return -1L;
        }
    }

    public void setLargeMaxRows(long max) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
            }
            ((StatementImpl)this.wrappedStmt).setLargeMaxRows(max);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    static {
        if (Util.isJdbc4()) {
            try {
                JDBC_4_STATEMENT_WRAPPER_CTOR = Class.forName("com.mysql.jdbc.jdbc2.optional.JDBC4StatementWrapper").getConstructor(ConnectionWrapper.class, MysqlPooledConnection.class, java.sql.Statement.class);
            }
            catch (SecurityException e) {
                throw new RuntimeException(e);
            }
            catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            JDBC_4_STATEMENT_WRAPPER_CTOR = null;
        }
    }
}


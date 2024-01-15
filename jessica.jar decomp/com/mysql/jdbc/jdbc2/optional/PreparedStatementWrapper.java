/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.jdbc2.optional.ConnectionWrapper;
import com.mysql.jdbc.jdbc2.optional.MysqlPooledConnection;
import com.mysql.jdbc.jdbc2.optional.StatementWrapper;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class PreparedStatementWrapper
extends StatementWrapper
implements java.sql.PreparedStatement {
    private static final Constructor<?> JDBC_4_PREPARED_STATEMENT_WRAPPER_CTOR;

    protected static PreparedStatementWrapper getInstance(ConnectionWrapper c, MysqlPooledConnection conn, java.sql.PreparedStatement toWrap) throws SQLException {
        if (!Util.isJdbc4()) {
            return new PreparedStatementWrapper(c, conn, toWrap);
        }
        return (PreparedStatementWrapper)Util.handleNewInstance(JDBC_4_PREPARED_STATEMENT_WRAPPER_CTOR, new Object[]{c, conn, toWrap}, conn.getExceptionInterceptor());
    }

    PreparedStatementWrapper(ConnectionWrapper c, MysqlPooledConnection conn, java.sql.PreparedStatement toWrap) {
        super(c, conn, toWrap);
    }

    public void setArray(int parameterIndex, Array x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setArray(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setAsciiStream(parameterIndex, x, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setBigDecimal(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setBinaryStream(parameterIndex, x, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setBlob(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setBoolean(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setByte(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setBytes(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setCharacterStream(parameterIndex, reader, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setClob(int parameterIndex, Clob x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setClob(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setDate(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setDate(parameterIndex, x, cal);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setDouble(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setFloat(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setInt(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setLong(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((java.sql.PreparedStatement)this.wrappedStmt).getMetaData();
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setNull(parameterIndex, sqlType);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setNull(parameterIndex, sqlType, typeName);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setObject(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setObject(parameterIndex, x, targetSqlType);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setObject(parameterIndex, x, targetSqlType, scale);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((java.sql.PreparedStatement)this.wrappedStmt).getParameterMetaData();
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    public void setRef(int parameterIndex, Ref x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setRef(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setShort(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setString(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setTime(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setTime(parameterIndex, x, cal);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setTimestamp(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setTimestamp(parameterIndex, x, cal);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setURL(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).setUnicodeStream(parameterIndex, x, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void addBatch() throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).addBatch();
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public void clearParameters() throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((java.sql.PreparedStatement)this.wrappedStmt).clearParameters();
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    public boolean execute() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((java.sql.PreparedStatement)this.wrappedStmt).execute();
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return false;
        }
    }

    public ResultSet executeQuery() throws SQLException {
        ResultSet rs = null;
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            rs = ((java.sql.PreparedStatement)this.wrappedStmt).executeQuery();
            ((ResultSetInternalMethods)rs).setWrapperStatement(this);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
        return rs;
    }

    public int executeUpdate() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((java.sql.PreparedStatement)this.wrappedStmt).executeUpdate();
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return -1;
        }
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString());
        if (this.wrappedStmt != null) {
            buf.append(": ");
            try {
                buf.append(((PreparedStatement)this.wrappedStmt).asSql());
            }
            catch (SQLException sqlEx) {
                buf.append("EXCEPTION: " + sqlEx.toString());
            }
        }
        return buf.toString();
    }

    public long executeLargeUpdate() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((PreparedStatement)this.wrappedStmt).executeLargeUpdate();
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return -1L;
        }
    }

    static {
        if (Util.isJdbc4()) {
            try {
                String jdbc4ClassName = Util.isJdbc42() ? "com.mysql.jdbc.jdbc2.optional.JDBC42PreparedStatementWrapper" : "com.mysql.jdbc.jdbc2.optional.JDBC4PreparedStatementWrapper";
                JDBC_4_PREPARED_STATEMENT_WRAPPER_CTOR = Class.forName(jdbc4ClassName).getConstructor(ConnectionWrapper.class, MysqlPooledConnection.class, java.sql.PreparedStatement.class);
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
            JDBC_4_PREPARED_STATEMENT_WRAPPER_CTOR = null;
        }
    }
}


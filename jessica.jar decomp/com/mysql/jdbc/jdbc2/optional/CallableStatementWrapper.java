/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.jdbc2.optional.ConnectionWrapper;
import com.mysql.jdbc.jdbc2.optional.MysqlPooledConnection;
import com.mysql.jdbc.jdbc2.optional.PreparedStatementWrapper;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CallableStatementWrapper
extends PreparedStatementWrapper
implements CallableStatement {
    private static final Constructor<?> JDBC_4_CALLABLE_STATEMENT_WRAPPER_CTOR;

    protected static CallableStatementWrapper getInstance(ConnectionWrapper c, MysqlPooledConnection conn, CallableStatement toWrap) throws SQLException {
        if (!Util.isJdbc4()) {
            return new CallableStatementWrapper(c, conn, toWrap);
        }
        return (CallableStatementWrapper)Util.handleNewInstance(JDBC_4_CALLABLE_STATEMENT_WRAPPER_CTOR, new Object[]{c, conn, toWrap}, conn.getExceptionInterceptor());
    }

    public CallableStatementWrapper(ConnectionWrapper c, MysqlPooledConnection conn, CallableStatement toWrap) {
        super(c, conn, toWrap);
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).registerOutParameter(parameterIndex, sqlType);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).registerOutParameter(parameterIndex, sqlType, scale);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public boolean wasNull() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).wasNull();
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return false;
        }
    }

    @Override
    public String getString(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getString(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public boolean getBoolean(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBoolean(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return false;
        }
    }

    @Override
    public byte getByte(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getByte(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }

    @Override
    public short getShort(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getShort(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }

    @Override
    public int getInt(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getInt(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }

    @Override
    public long getLong(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getLong(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0L;
        }
    }

    @Override
    public float getFloat(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getFloat(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0.0f;
        }
    }

    @Override
    public double getDouble(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getDouble(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0.0;
        }
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBigDecimal(parameterIndex, scale);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public byte[] getBytes(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBytes(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Date getDate(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getDate(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Time getTime(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getTime(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getTimestamp(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Object getObject(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getObject(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBigDecimal(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Object getObject(int parameterIndex, Map<String, Class<?>> typeMap) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getObject(parameterIndex, typeMap);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Ref getRef(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getRef(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Blob getBlob(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBlob(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Clob getClob(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getClob(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Array getArray(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getArray(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getDate(parameterIndex, cal);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getTime(parameterIndex, cal);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getTimestamp(parameterIndex, cal);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public void registerOutParameter(int paramIndex, int sqlType, String typeName) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).registerOutParameter(paramIndex, sqlType, typeName);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).registerOutParameter(parameterName, sqlType);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).registerOutParameter(parameterName, sqlType, scale);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).registerOutParameter(parameterName, sqlType, typeName);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public URL getURL(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getURL(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public void setURL(String parameterName, URL val) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setURL(parameterName, val);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setNull(String parameterName, int sqlType) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setNull(parameterName, sqlType);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setBoolean(String parameterName, boolean x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setBoolean(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setByte(String parameterName, byte x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setByte(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setShort(String parameterName, short x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setShort(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setInt(String parameterName, int x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setInt(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setLong(String parameterName, long x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setLong(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setFloat(String parameterName, float x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setFloat(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setDouble(String parameterName, double x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setDouble(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setBigDecimal(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setString(String parameterName, String x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setString(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setBytes(String parameterName, byte[] x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setBytes(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setDate(String parameterName, Date x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setDate(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setTime(String parameterName, Time x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setTime(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setTimestamp(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setAsciiStream(parameterName, x, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setBinaryStream(parameterName, x, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setObject(parameterName, x, targetSqlType, scale);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setObject(parameterName, x, targetSqlType);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setObject(String parameterName, Object x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setObject(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setCharacterStream(parameterName, reader, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setDate(parameterName, x, cal);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setTime(parameterName, x, cal);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setTimestamp(parameterName, x, cal);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setNull(parameterName, sqlType, typeName);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public String getString(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getString(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public boolean getBoolean(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBoolean(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return false;
        }
    }

    @Override
    public byte getByte(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getByte(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }

    @Override
    public short getShort(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getShort(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }

    @Override
    public int getInt(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getInt(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }

    @Override
    public long getLong(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getLong(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0L;
        }
    }

    @Override
    public float getFloat(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getFloat(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0.0f;
        }
    }

    @Override
    public double getDouble(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getDouble(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0.0;
        }
    }

    @Override
    public byte[] getBytes(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBytes(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Date getDate(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getDate(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Time getTime(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getTime(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Timestamp getTimestamp(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getTimestamp(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Object getObject(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getObject(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBigDecimal(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Object getObject(String parameterName, Map<String, Class<?>> typeMap) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getObject(parameterName, typeMap);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Ref getRef(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getRef(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Blob getBlob(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBlob(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Clob getClob(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getClob(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Array getArray(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getArray(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Date getDate(String parameterName, Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getDate(parameterName, cal);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Time getTime(String parameterName, Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getTime(parameterName, cal);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getTimestamp(parameterName, cal);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public URL getURL(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getURL(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    static {
        if (Util.isJdbc4()) {
            try {
                String jdbc4ClassName = Util.isJdbc42() ? "com.mysql.jdbc.jdbc2.optional.JDBC42CallableStatementWrapper" : "com.mysql.jdbc.jdbc2.optional.JDBC4CallableStatementWrapper";
                JDBC_4_CALLABLE_STATEMENT_WRAPPER_CTOR = Class.forName(jdbc4ClassName).getConstructor(ConnectionWrapper.class, MysqlPooledConnection.class, CallableStatement.class);
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
            JDBC_4_CALLABLE_STATEMENT_WRAPPER_CTOR = null;
        }
    }
}


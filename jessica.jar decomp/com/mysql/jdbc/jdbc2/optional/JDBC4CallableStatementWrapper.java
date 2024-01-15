/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.jdbc2.optional.CallableStatementWrapper;
import com.mysql.jdbc.jdbc2.optional.ConnectionWrapper;
import com.mysql.jdbc.jdbc2.optional.MysqlPooledConnection;
import com.mysql.jdbc.jdbc2.optional.WrapperBase;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Proxy;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.HashMap;

public class JDBC4CallableStatementWrapper
extends CallableStatementWrapper {
    public JDBC4CallableStatementWrapper(ConnectionWrapper c, MysqlPooledConnection conn, CallableStatement toWrap) {
        super(c, conn, toWrap);
    }

    @Override
    public void close() throws SQLException {
        try {
            super.close();
        }
        finally {
            this.unwrappedInterfaces = null;
        }
    }

    @Override
    public boolean isClosed() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.isClosed();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return false;
        }
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
            }
            this.wrappedStmt.setPoolable(poolable);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public boolean isPoolable() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.isPoolable();
            }
            throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return false;
        }
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setRowId(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setNClob(parameterIndex, value);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setSQLXML(parameterIndex, xmlObject);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setNString(parameterIndex, value);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setNCharacterStream(parameterIndex, value, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setClob(parameterIndex, reader, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setBlob(parameterIndex, inputStream, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setNClob(parameterIndex, reader, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setAsciiStream(parameterIndex, x, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setBinaryStream(parameterIndex, x, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setCharacterStream(parameterIndex, reader, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setAsciiStream(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setBinaryStream(parameterIndex, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setCharacterStream(parameterIndex, reader);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setNCharacterStream(parameterIndex, value);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setClob(parameterIndex, reader);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setBlob(parameterIndex, inputStream);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((PreparedStatement)this.wrappedStmt).setNClob(parameterIndex, reader);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        boolean isInstance = iface.isInstance(this);
        if (isInstance) {
            return true;
        }
        String interfaceClassName = iface.getName();
        return interfaceClassName.equals("com.mysql.jdbc.Statement") || interfaceClassName.equals("java.sql.Statement") || interfaceClassName.equals("java.sql.PreparedStatement") || interfaceClassName.equals("java.sql.Wrapper");
    }

    @Override
    public synchronized <T> T unwrap(Class<T> iface) throws SQLException {
        try {
            Object cachedUnwrapped;
            if ("java.sql.Statement".equals(iface.getName()) || "java.sql.PreparedStatement".equals(iface.getName()) || "java.sql.Wrapper.class".equals(iface.getName())) {
                return iface.cast(this);
            }
            if (this.unwrappedInterfaces == null) {
                this.unwrappedInterfaces = new HashMap();
            }
            if ((cachedUnwrapped = this.unwrappedInterfaces.get(iface)) == null) {
                if (cachedUnwrapped == null) {
                    cachedUnwrapped = Proxy.newProxyInstance(this.wrappedStmt.getClass().getClassLoader(), new Class[]{iface}, new WrapperBase.ConnectionErrorFiringInvocationHandler(this.wrappedStmt));
                    this.unwrappedInterfaces.put(iface, cachedUnwrapped);
                }
                this.unwrappedInterfaces.put(iface, cachedUnwrapped);
            }
            return iface.cast(cachedUnwrapped);
        }
        catch (ClassCastException cce) {
            throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", this.exceptionInterceptor);
        }
    }

    @Override
    public void setRowId(String parameterName, RowId x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setRowId(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setSQLXML(parameterName, xmlObject);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getSQLXML(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public SQLXML getSQLXML(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getSQLXML(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public RowId getRowId(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getRowId(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public void setNClob(String parameterName, NClob value) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setNClob(parameterName, value);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setNClob(String parameterName, Reader reader) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setNClob(parameterName, reader);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setNClob(parameterName, reader, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setNString(String parameterName, String value) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setNString(parameterName, value);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getCharacterStream(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Reader getCharacterStream(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getCharacterStream(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getNCharacterStream(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public Reader getNCharacterStream(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getNCharacterStream(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public NClob getNClob(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getNClob(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public String getNString(String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getNString(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setAsciiStream(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
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
    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setBinaryStream(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
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
    public void setBlob(String parameterName, InputStream x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setBlob(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setBlob(String parameterName, InputStream x, long length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setBlob(parameterName, x, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setBlob(String parameterName, Blob x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setBlob(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setCharacterStream(parameterName, reader);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
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
    public void setClob(String parameterName, Clob x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setClob(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setClob(String parameterName, Reader reader) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setClob(parameterName, reader);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setClob(parameterName, reader, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader reader) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setNCharacterStream(parameterName, reader);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setNCharacterStream(parameterName, reader, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }

    @Override
    public NClob getNClob(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getNClob(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public String getNString(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getNString(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }

    @Override
    public RowId getRowId(int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getRowId(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
}


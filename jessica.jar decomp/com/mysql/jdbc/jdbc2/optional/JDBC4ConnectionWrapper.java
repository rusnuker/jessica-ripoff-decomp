/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.jdbc2.optional.ConnectionWrapper;
import com.mysql.jdbc.jdbc2.optional.MysqlPooledConnection;
import com.mysql.jdbc.jdbc2.optional.WrapperBase;
import java.lang.reflect.Proxy;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Properties;

public class JDBC4ConnectionWrapper
extends ConnectionWrapper {
    public JDBC4ConnectionWrapper(MysqlPooledConnection mysqlPooledConnection, Connection mysqlConnection, boolean forXa) throws SQLException {
        super(mysqlPooledConnection, mysqlConnection, forXa);
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
    public SQLXML createSQLXML() throws SQLException {
        this.checkClosed();
        try {
            return this.mc.createSQLXML();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        this.checkClosed();
        try {
            return this.mc.createArrayOf(typeName, elements);
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        this.checkClosed();
        try {
            return this.mc.createStruct(typeName, attributes);
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        this.checkClosed();
        try {
            return this.mc.getClientInfo();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        this.checkClosed();
        try {
            return this.mc.getClientInfo(name);
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }

    @Override
    public synchronized boolean isValid(int timeout) throws SQLException {
        try {
            return this.mc.isValid(timeout);
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return false;
        }
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        try {
            this.checkClosed();
            this.mc.setClientInfo(properties);
        }
        catch (SQLException sqlException) {
            try {
                this.checkAndFireConnectionError(sqlException);
            }
            catch (SQLException sqlEx2) {
                SQLClientInfoException clientEx = new SQLClientInfoException();
                clientEx.initCause(sqlEx2);
                throw clientEx;
            }
        }
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        try {
            this.checkClosed();
            this.mc.setClientInfo(name, value);
        }
        catch (SQLException sqlException) {
            try {
                this.checkAndFireConnectionError(sqlException);
            }
            catch (SQLException sqlEx2) {
                SQLClientInfoException clientEx = new SQLClientInfoException();
                clientEx.initCause(sqlEx2);
                throw clientEx;
            }
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        this.checkClosed();
        boolean isInstance = iface.isInstance(this);
        if (isInstance) {
            return true;
        }
        return iface.getName().equals("com.mysql.jdbc.Connection") || iface.getName().equals("com.mysql.jdbc.ConnectionProperties");
    }

    @Override
    public synchronized <T> T unwrap(Class<T> iface) throws SQLException {
        try {
            Object cachedUnwrapped;
            if ("java.sql.Connection".equals(iface.getName()) || "java.sql.Wrapper.class".equals(iface.getName())) {
                return iface.cast(this);
            }
            if (this.unwrappedInterfaces == null) {
                this.unwrappedInterfaces = new HashMap();
            }
            if ((cachedUnwrapped = this.unwrappedInterfaces.get(iface)) == null) {
                cachedUnwrapped = Proxy.newProxyInstance(this.mc.getClass().getClassLoader(), new Class[]{iface}, new WrapperBase.ConnectionErrorFiringInvocationHandler(this.mc));
                this.unwrappedInterfaces.put(iface, cachedUnwrapped);
            }
            return iface.cast(cachedUnwrapped);
        }
        catch (ClassCastException cce) {
            throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", this.exceptionInterceptor);
        }
    }

    @Override
    public Blob createBlob() throws SQLException {
        this.checkClosed();
        try {
            return this.mc.createBlob();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }

    @Override
    public Clob createClob() throws SQLException {
        this.checkClosed();
        try {
            return this.mc.createClob();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }

    @Override
    public NClob createNClob() throws SQLException {
        this.checkClosed();
        try {
            return this.mc.createNClob();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
}


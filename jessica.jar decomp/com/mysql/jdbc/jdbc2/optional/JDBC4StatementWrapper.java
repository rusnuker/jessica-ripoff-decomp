/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.jdbc2.optional.ConnectionWrapper;
import com.mysql.jdbc.jdbc2.optional.MysqlPooledConnection;
import com.mysql.jdbc.jdbc2.optional.StatementWrapper;
import com.mysql.jdbc.jdbc2.optional.WrapperBase;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class JDBC4StatementWrapper
extends StatementWrapper {
    public JDBC4StatementWrapper(ConnectionWrapper c, MysqlPooledConnection conn, Statement toWrap) {
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
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        boolean isInstance = iface.isInstance(this);
        if (isInstance) {
            return true;
        }
        String interfaceClassName = iface.getName();
        return interfaceClassName.equals("com.mysql.jdbc.Statement") || interfaceClassName.equals("java.sql.Statement") || interfaceClassName.equals("java.sql.Wrapper");
    }

    @Override
    public synchronized <T> T unwrap(Class<T> iface) throws SQLException {
        try {
            Object cachedUnwrapped;
            if ("java.sql.Statement".equals(iface.getName()) || "java.sql.Wrapper.class".equals(iface.getName())) {
                return iface.cast(this);
            }
            if (this.unwrappedInterfaces == null) {
                this.unwrappedInterfaces = new HashMap();
            }
            if ((cachedUnwrapped = this.unwrappedInterfaces.get(iface)) == null) {
                cachedUnwrapped = Proxy.newProxyInstance(this.wrappedStmt.getClass().getClassLoader(), new Class[]{iface}, new WrapperBase.ConnectionErrorFiringInvocationHandler(this.wrappedStmt));
                this.unwrappedInterfaces.put(iface, cachedUnwrapped);
            }
            return iface.cast(cachedUnwrapped);
        }
        catch (ClassCastException cce) {
            throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", this.exceptionInterceptor);
        }
    }
}


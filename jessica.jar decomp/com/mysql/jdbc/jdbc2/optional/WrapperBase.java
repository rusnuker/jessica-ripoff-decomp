/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.jdbc2.optional.MysqlPooledConnection;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Map;

abstract class WrapperBase {
    protected MysqlPooledConnection pooledConnection;
    protected Map<Class<?>, Object> unwrappedInterfaces = null;
    protected ExceptionInterceptor exceptionInterceptor;

    protected void checkAndFireConnectionError(SQLException sqlEx) throws SQLException {
        if (this.pooledConnection != null && "08S01".equals(sqlEx.getSQLState())) {
            this.pooledConnection.callConnectionEventListeners(1, sqlEx);
        }
        throw sqlEx;
    }

    protected WrapperBase(MysqlPooledConnection pooledConnection) {
        this.pooledConnection = pooledConnection;
        this.exceptionInterceptor = this.pooledConnection.getExceptionInterceptor();
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    protected class ConnectionErrorFiringInvocationHandler
    implements InvocationHandler {
        Object invokeOn = null;

        public ConnectionErrorFiringInvocationHandler(Object toInvokeOn) {
            this.invokeOn = toInvokeOn;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = null;
            try {
                result = method.invoke(this.invokeOn, args);
                if (result != null) {
                    result = this.proxyIfInterfaceIsJdbc(result, result.getClass());
                }
            }
            catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof SQLException) {
                    WrapperBase.this.checkAndFireConnectionError((SQLException)e.getTargetException());
                }
                throw e;
            }
            return result;
        }

        private Object proxyIfInterfaceIsJdbc(Object toProxy, Class<?> clazz) {
            int i$ = 0;
            Class<?>[] interfaces = clazz.getInterfaces();
            Class<?>[] arr$ = interfaces;
            int len$ = arr$.length;
            if (i$ < len$) {
                Class<?> iclass = arr$[i$];
                String packageName = iclass.getPackage().getName();
                if ("java.sql".equals(packageName) || "javax.sql".equals(packageName)) {
                    return Proxy.newProxyInstance(toProxy.getClass().getClassLoader(), interfaces, new ConnectionErrorFiringInvocationHandler(toProxy));
                }
                return this.proxyIfInterfaceIsJdbc(toProxy, iclass);
            }
            return toProxy;
        }
    }
}


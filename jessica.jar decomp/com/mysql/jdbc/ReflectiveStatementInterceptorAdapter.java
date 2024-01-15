/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StatementInterceptor;
import com.mysql.jdbc.StatementInterceptorV2;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Properties;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ReflectiveStatementInterceptorAdapter
implements StatementInterceptorV2 {
    private final StatementInterceptor toProxy;
    final Method v2PostProcessMethod;

    public ReflectiveStatementInterceptorAdapter(StatementInterceptor toProxy) {
        this.toProxy = toProxy;
        this.v2PostProcessMethod = ReflectiveStatementInterceptorAdapter.getV2PostProcessMethod(toProxy.getClass());
    }

    @Override
    public void destroy() {
        this.toProxy.destroy();
    }

    @Override
    public boolean executeTopLevelOnly() {
        return this.toProxy.executeTopLevelOnly();
    }

    @Override
    public void init(Connection conn, Properties props) throws SQLException {
        this.toProxy.init(conn, props);
    }

    @Override
    public ResultSetInternalMethods postProcess(String sql, Statement interceptedStatement, ResultSetInternalMethods originalResultSet, Connection connection, int warningCount, boolean noIndexUsed, boolean noGoodIndexUsed, SQLException statementException) throws SQLException {
        try {
            return (ResultSetInternalMethods)this.v2PostProcessMethod.invoke(this.toProxy, sql, interceptedStatement, originalResultSet, connection, warningCount, noIndexUsed ? Boolean.TRUE : Boolean.FALSE, noGoodIndexUsed ? Boolean.TRUE : Boolean.FALSE, statementException);
        }
        catch (IllegalArgumentException e) {
            SQLException sqlEx = new SQLException("Unable to reflectively invoke interceptor");
            sqlEx.initCause(e);
            throw sqlEx;
        }
        catch (IllegalAccessException e) {
            SQLException sqlEx = new SQLException("Unable to reflectively invoke interceptor");
            sqlEx.initCause(e);
            throw sqlEx;
        }
        catch (InvocationTargetException e) {
            SQLException sqlEx = new SQLException("Unable to reflectively invoke interceptor");
            sqlEx.initCause(e);
            throw sqlEx;
        }
    }

    @Override
    public ResultSetInternalMethods preProcess(String sql, Statement interceptedStatement, Connection connection) throws SQLException {
        return this.toProxy.preProcess(sql, interceptedStatement, connection);
    }

    public static final Method getV2PostProcessMethod(Class<?> toProxyClass) {
        try {
            Method postProcessMethod = toProxyClass.getMethod("postProcess", String.class, Statement.class, ResultSetInternalMethods.class, Connection.class, Integer.TYPE, Boolean.TYPE, Boolean.TYPE, SQLException.class);
            return postProcessMethod;
        }
        catch (SecurityException e) {
            return null;
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }
}


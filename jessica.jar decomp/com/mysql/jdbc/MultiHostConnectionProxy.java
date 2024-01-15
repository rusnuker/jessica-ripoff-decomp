/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.MultiHostMySQLConnection;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.jdbc.Util;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class MultiHostConnectionProxy
implements InvocationHandler {
    private static final String METHOD_GET_MULTI_HOST_SAFE_PROXY = "getMultiHostSafeProxy";
    private static final String METHOD_EQUALS = "equals";
    private static final String METHOD_HASH_CODE = "hashCode";
    private static final String METHOD_CLOSE = "close";
    private static final String METHOD_ABORT_INTERNAL = "abortInternal";
    private static final String METHOD_ABORT = "abort";
    private static final String METHOD_IS_CLOSED = "isClosed";
    private static final String METHOD_GET_AUTO_COMMIT = "getAutoCommit";
    private static final String METHOD_GET_CATALOG = "getCatalog";
    private static final String METHOD_GET_TRANSACTION_ISOLATION = "getTransactionIsolation";
    private static final String METHOD_GET_SESSION_MAX_ROWS = "getSessionMaxRows";
    List<String> hostList;
    Properties localProps;
    boolean autoReconnect = false;
    MySQLConnection thisAsConnection = this.getNewWrapperForThisAsConnection();
    MySQLConnection proxyConnection = null;
    MySQLConnection currentConnection = null;
    boolean isClosed = false;
    boolean closedExplicitly = false;
    String closedReason = null;
    protected Throwable lastExceptionDealtWith = null;
    private static Constructor<?> JDBC_4_MS_CONNECTION_CTOR;

    MultiHostConnectionProxy() throws SQLException {
    }

    MultiHostConnectionProxy(List<String> hosts, Properties props) throws SQLException {
        this();
        this.initializeHostsSpecs(hosts, props);
    }

    int initializeHostsSpecs(List<String> hosts, Properties props) {
        this.autoReconnect = "true".equalsIgnoreCase(props.getProperty("autoReconnect")) || "true".equalsIgnoreCase(props.getProperty("autoReconnectForPools"));
        this.hostList = hosts;
        int numHosts = this.hostList.size();
        this.localProps = (Properties)props.clone();
        this.localProps.remove("HOST");
        this.localProps.remove("PORT");
        for (int i = 0; i < numHosts; ++i) {
            this.localProps.remove("HOST." + (i + 1));
            this.localProps.remove("PORT." + (i + 1));
        }
        this.localProps.remove("NUM_HOSTS");
        this.localProps.setProperty("useLocalSessionState", "true");
        return numHosts;
    }

    MySQLConnection getNewWrapperForThisAsConnection() throws SQLException {
        if (Util.isJdbc4() || JDBC_4_MS_CONNECTION_CTOR != null) {
            return (MySQLConnection)Util.handleNewInstance(JDBC_4_MS_CONNECTION_CTOR, new Object[]{this}, null);
        }
        return new MultiHostMySQLConnection(this);
    }

    protected MySQLConnection getProxy() {
        return this.proxyConnection != null ? this.proxyConnection : this.thisAsConnection;
    }

    protected final void setProxy(MySQLConnection proxyConn) {
        this.proxyConnection = proxyConn;
        this.propagateProxyDown(proxyConn);
    }

    protected void propagateProxyDown(MySQLConnection proxyConn) {
        this.currentConnection.setProxy(proxyConn);
    }

    Object proxyIfReturnTypeIsJdbcInterface(Class<?> returnType, Object toProxy) {
        if (toProxy != null && Util.isJdbcInterface(returnType)) {
            Class<?> toProxyClass = toProxy.getClass();
            return Proxy.newProxyInstance(toProxyClass.getClassLoader(), Util.getImplementedInterfaces(toProxyClass), this.getNewJdbcInterfaceProxy(toProxy));
        }
        return toProxy;
    }

    InvocationHandler getNewJdbcInterfaceProxy(Object toProxy) {
        return new JdbcInterfaceProxy(toProxy);
    }

    void dealWithInvocationException(InvocationTargetException e) throws SQLException, Throwable, InvocationTargetException {
        Throwable t = e.getTargetException();
        if (t != null) {
            if (this.lastExceptionDealtWith != t && this.shouldExceptionTriggerConnectionSwitch(t)) {
                this.invalidateCurrentConnection();
                this.pickNewConnection();
                this.lastExceptionDealtWith = t;
            }
            throw t;
        }
        throw e;
    }

    abstract boolean shouldExceptionTriggerConnectionSwitch(Throwable var1);

    abstract boolean isMasterConnection();

    synchronized void invalidateCurrentConnection() throws SQLException {
        this.invalidateConnection(this.currentConnection);
    }

    synchronized void invalidateConnection(MySQLConnection conn) throws SQLException {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.realClose(true, !conn.getAutoCommit(), true, null);
            }
        }
        catch (SQLException sQLException) {
            // empty catch block
        }
    }

    abstract void pickNewConnection() throws SQLException;

    synchronized ConnectionImpl createConnectionForHost(String hostPortSpec) throws SQLException {
        Properties connProps = (Properties)this.localProps.clone();
        String[] hostPortPair = NonRegisteringDriver.parseHostPortPair(hostPortSpec);
        String hostName = hostPortPair[0];
        String portNumber = hostPortPair[1];
        String dbName = connProps.getProperty("DBNAME");
        if (hostName == null) {
            throw new SQLException("Could not find a hostname to start a connection to");
        }
        if (portNumber == null) {
            portNumber = "3306";
        }
        connProps.setProperty("HOST", hostName);
        connProps.setProperty("PORT", portNumber);
        connProps.setProperty("HOST.1", hostName);
        connProps.setProperty("PORT.1", portNumber);
        connProps.setProperty("NUM_HOSTS", "1");
        connProps.setProperty("roundRobinLoadBalance", "false");
        ConnectionImpl conn = (ConnectionImpl)ConnectionImpl.getInstance(hostName, Integer.parseInt(portNumber), connProps, dbName, "jdbc:mysql://" + hostName + ":" + portNumber + "/");
        conn.setProxy(this.getProxy());
        return conn;
    }

    static void syncSessionState(Connection source, Connection target) throws SQLException {
        if (source == null || target == null) {
            return;
        }
        MultiHostConnectionProxy.syncSessionState(source, target, source.isReadOnly());
    }

    static void syncSessionState(Connection source, Connection target, boolean readOnly) throws SQLException {
        if (target != null) {
            target.setReadOnly(readOnly);
        }
        if (source == null || target == null) {
            return;
        }
        target.setAutoCommit(source.getAutoCommit());
        target.setCatalog(source.getCatalog());
        target.setTransactionIsolation(source.getTransactionIsolation());
        target.setSessionMaxRows(source.getSessionMaxRows());
    }

    abstract void doClose() throws SQLException;

    abstract void doAbortInternal() throws SQLException;

    abstract void doAbort(Executor var1) throws SQLException;

    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (METHOD_GET_MULTI_HOST_SAFE_PROXY.equals(methodName)) {
            return this.thisAsConnection;
        }
        if (METHOD_EQUALS.equals(methodName)) {
            return args[0].equals(this);
        }
        if (METHOD_HASH_CODE.equals(methodName)) {
            return this.hashCode();
        }
        if (METHOD_CLOSE.equals(methodName)) {
            this.doClose();
            this.isClosed = true;
            this.closedReason = "Connection explicitly closed.";
            this.closedExplicitly = true;
            return null;
        }
        if (METHOD_ABORT_INTERNAL.equals(methodName)) {
            this.doAbortInternal();
            this.currentConnection.abortInternal();
            this.isClosed = true;
            this.closedReason = "Connection explicitly closed.";
            return null;
        }
        if (METHOD_ABORT.equals(methodName) && args.length == 1) {
            this.doAbort((Executor)args[0]);
            this.isClosed = true;
            this.closedReason = "Connection explicitly closed.";
            return null;
        }
        if (METHOD_IS_CLOSED.equals(methodName)) {
            return this.isClosed;
        }
        try {
            return this.invokeMore(proxy, method, args);
        }
        catch (InvocationTargetException e) {
            throw e.getCause() != null ? e.getCause() : e;
        }
        catch (Exception e) {
            Class<?>[] declaredException;
            for (Class<?> declEx : declaredException = method.getExceptionTypes()) {
                if (!declEx.isAssignableFrom(e.getClass())) continue;
                throw e;
            }
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    abstract Object invokeMore(Object var1, Method var2, Object[] var3) throws Throwable;

    protected boolean allowedOnClosedConnection(Method method) {
        String methodName = method.getName();
        return methodName.equals(METHOD_GET_AUTO_COMMIT) || methodName.equals(METHOD_GET_CATALOG) || methodName.equals(METHOD_GET_TRANSACTION_ISOLATION) || methodName.equals(METHOD_GET_SESSION_MAX_ROWS);
    }

    static {
        if (Util.isJdbc4()) {
            try {
                JDBC_4_MS_CONNECTION_CTOR = Class.forName("com.mysql.jdbc.JDBC4MultiHostMySQLConnection").getConstructor(MultiHostConnectionProxy.class);
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
        }
    }

    class JdbcInterfaceProxy
    implements InvocationHandler {
        Object invokeOn = null;

        JdbcInterfaceProxy(Object toInvokeOn) {
            this.invokeOn = toInvokeOn;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            MultiHostConnectionProxy multiHostConnectionProxy = MultiHostConnectionProxy.this;
            synchronized (multiHostConnectionProxy) {
                Object result = null;
                try {
                    result = method.invoke(this.invokeOn, args);
                    result = MultiHostConnectionProxy.this.proxyIfReturnTypeIsJdbcInterface(method.getReturnType(), result);
                }
                catch (InvocationTargetException e) {
                    MultiHostConnectionProxy.this.dealWithInvocationException(e);
                }
                return result;
            }
        }
    }
}


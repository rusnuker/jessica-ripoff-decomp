/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.CommunicationsException;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.ConnectionPropertiesImpl;
import com.mysql.jdbc.MultiHostConnectionProxy;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.Util;
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
public class FailoverConnectionProxy
extends MultiHostConnectionProxy {
    private static final String METHOD_SET_READ_ONLY = "setReadOnly";
    private static final String METHOD_SET_AUTO_COMMIT = "setAutoCommit";
    private static final String METHOD_COMMIT = "commit";
    private static final String METHOD_ROLLBACK = "rollback";
    private static final int NO_CONNECTION_INDEX = -1;
    private static final int DEFAULT_PRIMARY_HOST_INDEX = 0;
    private int secondsBeforeRetryPrimaryHost;
    private long queriesBeforeRetryPrimaryHost;
    private boolean failoverReadOnly;
    private int retriesAllDown;
    private int currentHostIndex = -1;
    private int primaryHostIndex = 0;
    private Boolean explicitlyReadOnly = null;
    private boolean explicitlyAutoCommit = true;
    private boolean enableFallBackToPrimaryHost = true;
    private long primaryHostFailTimeMillis = 0L;
    private long queriesIssuedSinceFailover = 0L;
    private static Class<?>[] INTERFACES_TO_PROXY;

    public static Connection createProxyInstance(List<String> hosts, Properties props) throws SQLException {
        FailoverConnectionProxy connProxy = new FailoverConnectionProxy(hosts, props);
        return (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(), INTERFACES_TO_PROXY, connProxy);
    }

    private FailoverConnectionProxy(List<String> hosts, Properties props) throws SQLException {
        super(hosts, props);
        ConnectionPropertiesImpl connProps = new ConnectionPropertiesImpl();
        connProps.initializeProperties(props);
        this.secondsBeforeRetryPrimaryHost = connProps.getSecondsBeforeRetryMaster();
        this.queriesBeforeRetryPrimaryHost = connProps.getQueriesBeforeRetryMaster();
        this.failoverReadOnly = connProps.getFailOverReadOnly();
        this.retriesAllDown = connProps.getRetriesAllDown();
        this.enableFallBackToPrimaryHost = this.secondsBeforeRetryPrimaryHost > 0 || this.queriesBeforeRetryPrimaryHost > 0L;
        this.pickNewConnection();
        this.explicitlyAutoCommit = this.currentConnection.getAutoCommit();
    }

    @Override
    MultiHostConnectionProxy.JdbcInterfaceProxy getNewJdbcInterfaceProxy(Object toProxy) {
        return new FailoverJdbcInterfaceProxy(toProxy);
    }

    @Override
    boolean shouldExceptionTriggerConnectionSwitch(Throwable t) {
        if (!(t instanceof SQLException)) {
            return false;
        }
        String sqlState = ((SQLException)t).getSQLState();
        if (sqlState != null && sqlState.startsWith("08")) {
            return true;
        }
        return t instanceof CommunicationsException;
    }

    @Override
    boolean isMasterConnection() {
        return this.connectedToPrimaryHost();
    }

    @Override
    synchronized void pickNewConnection() throws SQLException {
        if (this.isClosed && this.closedExplicitly) {
            return;
        }
        if (!this.isConnected() || this.readyToFallBackToPrimaryHost()) {
            try {
                this.connectTo(this.primaryHostIndex);
            }
            catch (SQLException e) {
                this.resetAutoFallBackCounters();
                this.failOver(this.primaryHostIndex);
            }
        } else {
            this.failOver();
        }
    }

    synchronized ConnectionImpl createConnectionForHostIndex(int hostIndex) throws SQLException {
        return this.createConnectionForHost((String)this.hostList.get(hostIndex));
    }

    private synchronized void connectTo(int hostIndex) throws SQLException {
        try {
            this.switchCurrentConnectionTo(hostIndex, this.createConnectionForHostIndex(hostIndex));
        }
        catch (SQLException e) {
            if (this.currentConnection != null) {
                StringBuilder msg = new StringBuilder("Connection to ").append(this.isPrimaryHostIndex(hostIndex) ? "primary" : "secondary").append(" host '").append((String)this.hostList.get(hostIndex)).append("' failed");
                this.currentConnection.getLog().logWarn(msg.toString(), e);
            }
            throw e;
        }
    }

    private synchronized void switchCurrentConnectionTo(int hostIndex, MySQLConnection connection) throws SQLException {
        this.invalidateCurrentConnection();
        boolean readOnly = this.isPrimaryHostIndex(hostIndex) ? (this.explicitlyReadOnly == null ? false : this.explicitlyReadOnly) : (this.failoverReadOnly ? true : (this.explicitlyReadOnly != null ? this.explicitlyReadOnly : (this.currentConnection != null ? this.currentConnection.isReadOnly() : false)));
        FailoverConnectionProxy.syncSessionState(this.currentConnection, connection, readOnly);
        this.currentConnection = connection;
        this.currentHostIndex = hostIndex;
    }

    private synchronized void failOver() throws SQLException {
        this.failOver(this.currentHostIndex);
    }

    private synchronized void failOver(int failedHostIdx) throws SQLException {
        int nextHostIndex;
        int prevHostIndex = this.currentHostIndex;
        int firstHostIndexTried = nextHostIndex = this.nextHost(failedHostIdx, false);
        SQLException lastExceptionCaught = null;
        int attempts = 0;
        boolean gotConnection = false;
        boolean firstConnOrPassedByPrimaryHost = prevHostIndex == -1 || this.isPrimaryHostIndex(prevHostIndex);
        do {
            try {
                firstConnOrPassedByPrimaryHost = firstConnOrPassedByPrimaryHost || this.isPrimaryHostIndex(nextHostIndex);
                this.connectTo(nextHostIndex);
                if (firstConnOrPassedByPrimaryHost && this.connectedToSecondaryHost()) {
                    this.resetAutoFallBackCounters();
                }
                gotConnection = true;
            }
            catch (SQLException e) {
                lastExceptionCaught = e;
                if (this.shouldExceptionTriggerConnectionSwitch(e)) {
                    int newNextHostIndex = this.nextHost(nextHostIndex, attempts > 0);
                    if (newNextHostIndex == firstHostIndexTried && newNextHostIndex == (newNextHostIndex = this.nextHost(nextHostIndex, true))) {
                        ++attempts;
                        try {
                            Thread.sleep(250L);
                        }
                        catch (InterruptedException ie) {
                            // empty catch block
                        }
                    }
                    nextHostIndex = newNextHostIndex;
                    continue;
                }
                throw e;
            }
        } while (attempts < this.retriesAllDown && !gotConnection);
        if (!gotConnection) {
            throw lastExceptionCaught;
        }
    }

    synchronized void fallBackToPrimaryIfAvailable() {
        ConnectionImpl connection = null;
        try {
            connection = this.createConnectionForHostIndex(this.primaryHostIndex);
            this.switchCurrentConnectionTo(this.primaryHostIndex, connection);
        }
        catch (SQLException e1) {
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException sQLException) {
                    // empty catch block
                }
            }
            this.resetAutoFallBackCounters();
        }
    }

    private int nextHost(int currHostIdx, boolean vouchForPrimaryHost) {
        int nextHostIdx = (currHostIdx + 1) % this.hostList.size();
        if (this.isPrimaryHostIndex(nextHostIdx) && this.isConnected() && !vouchForPrimaryHost && this.enableFallBackToPrimaryHost && !this.readyToFallBackToPrimaryHost()) {
            nextHostIdx = this.nextHost(nextHostIdx, vouchForPrimaryHost);
        }
        return nextHostIdx;
    }

    synchronized void incrementQueriesIssuedSinceFailover() {
        ++this.queriesIssuedSinceFailover;
    }

    synchronized boolean readyToFallBackToPrimaryHost() {
        return this.enableFallBackToPrimaryHost && this.connectedToSecondaryHost() && (this.secondsBeforeRetryPrimaryHostIsMet() || this.queriesBeforeRetryPrimaryHostIsMet());
    }

    synchronized boolean isConnected() {
        return this.currentHostIndex != -1;
    }

    synchronized boolean isPrimaryHostIndex(int hostIndex) {
        return hostIndex == this.primaryHostIndex;
    }

    synchronized boolean connectedToPrimaryHost() {
        return this.isPrimaryHostIndex(this.currentHostIndex);
    }

    synchronized boolean connectedToSecondaryHost() {
        return this.currentHostIndex >= 0 && !this.isPrimaryHostIndex(this.currentHostIndex);
    }

    private synchronized boolean secondsBeforeRetryPrimaryHostIsMet() {
        return this.secondsBeforeRetryPrimaryHost > 0 && Util.secondsSinceMillis(this.primaryHostFailTimeMillis) >= (long)this.secondsBeforeRetryPrimaryHost;
    }

    private synchronized boolean queriesBeforeRetryPrimaryHostIsMet() {
        return this.queriesBeforeRetryPrimaryHost > 0L && this.queriesIssuedSinceFailover >= this.queriesBeforeRetryPrimaryHost;
    }

    private synchronized void resetAutoFallBackCounters() {
        this.primaryHostFailTimeMillis = System.currentTimeMillis();
        this.queriesIssuedSinceFailover = 0L;
    }

    @Override
    synchronized void doClose() throws SQLException {
        this.currentConnection.close();
    }

    @Override
    synchronized void doAbortInternal() throws SQLException {
        this.currentConnection.abortInternal();
    }

    @Override
    synchronized void doAbort(Executor executor) throws SQLException {
        this.currentConnection.abort(executor);
    }

    @Override
    public synchronized Object invokeMore(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (METHOD_SET_READ_ONLY.equals(methodName)) {
            this.explicitlyReadOnly = (Boolean)args[0];
            if (this.failoverReadOnly && this.connectedToSecondaryHost()) {
                return null;
            }
        }
        if (this.isClosed && !this.allowedOnClosedConnection(method)) {
            if (this.autoReconnect && !this.closedExplicitly) {
                this.currentHostIndex = -1;
                this.pickNewConnection();
                this.isClosed = false;
                this.closedReason = null;
            } else {
                String reason = "No operations allowed after connection closed.";
                if (this.closedReason != null) {
                    reason = reason + "  " + this.closedReason;
                }
                throw SQLError.createSQLException(reason, "08003", null);
            }
        }
        Object result = null;
        try {
            result = method.invoke(this.thisAsConnection, args);
            result = this.proxyIfReturnTypeIsJdbcInterface(method.getReturnType(), result);
        }
        catch (InvocationTargetException e) {
            this.dealWithInvocationException(e);
        }
        if (METHOD_SET_AUTO_COMMIT.equals(methodName)) {
            this.explicitlyAutoCommit = (Boolean)args[0];
        }
        if ((this.explicitlyAutoCommit || METHOD_COMMIT.equals(methodName) || METHOD_ROLLBACK.equals(methodName)) && this.readyToFallBackToPrimaryHost()) {
            this.fallBackToPrimaryIfAvailable();
        }
        return result;
    }

    static {
        if (Util.isJdbc4()) {
            try {
                INTERFACES_TO_PROXY = new Class[]{Class.forName("com.mysql.jdbc.JDBC4MySQLConnection")};
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            INTERFACES_TO_PROXY = new Class[]{MySQLConnection.class};
        }
    }

    class FailoverJdbcInterfaceProxy
    extends MultiHostConnectionProxy.JdbcInterfaceProxy {
        FailoverJdbcInterfaceProxy(Object toInvokeOn) {
            super(toInvokeOn);
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            boolean isExecute = methodName.startsWith("execute");
            if (FailoverConnectionProxy.this.connectedToSecondaryHost() && isExecute) {
                FailoverConnectionProxy.this.incrementQueriesIssuedSinceFailover();
            }
            Object result = super.invoke(proxy, method, args);
            if (FailoverConnectionProxy.this.explicitlyAutoCommit && isExecute && FailoverConnectionProxy.this.readyToFallBackToPrimaryHost()) {
                FailoverConnectionProxy.this.fallBackToPrimaryIfAvailable();
            }
            return result;
        }
    }
}


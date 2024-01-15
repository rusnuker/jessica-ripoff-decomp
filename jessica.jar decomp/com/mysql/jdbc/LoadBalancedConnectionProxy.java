/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.BalanceStrategy;
import com.mysql.jdbc.ConnectionGroup;
import com.mysql.jdbc.ConnectionGroupManager;
import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.LoadBalanceExceptionChecker;
import com.mysql.jdbc.LoadBalancedConnection;
import com.mysql.jdbc.LoadBalancedMySQLConnection;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MultiHostConnectionProxy;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.PingTarget;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.Util;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class LoadBalancedConnectionProxy
extends MultiHostConnectionProxy
implements PingTarget {
    private ConnectionGroup connectionGroup = null;
    private long connectionGroupProxyID = 0L;
    protected Map<String, ConnectionImpl> liveConnections;
    private Map<String, Integer> hostsToListIndexMap;
    private Map<ConnectionImpl, String> connectionsToHostsMap;
    private long totalPhysicalConnections = 0L;
    private long[] responseTimes;
    private int retriesAllDown;
    private BalanceStrategy balancer;
    private int autoCommitSwapThreshold = 0;
    public static final String BLACKLIST_TIMEOUT_PROPERTY_KEY = "loadBalanceBlacklistTimeout";
    private int globalBlacklistTimeout = 0;
    private static Map<String, Long> globalBlacklist = new HashMap<String, Long>();
    public static final String HOST_REMOVAL_GRACE_PERIOD_PROPERTY_KEY = "loadBalanceHostRemovalGracePeriod";
    private int hostRemovalGracePeriod = 0;
    private Set<String> hostsToRemove = new HashSet<String>();
    private boolean inTransaction = false;
    private long transactionStartTime = 0L;
    private long transactionCount = 0L;
    private LoadBalanceExceptionChecker exceptionChecker;
    private static Constructor<?> JDBC_4_LB_CONNECTION_CTOR;
    private static Class<?>[] INTERFACES_TO_PROXY;
    private static LoadBalancedConnection nullLBConnectionInstance;

    public static LoadBalancedConnection createProxyInstance(List<String> hosts, Properties props) throws SQLException {
        LoadBalancedConnectionProxy connProxy = new LoadBalancedConnectionProxy(hosts, props);
        return (LoadBalancedConnection)Proxy.newProxyInstance(LoadBalancedConnection.class.getClassLoader(), INTERFACES_TO_PROXY, connProxy);
    }

    private LoadBalancedConnectionProxy(List<String> hosts, Properties props) throws SQLException {
        String group = props.getProperty("loadBalanceConnectionGroup", null);
        boolean enableJMX = false;
        String enableJMXAsString = props.getProperty("loadBalanceEnableJMX", "false");
        try {
            enableJMX = Boolean.parseBoolean(enableJMXAsString);
        }
        catch (Exception e) {
            throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceEnableJMX", new Object[]{enableJMXAsString}), "S1009", null);
        }
        if (group != null) {
            this.connectionGroup = ConnectionGroupManager.getConnectionGroupInstance(group);
            if (enableJMX) {
                ConnectionGroupManager.registerJmx();
            }
            this.connectionGroupProxyID = this.connectionGroup.registerConnectionProxy(this, hosts);
            hosts = new ArrayList<String>(this.connectionGroup.getInitialHosts());
        }
        int numHosts = this.initializeHostsSpecs(hosts, props);
        this.liveConnections = new HashMap<String, ConnectionImpl>(numHosts);
        this.hostsToListIndexMap = new HashMap<String, Integer>(numHosts);
        for (int i = 0; i < numHosts; ++i) {
            this.hostsToListIndexMap.put((String)this.hostList.get(i), i);
        }
        this.connectionsToHostsMap = new HashMap<ConnectionImpl, String>(numHosts);
        this.responseTimes = new long[numHosts];
        String retriesAllDownAsString = this.localProps.getProperty("retriesAllDown", "120");
        try {
            this.retriesAllDown = Integer.parseInt(retriesAllDownAsString);
        }
        catch (NumberFormatException nfe) {
            throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForRetriesAllDown", new Object[]{retriesAllDownAsString}), "S1009", null);
        }
        String blacklistTimeoutAsString = this.localProps.getProperty(BLACKLIST_TIMEOUT_PROPERTY_KEY, "0");
        try {
            this.globalBlacklistTimeout = Integer.parseInt(blacklistTimeoutAsString);
        }
        catch (NumberFormatException nfe) {
            throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceBlacklistTimeout", new Object[]{blacklistTimeoutAsString}), "S1009", null);
        }
        String hostRemovalGracePeriodAsString = this.localProps.getProperty(HOST_REMOVAL_GRACE_PERIOD_PROPERTY_KEY, "15000");
        try {
            this.hostRemovalGracePeriod = Integer.parseInt(hostRemovalGracePeriodAsString);
        }
        catch (NumberFormatException nfe) {
            throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceHostRemovalGracePeriod", new Object[]{hostRemovalGracePeriodAsString}), "S1009", null);
        }
        String strategy = this.localProps.getProperty("loadBalanceStrategy", "random");
        this.balancer = "random".equals(strategy) ? (BalanceStrategy)Util.loadExtensions(null, props, "com.mysql.jdbc.RandomBalanceStrategy", "InvalidLoadBalanceStrategy", null).get(0) : ("bestResponseTime".equals(strategy) ? (BalanceStrategy)Util.loadExtensions(null, props, "com.mysql.jdbc.BestResponseTimeBalanceStrategy", "InvalidLoadBalanceStrategy", null).get(0) : (BalanceStrategy)Util.loadExtensions(null, props, strategy, "InvalidLoadBalanceStrategy", null).get(0));
        String autoCommitSwapThresholdAsString = props.getProperty("loadBalanceAutoCommitStatementThreshold", "0");
        try {
            this.autoCommitSwapThreshold = Integer.parseInt(autoCommitSwapThresholdAsString);
        }
        catch (NumberFormatException nfe) {
            throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceAutoCommitStatementThreshold", new Object[]{autoCommitSwapThresholdAsString}), "S1009", null);
        }
        String autoCommitSwapRegex = props.getProperty("loadBalanceAutoCommitStatementRegex", "");
        if (!"".equals(autoCommitSwapRegex)) {
            try {
                "".matches(autoCommitSwapRegex);
            }
            catch (Exception e) {
                throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceAutoCommitStatementRegex", new Object[]{autoCommitSwapRegex}), "S1009", null);
            }
        }
        if (this.autoCommitSwapThreshold > 0) {
            String statementInterceptors = this.localProps.getProperty("statementInterceptors");
            if (statementInterceptors == null) {
                this.localProps.setProperty("statementInterceptors", "com.mysql.jdbc.LoadBalancedAutoCommitInterceptor");
            } else if (statementInterceptors.length() > 0) {
                this.localProps.setProperty("statementInterceptors", statementInterceptors + ",com.mysql.jdbc.LoadBalancedAutoCommitInterceptor");
            }
            props.setProperty("statementInterceptors", this.localProps.getProperty("statementInterceptors"));
        }
        this.balancer.init(null, props);
        String lbExceptionChecker = this.localProps.getProperty("loadBalanceExceptionChecker", "com.mysql.jdbc.StandardLoadBalanceExceptionChecker");
        this.exceptionChecker = (LoadBalanceExceptionChecker)Util.loadExtensions(null, props, lbExceptionChecker, "InvalidLoadBalanceExceptionChecker", null).get(0);
        this.pickNewConnection();
    }

    @Override
    MySQLConnection getNewWrapperForThisAsConnection() throws SQLException {
        if (Util.isJdbc4() || JDBC_4_LB_CONNECTION_CTOR != null) {
            return (MySQLConnection)Util.handleNewInstance(JDBC_4_LB_CONNECTION_CTOR, new Object[]{this}, null);
        }
        return new LoadBalancedMySQLConnection(this);
    }

    @Override
    protected void propagateProxyDown(MySQLConnection proxyConn) {
        for (ConnectionImpl c : this.liveConnections.values()) {
            c.setProxy(proxyConn);
        }
    }

    @Override
    boolean shouldExceptionTriggerConnectionSwitch(Throwable t) {
        return t instanceof SQLException && this.exceptionChecker.shouldExceptionTriggerFailover((SQLException)t);
    }

    @Override
    boolean isMasterConnection() {
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    synchronized void invalidateConnection(MySQLConnection conn) throws SQLException {
        super.invalidateConnection(conn);
        if (this.isGlobalBlacklistEnabled()) {
            this.addToGlobalBlacklist(this.connectionsToHostsMap.get(conn));
        }
        this.liveConnections.remove(this.connectionsToHostsMap.get(conn));
        String mappedHost = this.connectionsToHostsMap.remove(conn);
        if (mappedHost == null || !this.hostsToListIndexMap.containsKey(mappedHost)) return;
        int hostIndex = this.hostsToListIndexMap.get(mappedHost);
        long[] lArray = this.responseTimes;
        synchronized (this.responseTimes) {
            this.responseTimes[hostIndex] = 0L;
            // ** MonitorExit[var4_4] (shouldn't be in output)
            return;
        }
    }

    @Override
    synchronized void pickNewConnection() throws SQLException {
        if (this.isClosed && this.closedExplicitly) {
            return;
        }
        if (this.currentConnection == null) {
            this.currentConnection = this.balancer.pickConnection(this, Collections.unmodifiableList(this.hostList), Collections.unmodifiableMap(this.liveConnections), (long[])this.responseTimes.clone(), this.retriesAllDown);
            return;
        }
        if (this.currentConnection.isClosed()) {
            this.invalidateCurrentConnection();
        }
        int pingTimeout = this.currentConnection.getLoadBalancePingTimeout();
        boolean pingBeforeReturn = this.currentConnection.getLoadBalanceValidateConnectionOnSwapServer();
        int hostsToTry = this.hostList.size();
        for (int hostsTried = 0; hostsTried < hostsToTry; ++hostsTried) {
            ConnectionImpl newConn = null;
            try {
                newConn = this.balancer.pickConnection(this, Collections.unmodifiableList(this.hostList), Collections.unmodifiableMap(this.liveConnections), (long[])this.responseTimes.clone(), this.retriesAllDown);
                if (this.currentConnection != null) {
                    if (pingBeforeReturn) {
                        if (pingTimeout == 0) {
                            newConn.ping();
                        } else {
                            newConn.pingInternal(true, pingTimeout);
                        }
                    }
                    LoadBalancedConnectionProxy.syncSessionState(this.currentConnection, newConn);
                }
                this.currentConnection = newConn;
                return;
            }
            catch (SQLException e) {
                if (!this.shouldExceptionTriggerConnectionSwitch(e) || newConn == null) continue;
                this.invalidateConnection(newConn);
                continue;
            }
        }
        this.isClosed = true;
        this.closedReason = "Connection closed after inability to pick valid new connection during load-balance.";
    }

    @Override
    public synchronized ConnectionImpl createConnectionForHost(String hostPortSpec) throws SQLException {
        ConnectionImpl conn = super.createConnectionForHost(hostPortSpec);
        this.liveConnections.put(hostPortSpec, conn);
        this.connectionsToHostsMap.put(conn, hostPortSpec);
        ++this.totalPhysicalConnections;
        return conn;
    }

    private synchronized void closeAllConnections() {
        for (ConnectionImpl c : this.liveConnections.values()) {
            try {
                c.close();
            }
            catch (SQLException sQLException) {}
        }
        if (!this.isClosed) {
            this.balancer.destroy();
            if (this.connectionGroup != null) {
                this.connectionGroup.closeConnectionProxy(this);
            }
        }
        this.liveConnections.clear();
        this.connectionsToHostsMap.clear();
    }

    @Override
    synchronized void doClose() {
        this.closeAllConnections();
    }

    @Override
    synchronized void doAbortInternal() {
        for (ConnectionImpl c : this.liveConnections.values()) {
            try {
                c.abortInternal();
            }
            catch (SQLException sQLException) {}
        }
        if (!this.isClosed) {
            this.balancer.destroy();
            if (this.connectionGroup != null) {
                this.connectionGroup.closeConnectionProxy(this);
            }
        }
        this.liveConnections.clear();
        this.connectionsToHostsMap.clear();
    }

    @Override
    synchronized void doAbort(Executor executor) {
        for (ConnectionImpl c : this.liveConnections.values()) {
            try {
                c.abort(executor);
            }
            catch (SQLException e) {}
        }
        if (!this.isClosed) {
            this.balancer.destroy();
            if (this.connectionGroup != null) {
                this.connectionGroup.closeConnectionProxy(this);
            }
        }
        this.liveConnections.clear();
        this.connectionsToHostsMap.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    @Override
    public synchronized Object invokeMore(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (this.isClosed && !this.allowedOnClosedConnection(method) && method.getExceptionTypes().length > 0) {
            if (this.autoReconnect && !this.closedExplicitly) {
                this.currentConnection = null;
                this.pickNewConnection();
                this.isClosed = false;
                this.closedReason = null;
            } else {
                String reason = "No operations allowed after connection closed.";
                if (this.closedReason == null) throw SQLError.createSQLException(reason, "08003", null);
                reason = reason + " " + this.closedReason;
                throw SQLError.createSQLException(reason, "08003", null);
            }
        }
        if (!this.inTransaction) {
            this.inTransaction = true;
            this.transactionStartTime = System.nanoTime();
            ++this.transactionCount;
        }
        Object result = null;
        try {
            block22: {
                try {
                    result = method.invoke(this.thisAsConnection, args);
                    if (result == null) break block22;
                    if (result instanceof Statement) {
                        ((Statement)result).setPingTarget(this);
                    }
                    result = this.proxyIfReturnTypeIsJdbcInterface(method.getReturnType(), result);
                }
                catch (InvocationTargetException e) {
                    this.dealWithInvocationException(e);
                    Object var8_8 = null;
                    if (!"commit".equals(methodName)) {
                        if (!"rollback".equals(methodName)) return result;
                    }
                    this.inTransaction = false;
                    String host = this.connectionsToHostsMap.get(this.currentConnection);
                    if (host != null) {
                        long[] lArray = this.responseTimes;
                        // MONITORENTER : this.responseTimes
                        Integer hostIndex = this.hostsToListIndexMap.get(host);
                        if (hostIndex != null && hostIndex < this.responseTimes.length) {
                            this.responseTimes[hostIndex.intValue()] = System.nanoTime() - this.transactionStartTime;
                        }
                        // MONITOREXIT : lArray
                    }
                    this.pickNewConnection();
                    return result;
                }
            }
            Object var8_7 = null;
            if (!"commit".equals(methodName)) {
                if (!"rollback".equals(methodName)) return result;
            }
            this.inTransaction = false;
            String host = this.connectionsToHostsMap.get(this.currentConnection);
            if (host != null) {
                long[] lArray = this.responseTimes;
                // MONITORENTER : this.responseTimes
                Integer hostIndex = this.hostsToListIndexMap.get(host);
                if (hostIndex != null && hostIndex < this.responseTimes.length) {
                    this.responseTimes[hostIndex.intValue()] = System.nanoTime() - this.transactionStartTime;
                }
                // MONITOREXIT : lArray
            }
            this.pickNewConnection();
            return result;
        }
        catch (Throwable throwable) {
            Object var8_9 = null;
            if (!"commit".equals(methodName)) {
                if (!"rollback".equals(methodName)) throw throwable;
            }
            this.inTransaction = false;
            String host = this.connectionsToHostsMap.get(this.currentConnection);
            if (host != null) {
                long[] lArray = this.responseTimes;
                // MONITORENTER : this.responseTimes
                Integer hostIndex = this.hostsToListIndexMap.get(host);
                if (hostIndex != null && hostIndex < this.responseTimes.length) {
                    this.responseTimes[hostIndex.intValue()] = System.nanoTime() - this.transactionStartTime;
                }
                // MONITOREXIT : lArray
            }
            this.pickNewConnection();
            throw throwable;
        }
    }

    @Override
    public synchronized void doPing() throws SQLException {
        SQLException se = null;
        boolean foundHost = false;
        int pingTimeout = this.currentConnection.getLoadBalancePingTimeout();
        for (String host : this.hostList) {
            ConnectionImpl conn = this.liveConnections.get(host);
            if (conn == null) continue;
            try {
                if (pingTimeout == 0) {
                    conn.ping();
                } else {
                    conn.pingInternal(true, pingTimeout);
                }
                foundHost = true;
            }
            catch (SQLException e) {
                if (host.equals(this.connectionsToHostsMap.get(this.currentConnection))) {
                    this.closeAllConnections();
                    this.isClosed = true;
                    this.closedReason = "Connection closed because ping of current connection failed.";
                    throw e;
                }
                if (e.getMessage().equals(Messages.getString("Connection.exceededConnectionLifetime"))) {
                    if (se == null) {
                        se = e;
                    }
                } else {
                    se = e;
                    if (this.isGlobalBlacklistEnabled()) {
                        this.addToGlobalBlacklist(host);
                    }
                }
                this.liveConnections.remove(this.connectionsToHostsMap.get(conn));
            }
        }
        if (!foundHost) {
            this.closeAllConnections();
            this.isClosed = true;
            this.closedReason = "Connection closed due to inability to ping any active connections.";
            if (se != null) {
                throw se;
            }
            ((ConnectionImpl)this.currentConnection).throwConnectionClosedException();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addToGlobalBlacklist(String host, long timeout) {
        if (this.isGlobalBlacklistEnabled()) {
            Map<String, Long> map = globalBlacklist;
            synchronized (map) {
                globalBlacklist.put(host, timeout);
            }
        }
    }

    public void addToGlobalBlacklist(String host) {
        this.addToGlobalBlacklist(host, System.currentTimeMillis() + (long)this.globalBlacklistTimeout);
    }

    public boolean isGlobalBlacklistEnabled() {
        return this.globalBlacklistTimeout > 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized Map<String, Long> getGlobalBlacklist() {
        if (!this.isGlobalBlacklistEnabled()) {
            if (this.hostsToRemove.isEmpty()) {
                return new HashMap<String, Long>(1);
            }
            HashMap<String, Long> fakedBlacklist = new HashMap<String, Long>();
            for (String h : this.hostsToRemove) {
                fakedBlacklist.put(h, System.currentTimeMillis() + 5000L);
            }
            return fakedBlacklist;
        }
        HashMap<String, Long> blacklistClone = new HashMap<String, Long>(globalBlacklist.size());
        Map<String, Long> i$ = globalBlacklist;
        synchronized (i$) {
            blacklistClone.putAll(globalBlacklist);
        }
        Set keys = blacklistClone.keySet();
        keys.retainAll(this.hostList);
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            String host = (String)i.next();
            Long timeout = globalBlacklist.get(host);
            if (timeout == null || timeout >= System.currentTimeMillis()) continue;
            Map<String, Long> map = globalBlacklist;
            synchronized (map) {
                globalBlacklist.remove(host);
            }
            i.remove();
        }
        if (keys.size() == this.hostList.size()) {
            return new HashMap<String, Long>(1);
        }
        return blacklistClone;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeHostWhenNotInUse(String hostPortPair) throws SQLException {
        if (this.hostRemovalGracePeriod <= 0) {
            this.removeHost(hostPortPair);
            return;
        }
        int timeBetweenChecks = this.hostRemovalGracePeriod > 1000 ? 1000 : this.hostRemovalGracePeriod;
        LoadBalancedConnectionProxy loadBalancedConnectionProxy = this;
        synchronized (loadBalancedConnectionProxy) {
            this.addToGlobalBlacklist(hostPortPair, System.currentTimeMillis() + (long)this.hostRemovalGracePeriod + (long)timeBetweenChecks);
            long cur = System.currentTimeMillis();
            while (System.currentTimeMillis() < cur + (long)this.hostRemovalGracePeriod) {
                this.hostsToRemove.add(hostPortPair);
                if (!hostPortPair.equals(this.currentConnection.getHostPortPair())) {
                    this.removeHost(hostPortPair);
                    return;
                }
                try {
                    Thread.sleep(timeBetweenChecks);
                }
                catch (InterruptedException e) {}
            }
        }
        this.removeHost(hostPortPair);
    }

    public synchronized void removeHost(String hostPortPair) throws SQLException {
        if (this.connectionGroup != null && this.connectionGroup.getInitialHosts().size() == 1 && this.connectionGroup.getInitialHosts().contains(hostPortPair)) {
            throw SQLError.createSQLException("Cannot remove only configured host.", null);
        }
        this.hostsToRemove.add(hostPortPair);
        this.connectionsToHostsMap.remove(this.liveConnections.remove(hostPortPair));
        if (this.hostsToListIndexMap.remove(hostPortPair) != null) {
            long[] newResponseTimes = new long[this.responseTimes.length - 1];
            int newIdx = 0;
            for (String h : this.hostList) {
                if (this.hostsToRemove.contains(h)) continue;
                Integer idx = this.hostsToListIndexMap.get(h);
                if (idx != null && idx < this.responseTimes.length) {
                    newResponseTimes[newIdx] = this.responseTimes[idx];
                }
                this.hostsToListIndexMap.put(h, newIdx++);
            }
            this.responseTimes = newResponseTimes;
        }
        if (hostPortPair.equals(this.currentConnection.getHostPortPair())) {
            this.invalidateConnection(this.currentConnection);
            this.pickNewConnection();
        }
    }

    public synchronized boolean addHost(String hostPortPair) {
        if (this.hostsToListIndexMap.containsKey(hostPortPair)) {
            return false;
        }
        long[] newResponseTimes = new long[this.responseTimes.length + 1];
        System.arraycopy(this.responseTimes, 0, newResponseTimes, 0, this.responseTimes.length);
        this.responseTimes = newResponseTimes;
        if (!this.hostList.contains(hostPortPair)) {
            this.hostList.add(hostPortPair);
        }
        this.hostsToListIndexMap.put(hostPortPair, this.responseTimes.length - 1);
        this.hostsToRemove.remove(hostPortPair);
        return true;
    }

    public synchronized boolean inTransaction() {
        return this.inTransaction;
    }

    public synchronized long getTransactionCount() {
        return this.transactionCount;
    }

    public synchronized long getActivePhysicalConnectionCount() {
        return this.liveConnections.size();
    }

    public synchronized long getTotalPhysicalConnectionCount() {
        return this.totalPhysicalConnections;
    }

    public synchronized long getConnectionGroupProxyID() {
        return this.connectionGroupProxyID;
    }

    public synchronized String getCurrentActiveHost() {
        String o;
        MySQLConnection c = this.currentConnection;
        if (c != null && (o = this.connectionsToHostsMap.get(c)) != null) {
            return o.toString();
        }
        return null;
    }

    public synchronized long getCurrentTransactionDuration() {
        if (this.inTransaction && this.transactionStartTime > 0L) {
            return System.nanoTime() - this.transactionStartTime;
        }
        return 0L;
    }

    static synchronized LoadBalancedConnection getNullLoadBalancedConnectionInstance() {
        if (nullLBConnectionInstance == null) {
            nullLBConnectionInstance = (LoadBalancedConnection)Proxy.newProxyInstance(LoadBalancedConnection.class.getClassLoader(), INTERFACES_TO_PROXY, new NullLoadBalancedConnectionProxy());
        }
        return nullLBConnectionInstance;
    }

    static {
        if (Util.isJdbc4()) {
            try {
                JDBC_4_LB_CONNECTION_CTOR = Class.forName("com.mysql.jdbc.JDBC4LoadBalancedMySQLConnection").getConstructor(LoadBalancedConnectionProxy.class);
                INTERFACES_TO_PROXY = new Class[]{LoadBalancedConnection.class, Class.forName("com.mysql.jdbc.JDBC4MySQLConnection")};
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
            INTERFACES_TO_PROXY = new Class[]{LoadBalancedConnection.class};
        }
        nullLBConnectionInstance = null;
    }

    private static class NullLoadBalancedConnectionProxy
    implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Class<?>[] declaredException;
            SQLException exceptionToThrow = SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.unusableConnection"), "25000", 1000001, true, null);
            for (Class<?> declEx : declaredException = method.getExceptionTypes()) {
                if (!declEx.isAssignableFrom(exceptionToThrow.getClass())) continue;
                throw exceptionToThrow;
            }
            throw new IllegalStateException(exceptionToThrow.getMessage(), exceptionToThrow);
        }
    }
}


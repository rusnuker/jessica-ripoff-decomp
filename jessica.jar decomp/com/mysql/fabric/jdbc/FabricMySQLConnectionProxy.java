/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.jdbc;

import com.mysql.fabric.FabricCommunicationException;
import com.mysql.fabric.FabricConnection;
import com.mysql.fabric.Server;
import com.mysql.fabric.ServerGroup;
import com.mysql.fabric.ShardMapping;
import com.mysql.fabric.jdbc.FabricMySQLConnection;
import com.mysql.fabric.jdbc.FabricMySQLConnectionProperties;
import com.mysql.jdbc.Buffer;
import com.mysql.jdbc.CachedResultSetMetaData;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ConnectionPropertiesImpl;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.Extension;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.MysqlIO;
import com.mysql.jdbc.ReplicationConnection;
import com.mysql.jdbc.ReplicationConnectionGroup;
import com.mysql.jdbc.ReplicationConnectionGroupManager;
import com.mysql.jdbc.ReplicationConnectionProxy;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.ServerPreparedStatement;
import com.mysql.jdbc.SingleByteCharsetConverter;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StatementImpl;
import com.mysql.jdbc.StatementInterceptorV2;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.exceptions.MySQLNonTransientConnectionException;
import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.log.LogFactory;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.concurrent.Executor;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class FabricMySQLConnectionProxy
extends ConnectionPropertiesImpl
implements FabricMySQLConnection,
FabricMySQLConnectionProperties {
    private static final long serialVersionUID = 5845485979107347258L;
    private Log log;
    protected FabricConnection fabricConnection;
    protected boolean closed = false;
    protected boolean transactionInProgress = false;
    protected Map<ServerGroup, ReplicationConnection> serverConnections = new HashMap<ServerGroup, ReplicationConnection>();
    protected ReplicationConnection currentConnection;
    protected String shardKey;
    protected String shardTable;
    protected String serverGroupName;
    protected Set<String> queryTables = new HashSet<String>();
    protected ServerGroup serverGroup;
    protected String host;
    protected String port;
    protected String username;
    protected String password;
    protected String database;
    protected ShardMapping shardMapping;
    protected boolean readOnly = false;
    protected boolean autoCommit = true;
    protected int transactionIsolation = 4;
    private String fabricShardKey;
    private String fabricShardTable;
    private String fabricServerGroup;
    private String fabricProtocol;
    private String fabricUsername;
    private String fabricPassword;
    private boolean reportErrors = false;
    private static final Set<String> replConnGroupLocks = Collections.synchronizedSet(new HashSet());
    private static final Class<?> JDBC4_NON_TRANSIENT_CONN_EXCEPTION;

    public FabricMySQLConnectionProxy(Properties props) throws SQLException {
        String exceptionInterceptors;
        this.fabricShardKey = props.getProperty("fabricShardKey");
        this.fabricShardTable = props.getProperty("fabricShardTable");
        this.fabricServerGroup = props.getProperty("fabricServerGroup");
        this.fabricProtocol = props.getProperty("fabricProtocol");
        this.fabricUsername = props.getProperty("fabricUsername");
        this.fabricPassword = props.getProperty("fabricPassword");
        this.reportErrors = Boolean.valueOf(props.getProperty("fabricReportErrors"));
        props.remove("fabricShardKey");
        props.remove("fabricShardTable");
        props.remove("fabricServerGroup");
        props.remove("fabricProtocol");
        props.remove("fabricUsername");
        props.remove("fabricPassword");
        props.remove("fabricReportErrors");
        this.host = props.getProperty("HOST");
        this.port = props.getProperty("PORT");
        this.username = props.getProperty("user");
        this.password = props.getProperty("password");
        this.database = props.getProperty("DBNAME");
        if (this.username == null) {
            this.username = "";
        }
        if (this.password == null) {
            this.password = "";
        }
        exceptionInterceptors = (exceptionInterceptors = props.getProperty("exceptionInterceptors")) == null || "null".equals("exceptionInterceptors") ? "" : exceptionInterceptors + ",";
        exceptionInterceptors = exceptionInterceptors + "com.mysql.fabric.jdbc.ErrorReportingExceptionInterceptor";
        props.setProperty("exceptionInterceptors", exceptionInterceptors);
        this.initializeProperties(props);
        if (this.fabricServerGroup != null && this.fabricShardTable != null) {
            throw SQLError.createSQLException("Server group and shard table are mutually exclusive. Only one may be provided.", "08004", null, this.getExceptionInterceptor(), this);
        }
        try {
            String url = this.fabricProtocol + "://" + this.host + ":" + this.port;
            this.fabricConnection = new FabricConnection(url, this.fabricUsername, this.fabricPassword);
        }
        catch (FabricCommunicationException ex) {
            throw SQLError.createSQLException("Unable to establish connection to the Fabric server", "08004", ex, this.getExceptionInterceptor(), this);
        }
        this.log = LogFactory.getLogger(this.getLogger(), "FabricMySQLConnectionProxy", null);
        this.setShardTable(this.fabricShardTable);
        this.setShardKey(this.fabricShardKey);
        this.setServerGroupName(this.fabricServerGroup);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    synchronized SQLException interceptException(SQLException sqlEx, Connection conn, String groupName, String hostname, String portNumber) throws FabricCommunicationException {
        if ((sqlEx.getSQLState() == null || !sqlEx.getSQLState().startsWith("08")) && !MySQLNonTransientConnectionException.class.isAssignableFrom(sqlEx.getClass()) && (JDBC4_NON_TRANSIENT_CONN_EXCEPTION == null || !JDBC4_NON_TRANSIENT_CONN_EXCEPTION.isAssignableFrom(sqlEx.getClass())) || sqlEx.getCause() != null && FabricCommunicationException.class.isAssignableFrom(sqlEx.getCause().getClass())) {
            return null;
        }
        Server currentServer = this.serverGroup.getServer(hostname + ":" + portNumber);
        if (currentServer == null) {
            return null;
        }
        if (this.reportErrors) {
            this.fabricConnection.getClient().reportServerError(currentServer, sqlEx.toString(), true);
        }
        if (!replConnGroupLocks.add(this.serverGroup.getName())) return SQLError.createSQLException("Fabric state syncing already in progress in another thread.", "08006", sqlEx, null);
        try {
            try {
                this.fabricConnection.refreshStatePassive();
                this.setCurrentServerGroup(this.serverGroup.getName());
            }
            catch (SQLException ex) {
                SQLException sQLException = SQLError.createSQLException("Unable to refresh Fabric state. Failover impossible", "08006", ex, null);
                Object var10_11 = null;
                replConnGroupLocks.remove(this.serverGroup.getName());
                return sQLException;
            }
        }
        catch (Throwable throwable) {
            Object var10_14 = null;
            replConnGroupLocks.remove(this.serverGroup.getName());
            throw throwable;
        }
        try {
            this.syncGroupServersToReplicationConnectionGroup(ReplicationConnectionGroupManager.getConnectionGroup(groupName));
        }
        catch (SQLException ex) {
            SQLException sQLException = ex;
            Object var10_12 = null;
            replConnGroupLocks.remove(this.serverGroup.getName());
            return sQLException;
        }
        Object var10_13 = null;
        replConnGroupLocks.remove(this.serverGroup.getName());
        return null;
    }

    private void refreshStateIfNecessary() throws SQLException {
        if (this.fabricConnection.isStateExpired()) {
            this.fabricConnection.refreshStatePassive();
            if (this.serverGroup != null) {
                this.setCurrentServerGroup(this.serverGroup.getName());
            }
        }
    }

    @Override
    public void setShardKey(String shardKey) throws SQLException {
        this.ensureNoTransactionInProgress();
        this.currentConnection = null;
        if (shardKey != null) {
            if (this.serverGroupName != null) {
                throw SQLError.createSQLException("Shard key cannot be provided when server group is chosen directly.", "S1009", null, this.getExceptionInterceptor(), this);
            }
            if (this.shardTable == null) {
                throw SQLError.createSQLException("Shard key cannot be provided without a shard table.", "S1009", null, this.getExceptionInterceptor(), this);
            }
            this.setCurrentServerGroup(this.shardMapping.getGroupNameForKey(shardKey));
        } else if (this.shardTable != null) {
            this.setCurrentServerGroup(this.shardMapping.getGlobalGroupName());
        }
        this.shardKey = shardKey;
    }

    @Override
    public String getShardKey() {
        return this.shardKey;
    }

    @Override
    public void setShardTable(String shardTable) throws SQLException {
        this.ensureNoTransactionInProgress();
        this.currentConnection = null;
        if (this.serverGroupName != null) {
            throw SQLError.createSQLException("Server group and shard table are mutually exclusive. Only one may be provided.", "S1009", null, this.getExceptionInterceptor(), this);
        }
        this.shardKey = null;
        this.serverGroup = null;
        this.shardTable = shardTable;
        if (shardTable == null) {
            this.shardMapping = null;
        } else {
            String table = shardTable;
            String db = this.database;
            if (shardTable.contains(".")) {
                String[] pair = shardTable.split("\\.");
                table = pair[0];
                db = pair[1];
            }
            this.shardMapping = this.fabricConnection.getShardMapping(db, table);
            if (this.shardMapping == null) {
                throw SQLError.createSQLException("Shard mapping not found for table `" + shardTable + "'", "S1009", null, this.getExceptionInterceptor(), this);
            }
            this.setCurrentServerGroup(this.shardMapping.getGlobalGroupName());
        }
    }

    @Override
    public String getShardTable() {
        return this.shardTable;
    }

    @Override
    public void setServerGroupName(String serverGroupName) throws SQLException {
        this.ensureNoTransactionInProgress();
        this.currentConnection = null;
        if (serverGroupName != null) {
            this.setCurrentServerGroup(serverGroupName);
        }
        this.serverGroupName = serverGroupName;
    }

    @Override
    public String getServerGroupName() {
        return this.serverGroupName;
    }

    @Override
    public void clearServerSelectionCriteria() throws SQLException {
        this.ensureNoTransactionInProgress();
        this.shardTable = null;
        this.shardKey = null;
        this.serverGroupName = null;
        this.serverGroup = null;
        this.queryTables.clear();
        this.currentConnection = null;
    }

    @Override
    public ServerGroup getCurrentServerGroup() {
        return this.serverGroup;
    }

    @Override
    public void clearQueryTables() throws SQLException {
        this.ensureNoTransactionInProgress();
        this.currentConnection = null;
        this.queryTables.clear();
        this.setShardTable(null);
    }

    @Override
    public void addQueryTable(String tableName) throws SQLException {
        this.ensureNoTransactionInProgress();
        this.currentConnection = null;
        if (this.shardMapping == null) {
            if (this.fabricConnection.getShardMapping(this.database, tableName) != null) {
                this.setShardTable(tableName);
            }
        } else {
            ShardMapping mappingForTableName = this.fabricConnection.getShardMapping(this.database, tableName);
            if (mappingForTableName != null && !mappingForTableName.equals(this.shardMapping)) {
                throw SQLError.createSQLException("Cross-shard query not allowed", "S1009", null, this.getExceptionInterceptor(), this);
            }
        }
        this.queryTables.add(tableName);
    }

    @Override
    public Set<String> getQueryTables() {
        return this.queryTables;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void setCurrentServerGroup(String serverGroupName) throws SQLException {
        this.serverGroup = this.fabricConnection.getServerGroup(serverGroupName);
        if (this.serverGroup == null) {
            throw SQLError.createSQLException("Cannot find server group: `" + serverGroupName + "'", "S1009", null, this.getExceptionInterceptor(), this);
        }
        ReplicationConnectionGroup replConnGroup = ReplicationConnectionGroupManager.getConnectionGroup(serverGroupName);
        if (replConnGroup != null && replConnGroupLocks.add(this.serverGroup.getName())) {
            try {
                this.syncGroupServersToReplicationConnectionGroup(replConnGroup);
                Object var4_3 = null;
                replConnGroupLocks.remove(this.serverGroup.getName());
            }
            catch (Throwable throwable) {
                Object var4_4 = null;
                replConnGroupLocks.remove(this.serverGroup.getName());
                throw throwable;
            }
            {
            }
        }
    }

    protected MySQLConnection getActiveMySQLConnection() throws SQLException {
        ReplicationConnection c = (ReplicationConnection)this.getActiveConnection();
        MySQLConnection mc = (MySQLConnection)c.getCurrentConnection();
        return mc;
    }

    protected MySQLConnection getActiveMySQLConnectionPassive() {
        try {
            return this.getActiveMySQLConnection();
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Unable to determine active connection", ex);
        }
    }

    protected Connection getActiveConnectionPassive() {
        try {
            return this.getActiveConnection();
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Unable to determine active connection", ex);
        }
    }

    private void syncGroupServersToReplicationConnectionGroup(ReplicationConnectionGroup replConnGroup) throws SQLException {
        Server newMaster;
        String currentMasterString = null;
        if (replConnGroup.getMasterHosts().size() == 1) {
            currentMasterString = replConnGroup.getMasterHosts().iterator().next();
        }
        if (!(currentMasterString == null || this.serverGroup.getMaster() != null && currentMasterString.equals(this.serverGroup.getMaster().getHostPortString()))) {
            try {
                replConnGroup.removeMasterHost(currentMasterString, false);
            }
            catch (SQLException ex) {
                this.getLog().logWarn("Unable to remove master: " + currentMasterString, ex);
            }
        }
        if ((newMaster = this.serverGroup.getMaster()) != null && replConnGroup.getMasterHosts().size() == 0) {
            this.getLog().logInfo("Changing master for group '" + replConnGroup.getGroupName() + "' to: " + newMaster);
            try {
                if (!replConnGroup.getSlaveHosts().contains(newMaster.getHostPortString())) {
                    replConnGroup.addSlaveHost(newMaster.getHostPortString());
                }
                replConnGroup.promoteSlaveToMaster(newMaster.getHostPortString());
            }
            catch (SQLException ex) {
                throw SQLError.createSQLException("Unable to promote new master '" + newMaster.toString() + "'", ex.getSQLState(), ex, null);
            }
        }
        for (Server s : this.serverGroup.getServers()) {
            if (!s.isSlave()) continue;
            try {
                replConnGroup.addSlaveHost(s.getHostPortString());
            }
            catch (SQLException ex) {
                this.getLog().logWarn("Unable to add slave: " + s.toString(), ex);
            }
        }
        for (String hostPortString : replConnGroup.getSlaveHosts()) {
            Server fabServer = this.serverGroup.getServer(hostPortString);
            if (fabServer != null && fabServer.isSlave()) continue;
            try {
                replConnGroup.removeSlaveHost(hostPortString, true);
            }
            catch (SQLException ex) {
                this.getLog().logWarn("Unable to remove slave: " + hostPortString, ex);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Connection getActiveConnection() throws SQLException {
        if (!this.transactionInProgress) {
            this.refreshStateIfNecessary();
        }
        if (this.currentConnection != null) {
            return this.currentConnection;
        }
        if (this.getCurrentServerGroup() == null) {
            throw SQLError.createSQLException("No server group selected.", "08004", null, this.getExceptionInterceptor(), this);
        }
        this.currentConnection = this.serverConnections.get(this.serverGroup);
        if (this.currentConnection != null) {
            return this.currentConnection;
        }
        ArrayList<String> masterHost = new ArrayList<String>();
        ArrayList<String> slaveHosts = new ArrayList<String>();
        for (Server s : this.serverGroup.getServers()) {
            if (s.isMaster()) {
                masterHost.add(s.getHostPortString());
                continue;
            }
            if (!s.isSlave()) continue;
            slaveHosts.add(s.getHostPortString());
        }
        Properties info = this.exposeAsProperties(null);
        ReplicationConnectionGroup replConnGroup = ReplicationConnectionGroupManager.getConnectionGroup(this.serverGroup.getName());
        if (replConnGroup != null && replConnGroupLocks.add(this.serverGroup.getName())) {
            try {
                this.syncGroupServersToReplicationConnectionGroup(replConnGroup);
                Object var6_5 = null;
                replConnGroupLocks.remove(this.serverGroup.getName());
            }
            catch (Throwable throwable) {
                Object var6_6 = null;
                replConnGroupLocks.remove(this.serverGroup.getName());
                throw throwable;
            }
            {
            }
        }
        info.put("replicationConnectionGroup", this.serverGroup.getName());
        info.setProperty("user", this.username);
        info.setProperty("password", this.password);
        info.setProperty("DBNAME", this.getCatalog());
        info.setProperty("connectionAttributes", "fabricHaGroup:" + this.serverGroup.getName());
        info.setProperty("retriesAllDown", "1");
        info.setProperty("allowMasterDownConnections", "true");
        info.setProperty("allowSlaveDownConnections", "true");
        info.setProperty("readFromMasterWhenNoSlaves", "true");
        this.currentConnection = ReplicationConnectionProxy.createProxyInstance(masterHost, info, slaveHosts, info);
        this.serverConnections.put(this.serverGroup, this.currentConnection);
        this.currentConnection.setProxy(this);
        this.currentConnection.setAutoCommit(this.autoCommit);
        this.currentConnection.setReadOnly(this.readOnly);
        this.currentConnection.setTransactionIsolation(this.transactionIsolation);
        return this.currentConnection;
    }

    private void ensureOpen() throws SQLException {
        if (this.closed) {
            throw SQLError.createSQLException("No operations allowed after connection closed.", "08003", this.getExceptionInterceptor());
        }
    }

    private void ensureNoTransactionInProgress() throws SQLException {
        this.ensureOpen();
        if (this.transactionInProgress && !this.autoCommit) {
            throw SQLError.createSQLException("Not allow while a transaction is active.", "25000", this.getExceptionInterceptor());
        }
    }

    @Override
    public void close() throws SQLException {
        this.closed = true;
        for (ReplicationConnection c : this.serverConnections.values()) {
            try {
                c.close();
            }
            catch (SQLException sQLException) {}
        }
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return !this.closed;
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        this.readOnly = readOnly;
        for (ReplicationConnection conn : this.serverConnections.values()) {
            conn.setReadOnly(readOnly);
        }
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return this.readOnly;
    }

    @Override
    public boolean isReadOnly(boolean useSessionStatus) throws SQLException {
        return this.readOnly;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        this.database = catalog;
        for (ReplicationConnection c : this.serverConnections.values()) {
            c.setCatalog(catalog);
        }
    }

    @Override
    public String getCatalog() {
        return this.database;
    }

    @Override
    public void rollback() throws SQLException {
        this.getActiveConnection().rollback();
        this.transactionCompleted();
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        this.getActiveConnection().rollback();
        this.transactionCompleted();
    }

    @Override
    public void commit() throws SQLException {
        this.getActiveConnection().commit();
        this.transactionCompleted();
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
        for (ReplicationConnection c : this.serverConnections.values()) {
            c.setAutoCommit(this.autoCommit);
        }
    }

    @Override
    public void transactionBegun() throws SQLException {
        if (!this.autoCommit) {
            this.transactionInProgress = true;
        }
    }

    @Override
    public void transactionCompleted() throws SQLException {
        this.transactionInProgress = false;
        this.refreshStateIfNecessary();
    }

    @Override
    public boolean getAutoCommit() {
        return this.autoCommit;
    }

    @Override
    @Deprecated
    public MySQLConnection getLoadBalanceSafeProxy() {
        return this.getMultiHostSafeProxy();
    }

    @Override
    public MySQLConnection getMultiHostSafeProxy() {
        return this.getActiveMySQLConnectionPassive();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        this.transactionIsolation = level;
        for (ReplicationConnection c : this.serverConnections.values()) {
            c.setTransactionIsolation(level);
        }
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        for (ReplicationConnection c : this.serverConnections.values()) {
            c.setTypeMap(map);
        }
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        for (ReplicationConnection c : this.serverConnections.values()) {
            c.setHoldability(holdability);
        }
    }

    @Override
    public void setProxy(MySQLConnection proxy) {
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return this.getActiveConnection().setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        this.transactionInProgress = true;
        return this.getActiveConnection().setSavepoint(name);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) {
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().prepareCall(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().prepareStatement(sql, columnNames);
    }

    @Override
    public PreparedStatement clientPrepareStatement(String sql) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().clientPrepareStatement(sql);
    }

    @Override
    public PreparedStatement clientPrepareStatement(String sql, int autoGenKeyIndex) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().clientPrepareStatement(sql, autoGenKeyIndex);
    }

    @Override
    public PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().clientPrepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement clientPrepareStatement(String sql, int[] autoGenKeyIndexes) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().clientPrepareStatement(sql, autoGenKeyIndexes);
    }

    @Override
    public PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().clientPrepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement clientPrepareStatement(String sql, String[] autoGenKeyColNames) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().clientPrepareStatement(sql, autoGenKeyColNames);
    }

    @Override
    public PreparedStatement serverPrepareStatement(String sql) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().serverPrepareStatement(sql);
    }

    @Override
    public PreparedStatement serverPrepareStatement(String sql, int autoGenKeyIndex) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().serverPrepareStatement(sql, autoGenKeyIndex);
    }

    @Override
    public PreparedStatement serverPrepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().serverPrepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement serverPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().serverPrepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement serverPrepareStatement(String sql, int[] autoGenKeyIndexes) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().serverPrepareStatement(sql, autoGenKeyIndexes);
    }

    @Override
    public PreparedStatement serverPrepareStatement(String sql, String[] autoGenKeyColNames) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().serverPrepareStatement(sql, autoGenKeyColNames);
    }

    @Override
    public java.sql.Statement createStatement() throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().createStatement();
    }

    @Override
    public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public ResultSetInternalMethods execSQL(StatementImpl callingStatement, String sql, int maxRows, Buffer packet, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Field[] cachedMetadata) throws SQLException {
        return this.getActiveMySQLConnection().execSQL(callingStatement, sql, maxRows, packet, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata);
    }

    @Override
    public ResultSetInternalMethods execSQL(StatementImpl callingStatement, String sql, int maxRows, Buffer packet, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Field[] cachedMetadata, boolean isBatch) throws SQLException {
        return this.getActiveMySQLConnection().execSQL(callingStatement, sql, maxRows, packet, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata, isBatch);
    }

    @Override
    public String extractSqlFromPacket(String possibleSqlQuery, Buffer queryPacket, int endOfQueryPacketPosition) throws SQLException {
        return this.getActiveMySQLConnection().extractSqlFromPacket(possibleSqlQuery, queryPacket, endOfQueryPacketPosition);
    }

    @Override
    public StringBuilder generateConnectionCommentBlock(StringBuilder buf) {
        return this.getActiveMySQLConnectionPassive().generateConnectionCommentBlock(buf);
    }

    @Override
    public MysqlIO getIO() throws SQLException {
        return this.getActiveMySQLConnection().getIO();
    }

    @Override
    public Calendar getCalendarInstanceForSessionOrNew() {
        return this.getActiveMySQLConnectionPassive().getCalendarInstanceForSessionOrNew();
    }

    @Override
    @Deprecated
    public String getServerCharacterEncoding() {
        return this.getServerCharset();
    }

    @Override
    public String getServerCharset() {
        return this.getActiveMySQLConnectionPassive().getServerCharset();
    }

    @Override
    public TimeZone getServerTimezoneTZ() {
        return this.getActiveMySQLConnectionPassive().getServerTimezoneTZ();
    }

    @Override
    public boolean versionMeetsMinimum(int major, int minor, int subminor) throws SQLException {
        return this.getActiveConnection().versionMeetsMinimum(major, minor, subminor);
    }

    @Override
    public boolean supportsIsolationLevel() {
        return this.getActiveConnectionPassive().supportsIsolationLevel();
    }

    @Override
    public boolean supportsQuotedIdentifiers() {
        return this.getActiveConnectionPassive().supportsQuotedIdentifiers();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return this.getActiveConnection().getMetaData();
    }

    @Override
    public String getCharacterSetMetadata() {
        return this.getActiveMySQLConnectionPassive().getCharacterSetMetadata();
    }

    @Override
    public java.sql.Statement getMetadataSafeStatement() throws SQLException {
        return this.getActiveMySQLConnection().getMetadataSafeStatement();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        return null;
    }

    @Override
    public void unSafeStatementInterceptors() throws SQLException {
    }

    @Override
    public boolean supportsTransactions() {
        return true;
    }

    @Override
    public boolean isRunningOnJDK13() {
        return false;
    }

    @Override
    public void createNewIO(boolean isForReconnect) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    public void dumpTestcaseQuery(String query) {
    }

    @Override
    public void abortInternal() throws SQLException {
    }

    @Override
    public boolean isServerLocal() throws SQLException {
        return false;
    }

    @Override
    public void shutdownServer() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    @Deprecated
    public void clearHasTriedMaster() {
    }

    @Override
    @Deprecated
    public boolean hasTriedMaster() {
        return false;
    }

    @Override
    public boolean isInGlobalTx() {
        return false;
    }

    @Override
    public void setInGlobalTx(boolean flag) {
        throw new RuntimeException("Global transactions not supported.");
    }

    @Override
    public void changeUser(String userName, String newPassword) throws SQLException {
        throw SQLError.createSQLException("User change not allowed.", this.getExceptionInterceptor());
    }

    @Override
    public void setFabricShardKey(String value) {
        this.fabricShardKey = value;
    }

    @Override
    public String getFabricShardKey() {
        return this.fabricShardKey;
    }

    @Override
    public void setFabricShardTable(String value) {
        this.fabricShardTable = value;
    }

    @Override
    public String getFabricShardTable() {
        return this.fabricShardTable;
    }

    @Override
    public void setFabricServerGroup(String value) {
        this.fabricServerGroup = value;
    }

    @Override
    public String getFabricServerGroup() {
        return this.fabricServerGroup;
    }

    @Override
    public void setFabricProtocol(String value) {
        this.fabricProtocol = value;
    }

    @Override
    public String getFabricProtocol() {
        return this.fabricProtocol;
    }

    @Override
    public void setFabricUsername(String value) {
        this.fabricUsername = value;
    }

    @Override
    public String getFabricUsername() {
        return this.fabricUsername;
    }

    @Override
    public void setFabricPassword(String value) {
        this.fabricPassword = value;
    }

    @Override
    public String getFabricPassword() {
        return this.fabricPassword;
    }

    @Override
    public void setFabricReportErrors(boolean value) {
        this.reportErrors = value;
    }

    @Override
    public boolean getFabricReportErrors() {
        return this.reportErrors;
    }

    @Override
    public void setAllowLoadLocalInfile(boolean property) {
        super.setAllowLoadLocalInfile(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAllowLoadLocalInfile(property);
        }
    }

    @Override
    public void setAllowMultiQueries(boolean property) {
        super.setAllowMultiQueries(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAllowMultiQueries(property);
        }
    }

    @Override
    public void setAllowNanAndInf(boolean flag) {
        super.setAllowNanAndInf(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAllowNanAndInf(flag);
        }
    }

    @Override
    public void setAllowUrlInLocalInfile(boolean flag) {
        super.setAllowUrlInLocalInfile(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAllowUrlInLocalInfile(flag);
        }
    }

    @Override
    public void setAlwaysSendSetIsolation(boolean flag) {
        super.setAlwaysSendSetIsolation(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAlwaysSendSetIsolation(flag);
        }
    }

    @Override
    public void setAutoDeserialize(boolean flag) {
        super.setAutoDeserialize(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAutoDeserialize(flag);
        }
    }

    @Override
    public void setAutoGenerateTestcaseScript(boolean flag) {
        super.setAutoGenerateTestcaseScript(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAutoGenerateTestcaseScript(flag);
        }
    }

    @Override
    public void setAutoReconnect(boolean flag) {
        super.setAutoReconnect(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAutoReconnect(flag);
        }
    }

    @Override
    public void setAutoReconnectForConnectionPools(boolean property) {
        super.setAutoReconnectForConnectionPools(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAutoReconnectForConnectionPools(property);
        }
    }

    @Override
    public void setAutoReconnectForPools(boolean flag) {
        super.setAutoReconnectForPools(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAutoReconnectForPools(flag);
        }
    }

    @Override
    public void setBlobSendChunkSize(String value) throws SQLException {
        super.setBlobSendChunkSize(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setBlobSendChunkSize(value);
        }
    }

    @Override
    public void setCacheCallableStatements(boolean flag) {
        super.setCacheCallableStatements(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCacheCallableStatements(flag);
        }
    }

    @Override
    public void setCachePreparedStatements(boolean flag) {
        super.setCachePreparedStatements(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCachePreparedStatements(flag);
        }
    }

    @Override
    public void setCacheResultSetMetadata(boolean property) {
        super.setCacheResultSetMetadata(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCacheResultSetMetadata(property);
        }
    }

    @Override
    public void setCacheServerConfiguration(boolean flag) {
        super.setCacheServerConfiguration(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCacheServerConfiguration(flag);
        }
    }

    @Override
    public void setCallableStatementCacheSize(int size) throws SQLException {
        super.setCallableStatementCacheSize(size);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCallableStatementCacheSize(size);
        }
    }

    @Override
    public void setCapitalizeDBMDTypes(boolean property) {
        super.setCapitalizeDBMDTypes(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCapitalizeDBMDTypes(property);
        }
    }

    @Override
    public void setCapitalizeTypeNames(boolean flag) {
        super.setCapitalizeTypeNames(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCapitalizeTypeNames(flag);
        }
    }

    @Override
    public void setCharacterEncoding(String encoding) {
        super.setCharacterEncoding(encoding);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCharacterEncoding(encoding);
        }
    }

    @Override
    public void setCharacterSetResults(String characterSet) {
        super.setCharacterSetResults(characterSet);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCharacterSetResults(characterSet);
        }
    }

    @Override
    public void setClobberStreamingResults(boolean flag) {
        super.setClobberStreamingResults(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setClobberStreamingResults(flag);
        }
    }

    @Override
    public void setClobCharacterEncoding(String encoding) {
        super.setClobCharacterEncoding(encoding);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setClobCharacterEncoding(encoding);
        }
    }

    @Override
    public void setConnectionCollation(String collation) {
        super.setConnectionCollation(collation);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setConnectionCollation(collation);
        }
    }

    @Override
    public void setConnectTimeout(int timeoutMs) throws SQLException {
        super.setConnectTimeout(timeoutMs);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setConnectTimeout(timeoutMs);
        }
    }

    @Override
    public void setContinueBatchOnError(boolean property) {
        super.setContinueBatchOnError(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setContinueBatchOnError(property);
        }
    }

    @Override
    public void setCreateDatabaseIfNotExist(boolean flag) {
        super.setCreateDatabaseIfNotExist(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCreateDatabaseIfNotExist(flag);
        }
    }

    @Override
    public void setDefaultFetchSize(int n) throws SQLException {
        super.setDefaultFetchSize(n);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDefaultFetchSize(n);
        }
    }

    @Override
    public void setDetectServerPreparedStmts(boolean property) {
        super.setDetectServerPreparedStmts(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDetectServerPreparedStmts(property);
        }
    }

    @Override
    public void setDontTrackOpenResources(boolean flag) {
        super.setDontTrackOpenResources(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDontTrackOpenResources(flag);
        }
    }

    @Override
    public void setDumpQueriesOnException(boolean flag) {
        super.setDumpQueriesOnException(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDumpQueriesOnException(flag);
        }
    }

    @Override
    public void setDynamicCalendars(boolean flag) {
        super.setDynamicCalendars(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDynamicCalendars(flag);
        }
    }

    @Override
    public void setElideSetAutoCommits(boolean flag) {
        super.setElideSetAutoCommits(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setElideSetAutoCommits(flag);
        }
    }

    @Override
    public void setEmptyStringsConvertToZero(boolean flag) {
        super.setEmptyStringsConvertToZero(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setEmptyStringsConvertToZero(flag);
        }
    }

    @Override
    public void setEmulateLocators(boolean property) {
        super.setEmulateLocators(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setEmulateLocators(property);
        }
    }

    @Override
    public void setEmulateUnsupportedPstmts(boolean flag) {
        super.setEmulateUnsupportedPstmts(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setEmulateUnsupportedPstmts(flag);
        }
    }

    @Override
    public void setEnablePacketDebug(boolean flag) {
        super.setEnablePacketDebug(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setEnablePacketDebug(flag);
        }
    }

    @Override
    public void setEncoding(String property) {
        super.setEncoding(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setEncoding(property);
        }
    }

    @Override
    public void setExplainSlowQueries(boolean flag) {
        super.setExplainSlowQueries(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setExplainSlowQueries(flag);
        }
    }

    @Override
    public void setFailOverReadOnly(boolean flag) {
        super.setFailOverReadOnly(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setFailOverReadOnly(flag);
        }
    }

    @Override
    public void setGatherPerformanceMetrics(boolean flag) {
        super.setGatherPerformanceMetrics(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setGatherPerformanceMetrics(flag);
        }
    }

    @Override
    public void setHoldResultsOpenOverStatementClose(boolean flag) {
        super.setHoldResultsOpenOverStatementClose(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setHoldResultsOpenOverStatementClose(flag);
        }
    }

    @Override
    public void setIgnoreNonTxTables(boolean property) {
        super.setIgnoreNonTxTables(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setIgnoreNonTxTables(property);
        }
    }

    @Override
    public void setInitialTimeout(int property) throws SQLException {
        super.setInitialTimeout(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setInitialTimeout(property);
        }
    }

    @Override
    public void setIsInteractiveClient(boolean property) {
        super.setIsInteractiveClient(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setIsInteractiveClient(property);
        }
    }

    @Override
    public void setJdbcCompliantTruncation(boolean flag) {
        super.setJdbcCompliantTruncation(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setJdbcCompliantTruncation(flag);
        }
    }

    @Override
    public void setLocatorFetchBufferSize(String value) throws SQLException {
        super.setLocatorFetchBufferSize(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLocatorFetchBufferSize(value);
        }
    }

    @Override
    public void setLogger(String property) {
        super.setLogger(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLogger(property);
        }
    }

    @Override
    public void setLoggerClassName(String className) {
        super.setLoggerClassName(className);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoggerClassName(className);
        }
    }

    @Override
    public void setLogSlowQueries(boolean flag) {
        super.setLogSlowQueries(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLogSlowQueries(flag);
        }
    }

    @Override
    public void setMaintainTimeStats(boolean flag) {
        super.setMaintainTimeStats(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setMaintainTimeStats(flag);
        }
    }

    @Override
    public void setMaxQuerySizeToLog(int sizeInBytes) throws SQLException {
        super.setMaxQuerySizeToLog(sizeInBytes);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setMaxQuerySizeToLog(sizeInBytes);
        }
    }

    @Override
    public void setMaxReconnects(int property) throws SQLException {
        super.setMaxReconnects(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setMaxReconnects(property);
        }
    }

    @Override
    public void setMaxRows(int property) throws SQLException {
        super.setMaxRows(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setMaxRows(property);
        }
    }

    @Override
    public void setMetadataCacheSize(int value) throws SQLException {
        super.setMetadataCacheSize(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setMetadataCacheSize(value);
        }
    }

    @Override
    public void setNoDatetimeStringSync(boolean flag) {
        super.setNoDatetimeStringSync(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setNoDatetimeStringSync(flag);
        }
    }

    @Override
    public void setNullCatalogMeansCurrent(boolean value) {
        super.setNullCatalogMeansCurrent(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setNullCatalogMeansCurrent(value);
        }
    }

    @Override
    public void setNullNamePatternMatchesAll(boolean value) {
        super.setNullNamePatternMatchesAll(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setNullNamePatternMatchesAll(value);
        }
    }

    @Override
    public void setPacketDebugBufferSize(int size) throws SQLException {
        super.setPacketDebugBufferSize(size);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPacketDebugBufferSize(size);
        }
    }

    @Override
    public void setParanoid(boolean property) {
        super.setParanoid(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setParanoid(property);
        }
    }

    @Override
    public void setPedantic(boolean property) {
        super.setPedantic(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPedantic(property);
        }
    }

    @Override
    public void setPreparedStatementCacheSize(int cacheSize) throws SQLException {
        super.setPreparedStatementCacheSize(cacheSize);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPreparedStatementCacheSize(cacheSize);
        }
    }

    @Override
    public void setPreparedStatementCacheSqlLimit(int cacheSqlLimit) throws SQLException {
        super.setPreparedStatementCacheSqlLimit(cacheSqlLimit);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPreparedStatementCacheSqlLimit(cacheSqlLimit);
        }
    }

    @Override
    public void setProfileSql(boolean property) {
        super.setProfileSql(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setProfileSql(property);
        }
    }

    @Override
    public void setProfileSQL(boolean flag) {
        super.setProfileSQL(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setProfileSQL(flag);
        }
    }

    @Override
    public void setPropertiesTransform(String value) {
        super.setPropertiesTransform(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPropertiesTransform(value);
        }
    }

    @Override
    public void setQueriesBeforeRetryMaster(int property) throws SQLException {
        super.setQueriesBeforeRetryMaster(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setQueriesBeforeRetryMaster(property);
        }
    }

    @Override
    public void setReconnectAtTxEnd(boolean property) {
        super.setReconnectAtTxEnd(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setReconnectAtTxEnd(property);
        }
    }

    @Override
    public void setRelaxAutoCommit(boolean property) {
        super.setRelaxAutoCommit(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setRelaxAutoCommit(property);
        }
    }

    @Override
    public void setReportMetricsIntervalMillis(int millis) throws SQLException {
        super.setReportMetricsIntervalMillis(millis);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setReportMetricsIntervalMillis(millis);
        }
    }

    @Override
    public void setRequireSSL(boolean property) {
        super.setRequireSSL(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setRequireSSL(property);
        }
    }

    @Override
    public void setRetainStatementAfterResultSetClose(boolean flag) {
        super.setRetainStatementAfterResultSetClose(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setRetainStatementAfterResultSetClose(flag);
        }
    }

    @Override
    public void setRollbackOnPooledClose(boolean flag) {
        super.setRollbackOnPooledClose(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setRollbackOnPooledClose(flag);
        }
    }

    @Override
    public void setRoundRobinLoadBalance(boolean flag) {
        super.setRoundRobinLoadBalance(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setRoundRobinLoadBalance(flag);
        }
    }

    @Override
    public void setRunningCTS13(boolean flag) {
        super.setRunningCTS13(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setRunningCTS13(flag);
        }
    }

    @Override
    public void setSecondsBeforeRetryMaster(int property) throws SQLException {
        super.setSecondsBeforeRetryMaster(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSecondsBeforeRetryMaster(property);
        }
    }

    @Override
    public void setServerTimezone(String property) {
        super.setServerTimezone(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setServerTimezone(property);
        }
    }

    @Override
    public void setSessionVariables(String variables) {
        super.setSessionVariables(variables);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSessionVariables(variables);
        }
    }

    @Override
    public void setSlowQueryThresholdMillis(int millis) throws SQLException {
        super.setSlowQueryThresholdMillis(millis);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSlowQueryThresholdMillis(millis);
        }
    }

    @Override
    public void setSocketFactoryClassName(String property) {
        super.setSocketFactoryClassName(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSocketFactoryClassName(property);
        }
    }

    @Override
    public void setSocketTimeout(int property) throws SQLException {
        super.setSocketTimeout(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSocketTimeout(property);
        }
    }

    @Override
    public void setStrictFloatingPoint(boolean property) {
        super.setStrictFloatingPoint(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setStrictFloatingPoint(property);
        }
    }

    @Override
    public void setStrictUpdates(boolean property) {
        super.setStrictUpdates(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setStrictUpdates(property);
        }
    }

    @Override
    public void setTinyInt1isBit(boolean flag) {
        super.setTinyInt1isBit(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTinyInt1isBit(flag);
        }
    }

    @Override
    public void setTraceProtocol(boolean flag) {
        super.setTraceProtocol(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTraceProtocol(flag);
        }
    }

    @Override
    public void setTransformedBitIsBoolean(boolean flag) {
        super.setTransformedBitIsBoolean(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTransformedBitIsBoolean(flag);
        }
    }

    @Override
    public void setUseCompression(boolean property) {
        super.setUseCompression(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseCompression(property);
        }
    }

    @Override
    public void setUseFastIntParsing(boolean flag) {
        super.setUseFastIntParsing(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseFastIntParsing(flag);
        }
    }

    @Override
    public void setUseHostsInPrivileges(boolean property) {
        super.setUseHostsInPrivileges(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseHostsInPrivileges(property);
        }
    }

    @Override
    public void setUseInformationSchema(boolean flag) {
        super.setUseInformationSchema(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseInformationSchema(flag);
        }
    }

    @Override
    public void setUseLocalSessionState(boolean flag) {
        super.setUseLocalSessionState(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseLocalSessionState(flag);
        }
    }

    @Override
    public void setUseOldUTF8Behavior(boolean flag) {
        super.setUseOldUTF8Behavior(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseOldUTF8Behavior(flag);
        }
    }

    @Override
    public void setUseOnlyServerErrorMessages(boolean flag) {
        super.setUseOnlyServerErrorMessages(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseOnlyServerErrorMessages(flag);
        }
    }

    @Override
    public void setUseReadAheadInput(boolean flag) {
        super.setUseReadAheadInput(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseReadAheadInput(flag);
        }
    }

    @Override
    public void setUseServerPreparedStmts(boolean flag) {
        super.setUseServerPreparedStmts(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseServerPreparedStmts(flag);
        }
    }

    @Override
    public void setUseSqlStateCodes(boolean flag) {
        super.setUseSqlStateCodes(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseSqlStateCodes(flag);
        }
    }

    @Override
    public void setUseSSL(boolean property) {
        super.setUseSSL(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseSSL(property);
        }
    }

    @Override
    public void setUseStreamLengthsInPrepStmts(boolean property) {
        super.setUseStreamLengthsInPrepStmts(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseStreamLengthsInPrepStmts(property);
        }
    }

    @Override
    public void setUseTimezone(boolean property) {
        super.setUseTimezone(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseTimezone(property);
        }
    }

    @Override
    public void setUseUltraDevWorkAround(boolean property) {
        super.setUseUltraDevWorkAround(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseUltraDevWorkAround(property);
        }
    }

    @Override
    public void setUseUnbufferedInput(boolean flag) {
        super.setUseUnbufferedInput(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseUnbufferedInput(flag);
        }
    }

    @Override
    public void setUseUnicode(boolean flag) {
        super.setUseUnicode(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseUnicode(flag);
        }
    }

    @Override
    public void setUseUsageAdvisor(boolean useUsageAdvisorFlag) {
        super.setUseUsageAdvisor(useUsageAdvisorFlag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseUsageAdvisor(useUsageAdvisorFlag);
        }
    }

    @Override
    public void setYearIsDateType(boolean flag) {
        super.setYearIsDateType(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setYearIsDateType(flag);
        }
    }

    @Override
    public void setZeroDateTimeBehavior(String behavior) {
        super.setZeroDateTimeBehavior(behavior);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setZeroDateTimeBehavior(behavior);
        }
    }

    @Override
    public void setUseCursorFetch(boolean flag) {
        super.setUseCursorFetch(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseCursorFetch(flag);
        }
    }

    @Override
    public void setOverrideSupportsIntegrityEnhancementFacility(boolean flag) {
        super.setOverrideSupportsIntegrityEnhancementFacility(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setOverrideSupportsIntegrityEnhancementFacility(flag);
        }
    }

    @Override
    public void setNoTimezoneConversionForTimeType(boolean flag) {
        super.setNoTimezoneConversionForTimeType(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setNoTimezoneConversionForTimeType(flag);
        }
    }

    @Override
    public void setUseJDBCCompliantTimezoneShift(boolean flag) {
        super.setUseJDBCCompliantTimezoneShift(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseJDBCCompliantTimezoneShift(flag);
        }
    }

    @Override
    public void setAutoClosePStmtStreams(boolean flag) {
        super.setAutoClosePStmtStreams(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAutoClosePStmtStreams(flag);
        }
    }

    @Override
    public void setProcessEscapeCodesForPrepStmts(boolean flag) {
        super.setProcessEscapeCodesForPrepStmts(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setProcessEscapeCodesForPrepStmts(flag);
        }
    }

    @Override
    public void setUseGmtMillisForDatetimes(boolean flag) {
        super.setUseGmtMillisForDatetimes(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseGmtMillisForDatetimes(flag);
        }
    }

    @Override
    public void setDumpMetadataOnColumnNotFound(boolean flag) {
        super.setDumpMetadataOnColumnNotFound(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDumpMetadataOnColumnNotFound(flag);
        }
    }

    @Override
    public void setResourceId(String resourceId) {
        super.setResourceId(resourceId);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setResourceId(resourceId);
        }
    }

    @Override
    public void setRewriteBatchedStatements(boolean flag) {
        super.setRewriteBatchedStatements(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setRewriteBatchedStatements(flag);
        }
    }

    @Override
    public void setJdbcCompliantTruncationForReads(boolean jdbcCompliantTruncationForReads) {
        super.setJdbcCompliantTruncationForReads(jdbcCompliantTruncationForReads);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setJdbcCompliantTruncationForReads(jdbcCompliantTruncationForReads);
        }
    }

    @Override
    public void setUseJvmCharsetConverters(boolean flag) {
        super.setUseJvmCharsetConverters(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseJvmCharsetConverters(flag);
        }
    }

    @Override
    public void setPinGlobalTxToPhysicalConnection(boolean flag) {
        super.setPinGlobalTxToPhysicalConnection(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPinGlobalTxToPhysicalConnection(flag);
        }
    }

    @Override
    public void setGatherPerfMetrics(boolean flag) {
        super.setGatherPerfMetrics(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setGatherPerfMetrics(flag);
        }
    }

    @Override
    public void setUltraDevHack(boolean flag) {
        super.setUltraDevHack(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUltraDevHack(flag);
        }
    }

    @Override
    public void setInteractiveClient(boolean property) {
        super.setInteractiveClient(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setInteractiveClient(property);
        }
    }

    @Override
    public void setSocketFactory(String name) {
        super.setSocketFactory(name);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSocketFactory(name);
        }
    }

    @Override
    public void setUseServerPrepStmts(boolean flag) {
        super.setUseServerPrepStmts(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseServerPrepStmts(flag);
        }
    }

    @Override
    public void setCacheCallableStmts(boolean flag) {
        super.setCacheCallableStmts(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCacheCallableStmts(flag);
        }
    }

    @Override
    public void setCachePrepStmts(boolean flag) {
        super.setCachePrepStmts(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCachePrepStmts(flag);
        }
    }

    @Override
    public void setCallableStmtCacheSize(int cacheSize) throws SQLException {
        super.setCallableStmtCacheSize(cacheSize);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCallableStmtCacheSize(cacheSize);
        }
    }

    @Override
    public void setPrepStmtCacheSize(int cacheSize) throws SQLException {
        super.setPrepStmtCacheSize(cacheSize);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPrepStmtCacheSize(cacheSize);
        }
    }

    @Override
    public void setPrepStmtCacheSqlLimit(int sqlLimit) throws SQLException {
        super.setPrepStmtCacheSqlLimit(sqlLimit);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPrepStmtCacheSqlLimit(sqlLimit);
        }
    }

    @Override
    public void setNoAccessToProcedureBodies(boolean flag) {
        super.setNoAccessToProcedureBodies(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setNoAccessToProcedureBodies(flag);
        }
    }

    @Override
    public void setUseOldAliasMetadataBehavior(boolean flag) {
        super.setUseOldAliasMetadataBehavior(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseOldAliasMetadataBehavior(flag);
        }
    }

    @Override
    public void setClientCertificateKeyStorePassword(String value) {
        super.setClientCertificateKeyStorePassword(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setClientCertificateKeyStorePassword(value);
        }
    }

    @Override
    public void setClientCertificateKeyStoreType(String value) {
        super.setClientCertificateKeyStoreType(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setClientCertificateKeyStoreType(value);
        }
    }

    @Override
    public void setClientCertificateKeyStoreUrl(String value) {
        super.setClientCertificateKeyStoreUrl(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setClientCertificateKeyStoreUrl(value);
        }
    }

    @Override
    public void setTrustCertificateKeyStorePassword(String value) {
        super.setTrustCertificateKeyStorePassword(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTrustCertificateKeyStorePassword(value);
        }
    }

    @Override
    public void setTrustCertificateKeyStoreType(String value) {
        super.setTrustCertificateKeyStoreType(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTrustCertificateKeyStoreType(value);
        }
    }

    @Override
    public void setTrustCertificateKeyStoreUrl(String value) {
        super.setTrustCertificateKeyStoreUrl(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTrustCertificateKeyStoreUrl(value);
        }
    }

    @Override
    public void setUseSSPSCompatibleTimezoneShift(boolean flag) {
        super.setUseSSPSCompatibleTimezoneShift(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseSSPSCompatibleTimezoneShift(flag);
        }
    }

    @Override
    public void setTreatUtilDateAsTimestamp(boolean flag) {
        super.setTreatUtilDateAsTimestamp(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTreatUtilDateAsTimestamp(flag);
        }
    }

    @Override
    public void setUseFastDateParsing(boolean flag) {
        super.setUseFastDateParsing(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseFastDateParsing(flag);
        }
    }

    @Override
    public void setLocalSocketAddress(String address) {
        super.setLocalSocketAddress(address);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLocalSocketAddress(address);
        }
    }

    @Override
    public void setUseConfigs(String configs) {
        super.setUseConfigs(configs);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseConfigs(configs);
        }
    }

    @Override
    public void setGenerateSimpleParameterMetadata(boolean flag) {
        super.setGenerateSimpleParameterMetadata(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setGenerateSimpleParameterMetadata(flag);
        }
    }

    @Override
    public void setLogXaCommands(boolean flag) {
        super.setLogXaCommands(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLogXaCommands(flag);
        }
    }

    @Override
    public void setResultSetSizeThreshold(int threshold) throws SQLException {
        super.setResultSetSizeThreshold(threshold);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setResultSetSizeThreshold(threshold);
        }
    }

    @Override
    public void setNetTimeoutForStreamingResults(int value) throws SQLException {
        super.setNetTimeoutForStreamingResults(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setNetTimeoutForStreamingResults(value);
        }
    }

    @Override
    public void setEnableQueryTimeouts(boolean flag) {
        super.setEnableQueryTimeouts(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setEnableQueryTimeouts(flag);
        }
    }

    @Override
    public void setPadCharsWithSpace(boolean flag) {
        super.setPadCharsWithSpace(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPadCharsWithSpace(flag);
        }
    }

    @Override
    public void setUseDynamicCharsetInfo(boolean flag) {
        super.setUseDynamicCharsetInfo(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseDynamicCharsetInfo(flag);
        }
    }

    @Override
    public void setClientInfoProvider(String classname) {
        super.setClientInfoProvider(classname);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setClientInfoProvider(classname);
        }
    }

    @Override
    public void setPopulateInsertRowWithDefaultValues(boolean flag) {
        super.setPopulateInsertRowWithDefaultValues(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPopulateInsertRowWithDefaultValues(flag);
        }
    }

    @Override
    public void setLoadBalanceStrategy(String strategy) {
        super.setLoadBalanceStrategy(strategy);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceStrategy(strategy);
        }
    }

    @Override
    public void setTcpNoDelay(boolean flag) {
        super.setTcpNoDelay(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTcpNoDelay(flag);
        }
    }

    @Override
    public void setTcpKeepAlive(boolean flag) {
        super.setTcpKeepAlive(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTcpKeepAlive(flag);
        }
    }

    @Override
    public void setTcpRcvBuf(int bufSize) throws SQLException {
        super.setTcpRcvBuf(bufSize);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTcpRcvBuf(bufSize);
        }
    }

    @Override
    public void setTcpSndBuf(int bufSize) throws SQLException {
        super.setTcpSndBuf(bufSize);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTcpSndBuf(bufSize);
        }
    }

    @Override
    public void setTcpTrafficClass(int classFlags) throws SQLException {
        super.setTcpTrafficClass(classFlags);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTcpTrafficClass(classFlags);
        }
    }

    @Override
    public void setUseNanosForElapsedTime(boolean flag) {
        super.setUseNanosForElapsedTime(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseNanosForElapsedTime(flag);
        }
    }

    @Override
    public void setSlowQueryThresholdNanos(long nanos) throws SQLException {
        super.setSlowQueryThresholdNanos(nanos);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSlowQueryThresholdNanos(nanos);
        }
    }

    @Override
    public void setStatementInterceptors(String value) {
        super.setStatementInterceptors(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setStatementInterceptors(value);
        }
    }

    @Override
    public void setUseDirectRowUnpack(boolean flag) {
        super.setUseDirectRowUnpack(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseDirectRowUnpack(flag);
        }
    }

    @Override
    public void setLargeRowSizeThreshold(String value) throws SQLException {
        super.setLargeRowSizeThreshold(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLargeRowSizeThreshold(value);
        }
    }

    @Override
    public void setUseBlobToStoreUTF8OutsideBMP(boolean flag) {
        super.setUseBlobToStoreUTF8OutsideBMP(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseBlobToStoreUTF8OutsideBMP(flag);
        }
    }

    @Override
    public void setUtf8OutsideBmpExcludedColumnNamePattern(String regexPattern) {
        super.setUtf8OutsideBmpExcludedColumnNamePattern(regexPattern);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUtf8OutsideBmpExcludedColumnNamePattern(regexPattern);
        }
    }

    @Override
    public void setUtf8OutsideBmpIncludedColumnNamePattern(String regexPattern) {
        super.setUtf8OutsideBmpIncludedColumnNamePattern(regexPattern);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUtf8OutsideBmpIncludedColumnNamePattern(regexPattern);
        }
    }

    @Override
    public void setIncludeInnodbStatusInDeadlockExceptions(boolean flag) {
        super.setIncludeInnodbStatusInDeadlockExceptions(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setIncludeInnodbStatusInDeadlockExceptions(flag);
        }
    }

    @Override
    public void setIncludeThreadDumpInDeadlockExceptions(boolean flag) {
        super.setIncludeThreadDumpInDeadlockExceptions(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setIncludeThreadDumpInDeadlockExceptions(flag);
        }
    }

    @Override
    public void setIncludeThreadNamesAsStatementComment(boolean flag) {
        super.setIncludeThreadNamesAsStatementComment(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setIncludeThreadNamesAsStatementComment(flag);
        }
    }

    @Override
    public void setBlobsAreStrings(boolean flag) {
        super.setBlobsAreStrings(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setBlobsAreStrings(flag);
        }
    }

    @Override
    public void setFunctionsNeverReturnBlobs(boolean flag) {
        super.setFunctionsNeverReturnBlobs(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setFunctionsNeverReturnBlobs(flag);
        }
    }

    @Override
    public void setAutoSlowLog(boolean flag) {
        super.setAutoSlowLog(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAutoSlowLog(flag);
        }
    }

    @Override
    public void setConnectionLifecycleInterceptors(String interceptors) {
        super.setConnectionLifecycleInterceptors(interceptors);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setConnectionLifecycleInterceptors(interceptors);
        }
    }

    @Override
    public void setProfilerEventHandler(String handler) {
        super.setProfilerEventHandler(handler);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setProfilerEventHandler(handler);
        }
    }

    @Override
    public void setVerifyServerCertificate(boolean flag) {
        super.setVerifyServerCertificate(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setVerifyServerCertificate(flag);
        }
    }

    @Override
    public void setUseLegacyDatetimeCode(boolean flag) {
        super.setUseLegacyDatetimeCode(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseLegacyDatetimeCode(flag);
        }
    }

    @Override
    public void setSelfDestructOnPingSecondsLifetime(int seconds) throws SQLException {
        super.setSelfDestructOnPingSecondsLifetime(seconds);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSelfDestructOnPingSecondsLifetime(seconds);
        }
    }

    @Override
    public void setSelfDestructOnPingMaxOperations(int maxOperations) throws SQLException {
        super.setSelfDestructOnPingMaxOperations(maxOperations);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSelfDestructOnPingMaxOperations(maxOperations);
        }
    }

    @Override
    public void setUseColumnNamesInFindColumn(boolean flag) {
        super.setUseColumnNamesInFindColumn(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseColumnNamesInFindColumn(flag);
        }
    }

    @Override
    public void setUseLocalTransactionState(boolean flag) {
        super.setUseLocalTransactionState(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseLocalTransactionState(flag);
        }
    }

    @Override
    public void setCompensateOnDuplicateKeyUpdateCounts(boolean flag) {
        super.setCompensateOnDuplicateKeyUpdateCounts(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCompensateOnDuplicateKeyUpdateCounts(flag);
        }
    }

    @Override
    public void setUseAffectedRows(boolean flag) {
        super.setUseAffectedRows(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseAffectedRows(flag);
        }
    }

    @Override
    public void setPasswordCharacterEncoding(String characterSet) {
        super.setPasswordCharacterEncoding(characterSet);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPasswordCharacterEncoding(characterSet);
        }
    }

    @Override
    public void setLoadBalanceBlacklistTimeout(int loadBalanceBlacklistTimeout) throws SQLException {
        super.setLoadBalanceBlacklistTimeout(loadBalanceBlacklistTimeout);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceBlacklistTimeout(loadBalanceBlacklistTimeout);
        }
    }

    @Override
    public void setRetriesAllDown(int retriesAllDown) throws SQLException {
        super.setRetriesAllDown(retriesAllDown);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setRetriesAllDown(retriesAllDown);
        }
    }

    @Override
    public void setExceptionInterceptors(String exceptionInterceptors) {
        super.setExceptionInterceptors(exceptionInterceptors);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setExceptionInterceptors(exceptionInterceptors);
        }
    }

    @Override
    public void setQueryTimeoutKillsConnection(boolean queryTimeoutKillsConnection) {
        super.setQueryTimeoutKillsConnection(queryTimeoutKillsConnection);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setQueryTimeoutKillsConnection(queryTimeoutKillsConnection);
        }
    }

    @Override
    public void setLoadBalancePingTimeout(int loadBalancePingTimeout) throws SQLException {
        super.setLoadBalancePingTimeout(loadBalancePingTimeout);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalancePingTimeout(loadBalancePingTimeout);
        }
    }

    @Override
    public void setLoadBalanceValidateConnectionOnSwapServer(boolean loadBalanceValidateConnectionOnSwapServer) {
        super.setLoadBalanceValidateConnectionOnSwapServer(loadBalanceValidateConnectionOnSwapServer);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceValidateConnectionOnSwapServer(loadBalanceValidateConnectionOnSwapServer);
        }
    }

    @Override
    public void setLoadBalanceConnectionGroup(String loadBalanceConnectionGroup) {
        super.setLoadBalanceConnectionGroup(loadBalanceConnectionGroup);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceConnectionGroup(loadBalanceConnectionGroup);
        }
    }

    @Override
    public void setLoadBalanceExceptionChecker(String loadBalanceExceptionChecker) {
        super.setLoadBalanceExceptionChecker(loadBalanceExceptionChecker);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceExceptionChecker(loadBalanceExceptionChecker);
        }
    }

    @Override
    public void setLoadBalanceSQLStateFailover(String loadBalanceSQLStateFailover) {
        super.setLoadBalanceSQLStateFailover(loadBalanceSQLStateFailover);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceSQLStateFailover(loadBalanceSQLStateFailover);
        }
    }

    @Override
    public void setLoadBalanceSQLExceptionSubclassFailover(String loadBalanceSQLExceptionSubclassFailover) {
        super.setLoadBalanceSQLExceptionSubclassFailover(loadBalanceSQLExceptionSubclassFailover);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceSQLExceptionSubclassFailover(loadBalanceSQLExceptionSubclassFailover);
        }
    }

    @Override
    public void setLoadBalanceEnableJMX(boolean loadBalanceEnableJMX) {
        super.setLoadBalanceEnableJMX(loadBalanceEnableJMX);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceEnableJMX(loadBalanceEnableJMX);
        }
    }

    @Override
    public void setLoadBalanceAutoCommitStatementThreshold(int loadBalanceAutoCommitStatementThreshold) throws SQLException {
        super.setLoadBalanceAutoCommitStatementThreshold(loadBalanceAutoCommitStatementThreshold);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceAutoCommitStatementThreshold(loadBalanceAutoCommitStatementThreshold);
        }
    }

    @Override
    public void setLoadBalanceAutoCommitStatementRegex(String loadBalanceAutoCommitStatementRegex) {
        super.setLoadBalanceAutoCommitStatementRegex(loadBalanceAutoCommitStatementRegex);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceAutoCommitStatementRegex(loadBalanceAutoCommitStatementRegex);
        }
    }

    @Override
    public void setAuthenticationPlugins(String authenticationPlugins) {
        super.setAuthenticationPlugins(authenticationPlugins);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAuthenticationPlugins(authenticationPlugins);
        }
    }

    @Override
    public void setDisabledAuthenticationPlugins(String disabledAuthenticationPlugins) {
        super.setDisabledAuthenticationPlugins(disabledAuthenticationPlugins);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDisabledAuthenticationPlugins(disabledAuthenticationPlugins);
        }
    }

    @Override
    public void setDefaultAuthenticationPlugin(String defaultAuthenticationPlugin) {
        super.setDefaultAuthenticationPlugin(defaultAuthenticationPlugin);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDefaultAuthenticationPlugin(defaultAuthenticationPlugin);
        }
    }

    @Override
    public void setParseInfoCacheFactory(String factoryClassname) {
        super.setParseInfoCacheFactory(factoryClassname);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setParseInfoCacheFactory(factoryClassname);
        }
    }

    @Override
    public void setServerConfigCacheFactory(String factoryClassname) {
        super.setServerConfigCacheFactory(factoryClassname);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setServerConfigCacheFactory(factoryClassname);
        }
    }

    @Override
    public void setDisconnectOnExpiredPasswords(boolean disconnectOnExpiredPasswords) {
        super.setDisconnectOnExpiredPasswords(disconnectOnExpiredPasswords);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDisconnectOnExpiredPasswords(disconnectOnExpiredPasswords);
        }
    }

    @Override
    public void setGetProceduresReturnsFunctions(boolean getProcedureReturnsFunctions) {
        super.setGetProceduresReturnsFunctions(getProcedureReturnsFunctions);
    }

    @Override
    public int getActiveStatementCount() {
        return -1;
    }

    @Override
    public long getIdleFor() {
        return -1L;
    }

    @Override
    public Log getLog() {
        return this.log;
    }

    @Override
    public boolean isMasterConnection() {
        return false;
    }

    @Override
    public boolean isNoBackslashEscapesSet() {
        return false;
    }

    @Override
    public boolean isSameResource(Connection c) {
        return false;
    }

    @Override
    public boolean parserKnowsUnicode() {
        return false;
    }

    @Override
    public void ping() throws SQLException {
    }

    @Override
    public void resetServerState() throws SQLException {
    }

    @Override
    public void setFailedOver(boolean flag) {
    }

    @Override
    @Deprecated
    public void setPreferSlaveDuringFailover(boolean flag) {
    }

    @Override
    public void setStatementComment(String comment) {
    }

    @Override
    public void reportQueryTime(long millisOrNanos) {
    }

    @Override
    public boolean isAbonormallyLongQuery(long millisOrNanos) {
        return false;
    }

    @Override
    public void initializeExtension(Extension ex) throws SQLException {
    }

    @Override
    public int getAutoIncrementIncrement() {
        return -1;
    }

    @Override
    public boolean hasSameProperties(Connection c) {
        return false;
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public void setSchema(String schema) throws SQLException {
    }

    @Override
    public String getSchema() throws SQLException {
        return null;
    }

    @Override
    public void abort(Executor executor) throws SQLException {
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return -1;
    }

    @Override
    public void checkClosed() throws SQLException {
    }

    @Override
    public Object getConnectionMutex() {
        return this;
    }

    @Override
    public void setSessionMaxRows(int max) throws SQLException {
        for (ReplicationConnection c : this.serverConnections.values()) {
            c.setSessionMaxRows(max);
        }
    }

    @Override
    public int getSessionMaxRows() {
        return this.getActiveConnectionPassive().getSessionMaxRows();
    }

    @Override
    public boolean isProxySet() {
        return false;
    }

    @Override
    public Connection duplicate() throws SQLException {
        return null;
    }

    @Override
    public CachedResultSetMetaData getCachedMetaData(String sql) {
        return null;
    }

    @Override
    public Timer getCancelTimer() {
        return null;
    }

    @Override
    public SingleByteCharsetConverter getCharsetConverter(String javaEncodingName) throws SQLException {
        return null;
    }

    @Override
    @Deprecated
    public String getCharsetNameForIndex(int charsetIndex) throws SQLException {
        return this.getEncodingForIndex(charsetIndex);
    }

    @Override
    public String getEncodingForIndex(int charsetIndex) throws SQLException {
        return null;
    }

    @Override
    public TimeZone getDefaultTimeZone() {
        return null;
    }

    @Override
    public String getErrorMessageEncoding() {
        return null;
    }

    @Override
    public ExceptionInterceptor getExceptionInterceptor() {
        if (this.currentConnection == null) {
            return null;
        }
        return this.currentConnection.getExceptionInterceptor();
    }

    @Override
    public String getHost() {
        return null;
    }

    @Override
    public String getHostPortPair() {
        return this.getActiveMySQLConnectionPassive().getHostPortPair();
    }

    @Override
    public long getId() {
        return -1L;
    }

    @Override
    public int getMaxBytesPerChar(String javaCharsetName) throws SQLException {
        return -1;
    }

    @Override
    public int getMaxBytesPerChar(Integer charsetIndex, String javaCharsetName) throws SQLException {
        return -1;
    }

    @Override
    public int getNetBufferLength() {
        return -1;
    }

    @Override
    public boolean getRequiresEscapingEncoder() {
        return false;
    }

    @Override
    public int getServerMajorVersion() {
        return -1;
    }

    @Override
    public int getServerMinorVersion() {
        return -1;
    }

    @Override
    public int getServerSubMinorVersion() {
        return -1;
    }

    @Override
    public String getServerVariable(String variableName) {
        return null;
    }

    @Override
    public String getServerVersion() {
        return null;
    }

    @Override
    public Calendar getSessionLockedCalendar() {
        return null;
    }

    @Override
    public String getStatementComment() {
        return null;
    }

    @Override
    public List<StatementInterceptorV2> getStatementInterceptorsInstances() {
        return null;
    }

    @Override
    public String getURL() {
        return null;
    }

    @Override
    public String getUser() {
        return null;
    }

    @Override
    public Calendar getUtcCalendar() {
        return null;
    }

    @Override
    public void incrementNumberOfPreparedExecutes() {
    }

    @Override
    public void incrementNumberOfPrepares() {
    }

    @Override
    public void incrementNumberOfResultSetsCreated() {
    }

    @Override
    public void initializeResultsMetadataFromCache(String sql, CachedResultSetMetaData cachedMetaData, ResultSetInternalMethods resultSet) throws SQLException {
    }

    @Override
    public void initializeSafeStatementInterceptors() throws SQLException {
    }

    @Override
    public boolean isClientTzUTC() {
        return false;
    }

    @Override
    public boolean isCursorFetchEnabled() throws SQLException {
        return false;
    }

    @Override
    public boolean isReadInfoMsgEnabled() {
        return false;
    }

    @Override
    public boolean isServerTzUTC() {
        return false;
    }

    @Override
    public boolean lowerCaseTableNames() {
        return this.getActiveMySQLConnectionPassive().lowerCaseTableNames();
    }

    public void maxRowsChanged(Statement stmt) {
    }

    @Override
    public void pingInternal(boolean checkForClosedConnection, int timeoutMillis) throws SQLException {
    }

    @Override
    public void realClose(boolean calledExplicitly, boolean issueRollback, boolean skipLocalTeardown, Throwable reason) throws SQLException {
    }

    @Override
    public void recachePreparedStatement(ServerPreparedStatement pstmt) throws SQLException {
    }

    @Override
    public void registerQueryExecutionTime(long queryTimeMs) {
    }

    @Override
    public void registerStatement(Statement stmt) {
    }

    @Override
    public void reportNumberOfTablesAccessed(int numTablesAccessed) {
    }

    @Override
    public boolean serverSupportsConvertFn() throws SQLException {
        return this.getActiveMySQLConnection().serverSupportsConvertFn();
    }

    @Override
    public void setReadInfoMsgEnabled(boolean flag) {
    }

    @Override
    public void setReadOnlyInternal(boolean readOnlyFlag) throws SQLException {
    }

    @Override
    public boolean storesLowerCaseTableName() {
        return this.getActiveMySQLConnectionPassive().storesLowerCaseTableName();
    }

    @Override
    public void throwConnectionClosedException() throws SQLException {
    }

    @Override
    public void unregisterStatement(Statement stmt) {
    }

    public void unsetMaxRows(Statement stmt) throws SQLException {
    }

    @Override
    public boolean useAnsiQuotedIdentifiers() {
        return false;
    }

    public boolean useMaxRows() {
        return false;
    }

    @Override
    public void clearWarnings() {
    }

    @Override
    public Properties getClientInfo() {
        return null;
    }

    @Override
    public String getClientInfo(String name) {
        return null;
    }

    @Override
    public int getHoldability() {
        return -1;
    }

    @Override
    public int getTransactionIsolation() {
        return -1;
    }

    @Override
    public Map<String, Class<?>> getTypeMap() {
        return null;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.getActiveMySQLConnection().getWarnings();
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return this.getActiveMySQLConnection().nativeSQL(sql);
    }

    @Override
    public ProfilerEventHandler getProfilerEventHandlerInstance() {
        return null;
    }

    @Override
    public void setProfilerEventHandlerInstance(ProfilerEventHandler h) {
    }

    @Override
    public void decachePreparedStatement(ServerPreparedStatement pstmt) throws SQLException {
    }

    static {
        Class<?> clazz = null;
        try {
            if (Util.isJdbc4()) {
                clazz = Class.forName("com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException");
            }
        }
        catch (ClassNotFoundException classNotFoundException) {
            // empty catch block
        }
        JDBC4_NON_TRANSIENT_CONN_EXCEPTION = clazz;
    }
}


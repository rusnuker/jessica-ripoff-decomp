/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.LoadBalancedConnection;
import com.mysql.jdbc.MultiHostConnectionProxy;
import com.mysql.jdbc.MultiHostMySQLConnection;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.ReplicationConnection;
import com.mysql.jdbc.ReplicationConnectionProxy;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ReplicationMySQLConnection
extends MultiHostMySQLConnection
implements ReplicationConnection {
    public ReplicationMySQLConnection(MultiHostConnectionProxy proxy) {
        super(proxy);
    }

    protected ReplicationConnectionProxy getThisAsProxy() {
        return (ReplicationConnectionProxy)super.getThisAsProxy();
    }

    protected MySQLConnection getActiveMySQLConnection() {
        return (MySQLConnection)this.getCurrentConnection();
    }

    public synchronized Connection getCurrentConnection() {
        return this.getThisAsProxy().getCurrentConnection();
    }

    public long getConnectionGroupId() {
        return this.getThisAsProxy().getConnectionGroupId();
    }

    public synchronized Connection getMasterConnection() {
        return this.getThisAsProxy().getMasterConnection();
    }

    private Connection getValidatedMasterConnection() {
        LoadBalancedConnection conn = this.getThisAsProxy().masterConnection;
        try {
            return conn == null || conn.isClosed() ? null : conn;
        }
        catch (SQLException e) {
            return null;
        }
    }

    public void promoteSlaveToMaster(String host) throws SQLException {
        this.getThisAsProxy().promoteSlaveToMaster(host);
    }

    public void removeMasterHost(String host) throws SQLException {
        this.getThisAsProxy().removeMasterHost(host);
    }

    public void removeMasterHost(String host, boolean waitUntilNotInUse) throws SQLException {
        this.getThisAsProxy().removeMasterHost(host, waitUntilNotInUse);
    }

    public boolean isHostMaster(String host) {
        return this.getThisAsProxy().isHostMaster(host);
    }

    public synchronized Connection getSlavesConnection() {
        return this.getThisAsProxy().getSlavesConnection();
    }

    private Connection getValidatedSlavesConnection() {
        LoadBalancedConnection conn = this.getThisAsProxy().slavesConnection;
        try {
            return conn == null || conn.isClosed() ? null : conn;
        }
        catch (SQLException e) {
            return null;
        }
    }

    public void addSlaveHost(String host) throws SQLException {
        this.getThisAsProxy().addSlaveHost(host);
    }

    public void removeSlave(String host) throws SQLException {
        this.getThisAsProxy().removeSlave(host);
    }

    public void removeSlave(String host, boolean closeGently) throws SQLException {
        this.getThisAsProxy().removeSlave(host, closeGently);
    }

    public boolean isHostSlave(String host) {
        return this.getThisAsProxy().isHostSlave(host);
    }

    public void setReadOnly(boolean readOnlyFlag) throws SQLException {
        this.getThisAsProxy().setReadOnly(readOnlyFlag);
    }

    public boolean isReadOnly() throws SQLException {
        return this.getThisAsProxy().isReadOnly();
    }

    public synchronized void ping() throws SQLException {
        block7: {
            Connection conn;
            block6: {
                try {
                    conn = this.getValidatedMasterConnection();
                    if (conn != null) {
                        conn.ping();
                    }
                }
                catch (SQLException e) {
                    if (!this.isMasterConnection()) break block6;
                    throw e;
                }
            }
            try {
                conn = this.getValidatedSlavesConnection();
                if (conn != null) {
                    conn.ping();
                }
            }
            catch (SQLException e) {
                if (this.isMasterConnection()) break block7;
                throw e;
            }
        }
    }

    public synchronized void changeUser(String userName, String newPassword) throws SQLException {
        Connection conn = this.getValidatedMasterConnection();
        if (conn != null) {
            conn.changeUser(userName, newPassword);
        }
        if ((conn = this.getValidatedSlavesConnection()) != null) {
            conn.changeUser(userName, newPassword);
        }
    }

    public synchronized void setStatementComment(String comment) {
        Connection conn = this.getValidatedMasterConnection();
        if (conn != null) {
            conn.setStatementComment(comment);
        }
        if ((conn = this.getValidatedSlavesConnection()) != null) {
            conn.setStatementComment(comment);
        }
    }

    public boolean hasSameProperties(Connection c) {
        Connection connM = this.getValidatedMasterConnection();
        Connection connS = this.getValidatedSlavesConnection();
        if (connM == null && connS == null) {
            return false;
        }
        return !(connM != null && !connM.hasSameProperties(c) || connS != null && !connS.hasSameProperties(c));
    }

    public Properties getProperties() {
        Properties props = new Properties();
        Connection conn = this.getValidatedMasterConnection();
        if (conn != null) {
            props.putAll(conn.getProperties());
        }
        if ((conn = this.getValidatedSlavesConnection()) != null) {
            props.putAll(conn.getProperties());
        }
        return props;
    }

    public void abort(Executor executor) throws SQLException {
        this.getThisAsProxy().doAbort(executor);
    }

    public void abortInternal() throws SQLException {
        this.getThisAsProxy().doAbortInternal();
    }

    public boolean getAllowMasterDownConnections() {
        return this.getThisAsProxy().allowMasterDownConnections;
    }

    public void setAllowMasterDownConnections(boolean connectIfMasterDown) {
        this.getThisAsProxy().allowMasterDownConnections = connectIfMasterDown;
    }

    public boolean getReplicationEnableJMX() {
        return this.getThisAsProxy().enableJMX;
    }

    public void setReplicationEnableJMX(boolean replicationEnableJMX) {
        this.getThisAsProxy().enableJMX = replicationEnableJMX;
    }

    public void setProxy(MySQLConnection proxy) {
        this.getThisAsProxy().setProxy(proxy);
    }
}


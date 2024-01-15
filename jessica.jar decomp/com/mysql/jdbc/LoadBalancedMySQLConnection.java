/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.LoadBalancedConnection;
import com.mysql.jdbc.LoadBalancedConnectionProxy;
import com.mysql.jdbc.MultiHostMySQLConnection;
import java.sql.SQLException;

public class LoadBalancedMySQLConnection
extends MultiHostMySQLConnection
implements LoadBalancedConnection {
    public LoadBalancedMySQLConnection(LoadBalancedConnectionProxy proxy) {
        super(proxy);
    }

    protected LoadBalancedConnectionProxy getThisAsProxy() {
        return (LoadBalancedConnectionProxy)super.getThisAsProxy();
    }

    public void close() throws SQLException {
        this.getThisAsProxy().doClose();
    }

    public void ping() throws SQLException {
        this.ping(true);
    }

    public void ping(boolean allConnections) throws SQLException {
        if (allConnections) {
            this.getThisAsProxy().doPing();
        } else {
            this.getActiveMySQLConnection().ping();
        }
    }

    public boolean addHost(String host) throws SQLException {
        return this.getThisAsProxy().addHost(host);
    }

    public void removeHost(String host) throws SQLException {
        this.getThisAsProxy().removeHost(host);
    }

    public void removeHostWhenNotInUse(String host) throws SQLException {
        this.getThisAsProxy().removeHostWhenNotInUse(host);
    }
}


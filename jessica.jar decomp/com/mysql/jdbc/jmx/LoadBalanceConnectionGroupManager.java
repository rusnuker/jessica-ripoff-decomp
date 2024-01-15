/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jmx;

import com.mysql.jdbc.ConnectionGroupManager;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.jmx.LoadBalanceConnectionGroupManagerMBean;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class LoadBalanceConnectionGroupManager
implements LoadBalanceConnectionGroupManagerMBean {
    private boolean isJmxRegistered = false;

    public synchronized void registerJmx() throws SQLException {
        if (this.isJmxRegistered) {
            return;
        }
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName name = new ObjectName("com.mysql.jdbc.jmx:type=LoadBalanceConnectionGroupManager");
            mbs.registerMBean(this, name);
            this.isJmxRegistered = true;
        }
        catch (Exception e) {
            throw SQLError.createSQLException("Unable to register load-balance management bean with JMX", null, e, null);
        }
    }

    public void addHost(String group, String host, boolean forExisting) {
        try {
            ConnectionGroupManager.addHost(group, host, forExisting);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getActiveHostCount(String group) {
        return ConnectionGroupManager.getActiveHostCount(group);
    }

    public long getActiveLogicalConnectionCount(String group) {
        return ConnectionGroupManager.getActiveLogicalConnectionCount(group);
    }

    public long getActivePhysicalConnectionCount(String group) {
        return ConnectionGroupManager.getActivePhysicalConnectionCount(group);
    }

    public int getTotalHostCount(String group) {
        return ConnectionGroupManager.getTotalHostCount(group);
    }

    public long getTotalLogicalConnectionCount(String group) {
        return ConnectionGroupManager.getTotalLogicalConnectionCount(group);
    }

    public long getTotalPhysicalConnectionCount(String group) {
        return ConnectionGroupManager.getTotalPhysicalConnectionCount(group);
    }

    public long getTotalTransactionCount(String group) {
        return ConnectionGroupManager.getTotalTransactionCount(group);
    }

    public void removeHost(String group, String host) throws SQLException {
        ConnectionGroupManager.removeHost(group, host);
    }

    public String getActiveHostsList(String group) {
        return ConnectionGroupManager.getActiveHostLists(group);
    }

    public String getRegisteredConnectionGroups() {
        return ConnectionGroupManager.getRegisteredConnectionGroups();
    }

    public void stopNewConnectionsToHost(String group, String host) throws SQLException {
        ConnectionGroupManager.removeHost(group, host);
    }
}


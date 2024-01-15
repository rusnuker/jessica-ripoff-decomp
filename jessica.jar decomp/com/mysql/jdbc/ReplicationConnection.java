/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.MySQLConnection;
import java.sql.SQLException;

public interface ReplicationConnection
extends MySQLConnection {
    public long getConnectionGroupId();

    public Connection getCurrentConnection();

    public Connection getMasterConnection();

    public void promoteSlaveToMaster(String var1) throws SQLException;

    public void removeMasterHost(String var1) throws SQLException;

    public void removeMasterHost(String var1, boolean var2) throws SQLException;

    public boolean isHostMaster(String var1);

    public Connection getSlavesConnection();

    public void addSlaveHost(String var1) throws SQLException;

    public void removeSlave(String var1) throws SQLException;

    public void removeSlave(String var1, boolean var2) throws SQLException;

    public boolean isHostSlave(String var1);
}


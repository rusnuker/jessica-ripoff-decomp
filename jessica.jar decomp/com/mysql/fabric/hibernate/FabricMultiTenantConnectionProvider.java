/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hibernate.service.jdbc.connections.spi.MultiTenantConnectionProvider
 */
package com.mysql.fabric.hibernate;

import com.mysql.fabric.FabricCommunicationException;
import com.mysql.fabric.FabricConnection;
import com.mysql.fabric.Server;
import com.mysql.fabric.ServerGroup;
import com.mysql.fabric.ServerMode;
import com.mysql.fabric.ShardMapping;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.hibernate.service.jdbc.connections.spi.MultiTenantConnectionProvider;

public class FabricMultiTenantConnectionProvider
implements MultiTenantConnectionProvider {
    private static final long serialVersionUID = 1L;
    private FabricConnection fabricConnection;
    private String database;
    private String table;
    private String user;
    private String password;
    private ShardMapping shardMapping;
    private ServerGroup globalGroup;

    public FabricMultiTenantConnectionProvider(String fabricUrl, String database, String table, String user, String password, String fabricUser, String fabricPassword) {
        try {
            this.fabricConnection = new FabricConnection(fabricUrl, fabricUser, fabricPassword);
            this.database = database;
            this.table = table;
            this.user = user;
            this.password = password;
            this.shardMapping = this.fabricConnection.getShardMapping(this.database, this.table);
            this.globalGroup = this.fabricConnection.getServerGroup(this.shardMapping.getGlobalGroupName());
        }
        catch (FabricCommunicationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Connection getReadWriteConnectionFromServerGroup(ServerGroup serverGroup) throws SQLException {
        for (Server s : serverGroup.getServers()) {
            if (!ServerMode.READ_WRITE.equals((Object)s.getMode())) continue;
            String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s", s.getHostname(), s.getPort(), this.database);
            return DriverManager.getConnection(jdbcUrl, this.user, this.password);
        }
        throw new SQLException("Unable to find r/w server for chosen shard mapping in group " + serverGroup.getName());
    }

    public Connection getAnyConnection() throws SQLException {
        return this.getReadWriteConnectionFromServerGroup(this.globalGroup);
    }

    public Connection getConnection(String tenantIdentifier) throws SQLException {
        String serverGroupName = this.shardMapping.getGroupNameForKey(tenantIdentifier);
        ServerGroup serverGroup = this.fabricConnection.getServerGroup(serverGroupName);
        return this.getReadWriteConnectionFromServerGroup(serverGroup);
    }

    public void releaseAnyConnection(Connection connection) throws SQLException {
        try {
            connection.close();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        this.releaseAnyConnection(connection);
    }

    public boolean supportsAggressiveRelease() {
        return false;
    }

    public boolean isUnwrappableAs(Class unwrapType) {
        return false;
    }

    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}


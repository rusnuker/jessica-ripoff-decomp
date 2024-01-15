/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric;

import com.mysql.fabric.FabricCommunicationException;
import com.mysql.fabric.FabricStateResponse;
import com.mysql.fabric.ServerGroup;
import com.mysql.fabric.ShardMapping;
import com.mysql.fabric.ShardTable;
import com.mysql.fabric.proto.xmlrpc.XmlRpcClient;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class FabricConnection {
    private XmlRpcClient client;
    private Map<String, ShardMapping> shardMappingsByTableName = new HashMap<String, ShardMapping>();
    private Map<String, ServerGroup> serverGroupsByName = new HashMap<String, ServerGroup>();
    private long shardMappingsExpiration;
    private int shardMappingsTtl;
    private long serverGroupsExpiration;
    private int serverGroupsTtl;

    public FabricConnection(String url, String username, String password) throws FabricCommunicationException {
        this.client = new XmlRpcClient(url, username, password);
        this.refreshState();
    }

    public FabricConnection(Set<String> urls, String username, String password) throws FabricCommunicationException {
        throw new UnsupportedOperationException("Multiple connections not supported.");
    }

    public String getInstanceUuid() {
        return null;
    }

    public int getVersion() {
        return 0;
    }

    public int refreshState() throws FabricCommunicationException {
        FabricStateResponse<Set<ServerGroup>> serverGroups = this.client.getServerGroups();
        FabricStateResponse<Set<ShardMapping>> shardMappings = this.client.getShardMappings();
        this.serverGroupsExpiration = serverGroups.getExpireTimeMillis();
        this.serverGroupsTtl = serverGroups.getTtl();
        for (ServerGroup g : serverGroups.getData()) {
            this.serverGroupsByName.put(g.getName(), g);
        }
        this.shardMappingsExpiration = shardMappings.getExpireTimeMillis();
        this.shardMappingsTtl = shardMappings.getTtl();
        for (ShardMapping m : shardMappings.getData()) {
            for (ShardTable t : m.getShardTables()) {
                this.shardMappingsByTableName.put(t.getDatabase() + "." + t.getTable(), m);
            }
        }
        return 0;
    }

    public int refreshStatePassive() {
        try {
            return this.refreshState();
        }
        catch (FabricCommunicationException e) {
            this.serverGroupsExpiration = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(this.serverGroupsTtl);
            this.shardMappingsExpiration = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(this.shardMappingsTtl);
            return 0;
        }
    }

    public ServerGroup getServerGroup(String serverGroupName) {
        if (this.isStateExpired()) {
            this.refreshStatePassive();
        }
        return this.serverGroupsByName.get(serverGroupName);
    }

    public ShardMapping getShardMapping(String database, String table) {
        if (this.isStateExpired()) {
            this.refreshStatePassive();
        }
        return this.shardMappingsByTableName.get(database + "." + table);
    }

    public boolean isStateExpired() {
        return System.currentTimeMillis() > this.shardMappingsExpiration || System.currentTimeMillis() > this.serverGroupsExpiration;
    }

    public Set<String> getFabricHosts() {
        return null;
    }

    public XmlRpcClient getClient() {
        return this.client;
    }
}


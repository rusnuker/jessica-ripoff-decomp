/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric;

import com.mysql.fabric.Server;
import com.mysql.fabric.ServerRole;
import java.util.Set;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ServerGroup {
    private String name;
    private Set<Server> servers;

    public ServerGroup(String name, Set<Server> servers) {
        this.name = name;
        this.servers = servers;
    }

    public String getName() {
        return this.name;
    }

    public Set<Server> getServers() {
        return this.servers;
    }

    public Server getMaster() {
        for (Server s : this.servers) {
            if (s.getRole() != ServerRole.PRIMARY) continue;
            return s;
        }
        return null;
    }

    public Server getServer(String hostPortString) {
        for (Server s : this.servers) {
            if (!s.getHostPortString().equals(hostPortString)) continue;
            return s;
        }
        return null;
    }

    public String toString() {
        return String.format("Group[name=%s, servers=%s]", this.name, this.servers);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric;

import com.mysql.fabric.ServerMode;
import com.mysql.fabric.ServerRole;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Server
implements Comparable<Server> {
    private String groupName;
    private String uuid;
    private String hostname;
    private int port;
    private ServerMode mode;
    private ServerRole role;
    private double weight;

    public Server(String groupName, String uuid, String hostname, int port, ServerMode mode, ServerRole role, double weight) {
        this.groupName = groupName;
        this.uuid = uuid;
        this.hostname = hostname;
        this.port = port;
        this.mode = mode;
        this.role = role;
        this.weight = weight;
        assert (uuid != null && !"".equals(uuid));
        assert (hostname != null && !"".equals(hostname));
        assert (port > 0);
        assert (mode != null);
        assert (role != null);
        assert (weight > 0.0);
    }

    public String getGroupName() {
        return this.groupName;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getHostname() {
        return this.hostname;
    }

    public int getPort() {
        return this.port;
    }

    public ServerMode getMode() {
        return this.mode;
    }

    public ServerRole getRole() {
        return this.role;
    }

    public double getWeight() {
        return this.weight;
    }

    public String getHostPortString() {
        return this.hostname + ":" + this.port;
    }

    public boolean isMaster() {
        return this.role == ServerRole.PRIMARY;
    }

    public boolean isSlave() {
        return this.role == ServerRole.SECONDARY || this.role == ServerRole.SPARE;
    }

    public String toString() {
        return String.format("Server[%s, %s:%d, %s, %s, weight=%s]", new Object[]{this.uuid, this.hostname, this.port, this.mode, this.role, this.weight});
    }

    public boolean equals(Object o) {
        if (!(o instanceof Server)) {
            return false;
        }
        Server s = (Server)o;
        return s.getUuid().equals(this.getUuid());
    }

    public int hashCode() {
        return this.getUuid().hashCode();
    }

    @Override
    public int compareTo(Server other) {
        return this.getUuid().compareTo(other.getUuid());
    }
}


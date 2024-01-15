/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.jdbc;

import com.mysql.fabric.ServerGroup;
import com.mysql.jdbc.MySQLConnection;
import java.sql.SQLException;
import java.util.Set;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface FabricMySQLConnection
extends MySQLConnection {
    public void clearServerSelectionCriteria() throws SQLException;

    public void setShardKey(String var1) throws SQLException;

    public String getShardKey();

    public void setShardTable(String var1) throws SQLException;

    public String getShardTable();

    public void setServerGroupName(String var1) throws SQLException;

    public String getServerGroupName();

    public ServerGroup getCurrentServerGroup();

    public void clearQueryTables() throws SQLException;

    public void addQueryTable(String var1) throws SQLException;

    public Set<String> getQueryTables();
}


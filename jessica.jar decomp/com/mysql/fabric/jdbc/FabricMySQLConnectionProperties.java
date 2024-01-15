/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.jdbc;

import com.mysql.jdbc.ConnectionProperties;

public interface FabricMySQLConnectionProperties
extends ConnectionProperties {
    public void setFabricShardKey(String var1);

    public String getFabricShardKey();

    public void setFabricShardTable(String var1);

    public String getFabricShardTable();

    public void setFabricServerGroup(String var1);

    public String getFabricServerGroup();

    public void setFabricProtocol(String var1);

    public String getFabricProtocol();

    public void setFabricUsername(String var1);

    public String getFabricUsername();

    public void setFabricPassword(String var1);

    public String getFabricPassword();

    public void setFabricReportErrors(boolean var1);

    public boolean getFabricReportErrors();
}


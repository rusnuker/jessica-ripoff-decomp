/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.jdbc;

import com.mysql.fabric.jdbc.FabricMySQLConnectionProperties;
import com.mysql.fabric.jdbc.FabricMySQLDriver;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class FabricMySQLDataSource
extends MysqlDataSource
implements FabricMySQLConnectionProperties {
    private static final long serialVersionUID = 1L;
    private static final Driver driver;
    private String fabricShardKey;
    private String fabricShardTable;
    private String fabricServerGroup;
    private String fabricProtocol = "http";
    private String fabricUsername;
    private String fabricPassword;
    private boolean fabricReportErrors = false;

    protected Connection getConnection(Properties props) throws SQLException {
        String jdbcUrlToUse = null;
        if (!this.explicitUrl) {
            StringBuilder jdbcUrl = new StringBuilder("jdbc:mysql:fabric://");
            if (this.hostName != null) {
                jdbcUrl.append(this.hostName);
            }
            jdbcUrl.append(":");
            jdbcUrl.append(this.port);
            jdbcUrl.append("/");
            if (this.databaseName != null) {
                jdbcUrl.append(this.databaseName);
            }
            jdbcUrlToUse = jdbcUrl.toString();
        } else {
            jdbcUrlToUse = this.url;
        }
        Properties urlProps = ((FabricMySQLDriver)driver).parseFabricURL(jdbcUrlToUse, null);
        urlProps.remove("DBNAME");
        urlProps.remove("HOST");
        urlProps.remove("PORT");
        for (String key : urlProps.keySet()) {
            props.setProperty(key, urlProps.getProperty(key));
        }
        if (this.fabricShardKey != null) {
            props.setProperty("fabricShardKey", this.fabricShardKey);
        }
        if (this.fabricShardTable != null) {
            props.setProperty("fabricShardTable", this.fabricShardTable);
        }
        if (this.fabricServerGroup != null) {
            props.setProperty("fabricServerGroup", this.fabricServerGroup);
        }
        props.setProperty("fabricProtocol", this.fabricProtocol);
        if (this.fabricUsername != null) {
            props.setProperty("fabricUsername", this.fabricUsername);
        }
        if (this.fabricPassword != null) {
            props.setProperty("fabricPassword", this.fabricPassword);
        }
        props.setProperty("fabricReportErrors", Boolean.toString(this.fabricReportErrors));
        return driver.connect(jdbcUrlToUse, props);
    }

    public void setFabricShardKey(String value) {
        this.fabricShardKey = value;
    }

    public String getFabricShardKey() {
        return this.fabricShardKey;
    }

    public void setFabricShardTable(String value) {
        this.fabricShardTable = value;
    }

    public String getFabricShardTable() {
        return this.fabricShardTable;
    }

    public void setFabricServerGroup(String value) {
        this.fabricServerGroup = value;
    }

    public String getFabricServerGroup() {
        return this.fabricServerGroup;
    }

    public void setFabricProtocol(String value) {
        this.fabricProtocol = value;
    }

    public String getFabricProtocol() {
        return this.fabricProtocol;
    }

    public void setFabricUsername(String value) {
        this.fabricUsername = value;
    }

    public String getFabricUsername() {
        return this.fabricUsername;
    }

    public void setFabricPassword(String value) {
        this.fabricPassword = value;
    }

    public String getFabricPassword() {
        return this.fabricPassword;
    }

    public void setFabricReportErrors(boolean value) {
        this.fabricReportErrors = value;
    }

    public boolean getFabricReportErrors() {
        return this.fabricReportErrors;
    }

    static {
        try {
            driver = new FabricMySQLDriver();
        }
        catch (Exception ex) {
            throw new RuntimeException("Can create driver", ex);
        }
    }
}


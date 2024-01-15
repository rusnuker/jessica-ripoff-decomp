/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.jdbc;

import com.mysql.fabric.FabricConnection;
import com.mysql.fabric.jdbc.FabricMySQLConnectionProperties;
import com.mysql.fabric.jdbc.FabricMySQLConnectionProxy;
import com.mysql.fabric.jdbc.JDBC4FabricMySQLConnection;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.JDBC4ClientInfoProvider;
import com.mysql.jdbc.JDBC4MySQLConnection;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Struct;
import java.util.Properties;

public class JDBC4FabricMySQLConnectionProxy
extends FabricMySQLConnectionProxy
implements JDBC4FabricMySQLConnection,
FabricMySQLConnectionProperties {
    private static final long serialVersionUID = 5845485979107347258L;
    private FabricConnection fabricConnection;

    public JDBC4FabricMySQLConnectionProxy(Properties props) throws SQLException {
        super(props);
    }

    @Override
    public Blob createBlob() {
        try {
            this.transactionBegun();
            return this.getActiveConnection().createBlob();
        }
        catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Clob createClob() {
        try {
            this.transactionBegun();
            return this.getActiveConnection().createClob();
        }
        catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public NClob createNClob() {
        try {
            this.transactionBegun();
            return this.getActiveConnection().createNClob();
        }
        catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().createSQLXML();
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        for (Connection c : this.serverConnections.values()) {
            c.setClientInfo(properties);
        }
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        for (Connection c : this.serverConnections.values()) {
            c.setClientInfo(name, value);
        }
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return this.getActiveConnection().createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        this.transactionBegun();
        return this.getActiveConnection().createStruct(typeName, attributes);
    }

    @Override
    public JDBC4ClientInfoProvider getClientInfoProviderImpl() throws SQLException {
        return ((JDBC4MySQLConnection)this.getActiveConnection()).getClientInfoProviderImpl();
    }
}


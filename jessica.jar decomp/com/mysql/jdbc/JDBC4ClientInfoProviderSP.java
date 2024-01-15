/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.JDBC4ClientInfoProvider;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

public class JDBC4ClientInfoProviderSP
implements JDBC4ClientInfoProvider {
    PreparedStatement setClientInfoSp;
    PreparedStatement getClientInfoSp;
    PreparedStatement getClientInfoBulkSp;

    @Override
    public synchronized void initialize(java.sql.Connection conn, Properties configurationProps) throws SQLException {
        String identifierQuote = conn.getMetaData().getIdentifierQuoteString();
        String setClientInfoSpName = configurationProps.getProperty("clientInfoSetSPName", "setClientInfo");
        String getClientInfoSpName = configurationProps.getProperty("clientInfoGetSPName", "getClientInfo");
        String getClientInfoBulkSpName = configurationProps.getProperty("clientInfoGetBulkSPName", "getClientInfoBulk");
        String clientInfoCatalog = configurationProps.getProperty("clientInfoCatalog", "");
        String catalog = "".equals(clientInfoCatalog) ? conn.getCatalog() : clientInfoCatalog;
        this.setClientInfoSp = ((Connection)conn).clientPrepareStatement("CALL " + identifierQuote + catalog + identifierQuote + "." + identifierQuote + setClientInfoSpName + identifierQuote + "(?, ?)");
        this.getClientInfoSp = ((Connection)conn).clientPrepareStatement("CALL" + identifierQuote + catalog + identifierQuote + "." + identifierQuote + getClientInfoSpName + identifierQuote + "(?)");
        this.getClientInfoBulkSp = ((Connection)conn).clientPrepareStatement("CALL " + identifierQuote + catalog + identifierQuote + "." + identifierQuote + getClientInfoBulkSpName + identifierQuote + "()");
    }

    @Override
    public synchronized void destroy() throws SQLException {
        if (this.setClientInfoSp != null) {
            this.setClientInfoSp.close();
            this.setClientInfoSp = null;
        }
        if (this.getClientInfoSp != null) {
            this.getClientInfoSp.close();
            this.getClientInfoSp = null;
        }
        if (this.getClientInfoBulkSp != null) {
            this.getClientInfoBulkSp.close();
            this.getClientInfoBulkSp = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized Properties getClientInfo(java.sql.Connection conn) throws SQLException {
        ResultSet rs = null;
        Properties props = new Properties();
        try {
            this.getClientInfoBulkSp.execute();
            rs = this.getClientInfoBulkSp.getResultSet();
            while (rs.next()) {
                props.setProperty(rs.getString(1), rs.getString(2));
            }
        }
        finally {
            if (rs != null) {
                rs.close();
            }
        }
        return props;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized String getClientInfo(java.sql.Connection conn, String name) throws SQLException {
        ResultSet rs = null;
        String clientInfo = null;
        try {
            this.getClientInfoSp.setString(1, name);
            this.getClientInfoSp.execute();
            rs = this.getClientInfoSp.getResultSet();
            if (rs.next()) {
                clientInfo = rs.getString(1);
            }
        }
        finally {
            if (rs != null) {
                rs.close();
            }
        }
        return clientInfo;
    }

    @Override
    public synchronized void setClientInfo(java.sql.Connection conn, Properties properties) throws SQLClientInfoException {
        try {
            Enumeration<?> propNames = properties.propertyNames();
            while (propNames.hasMoreElements()) {
                String name = (String)propNames.nextElement();
                String value = properties.getProperty(name);
                this.setClientInfo(conn, name, value);
            }
        }
        catch (SQLException sqlEx) {
            SQLClientInfoException clientInfoEx = new SQLClientInfoException();
            clientInfoEx.initCause(sqlEx);
            throw clientInfoEx;
        }
    }

    @Override
    public synchronized void setClientInfo(java.sql.Connection conn, String name, String value) throws SQLClientInfoException {
        try {
            this.setClientInfoSp.setString(1, name);
            this.setClientInfoSp.setString(2, value);
            this.setClientInfoSp.execute();
        }
        catch (SQLException sqlEx) {
            SQLClientInfoException clientInfoEx = new SQLClientInfoException();
            clientInfoEx.initCause(sqlEx);
            throw clientInfoEx;
        }
    }
}


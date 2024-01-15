/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.authentication;

import com.mysql.jdbc.AuthenticationPlugin;
import com.mysql.jdbc.Buffer;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.StringUtils;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class MysqlClearPasswordPlugin
implements AuthenticationPlugin {
    private Connection connection;
    private String password = null;

    @Override
    public void init(Connection conn, Properties props) throws SQLException {
        this.connection = conn;
    }

    @Override
    public void destroy() {
        this.password = null;
    }

    @Override
    public String getProtocolPluginName() {
        return "mysql_clear_password";
    }

    @Override
    public boolean requiresConfidentiality() {
        return true;
    }

    @Override
    public boolean isReusable() {
        return true;
    }

    @Override
    public void setAuthenticationParameters(String user, String password) {
        this.password = password;
    }

    @Override
    public boolean nextAuthenticationStep(Buffer fromServer, List<Buffer> toServer) throws SQLException {
        Buffer bresp;
        toServer.clear();
        try {
            String encoding = this.connection.versionMeetsMinimum(5, 7, 6) ? this.connection.getPasswordCharacterEncoding() : "UTF-8";
            bresp = new Buffer(StringUtils.getBytes(this.password != null ? this.password : "", encoding));
        }
        catch (UnsupportedEncodingException e) {
            throw SQLError.createSQLException(Messages.getString("MysqlClearPasswordPlugin.1", new Object[]{this.connection.getPasswordCharacterEncoding()}), "S1000", null);
        }
        bresp.setPosition(bresp.getBufLength());
        int oldBufLength = bresp.getBufLength();
        bresp.writeByte((byte)0);
        bresp.setBufLength(oldBufLength + 1);
        bresp.setPosition(0);
        toServer.add(bresp);
        return true;
    }
}


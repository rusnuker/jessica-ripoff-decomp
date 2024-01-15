/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.authentication;

import com.mysql.jdbc.AuthenticationPlugin;
import com.mysql.jdbc.Buffer;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ExportControlled;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.Security;
import com.mysql.jdbc.StringUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Sha256PasswordPlugin
implements AuthenticationPlugin {
    public static String PLUGIN_NAME = "sha256_password";
    private Connection connection;
    private String password = null;
    private String seed = null;
    private boolean publicKeyRequested = false;
    private String publicKeyString = null;

    @Override
    public void init(Connection conn, Properties props) throws SQLException {
        this.connection = conn;
        String pkURL = this.connection.getServerRSAPublicKeyFile();
        if (pkURL != null) {
            this.publicKeyString = Sha256PasswordPlugin.readRSAKey(this.connection, pkURL);
        }
    }

    @Override
    public void destroy() {
        this.password = null;
        this.seed = null;
        this.publicKeyRequested = false;
    }

    @Override
    public String getProtocolPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public boolean requiresConfidentiality() {
        return false;
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
        toServer.clear();
        if (this.password == null || this.password.length() == 0 || fromServer == null) {
            Buffer bresp = new Buffer(new byte[]{0});
            toServer.add(bresp);
        } else if (((MySQLConnection)this.connection).getIO().isSSLEstablished()) {
            Buffer bresp;
            try {
                bresp = new Buffer(StringUtils.getBytes(this.password, this.connection.getPasswordCharacterEncoding()));
            }
            catch (UnsupportedEncodingException e) {
                throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.3", new Object[]{this.connection.getPasswordCharacterEncoding()}), "S1000", null);
            }
            bresp.setPosition(bresp.getBufLength());
            int oldBufLength = bresp.getBufLength();
            bresp.writeByte((byte)0);
            bresp.setBufLength(oldBufLength + 1);
            bresp.setPosition(0);
            toServer.add(bresp);
        } else if (this.connection.getServerRSAPublicKeyFile() != null) {
            this.seed = fromServer.readString();
            Buffer bresp = new Buffer(Sha256PasswordPlugin.encryptPassword(this.password, this.seed, this.connection, this.publicKeyString));
            toServer.add(bresp);
        } else {
            if (!this.connection.getAllowPublicKeyRetrieval()) {
                throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.2"), "08001", this.connection.getExceptionInterceptor());
            }
            if (this.publicKeyRequested && fromServer.getBufLength() > 20) {
                Buffer bresp = new Buffer(Sha256PasswordPlugin.encryptPassword(this.password, this.seed, this.connection, fromServer.readString()));
                toServer.add(bresp);
                this.publicKeyRequested = false;
            } else {
                this.seed = fromServer.readString();
                Buffer bresp = new Buffer(new byte[]{1});
                toServer.add(bresp);
                this.publicKeyRequested = true;
            }
        }
        return true;
    }

    private static byte[] encryptPassword(String password, String seed, Connection connection, String key) throws SQLException {
        byte[] input = null;
        try {
            byte[] byArray;
            if (password != null) {
                byArray = StringUtils.getBytesNullTerminated(password, connection.getPasswordCharacterEncoding());
            } else {
                byte[] byArray2 = new byte[1];
                byArray = byArray2;
                byArray2[0] = 0;
            }
            input = byArray;
        }
        catch (UnsupportedEncodingException e) {
            throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.3", new Object[]{connection.getPasswordCharacterEncoding()}), "S1000", null);
        }
        byte[] mysqlScrambleBuff = new byte[input.length];
        Security.xorString(input, mysqlScrambleBuff, seed.getBytes(), input.length);
        return ExportControlled.encryptWithRSAPublicKey(mysqlScrambleBuff, ExportControlled.decodeRSAPublicKey(key, ((MySQLConnection)connection).getExceptionInterceptor()), ((MySQLConnection)connection).getExceptionInterceptor());
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static String readRSAKey(Connection connection, String pkPath) throws SQLException {
        String res = null;
        byte[] fileBuf = new byte[2048];
        BufferedInputStream fileIn = null;
        try {
            try {
                File f = new File(pkPath);
                String canonicalPath = f.getCanonicalPath();
                fileIn = new BufferedInputStream(new FileInputStream(canonicalPath));
                int bytesRead = 0;
                StringBuilder sb = new StringBuilder();
                while ((bytesRead = fileIn.read(fileBuf)) != -1) {
                    sb.append(StringUtils.toAsciiString(fileBuf, 0, bytesRead));
                }
                res = sb.toString();
            }
            catch (IOException ioEx) {
                if (connection.getParanoid()) {
                    throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.0", new Object[]{""}), "S1009", connection.getExceptionInterceptor());
                }
                throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.0", new Object[]{"'" + pkPath + "'"}), "S1009", ioEx, connection.getExceptionInterceptor());
            }
            Object var10_10 = null;
            if (fileIn == null) return res;
        }
        catch (Throwable throwable) {
            Object var10_11 = null;
            if (fileIn == null) throw throwable;
            try {
                fileIn.close();
                throw throwable;
            }
            catch (Exception ex) {
                SQLException sqlEx = SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.1"), "S1000", ex, connection.getExceptionInterceptor());
                throw sqlEx;
            }
        }
        try {}
        catch (Exception ex) {
            SQLException sqlEx = SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.1"), "S1000", ex, connection.getExceptionInterceptor());
            throw sqlEx;
        }
        fileIn.close();
        return res;
    }
}


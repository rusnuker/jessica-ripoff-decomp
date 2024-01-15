/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Blob;
import com.mysql.jdbc.Clob;
import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.JDBC4ClientInfoProvider;
import com.mysql.jdbc.JDBC4MySQLConnection;
import com.mysql.jdbc.JDBC4MysqlSQLXML;
import com.mysql.jdbc.JDBC4NClob;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.Util;
import java.sql.Array;
import java.sql.NClob;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Struct;
import java.util.Properties;

public class JDBC4Connection
extends ConnectionImpl
implements JDBC4MySQLConnection {
    private static final long serialVersionUID = 2877471301981509475L;
    private JDBC4ClientInfoProvider infoProvider;

    public JDBC4Connection(String hostToConnectTo, int portToConnectTo, Properties info, String databaseToConnectTo, String url) throws SQLException {
        super(hostToConnectTo, portToConnectTo, info, databaseToConnectTo, url);
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return new JDBC4MysqlSQLXML(this.getExceptionInterceptor());
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return this.getClientInfoProviderImpl().getClientInfo(this);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return this.getClientInfoProviderImpl().getClientInfo(this, name);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isValid(int timeout) throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            if (this.isClosed()) {
                return false;
            }
            try {
                try {
                    this.pingInternal(false, timeout * 1000);
                }
                catch (Throwable t) {
                    try {
                        this.abortInternal();
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                    return false;
                }
            }
            catch (Throwable t) {
                return false;
            }
            return true;
        }
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        try {
            this.getClientInfoProviderImpl().setClientInfo(this, properties);
        }
        catch (SQLClientInfoException ciEx) {
            throw ciEx;
        }
        catch (SQLException sqlEx) {
            SQLClientInfoException clientInfoEx = new SQLClientInfoException();
            clientInfoEx.initCause(sqlEx);
            throw clientInfoEx;
        }
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        try {
            this.getClientInfoProviderImpl().setClientInfo(this, name, value);
        }
        catch (SQLClientInfoException ciEx) {
            throw ciEx;
        }
        catch (SQLException sqlEx) {
            SQLClientInfoException clientInfoEx = new SQLClientInfoException();
            clientInfoEx.initCause(sqlEx);
            throw clientInfoEx;
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        this.checkClosed();
        return iface.isInstance(this);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        try {
            return iface.cast(this);
        }
        catch (ClassCastException cce) {
            throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", this.getExceptionInterceptor());
        }
    }

    @Override
    public java.sql.Blob createBlob() {
        return new Blob(this.getExceptionInterceptor());
    }

    @Override
    public java.sql.Clob createClob() {
        return new Clob(this.getExceptionInterceptor());
    }

    @Override
    public NClob createNClob() {
        return new JDBC4NClob(this.getExceptionInterceptor());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public JDBC4ClientInfoProvider getClientInfoProviderImpl() throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            if (this.infoProvider == null) {
                try {
                    try {
                        this.infoProvider = (JDBC4ClientInfoProvider)Util.getInstance(this.getClientInfoProvider(), new Class[0], new Object[0], this.getExceptionInterceptor());
                    }
                    catch (SQLException sqlEx) {
                        if (sqlEx.getCause() instanceof ClassCastException) {
                            this.infoProvider = (JDBC4ClientInfoProvider)Util.getInstance("com.mysql.jdbc." + this.getClientInfoProvider(), new Class[0], new Object[0], this.getExceptionInterceptor());
                        }
                    }
                }
                catch (ClassCastException cce) {
                    throw SQLError.createSQLException(Messages.getString("JDBC4Connection.ClientInfoNotImplemented", new Object[]{this.getClientInfoProvider()}), "S1009", this.getExceptionInterceptor());
                }
                this.infoProvider.initialize(this, this.props);
            }
            return this.infoProvider;
        }
    }
}


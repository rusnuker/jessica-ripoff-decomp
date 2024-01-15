/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.jdbc2.optional.MysqlPooledConnection;
import com.mysql.jdbc.jdbc2.optional.MysqlXAConnection;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class SuspendableXAConnection
extends MysqlPooledConnection
implements XAConnection,
XAResource {
    private static final Constructor<?> JDBC_4_XA_CONNECTION_WRAPPER_CTOR;
    private static final Map<Xid, XAConnection> XIDS_TO_PHYSICAL_CONNECTIONS;
    private Xid currentXid;
    private XAConnection currentXAConnection;
    private XAResource currentXAResource;
    private Connection underlyingConnection;

    protected static SuspendableXAConnection getInstance(Connection mysqlConnection) throws SQLException {
        if (!Util.isJdbc4()) {
            return new SuspendableXAConnection(mysqlConnection);
        }
        return (SuspendableXAConnection)Util.handleNewInstance(JDBC_4_XA_CONNECTION_WRAPPER_CTOR, new Object[]{mysqlConnection}, mysqlConnection.getExceptionInterceptor());
    }

    public SuspendableXAConnection(Connection connection) {
        super(connection);
        this.underlyingConnection = connection;
    }

    private static synchronized XAConnection findConnectionForXid(Connection connectionToWrap, Xid xid) throws SQLException {
        XAConnection conn = XIDS_TO_PHYSICAL_CONNECTIONS.get(xid);
        if (conn == null) {
            conn = new MysqlXAConnection(connectionToWrap, connectionToWrap.getLogXaCommands());
            XIDS_TO_PHYSICAL_CONNECTIONS.put(xid, conn);
        }
        return conn;
    }

    private static synchronized void removeXAConnectionMapping(Xid xid) {
        XIDS_TO_PHYSICAL_CONNECTIONS.remove(xid);
    }

    private synchronized void switchToXid(Xid xid) throws XAException {
        if (xid == null) {
            throw new XAException();
        }
        try {
            if (!xid.equals(this.currentXid)) {
                XAConnection toSwitchTo;
                this.currentXAConnection = toSwitchTo = SuspendableXAConnection.findConnectionForXid(this.underlyingConnection, xid);
                this.currentXid = xid;
                this.currentXAResource = toSwitchTo.getXAResource();
            }
        }
        catch (SQLException sqlEx) {
            throw new XAException();
        }
    }

    public XAResource getXAResource() throws SQLException {
        return this;
    }

    public void commit(Xid xid, boolean arg1) throws XAException {
        this.switchToXid(xid);
        this.currentXAResource.commit(xid, arg1);
        SuspendableXAConnection.removeXAConnectionMapping(xid);
    }

    public void end(Xid xid, int arg1) throws XAException {
        this.switchToXid(xid);
        this.currentXAResource.end(xid, arg1);
    }

    public void forget(Xid xid) throws XAException {
        this.switchToXid(xid);
        this.currentXAResource.forget(xid);
        SuspendableXAConnection.removeXAConnectionMapping(xid);
    }

    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    public boolean isSameRM(XAResource xaRes) throws XAException {
        return xaRes == this;
    }

    public int prepare(Xid xid) throws XAException {
        this.switchToXid(xid);
        return this.currentXAResource.prepare(xid);
    }

    public Xid[] recover(int flag) throws XAException {
        return MysqlXAConnection.recover(this.underlyingConnection, flag);
    }

    public void rollback(Xid xid) throws XAException {
        this.switchToXid(xid);
        this.currentXAResource.rollback(xid);
        SuspendableXAConnection.removeXAConnectionMapping(xid);
    }

    public boolean setTransactionTimeout(int arg0) throws XAException {
        return false;
    }

    public void start(Xid xid, int arg1) throws XAException {
        this.switchToXid(xid);
        if (arg1 != 0x200000) {
            this.currentXAResource.start(xid, arg1);
            return;
        }
        this.currentXAResource.start(xid, 0x8000000);
    }

    public synchronized java.sql.Connection getConnection() throws SQLException {
        if (this.currentXAConnection == null) {
            return this.getConnection(false, true);
        }
        return this.currentXAConnection.getConnection();
    }

    public void close() throws SQLException {
        if (this.currentXAConnection == null) {
            super.close();
        } else {
            SuspendableXAConnection.removeXAConnectionMapping(this.currentXid);
            this.currentXAConnection.close();
        }
    }

    static {
        if (Util.isJdbc4()) {
            try {
                JDBC_4_XA_CONNECTION_WRAPPER_CTOR = Class.forName("com.mysql.jdbc.jdbc2.optional.JDBC4SuspendableXAConnection").getConstructor(Connection.class);
            }
            catch (SecurityException e) {
                throw new RuntimeException(e);
            }
            catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            JDBC_4_XA_CONNECTION_WRAPPER_CTOR = null;
        }
        XIDS_TO_PHYSICAL_CONNECTIONS = new HashMap<Xid, XAConnection>();
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.StringUtils;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.jdbc2.optional.MysqlPooledConnection;
import com.mysql.jdbc.jdbc2.optional.MysqlXAException;
import com.mysql.jdbc.jdbc2.optional.MysqlXid;
import com.mysql.jdbc.log.Log;
import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class MysqlXAConnection
extends MysqlPooledConnection
implements XAConnection,
XAResource {
    private static final int MAX_COMMAND_LENGTH = 300;
    private Connection underlyingConnection;
    private static final Map<Integer, Integer> MYSQL_ERROR_CODES_TO_XA_ERROR_CODES;
    private Log log;
    protected boolean logXaCommands;
    private static final Constructor<?> JDBC_4_XA_CONNECTION_WRAPPER_CTOR;

    protected static MysqlXAConnection getInstance(Connection mysqlConnection, boolean logXaCommands) throws SQLException {
        if (!Util.isJdbc4()) {
            return new MysqlXAConnection(mysqlConnection, logXaCommands);
        }
        return (MysqlXAConnection)Util.handleNewInstance(JDBC_4_XA_CONNECTION_WRAPPER_CTOR, new Object[]{mysqlConnection, logXaCommands}, mysqlConnection.getExceptionInterceptor());
    }

    public MysqlXAConnection(Connection connection, boolean logXaCommands) throws SQLException {
        super(connection);
        this.underlyingConnection = connection;
        this.log = connection.getLog();
        this.logXaCommands = logXaCommands;
    }

    public XAResource getXAResource() throws SQLException {
        return this;
    }

    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    public boolean setTransactionTimeout(int arg0) throws XAException {
        return false;
    }

    public boolean isSameRM(XAResource xares) throws XAException {
        if (xares instanceof MysqlXAConnection) {
            return this.underlyingConnection.isSameResource(((MysqlXAConnection)xares).underlyingConnection);
        }
        return false;
    }

    public Xid[] recover(int flag) throws XAException {
        return MysqlXAConnection.recover(this.underlyingConnection, flag);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected static Xid[] recover(java.sql.Connection c, int flag) throws XAException {
        ArrayList<MysqlXid> recoveredXidList;
        Statement stmt;
        block17: {
            boolean endRscan;
            boolean startRscan = (flag & 0x1000000) > 0;
            boolean bl = endRscan = (flag & 0x800000) > 0;
            if (!startRscan && !endRscan && flag != 0) {
                throw new MysqlXAException(-5, Messages.getString("MysqlXAConnection.001"), null);
            }
            if (!startRscan) {
                return new Xid[0];
            }
            ResultSet rs = null;
            stmt = null;
            recoveredXidList = new ArrayList<MysqlXid>();
            try {
                try {
                    stmt = c.createStatement();
                    rs = stmt.executeQuery("XA RECOVER");
                    while (rs.next()) {
                        int formatId = rs.getInt(1);
                        int gtridLength = rs.getInt(2);
                        int bqualLength = rs.getInt(3);
                        byte[] gtridAndBqual = rs.getBytes(4);
                        byte[] gtrid = new byte[gtridLength];
                        byte[] bqual = new byte[bqualLength];
                        if (gtridAndBqual.length != gtridLength + bqualLength) {
                            throw new MysqlXAException(105, Messages.getString("MysqlXAConnection.002"), null);
                        }
                        System.arraycopy(gtridAndBqual, 0, gtrid, 0, gtridLength);
                        System.arraycopy(gtridAndBqual, gtridLength, bqual, 0, bqualLength);
                        recoveredXidList.add(new MysqlXid(gtrid, bqual, formatId));
                    }
                    Object var14_17 = null;
                    if (rs == null) break block17;
                }
                catch (SQLException sqlEx) {
                    throw MysqlXAConnection.mapXAExceptionFromSQLException(sqlEx);
                }
            }
            catch (Throwable throwable) {
                Object var14_18 = null;
                if (rs != null) {
                    try {
                        rs.close();
                    }
                    catch (SQLException sqlEx) {
                        throw MysqlXAConnection.mapXAExceptionFromSQLException(sqlEx);
                    }
                }
                if (stmt == null) throw throwable;
                try {
                    stmt.close();
                    throw throwable;
                }
                catch (SQLException sqlEx) {
                    throw MysqlXAConnection.mapXAExceptionFromSQLException(sqlEx);
                }
            }
            try {}
            catch (SQLException sqlEx) {
                throw MysqlXAConnection.mapXAExceptionFromSQLException(sqlEx);
            }
            rs.close();
        }
        if (stmt != null) {
            try {}
            catch (SQLException sqlEx) {
                throw MysqlXAConnection.mapXAExceptionFromSQLException(sqlEx);
            }
            stmt.close();
        }
        int numXids = recoveredXidList.size();
        Xid[] asXids = new Xid[numXids];
        Object[] asObjects = recoveredXidList.toArray();
        int i = 0;
        while (i < numXids) {
            asXids[i] = (Xid)asObjects[i];
            ++i;
        }
        return asXids;
    }

    public int prepare(Xid xid) throws XAException {
        StringBuilder commandBuf = new StringBuilder(300);
        commandBuf.append("XA PREPARE ");
        MysqlXAConnection.appendXid(commandBuf, xid);
        this.dispatchCommand(commandBuf.toString());
        return 0;
    }

    public void forget(Xid xid) throws XAException {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void rollback(Xid xid) throws XAException {
        StringBuilder commandBuf = new StringBuilder(300);
        commandBuf.append("XA ROLLBACK ");
        MysqlXAConnection.appendXid(commandBuf, xid);
        try {
            this.dispatchCommand(commandBuf.toString());
            Object var4_3 = null;
            this.underlyingConnection.setInGlobalTx(false);
        }
        catch (Throwable throwable) {
            Object var4_4 = null;
            this.underlyingConnection.setInGlobalTx(false);
            throw throwable;
        }
    }

    public void end(Xid xid, int flags) throws XAException {
        StringBuilder commandBuf = new StringBuilder(300);
        commandBuf.append("XA END ");
        MysqlXAConnection.appendXid(commandBuf, xid);
        switch (flags) {
            case 0x4000000: {
                break;
            }
            case 0x2000000: {
                commandBuf.append(" SUSPEND");
                break;
            }
            case 0x20000000: {
                break;
            }
            default: {
                throw new XAException(-5);
            }
        }
        this.dispatchCommand(commandBuf.toString());
    }

    public void start(Xid xid, int flags) throws XAException {
        StringBuilder commandBuf = new StringBuilder(300);
        commandBuf.append("XA START ");
        MysqlXAConnection.appendXid(commandBuf, xid);
        switch (flags) {
            case 0x200000: {
                commandBuf.append(" JOIN");
                break;
            }
            case 0x8000000: {
                commandBuf.append(" RESUME");
                break;
            }
            case 0: {
                break;
            }
            default: {
                throw new XAException(-5);
            }
        }
        this.dispatchCommand(commandBuf.toString());
        this.underlyingConnection.setInGlobalTx(true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void commit(Xid xid, boolean onePhase) throws XAException {
        StringBuilder commandBuf = new StringBuilder(300);
        commandBuf.append("XA COMMIT ");
        MysqlXAConnection.appendXid(commandBuf, xid);
        if (onePhase) {
            commandBuf.append(" ONE PHASE");
        }
        try {
            this.dispatchCommand(commandBuf.toString());
            Object var5_4 = null;
            this.underlyingConnection.setInGlobalTx(false);
        }
        catch (Throwable throwable) {
            Object var5_5 = null;
            this.underlyingConnection.setInGlobalTx(false);
            throw throwable;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private ResultSet dispatchCommand(String command) throws XAException {
        ResultSet resultSet;
        Statement stmt = null;
        try {
            try {
                ResultSet rs;
                if (this.logXaCommands) {
                    this.log.logDebug("Executing XA statement: " + command);
                }
                stmt = this.underlyingConnection.createStatement();
                stmt.execute(command);
                resultSet = rs = stmt.getResultSet();
                Object var6_6 = null;
                if (stmt == null) return resultSet;
            }
            catch (SQLException sqlEx) {
                throw MysqlXAConnection.mapXAExceptionFromSQLException(sqlEx);
            }
        }
        catch (Throwable throwable) {
            Object var6_7 = null;
            if (stmt == null) throw throwable;
            try {
                stmt.close();
                throw throwable;
            }
            catch (SQLException sqlEx2) {
                throw throwable;
            }
        }
        try {}
        catch (SQLException sqlEx2) {
            // empty catch block
            return resultSet;
        }
        stmt.close();
        return resultSet;
    }

    protected static XAException mapXAExceptionFromSQLException(SQLException sqlEx) {
        Integer xaCode = MYSQL_ERROR_CODES_TO_XA_ERROR_CODES.get(sqlEx.getErrorCode());
        if (xaCode != null) {
            return (XAException)new MysqlXAException(xaCode, sqlEx.getMessage(), null).initCause(sqlEx);
        }
        return (XAException)new MysqlXAException(-7, Messages.getString("MysqlXAConnection.003"), null).initCause(sqlEx);
    }

    private static void appendXid(StringBuilder builder, Xid xid) {
        byte[] gtrid = xid.getGlobalTransactionId();
        byte[] btrid = xid.getBranchQualifier();
        if (gtrid != null) {
            StringUtils.appendAsHex(builder, gtrid);
        }
        builder.append(',');
        if (btrid != null) {
            StringUtils.appendAsHex(builder, btrid);
        }
        builder.append(',');
        StringUtils.appendAsHex(builder, xid.getFormatId());
    }

    public synchronized java.sql.Connection getConnection() throws SQLException {
        java.sql.Connection connToWrap = this.getConnection(false, true);
        return connToWrap;
    }

    static {
        HashMap<Integer, Integer> temp = new HashMap<Integer, Integer>();
        temp.put(1397, -4);
        temp.put(1398, -5);
        temp.put(1399, -7);
        temp.put(1400, -9);
        temp.put(1401, -3);
        temp.put(1402, 100);
        temp.put(1440, -8);
        temp.put(1613, 106);
        temp.put(1614, 102);
        MYSQL_ERROR_CODES_TO_XA_ERROR_CODES = Collections.unmodifiableMap(temp);
        if (Util.isJdbc4()) {
            try {
                JDBC_4_XA_CONNECTION_WRAPPER_CTOR = Class.forName("com.mysql.jdbc.jdbc2.optional.JDBC4MysqlXAConnection").getConstructor(Connection.class, Boolean.TYPE);
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
    }
}


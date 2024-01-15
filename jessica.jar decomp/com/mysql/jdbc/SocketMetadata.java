/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.Messages;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public interface SocketMetadata {
    public boolean isLocallyConnected(ConnectionImpl var1) throws SQLException;

    public static class Helper {
        public static final String IS_LOCAL_HOSTNAME_REPLACEMENT_PROPERTY_NAME = "com.mysql.jdbc.test.isLocalHostnameReplacement";

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public static boolean isLocallyConnected(ConnectionImpl conn) throws SQLException {
            long threadId = conn.getId();
            Statement processListStmt = conn.getMetadataSafeStatement();
            ResultSet rs = null;
            String processHost = null;
            if (System.getProperty(IS_LOCAL_HOSTNAME_REPLACEMENT_PROPERTY_NAME) != null) {
                processHost = System.getProperty(IS_LOCAL_HOSTNAME_REPLACEMENT_PROPERTY_NAME);
            } else if (conn.getProperties().getProperty(IS_LOCAL_HOSTNAME_REPLACEMENT_PROPERTY_NAME) != null) {
                processHost = conn.getProperties().getProperty(IS_LOCAL_HOSTNAME_REPLACEMENT_PROPERTY_NAME);
            } else {
                try {
                    processHost = Helper.findProcessHost(threadId, processListStmt);
                    if (processHost == null) {
                        conn.getLog().logWarn(String.format("Connection id %d not found in \"SHOW PROCESSLIST\", assuming 32-bit overflow, using SELECT CONNECTION_ID() instead", threadId));
                        rs = processListStmt.executeQuery("SELECT CONNECTION_ID()");
                        if (rs.next()) {
                            threadId = rs.getLong(1);
                            processHost = Helper.findProcessHost(threadId, processListStmt);
                        } else {
                            conn.getLog().logError("No rows returned for statement \"SELECT CONNECTION_ID()\", local connection check will most likely be incorrect");
                        }
                    }
                    Object var7_5 = null;
                }
                catch (Throwable throwable) {
                    Object var7_6 = null;
                    processListStmt.close();
                    throw throwable;
                }
                processListStmt.close();
                {
                }
            }
            if (processHost != null) {
                conn.getLog().logDebug(String.format("Using 'host' value of '%s' to determine locality of connection", processHost));
                int endIndex = processHost.lastIndexOf(":");
                if (endIndex != -1) {
                    processHost = processHost.substring(0, endIndex);
                    try {
                        boolean isLocal = false;
                        InetAddress[] allHostAddr = InetAddress.getAllByName(processHost);
                        SocketAddress remoteSocketAddr = conn.getIO().mysqlConnection.getRemoteSocketAddress();
                        if (remoteSocketAddr instanceof InetSocketAddress) {
                            InetAddress whereIConnectedTo = ((InetSocketAddress)remoteSocketAddr).getAddress();
                            for (InetAddress hostAddr : allHostAddr) {
                                if (hostAddr.equals(whereIConnectedTo)) {
                                    conn.getLog().logDebug(String.format("Locally connected - HostAddress(%s).equals(whereIconnectedTo({%s})", hostAddr, whereIConnectedTo));
                                    isLocal = true;
                                    break;
                                }
                                conn.getLog().logDebug(String.format("Attempted locally connected check failed - ! HostAddress(%s).equals(whereIconnectedTo(%s)", hostAddr, whereIConnectedTo));
                            }
                        } else {
                            String msg = String.format("Remote socket address %s is not an inet socket address", remoteSocketAddr);
                            conn.getLog().logDebug(msg);
                        }
                        return isLocal;
                    }
                    catch (UnknownHostException e) {
                        conn.getLog().logWarn(Messages.getString("Connection.CantDetectLocalConnect", new Object[]{processHost}), e);
                        return false;
                    }
                }
                conn.getLog().logWarn(String.format("No port number present in 'host' from SHOW PROCESSLIST '%s', unable to determine whether locally connected", processHost));
                return false;
            }
            conn.getLog().logWarn(String.format("Cannot find process listing for connection %d in SHOW PROCESSLIST output, unable to determine if locally connected", threadId));
            return false;
        }

        private static String findProcessHost(long threadId, Statement processListStmt) throws SQLException {
            String processHost = null;
            ResultSet rs = processListStmt.executeQuery("SHOW PROCESSLIST");
            while (rs.next()) {
                long id = rs.getLong(1);
                if (threadId != id) continue;
                processHost = rs.getString(3);
                break;
            }
            return processHost;
        }
    }
}


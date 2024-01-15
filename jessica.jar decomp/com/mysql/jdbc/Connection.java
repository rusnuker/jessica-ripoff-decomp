/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.ConnectionProperties;
import com.mysql.jdbc.Extension;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.log.Log;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.Executor;

public interface Connection
extends java.sql.Connection,
ConnectionProperties {
    public void changeUser(String var1, String var2) throws SQLException;

    @Deprecated
    public void clearHasTriedMaster();

    public PreparedStatement clientPrepareStatement(String var1) throws SQLException;

    public PreparedStatement clientPrepareStatement(String var1, int var2) throws SQLException;

    public PreparedStatement clientPrepareStatement(String var1, int var2, int var3) throws SQLException;

    public PreparedStatement clientPrepareStatement(String var1, int[] var2) throws SQLException;

    public PreparedStatement clientPrepareStatement(String var1, int var2, int var3, int var4) throws SQLException;

    public PreparedStatement clientPrepareStatement(String var1, String[] var2) throws SQLException;

    public int getActiveStatementCount();

    public long getIdleFor();

    public Log getLog() throws SQLException;

    @Deprecated
    public String getServerCharacterEncoding();

    public String getServerCharset();

    public TimeZone getServerTimezoneTZ();

    public String getStatementComment();

    @Deprecated
    public boolean hasTriedMaster();

    public boolean isInGlobalTx();

    public void setInGlobalTx(boolean var1);

    public boolean isMasterConnection();

    public boolean isNoBackslashEscapesSet();

    public boolean isSameResource(Connection var1);

    public boolean lowerCaseTableNames();

    public boolean parserKnowsUnicode();

    public void ping() throws SQLException;

    public void resetServerState() throws SQLException;

    public PreparedStatement serverPrepareStatement(String var1) throws SQLException;

    public PreparedStatement serverPrepareStatement(String var1, int var2) throws SQLException;

    public PreparedStatement serverPrepareStatement(String var1, int var2, int var3) throws SQLException;

    public PreparedStatement serverPrepareStatement(String var1, int var2, int var3, int var4) throws SQLException;

    public PreparedStatement serverPrepareStatement(String var1, int[] var2) throws SQLException;

    public PreparedStatement serverPrepareStatement(String var1, String[] var2) throws SQLException;

    public void setFailedOver(boolean var1);

    @Deprecated
    public void setPreferSlaveDuringFailover(boolean var1);

    public void setStatementComment(String var1);

    public void shutdownServer() throws SQLException;

    public boolean supportsIsolationLevel();

    public boolean supportsQuotedIdentifiers();

    public boolean supportsTransactions();

    public boolean versionMeetsMinimum(int var1, int var2, int var3) throws SQLException;

    public void reportQueryTime(long var1);

    public boolean isAbonormallyLongQuery(long var1);

    public void initializeExtension(Extension var1) throws SQLException;

    public int getAutoIncrementIncrement();

    public boolean hasSameProperties(Connection var1);

    public Properties getProperties();

    public String getHost();

    public void setProxy(MySQLConnection var1);

    public boolean isServerLocal() throws SQLException;

    public int getSessionMaxRows();

    public void setSessionMaxRows(int var1) throws SQLException;

    public void setSchema(String var1) throws SQLException;

    public String getSchema() throws SQLException;

    public void abort(Executor var1) throws SQLException;

    public void setNetworkTimeout(Executor var1, int var2) throws SQLException;

    public int getNetworkTimeout() throws SQLException;

    public void abortInternal() throws SQLException;

    public void checkClosed() throws SQLException;

    public Object getConnectionMutex();
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Buffer;
import com.mysql.jdbc.CachedResultSetMetaData;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ConnectionProperties;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.MysqlIO;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.ServerPreparedStatement;
import com.mysql.jdbc.SingleByteCharsetConverter;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StatementImpl;
import com.mysql.jdbc.StatementInterceptorV2;
import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Timer;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface MySQLConnection
extends Connection,
ConnectionProperties {
    public boolean isProxySet();

    public void createNewIO(boolean var1) throws SQLException;

    public void dumpTestcaseQuery(String var1);

    public Connection duplicate() throws SQLException;

    public ResultSetInternalMethods execSQL(StatementImpl var1, String var2, int var3, Buffer var4, int var5, int var6, boolean var7, String var8, Field[] var9) throws SQLException;

    public ResultSetInternalMethods execSQL(StatementImpl var1, String var2, int var3, Buffer var4, int var5, int var6, boolean var7, String var8, Field[] var9, boolean var10) throws SQLException;

    public String extractSqlFromPacket(String var1, Buffer var2, int var3) throws SQLException;

    public StringBuilder generateConnectionCommentBlock(StringBuilder var1);

    @Override
    public int getActiveStatementCount();

    @Override
    public int getAutoIncrementIncrement();

    public CachedResultSetMetaData getCachedMetaData(String var1);

    public Calendar getCalendarInstanceForSessionOrNew();

    public Timer getCancelTimer();

    public String getCharacterSetMetadata();

    public SingleByteCharsetConverter getCharsetConverter(String var1) throws SQLException;

    @Deprecated
    public String getCharsetNameForIndex(int var1) throws SQLException;

    public String getEncodingForIndex(int var1) throws SQLException;

    public TimeZone getDefaultTimeZone();

    public String getErrorMessageEncoding();

    @Override
    public ExceptionInterceptor getExceptionInterceptor();

    @Override
    public String getHost();

    public String getHostPortPair();

    public long getId();

    @Override
    public long getIdleFor();

    public MysqlIO getIO() throws SQLException;

    @Override
    public Log getLog() throws SQLException;

    public int getMaxBytesPerChar(String var1) throws SQLException;

    public int getMaxBytesPerChar(Integer var1, String var2) throws SQLException;

    public java.sql.Statement getMetadataSafeStatement() throws SQLException;

    public int getNetBufferLength();

    @Override
    public Properties getProperties();

    public boolean getRequiresEscapingEncoder();

    @Override
    public String getServerCharset();

    public int getServerMajorVersion();

    public int getServerMinorVersion();

    public int getServerSubMinorVersion();

    @Override
    public TimeZone getServerTimezoneTZ();

    public String getServerVariable(String var1);

    public String getServerVersion();

    public Calendar getSessionLockedCalendar();

    @Override
    public String getStatementComment();

    public List<StatementInterceptorV2> getStatementInterceptorsInstances();

    public String getURL();

    public String getUser();

    public Calendar getUtcCalendar();

    public void incrementNumberOfPreparedExecutes();

    public void incrementNumberOfPrepares();

    public void incrementNumberOfResultSetsCreated();

    public void initializeResultsMetadataFromCache(String var1, CachedResultSetMetaData var2, ResultSetInternalMethods var3) throws SQLException;

    public void initializeSafeStatementInterceptors() throws SQLException;

    @Override
    public boolean isAbonormallyLongQuery(long var1);

    public boolean isClientTzUTC();

    public boolean isCursorFetchEnabled() throws SQLException;

    public boolean isReadInfoMsgEnabled();

    @Override
    public boolean isReadOnly() throws SQLException;

    public boolean isReadOnly(boolean var1) throws SQLException;

    public boolean isRunningOnJDK13();

    public boolean isServerTzUTC();

    @Override
    public boolean lowerCaseTableNames();

    public void pingInternal(boolean var1, int var2) throws SQLException;

    public void realClose(boolean var1, boolean var2, boolean var3, Throwable var4) throws SQLException;

    public void recachePreparedStatement(ServerPreparedStatement var1) throws SQLException;

    public void decachePreparedStatement(ServerPreparedStatement var1) throws SQLException;

    public void registerQueryExecutionTime(long var1);

    public void registerStatement(Statement var1);

    public void reportNumberOfTablesAccessed(int var1);

    public boolean serverSupportsConvertFn() throws SQLException;

    @Override
    public void setProxy(MySQLConnection var1);

    public void setReadInfoMsgEnabled(boolean var1);

    public void setReadOnlyInternal(boolean var1) throws SQLException;

    @Override
    public void shutdownServer() throws SQLException;

    public boolean storesLowerCaseTableName();

    public void throwConnectionClosedException() throws SQLException;

    public void transactionBegun() throws SQLException;

    public void transactionCompleted() throws SQLException;

    public void unregisterStatement(Statement var1);

    public void unSafeStatementInterceptors() throws SQLException;

    public boolean useAnsiQuotedIdentifiers();

    @Override
    public String getConnectionAttributes() throws SQLException;

    @Deprecated
    public MySQLConnection getLoadBalanceSafeProxy();

    public MySQLConnection getMultiHostSafeProxy();

    public ProfilerEventHandler getProfilerEventHandlerInstance();

    public void setProfilerEventHandlerInstance(ProfilerEventHandler var1);
}


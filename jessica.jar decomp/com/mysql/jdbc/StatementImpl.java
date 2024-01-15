/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.AssertionFailedException;
import com.mysql.jdbc.ByteArrayRow;
import com.mysql.jdbc.CachedResultSetMetaData;
import com.mysql.jdbc.CharsetMapping;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Constants;
import com.mysql.jdbc.DatabaseMetaData;
import com.mysql.jdbc.EscapeProcessor;
import com.mysql.jdbc.EscapeProcessorResult;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.MysqlIO;
import com.mysql.jdbc.PingTarget;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ProfilerEventHandlerFactory;
import com.mysql.jdbc.ResultSetImpl;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.ResultSetRow;
import com.mysql.jdbc.RowDataStatic;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.SingleByteCharsetConverter;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StringUtils;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.exceptions.MySQLStatementCancelledException;
import com.mysql.jdbc.exceptions.MySQLTimeoutException;
import com.mysql.jdbc.log.LogUtils;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.BatchUpdateException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class StatementImpl
implements Statement {
    protected static final String PING_MARKER = "/* ping */";
    protected static final String[] ON_DUPLICATE_KEY_UPDATE_CLAUSE = new String[]{"ON", "DUPLICATE", "KEY", "UPDATE"};
    protected Object cancelTimeoutMutex = new Object();
    static int statementCounter = 1;
    public static final byte USES_VARIABLES_FALSE = 0;
    public static final byte USES_VARIABLES_TRUE = 1;
    public static final byte USES_VARIABLES_UNKNOWN = -1;
    protected boolean wasCancelled = false;
    protected boolean wasCancelledByTimeout = false;
    protected List<Object> batchedArgs;
    protected SingleByteCharsetConverter charConverter = null;
    protected String charEncoding = null;
    protected volatile MySQLConnection connection = null;
    protected long connectionId = 0L;
    protected String currentCatalog = null;
    protected boolean doEscapeProcessing = true;
    protected ProfilerEventHandler eventSink = null;
    private int fetchSize = 0;
    protected boolean isClosed = false;
    protected long lastInsertId = -1L;
    protected int maxFieldSize = MysqlIO.getMaxBuf();
    protected int maxRows = -1;
    protected Set<ResultSetInternalMethods> openResults = new HashSet<ResultSetInternalMethods>();
    protected boolean pedantic = false;
    protected String pointOfOrigin;
    protected boolean profileSQL = false;
    protected ResultSetInternalMethods results = null;
    protected ResultSetInternalMethods generatedKeysResults = null;
    protected int resultSetConcurrency = 0;
    protected int resultSetType = 0;
    protected int statementId;
    protected int timeoutInMillis = 0;
    protected long updateCount = -1L;
    protected boolean useUsageAdvisor = false;
    protected SQLWarning warningChain = null;
    protected boolean clearWarningsCalled = false;
    protected boolean holdResultsOpenOverClose = false;
    protected ArrayList<ResultSetRow> batchedGeneratedKeys = null;
    protected boolean retrieveGeneratedKeys = false;
    protected boolean continueBatchOnError = false;
    protected PingTarget pingTarget = null;
    protected boolean useLegacyDatetimeCode;
    protected boolean sendFractionalSeconds;
    private ExceptionInterceptor exceptionInterceptor;
    protected boolean lastQueryIsOnDupKeyUpdate = false;
    protected final AtomicBoolean statementExecuting = new AtomicBoolean(false);
    private boolean isImplicitlyClosingResults = false;
    private int originalResultSetType = 0;
    private int originalFetchSize = 0;
    private boolean isPoolable = true;
    private InputStream localInfileInputStream;
    protected final boolean version5013OrNewer;
    private boolean closeOnCompletion = false;

    public StatementImpl(MySQLConnection c, String catalog) throws SQLException {
        int maxRowsConn;
        boolean profiling;
        if (c == null || c.isClosed()) {
            throw SQLError.createSQLException(Messages.getString("Statement.0"), "08003", null);
        }
        this.connection = c;
        this.connectionId = this.connection.getId();
        this.exceptionInterceptor = this.connection.getExceptionInterceptor();
        this.currentCatalog = catalog;
        this.pedantic = this.connection.getPedantic();
        this.continueBatchOnError = this.connection.getContinueBatchOnError();
        this.useLegacyDatetimeCode = this.connection.getUseLegacyDatetimeCode();
        this.sendFractionalSeconds = this.connection.getSendFractionalSeconds();
        this.doEscapeProcessing = this.connection.getEnableEscapeProcessing();
        if (!this.connection.getDontTrackOpenResources()) {
            this.connection.registerStatement(this);
        }
        this.maxFieldSize = this.connection.getMaxAllowedPacket();
        int defaultFetchSize = this.connection.getDefaultFetchSize();
        if (defaultFetchSize != 0) {
            this.setFetchSize(defaultFetchSize);
        }
        if (this.connection.getUseUnicode()) {
            this.charEncoding = this.connection.getEncoding();
            this.charConverter = this.connection.getCharsetConverter(this.charEncoding);
        }
        boolean bl = profiling = this.connection.getProfileSql() || this.connection.getUseUsageAdvisor() || this.connection.getLogSlowQueries();
        if (this.connection.getAutoGenerateTestcaseScript() || profiling) {
            this.statementId = statementCounter++;
        }
        if (profiling) {
            this.pointOfOrigin = LogUtils.findCallingClassAndMethod(new Throwable());
            this.profileSQL = this.connection.getProfileSql();
            this.useUsageAdvisor = this.connection.getUseUsageAdvisor();
            this.eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
        }
        if ((maxRowsConn = this.connection.getMaxRows()) != -1) {
            this.setMaxRows(maxRowsConn);
        }
        this.holdResultsOpenOverClose = this.connection.getHoldResultsOpenOverStatementClose();
        this.version5013OrNewer = this.connection.versionMeetsMinimum(5, 0, 13);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addBatch(String sql) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.batchedArgs == null) {
                this.batchedArgs = new ArrayList<Object>();
            }
            if (sql != null) {
                this.batchedArgs.add(sql);
            }
        }
    }

    public List<Object> getBatchedArgs() {
        return this.batchedArgs == null ? null : Collections.unmodifiableList(this.batchedArgs);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void cancel() throws SQLException {
        if (!this.statementExecuting.get()) {
            return;
        }
        if (!this.isClosed && this.connection != null && this.connection.versionMeetsMinimum(5, 0, 0)) {
            Connection cancelConn;
            block7: {
                cancelConn = null;
                java.sql.Statement cancelStmt = null;
                try {
                    cancelConn = this.connection.duplicate();
                    cancelStmt = cancelConn.createStatement();
                    cancelStmt.execute("KILL QUERY " + this.connection.getIO().getThreadId());
                    this.wasCancelled = true;
                    Object var4_3 = null;
                    if (cancelStmt == null) break block7;
                }
                catch (Throwable throwable) {
                    Object var4_4 = null;
                    if (cancelStmt != null) {
                        cancelStmt.close();
                    }
                    if (cancelConn != null) {
                        cancelConn.close();
                    }
                    throw throwable;
                }
                cancelStmt.close();
            }
            if (cancelConn != null) {
                cancelConn.close();
            }
        }
    }

    protected MySQLConnection checkClosed() throws SQLException {
        MySQLConnection c = this.connection;
        if (c == null) {
            throw SQLError.createSQLException(Messages.getString("Statement.49"), "S1009", this.getExceptionInterceptor());
        }
        return c;
    }

    protected void checkForDml(String sql, char firstStatementChar) throws SQLException {
        String noCommentSql;
        if ((firstStatementChar == 'I' || firstStatementChar == 'U' || firstStatementChar == 'D' || firstStatementChar == 'A' || firstStatementChar == 'C' || firstStatementChar == 'T' || firstStatementChar == 'R') && (StringUtils.startsWithIgnoreCaseAndWs(noCommentSql = StringUtils.stripComments(sql, "'\"", "'\"", true, false, true, true), "INSERT") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "UPDATE") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "DELETE") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "DROP") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "CREATE") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "ALTER") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "TRUNCATE") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "RENAME"))) {
            throw SQLError.createSQLException(Messages.getString("Statement.57"), "S1009", this.getExceptionInterceptor());
        }
    }

    protected void checkNullOrEmptyQuery(String sql) throws SQLException {
        if (sql == null) {
            throw SQLError.createSQLException(Messages.getString("Statement.59"), "S1009", this.getExceptionInterceptor());
        }
        if (sql.length() == 0) {
            throw SQLError.createSQLException(Messages.getString("Statement.61"), "S1009", this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void clearBatch() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.batchedArgs != null) {
                this.batchedArgs.clear();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void clearWarnings() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.clearWarningsCalled = true;
            this.warningChain = null;
        }
    }

    @Override
    public void close() throws SQLException {
        this.realClose(true, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void closeAllOpenResults() throws SQLException {
        MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn == null) {
            return;
        }
        Object object = locallyScopedConn.getConnectionMutex();
        synchronized (object) {
            if (this.openResults != null) {
                for (ResultSetInternalMethods element : this.openResults) {
                    try {
                        element.realClose(false);
                    }
                    catch (SQLException sqlEx) {
                        AssertionFailedException.shouldNotHappen(sqlEx);
                    }
                }
                this.openResults.clear();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void implicitlyCloseAllOpenResults() throws SQLException {
        this.isImplicitlyClosingResults = true;
        try {
            if (!(this.connection.getHoldResultsOpenOverStatementClose() || this.connection.getDontTrackOpenResources() || this.holdResultsOpenOverClose)) {
                if (this.results != null) {
                    this.results.realClose(false);
                }
                if (this.generatedKeysResults != null) {
                    this.generatedKeysResults.realClose(false);
                }
                this.closeAllOpenResults();
            }
            Object var2_1 = null;
            this.isImplicitlyClosingResults = false;
        }
        catch (Throwable throwable) {
            Object var2_2 = null;
            this.isImplicitlyClosingResults = false;
            throw throwable;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeOpenResultSet(ResultSetInternalMethods rs) {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                boolean hasMoreResults;
                if (this.openResults != null) {
                    this.openResults.remove(rs);
                }
                boolean bl = hasMoreResults = rs.getNextResultSet() != null;
                if (this.results == rs && !hasMoreResults) {
                    this.results = null;
                }
                if (this.generatedKeysResults == rs) {
                    this.generatedKeysResults = null;
                }
                if (!this.isImplicitlyClosingResults && !hasMoreResults) {
                    this.checkAndPerformCloseOnCompletionAction();
                }
            }
        }
        catch (SQLException sQLException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getOpenResultSetCount() {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                if (this.openResults != null) {
                    return this.openResults.size();
                }
                return 0;
            }
        }
        catch (SQLException e) {
            return 0;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void checkAndPerformCloseOnCompletionAction() {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                if (!(!this.isCloseOnCompletion() || this.connection.getDontTrackOpenResources() || this.getOpenResultSetCount() != 0 || this.results != null && this.results.reallyResult() && !this.results.isClosed() || this.generatedKeysResults != null && this.generatedKeysResults.reallyResult() && !this.generatedKeysResults.isClosed())) {
                    this.realClose(false, false);
                }
            }
        }
        catch (SQLException sQLException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private ResultSetInternalMethods createResultSetUsingServerFetch(String sql) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            java.sql.PreparedStatement pStmt = this.connection.prepareStatement(sql, this.resultSetType, this.resultSetConcurrency);
            pStmt.setFetchSize(this.fetchSize);
            if (this.maxRows > -1) {
                pStmt.setMaxRows(this.maxRows);
            }
            this.statementBegins();
            pStmt.execute();
            ResultSetInternalMethods rs = ((StatementImpl)((Object)pStmt)).getResultSetInternal();
            rs.setStatementUsedForFetchingRows((PreparedStatement)pStmt);
            this.results = rs;
            return rs;
        }
    }

    protected boolean createStreamingResultSet() {
        return this.resultSetType == 1003 && this.resultSetConcurrency == 1007 && this.fetchSize == Integer.MIN_VALUE;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void enableStreamingResults() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.originalResultSetType = this.resultSetType;
            this.originalFetchSize = this.fetchSize;
            this.setFetchSize(Integer.MIN_VALUE);
            this.setResultSetType(1003);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void disableStreamingResults() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.fetchSize == Integer.MIN_VALUE && this.resultSetType == 1003) {
                this.setFetchSize(this.originalFetchSize);
                this.setResultSetType(this.originalResultSetType);
            }
        }
    }

    protected void setupStreamingTimeout(MySQLConnection con) throws SQLException {
        if (this.createStreamingResultSet() && con.getNetTimeoutForStreamingResults() > 0) {
            this.executeSimpleNonQuery(con, "SET net_write_timeout=" + con.getNetTimeoutForStreamingResults());
        }
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return this.executeInternal(sql, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private boolean executeInternal(String sql, boolean returnGeneratedKeys) throws SQLException {
        MySQLConnection locallyScopedConn = this.checkClosed();
        Object object = locallyScopedConn.getConnectionMutex();
        synchronized (object) {
            boolean bl;
            this.checkClosed();
            this.checkNullOrEmptyQuery(sql);
            this.resetCancelledState();
            char firstNonWsChar = StringUtils.firstAlphaCharUc(sql, StatementImpl.findStartOfStatement(sql));
            boolean maybeSelect = firstNonWsChar == 'S';
            this.retrieveGeneratedKeys = returnGeneratedKeys;
            boolean bl2 = this.lastQueryIsOnDupKeyUpdate = returnGeneratedKeys && firstNonWsChar == 'I' && this.containsOnDuplicateKeyInString(sql);
            if (!maybeSelect && locallyScopedConn.isReadOnly()) {
                throw SQLError.createSQLException(Messages.getString("Statement.27") + Messages.getString("Statement.28"), "S1009", this.getExceptionInterceptor());
            }
            boolean readInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
            if (returnGeneratedKeys && firstNonWsChar == 'R') {
                locallyScopedConn.setReadInfoMsgEnabled(true);
            }
            try {
                this.setupStreamingTimeout(locallyScopedConn);
                if (this.doEscapeProcessing) {
                    Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, locallyScopedConn.serverSupportsConvertFn(), locallyScopedConn);
                    sql = escapedSqlResult instanceof String ? (String)escapedSqlResult : ((EscapeProcessorResult)escapedSqlResult).escapedSql;
                }
                this.implicitlyCloseAllOpenResults();
                if (sql.charAt(0) == '/' && sql.startsWith(PING_MARKER)) {
                    this.doPingInstead();
                    boolean escapedSqlResult = true;
                    Object var19_10 = null;
                    locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
                    this.statementExecuting.set(false);
                    return escapedSqlResult;
                }
                CachedResultSetMetaData cachedMetaData = null;
                ResultSetInternalMethods rs = null;
                this.batchedGeneratedKeys = null;
                if (this.useServerFetch()) {
                    rs = this.createResultSetUsingServerFetch(sql);
                } else {
                    String oldCatalog;
                    block28: {
                        TimerTask timeoutTask = null;
                        oldCatalog = null;
                        try {
                            if (locallyScopedConn.getEnableQueryTimeouts() && this.timeoutInMillis != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                                timeoutTask = new CancelTask(this);
                                locallyScopedConn.getCancelTimer().schedule(timeoutTask, this.timeoutInMillis);
                            }
                            if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
                                oldCatalog = locallyScopedConn.getCatalog();
                                locallyScopedConn.setCatalog(this.currentCatalog);
                            }
                            Field[] cachedFields = null;
                            if (locallyScopedConn.getCacheResultSetMetadata() && (cachedMetaData = locallyScopedConn.getCachedMetaData(sql)) != null) {
                                cachedFields = cachedMetaData.fields;
                            }
                            locallyScopedConn.setSessionMaxRows(maybeSelect ? this.maxRows : -1);
                            this.statementBegins();
                            rs = locallyScopedConn.execSQL(this, sql, this.maxRows, null, this.resultSetType, this.resultSetConcurrency, this.createStreamingResultSet(), this.currentCatalog, cachedFields);
                            if (timeoutTask != null) {
                                if (((CancelTask)timeoutTask).caughtWhileCancelling != null) {
                                    throw ((CancelTask)timeoutTask).caughtWhileCancelling;
                                }
                                timeoutTask.cancel();
                                timeoutTask = null;
                            }
                            Object object2 = this.cancelTimeoutMutex;
                            synchronized (object2) {
                                if (this.wasCancelled) {
                                    SQLException cause = null;
                                    cause = this.wasCancelledByTimeout ? new MySQLTimeoutException() : new MySQLStatementCancelledException();
                                    this.resetCancelledState();
                                    throw cause;
                                }
                            }
                            Object var17_20 = null;
                            if (timeoutTask == null) break block28;
                        }
                        catch (Throwable throwable) {
                            Object var17_21 = null;
                            if (timeoutTask != null) {
                                timeoutTask.cancel();
                                locallyScopedConn.getCancelTimer().purge();
                            }
                            if (oldCatalog == null) throw throwable;
                            locallyScopedConn.setCatalog(oldCatalog);
                            throw throwable;
                        }
                        timeoutTask.cancel();
                        locallyScopedConn.getCancelTimer().purge();
                    }
                    if (oldCatalog != null) {
                        locallyScopedConn.setCatalog(oldCatalog);
                    }
                }
                if (rs != null) {
                    this.lastInsertId = rs.getUpdateID();
                    this.results = rs;
                    rs.setFirstCharOfQuery(firstNonWsChar);
                    if (rs.reallyResult()) {
                        if (cachedMetaData != null) {
                            locallyScopedConn.initializeResultsMetadataFromCache(sql, cachedMetaData, this.results);
                        } else if (this.connection.getCacheResultSetMetadata()) {
                            locallyScopedConn.initializeResultsMetadataFromCache(sql, null, this.results);
                        }
                    }
                }
                bl = rs != null && rs.reallyResult();
            }
            catch (Throwable throwable) {
                Object var19_12 = null;
                locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
                this.statementExecuting.set(false);
                throw throwable;
            }
            Object var19_11 = null;
            locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
            this.statementExecuting.set(false);
            return bl;
        }
    }

    protected void statementBegins() {
        this.clearWarningsCalled = false;
        this.statementExecuting.set(true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void resetCancelledState() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.cancelTimeoutMutex == null) {
                return;
            }
            Object object2 = this.cancelTimeoutMutex;
            synchronized (object2) {
                this.wasCancelled = false;
                this.wasCancelledByTimeout = false;
            }
        }
    }

    @Override
    public boolean execute(String sql, int returnGeneratedKeys) throws SQLException {
        return this.executeInternal(sql, returnGeneratedKeys == 1);
    }

    @Override
    public boolean execute(String sql, int[] generatedKeyIndices) throws SQLException {
        return this.executeInternal(sql, generatedKeyIndices != null && generatedKeyIndices.length > 0);
    }

    @Override
    public boolean execute(String sql, String[] generatedKeyNames) throws SQLException {
        return this.executeInternal(sql, generatedKeyNames != null && generatedKeyNames.length > 0);
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return Util.truncateAndConvertToInt(this.executeBatchInternal());
    }

    /*
     * Exception decompiling
     */
    protected long[] executeBatchInternal() throws SQLException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 2 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    protected final boolean hasDeadlockOrTimeoutRolledBackTx(SQLException ex) {
        int vendorCode = ex.getErrorCode();
        switch (vendorCode) {
            case 1206: 
            case 1213: {
                return true;
            }
            case 1205: {
                return !this.version5013OrNewer;
            }
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private long[] executeBatchUsingMultiQueries(boolean multiQueriesEnabled, int nbrCommands, int individualStatementTimeout) throws SQLException {
        MySQLConnection locallyScopedConn = this.checkClosed();
        Object object = locallyScopedConn.getConnectionMutex();
        synchronized (object) {
            if (!multiQueriesEnabled) {
                locallyScopedConn.getIO().enableMultiQueries();
            }
            java.sql.Statement batchStmt = null;
            TimerTask timeoutTask = null;
            try {
                long[] updateCounts = new long[nbrCommands];
                for (int i = 0; i < nbrCommands; ++i) {
                    updateCounts[i] = -3L;
                }
                int commandIndex = 0;
                StringBuilder queryBuf = new StringBuilder();
                batchStmt = locallyScopedConn.createStatement();
                if (locallyScopedConn.getEnableQueryTimeouts() && individualStatementTimeout != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                    timeoutTask = new CancelTask((StatementImpl)batchStmt);
                    locallyScopedConn.getCancelTimer().schedule(timeoutTask, individualStatementTimeout);
                }
                int counter = 0;
                int numberOfBytesPerChar = 1;
                String connectionEncoding = locallyScopedConn.getEncoding();
                if (StringUtils.startsWithIgnoreCase(connectionEncoding, "utf")) {
                    numberOfBytesPerChar = 3;
                } else if (CharsetMapping.isMultibyteCharset(connectionEncoding)) {
                    numberOfBytesPerChar = 2;
                }
                int escapeAdjust = 1;
                batchStmt.setEscapeProcessing(this.doEscapeProcessing);
                if (this.doEscapeProcessing) {
                    escapeAdjust = 2;
                }
                SQLException sqlEx = null;
                int argumentSetsInBatchSoFar = 0;
                for (commandIndex = 0; commandIndex < nbrCommands; ++argumentSetsInBatchSoFar, ++commandIndex) {
                    String nextQuery = (String)this.batchedArgs.get(commandIndex);
                    if (((queryBuf.length() + nextQuery.length()) * numberOfBytesPerChar + 1 + 4) * escapeAdjust + 32 > this.connection.getMaxAllowedPacket()) {
                        try {
                            batchStmt.execute(queryBuf.toString(), 1);
                        }
                        catch (SQLException ex) {
                            sqlEx = this.handleExceptionForBatch(commandIndex, argumentSetsInBatchSoFar, updateCounts, ex);
                        }
                        counter = this.processMultiCountsAndKeys((StatementImpl)batchStmt, counter, updateCounts);
                        queryBuf = new StringBuilder();
                        argumentSetsInBatchSoFar = 0;
                    }
                    queryBuf.append(nextQuery);
                    queryBuf.append(";");
                }
                if (queryBuf.length() > 0) {
                    try {
                        batchStmt.execute(queryBuf.toString(), 1);
                    }
                    catch (SQLException ex) {
                        sqlEx = this.handleExceptionForBatch(commandIndex - 1, argumentSetsInBatchSoFar, updateCounts, ex);
                    }
                    counter = this.processMultiCountsAndKeys((StatementImpl)batchStmt, counter, updateCounts);
                }
                if (timeoutTask != null) {
                    if (((CancelTask)timeoutTask).caughtWhileCancelling != null) {
                        throw ((CancelTask)timeoutTask).caughtWhileCancelling;
                    }
                    timeoutTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                    timeoutTask = null;
                }
                if (sqlEx != null) {
                    throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.getExceptionInterceptor());
                }
                long[] lArray = updateCounts != null ? updateCounts : new long[]{};
                Object var20_20 = null;
                if (timeoutTask != null) {
                    timeoutTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                }
                this.resetCancelledState();
                try {
                    if (batchStmt != null) {
                        batchStmt.close();
                    }
                    Object var22_22 = null;
                    if (multiQueriesEnabled) return lArray;
                    locallyScopedConn.getIO().disableMultiQueries();
                }
                catch (Throwable throwable) {
                    Object var22_23 = null;
                    if (multiQueriesEnabled) throw throwable;
                    locallyScopedConn.getIO().disableMultiQueries();
                    throw throwable;
                }
                return lArray;
            }
            catch (Throwable throwable) {
                Object var20_21 = null;
                if (timeoutTask != null) {
                    timeoutTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                }
                this.resetCancelledState();
                try {}
                catch (Throwable throwable2) {
                    Object var22_25 = null;
                    if (multiQueriesEnabled) throw throwable2;
                    locallyScopedConn.getIO().disableMultiQueries();
                    throw throwable2;
                }
                if (batchStmt != null) {
                    batchStmt.close();
                }
                Object var22_24 = null;
                if (multiQueriesEnabled) throw throwable;
                locallyScopedConn.getIO().disableMultiQueries();
                throw throwable;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected int processMultiCountsAndKeys(StatementImpl batchedStatement, int updateCountCounter, long[] updateCounts) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            long generatedKey;
            updateCounts[updateCountCounter++] = batchedStatement.getLargeUpdateCount();
            boolean doGenKeys = this.batchedGeneratedKeys != null;
            Object row = null;
            if (doGenKeys) {
                generatedKey = batchedStatement.getLastInsertID();
                row = new byte[1][];
                row[0] = StringUtils.getBytes(Long.toString(generatedKey));
                this.batchedGeneratedKeys.add(new ByteArrayRow((byte[][])row, this.getExceptionInterceptor()));
            }
            while (batchedStatement.getMoreResults() || batchedStatement.getLargeUpdateCount() != -1L) {
                updateCounts[updateCountCounter++] = batchedStatement.getLargeUpdateCount();
                if (!doGenKeys) continue;
                generatedKey = batchedStatement.getLastInsertID();
                row = new byte[1][];
                row[0] = StringUtils.getBytes(Long.toString(generatedKey));
                this.batchedGeneratedKeys.add(new ByteArrayRow((byte[][])row, this.getExceptionInterceptor()));
            }
            return updateCountCounter;
        }
    }

    protected SQLException handleExceptionForBatch(int endOfBatchIndex, int numValuesPerBatch, long[] updateCounts, SQLException ex) throws BatchUpdateException, SQLException {
        for (int j = endOfBatchIndex; j > endOfBatchIndex - numValuesPerBatch; --j) {
            updateCounts[j] = -3L;
        }
        if (this.continueBatchOnError && !(ex instanceof MySQLTimeoutException) && !(ex instanceof MySQLStatementCancelledException) && !this.hasDeadlockOrTimeoutRolledBackTx(ex)) {
            return ex;
        }
        long[] newUpdateCounts = new long[endOfBatchIndex];
        System.arraycopy(updateCounts, 0, newUpdateCounts, 0, endOfBatchIndex);
        throw SQLError.createBatchUpdateException(ex, newUpdateCounts, this.getExceptionInterceptor());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            String oldCatalog;
            CachedResultSetMetaData cachedMetaData;
            MySQLConnection locallyScopedConn;
            block23: {
                locallyScopedConn = this.connection;
                this.retrieveGeneratedKeys = false;
                this.resetCancelledState();
                this.checkNullOrEmptyQuery(sql);
                this.setupStreamingTimeout(locallyScopedConn);
                if (this.doEscapeProcessing) {
                    Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, locallyScopedConn.serverSupportsConvertFn(), this.connection);
                    sql = escapedSqlResult instanceof String ? (String)escapedSqlResult : ((EscapeProcessorResult)escapedSqlResult).escapedSql;
                }
                char firstStatementChar = StringUtils.firstAlphaCharUc(sql, StatementImpl.findStartOfStatement(sql));
                if (sql.charAt(0) == '/' && sql.startsWith(PING_MARKER)) {
                    this.doPingInstead();
                    return this.results;
                }
                this.checkForDml(sql, firstStatementChar);
                this.implicitlyCloseAllOpenResults();
                cachedMetaData = null;
                if (this.useServerFetch()) {
                    this.results = this.createResultSetUsingServerFetch(sql);
                    return this.results;
                }
                TimerTask timeoutTask = null;
                oldCatalog = null;
                try {
                    if (locallyScopedConn.getEnableQueryTimeouts() && this.timeoutInMillis != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                        timeoutTask = new CancelTask(this);
                        locallyScopedConn.getCancelTimer().schedule(timeoutTask, this.timeoutInMillis);
                    }
                    if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
                        oldCatalog = locallyScopedConn.getCatalog();
                        locallyScopedConn.setCatalog(this.currentCatalog);
                    }
                    Field[] cachedFields = null;
                    if (locallyScopedConn.getCacheResultSetMetadata() && (cachedMetaData = locallyScopedConn.getCachedMetaData(sql)) != null) {
                        cachedFields = cachedMetaData.fields;
                    }
                    locallyScopedConn.setSessionMaxRows(this.maxRows);
                    this.statementBegins();
                    this.results = locallyScopedConn.execSQL(this, sql, this.maxRows, null, this.resultSetType, this.resultSetConcurrency, this.createStreamingResultSet(), this.currentCatalog, cachedFields);
                    if (timeoutTask != null) {
                        if (((CancelTask)timeoutTask).caughtWhileCancelling != null) {
                            throw ((CancelTask)timeoutTask).caughtWhileCancelling;
                        }
                        timeoutTask.cancel();
                        locallyScopedConn.getCancelTimer().purge();
                        timeoutTask = null;
                    }
                    Object object2 = this.cancelTimeoutMutex;
                    synchronized (object2) {
                        if (this.wasCancelled) {
                            SQLException cause = null;
                            cause = this.wasCancelledByTimeout ? new MySQLTimeoutException() : new MySQLStatementCancelledException();
                            this.resetCancelledState();
                            throw cause;
                        }
                    }
                    Object var13_13 = null;
                    this.statementExecuting.set(false);
                    if (timeoutTask == null) break block23;
                }
                catch (Throwable throwable) {
                    Object var13_14 = null;
                    this.statementExecuting.set(false);
                    if (timeoutTask != null) {
                        timeoutTask.cancel();
                        locallyScopedConn.getCancelTimer().purge();
                    }
                    if (oldCatalog != null) {
                        locallyScopedConn.setCatalog(oldCatalog);
                    }
                    throw throwable;
                }
                timeoutTask.cancel();
                locallyScopedConn.getCancelTimer().purge();
            }
            if (oldCatalog != null) {
                locallyScopedConn.setCatalog(oldCatalog);
            }
            this.lastInsertId = this.results.getUpdateID();
            if (cachedMetaData != null) {
                locallyScopedConn.initializeResultsMetadataFromCache(sql, cachedMetaData, this.results);
            } else if (this.connection.getCacheResultSetMetadata()) {
                locallyScopedConn.initializeResultsMetadataFromCache(sql, null, this.results);
            }
            return this.results;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void doPingInstead() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods fakeSelectOneResultSet;
            if (this.pingTarget != null) {
                this.pingTarget.doPing();
            } else {
                this.connection.ping();
            }
            this.results = fakeSelectOneResultSet = this.generatePingResultSet();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected ResultSetInternalMethods generatePingResultSet() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            Field[] fields = new Field[]{new Field(null, "1", -5, 1)};
            ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
            byte[] colVal = new byte[]{49};
            rows.add(new ByteArrayRow(new byte[][]{colVal}, this.getExceptionInterceptor()));
            return (ResultSetInternalMethods)DatabaseMetaData.buildResultSet(fields, rows, this.connection);
        }
    }

    protected void executeSimpleNonQuery(MySQLConnection c, String nonQuery) throws SQLException {
        c.execSQL(this, nonQuery, -1, null, 1003, 1007, false, this.currentCatalog, null, false).close();
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected long executeUpdateInternal(String sql, boolean isBatch, boolean returnGeneratedKeys) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            String oldCatalog;
            ResultSetInternalMethods rs;
            char firstStatementChar;
            MySQLConnection locallyScopedConn;
            block22: {
                locallyScopedConn = this.connection;
                this.checkNullOrEmptyQuery(sql);
                this.resetCancelledState();
                firstStatementChar = StringUtils.firstAlphaCharUc(sql, StatementImpl.findStartOfStatement(sql));
                this.retrieveGeneratedKeys = returnGeneratedKeys;
                this.lastQueryIsOnDupKeyUpdate = returnGeneratedKeys && firstStatementChar == 'I' && this.containsOnDuplicateKeyInString(sql);
                rs = null;
                if (this.doEscapeProcessing) {
                    Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, this.connection.serverSupportsConvertFn(), this.connection);
                    sql = escapedSqlResult instanceof String ? (String)escapedSqlResult : ((EscapeProcessorResult)escapedSqlResult).escapedSql;
                }
                if (locallyScopedConn.isReadOnly(false)) {
                    throw SQLError.createSQLException(Messages.getString("Statement.42") + Messages.getString("Statement.43"), "S1009", this.getExceptionInterceptor());
                }
                if (StringUtils.startsWithIgnoreCaseAndWs(sql, "select")) {
                    throw SQLError.createSQLException(Messages.getString("Statement.46"), "01S03", this.getExceptionInterceptor());
                }
                this.implicitlyCloseAllOpenResults();
                TimerTask timeoutTask = null;
                oldCatalog = null;
                boolean readInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
                if (returnGeneratedKeys && firstStatementChar == 'R') {
                    locallyScopedConn.setReadInfoMsgEnabled(true);
                }
                try {
                    if (locallyScopedConn.getEnableQueryTimeouts() && this.timeoutInMillis != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                        timeoutTask = new CancelTask(this);
                        locallyScopedConn.getCancelTimer().schedule(timeoutTask, this.timeoutInMillis);
                    }
                    if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
                        oldCatalog = locallyScopedConn.getCatalog();
                        locallyScopedConn.setCatalog(this.currentCatalog);
                    }
                    locallyScopedConn.setSessionMaxRows(-1);
                    this.statementBegins();
                    rs = locallyScopedConn.execSQL(this, sql, -1, null, 1003, 1007, false, this.currentCatalog, null, isBatch);
                    if (timeoutTask != null) {
                        if (((CancelTask)timeoutTask).caughtWhileCancelling != null) {
                            throw ((CancelTask)timeoutTask).caughtWhileCancelling;
                        }
                        timeoutTask.cancel();
                        locallyScopedConn.getCancelTimer().purge();
                        timeoutTask = null;
                    }
                    Object object2 = this.cancelTimeoutMutex;
                    synchronized (object2) {
                        if (this.wasCancelled) {
                            SQLException cause = null;
                            cause = this.wasCancelledByTimeout ? new MySQLTimeoutException() : new MySQLStatementCancelledException();
                            this.resetCancelledState();
                            throw cause;
                        }
                    }
                    Object var15_14 = null;
                    locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
                    if (timeoutTask == null) break block22;
                }
                catch (Throwable throwable) {
                    Object var15_15 = null;
                    locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
                    if (timeoutTask != null) {
                        timeoutTask.cancel();
                        locallyScopedConn.getCancelTimer().purge();
                    }
                    if (oldCatalog != null) {
                        locallyScopedConn.setCatalog(oldCatalog);
                    }
                    if (!isBatch) {
                        this.statementExecuting.set(false);
                    }
                    throw throwable;
                }
                timeoutTask.cancel();
                locallyScopedConn.getCancelTimer().purge();
            }
            if (oldCatalog != null) {
                locallyScopedConn.setCatalog(oldCatalog);
            }
            if (!isBatch) {
                this.statementExecuting.set(false);
            }
            this.results = rs;
            rs.setFirstCharOfQuery(firstStatementChar);
            this.updateCount = rs.getUpdateCount();
            this.lastInsertId = rs.getUpdateID();
            return this.updateCount;
        }
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql, autoGeneratedKeys));
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql, columnIndexes));
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql, columnNames));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Calendar getCalendarInstanceForSessionOrNew() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.connection != null) {
                return this.connection.getCalendarInstanceForSessionOrNew();
            }
            return new GregorianCalendar();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public java.sql.Connection getConnection() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.connection;
        }
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 1000;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getFetchSize() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.fetchSize;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (!this.retrieveGeneratedKeys) {
                throw SQLError.createSQLException(Messages.getString("Statement.GeneratedKeysNotRequested"), "S1009", this.getExceptionInterceptor());
            }
            if (this.batchedGeneratedKeys == null) {
                if (this.lastQueryIsOnDupKeyUpdate) {
                    this.generatedKeysResults = this.getGeneratedKeysInternal(1L);
                    return this.generatedKeysResults;
                }
                this.generatedKeysResults = this.getGeneratedKeysInternal();
                return this.generatedKeysResults;
            }
            Field[] fields = new Field[]{new Field("", "GENERATED_KEY", -5, 20)};
            fields[0].setConnection(this.connection);
            this.generatedKeysResults = ResultSetImpl.getInstance(this.currentCatalog, fields, new RowDataStatic(this.batchedGeneratedKeys), this.connection, this, false);
            return this.generatedKeysResults;
        }
    }

    protected ResultSetInternalMethods getGeneratedKeysInternal() throws SQLException {
        long numKeys = this.getLargeUpdateCount();
        return this.getGeneratedKeysInternal(numKeys);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected ResultSetInternalMethods getGeneratedKeysInternal(long numKeys) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            Field[] fields = new Field[]{new Field("", "GENERATED_KEY", -5, 20)};
            fields[0].setConnection(this.connection);
            fields[0].setUseOldNameMetadata(true);
            ArrayList<ResultSetRow> rowSet = new ArrayList<ResultSetRow>();
            long beginAt = this.getLastInsertID();
            if (beginAt < 0L) {
                fields[0].setUnsigned();
            }
            if (this.results != null) {
                String serverInfo = this.results.getServerInfo();
                if (numKeys > 0L && this.results.getFirstCharOfQuery() == 'R' && serverInfo != null && serverInfo.length() > 0) {
                    numKeys = this.getRecordCountFromInfo(serverInfo);
                }
                if (beginAt != 0L && numKeys > 0L) {
                    int i = 0;
                    while ((long)i < numKeys) {
                        byte[][] row = new byte[1][];
                        if (beginAt > 0L) {
                            row[0] = StringUtils.getBytes(Long.toString(beginAt));
                        } else {
                            byte[] asBytes = new byte[8];
                            asBytes[7] = (byte)(beginAt & 0xFFL);
                            asBytes[6] = (byte)(beginAt >>> 8);
                            asBytes[5] = (byte)(beginAt >>> 16);
                            asBytes[4] = (byte)(beginAt >>> 24);
                            asBytes[3] = (byte)(beginAt >>> 32);
                            asBytes[2] = (byte)(beginAt >>> 40);
                            asBytes[1] = (byte)(beginAt >>> 48);
                            asBytes[0] = (byte)(beginAt >>> 56);
                            BigInteger val = new BigInteger(1, asBytes);
                            row[0] = val.toString().getBytes();
                        }
                        rowSet.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
                        beginAt += (long)this.connection.getAutoIncrementIncrement();
                        ++i;
                    }
                }
            }
            ResultSetImpl gkRs = ResultSetImpl.getInstance(this.currentCatalog, fields, new RowDataStatic(rowSet), this.connection, this, false);
            return gkRs;
        }
    }

    protected int getId() {
        return this.statementId;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long getLastInsertID() {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                return this.lastInsertId;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long getLongUpdateCount() {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                if (this.results == null) {
                    return -1L;
                }
                if (this.results.reallyResult()) {
                    return -1L;
                }
                return this.updateCount;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getMaxFieldSize() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.maxFieldSize;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getMaxRows() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.maxRows <= 0) {
                return 0;
            }
            return this.maxRows;
        }
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return this.getMoreResults(1);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean getMoreResults(int current) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            boolean moreResults;
            if (this.results == null) {
                return false;
            }
            boolean streamingMode = this.createStreamingResultSet();
            if (streamingMode && this.results.reallyResult()) {
                while (this.results.next()) {
                }
            }
            ResultSetInternalMethods nextResultSet = this.results.getNextResultSet();
            switch (current) {
                case 1: {
                    if (this.results == null) break;
                    if (!streamingMode && !this.connection.getDontTrackOpenResources()) {
                        this.results.realClose(false);
                    }
                    this.results.clearNextResult();
                    break;
                }
                case 3: {
                    if (this.results != null) {
                        if (!streamingMode && !this.connection.getDontTrackOpenResources()) {
                            this.results.realClose(false);
                        }
                        this.results.clearNextResult();
                    }
                    this.closeAllOpenResults();
                    break;
                }
                case 2: {
                    if (!this.connection.getDontTrackOpenResources()) {
                        this.openResults.add(this.results);
                    }
                    this.results.clearNextResult();
                    break;
                }
                default: {
                    throw SQLError.createSQLException(Messages.getString("Statement.19"), "S1009", this.getExceptionInterceptor());
                }
            }
            this.results = nextResultSet;
            if (this.results == null) {
                this.updateCount = -1L;
                this.lastInsertId = -1L;
            } else if (this.results.reallyResult()) {
                this.updateCount = -1L;
                this.lastInsertId = -1L;
            } else {
                this.updateCount = this.results.getUpdateCount();
                this.lastInsertId = this.results.getUpdateID();
            }
            boolean bl = moreResults = this.results != null && this.results.reallyResult();
            if (!moreResults) {
                this.checkAndPerformCloseOnCompletionAction();
            }
            return moreResults;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getQueryTimeout() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.timeoutInMillis / 1000;
        }
    }

    private long getRecordCountFromInfo(String serverInfo) {
        int i;
        StringBuilder recordsBuf = new StringBuilder();
        long recordsCount = 0L;
        long duplicatesCount = 0L;
        char c = '\u0000';
        int length = serverInfo.length();
        for (i = 0; i < length && !Character.isDigit(c = serverInfo.charAt(i)); ++i) {
        }
        recordsBuf.append(c);
        ++i;
        while (i < length && Character.isDigit(c = serverInfo.charAt(i))) {
            recordsBuf.append(c);
            ++i;
        }
        recordsCount = Long.parseLong(recordsBuf.toString());
        StringBuilder duplicatesBuf = new StringBuilder();
        while (i < length && !Character.isDigit(c = serverInfo.charAt(i))) {
            ++i;
        }
        duplicatesBuf.append(c);
        ++i;
        while (i < length && Character.isDigit(c = serverInfo.charAt(i))) {
            duplicatesBuf.append(c);
            ++i;
        }
        duplicatesCount = Long.parseLong(duplicatesBuf.toString());
        return recordsCount - duplicatesCount;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ResultSet getResultSet() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.results != null && this.results.reallyResult() ? this.results : null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getResultSetConcurrency() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.resultSetConcurrency;
        }
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return 1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected ResultSetInternalMethods getResultSetInternal() {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                return this.results;
            }
        }
        catch (SQLException e) {
            return this.results;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getResultSetType() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.resultSetType;
        }
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return Util.truncateAndConvertToInt(this.getLargeUpdateCount());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public SQLWarning getWarnings() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.clearWarningsCalled) {
                return null;
            }
            if (this.connection.versionMeetsMinimum(4, 1, 0)) {
                SQLWarning pendingWarningsFromServer = SQLError.convertShowWarningsToSQLWarnings(this.connection);
                if (this.warningChain != null) {
                    this.warningChain.setNextWarning(pendingWarningsFromServer);
                } else {
                    this.warningChain = pendingWarningsFromServer;
                }
                return this.warningChain;
            }
            return this.warningChain;
        }
    }

    protected void realClose(boolean calledExplicitly, boolean closeOpenResults) throws SQLException {
        MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn == null || this.isClosed) {
            return;
        }
        if (!locallyScopedConn.getDontTrackOpenResources()) {
            locallyScopedConn.unregisterStatement(this);
        }
        if (this.useUsageAdvisor && !calledExplicitly) {
            String message = Messages.getString("Statement.63") + Messages.getString("Statement.64");
            this.eventSink.consumeEvent(new ProfilerEvent(0, "", this.currentCatalog, this.connectionId, this.getId(), -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, message));
        }
        if (closeOpenResults) {
            boolean bl = closeOpenResults = !this.holdResultsOpenOverClose && !this.connection.getDontTrackOpenResources();
        }
        if (closeOpenResults) {
            if (this.results != null) {
                try {
                    this.results.close();
                }
                catch (Exception ex) {
                    // empty catch block
                }
            }
            if (this.generatedKeysResults != null) {
                try {
                    this.generatedKeysResults.close();
                }
                catch (Exception ex) {
                    // empty catch block
                }
            }
            this.closeAllOpenResults();
        }
        this.isClosed = true;
        this.results = null;
        this.generatedKeysResults = null;
        this.connection = null;
        this.warningChain = null;
        this.openResults = null;
        this.batchedGeneratedKeys = null;
        this.localInfileInputStream = null;
        this.pingTarget = null;
    }

    @Override
    public void setCursorName(String name) throws SQLException {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.doEscapeProcessing = enable;
        }
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        switch (direction) {
            case 1000: 
            case 1001: 
            case 1002: {
                break;
            }
            default: {
                throw SQLError.createSQLException(Messages.getString("Statement.5"), "S1009", this.getExceptionInterceptor());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setFetchSize(int rows) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (rows < 0 && rows != Integer.MIN_VALUE || this.maxRows > 0 && rows > this.getMaxRows()) {
                throw SQLError.createSQLException(Messages.getString("Statement.7"), "S1009", this.getExceptionInterceptor());
            }
            this.fetchSize = rows;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setHoldResultsOpenOverClose(boolean holdResultsOpenOverClose) {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                this.holdResultsOpenOverClose = holdResultsOpenOverClose;
            }
        }
        catch (SQLException sQLException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            int maxBuf;
            if (max < 0) {
                throw SQLError.createSQLException(Messages.getString("Statement.11"), "S1009", this.getExceptionInterceptor());
            }
            int n = maxBuf = this.connection != null ? this.connection.getMaxAllowedPacket() : MysqlIO.getMaxBuf();
            if (max > maxBuf) {
                throw SQLError.createSQLException(Messages.getString("Statement.13", new Object[]{(long)maxBuf}), "S1009", this.getExceptionInterceptor());
            }
            this.maxFieldSize = max;
        }
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        this.setLargeMaxRows(max);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (seconds < 0) {
                throw SQLError.createSQLException(Messages.getString("Statement.21"), "S1009", this.getExceptionInterceptor());
            }
            this.timeoutInMillis = seconds * 1000;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void setResultSetConcurrency(int concurrencyFlag) {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                this.resultSetConcurrency = concurrencyFlag;
            }
        }
        catch (SQLException sQLException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void setResultSetType(int typeFlag) {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                this.resultSetType = typeFlag;
            }
        }
        catch (SQLException sQLException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected void getBatchedGeneratedKeys(java.sql.Statement batchedStatement) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (!this.retrieveGeneratedKeys) return;
            ResultSet rs = null;
            try {
                rs = batchedStatement.getGeneratedKeys();
                while (rs.next()) {
                    this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][]{rs.getBytes(1)}, this.getExceptionInterceptor()));
                }
                Object var5_4 = null;
                if (rs == null) return;
            }
            catch (Throwable throwable) {
                Object var5_5 = null;
                if (rs == null) throw throwable;
                rs.close();
                throw throwable;
            }
            rs.close();
            {
            }
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void getBatchedGeneratedKeys(int maxKeys) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.retrieveGeneratedKeys) {
                Object v1;
                ResultSetInternalMethods rs = null;
                try {
                    rs = maxKeys == 0 ? this.getGeneratedKeysInternal() : this.getGeneratedKeysInternal(maxKeys);
                    while (rs.next()) {
                        this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][]{rs.getBytes(1)}, this.getExceptionInterceptor()));
                    }
                    Object var5_4 = null;
                    this.isImplicitlyClosingResults = true;
                }
                catch (Throwable throwable) {
                    Object v0;
                    Object var5_5 = null;
                    this.isImplicitlyClosingResults = true;
                    try {
                        if (rs != null) {
                            rs.close();
                        }
                        v0 = null;
                    }
                    catch (Throwable throwable2) {
                        v0 = null;
                    }
                    Object var7_9 = v0;
                    this.isImplicitlyClosingResults = false;
                    throw throwable;
                }
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    v1 = null;
                }
                catch (Throwable throwable) {
                    v1 = null;
                }
                Object var7_8 = v1;
                this.isImplicitlyClosingResults = false;
                {
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean useServerFetch() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.connection.isCursorFetchEnabled() && this.fetchSize > 0 && this.resultSetConcurrency == 1007 && this.resultSetType == 1003;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isClosed() throws SQLException {
        MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn == null) {
            return true;
        }
        Object object = locallyScopedConn.getConnectionMutex();
        synchronized (object) {
            return this.isClosed;
        }
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return this.isPoolable;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        this.isPoolable = poolable;
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

    protected static int findStartOfStatement(String sql) {
        int statementStartPos = 0;
        if (StringUtils.startsWithIgnoreCaseAndWs(sql, "/*")) {
            statementStartPos = sql.indexOf("*/");
            statementStartPos = statementStartPos == -1 ? 0 : (statementStartPos += 2);
        } else if ((StringUtils.startsWithIgnoreCaseAndWs(sql, "--") || StringUtils.startsWithIgnoreCaseAndWs(sql, "#")) && (statementStartPos = sql.indexOf(10)) == -1 && (statementStartPos = sql.indexOf(13)) == -1) {
            statementStartPos = 0;
        }
        return statementStartPos;
    }

    @Override
    public InputStream getLocalInfileInputStream() {
        return this.localInfileInputStream;
    }

    @Override
    public void setLocalInfileInputStream(InputStream stream) {
        this.localInfileInputStream = stream;
    }

    @Override
    public void setPingTarget(PingTarget pingTarget) {
        this.pingTarget = pingTarget;
    }

    @Override
    public ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }

    protected boolean containsOnDuplicateKeyInString(String sql) {
        return StatementImpl.getOnDuplicateKeyLocation(sql, this.connection.getDontCheckOnDuplicateKeyUpdateInSQL(), this.connection.getRewriteBatchedStatements(), this.connection.isNoBackslashEscapesSet()) != -1;
    }

    protected static int getOnDuplicateKeyLocation(String sql, boolean dontCheckOnDuplicateKeyUpdateInSQL, boolean rewriteBatchedStatements, boolean noBackslashEscapes) {
        return dontCheckOnDuplicateKeyUpdateInSQL && !rewriteBatchedStatements ? -1 : StringUtils.indexOfIgnoreCase(0, sql, ON_DUPLICATE_KEY_UPDATE_CLAUSE, "\"'`", "\"'`", noBackslashEscapes ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void closeOnCompletion() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.closeOnCompletion = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.closeOnCompletion;
        }
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        return this.executeBatchInternal();
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        return this.executeUpdateInternal(sql, false, false);
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return this.executeUpdateInternal(sql, false, autoGeneratedKeys == 1);
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return this.executeUpdateInternal(sql, false, columnIndexes != null && columnIndexes.length > 0);
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        return this.executeUpdateInternal(sql, false, columnNames != null && columnNames.length > 0);
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        return this.getMaxRows();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long getLargeUpdateCount() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.results == null) {
                return -1L;
            }
            if (this.results.reallyResult()) {
                return -1L;
            }
            return this.results.getUpdateCount();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (max > 50000000L || max < 0L) {
                throw SQLError.createSQLException(Messages.getString("Statement.15") + max + " > " + 50000000 + ".", "S1009", this.getExceptionInterceptor());
            }
            if (max == 0L) {
                max = -1L;
            }
            this.maxRows = (int)max;
        }
    }

    boolean isCursorRequired() throws SQLException {
        return false;
    }

    class CancelTask
    extends TimerTask {
        long connectionId = 0L;
        SQLException caughtWhileCancelling = null;
        StatementImpl toCancel;
        Properties origConnProps = null;
        String origConnURL = "";

        CancelTask(StatementImpl cancellee) throws SQLException {
            this.connectionId = cancellee.connectionId;
            this.toCancel = cancellee;
            this.origConnProps = new Properties();
            Properties props = StatementImpl.this.connection.getProperties();
            Enumeration<?> keys = props.propertyNames();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                this.origConnProps.setProperty(key, props.getProperty(key));
            }
            this.origConnURL = StatementImpl.this.connection.getURL();
        }

        public void run() {
            Thread cancelThread = new Thread(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 * Enabled aggressive block sorting
                 * Enabled unnecessary exception pruning
                 * Enabled aggressive exception aggregation
                 */
                public void run() {
                    java.sql.Connection cancelConn;
                    block35: {
                        cancelConn = null;
                        java.sql.Statement cancelStmt = null;
                        try {
                            block30: {
                                try {
                                    if (StatementImpl.this.connection.getQueryTimeoutKillsConnection()) {
                                        CancelTask.this.toCancel.wasCancelled = true;
                                        CancelTask.this.toCancel.wasCancelledByTimeout = true;
                                        StatementImpl.this.connection.realClose(false, false, true, new MySQLStatementCancelledException(Messages.getString("Statement.ConnectionKilledDueToTimeout")));
                                        break block30;
                                    }
                                    Object object = StatementImpl.this.cancelTimeoutMutex;
                                    synchronized (object) {
                                        if (CancelTask.this.origConnURL.equals(StatementImpl.this.connection.getURL())) {
                                            cancelConn = StatementImpl.this.connection.duplicate();
                                            cancelStmt = cancelConn.createStatement();
                                            cancelStmt.execute("KILL QUERY " + CancelTask.this.connectionId);
                                        } else {
                                            try {
                                                cancelConn = (Connection)DriverManager.getConnection(CancelTask.this.origConnURL, CancelTask.this.origConnProps);
                                                cancelStmt = cancelConn.createStatement();
                                                cancelStmt.execute("KILL QUERY " + CancelTask.this.connectionId);
                                            }
                                            catch (NullPointerException npe) {
                                                // empty catch block
                                            }
                                        }
                                        CancelTask.this.toCancel.wasCancelled = true;
                                        CancelTask.this.toCancel.wasCancelledByTimeout = true;
                                    }
                                }
                                catch (SQLException sqlEx) {
                                    CancelTask.this.caughtWhileCancelling = sqlEx;
                                    Object var7_8 = null;
                                    if (cancelStmt != null) {
                                        try {
                                            cancelStmt.close();
                                        }
                                        catch (SQLException sqlEx2) {
                                            throw new RuntimeException(sqlEx2.toString());
                                        }
                                    }
                                    if (cancelConn != null) {
                                        try {
                                            cancelConn.close();
                                        }
                                        catch (SQLException sqlEx3) {
                                            throw new RuntimeException(sqlEx3.toString());
                                        }
                                    }
                                    CancelTask.this.toCancel = null;
                                    CancelTask.this.origConnProps = null;
                                    CancelTask.this.origConnURL = null;
                                    return;
                                }
                                catch (NullPointerException nullPointerException) {
                                    Object var7_9 = null;
                                    if (cancelStmt != null) {
                                        try {}
                                        catch (SQLException sqlEx2) {
                                            throw new RuntimeException(sqlEx2.toString());
                                        }
                                        cancelStmt.close();
                                    }
                                    if (cancelConn != null) {
                                        try {}
                                        catch (SQLException sqlEx3) {
                                            throw new RuntimeException(sqlEx3.toString());
                                        }
                                        cancelConn.close();
                                    }
                                    CancelTask.this.toCancel = null;
                                    CancelTask.this.origConnProps = null;
                                    CancelTask.this.origConnURL = null;
                                    return;
                                }
                            }
                            Object var7_7 = null;
                            if (cancelStmt == null) break block35;
                        }
                        catch (Throwable throwable) {
                            Object var7_10 = null;
                            if (cancelStmt != null) {
                                try {}
                                catch (SQLException sqlEx2) {
                                    throw new RuntimeException(sqlEx2.toString());
                                }
                                cancelStmt.close();
                            }
                            if (cancelConn != null) {
                                try {}
                                catch (SQLException sqlEx3) {
                                    throw new RuntimeException(sqlEx3.toString());
                                }
                                cancelConn.close();
                            }
                            CancelTask.this.toCancel = null;
                            CancelTask.this.origConnProps = null;
                            CancelTask.this.origConnURL = null;
                            throw throwable;
                        }
                        try {}
                        catch (SQLException sqlEx2) {
                            throw new RuntimeException(sqlEx2.toString());
                        }
                        cancelStmt.close();
                    }
                    if (cancelConn != null) {
                        try {}
                        catch (SQLException sqlEx3) {
                            throw new RuntimeException(sqlEx3.toString());
                        }
                        cancelConn.close();
                    }
                    CancelTask.this.toCancel = null;
                    CancelTask.this.origConnProps = null;
                    CancelTask.this.origConnURL = null;
                }
            };
            cancelThread.start();
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Buffer;
import com.mysql.jdbc.ByteArrayRow;
import com.mysql.jdbc.CachedResultSetMetaData;
import com.mysql.jdbc.CharsetMapping;
import com.mysql.jdbc.Constants;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.MysqlParameterMetadata;
import com.mysql.jdbc.ParameterBindings;
import com.mysql.jdbc.ResultSetImpl;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.ResultSetRow;
import com.mysql.jdbc.RowDataStatic;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.SingleByteCharsetConverter;
import com.mysql.jdbc.StatementImpl;
import com.mysql.jdbc.StringUtils;
import com.mysql.jdbc.TimeUtil;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.Wrapper;
import com.mysql.jdbc.exceptions.MySQLStatementCancelledException;
import com.mysql.jdbc.exceptions.MySQLTimeoutException;
import com.mysql.jdbc.profiler.ProfilerEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TimerTask;

public class PreparedStatement
extends StatementImpl
implements java.sql.PreparedStatement {
    private static final Constructor<?> JDBC_4_PSTMT_2_ARG_CTOR;
    private static final Constructor<?> JDBC_4_PSTMT_3_ARG_CTOR;
    private static final Constructor<?> JDBC_4_PSTMT_4_ARG_CTOR;
    private static final byte[] HEX_DIGITS;
    protected boolean batchHasPlainStatements = false;
    private DatabaseMetaData dbmd = null;
    protected char firstCharOfStmt = '\u0000';
    protected boolean isLoadDataQuery = false;
    protected boolean[] isNull = null;
    private boolean[] isStream = null;
    protected int numberOfExecutions = 0;
    protected String originalSql = null;
    protected int parameterCount;
    protected MysqlParameterMetadata parameterMetaData;
    private InputStream[] parameterStreams = null;
    private byte[][] parameterValues = null;
    protected int[] parameterTypes = null;
    protected ParseInfo parseInfo;
    private java.sql.ResultSetMetaData pstmtResultMetaData;
    private byte[][] staticSqlStrings = null;
    private byte[] streamConvertBuf = null;
    private int[] streamLengths = null;
    private SimpleDateFormat tsdf = null;
    private SimpleDateFormat ddf;
    private SimpleDateFormat tdf;
    protected boolean useTrueBoolean = false;
    protected boolean usingAnsiMode;
    protected String batchedValuesClause;
    private boolean doPingInstead;
    private boolean compensateForOnDuplicateKeyUpdate = false;
    private CharsetEncoder charsetEncoder;
    protected int batchCommandIndex = -1;
    protected boolean serverSupportsFracSecs;
    protected int rewrittenBatchSize = 0;

    protected static int readFully(Reader reader, char[] buf, int length) throws IOException {
        int numCharsRead;
        int count;
        for (numCharsRead = 0; numCharsRead < length && (count = reader.read(buf, numCharsRead, length - numCharsRead)) >= 0; numCharsRead += count) {
        }
        return numCharsRead;
    }

    protected static PreparedStatement getInstance(MySQLConnection conn, String catalog) throws SQLException {
        if (!Util.isJdbc4()) {
            return new PreparedStatement(conn, catalog);
        }
        return (PreparedStatement)Util.handleNewInstance(JDBC_4_PSTMT_2_ARG_CTOR, new Object[]{conn, catalog}, conn.getExceptionInterceptor());
    }

    protected static PreparedStatement getInstance(MySQLConnection conn, String sql, String catalog) throws SQLException {
        if (!Util.isJdbc4()) {
            return new PreparedStatement(conn, sql, catalog);
        }
        return (PreparedStatement)Util.handleNewInstance(JDBC_4_PSTMT_3_ARG_CTOR, new Object[]{conn, sql, catalog}, conn.getExceptionInterceptor());
    }

    protected static PreparedStatement getInstance(MySQLConnection conn, String sql, String catalog, ParseInfo cachedParseInfo) throws SQLException {
        if (!Util.isJdbc4()) {
            return new PreparedStatement(conn, sql, catalog, cachedParseInfo);
        }
        return (PreparedStatement)Util.handleNewInstance(JDBC_4_PSTMT_4_ARG_CTOR, new Object[]{conn, sql, catalog, cachedParseInfo}, conn.getExceptionInterceptor());
    }

    public PreparedStatement(MySQLConnection conn, String catalog) throws SQLException {
        super(conn, catalog);
        this.detectFractionalSecondsSupport();
        this.compensateForOnDuplicateKeyUpdate = this.connection.getCompensateOnDuplicateKeyUpdateCounts();
    }

    protected void detectFractionalSecondsSupport() throws SQLException {
        this.serverSupportsFracSecs = this.connection != null && this.connection.versionMeetsMinimum(5, 6, 4);
    }

    public PreparedStatement(MySQLConnection conn, String sql, String catalog) throws SQLException {
        super(conn, catalog);
        if (sql == null) {
            throw SQLError.createSQLException(Messages.getString("PreparedStatement.0"), "S1009", this.getExceptionInterceptor());
        }
        this.detectFractionalSecondsSupport();
        this.originalSql = sql;
        this.doPingInstead = this.originalSql.startsWith("/* ping */");
        this.dbmd = this.connection.getMetaData();
        this.useTrueBoolean = this.connection.versionMeetsMinimum(3, 21, 23);
        this.parseInfo = new ParseInfo(sql, this.connection, this.dbmd, this.charEncoding, this.charConverter);
        this.initializeFromParseInfo();
        this.compensateForOnDuplicateKeyUpdate = this.connection.getCompensateOnDuplicateKeyUpdateCounts();
        if (conn.getRequiresEscapingEncoder()) {
            this.charsetEncoder = Charset.forName(conn.getEncoding()).newEncoder();
        }
    }

    public PreparedStatement(MySQLConnection conn, String sql, String catalog, ParseInfo cachedParseInfo) throws SQLException {
        super(conn, catalog);
        if (sql == null) {
            throw SQLError.createSQLException(Messages.getString("PreparedStatement.1"), "S1009", this.getExceptionInterceptor());
        }
        this.detectFractionalSecondsSupport();
        this.originalSql = sql;
        this.dbmd = this.connection.getMetaData();
        this.useTrueBoolean = this.connection.versionMeetsMinimum(3, 21, 23);
        this.parseInfo = cachedParseInfo;
        this.usingAnsiMode = !this.connection.useAnsiQuotedIdentifiers();
        this.initializeFromParseInfo();
        this.compensateForOnDuplicateKeyUpdate = this.connection.getCompensateOnDuplicateKeyUpdateCounts();
        if (conn.getRequiresEscapingEncoder()) {
            this.charsetEncoder = Charset.forName(conn.getEncoding()).newEncoder();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addBatch() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.batchedArgs == null) {
                this.batchedArgs = new ArrayList();
            }
            for (int i = 0; i < this.parameterValues.length; ++i) {
                this.checkAllParametersSet(this.parameterValues[i], this.parameterStreams[i], i);
            }
            this.batchedArgs.add(new BatchParams(this.parameterValues, this.parameterStreams, this.isStream, this.streamLengths, this.isNull));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addBatch(String sql) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.batchHasPlainStatements = true;
            super.addBatch(sql);
        }
    }

    public String asSql() throws SQLException {
        return this.asSql(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String asSql(boolean quoteStreamsAndUnknowns) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            StringBuilder buf = new StringBuilder();
            try {
                int realParameterCount = this.parameterCount + this.getParameterIndexOffset();
                Object batchArg = null;
                if (this.batchCommandIndex != -1) {
                    batchArg = this.batchedArgs.get(this.batchCommandIndex);
                }
                for (int i = 0; i < realParameterCount; ++i) {
                    if (this.charEncoding != null) {
                        buf.append(StringUtils.toString(this.staticSqlStrings[i], this.charEncoding));
                    } else {
                        buf.append(StringUtils.toString(this.staticSqlStrings[i]));
                    }
                    byte[] val = null;
                    if (batchArg != null && batchArg instanceof String) {
                        buf.append((String)batchArg);
                        continue;
                    }
                    val = this.batchCommandIndex == -1 ? this.parameterValues[i] : ((BatchParams)batchArg).parameterStrings[i];
                    boolean isStreamParam = false;
                    isStreamParam = this.batchCommandIndex == -1 ? this.isStream[i] : ((BatchParams)batchArg).isStream[i];
                    if (val == null && !isStreamParam) {
                        if (quoteStreamsAndUnknowns) {
                            buf.append("'");
                        }
                        buf.append("** NOT SPECIFIED **");
                        if (!quoteStreamsAndUnknowns) continue;
                        buf.append("'");
                        continue;
                    }
                    if (isStreamParam) {
                        if (quoteStreamsAndUnknowns) {
                            buf.append("'");
                        }
                        buf.append("** STREAM DATA **");
                        if (!quoteStreamsAndUnknowns) continue;
                        buf.append("'");
                        continue;
                    }
                    if (this.charConverter != null) {
                        buf.append(this.charConverter.toString(val));
                        continue;
                    }
                    if (this.charEncoding != null) {
                        buf.append(new String(val, this.charEncoding));
                        continue;
                    }
                    buf.append(StringUtils.toAsciiString(val));
                }
                if (this.charEncoding != null) {
                    buf.append(StringUtils.toString(this.staticSqlStrings[this.parameterCount + this.getParameterIndexOffset()], this.charEncoding));
                } else {
                    buf.append(StringUtils.toAsciiString(this.staticSqlStrings[this.parameterCount + this.getParameterIndexOffset()]));
                }
            }
            catch (UnsupportedEncodingException uue) {
                throw new RuntimeException(Messages.getString("PreparedStatement.32") + this.charEncoding + Messages.getString("PreparedStatement.33"));
            }
            return buf.toString();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void clearBatch() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.batchHasPlainStatements = false;
            super.clearBatch();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void clearParameters() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            for (int i = 0; i < this.parameterValues.length; ++i) {
                this.parameterValues[i] = null;
                this.parameterStreams[i] = null;
                this.isStream[i] = false;
                this.isNull[i] = false;
                this.parameterTypes[i] = 0;
            }
        }
    }

    private final void escapeblockFast(byte[] buf, Buffer packet, int size) throws SQLException {
        int lastwritten = 0;
        for (int i = 0; i < size; ++i) {
            byte b = buf[i];
            if (b == 0) {
                if (i > lastwritten) {
                    packet.writeBytesNoNull(buf, lastwritten, i - lastwritten);
                }
                packet.writeByte((byte)92);
                packet.writeByte((byte)48);
                lastwritten = i + 1;
                continue;
            }
            if (b != 92 && b != 39 && (this.usingAnsiMode || b != 34)) continue;
            if (i > lastwritten) {
                packet.writeBytesNoNull(buf, lastwritten, i - lastwritten);
            }
            packet.writeByte((byte)92);
            lastwritten = i;
        }
        if (lastwritten < size) {
            packet.writeBytesNoNull(buf, lastwritten, size - lastwritten);
        }
    }

    private final void escapeblockFast(byte[] buf, ByteArrayOutputStream bytesOut, int size) {
        int lastwritten = 0;
        for (int i = 0; i < size; ++i) {
            byte b = buf[i];
            if (b == 0) {
                if (i > lastwritten) {
                    bytesOut.write(buf, lastwritten, i - lastwritten);
                }
                bytesOut.write(92);
                bytesOut.write(48);
                lastwritten = i + 1;
                continue;
            }
            if (b != 92 && b != 39 && (this.usingAnsiMode || b != 34)) continue;
            if (i > lastwritten) {
                bytesOut.write(buf, lastwritten, i - lastwritten);
            }
            bytesOut.write(92);
            lastwritten = i;
        }
        if (lastwritten < size) {
            bytesOut.write(buf, lastwritten, size - lastwritten);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected boolean checkReadOnlySafeStatement() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.firstCharOfStmt == 'S' || !this.connection.isReadOnly();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean execute() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            MySQLConnection locallyScopedConn = this.connection;
            if (!this.checkReadOnlySafeStatement()) {
                throw SQLError.createSQLException(Messages.getString("PreparedStatement.20") + Messages.getString("PreparedStatement.21"), "S1009", this.getExceptionInterceptor());
            }
            ResultSetInternalMethods rs = null;
            CachedResultSetMetaData cachedMetadata = null;
            this.lastQueryIsOnDupKeyUpdate = false;
            if (this.retrieveGeneratedKeys) {
                this.lastQueryIsOnDupKeyUpdate = this.containsOnDuplicateKeyUpdateInSQL();
            }
            this.clearWarnings();
            this.setupStreamingTimeout(locallyScopedConn);
            this.batchedGeneratedKeys = null;
            Buffer sendPacket = this.fillSendPacket();
            String oldCatalog = null;
            if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
                oldCatalog = locallyScopedConn.getCatalog();
                locallyScopedConn.setCatalog(this.currentCatalog);
            }
            if (locallyScopedConn.getCacheResultSetMetadata()) {
                cachedMetadata = locallyScopedConn.getCachedMetaData(this.originalSql);
            }
            Field[] metadataFromCache = null;
            if (cachedMetadata != null) {
                metadataFromCache = cachedMetadata.fields;
            }
            boolean oldInfoMsgState = false;
            if (this.retrieveGeneratedKeys) {
                oldInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
                locallyScopedConn.setReadInfoMsgEnabled(true);
            }
            locallyScopedConn.setSessionMaxRows(this.firstCharOfStmt == 'S' ? this.maxRows : -1);
            rs = this.executeInternal(this.maxRows, sendPacket, this.createStreamingResultSet(), this.firstCharOfStmt == 'S', metadataFromCache, false);
            if (cachedMetadata != null) {
                locallyScopedConn.initializeResultsMetadataFromCache(this.originalSql, cachedMetadata, rs);
            } else if (rs.reallyResult() && locallyScopedConn.getCacheResultSetMetadata()) {
                locallyScopedConn.initializeResultsMetadataFromCache(this.originalSql, null, rs);
            }
            if (this.retrieveGeneratedKeys) {
                locallyScopedConn.setReadInfoMsgEnabled(oldInfoMsgState);
                rs.setFirstCharOfQuery(this.firstCharOfStmt);
            }
            if (oldCatalog != null) {
                locallyScopedConn.setCatalog(oldCatalog);
            }
            if (rs != null) {
                this.lastInsertId = rs.getUpdateID();
                this.results = rs;
            }
            return rs != null && rs.reallyResult();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected long[] executeBatchInternal() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            long[] lArray;
            block12: {
                long[] lArray2;
                block11: {
                    long[] lArray3;
                    block10: {
                        if (this.connection.isReadOnly()) {
                            throw new SQLException(Messages.getString("PreparedStatement.25") + Messages.getString("PreparedStatement.26"), "S1009");
                        }
                        if (this.batchedArgs == null) return new long[0];
                        if (this.batchedArgs.size() == 0) {
                            return new long[0];
                        }
                        int batchTimeout = this.timeoutInMillis;
                        this.timeoutInMillis = 0;
                        this.resetCancelledState();
                        try {
                            this.statementBegins();
                            this.clearWarnings();
                            if (!this.batchHasPlainStatements && this.connection.getRewriteBatchedStatements()) {
                                if (this.canRewriteAsMultiValueInsertAtSqlLevel()) {
                                    lArray3 = this.executeBatchedInserts(batchTimeout);
                                    Object var5_6 = null;
                                    this.statementExecuting.set(false);
                                    break block10;
                                }
                                if (this.connection.versionMeetsMinimum(4, 1, 0) && !this.batchHasPlainStatements && this.batchedArgs != null && this.batchedArgs.size() > 3) {
                                    lArray2 = this.executePreparedBatchAsMultiStatement(batchTimeout);
                                    break block11;
                                }
                            }
                            lArray = this.executeBatchSerially(batchTimeout);
                            break block12;
                        }
                        catch (Throwable throwable) {
                            Object var5_9 = null;
                            this.statementExecuting.set(false);
                            this.clearBatch();
                            throw throwable;
                        }
                    }
                    this.clearBatch();
                    return lArray3;
                }
                Object var5_7 = null;
                this.statementExecuting.set(false);
                this.clearBatch();
                return lArray2;
            }
            Object var5_8 = null;
            this.statementExecuting.set(false);
            this.clearBatch();
            return lArray;
        }
    }

    public boolean canRewriteAsMultiValueInsertAtSqlLevel() throws SQLException {
        return this.parseInfo.canRewriteAsMultiValueInsert;
    }

    protected int getLocationOfOnDuplicateKeyUpdate() throws SQLException {
        return this.parseInfo.locationOfOnDuplicateKeyUpdate;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected long[] executePreparedBatchAsMultiStatement(int batchTimeout) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.batchedValuesClause == null) {
                this.batchedValuesClause = this.originalSql + ";";
            }
            MySQLConnection locallyScopedConn = this.connection;
            boolean multiQueriesEnabled = locallyScopedConn.getAllowMultiQueries();
            TimerTask timeoutTask = null;
            try {
                long[] lArray;
                block32: {
                    int numValuesPerBatch;
                    this.clearWarnings();
                    int numBatchedArgs = this.batchedArgs.size();
                    if (this.retrieveGeneratedKeys) {
                        this.batchedGeneratedKeys = new ArrayList(numBatchedArgs);
                    }
                    if (numBatchedArgs < (numValuesPerBatch = this.computeBatchSize(numBatchedArgs))) {
                        numValuesPerBatch = numBatchedArgs;
                    }
                    Statement batchedStatement = null;
                    int batchedParamIndex = 1;
                    int numberToExecuteAsMultiValue = 0;
                    int batchCounter = 0;
                    int updateCountCounter = 0;
                    long[] updateCounts = new long[numBatchedArgs];
                    SQLException sqlEx = null;
                    try {
                        if (!multiQueriesEnabled) {
                            locallyScopedConn.getIO().enableMultiQueries();
                        }
                        batchedStatement = this.retrieveGeneratedKeys ? ((Wrapper)((Object)locallyScopedConn.prepareStatement(this.generateMultiStatementForBatch(numValuesPerBatch), 1))).unwrap(java.sql.PreparedStatement.class) : ((Wrapper)((Object)locallyScopedConn.prepareStatement(this.generateMultiStatementForBatch(numValuesPerBatch)))).unwrap(java.sql.PreparedStatement.class);
                        if (locallyScopedConn.getEnableQueryTimeouts() && batchTimeout != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                            timeoutTask = new StatementImpl.CancelTask((StatementImpl)batchedStatement);
                            locallyScopedConn.getCancelTimer().schedule(timeoutTask, batchTimeout);
                        }
                        numberToExecuteAsMultiValue = numBatchedArgs < numValuesPerBatch ? numBatchedArgs : numBatchedArgs / numValuesPerBatch;
                        int numberArgsToExecute = numberToExecuteAsMultiValue * numValuesPerBatch;
                        for (int i = 0; i < numberArgsToExecute; ++i) {
                            if (i != 0 && i % numValuesPerBatch == 0) {
                                try {
                                    batchedStatement.execute();
                                }
                                catch (SQLException ex) {
                                    sqlEx = this.handleExceptionForBatch(batchCounter, numValuesPerBatch, updateCounts, ex);
                                }
                                updateCountCounter = this.processMultiCountsAndKeys((StatementImpl)batchedStatement, updateCountCounter, updateCounts);
                                batchedStatement.clearParameters();
                                batchedParamIndex = 1;
                            }
                            batchedParamIndex = this.setOneBatchedParameterSet((java.sql.PreparedStatement)batchedStatement, batchedParamIndex, this.batchedArgs.get(batchCounter++));
                        }
                        try {
                            batchedStatement.execute();
                        }
                        catch (SQLException ex) {
                            sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex);
                        }
                        updateCountCounter = this.processMultiCountsAndKeys((StatementImpl)batchedStatement, updateCountCounter, updateCounts);
                        batchedStatement.clearParameters();
                        numValuesPerBatch = numBatchedArgs - batchCounter;
                        Object var19_21 = null;
                        if (batchedStatement != null) {
                            batchedStatement.close();
                            batchedStatement = null;
                        }
                    }
                    catch (Throwable throwable) {
                        Object var19_22 = null;
                        if (batchedStatement == null) throw throwable;
                        batchedStatement.close();
                        batchedStatement = null;
                        throw throwable;
                    }
                    try {
                        if (numValuesPerBatch > 0) {
                            batchedStatement = this.retrieveGeneratedKeys ? locallyScopedConn.prepareStatement(this.generateMultiStatementForBatch(numValuesPerBatch), 1) : locallyScopedConn.prepareStatement(this.generateMultiStatementForBatch(numValuesPerBatch));
                            if (timeoutTask != null) {
                                ((StatementImpl.CancelTask)timeoutTask).toCancel = (StatementImpl)batchedStatement;
                            }
                            batchedParamIndex = 1;
                            while (batchCounter < numBatchedArgs) {
                                batchedParamIndex = this.setOneBatchedParameterSet((java.sql.PreparedStatement)batchedStatement, batchedParamIndex, this.batchedArgs.get(batchCounter++));
                            }
                            try {
                                batchedStatement.execute();
                            }
                            catch (SQLException ex) {
                                sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex);
                            }
                            updateCountCounter = this.processMultiCountsAndKeys((StatementImpl)batchedStatement, updateCountCounter, updateCounts);
                            batchedStatement.clearParameters();
                        }
                        if (timeoutTask != null) {
                            if (((StatementImpl.CancelTask)timeoutTask).caughtWhileCancelling != null) {
                                throw ((StatementImpl.CancelTask)timeoutTask).caughtWhileCancelling;
                            }
                            timeoutTask.cancel();
                            locallyScopedConn.getCancelTimer().purge();
                            timeoutTask = null;
                        }
                        if (sqlEx != null) {
                            throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.getExceptionInterceptor());
                        }
                        lArray = updateCounts;
                        Object var21_24 = null;
                        if (batchedStatement == null) break block32;
                    }
                    catch (Throwable throwable) {
                        Object var21_25 = null;
                        if (batchedStatement == null) throw throwable;
                        batchedStatement.close();
                        throw throwable;
                    }
                    batchedStatement.close();
                }
                Object var23_26 = null;
                if (timeoutTask != null) {
                    timeoutTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                }
                this.resetCancelledState();
                if (!multiQueriesEnabled) {
                    locallyScopedConn.getIO().disableMultiQueries();
                }
                this.clearBatch();
                return lArray;
            }
            catch (Throwable throwable) {
                Object var23_27 = null;
                if (timeoutTask != null) {
                    timeoutTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                }
                this.resetCancelledState();
                if (!multiQueriesEnabled) {
                    locallyScopedConn.getIO().disableMultiQueries();
                }
                this.clearBatch();
                throw throwable;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String generateMultiStatementForBatch(int numBatches) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            StringBuilder newStatementSql = new StringBuilder((this.originalSql.length() + 1) * numBatches);
            newStatementSql.append(this.originalSql);
            for (int i = 0; i < numBatches - 1; ++i) {
                newStatementSql.append(';');
                newStatementSql.append(this.originalSql);
            }
            return newStatementSql.toString();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected long[] executeBatchedInserts(int batchTimeout) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            int numValuesPerBatch;
            String valuesClause = this.getValuesClause();
            MySQLConnection locallyScopedConn = this.connection;
            if (valuesClause == null) {
                return this.executeBatchSerially(batchTimeout);
            }
            int numBatchedArgs = this.batchedArgs.size();
            if (this.retrieveGeneratedKeys) {
                this.batchedGeneratedKeys = new ArrayList(numBatchedArgs);
            }
            if (numBatchedArgs < (numValuesPerBatch = this.computeBatchSize(numBatchedArgs))) {
                numValuesPerBatch = numBatchedArgs;
            }
            PreparedStatement batchedStatement = null;
            int batchedParamIndex = 1;
            long updateCountRunningTotal = 0L;
            int numberToExecuteAsMultiValue = 0;
            int batchCounter = 0;
            TimerTask timeoutTask = null;
            SQLException sqlEx = null;
            long[] updateCounts = new long[numBatchedArgs];
            try {
                long[] lArray;
                block30: {
                    try {
                        batchedStatement = this.prepareBatchedInsertSQL(locallyScopedConn, numValuesPerBatch);
                        if (locallyScopedConn.getEnableQueryTimeouts() && batchTimeout != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                            timeoutTask = new StatementImpl.CancelTask(batchedStatement);
                            locallyScopedConn.getCancelTimer().schedule(timeoutTask, batchTimeout);
                        }
                        numberToExecuteAsMultiValue = numBatchedArgs < numValuesPerBatch ? numBatchedArgs : numBatchedArgs / numValuesPerBatch;
                        int numberArgsToExecute = numberToExecuteAsMultiValue * numValuesPerBatch;
                        for (int i = 0; i < numberArgsToExecute; ++i) {
                            if (i != 0 && i % numValuesPerBatch == 0) {
                                try {
                                    updateCountRunningTotal += batchedStatement.executeLargeUpdate();
                                }
                                catch (SQLException ex) {
                                    sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex);
                                }
                                this.getBatchedGeneratedKeys(batchedStatement);
                                batchedStatement.clearParameters();
                                batchedParamIndex = 1;
                            }
                            batchedParamIndex = this.setOneBatchedParameterSet(batchedStatement, batchedParamIndex, this.batchedArgs.get(batchCounter++));
                        }
                        try {
                            updateCountRunningTotal += batchedStatement.executeLargeUpdate();
                        }
                        catch (SQLException ex) {
                            sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex);
                        }
                        this.getBatchedGeneratedKeys(batchedStatement);
                        numValuesPerBatch = numBatchedArgs - batchCounter;
                        Object var20_23 = null;
                        if (batchedStatement != null) {
                            batchedStatement.close();
                            batchedStatement = null;
                        }
                    }
                    catch (Throwable throwable) {
                        Object var20_24 = null;
                        if (batchedStatement == null) throw throwable;
                        batchedStatement.close();
                        batchedStatement = null;
                        throw throwable;
                    }
                    try {
                        if (numValuesPerBatch > 0) {
                            batchedStatement = this.prepareBatchedInsertSQL(locallyScopedConn, numValuesPerBatch);
                            if (timeoutTask != null) {
                                ((StatementImpl.CancelTask)timeoutTask).toCancel = batchedStatement;
                            }
                            batchedParamIndex = 1;
                            while (batchCounter < numBatchedArgs) {
                                batchedParamIndex = this.setOneBatchedParameterSet(batchedStatement, batchedParamIndex, this.batchedArgs.get(batchCounter++));
                            }
                            try {
                                updateCountRunningTotal += batchedStatement.executeLargeUpdate();
                            }
                            catch (SQLException ex) {
                                sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex);
                            }
                            this.getBatchedGeneratedKeys(batchedStatement);
                        }
                        if (sqlEx != null) {
                            throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.getExceptionInterceptor());
                        }
                        if (numBatchedArgs > 1) {
                            long updCount = updateCountRunningTotal > 0L ? -2L : 0L;
                            for (int j = 0; j < numBatchedArgs; ++j) {
                                updateCounts[j] = updCount;
                            }
                        } else {
                            updateCounts[0] = updateCountRunningTotal;
                        }
                        lArray = updateCounts;
                        Object var22_26 = null;
                        if (batchedStatement == null) break block30;
                    }
                    catch (Throwable throwable) {
                        Object var22_27 = null;
                        if (batchedStatement == null) throw throwable;
                        batchedStatement.close();
                        throw throwable;
                    }
                    batchedStatement.close();
                }
                Object var24_28 = null;
                if (timeoutTask != null) {
                    timeoutTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                }
                this.resetCancelledState();
                return lArray;
            }
            catch (Throwable throwable) {
                Object var24_29 = null;
                if (timeoutTask != null) {
                    timeoutTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                }
                this.resetCancelledState();
                throw throwable;
            }
        }
    }

    protected String getValuesClause() throws SQLException {
        return this.parseInfo.valuesClause;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected int computeBatchSize(int numBatchedArgs) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            long[] combinedValues = this.computeMaxParameterSetSizeAndBatchSize(numBatchedArgs);
            long maxSizeOfParameterSet = combinedValues[0];
            long sizeOfEntireBatch = combinedValues[1];
            int maxAllowedPacket = this.connection.getMaxAllowedPacket();
            if (sizeOfEntireBatch < (long)(maxAllowedPacket - this.originalSql.length())) {
                return numBatchedArgs;
            }
            return (int)Math.max(1L, (long)(maxAllowedPacket - this.originalSql.length()) / maxSizeOfParameterSet);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected long[] computeMaxParameterSetSizeAndBatchSize(int numBatchedArgs) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            long sizeOfEntireBatch = 0L;
            long maxSizeOfParameterSet = 0L;
            for (int i = 0; i < numBatchedArgs; ++i) {
                BatchParams paramArg = (BatchParams)this.batchedArgs.get(i);
                boolean[] isNullBatch = paramArg.isNull;
                boolean[] isStreamBatch = paramArg.isStream;
                long sizeOfParameterSet = 0L;
                for (int j = 0; j < isNullBatch.length; ++j) {
                    if (!isNullBatch[j]) {
                        if (isStreamBatch[j]) {
                            int streamLength = paramArg.streamLengths[j];
                            if (streamLength != -1) {
                                sizeOfParameterSet += (long)(streamLength * 2);
                                continue;
                            }
                            int paramLength = paramArg.parameterStrings[j].length;
                            sizeOfParameterSet += (long)paramLength;
                            continue;
                        }
                        sizeOfParameterSet += (long)paramArg.parameterStrings[j].length;
                        continue;
                    }
                    sizeOfParameterSet += 4L;
                }
                sizeOfParameterSet = this.getValuesClause() != null ? (sizeOfParameterSet += (long)(this.getValuesClause().length() + 1)) : (sizeOfParameterSet += (long)(this.originalSql.length() + 1));
                sizeOfEntireBatch += sizeOfParameterSet;
                if (sizeOfParameterSet <= maxSizeOfParameterSet) continue;
                maxSizeOfParameterSet = sizeOfParameterSet;
            }
            return new long[]{maxSizeOfParameterSet, sizeOfEntireBatch};
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected long[] executeBatchSerially(int batchTimeout) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            MySQLConnection locallyScopedConn = this.connection;
            if (locallyScopedConn == null) {
                this.checkClosed();
            }
            long[] updateCounts = null;
            if (this.batchedArgs != null) {
                block22: {
                    int nbrCommands = this.batchedArgs.size();
                    updateCounts = new long[nbrCommands];
                    for (int i = 0; i < nbrCommands; ++i) {
                        updateCounts[i] = -3L;
                    }
                    SQLException sqlEx = null;
                    TimerTask timeoutTask = null;
                    try {
                        try {
                            if (locallyScopedConn.getEnableQueryTimeouts() && batchTimeout != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                                timeoutTask = new StatementImpl.CancelTask(this);
                                locallyScopedConn.getCancelTimer().schedule(timeoutTask, batchTimeout);
                            }
                            if (this.retrieveGeneratedKeys) {
                                this.batchedGeneratedKeys = new ArrayList(nbrCommands);
                            }
                            this.batchCommandIndex = 0;
                            while (this.batchCommandIndex < nbrCommands) {
                                Object arg = this.batchedArgs.get(this.batchCommandIndex);
                                try {
                                    if (arg instanceof String) {
                                        updateCounts[this.batchCommandIndex] = this.executeUpdateInternal((String)arg, true, this.retrieveGeneratedKeys);
                                        this.getBatchedGeneratedKeys(this.results.getFirstCharOfQuery() == 'I' && this.containsOnDuplicateKeyInString((String)arg) ? 1 : 0);
                                    } else {
                                        BatchParams paramArg = (BatchParams)arg;
                                        updateCounts[this.batchCommandIndex] = this.executeUpdateInternal(paramArg.parameterStrings, paramArg.parameterStreams, paramArg.isStream, paramArg.streamLengths, paramArg.isNull, true);
                                        this.getBatchedGeneratedKeys(this.containsOnDuplicateKeyUpdateInSQL() ? 1 : 0);
                                    }
                                }
                                catch (SQLException ex) {
                                    updateCounts[this.batchCommandIndex] = -3L;
                                    if (this.continueBatchOnError && !(ex instanceof MySQLTimeoutException) && !(ex instanceof MySQLStatementCancelledException) && !this.hasDeadlockOrTimeoutRolledBackTx(ex)) {
                                        sqlEx = ex;
                                    }
                                    long[] newUpdateCounts = new long[this.batchCommandIndex];
                                    System.arraycopy(updateCounts, 0, newUpdateCounts, 0, this.batchCommandIndex);
                                    throw SQLError.createBatchUpdateException(ex, newUpdateCounts, this.getExceptionInterceptor());
                                }
                                ++this.batchCommandIndex;
                            }
                            if (sqlEx != null) {
                                throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.getExceptionInterceptor());
                            }
                            Object var12_16 = null;
                            this.batchCommandIndex = -1;
                            if (timeoutTask == null) break block22;
                        }
                        catch (NullPointerException npe) {
                            try {
                                this.checkClosed();
                                throw npe;
                            }
                            catch (SQLException connectionClosedEx) {
                                updateCounts[this.batchCommandIndex] = -3L;
                                long[] newUpdateCounts = new long[this.batchCommandIndex];
                                System.arraycopy(updateCounts, 0, newUpdateCounts, 0, this.batchCommandIndex);
                                throw SQLError.createBatchUpdateException(connectionClosedEx, newUpdateCounts, this.getExceptionInterceptor());
                            }
                        }
                    }
                    catch (Throwable throwable) {
                        Object var12_17 = null;
                        this.batchCommandIndex = -1;
                        if (timeoutTask != null) {
                            timeoutTask.cancel();
                            locallyScopedConn.getCancelTimer().purge();
                        }
                        this.resetCancelledState();
                        throw throwable;
                    }
                    timeoutTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                }
                this.resetCancelledState();
            }
            return updateCounts != null ? updateCounts : new long[]{};
        }
    }

    public String getDateTime(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(new Date());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * WARNING - void declaration
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected ResultSetInternalMethods executeInternal(int maxRowsToRetrieve, Buffer sendPacket, boolean createStreamingResultSet, boolean queryIsSelectOnly, Field[] metadataFromCache, boolean isBatch) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            try {
                ResultSetInternalMethods rs;
                block19: {
                    this.resetCancelledState();
                    MySQLConnection locallyScopedConnection = this.connection;
                    ++this.numberOfExecutions;
                    if (this.doPingInstead) {
                        this.doPingInstead();
                        return this.results;
                    }
                    TimerTask timeoutTask = null;
                    try {
                        if (locallyScopedConnection.getEnableQueryTimeouts() && this.timeoutInMillis != 0 && locallyScopedConnection.versionMeetsMinimum(5, 0, 0)) {
                            timeoutTask = new StatementImpl.CancelTask(this);
                            locallyScopedConnection.getCancelTimer().schedule(timeoutTask, this.timeoutInMillis);
                        }
                        if (!isBatch) {
                            this.statementBegins();
                        }
                        rs = locallyScopedConnection.execSQL(this, null, maxRowsToRetrieve, sendPacket, this.resultSetType, this.resultSetConcurrency, createStreamingResultSet, this.currentCatalog, metadataFromCache, isBatch);
                        if (timeoutTask != null) {
                            timeoutTask.cancel();
                            locallyScopedConnection.getCancelTimer().purge();
                            if (((StatementImpl.CancelTask)timeoutTask).caughtWhileCancelling != null) {
                                throw ((StatementImpl.CancelTask)timeoutTask).caughtWhileCancelling;
                            }
                            timeoutTask = null;
                        }
                        Object object2 = this.cancelTimeoutMutex;
                        synchronized (object2) {
                            if (this.wasCancelled) {
                                void var12_16;
                                Object var12_13 = null;
                                if (this.wasCancelledByTimeout) {
                                    MySQLTimeoutException mySQLTimeoutException = new MySQLTimeoutException();
                                } else {
                                    MySQLStatementCancelledException mySQLStatementCancelledException = new MySQLStatementCancelledException();
                                }
                                this.resetCancelledState();
                                throw var12_16;
                            }
                        }
                        Object var15_17 = null;
                        if (!isBatch) {
                            this.statementExecuting.set(false);
                        }
                        if (timeoutTask == null) break block19;
                    }
                    catch (Throwable throwable) {
                        Object var15_18 = null;
                        if (!isBatch) {
                            this.statementExecuting.set(false);
                        }
                        if (timeoutTask != null) {
                            timeoutTask.cancel();
                            locallyScopedConnection.getCancelTimer().purge();
                        }
                        throw throwable;
                    }
                    timeoutTask.cancel();
                    locallyScopedConnection.getCancelTimer().purge();
                }
                return rs;
            }
            catch (NullPointerException npe) {
                this.checkClosed();
                throw npe;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ResultSet executeQuery() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            MySQLConnection locallyScopedConn = this.connection;
            this.checkForDml(this.originalSql, this.firstCharOfStmt);
            CachedResultSetMetaData cachedMetadata = null;
            this.clearWarnings();
            this.batchedGeneratedKeys = null;
            this.setupStreamingTimeout(locallyScopedConn);
            Buffer sendPacket = this.fillSendPacket();
            this.implicitlyCloseAllOpenResults();
            String oldCatalog = null;
            if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
                oldCatalog = locallyScopedConn.getCatalog();
                locallyScopedConn.setCatalog(this.currentCatalog);
            }
            if (locallyScopedConn.getCacheResultSetMetadata()) {
                cachedMetadata = locallyScopedConn.getCachedMetaData(this.originalSql);
            }
            Field[] metadataFromCache = null;
            if (cachedMetadata != null) {
                metadataFromCache = cachedMetadata.fields;
            }
            locallyScopedConn.setSessionMaxRows(this.maxRows);
            this.results = this.executeInternal(this.maxRows, sendPacket, this.createStreamingResultSet(), true, metadataFromCache, false);
            if (oldCatalog != null) {
                locallyScopedConn.setCatalog(oldCatalog);
            }
            if (cachedMetadata != null) {
                locallyScopedConn.initializeResultsMetadataFromCache(this.originalSql, cachedMetadata, this.results);
            } else if (locallyScopedConn.getCacheResultSetMetadata()) {
                locallyScopedConn.initializeResultsMetadataFromCache(this.originalSql, null, this.results);
            }
            this.lastInsertId = this.results.getUpdateID();
            return this.results;
        }
    }

    public int executeUpdate() throws SQLException {
        return Util.truncateAndConvertToInt(this.executeLargeUpdate());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected long executeUpdateInternal(boolean clearBatchedGeneratedKeysAndWarnings, boolean isBatch) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (clearBatchedGeneratedKeysAndWarnings) {
                this.clearWarnings();
                this.batchedGeneratedKeys = null;
            }
            return this.executeUpdateInternal(this.parameterValues, this.parameterStreams, this.isStream, this.streamLengths, this.isNull, isBatch);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected long executeUpdateInternal(byte[][] batchedParameterStrings, InputStream[] batchedParameterStreams, boolean[] batchedIsStream, int[] batchedStreamLengths, boolean[] batchedIsNull, boolean isReallyBatch) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            MySQLConnection locallyScopedConn = this.connection;
            if (locallyScopedConn.isReadOnly(false)) {
                throw SQLError.createSQLException(Messages.getString("PreparedStatement.34") + Messages.getString("PreparedStatement.35"), "S1009", this.getExceptionInterceptor());
            }
            if (this.firstCharOfStmt == 'S' && this.isSelectQuery()) {
                throw SQLError.createSQLException(Messages.getString("PreparedStatement.37"), "01S03", this.getExceptionInterceptor());
            }
            this.implicitlyCloseAllOpenResults();
            ResultSetInternalMethods rs = null;
            Buffer sendPacket = this.fillSendPacket(batchedParameterStrings, batchedParameterStreams, batchedIsStream, batchedStreamLengths);
            String oldCatalog = null;
            if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
                oldCatalog = locallyScopedConn.getCatalog();
                locallyScopedConn.setCatalog(this.currentCatalog);
            }
            locallyScopedConn.setSessionMaxRows(-1);
            boolean oldInfoMsgState = false;
            if (this.retrieveGeneratedKeys) {
                oldInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
                locallyScopedConn.setReadInfoMsgEnabled(true);
            }
            rs = this.executeInternal(-1, sendPacket, false, false, null, isReallyBatch);
            if (this.retrieveGeneratedKeys) {
                locallyScopedConn.setReadInfoMsgEnabled(oldInfoMsgState);
                rs.setFirstCharOfQuery(this.firstCharOfStmt);
            }
            if (oldCatalog != null) {
                locallyScopedConn.setCatalog(oldCatalog);
            }
            this.results = rs;
            this.updateCount = rs.getUpdateCount();
            if (this.containsOnDuplicateKeyUpdateInSQL() && this.compensateForOnDuplicateKeyUpdate && (this.updateCount == 2L || this.updateCount == 0L)) {
                this.updateCount = 1L;
            }
            this.lastInsertId = rs.getUpdateID();
            return this.updateCount;
        }
    }

    protected boolean containsOnDuplicateKeyUpdateInSQL() {
        return this.parseInfo.isOnDuplicateKeyUpdate;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Buffer fillSendPacket() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.fillSendPacket(this.parameterValues, this.parameterStreams, this.isStream, this.streamLengths);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Buffer fillSendPacket(byte[][] batchedParameterStrings, InputStream[] batchedParameterStreams, boolean[] batchedIsStream, int[] batchedStreamLengths) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            int i;
            Buffer sendPacket = this.connection.getIO().getSharedSendPacket();
            sendPacket.clear();
            sendPacket.writeByte((byte)3);
            boolean useStreamLengths = this.connection.getUseStreamLengthsInPrepStmts();
            int ensurePacketSize = 0;
            String statementComment = this.connection.getStatementComment();
            byte[] commentAsBytes = null;
            if (statementComment != null) {
                commentAsBytes = this.charConverter != null ? this.charConverter.toBytes(statementComment) : StringUtils.getBytes(statementComment, this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
                ensurePacketSize += commentAsBytes.length;
                ensurePacketSize += 6;
            }
            for (i = 0; i < batchedParameterStrings.length; ++i) {
                if (!batchedIsStream[i] || !useStreamLengths) continue;
                ensurePacketSize += batchedStreamLengths[i];
            }
            if (ensurePacketSize != 0) {
                sendPacket.ensureCapacity(ensurePacketSize);
            }
            if (commentAsBytes != null) {
                sendPacket.writeBytesNoNull(Constants.SLASH_STAR_SPACE_AS_BYTES);
                sendPacket.writeBytesNoNull(commentAsBytes);
                sendPacket.writeBytesNoNull(Constants.SPACE_STAR_SLASH_SPACE_AS_BYTES);
            }
            for (i = 0; i < batchedParameterStrings.length; ++i) {
                this.checkAllParametersSet(batchedParameterStrings[i], batchedParameterStreams[i], i);
                sendPacket.writeBytesNoNull(this.staticSqlStrings[i]);
                if (batchedIsStream[i]) {
                    this.streamToBytes(sendPacket, batchedParameterStreams[i], true, batchedStreamLengths[i], useStreamLengths);
                    continue;
                }
                sendPacket.writeBytesNoNull(batchedParameterStrings[i]);
            }
            sendPacket.writeBytesNoNull(this.staticSqlStrings[batchedParameterStrings.length]);
            return sendPacket;
        }
    }

    private void checkAllParametersSet(byte[] parameterString, InputStream parameterStream, int columnIndex) throws SQLException {
        if (parameterString == null && parameterStream == null) {
            throw SQLError.createSQLException(Messages.getString("PreparedStatement.40") + (columnIndex + 1), "07001", this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected PreparedStatement prepareBatchedInsertSQL(MySQLConnection localConn, int numBatches) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            PreparedStatement pstmt = new PreparedStatement(localConn, "Rewritten batch of: " + this.originalSql, this.currentCatalog, this.parseInfo.getParseInfoForBatch(numBatches));
            pstmt.setRetrieveGeneratedKeys(this.retrieveGeneratedKeys);
            pstmt.rewrittenBatchSize = numBatches;
            return pstmt;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void setRetrieveGeneratedKeys(boolean flag) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.retrieveGeneratedKeys = flag;
        }
    }

    public int getRewrittenBatchSize() {
        return this.rewrittenBatchSize;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String getNonRewrittenSql() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            int indexOfBatch = this.originalSql.indexOf(" of: ");
            if (indexOfBatch != -1) {
                return this.originalSql.substring(indexOfBatch + 5);
            }
            return this.originalSql;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public byte[] getBytesRepresentation(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.isStream[parameterIndex]) {
                return this.streamToBytes(this.parameterStreams[parameterIndex], false, this.streamLengths[parameterIndex], this.connection.getUseStreamLengthsInPrepStmts());
            }
            byte[] parameterVal = this.parameterValues[parameterIndex];
            if (parameterVal == null) {
                return null;
            }
            if (parameterVal[0] == 39 && parameterVal[parameterVal.length - 1] == 39) {
                byte[] valNoQuotes = new byte[parameterVal.length - 2];
                System.arraycopy(parameterVal, 1, valNoQuotes, 0, parameterVal.length - 2);
                return valNoQuotes;
            }
            return parameterVal;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected byte[] getBytesRepresentationForBatch(int parameterIndex, int commandIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            Object batchedArg = this.batchedArgs.get(commandIndex);
            if (batchedArg instanceof String) {
                try {
                    return StringUtils.getBytes((String)batchedArg, this.charEncoding);
                }
                catch (UnsupportedEncodingException uue) {
                    throw new RuntimeException(Messages.getString("PreparedStatement.32") + this.charEncoding + Messages.getString("PreparedStatement.33"));
                }
            }
            BatchParams params = (BatchParams)batchedArg;
            if (params.isStream[parameterIndex]) {
                return this.streamToBytes(params.parameterStreams[parameterIndex], false, params.streamLengths[parameterIndex], this.connection.getUseStreamLengthsInPrepStmts());
            }
            byte[] parameterVal = params.parameterStrings[parameterIndex];
            if (parameterVal == null) {
                return null;
            }
            if (parameterVal[0] == 39 && parameterVal[parameterVal.length - 1] == 39) {
                byte[] valNoQuotes = new byte[parameterVal.length - 2];
                System.arraycopy(parameterVal, 1, valNoQuotes, 0, parameterVal.length - 2);
                return valNoQuotes;
            }
            return parameterVal;
        }
    }

    private final String getDateTimePattern(String dt, boolean toTime) throws Exception {
        int i;
        int size;
        char c;
        int n;
        Object[] v;
        int z;
        int dtLength;
        int n2 = dtLength = dt != null ? dt.length() : 0;
        if (dtLength >= 8 && dtLength <= 10) {
            int dashCount = 0;
            boolean isDateOnly = true;
            for (int i2 = 0; i2 < dtLength; ++i2) {
                char c2 = dt.charAt(i2);
                if (!Character.isDigit(c2) && c2 != '-') {
                    isDateOnly = false;
                    break;
                }
                if (c2 != '-') continue;
                ++dashCount;
            }
            if (isDateOnly && dashCount == 2) {
                return "yyyy-MM-dd";
            }
        }
        boolean colonsOnly = true;
        for (int i3 = 0; i3 < dtLength; ++i3) {
            char c3 = dt.charAt(i3);
            if (Character.isDigit(c3) || c3 == ':') continue;
            colonsOnly = false;
            break;
        }
        if (colonsOnly) {
            return "HH:mm:ss";
        }
        StringReader reader = new StringReader(dt + " ");
        ArrayList<Object[]> vec = new ArrayList<Object[]>();
        ArrayList<Object[]> vecRemovelist = new ArrayList<Object[]>();
        Object[] nv = new Object[]{Character.valueOf('y'), new StringBuilder(), 0};
        vec.add(nv);
        if (toTime) {
            nv = new Object[]{Character.valueOf('h'), new StringBuilder(), 0};
            vec.add(nv);
        }
        while ((z = reader.read()) != -1) {
            char separator = (char)z;
            int maxvecs = vec.size();
            for (int count = 0; count < maxvecs; ++count) {
                v = (Object[])vec.get(count);
                n = (Integer)v[2];
                c = this.getSuccessor(((Character)v[0]).charValue(), n);
                if (!Character.isLetterOrDigit(separator)) {
                    if (c == ((Character)v[0]).charValue() && c != 'S') {
                        vecRemovelist.add(v);
                        continue;
                    }
                    ((StringBuilder)v[1]).append(separator);
                    if (c != 'X' && c != 89) continue;
                    v[2] = 4;
                    continue;
                }
                if (c == 'X') {
                    c = 'y';
                    nv = new Object[3];
                    nv[1] = new StringBuilder(((StringBuilder)v[1]).toString()).append('M');
                    nv[0] = Character.valueOf('M');
                    nv[2] = 1;
                    vec.add(nv);
                } else if (c == 'Y') {
                    c = 'M';
                    nv = new Object[3];
                    nv[1] = new StringBuilder(((StringBuilder)v[1]).toString()).append('d');
                    nv[0] = Character.valueOf('d');
                    nv[2] = 1;
                    vec.add(nv);
                }
                ((StringBuilder)v[1]).append(c);
                if (c == ((Character)v[0]).charValue()) {
                    v[2] = n + 1;
                    continue;
                }
                v[0] = Character.valueOf(c);
                v[2] = 1;
            }
            size = vecRemovelist.size();
            for (i = 0; i < size; ++i) {
                v = (Object[])vecRemovelist.get(i);
                vec.remove(v);
            }
            vecRemovelist.clear();
        }
        size = vec.size();
        for (i = 0; i < size; ++i) {
            boolean containsEnd;
            v = (Object[])vec.get(i);
            c = ((Character)v[0]).charValue();
            boolean bk = this.getSuccessor(c, n = ((Integer)v[2]).intValue()) != c;
            boolean atEnd = (c == 's' || c == 'm' || c == 'h' && toTime) && bk;
            boolean finishesAtDate = bk && c == 'd' && !toTime;
            boolean bl = containsEnd = ((StringBuilder)v[1]).toString().indexOf(87) != -1;
            if ((atEnd || finishesAtDate) && !containsEnd) continue;
            vecRemovelist.add(v);
        }
        size = vecRemovelist.size();
        for (i = 0; i < size; ++i) {
            vec.remove(vecRemovelist.get(i));
        }
        vecRemovelist.clear();
        v = (Object[])vec.get(0);
        StringBuilder format = (StringBuilder)v[1];
        format.setLength(format.length() - 1);
        return format.toString();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public java.sql.ResultSetMetaData getMetaData() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (!this.isSelectQuery()) {
                return null;
            }
            PreparedStatement mdStmt = null;
            ResultSet mdRs = null;
            if (this.pstmtResultMetaData == null) {
                SQLException sqlEx22;
                SQLException sqlExRethrow2;
                block21: {
                    try {
                        mdStmt = new PreparedStatement(this.connection, this.originalSql, this.currentCatalog, this.parseInfo);
                        mdStmt.setMaxRows(1);
                        int paramCount = this.parameterValues.length;
                        for (int i = 1; i <= paramCount; ++i) {
                            mdStmt.setString(i, "");
                        }
                        boolean hadResults = mdStmt.execute();
                        if (hadResults) {
                            mdRs = mdStmt.getResultSet();
                            this.pstmtResultMetaData = mdRs.getMetaData();
                        } else {
                            this.pstmtResultMetaData = new ResultSetMetaData(new Field[0], this.connection.getUseOldAliasMetadataBehavior(), this.connection.getYearIsDateType(), this.getExceptionInterceptor());
                        }
                        Object var7_6 = null;
                        sqlExRethrow2 = null;
                        if (mdRs == null) break block21;
                    }
                    catch (Throwable throwable) {
                        SQLException sqlEx22;
                        Object var7_7 = null;
                        SQLException sqlExRethrow2 = null;
                        if (mdRs != null) {
                            try {
                                mdRs.close();
                            }
                            catch (SQLException sqlEx22) {
                                sqlExRethrow2 = sqlEx22;
                            }
                            mdRs = null;
                        }
                        if (mdStmt != null) {
                            try {
                                mdStmt.close();
                            }
                            catch (SQLException sqlEx22) {
                                sqlExRethrow2 = sqlEx22;
                            }
                            mdStmt = null;
                        }
                        if (sqlExRethrow2 != null) {
                            throw sqlExRethrow2;
                        }
                        throw throwable;
                    }
                    try {
                        mdRs.close();
                    }
                    catch (SQLException sqlEx22) {
                        sqlExRethrow2 = sqlEx22;
                    }
                    mdRs = null;
                }
                if (mdStmt != null) {
                    try {
                        mdStmt.close();
                    }
                    catch (SQLException sqlEx22) {
                        sqlExRethrow2 = sqlEx22;
                    }
                    mdStmt = null;
                }
                if (sqlExRethrow2 != null) {
                    throw sqlExRethrow2;
                }
            }
            return this.pstmtResultMetaData;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected boolean isSelectQuery() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return StringUtils.startsWithIgnoreCaseAndWs(StringUtils.stripComments(this.originalSql, "'\"", "'\"", true, false, true, true), "SELECT");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ParameterMetaData getParameterMetaData() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.parameterMetaData == null) {
                this.parameterMetaData = this.connection.getGenerateSimpleParameterMetadata() ? new MysqlParameterMetadata(this.parameterCount) : new MysqlParameterMetadata(null, this.parameterCount, this.getExceptionInterceptor());
            }
            return this.parameterMetaData;
        }
    }

    ParseInfo getParseInfo() {
        return this.parseInfo;
    }

    private final char getSuccessor(char c, int n) {
        return (char)(c == 'y' && n == 2 ? 88 : (c == 'y' && n < 4 ? 121 : (c == 'y' ? 77 : (c == 'M' && n == 2 ? 89 : (c == 'M' && n < 3 ? 77 : (c == 'M' ? 100 : (c == 'd' && n < 2 ? 100 : (c == 'd' ? 72 : (c == 'H' && n < 2 ? 72 : (c == 'H' ? 109 : (c == 'm' && n < 2 ? 109 : (c == 'm' ? 115 : (c == 's' && n < 2 ? 115 : 87)))))))))))));
    }

    private final void hexEscapeBlock(byte[] buf, Buffer packet, int size) throws SQLException {
        for (int i = 0; i < size; ++i) {
            byte b = buf[i];
            int lowBits = (b & 0xFF) / 16;
            int highBits = (b & 0xFF) % 16;
            packet.writeByte(HEX_DIGITS[lowBits]);
            packet.writeByte(HEX_DIGITS[highBits]);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void initializeFromParseInfo() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.staticSqlStrings = this.parseInfo.staticSql;
            this.isLoadDataQuery = this.parseInfo.foundLoadData;
            this.firstCharOfStmt = this.parseInfo.firstStmtChar;
            this.parameterCount = this.staticSqlStrings.length - 1;
            this.parameterValues = new byte[this.parameterCount][];
            this.parameterStreams = new InputStream[this.parameterCount];
            this.isStream = new boolean[this.parameterCount];
            this.streamLengths = new int[this.parameterCount];
            this.isNull = new boolean[this.parameterCount];
            this.parameterTypes = new int[this.parameterCount];
            this.clearParameters();
            for (int j = 0; j < this.parameterCount; ++j) {
                this.isStream[j] = false;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    boolean isNull(int paramIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.isNull[paramIndex];
        }
    }

    private final int readblock(InputStream i, byte[] b) throws SQLException {
        try {
            return i.read(b);
        }
        catch (Throwable ex) {
            SQLException sqlEx = SQLError.createSQLException(Messages.getString("PreparedStatement.56") + ex.getClass().getName(), "S1000", this.getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }

    private final int readblock(InputStream i, byte[] b, int length) throws SQLException {
        try {
            int lengthToRead = length;
            if (lengthToRead > b.length) {
                lengthToRead = b.length;
            }
            return i.read(b, 0, lengthToRead);
        }
        catch (Throwable ex) {
            SQLException sqlEx = SQLError.createSQLException(Messages.getString("PreparedStatement.56") + ex.getClass().getName(), "S1000", this.getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void realClose(boolean calledExplicitly, boolean closeOpenResults) throws SQLException {
        MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn == null) {
            return;
        }
        Object object = locallyScopedConn.getConnectionMutex();
        synchronized (object) {
            if (this.isClosed) {
                return;
            }
            if (this.useUsageAdvisor && this.numberOfExecutions <= 1) {
                String message = Messages.getString("PreparedStatement.43");
                this.eventSink.consumeEvent(new ProfilerEvent(0, "", this.currentCatalog, this.connectionId, this.getId(), -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, message));
            }
            super.realClose(calledExplicitly, closeOpenResults);
            this.dbmd = null;
            this.originalSql = null;
            this.staticSqlStrings = null;
            this.parameterValues = null;
            this.parameterStreams = null;
            this.isStream = null;
            this.streamLengths = null;
            this.isNull = null;
            this.streamConvertBuf = null;
            this.parameterTypes = null;
        }
    }

    public void setArray(int i, Array x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 12);
        } else {
            this.setBinaryStream(parameterIndex, x, length);
        }
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 3);
        } else {
            this.setInternal(parameterIndex, StringUtils.fixDecimalExponent(StringUtils.consistentToString(x)));
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 3;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (x == null) {
                this.setNull(parameterIndex, -2);
            } else {
                int parameterIndexOffset = this.getParameterIndexOffset();
                if (parameterIndex < 1 || parameterIndex > this.staticSqlStrings.length) {
                    throw SQLError.createSQLException(Messages.getString("PreparedStatement.2") + parameterIndex + Messages.getString("PreparedStatement.3") + this.staticSqlStrings.length + Messages.getString("PreparedStatement.4"), "S1009", this.getExceptionInterceptor());
                }
                if (parameterIndexOffset == -1 && parameterIndex == 1) {
                    throw SQLError.createSQLException("Can't set IN parameter for return value of stored function call.", "S1009", this.getExceptionInterceptor());
                }
                this.parameterStreams[parameterIndex - 1 + parameterIndexOffset] = x;
                this.isStream[parameterIndex - 1 + parameterIndexOffset] = true;
                this.streamLengths[parameterIndex - 1 + parameterIndexOffset] = length;
                this.isNull[parameterIndex - 1 + parameterIndexOffset] = false;
                this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2004;
            }
        }
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        this.setBinaryStream(parameterIndex, inputStream, (int)length);
    }

    public void setBlob(int i, Blob x) throws SQLException {
        if (x == null) {
            this.setNull(i, 2004);
        } else {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            bytesOut.write(39);
            this.escapeblockFast(x.getBytes(1L, (int)x.length()), bytesOut, (int)x.length());
            bytesOut.write(39);
            this.setInternal(i, bytesOut.toByteArray());
            this.parameterTypes[i - 1 + this.getParameterIndexOffset()] = 2004;
        }
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        if (this.useTrueBoolean) {
            this.setInternal(parameterIndex, x ? "1" : "0");
        } else {
            this.setInternal(parameterIndex, x ? "'t'" : "'f'");
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 16;
        }
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        this.setInternal(parameterIndex, String.valueOf(x));
        this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = -6;
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        this.setBytes(parameterIndex, x, true, true);
        if (x != null) {
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = -2;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void setBytes(int parameterIndex, byte[] x, boolean checkForIntroducer, boolean escapeForMBChars) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (x == null) {
                this.setNull(parameterIndex, -2);
            } else {
                boolean needsIntroducer;
                String connectionEncoding = this.connection.getEncoding();
                try {
                    if (this.connection.isNoBackslashEscapesSet() || escapeForMBChars && this.connection.getUseUnicode() && connectionEncoding != null && CharsetMapping.isMultibyteCharset(connectionEncoding)) {
                        ByteArrayOutputStream bOut = new ByteArrayOutputStream(x.length * 2 + 3);
                        bOut.write(120);
                        bOut.write(39);
                        for (int i = 0; i < x.length; ++i) {
                            int lowBits = (x[i] & 0xFF) / 16;
                            int highBits = (x[i] & 0xFF) % 16;
                            bOut.write(HEX_DIGITS[lowBits]);
                            bOut.write(HEX_DIGITS[highBits]);
                        }
                        bOut.write(39);
                        this.setInternal(parameterIndex, bOut.toByteArray());
                        return;
                    }
                }
                catch (SQLException ex) {
                    throw ex;
                }
                catch (RuntimeException ex) {
                    SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
                    sqlEx.initCause(ex);
                    throw sqlEx;
                }
                int numBytes = x.length;
                int pad = 2;
                boolean bl = needsIntroducer = checkForIntroducer && this.connection.versionMeetsMinimum(4, 1, 0);
                if (needsIntroducer) {
                    pad += 7;
                }
                ByteArrayOutputStream bOut = new ByteArrayOutputStream(numBytes + pad);
                if (needsIntroducer) {
                    bOut.write(95);
                    bOut.write(98);
                    bOut.write(105);
                    bOut.write(110);
                    bOut.write(97);
                    bOut.write(114);
                    bOut.write(121);
                }
                bOut.write(39);
                block16: for (int i = 0; i < numBytes; ++i) {
                    byte b = x[i];
                    switch (b) {
                        case 0: {
                            bOut.write(92);
                            bOut.write(48);
                            continue block16;
                        }
                        case 10: {
                            bOut.write(92);
                            bOut.write(110);
                            continue block16;
                        }
                        case 13: {
                            bOut.write(92);
                            bOut.write(114);
                            continue block16;
                        }
                        case 92: {
                            bOut.write(92);
                            bOut.write(92);
                            continue block16;
                        }
                        case 39: {
                            bOut.write(92);
                            bOut.write(39);
                            continue block16;
                        }
                        case 34: {
                            bOut.write(92);
                            bOut.write(34);
                            continue block16;
                        }
                        case 26: {
                            bOut.write(92);
                            bOut.write(90);
                            continue block16;
                        }
                        default: {
                            bOut.write(b);
                        }
                    }
                }
                bOut.write(39);
                this.setInternal(parameterIndex, bOut.toByteArray());
            }
        }
    }

    protected void setBytesNoEscape(int parameterIndex, byte[] parameterAsBytes) throws SQLException {
        byte[] parameterWithQuotes = new byte[parameterAsBytes.length + 2];
        parameterWithQuotes[0] = 39;
        System.arraycopy(parameterAsBytes, 0, parameterWithQuotes, 1, parameterAsBytes.length);
        parameterWithQuotes[parameterAsBytes.length + 1] = 39;
        this.setInternal(parameterIndex, parameterWithQuotes);
    }

    protected void setBytesNoEscapeNoQuotes(int parameterIndex, byte[] parameterAsBytes) throws SQLException {
        this.setInternal(parameterIndex, parameterAsBytes);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            block17: {
                try {
                    if (reader == null) {
                        this.setNull(parameterIndex, -1);
                        break block17;
                    }
                    char[] c = null;
                    int len = 0;
                    boolean useLength = this.connection.getUseStreamLengthsInPrepStmts();
                    String forcedEncoding = this.connection.getClobCharacterEncoding();
                    if (useLength && length != -1) {
                        c = new char[length];
                        int numCharsRead = PreparedStatement.readFully(reader, c, length);
                        if (forcedEncoding == null) {
                            this.setString(parameterIndex, new String(c, 0, numCharsRead));
                        } else {
                            try {
                                this.setBytes(parameterIndex, StringUtils.getBytes(new String(c, 0, numCharsRead), forcedEncoding));
                            }
                            catch (UnsupportedEncodingException uee) {
                                throw SQLError.createSQLException("Unsupported character encoding " + forcedEncoding, "S1009", this.getExceptionInterceptor());
                            }
                        }
                    } else {
                        c = new char[4096];
                        StringBuilder buf = new StringBuilder();
                        while ((len = reader.read(c)) != -1) {
                            buf.append(c, 0, len);
                        }
                        if (forcedEncoding == null) {
                            this.setString(parameterIndex, buf.toString());
                        } else {
                            try {
                                this.setBytes(parameterIndex, StringUtils.getBytes(buf.toString(), forcedEncoding));
                            }
                            catch (UnsupportedEncodingException uee) {
                                throw SQLError.createSQLException("Unsupported character encoding " + forcedEncoding, "S1009", this.getExceptionInterceptor());
                            }
                        }
                    }
                    this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2005;
                }
                catch (IOException ioEx) {
                    throw SQLError.createSQLException(ioEx.toString(), "S1000", this.getExceptionInterceptor());
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setClob(int i, Clob x) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (x == null) {
                this.setNull(i, 2005);
            } else {
                String forcedEncoding = this.connection.getClobCharacterEncoding();
                if (forcedEncoding == null) {
                    this.setString(i, x.getSubString(1L, (int)x.length()));
                } else {
                    try {
                        this.setBytes(i, StringUtils.getBytes(x.getSubString(1L, (int)x.length()), forcedEncoding));
                    }
                    catch (UnsupportedEncodingException uee) {
                        throw SQLError.createSQLException("Unsupported character encoding " + forcedEncoding, "S1009", this.getExceptionInterceptor());
                    }
                }
                this.parameterTypes[i - 1 + this.getParameterIndexOffset()] = 2005;
            }
        }
    }

    public void setDate(int parameterIndex, java.sql.Date x) throws SQLException {
        this.setDate(parameterIndex, x, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 91);
        } else if (!this.useLegacyDatetimeCode) {
            this.newSetDateInternal(parameterIndex, x, cal);
        } else {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                if (this.ddf == null) {
                    this.ddf = new SimpleDateFormat("''yyyy-MM-dd''", Locale.US);
                }
                if (cal != null) {
                    this.ddf.setTimeZone(cal.getTimeZone());
                }
                this.setInternal(parameterIndex, this.ddf.format(x));
                this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 91;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setDouble(int parameterIndex, double x) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (!this.connection.getAllowNanAndInf() && (x == Double.POSITIVE_INFINITY || x == Double.NEGATIVE_INFINITY || Double.isNaN(x))) {
                throw SQLError.createSQLException("'" + x + "' is not a valid numeric or approximate numeric value", "S1009", this.getExceptionInterceptor());
            }
            this.setInternal(parameterIndex, StringUtils.fixDecimalExponent(String.valueOf(x)));
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 8;
        }
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        this.setInternal(parameterIndex, StringUtils.fixDecimalExponent(String.valueOf(x)));
        this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 6;
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        this.setInternal(parameterIndex, String.valueOf(x));
        this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 4;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void setInternal(int paramIndex, byte[] val) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            int parameterIndexOffset = this.getParameterIndexOffset();
            this.checkBounds(paramIndex, parameterIndexOffset);
            this.isStream[paramIndex - 1 + parameterIndexOffset] = false;
            this.isNull[paramIndex - 1 + parameterIndexOffset] = false;
            this.parameterStreams[paramIndex - 1 + parameterIndexOffset] = null;
            this.parameterValues[paramIndex - 1 + parameterIndexOffset] = val;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void checkBounds(int paramIndex, int parameterIndexOffset) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (paramIndex < 1) {
                throw SQLError.createSQLException(Messages.getString("PreparedStatement.49") + paramIndex + Messages.getString("PreparedStatement.50"), "S1009", this.getExceptionInterceptor());
            }
            if (paramIndex > this.parameterCount) {
                throw SQLError.createSQLException(Messages.getString("PreparedStatement.51") + paramIndex + Messages.getString("PreparedStatement.52") + this.parameterValues.length + Messages.getString("PreparedStatement.53"), "S1009", this.getExceptionInterceptor());
            }
            if (parameterIndexOffset == -1 && paramIndex == 1) {
                throw SQLError.createSQLException("Can't set IN parameter for return value of stored function call.", "S1009", this.getExceptionInterceptor());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void setInternal(int paramIndex, String val) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            byte[] parameterAsBytes = null;
            parameterAsBytes = this.charConverter != null ? this.charConverter.toBytes(val) : StringUtils.getBytes(val, this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
            this.setInternal(paramIndex, parameterAsBytes);
        }
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        this.setInternal(parameterIndex, String.valueOf(x));
        this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = -5;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.setInternal(parameterIndex, "null");
            this.isNull[parameterIndex - 1 + this.getParameterIndexOffset()] = true;
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 0;
        }
    }

    public void setNull(int parameterIndex, int sqlType, String arg) throws SQLException {
        this.setNull(parameterIndex, sqlType);
        this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 0;
    }

    private void setNumericObject(int parameterIndex, Object parameterObj, int targetSqlType, int scale) throws SQLException {
        Number parameterAsNum;
        if (parameterObj instanceof Boolean) {
            parameterAsNum = (Boolean)parameterObj != false ? Integer.valueOf(1) : Integer.valueOf(0);
        } else if (parameterObj instanceof String) {
            switch (targetSqlType) {
                case -7: {
                    if ("1".equals(parameterObj) || "0".equals(parameterObj)) {
                        parameterAsNum = Integer.valueOf((String)parameterObj);
                        break;
                    }
                    boolean parameterAsBoolean = "true".equalsIgnoreCase((String)parameterObj);
                    parameterAsNum = parameterAsBoolean ? Integer.valueOf(1) : Integer.valueOf(0);
                    break;
                }
                case -6: 
                case 4: 
                case 5: {
                    parameterAsNum = Integer.valueOf((String)parameterObj);
                    break;
                }
                case -5: {
                    parameterAsNum = Long.valueOf((String)parameterObj);
                    break;
                }
                case 7: {
                    parameterAsNum = Float.valueOf((String)parameterObj);
                    break;
                }
                case 6: 
                case 8: {
                    parameterAsNum = Double.valueOf((String)parameterObj);
                    break;
                }
                default: {
                    parameterAsNum = new BigDecimal((String)parameterObj);
                    break;
                }
            }
        } else {
            parameterAsNum = (Number)parameterObj;
        }
        switch (targetSqlType) {
            case -7: 
            case -6: 
            case 4: 
            case 5: {
                this.setInt(parameterIndex, parameterAsNum.intValue());
                break;
            }
            case -5: {
                this.setLong(parameterIndex, parameterAsNum.longValue());
                break;
            }
            case 7: {
                this.setFloat(parameterIndex, parameterAsNum.floatValue());
                break;
            }
            case 6: 
            case 8: {
                this.setDouble(parameterIndex, parameterAsNum.doubleValue());
                break;
            }
            case 2: 
            case 3: {
                if (parameterAsNum instanceof BigDecimal) {
                    BigDecimal scaledBigDecimal = null;
                    try {
                        scaledBigDecimal = ((BigDecimal)parameterAsNum).setScale(scale);
                    }
                    catch (ArithmeticException ex) {
                        try {
                            scaledBigDecimal = ((BigDecimal)parameterAsNum).setScale(scale, 4);
                        }
                        catch (ArithmeticException arEx) {
                            throw SQLError.createSQLException("Can't set scale of '" + scale + "' for DECIMAL argument '" + parameterAsNum + "'", "S1009", this.getExceptionInterceptor());
                        }
                    }
                    this.setBigDecimal(parameterIndex, scaledBigDecimal);
                    break;
                }
                if (parameterAsNum instanceof BigInteger) {
                    this.setBigDecimal(parameterIndex, new BigDecimal((BigInteger)parameterAsNum, scale));
                    break;
                }
                this.setBigDecimal(parameterIndex, new BigDecimal(parameterAsNum.doubleValue()));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setObject(int parameterIndex, Object parameterObj) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (parameterObj == null) {
                this.setNull(parameterIndex, 1111);
            } else if (parameterObj instanceof Byte) {
                this.setInt(parameterIndex, ((Byte)parameterObj).intValue());
            } else if (parameterObj instanceof String) {
                this.setString(parameterIndex, (String)parameterObj);
            } else if (parameterObj instanceof BigDecimal) {
                this.setBigDecimal(parameterIndex, (BigDecimal)parameterObj);
            } else if (parameterObj instanceof Short) {
                this.setShort(parameterIndex, (Short)parameterObj);
            } else if (parameterObj instanceof Integer) {
                this.setInt(parameterIndex, (Integer)parameterObj);
            } else if (parameterObj instanceof Long) {
                this.setLong(parameterIndex, (Long)parameterObj);
            } else if (parameterObj instanceof Float) {
                this.setFloat(parameterIndex, ((Float)parameterObj).floatValue());
            } else if (parameterObj instanceof Double) {
                this.setDouble(parameterIndex, (Double)parameterObj);
            } else if (parameterObj instanceof byte[]) {
                this.setBytes(parameterIndex, (byte[])parameterObj);
            } else if (parameterObj instanceof java.sql.Date) {
                this.setDate(parameterIndex, (java.sql.Date)parameterObj);
            } else if (parameterObj instanceof Time) {
                this.setTime(parameterIndex, (Time)parameterObj);
            } else if (parameterObj instanceof Timestamp) {
                this.setTimestamp(parameterIndex, (Timestamp)parameterObj);
            } else if (parameterObj instanceof Boolean) {
                this.setBoolean(parameterIndex, (Boolean)parameterObj);
            } else if (parameterObj instanceof InputStream) {
                this.setBinaryStream(parameterIndex, (InputStream)parameterObj, -1);
            } else if (parameterObj instanceof Blob) {
                this.setBlob(parameterIndex, (Blob)parameterObj);
            } else if (parameterObj instanceof Clob) {
                this.setClob(parameterIndex, (Clob)parameterObj);
            } else if (this.connection.getTreatUtilDateAsTimestamp() && parameterObj instanceof Date) {
                this.setTimestamp(parameterIndex, new Timestamp(((Date)parameterObj).getTime()));
            } else if (parameterObj instanceof BigInteger) {
                this.setString(parameterIndex, parameterObj.toString());
            } else {
                this.setSerializableObject(parameterIndex, parameterObj);
            }
        }
    }

    public void setObject(int parameterIndex, Object parameterObj, int targetSqlType) throws SQLException {
        if (!(parameterObj instanceof BigDecimal)) {
            this.setObject(parameterIndex, parameterObj, targetSqlType, 0);
        } else {
            this.setObject(parameterIndex, parameterObj, targetSqlType, ((BigDecimal)parameterObj).scale());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setObject(int parameterIndex, Object parameterObj, int targetSqlType, int scale) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (parameterObj == null) {
                this.setNull(parameterIndex, 1111);
            } else {
                try {
                    block3 : switch (targetSqlType) {
                        case 16: {
                            if (parameterObj instanceof Boolean) {
                                this.setBoolean(parameterIndex, (Boolean)parameterObj);
                                break;
                            }
                            if (parameterObj instanceof String) {
                                this.setBoolean(parameterIndex, "true".equalsIgnoreCase((String)parameterObj) || !"0".equalsIgnoreCase((String)parameterObj));
                                break;
                            }
                            if (parameterObj instanceof Number) {
                                int intValue = ((Number)parameterObj).intValue();
                                this.setBoolean(parameterIndex, intValue != 0);
                                break;
                            }
                            throw SQLError.createSQLException("No conversion from " + parameterObj.getClass().getName() + " to Types.BOOLEAN possible.", "S1009", this.getExceptionInterceptor());
                        }
                        case -7: 
                        case -6: 
                        case -5: 
                        case 2: 
                        case 3: 
                        case 4: 
                        case 5: 
                        case 6: 
                        case 7: 
                        case 8: {
                            this.setNumericObject(parameterIndex, parameterObj, targetSqlType, scale);
                            break;
                        }
                        case -1: 
                        case 1: 
                        case 12: {
                            if (parameterObj instanceof BigDecimal) {
                                this.setString(parameterIndex, StringUtils.fixDecimalExponent(StringUtils.consistentToString((BigDecimal)parameterObj)));
                                break;
                            }
                            this.setString(parameterIndex, parameterObj.toString());
                            break;
                        }
                        case 2005: {
                            if (parameterObj instanceof Clob) {
                                this.setClob(parameterIndex, (Clob)parameterObj);
                                break;
                            }
                            this.setString(parameterIndex, parameterObj.toString());
                            break;
                        }
                        case -4: 
                        case -3: 
                        case -2: 
                        case 2004: {
                            if (parameterObj instanceof byte[]) {
                                this.setBytes(parameterIndex, (byte[])parameterObj);
                                break;
                            }
                            if (parameterObj instanceof Blob) {
                                this.setBlob(parameterIndex, (Blob)parameterObj);
                                break;
                            }
                            this.setBytes(parameterIndex, StringUtils.getBytes(parameterObj.toString(), this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor()));
                            break;
                        }
                        case 91: 
                        case 93: {
                            Date parameterAsDate;
                            if (parameterObj instanceof String) {
                                ParsePosition pp = new ParsePosition(0);
                                SimpleDateFormat sdf = new SimpleDateFormat(this.getDateTimePattern((String)parameterObj, false), Locale.US);
                                parameterAsDate = ((DateFormat)sdf).parse((String)parameterObj, pp);
                            } else {
                                parameterAsDate = (Date)parameterObj;
                            }
                            switch (targetSqlType) {
                                case 91: {
                                    if (parameterAsDate instanceof java.sql.Date) {
                                        this.setDate(parameterIndex, (java.sql.Date)parameterAsDate);
                                        break block3;
                                    }
                                    this.setDate(parameterIndex, new java.sql.Date(parameterAsDate.getTime()));
                                    break block3;
                                }
                                case 93: {
                                    if (parameterAsDate instanceof Timestamp) {
                                        this.setTimestamp(parameterIndex, (Timestamp)parameterAsDate);
                                        break block3;
                                    }
                                    this.setTimestamp(parameterIndex, new Timestamp(parameterAsDate.getTime()));
                                }
                            }
                            break;
                        }
                        case 92: {
                            if (parameterObj instanceof String) {
                                SimpleDateFormat sdf = new SimpleDateFormat(this.getDateTimePattern((String)parameterObj, true), Locale.US);
                                this.setTime(parameterIndex, new Time(sdf.parse((String)parameterObj).getTime()));
                                break;
                            }
                            if (parameterObj instanceof Timestamp) {
                                Timestamp xT = (Timestamp)parameterObj;
                                this.setTime(parameterIndex, new Time(xT.getTime()));
                                break;
                            }
                            this.setTime(parameterIndex, (Time)parameterObj);
                            break;
                        }
                        case 1111: {
                            this.setSerializableObject(parameterIndex, parameterObj);
                            break;
                        }
                        default: {
                            throw SQLError.createSQLException(Messages.getString("PreparedStatement.16"), "S1000", this.getExceptionInterceptor());
                        }
                    }
                }
                catch (Exception ex) {
                    if (ex instanceof SQLException) {
                        throw (SQLException)ex;
                    }
                    SQLException sqlEx = SQLError.createSQLException(Messages.getString("PreparedStatement.17") + parameterObj.getClass().toString() + Messages.getString("PreparedStatement.18") + ex.getClass().getName() + Messages.getString("PreparedStatement.19") + ex.getMessage(), "S1000", this.getExceptionInterceptor());
                    sqlEx.initCause(ex);
                    throw sqlEx;
                }
            }
        }
    }

    protected int setOneBatchedParameterSet(java.sql.PreparedStatement batchedStatement, int batchedParamIndex, Object paramSet) throws SQLException {
        BatchParams paramArg = (BatchParams)paramSet;
        boolean[] isNullBatch = paramArg.isNull;
        boolean[] isStreamBatch = paramArg.isStream;
        for (int j = 0; j < isNullBatch.length; ++j) {
            if (isNullBatch[j]) {
                batchedStatement.setNull(batchedParamIndex++, 0);
                continue;
            }
            if (isStreamBatch[j]) {
                batchedStatement.setBinaryStream(batchedParamIndex++, paramArg.parameterStreams[j], paramArg.streamLengths[j]);
                continue;
            }
            ((PreparedStatement)batchedStatement).setBytesNoEscapeNoQuotes(batchedParamIndex++, paramArg.parameterStrings[j]);
        }
        return batchedParamIndex;
    }

    public void setRef(int i, Ref x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    private final void setSerializableObject(int parameterIndex, Object parameterObj) throws SQLException {
        try {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(bytesOut);
            objectOut.writeObject(parameterObj);
            objectOut.flush();
            objectOut.close();
            bytesOut.flush();
            bytesOut.close();
            byte[] buf = bytesOut.toByteArray();
            ByteArrayInputStream bytesIn = new ByteArrayInputStream(buf);
            this.setBinaryStream(parameterIndex, (InputStream)bytesIn, buf.length);
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = -2;
        }
        catch (Exception ex) {
            SQLException sqlEx = SQLError.createSQLException(Messages.getString("PreparedStatement.54") + ex.getClass().getName(), "S1009", this.getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        this.setInternal(parameterIndex, String.valueOf(x));
        this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 5;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setString(int parameterIndex, String x) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (x == null) {
                this.setNull(parameterIndex, 1);
            } else {
                this.checkClosed();
                int stringLength = x.length();
                if (this.connection.isNoBackslashEscapesSet()) {
                    boolean needsHexEscape = this.isEscapeNeededForString(x, stringLength);
                    if (!needsHexEscape) {
                        byte[] parameterAsBytes = null;
                        StringBuilder quotedString = new StringBuilder(x.length() + 2);
                        quotedString.append('\'');
                        quotedString.append(x);
                        quotedString.append('\'');
                        parameterAsBytes = !this.isLoadDataQuery ? StringUtils.getBytes(quotedString.toString(), this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor()) : StringUtils.getBytes(quotedString.toString());
                        this.setInternal(parameterIndex, parameterAsBytes);
                    } else {
                        byte[] parameterAsBytes = null;
                        parameterAsBytes = !this.isLoadDataQuery ? StringUtils.getBytes(x, this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor()) : StringUtils.getBytes(x);
                        this.setBytes(parameterIndex, parameterAsBytes);
                    }
                    return;
                }
                String parameterAsString = x;
                boolean needsQuoted = true;
                if (this.isLoadDataQuery || this.isEscapeNeededForString(x, stringLength)) {
                    needsQuoted = false;
                    StringBuilder buf = new StringBuilder((int)((double)x.length() * 1.1));
                    buf.append('\'');
                    block13: for (int i = 0; i < stringLength; ++i) {
                        char c = x.charAt(i);
                        switch (c) {
                            case '\u0000': {
                                buf.append('\\');
                                buf.append('0');
                                continue block13;
                            }
                            case '\n': {
                                buf.append('\\');
                                buf.append('n');
                                continue block13;
                            }
                            case '\r': {
                                buf.append('\\');
                                buf.append('r');
                                continue block13;
                            }
                            case '\\': {
                                buf.append('\\');
                                buf.append('\\');
                                continue block13;
                            }
                            case '\'': {
                                buf.append('\\');
                                buf.append('\'');
                                continue block13;
                            }
                            case '\"': {
                                if (this.usingAnsiMode) {
                                    buf.append('\\');
                                }
                                buf.append('\"');
                                continue block13;
                            }
                            case '\u001a': {
                                buf.append('\\');
                                buf.append('Z');
                                continue block13;
                            }
                            case '\u00a5': 
                            case '\u20a9': {
                                if (this.charsetEncoder != null) {
                                    CharBuffer cbuf = CharBuffer.allocate(1);
                                    ByteBuffer bbuf = ByteBuffer.allocate(1);
                                    cbuf.put(c);
                                    cbuf.position(0);
                                    this.charsetEncoder.encode(cbuf, bbuf, true);
                                    if (bbuf.get(0) == 92) {
                                        buf.append('\\');
                                    }
                                }
                                buf.append(c);
                                continue block13;
                            }
                            default: {
                                buf.append(c);
                            }
                        }
                    }
                    buf.append('\'');
                    parameterAsString = buf.toString();
                }
                byte[] parameterAsBytes = null;
                parameterAsBytes = !this.isLoadDataQuery ? (needsQuoted ? StringUtils.getBytesWrapped(parameterAsString, '\'', '\'', this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor()) : StringUtils.getBytes(parameterAsString, this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor())) : StringUtils.getBytes(parameterAsString);
                this.setInternal(parameterIndex, parameterAsBytes);
                this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 12;
            }
        }
    }

    private boolean isEscapeNeededForString(String x, int stringLength) {
        boolean needsHexEscape = false;
        for (int i = 0; i < stringLength; ++i) {
            char c = x.charAt(i);
            switch (c) {
                case '\u0000': {
                    needsHexEscape = true;
                    break;
                }
                case '\n': {
                    needsHexEscape = true;
                    break;
                }
                case '\r': {
                    needsHexEscape = true;
                    break;
                }
                case '\\': {
                    needsHexEscape = true;
                    break;
                }
                case '\'': {
                    needsHexEscape = true;
                    break;
                }
                case '\"': {
                    needsHexEscape = true;
                    break;
                }
                case '\u001a': {
                    needsHexEscape = true;
                }
            }
            if (needsHexEscape) break;
        }
        return needsHexEscape;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.setTimeInternal(parameterIndex, x, cal, cal.getTimeZone(), true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setTime(int parameterIndex, Time x) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.setTimeInternal(parameterIndex, x, null, this.connection.getDefaultTimeZone(), false);
        }
    }

    private void setTimeInternal(int parameterIndex, Time x, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 92);
        } else {
            this.checkClosed();
            if (!this.useLegacyDatetimeCode) {
                this.newSetTimeInternal(parameterIndex, x, targetCalendar);
            } else {
                Calendar sessionCalendar = this.getCalendarInstanceForSessionOrNew();
                x = TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, x, tz, this.connection.getServerTimezoneTZ(), rollForward);
                this.setInternal(parameterIndex, "'" + x.toString() + "'");
            }
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 92;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.setTimestampInternal(parameterIndex, x, cal, cal.getTimeZone(), true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.setTimestampInternal(parameterIndex, x, null, this.connection.getDefaultTimeZone(), false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setTimestampInternal(int parameterIndex, Timestamp x, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 93);
        } else {
            this.checkClosed();
            if (!this.sendFractionalSeconds) {
                x = TimeUtil.truncateFractionalSeconds(x);
            }
            if (!this.useLegacyDatetimeCode) {
                this.newSetTimestampInternal(parameterIndex, x, targetCalendar);
            } else {
                Calendar sessionCalendar = this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : this.getCalendarInstanceForSessionOrNew();
                x = TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, x, tz, this.connection.getServerTimezoneTZ(), rollForward);
                if (this.connection.getUseSSPSCompatibleTimezoneShift()) {
                    this.doSSPSCompatibleTimezoneShift(parameterIndex, x);
                } else {
                    PreparedStatement preparedStatement = this;
                    synchronized (preparedStatement) {
                        int nanos;
                        if (this.tsdf == null) {
                            this.tsdf = new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss", Locale.US);
                        }
                        StringBuffer buf = new StringBuffer();
                        buf.append(this.tsdf.format(x));
                        if (this.serverSupportsFracSecs && (nanos = x.getNanos()) != 0) {
                            buf.append('.');
                            buf.append(TimeUtil.formatNanos(nanos, this.serverSupportsFracSecs, true));
                        }
                        buf.append('\'');
                        this.setInternal(parameterIndex, buf.toString());
                    }
                }
            }
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 93;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void newSetTimestampInternal(int parameterIndex, Timestamp x, Calendar targetCalendar) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.tsdf == null) {
                this.tsdf = new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss", Locale.US);
            }
            if (targetCalendar != null) {
                this.tsdf.setTimeZone(targetCalendar.getTimeZone());
            } else {
                this.tsdf.setTimeZone(this.connection.getServerTimezoneTZ());
            }
            StringBuffer buf = new StringBuffer();
            buf.append(this.tsdf.format(x));
            buf.append('.');
            buf.append(TimeUtil.formatNanos(x.getNanos(), this.serverSupportsFracSecs, true));
            buf.append('\'');
            this.setInternal(parameterIndex, buf.toString());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void newSetTimeInternal(int parameterIndex, Time x, Calendar targetCalendar) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.tdf == null) {
                this.tdf = new SimpleDateFormat("''HH:mm:ss''", Locale.US);
            }
            if (targetCalendar != null) {
                this.tdf.setTimeZone(targetCalendar.getTimeZone());
            } else {
                this.tdf.setTimeZone(this.connection.getServerTimezoneTZ());
            }
            this.setInternal(parameterIndex, this.tdf.format(x));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void newSetDateInternal(int parameterIndex, java.sql.Date x, Calendar targetCalendar) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.ddf == null) {
                this.ddf = new SimpleDateFormat("''yyyy-MM-dd''", Locale.US);
            }
            if (targetCalendar != null) {
                this.ddf.setTimeZone(targetCalendar.getTimeZone());
            } else if (this.connection.getNoTimezoneConversionForDateType()) {
                this.ddf.setTimeZone(this.connection.getDefaultTimeZone());
            } else {
                this.ddf.setTimeZone(this.connection.getServerTimezoneTZ());
            }
            this.setInternal(parameterIndex, this.ddf.format(x));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void doSSPSCompatibleTimezoneShift(int parameterIndex, Timestamp x) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            Calendar sessionCalendar2;
            Calendar calendar = sessionCalendar2 = this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : this.getCalendarInstanceForSessionOrNew();
            synchronized (calendar) {
                Date oldTime = sessionCalendar2.getTime();
                try {
                    sessionCalendar2.setTime(x);
                    int year = sessionCalendar2.get(1);
                    int month = sessionCalendar2.get(2) + 1;
                    int date = sessionCalendar2.get(5);
                    int hour = sessionCalendar2.get(11);
                    int minute = sessionCalendar2.get(12);
                    int seconds = sessionCalendar2.get(13);
                    StringBuilder tsBuf = new StringBuilder();
                    tsBuf.append('\'');
                    tsBuf.append(year);
                    tsBuf.append("-");
                    if (month < 10) {
                        tsBuf.append('0');
                    }
                    tsBuf.append(month);
                    tsBuf.append('-');
                    if (date < 10) {
                        tsBuf.append('0');
                    }
                    tsBuf.append(date);
                    tsBuf.append(' ');
                    if (hour < 10) {
                        tsBuf.append('0');
                    }
                    tsBuf.append(hour);
                    tsBuf.append(':');
                    if (minute < 10) {
                        tsBuf.append('0');
                    }
                    tsBuf.append(minute);
                    tsBuf.append(':');
                    if (seconds < 10) {
                        tsBuf.append('0');
                    }
                    tsBuf.append(seconds);
                    tsBuf.append('.');
                    tsBuf.append(TimeUtil.formatNanos(x.getNanos(), this.serverSupportsFracSecs, true));
                    tsBuf.append('\'');
                    this.setInternal(parameterIndex, tsBuf.toString());
                    Object var15_14 = null;
                    sessionCalendar2.setTime(oldTime);
                }
                catch (Throwable throwable) {
                    Object var15_15 = null;
                    sessionCalendar2.setTime(oldTime);
                    throw throwable;
                }
            }
        }
    }

    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 12);
        } else {
            this.setBinaryStream(parameterIndex, x, length);
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2005;
        }
    }

    public void setURL(int parameterIndex, URL arg) throws SQLException {
        if (arg != null) {
            this.setString(parameterIndex, arg.toString());
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 70;
        } else {
            this.setNull(parameterIndex, 1);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private final void streamToBytes(Buffer packet, InputStream in, boolean escape, int streamLength, boolean useLength) throws SQLException {
        block26: {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                try {
                    if (this.streamConvertBuf == null) {
                        this.streamConvertBuf = new byte[4096];
                    }
                    String connectionEncoding = this.connection.getEncoding();
                    boolean hexEscape = false;
                    try {
                        if (this.connection.isNoBackslashEscapesSet() || this.connection.getUseUnicode() && connectionEncoding != null && CharsetMapping.isMultibyteCharset(connectionEncoding) && !this.connection.parserKnowsUnicode()) {
                            hexEscape = true;
                        }
                    }
                    catch (RuntimeException ex) {
                        SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
                        sqlEx.initCause(ex);
                        throw sqlEx;
                    }
                    if (streamLength == -1) {
                        useLength = false;
                    }
                    int bc = -1;
                    bc = useLength ? this.readblock(in, this.streamConvertBuf, streamLength) : this.readblock(in, this.streamConvertBuf);
                    int lengthLeftToRead = streamLength - bc;
                    if (hexEscape) {
                        packet.writeStringNoNull("x");
                    } else if (this.connection.getIO().versionMeetsMinimum(4, 1, 0)) {
                        packet.writeStringNoNull("_binary");
                    }
                    if (escape) {
                        packet.writeByte((byte)39);
                    }
                    while (bc > 0) {
                        if (hexEscape) {
                            this.hexEscapeBlock(this.streamConvertBuf, packet, bc);
                        } else if (escape) {
                            this.escapeblockFast(this.streamConvertBuf, packet, bc);
                        } else {
                            packet.writeBytesNoNull(this.streamConvertBuf, 0, bc);
                        }
                        if (useLength) {
                            bc = this.readblock(in, this.streamConvertBuf, lengthLeftToRead);
                            if (bc <= 0) continue;
                            lengthLeftToRead -= bc;
                            continue;
                        }
                        bc = this.readblock(in, this.streamConvertBuf);
                    }
                    if (escape) {
                        packet.writeByte((byte)39);
                    }
                    Object var12_13 = null;
                }
                catch (Throwable throwable) {
                    Object var12_14 = null;
                    if (this.connection.getAutoClosePStmtStreams()) {
                        try {
                            in.close();
                        }
                        catch (IOException ioEx) {
                            // empty catch block
                        }
                        in = null;
                    }
                    throw throwable;
                }
                if (!this.connection.getAutoClosePStmtStreams()) break block26;
                try {
                    in.close();
                }
                catch (IOException ioEx) {
                    // empty catch block
                }
                in = null;
                {
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive exception aggregation
     */
    private final byte[] streamToBytes(InputStream in, boolean escape, int streamLength, boolean useLength) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            byte[] byArray;
            block23: {
                in.mark(Integer.MAX_VALUE);
                try {
                    if (this.streamConvertBuf == null) {
                        this.streamConvertBuf = new byte[4096];
                    }
                    if (streamLength == -1) {
                        useLength = false;
                    }
                    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                    int bc = -1;
                    bc = useLength ? this.readblock(in, this.streamConvertBuf, streamLength) : this.readblock(in, this.streamConvertBuf);
                    int lengthLeftToRead = streamLength - bc;
                    if (escape) {
                        if (this.connection.versionMeetsMinimum(4, 1, 0)) {
                            bytesOut.write(95);
                            bytesOut.write(98);
                            bytesOut.write(105);
                            bytesOut.write(110);
                            bytesOut.write(97);
                            bytesOut.write(114);
                            bytesOut.write(121);
                        }
                        bytesOut.write(39);
                    }
                    while (bc > 0) {
                        if (escape) {
                            this.escapeblockFast(this.streamConvertBuf, bytesOut, bc);
                        } else {
                            bytesOut.write(this.streamConvertBuf, 0, bc);
                        }
                        if (useLength) {
                            bc = this.readblock(in, this.streamConvertBuf, lengthLeftToRead);
                            if (bc <= 0) continue;
                            lengthLeftToRead -= bc;
                            continue;
                        }
                        bc = this.readblock(in, this.streamConvertBuf);
                    }
                    if (escape) {
                        bytesOut.write(39);
                    }
                    byArray = bytesOut.toByteArray();
                    Object var11_10 = null;
                }
                catch (Throwable throwable) {
                    Object var11_11 = null;
                    try {
                        in.reset();
                    }
                    catch (IOException e) {
                        // empty catch block
                    }
                    if (this.connection.getAutoClosePStmtStreams()) {
                        try {
                            in.close();
                        }
                        catch (IOException ioEx) {
                            // empty catch block
                        }
                        in = null;
                    }
                    throw throwable;
                }
                try {
                    in.reset();
                }
                catch (IOException e) {
                    // empty catch block
                }
                if (!this.connection.getAutoClosePStmtStreams()) break block23;
                try {
                    in.close();
                }
                catch (IOException ioEx) {
                    // empty catch block
                }
                in = null;
            }
            return byArray;
        }
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append(": ");
        try {
            buf.append(this.asSql());
        }
        catch (SQLException sqlEx) {
            buf.append("EXCEPTION: " + sqlEx.toString());
        }
        return buf.toString();
    }

    protected int getParameterIndexOffset() {
        return 0;
    }

    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        this.setAsciiStream(parameterIndex, x, -1);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        this.setAsciiStream(parameterIndex, x, (int)length);
        this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2005;
    }

    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        this.setBinaryStream(parameterIndex, x, -1);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        this.setBinaryStream(parameterIndex, x, (int)length);
    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        this.setBinaryStream(parameterIndex, inputStream);
    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        this.setCharacterStream(parameterIndex, reader, -1);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        this.setCharacterStream(parameterIndex, reader, (int)length);
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        this.setCharacterStream(parameterIndex, reader);
    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        this.setCharacterStream(parameterIndex, reader, length);
    }

    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        this.setNCharacterStream(parameterIndex, value, -1L);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setNString(int parameterIndex, String x) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.charEncoding.equalsIgnoreCase("UTF-8") || this.charEncoding.equalsIgnoreCase("utf8")) {
                this.setString(parameterIndex, x);
                return;
            }
            if (x == null) {
                this.setNull(parameterIndex, 1);
            } else {
                int stringLength = x.length();
                StringBuilder buf = new StringBuilder((int)((double)x.length() * 1.1 + 4.0));
                buf.append("_utf8");
                buf.append('\'');
                block12: for (int i = 0; i < stringLength; ++i) {
                    char c = x.charAt(i);
                    switch (c) {
                        case '\u0000': {
                            buf.append('\\');
                            buf.append('0');
                            continue block12;
                        }
                        case '\n': {
                            buf.append('\\');
                            buf.append('n');
                            continue block12;
                        }
                        case '\r': {
                            buf.append('\\');
                            buf.append('r');
                            continue block12;
                        }
                        case '\\': {
                            buf.append('\\');
                            buf.append('\\');
                            continue block12;
                        }
                        case '\'': {
                            buf.append('\\');
                            buf.append('\'');
                            continue block12;
                        }
                        case '\"': {
                            if (this.usingAnsiMode) {
                                buf.append('\\');
                            }
                            buf.append('\"');
                            continue block12;
                        }
                        case '\u001a': {
                            buf.append('\\');
                            buf.append('Z');
                            continue block12;
                        }
                        default: {
                            buf.append(c);
                        }
                    }
                }
                buf.append('\'');
                String parameterAsString = buf.toString();
                byte[] parameterAsBytes = null;
                parameterAsBytes = !this.isLoadDataQuery ? StringUtils.getBytes(parameterAsString, this.connection.getCharsetConverter("UTF-8"), "UTF-8", this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor()) : StringUtils.getBytes(parameterAsString);
                this.setInternal(parameterIndex, parameterAsBytes);
                this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = -9;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setNCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            try {
                if (reader == null) {
                    this.setNull(parameterIndex, -1);
                } else {
                    char[] c = null;
                    int len = 0;
                    boolean useLength = this.connection.getUseStreamLengthsInPrepStmts();
                    if (useLength && length != -1L) {
                        c = new char[(int)length];
                        int numCharsRead = PreparedStatement.readFully(reader, c, (int)length);
                        this.setNString(parameterIndex, new String(c, 0, numCharsRead));
                    } else {
                        c = new char[4096];
                        StringBuilder buf = new StringBuilder();
                        while ((len = reader.read(c)) != -1) {
                            buf.append(c, 0, len);
                        }
                        this.setNString(parameterIndex, buf.toString());
                    }
                    this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2011;
                }
            }
            catch (IOException ioEx) {
                throw SQLError.createSQLException(ioEx.toString(), "S1000", this.getExceptionInterceptor());
            }
        }
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        this.setNCharacterStream(parameterIndex, reader);
    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        if (reader == null) {
            this.setNull(parameterIndex, -1);
        } else {
            this.setNCharacterStream(parameterIndex, reader, length);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ParameterBindings getParameterBindings() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return new EmulatedPreparedStatementBindings();
        }
    }

    public String getPreparedSql() {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                if (this.rewrittenBatchSize == 0) {
                    return this.originalSql;
                }
                try {
                    return this.parseInfo.getSqlForBatch(this.parseInfo);
                }
                catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getUpdateCount() throws SQLException {
        int count = super.getUpdateCount();
        if (this.containsOnDuplicateKeyUpdateInSQL() && this.compensateForOnDuplicateKeyUpdate && (count == 2 || count == 0)) {
            count = 1;
        }
        return count;
    }

    protected static boolean canRewrite(String sql, boolean isOnDuplicateKeyUpdate, int locationOfOnDuplicateKeyUpdate, int statementStartPos) {
        if (StringUtils.startsWithIgnoreCaseAndWs(sql, "INSERT", statementStartPos)) {
            int updateClausePos;
            if (StringUtils.indexOfIgnoreCase(statementStartPos, sql, "SELECT", "\"'`", "\"'`", StringUtils.SEARCH_MODE__MRK_COM_WS) != -1) {
                return false;
            }
            if (isOnDuplicateKeyUpdate && (updateClausePos = StringUtils.indexOfIgnoreCase(locationOfOnDuplicateKeyUpdate, sql, " UPDATE ")) != -1) {
                return StringUtils.indexOfIgnoreCase(updateClausePos, sql, "LAST_INSERT_ID", "\"'`", "\"'`", StringUtils.SEARCH_MODE__MRK_COM_WS) == -1;
            }
            return true;
        }
        return StringUtils.startsWithIgnoreCaseAndWs(sql, "REPLACE", statementStartPos) && StringUtils.indexOfIgnoreCase(statementStartPos, sql, "SELECT", "\"'`", "\"'`", StringUtils.SEARCH_MODE__MRK_COM_WS) == -1;
    }

    public long executeLargeUpdate() throws SQLException {
        return this.executeUpdateInternal(true, false);
    }

    static {
        if (Util.isJdbc4()) {
            try {
                String jdbc4ClassName = Util.isJdbc42() ? "com.mysql.jdbc.JDBC42PreparedStatement" : "com.mysql.jdbc.JDBC4PreparedStatement";
                JDBC_4_PSTMT_2_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(MySQLConnection.class, String.class);
                JDBC_4_PSTMT_3_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(MySQLConnection.class, String.class, String.class);
                JDBC_4_PSTMT_4_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(MySQLConnection.class, String.class, String.class, ParseInfo.class);
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
            JDBC_4_PSTMT_2_ARG_CTOR = null;
            JDBC_4_PSTMT_3_ARG_CTOR = null;
            JDBC_4_PSTMT_4_ARG_CTOR = null;
        }
        HEX_DIGITS = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};
    }

    class EmulatedPreparedStatementBindings
    implements ParameterBindings {
        private ResultSetImpl bindingsAsRs;
        private boolean[] parameterIsNull;

        EmulatedPreparedStatementBindings() throws SQLException {
            ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
            this.parameterIsNull = new boolean[PreparedStatement.this.parameterCount];
            System.arraycopy(PreparedStatement.this.isNull, 0, this.parameterIsNull, 0, PreparedStatement.this.parameterCount);
            byte[][] rowData = new byte[PreparedStatement.this.parameterCount][];
            Field[] typeMetadata = new Field[PreparedStatement.this.parameterCount];
            for (int i = 0; i < PreparedStatement.this.parameterCount; ++i) {
                rowData[i] = PreparedStatement.this.batchCommandIndex == -1 ? PreparedStatement.this.getBytesRepresentation(i) : PreparedStatement.this.getBytesRepresentationForBatch(i, PreparedStatement.this.batchCommandIndex);
                int charsetIndex = 0;
                if (PreparedStatement.this.parameterTypes[i] == -2 || PreparedStatement.this.parameterTypes[i] == 2004) {
                    charsetIndex = 63;
                } else {
                    try {
                        charsetIndex = CharsetMapping.getCollationIndexForJavaEncoding(PreparedStatement.this.connection.getEncoding(), PreparedStatement.this.connection);
                    }
                    catch (SQLException ex) {
                        throw ex;
                    }
                    catch (RuntimeException ex) {
                        SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
                        sqlEx.initCause(ex);
                        throw sqlEx;
                    }
                }
                Field parameterMetadata = new Field(null, "parameter_" + (i + 1), charsetIndex, PreparedStatement.this.parameterTypes[i], rowData[i].length);
                parameterMetadata.setConnection(PreparedStatement.this.connection);
                typeMetadata[i] = parameterMetadata;
            }
            rows.add(new ByteArrayRow(rowData, PreparedStatement.this.getExceptionInterceptor()));
            this.bindingsAsRs = new ResultSetImpl(PreparedStatement.this.connection.getCatalog(), typeMetadata, new RowDataStatic(rows), PreparedStatement.this.connection, null);
            this.bindingsAsRs.next();
        }

        public Array getArray(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getArray(parameterIndex);
        }

        public InputStream getAsciiStream(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getAsciiStream(parameterIndex);
        }

        public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBigDecimal(parameterIndex);
        }

        public InputStream getBinaryStream(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBinaryStream(parameterIndex);
        }

        public Blob getBlob(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBlob(parameterIndex);
        }

        public boolean getBoolean(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBoolean(parameterIndex);
        }

        public byte getByte(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getByte(parameterIndex);
        }

        public byte[] getBytes(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBytes(parameterIndex);
        }

        public Reader getCharacterStream(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getCharacterStream(parameterIndex);
        }

        public Clob getClob(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getClob(parameterIndex);
        }

        public java.sql.Date getDate(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getDate(parameterIndex);
        }

        public double getDouble(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getDouble(parameterIndex);
        }

        public float getFloat(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getFloat(parameterIndex);
        }

        public int getInt(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getInt(parameterIndex);
        }

        public long getLong(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getLong(parameterIndex);
        }

        public Reader getNCharacterStream(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getCharacterStream(parameterIndex);
        }

        public Reader getNClob(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getCharacterStream(parameterIndex);
        }

        public Object getObject(int parameterIndex) throws SQLException {
            PreparedStatement.this.checkBounds(parameterIndex, 0);
            if (this.parameterIsNull[parameterIndex - 1]) {
                return null;
            }
            switch (PreparedStatement.this.parameterTypes[parameterIndex - 1]) {
                case -6: {
                    return this.getByte(parameterIndex);
                }
                case 5: {
                    return this.getShort(parameterIndex);
                }
                case 4: {
                    return this.getInt(parameterIndex);
                }
                case -5: {
                    return this.getLong(parameterIndex);
                }
                case 6: {
                    return Float.valueOf(this.getFloat(parameterIndex));
                }
                case 8: {
                    return this.getDouble(parameterIndex);
                }
            }
            return this.bindingsAsRs.getObject(parameterIndex);
        }

        public Ref getRef(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getRef(parameterIndex);
        }

        public short getShort(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getShort(parameterIndex);
        }

        public String getString(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getString(parameterIndex);
        }

        public Time getTime(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getTime(parameterIndex);
        }

        public Timestamp getTimestamp(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getTimestamp(parameterIndex);
        }

        public URL getURL(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getURL(parameterIndex);
        }

        public boolean isNull(int parameterIndex) throws SQLException {
            PreparedStatement.this.checkBounds(parameterIndex, 0);
            return this.parameterIsNull[parameterIndex - 1];
        }
    }

    static class AppendingBatchVisitor
    implements BatchVisitor {
        LinkedList<byte[]> statementComponents = new LinkedList();

        AppendingBatchVisitor() {
        }

        public BatchVisitor append(byte[] values) {
            this.statementComponents.addLast(values);
            return this;
        }

        public BatchVisitor increment() {
            return this;
        }

        public BatchVisitor decrement() {
            this.statementComponents.removeLast();
            return this;
        }

        public BatchVisitor merge(byte[] front, byte[] back) {
            int mergedLength = front.length + back.length;
            byte[] merged = new byte[mergedLength];
            System.arraycopy(front, 0, merged, 0, front.length);
            System.arraycopy(back, 0, merged, front.length, back.length);
            this.statementComponents.addLast(merged);
            return this;
        }

        public byte[][] getStaticSqlStrings() {
            byte[][] asBytes = new byte[this.statementComponents.size()][];
            this.statementComponents.toArray((T[])asBytes);
            return asBytes;
        }

        public String toString() {
            StringBuilder buf = new StringBuilder();
            Iterator iter = this.statementComponents.iterator();
            while (iter.hasNext()) {
                buf.append(StringUtils.toString((byte[])iter.next()));
            }
            return buf.toString();
        }
    }

    static interface BatchVisitor {
        public BatchVisitor increment();

        public BatchVisitor decrement();

        public BatchVisitor append(byte[] var1);

        public BatchVisitor merge(byte[] var1, byte[] var2);
    }

    public static final class ParseInfo {
        char firstStmtChar = '\u0000';
        boolean foundLoadData = false;
        long lastUsed = 0L;
        int statementLength = 0;
        int statementStartPos = 0;
        boolean canRewriteAsMultiValueInsert = false;
        byte[][] staticSql = null;
        boolean isOnDuplicateKeyUpdate = false;
        int locationOfOnDuplicateKeyUpdate = -1;
        String valuesClause;
        boolean parametersInDuplicateKeyClause = false;
        String charEncoding;
        private ParseInfo batchHead;
        private ParseInfo batchValues;
        private ParseInfo batchODKUClause;

        ParseInfo(String sql, MySQLConnection conn, DatabaseMetaData dbmd, String encoding, SingleByteCharsetConverter converter) throws SQLException {
            this(sql, conn, dbmd, encoding, converter, true);
        }

        public ParseInfo(String sql, MySQLConnection conn, DatabaseMetaData dbmd, String encoding, SingleByteCharsetConverter converter, boolean buildRewriteInfo) throws SQLException {
            try {
                int i;
                if (sql == null) {
                    throw SQLError.createSQLException(Messages.getString("PreparedStatement.61"), "S1009", conn.getExceptionInterceptor());
                }
                this.charEncoding = encoding;
                this.lastUsed = System.currentTimeMillis();
                String quotedIdentifierString = dbmd.getIdentifierQuoteString();
                char quotedIdentifierChar = '\u0000';
                if (quotedIdentifierString != null && !quotedIdentifierString.equals(" ") && quotedIdentifierString.length() > 0) {
                    quotedIdentifierChar = quotedIdentifierString.charAt(0);
                }
                this.statementLength = sql.length();
                ArrayList<int[]> endpointList = new ArrayList<int[]>();
                boolean inQuotes = false;
                char quoteChar = '\u0000';
                boolean inQuotedId = false;
                int lastParmEnd = 0;
                boolean noBackslashEscapes = conn.isNoBackslashEscapesSet();
                for (i = this.statementStartPos = StatementImpl.findStartOfStatement(sql); i < this.statementLength; ++i) {
                    char c = sql.charAt(i);
                    if (this.firstStmtChar == '\u0000' && Character.isLetter(c)) {
                        this.firstStmtChar = Character.toUpperCase(c);
                        if (this.firstStmtChar == 'I') {
                            this.locationOfOnDuplicateKeyUpdate = StatementImpl.getOnDuplicateKeyLocation(sql, conn.getDontCheckOnDuplicateKeyUpdateInSQL(), conn.getRewriteBatchedStatements(), conn.isNoBackslashEscapesSet());
                            boolean bl = this.isOnDuplicateKeyUpdate = this.locationOfOnDuplicateKeyUpdate != -1;
                        }
                    }
                    if (!noBackslashEscapes && c == '\\' && i < this.statementLength - 1) {
                        ++i;
                        continue;
                    }
                    if (!inQuotes && quotedIdentifierChar != '\u0000' && c == quotedIdentifierChar) {
                        inQuotedId = !inQuotedId;
                    } else if (!inQuotedId) {
                        if (inQuotes) {
                            if ((c == '\'' || c == '\"') && c == quoteChar) {
                                if (i < this.statementLength - 1 && sql.charAt(i + 1) == quoteChar) {
                                    ++i;
                                    continue;
                                }
                                inQuotes = !inQuotes;
                                quoteChar = '\u0000';
                            } else if ((c == '\'' || c == '\"') && c == quoteChar) {
                                inQuotes = !inQuotes;
                                quoteChar = '\u0000';
                            }
                        } else {
                            if (c == '#' || c == '-' && i + 1 < this.statementLength && sql.charAt(i + 1) == '-') {
                                int endOfStmt = this.statementLength - 1;
                                while (i < endOfStmt && (c = sql.charAt(i)) != '\r' && c != '\n') {
                                    ++i;
                                }
                                continue;
                            }
                            if (c == '/' && i + 1 < this.statementLength) {
                                char cNext = sql.charAt(i + 1);
                                if (cNext == '*') {
                                    for (int j = i += 2; j < this.statementLength; ++j) {
                                        ++i;
                                        cNext = sql.charAt(j);
                                        if (cNext != '*' || j + 1 >= this.statementLength || sql.charAt(j + 1) != '/') continue;
                                        if (++i < this.statementLength) {
                                            c = sql.charAt(i);
                                        }
                                        break;
                                    }
                                }
                            } else if (c == '\'' || c == '\"') {
                                inQuotes = true;
                                quoteChar = c;
                            }
                        }
                    }
                    if (c != '?' || inQuotes || inQuotedId) continue;
                    endpointList.add(new int[]{lastParmEnd, i});
                    lastParmEnd = i + 1;
                    if (!this.isOnDuplicateKeyUpdate || i <= this.locationOfOnDuplicateKeyUpdate) continue;
                    this.parametersInDuplicateKeyClause = true;
                }
                this.foundLoadData = this.firstStmtChar == 'L' ? StringUtils.startsWithIgnoreCaseAndWs(sql, "LOAD DATA") : false;
                endpointList.add(new int[]{lastParmEnd, this.statementLength});
                this.staticSql = new byte[endpointList.size()][];
                for (i = 0; i < this.staticSql.length; ++i) {
                    int[] ep = (int[])endpointList.get(i);
                    int end = ep[1];
                    int begin = ep[0];
                    int len = end - begin;
                    if (this.foundLoadData) {
                        this.staticSql[i] = StringUtils.getBytes(sql, begin, len);
                        continue;
                    }
                    if (encoding == null) {
                        byte[] buf = new byte[len];
                        for (int j = 0; j < len; ++j) {
                            buf[j] = (byte)sql.charAt(begin + j);
                        }
                        this.staticSql[i] = buf;
                        continue;
                    }
                    this.staticSql[i] = converter != null ? StringUtils.getBytes(sql, converter, encoding, conn.getServerCharset(), begin, len, conn.parserKnowsUnicode(), conn.getExceptionInterceptor()) : StringUtils.getBytes(sql, encoding, conn.getServerCharset(), begin, len, conn.parserKnowsUnicode(), conn, conn.getExceptionInterceptor());
                }
            }
            catch (StringIndexOutOfBoundsException oobEx) {
                SQLException sqlEx = new SQLException("Parse error for " + sql);
                sqlEx.initCause(oobEx);
                throw sqlEx;
            }
            if (buildRewriteInfo) {
                boolean bl = this.canRewriteAsMultiValueInsert = PreparedStatement.canRewrite(sql, this.isOnDuplicateKeyUpdate, this.locationOfOnDuplicateKeyUpdate, this.statementStartPos) && !this.parametersInDuplicateKeyClause;
                if (this.canRewriteAsMultiValueInsert && conn.getRewriteBatchedStatements()) {
                    this.buildRewriteBatchedParams(sql, conn, dbmd, encoding, converter);
                }
            }
        }

        private void buildRewriteBatchedParams(String sql, MySQLConnection conn, DatabaseMetaData metadata, String encoding, SingleByteCharsetConverter converter) throws SQLException {
            this.valuesClause = this.extractValuesClause(sql, conn.getMetaData().getIdentifierQuoteString());
            String odkuClause = this.isOnDuplicateKeyUpdate ? sql.substring(this.locationOfOnDuplicateKeyUpdate) : null;
            String headSql = null;
            headSql = this.isOnDuplicateKeyUpdate ? sql.substring(0, this.locationOfOnDuplicateKeyUpdate) : sql;
            this.batchHead = new ParseInfo(headSql, conn, metadata, encoding, converter, false);
            this.batchValues = new ParseInfo("," + this.valuesClause, conn, metadata, encoding, converter, false);
            this.batchODKUClause = null;
            if (odkuClause != null && odkuClause.length() > 0) {
                this.batchODKUClause = new ParseInfo("," + this.valuesClause + " " + odkuClause, conn, metadata, encoding, converter, false);
            }
        }

        private String extractValuesClause(String sql, String quoteCharStr) throws SQLException {
            int indexOfValues = -1;
            int valuesSearchStart = this.statementStartPos;
            while (indexOfValues == -1 && (indexOfValues = quoteCharStr.length() > 0 ? StringUtils.indexOfIgnoreCase(valuesSearchStart, sql, "VALUES", quoteCharStr, quoteCharStr, StringUtils.SEARCH_MODE__MRK_COM_WS) : StringUtils.indexOfIgnoreCase(valuesSearchStart, sql, "VALUES")) > 0) {
                char c = sql.charAt(indexOfValues - 1);
                if (!Character.isWhitespace(c) && c != ')' && c != '`') {
                    valuesSearchStart = indexOfValues + 6;
                    indexOfValues = -1;
                    continue;
                }
                c = sql.charAt(indexOfValues + 6);
                if (Character.isWhitespace(c) || c == '(') continue;
                valuesSearchStart = indexOfValues + 6;
                indexOfValues = -1;
            }
            if (indexOfValues == -1) {
                return null;
            }
            int indexOfFirstParen = sql.indexOf(40, indexOfValues + 6);
            if (indexOfFirstParen == -1) {
                return null;
            }
            int endOfValuesClause = sql.lastIndexOf(41);
            if (endOfValuesClause == -1) {
                return null;
            }
            if (this.isOnDuplicateKeyUpdate) {
                endOfValuesClause = this.locationOfOnDuplicateKeyUpdate - 1;
            }
            return sql.substring(indexOfFirstParen, endOfValuesClause + 1);
        }

        synchronized ParseInfo getParseInfoForBatch(int numBatch) {
            AppendingBatchVisitor apv = new AppendingBatchVisitor();
            this.buildInfoForBatch(numBatch, apv);
            ParseInfo batchParseInfo = new ParseInfo(apv.getStaticSqlStrings(), this.firstStmtChar, this.foundLoadData, this.isOnDuplicateKeyUpdate, this.locationOfOnDuplicateKeyUpdate, this.statementLength, this.statementStartPos);
            return batchParseInfo;
        }

        String getSqlForBatch(int numBatch) throws UnsupportedEncodingException {
            ParseInfo batchInfo = this.getParseInfoForBatch(numBatch);
            return this.getSqlForBatch(batchInfo);
        }

        String getSqlForBatch(ParseInfo batchInfo) throws UnsupportedEncodingException {
            int size = 0;
            byte[][] sqlStrings = batchInfo.staticSql;
            int sqlStringsLength = sqlStrings.length;
            for (int i = 0; i < sqlStringsLength; ++i) {
                size += sqlStrings[i].length;
                ++size;
            }
            StringBuilder buf = new StringBuilder(size);
            for (int i = 0; i < sqlStringsLength - 1; ++i) {
                buf.append(StringUtils.toString(sqlStrings[i], this.charEncoding));
                buf.append("?");
            }
            buf.append(StringUtils.toString(sqlStrings[sqlStringsLength - 1]));
            return buf.toString();
        }

        private void buildInfoForBatch(int numBatch, BatchVisitor visitor) {
            byte[][] headStaticSql = this.batchHead.staticSql;
            int headStaticSqlLength = headStaticSql.length;
            if (headStaticSqlLength > 1) {
                for (int i = 0; i < headStaticSqlLength - 1; ++i) {
                    visitor.append(headStaticSql[i]).increment();
                }
            }
            byte[] endOfHead = headStaticSql[headStaticSqlLength - 1];
            byte[][] valuesStaticSql = this.batchValues.staticSql;
            byte[] beginOfValues = valuesStaticSql[0];
            visitor.merge(endOfHead, beginOfValues).increment();
            int numValueRepeats = numBatch - 1;
            if (this.batchODKUClause != null) {
                --numValueRepeats;
            }
            int valuesStaticSqlLength = valuesStaticSql.length;
            byte[] endOfValues = valuesStaticSql[valuesStaticSqlLength - 1];
            for (int i = 0; i < numValueRepeats; ++i) {
                for (int j = 1; j < valuesStaticSqlLength - 1; ++j) {
                    visitor.append(valuesStaticSql[j]).increment();
                }
                visitor.merge(endOfValues, beginOfValues).increment();
            }
            if (this.batchODKUClause != null) {
                byte[][] batchOdkuStaticSql = this.batchODKUClause.staticSql;
                byte[] beginOfOdku = batchOdkuStaticSql[0];
                visitor.decrement().merge(endOfValues, beginOfOdku).increment();
                int batchOdkuStaticSqlLength = batchOdkuStaticSql.length;
                if (numBatch > 1) {
                    for (int i = 1; i < batchOdkuStaticSqlLength; ++i) {
                        visitor.append(batchOdkuStaticSql[i]).increment();
                    }
                } else {
                    visitor.decrement().append(batchOdkuStaticSql[batchOdkuStaticSqlLength - 1]);
                }
            } else {
                visitor.decrement().append(this.staticSql[this.staticSql.length - 1]);
            }
        }

        private ParseInfo(byte[][] staticSql, char firstStmtChar, boolean foundLoadData, boolean isOnDuplicateKeyUpdate, int locationOfOnDuplicateKeyUpdate, int statementLength, int statementStartPos) {
            this.firstStmtChar = firstStmtChar;
            this.foundLoadData = foundLoadData;
            this.isOnDuplicateKeyUpdate = isOnDuplicateKeyUpdate;
            this.locationOfOnDuplicateKeyUpdate = locationOfOnDuplicateKeyUpdate;
            this.statementLength = statementLength;
            this.statementStartPos = statementStartPos;
            this.staticSql = staticSql;
        }
    }

    class EndPoint {
        int begin;
        int end;

        EndPoint(int b, int e) {
            this.begin = b;
            this.end = e;
        }
    }

    public class BatchParams {
        public boolean[] isNull = null;
        public boolean[] isStream = null;
        public InputStream[] parameterStreams = null;
        public byte[][] parameterStrings = null;
        public int[] streamLengths = null;

        BatchParams(byte[][] strings, InputStream[] streams, boolean[] isStreamFlags, int[] lengths, boolean[] isNullFlags) {
            this.parameterStrings = new byte[strings.length][];
            this.parameterStreams = new InputStream[streams.length];
            this.isStream = new boolean[isStreamFlags.length];
            this.streamLengths = new int[lengths.length];
            this.isNull = new boolean[isNullFlags.length];
            System.arraycopy(strings, 0, this.parameterStrings, 0, strings.length);
            System.arraycopy(streams, 0, this.parameterStreams, 0, streams.length);
            System.arraycopy(isStreamFlags, 0, this.isStream, 0, isStreamFlags.length);
            System.arraycopy(lengths, 0, this.streamLengths, 0, lengths.length);
            System.arraycopy(isNullFlags, 0, this.isNull, 0, isNullFlags.length);
        }
    }
}


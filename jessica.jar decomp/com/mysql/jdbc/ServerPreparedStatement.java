/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Buffer;
import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.MysqlIO;
import com.mysql.jdbc.MysqlParameterMetadata;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ProfilerEventHandlerFactory;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.StatementImpl;
import com.mysql.jdbc.StringUtils;
import com.mysql.jdbc.TimeUtil;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.Wrapper;
import com.mysql.jdbc.exceptions.MySQLStatementCancelledException;
import com.mysql.jdbc.exceptions.MySQLTimeoutException;
import com.mysql.jdbc.log.LogUtils;
import com.mysql.jdbc.profiler.ProfilerEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.TimerTask;

public class ServerPreparedStatement
extends PreparedStatement {
    private static final Constructor<?> JDBC_4_SPS_CTOR;
    protected static final int BLOB_STREAM_READ_BUF_SIZE = 8192;
    private boolean hasOnDuplicateKeyUpdate = false;
    private boolean detectedLongParameterSwitch = false;
    private int fieldCount;
    private boolean invalid = false;
    private SQLException invalidationException;
    private Buffer outByteBuffer;
    private BindValue[] parameterBindings;
    private Field[] parameterFields;
    private Field[] resultFields;
    private boolean sendTypesToServer = false;
    private long serverStatementId;
    private int stringTypeCode = 254;
    private boolean serverNeedsResetBeforeEachExecution;
    protected boolean isCached = false;
    private boolean useAutoSlowLog;
    private Calendar serverTzCalendar;
    private Calendar defaultTzCalendar;
    private boolean hasCheckedRewrite = false;
    private boolean canRewrite = false;
    private int locationOfOnDuplicateKeyUpdate = -2;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void storeTime(Buffer intoBuf, Time tm) throws SQLException {
        Calendar sessionCalendar;
        intoBuf.ensureCapacity(9);
        intoBuf.writeByte((byte)8);
        intoBuf.writeByte((byte)0);
        intoBuf.writeLong(0L);
        Calendar calendar = sessionCalendar = this.getCalendarInstanceForSessionOrNew();
        synchronized (calendar) {
            java.util.Date oldTime = sessionCalendar.getTime();
            try {
                sessionCalendar.setTime(tm);
                intoBuf.writeByte((byte)sessionCalendar.get(11));
                intoBuf.writeByte((byte)sessionCalendar.get(12));
                intoBuf.writeByte((byte)sessionCalendar.get(13));
                Object var7_6 = null;
                sessionCalendar.setTime(oldTime);
            }
            catch (Throwable throwable) {
                Object var7_7 = null;
                sessionCalendar.setTime(oldTime);
                throw throwable;
            }
        }
    }

    protected static ServerPreparedStatement getInstance(MySQLConnection conn, String sql, String catalog, int resultSetType, int resultSetConcurrency) throws SQLException {
        if (!Util.isJdbc4()) {
            return new ServerPreparedStatement(conn, sql, catalog, resultSetType, resultSetConcurrency);
        }
        try {
            return (ServerPreparedStatement)JDBC_4_SPS_CTOR.newInstance(conn, sql, catalog, resultSetType, resultSetConcurrency);
        }
        catch (IllegalArgumentException e) {
            throw new SQLException(e.toString(), "S1000");
        }
        catch (InstantiationException e) {
            throw new SQLException(e.toString(), "S1000");
        }
        catch (IllegalAccessException e) {
            throw new SQLException(e.toString(), "S1000");
        }
        catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof SQLException) {
                throw (SQLException)target;
            }
            throw new SQLException(target.toString(), "S1000");
        }
    }

    protected ServerPreparedStatement(MySQLConnection conn, String sql, String catalog, int resultSetType, int resultSetConcurrency) throws SQLException {
        super(conn, catalog);
        this.checkNullOrEmptyQuery(sql);
        int startOfStatement = ServerPreparedStatement.findStartOfStatement(sql);
        this.firstCharOfStmt = StringUtils.firstAlphaCharUc(sql, startOfStatement);
        boolean bl = this.hasOnDuplicateKeyUpdate = this.firstCharOfStmt == 'I' && this.containsOnDuplicateKeyInString(sql);
        this.serverNeedsResetBeforeEachExecution = this.connection.versionMeetsMinimum(5, 0, 0) ? !this.connection.versionMeetsMinimum(5, 0, 3) : !this.connection.versionMeetsMinimum(4, 1, 10);
        this.useAutoSlowLog = this.connection.getAutoSlowLog();
        this.useTrueBoolean = this.connection.versionMeetsMinimum(3, 21, 23);
        String statementComment = this.connection.getStatementComment();
        this.originalSql = statementComment == null ? sql : "/* " + statementComment + " */ " + sql;
        this.stringTypeCode = this.connection.versionMeetsMinimum(4, 1, 2) ? 253 : 254;
        try {
            this.serverPrepare(sql);
        }
        catch (SQLException sqlEx) {
            this.realClose(false, true);
            throw sqlEx;
        }
        catch (Exception ex) {
            this.realClose(false, true);
            SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1000", this.getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
        this.setResultSetType(resultSetType);
        this.setResultSetConcurrency(resultSetConcurrency);
        this.parameterTypes = new int[this.parameterCount];
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
            this.batchedArgs.add(new BatchedBindValues(this.parameterBindings));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public String asSql(boolean quoteStreamsAndUnknowns) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            PreparedStatement pStmtForSub = null;
            try {
                pStmtForSub = PreparedStatement.getInstance(this.connection, this.originalSql, this.currentCatalog);
                int numParameters = pStmtForSub.parameterCount;
                int ourNumParameters = this.parameterCount;
                block15: for (int i = 0; i < numParameters && i < ourNumParameters; ++i) {
                    if (this.parameterBindings[i] == null) continue;
                    if (this.parameterBindings[i].isNull) {
                        pStmtForSub.setNull(i + 1, 0);
                        continue;
                    }
                    BindValue bindValue = this.parameterBindings[i];
                    switch (bindValue.bufferType) {
                        case 1: {
                            pStmtForSub.setByte(i + 1, (byte)bindValue.longBinding);
                            continue block15;
                        }
                        case 2: {
                            pStmtForSub.setShort(i + 1, (short)bindValue.longBinding);
                            continue block15;
                        }
                        case 3: {
                            pStmtForSub.setInt(i + 1, (int)bindValue.longBinding);
                            continue block15;
                        }
                        case 8: {
                            pStmtForSub.setLong(i + 1, bindValue.longBinding);
                            continue block15;
                        }
                        case 4: {
                            pStmtForSub.setFloat(i + 1, bindValue.floatBinding);
                            continue block15;
                        }
                        case 5: {
                            pStmtForSub.setDouble(i + 1, bindValue.doubleBinding);
                            continue block15;
                        }
                        default: {
                            pStmtForSub.setObject(i + 1, this.parameterBindings[i].value);
                        }
                    }
                }
                String string = pStmtForSub.asSql(quoteStreamsAndUnknowns);
                Object var9_9 = null;
                if (pStmtForSub == null) return string;
                try {
                    pStmtForSub.close();
                }
                catch (SQLException sqlEx) {
                    // empty catch block
                }
                return string;
            }
            catch (Throwable throwable) {
                Object var9_10 = null;
                if (pStmtForSub == null) throw throwable;
                try {}
                catch (SQLException sqlEx) {
                    throw throwable;
                }
                pStmtForSub.close();
                throw throwable;
            }
        }
    }

    protected MySQLConnection checkClosed() throws SQLException {
        if (this.invalid) {
            throw this.invalidationException;
        }
        return super.checkClosed();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void clearParameters() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.clearParametersInternal(true);
        }
    }

    private void clearParametersInternal(boolean clearServerParameters) throws SQLException {
        boolean hadLongData = false;
        if (this.parameterBindings != null) {
            for (int i = 0; i < this.parameterCount; ++i) {
                if (this.parameterBindings[i] != null && this.parameterBindings[i].isLongData) {
                    hadLongData = true;
                }
                this.parameterBindings[i].reset();
            }
        }
        if (clearServerParameters && hadLongData) {
            this.serverResetStatement();
            this.detectedLongParameterSwitch = false;
        }
    }

    protected void setClosed(boolean flag) {
        this.isClosed = flag;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() throws SQLException {
        MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn == null) {
            return;
        }
        Object object = locallyScopedConn.getConnectionMutex();
        synchronized (object) {
            if (this.isCached && this.isPoolable() && !this.isClosed) {
                this.clearParameters();
                this.isClosed = true;
                this.connection.recachePreparedStatement(this);
                return;
            }
            this.realClose(true, true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void dumpCloseForTestcase() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            StringBuilder buf = new StringBuilder();
            this.connection.generateConnectionCommentBlock(buf);
            buf.append("DEALLOCATE PREPARE debug_stmt_");
            buf.append(this.statementId);
            buf.append(";\n");
            this.connection.dumpTestcaseQuery(buf.toString());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void dumpExecuteForTestcase() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            int i;
            StringBuilder buf = new StringBuilder();
            for (i = 0; i < this.parameterCount; ++i) {
                this.connection.generateConnectionCommentBlock(buf);
                buf.append("SET @debug_stmt_param");
                buf.append(this.statementId);
                buf.append("_");
                buf.append(i);
                buf.append("=");
                if (this.parameterBindings[i].isNull) {
                    buf.append("NULL");
                } else {
                    buf.append(this.parameterBindings[i].toString(true));
                }
                buf.append(";\n");
            }
            this.connection.generateConnectionCommentBlock(buf);
            buf.append("EXECUTE debug_stmt_");
            buf.append(this.statementId);
            if (this.parameterCount > 0) {
                buf.append(" USING ");
                for (i = 0; i < this.parameterCount; ++i) {
                    if (i > 0) {
                        buf.append(", ");
                    }
                    buf.append("@debug_stmt_param");
                    buf.append(this.statementId);
                    buf.append("_");
                    buf.append(i);
                }
            }
            buf.append(";\n");
            this.connection.dumpTestcaseQuery(buf.toString());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void dumpPrepareForTestcase() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            StringBuilder buf = new StringBuilder(this.originalSql.length() + 64);
            this.connection.generateConnectionCommentBlock(buf);
            buf.append("PREPARE debug_stmt_");
            buf.append(this.statementId);
            buf.append(" FROM \"");
            buf.append(this.originalSql);
            buf.append("\";\n");
            this.connection.dumpTestcaseQuery(buf.toString());
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
            long[] lArray;
            MySQLConnection locallyScopedConn = this.connection;
            if (locallyScopedConn.isReadOnly()) {
                throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.2") + Messages.getString("ServerPreparedStatement.3"), "S1009", this.getExceptionInterceptor());
            }
            this.clearWarnings();
            BindValue[] oldBindValues = this.parameterBindings;
            try {
                long[] updateCounts = null;
                if (this.batchedArgs != null) {
                    SQLException sqlEx;
                    block22: {
                        int nbrCommands = this.batchedArgs.size();
                        updateCounts = new long[nbrCommands];
                        if (this.retrieveGeneratedKeys) {
                            this.batchedGeneratedKeys = new ArrayList(nbrCommands);
                        }
                        for (int i = 0; i < nbrCommands; ++i) {
                            updateCounts[i] = -3L;
                        }
                        sqlEx = null;
                        int commandIndex = 0;
                        BindValue[] previousBindValuesForBatch = null;
                        TimerTask timeoutTask = null;
                        try {
                            if (locallyScopedConn.getEnableQueryTimeouts() && batchTimeout != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                                timeoutTask = new StatementImpl.CancelTask(this);
                                locallyScopedConn.getCancelTimer().schedule(timeoutTask, batchTimeout);
                            }
                            for (commandIndex = 0; commandIndex < nbrCommands; ++commandIndex) {
                                Object arg = this.batchedArgs.get(commandIndex);
                                try {
                                    Object var14_16;
                                    if (arg instanceof String) {
                                        updateCounts[commandIndex] = this.executeUpdateInternal((String)arg, true, this.retrieveGeneratedKeys);
                                        this.getBatchedGeneratedKeys(this.results.getFirstCharOfQuery() == 'I' && this.containsOnDuplicateKeyInString((String)arg) ? 1 : 0);
                                        continue;
                                    }
                                    this.parameterBindings = ((BatchedBindValues)arg).batchedParameterValues;
                                    if (previousBindValuesForBatch != null) {
                                        for (int j = 0; j < this.parameterBindings.length; ++j) {
                                            if (this.parameterBindings[j].bufferType == previousBindValuesForBatch[j].bufferType) continue;
                                            this.sendTypesToServer = true;
                                            break;
                                        }
                                    }
                                    try {
                                        updateCounts[commandIndex] = this.executeUpdateInternal(false, true);
                                        var14_16 = null;
                                        previousBindValuesForBatch = this.parameterBindings;
                                    }
                                    catch (Throwable throwable) {
                                        var14_16 = null;
                                        previousBindValuesForBatch = this.parameterBindings;
                                        throw throwable;
                                    }
                                    this.getBatchedGeneratedKeys(this.containsOnDuplicateKeyUpdateInSQL() ? 1 : 0);
                                    continue;
                                }
                                catch (SQLException ex) {
                                    updateCounts[commandIndex] = -3L;
                                    if (this.continueBatchOnError && !(ex instanceof MySQLTimeoutException) && !(ex instanceof MySQLStatementCancelledException) && !this.hasDeadlockOrTimeoutRolledBackTx(ex)) {
                                        sqlEx = ex;
                                        continue;
                                    }
                                    long[] newUpdateCounts = new long[commandIndex];
                                    System.arraycopy(updateCounts, 0, newUpdateCounts, 0, commandIndex);
                                    throw SQLError.createBatchUpdateException(ex, newUpdateCounts, this.getExceptionInterceptor());
                                }
                            }
                            Object var16_19 = null;
                            if (timeoutTask == null) break block22;
                        }
                        catch (Throwable throwable) {
                            Object var16_20 = null;
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
                    if (sqlEx != null) {
                        throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.getExceptionInterceptor());
                    }
                }
                lArray = updateCounts != null ? updateCounts : new long[]{};
                Object var18_22 = null;
                this.parameterBindings = oldBindValues;
                this.sendTypesToServer = true;
            }
            catch (Throwable throwable) {
                Object var18_23 = null;
                this.parameterBindings = oldBindValues;
                this.sendTypesToServer = true;
                this.clearBatch();
                throw throwable;
            }
            this.clearBatch();
            return lArray;
        }
    }

    protected ResultSetInternalMethods executeInternal(int maxRowsToRetrieve, Buffer sendPacket, boolean createStreamingResultSet, boolean queryIsSelectOnly, Field[] metadataFromCache, boolean isBatch) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ++this.numberOfExecutions;
            try {
                return this.serverExecute(maxRowsToRetrieve, createStreamingResultSet, metadataFromCache);
            }
            catch (SQLException sqlEx) {
                if (this.connection.getEnablePacketDebug()) {
                    this.connection.getIO().dumpPacketRingBuffer();
                }
                if (this.connection.getDumpQueriesOnException()) {
                    String extractedSql = this.toString();
                    StringBuilder messageBuf = new StringBuilder(extractedSql.length() + 32);
                    messageBuf.append("\n\nQuery being executed when exception was thrown:\n");
                    messageBuf.append(extractedSql);
                    messageBuf.append("\n\n");
                    sqlEx = ConnectionImpl.appendMessageToException(sqlEx, messageBuf.toString(), this.getExceptionInterceptor());
                }
                throw sqlEx;
            }
            catch (Exception ex) {
                if (this.connection.getEnablePacketDebug()) {
                    this.connection.getIO().dumpPacketRingBuffer();
                }
                SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1000", this.getExceptionInterceptor());
                if (this.connection.getDumpQueriesOnException()) {
                    String extractedSql = this.toString();
                    StringBuilder messageBuf = new StringBuilder(extractedSql.length() + 32);
                    messageBuf.append("\n\nQuery being executed when exception was thrown:\n");
                    messageBuf.append(extractedSql);
                    messageBuf.append("\n\n");
                    sqlEx = ConnectionImpl.appendMessageToException(sqlEx, messageBuf.toString(), this.getExceptionInterceptor());
                }
                sqlEx.initCause(ex);
                throw sqlEx;
            }
        }
    }

    protected Buffer fillSendPacket() throws SQLException {
        return null;
    }

    protected Buffer fillSendPacket(byte[][] batchedParameterStrings, InputStream[] batchedParameterStreams, boolean[] batchedIsStream, int[] batchedStreamLengths) throws SQLException {
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected BindValue getBinding(int parameterIndex, boolean forLongData) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.parameterBindings.length == 0) {
                throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.8"), "S1009", this.getExceptionInterceptor());
            }
            if (--parameterIndex < 0 || parameterIndex >= this.parameterBindings.length) {
                throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.9") + (parameterIndex + 1) + Messages.getString("ServerPreparedStatement.10") + this.parameterBindings.length, "S1009", this.getExceptionInterceptor());
            }
            if (this.parameterBindings[parameterIndex] == null) {
                this.parameterBindings[parameterIndex] = new BindValue();
            } else if (this.parameterBindings[parameterIndex].isLongData && !forLongData) {
                this.detectedLongParameterSwitch = true;
            }
            return this.parameterBindings[parameterIndex];
        }
    }

    public BindValue[] getParameterBindValues() {
        return this.parameterBindings;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    byte[] getBytes(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            BindValue bindValue = this.getBinding(parameterIndex, false);
            if (bindValue.isNull) {
                return null;
            }
            if (bindValue.isLongData) {
                throw SQLError.createSQLFeatureNotSupportedException();
            }
            if (this.outByteBuffer == null) {
                this.outByteBuffer = new Buffer(this.connection.getNetBufferLength());
            }
            this.outByteBuffer.clear();
            int originalPosition = this.outByteBuffer.getPosition();
            this.storeBinding(this.outByteBuffer, bindValue, this.connection.getIO());
            int newPosition = this.outByteBuffer.getPosition();
            int length = newPosition - originalPosition;
            byte[] valueAsBytes = new byte[length];
            System.arraycopy(this.outByteBuffer.getByteBuffer(), originalPosition, valueAsBytes, 0, length);
            return valueAsBytes;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public java.sql.ResultSetMetaData getMetaData() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.resultFields == null) {
                return null;
            }
            return new ResultSetMetaData(this.resultFields, this.connection.getUseOldAliasMetadataBehavior(), this.connection.getYearIsDateType(), this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ParameterMetaData getParameterMetaData() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.parameterMetaData == null) {
                this.parameterMetaData = new MysqlParameterMetadata(this.parameterFields, this.parameterCount, this.getExceptionInterceptor());
            }
            return this.parameterMetaData;
        }
    }

    boolean isNull(int paramIndex) {
        throw new IllegalArgumentException(Messages.getString("ServerPreparedStatement.7"));
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
            if (this.connection != null) {
                if (this.connection.getAutoGenerateTestcaseScript()) {
                    this.dumpCloseForTestcase();
                }
                SQLException exceptionDuringClose = null;
                if (calledExplicitly && !this.connection.isClosed()) {
                    Object object2 = this.connection.getConnectionMutex();
                    synchronized (object2) {
                        try {
                            MysqlIO mysql = this.connection.getIO();
                            Buffer packet = mysql.getSharedSendPacket();
                            packet.writeByte((byte)25);
                            packet.writeLong(this.serverStatementId);
                            mysql.sendCommand(25, null, packet, true, null, 0);
                        }
                        catch (SQLException sqlEx) {
                            exceptionDuringClose = sqlEx;
                        }
                    }
                }
                if (this.isCached) {
                    this.connection.decachePreparedStatement(this);
                }
                super.realClose(calledExplicitly, closeOpenResults);
                this.clearParametersInternal(false);
                this.parameterBindings = null;
                this.parameterFields = null;
                this.resultFields = null;
                if (exceptionDuringClose != null) {
                    throw exceptionDuringClose;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void rePrepare() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.invalidationException = null;
            try {
                this.serverPrepare(this.originalSql);
            }
            catch (SQLException sqlEx) {
                this.invalidationException = sqlEx;
            }
            catch (Exception ex) {
                this.invalidationException = SQLError.createSQLException(ex.toString(), "S1000", this.getExceptionInterceptor());
                this.invalidationException.initCause(ex);
            }
            if (this.invalidationException != null) {
                this.invalid = true;
                this.parameterBindings = null;
                this.parameterFields = null;
                this.resultFields = null;
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
                try {
                    this.closeAllOpenResults();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (this.connection != null && !this.connection.getDontTrackOpenResources()) {
                    this.connection.unregisterStatement(this);
                }
            }
        }
    }

    boolean isCursorRequired() throws SQLException {
        return this.resultFields != null && this.connection.isCursorFetchEnabled() && this.getResultSetType() == 1003 && this.getResultSetConcurrency() == 1007 && this.getFetchSize() > 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private ResultSetInternalMethods serverExecute(int maxRowsToRetrieve, boolean createStreamingResultSet, Field[] metadataFromCache) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods resultSetInternalMethods;
            int i;
            int i2;
            int i3;
            ResultSetInternalMethods interceptedResults;
            MysqlIO mysql = this.connection.getIO();
            if (mysql.shouldIntercept() && (interceptedResults = mysql.invokeStatementInterceptorsPre(this.originalSql, this, true)) != null) {
                return interceptedResults;
            }
            if (this.detectedLongParameterSwitch) {
                boolean firstFound = false;
                long boundTimeToCheck = 0L;
                for (i3 = 0; i3 < this.parameterCount - 1; ++i3) {
                    if (!this.parameterBindings[i3].isLongData) continue;
                    if (firstFound && boundTimeToCheck != this.parameterBindings[i3].boundBeforeExecutionNum) {
                        throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.11") + Messages.getString("ServerPreparedStatement.12"), "S1C00", this.getExceptionInterceptor());
                    }
                    firstFound = true;
                    boundTimeToCheck = this.parameterBindings[i3].boundBeforeExecutionNum;
                }
                this.serverResetStatement();
            }
            for (i2 = 0; i2 < this.parameterCount; ++i2) {
                if (this.parameterBindings[i2].isSet) continue;
                throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.13") + (i2 + 1) + Messages.getString("ServerPreparedStatement.14"), "S1009", this.getExceptionInterceptor());
            }
            for (i2 = 0; i2 < this.parameterCount; ++i2) {
                if (!this.parameterBindings[i2].isLongData) continue;
                this.serverLongData(i2, this.parameterBindings[i2]);
            }
            if (this.connection.getAutoGenerateTestcaseScript()) {
                this.dumpExecuteForTestcase();
            }
            Buffer packet = mysql.getSharedSendPacket();
            packet.clear();
            packet.writeByte((byte)23);
            packet.writeLong(this.serverStatementId);
            if (this.connection.versionMeetsMinimum(4, 1, 2)) {
                if (this.isCursorRequired()) {
                    packet.writeByte((byte)1);
                } else {
                    packet.writeByte((byte)0);
                }
                packet.writeLong(1L);
            }
            int nullCount = (this.parameterCount + 7) / 8;
            int nullBitsPosition = packet.getPosition();
            for (i3 = 0; i3 < nullCount; ++i3) {
                packet.writeByte((byte)0);
            }
            byte[] nullBitsBuffer = new byte[nullCount];
            packet.writeByte(this.sendTypesToServer ? (byte)1 : 0);
            if (this.sendTypesToServer) {
                for (i = 0; i < this.parameterCount; ++i) {
                    packet.writeInt(this.parameterBindings[i].bufferType);
                }
            }
            for (i = 0; i < this.parameterCount; ++i) {
                if (this.parameterBindings[i].isLongData) continue;
                if (!this.parameterBindings[i].isNull) {
                    this.storeBinding(packet, this.parameterBindings[i], mysql);
                    continue;
                }
                int n = i / 8;
                nullBitsBuffer[n] = (byte)(nullBitsBuffer[n] | 1 << (i & 7));
            }
            int endPosition = packet.getPosition();
            packet.setPosition(nullBitsPosition);
            packet.writeBytesNoNull(nullBitsBuffer);
            packet.setPosition(endPosition);
            long begin = 0L;
            boolean logSlowQueries = this.connection.getLogSlowQueries();
            boolean gatherPerformanceMetrics = this.connection.getGatherPerformanceMetrics();
            if (this.profileSQL || logSlowQueries || gatherPerformanceMetrics) {
                begin = mysql.getCurrentTimeNanosOrMillis();
            }
            this.resetCancelledState();
            TimerTask timeoutTask = null;
            try {
                try {
                    ResultSetInternalMethods interceptedResults2;
                    String queryAsString = "";
                    if (this.profileSQL || logSlowQueries || gatherPerformanceMetrics) {
                        queryAsString = this.asSql(true);
                    }
                    if (this.connection.getEnableQueryTimeouts() && this.timeoutInMillis != 0 && this.connection.versionMeetsMinimum(5, 0, 0)) {
                        timeoutTask = new StatementImpl.CancelTask(this);
                        this.connection.getCancelTimer().schedule(timeoutTask, this.timeoutInMillis);
                    }
                    this.statementBegins();
                    Buffer resultPacket = mysql.sendCommand(23, null, packet, false, null, 0);
                    long queryEndTime = 0L;
                    if (logSlowQueries || gatherPerformanceMetrics || this.profileSQL) {
                        queryEndTime = mysql.getCurrentTimeNanosOrMillis();
                    }
                    if (timeoutTask != null) {
                        timeoutTask.cancel();
                        this.connection.getCancelTimer().purge();
                        if (((StatementImpl.CancelTask)timeoutTask).caughtWhileCancelling != null) {
                            throw ((StatementImpl.CancelTask)timeoutTask).caughtWhileCancelling;
                        }
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
                    boolean queryWasSlow = false;
                    if (logSlowQueries || gatherPerformanceMetrics) {
                        long elapsedTime = queryEndTime - begin;
                        if (logSlowQueries) {
                            if (this.useAutoSlowLog) {
                                queryWasSlow = elapsedTime > (long)this.connection.getSlowQueryThresholdMillis();
                            } else {
                                queryWasSlow = this.connection.isAbonormallyLongQuery(elapsedTime);
                                this.connection.reportQueryTime(elapsedTime);
                            }
                        }
                        if (queryWasSlow) {
                            StringBuilder mesgBuf = new StringBuilder(48 + this.originalSql.length());
                            mesgBuf.append(Messages.getString("ServerPreparedStatement.15"));
                            mesgBuf.append(mysql.getSlowQueryThreshold());
                            mesgBuf.append(Messages.getString("ServerPreparedStatement.15a"));
                            mesgBuf.append(elapsedTime);
                            mesgBuf.append(Messages.getString("ServerPreparedStatement.16"));
                            mesgBuf.append("as prepared: ");
                            mesgBuf.append(this.originalSql);
                            mesgBuf.append("\n\n with parameters bound:\n\n");
                            mesgBuf.append(queryAsString);
                            this.eventSink.consumeEvent(new ProfilerEvent(6, "", this.currentCatalog, this.connection.getId(), this.getId(), 0, System.currentTimeMillis(), elapsedTime, mysql.getQueryTimingUnits(), null, LogUtils.findCallingClassAndMethod(new Throwable()), mesgBuf.toString()));
                        }
                        if (gatherPerformanceMetrics) {
                            this.connection.registerQueryExecutionTime(elapsedTime);
                        }
                    }
                    this.connection.incrementNumberOfPreparedExecutes();
                    if (this.profileSQL) {
                        this.eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
                        this.eventSink.consumeEvent(new ProfilerEvent(4, "", this.currentCatalog, this.connectionId, this.statementId, -1, System.currentTimeMillis(), mysql.getCurrentTimeNanosOrMillis() - begin, mysql.getQueryTimingUnits(), null, LogUtils.findCallingClassAndMethod(new Throwable()), this.truncateQueryToLog(queryAsString)));
                    }
                    ResultSetInternalMethods rs = mysql.readAllResults(this, maxRowsToRetrieve, this.resultSetType, this.resultSetConcurrency, createStreamingResultSet, this.currentCatalog, resultPacket, true, this.fieldCount, metadataFromCache);
                    if (mysql.shouldIntercept() && (interceptedResults2 = mysql.invokeStatementInterceptorsPost(this.originalSql, this, rs, true, null)) != null) {
                        rs = interceptedResults2;
                    }
                    if (this.profileSQL) {
                        long fetchEndTime = mysql.getCurrentTimeNanosOrMillis();
                        this.eventSink.consumeEvent(new ProfilerEvent(5, "", this.currentCatalog, this.connection.getId(), this.getId(), 0, System.currentTimeMillis(), fetchEndTime - queryEndTime, mysql.getQueryTimingUnits(), null, LogUtils.findCallingClassAndMethod(new Throwable()), null));
                    }
                    if (queryWasSlow && this.connection.getExplainSlowQueries()) {
                        mysql.explainSlowQuery(StringUtils.getBytes(queryAsString), queryAsString);
                    }
                    if (!createStreamingResultSet && this.serverNeedsResetBeforeEachExecution) {
                        this.serverResetStatement();
                    }
                    this.sendTypesToServer = false;
                    this.results = rs;
                    if (mysql.hadWarnings()) {
                        mysql.scanForAndThrowDataTruncation();
                    }
                    resultSetInternalMethods = rs;
                    Object var25_32 = null;
                    this.statementExecuting.set(false);
                    if (timeoutTask == null) return resultSetInternalMethods;
                }
                catch (SQLException sqlEx) {
                    if (!mysql.shouldIntercept()) throw sqlEx;
                    mysql.invokeStatementInterceptorsPost(this.originalSql, this, null, true, sqlEx);
                    throw sqlEx;
                }
            }
            catch (Throwable throwable) {
                Object var25_33 = null;
                this.statementExecuting.set(false);
                if (timeoutTask == null) throw throwable;
                timeoutTask.cancel();
                this.connection.getCancelTimer().purge();
                throw throwable;
            }
            timeoutTask.cancel();
            this.connection.getCancelTimer().purge();
            return resultSetInternalMethods;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void serverLongData(int parameterIndex, BindValue longData) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            MysqlIO mysql = this.connection.getIO();
            Buffer packet = mysql.getSharedSendPacket();
            Object value = longData.value;
            if (value instanceof byte[]) {
                packet.clear();
                packet.writeByte((byte)24);
                packet.writeLong(this.serverStatementId);
                packet.writeInt(parameterIndex);
                packet.writeBytesNoNull((byte[])longData.value);
                mysql.sendCommand(24, null, packet, true, null, 0);
            } else if (value instanceof InputStream) {
                this.storeStream(mysql, parameterIndex, packet, (InputStream)value);
            } else if (value instanceof Blob) {
                this.storeStream(mysql, parameterIndex, packet, ((Blob)value).getBinaryStream());
            } else if (value instanceof Reader) {
                this.storeReader(mysql, parameterIndex, packet, (Reader)value);
            } else {
                throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.18") + value.getClass().getName() + "'", "S1009", this.getExceptionInterceptor());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void serverPrepare(String sql) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            MysqlIO mysql = this.connection.getIO();
            if (this.connection.getAutoGenerateTestcaseScript()) {
                this.dumpPrepareForTestcase();
            }
            try {
                block19: {
                    try {
                        int i;
                        boolean checkEOF;
                        long begin = 0L;
                        this.isLoadDataQuery = StringUtils.startsWithIgnoreCaseAndWs(sql, "LOAD DATA");
                        if (this.connection.getProfileSql()) {
                            begin = System.currentTimeMillis();
                        }
                        String characterEncoding = null;
                        String connectionEncoding = this.connection.getEncoding();
                        if (!this.isLoadDataQuery && this.connection.getUseUnicode() && connectionEncoding != null) {
                            characterEncoding = connectionEncoding;
                        }
                        Buffer prepareResultPacket = mysql.sendCommand(22, sql, null, false, characterEncoding, 0);
                        if (this.connection.versionMeetsMinimum(4, 1, 1)) {
                            prepareResultPacket.setPosition(1);
                        } else {
                            prepareResultPacket.setPosition(0);
                        }
                        this.serverStatementId = prepareResultPacket.readLong();
                        this.fieldCount = prepareResultPacket.readInt();
                        this.parameterCount = prepareResultPacket.readInt();
                        this.parameterBindings = new BindValue[this.parameterCount];
                        for (int i2 = 0; i2 < this.parameterCount; ++i2) {
                            this.parameterBindings[i2] = new BindValue();
                        }
                        this.connection.incrementNumberOfPrepares();
                        if (this.profileSQL) {
                            this.eventSink.consumeEvent(new ProfilerEvent(2, "", this.currentCatalog, this.connectionId, this.statementId, -1, System.currentTimeMillis(), mysql.getCurrentTimeNanosOrMillis() - begin, mysql.getQueryTimingUnits(), null, LogUtils.findCallingClassAndMethod(new Throwable()), this.truncateQueryToLog(sql)));
                        }
                        boolean bl = checkEOF = !mysql.isEOFDeprecated();
                        if (this.parameterCount > 0 && this.connection.versionMeetsMinimum(4, 1, 2) && !mysql.isVersion(5, 0, 0)) {
                            this.parameterFields = new Field[this.parameterCount];
                            for (i = 0; i < this.parameterCount; ++i) {
                                Buffer metaDataPacket = mysql.readPacket();
                                this.parameterFields[i] = mysql.unpackField(metaDataPacket, false);
                            }
                            if (checkEOF) {
                                mysql.readPacket();
                            }
                        }
                        if (this.fieldCount <= 0) break block19;
                        this.resultFields = new Field[this.fieldCount];
                        for (i = 0; i < this.fieldCount; ++i) {
                            Buffer fieldPacket = mysql.readPacket();
                            this.resultFields[i] = mysql.unpackField(fieldPacket, false);
                        }
                        if (!checkEOF) break block19;
                        mysql.readPacket();
                    }
                    catch (SQLException sqlEx) {
                        if (this.connection.getDumpQueriesOnException()) {
                            StringBuilder messageBuf = new StringBuilder(this.originalSql.length() + 32);
                            messageBuf.append("\n\nQuery being prepared when exception was thrown:\n\n");
                            messageBuf.append(this.originalSql);
                            sqlEx = ConnectionImpl.appendMessageToException(sqlEx, messageBuf.toString(), this.getExceptionInterceptor());
                        }
                        throw sqlEx;
                    }
                }
                Object var13_13 = null;
            }
            catch (Throwable throwable) {
                Object var13_14 = null;
                this.connection.getIO().clearInputStream();
                throw throwable;
            }
            this.connection.getIO().clearInputStream();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String truncateQueryToLog(String sql) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            String query = null;
            if (sql.length() > this.connection.getMaxQuerySizeToLog()) {
                StringBuilder queryBuf = new StringBuilder(this.connection.getMaxQuerySizeToLog() + 12);
                queryBuf.append(sql.substring(0, this.connection.getMaxQuerySizeToLog()));
                queryBuf.append(Messages.getString("MysqlIO.25"));
                query = queryBuf.toString();
            } else {
                query = sql;
            }
            return query;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void serverResetStatement() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            MysqlIO mysql = this.connection.getIO();
            Buffer packet = mysql.getSharedSendPacket();
            packet.clear();
            packet.writeByte((byte)26);
            packet.writeLong(this.serverStatementId);
            try {
                try {
                    mysql.sendCommand(26, null, packet, !this.connection.versionMeetsMinimum(4, 1, 2), null, 0);
                }
                catch (SQLException sqlEx) {
                    throw sqlEx;
                }
                catch (Exception ex) {
                    SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1000", this.getExceptionInterceptor());
                    sqlEx.initCause(ex);
                    throw sqlEx;
                }
                Object var7_4 = null;
            }
            catch (Throwable throwable) {
                Object var7_5 = null;
                mysql.clearInputStream();
                throw throwable;
            }
            mysql.clearInputStream();
        }
    }

    public void setArray(int i, Array x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (x == null) {
                this.setNull(parameterIndex, -2);
            } else {
                BindValue binding = this.getBinding(parameterIndex, true);
                this.resetToType(binding, 252);
                binding.value = x;
                binding.isLongData = true;
                binding.bindLength = this.connection.getUseStreamLengthsInPrepStmts() ? (long)length : -1L;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (x == null) {
                this.setNull(parameterIndex, 3);
            } else {
                BindValue binding = this.getBinding(parameterIndex, false);
                if (this.connection.versionMeetsMinimum(5, 0, 3)) {
                    this.resetToType(binding, 246);
                } else {
                    this.resetToType(binding, this.stringTypeCode);
                }
                binding.value = StringUtils.fixDecimalExponent(StringUtils.consistentToString(x));
            }
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
                BindValue binding = this.getBinding(parameterIndex, true);
                this.resetToType(binding, 252);
                binding.value = x;
                binding.isLongData = true;
                binding.bindLength = this.connection.getUseStreamLengthsInPrepStmts() ? (long)length : -1L;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (x == null) {
                this.setNull(parameterIndex, -2);
            } else {
                BindValue binding = this.getBinding(parameterIndex, true);
                this.resetToType(binding, 252);
                binding.value = x;
                binding.isLongData = true;
                binding.bindLength = this.connection.getUseStreamLengthsInPrepStmts() ? x.length() : -1L;
            }
        }
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        this.setByte(parameterIndex, x ? (byte)1 : 0);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        this.checkClosed();
        BindValue binding = this.getBinding(parameterIndex, false);
        this.resetToType(binding, 1);
        binding.longBinding = x;
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.setNull(parameterIndex, -2);
        } else {
            BindValue binding = this.getBinding(parameterIndex, false);
            this.resetToType(binding, 253);
            binding.value = x;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (reader == null) {
                this.setNull(parameterIndex, -2);
            } else {
                BindValue binding = this.getBinding(parameterIndex, true);
                this.resetToType(binding, 252);
                binding.value = reader;
                binding.isLongData = true;
                binding.bindLength = this.connection.getUseStreamLengthsInPrepStmts() ? (long)length : -1L;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (x == null) {
                this.setNull(parameterIndex, -2);
            } else {
                BindValue binding = this.getBinding(parameterIndex, true);
                this.resetToType(binding, 252);
                binding.value = x.getCharacterStream();
                binding.isLongData = true;
                binding.bindLength = this.connection.getUseStreamLengthsInPrepStmts() ? x.length() : -1L;
            }
        }
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        this.setDate(parameterIndex, x, null);
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 91);
        } else {
            BindValue binding = this.getBinding(parameterIndex, false);
            this.resetToType(binding, 10);
            binding.value = x;
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
            BindValue binding = this.getBinding(parameterIndex, false);
            this.resetToType(binding, 5);
            binding.doubleBinding = x;
        }
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        this.checkClosed();
        BindValue binding = this.getBinding(parameterIndex, false);
        this.resetToType(binding, 4);
        binding.floatBinding = x;
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        this.checkClosed();
        BindValue binding = this.getBinding(parameterIndex, false);
        this.resetToType(binding, 3);
        binding.longBinding = x;
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        this.checkClosed();
        BindValue binding = this.getBinding(parameterIndex, false);
        this.resetToType(binding, 8);
        binding.longBinding = x;
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        this.checkClosed();
        BindValue binding = this.getBinding(parameterIndex, false);
        this.resetToType(binding, 6);
        binding.isNull = true;
    }

    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        this.checkClosed();
        BindValue binding = this.getBinding(parameterIndex, false);
        this.resetToType(binding, 6);
        binding.isNull = true;
    }

    public void setRef(int i, Ref x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        this.checkClosed();
        BindValue binding = this.getBinding(parameterIndex, false);
        this.resetToType(binding, 2);
        binding.longBinding = x;
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.setNull(parameterIndex, 1);
        } else {
            BindValue binding = this.getBinding(parameterIndex, false);
            this.resetToType(binding, this.stringTypeCode);
            binding.value = x;
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.setTimeInternal(parameterIndex, x, cal, cal.getTimeZone(), true);
        }
    }

    private void setTimeInternal(int parameterIndex, Time x, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 92);
        } else {
            BindValue binding = this.getBinding(parameterIndex, false);
            this.resetToType(binding, 11);
            if (!this.useLegacyDatetimeCode) {
                binding.value = x;
            } else {
                Calendar sessionCalendar = this.getCalendarInstanceForSessionOrNew();
                binding.value = TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, x, tz, this.connection.getServerTimezoneTZ(), rollForward);
            }
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
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.setTimestampInternal(parameterIndex, x, cal, cal.getTimeZone(), true);
        }
    }

    private void setTimestampInternal(int parameterIndex, Timestamp x, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 93);
        } else {
            BindValue binding = this.getBinding(parameterIndex, false);
            this.resetToType(binding, 12);
            if (!this.sendFractionalSeconds) {
                x = TimeUtil.truncateFractionalSeconds(x);
            }
            if (!this.useLegacyDatetimeCode) {
                binding.value = x;
            } else {
                Calendar sessionCalendar = this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : this.getCalendarInstanceForSessionOrNew();
                binding.value = TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, x, tz, this.connection.getServerTimezoneTZ(), rollForward);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void resetToType(BindValue oldValue, int bufferType) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            oldValue.reset();
            if ((bufferType != 6 || oldValue.bufferType == 0) && oldValue.bufferType != bufferType) {
                this.sendTypesToServer = true;
                oldValue.bufferType = bufferType;
            }
            oldValue.isSet = true;
            oldValue.boundBeforeExecutionNum = this.numberOfExecutions;
        }
    }

    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        this.checkClosed();
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        this.checkClosed();
        this.setString(parameterIndex, x.toString());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void storeBinding(Buffer packet, BindValue bindValue, MysqlIO mysql) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            try {
                Object value = bindValue.value;
                switch (bindValue.bufferType) {
                    case 1: {
                        packet.writeByte((byte)bindValue.longBinding);
                        return;
                    }
                    case 2: {
                        packet.ensureCapacity(2);
                        packet.writeInt((int)bindValue.longBinding);
                        return;
                    }
                    case 3: {
                        packet.ensureCapacity(4);
                        packet.writeLong((int)bindValue.longBinding);
                        return;
                    }
                    case 8: {
                        packet.ensureCapacity(8);
                        packet.writeLongLong(bindValue.longBinding);
                        return;
                    }
                    case 4: {
                        packet.ensureCapacity(4);
                        packet.writeFloat(bindValue.floatBinding);
                        return;
                    }
                    case 5: {
                        packet.ensureCapacity(8);
                        packet.writeDouble(bindValue.doubleBinding);
                        return;
                    }
                    case 11: {
                        this.storeTime(packet, (Time)value);
                        return;
                    }
                    case 7: 
                    case 10: 
                    case 12: {
                        this.storeDateTime(packet, (java.util.Date)value, mysql, bindValue.bufferType);
                        return;
                    }
                    case 0: 
                    case 15: 
                    case 246: 
                    case 253: 
                    case 254: {
                        if (value instanceof byte[]) {
                            packet.writeLenBytes((byte[])value);
                        } else if (!this.isLoadDataQuery) {
                            packet.writeLenString((String)value, this.charEncoding, this.connection.getServerCharset(), this.charConverter, this.connection.parserKnowsUnicode(), this.connection);
                        } else {
                            packet.writeLenBytes(StringUtils.getBytes((String)value));
                        }
                        return;
                    }
                }
            }
            catch (UnsupportedEncodingException uEE) {
                throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.22") + this.connection.getEncoding() + "'", "S1000", this.getExceptionInterceptor());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void storeDateTime412AndOlder(Buffer intoBuf, java.util.Date dt, int bufferType) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            Calendar sessionCalendar = null;
            sessionCalendar = !this.useLegacyDatetimeCode ? (bufferType == 10 ? this.getDefaultTzCalendar() : this.getServerTzCalendar()) : (dt instanceof Timestamp && this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : this.getCalendarInstanceForSessionOrNew());
            java.util.Date oldTime = sessionCalendar.getTime();
            try {
                intoBuf.ensureCapacity(8);
                intoBuf.writeByte((byte)7);
                sessionCalendar.setTime(dt);
                int year = sessionCalendar.get(1);
                int month = sessionCalendar.get(2) + 1;
                int date = sessionCalendar.get(5);
                intoBuf.writeInt(year);
                intoBuf.writeByte((byte)month);
                intoBuf.writeByte((byte)date);
                if (dt instanceof Date) {
                    intoBuf.writeByte((byte)0);
                    intoBuf.writeByte((byte)0);
                    intoBuf.writeByte((byte)0);
                } else {
                    intoBuf.writeByte((byte)sessionCalendar.get(11));
                    intoBuf.writeByte((byte)sessionCalendar.get(12));
                    intoBuf.writeByte((byte)sessionCalendar.get(13));
                }
                Object var11_10 = null;
                sessionCalendar.setTime(oldTime);
            }
            catch (Throwable throwable) {
                Object var11_11 = null;
                sessionCalendar.setTime(oldTime);
                throw throwable;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void storeDateTime(Buffer intoBuf, java.util.Date dt, MysqlIO mysql, int bufferType) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.connection.versionMeetsMinimum(4, 1, 3)) {
                this.storeDateTime413AndNewer(intoBuf, dt, bufferType);
            } else {
                this.storeDateTime412AndOlder(intoBuf, dt, bufferType);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void storeDateTime413AndNewer(Buffer intoBuf, java.util.Date dt, int bufferType) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            Calendar sessionCalendar = null;
            sessionCalendar = !this.useLegacyDatetimeCode ? (bufferType == 10 ? this.getDefaultTzCalendar() : this.getServerTzCalendar()) : (dt instanceof Timestamp && this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : this.getCalendarInstanceForSessionOrNew());
            java.util.Date oldTime = sessionCalendar.getTime();
            try {
                sessionCalendar.setTime(dt);
                if (dt instanceof Date) {
                    sessionCalendar.set(11, 0);
                    sessionCalendar.set(12, 0);
                    sessionCalendar.set(13, 0);
                }
                byte length = 7;
                if (dt instanceof Timestamp) {
                    length = 11;
                }
                intoBuf.ensureCapacity(length);
                intoBuf.writeByte(length);
                int year = sessionCalendar.get(1);
                int month = sessionCalendar.get(2) + 1;
                int date = sessionCalendar.get(5);
                intoBuf.writeInt(year);
                intoBuf.writeByte((byte)month);
                intoBuf.writeByte((byte)date);
                if (dt instanceof Date) {
                    intoBuf.writeByte((byte)0);
                    intoBuf.writeByte((byte)0);
                    intoBuf.writeByte((byte)0);
                } else {
                    intoBuf.writeByte((byte)sessionCalendar.get(11));
                    intoBuf.writeByte((byte)sessionCalendar.get(12));
                    intoBuf.writeByte((byte)sessionCalendar.get(13));
                }
                if (length == 11) {
                    intoBuf.writeLong(((Timestamp)dt).getNanos() / 1000);
                }
                Object var12_11 = null;
                sessionCalendar.setTime(oldTime);
            }
            catch (Throwable throwable) {
                Object var12_12 = null;
                sessionCalendar.setTime(oldTime);
                throw throwable;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Calendar getServerTzCalendar() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.serverTzCalendar == null) {
                this.serverTzCalendar = new GregorianCalendar(this.connection.getServerTimezoneTZ());
            }
            return this.serverTzCalendar;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Calendar getDefaultTzCalendar() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.defaultTzCalendar == null) {
                this.defaultTzCalendar = new GregorianCalendar(TimeZone.getDefault());
            }
            return this.defaultTzCalendar;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void storeReader(MysqlIO mysql, int parameterIndex, Buffer packet, Reader inStream) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            String forcedEncoding = this.connection.getClobCharacterEncoding();
            String clobEncoding = forcedEncoding == null ? this.connection.getEncoding() : forcedEncoding;
            int maxBytesChar = 2;
            if (clobEncoding != null) {
                if (!clobEncoding.equals("UTF-16")) {
                    maxBytesChar = this.connection.getMaxBytesPerChar(clobEncoding);
                    if (maxBytesChar == 1) {
                        maxBytesChar = 2;
                    }
                } else {
                    maxBytesChar = 4;
                }
            }
            char[] buf = new char[8192 / maxBytesChar];
            int numRead = 0;
            int bytesInPacket = 0;
            int totalBytesRead = 0;
            int bytesReadAtLastSend = 0;
            int packetIsFullAt = this.connection.getBlobSendChunkSize();
            try {
                try {
                    packet.clear();
                    packet.writeByte((byte)24);
                    packet.writeLong(this.serverStatementId);
                    packet.writeInt(parameterIndex);
                    boolean readAny = false;
                    while ((numRead = inStream.read(buf)) != -1) {
                        readAny = true;
                        byte[] valueAsBytes = StringUtils.getBytes(buf, null, clobEncoding, this.connection.getServerCharset(), 0, numRead, this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
                        packet.writeBytesNoNull(valueAsBytes, 0, valueAsBytes.length);
                        totalBytesRead += valueAsBytes.length;
                        if ((bytesInPacket += valueAsBytes.length) < packetIsFullAt) continue;
                        bytesReadAtLastSend = totalBytesRead;
                        mysql.sendCommand(24, null, packet, true, null, 0);
                        bytesInPacket = 0;
                        packet.clear();
                        packet.writeByte((byte)24);
                        packet.writeLong(this.serverStatementId);
                        packet.writeInt(parameterIndex);
                    }
                    if (totalBytesRead != bytesReadAtLastSend) {
                        mysql.sendCommand(24, null, packet, true, null, 0);
                    }
                    if (!readAny) {
                        mysql.sendCommand(24, null, packet, true, null, 0);
                    }
                }
                catch (IOException ioEx) {
                    SQLException sqlEx = SQLError.createSQLException(Messages.getString("ServerPreparedStatement.24") + ioEx.toString(), "S1000", this.getExceptionInterceptor());
                    sqlEx.initCause(ioEx);
                    throw sqlEx;
                }
                Object var18_19 = null;
                {
                }
            }
            catch (Throwable throwable) {
                Object var18_20 = null;
                if (!this.connection.getAutoClosePStmtStreams()) throw throwable;
                if (inStream == null) throw throwable;
                try {
                    inStream.close();
                    throw throwable;
                }
                catch (IOException ioEx) {
                    // empty catch block
                }
                throw throwable;
            }
            if (!this.connection.getAutoClosePStmtStreams()) return;
            if (inStream == null) return;
            try {}
            catch (IOException ioEx) {}
            inStream.close();
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void storeStream(MysqlIO mysql, int parameterIndex, Buffer packet, InputStream inStream) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            byte[] buf = new byte[8192];
            int numRead = 0;
            try {
                try {
                    int bytesInPacket = 0;
                    int totalBytesRead = 0;
                    int bytesReadAtLastSend = 0;
                    int packetIsFullAt = this.connection.getBlobSendChunkSize();
                    packet.clear();
                    packet.writeByte((byte)24);
                    packet.writeLong(this.serverStatementId);
                    packet.writeInt(parameterIndex);
                    boolean readAny = false;
                    while ((numRead = inStream.read(buf)) != -1) {
                        readAny = true;
                        packet.writeBytesNoNull(buf, 0, numRead);
                        totalBytesRead += numRead;
                        if ((bytesInPacket += numRead) < packetIsFullAt) continue;
                        bytesReadAtLastSend = totalBytesRead;
                        mysql.sendCommand(24, null, packet, true, null, 0);
                        bytesInPacket = 0;
                        packet.clear();
                        packet.writeByte((byte)24);
                        packet.writeLong(this.serverStatementId);
                        packet.writeInt(parameterIndex);
                    }
                    if (totalBytesRead != bytesReadAtLastSend) {
                        mysql.sendCommand(24, null, packet, true, null, 0);
                    }
                    if (!readAny) {
                        mysql.sendCommand(24, null, packet, true, null, 0);
                    }
                }
                catch (IOException ioEx) {
                    SQLException sqlEx = SQLError.createSQLException(Messages.getString("ServerPreparedStatement.25") + ioEx.toString(), "S1000", this.getExceptionInterceptor());
                    sqlEx.initCause(ioEx);
                    throw sqlEx;
                }
                Object var14_15 = null;
                {
                }
            }
            catch (Throwable throwable) {
                Object var14_16 = null;
                if (!this.connection.getAutoClosePStmtStreams()) throw throwable;
                if (inStream == null) throw throwable;
                try {
                    inStream.close();
                    throw throwable;
                }
                catch (IOException ioEx) {
                    // empty catch block
                }
                throw throwable;
            }
            if (!this.connection.getAutoClosePStmtStreams()) return;
            if (inStream == null) return;
            try {}
            catch (IOException ioEx) {}
            inStream.close();
            return;
        }
    }

    public String toString() {
        StringBuilder toStringBuf = new StringBuilder();
        toStringBuf.append("com.mysql.jdbc.ServerPreparedStatement[");
        toStringBuf.append(this.serverStatementId);
        toStringBuf.append("] - ");
        try {
            toStringBuf.append(this.asSql());
        }
        catch (SQLException sqlEx) {
            toStringBuf.append(Messages.getString("ServerPreparedStatement.6"));
            toStringBuf.append(sqlEx);
        }
        return toStringBuf.toString();
    }

    protected long getServerStatementId() {
        return this.serverStatementId;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean canRewriteAsMultiValueInsertAtSqlLevel() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (!this.hasCheckedRewrite) {
                this.hasCheckedRewrite = true;
                this.canRewrite = ServerPreparedStatement.canRewrite(this.originalSql, this.isOnDuplicateKeyUpdate(), this.getLocationOfOnDuplicateKeyUpdate(), 0);
                this.parseInfo = new PreparedStatement.ParseInfo(this.originalSql, this.connection, this.connection.getMetaData(), this.charEncoding, this.charConverter);
            }
            return this.canRewrite;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean canRewriteAsMultivalueInsertStatement() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (!this.canRewriteAsMultiValueInsertAtSqlLevel()) {
                return false;
            }
            BindValue[] currentBindValues = null;
            Object previousBindValues = null;
            int nbrCommands = this.batchedArgs.size();
            for (int commandIndex = 0; commandIndex < nbrCommands; ++commandIndex) {
                Object arg = this.batchedArgs.get(commandIndex);
                if (arg instanceof String) continue;
                currentBindValues = ((BatchedBindValues)arg).batchedParameterValues;
                if (previousBindValues == null) continue;
                for (int j = 0; j < this.parameterBindings.length; ++j) {
                    if (currentBindValues[j].bufferType == previousBindValues[j].bufferType) continue;
                    return false;
                }
            }
            return true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected int getLocationOfOnDuplicateKeyUpdate() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.locationOfOnDuplicateKeyUpdate == -2) {
                this.locationOfOnDuplicateKeyUpdate = ServerPreparedStatement.getOnDuplicateKeyLocation(this.originalSql, this.connection.getDontCheckOnDuplicateKeyUpdateInSQL(), this.connection.getRewriteBatchedStatements(), this.connection.isNoBackslashEscapesSet());
            }
            return this.locationOfOnDuplicateKeyUpdate;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected boolean isOnDuplicateKeyUpdate() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.getLocationOfOnDuplicateKeyUpdate() != -1;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected long[] computeMaxParameterSetSizeAndBatchSize(int numBatchedArgs) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            long sizeOfEntireBatch = 10L;
            long maxSizeOfParameterSet = 0L;
            for (int i = 0; i < numBatchedArgs; ++i) {
                BindValue[] paramArg = ((BatchedBindValues)this.batchedArgs.get((int)i)).batchedParameterValues;
                long sizeOfParameterSet = 0L;
                sizeOfParameterSet += (long)((this.parameterCount + 7) / 8);
                sizeOfParameterSet += (long)(this.parameterCount * 2);
                for (int j = 0; j < this.parameterBindings.length; ++j) {
                    if (paramArg[j].isNull) continue;
                    long size = paramArg[j].getBoundLength();
                    if (paramArg[j].isLongData) {
                        if (size == -1L) continue;
                        sizeOfParameterSet += size;
                        continue;
                    }
                    sizeOfParameterSet += size;
                }
                sizeOfEntireBatch += sizeOfParameterSet;
                if (sizeOfParameterSet <= maxSizeOfParameterSet) continue;
                maxSizeOfParameterSet = sizeOfParameterSet;
            }
            return new long[]{maxSizeOfParameterSet, sizeOfEntireBatch};
        }
    }

    protected int setOneBatchedParameterSet(java.sql.PreparedStatement batchedStatement, int batchedParamIndex, Object paramSet) throws SQLException {
        BindValue[] paramArg = ((BatchedBindValues)paramSet).batchedParameterValues;
        block12: for (int j = 0; j < paramArg.length; ++j) {
            Object value;
            if (paramArg[j].isNull) {
                batchedStatement.setNull(batchedParamIndex++, 0);
                continue;
            }
            if (paramArg[j].isLongData) {
                value = paramArg[j].value;
                if (value instanceof InputStream) {
                    batchedStatement.setBinaryStream(batchedParamIndex++, (InputStream)value, (int)paramArg[j].bindLength);
                    continue;
                }
                batchedStatement.setCharacterStream(batchedParamIndex++, (Reader)value, (int)paramArg[j].bindLength);
                continue;
            }
            switch (paramArg[j].bufferType) {
                case 1: {
                    batchedStatement.setByte(batchedParamIndex++, (byte)paramArg[j].longBinding);
                    continue block12;
                }
                case 2: {
                    batchedStatement.setShort(batchedParamIndex++, (short)paramArg[j].longBinding);
                    continue block12;
                }
                case 3: {
                    batchedStatement.setInt(batchedParamIndex++, (int)paramArg[j].longBinding);
                    continue block12;
                }
                case 8: {
                    batchedStatement.setLong(batchedParamIndex++, paramArg[j].longBinding);
                    continue block12;
                }
                case 4: {
                    batchedStatement.setFloat(batchedParamIndex++, paramArg[j].floatBinding);
                    continue block12;
                }
                case 5: {
                    batchedStatement.setDouble(batchedParamIndex++, paramArg[j].doubleBinding);
                    continue block12;
                }
                case 11: {
                    batchedStatement.setTime(batchedParamIndex++, (Time)paramArg[j].value);
                    continue block12;
                }
                case 10: {
                    batchedStatement.setDate(batchedParamIndex++, (Date)paramArg[j].value);
                    continue block12;
                }
                case 7: 
                case 12: {
                    batchedStatement.setTimestamp(batchedParamIndex++, (Timestamp)paramArg[j].value);
                    continue block12;
                }
                case 0: 
                case 15: 
                case 246: 
                case 253: 
                case 254: {
                    value = paramArg[j].value;
                    if (value instanceof byte[]) {
                        batchedStatement.setBytes(batchedParamIndex, (byte[])value);
                    } else {
                        batchedStatement.setString(batchedParamIndex, (String)value);
                    }
                    if (batchedStatement instanceof ServerPreparedStatement) {
                        BindValue asBound = ((ServerPreparedStatement)batchedStatement).getBinding(batchedParamIndex, false);
                        asBound.bufferType = paramArg[j].bufferType;
                    }
                    ++batchedParamIndex;
                    continue block12;
                }
                default: {
                    throw new IllegalArgumentException("Unknown type when re-binding parameter into batched statement for parameter index " + batchedParamIndex);
                }
            }
        }
        return batchedParamIndex;
    }

    protected boolean containsOnDuplicateKeyUpdateInSQL() {
        return this.hasOnDuplicateKeyUpdate;
    }

    protected PreparedStatement prepareBatchedInsertSQL(MySQLConnection localConn, int numBatches) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            try {
                PreparedStatement pstmt = ((Wrapper)((Object)localConn.prepareStatement(this.parseInfo.getSqlForBatch(numBatches), this.resultSetConcurrency, this.resultSetType))).unwrap(PreparedStatement.class);
                pstmt.setRetrieveGeneratedKeys(this.retrieveGeneratedKeys);
                return pstmt;
            }
            catch (UnsupportedEncodingException e) {
                SQLException sqlEx = SQLError.createSQLException("Unable to prepare batch statement", "S1000", this.getExceptionInterceptor());
                sqlEx.initCause(e);
                throw sqlEx;
            }
        }
    }

    public void setPoolable(boolean poolable) throws SQLException {
        if (!poolable) {
            this.connection.decachePreparedStatement(this);
        }
        super.setPoolable(poolable);
    }

    static {
        if (Util.isJdbc4()) {
            try {
                String jdbc4ClassName = Util.isJdbc42() ? "com.mysql.jdbc.JDBC42ServerPreparedStatement" : "com.mysql.jdbc.JDBC4ServerPreparedStatement";
                JDBC_4_SPS_CTOR = Class.forName(jdbc4ClassName).getConstructor(MySQLConnection.class, String.class, String.class, Integer.TYPE, Integer.TYPE);
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
            JDBC_4_SPS_CTOR = null;
        }
    }

    public static class BindValue {
        public long boundBeforeExecutionNum = 0L;
        public long bindLength;
        public int bufferType;
        public double doubleBinding;
        public float floatBinding;
        public boolean isLongData;
        public boolean isNull;
        public boolean isSet = false;
        public long longBinding;
        public Object value;

        BindValue() {
        }

        BindValue(BindValue copyMe) {
            this.value = copyMe.value;
            this.isSet = copyMe.isSet;
            this.isLongData = copyMe.isLongData;
            this.isNull = copyMe.isNull;
            this.bufferType = copyMe.bufferType;
            this.bindLength = copyMe.bindLength;
            this.longBinding = copyMe.longBinding;
            this.floatBinding = copyMe.floatBinding;
            this.doubleBinding = copyMe.doubleBinding;
        }

        void reset() {
            this.isNull = false;
            this.isSet = false;
            this.value = null;
            this.isLongData = false;
            this.longBinding = 0L;
            this.floatBinding = 0.0f;
            this.doubleBinding = 0.0;
        }

        public String toString() {
            return this.toString(false);
        }

        public String toString(boolean quoteIfNeeded) {
            if (this.isLongData) {
                return "' STREAM DATA '";
            }
            if (this.isNull) {
                return "NULL";
            }
            switch (this.bufferType) {
                case 1: 
                case 2: 
                case 3: 
                case 8: {
                    return String.valueOf(this.longBinding);
                }
                case 4: {
                    return String.valueOf(this.floatBinding);
                }
                case 5: {
                    return String.valueOf(this.doubleBinding);
                }
                case 7: 
                case 10: 
                case 11: 
                case 12: 
                case 15: 
                case 253: 
                case 254: {
                    if (quoteIfNeeded) {
                        return "'" + String.valueOf(this.value) + "'";
                    }
                    return String.valueOf(this.value);
                }
            }
            if (this.value instanceof byte[]) {
                return "byte data";
            }
            if (quoteIfNeeded) {
                return "'" + String.valueOf(this.value) + "'";
            }
            return String.valueOf(this.value);
        }

        long getBoundLength() {
            if (this.isNull) {
                return 0L;
            }
            if (this.isLongData) {
                return this.bindLength;
            }
            switch (this.bufferType) {
                case 1: {
                    return 1L;
                }
                case 2: {
                    return 2L;
                }
                case 3: {
                    return 4L;
                }
                case 8: {
                    return 8L;
                }
                case 4: {
                    return 4L;
                }
                case 5: {
                    return 8L;
                }
                case 11: {
                    return 9L;
                }
                case 10: {
                    return 7L;
                }
                case 7: 
                case 12: {
                    return 11L;
                }
                case 0: 
                case 15: 
                case 246: 
                case 253: 
                case 254: {
                    if (this.value instanceof byte[]) {
                        return ((byte[])this.value).length;
                    }
                    return ((String)this.value).length();
                }
            }
            return 0L;
        }
    }

    public static class BatchedBindValues {
        public BindValue[] batchedParameterValues;

        BatchedBindValues(BindValue[] paramVals) {
            int numParams = paramVals.length;
            this.batchedParameterValues = new BindValue[numParams];
            for (int i = 0; i < numParams; ++i) {
                this.batchedParameterValues[i] = new BindValue(paramVals[i]);
            }
        }
    }
}


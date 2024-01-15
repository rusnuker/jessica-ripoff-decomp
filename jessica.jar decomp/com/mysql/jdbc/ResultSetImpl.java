/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Blob;
import com.mysql.jdbc.BlobFromLocator;
import com.mysql.jdbc.CachedResultSetMetaData;
import com.mysql.jdbc.Clob;
import com.mysql.jdbc.Constants;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.MysqlDefs;
import com.mysql.jdbc.NotUpdatable;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ProfilerEventHandlerFactory;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.ResultSetRow;
import com.mysql.jdbc.RowData;
import com.mysql.jdbc.RowDataStatic;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.StatementImpl;
import com.mysql.jdbc.StringUtils;
import com.mysql.jdbc.TimeUtil;
import com.mysql.jdbc.UpdatableResultSet;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.log.LogUtils;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.sql.Date;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ResultSetImpl
implements ResultSetInternalMethods {
    private static final Constructor<?> JDBC_4_RS_4_ARG_CTOR;
    private static final Constructor<?> JDBC_4_RS_5_ARG_CTOR;
    private static final Constructor<?> JDBC_4_UPD_RS_5_ARG_CTOR;
    protected static final double MIN_DIFF_PREC;
    protected static final double MAX_DIFF_PREC;
    static int resultCounter;
    protected String catalog = null;
    protected Map<String, Integer> columnLabelToIndex = null;
    protected Map<String, Integer> columnToIndexCache = null;
    protected boolean[] columnUsed = null;
    protected volatile MySQLConnection connection;
    protected long connectionId = 0L;
    protected int currentRow = -1;
    protected boolean doingUpdates = false;
    protected ProfilerEventHandler eventSink = null;
    Calendar fastDefaultCal = null;
    Calendar fastClientCal = null;
    protected int fetchDirection = 1000;
    protected int fetchSize = 0;
    protected Field[] fields;
    protected char firstCharOfQuery;
    protected Map<String, Integer> fullColumnNameToIndex = null;
    protected Map<String, Integer> columnNameToIndex = null;
    protected boolean hasBuiltIndexMapping = false;
    protected boolean isBinaryEncoded = false;
    protected boolean isClosed = false;
    protected ResultSetInternalMethods nextResultSet = null;
    protected boolean onInsertRow = false;
    protected StatementImpl owningStatement;
    protected String pointOfOrigin;
    protected boolean profileSql = false;
    protected boolean reallyResult = false;
    protected int resultId;
    protected int resultSetConcurrency = 0;
    protected int resultSetType = 0;
    protected RowData rowData;
    protected String serverInfo = null;
    PreparedStatement statementUsedForFetchingRows;
    protected ResultSetRow thisRow = null;
    protected long updateCount;
    protected long updateId = -1L;
    private boolean useStrictFloatingPoint = false;
    protected boolean useUsageAdvisor = false;
    protected SQLWarning warningChain = null;
    protected boolean wasNullFlag = false;
    protected Statement wrapperStatement;
    protected boolean retainOwningStatement;
    protected Calendar gmtCalendar = null;
    protected boolean useFastDateParsing = false;
    private boolean padCharsWithSpace = false;
    private boolean jdbcCompliantTruncationForReads;
    private boolean useFastIntParsing = true;
    private boolean useColumnNamesInFindColumn;
    private ExceptionInterceptor exceptionInterceptor;
    static final char[] EMPTY_SPACE;
    private boolean onValidRow = false;
    private String invalidRowReason = null;
    protected boolean useLegacyDatetimeCode;
    private TimeZone serverTimeZoneTz;

    protected static BigInteger convertLongToUlong(long longVal) {
        byte[] asBytes = new byte[8];
        asBytes[7] = (byte)(longVal & 0xFFL);
        asBytes[6] = (byte)(longVal >>> 8);
        asBytes[5] = (byte)(longVal >>> 16);
        asBytes[4] = (byte)(longVal >>> 24);
        asBytes[3] = (byte)(longVal >>> 32);
        asBytes[2] = (byte)(longVal >>> 40);
        asBytes[1] = (byte)(longVal >>> 48);
        asBytes[0] = (byte)(longVal >>> 56);
        return new BigInteger(1, asBytes);
    }

    protected static ResultSetImpl getInstance(long updateCount, long updateID, MySQLConnection conn, StatementImpl creatorStmt) throws SQLException {
        if (!Util.isJdbc4()) {
            return new ResultSetImpl(updateCount, updateID, conn, creatorStmt);
        }
        return (ResultSetImpl)Util.handleNewInstance(JDBC_4_RS_4_ARG_CTOR, new Object[]{updateCount, updateID, conn, creatorStmt}, conn.getExceptionInterceptor());
    }

    protected static ResultSetImpl getInstance(String catalog, Field[] fields, RowData tuples, MySQLConnection conn, StatementImpl creatorStmt, boolean isUpdatable) throws SQLException {
        if (!Util.isJdbc4()) {
            if (!isUpdatable) {
                return new ResultSetImpl(catalog, fields, tuples, conn, creatorStmt);
            }
            return new UpdatableResultSet(catalog, fields, tuples, conn, creatorStmt);
        }
        if (!isUpdatable) {
            return (ResultSetImpl)Util.handleNewInstance(JDBC_4_RS_5_ARG_CTOR, new Object[]{catalog, fields, tuples, conn, creatorStmt}, conn.getExceptionInterceptor());
        }
        return (ResultSetImpl)Util.handleNewInstance(JDBC_4_UPD_RS_5_ARG_CTOR, new Object[]{catalog, fields, tuples, conn, creatorStmt}, conn.getExceptionInterceptor());
    }

    public ResultSetImpl(long updateCount, long updateID, MySQLConnection conn, StatementImpl creatorStmt) {
        this.updateCount = updateCount;
        this.updateId = updateID;
        this.reallyResult = false;
        this.fields = new Field[0];
        this.connection = conn;
        this.owningStatement = creatorStmt;
        this.retainOwningStatement = false;
        if (this.connection != null) {
            this.exceptionInterceptor = this.connection.getExceptionInterceptor();
            this.retainOwningStatement = this.connection.getRetainStatementAfterResultSetClose();
            this.connectionId = this.connection.getId();
            this.serverTimeZoneTz = this.connection.getServerTimezoneTZ();
            this.padCharsWithSpace = this.connection.getPadCharsWithSpace();
            this.useLegacyDatetimeCode = this.connection.getUseLegacyDatetimeCode();
        }
    }

    public ResultSetImpl(String catalog, Field[] fields, RowData tuples, MySQLConnection conn, StatementImpl creatorStmt) throws SQLException {
        this.connection = conn;
        this.retainOwningStatement = false;
        if (this.connection != null) {
            this.exceptionInterceptor = this.connection.getExceptionInterceptor();
            this.useStrictFloatingPoint = this.connection.getStrictFloatingPoint();
            this.connectionId = this.connection.getId();
            this.useFastDateParsing = this.connection.getUseFastDateParsing();
            this.profileSql = this.connection.getProfileSql();
            this.retainOwningStatement = this.connection.getRetainStatementAfterResultSetClose();
            this.jdbcCompliantTruncationForReads = this.connection.getJdbcCompliantTruncationForReads();
            this.useFastIntParsing = this.connection.getUseFastIntParsing();
            this.serverTimeZoneTz = this.connection.getServerTimezoneTZ();
            this.padCharsWithSpace = this.connection.getPadCharsWithSpace();
        }
        this.owningStatement = creatorStmt;
        this.catalog = catalog;
        this.fields = fields;
        this.rowData = tuples;
        this.updateCount = this.rowData.size();
        this.reallyResult = true;
        if (this.rowData.size() > 0) {
            if (this.updateCount == 1L && this.thisRow == null) {
                this.rowData.close();
                this.updateCount = -1L;
            }
        } else {
            this.thisRow = null;
        }
        this.rowData.setOwner(this);
        if (this.fields != null) {
            this.initializeWithMetadata();
        }
        this.useLegacyDatetimeCode = this.connection.getUseLegacyDatetimeCode();
        this.useColumnNamesInFindColumn = this.connection.getUseColumnNamesInFindColumn();
        this.setRowPositionValidity();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void initializeWithMetadata() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.rowData.setMetadata(this.fields);
            this.columnToIndexCache = new HashMap<String, Integer>();
            if (this.profileSql || this.connection.getUseUsageAdvisor()) {
                this.columnUsed = new boolean[this.fields.length];
                this.pointOfOrigin = LogUtils.findCallingClassAndMethod(new Throwable());
                this.resultId = resultCounter++;
                this.useUsageAdvisor = this.connection.getUseUsageAdvisor();
                this.eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
            }
            if (this.connection.getGatherPerformanceMetrics()) {
                this.connection.incrementNumberOfResultSetsCreated();
                HashSet<String> tableNamesSet = new HashSet<String>();
                for (int i = 0; i < this.fields.length; ++i) {
                    Field f = this.fields[i];
                    String tableName = f.getOriginalTableName();
                    if (tableName == null) {
                        tableName = f.getTableName();
                    }
                    if (tableName == null) continue;
                    if (this.connection.lowerCaseTableNames()) {
                        tableName = tableName.toLowerCase();
                    }
                    tableNamesSet.add(tableName);
                }
                this.connection.reportNumberOfTablesAccessed(tableNamesSet.size());
            }
        }
    }

    private synchronized Calendar getFastDefaultCalendar() {
        if (this.fastDefaultCal == null) {
            this.fastDefaultCal = new GregorianCalendar(Locale.US);
            this.fastDefaultCal.setTimeZone(this.getDefaultTimeZone());
        }
        return this.fastDefaultCal;
    }

    private synchronized Calendar getFastClientCalendar() {
        if (this.fastClientCal == null) {
            this.fastClientCal = new GregorianCalendar(Locale.US);
        }
        return this.fastClientCal;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean absolute(int row) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            boolean b;
            if (this.rowData.size() == 0) {
                b = false;
            } else {
                if (this.onInsertRow) {
                    this.onInsertRow = false;
                }
                if (this.doingUpdates) {
                    this.doingUpdates = false;
                }
                if (this.thisRow != null) {
                    this.thisRow.closeOpenStreams();
                }
                if (row == 0) {
                    this.beforeFirst();
                    b = false;
                } else if (row == 1) {
                    b = this.first();
                } else if (row == -1) {
                    b = this.last();
                } else if (row > this.rowData.size()) {
                    this.afterLast();
                    b = false;
                } else if (row < 0) {
                    int newRowPosition = this.rowData.size() + row + 1;
                    if (newRowPosition <= 0) {
                        this.beforeFirst();
                        b = false;
                    } else {
                        b = this.absolute(newRowPosition);
                    }
                } else {
                    this.rowData.setCurrentRow(--row);
                    this.thisRow = this.rowData.getAt(row);
                    b = true;
                }
            }
            this.setRowPositionValidity();
            return b;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void afterLast() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.onInsertRow) {
                this.onInsertRow = false;
            }
            if (this.doingUpdates) {
                this.doingUpdates = false;
            }
            if (this.thisRow != null) {
                this.thisRow.closeOpenStreams();
            }
            if (this.rowData.size() != 0) {
                this.rowData.afterLast();
                this.thisRow = null;
            }
            this.setRowPositionValidity();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void beforeFirst() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.onInsertRow) {
                this.onInsertRow = false;
            }
            if (this.doingUpdates) {
                this.doingUpdates = false;
            }
            if (this.rowData.size() == 0) {
                return;
            }
            if (this.thisRow != null) {
                this.thisRow.closeOpenStreams();
            }
            this.rowData.beforeFirst();
            this.thisRow = null;
            this.setRowPositionValidity();
        }
    }

    @Override
    public void buildIndexMapping() throws SQLException {
        int numFields = this.fields.length;
        this.columnLabelToIndex = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
        this.fullColumnNameToIndex = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
        this.columnNameToIndex = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
        for (int i = numFields - 1; i >= 0; --i) {
            Integer index = i;
            String columnName = this.fields[i].getOriginalName();
            String columnLabel = this.fields[i].getName();
            String fullColumnName = this.fields[i].getFullName();
            if (columnLabel != null) {
                this.columnLabelToIndex.put(columnLabel, index);
            }
            if (fullColumnName != null) {
                this.fullColumnNameToIndex.put(fullColumnName, index);
            }
            if (columnName == null) continue;
            this.columnNameToIndex.put(columnName, index);
        }
        this.hasBuiltIndexMapping = true;
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        throw new NotUpdatable();
    }

    protected final MySQLConnection checkClosed() throws SQLException {
        MySQLConnection c = this.connection;
        if (c == null) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Operation_not_allowed_after_ResultSet_closed_144"), "S1000", this.getExceptionInterceptor());
        }
        return c;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void checkColumnBounds(int columnIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (columnIndex < 1) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Column_Index_out_of_range_low", new Object[]{columnIndex, this.fields.length}), "S1009", this.getExceptionInterceptor());
            }
            if (columnIndex > this.fields.length) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Column_Index_out_of_range_high", new Object[]{columnIndex, this.fields.length}), "S1009", this.getExceptionInterceptor());
            }
            if (this.profileSql || this.useUsageAdvisor) {
                this.columnUsed[columnIndex - 1] = true;
            }
        }
    }

    protected void checkRowPos() throws SQLException {
        this.checkClosed();
        if (!this.onValidRow) {
            throw SQLError.createSQLException(this.invalidRowReason, "S1000", this.getExceptionInterceptor());
        }
    }

    private void setRowPositionValidity() throws SQLException {
        if (!this.rowData.isDynamic() && this.rowData.size() == 0) {
            this.invalidRowReason = Messages.getString("ResultSet.Illegal_operation_on_empty_result_set");
            this.onValidRow = false;
        } else if (this.rowData.isBeforeFirst()) {
            this.invalidRowReason = Messages.getString("ResultSet.Before_start_of_result_set_146");
            this.onValidRow = false;
        } else if (this.rowData.isAfterLast()) {
            this.invalidRowReason = Messages.getString("ResultSet.After_end_of_result_set_148");
            this.onValidRow = false;
        } else {
            this.onValidRow = true;
            this.invalidRowReason = null;
        }
    }

    @Override
    public synchronized void clearNextResult() {
        this.nextResultSet = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void clearWarnings() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.warningChain = null;
        }
    }

    @Override
    public void close() throws SQLException {
        this.realClose(true);
    }

    private int convertToZeroWithEmptyCheck() throws SQLException {
        if (this.connection.getEmptyStringsConvertToZero()) {
            return 0;
        }
        throw SQLError.createSQLException("Can't convert empty string ('') to numeric", "22018", this.getExceptionInterceptor());
    }

    private String convertToZeroLiteralStringWithEmptyCheck() throws SQLException {
        if (this.connection.getEmptyStringsConvertToZero()) {
            return "0";
        }
        throw SQLError.createSQLException("Can't convert empty string ('') to numeric", "22018", this.getExceptionInterceptor());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ResultSetInternalMethods copy() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetImpl rs = ResultSetImpl.getInstance(this.catalog, this.fields, this.rowData, this.connection, this.owningStatement, false);
            return rs;
        }
    }

    @Override
    public void redefineFieldsForDBMD(Field[] f) {
        this.fields = f;
        for (int i = 0; i < this.fields.length; ++i) {
            this.fields[i].setUseOldNameMetadata(true);
            this.fields[i].setConnection(this.connection);
        }
    }

    @Override
    public void populateCachedMetaData(CachedResultSetMetaData cachedMetaData) throws SQLException {
        cachedMetaData.fields = this.fields;
        cachedMetaData.columnNameToIndex = this.columnLabelToIndex;
        cachedMetaData.fullColumnNameToIndex = this.fullColumnNameToIndex;
        cachedMetaData.metadata = this.getMetaData();
    }

    @Override
    public void initializeFromCachedMetaData(CachedResultSetMetaData cachedMetaData) {
        this.fields = cachedMetaData.fields;
        this.columnLabelToIndex = cachedMetaData.columnNameToIndex;
        this.fullColumnNameToIndex = cachedMetaData.fullColumnNameToIndex;
        this.hasBuiltIndexMapping = true;
    }

    @Override
    public void deleteRow() throws SQLException {
        throw new NotUpdatable();
    }

    private String extractStringFromNativeColumn(int columnIndex, int mysqlType) throws SQLException {
        int columnIndexMinusOne = columnIndex - 1;
        this.wasNullFlag = false;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        String encoding = this.fields[columnIndexMinusOne].getEncoding();
        return this.thisRow.getString(columnIndex - 1, encoding, this.connection);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Date fastDateCreate(Calendar cal, int year, int month, int day) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            Calendar targetCalendar = cal;
            if (cal == null) {
                targetCalendar = this.connection.getNoTimezoneConversionForDateType() ? this.getFastClientCalendar() : this.getFastDefaultCalendar();
            }
            if (!this.useLegacyDatetimeCode) {
                return TimeUtil.fastDateCreate(year, month, day, targetCalendar);
            }
            boolean useGmtMillis = cal == null && !this.connection.getNoTimezoneConversionForDateType() && this.connection.getUseGmtMillisForDatetimes();
            return TimeUtil.fastDateCreate(useGmtMillis, useGmtMillis ? this.getGmtCalendar() : targetCalendar, targetCalendar, year, month, day);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Time fastTimeCreate(Calendar cal, int hour, int minute, int second) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (!this.useLegacyDatetimeCode) {
                return TimeUtil.fastTimeCreate(hour, minute, second, cal, this.getExceptionInterceptor());
            }
            if (cal == null) {
                cal = this.getFastDefaultCalendar();
            }
            return TimeUtil.fastTimeCreate(cal, hour, minute, second, this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Timestamp fastTimestampCreate(Calendar cal, int year, int month, int day, int hour, int minute, int seconds, int secondsPart) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            boolean useGmtMillis;
            if (!this.useLegacyDatetimeCode) {
                return TimeUtil.fastTimestampCreate(cal.getTimeZone(), year, month, day, hour, minute, seconds, secondsPart);
            }
            if (cal == null) {
                cal = this.getFastDefaultCalendar();
            }
            Calendar calendar = (useGmtMillis = this.connection.getUseGmtMillisForDatetimes()) ? this.getGmtCalendar() : null;
            return TimeUtil.fastTimestampCreate(useGmtMillis, calendar, cal, year, month, day, hour, minute, seconds, secondsPart);
        }
    }

    @Override
    public int findColumn(String columnName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            Integer index;
            if (!this.hasBuiltIndexMapping) {
                this.buildIndexMapping();
            }
            if ((index = this.columnToIndexCache.get(columnName)) != null) {
                return index + 1;
            }
            index = this.columnLabelToIndex.get(columnName);
            if (index == null && this.useColumnNamesInFindColumn) {
                index = this.columnNameToIndex.get(columnName);
            }
            if (index == null) {
                index = this.fullColumnNameToIndex.get(columnName);
            }
            if (index != null) {
                this.columnToIndexCache.put(columnName, index);
                return index + 1;
            }
            for (int i = 0; i < this.fields.length; ++i) {
                if (this.fields[i].getName().equalsIgnoreCase(columnName)) {
                    return i + 1;
                }
                if (!this.fields[i].getFullName().equalsIgnoreCase(columnName)) continue;
                return i + 1;
            }
            throw SQLError.createSQLException(Messages.getString("ResultSet.Column____112") + columnName + Messages.getString("ResultSet.___not_found._113"), "S0022", this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean first() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            boolean b = true;
            if (this.rowData.isEmpty()) {
                b = false;
            } else {
                if (this.onInsertRow) {
                    this.onInsertRow = false;
                }
                if (this.doingUpdates) {
                    this.doingUpdates = false;
                }
                this.rowData.beforeFirst();
                this.thisRow = this.rowData.next();
            }
            this.setRowPositionValidity();
            return b;
        }
    }

    @Override
    public Array getArray(int i) throws SQLException {
        this.checkColumnBounds(i);
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    public Array getArray(String colName) throws SQLException {
        return this.getArray(this.findColumn(colName));
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        this.checkRowPos();
        if (!this.isBinaryEncoded) {
            return this.getBinaryStream(columnIndex);
        }
        return this.getNativeBinaryStream(columnIndex);
    }

    @Override
    public InputStream getAsciiStream(String columnName) throws SQLException {
        return this.getAsciiStream(this.findColumn(columnName));
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        if (!this.isBinaryEncoded) {
            String stringVal = this.getString(columnIndex);
            if (stringVal != null) {
                if (stringVal.length() == 0) {
                    BigDecimal val = new BigDecimal(this.convertToZeroLiteralStringWithEmptyCheck());
                    return val;
                }
                try {
                    BigDecimal val = new BigDecimal(stringVal);
                    return val;
                }
                catch (NumberFormatException ex) {
                    throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, columnIndex}), "S1009", this.getExceptionInterceptor());
                }
            }
            return null;
        }
        return this.getNativeBigDecimal(columnIndex);
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        if (!this.isBinaryEncoded) {
            String stringVal = this.getString(columnIndex);
            if (stringVal != null) {
                BigDecimal val;
                if (stringVal.length() == 0) {
                    BigDecimal val2 = new BigDecimal(this.convertToZeroLiteralStringWithEmptyCheck());
                    try {
                        return val2.setScale(scale);
                    }
                    catch (ArithmeticException ex) {
                        try {
                            return val2.setScale(scale, 4);
                        }
                        catch (ArithmeticException arEx) {
                            throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, columnIndex}), "S1009", this.getExceptionInterceptor());
                        }
                    }
                }
                try {
                    val = new BigDecimal(stringVal);
                }
                catch (NumberFormatException ex) {
                    if (this.fields[columnIndex - 1].getMysqlType() == 16) {
                        long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex);
                        val = new BigDecimal(valueAsLong);
                    }
                    throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{columnIndex, stringVal}), "S1009", this.getExceptionInterceptor());
                }
                try {
                    return val.setScale(scale);
                }
                catch (ArithmeticException ex) {
                    try {
                        return val.setScale(scale, 4);
                    }
                    catch (ArithmeticException arithEx) {
                        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{columnIndex, stringVal}), "S1009", this.getExceptionInterceptor());
                    }
                }
            }
            return null;
        }
        return this.getNativeBigDecimal(columnIndex, scale);
    }

    @Override
    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return this.getBigDecimal(this.findColumn(columnName));
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        return this.getBigDecimal(this.findColumn(columnName), scale);
    }

    private final BigDecimal getBigDecimalFromString(String stringVal, int columnIndex, int scale) throws SQLException {
        if (stringVal != null) {
            if (stringVal.length() == 0) {
                BigDecimal bdVal = new BigDecimal(this.convertToZeroLiteralStringWithEmptyCheck());
                try {
                    return bdVal.setScale(scale);
                }
                catch (ArithmeticException ex) {
                    try {
                        return bdVal.setScale(scale, 4);
                    }
                    catch (ArithmeticException arEx) {
                        throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, columnIndex}), "S1009");
                    }
                }
            }
            try {
                try {
                    return new BigDecimal(stringVal).setScale(scale);
                }
                catch (ArithmeticException ex) {
                    try {
                        return new BigDecimal(stringVal).setScale(scale, 4);
                    }
                    catch (ArithmeticException arEx) {
                        throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, columnIndex}), "S1009");
                    }
                }
            }
            catch (NumberFormatException ex) {
                if (this.fields[columnIndex - 1].getMysqlType() == 16) {
                    long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex);
                    try {
                        return new BigDecimal(valueAsLong).setScale(scale);
                    }
                    catch (ArithmeticException arEx1) {
                        try {
                            return new BigDecimal(valueAsLong).setScale(scale, 4);
                        }
                        catch (ArithmeticException arEx2) {
                            throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, columnIndex}), "S1009");
                        }
                    }
                }
                if (this.fields[columnIndex - 1].getMysqlType() == 1 && this.connection.getTinyInt1isBit() && this.fields[columnIndex - 1].getLength() == 1L) {
                    return new BigDecimal(stringVal.equalsIgnoreCase("true") ? 1 : 0).setScale(scale);
                }
                throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, columnIndex}), "S1009");
            }
        }
        return null;
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        this.checkRowPos();
        if (!this.isBinaryEncoded) {
            this.checkColumnBounds(columnIndex);
            int columnIndexMinusOne = columnIndex - 1;
            if (this.thisRow.isNull(columnIndexMinusOne)) {
                this.wasNullFlag = true;
                return null;
            }
            this.wasNullFlag = false;
            return this.thisRow.getBinaryInputStream(columnIndexMinusOne);
        }
        return this.getNativeBinaryStream(columnIndex);
    }

    @Override
    public InputStream getBinaryStream(String columnName) throws SQLException {
        return this.getBinaryStream(this.findColumn(columnName));
    }

    @Override
    public java.sql.Blob getBlob(int columnIndex) throws SQLException {
        if (!this.isBinaryEncoded) {
            this.checkRowPos();
            this.checkColumnBounds(columnIndex);
            int columnIndexMinusOne = columnIndex - 1;
            this.wasNullFlag = this.thisRow.isNull(columnIndexMinusOne);
            if (this.wasNullFlag) {
                return null;
            }
            if (!this.connection.getEmulateLocators()) {
                return new Blob(this.thisRow.getColumnValue(columnIndexMinusOne), this.getExceptionInterceptor());
            }
            return new BlobFromLocator(this, columnIndex, this.getExceptionInterceptor());
        }
        return this.getNativeBlob(columnIndex);
    }

    @Override
    public java.sql.Blob getBlob(String colName) throws SQLException {
        return this.getBlob(this.findColumn(colName));
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        this.checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        Field field = this.fields[columnIndexMinusOne];
        if (field.getMysqlType() == 16) {
            return this.byteArrayToBoolean(columnIndexMinusOne);
        }
        this.wasNullFlag = false;
        int sqlType = field.getSQLType();
        switch (sqlType) {
            case 16: {
                if (field.getMysqlType() == -1) {
                    String stringVal = this.getString(columnIndex);
                    return this.getBooleanFromString(stringVal);
                }
                long boolVal = this.getLong(columnIndex, false);
                return boolVal == -1L || boolVal > 0L;
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
                long boolVal = this.getLong(columnIndex, false);
                return boolVal == -1L || boolVal > 0L;
            }
        }
        if (this.connection.getPedantic()) {
            switch (sqlType) {
                case -4: 
                case -3: 
                case -2: 
                case 70: 
                case 91: 
                case 92: 
                case 93: 
                case 2000: 
                case 2002: 
                case 2003: 
                case 2004: 
                case 2005: 
                case 2006: {
                    throw SQLError.createSQLException("Required type conversion not allowed", "22018", this.getExceptionInterceptor());
                }
            }
        }
        if (sqlType == -2 || sqlType == -3 || sqlType == -4 || sqlType == 2004) {
            return this.byteArrayToBoolean(columnIndexMinusOne);
        }
        if (this.useUsageAdvisor) {
            this.issueConversionViaParsingWarning("getBoolean()", columnIndex, this.thisRow.getColumnValue(columnIndexMinusOne), this.fields[columnIndex], new int[]{16, 5, 1, 2, 3, 8, 4});
        }
        String stringVal = this.getString(columnIndex);
        return this.getBooleanFromString(stringVal);
    }

    private boolean byteArrayToBoolean(int columnIndexMinusOne) throws SQLException {
        byte[] value = this.thisRow.getColumnValue(columnIndexMinusOne);
        if (value == null) {
            this.wasNullFlag = true;
            return false;
        }
        this.wasNullFlag = false;
        if (value.length == 0) {
            return false;
        }
        byte boolVal = value[0];
        if (boolVal == 49) {
            return true;
        }
        if (boolVal == 48) {
            return false;
        }
        return boolVal == -1 || boolVal > 0;
    }

    @Override
    public boolean getBoolean(String columnName) throws SQLException {
        return this.getBoolean(this.findColumn(columnName));
    }

    private final boolean getBooleanFromString(String stringVal) throws SQLException {
        if (stringVal != null && stringVal.length() > 0) {
            char c = Character.toLowerCase(stringVal.charAt(0));
            return c == 't' || c == 'y' || c == '1' || stringVal.equals("-1");
        }
        return false;
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        if (!this.isBinaryEncoded) {
            String stringVal = this.getString(columnIndex);
            if (this.wasNullFlag || stringVal == null) {
                return 0;
            }
            return this.getByteFromString(stringVal, columnIndex);
        }
        return this.getNativeByte(columnIndex);
    }

    @Override
    public byte getByte(String columnName) throws SQLException {
        return this.getByte(this.findColumn(columnName));
    }

    private final byte getByteFromString(String stringVal, int columnIndex) throws SQLException {
        if (stringVal != null && stringVal.length() == 0) {
            return (byte)this.convertToZeroWithEmptyCheck();
        }
        if (stringVal == null) {
            return 0;
        }
        stringVal = stringVal.trim();
        try {
            int decimalIndex = stringVal.indexOf(".");
            if (decimalIndex != -1) {
                double valueAsDouble = Double.parseDouble(stringVal);
                if (this.jdbcCompliantTruncationForReads && (valueAsDouble < -128.0 || valueAsDouble > 127.0)) {
                    this.throwRangeException(stringVal, columnIndex, -6);
                }
                return (byte)valueAsDouble;
            }
            long valueAsLong = Long.parseLong(stringVal);
            if (this.jdbcCompliantTruncationForReads && (valueAsLong < -128L || valueAsLong > 127L)) {
                this.throwRangeException(String.valueOf(valueAsLong), columnIndex, -6);
            }
            return (byte)valueAsLong;
        }
        catch (NumberFormatException NFE) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Value____173") + stringVal + Messages.getString("ResultSet.___is_out_of_range_[-127,127]_174"), "S1009", this.getExceptionInterceptor());
        }
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        return this.getBytes(columnIndex, false);
    }

    protected byte[] getBytes(int columnIndex, boolean noConversion) throws SQLException {
        if (!this.isBinaryEncoded) {
            this.checkRowPos();
            this.checkColumnBounds(columnIndex);
            int columnIndexMinusOne = columnIndex - 1;
            this.wasNullFlag = this.thisRow.isNull(columnIndexMinusOne);
            if (this.wasNullFlag) {
                return null;
            }
            return this.thisRow.getColumnValue(columnIndexMinusOne);
        }
        return this.getNativeBytes(columnIndex, noConversion);
    }

    @Override
    public byte[] getBytes(String columnName) throws SQLException {
        return this.getBytes(this.findColumn(columnName));
    }

    private final byte[] getBytesFromString(String stringVal) throws SQLException {
        if (stringVal != null) {
            return StringUtils.getBytes(stringVal, this.connection.getEncoding(), this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.connection, this.getExceptionInterceptor());
        }
        return null;
    }

    @Override
    public int getBytesSize() throws SQLException {
        RowData localRowData = this.rowData;
        this.checkClosed();
        if (localRowData instanceof RowDataStatic) {
            int bytesSize = 0;
            int numRows = localRowData.size();
            for (int i = 0; i < numRows; ++i) {
                bytesSize += localRowData.getAt(i).getBytesSize();
            }
            return bytesSize;
        }
        return -1;
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

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        if (!this.isBinaryEncoded) {
            this.checkColumnBounds(columnIndex);
            int columnIndexMinusOne = columnIndex - 1;
            if (this.thisRow.isNull(columnIndexMinusOne)) {
                this.wasNullFlag = true;
                return null;
            }
            this.wasNullFlag = false;
            return this.thisRow.getReader(columnIndexMinusOne);
        }
        return this.getNativeCharacterStream(columnIndex);
    }

    @Override
    public Reader getCharacterStream(String columnName) throws SQLException {
        return this.getCharacterStream(this.findColumn(columnName));
    }

    private final Reader getCharacterStreamFromString(String stringVal) throws SQLException {
        if (stringVal != null) {
            return new StringReader(stringVal);
        }
        return null;
    }

    @Override
    public java.sql.Clob getClob(int i) throws SQLException {
        if (!this.isBinaryEncoded) {
            String asString = this.getStringForClob(i);
            if (asString == null) {
                return null;
            }
            return new Clob(asString, this.getExceptionInterceptor());
        }
        return this.getNativeClob(i);
    }

    @Override
    public java.sql.Clob getClob(String colName) throws SQLException {
        return this.getClob(this.findColumn(colName));
    }

    private final java.sql.Clob getClobFromString(String stringVal) throws SQLException {
        return new Clob(stringVal, this.getExceptionInterceptor());
    }

    @Override
    public int getConcurrency() throws SQLException {
        return 1007;
    }

    @Override
    public String getCursorName() throws SQLException {
        throw SQLError.createSQLException(Messages.getString("ResultSet.Positioned_Update_not_supported"), "S1C00", this.getExceptionInterceptor());
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return this.getDate(columnIndex, null);
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        if (this.isBinaryEncoded) {
            return this.getNativeDate(columnIndex, cal);
        }
        if (!this.useFastDateParsing) {
            String stringVal = this.getStringInternal(columnIndex, false);
            if (stringVal == null) {
                return null;
            }
            return this.getDateFromString(stringVal, columnIndex, cal);
        }
        this.checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        Date tmpDate = this.thisRow.getDateFast(columnIndexMinusOne, this.connection, this, cal);
        if (this.thisRow.isNull(columnIndexMinusOne) || tmpDate == null) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        return tmpDate;
    }

    @Override
    public Date getDate(String columnName) throws SQLException {
        return this.getDate(this.findColumn(columnName));
    }

    @Override
    public Date getDate(String columnName, Calendar cal) throws SQLException {
        return this.getDate(this.findColumn(columnName), cal);
    }

    private final Date getDateFromString(String stringVal, int columnIndex, Calendar targetCalendar) throws SQLException {
        int year = 0;
        int month = 0;
        int day = 0;
        try {
            this.wasNullFlag = false;
            if (stringVal == null) {
                this.wasNullFlag = true;
                return null;
            }
            int dec = (stringVal = stringVal.trim()).indexOf(".");
            if (dec > -1) {
                stringVal = stringVal.substring(0, dec);
            }
            if (stringVal.equals("0") || stringVal.equals("0000-00-00") || stringVal.equals("0000-00-00 00:00:00") || stringVal.equals("00000000000000") || stringVal.equals("0")) {
                if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior())) {
                    this.wasNullFlag = true;
                    return null;
                }
                if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
                    throw SQLError.createSQLException("Value '" + stringVal + "' can not be represented as java.sql.Date", "S1009", this.getExceptionInterceptor());
                }
                return this.fastDateCreate(targetCalendar, 1, 1, 1);
            }
            if (this.fields[columnIndex - 1].getMysqlType() == 7) {
                switch (stringVal.length()) {
                    case 19: 
                    case 21: {
                        year = Integer.parseInt(stringVal.substring(0, 4));
                        month = Integer.parseInt(stringVal.substring(5, 7));
                        day = Integer.parseInt(stringVal.substring(8, 10));
                        return this.fastDateCreate(targetCalendar, year, month, day);
                    }
                    case 8: 
                    case 14: {
                        year = Integer.parseInt(stringVal.substring(0, 4));
                        month = Integer.parseInt(stringVal.substring(4, 6));
                        day = Integer.parseInt(stringVal.substring(6, 8));
                        return this.fastDateCreate(targetCalendar, year, month, day);
                    }
                    case 6: 
                    case 10: 
                    case 12: {
                        year = Integer.parseInt(stringVal.substring(0, 2));
                        if (year <= 69) {
                            year += 100;
                        }
                        month = Integer.parseInt(stringVal.substring(2, 4));
                        day = Integer.parseInt(stringVal.substring(4, 6));
                        return this.fastDateCreate(targetCalendar, year + 1900, month, day);
                    }
                    case 4: {
                        year = Integer.parseInt(stringVal.substring(0, 4));
                        if (year <= 69) {
                            year += 100;
                        }
                        month = Integer.parseInt(stringVal.substring(2, 4));
                        return this.fastDateCreate(targetCalendar, year + 1900, month, 1);
                    }
                    case 2: {
                        year = Integer.parseInt(stringVal.substring(0, 2));
                        if (year <= 69) {
                            year += 100;
                        }
                        return this.fastDateCreate(targetCalendar, year + 1900, 1, 1);
                    }
                }
                throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[]{stringVal, columnIndex}), "S1009", this.getExceptionInterceptor());
            }
            if (this.fields[columnIndex - 1].getMysqlType() == 13) {
                if (stringVal.length() == 2 || stringVal.length() == 1) {
                    year = Integer.parseInt(stringVal);
                    if (year <= 69) {
                        year += 100;
                    }
                    year += 1900;
                } else {
                    year = Integer.parseInt(stringVal.substring(0, 4));
                }
                return this.fastDateCreate(targetCalendar, year, 1, 1);
            }
            if (this.fields[columnIndex - 1].getMysqlType() == 11) {
                return this.fastDateCreate(targetCalendar, 1970, 1, 1);
            }
            if (stringVal.length() < 10) {
                if (stringVal.length() == 8) {
                    return this.fastDateCreate(targetCalendar, 1970, 1, 1);
                }
                throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[]{stringVal, columnIndex}), "S1009", this.getExceptionInterceptor());
            }
            if (stringVal.length() != 18) {
                year = Integer.parseInt(stringVal.substring(0, 4));
                month = Integer.parseInt(stringVal.substring(5, 7));
                day = Integer.parseInt(stringVal.substring(8, 10));
            } else {
                StringTokenizer st = new StringTokenizer(stringVal, "- ");
                year = Integer.parseInt(st.nextToken());
                month = Integer.parseInt(st.nextToken());
                day = Integer.parseInt(st.nextToken());
            }
            return this.fastDateCreate(targetCalendar, year, month, day);
        }
        catch (SQLException sqlEx) {
            throw sqlEx;
        }
        catch (Exception e) {
            SQLException sqlEx = SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[]{stringVal, columnIndex}), "S1009", this.getExceptionInterceptor());
            sqlEx.initCause(e);
            throw sqlEx;
        }
    }

    private TimeZone getDefaultTimeZone() {
        return this.useLegacyDatetimeCode ? this.connection.getDefaultTimeZone() : this.serverTimeZoneTz;
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        if (!this.isBinaryEncoded) {
            return this.getDoubleInternal(columnIndex);
        }
        return this.getNativeDouble(columnIndex);
    }

    @Override
    public double getDouble(String columnName) throws SQLException {
        return this.getDouble(this.findColumn(columnName));
    }

    private final double getDoubleFromString(String stringVal, int columnIndex) throws SQLException {
        return this.getDoubleInternal(stringVal, columnIndex);
    }

    protected double getDoubleInternal(int colIndex) throws SQLException {
        return this.getDoubleInternal(this.getString(colIndex), colIndex);
    }

    protected double getDoubleInternal(String stringVal, int colIndex) throws SQLException {
        try {
            if (stringVal == null) {
                return 0.0;
            }
            if (stringVal.length() == 0) {
                return this.convertToZeroWithEmptyCheck();
            }
            double d = Double.parseDouble(stringVal);
            if (this.useStrictFloatingPoint) {
                if (d == 2.147483648E9) {
                    d = 2.147483647E9;
                } else if (d == 1.0000000036275E-15) {
                    d = 1.0E-15;
                } else if (d == 9.999999869911E14) {
                    d = 9.99999999999999E14;
                } else if (d == 1.4012984643248E-45) {
                    d = 1.4E-45;
                } else if (d == 1.4013E-45) {
                    d = 1.4E-45;
                } else if (d == 3.4028234663853E37) {
                    d = 3.4028235E37;
                } else if (d == -2.14748E9) {
                    d = -2.147483648E9;
                } else if (d == 3.40282E37) {
                    d = 3.4028235E37;
                }
            }
            return d;
        }
        catch (NumberFormatException e) {
            if (this.fields[colIndex - 1].getMysqlType() == 16) {
                long valueAsLong = this.getNumericRepresentationOfSQLBitType(colIndex);
                return valueAsLong;
            }
            throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_number", new Object[]{stringVal, colIndex}), "S1009", this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getFetchDirection() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.fetchDirection;
        }
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
    public char getFirstCharOfQuery() {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                return this.firstCharOfQuery;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        if (!this.isBinaryEncoded) {
            String val = null;
            val = this.getString(columnIndex);
            return this.getFloatFromString(val, columnIndex);
        }
        return this.getNativeFloat(columnIndex);
    }

    @Override
    public float getFloat(String columnName) throws SQLException {
        return this.getFloat(this.findColumn(columnName));
    }

    private final float getFloatFromString(String val, int columnIndex) throws SQLException {
        try {
            if (val != null) {
                double valAsDouble;
                if (val.length() == 0) {
                    return this.convertToZeroWithEmptyCheck();
                }
                float f = Float.parseFloat(val);
                if (this.jdbcCompliantTruncationForReads && (f == Float.MIN_VALUE || f == Float.MAX_VALUE) && ((valAsDouble = Double.parseDouble(val)) < (double)1.4E-45f - MIN_DIFF_PREC || valAsDouble > 3.4028234663852886E38 - MAX_DIFF_PREC)) {
                    this.throwRangeException(String.valueOf(valAsDouble), columnIndex, 6);
                }
                return f;
            }
            return 0.0f;
        }
        catch (NumberFormatException nfe) {
            try {
                Double valueAsDouble = new Double(val);
                float valueAsFloat = valueAsDouble.floatValue();
                if (this.jdbcCompliantTruncationForReads && (this.jdbcCompliantTruncationForReads && valueAsFloat == Float.NEGATIVE_INFINITY || valueAsFloat == Float.POSITIVE_INFINITY)) {
                    this.throwRangeException(valueAsDouble.toString(), columnIndex, 6);
                }
                return valueAsFloat;
            }
            catch (NumberFormatException newNfe) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getFloat()_-____200") + val + Messages.getString("ResultSet.___in_column__201") + columnIndex, "S1009", this.getExceptionInterceptor());
            }
        }
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        if (!this.isBinaryEncoded) {
            int columnIndexMinusOne = columnIndex - 1;
            if (this.fields[columnIndexMinusOne].getMysqlType() == 16) {
                long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex);
                if (this.jdbcCompliantTruncationForReads && (valueAsLong < Integer.MIN_VALUE || valueAsLong > Integer.MAX_VALUE)) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex, 4);
                }
                return (int)valueAsLong;
            }
            if (this.useFastIntParsing) {
                this.wasNullFlag = this.thisRow.isNull(columnIndexMinusOne);
                if (this.wasNullFlag) {
                    return 0;
                }
                if (this.thisRow.length(columnIndexMinusOne) == 0L) {
                    return this.convertToZeroWithEmptyCheck();
                }
                boolean needsFullParse = this.thisRow.isFloatingPointNumber(columnIndexMinusOne);
                if (!needsFullParse) {
                    try {
                        return this.getIntWithOverflowCheck(columnIndexMinusOne);
                    }
                    catch (NumberFormatException nfe) {
                        try {
                            return this.parseIntAsDouble(columnIndex, this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getEncoding(), this.connection));
                        }
                        catch (NumberFormatException newNfe) {
                            throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getInt()_-____74") + this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getEncoding(), this.connection) + "'", "S1009", this.getExceptionInterceptor());
                        }
                    }
                }
            }
            String val = null;
            try {
                val = this.getString(columnIndex);
                if (val != null) {
                    if (val.length() == 0) {
                        return this.convertToZeroWithEmptyCheck();
                    }
                    if (val.indexOf("e") == -1 && val.indexOf("E") == -1 && val.indexOf(".") == -1) {
                        int intVal = Integer.parseInt(val);
                        this.checkForIntegerTruncation(columnIndexMinusOne, null, intVal);
                        return intVal;
                    }
                    int intVal = this.parseIntAsDouble(columnIndex, val);
                    this.checkForIntegerTruncation(columnIndex, null, intVal);
                    return intVal;
                }
                return 0;
            }
            catch (NumberFormatException nfe) {
                try {
                    return this.parseIntAsDouble(columnIndex, val);
                }
                catch (NumberFormatException newNfe) {
                    throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getInt()_-____74") + val + "'", "S1009", this.getExceptionInterceptor());
                }
            }
        }
        return this.getNativeInt(columnIndex);
    }

    @Override
    public int getInt(String columnName) throws SQLException {
        return this.getInt(this.findColumn(columnName));
    }

    private final int getIntFromString(String val, int columnIndex) throws SQLException {
        try {
            if (val != null) {
                if (val.length() == 0) {
                    return this.convertToZeroWithEmptyCheck();
                }
                if (val.indexOf("e") == -1 && val.indexOf("E") == -1 && val.indexOf(".") == -1) {
                    long valueAsLong;
                    val = val.trim();
                    int valueAsInt = Integer.parseInt(val);
                    if (!(!this.jdbcCompliantTruncationForReads || valueAsInt != Integer.MIN_VALUE && valueAsInt != Integer.MAX_VALUE || (valueAsLong = Long.parseLong(val)) >= Integer.MIN_VALUE && valueAsLong <= Integer.MAX_VALUE)) {
                        this.throwRangeException(String.valueOf(valueAsLong), columnIndex, 4);
                    }
                    return valueAsInt;
                }
                double valueAsDouble = Double.parseDouble(val);
                if (this.jdbcCompliantTruncationForReads && (valueAsDouble < -2.147483648E9 || valueAsDouble > 2.147483647E9)) {
                    this.throwRangeException(String.valueOf(valueAsDouble), columnIndex, 4);
                }
                return (int)valueAsDouble;
            }
            return 0;
        }
        catch (NumberFormatException nfe) {
            try {
                double valueAsDouble = Double.parseDouble(val);
                if (this.jdbcCompliantTruncationForReads && (valueAsDouble < -2.147483648E9 || valueAsDouble > 2.147483647E9)) {
                    this.throwRangeException(String.valueOf(valueAsDouble), columnIndex, 4);
                }
                return (int)valueAsDouble;
            }
            catch (NumberFormatException newNfe) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getInt()_-____206") + val + Messages.getString("ResultSet.___in_column__207") + columnIndex, "S1009", this.getExceptionInterceptor());
            }
        }
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return this.getLong(columnIndex, true);
    }

    private long getLong(int columnIndex, boolean overflowCheck) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        if (!this.isBinaryEncoded) {
            int columnIndexMinusOne = columnIndex - 1;
            if (this.fields[columnIndexMinusOne].getMysqlType() == 16) {
                return this.getNumericRepresentationOfSQLBitType(columnIndex);
            }
            if (this.useFastIntParsing) {
                this.wasNullFlag = this.thisRow.isNull(columnIndexMinusOne);
                if (this.wasNullFlag) {
                    return 0L;
                }
                if (this.thisRow.length(columnIndexMinusOne) == 0L) {
                    return this.convertToZeroWithEmptyCheck();
                }
                boolean needsFullParse = this.thisRow.isFloatingPointNumber(columnIndexMinusOne);
                if (!needsFullParse) {
                    try {
                        return this.getLongWithOverflowCheck(columnIndexMinusOne, overflowCheck);
                    }
                    catch (NumberFormatException nfe) {
                        try {
                            return this.parseLongAsDouble(columnIndexMinusOne, this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getEncoding(), this.connection));
                        }
                        catch (NumberFormatException newNfe) {
                            throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getLong()_-____79") + this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getEncoding(), this.connection) + "'", "S1009", this.getExceptionInterceptor());
                        }
                    }
                }
            }
            String val = null;
            try {
                val = this.getString(columnIndex);
                if (val != null) {
                    if (val.length() == 0) {
                        return this.convertToZeroWithEmptyCheck();
                    }
                    if (val.indexOf("e") == -1 && val.indexOf("E") == -1) {
                        return this.parseLongWithOverflowCheck(columnIndexMinusOne, null, val, overflowCheck);
                    }
                    return this.parseLongAsDouble(columnIndexMinusOne, val);
                }
                return 0L;
            }
            catch (NumberFormatException nfe) {
                try {
                    return this.parseLongAsDouble(columnIndexMinusOne, val);
                }
                catch (NumberFormatException newNfe) {
                    throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getLong()_-____79") + val + "'", "S1009", this.getExceptionInterceptor());
                }
            }
        }
        return this.getNativeLong(columnIndex, overflowCheck, true);
    }

    @Override
    public long getLong(String columnName) throws SQLException {
        return this.getLong(this.findColumn(columnName));
    }

    private final long getLongFromString(String val, int columnIndexZeroBased) throws SQLException {
        try {
            if (val != null) {
                if (val.length() == 0) {
                    return this.convertToZeroWithEmptyCheck();
                }
                if (val.indexOf("e") == -1 && val.indexOf("E") == -1) {
                    return this.parseLongWithOverflowCheck(columnIndexZeroBased, null, val, true);
                }
                return this.parseLongAsDouble(columnIndexZeroBased, val);
            }
            return 0L;
        }
        catch (NumberFormatException nfe) {
            try {
                return this.parseLongAsDouble(columnIndexZeroBased, val);
            }
            catch (NumberFormatException newNfe) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getLong()_-____211") + val + Messages.getString("ResultSet.___in_column__212") + (columnIndexZeroBased + 1), "S1009", this.getExceptionInterceptor());
            }
        }
    }

    @Override
    public java.sql.ResultSetMetaData getMetaData() throws SQLException {
        this.checkClosed();
        return new ResultSetMetaData(this.fields, this.connection.getUseOldAliasMetadataBehavior(), this.connection.getYearIsDateType(), this.getExceptionInterceptor());
    }

    protected Array getNativeArray(int i) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    protected InputStream getNativeAsciiStream(int columnIndex) throws SQLException {
        this.checkRowPos();
        return this.getNativeBinaryStream(columnIndex);
    }

    protected BigDecimal getNativeBigDecimal(int columnIndex) throws SQLException {
        this.checkColumnBounds(columnIndex);
        int scale = this.fields[columnIndex - 1].getDecimals();
        return this.getNativeBigDecimal(columnIndex, scale);
    }

    protected BigDecimal getNativeBigDecimal(int columnIndex, int scale) throws SQLException {
        this.checkColumnBounds(columnIndex);
        String stringVal = null;
        Field f = this.fields[columnIndex - 1];
        byte[] value = this.thisRow.getColumnValue(columnIndex - 1);
        if (value == null) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        switch (f.getSQLType()) {
            case 2: 
            case 3: {
                stringVal = StringUtils.toAsciiString(value);
                break;
            }
            default: {
                stringVal = this.getNativeString(columnIndex);
            }
        }
        return this.getBigDecimalFromString(stringVal, columnIndex, scale);
    }

    protected InputStream getNativeBinaryStream(int columnIndex) throws SQLException {
        this.checkRowPos();
        int columnIndexMinusOne = columnIndex - 1;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        switch (this.fields[columnIndexMinusOne].getSQLType()) {
            case -7: 
            case -4: 
            case -3: 
            case -2: 
            case 2004: {
                return this.thisRow.getBinaryInputStream(columnIndexMinusOne);
            }
        }
        byte[] b = this.getNativeBytes(columnIndex, false);
        if (b != null) {
            return new ByteArrayInputStream(b);
        }
        return null;
    }

    protected java.sql.Blob getNativeBlob(int columnIndex) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        byte[] value = this.thisRow.getColumnValue(columnIndex - 1);
        this.wasNullFlag = value == null;
        if (this.wasNullFlag) {
            return null;
        }
        int mysqlType = this.fields[columnIndex - 1].getMysqlType();
        byte[] dataAsBytes = null;
        switch (mysqlType) {
            case 249: 
            case 250: 
            case 251: 
            case 252: {
                dataAsBytes = value;
                break;
            }
            default: {
                dataAsBytes = this.getNativeBytes(columnIndex, false);
            }
        }
        if (!this.connection.getEmulateLocators()) {
            return new Blob(dataAsBytes, this.getExceptionInterceptor());
        }
        return new BlobFromLocator(this, columnIndex, this.getExceptionInterceptor());
    }

    public static boolean arraysEqual(byte[] left, byte[] right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }
        if (left.length != right.length) {
            return false;
        }
        for (int i = 0; i < left.length; ++i) {
            if (left[i] == right[i]) continue;
            return false;
        }
        return true;
    }

    protected byte getNativeByte(int columnIndex) throws SQLException {
        return this.getNativeByte(columnIndex, true);
    }

    protected byte getNativeByte(int columnIndex, boolean overflowCheck) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        byte[] value = this.thisRow.getColumnValue(columnIndex - 1);
        if (value == null) {
            this.wasNullFlag = true;
            return 0;
        }
        this.wasNullFlag = false;
        Field field = this.fields[--columnIndex];
        switch (field.getMysqlType()) {
            case 16: {
                long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < -128L || valueAsLong > 127L)) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, -6);
                }
                return (byte)valueAsLong;
            }
            case 1: {
                short valueAsShort;
                byte valueAsByte = value[0];
                if (!field.isUnsigned()) {
                    return valueAsByte;
                }
                short s = valueAsShort = valueAsByte >= 0 ? (short)valueAsByte : (short)(valueAsByte + 256);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && valueAsShort > 127) {
                    this.throwRangeException(String.valueOf(valueAsShort), columnIndex + 1, -6);
                }
                return (byte)valueAsShort;
            }
            case 2: 
            case 13: {
                short valueAsShort = this.getNativeShort(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsShort < -128 || valueAsShort > 127)) {
                    this.throwRangeException(String.valueOf(valueAsShort), columnIndex + 1, -6);
                }
                return (byte)valueAsShort;
            }
            case 3: 
            case 9: {
                int valueAsInt = this.getNativeInt(columnIndex + 1, false);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsInt < -128 || valueAsInt > 127)) {
                    this.throwRangeException(String.valueOf(valueAsInt), columnIndex + 1, -6);
                }
                return (byte)valueAsInt;
            }
            case 4: {
                float valueAsFloat = this.getNativeFloat(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsFloat < -128.0f || valueAsFloat > 127.0f)) {
                    this.throwRangeException(String.valueOf(valueAsFloat), columnIndex + 1, -6);
                }
                return (byte)valueAsFloat;
            }
            case 5: {
                double valueAsDouble = this.getNativeDouble(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < -128.0 || valueAsDouble > 127.0)) {
                    this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, -6);
                }
                return (byte)valueAsDouble;
            }
            case 8: {
                long valueAsLong = this.getNativeLong(columnIndex + 1, false, true);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < -128L || valueAsLong > 127L)) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, -6);
                }
                return (byte)valueAsLong;
            }
        }
        if (this.useUsageAdvisor) {
            this.issueConversionViaParsingWarning("getByte()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[columnIndex], new int[]{5, 1, 2, 3, 8, 4});
        }
        return this.getByteFromString(this.getNativeString(columnIndex + 1), columnIndex + 1);
    }

    protected byte[] getNativeBytes(int columnIndex, boolean noConversion) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        byte[] value = this.thisRow.getColumnValue(columnIndex - 1);
        this.wasNullFlag = value == null;
        if (this.wasNullFlag) {
            return null;
        }
        Field field = this.fields[columnIndex - 1];
        int mysqlType = field.getMysqlType();
        if (noConversion) {
            mysqlType = 252;
        }
        switch (mysqlType) {
            case 16: 
            case 249: 
            case 250: 
            case 251: 
            case 252: {
                return value;
            }
            case 15: 
            case 253: 
            case 254: {
                if (!(value instanceof byte[])) break;
                return value;
            }
        }
        int sqlType = field.getSQLType();
        if (sqlType == -3 || sqlType == -2) {
            return value;
        }
        return this.getBytesFromString(this.getNativeString(columnIndex));
    }

    protected Reader getNativeCharacterStream(int columnIndex) throws SQLException {
        int columnIndexMinusOne = columnIndex - 1;
        switch (this.fields[columnIndexMinusOne].getSQLType()) {
            case -1: 
            case 1: 
            case 12: 
            case 2005: {
                if (this.thisRow.isNull(columnIndexMinusOne)) {
                    this.wasNullFlag = true;
                    return null;
                }
                this.wasNullFlag = false;
                return this.thisRow.getReader(columnIndexMinusOne);
            }
        }
        String asString = this.getStringForClob(columnIndex);
        if (asString == null) {
            return null;
        }
        return this.getCharacterStreamFromString(asString);
    }

    protected java.sql.Clob getNativeClob(int columnIndex) throws SQLException {
        String stringVal = this.getStringForClob(columnIndex);
        if (stringVal == null) {
            return null;
        }
        return this.getClobFromString(stringVal);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String getNativeConvertToString(int columnIndex, Field field) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            int sqlType = field.getSQLType();
            int mysqlType = field.getMysqlType();
            switch (sqlType) {
                case -7: {
                    return String.valueOf(this.getNumericRepresentationOfSQLBitType(columnIndex));
                }
                case 16: {
                    boolean booleanVal = this.getBoolean(columnIndex);
                    if (this.wasNullFlag) {
                        return null;
                    }
                    return String.valueOf(booleanVal);
                }
                case -6: {
                    byte tinyintVal = this.getNativeByte(columnIndex, false);
                    if (this.wasNullFlag) {
                        return null;
                    }
                    if (!field.isUnsigned() || tinyintVal >= 0) {
                        return String.valueOf(tinyintVal);
                    }
                    short unsignedTinyVal = (short)(tinyintVal & 0xFF);
                    return String.valueOf(unsignedTinyVal);
                }
                case 5: {
                    int intVal = this.getNativeInt(columnIndex, false);
                    if (this.wasNullFlag) {
                        return null;
                    }
                    if (!field.isUnsigned() || intVal >= 0) {
                        return String.valueOf(intVal);
                    }
                    return String.valueOf(intVal &= 0xFFFF);
                }
                case 4: {
                    int intVal = this.getNativeInt(columnIndex, false);
                    if (this.wasNullFlag) {
                        return null;
                    }
                    if (!field.isUnsigned() || intVal >= 0 || field.getMysqlType() == 9) {
                        return String.valueOf(intVal);
                    }
                    long longVal = (long)intVal & 0xFFFFFFFFL;
                    return String.valueOf(longVal);
                }
                case -5: {
                    if (!field.isUnsigned()) {
                        long longVal = this.getNativeLong(columnIndex, false, true);
                        if (this.wasNullFlag) {
                            return null;
                        }
                        return String.valueOf(longVal);
                    }
                    long longVal = this.getNativeLong(columnIndex, false, false);
                    if (this.wasNullFlag) {
                        return null;
                    }
                    return String.valueOf(ResultSetImpl.convertLongToUlong(longVal));
                }
                case 7: {
                    float floatVal = this.getNativeFloat(columnIndex);
                    if (this.wasNullFlag) {
                        return null;
                    }
                    return String.valueOf(floatVal);
                }
                case 6: 
                case 8: {
                    double doubleVal = this.getNativeDouble(columnIndex);
                    if (this.wasNullFlag) {
                        return null;
                    }
                    return String.valueOf(doubleVal);
                }
                case 2: 
                case 3: {
                    String stringVal = StringUtils.toAsciiString(this.thisRow.getColumnValue(columnIndex - 1));
                    if (stringVal != null) {
                        BigDecimal val;
                        this.wasNullFlag = false;
                        if (stringVal.length() == 0) {
                            BigDecimal val2 = new BigDecimal(0);
                            return val2.toString();
                        }
                        try {
                            val = new BigDecimal(stringVal);
                        }
                        catch (NumberFormatException ex) {
                            throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, columnIndex}), "S1009", this.getExceptionInterceptor());
                        }
                        return val.toString();
                    }
                    this.wasNullFlag = true;
                    return null;
                }
                case -1: 
                case 1: 
                case 12: {
                    return this.extractStringFromNativeColumn(columnIndex, mysqlType);
                }
                case -4: 
                case -3: 
                case -2: {
                    byte[] data;
                    if (!field.isBlob()) {
                        return this.extractStringFromNativeColumn(columnIndex, mysqlType);
                    }
                    if (!field.isBinary()) {
                        return this.extractStringFromNativeColumn(columnIndex, mysqlType);
                    }
                    Object obj = data = this.getBytes(columnIndex);
                    if (data != null && data.length >= 2) {
                        if (data[0] == -84 && data[1] == -19) {
                            try {
                                ByteArrayInputStream bytesIn = new ByteArrayInputStream(data);
                                ObjectInputStream objIn = new ObjectInputStream(bytesIn);
                                obj = objIn.readObject();
                                objIn.close();
                                bytesIn.close();
                            }
                            catch (ClassNotFoundException cnfe) {
                                throw SQLError.createSQLException(Messages.getString("ResultSet.Class_not_found___91") + cnfe.toString() + Messages.getString("ResultSet._while_reading_serialized_object_92"), this.getExceptionInterceptor());
                            }
                            catch (IOException ex) {
                                obj = data;
                            }
                        }
                        return obj.toString();
                    }
                    return this.extractStringFromNativeColumn(columnIndex, mysqlType);
                }
                case 91: {
                    Date dt;
                    if (mysqlType == 13) {
                        short shortVal = this.getNativeShort(columnIndex);
                        if (!this.connection.getYearIsDateType()) {
                            if (this.wasNullFlag) {
                                return null;
                            }
                            return String.valueOf(shortVal);
                        }
                        if (field.getLength() == 2L) {
                            if (shortVal <= 69) {
                                shortVal = (short)(shortVal + 100);
                            }
                            shortVal = (short)(shortVal + 1900);
                        }
                        return this.fastDateCreate(null, shortVal, 1, 1).toString();
                    }
                    if (this.connection.getNoDatetimeStringSync()) {
                        byte[] asBytes = this.getNativeBytes(columnIndex, true);
                        if (asBytes == null) {
                            return null;
                        }
                        if (asBytes.length == 0) {
                            return "0000-00-00";
                        }
                        int year = asBytes[0] & 0xFF | (asBytes[1] & 0xFF) << 8;
                        byte month = asBytes[2];
                        byte day = asBytes[3];
                        if (year == 0 && month == 0 && day == 0) {
                            return "0000-00-00";
                        }
                    }
                    if ((dt = this.getNativeDate(columnIndex)) == null) {
                        return null;
                    }
                    return String.valueOf(dt);
                }
                case 92: {
                    Time tm = this.getNativeTime(columnIndex, null, this.connection.getDefaultTimeZone(), false);
                    if (tm == null) {
                        return null;
                    }
                    return String.valueOf(tm);
                }
                case 93: {
                    Timestamp tstamp;
                    if (this.connection.getNoDatetimeStringSync()) {
                        byte[] asBytes = this.getNativeBytes(columnIndex, true);
                        if (asBytes == null) {
                            return null;
                        }
                        if (asBytes.length == 0) {
                            return "0000-00-00 00:00:00";
                        }
                        int year = asBytes[0] & 0xFF | (asBytes[1] & 0xFF) << 8;
                        byte month = asBytes[2];
                        byte day = asBytes[3];
                        if (year == 0 && month == 0 && day == 0) {
                            return "0000-00-00 00:00:00";
                        }
                    }
                    if ((tstamp = this.getNativeTimestamp(columnIndex, null, this.connection.getDefaultTimeZone(), false)) == null) {
                        return null;
                    }
                    String result = String.valueOf(tstamp);
                    if (!this.connection.getNoDatetimeStringSync()) {
                        return result;
                    }
                    if (result.endsWith(".0")) {
                        return result.substring(0, result.length() - 2);
                    }
                    return this.extractStringFromNativeColumn(columnIndex, mysqlType);
                }
            }
            return this.extractStringFromNativeColumn(columnIndex, mysqlType);
        }
    }

    protected Date getNativeDate(int columnIndex) throws SQLException {
        return this.getNativeDate(columnIndex, null);
    }

    protected Date getNativeDate(int columnIndex, Calendar cal) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        int mysqlType = this.fields[columnIndexMinusOne].getMysqlType();
        Date dateToReturn = null;
        if (mysqlType == 10) {
            dateToReturn = this.thisRow.getNativeDate(columnIndexMinusOne, this.connection, this, cal);
        } else {
            TimeZone tz = cal != null ? cal.getTimeZone() : this.getDefaultTimeZone();
            boolean rollForward = tz != null && !tz.equals(this.getDefaultTimeZone());
            dateToReturn = (Date)this.thisRow.getNativeDateTimeValue(columnIndexMinusOne, null, 91, mysqlType, tz, rollForward, this.connection, this);
        }
        if (dateToReturn == null) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        return dateToReturn;
    }

    Date getNativeDateViaParseConversion(int columnIndex) throws SQLException {
        if (this.useUsageAdvisor) {
            this.issueConversionViaParsingWarning("getDate()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[columnIndex - 1], new int[]{10});
        }
        String stringVal = this.getNativeString(columnIndex);
        return this.getDateFromString(stringVal, columnIndex, null);
    }

    protected double getNativeDouble(int columnIndex) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        if (this.thisRow.isNull(--columnIndex)) {
            this.wasNullFlag = true;
            return 0.0;
        }
        this.wasNullFlag = false;
        Field f = this.fields[columnIndex];
        switch (f.getMysqlType()) {
            case 5: {
                return this.thisRow.getNativeDouble(columnIndex);
            }
            case 1: {
                if (!f.isUnsigned()) {
                    return this.getNativeByte(columnIndex + 1);
                }
                return this.getNativeShort(columnIndex + 1);
            }
            case 2: 
            case 13: {
                if (!f.isUnsigned()) {
                    return this.getNativeShort(columnIndex + 1);
                }
                return this.getNativeInt(columnIndex + 1);
            }
            case 3: 
            case 9: {
                if (!f.isUnsigned()) {
                    return this.getNativeInt(columnIndex + 1);
                }
                return this.getNativeLong(columnIndex + 1);
            }
            case 8: {
                long valueAsLong = this.getNativeLong(columnIndex + 1);
                if (!f.isUnsigned()) {
                    return valueAsLong;
                }
                BigInteger asBigInt = ResultSetImpl.convertLongToUlong(valueAsLong);
                return asBigInt.doubleValue();
            }
            case 4: {
                return this.getNativeFloat(columnIndex + 1);
            }
            case 16: {
                return this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
            }
        }
        String stringVal = this.getNativeString(columnIndex + 1);
        if (this.useUsageAdvisor) {
            this.issueConversionViaParsingWarning("getDouble()", columnIndex, stringVal, this.fields[columnIndex], new int[]{5, 1, 2, 3, 8, 4});
        }
        return this.getDoubleFromString(stringVal, columnIndex + 1);
    }

    protected float getNativeFloat(int columnIndex) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        if (this.thisRow.isNull(--columnIndex)) {
            this.wasNullFlag = true;
            return 0.0f;
        }
        this.wasNullFlag = false;
        Field f = this.fields[columnIndex];
        switch (f.getMysqlType()) {
            case 16: {
                long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
                return valueAsLong;
            }
            case 5: {
                Double valueAsDouble = new Double(this.getNativeDouble(columnIndex + 1));
                float valueAsFloat = valueAsDouble.floatValue();
                if (this.jdbcCompliantTruncationForReads && valueAsFloat == Float.NEGATIVE_INFINITY || valueAsFloat == Float.POSITIVE_INFINITY) {
                    this.throwRangeException(valueAsDouble.toString(), columnIndex + 1, 6);
                }
                return (float)this.getNativeDouble(columnIndex + 1);
            }
            case 1: {
                if (!f.isUnsigned()) {
                    return this.getNativeByte(columnIndex + 1);
                }
                return this.getNativeShort(columnIndex + 1);
            }
            case 2: 
            case 13: {
                if (!f.isUnsigned()) {
                    return this.getNativeShort(columnIndex + 1);
                }
                return this.getNativeInt(columnIndex + 1);
            }
            case 3: 
            case 9: {
                if (!f.isUnsigned()) {
                    return this.getNativeInt(columnIndex + 1);
                }
                return this.getNativeLong(columnIndex + 1);
            }
            case 8: {
                long valueAsLong = this.getNativeLong(columnIndex + 1);
                if (!f.isUnsigned()) {
                    return valueAsLong;
                }
                BigInteger asBigInt = ResultSetImpl.convertLongToUlong(valueAsLong);
                return asBigInt.floatValue();
            }
            case 4: {
                return this.thisRow.getNativeFloat(columnIndex);
            }
        }
        String stringVal = this.getNativeString(columnIndex + 1);
        if (this.useUsageAdvisor) {
            this.issueConversionViaParsingWarning("getFloat()", columnIndex, stringVal, this.fields[columnIndex], new int[]{5, 1, 2, 3, 8, 4});
        }
        return this.getFloatFromString(stringVal, columnIndex + 1);
    }

    protected int getNativeInt(int columnIndex) throws SQLException {
        return this.getNativeInt(columnIndex, true);
    }

    protected int getNativeInt(int columnIndex, boolean overflowCheck) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        if (this.thisRow.isNull(--columnIndex)) {
            this.wasNullFlag = true;
            return 0;
        }
        this.wasNullFlag = false;
        Field f = this.fields[columnIndex];
        switch (f.getMysqlType()) {
            case 16: {
                long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < Integer.MIN_VALUE || valueAsLong > Integer.MAX_VALUE)) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 4);
                }
                return (int)valueAsLong;
            }
            case 1: {
                byte tinyintVal = this.getNativeByte(columnIndex + 1, false);
                if (!f.isUnsigned() || tinyintVal >= 0) {
                    return tinyintVal;
                }
                return tinyintVal + 256;
            }
            case 2: 
            case 13: {
                short asShort = this.getNativeShort(columnIndex + 1, false);
                if (!f.isUnsigned() || asShort >= 0) {
                    return asShort;
                }
                return asShort + 65536;
            }
            case 3: 
            case 9: {
                long valueAsLong;
                int valueAsInt = this.thisRow.getNativeInt(columnIndex);
                if (!f.isUnsigned()) {
                    return valueAsInt;
                }
                long l = valueAsLong = valueAsInt >= 0 ? (long)valueAsInt : (long)valueAsInt + 0x100000000L;
                if (overflowCheck && this.jdbcCompliantTruncationForReads && valueAsLong > Integer.MAX_VALUE) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 4);
                }
                return (int)valueAsLong;
            }
            case 8: {
                long valueAsLong = this.getNativeLong(columnIndex + 1, false, true);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < Integer.MIN_VALUE || valueAsLong > Integer.MAX_VALUE)) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 4);
                }
                return (int)valueAsLong;
            }
            case 5: {
                double valueAsDouble = this.getNativeDouble(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < -2.147483648E9 || valueAsDouble > 2.147483647E9)) {
                    this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, 4);
                }
                return (int)valueAsDouble;
            }
            case 4: {
                double valueAsDouble = this.getNativeFloat(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < -2.147483648E9 || valueAsDouble > 2.147483647E9)) {
                    this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, 4);
                }
                return (int)valueAsDouble;
            }
        }
        String stringVal = this.getNativeString(columnIndex + 1);
        if (this.useUsageAdvisor) {
            this.issueConversionViaParsingWarning("getInt()", columnIndex, stringVal, this.fields[columnIndex], new int[]{5, 1, 2, 3, 8, 4});
        }
        return this.getIntFromString(stringVal, columnIndex + 1);
    }

    protected long getNativeLong(int columnIndex) throws SQLException {
        return this.getNativeLong(columnIndex, true, true);
    }

    protected long getNativeLong(int columnIndex, boolean overflowCheck, boolean expandUnsignedLong) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        if (this.thisRow.isNull(--columnIndex)) {
            this.wasNullFlag = true;
            return 0L;
        }
        this.wasNullFlag = false;
        Field f = this.fields[columnIndex];
        switch (f.getMysqlType()) {
            case 16: {
                return this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
            }
            case 1: {
                if (!f.isUnsigned()) {
                    return this.getNativeByte(columnIndex + 1);
                }
                return this.getNativeInt(columnIndex + 1);
            }
            case 2: {
                if (!f.isUnsigned()) {
                    return this.getNativeShort(columnIndex + 1);
                }
                return this.getNativeInt(columnIndex + 1, false);
            }
            case 13: {
                return this.getNativeShort(columnIndex + 1);
            }
            case 3: 
            case 9: {
                int asInt = this.getNativeInt(columnIndex + 1, false);
                if (!f.isUnsigned() || asInt >= 0) {
                    return asInt;
                }
                return (long)asInt + 0x100000000L;
            }
            case 8: {
                long valueAsLong = this.thisRow.getNativeLong(columnIndex);
                if (!f.isUnsigned() || !expandUnsignedLong) {
                    return valueAsLong;
                }
                BigInteger asBigInt = ResultSetImpl.convertLongToUlong(valueAsLong);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (asBigInt.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) > 0 || asBigInt.compareTo(new BigInteger(String.valueOf(Long.MIN_VALUE))) < 0)) {
                    this.throwRangeException(asBigInt.toString(), columnIndex + 1, -5);
                }
                return this.getLongFromString(asBigInt.toString(), columnIndex);
            }
            case 5: {
                double valueAsDouble = this.getNativeDouble(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < -9.223372036854776E18 || valueAsDouble > 9.223372036854776E18)) {
                    this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, -5);
                }
                return (long)valueAsDouble;
            }
            case 4: {
                double valueAsDouble = this.getNativeFloat(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < -9.223372036854776E18 || valueAsDouble > 9.223372036854776E18)) {
                    this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, -5);
                }
                return (long)valueAsDouble;
            }
        }
        String stringVal = this.getNativeString(columnIndex + 1);
        if (this.useUsageAdvisor) {
            this.issueConversionViaParsingWarning("getLong()", columnIndex, stringVal, this.fields[columnIndex], new int[]{5, 1, 2, 3, 8, 4});
        }
        return this.getLongFromString(stringVal, columnIndex + 1);
    }

    protected Ref getNativeRef(int i) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    protected short getNativeShort(int columnIndex) throws SQLException {
        return this.getNativeShort(columnIndex, true);
    }

    protected short getNativeShort(int columnIndex, boolean overflowCheck) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        if (this.thisRow.isNull(--columnIndex)) {
            this.wasNullFlag = true;
            return 0;
        }
        this.wasNullFlag = false;
        Field f = this.fields[columnIndex];
        switch (f.getMysqlType()) {
            case 16: {
                long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < -32768L || valueAsLong > 32767L)) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 5);
                }
                return (short)valueAsLong;
            }
            case 1: {
                byte tinyintVal = this.getNativeByte(columnIndex + 1, false);
                if (!f.isUnsigned() || tinyintVal >= 0) {
                    return tinyintVal;
                }
                return (short)(tinyintVal + 256);
            }
            case 2: 
            case 13: {
                short asShort = this.thisRow.getNativeShort(columnIndex);
                if (!f.isUnsigned()) {
                    return asShort;
                }
                int valueAsInt = asShort & 0xFFFF;
                if (overflowCheck && this.jdbcCompliantTruncationForReads && valueAsInt > Short.MAX_VALUE) {
                    this.throwRangeException(String.valueOf(valueAsInt), columnIndex + 1, 5);
                }
                return (short)valueAsInt;
            }
            case 3: 
            case 9: {
                if (!f.isUnsigned()) {
                    int valueAsInt = this.getNativeInt(columnIndex + 1, false);
                    if (overflowCheck && this.jdbcCompliantTruncationForReads && valueAsInt > Short.MAX_VALUE || valueAsInt < Short.MIN_VALUE) {
                        this.throwRangeException(String.valueOf(valueAsInt), columnIndex + 1, 5);
                    }
                    return (short)valueAsInt;
                }
                long valueAsLong = this.getNativeLong(columnIndex + 1, false, true);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && valueAsLong > 32767L) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 5);
                }
                return (short)valueAsLong;
            }
            case 8: {
                long valueAsLong = this.getNativeLong(columnIndex + 1, false, false);
                if (!f.isUnsigned()) {
                    if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < -32768L || valueAsLong > 32767L)) {
                        this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 5);
                    }
                    return (short)valueAsLong;
                }
                BigInteger asBigInt = ResultSetImpl.convertLongToUlong(valueAsLong);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (asBigInt.compareTo(new BigInteger(String.valueOf(Short.MAX_VALUE))) > 0 || asBigInt.compareTo(new BigInteger(String.valueOf(Short.MIN_VALUE))) < 0)) {
                    this.throwRangeException(asBigInt.toString(), columnIndex + 1, 5);
                }
                return (short)this.getIntFromString(asBigInt.toString(), columnIndex + 1);
            }
            case 5: {
                double valueAsDouble = this.getNativeDouble(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < -32768.0 || valueAsDouble > 32767.0)) {
                    this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, 5);
                }
                return (short)valueAsDouble;
            }
            case 4: {
                float valueAsFloat = this.getNativeFloat(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsFloat < -32768.0f || valueAsFloat > 32767.0f)) {
                    this.throwRangeException(String.valueOf(valueAsFloat), columnIndex + 1, 5);
                }
                return (short)valueAsFloat;
            }
        }
        String stringVal = this.getNativeString(columnIndex + 1);
        if (this.useUsageAdvisor) {
            this.issueConversionViaParsingWarning("getShort()", columnIndex, stringVal, this.fields[columnIndex], new int[]{5, 1, 2, 3, 8, 4});
        }
        return this.getShortFromString(stringVal, columnIndex + 1);
    }

    protected String getNativeString(int columnIndex) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        if (this.fields == null) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Query_generated_no_fields_for_ResultSet_133"), "S1002", this.getExceptionInterceptor());
        }
        if (this.thisRow.isNull(columnIndex - 1)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        String stringVal = null;
        Field field = this.fields[columnIndex - 1];
        stringVal = this.getNativeConvertToString(columnIndex, field);
        int mysqlType = field.getMysqlType();
        if (mysqlType != 7 && mysqlType != 10 && field.isZeroFill() && stringVal != null) {
            int origLength = stringVal.length();
            StringBuilder zeroFillBuf = new StringBuilder(origLength);
            long numZeros = field.getLength() - (long)origLength;
            for (long i = 0L; i < numZeros; ++i) {
                zeroFillBuf.append('0');
            }
            zeroFillBuf.append(stringVal);
            stringVal = zeroFillBuf.toString();
        }
        return stringVal;
    }

    private Time getNativeTime(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        int mysqlType = this.fields[columnIndexMinusOne].getMysqlType();
        Time timeVal = null;
        timeVal = mysqlType == 11 ? this.thisRow.getNativeTime(columnIndexMinusOne, targetCalendar, tz, rollForward, this.connection, this) : (Time)this.thisRow.getNativeDateTimeValue(columnIndexMinusOne, null, 92, mysqlType, tz, rollForward, this.connection, this);
        if (timeVal == null) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        return timeVal;
    }

    Time getNativeTimeViaParseConversion(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        if (this.useUsageAdvisor) {
            this.issueConversionViaParsingWarning("getTime()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[columnIndex - 1], new int[]{11});
        }
        String strTime = this.getNativeString(columnIndex);
        return this.getTimeFromString(strTime, targetCalendar, columnIndex, tz, rollForward);
    }

    private Timestamp getNativeTimestamp(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        Timestamp tsVal = null;
        int mysqlType = this.fields[columnIndexMinusOne].getMysqlType();
        switch (mysqlType) {
            case 7: 
            case 12: {
                tsVal = this.thisRow.getNativeTimestamp(columnIndexMinusOne, targetCalendar, tz, rollForward, this.connection, this);
                break;
            }
            default: {
                tsVal = (Timestamp)this.thisRow.getNativeDateTimeValue(columnIndexMinusOne, null, 93, mysqlType, tz, rollForward, this.connection, this);
            }
        }
        if (tsVal == null) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        return tsVal;
    }

    Timestamp getNativeTimestampViaParseConversion(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        if (this.useUsageAdvisor) {
            this.issueConversionViaParsingWarning("getTimestamp()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[columnIndex - 1], new int[]{7, 12});
        }
        String strTimestamp = this.getNativeString(columnIndex);
        return this.getTimestampFromString(columnIndex, targetCalendar, strTimestamp, tz, rollForward);
    }

    protected InputStream getNativeUnicodeStream(int columnIndex) throws SQLException {
        this.checkRowPos();
        return this.getBinaryStream(columnIndex);
    }

    protected URL getNativeURL(int colIndex) throws SQLException {
        String val = this.getString(colIndex);
        if (val == null) {
            return null;
        }
        try {
            return new URL(val);
        }
        catch (MalformedURLException mfe) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Malformed_URL____141") + val + "'", "S1009", this.getExceptionInterceptor());
        }
    }

    @Override
    public synchronized ResultSetInternalMethods getNextResultSet() {
        return this.nextResultSet;
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        Field field = this.fields[columnIndexMinusOne];
        switch (field.getSQLType()) {
            case -7: {
                if (field.getMysqlType() == 16 && !field.isSingleBit()) {
                    return this.getObjectDeserializingIfNeeded(columnIndex);
                }
                return this.getBoolean(columnIndex);
            }
            case 16: {
                return this.getBoolean(columnIndex);
            }
            case -6: {
                if (!field.isUnsigned()) {
                    return (int)this.getByte(columnIndex);
                }
                return this.getInt(columnIndex);
            }
            case 5: {
                return this.getInt(columnIndex);
            }
            case 4: {
                if (!field.isUnsigned() || field.getMysqlType() == 9) {
                    return this.getInt(columnIndex);
                }
                return this.getLong(columnIndex);
            }
            case -5: {
                if (!field.isUnsigned()) {
                    return this.getLong(columnIndex);
                }
                String stringVal = this.getString(columnIndex);
                if (stringVal == null) {
                    return null;
                }
                try {
                    return new BigInteger(stringVal);
                }
                catch (NumberFormatException nfe) {
                    throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigInteger", new Object[]{columnIndex, stringVal}), "S1009", this.getExceptionInterceptor());
                }
            }
            case 2: 
            case 3: {
                String stringVal = this.getString(columnIndex);
                if (stringVal != null) {
                    BigDecimal val;
                    if (stringVal.length() == 0) {
                        BigDecimal val2 = new BigDecimal(0);
                        return val2;
                    }
                    try {
                        val = new BigDecimal(stringVal);
                    }
                    catch (NumberFormatException ex) {
                        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, columnIndex}), "S1009", this.getExceptionInterceptor());
                    }
                    return val;
                }
                return null;
            }
            case 7: {
                return new Float(this.getFloat(columnIndex));
            }
            case 6: 
            case 8: {
                return new Double(this.getDouble(columnIndex));
            }
            case 1: 
            case 12: {
                if (!field.isOpaqueBinary()) {
                    return this.getString(columnIndex);
                }
                return this.getBytes(columnIndex);
            }
            case -1: {
                if (!field.isOpaqueBinary()) {
                    return this.getStringForClob(columnIndex);
                }
                return this.getBytes(columnIndex);
            }
            case -4: 
            case -3: 
            case -2: {
                if (field.getMysqlType() == 255) {
                    return this.getBytes(columnIndex);
                }
                return this.getObjectDeserializingIfNeeded(columnIndex);
            }
            case 91: {
                if (field.getMysqlType() == 13 && !this.connection.getYearIsDateType()) {
                    return this.getShort(columnIndex);
                }
                return this.getDate(columnIndex);
            }
            case 92: {
                return this.getTime(columnIndex);
            }
            case 93: {
                return this.getTimestamp(columnIndex);
            }
        }
        return this.getString(columnIndex);
    }

    private Object getObjectDeserializingIfNeeded(int columnIndex) throws SQLException {
        Field field = this.fields[columnIndex - 1];
        if (field.isBinary() || field.isBlob()) {
            byte[] data = this.getBytes(columnIndex);
            if (this.connection.getAutoDeserialize()) {
                Object obj = data;
                if (data != null && data.length >= 2) {
                    if (data[0] == -84 && data[1] == -19) {
                        try {
                            ByteArrayInputStream bytesIn = new ByteArrayInputStream(data);
                            ObjectInputStream objIn = new ObjectInputStream(bytesIn);
                            obj = objIn.readObject();
                            objIn.close();
                            bytesIn.close();
                        }
                        catch (ClassNotFoundException cnfe) {
                            throw SQLError.createSQLException(Messages.getString("ResultSet.Class_not_found___91") + cnfe.toString() + Messages.getString("ResultSet._while_reading_serialized_object_92"), this.getExceptionInterceptor());
                        }
                        catch (IOException ex) {
                            obj = data;
                        }
                    } else {
                        return this.getString(columnIndex);
                    }
                }
                return obj;
            }
            return data;
        }
        return this.getBytes(columnIndex);
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        if (type == null) {
            throw SQLError.createSQLException("Type parameter can not be null", "S1009", this.getExceptionInterceptor());
        }
        if (type.equals(String.class)) {
            return (T)this.getString(columnIndex);
        }
        if (type.equals(BigDecimal.class)) {
            return (T)this.getBigDecimal(columnIndex);
        }
        if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
            return (T)Boolean.valueOf(this.getBoolean(columnIndex));
        }
        if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return (T)Integer.valueOf(this.getInt(columnIndex));
        }
        if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            return (T)Long.valueOf(this.getLong(columnIndex));
        }
        if (type.equals(Float.class) || type.equals(Float.TYPE)) {
            return (T)Float.valueOf(this.getFloat(columnIndex));
        }
        if (type.equals(Double.class) || type.equals(Double.TYPE)) {
            return (T)Double.valueOf(this.getDouble(columnIndex));
        }
        if (type.equals(byte[].class)) {
            return (T)this.getBytes(columnIndex);
        }
        if (type.equals(Date.class)) {
            return (T)this.getDate(columnIndex);
        }
        if (type.equals(Time.class)) {
            return (T)this.getTime(columnIndex);
        }
        if (type.equals(Timestamp.class)) {
            return (T)this.getTimestamp(columnIndex);
        }
        if (type.equals(Clob.class)) {
            return (T)this.getClob(columnIndex);
        }
        if (type.equals(Blob.class)) {
            return (T)this.getBlob(columnIndex);
        }
        if (type.equals(Array.class)) {
            return (T)this.getArray(columnIndex);
        }
        if (type.equals(Ref.class)) {
            return (T)this.getRef(columnIndex);
        }
        if (type.equals(URL.class)) {
            return (T)this.getURL(columnIndex);
        }
        if (this.connection.getAutoDeserialize()) {
            try {
                return type.cast(this.getObject(columnIndex));
            }
            catch (ClassCastException cce) {
                SQLException sqlEx = SQLError.createSQLException("Conversion not supported for type " + type.getName(), "S1009", this.getExceptionInterceptor());
                sqlEx.initCause(cce);
                throw sqlEx;
            }
        }
        throw SQLError.createSQLException("Conversion not supported for type " + type.getName(), "S1009", this.getExceptionInterceptor());
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return this.getObject(this.findColumn(columnLabel), type);
    }

    @Override
    public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
        return this.getObject(i);
    }

    @Override
    public Object getObject(String columnName) throws SQLException {
        return this.getObject(this.findColumn(columnName));
    }

    @Override
    public Object getObject(String colName, Map<String, Class<?>> map) throws SQLException {
        return this.getObject(this.findColumn(colName), map);
    }

    @Override
    public Object getObjectStoredProc(int columnIndex, int desiredSqlType) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        byte[] value = this.thisRow.getColumnValue(columnIndex - 1);
        if (value == null) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        Field field = this.fields[columnIndex - 1];
        switch (desiredSqlType) {
            case -7: 
            case 16: {
                return this.getBoolean(columnIndex);
            }
            case -6: {
                return this.getInt(columnIndex);
            }
            case 5: {
                return this.getInt(columnIndex);
            }
            case 4: {
                if (!field.isUnsigned() || field.getMysqlType() == 9) {
                    return this.getInt(columnIndex);
                }
                return this.getLong(columnIndex);
            }
            case -5: {
                if (field.isUnsigned()) {
                    return this.getBigDecimal(columnIndex);
                }
                return this.getLong(columnIndex);
            }
            case 2: 
            case 3: {
                String stringVal = this.getString(columnIndex);
                if (stringVal != null) {
                    BigDecimal val;
                    if (stringVal.length() == 0) {
                        BigDecimal val2 = new BigDecimal(0);
                        return val2;
                    }
                    try {
                        val = new BigDecimal(stringVal);
                    }
                    catch (NumberFormatException ex) {
                        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, columnIndex}), "S1009", this.getExceptionInterceptor());
                    }
                    return val;
                }
                return null;
            }
            case 7: {
                return new Float(this.getFloat(columnIndex));
            }
            case 6: {
                if (!this.connection.getRunningCTS13()) {
                    return new Double(this.getFloat(columnIndex));
                }
                return new Float(this.getFloat(columnIndex));
            }
            case 8: {
                return new Double(this.getDouble(columnIndex));
            }
            case 1: 
            case 12: {
                return this.getString(columnIndex);
            }
            case -1: {
                return this.getStringForClob(columnIndex);
            }
            case -4: 
            case -3: 
            case -2: {
                return this.getBytes(columnIndex);
            }
            case 91: {
                if (field.getMysqlType() == 13 && !this.connection.getYearIsDateType()) {
                    return this.getShort(columnIndex);
                }
                return this.getDate(columnIndex);
            }
            case 92: {
                return this.getTime(columnIndex);
            }
            case 93: {
                return this.getTimestamp(columnIndex);
            }
        }
        return this.getString(columnIndex);
    }

    @Override
    public Object getObjectStoredProc(int i, Map<Object, Object> map, int desiredSqlType) throws SQLException {
        return this.getObjectStoredProc(i, desiredSqlType);
    }

    @Override
    public Object getObjectStoredProc(String columnName, int desiredSqlType) throws SQLException {
        return this.getObjectStoredProc(this.findColumn(columnName), desiredSqlType);
    }

    @Override
    public Object getObjectStoredProc(String colName, Map<Object, Object> map, int desiredSqlType) throws SQLException {
        return this.getObjectStoredProc(this.findColumn(colName), map, desiredSqlType);
    }

    @Override
    public Ref getRef(int i) throws SQLException {
        this.checkColumnBounds(i);
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    public Ref getRef(String colName) throws SQLException {
        return this.getRef(this.findColumn(colName));
    }

    @Override
    public int getRow() throws SQLException {
        this.checkClosed();
        int currentRowNumber = this.rowData.getCurrentRowNumber();
        int row = 0;
        row = !this.rowData.isDynamic() ? (currentRowNumber < 0 || this.rowData.isAfterLast() || this.rowData.isEmpty() ? 0 : currentRowNumber + 1) : currentRowNumber + 1;
        return row;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getServerInfo() {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                return this.serverInfo;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private long getNumericRepresentationOfSQLBitType(int columnIndex) throws SQLException {
        byte[] value = this.thisRow.getColumnValue(columnIndex - 1);
        if (this.fields[columnIndex - 1].isSingleBit() || value.length == 1) {
            return value[0];
        }
        byte[] asBytes = value;
        int shift = 0;
        long[] steps = new long[asBytes.length];
        for (int i = asBytes.length - 1; i >= 0; --i) {
            steps[i] = (long)(asBytes[i] & 0xFF) << shift;
            shift += 8;
        }
        long valueAsLong = 0L;
        for (int i = 0; i < asBytes.length; ++i) {
            valueAsLong |= steps[i];
        }
        return valueAsLong;
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        if (!this.isBinaryEncoded) {
            if (this.fields[columnIndex - 1].getMysqlType() == 16) {
                long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex);
                if (this.jdbcCompliantTruncationForReads && (valueAsLong < -32768L || valueAsLong > 32767L)) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex, 5);
                }
                return (short)valueAsLong;
            }
            if (this.useFastIntParsing) {
                byte[] value = this.thisRow.getColumnValue(columnIndex - 1);
                this.wasNullFlag = value == null;
                if (this.wasNullFlag) {
                    return 0;
                }
                byte[] shortAsBytes = value;
                if (shortAsBytes.length == 0) {
                    return (short)this.convertToZeroWithEmptyCheck();
                }
                boolean needsFullParse = false;
                for (int i = 0; i < shortAsBytes.length; ++i) {
                    if ((char)shortAsBytes[i] != 'e' && (char)shortAsBytes[i] != 'E') continue;
                    needsFullParse = true;
                    break;
                }
                if (!needsFullParse) {
                    try {
                        return this.parseShortWithOverflowCheck(columnIndex, shortAsBytes, null);
                    }
                    catch (NumberFormatException nfe) {
                        try {
                            return this.parseShortAsDouble(columnIndex, StringUtils.toString(shortAsBytes));
                        }
                        catch (NumberFormatException newNfe) {
                            throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getShort()_-____96") + StringUtils.toString(shortAsBytes) + "'", "S1009", this.getExceptionInterceptor());
                        }
                    }
                }
            }
            String val = null;
            try {
                val = this.getString(columnIndex);
                if (val != null) {
                    if (val.length() == 0) {
                        return (short)this.convertToZeroWithEmptyCheck();
                    }
                    if (val.indexOf("e") == -1 && val.indexOf("E") == -1 && val.indexOf(".") == -1) {
                        return this.parseShortWithOverflowCheck(columnIndex, null, val);
                    }
                    return this.parseShortAsDouble(columnIndex, val);
                }
                return 0;
            }
            catch (NumberFormatException nfe) {
                try {
                    return this.parseShortAsDouble(columnIndex, val);
                }
                catch (NumberFormatException newNfe) {
                    throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getShort()_-____96") + val + "'", "S1009", this.getExceptionInterceptor());
                }
            }
        }
        return this.getNativeShort(columnIndex);
    }

    @Override
    public short getShort(String columnName) throws SQLException {
        return this.getShort(this.findColumn(columnName));
    }

    private final short getShortFromString(String val, int columnIndex) throws SQLException {
        try {
            if (val != null) {
                if (val.length() == 0) {
                    return (short)this.convertToZeroWithEmptyCheck();
                }
                if (val.indexOf("e") == -1 && val.indexOf("E") == -1 && val.indexOf(".") == -1) {
                    return this.parseShortWithOverflowCheck(columnIndex, null, val);
                }
                return this.parseShortAsDouble(columnIndex, val);
            }
            return 0;
        }
        catch (NumberFormatException nfe) {
            try {
                return this.parseShortAsDouble(columnIndex, val);
            }
            catch (NumberFormatException newNfe) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getShort()_-____217") + val + Messages.getString("ResultSet.___in_column__218") + columnIndex, "S1009", this.getExceptionInterceptor());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Statement getStatement() throws SQLException {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                if (this.wrapperStatement != null) {
                    return this.wrapperStatement;
                }
                return this.owningStatement;
            }
        }
        catch (SQLException sqlEx) {
            if (!this.retainOwningStatement) {
                throw SQLError.createSQLException("Operation not allowed on closed ResultSet. Statements can be retained over result set closure by setting the connection property \"retainStatementAfterResultSetClose\" to \"true\".", "S1000", this.getExceptionInterceptor());
            }
            if (this.wrapperStatement != null) {
                return this.wrapperStatement;
            }
            return this.owningStatement;
        }
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        Field f;
        String stringVal = this.getStringInternal(columnIndex, true);
        if (this.padCharsWithSpace && stringVal != null && (f = this.fields[columnIndex - 1]).getMysqlType() == 254) {
            int fieldLength = (int)f.getLength() / f.getMaxBytesPerCharacter();
            int currentLength = stringVal.length();
            if (currentLength < fieldLength) {
                StringBuilder paddedBuf = new StringBuilder(fieldLength);
                paddedBuf.append(stringVal);
                int difference = fieldLength - currentLength;
                paddedBuf.append(EMPTY_SPACE, 0, difference);
                stringVal = paddedBuf.toString();
            }
        }
        return stringVal;
    }

    @Override
    public String getString(String columnName) throws SQLException {
        return this.getString(this.findColumn(columnName));
    }

    private String getStringForClob(int columnIndex) throws SQLException {
        String asString = null;
        String forcedEncoding = this.connection.getClobCharacterEncoding();
        if (forcedEncoding == null) {
            asString = !this.isBinaryEncoded ? this.getString(columnIndex) : this.getNativeString(columnIndex);
        } else {
            try {
                byte[] asBytes = null;
                asBytes = !this.isBinaryEncoded ? this.getBytes(columnIndex) : this.getNativeBytes(columnIndex, true);
                if (asBytes != null) {
                    asString = StringUtils.toString(asBytes, forcedEncoding);
                }
            }
            catch (UnsupportedEncodingException uee) {
                throw SQLError.createSQLException("Unsupported character encoding " + forcedEncoding, "S1009", this.getExceptionInterceptor());
            }
        }
        return asString;
    }

    protected String getStringInternal(int columnIndex, boolean checkDateTypes) throws SQLException {
        if (!this.isBinaryEncoded) {
            this.checkRowPos();
            this.checkColumnBounds(columnIndex);
            if (this.fields == null) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Query_generated_no_fields_for_ResultSet_99"), "S1002", this.getExceptionInterceptor());
            }
            int internalColumnIndex = columnIndex - 1;
            if (this.thisRow.isNull(internalColumnIndex)) {
                this.wasNullFlag = true;
                return null;
            }
            this.wasNullFlag = false;
            Field metadata = this.fields[internalColumnIndex];
            String stringVal = null;
            if (metadata.getMysqlType() == 16) {
                if (metadata.isSingleBit()) {
                    byte[] value = this.thisRow.getColumnValue(internalColumnIndex);
                    if (value.length == 0) {
                        return String.valueOf(this.convertToZeroWithEmptyCheck());
                    }
                    return String.valueOf(value[0]);
                }
                return String.valueOf(this.getNumericRepresentationOfSQLBitType(columnIndex));
            }
            String encoding = metadata.getEncoding();
            stringVal = this.thisRow.getString(internalColumnIndex, encoding, this.connection);
            if (metadata.getMysqlType() == 13) {
                if (!this.connection.getYearIsDateType()) {
                    return stringVal;
                }
                Date dt = this.getDateFromString(stringVal, columnIndex, null);
                if (dt == null) {
                    this.wasNullFlag = true;
                    return null;
                }
                this.wasNullFlag = false;
                return dt.toString();
            }
            if (checkDateTypes && !this.connection.getNoDatetimeStringSync()) {
                switch (metadata.getSQLType()) {
                    case 92: {
                        Time tm = this.getTimeFromString(stringVal, null, columnIndex, this.getDefaultTimeZone(), false);
                        if (tm == null) {
                            this.wasNullFlag = true;
                            return null;
                        }
                        this.wasNullFlag = false;
                        return tm.toString();
                    }
                    case 91: {
                        Date dt = this.getDateFromString(stringVal, columnIndex, null);
                        if (dt == null) {
                            this.wasNullFlag = true;
                            return null;
                        }
                        this.wasNullFlag = false;
                        return dt.toString();
                    }
                    case 93: {
                        Timestamp ts = this.getTimestampFromString(columnIndex, null, stringVal, this.getDefaultTimeZone(), false);
                        if (ts == null) {
                            this.wasNullFlag = true;
                            return null;
                        }
                        this.wasNullFlag = false;
                        return ts.toString();
                    }
                }
            }
            return stringVal;
        }
        return this.getNativeString(columnIndex);
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return this.getTimeInternal(columnIndex, null, this.getDefaultTimeZone(), false);
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return this.getTimeInternal(columnIndex, cal, cal.getTimeZone(), true);
    }

    @Override
    public Time getTime(String columnName) throws SQLException {
        return this.getTime(this.findColumn(columnName));
    }

    @Override
    public Time getTime(String columnName, Calendar cal) throws SQLException {
        return this.getTime(this.findColumn(columnName), cal);
    }

    private Time getTimeFromString(String timeAsString, Calendar targetCalendar, int columnIndex, TimeZone tz, boolean rollForward) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            int hr = 0;
            int min = 0;
            int sec = 0;
            try {
                if (timeAsString == null) {
                    this.wasNullFlag = true;
                    return null;
                }
                int dec = (timeAsString = timeAsString.trim()).indexOf(".");
                if (dec > -1) {
                    timeAsString = timeAsString.substring(0, dec);
                }
                if (timeAsString.equals("0") || timeAsString.equals("0000-00-00") || timeAsString.equals("0000-00-00 00:00:00") || timeAsString.equals("00000000000000")) {
                    if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior())) {
                        this.wasNullFlag = true;
                        return null;
                    }
                    if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
                        throw SQLError.createSQLException("Value '" + timeAsString + "' can not be represented as java.sql.Time", "S1009", this.getExceptionInterceptor());
                    }
                    return this.fastTimeCreate(targetCalendar, 0, 0, 0);
                }
                this.wasNullFlag = false;
                Field timeColField = this.fields[columnIndex - 1];
                if (timeColField.getMysqlType() == 7) {
                    int length = timeAsString.length();
                    switch (length) {
                        case 19: {
                            hr = Integer.parseInt(timeAsString.substring(length - 8, length - 6));
                            min = Integer.parseInt(timeAsString.substring(length - 5, length - 3));
                            sec = Integer.parseInt(timeAsString.substring(length - 2, length));
                            break;
                        }
                        case 12: 
                        case 14: {
                            hr = Integer.parseInt(timeAsString.substring(length - 6, length - 4));
                            min = Integer.parseInt(timeAsString.substring(length - 4, length - 2));
                            sec = Integer.parseInt(timeAsString.substring(length - 2, length));
                            break;
                        }
                        case 10: {
                            hr = Integer.parseInt(timeAsString.substring(6, 8));
                            min = Integer.parseInt(timeAsString.substring(8, 10));
                            sec = 0;
                            break;
                        }
                        default: {
                            throw SQLError.createSQLException(Messages.getString("ResultSet.Timestamp_too_small_to_convert_to_Time_value_in_column__257") + columnIndex + "(" + this.fields[columnIndex - 1] + ").", "S1009", this.getExceptionInterceptor());
                        }
                    }
                    SQLWarning precisionLost = new SQLWarning(Messages.getString("ResultSet.Precision_lost_converting_TIMESTAMP_to_Time_with_getTime()_on_column__261") + columnIndex + "(" + this.fields[columnIndex - 1] + ").");
                    if (this.warningChain == null) {
                        this.warningChain = precisionLost;
                    } else {
                        this.warningChain.setNextWarning(precisionLost);
                    }
                } else if (timeColField.getMysqlType() == 12) {
                    hr = Integer.parseInt(timeAsString.substring(11, 13));
                    min = Integer.parseInt(timeAsString.substring(14, 16));
                    sec = Integer.parseInt(timeAsString.substring(17, 19));
                    SQLWarning precisionLost = new SQLWarning(Messages.getString("ResultSet.Precision_lost_converting_DATETIME_to_Time_with_getTime()_on_column__264") + columnIndex + "(" + this.fields[columnIndex - 1] + ").");
                    if (this.warningChain == null) {
                        this.warningChain = precisionLost;
                    } else {
                        this.warningChain.setNextWarning(precisionLost);
                    }
                } else {
                    if (timeColField.getMysqlType() == 10) {
                        return this.fastTimeCreate(targetCalendar, 0, 0, 0);
                    }
                    if (timeAsString.length() != 5 && timeAsString.length() != 8) {
                        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Time____267") + timeAsString + Messages.getString("ResultSet.___in_column__268") + columnIndex, "S1009", this.getExceptionInterceptor());
                    }
                    hr = Integer.parseInt(timeAsString.substring(0, 2));
                    min = Integer.parseInt(timeAsString.substring(3, 5));
                    sec = timeAsString.length() == 5 ? 0 : Integer.parseInt(timeAsString.substring(6));
                }
                Calendar sessionCalendar = this.getCalendarInstanceForSessionOrNew();
                return TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, this.fastTimeCreate(sessionCalendar, hr, min, sec), this.connection.getServerTimezoneTZ(), tz, rollForward);
            }
            catch (RuntimeException ex) {
                SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", this.getExceptionInterceptor());
                sqlEx.initCause(ex);
                throw sqlEx;
            }
        }
    }

    private Time getTimeInternal(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        this.checkRowPos();
        if (this.isBinaryEncoded) {
            return this.getNativeTime(columnIndex, targetCalendar, tz, rollForward);
        }
        if (!this.useFastDateParsing) {
            String timeAsString = this.getStringInternal(columnIndex, false);
            return this.getTimeFromString(timeAsString, targetCalendar, columnIndex, tz, rollForward);
        }
        this.checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        return this.thisRow.getTimeFast(columnIndexMinusOne, targetCalendar, tz, rollForward, this.connection, this);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return this.getTimestampInternal(columnIndex, null, this.getDefaultTimeZone(), false);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return this.getTimestampInternal(columnIndex, cal, cal.getTimeZone(), true);
    }

    @Override
    public Timestamp getTimestamp(String columnName) throws SQLException {
        return this.getTimestamp(this.findColumn(columnName));
    }

    @Override
    public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        return this.getTimestamp(this.findColumn(columnName), cal);
    }

    private Timestamp getTimestampFromString(int columnIndex, Calendar targetCalendar, String timestampValue, TimeZone tz, boolean rollForward) throws SQLException {
        try {
            Calendar sessionCalendar;
            this.wasNullFlag = false;
            if (timestampValue == null) {
                this.wasNullFlag = true;
                return null;
            }
            timestampValue = timestampValue.trim();
            int length = timestampValue.length();
            Calendar calendar = sessionCalendar = this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : this.getCalendarInstanceForSessionOrNew();
            if (length > 0 && timestampValue.charAt(0) == '0' && (timestampValue.equals("0000-00-00") || timestampValue.equals("0000-00-00 00:00:00") || timestampValue.equals("00000000000000") || timestampValue.equals("0"))) {
                if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior())) {
                    this.wasNullFlag = true;
                    return null;
                }
                if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
                    throw SQLError.createSQLException("Value '" + timestampValue + "' can not be represented as java.sql.Timestamp", "S1009", this.getExceptionInterceptor());
                }
                return this.fastTimestampCreate(null, 1, 1, 1, 0, 0, 0, 0);
            }
            if (this.fields[columnIndex - 1].getMysqlType() == 13) {
                if (!this.useLegacyDatetimeCode) {
                    return TimeUtil.fastTimestampCreate(tz, Integer.parseInt(timestampValue.substring(0, 4)), 1, 1, 0, 0, 0, 0);
                }
                return TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, this.fastTimestampCreate(sessionCalendar, Integer.parseInt(timestampValue.substring(0, 4)), 1, 1, 0, 0, 0, 0), this.connection.getServerTimezoneTZ(), tz, rollForward);
            }
            int year = 0;
            int month = 0;
            int day = 0;
            int hour = 0;
            int minutes = 0;
            int seconds = 0;
            int nanos = 0;
            int decimalIndex = timestampValue.indexOf(".");
            if (decimalIndex == length - 1) {
                --length;
            } else if (decimalIndex != -1) {
                if (decimalIndex + 2 <= length) {
                    nanos = Integer.parseInt(timestampValue.substring(decimalIndex + 1));
                    int numDigits = length - (decimalIndex + 1);
                    if (numDigits < 9) {
                        int factor = (int)Math.pow(10.0, 9 - numDigits);
                        nanos *= factor;
                    }
                    length = decimalIndex;
                } else {
                    throw new IllegalArgumentException();
                }
            }
            switch (length) {
                case 19: 
                case 20: 
                case 21: 
                case 22: 
                case 23: 
                case 24: 
                case 25: 
                case 26: {
                    year = Integer.parseInt(timestampValue.substring(0, 4));
                    month = Integer.parseInt(timestampValue.substring(5, 7));
                    day = Integer.parseInt(timestampValue.substring(8, 10));
                    hour = Integer.parseInt(timestampValue.substring(11, 13));
                    minutes = Integer.parseInt(timestampValue.substring(14, 16));
                    seconds = Integer.parseInt(timestampValue.substring(17, 19));
                    break;
                }
                case 14: {
                    year = Integer.parseInt(timestampValue.substring(0, 4));
                    month = Integer.parseInt(timestampValue.substring(4, 6));
                    day = Integer.parseInt(timestampValue.substring(6, 8));
                    hour = Integer.parseInt(timestampValue.substring(8, 10));
                    minutes = Integer.parseInt(timestampValue.substring(10, 12));
                    seconds = Integer.parseInt(timestampValue.substring(12, 14));
                    break;
                }
                case 12: {
                    year = Integer.parseInt(timestampValue.substring(0, 2));
                    if (year <= 69) {
                        year += 100;
                    }
                    year += 1900;
                    month = Integer.parseInt(timestampValue.substring(2, 4));
                    day = Integer.parseInt(timestampValue.substring(4, 6));
                    hour = Integer.parseInt(timestampValue.substring(6, 8));
                    minutes = Integer.parseInt(timestampValue.substring(8, 10));
                    seconds = Integer.parseInt(timestampValue.substring(10, 12));
                    break;
                }
                case 10: {
                    if (this.fields[columnIndex - 1].getMysqlType() == 10 || timestampValue.indexOf("-") != -1) {
                        year = Integer.parseInt(timestampValue.substring(0, 4));
                        month = Integer.parseInt(timestampValue.substring(5, 7));
                        day = Integer.parseInt(timestampValue.substring(8, 10));
                        hour = 0;
                        minutes = 0;
                        break;
                    }
                    year = Integer.parseInt(timestampValue.substring(0, 2));
                    if (year <= 69) {
                        year += 100;
                    }
                    month = Integer.parseInt(timestampValue.substring(2, 4));
                    day = Integer.parseInt(timestampValue.substring(4, 6));
                    hour = Integer.parseInt(timestampValue.substring(6, 8));
                    minutes = Integer.parseInt(timestampValue.substring(8, 10));
                    year += 1900;
                    break;
                }
                case 8: {
                    if (timestampValue.indexOf(":") != -1) {
                        hour = Integer.parseInt(timestampValue.substring(0, 2));
                        minutes = Integer.parseInt(timestampValue.substring(3, 5));
                        seconds = Integer.parseInt(timestampValue.substring(6, 8));
                        year = 1970;
                        month = 1;
                        day = 1;
                        break;
                    }
                    year = Integer.parseInt(timestampValue.substring(0, 4));
                    month = Integer.parseInt(timestampValue.substring(4, 6));
                    day = Integer.parseInt(timestampValue.substring(6, 8));
                    year -= 1900;
                    --month;
                    break;
                }
                case 6: {
                    year = Integer.parseInt(timestampValue.substring(0, 2));
                    if (year <= 69) {
                        year += 100;
                    }
                    year += 1900;
                    month = Integer.parseInt(timestampValue.substring(2, 4));
                    day = Integer.parseInt(timestampValue.substring(4, 6));
                    break;
                }
                case 4: {
                    year = Integer.parseInt(timestampValue.substring(0, 2));
                    if (year <= 69) {
                        year += 100;
                    }
                    year += 1900;
                    month = Integer.parseInt(timestampValue.substring(2, 4));
                    day = 1;
                    break;
                }
                case 2: {
                    year = Integer.parseInt(timestampValue.substring(0, 2));
                    if (year <= 69) {
                        year += 100;
                    }
                    year += 1900;
                    month = 1;
                    day = 1;
                    break;
                }
                default: {
                    throw new SQLException("Bad format for Timestamp '" + timestampValue + "' in column " + columnIndex + ".", "S1009");
                }
            }
            if (!this.useLegacyDatetimeCode) {
                return TimeUtil.fastTimestampCreate(tz, year, month, day, hour, minutes, seconds, nanos);
            }
            return TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, this.fastTimestampCreate(sessionCalendar, year, month, day, hour, minutes, seconds, nanos), this.connection.getServerTimezoneTZ(), tz, rollForward);
        }
        catch (RuntimeException e) {
            SQLException sqlEx = SQLError.createSQLException("Cannot convert value '" + timestampValue + "' from column " + columnIndex + " to TIMESTAMP.", "S1009", this.getExceptionInterceptor());
            sqlEx.initCause(e);
            throw sqlEx;
        }
    }

    private Timestamp getTimestampInternal(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        if (this.isBinaryEncoded) {
            return this.getNativeTimestamp(columnIndex, targetCalendar, tz, rollForward);
        }
        Timestamp tsVal = null;
        if (!this.useFastDateParsing) {
            String timestampValue = this.getStringInternal(columnIndex, false);
            tsVal = this.getTimestampFromString(columnIndex, targetCalendar, timestampValue, tz, rollForward);
        } else {
            this.checkClosed();
            this.checkRowPos();
            this.checkColumnBounds(columnIndex);
            tsVal = this.thisRow.getTimestampFast(columnIndex - 1, targetCalendar, tz, rollForward, this.connection, this);
        }
        this.wasNullFlag = tsVal == null;
        return tsVal;
    }

    @Override
    public int getType() throws SQLException {
        return this.resultSetType;
    }

    @Override
    @Deprecated
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        if (!this.isBinaryEncoded) {
            this.checkRowPos();
            return this.getBinaryStream(columnIndex);
        }
        return this.getNativeBinaryStream(columnIndex);
    }

    @Override
    @Deprecated
    public InputStream getUnicodeStream(String columnName) throws SQLException {
        return this.getUnicodeStream(this.findColumn(columnName));
    }

    @Override
    public long getUpdateCount() {
        return this.updateCount;
    }

    @Override
    public long getUpdateID() {
        return this.updateId;
    }

    @Override
    public URL getURL(int colIndex) throws SQLException {
        String val = this.getString(colIndex);
        if (val == null) {
            return null;
        }
        try {
            return new URL(val);
        }
        catch (MalformedURLException mfe) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Malformed_URL____104") + val + "'", "S1009", this.getExceptionInterceptor());
        }
    }

    @Override
    public URL getURL(String colName) throws SQLException {
        String val = this.getString(colName);
        if (val == null) {
            return null;
        }
        try {
            return new URL(val);
        }
        catch (MalformedURLException mfe) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Malformed_URL____107") + val + "'", "S1009", this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public SQLWarning getWarnings() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.warningChain;
        }
    }

    @Override
    public void insertRow() throws SQLException {
        throw new NotUpdatable();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isAfterLast() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            boolean b = this.rowData.isAfterLast();
            return b;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isBeforeFirst() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.rowData.isBeforeFirst();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isFirst() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.rowData.isFirst();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isLast() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.rowData.isLast();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void issueConversionViaParsingWarning(String methodName, int columnIndex, Object value, Field fieldInfo, int[] typesWithNoParseConversion) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            StringBuilder originalQueryBuf = new StringBuilder();
            if (this.owningStatement != null && this.owningStatement instanceof PreparedStatement) {
                originalQueryBuf.append(Messages.getString("ResultSet.CostlyConversionCreatedFromQuery"));
                originalQueryBuf.append(((PreparedStatement)this.owningStatement).originalSql);
                originalQueryBuf.append("\n\n");
            } else {
                originalQueryBuf.append(".");
            }
            StringBuilder convertibleTypesBuf = new StringBuilder();
            for (int i = 0; i < typesWithNoParseConversion.length; ++i) {
                convertibleTypesBuf.append(MysqlDefs.typeToName(typesWithNoParseConversion[i]));
                convertibleTypesBuf.append("\n");
            }
            String message = Messages.getString("ResultSet.CostlyConversion", new Object[]{methodName, columnIndex + 1, fieldInfo.getOriginalName(), fieldInfo.getOriginalTableName(), originalQueryBuf.toString(), value != null ? value.getClass().getName() : ResultSetMetaData.getClassNameForJavaType(fieldInfo.getSQLType(), fieldInfo.isUnsigned(), fieldInfo.getMysqlType(), fieldInfo.isBinary() || fieldInfo.isBlob(), fieldInfo.isOpaqueBinary(), this.connection.getYearIsDateType()), MysqlDefs.typeToName(fieldInfo.getMysqlType()), convertibleTypesBuf.toString()});
            this.eventSink.consumeEvent(new ProfilerEvent(0, "", this.owningStatement == null ? "N/A" : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, message));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean last() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            boolean b = true;
            if (this.rowData.size() == 0) {
                b = false;
            } else {
                if (this.onInsertRow) {
                    this.onInsertRow = false;
                }
                if (this.doingUpdates) {
                    this.doingUpdates = false;
                }
                if (this.thisRow != null) {
                    this.thisRow.closeOpenStreams();
                }
                this.rowData.beforeLast();
                this.thisRow = this.rowData.next();
            }
            this.setRowPositionValidity();
            return b;
        }
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        throw new NotUpdatable();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean next() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            boolean b;
            if (this.onInsertRow) {
                this.onInsertRow = false;
            }
            if (this.doingUpdates) {
                this.doingUpdates = false;
            }
            if (!this.reallyResult()) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.ResultSet_is_from_UPDATE._No_Data_115"), "S1000", this.getExceptionInterceptor());
            }
            if (this.thisRow != null) {
                this.thisRow.closeOpenStreams();
            }
            if (this.rowData.size() == 0) {
                b = false;
            } else {
                this.thisRow = this.rowData.next();
                if (this.thisRow == null) {
                    b = false;
                } else {
                    this.clearWarnings();
                    b = true;
                }
            }
            this.setRowPositionValidity();
            return b;
        }
    }

    private int parseIntAsDouble(int columnIndex, String val) throws NumberFormatException, SQLException {
        if (val == null) {
            return 0;
        }
        double valueAsDouble = Double.parseDouble(val);
        if (this.jdbcCompliantTruncationForReads && (valueAsDouble < -2.147483648E9 || valueAsDouble > 2.147483647E9)) {
            this.throwRangeException(String.valueOf(valueAsDouble), columnIndex, 4);
        }
        return (int)valueAsDouble;
    }

    private int getIntWithOverflowCheck(int columnIndex) throws SQLException {
        int intValue = this.thisRow.getInt(columnIndex);
        this.checkForIntegerTruncation(columnIndex, null, intValue);
        return intValue;
    }

    private void checkForIntegerTruncation(int columnIndex, byte[] valueAsBytes, int intValue) throws SQLException {
        if (this.jdbcCompliantTruncationForReads && (intValue == Integer.MIN_VALUE || intValue == Integer.MAX_VALUE)) {
            long valueAsLong;
            String valueAsString = null;
            if (valueAsBytes == null) {
                valueAsString = this.thisRow.getString(columnIndex, this.fields[columnIndex].getEncoding(), this.connection);
            }
            if ((valueAsLong = Long.parseLong(valueAsString == null ? StringUtils.toString(valueAsBytes) : valueAsString)) < Integer.MIN_VALUE || valueAsLong > Integer.MAX_VALUE) {
                this.throwRangeException(valueAsString == null ? StringUtils.toString(valueAsBytes) : valueAsString, columnIndex + 1, 4);
            }
        }
    }

    private long parseLongAsDouble(int columnIndexZeroBased, String val) throws NumberFormatException, SQLException {
        if (val == null) {
            return 0L;
        }
        double valueAsDouble = Double.parseDouble(val);
        if (this.jdbcCompliantTruncationForReads && (valueAsDouble < -9.223372036854776E18 || valueAsDouble > 9.223372036854776E18)) {
            this.throwRangeException(val, columnIndexZeroBased + 1, -5);
        }
        return (long)valueAsDouble;
    }

    private long getLongWithOverflowCheck(int columnIndexZeroBased, boolean doOverflowCheck) throws SQLException {
        long longValue = this.thisRow.getLong(columnIndexZeroBased);
        if (doOverflowCheck) {
            this.checkForLongTruncation(columnIndexZeroBased, null, longValue);
        }
        return longValue;
    }

    private long parseLongWithOverflowCheck(int columnIndexZeroBased, byte[] valueAsBytes, String valueAsString, boolean doCheck) throws NumberFormatException, SQLException {
        long longValue = 0L;
        if (valueAsBytes == null && valueAsString == null) {
            return 0L;
        }
        if (valueAsBytes != null) {
            longValue = StringUtils.getLong(valueAsBytes);
        } else {
            valueAsString = valueAsString.trim();
            longValue = Long.parseLong(valueAsString);
        }
        if (doCheck && this.jdbcCompliantTruncationForReads) {
            this.checkForLongTruncation(columnIndexZeroBased, valueAsBytes, longValue);
        }
        return longValue;
    }

    private void checkForLongTruncation(int columnIndexZeroBased, byte[] valueAsBytes, long longValue) throws SQLException {
        if (longValue == Long.MIN_VALUE || longValue == Long.MAX_VALUE) {
            double valueAsDouble;
            String valueAsString = null;
            if (valueAsBytes == null) {
                valueAsString = this.thisRow.getString(columnIndexZeroBased, this.fields[columnIndexZeroBased].getEncoding(), this.connection);
            }
            if ((valueAsDouble = Double.parseDouble(valueAsString == null ? StringUtils.toString(valueAsBytes) : valueAsString)) < -9.223372036854776E18 || valueAsDouble > 9.223372036854776E18) {
                this.throwRangeException(valueAsString == null ? StringUtils.toString(valueAsBytes) : valueAsString, columnIndexZeroBased + 1, -5);
            }
        }
    }

    private short parseShortAsDouble(int columnIndex, String val) throws NumberFormatException, SQLException {
        if (val == null) {
            return 0;
        }
        double valueAsDouble = Double.parseDouble(val);
        if (this.jdbcCompliantTruncationForReads && (valueAsDouble < -32768.0 || valueAsDouble > 32767.0)) {
            this.throwRangeException(String.valueOf(valueAsDouble), columnIndex, 5);
        }
        return (short)valueAsDouble;
    }

    private short parseShortWithOverflowCheck(int columnIndex, byte[] valueAsBytes, String valueAsString) throws NumberFormatException, SQLException {
        long valueAsLong;
        short shortValue = 0;
        if (valueAsBytes == null && valueAsString == null) {
            return 0;
        }
        if (valueAsBytes != null) {
            shortValue = StringUtils.getShort(valueAsBytes);
        } else {
            valueAsString = valueAsString.trim();
            shortValue = Short.parseShort(valueAsString);
        }
        if (!(!this.jdbcCompliantTruncationForReads || shortValue != Short.MIN_VALUE && shortValue != Short.MAX_VALUE || (valueAsLong = Long.parseLong(valueAsString == null ? StringUtils.toString(valueAsBytes) : valueAsString)) >= -32768L && valueAsLong <= 32767L)) {
            this.throwRangeException(valueAsString == null ? StringUtils.toString(valueAsBytes) : valueAsString, columnIndex, 5);
        }
        return shortValue;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean prev() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            int rowIndex = this.rowData.getCurrentRowNumber();
            if (this.thisRow != null) {
                this.thisRow.closeOpenStreams();
            }
            boolean b = true;
            if (rowIndex - 1 >= 0) {
                this.rowData.setCurrentRow(--rowIndex);
                this.thisRow = this.rowData.getAt(rowIndex);
                b = true;
            } else if (rowIndex - 1 == -1) {
                this.rowData.setCurrentRow(--rowIndex);
                this.thisRow = null;
                b = false;
            } else {
                b = false;
            }
            this.setRowPositionValidity();
            return b;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean previous() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.onInsertRow) {
                this.onInsertRow = false;
            }
            if (this.doingUpdates) {
                this.doingUpdates = false;
            }
            return this.prev();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void realClose(boolean calledExplicitly) throws SQLException {
        MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn == null) {
            return;
        }
        Object object = locallyScopedConn.getConnectionMutex();
        synchronized (object) {
            SQLException exceptionDuringClose2;
            block35: {
                SQLException sqlEx22;
                block34: {
                    if (this.isClosed) {
                        return;
                    }
                    try {
                        if (this.useUsageAdvisor) {
                            if (!calledExplicitly) {
                                this.eventSink.consumeEvent(new ProfilerEvent(0, "", this.owningStatement == null ? "N/A" : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, Messages.getString("ResultSet.ResultSet_implicitly_closed_by_driver")));
                            }
                            if (this.rowData instanceof RowDataStatic) {
                                if (this.rowData.size() > this.connection.getResultSetSizeThreshold()) {
                                    this.eventSink.consumeEvent(new ProfilerEvent(0, "", this.owningStatement == null ? Messages.getString("ResultSet.N/A_159") : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, Messages.getString("ResultSet.Too_Large_Result_Set", new Object[]{this.rowData.size(), this.connection.getResultSetSizeThreshold()})));
                                }
                                if (!this.isLast() && !this.isAfterLast() && this.rowData.size() != 0) {
                                    this.eventSink.consumeEvent(new ProfilerEvent(0, "", this.owningStatement == null ? Messages.getString("ResultSet.N/A_159") : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, Messages.getString("ResultSet.Possible_incomplete_traversal_of_result_set", new Object[]{this.getRow(), this.rowData.size()})));
                                }
                            }
                            if (this.columnUsed.length > 0 && !this.rowData.wasEmpty()) {
                                StringBuilder buf = new StringBuilder(Messages.getString("ResultSet.The_following_columns_were_never_referenced"));
                                boolean issueWarn = false;
                                for (int i = 0; i < this.columnUsed.length; ++i) {
                                    if (this.columnUsed[i]) continue;
                                    if (!issueWarn) {
                                        issueWarn = true;
                                    } else {
                                        buf.append(", ");
                                    }
                                    buf.append(this.fields[i].getFullName());
                                }
                                if (issueWarn) {
                                    this.eventSink.consumeEvent(new ProfilerEvent(0, "", this.owningStatement == null ? "N/A" : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), 0, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, buf.toString()));
                                }
                            }
                        }
                        Object var8_7 = null;
                        if (this.owningStatement != null && calledExplicitly) {
                            this.owningStatement.removeOpenResultSet(this);
                        }
                        exceptionDuringClose2 = null;
                        if (this.rowData == null) break block34;
                    }
                    catch (Throwable throwable) {
                        SQLException exceptionDuringClose2;
                        block36: {
                            SQLException sqlEx22;
                            Object var8_8 = null;
                            if (this.owningStatement != null && calledExplicitly) {
                                this.owningStatement.removeOpenResultSet(this);
                            }
                            exceptionDuringClose2 = null;
                            if (this.rowData != null) {
                                try {
                                    this.rowData.close();
                                }
                                catch (SQLException sqlEx22) {
                                    exceptionDuringClose2 = sqlEx22;
                                }
                            }
                            if (this.statementUsedForFetchingRows != null) {
                                try {
                                    this.statementUsedForFetchingRows.realClose(true, false);
                                }
                                catch (SQLException sqlEx22) {
                                    if (exceptionDuringClose2 != null) {
                                        exceptionDuringClose2.setNextException(sqlEx22);
                                        break block36;
                                    }
                                    exceptionDuringClose2 = sqlEx22;
                                }
                            }
                        }
                        this.rowData = null;
                        this.fields = null;
                        this.columnLabelToIndex = null;
                        this.fullColumnNameToIndex = null;
                        this.columnToIndexCache = null;
                        this.eventSink = null;
                        this.warningChain = null;
                        if (!this.retainOwningStatement) {
                            this.owningStatement = null;
                        }
                        this.catalog = null;
                        this.serverInfo = null;
                        this.thisRow = null;
                        this.fastDefaultCal = null;
                        this.fastClientCal = null;
                        this.connection = null;
                        this.isClosed = true;
                        if (exceptionDuringClose2 != null) {
                            throw exceptionDuringClose2;
                        }
                        throw throwable;
                    }
                    try {
                        this.rowData.close();
                    }
                    catch (SQLException sqlEx22) {
                        exceptionDuringClose2 = sqlEx22;
                    }
                }
                if (this.statementUsedForFetchingRows != null) {
                    try {
                        this.statementUsedForFetchingRows.realClose(true, false);
                    }
                    catch (SQLException sqlEx22) {
                        if (exceptionDuringClose2 != null) {
                            exceptionDuringClose2.setNextException(sqlEx22);
                            break block35;
                        }
                        exceptionDuringClose2 = sqlEx22;
                    }
                }
            }
            this.rowData = null;
            this.fields = null;
            this.columnLabelToIndex = null;
            this.fullColumnNameToIndex = null;
            this.columnToIndexCache = null;
            this.eventSink = null;
            this.warningChain = null;
            if (!this.retainOwningStatement) {
                this.owningStatement = null;
            }
            this.catalog = null;
            this.serverInfo = null;
            this.thisRow = null;
            this.fastDefaultCal = null;
            this.fastClientCal = null;
            this.connection = null;
            this.isClosed = true;
            if (exceptionDuringClose2 != null) {
                throw exceptionDuringClose2;
            }
        }
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.isClosed;
    }

    @Override
    public boolean reallyResult() {
        if (this.rowData != null) {
            return true;
        }
        return this.reallyResult;
    }

    @Override
    public void refreshRow() throws SQLException {
        throw new NotUpdatable();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean relative(int rows) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.rowData.size() == 0) {
                this.setRowPositionValidity();
                return false;
            }
            if (this.thisRow != null) {
                this.thisRow.closeOpenStreams();
            }
            this.rowData.moveRowRelative(rows);
            this.thisRow = this.rowData.getAt(this.rowData.getCurrentRowNumber());
            this.setRowPositionValidity();
            return !this.rowData.isAfterLast() && !this.rowData.isBeforeFirst();
        }
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    public boolean rowInserted() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    protected void setBinaryEncoded() {
        this.isBinaryEncoded = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setFetchDirection(int direction) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (direction != 1000 && direction != 1001 && direction != 1002) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Illegal_value_for_fetch_direction_64"), "S1009", this.getExceptionInterceptor());
            }
            this.fetchDirection = direction;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setFetchSize(int rows) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (rows < 0) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Value_must_be_between_0_and_getMaxRows()_66"), "S1009", this.getExceptionInterceptor());
            }
            this.fetchSize = rows;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setFirstCharOfQuery(char c) {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                this.firstCharOfQuery = c;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected synchronized void setNextResultSet(ResultSetInternalMethods nextResultSet) {
        this.nextResultSet = nextResultSet;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setOwningStatement(StatementImpl owningStatement) {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                this.owningStatement = owningStatement;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected synchronized void setResultSetConcurrency(int concurrencyFlag) {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                this.resultSetConcurrency = concurrencyFlag;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected synchronized void setResultSetType(int typeFlag) {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                this.resultSetType = typeFlag;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void setServerInfo(String info) {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                this.serverInfo = info;
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
    public synchronized void setStatementUsedForFetchingRows(PreparedStatement stmt) {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                this.statementUsedForFetchingRows = stmt;
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
    public synchronized void setWrapperStatement(Statement wrapperStatement) {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                this.wrapperStatement = wrapperStatement;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void throwRangeException(String valueAsString, int columnIndex, int jdbcType) throws SQLException {
        String datatype = null;
        switch (jdbcType) {
            case -6: {
                datatype = "TINYINT";
                break;
            }
            case 5: {
                datatype = "SMALLINT";
                break;
            }
            case 4: {
                datatype = "INTEGER";
                break;
            }
            case -5: {
                datatype = "BIGINT";
                break;
            }
            case 7: {
                datatype = "REAL";
                break;
            }
            case 6: {
                datatype = "FLOAT";
                break;
            }
            case 8: {
                datatype = "DOUBLE";
                break;
            }
            case 3: {
                datatype = "DECIMAL";
                break;
            }
            default: {
                datatype = " (JDBC type '" + jdbcType + "')";
            }
        }
        throw SQLError.createSQLException("'" + valueAsString + "' in column '" + columnIndex + "' is outside valid range for the datatype " + datatype + ".", "22003", this.getExceptionInterceptor());
    }

    public String toString() {
        if (this.reallyResult) {
            return super.toString();
        }
        return "Result set representing update count of " + this.updateCount;
    }

    @Override
    public void updateArray(int arg0, Array arg1) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    public void updateArray(String arg0, Array arg1) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnName), x, length);
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        this.updateBigDecimal(this.findColumn(columnName), x);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnName), x, length);
    }

    @Override
    public void updateBlob(int arg0, java.sql.Blob arg1) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateBlob(String arg0, java.sql.Blob arg1) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateBoolean(String columnName, boolean x) throws SQLException {
        this.updateBoolean(this.findColumn(columnName), x);
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateByte(String columnName, byte x) throws SQLException {
        this.updateByte(this.findColumn(columnName), x);
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateBytes(String columnName, byte[] x) throws SQLException {
        this.updateBytes(this.findColumn(columnName), x);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnName), reader, length);
    }

    @Override
    public void updateClob(int arg0, java.sql.Clob arg1) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    public void updateClob(String columnName, java.sql.Clob clob) throws SQLException {
        this.updateClob(this.findColumn(columnName), clob);
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateDate(String columnName, Date x) throws SQLException {
        this.updateDate(this.findColumn(columnName), x);
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateDouble(String columnName, double x) throws SQLException {
        this.updateDouble(this.findColumn(columnName), x);
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateFloat(String columnName, float x) throws SQLException {
        this.updateFloat(this.findColumn(columnName), x);
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateInt(String columnName, int x) throws SQLException {
        this.updateInt(this.findColumn(columnName), x);
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateLong(String columnName, long x) throws SQLException {
        this.updateLong(this.findColumn(columnName), x);
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateNull(String columnName) throws SQLException {
        this.updateNull(this.findColumn(columnName));
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateObject(String columnName, Object x) throws SQLException {
        this.updateObject(this.findColumn(columnName), x);
    }

    @Override
    public void updateObject(String columnName, Object x, int scale) throws SQLException {
        this.updateObject(this.findColumn(columnName), x);
    }

    @Override
    public void updateRef(int arg0, Ref arg1) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    public void updateRef(String arg0, Ref arg1) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    public void updateRow() throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateShort(String columnName, short x) throws SQLException {
        this.updateShort(this.findColumn(columnName), x);
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateString(String columnName, String x) throws SQLException {
        this.updateString(this.findColumn(columnName), x);
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateTime(String columnName, Time x) throws SQLException {
        this.updateTime(this.findColumn(columnName), x);
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
        this.updateTimestamp(this.findColumn(columnName), x);
    }

    @Override
    public boolean wasNull() throws SQLException {
        return this.wasNullFlag;
    }

    protected Calendar getGmtCalendar() {
        if (this.gmtCalendar == null) {
            this.gmtCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        }
        return this.gmtCalendar;
    }

    protected ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }

    static {
        if (Util.isJdbc4()) {
            try {
                String jdbc4ClassName = Util.isJdbc42() ? "com.mysql.jdbc.JDBC42ResultSet" : "com.mysql.jdbc.JDBC4ResultSet";
                JDBC_4_RS_4_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(Long.TYPE, Long.TYPE, MySQLConnection.class, StatementImpl.class);
                JDBC_4_RS_5_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(String.class, Field[].class, RowData.class, MySQLConnection.class, StatementImpl.class);
                jdbc4ClassName = Util.isJdbc42() ? "com.mysql.jdbc.JDBC42UpdatableResultSet" : "com.mysql.jdbc.JDBC4UpdatableResultSet";
                JDBC_4_UPD_RS_5_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(String.class, Field[].class, RowData.class, MySQLConnection.class, StatementImpl.class);
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
            JDBC_4_RS_4_ARG_CTOR = null;
            JDBC_4_RS_5_ARG_CTOR = null;
            JDBC_4_UPD_RS_5_ARG_CTOR = null;
        }
        MIN_DIFF_PREC = (double)Float.parseFloat(Float.toString(Float.MIN_VALUE)) - Double.parseDouble(Float.toString(Float.MIN_VALUE));
        MAX_DIFF_PREC = (double)Float.parseFloat(Float.toString(Float.MAX_VALUE)) - Double.parseDouble(Float.toString(Float.MAX_VALUE));
        resultCounter = 1;
        EMPTY_SPACE = new char[255];
        for (int i = 0; i < EMPTY_SPACE.length; ++i) {
            ResultSetImpl.EMPTY_SPACE[i] = 32;
        }
    }
}


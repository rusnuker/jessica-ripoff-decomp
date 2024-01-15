/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.AssertionFailedException;
import com.mysql.jdbc.ByteArrayRow;
import com.mysql.jdbc.Constants;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.NotUpdatable;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ProfilerEventHandlerFactory;
import com.mysql.jdbc.ResultSetImpl;
import com.mysql.jdbc.ResultSetRow;
import com.mysql.jdbc.RowData;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.SingleByteCharsetConverter;
import com.mysql.jdbc.StatementImpl;
import com.mysql.jdbc.StringUtils;
import com.mysql.jdbc.profiler.ProfilerEvent;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class UpdatableResultSet
extends ResultSetImpl {
    static final byte[] STREAM_DATA_MARKER = StringUtils.getBytes("** STREAM DATA **");
    protected SingleByteCharsetConverter charConverter;
    private String charEncoding;
    private byte[][] defaultColumnValue;
    private PreparedStatement deleter = null;
    private String deleteSQL = null;
    private boolean initializedCharConverter = false;
    protected PreparedStatement inserter = null;
    private String insertSQL = null;
    private boolean isUpdatable = false;
    private String notUpdatableReason = null;
    private List<Integer> primaryKeyIndicies = null;
    private String qualifiedAndQuotedTableName;
    private String quotedIdChar = null;
    private PreparedStatement refresher;
    private String refreshSQL = null;
    private ResultSetRow savedCurrentRow;
    protected PreparedStatement updater = null;
    private String updateSQL = null;
    private boolean populateInserterWithDefaultValues = false;
    private Map<String, Map<String, Map<String, Integer>>> databasesUsedToTablesUsed = null;

    protected UpdatableResultSet(String catalog, Field[] fields, RowData tuples, MySQLConnection conn, StatementImpl creatorStmt) throws SQLException {
        super(catalog, fields, tuples, conn, creatorStmt);
        this.checkUpdatability();
        this.populateInserterWithDefaultValues = this.connection.getPopulateInsertRowWithDefaultValues();
    }

    @Override
    public synchronized boolean absolute(int row) throws SQLException {
        return super.absolute(row);
    }

    @Override
    public synchronized void afterLast() throws SQLException {
        super.afterLast();
    }

    @Override
    public synchronized void beforeFirst() throws SQLException {
        super.beforeFirst();
    }

    @Override
    public synchronized void cancelRowUpdates() throws SQLException {
        this.checkClosed();
        if (this.doingUpdates) {
            this.doingUpdates = false;
            this.updater.clearParameters();
        }
    }

    @Override
    protected synchronized void checkRowPos() throws SQLException {
        this.checkClosed();
        if (!this.onInsertRow) {
            super.checkRowPos();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void checkUpdatability() throws SQLException {
        try {
            if (this.fields == null) {
                return;
            }
            String singleTableName = null;
            String catalogName = null;
            int primaryKeyCount = 0;
            if (this.catalog == null || this.catalog.length() == 0) {
                this.catalog = this.fields[0].getDatabaseName();
                if (this.catalog == null || this.catalog.length() == 0) {
                    throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.43"), "S1009", this.getExceptionInterceptor());
                }
            }
            if (this.fields.length > 0) {
                singleTableName = this.fields[0].getOriginalTableName();
                catalogName = this.fields[0].getDatabaseName();
                if (singleTableName == null) {
                    singleTableName = this.fields[0].getTableName();
                    catalogName = this.catalog;
                }
                if (singleTableName != null && singleTableName.length() == 0) {
                    this.isUpdatable = false;
                    this.notUpdatableReason = Messages.getString("NotUpdatableReason.3");
                    return;
                }
                if (this.fields[0].isPrimaryKey()) {
                    ++primaryKeyCount;
                }
                for (int i = 1; i < this.fields.length; ++i) {
                    String otherTableName = this.fields[i].getOriginalTableName();
                    String otherCatalogName = this.fields[i].getDatabaseName();
                    if (otherTableName == null) {
                        otherTableName = this.fields[i].getTableName();
                        otherCatalogName = this.catalog;
                    }
                    if (otherTableName != null && otherTableName.length() == 0) {
                        this.isUpdatable = false;
                        this.notUpdatableReason = Messages.getString("NotUpdatableReason.3");
                        return;
                    }
                    if (singleTableName == null || !otherTableName.equals(singleTableName)) {
                        this.isUpdatable = false;
                        this.notUpdatableReason = Messages.getString("NotUpdatableReason.0");
                        return;
                    }
                    if (catalogName == null || !otherCatalogName.equals(catalogName)) {
                        this.isUpdatable = false;
                        this.notUpdatableReason = Messages.getString("NotUpdatableReason.1");
                        return;
                    }
                    if (!this.fields[i].isPrimaryKey()) continue;
                    ++primaryKeyCount;
                }
                if (singleTableName == null || singleTableName.length() == 0) {
                    this.isUpdatable = false;
                    this.notUpdatableReason = Messages.getString("NotUpdatableReason.2");
                    return;
                }
            } else {
                this.isUpdatable = false;
                this.notUpdatableReason = Messages.getString("NotUpdatableReason.3");
                return;
            }
            if (this.connection.getStrictUpdates()) {
                HashMap<String, String> primaryKeyNames;
                block28: {
                    DatabaseMetaData dbmd = this.connection.getMetaData();
                    ResultSet rs = null;
                    primaryKeyNames = new HashMap<String, String>();
                    try {
                        rs = dbmd.getPrimaryKeys(catalogName, null, singleTableName);
                        while (rs.next()) {
                            String keyName = rs.getString(4);
                            keyName = keyName.toUpperCase();
                            primaryKeyNames.put(keyName, keyName);
                        }
                        Object var9_11 = null;
                        if (rs == null) break block28;
                    }
                    catch (Throwable throwable) {
                        Object var9_12 = null;
                        if (rs != null) {
                            try {
                                rs.close();
                            }
                            catch (Exception ex) {
                                AssertionFailedException.shouldNotHappen(ex);
                            }
                            rs = null;
                        }
                        throw throwable;
                    }
                    try {
                        rs.close();
                    }
                    catch (Exception ex) {
                        AssertionFailedException.shouldNotHappen(ex);
                    }
                    rs = null;
                    {
                    }
                }
                int existingPrimaryKeysCount = primaryKeyNames.size();
                if (existingPrimaryKeysCount == 0) {
                    this.isUpdatable = false;
                    this.notUpdatableReason = Messages.getString("NotUpdatableReason.5");
                    return;
                }
                for (int i = 0; i < this.fields.length; ++i) {
                    String originalName;
                    String columnNameUC;
                    if (!this.fields[i].isPrimaryKey() || primaryKeyNames.remove(columnNameUC = this.fields[i].getName().toUpperCase()) != null || (originalName = this.fields[i].getOriginalName()) == null || primaryKeyNames.remove(originalName.toUpperCase()) != null) continue;
                    this.isUpdatable = false;
                    this.notUpdatableReason = Messages.getString("NotUpdatableReason.6", new Object[]{originalName});
                    return;
                }
                this.isUpdatable = primaryKeyNames.isEmpty();
                if (!this.isUpdatable) {
                    this.notUpdatableReason = existingPrimaryKeysCount > 1 ? Messages.getString("NotUpdatableReason.7") : Messages.getString("NotUpdatableReason.4");
                    return;
                }
            }
            if (primaryKeyCount == 0) {
                this.isUpdatable = false;
                this.notUpdatableReason = Messages.getString("NotUpdatableReason.4");
                return;
            }
            this.isUpdatable = true;
            this.notUpdatableReason = null;
            return;
        }
        catch (SQLException sqlEx) {
            this.isUpdatable = false;
            this.notUpdatableReason = sqlEx.getMessage();
            return;
        }
    }

    @Override
    public synchronized void deleteRow() throws SQLException {
        this.checkClosed();
        if (!this.isUpdatable) {
            throw new NotUpdatable(this.notUpdatableReason);
        }
        if (this.onInsertRow) {
            throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.1"), this.getExceptionInterceptor());
        }
        if (this.rowData.size() == 0) {
            throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.2"), this.getExceptionInterceptor());
        }
        if (this.isBeforeFirst()) {
            throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.3"), this.getExceptionInterceptor());
        }
        if (this.isAfterLast()) {
            throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.4"), this.getExceptionInterceptor());
        }
        if (this.deleter == null) {
            if (this.deleteSQL == null) {
                this.generateStatements();
            }
            this.deleter = (PreparedStatement)this.connection.clientPrepareStatement(this.deleteSQL);
        }
        this.deleter.clearParameters();
        int numKeys = this.primaryKeyIndicies.size();
        if (numKeys == 1) {
            int index = this.primaryKeyIndicies.get(0);
            this.setParamValue(this.deleter, 1, this.thisRow, index, this.fields[index].getSQLType());
        } else {
            for (int i = 0; i < numKeys; ++i) {
                int index = this.primaryKeyIndicies.get(i);
                this.setParamValue(this.deleter, i + 1, this.thisRow, index, this.fields[index].getSQLType());
            }
        }
        this.deleter.executeUpdate();
        this.rowData.removeRow(this.rowData.getCurrentRowNumber());
        this.previous();
    }

    private synchronized void setParamValue(PreparedStatement ps, int psIdx, ResultSetRow row, int rsIdx, int sqlType) throws SQLException {
        byte[] val = row.getColumnValue(rsIdx);
        if (val == null) {
            ps.setNull(psIdx, 0);
            return;
        }
        switch (sqlType) {
            case 0: {
                ps.setNull(psIdx, 0);
                break;
            }
            case -6: 
            case 4: 
            case 5: {
                ps.setInt(psIdx, row.getInt(rsIdx));
                break;
            }
            case -5: {
                ps.setLong(psIdx, row.getLong(rsIdx));
                break;
            }
            case -1: 
            case 1: 
            case 2: 
            case 3: 
            case 12: {
                ps.setString(psIdx, row.getString(rsIdx, this.charEncoding, this.connection));
                break;
            }
            case 91: {
                ps.setDate(psIdx, row.getDateFast(rsIdx, this.connection, this, this.fastDefaultCal), this.fastDefaultCal);
                break;
            }
            case 93: {
                ps.setTimestamp(psIdx, row.getTimestampFast(rsIdx, this.fastDefaultCal, this.connection.getDefaultTimeZone(), false, this.connection, this));
                break;
            }
            case 92: {
                ps.setTime(psIdx, row.getTimeFast(rsIdx, this.fastDefaultCal, this.connection.getDefaultTimeZone(), false, this.connection, this));
                break;
            }
            case 6: 
            case 7: 
            case 8: 
            case 16: {
                ps.setBytesNoEscapeNoQuotes(psIdx, val);
                break;
            }
            default: {
                ps.setBytes(psIdx, val);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private synchronized void extractDefaultValues() throws SQLException {
        DatabaseMetaData dbmd = this.connection.getMetaData();
        this.defaultColumnValue = new byte[this.fields.length][];
        ResultSet columnsResultSet = null;
        for (Map.Entry<String, Map<String, Map<String, Integer>>> dbEntry : this.databasesUsedToTablesUsed.entrySet()) {
            for (Map.Entry<String, Map<String, Integer>> tableEntry : dbEntry.getValue().entrySet()) {
                Object var13_12;
                String tableName = tableEntry.getKey();
                Map<String, Integer> columnNamesToIndices = tableEntry.getValue();
                try {
                    columnsResultSet = dbmd.getColumns(this.catalog, null, tableName, "%");
                    while (columnsResultSet.next()) {
                        String columnName = columnsResultSet.getString("COLUMN_NAME");
                        byte[] defaultValue = columnsResultSet.getBytes("COLUMN_DEF");
                        if (!columnNamesToIndices.containsKey(columnName)) continue;
                        int localColumnIndex = columnNamesToIndices.get(columnName);
                        this.defaultColumnValue[localColumnIndex] = defaultValue;
                    }
                    var13_12 = null;
                    if (columnsResultSet == null) continue;
                }
                catch (Throwable throwable) {
                    var13_12 = null;
                    if (columnsResultSet != null) {
                        columnsResultSet.close();
                        columnsResultSet = null;
                    }
                    throw throwable;
                }
                columnsResultSet.close();
                columnsResultSet = null;
                {
                }
            }
        }
    }

    @Override
    public synchronized boolean first() throws SQLException {
        return super.first();
    }

    protected synchronized void generateStatements() throws SQLException {
        if (!this.isUpdatable) {
            this.doingUpdates = false;
            this.onInsertRow = false;
            throw new NotUpdatable(this.notUpdatableReason);
        }
        String quotedId = this.getQuotedIdChar();
        TreeMap<String, String> tableNamesSoFar = null;
        if (this.connection.lowerCaseTableNames()) {
            tableNamesSoFar = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            this.databasesUsedToTablesUsed = new TreeMap<String, Map<String, Map<String, Integer>>>(String.CASE_INSENSITIVE_ORDER);
        } else {
            tableNamesSoFar = new TreeMap<String, String>();
            this.databasesUsedToTablesUsed = new TreeMap<String, Map<String, Map<String, Integer>>>();
        }
        this.primaryKeyIndicies = new ArrayList<Integer>();
        StringBuilder fieldValues = new StringBuilder();
        StringBuilder keyValues = new StringBuilder();
        StringBuilder columnNames = new StringBuilder();
        StringBuilder insertPlaceHolders = new StringBuilder();
        StringBuilder allTablesBuf = new StringBuilder();
        HashMap<Integer, String> columnIndicesToTable = new HashMap<Integer, String>();
        boolean firstTime = true;
        boolean keysFirstTime = true;
        String equalsStr = this.connection.versionMeetsMinimum(3, 23, 0) ? "<=>" : "=";
        for (int i = 0; i < this.fields.length; ++i) {
            StringBuilder tableNameBuffer = new StringBuilder();
            Map<String, Integer> updColumnNameToIndex = null;
            if (this.fields[i].getOriginalTableName() != null) {
                String databaseName = this.fields[i].getDatabaseName();
                if (databaseName != null && databaseName.length() > 0) {
                    tableNameBuffer.append(quotedId);
                    tableNameBuffer.append(databaseName);
                    tableNameBuffer.append(quotedId);
                    tableNameBuffer.append('.');
                }
                String tableOnlyName = this.fields[i].getOriginalTableName();
                tableNameBuffer.append(quotedId);
                tableNameBuffer.append(tableOnlyName);
                tableNameBuffer.append(quotedId);
                String fqTableName = tableNameBuffer.toString();
                if (!tableNamesSoFar.containsKey(fqTableName)) {
                    if (!tableNamesSoFar.isEmpty()) {
                        allTablesBuf.append(',');
                    }
                    allTablesBuf.append(fqTableName);
                    tableNamesSoFar.put(fqTableName, fqTableName);
                }
                columnIndicesToTable.put(i, fqTableName);
                updColumnNameToIndex = this.getColumnsToIndexMapForTableAndDB(databaseName, tableOnlyName);
            } else {
                String tableOnlyName = this.fields[i].getTableName();
                if (tableOnlyName != null) {
                    tableNameBuffer.append(quotedId);
                    tableNameBuffer.append(tableOnlyName);
                    tableNameBuffer.append(quotedId);
                    String fqTableName = tableNameBuffer.toString();
                    if (!tableNamesSoFar.containsKey(fqTableName)) {
                        if (!tableNamesSoFar.isEmpty()) {
                            allTablesBuf.append(',');
                        }
                        allTablesBuf.append(fqTableName);
                        tableNamesSoFar.put(fqTableName, fqTableName);
                    }
                    columnIndicesToTable.put(i, fqTableName);
                    updColumnNameToIndex = this.getColumnsToIndexMapForTableAndDB(this.catalog, tableOnlyName);
                }
            }
            String originalColumnName = this.fields[i].getOriginalName();
            String columnName = null;
            columnName = this.connection.getIO().hasLongColumnInfo() && originalColumnName != null && originalColumnName.length() > 0 ? originalColumnName : this.fields[i].getName();
            if (updColumnNameToIndex != null && columnName != null) {
                updColumnNameToIndex.put(columnName, i);
            }
            String originalTableName = this.fields[i].getOriginalTableName();
            String tableName = null;
            tableName = this.connection.getIO().hasLongColumnInfo() && originalTableName != null && originalTableName.length() > 0 ? originalTableName : this.fields[i].getTableName();
            StringBuilder fqcnBuf = new StringBuilder();
            String databaseName = this.fields[i].getDatabaseName();
            if (databaseName != null && databaseName.length() > 0) {
                fqcnBuf.append(quotedId);
                fqcnBuf.append(databaseName);
                fqcnBuf.append(quotedId);
                fqcnBuf.append('.');
            }
            fqcnBuf.append(quotedId);
            fqcnBuf.append(tableName);
            fqcnBuf.append(quotedId);
            fqcnBuf.append('.');
            fqcnBuf.append(quotedId);
            fqcnBuf.append(columnName);
            fqcnBuf.append(quotedId);
            String qualifiedColumnName = fqcnBuf.toString();
            if (this.fields[i].isPrimaryKey()) {
                this.primaryKeyIndicies.add(i);
                if (!keysFirstTime) {
                    keyValues.append(" AND ");
                } else {
                    keysFirstTime = false;
                }
                keyValues.append(qualifiedColumnName);
                keyValues.append(equalsStr);
                keyValues.append("?");
            }
            if (firstTime) {
                firstTime = false;
                fieldValues.append("SET ");
            } else {
                fieldValues.append(",");
                columnNames.append(",");
                insertPlaceHolders.append(",");
            }
            insertPlaceHolders.append("?");
            columnNames.append(qualifiedColumnName);
            fieldValues.append(qualifiedColumnName);
            fieldValues.append("=?");
        }
        this.qualifiedAndQuotedTableName = allTablesBuf.toString();
        this.updateSQL = "UPDATE " + this.qualifiedAndQuotedTableName + " " + fieldValues.toString() + " WHERE " + keyValues.toString();
        this.insertSQL = "INSERT INTO " + this.qualifiedAndQuotedTableName + " (" + columnNames.toString() + ") VALUES (" + insertPlaceHolders.toString() + ")";
        this.refreshSQL = "SELECT " + columnNames.toString() + " FROM " + this.qualifiedAndQuotedTableName + " WHERE " + keyValues.toString();
        this.deleteSQL = "DELETE FROM " + this.qualifiedAndQuotedTableName + " WHERE " + keyValues.toString();
    }

    private Map<String, Integer> getColumnsToIndexMapForTableAndDB(String databaseName, String tableName) {
        Map<String, Integer> nameToIndex;
        Map<String, Map<String, Integer>> tablesUsedToColumnsMap = this.databasesUsedToTablesUsed.get(databaseName);
        if (tablesUsedToColumnsMap == null) {
            tablesUsedToColumnsMap = this.connection.lowerCaseTableNames() ? new TreeMap<String, Map<String, Integer>>(String.CASE_INSENSITIVE_ORDER) : new TreeMap<String, Map<String, Integer>>();
            this.databasesUsedToTablesUsed.put(databaseName, tablesUsedToColumnsMap);
        }
        if ((nameToIndex = tablesUsedToColumnsMap.get(tableName)) == null) {
            nameToIndex = new HashMap<String, Integer>();
            tablesUsedToColumnsMap.put(tableName, nameToIndex);
        }
        return nameToIndex;
    }

    private synchronized SingleByteCharsetConverter getCharConverter() throws SQLException {
        if (!this.initializedCharConverter) {
            this.initializedCharConverter = true;
            if (this.connection.getUseUnicode()) {
                this.charEncoding = this.connection.getEncoding();
                this.charConverter = this.connection.getCharsetConverter(this.charEncoding);
            }
        }
        return this.charConverter;
    }

    @Override
    public int getConcurrency() throws SQLException {
        return this.isUpdatable ? 1008 : 1007;
    }

    private synchronized String getQuotedIdChar() throws SQLException {
        if (this.quotedIdChar == null) {
            boolean useQuotedIdentifiers = this.connection.supportsQuotedIdentifiers();
            if (useQuotedIdentifiers) {
                DatabaseMetaData dbmd = this.connection.getMetaData();
                this.quotedIdChar = dbmd.getIdentifierQuoteString();
            } else {
                this.quotedIdChar = "";
            }
        }
        return this.quotedIdChar;
    }

    @Override
    public synchronized void insertRow() throws SQLException {
        this.checkClosed();
        if (!this.onInsertRow) {
            throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.7"), this.getExceptionInterceptor());
        }
        this.inserter.executeUpdate();
        long autoIncrementId = this.inserter.getLastInsertID();
        int numFields = this.fields.length;
        byte[][] newRow = new byte[numFields][];
        for (int i = 0; i < numFields; ++i) {
            newRow[i] = (byte[])(this.inserter.isNull(i) ? null : this.inserter.getBytesRepresentation(i));
            if (!this.fields[i].isAutoIncrement() || autoIncrementId <= 0L) continue;
            newRow[i] = StringUtils.getBytes(String.valueOf(autoIncrementId));
            this.inserter.setBytesNoEscapeNoQuotes(i + 1, newRow[i]);
        }
        ByteArrayRow resultSetRow = new ByteArrayRow(newRow, this.getExceptionInterceptor());
        this.refreshRow(this.inserter, resultSetRow);
        this.rowData.addRow(resultSetRow);
        this.resetInserter();
    }

    @Override
    public synchronized boolean isAfterLast() throws SQLException {
        return super.isAfterLast();
    }

    @Override
    public synchronized boolean isBeforeFirst() throws SQLException {
        return super.isBeforeFirst();
    }

    @Override
    public synchronized boolean isFirst() throws SQLException {
        return super.isFirst();
    }

    @Override
    public synchronized boolean isLast() throws SQLException {
        return super.isLast();
    }

    boolean isUpdatable() {
        return this.isUpdatable;
    }

    @Override
    public synchronized boolean last() throws SQLException {
        return super.last();
    }

    @Override
    public synchronized void moveToCurrentRow() throws SQLException {
        this.checkClosed();
        if (!this.isUpdatable) {
            throw new NotUpdatable(this.notUpdatableReason);
        }
        if (this.onInsertRow) {
            this.onInsertRow = false;
            this.thisRow = this.savedCurrentRow;
        }
    }

    @Override
    public synchronized void moveToInsertRow() throws SQLException {
        this.checkClosed();
        if (!this.isUpdatable) {
            throw new NotUpdatable(this.notUpdatableReason);
        }
        if (this.inserter == null) {
            if (this.insertSQL == null) {
                this.generateStatements();
            }
            this.inserter = (PreparedStatement)this.connection.clientPrepareStatement(this.insertSQL);
            if (this.populateInserterWithDefaultValues) {
                this.extractDefaultValues();
            }
            this.resetInserter();
        } else {
            this.resetInserter();
        }
        int numFields = this.fields.length;
        this.onInsertRow = true;
        this.doingUpdates = false;
        this.savedCurrentRow = this.thisRow;
        Object newRowData = new byte[numFields][];
        this.thisRow = new ByteArrayRow((byte[][])newRowData, this.getExceptionInterceptor());
        this.thisRow.setMetadata(this.fields);
        for (int i = 0; i < numFields; ++i) {
            if (!this.populateInserterWithDefaultValues) {
                this.inserter.setBytesNoEscapeNoQuotes(i + 1, StringUtils.getBytes("DEFAULT"));
                newRowData = null;
                continue;
            }
            if (this.defaultColumnValue[i] != null) {
                Field f = this.fields[i];
                switch (f.getMysqlType()) {
                    case 7: 
                    case 10: 
                    case 11: 
                    case 12: 
                    case 14: {
                        if (this.defaultColumnValue[i].length > 7 && this.defaultColumnValue[i][0] == 67 && this.defaultColumnValue[i][1] == 85 && this.defaultColumnValue[i][2] == 82 && this.defaultColumnValue[i][3] == 82 && this.defaultColumnValue[i][4] == 69 && this.defaultColumnValue[i][5] == 78 && this.defaultColumnValue[i][6] == 84 && this.defaultColumnValue[i][7] == 95) {
                            this.inserter.setBytesNoEscapeNoQuotes(i + 1, this.defaultColumnValue[i]);
                            break;
                        }
                        this.inserter.setBytes(i + 1, this.defaultColumnValue[i], false, false);
                        break;
                    }
                    default: {
                        this.inserter.setBytes(i + 1, this.defaultColumnValue[i], false, false);
                    }
                }
                byte[] defaultValueCopy = new byte[this.defaultColumnValue[i].length];
                System.arraycopy(this.defaultColumnValue[i], 0, defaultValueCopy, 0, defaultValueCopy.length);
                newRowData[i] = defaultValueCopy;
                continue;
            }
            this.inserter.setNull(i + 1, 0);
            newRowData[i] = null;
        }
    }

    @Override
    public synchronized boolean next() throws SQLException {
        return super.next();
    }

    @Override
    public synchronized boolean prev() throws SQLException {
        return super.prev();
    }

    @Override
    public synchronized boolean previous() throws SQLException {
        return super.previous();
    }

    @Override
    public synchronized void realClose(boolean calledExplicitly) throws SQLException {
        if (this.isClosed) {
            return;
        }
        SQLException sqlEx = null;
        if (this.useUsageAdvisor && this.deleter == null && this.inserter == null && this.refresher == null && this.updater == null) {
            this.eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
            String message = Messages.getString("UpdatableResultSet.34");
            this.eventSink.consumeEvent(new ProfilerEvent(0, "", this.owningStatement == null ? "N/A" : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, message));
        }
        try {
            if (this.deleter != null) {
                this.deleter.close();
            }
        }
        catch (SQLException ex) {
            sqlEx = ex;
        }
        try {
            if (this.inserter != null) {
                this.inserter.close();
            }
        }
        catch (SQLException ex) {
            sqlEx = ex;
        }
        try {
            if (this.refresher != null) {
                this.refresher.close();
            }
        }
        catch (SQLException ex) {
            sqlEx = ex;
        }
        try {
            if (this.updater != null) {
                this.updater.close();
            }
        }
        catch (SQLException ex) {
            sqlEx = ex;
        }
        super.realClose(calledExplicitly);
        if (sqlEx != null) {
            throw sqlEx;
        }
    }

    @Override
    public synchronized void refreshRow() throws SQLException {
        this.checkClosed();
        if (!this.isUpdatable) {
            throw new NotUpdatable();
        }
        if (this.onInsertRow) {
            throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.8"), this.getExceptionInterceptor());
        }
        if (this.rowData.size() == 0) {
            throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.9"), this.getExceptionInterceptor());
        }
        if (this.isBeforeFirst()) {
            throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.10"), this.getExceptionInterceptor());
        }
        if (this.isAfterLast()) {
            throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.11"), this.getExceptionInterceptor());
        }
        this.refreshRow(this.updater, this.thisRow);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private synchronized void refreshRow(PreparedStatement updateInsertStmt, ResultSetRow rowToRefresh) throws SQLException {
        if (this.refresher == null) {
            if (this.refreshSQL == null) {
                this.generateStatements();
            }
            this.refresher = (PreparedStatement)this.connection.clientPrepareStatement(this.refreshSQL);
        }
        this.refresher.clearParameters();
        int numKeys = this.primaryKeyIndicies.size();
        if (numKeys == 1) {
            byte[] dataFrom = null;
            int index = this.primaryKeyIndicies.get(0);
            if (!this.doingUpdates && !this.onInsertRow) {
                dataFrom = rowToRefresh.getColumnValue(index);
            } else {
                dataFrom = updateInsertStmt.getBytesRepresentation(index);
                dataFrom = updateInsertStmt.isNull(index) || dataFrom.length == 0 ? rowToRefresh.getColumnValue(index) : this.stripBinaryPrefix(dataFrom);
            }
            if (this.fields[index].getvalueNeedsQuoting()) {
                this.refresher.setBytesNoEscape(1, dataFrom);
            } else {
                this.refresher.setBytesNoEscapeNoQuotes(1, dataFrom);
            }
        } else {
            for (int i = 0; i < numKeys; ++i) {
                byte[] dataFrom = null;
                int index = this.primaryKeyIndicies.get(i);
                if (!this.doingUpdates && !this.onInsertRow) {
                    dataFrom = rowToRefresh.getColumnValue(index);
                } else {
                    dataFrom = updateInsertStmt.getBytesRepresentation(index);
                    dataFrom = updateInsertStmt.isNull(index) || dataFrom.length == 0 ? rowToRefresh.getColumnValue(index) : this.stripBinaryPrefix(dataFrom);
                }
                this.refresher.setBytesNoEscape(i + 1, dataFrom);
            }
        }
        ResultSet rs = null;
        try {
            rs = this.refresher.executeQuery();
            int numCols = rs.getMetaData().getColumnCount();
            if (!rs.next()) throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.12"), "S1000", this.getExceptionInterceptor());
            for (int i = 0; i < numCols; ++i) {
                byte[] val = rs.getBytes(i + 1);
                if (val == null || rs.wasNull()) {
                    rowToRefresh.setColumnValue(i, null);
                    continue;
                }
                rowToRefresh.setColumnValue(i, rs.getBytes(i + 1));
            }
            Object var9_11 = null;
            if (rs == null) return;
        }
        catch (Throwable throwable) {
            Object var9_12 = null;
            if (rs == null) throw throwable;
            try {
                rs.close();
                throw throwable;
            }
            catch (SQLException ex) {
                // empty catch block
            }
            throw throwable;
        }
        try {
            rs.close();
            return;
        }
        catch (SQLException ex) {}
    }

    @Override
    public synchronized boolean relative(int rows) throws SQLException {
        return super.relative(rows);
    }

    private void resetInserter() throws SQLException {
        this.inserter.clearParameters();
        for (int i = 0; i < this.fields.length; ++i) {
            this.inserter.setNull(i + 1, 0);
        }
    }

    @Override
    public synchronized boolean rowDeleted() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    public synchronized boolean rowInserted() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    public synchronized boolean rowUpdated() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    protected void setResultSetConcurrency(int concurrencyFlag) {
        super.setResultSetConcurrency(concurrencyFlag);
    }

    private byte[] stripBinaryPrefix(byte[] dataFrom) {
        return StringUtils.stripEnclosure(dataFrom, "_binary'", "'");
    }

    protected synchronized void syncUpdate() throws SQLException {
        if (this.updater == null) {
            if (this.updateSQL == null) {
                this.generateStatements();
            }
            this.updater = (PreparedStatement)this.connection.clientPrepareStatement(this.updateSQL);
        }
        int numFields = this.fields.length;
        this.updater.clearParameters();
        for (int i = 0; i < numFields; ++i) {
            if (this.thisRow.getColumnValue(i) != null) {
                if (this.fields[i].getvalueNeedsQuoting()) {
                    this.updater.setBytes(i + 1, this.thisRow.getColumnValue(i), this.fields[i].isBinary(), false);
                    continue;
                }
                this.updater.setBytesNoEscapeNoQuotes(i + 1, this.thisRow.getColumnValue(i));
                continue;
            }
            this.updater.setNull(i + 1, 0);
        }
        int numKeys = this.primaryKeyIndicies.size();
        if (numKeys == 1) {
            int index = this.primaryKeyIndicies.get(0);
            this.setParamValue(this.updater, numFields + 1, this.thisRow, index, this.fields[index].getSQLType());
        } else {
            for (int i = 0; i < numKeys; ++i) {
                int idx = this.primaryKeyIndicies.get(i);
                this.setParamValue(this.updater, numFields + i + 1, this.thisRow, idx, this.fields[idx].getSQLType());
            }
        }
    }

    @Override
    public synchronized void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setAsciiStream(columnIndex, x, length);
        } else {
            this.inserter.setAsciiStream(columnIndex, x, length);
            this.thisRow.setColumnValue(columnIndex - 1, STREAM_DATA_MARKER);
        }
    }

    @Override
    public synchronized void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnName), x, length);
    }

    @Override
    public synchronized void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setBigDecimal(columnIndex, x);
        } else {
            this.inserter.setBigDecimal(columnIndex, x);
            if (x == null) {
                this.thisRow.setColumnValue(columnIndex - 1, null);
            } else {
                this.thisRow.setColumnValue(columnIndex - 1, StringUtils.getBytes(x.toString()));
            }
        }
    }

    @Override
    public synchronized void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        this.updateBigDecimal(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setBinaryStream(columnIndex, x, length);
        } else {
            this.inserter.setBinaryStream(columnIndex, x, length);
            if (x == null) {
                this.thisRow.setColumnValue(columnIndex - 1, null);
            } else {
                this.thisRow.setColumnValue(columnIndex - 1, STREAM_DATA_MARKER);
            }
        }
    }

    @Override
    public synchronized void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnName), x, length);
    }

    @Override
    public synchronized void updateBlob(int columnIndex, Blob blob) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setBlob(columnIndex, blob);
        } else {
            this.inserter.setBlob(columnIndex, blob);
            if (blob == null) {
                this.thisRow.setColumnValue(columnIndex - 1, null);
            } else {
                this.thisRow.setColumnValue(columnIndex - 1, STREAM_DATA_MARKER);
            }
        }
    }

    @Override
    public synchronized void updateBlob(String columnName, Blob blob) throws SQLException {
        this.updateBlob(this.findColumn(columnName), blob);
    }

    @Override
    public synchronized void updateBoolean(int columnIndex, boolean x) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setBoolean(columnIndex, x);
        } else {
            this.inserter.setBoolean(columnIndex, x);
            this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
        }
    }

    @Override
    public synchronized void updateBoolean(String columnName, boolean x) throws SQLException {
        this.updateBoolean(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateByte(int columnIndex, byte x) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setByte(columnIndex, x);
        } else {
            this.inserter.setByte(columnIndex, x);
            this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
        }
    }

    @Override
    public synchronized void updateByte(String columnName, byte x) throws SQLException {
        this.updateByte(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateBytes(int columnIndex, byte[] x) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setBytes(columnIndex, x);
        } else {
            this.inserter.setBytes(columnIndex, x);
            this.thisRow.setColumnValue(columnIndex - 1, x);
        }
    }

    @Override
    public synchronized void updateBytes(String columnName, byte[] x) throws SQLException {
        this.updateBytes(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setCharacterStream(columnIndex, x, length);
        } else {
            this.inserter.setCharacterStream(columnIndex, x, length);
            if (x == null) {
                this.thisRow.setColumnValue(columnIndex - 1, null);
            } else {
                this.thisRow.setColumnValue(columnIndex - 1, STREAM_DATA_MARKER);
            }
        }
    }

    @Override
    public synchronized void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnName), reader, length);
    }

    @Override
    public void updateClob(int columnIndex, Clob clob) throws SQLException {
        if (clob == null) {
            this.updateNull(columnIndex);
        } else {
            this.updateCharacterStream(columnIndex, clob.getCharacterStream(), (int)clob.length());
        }
    }

    @Override
    public synchronized void updateDate(int columnIndex, Date x) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setDate(columnIndex, x);
        } else {
            this.inserter.setDate(columnIndex, x);
            this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
        }
    }

    @Override
    public synchronized void updateDate(String columnName, Date x) throws SQLException {
        this.updateDate(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateDouble(int columnIndex, double x) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setDouble(columnIndex, x);
        } else {
            this.inserter.setDouble(columnIndex, x);
            this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
        }
    }

    @Override
    public synchronized void updateDouble(String columnName, double x) throws SQLException {
        this.updateDouble(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateFloat(int columnIndex, float x) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setFloat(columnIndex, x);
        } else {
            this.inserter.setFloat(columnIndex, x);
            this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
        }
    }

    @Override
    public synchronized void updateFloat(String columnName, float x) throws SQLException {
        this.updateFloat(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateInt(int columnIndex, int x) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setInt(columnIndex, x);
        } else {
            this.inserter.setInt(columnIndex, x);
            this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
        }
    }

    @Override
    public synchronized void updateInt(String columnName, int x) throws SQLException {
        this.updateInt(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateLong(int columnIndex, long x) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setLong(columnIndex, x);
        } else {
            this.inserter.setLong(columnIndex, x);
            this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
        }
    }

    @Override
    public synchronized void updateLong(String columnName, long x) throws SQLException {
        this.updateLong(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateNull(int columnIndex) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setNull(columnIndex, 0);
        } else {
            this.inserter.setNull(columnIndex, 0);
            this.thisRow.setColumnValue(columnIndex - 1, null);
        }
    }

    @Override
    public synchronized void updateNull(String columnName) throws SQLException {
        this.updateNull(this.findColumn(columnName));
    }

    @Override
    public synchronized void updateObject(int columnIndex, Object x) throws SQLException {
        this.updateObjectInternal(columnIndex, x, null, 0);
    }

    @Override
    public synchronized void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        this.updateObjectInternal(columnIndex, x, null, scale);
    }

    protected synchronized void updateObjectInternal(int columnIndex, Object x, Integer targetType, int scaleOrLength) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            if (targetType == null) {
                this.updater.setObject(columnIndex, x);
            } else {
                this.updater.setObject(columnIndex, x, targetType);
            }
        } else {
            if (targetType == null) {
                this.inserter.setObject(columnIndex, x);
            } else {
                this.inserter.setObject(columnIndex, x, targetType);
            }
            this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
        }
    }

    @Override
    public synchronized void updateObject(String columnName, Object x) throws SQLException {
        this.updateObject(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateObject(String columnName, Object x, int scale) throws SQLException {
        this.updateObject(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateRow() throws SQLException {
        if (!this.isUpdatable) {
            throw new NotUpdatable(this.notUpdatableReason);
        }
        if (this.doingUpdates) {
            this.updater.executeUpdate();
            this.refreshRow();
            this.doingUpdates = false;
        } else if (this.onInsertRow) {
            throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.44"), this.getExceptionInterceptor());
        }
        this.syncUpdate();
    }

    @Override
    public synchronized void updateShort(int columnIndex, short x) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setShort(columnIndex, x);
        } else {
            this.inserter.setShort(columnIndex, x);
            this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
        }
    }

    @Override
    public synchronized void updateShort(String columnName, short x) throws SQLException {
        this.updateShort(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateString(int columnIndex, String x) throws SQLException {
        this.checkClosed();
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setString(columnIndex, x);
        } else {
            this.inserter.setString(columnIndex, x);
            if (x == null) {
                this.thisRow.setColumnValue(columnIndex - 1, null);
            } else if (this.getCharConverter() != null) {
                this.thisRow.setColumnValue(columnIndex - 1, StringUtils.getBytes(x, this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor()));
            } else {
                this.thisRow.setColumnValue(columnIndex - 1, StringUtils.getBytes(x));
            }
        }
    }

    @Override
    public synchronized void updateString(String columnName, String x) throws SQLException {
        this.updateString(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateTime(int columnIndex, Time x) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setTime(columnIndex, x);
        } else {
            this.inserter.setTime(columnIndex, x);
            this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
        }
    }

    @Override
    public synchronized void updateTime(String columnName, Time x) throws SQLException {
        this.updateTime(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            this.updater.setTimestamp(columnIndex, x);
        } else {
            this.inserter.setTimestamp(columnIndex, x);
            this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
        }
    }

    @Override
    public synchronized void updateTimestamp(String columnName, Timestamp x) throws SQLException {
        this.updateTimestamp(this.findColumn(columnName), x);
    }
}


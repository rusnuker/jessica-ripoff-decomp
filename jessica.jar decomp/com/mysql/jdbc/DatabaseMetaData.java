/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.AssertionFailedException;
import com.mysql.jdbc.ByteArrayRow;
import com.mysql.jdbc.DatabaseMetaDataUsingInfoSchema;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.IterateBlock;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.MysqlDefs;
import com.mysql.jdbc.MysqlIO;
import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.jdbc.ResultSetImpl;
import com.mysql.jdbc.ResultSetRow;
import com.mysql.jdbc.RowDataStatic;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StringUtils;
import com.mysql.jdbc.Util;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class DatabaseMetaData
implements java.sql.DatabaseMetaData {
    protected static final int MAX_IDENTIFIER_LENGTH = 64;
    private static final int DEFERRABILITY = 13;
    private static final int DELETE_RULE = 10;
    private static final int FK_NAME = 11;
    private static final int FKCOLUMN_NAME = 7;
    private static final int FKTABLE_CAT = 4;
    private static final int FKTABLE_NAME = 6;
    private static final int FKTABLE_SCHEM = 5;
    private static final int KEY_SEQ = 8;
    private static final int PK_NAME = 12;
    private static final int PKCOLUMN_NAME = 3;
    private static final int PKTABLE_CAT = 0;
    private static final int PKTABLE_NAME = 2;
    private static final int PKTABLE_SCHEM = 1;
    private static final String SUPPORTS_FK = "SUPPORTS_FK";
    protected static final byte[] TABLE_AS_BYTES = "TABLE".getBytes();
    protected static final byte[] SYSTEM_TABLE_AS_BYTES = "SYSTEM TABLE".getBytes();
    private static final int UPDATE_RULE = 9;
    protected static final byte[] VIEW_AS_BYTES = "VIEW".getBytes();
    private static final Constructor<?> JDBC_4_DBMD_SHOW_CTOR;
    private static final Constructor<?> JDBC_4_DBMD_IS_CTOR;
    private static final String[] MYSQL_KEYWORDS;
    private static final String[] SQL92_KEYWORDS;
    private static final String[] SQL2003_KEYWORDS;
    private static volatile String mysqlKeywords;
    protected MySQLConnection conn;
    protected String database = null;
    protected final String quotedId;
    private ExceptionInterceptor exceptionInterceptor;

    protected static DatabaseMetaData getInstance(MySQLConnection connToSet, String databaseToSet, boolean checkForInfoSchema) throws SQLException {
        if (!Util.isJdbc4()) {
            if (checkForInfoSchema && connToSet.getUseInformationSchema() && connToSet.versionMeetsMinimum(5, 0, 7)) {
                return new DatabaseMetaDataUsingInfoSchema(connToSet, databaseToSet);
            }
            return new DatabaseMetaData(connToSet, databaseToSet);
        }
        if (checkForInfoSchema && connToSet.getUseInformationSchema() && connToSet.versionMeetsMinimum(5, 0, 7)) {
            return (DatabaseMetaData)Util.handleNewInstance(JDBC_4_DBMD_IS_CTOR, new Object[]{connToSet, databaseToSet}, connToSet.getExceptionInterceptor());
        }
        return (DatabaseMetaData)Util.handleNewInstance(JDBC_4_DBMD_SHOW_CTOR, new Object[]{connToSet, databaseToSet}, connToSet.getExceptionInterceptor());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected DatabaseMetaData(MySQLConnection connToSet, String databaseToSet) {
        this.conn = connToSet;
        this.database = databaseToSet;
        this.exceptionInterceptor = this.conn.getExceptionInterceptor();
        String identifierQuote = null;
        try {
            try {
                identifierQuote = this.getIdentifierQuoteString();
            }
            catch (SQLException sqlEx) {
                AssertionFailedException.shouldNotHappen(sqlEx);
                Object var6_5 = null;
                this.quotedId = identifierQuote;
            }
            Object var6_4 = null;
            this.quotedId = identifierQuote;
        }
        catch (Throwable throwable) {
            Object var6_6 = null;
            this.quotedId = identifierQuote;
            throw throwable;
        }
    }

    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        return false;
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        return false;
    }

    private ResultSet buildResultSet(Field[] fields, ArrayList<ResultSetRow> rows) throws SQLException {
        return DatabaseMetaData.buildResultSet(fields, rows, this.conn);
    }

    static ResultSet buildResultSet(Field[] fields, ArrayList<ResultSetRow> rows, MySQLConnection c) throws SQLException {
        int fieldsLength = fields.length;
        for (int i = 0; i < fieldsLength; ++i) {
            int jdbcType = fields[i].getSQLType();
            switch (jdbcType) {
                case -1: 
                case 1: 
                case 12: {
                    fields[i].setEncoding(c.getCharacterSetMetadata(), c);
                    break;
                }
            }
            fields[i].setConnection(c);
            fields[i].setUseOldNameMetadata(true);
        }
        return ResultSetImpl.getInstance(c.getCatalog(), fields, new RowDataStatic(rows), c, null, false);
    }

    protected void convertToJdbcFunctionList(String catalog, ResultSet proceduresRs, boolean needsClientFiltering, String db, List<ComparableWrapper<String, ResultSetRow>> procedureRows, int nameIndex, Field[] fields) throws SQLException {
        while (proceduresRs.next()) {
            boolean shouldAdd = true;
            if (needsClientFiltering) {
                shouldAdd = false;
                String procDb = proceduresRs.getString(1);
                if (db == null && procDb == null) {
                    shouldAdd = true;
                } else if (db != null && db.equals(procDb)) {
                    shouldAdd = true;
                }
            }
            if (!shouldAdd) continue;
            String functionName = proceduresRs.getString(nameIndex);
            Object rowData = null;
            if (fields != null && fields.length == 9) {
                rowData = new byte[9][];
                rowData[0] = catalog == null ? null : this.s2b(catalog);
                rowData[1] = null;
                rowData[2] = this.s2b(functionName);
                rowData[3] = null;
                rowData[4] = null;
                rowData[5] = null;
                rowData[6] = this.s2b(proceduresRs.getString("comment"));
                rowData[7] = this.s2b(Integer.toString(2));
                rowData[8] = this.s2b(functionName);
            } else {
                rowData = new byte[6][];
                rowData[0] = catalog == null ? null : this.s2b(catalog);
                rowData[1] = null;
                rowData[2] = this.s2b(functionName);
                rowData[3] = this.s2b(proceduresRs.getString("comment"));
                rowData[4] = this.s2b(Integer.toString(this.getJDBC4FunctionNoTableConstant()));
                rowData[5] = this.s2b(functionName);
            }
            procedureRows.add(new ComparableWrapper<String, ByteArrayRow>(this.getFullyQualifiedName(catalog, functionName), new ByteArrayRow((byte[][])rowData, this.getExceptionInterceptor())));
        }
    }

    protected String getFullyQualifiedName(String catalog, String entity) {
        StringBuilder fullyQualifiedName = new StringBuilder(StringUtils.quoteIdentifier(catalog == null ? "" : catalog, this.quotedId, this.conn.getPedantic()));
        fullyQualifiedName.append('.');
        fullyQualifiedName.append(StringUtils.quoteIdentifier(entity, this.quotedId, this.conn.getPedantic()));
        return fullyQualifiedName.toString();
    }

    protected int getJDBC4FunctionNoTableConstant() {
        return 0;
    }

    protected void convertToJdbcProcedureList(boolean fromSelect, String catalog, ResultSet proceduresRs, boolean needsClientFiltering, String db, List<ComparableWrapper<String, ResultSetRow>> procedureRows, int nameIndex) throws SQLException {
        while (proceduresRs.next()) {
            boolean shouldAdd = true;
            if (needsClientFiltering) {
                shouldAdd = false;
                String procDb = proceduresRs.getString(1);
                if (db == null && procDb == null) {
                    shouldAdd = true;
                } else if (db != null && db.equals(procDb)) {
                    shouldAdd = true;
                }
            }
            if (!shouldAdd) continue;
            String procedureName = proceduresRs.getString(nameIndex);
            byte[][] rowData = new byte[9][];
            rowData[0] = catalog == null ? null : this.s2b(catalog);
            rowData[1] = null;
            rowData[2] = this.s2b(procedureName);
            rowData[3] = null;
            rowData[4] = null;
            rowData[5] = null;
            rowData[6] = this.s2b(proceduresRs.getString("comment"));
            boolean isFunction = fromSelect ? "FUNCTION".equalsIgnoreCase(proceduresRs.getString("type")) : false;
            rowData[7] = this.s2b(isFunction ? Integer.toString(2) : Integer.toString(1));
            rowData[8] = this.s2b(procedureName);
            procedureRows.add(new ComparableWrapper<String, ByteArrayRow>(this.getFullyQualifiedName(catalog, procedureName), new ByteArrayRow(rowData, this.getExceptionInterceptor())));
        }
    }

    private ResultSetRow convertTypeDescriptorToProcedureRow(byte[] procNameAsBytes, byte[] procCatAsBytes, String paramName, boolean isOutParam, boolean isInParam, boolean isReturnParam, TypeDescriptor typeDesc, boolean forGetFunctionColumns, int ordinal) throws SQLException {
        byte[][] row = forGetFunctionColumns ? new byte[17][] : new byte[20][];
        row[0] = procCatAsBytes;
        row[1] = null;
        row[2] = procNameAsBytes;
        row[3] = this.s2b(paramName);
        row[4] = this.s2b(String.valueOf(this.getColumnType(isOutParam, isInParam, isReturnParam, forGetFunctionColumns)));
        row[5] = this.s2b(Short.toString(typeDesc.dataType));
        row[6] = this.s2b(typeDesc.typeName);
        row[7] = typeDesc.columnSize == null ? null : this.s2b(typeDesc.columnSize.toString());
        row[8] = row[7];
        row[9] = typeDesc.decimalDigits == null ? null : this.s2b(typeDesc.decimalDigits.toString());
        row[10] = this.s2b(Integer.toString(typeDesc.numPrecRadix));
        switch (typeDesc.nullability) {
            case 0: {
                row[11] = this.s2b(String.valueOf(0));
                break;
            }
            case 1: {
                row[11] = this.s2b(String.valueOf(1));
                break;
            }
            case 2: {
                row[11] = this.s2b(String.valueOf(2));
                break;
            }
            default: {
                throw SQLError.createSQLException("Internal error while parsing callable statement metadata (unknown nullability value fount)", "S1000", this.getExceptionInterceptor());
            }
        }
        row[12] = null;
        if (forGetFunctionColumns) {
            row[13] = null;
            row[14] = this.s2b(String.valueOf(ordinal));
            row[15] = this.s2b(typeDesc.isNullable);
            row[16] = procNameAsBytes;
        } else {
            row[13] = null;
            row[14] = null;
            row[15] = null;
            row[16] = null;
            row[17] = this.s2b(String.valueOf(ordinal));
            row[18] = this.s2b(typeDesc.isNullable);
            row[19] = procNameAsBytes;
        }
        return new ByteArrayRow(row, this.getExceptionInterceptor());
    }

    protected int getColumnType(boolean isOutParam, boolean isInParam, boolean isReturnParam, boolean forGetFunctionColumns) {
        if (isInParam && isOutParam) {
            return 2;
        }
        if (isInParam) {
            return 1;
        }
        if (isOutParam) {
            return 4;
        }
        if (isReturnParam) {
            return 5;
        }
        return 0;
    }

    protected ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return true;
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return false;
    }

    @Override
    public boolean deletesAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return true;
    }

    public List<ResultSetRow> extractForeignKeyForTable(ArrayList<ResultSetRow> rows, ResultSet rs, String catalog) throws SQLException {
        byte[][] row = new byte[3][];
        row[0] = rs.getBytes(1);
        row[1] = this.s2b(SUPPORTS_FK);
        String createTableString = rs.getString(2);
        StringTokenizer lineTokenizer = new StringTokenizer(createTableString, "\n");
        StringBuilder commentBuf = new StringBuilder("comment; ");
        boolean firstTime = true;
        while (lineTokenizer.hasMoreTokens()) {
            int afterFk;
            int indexOfRef;
            String line = lineTokenizer.nextToken().trim();
            String constraintName = null;
            if (StringUtils.startsWithIgnoreCase(line, "CONSTRAINT")) {
                boolean usingBackTicks = true;
                int beginPos = StringUtils.indexOfQuoteDoubleAware(line, this.quotedId, 0);
                if (beginPos == -1) {
                    beginPos = line.indexOf("\"");
                    usingBackTicks = false;
                }
                if (beginPos != -1) {
                    int endPos = -1;
                    endPos = usingBackTicks ? StringUtils.indexOfQuoteDoubleAware(line, this.quotedId, beginPos + 1) : StringUtils.indexOfQuoteDoubleAware(line, "\"", beginPos + 1);
                    if (endPos != -1) {
                        constraintName = line.substring(beginPos + 1, endPos);
                        line = line.substring(endPos + 1, line.length()).trim();
                    }
                }
            }
            if (!line.startsWith("FOREIGN KEY")) continue;
            if (line.endsWith(",")) {
                line = line.substring(0, line.length() - 1);
            }
            int indexOfFK = line.indexOf("FOREIGN KEY");
            String localColumnName = null;
            String referencedCatalogName = StringUtils.quoteIdentifier(catalog, this.quotedId, this.conn.getPedantic());
            String referencedTableName = null;
            String referencedColumnName = null;
            if (indexOfFK != -1 && (indexOfRef = StringUtils.indexOfIgnoreCase(afterFk = indexOfFK + "FOREIGN KEY".length(), line, "REFERENCES", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL)) != -1) {
                int indexOfParenOpen = line.indexOf(40, afterFk);
                int indexOfParenClose = StringUtils.indexOfIgnoreCase(indexOfParenOpen, line, ")", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
                if (indexOfParenOpen == -1 || indexOfParenClose == -1) {
                    // empty if block
                }
                localColumnName = line.substring(indexOfParenOpen + 1, indexOfParenClose);
                int afterRef = indexOfRef + "REFERENCES".length();
                int referencedColumnBegin = StringUtils.indexOfIgnoreCase(afterRef, line, "(", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
                if (referencedColumnBegin != -1) {
                    int indexOfCatalogSep;
                    referencedTableName = line.substring(afterRef, referencedColumnBegin);
                    int referencedColumnEnd = StringUtils.indexOfIgnoreCase(referencedColumnBegin + 1, line, ")", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
                    if (referencedColumnEnd != -1) {
                        referencedColumnName = line.substring(referencedColumnBegin + 1, referencedColumnEnd);
                    }
                    if ((indexOfCatalogSep = StringUtils.indexOfIgnoreCase(0, referencedTableName, ".", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL)) != -1) {
                        referencedCatalogName = referencedTableName.substring(0, indexOfCatalogSep);
                        referencedTableName = referencedTableName.substring(indexOfCatalogSep + 1);
                    }
                }
            }
            if (!firstTime) {
                commentBuf.append("; ");
            } else {
                firstTime = false;
            }
            if (constraintName != null) {
                commentBuf.append(constraintName);
            } else {
                commentBuf.append("not_available");
            }
            commentBuf.append("(");
            commentBuf.append(localColumnName);
            commentBuf.append(") REFER ");
            commentBuf.append(referencedCatalogName);
            commentBuf.append("/");
            commentBuf.append(referencedTableName);
            commentBuf.append("(");
            commentBuf.append(referencedColumnName);
            commentBuf.append(")");
            int lastParenIndex = line.lastIndexOf(")");
            if (lastParenIndex == line.length() - 1) continue;
            String cascadeOptions = line.substring(lastParenIndex + 1);
            commentBuf.append(" ");
            commentBuf.append(cascadeOptions);
        }
        row[2] = this.s2b(commentBuf.toString());
        rows.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
        return rows;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ResultSet extractForeignKeyFromCreateTable(String catalog, String tableName) throws SQLException {
        Field[] fields;
        ArrayList<ResultSetRow> rows;
        java.sql.Statement stmt;
        ResultSet rs;
        block16: {
            ArrayList<String> tableList = new ArrayList<String>();
            rs = null;
            stmt = null;
            if (tableName != null) {
                tableList.add(tableName);
            } else {
                block15: {
                    try {
                        rs = this.getTables(catalog, "", "%", new String[]{"TABLE"});
                        while (rs.next()) {
                            tableList.add(rs.getString("TABLE_NAME"));
                        }
                        Object var7_6 = null;
                        if (rs == null) break block15;
                    }
                    catch (Throwable throwable) {
                        Object var7_7 = null;
                        if (rs != null) {
                            rs.close();
                        }
                        rs = null;
                        throw throwable;
                    }
                    rs.close();
                }
                rs = null;
                {
                }
            }
            rows = new ArrayList<ResultSetRow>();
            fields = new Field[]{new Field("", "Name", 1, Integer.MAX_VALUE), new Field("", "Type", 1, 255), new Field("", "Comment", 1, Integer.MAX_VALUE)};
            int numTables = tableList.size();
            stmt = this.conn.getMetadataSafeStatement();
            try {
                for (int i = 0; i < numTables; ++i) {
                    String tableToExtract = (String)tableList.get(i);
                    String query = "SHOW CREATE TABLE " + this.getFullyQualifiedName(catalog, tableToExtract);
                    try {
                        rs = stmt.executeQuery(query);
                    }
                    catch (SQLException sqlEx) {
                        String sqlState = sqlEx.getSQLState();
                        if ("42S02".equals(sqlState) || sqlEx.getErrorCode() == 1146) continue;
                        throw sqlEx;
                    }
                    while (rs.next()) {
                        this.extractForeignKeyForTable(rows, rs, catalog);
                    }
                }
                Object var15_16 = null;
                if (rs == null) break block16;
            }
            catch (Throwable throwable) {
                Object var15_17 = null;
                if (rs != null) {
                    rs.close();
                }
                rs = null;
                if (stmt != null) {
                    stmt.close();
                }
                stmt = null;
                throw throwable;
            }
            rs.close();
        }
        rs = null;
        if (stmt != null) {
            stmt.close();
        }
        stmt = null;
        return this.buildResultSet(fields, rows);
    }

    @Override
    public ResultSet getAttributes(String arg0, String arg1, String arg2, String arg3) throws SQLException {
        Field[] fields = new Field[]{new Field("", "TYPE_CAT", 1, 32), new Field("", "TYPE_SCHEM", 1, 32), new Field("", "TYPE_NAME", 1, 32), new Field("", "ATTR_NAME", 1, 32), new Field("", "DATA_TYPE", 5, 32), new Field("", "ATTR_TYPE_NAME", 1, 32), new Field("", "ATTR_SIZE", 4, 32), new Field("", "DECIMAL_DIGITS", 4, 32), new Field("", "NUM_PREC_RADIX", 4, 32), new Field("", "NULLABLE ", 4, 32), new Field("", "REMARKS", 1, 32), new Field("", "ATTR_DEF", 1, 32), new Field("", "SQL_DATA_TYPE", 4, 32), new Field("", "SQL_DATETIME_SUB", 4, 32), new Field("", "CHAR_OCTET_LENGTH", 4, 32), new Field("", "ORDINAL_POSITION", 4, 32), new Field("", "IS_NULLABLE", 1, 32), new Field("", "SCOPE_CATALOG", 1, 32), new Field("", "SCOPE_SCHEMA", 1, 32), new Field("", "SCOPE_TABLE", 1, 32), new Field("", "SOURCE_DATA_TYPE", 5, 32)};
        return this.buildResultSet(fields, new ArrayList<ResultSetRow>());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, final String table, int scope, boolean nullable) throws SQLException {
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
        }
        Field[] fields = new Field[]{new Field("", "SCOPE", 5, 5), new Field("", "COLUMN_NAME", 1, 32), new Field("", "DATA_TYPE", 4, 32), new Field("", "TYPE_NAME", 1, 32), new Field("", "COLUMN_SIZE", 4, 10), new Field("", "BUFFER_LENGTH", 4, 10), new Field("", "DECIMAL_DIGITS", 5, 10), new Field("", "PSEUDO_COLUMN", 5, 5)};
        final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        final java.sql.Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            new IterateBlock<String>(this.getCatalogIterator(catalog)){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 * Loose catch block
                 */
                @Override
                void forEach(String catalogStr) throws SQLException {
                    block17: {
                        ResultSet results = null;
                        StringBuilder queryBuf = new StringBuilder("SHOW COLUMNS FROM ");
                        queryBuf.append(StringUtils.quoteIdentifier(table, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                        queryBuf.append(" FROM ");
                        queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                        results = stmt.executeQuery(queryBuf.toString());
                        while (results.next()) {
                            String keyType = results.getString("Key");
                            if (keyType == null || !StringUtils.startsWithIgnoreCase(keyType, "PRI")) continue;
                            byte[][] rowVal = new byte[8][];
                            rowVal[0] = Integer.toString(2).getBytes();
                            rowVal[1] = results.getBytes("Field");
                            String type = results.getString("Type");
                            int size = MysqlIO.getMaxBuf();
                            int decimals = 0;
                            if (type.indexOf("enum") != -1) {
                                String temp = type.substring(type.indexOf("("), type.indexOf(")"));
                                StringTokenizer tokenizer = new StringTokenizer(temp, ",");
                                int maxLength = 0;
                                while (tokenizer.hasMoreTokens()) {
                                    maxLength = Math.max(maxLength, tokenizer.nextToken().length() - 2);
                                }
                                size = maxLength;
                                decimals = 0;
                                type = "enum";
                            } else if (type.indexOf("(") != -1) {
                                if (type.indexOf(",") != -1) {
                                    size = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(",")));
                                    decimals = Integer.parseInt(type.substring(type.indexOf(",") + 1, type.indexOf(")")));
                                } else {
                                    size = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(")")));
                                }
                                type = type.substring(0, type.indexOf("("));
                            }
                            rowVal[2] = DatabaseMetaData.this.s2b(String.valueOf(MysqlDefs.mysqlToJavaType(type)));
                            rowVal[3] = DatabaseMetaData.this.s2b(type);
                            rowVal[4] = Integer.toString(size + decimals).getBytes();
                            rowVal[5] = Integer.toString(size + decimals).getBytes();
                            rowVal[6] = Integer.toString(decimals).getBytes();
                            rowVal[7] = Integer.toString(1).getBytes();
                            rows.add(new ByteArrayRow(rowVal, DatabaseMetaData.this.getExceptionInterceptor()));
                        }
                        Object var13_13 = null;
                        if (results == null) break block17;
                        try {
                            results.close();
                        }
                        catch (Exception ex) {
                            // empty catch block
                        }
                        results = null;
                        {
                            break block17;
                            catch (SQLException sqlEx) {
                                if (!"42S02".equals(sqlEx.getSQLState())) {
                                    throw sqlEx;
                                }
                                Object var13_14 = null;
                                if (results == null) break block17;
                                try {
                                    results.close();
                                }
                                catch (Exception ex) {
                                    // empty catch block
                                }
                                results = null;
                            }
                        }
                        catch (Throwable throwable) {
                            Object var13_15 = null;
                            if (results != null) {
                                try {
                                    results.close();
                                }
                                catch (Exception ex) {
                                    // empty catch block
                                }
                                results = null;
                            }
                            throw throwable;
                        }
                    }
                }
            }.doForAll();
            Object var10_9 = null;
            if (stmt == null) return this.buildResultSet(fields, rows);
        }
        catch (Throwable throwable) {
            Object var10_10 = null;
            if (stmt == null) throw throwable;
            stmt.close();
            throw throwable;
        }
        stmt.close();
        return this.buildResultSet(fields, rows);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void getCallStmtParameterTypes(String catalog, String quotedProcName, ProcedureType procType, String parameterNamePattern, List<ResultSetRow> resultRows, boolean forGetFunctionColumns) throws SQLException {
        String declaration;
        SQLException sqlEx32;
        SQLException sqlExRethrow2;
        String storageDefnClosures;
        String storageDefnDelims;
        boolean isProcedureInAnsiMode;
        byte[] procCatAsBytes;
        byte[] procNameAsBytes;
        String parameterDef;
        java.sql.Statement paramRetrievalStmt;
        block45: {
            paramRetrievalStmt = null;
            ResultSet paramRetrievalRs = null;
            if (parameterNamePattern == null) {
                if (!this.conn.getNullNamePatternMatchesAll()) throw SQLError.createSQLException("Parameter/Column name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
                parameterNamePattern = "%";
            }
            parameterDef = null;
            procNameAsBytes = null;
            procCatAsBytes = null;
            isProcedureInAnsiMode = false;
            storageDefnDelims = null;
            storageDefnClosures = null;
            try {
                paramRetrievalStmt = this.conn.getMetadataSafeStatement();
                String oldCatalog = this.conn.getCatalog();
                if (this.conn.lowerCaseTableNames() && catalog != null && catalog.length() != 0 && oldCatalog != null && oldCatalog.length() != 0) {
                    ResultSet rs = null;
                    try {
                        this.conn.setCatalog(StringUtils.unQuoteIdentifier(catalog, this.quotedId));
                        rs = paramRetrievalStmt.executeQuery("SELECT DATABASE()");
                        rs.next();
                        catalog = rs.getString(1);
                        Object var18_20 = null;
                    }
                    catch (Throwable throwable) {
                        Object var18_21 = null;
                        this.conn.setCatalog(oldCatalog);
                        if (rs == null) throw throwable;
                        rs.close();
                        throw throwable;
                    }
                    this.conn.setCatalog(oldCatalog);
                    if (rs != null) {
                        rs.close();
                    }
                }
                if (paramRetrievalStmt.getMaxRows() != 0) {
                    paramRetrievalStmt.setMaxRows(0);
                }
                int dotIndex = -1;
                dotIndex = !" ".equals(this.quotedId) ? StringUtils.indexOfIgnoreCase(0, quotedProcName, ".", this.quotedId, this.quotedId, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL) : quotedProcName.indexOf(".");
                String dbName = null;
                if (dotIndex != -1 && dotIndex + 1 < quotedProcName.length()) {
                    dbName = quotedProcName.substring(0, dotIndex);
                    quotedProcName = quotedProcName.substring(dotIndex + 1);
                } else {
                    dbName = StringUtils.quoteIdentifier(catalog, this.quotedId, this.conn.getPedantic());
                }
                String tmpProcName = StringUtils.unQuoteIdentifier(quotedProcName, this.quotedId);
                try {
                    procNameAsBytes = StringUtils.getBytes(tmpProcName, "UTF-8");
                }
                catch (UnsupportedEncodingException ueEx) {
                    procNameAsBytes = this.s2b(tmpProcName);
                }
                tmpProcName = StringUtils.unQuoteIdentifier(dbName, this.quotedId);
                try {
                    procCatAsBytes = StringUtils.getBytes(tmpProcName, "UTF-8");
                }
                catch (UnsupportedEncodingException ueEx) {
                    procCatAsBytes = this.s2b(tmpProcName);
                }
                StringBuilder procNameBuf = new StringBuilder();
                procNameBuf.append(dbName);
                procNameBuf.append('.');
                procNameBuf.append(quotedProcName);
                String fieldName = null;
                if (procType == ProcedureType.PROCEDURE) {
                    paramRetrievalRs = paramRetrievalStmt.executeQuery("SHOW CREATE PROCEDURE " + procNameBuf.toString());
                    fieldName = "Create Procedure";
                } else {
                    paramRetrievalRs = paramRetrievalStmt.executeQuery("SHOW CREATE FUNCTION " + procNameBuf.toString());
                    fieldName = "Create Function";
                }
                if (paramRetrievalRs.next()) {
                    String procedureDef = paramRetrievalRs.getString(fieldName);
                    if (!(this.conn.getNoAccessToProcedureBodies() || procedureDef != null && procedureDef.length() != 0)) {
                        throw SQLError.createSQLException("User does not have access to metadata required to determine stored procedure parameter types. If rights can not be granted, configure connection with \"noAccessToProcedureBodies=true\" to have driver generate parameters that represent INOUT strings irregardless of actual parameter types.", "S1000", this.getExceptionInterceptor());
                    }
                    try {
                        String sqlMode = paramRetrievalRs.getString("sql_mode");
                        if (StringUtils.indexOfIgnoreCase(sqlMode, "ANSI") != -1) {
                            isProcedureInAnsiMode = true;
                        }
                    }
                    catch (SQLException sqlEx2) {
                        // empty catch block
                    }
                    String identifierMarkers = isProcedureInAnsiMode ? "`\"" : "`";
                    String identifierAndStringMarkers = "'" + identifierMarkers;
                    storageDefnDelims = "(" + identifierMarkers;
                    storageDefnClosures = ")" + identifierMarkers;
                    if (procedureDef != null && procedureDef.length() != 0) {
                        procedureDef = StringUtils.stripComments(procedureDef, identifierAndStringMarkers, identifierAndStringMarkers, true, false, true, true);
                        int openParenIndex = StringUtils.indexOfIgnoreCase(0, procedureDef, "(", this.quotedId, this.quotedId, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
                        int endOfParamDeclarationIndex = 0;
                        endOfParamDeclarationIndex = this.endPositionOfParameterDeclaration(openParenIndex, procedureDef, this.quotedId);
                        if (procType == ProcedureType.FUNCTION) {
                            int declarationStart;
                            int returnsIndex = StringUtils.indexOfIgnoreCase(0, procedureDef, " RETURNS ", this.quotedId, this.quotedId, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
                            int endReturnsDef = this.findEndOfReturnsClause(procedureDef, returnsIndex);
                            for (declarationStart = returnsIndex + "RETURNS ".length(); declarationStart < procedureDef.length() && Character.isWhitespace(procedureDef.charAt(declarationStart)); ++declarationStart) {
                            }
                            String returnsDefn = procedureDef.substring(declarationStart, endReturnsDef).trim();
                            TypeDescriptor returnDescriptor = new TypeDescriptor(returnsDefn, "YES");
                            resultRows.add(this.convertTypeDescriptorToProcedureRow(procNameAsBytes, procCatAsBytes, "", false, false, true, returnDescriptor, forGetFunctionColumns, 0));
                        }
                        if (openParenIndex == -1 || endOfParamDeclarationIndex == -1) {
                            throw SQLError.createSQLException("Internal error when parsing callable statement metadata", "S1000", this.getExceptionInterceptor());
                        }
                        parameterDef = procedureDef.substring(openParenIndex + 1, endOfParamDeclarationIndex);
                    }
                }
                Object var32_47 = null;
                sqlExRethrow2 = null;
                if (paramRetrievalRs == null) break block45;
            }
            catch (Throwable throwable) {
                SQLException sqlEx32;
                Object var32_48 = null;
                SQLException sqlExRethrow2 = null;
                if (paramRetrievalRs != null) {
                    try {
                        paramRetrievalRs.close();
                    }
                    catch (SQLException sqlEx32) {
                        sqlExRethrow2 = sqlEx32;
                    }
                    paramRetrievalRs = null;
                }
                if (paramRetrievalStmt != null) {
                    try {
                        paramRetrievalStmt.close();
                    }
                    catch (SQLException sqlEx32) {
                        sqlExRethrow2 = sqlEx32;
                    }
                    paramRetrievalStmt = null;
                }
                if (sqlExRethrow2 == null) throw throwable;
                throw sqlExRethrow2;
            }
            try {
                paramRetrievalRs.close();
            }
            catch (SQLException sqlEx32) {
                sqlExRethrow2 = sqlEx32;
            }
            paramRetrievalRs = null;
        }
        if (paramRetrievalStmt != null) {
            try {
                paramRetrievalStmt.close();
            }
            catch (SQLException sqlEx32) {
                sqlExRethrow2 = sqlEx32;
            }
            paramRetrievalStmt = null;
        }
        if (sqlExRethrow2 != null) {
            throw sqlExRethrow2;
        }
        if (parameterDef == null) return;
        int ordinal = 1;
        List<String> parseList = StringUtils.split(parameterDef, ",", storageDefnDelims, storageDefnClosures, true);
        int parseListLen = parseList.size();
        for (int i = 0; i < parseListLen && (declaration = parseList.get(i)).trim().length() != 0; ++i) {
            int wildCompareRes;
            declaration = declaration.replaceAll("[\\t\\n\\x0B\\f\\r]", " ");
            StringTokenizer declarationTok = new StringTokenizer(declaration, " \t");
            String paramName = null;
            boolean isOutParam = false;
            boolean isInParam = false;
            if (!declarationTok.hasMoreTokens()) throw SQLError.createSQLException("Internal error when parsing callable statement metadata (unknown output from 'SHOW CREATE PROCEDURE')", "S1000", this.getExceptionInterceptor());
            String possibleParamName = declarationTok.nextToken();
            if (possibleParamName.equalsIgnoreCase("OUT")) {
                isOutParam = true;
                if (!declarationTok.hasMoreTokens()) throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", "S1000", this.getExceptionInterceptor());
                paramName = declarationTok.nextToken();
            } else if (possibleParamName.equalsIgnoreCase("INOUT")) {
                isOutParam = true;
                isInParam = true;
                if (!declarationTok.hasMoreTokens()) throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", "S1000", this.getExceptionInterceptor());
                paramName = declarationTok.nextToken();
            } else if (possibleParamName.equalsIgnoreCase("IN")) {
                isOutParam = false;
                isInParam = true;
                if (!declarationTok.hasMoreTokens()) throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", "S1000", this.getExceptionInterceptor());
                paramName = declarationTok.nextToken();
            } else {
                isOutParam = false;
                isInParam = true;
                paramName = possibleParamName;
            }
            TypeDescriptor typeDesc = null;
            if (!declarationTok.hasMoreTokens()) throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter type)", "S1000", this.getExceptionInterceptor());
            StringBuilder typeInfoBuf = new StringBuilder(declarationTok.nextToken());
            while (declarationTok.hasMoreTokens()) {
                typeInfoBuf.append(" ");
                typeInfoBuf.append(declarationTok.nextToken());
            }
            String typeInfo = typeInfoBuf.toString();
            typeDesc = new TypeDescriptor(typeInfo, "YES");
            if (paramName.startsWith("`") && paramName.endsWith("`") || isProcedureInAnsiMode && paramName.startsWith("\"") && paramName.endsWith("\"")) {
                paramName = paramName.substring(1, paramName.length() - 1);
            }
            if ((wildCompareRes = StringUtils.wildCompare(paramName, parameterNamePattern)) == -1) continue;
            ResultSetRow row = this.convertTypeDescriptorToProcedureRow(procNameAsBytes, procCatAsBytes, paramName, isOutParam, isInParam, false, typeDesc, forGetFunctionColumns, ordinal++);
            resultRows.add(row);
        }
    }

    private int endPositionOfParameterDeclaration(int beginIndex, String procedureDef, String quoteChar) throws SQLException {
        int currentPos = beginIndex + 1;
        int parenDepth = 1;
        while (parenDepth > 0 && currentPos < procedureDef.length()) {
            int closedParenIndex = StringUtils.indexOfIgnoreCase(currentPos, procedureDef, ")", quoteChar, quoteChar, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
            if (closedParenIndex != -1) {
                int nextOpenParenIndex = StringUtils.indexOfIgnoreCase(currentPos, procedureDef, "(", quoteChar, quoteChar, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
                if (nextOpenParenIndex != -1 && nextOpenParenIndex < closedParenIndex) {
                    ++parenDepth;
                    currentPos = closedParenIndex + 1;
                    continue;
                }
                --parenDepth;
                currentPos = closedParenIndex;
                continue;
            }
            throw SQLError.createSQLException("Internal error when parsing callable statement metadata", "S1000", this.getExceptionInterceptor());
        }
        return currentPos;
    }

    private int findEndOfReturnsClause(String procedureDefn, int positionOfReturnKeyword) throws SQLException {
        int i;
        String openingMarkers = this.quotedId + "(";
        String closingMarkers = this.quotedId + ")";
        String[] tokens = new String[]{"LANGUAGE", "NOT", "DETERMINISTIC", "CONTAINS", "NO", "READ", "MODIFIES", "SQL", "COMMENT", "BEGIN", "RETURN"};
        int startLookingAt = positionOfReturnKeyword + "RETURNS".length() + 1;
        int endOfReturn = -1;
        for (i = 0; i < tokens.length; ++i) {
            int nextEndOfReturn = StringUtils.indexOfIgnoreCase(startLookingAt, procedureDefn, tokens[i], openingMarkers, closingMarkers, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
            if (nextEndOfReturn == -1 || endOfReturn != -1 && nextEndOfReturn >= endOfReturn) continue;
            endOfReturn = nextEndOfReturn;
        }
        if (endOfReturn != -1) {
            return endOfReturn;
        }
        endOfReturn = StringUtils.indexOfIgnoreCase(startLookingAt, procedureDefn, ":", openingMarkers, closingMarkers, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
        if (endOfReturn != -1) {
            for (i = endOfReturn; i > 0; --i) {
                if (!Character.isWhitespace(procedureDefn.charAt(i))) continue;
                return i;
            }
        }
        throw SQLError.createSQLException("Internal error when parsing callable statement metadata", "S1000", this.getExceptionInterceptor());
    }

    private int getCascadeDeleteOption(String cascadeOptions) {
        int onDeletePos = cascadeOptions.indexOf("ON DELETE");
        if (onDeletePos != -1) {
            String deleteOptions = cascadeOptions.substring(onDeletePos, cascadeOptions.length());
            if (deleteOptions.startsWith("ON DELETE CASCADE")) {
                return 0;
            }
            if (deleteOptions.startsWith("ON DELETE SET NULL")) {
                return 2;
            }
            if (deleteOptions.startsWith("ON DELETE RESTRICT")) {
                return 1;
            }
            if (deleteOptions.startsWith("ON DELETE NO ACTION")) {
                return 3;
            }
        }
        return 3;
    }

    private int getCascadeUpdateOption(String cascadeOptions) {
        int onUpdatePos = cascadeOptions.indexOf("ON UPDATE");
        if (onUpdatePos != -1) {
            String updateOptions = cascadeOptions.substring(onUpdatePos, cascadeOptions.length());
            if (updateOptions.startsWith("ON UPDATE CASCADE")) {
                return 0;
            }
            if (updateOptions.startsWith("ON UPDATE SET NULL")) {
                return 2;
            }
            if (updateOptions.startsWith("ON UPDATE RESTRICT")) {
                return 1;
            }
            if (updateOptions.startsWith("ON UPDATE NO ACTION")) {
                return 3;
            }
        }
        return 3;
    }

    protected IteratorWithCleanup<String> getCatalogIterator(String catalogSpec) throws SQLException {
        IteratorWithCleanup allCatalogsIter = catalogSpec != null ? (!catalogSpec.equals("") ? (this.conn.getPedantic() ? new SingleStringIterator(catalogSpec) : new SingleStringIterator(StringUtils.unQuoteIdentifier(catalogSpec, this.quotedId))) : new SingleStringIterator(this.database)) : (this.conn.getNullCatalogMeansCurrent() ? new SingleStringIterator(this.database) : new ResultSetIterator(this.getCatalogs(), 1));
        return allCatalogsIter;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ResultSet getCatalogs() throws SQLException {
        SQLException sqlEx22;
        ResultSet resultSet;
        java.sql.Statement stmt;
        block13: {
            ResultSet results = null;
            stmt = null;
            try {
                stmt = this.conn.getMetadataSafeStatement();
                results = stmt.executeQuery("SHOW DATABASES");
                int catalogsCount = 0;
                if (results.last()) {
                    catalogsCount = results.getRow();
                    results.beforeFirst();
                }
                ArrayList<String> resultsAsList = new ArrayList<String>(catalogsCount);
                while (results.next()) {
                    resultsAsList.add(results.getString(1));
                }
                Collections.sort(resultsAsList);
                Field[] fields = new Field[]{new Field("", "TABLE_CAT", 12, results.getMetaData().getColumnDisplaySize(1))};
                ArrayList<ResultSetRow> tuples = new ArrayList<ResultSetRow>(catalogsCount);
                for (String cat : resultsAsList) {
                    byte[][] rowVal = new byte[][]{this.s2b(cat)};
                    tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
                }
                resultSet = this.buildResultSet(fields, tuples);
                Object var11_10 = null;
                if (results == null) break block13;
            }
            catch (Throwable throwable) {
                block16: {
                    SQLException sqlEx22;
                    Object var11_11 = null;
                    if (results != null) {
                        try {
                            results.close();
                        }
                        catch (SQLException sqlEx22) {
                            AssertionFailedException.shouldNotHappen(sqlEx22);
                        }
                        results = null;
                    }
                    if (stmt == null) break block16;
                    try {
                        stmt.close();
                    }
                    catch (SQLException sqlEx22) {
                        AssertionFailedException.shouldNotHappen(sqlEx22);
                    }
                    stmt = null;
                }
                throw throwable;
            }
            try {
                results.close();
            }
            catch (SQLException sqlEx22) {
                AssertionFailedException.shouldNotHappen(sqlEx22);
            }
            results = null;
        }
        if (stmt != null) {
            try {
                stmt.close();
            }
            catch (SQLException sqlEx22) {
                AssertionFailedException.shouldNotHappen(sqlEx22);
            }
            stmt = null;
        }
        return resultSet;
    }

    @Override
    public String getCatalogSeparator() throws SQLException {
        return ".";
    }

    @Override
    public String getCatalogTerm() throws SQLException {
        return "database";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
        ArrayList<ResultSetRow> grantRows;
        Field[] fields;
        block17: {
            Exception ex22;
            PreparedStatement pStmt;
            block14: {
                fields = new Field[]{new Field("", "TABLE_CAT", 1, 64), new Field("", "TABLE_SCHEM", 1, 1), new Field("", "TABLE_NAME", 1, 64), new Field("", "COLUMN_NAME", 1, 64), new Field("", "GRANTOR", 1, 77), new Field("", "GRANTEE", 1, 77), new Field("", "PRIVILEGE", 1, 64), new Field("", "IS_GRANTABLE", 1, 3)};
                String grantQuery = "SELECT c.host, c.db, t.grantor, c.user, c.table_name, c.column_name, c.column_priv FROM mysql.columns_priv c, mysql.tables_priv t WHERE c.host = t.host AND c.db = t.db AND c.table_name = t.table_name AND c.db LIKE ? AND c.table_name = ? AND c.column_name LIKE ?";
                pStmt = null;
                ResultSet results = null;
                grantRows = new ArrayList<ResultSetRow>();
                try {
                    pStmt = this.prepareMetaDataSafeStatement(grantQuery);
                    pStmt.setString(1, catalog != null && catalog.length() != 0 ? catalog : "%");
                    pStmt.setString(2, table);
                    pStmt.setString(3, columnNamePattern);
                    results = pStmt.executeQuery();
                    while (results.next()) {
                        String host = results.getString(1);
                        String db = results.getString(2);
                        String grantor = results.getString(3);
                        String user = results.getString(4);
                        if (user == null || user.length() == 0) {
                            user = "%";
                        }
                        StringBuilder fullUser = new StringBuilder(user);
                        if (host != null && this.conn.getUseHostsInPrivileges()) {
                            fullUser.append("@");
                            fullUser.append(host);
                        }
                        String columnName = results.getString(6);
                        String allPrivileges = results.getString(7);
                        if (allPrivileges == null) continue;
                        allPrivileges = allPrivileges.toUpperCase(Locale.ENGLISH);
                        StringTokenizer st = new StringTokenizer(allPrivileges, ",");
                        while (st.hasMoreTokens()) {
                            String privilege = st.nextToken().trim();
                            byte[][] tuple = new byte[][]{this.s2b(db), null, this.s2b(table), this.s2b(columnName), (byte[])(grantor != null ? this.s2b(grantor) : null), this.s2b(fullUser.toString()), this.s2b(privilege), null};
                            grantRows.add(new ByteArrayRow(tuple, this.getExceptionInterceptor()));
                        }
                    }
                    Object var21_20 = null;
                    if (results == null) break block14;
                }
                catch (Throwable throwable) {
                    Exception ex22;
                    Object var21_21 = null;
                    if (results != null) {
                        try {
                            results.close();
                        }
                        catch (Exception ex22) {
                            // empty catch block
                        }
                        results = null;
                    }
                    if (pStmt != null) {
                        try {
                            pStmt.close();
                        }
                        catch (Exception ex22) {
                            // empty catch block
                        }
                        pStmt = null;
                    }
                    throw throwable;
                }
                try {
                    results.close();
                }
                catch (Exception ex22) {
                    // empty catch block
                }
                results = null;
            }
            if (pStmt == null) break block17;
            try {
                pStmt.close();
            }
            catch (Exception ex22) {
                // empty catch block
            }
            pStmt = null;
            {
            }
        }
        return this.buildResultSet(fields, grantRows);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public ResultSet getColumns(String catalog, final String schemaPattern, final String tableNamePattern, String columnNamePattern) throws SQLException {
        if (columnNamePattern == null) {
            if (!this.conn.getNullNamePatternMatchesAll()) throw SQLError.createSQLException("Column name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
            columnNamePattern = "%";
        }
        final String colPattern = columnNamePattern;
        Field[] fields = this.createColumnsFields();
        final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        final java.sql.Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            new IterateBlock<String>(this.getCatalogIterator(catalog)){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                void forEach(String catalogStr) throws SQLException {
                    ArrayList<String> tableNameList;
                    block40: {
                        String tableNameFromList;
                        ResultSet tables;
                        tableNameList = new ArrayList<String>();
                        if (tableNamePattern == null) {
                            tables = null;
                            try {
                                tables = DatabaseMetaData.this.getTables(catalogStr, schemaPattern, "%", new String[0]);
                                while (tables.next()) {
                                    tableNameFromList = tables.getString("TABLE_NAME");
                                    tableNameList.add(tableNameFromList);
                                }
                                Object var6_5 = null;
                                if (tables == null) break block40;
                            }
                            catch (Throwable throwable) {
                                Object var6_6 = null;
                                if (tables != null) {
                                    try {
                                        tables.close();
                                    }
                                    catch (Exception sqlEx) {
                                        AssertionFailedException.shouldNotHappen(sqlEx);
                                    }
                                    tables = null;
                                }
                                throw throwable;
                            }
                            try {
                                tables.close();
                            }
                            catch (Exception sqlEx) {
                                AssertionFailedException.shouldNotHappen(sqlEx);
                            }
                            tables = null;
                            {
                                break block40;
                            }
                        }
                        tables = null;
                        try {
                            tables = DatabaseMetaData.this.getTables(catalogStr, schemaPattern, tableNamePattern, new String[0]);
                            while (tables.next()) {
                                tableNameFromList = tables.getString("TABLE_NAME");
                                tableNameList.add(tableNameFromList);
                            }
                            Object var9_12 = null;
                            if (tables == null) break block40;
                        }
                        catch (Throwable throwable) {
                            Object var9_13 = null;
                            if (tables != null) {
                                try {
                                    tables.close();
                                }
                                catch (SQLException sqlEx) {
                                    AssertionFailedException.shouldNotHappen(sqlEx);
                                }
                                tables = null;
                            }
                            throw throwable;
                        }
                        try {
                            tables.close();
                        }
                        catch (SQLException sqlEx) {
                            AssertionFailedException.shouldNotHappen(sqlEx);
                        }
                        tables = null;
                        {
                        }
                    }
                    for (String tableName : tableNameList) {
                        Exception ex2;
                        Object var15_26;
                        ResultSet results = null;
                        try {
                            StringBuilder queryBuf = new StringBuilder("SHOW ");
                            if (DatabaseMetaData.this.conn.versionMeetsMinimum(4, 1, 0)) {
                                queryBuf.append("FULL ");
                            }
                            queryBuf.append("COLUMNS FROM ");
                            queryBuf.append(StringUtils.quoteIdentifier(tableName, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                            queryBuf.append(" FROM ");
                            queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                            queryBuf.append(" LIKE ");
                            queryBuf.append(StringUtils.quoteIdentifier(colPattern, "'", true));
                            boolean fixUpOrdinalsRequired = false;
                            HashMap<String, Integer> ordinalFixUpMap = null;
                            if (!colPattern.equals("%")) {
                                fixUpOrdinalsRequired = true;
                                StringBuilder fullColumnQueryBuf = new StringBuilder("SHOW ");
                                if (DatabaseMetaData.this.conn.versionMeetsMinimum(4, 1, 0)) {
                                    fullColumnQueryBuf.append("FULL ");
                                }
                                fullColumnQueryBuf.append("COLUMNS FROM ");
                                fullColumnQueryBuf.append(StringUtils.quoteIdentifier(tableName, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                fullColumnQueryBuf.append(" FROM ");
                                fullColumnQueryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                results = stmt.executeQuery(fullColumnQueryBuf.toString());
                                ordinalFixUpMap = new HashMap<String, Integer>();
                                int fullOrdinalPos = 1;
                                while (results.next()) {
                                    String fullOrdColName = results.getString("Field");
                                    ordinalFixUpMap.put(fullOrdColName, fullOrdinalPos++);
                                }
                            }
                            results = stmt.executeQuery(queryBuf.toString());
                            int ordPos = 1;
                            while (results.next()) {
                                byte[][] rowVal = new byte[24][];
                                rowVal[0] = DatabaseMetaData.this.s2b(catalogStr);
                                rowVal[1] = null;
                                rowVal[2] = DatabaseMetaData.this.s2b(tableName);
                                rowVal[3] = results.getBytes("Field");
                                TypeDescriptor typeDesc = new TypeDescriptor(results.getString("Type"), results.getString("Null"));
                                rowVal[4] = Short.toString(typeDesc.dataType).getBytes();
                                rowVal[5] = DatabaseMetaData.this.s2b(typeDesc.typeName);
                                if (typeDesc.columnSize == null) {
                                    rowVal[6] = null;
                                } else {
                                    String collation = results.getString("Collation");
                                    int mbminlen = 1;
                                    if (collation != null && ("TEXT".equals(typeDesc.typeName) || "TINYTEXT".equals(typeDesc.typeName) || "MEDIUMTEXT".equals(typeDesc.typeName))) {
                                        if (collation.indexOf("ucs2") > -1 || collation.indexOf("utf16") > -1) {
                                            mbminlen = 2;
                                        } else if (collation.indexOf("utf32") > -1) {
                                            mbminlen = 4;
                                        }
                                    }
                                    rowVal[6] = mbminlen == 1 ? DatabaseMetaData.this.s2b(typeDesc.columnSize.toString()) : DatabaseMetaData.this.s2b(Integer.valueOf(typeDesc.columnSize / mbminlen).toString());
                                }
                                rowVal[7] = DatabaseMetaData.this.s2b(Integer.toString(typeDesc.bufferLength));
                                rowVal[8] = typeDesc.decimalDigits == null ? null : DatabaseMetaData.this.s2b(typeDesc.decimalDigits.toString());
                                rowVal[9] = DatabaseMetaData.this.s2b(Integer.toString(typeDesc.numPrecRadix));
                                rowVal[10] = DatabaseMetaData.this.s2b(Integer.toString(typeDesc.nullability));
                                try {
                                    rowVal[11] = DatabaseMetaData.this.conn.versionMeetsMinimum(4, 1, 0) ? results.getBytes("Comment") : results.getBytes("Extra");
                                }
                                catch (Exception E) {
                                    rowVal[11] = new byte[0];
                                }
                                rowVal[12] = results.getBytes("Default");
                                rowVal[13] = new byte[]{48};
                                rowVal[14] = new byte[]{48};
                                rowVal[15] = (byte[])(StringUtils.indexOfIgnoreCase(typeDesc.typeName, "CHAR") != -1 || StringUtils.indexOfIgnoreCase(typeDesc.typeName, "BLOB") != -1 || StringUtils.indexOfIgnoreCase(typeDesc.typeName, "TEXT") != -1 || StringUtils.indexOfIgnoreCase(typeDesc.typeName, "BINARY") != -1 ? rowVal[6] : null);
                                if (!fixUpOrdinalsRequired) {
                                    rowVal[16] = Integer.toString(ordPos++).getBytes();
                                } else {
                                    String origColName = results.getString("Field");
                                    Integer realOrdinal = (Integer)ordinalFixUpMap.get(origColName);
                                    if (realOrdinal != null) {
                                        rowVal[16] = realOrdinal.toString().getBytes();
                                    } else {
                                        throw SQLError.createSQLException("Can not find column in full column list to determine true ordinal position.", "S1000", DatabaseMetaData.this.getExceptionInterceptor());
                                    }
                                }
                                rowVal[17] = DatabaseMetaData.this.s2b(typeDesc.isNullable);
                                rowVal[18] = null;
                                rowVal[19] = null;
                                rowVal[20] = null;
                                rowVal[21] = null;
                                rowVal[22] = DatabaseMetaData.this.s2b("");
                                String extra = results.getString("Extra");
                                if (extra != null) {
                                    rowVal[22] = DatabaseMetaData.this.s2b(StringUtils.indexOfIgnoreCase(extra, "auto_increment") != -1 ? "YES" : "NO");
                                    rowVal[23] = DatabaseMetaData.this.s2b(StringUtils.indexOfIgnoreCase(extra, "generated") != -1 ? "YES" : "NO");
                                }
                                rows.add(new ByteArrayRow(rowVal, DatabaseMetaData.this.getExceptionInterceptor()));
                            }
                            var15_26 = null;
                            if (results == null) continue;
                        }
                        catch (Throwable throwable) {
                            var15_26 = null;
                            if (results != null) {
                                try {
                                    results.close();
                                }
                                catch (Exception ex2) {
                                    // empty catch block
                                }
                                results = null;
                            }
                            throw throwable;
                        }
                        try {
                            results.close();
                        }
                        catch (Exception ex2) {
                            // empty catch block
                        }
                        results = null;
                        {
                        }
                    }
                }
            }.doForAll();
            Object var10_9 = null;
            if (stmt == null) return this.buildResultSet(fields, rows);
        }
        catch (Throwable throwable) {
            Object var10_10 = null;
            if (stmt == null) throw throwable;
            stmt.close();
            throw throwable;
        }
        stmt.close();
        return this.buildResultSet(fields, rows);
    }

    protected Field[] createColumnsFields() {
        Field[] fields = new Field[]{new Field("", "TABLE_CAT", 1, 255), new Field("", "TABLE_SCHEM", 1, 0), new Field("", "TABLE_NAME", 1, 255), new Field("", "COLUMN_NAME", 1, 32), new Field("", "DATA_TYPE", 4, 5), new Field("", "TYPE_NAME", 1, 16), new Field("", "COLUMN_SIZE", 4, Integer.toString(Integer.MAX_VALUE).length()), new Field("", "BUFFER_LENGTH", 4, 10), new Field("", "DECIMAL_DIGITS", 4, 10), new Field("", "NUM_PREC_RADIX", 4, 10), new Field("", "NULLABLE", 4, 10), new Field("", "REMARKS", 1, 0), new Field("", "COLUMN_DEF", 1, 0), new Field("", "SQL_DATA_TYPE", 4, 10), new Field("", "SQL_DATETIME_SUB", 4, 10), new Field("", "CHAR_OCTET_LENGTH", 4, Integer.toString(Integer.MAX_VALUE).length()), new Field("", "ORDINAL_POSITION", 4, 10), new Field("", "IS_NULLABLE", 1, 3), new Field("", "SCOPE_CATALOG", 1, 255), new Field("", "SCOPE_SCHEMA", 1, 255), new Field("", "SCOPE_TABLE", 1, 255), new Field("", "SOURCE_DATA_TYPE", 5, 10), new Field("", "IS_AUTOINCREMENT", 1, 3), new Field("", "IS_GENERATEDCOLUMN", 1, 3)};
        return fields;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.conn;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public ResultSet getCrossReference(final String primaryCatalog, final String primarySchema, final String primaryTable, final String foreignCatalog, final String foreignSchema, final String foreignTable) throws SQLException {
        if (primaryTable == null) {
            throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
        }
        Field[] fields = this.createFkMetadataFields();
        final ArrayList<ResultSetRow> tuples = new ArrayList<ResultSetRow>();
        if (!this.conn.versionMeetsMinimum(3, 23, 0)) return this.buildResultSet(fields, tuples);
        final java.sql.Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            new IterateBlock<String>(this.getCatalogIterator(foreignCatalog)){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                void forEach(String catalogStr) throws SQLException {
                    block12: {
                        ResultSet fkresults = null;
                        try {
                            if (DatabaseMetaData.this.conn.versionMeetsMinimum(3, 23, 50)) {
                                fkresults = DatabaseMetaData.this.extractForeignKeyFromCreateTable(catalogStr, null);
                            } else {
                                StringBuilder queryBuf = new StringBuilder("SHOW TABLE STATUS FROM ");
                                queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                fkresults = stmt.executeQuery(queryBuf.toString());
                            }
                            String foreignTableWithCase = DatabaseMetaData.this.getTableNameWithCase(foreignTable);
                            String primaryTableWithCase = DatabaseMetaData.this.getTableNameWithCase(primaryTable);
                            while (fkresults.next()) {
                                String comment;
                                String tableType = fkresults.getString("Type");
                                if (tableType == null || !tableType.equalsIgnoreCase("innodb") && !tableType.equalsIgnoreCase(DatabaseMetaData.SUPPORTS_FK) || (comment = fkresults.getString("Comment").trim()) == null) continue;
                                StringTokenizer commentTokens = new StringTokenizer(comment, ";", false);
                                if (commentTokens.hasMoreTokens()) {
                                    String string = commentTokens.nextToken();
                                }
                                while (commentTokens.hasMoreTokens()) {
                                    String keys = commentTokens.nextToken();
                                    LocalAndReferencedColumns parsedInfo = DatabaseMetaData.this.parseTableStatusIntoLocalAndReferencedColumns(keys);
                                    int keySeq = 0;
                                    Iterator<String> referencingColumns = parsedInfo.localColumnsList.iterator();
                                    Iterator<String> referencedColumns = parsedInfo.referencedColumnsList.iterator();
                                    while (referencingColumns.hasNext()) {
                                        String referencingColumn = StringUtils.unQuoteIdentifier(referencingColumns.next(), DatabaseMetaData.this.quotedId);
                                        byte[][] tuple = new byte[14][];
                                        tuple[4] = foreignCatalog == null ? null : DatabaseMetaData.this.s2b(foreignCatalog);
                                        tuple[5] = foreignSchema == null ? null : DatabaseMetaData.this.s2b(foreignSchema);
                                        String dummy = fkresults.getString("Name");
                                        if (dummy.compareTo(foreignTableWithCase) != 0) continue;
                                        tuple[6] = DatabaseMetaData.this.s2b(dummy);
                                        tuple[7] = DatabaseMetaData.this.s2b(referencingColumn);
                                        tuple[0] = primaryCatalog == null ? null : DatabaseMetaData.this.s2b(primaryCatalog);
                                        byte[] byArray = tuple[1] = primarySchema == null ? null : DatabaseMetaData.this.s2b(primarySchema);
                                        if (parsedInfo.referencedTable.compareTo(primaryTableWithCase) != 0) continue;
                                        tuple[2] = DatabaseMetaData.this.s2b(parsedInfo.referencedTable);
                                        tuple[3] = DatabaseMetaData.this.s2b(StringUtils.unQuoteIdentifier(referencedColumns.next(), DatabaseMetaData.this.quotedId));
                                        tuple[8] = Integer.toString(keySeq).getBytes();
                                        int[] actions = DatabaseMetaData.this.getForeignKeyActions(keys);
                                        tuple[9] = Integer.toString(actions[1]).getBytes();
                                        tuple[10] = Integer.toString(actions[0]).getBytes();
                                        tuple[11] = null;
                                        tuple[12] = null;
                                        tuple[13] = Integer.toString(7).getBytes();
                                        tuples.add(new ByteArrayRow(tuple, DatabaseMetaData.this.getExceptionInterceptor()));
                                        ++keySeq;
                                    }
                                }
                            }
                            Object var18_17 = null;
                            if (fkresults == null) break block12;
                        }
                        catch (Throwable throwable) {
                            Object var18_18 = null;
                            if (fkresults != null) {
                                try {
                                    fkresults.close();
                                }
                                catch (Exception sqlEx) {
                                    AssertionFailedException.shouldNotHappen(sqlEx);
                                }
                                fkresults = null;
                            }
                            throw throwable;
                        }
                        try {
                            fkresults.close();
                        }
                        catch (Exception sqlEx) {
                            AssertionFailedException.shouldNotHappen(sqlEx);
                        }
                        fkresults = null;
                        {
                        }
                    }
                }
            }.doForAll();
            Object var11_10 = null;
            if (stmt == null) return this.buildResultSet(fields, tuples);
        }
        catch (Throwable throwable) {
            Object var11_11 = null;
            if (stmt == null) throw throwable;
            stmt.close();
            throw throwable;
        }
        stmt.close();
        return this.buildResultSet(fields, tuples);
    }

    protected Field[] createFkMetadataFields() {
        Field[] fields = new Field[]{new Field("", "PKTABLE_CAT", 1, 255), new Field("", "PKTABLE_SCHEM", 1, 0), new Field("", "PKTABLE_NAME", 1, 255), new Field("", "PKCOLUMN_NAME", 1, 32), new Field("", "FKTABLE_CAT", 1, 255), new Field("", "FKTABLE_SCHEM", 1, 0), new Field("", "FKTABLE_NAME", 1, 255), new Field("", "FKCOLUMN_NAME", 1, 32), new Field("", "KEY_SEQ", 5, 2), new Field("", "UPDATE_RULE", 5, 2), new Field("", "DELETE_RULE", 5, 2), new Field("", "FK_NAME", 1, 0), new Field("", "PK_NAME", 1, 0), new Field("", "DEFERRABILITY", 5, 2)};
        return fields;
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return this.conn.getServerMajorVersion();
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        return this.conn.getServerMinorVersion();
    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        return "MySQL";
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        return this.conn.getServerVersion();
    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        if (this.conn.supportsIsolationLevel()) {
            return 2;
        }
        return 0;
    }

    @Override
    public int getDriverMajorVersion() {
        return NonRegisteringDriver.getMajorVersionInternal();
    }

    @Override
    public int getDriverMinorVersion() {
        return NonRegisteringDriver.getMinorVersionInternal();
    }

    @Override
    public String getDriverName() throws SQLException {
        return "MySQL Connector Java";
    }

    @Override
    public String getDriverVersion() throws SQLException {
        return "mysql-connector-java-5.1.40 ( Revision: 402933ef52cad9aa82624e80acbea46e3a701ce6 )";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public ResultSet getExportedKeys(String catalog, String schema, final String table) throws SQLException {
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
        }
        Field[] fields = this.createFkMetadataFields();
        final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        if (!this.conn.versionMeetsMinimum(3, 23, 0)) return this.buildResultSet(fields, rows);
        final java.sql.Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            new IterateBlock<String>(this.getCatalogIterator(catalog)){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                void forEach(String catalogStr) throws SQLException {
                    block10: {
                        ResultSet fkresults = null;
                        try {
                            if (DatabaseMetaData.this.conn.versionMeetsMinimum(3, 23, 50)) {
                                fkresults = DatabaseMetaData.this.extractForeignKeyFromCreateTable(catalogStr, null);
                            } else {
                                StringBuilder queryBuf = new StringBuilder("SHOW TABLE STATUS FROM ");
                                queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                fkresults = stmt.executeQuery(queryBuf.toString());
                            }
                            String tableNameWithCase = DatabaseMetaData.this.getTableNameWithCase(table);
                            while (fkresults.next()) {
                                StringTokenizer commentTokens;
                                String comment;
                                String tableType = fkresults.getString("Type");
                                if (tableType == null || !tableType.equalsIgnoreCase("innodb") && !tableType.equalsIgnoreCase(DatabaseMetaData.SUPPORTS_FK) || (comment = fkresults.getString("Comment").trim()) == null || !(commentTokens = new StringTokenizer(comment, ";", false)).hasMoreTokens()) continue;
                                commentTokens.nextToken();
                                while (commentTokens.hasMoreTokens()) {
                                    String keys = commentTokens.nextToken();
                                    DatabaseMetaData.this.getExportKeyResults(catalogStr, tableNameWithCase, keys, rows, fkresults.getString("Name"));
                                }
                            }
                            Object var9_8 = null;
                            if (fkresults == null) break block10;
                        }
                        catch (Throwable throwable) {
                            Object var9_9 = null;
                            if (fkresults != null) {
                                try {
                                    fkresults.close();
                                }
                                catch (SQLException sqlEx) {
                                    AssertionFailedException.shouldNotHappen(sqlEx);
                                }
                                fkresults = null;
                            }
                            throw throwable;
                        }
                        try {
                            fkresults.close();
                        }
                        catch (SQLException sqlEx) {
                            AssertionFailedException.shouldNotHappen(sqlEx);
                        }
                        fkresults = null;
                        {
                        }
                    }
                }
            }.doForAll();
            Object var8_7 = null;
            if (stmt == null) return this.buildResultSet(fields, rows);
        }
        catch (Throwable throwable) {
            Object var8_8 = null;
            if (stmt == null) throw throwable;
            stmt.close();
            throw throwable;
        }
        stmt.close();
        return this.buildResultSet(fields, rows);
    }

    protected void getExportKeyResults(String catalog, String exportingTable, String keysComment, List<ResultSetRow> tuples, String fkTableName) throws SQLException {
        this.getResultsImpl(catalog, exportingTable, keysComment, tuples, fkTableName, true);
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        return "#@";
    }

    protected int[] getForeignKeyActions(String commentString) {
        int[] actions = new int[]{3, 3};
        int lastParenIndex = commentString.lastIndexOf(")");
        if (lastParenIndex != commentString.length() - 1) {
            String cascadeOptions = commentString.substring(lastParenIndex + 1).trim().toUpperCase(Locale.ENGLISH);
            actions[0] = this.getCascadeDeleteOption(cascadeOptions);
            actions[1] = this.getCascadeUpdateOption(cascadeOptions);
        }
        return actions;
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        if (this.conn.supportsQuotedIdentifiers()) {
            return this.conn.useAnsiQuotedIdentifiers() ? "\"" : "`";
        }
        return " ";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public ResultSet getImportedKeys(String catalog, String schema, final String table) throws SQLException {
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
        }
        Field[] fields = this.createFkMetadataFields();
        final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        if (!this.conn.versionMeetsMinimum(3, 23, 0)) return this.buildResultSet(fields, rows);
        final java.sql.Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            new IterateBlock<String>(this.getCatalogIterator(catalog)){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                void forEach(String catalogStr) throws SQLException {
                    block10: {
                        ResultSet fkresults = null;
                        try {
                            if (DatabaseMetaData.this.conn.versionMeetsMinimum(3, 23, 50)) {
                                fkresults = DatabaseMetaData.this.extractForeignKeyFromCreateTable(catalogStr, table);
                            } else {
                                StringBuilder queryBuf = new StringBuilder("SHOW TABLE STATUS ");
                                queryBuf.append(" FROM ");
                                queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                queryBuf.append(" LIKE ");
                                queryBuf.append(StringUtils.quoteIdentifier(table, "'", true));
                                fkresults = stmt.executeQuery(queryBuf.toString());
                            }
                            while (fkresults.next()) {
                                StringTokenizer commentTokens;
                                String comment;
                                String tableType = fkresults.getString("Type");
                                if (tableType == null || !tableType.equalsIgnoreCase("innodb") && !tableType.equalsIgnoreCase(DatabaseMetaData.SUPPORTS_FK) || (comment = fkresults.getString("Comment").trim()) == null || !(commentTokens = new StringTokenizer(comment, ";", false)).hasMoreTokens()) continue;
                                commentTokens.nextToken();
                                while (commentTokens.hasMoreTokens()) {
                                    String keys = commentTokens.nextToken();
                                    DatabaseMetaData.this.getImportKeyResults(catalogStr, table, keys, rows);
                                }
                            }
                            Object var8_7 = null;
                            if (fkresults == null) break block10;
                        }
                        catch (Throwable throwable) {
                            Object var8_8 = null;
                            if (fkresults != null) {
                                try {
                                    fkresults.close();
                                }
                                catch (SQLException sqlEx) {
                                    AssertionFailedException.shouldNotHappen(sqlEx);
                                }
                                fkresults = null;
                            }
                            throw throwable;
                        }
                        try {
                            fkresults.close();
                        }
                        catch (SQLException sqlEx) {
                            AssertionFailedException.shouldNotHappen(sqlEx);
                        }
                        fkresults = null;
                        {
                        }
                    }
                }
            }.doForAll();
            Object var8_7 = null;
            if (stmt == null) return this.buildResultSet(fields, rows);
        }
        catch (Throwable throwable) {
            Object var8_8 = null;
            if (stmt == null) throw throwable;
            stmt.close();
            throw throwable;
        }
        stmt.close();
        return this.buildResultSet(fields, rows);
    }

    protected void getImportKeyResults(String catalog, String importingTable, String keysComment, List<ResultSetRow> tuples) throws SQLException {
        this.getResultsImpl(catalog, importingTable, keysComment, tuples, null, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ResultSet getIndexInfo(String catalog, String schema, final String table, final boolean unique, boolean approximate) throws SQLException {
        ResultSet resultSet;
        block3: {
            Field[] fields = this.createIndexInfoFields();
            final TreeMap sortedRows = new TreeMap();
            ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
            final java.sql.Statement stmt = this.conn.getMetadataSafeStatement();
            try {
                ResultSet indexInfo;
                new IterateBlock<String>(this.getCatalogIterator(catalog)){

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
                    @Override
                    void forEach(String catalogStr) throws SQLException {
                        block12: {
                            ResultSet results = null;
                            try {
                                block11: {
                                    StringBuilder queryBuf = new StringBuilder("SHOW INDEX FROM ");
                                    queryBuf.append(StringUtils.quoteIdentifier(table, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                    queryBuf.append(" FROM ");
                                    queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                    try {
                                        results = stmt.executeQuery(queryBuf.toString());
                                    }
                                    catch (SQLException sqlEx) {
                                        int errorCode = sqlEx.getErrorCode();
                                        if ("42S02".equals(sqlEx.getSQLState()) || errorCode == 1146) break block11;
                                        throw sqlEx;
                                    }
                                }
                                while (results != null && results.next()) {
                                    byte[][] row = new byte[14][];
                                    row[0] = catalogStr == null ? new byte[]{} : DatabaseMetaData.this.s2b(catalogStr);
                                    row[1] = null;
                                    row[2] = results.getBytes("Table");
                                    boolean indexIsUnique = results.getInt("Non_unique") == 0;
                                    row[3] = !indexIsUnique ? DatabaseMetaData.this.s2b("true") : DatabaseMetaData.this.s2b("false");
                                    row[4] = new byte[0];
                                    row[5] = results.getBytes("Key_name");
                                    short indexType = 3;
                                    row[6] = Integer.toString(indexType).getBytes();
                                    row[7] = results.getBytes("Seq_in_index");
                                    row[8] = results.getBytes("Column_name");
                                    row[9] = results.getBytes("Collation");
                                    long cardinality = results.getLong("Cardinality");
                                    if (!Util.isJdbc42() && cardinality > Integer.MAX_VALUE) {
                                        cardinality = Integer.MAX_VALUE;
                                    }
                                    row[10] = DatabaseMetaData.this.s2b(String.valueOf(cardinality));
                                    row[11] = DatabaseMetaData.this.s2b("0");
                                    row[12] = null;
                                    IndexMetaDataKey indexInfoKey = new IndexMetaDataKey(!indexIsUnique, indexType, results.getString("Key_name").toLowerCase(), results.getShort("Seq_in_index"));
                                    if (unique) {
                                        if (!indexIsUnique) continue;
                                        sortedRows.put(indexInfoKey, new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                                        continue;
                                    }
                                    sortedRows.put(indexInfoKey, new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                                }
                                Object var11_10 = null;
                                if (results == null) break block12;
                            }
                            catch (Throwable throwable) {
                                Object var11_11 = null;
                                if (results != null) {
                                    try {
                                        results.close();
                                    }
                                    catch (Exception ex) {
                                        // empty catch block
                                    }
                                    results = null;
                                }
                                throw throwable;
                            }
                            try {
                                results.close();
                            }
                            catch (Exception ex) {
                                // empty catch block
                            }
                            results = null;
                            {
                            }
                        }
                    }
                }.doForAll();
                Iterator sortedRowsIterator = sortedRows.values().iterator();
                while (sortedRowsIterator.hasNext()) {
                    rows.add((ResultSetRow)sortedRowsIterator.next());
                }
                resultSet = indexInfo = this.buildResultSet(fields, rows);
                Object var14_13 = null;
                if (stmt == null) break block3;
            }
            catch (Throwable throwable) {
                block4: {
                    Object var14_14 = null;
                    if (stmt == null) break block4;
                    stmt.close();
                }
                throw throwable;
            }
            stmt.close();
        }
        return resultSet;
    }

    protected Field[] createIndexInfoFields() {
        Field[] fields = new Field[13];
        fields[0] = new Field("", "TABLE_CAT", 1, 255);
        fields[1] = new Field("", "TABLE_SCHEM", 1, 0);
        fields[2] = new Field("", "TABLE_NAME", 1, 255);
        fields[3] = new Field("", "NON_UNIQUE", 16, 4);
        fields[4] = new Field("", "INDEX_QUALIFIER", 1, 1);
        fields[5] = new Field("", "INDEX_NAME", 1, 32);
        fields[6] = new Field("", "TYPE", 5, 32);
        fields[7] = new Field("", "ORDINAL_POSITION", 5, 5);
        fields[8] = new Field("", "COLUMN_NAME", 1, 32);
        fields[9] = new Field("", "ASC_OR_DESC", 1, 1);
        if (Util.isJdbc42()) {
            fields[10] = new Field("", "CARDINALITY", -5, 20);
            fields[11] = new Field("", "PAGES", -5, 20);
        } else {
            fields[10] = new Field("", "CARDINALITY", 4, 20);
            fields[11] = new Field("", "PAGES", 4, 10);
        }
        fields[12] = new Field("", "FILTER_CONDITION", 1, 32);
        return fields;
    }

    @Override
    public int getJDBCMajorVersion() throws SQLException {
        return 4;
    }

    @Override
    public int getJDBCMinorVersion() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        return 0xFFFFF8;
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        return 32;
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        return 0xFFFFF8;
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException {
        return 64;
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        return 64;
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        return 16;
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        return 64;
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        return 256;
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException {
        return 512;
    }

    @Override
    public int getMaxConnections() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException {
        return 64;
    }

    @Override
    public int getMaxIndexLength() throws SQLException {
        return 256;
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxRowSize() throws SQLException {
        return 0x7FFFFFF7;
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxStatementLength() throws SQLException {
        return MysqlIO.getMaxBuf() - 4;
    }

    @Override
    public int getMaxStatements() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxTableNameLength() throws SQLException {
        return 64;
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException {
        return 256;
    }

    @Override
    public int getMaxUserNameLength() throws SQLException {
        return 16;
    }

    @Override
    public String getNumericFunctions() throws SQLException {
        return "ABS,ACOS,ASIN,ATAN,ATAN2,BIT_COUNT,CEILING,COS,COT,DEGREES,EXP,FLOOR,LOG,LOG10,MAX,MIN,MOD,PI,POW,POWER,RADIANS,RAND,ROUND,SIN,SQRT,TAN,TRUNCATE";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, final String table) throws SQLException {
        Field[] fields = new Field[]{new Field("", "TABLE_CAT", 1, 255), new Field("", "TABLE_SCHEM", 1, 0), new Field("", "TABLE_NAME", 1, 255), new Field("", "COLUMN_NAME", 1, 32), new Field("", "KEY_SEQ", 5, 5), new Field("", "PK_NAME", 1, 32)};
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
        }
        final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        final java.sql.Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            new IterateBlock<String>(this.getCatalogIterator(catalog)){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                void forEach(String catalogStr) throws SQLException {
                    block8: {
                        ResultSet rs = null;
                        try {
                            StringBuilder queryBuf = new StringBuilder("SHOW KEYS FROM ");
                            queryBuf.append(StringUtils.quoteIdentifier(table, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                            queryBuf.append(" FROM ");
                            queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                            rs = stmt.executeQuery(queryBuf.toString());
                            TreeMap<String, byte[][]> sortMap = new TreeMap<String, byte[][]>();
                            while (rs.next()) {
                                String keyType = rs.getString("Key_name");
                                if (keyType == null || !keyType.equalsIgnoreCase("PRIMARY") && !keyType.equalsIgnoreCase("PRI")) continue;
                                byte[][] tuple = new byte[6][];
                                tuple[0] = catalogStr == null ? new byte[]{} : DatabaseMetaData.this.s2b(catalogStr);
                                tuple[1] = null;
                                tuple[2] = DatabaseMetaData.this.s2b(table);
                                String columnName = rs.getString("Column_name");
                                tuple[3] = DatabaseMetaData.this.s2b(columnName);
                                tuple[4] = DatabaseMetaData.this.s2b(rs.getString("Seq_in_index"));
                                tuple[5] = DatabaseMetaData.this.s2b(keyType);
                                sortMap.put(columnName, tuple);
                            }
                            Iterator sortedIterator = sortMap.values().iterator();
                            while (sortedIterator.hasNext()) {
                                rows.add(new ByteArrayRow((byte[][])sortedIterator.next(), DatabaseMetaData.this.getExceptionInterceptor()));
                            }
                            Object var9_8 = null;
                            if (rs == null) break block8;
                        }
                        catch (Throwable throwable) {
                            Object var9_9 = null;
                            if (rs != null) {
                                try {
                                    rs.close();
                                }
                                catch (Exception ex) {
                                    // empty catch block
                                }
                                rs = null;
                            }
                            throw throwable;
                        }
                        try {
                            rs.close();
                        }
                        catch (Exception ex) {
                            // empty catch block
                        }
                        rs = null;
                        {
                        }
                    }
                }
            }.doForAll();
            Object var8_7 = null;
            if (stmt == null) return this.buildResultSet(fields, rows);
        }
        catch (Throwable throwable) {
            Object var8_8 = null;
            if (stmt == null) throw throwable;
            stmt.close();
            throw throwable;
        }
        stmt.close();
        return this.buildResultSet(fields, rows);
    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
        Field[] fields = this.createProcedureColumnsFields();
        return this.getProcedureOrFunctionColumns(fields, catalog, schemaPattern, procedureNamePattern, columnNamePattern, true, true);
    }

    protected Field[] createProcedureColumnsFields() {
        Field[] fields = new Field[]{new Field("", "PROCEDURE_CAT", 1, 512), new Field("", "PROCEDURE_SCHEM", 1, 512), new Field("", "PROCEDURE_NAME", 1, 512), new Field("", "COLUMN_NAME", 1, 512), new Field("", "COLUMN_TYPE", 1, 64), new Field("", "DATA_TYPE", 5, 6), new Field("", "TYPE_NAME", 1, 64), new Field("", "PRECISION", 4, 12), new Field("", "LENGTH", 4, 12), new Field("", "SCALE", 5, 12), new Field("", "RADIX", 5, 6), new Field("", "NULLABLE", 5, 6), new Field("", "REMARKS", 1, 512), new Field("", "COLUMN_DEF", 1, 512), new Field("", "SQL_DATA_TYPE", 4, 12), new Field("", "SQL_DATETIME_SUB", 4, 12), new Field("", "CHAR_OCTET_LENGTH", 4, 12), new Field("", "ORDINAL_POSITION", 4, 12), new Field("", "IS_NULLABLE", 1, 512), new Field("", "SPECIFIC_NAME", 1, 512)};
        return fields;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected ResultSet getProcedureOrFunctionColumns(Field[] fields, String catalog, String schemaPattern, String procedureOrFunctionNamePattern, String columnNamePattern, boolean returnProcedures, boolean returnFunctions) throws SQLException {
        ArrayList<ComparableWrapper<String, ProcedureType>> procsOrFuncsToExtractList = new ArrayList<ComparableWrapper<String, ProcedureType>>();
        ResultSet procsAndOrFuncsRs = null;
        if (this.supportsStoredProcedures()) {
            SQLException rethrowSqlEx22;
            block18: {
                try {
                    String tmpProcedureOrFunctionNamePattern = null;
                    if (procedureOrFunctionNamePattern != null && !procedureOrFunctionNamePattern.equals("%")) {
                        tmpProcedureOrFunctionNamePattern = StringUtils.sanitizeProcOrFuncName(procedureOrFunctionNamePattern);
                    }
                    if (tmpProcedureOrFunctionNamePattern == null) {
                        tmpProcedureOrFunctionNamePattern = procedureOrFunctionNamePattern;
                    } else {
                        String tmpCatalog = catalog;
                        List<String> parseList = StringUtils.splitDBdotName(tmpProcedureOrFunctionNamePattern, tmpCatalog, this.quotedId, this.conn.isNoBackslashEscapesSet());
                        if (parseList.size() == 2) {
                            tmpCatalog = parseList.get(0);
                            tmpProcedureOrFunctionNamePattern = parseList.get(1);
                        }
                    }
                    procsAndOrFuncsRs = this.getProceduresAndOrFunctions(this.createFieldMetadataForGetProcedures(), catalog, schemaPattern, tmpProcedureOrFunctionNamePattern, returnProcedures, returnFunctions);
                    boolean hasResults = false;
                    while (procsAndOrFuncsRs.next()) {
                        procsOrFuncsToExtractList.add(new ComparableWrapper<String, ProcedureType>(this.getFullyQualifiedName(procsAndOrFuncsRs.getString(1), procsAndOrFuncsRs.getString(3)), procsAndOrFuncsRs.getShort(8) == 1 ? ProcedureType.PROCEDURE : ProcedureType.FUNCTION));
                        hasResults = true;
                    }
                    if (hasResults) {
                        Collections.sort(procsOrFuncsToExtractList);
                    }
                    Object var14_14 = null;
                    rethrowSqlEx22 = null;
                    if (procsAndOrFuncsRs == null) break block18;
                }
                catch (Throwable throwable) {
                    Object var14_15 = null;
                    SQLException rethrowSqlEx22 = null;
                    if (procsAndOrFuncsRs != null) {
                        try {
                            procsAndOrFuncsRs.close();
                        }
                        catch (SQLException sqlEx) {
                            rethrowSqlEx22 = sqlEx;
                        }
                    }
                    if (rethrowSqlEx22 != null) {
                        throw rethrowSqlEx22;
                    }
                    throw throwable;
                }
                try {
                    procsAndOrFuncsRs.close();
                }
                catch (SQLException sqlEx) {
                    rethrowSqlEx22 = sqlEx;
                }
            }
            if (rethrowSqlEx22 != null) {
                throw rethrowSqlEx22;
            }
        }
        ArrayList<ResultSetRow> resultRows = new ArrayList<ResultSetRow>();
        int idx = 0;
        String procNameToCall = "";
        for (ComparableWrapper comparableWrapper : procsOrFuncsToExtractList) {
            String procName = (String)comparableWrapper.getKey();
            ProcedureType procType = (ProcedureType)((Object)comparableWrapper.getValue());
            idx = !" ".equals(this.quotedId) ? StringUtils.indexOfIgnoreCase(0, procName, ".", this.quotedId, this.quotedId, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL) : procName.indexOf(".");
            if (idx > 0) {
                catalog = StringUtils.unQuoteIdentifier(procName.substring(0, idx), this.quotedId);
                procNameToCall = procName;
            } else {
                procNameToCall = procName;
            }
            this.getCallStmtParameterTypes(catalog, procNameToCall, procType, columnNamePattern, resultRows, fields.length == 17);
        }
        return this.buildResultSet(fields, resultRows);
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
        Field[] fields = this.createFieldMetadataForGetProcedures();
        return this.getProceduresAndOrFunctions(fields, catalog, schemaPattern, procedureNamePattern, true, true);
    }

    protected Field[] createFieldMetadataForGetProcedures() {
        Field[] fields = new Field[]{new Field("", "PROCEDURE_CAT", 1, 255), new Field("", "PROCEDURE_SCHEM", 1, 255), new Field("", "PROCEDURE_NAME", 1, 255), new Field("", "reserved1", 1, 0), new Field("", "reserved2", 1, 0), new Field("", "reserved3", 1, 0), new Field("", "REMARKS", 1, 255), new Field("", "PROCEDURE_TYPE", 5, 6), new Field("", "SPECIFIC_NAME", 1, 255)};
        return fields;
    }

    protected ResultSet getProceduresAndOrFunctions(final Field[] fields, String catalog, String schemaPattern, String procedureNamePattern, final boolean returnProcedures, final boolean returnFunctions) throws SQLException {
        if (procedureNamePattern == null || procedureNamePattern.length() == 0) {
            if (this.conn.getNullNamePatternMatchesAll()) {
                procedureNamePattern = "%";
            } else {
                throw SQLError.createSQLException("Procedure name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
            }
        }
        ArrayList<ResultSetRow> procedureRows = new ArrayList<ResultSetRow>();
        if (this.supportsStoredProcedures()) {
            final String procNamePattern = procedureNamePattern;
            final ArrayList procedureRowsToSort = new ArrayList();
            new IterateBlock<String>(this.getCatalogIterator(catalog)){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                void forEach(String catalogStr) throws SQLException {
                    SQLException sqlEx32;
                    SQLException rethrowSqlEx2;
                    PreparedStatement proceduresStmt;
                    block25: {
                        String db = catalogStr;
                        ResultSet proceduresRs = null;
                        boolean needsClientFiltering = true;
                        StringBuilder selectFromMySQLProcSQL = new StringBuilder();
                        selectFromMySQLProcSQL.append("SELECT name, type, comment FROM mysql.proc WHERE ");
                        if (returnProcedures && !returnFunctions) {
                            selectFromMySQLProcSQL.append("type = 'PROCEDURE' AND ");
                        } else if (!returnProcedures && returnFunctions) {
                            selectFromMySQLProcSQL.append("type = 'FUNCTION' AND ");
                        }
                        selectFromMySQLProcSQL.append("name LIKE ? AND db <=> ? ORDER BY name, type");
                        proceduresStmt = DatabaseMetaData.this.prepareMetaDataSafeStatement(selectFromMySQLProcSQL.toString());
                        try {
                            block24: {
                                if (db != null) {
                                    if (DatabaseMetaData.this.conn.lowerCaseTableNames()) {
                                        db = db.toLowerCase();
                                    }
                                    proceduresStmt.setString(2, db);
                                } else {
                                    proceduresStmt.setNull(2, 12);
                                }
                                int nameIndex = 1;
                                proceduresStmt.setString(1, procNamePattern);
                                try {
                                    proceduresRs = proceduresStmt.executeQuery();
                                    needsClientFiltering = false;
                                    if (returnProcedures) {
                                        DatabaseMetaData.this.convertToJdbcProcedureList(true, db, proceduresRs, needsClientFiltering, db, procedureRowsToSort, nameIndex);
                                    }
                                    if (returnFunctions) {
                                        DatabaseMetaData.this.convertToJdbcFunctionList(db, proceduresRs, needsClientFiltering, db, procedureRowsToSort, nameIndex, fields);
                                    }
                                }
                                catch (SQLException sqlEx2) {
                                    int n = nameIndex = DatabaseMetaData.this.conn.versionMeetsMinimum(5, 0, 1) ? 2 : 1;
                                    if (returnFunctions) {
                                        proceduresStmt.close();
                                        proceduresStmt = DatabaseMetaData.this.prepareMetaDataSafeStatement("SHOW FUNCTION STATUS LIKE ?");
                                        proceduresStmt.setString(1, procNamePattern);
                                        proceduresRs = proceduresStmt.executeQuery();
                                        DatabaseMetaData.this.convertToJdbcFunctionList(db, proceduresRs, needsClientFiltering, db, procedureRowsToSort, nameIndex, fields);
                                    }
                                    if (!returnProcedures) break block24;
                                    proceduresStmt.close();
                                    proceduresStmt = DatabaseMetaData.this.prepareMetaDataSafeStatement("SHOW PROCEDURE STATUS LIKE ?");
                                    proceduresStmt.setString(1, procNamePattern);
                                    proceduresRs = proceduresStmt.executeQuery();
                                    DatabaseMetaData.this.convertToJdbcProcedureList(false, db, proceduresRs, needsClientFiltering, db, procedureRowsToSort, nameIndex);
                                }
                            }
                            Object var10_9 = null;
                            rethrowSqlEx2 = null;
                            if (proceduresRs == null) break block25;
                        }
                        catch (Throwable throwable) {
                            SQLException sqlEx32;
                            Object var10_10 = null;
                            SQLException rethrowSqlEx2 = null;
                            if (proceduresRs != null) {
                                try {
                                    proceduresRs.close();
                                }
                                catch (SQLException sqlEx32) {
                                    rethrowSqlEx2 = sqlEx32;
                                }
                            }
                            if (proceduresStmt != null) {
                                try {
                                    proceduresStmt.close();
                                }
                                catch (SQLException sqlEx32) {
                                    rethrowSqlEx2 = sqlEx32;
                                }
                            }
                            if (rethrowSqlEx2 != null) {
                                throw rethrowSqlEx2;
                            }
                            throw throwable;
                        }
                        try {
                            proceduresRs.close();
                        }
                        catch (SQLException sqlEx32) {
                            rethrowSqlEx2 = sqlEx32;
                        }
                    }
                    if (proceduresStmt != null) {
                        try {
                            proceduresStmt.close();
                        }
                        catch (SQLException sqlEx32) {
                            rethrowSqlEx2 = sqlEx32;
                        }
                    }
                    if (rethrowSqlEx2 != null) {
                        throw rethrowSqlEx2;
                    }
                }
            }.doForAll();
            Collections.sort(procedureRowsToSort);
            for (ComparableWrapper procRow : procedureRowsToSort) {
                procedureRows.add((ResultSetRow)procRow.getValue());
            }
        }
        return this.buildResultSet(fields, procedureRows);
    }

    @Override
    public String getProcedureTerm() throws SQLException {
        return "PROCEDURE";
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return 1;
    }

    private void getResultsImpl(String catalog, String table, String keysComment, List<ResultSetRow> tuples, String fkTableName, boolean isExport) throws SQLException {
        LocalAndReferencedColumns parsedInfo = this.parseTableStatusIntoLocalAndReferencedColumns(keysComment);
        if (isExport && !parsedInfo.referencedTable.equals(table)) {
            return;
        }
        if (parsedInfo.localColumnsList.size() != parsedInfo.referencedColumnsList.size()) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, number of local and referenced columns is not the same.", "S1000", this.getExceptionInterceptor());
        }
        Iterator<String> localColumnNames = parsedInfo.localColumnsList.iterator();
        Iterator<String> referColumnNames = parsedInfo.referencedColumnsList.iterator();
        int keySeqIndex = 1;
        while (localColumnNames.hasNext()) {
            byte[][] tuple = new byte[14][];
            String lColumnName = StringUtils.unQuoteIdentifier(localColumnNames.next(), this.quotedId);
            String rColumnName = StringUtils.unQuoteIdentifier(referColumnNames.next(), this.quotedId);
            tuple[4] = catalog == null ? new byte[]{} : this.s2b(catalog);
            tuple[5] = null;
            tuple[6] = this.s2b(isExport ? fkTableName : table);
            tuple[7] = this.s2b(lColumnName);
            tuple[0] = this.s2b(parsedInfo.referencedCatalog);
            tuple[1] = null;
            tuple[2] = this.s2b(isExport ? table : parsedInfo.referencedTable);
            tuple[3] = this.s2b(rColumnName);
            tuple[8] = this.s2b(Integer.toString(keySeqIndex++));
            int[] actions = this.getForeignKeyActions(keysComment);
            tuple[9] = this.s2b(Integer.toString(actions[1]));
            tuple[10] = this.s2b(Integer.toString(actions[0]));
            tuple[11] = this.s2b(parsedInfo.constraintName);
            tuple[12] = null;
            tuple[13] = this.s2b(Integer.toString(7));
            tuples.add(new ByteArrayRow(tuple, this.getExceptionInterceptor()));
        }
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        Field[] fields = new Field[]{new Field("", "TABLE_SCHEM", 1, 0), new Field("", "TABLE_CATALOG", 1, 0)};
        ArrayList<ResultSetRow> tuples = new ArrayList<ResultSetRow>();
        ResultSet results = this.buildResultSet(fields, tuples);
        return results;
    }

    @Override
    public String getSchemaTerm() throws SQLException {
        return "";
    }

    @Override
    public String getSearchStringEscape() throws SQLException {
        return "\\";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getSQLKeywords() throws SQLException {
        if (mysqlKeywords != null) {
            return mysqlKeywords;
        }
        Class<DatabaseMetaData> clazz = DatabaseMetaData.class;
        synchronized (DatabaseMetaData.class) {
            if (mysqlKeywords != null) {
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return mysqlKeywords;
            }
            TreeSet mysqlKeywordSet = new TreeSet();
            StringBuilder mysqlKeywordsBuffer = new StringBuilder();
            Collections.addAll(mysqlKeywordSet, MYSQL_KEYWORDS);
            mysqlKeywordSet.removeAll(Arrays.asList(Util.isJdbc4() ? SQL2003_KEYWORDS : SQL92_KEYWORDS));
            for (String keyword : mysqlKeywordSet) {
                mysqlKeywordsBuffer.append(",").append(keyword);
            }
            mysqlKeywords = mysqlKeywordsBuffer.substring(1);
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return mysqlKeywords;
        }
    }

    @Override
    public int getSQLStateType() throws SQLException {
        if (this.conn.versionMeetsMinimum(4, 1, 0)) {
            return 2;
        }
        if (this.conn.getUseSqlStateCodes()) {
            return 2;
        }
        return 1;
    }

    @Override
    public String getStringFunctions() throws SQLException {
        return "ASCII,BIN,BIT_LENGTH,CHAR,CHARACTER_LENGTH,CHAR_LENGTH,CONCAT,CONCAT_WS,CONV,ELT,EXPORT_SET,FIELD,FIND_IN_SET,HEX,INSERT,INSTR,LCASE,LEFT,LENGTH,LOAD_FILE,LOCATE,LOCATE,LOWER,LPAD,LTRIM,MAKE_SET,MATCH,MID,OCT,OCTET_LENGTH,ORD,POSITION,QUOTE,REPEAT,REPLACE,REVERSE,RIGHT,RPAD,RTRIM,SOUNDEX,SPACE,STRCMP,SUBSTRING,SUBSTRING,SUBSTRING,SUBSTRING,SUBSTRING_INDEX,TRIM,UCASE,UPPER";
    }

    @Override
    public ResultSet getSuperTables(String arg0, String arg1, String arg2) throws SQLException {
        Field[] fields = new Field[]{new Field("", "TABLE_CAT", 1, 32), new Field("", "TABLE_SCHEM", 1, 32), new Field("", "TABLE_NAME", 1, 32), new Field("", "SUPERTABLE_NAME", 1, 32)};
        return this.buildResultSet(fields, new ArrayList<ResultSetRow>());
    }

    @Override
    public ResultSet getSuperTypes(String arg0, String arg1, String arg2) throws SQLException {
        Field[] fields = new Field[]{new Field("", "TYPE_CAT", 1, 32), new Field("", "TYPE_SCHEM", 1, 32), new Field("", "TYPE_NAME", 1, 32), new Field("", "SUPERTYPE_CAT", 1, 32), new Field("", "SUPERTYPE_SCHEM", 1, 32), new Field("", "SUPERTYPE_NAME", 1, 32)};
        return this.buildResultSet(fields, new ArrayList<ResultSetRow>());
    }

    @Override
    public String getSystemFunctions() throws SQLException {
        return "DATABASE,USER,SYSTEM_USER,SESSION_USER,PASSWORD,ENCRYPT,LAST_INSERT_ID,VERSION";
    }

    protected String getTableNameWithCase(String table) {
        String tableNameWithCase = this.conn.lowerCaseTableNames() ? table.toLowerCase() : table;
        return tableNameWithCase;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        ArrayList<ResultSetRow> grantRows;
        Field[] fields;
        block27: {
            Exception ex32;
            PreparedStatement pStmt;
            block24: {
                if (tableNamePattern == null) {
                    if (this.conn.getNullNamePatternMatchesAll()) {
                        tableNamePattern = "%";
                    } else {
                        throw SQLError.createSQLException("Table name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
                    }
                }
                fields = new Field[]{new Field("", "TABLE_CAT", 1, 64), new Field("", "TABLE_SCHEM", 1, 1), new Field("", "TABLE_NAME", 1, 64), new Field("", "GRANTOR", 1, 77), new Field("", "GRANTEE", 1, 77), new Field("", "PRIVILEGE", 1, 64), new Field("", "IS_GRANTABLE", 1, 3)};
                String grantQuery = "SELECT host,db,table_name,grantor,user,table_priv FROM mysql.tables_priv WHERE db LIKE ? AND table_name LIKE ?";
                ResultSet results = null;
                grantRows = new ArrayList<ResultSetRow>();
                pStmt = null;
                try {
                    pStmt = this.prepareMetaDataSafeStatement(grantQuery);
                    pStmt.setString(1, catalog != null && catalog.length() != 0 ? catalog : "%");
                    pStmt.setString(2, tableNamePattern);
                    results = pStmt.executeQuery();
                    while (results.next()) {
                        String allPrivileges;
                        String host = results.getString(1);
                        String db = results.getString(2);
                        String table = results.getString(3);
                        String grantor = results.getString(4);
                        String user = results.getString(5);
                        if (user == null || user.length() == 0) {
                            user = "%";
                        }
                        StringBuilder fullUser = new StringBuilder(user);
                        if (host != null && this.conn.getUseHostsInPrivileges()) {
                            fullUser.append("@");
                            fullUser.append(host);
                        }
                        if ((allPrivileges = results.getString(6)) == null) continue;
                        allPrivileges = allPrivileges.toUpperCase(Locale.ENGLISH);
                        StringTokenizer st = new StringTokenizer(allPrivileges, ",");
                        while (st.hasMoreTokens()) {
                            Exception ex22;
                            Object var21_20;
                            String privilege = st.nextToken().trim();
                            ResultSet columnResults = null;
                            try {
                                columnResults = this.getColumns(catalog, schemaPattern, table, "%");
                                while (columnResults.next()) {
                                    byte[][] tuple = new byte[8][];
                                    tuple[0] = this.s2b(db);
                                    tuple[1] = null;
                                    tuple[2] = this.s2b(table);
                                    tuple[3] = (byte[])(grantor != null ? this.s2b(grantor) : null);
                                    tuple[4] = this.s2b(fullUser.toString());
                                    tuple[5] = this.s2b(privilege);
                                    tuple[6] = null;
                                    grantRows.add(new ByteArrayRow(tuple, this.getExceptionInterceptor()));
                                }
                                var21_20 = null;
                                if (columnResults == null) continue;
                            }
                            catch (Throwable throwable) {
                                var21_20 = null;
                                if (columnResults != null) {
                                    try {
                                        columnResults.close();
                                    }
                                    catch (Exception ex22) {
                                        // empty catch block
                                    }
                                }
                                throw throwable;
                            }
                            try {
                                columnResults.close();
                            }
                            catch (Exception ex22) {}
                        }
                    }
                    Object var24_23 = null;
                    if (results == null) break block24;
                }
                catch (Throwable throwable) {
                    Exception ex32;
                    Object var24_24 = null;
                    if (results != null) {
                        try {
                            results.close();
                        }
                        catch (Exception ex32) {
                            // empty catch block
                        }
                        results = null;
                    }
                    if (pStmt != null) {
                        try {
                            pStmt.close();
                        }
                        catch (Exception ex32) {
                            // empty catch block
                        }
                        pStmt = null;
                    }
                    throw throwable;
                }
                try {
                    results.close();
                }
                catch (Exception ex32) {
                    // empty catch block
                }
                results = null;
            }
            if (pStmt == null) break block27;
            try {
                pStmt.close();
            }
            catch (Exception ex32) {
                // empty catch block
            }
            pStmt = null;
            {
            }
        }
        return this.buildResultSet(fields, grantRows);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, final String[] types) throws SQLException {
        ArrayList<ResultSetRow> tuples;
        TreeMap sortedRows;
        block7: {
            List<String> parseList;
            if (tableNamePattern == null) {
                if (!this.conn.getNullNamePatternMatchesAll()) {
                    throw SQLError.createSQLException("Table name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
                }
                tableNamePattern = "%";
            }
            sortedRows = new TreeMap();
            tuples = new ArrayList<ResultSetRow>();
            final java.sql.Statement stmt = this.conn.getMetadataSafeStatement();
            String tmpCat = "";
            if (catalog == null || catalog.length() == 0) {
                if (this.conn.getNullCatalogMeansCurrent()) {
                    tmpCat = this.database;
                }
            } else {
                tmpCat = catalog;
            }
            final String tableNamePat = (parseList = StringUtils.splitDBdotName(tableNamePattern, tmpCat, this.quotedId, this.conn.isNoBackslashEscapesSet())).size() == 2 ? parseList.get(1) : tableNamePattern;
            try {
                new IterateBlock<String>(this.getCatalogIterator(catalog)){

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
                    @Override
                    void forEach(String catalogStr) throws SQLException {
                        block36: {
                            boolean operatingOnSystemDB = "information_schema".equalsIgnoreCase(catalogStr) || "mysql".equalsIgnoreCase(catalogStr) || "performance_schema".equalsIgnoreCase(catalogStr);
                            ResultSet results = null;
                            try {
                                try {
                                    results = stmt.executeQuery((!DatabaseMetaData.this.conn.versionMeetsMinimum(5, 0, 2) ? "SHOW TABLES FROM " : "SHOW FULL TABLES FROM ") + StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()) + " LIKE " + StringUtils.quoteIdentifier(tableNamePat, "'", true));
                                }
                                catch (SQLException sqlEx) {
                                    if ("08S01".equals(sqlEx.getSQLState())) {
                                        throw sqlEx;
                                    }
                                    Object var16_6 = null;
                                    if (results != null) {
                                        try {
                                            results.close();
                                        }
                                        catch (Exception ex) {
                                            // empty catch block
                                        }
                                        results = null;
                                    }
                                    return;
                                }
                                boolean shouldReportTables = false;
                                boolean shouldReportViews = false;
                                boolean shouldReportSystemTables = false;
                                boolean shouldReportSystemViews = false;
                                boolean shouldReportLocalTemporaries = false;
                                if (types == null || types.length == 0) {
                                    shouldReportTables = true;
                                    shouldReportViews = true;
                                    shouldReportSystemTables = true;
                                    shouldReportSystemViews = true;
                                    shouldReportLocalTemporaries = true;
                                } else {
                                    for (int i = 0; i < types.length; ++i) {
                                        if (TableType.TABLE.equalsTo(types[i])) {
                                            shouldReportTables = true;
                                            continue;
                                        }
                                        if (TableType.VIEW.equalsTo(types[i])) {
                                            shouldReportViews = true;
                                            continue;
                                        }
                                        if (TableType.SYSTEM_TABLE.equalsTo(types[i])) {
                                            shouldReportSystemTables = true;
                                            continue;
                                        }
                                        if (TableType.SYSTEM_VIEW.equalsTo(types[i])) {
                                            shouldReportSystemViews = true;
                                            continue;
                                        }
                                        if (!TableType.LOCAL_TEMPORARY.equalsTo(types[i])) continue;
                                        shouldReportLocalTemporaries = true;
                                    }
                                }
                                int typeColumnIndex = 1;
                                boolean hasTableTypes = false;
                                if (DatabaseMetaData.this.conn.versionMeetsMinimum(5, 0, 2)) {
                                    try {
                                        typeColumnIndex = results.findColumn("table_type");
                                        hasTableTypes = true;
                                    }
                                    catch (SQLException sqlEx) {
                                        try {
                                            typeColumnIndex = results.findColumn("Type");
                                            hasTableTypes = true;
                                        }
                                        catch (SQLException sqlEx2) {
                                            hasTableTypes = false;
                                        }
                                    }
                                }
                                block20: while (results.next()) {
                                    byte[][] row = new byte[10][];
                                    row[0] = catalogStr == null ? null : DatabaseMetaData.this.s2b(catalogStr);
                                    row[1] = null;
                                    row[2] = results.getBytes(1);
                                    row[4] = new byte[0];
                                    row[5] = null;
                                    row[6] = null;
                                    row[7] = null;
                                    row[8] = null;
                                    row[9] = null;
                                    if (hasTableTypes) {
                                        String tableType = results.getString(typeColumnIndex);
                                        switch (TableType.getTableTypeCompliantWith(tableType)) {
                                            case TABLE: {
                                                boolean reportTable = false;
                                                TableMetaDataKey tablesKey = null;
                                                if (operatingOnSystemDB && shouldReportSystemTables) {
                                                    row[3] = TableType.SYSTEM_TABLE.asBytes();
                                                    tablesKey = new TableMetaDataKey(TableType.SYSTEM_TABLE.getName(), catalogStr, null, results.getString(1));
                                                    reportTable = true;
                                                } else if (!operatingOnSystemDB && shouldReportTables) {
                                                    row[3] = TableType.TABLE.asBytes();
                                                    tablesKey = new TableMetaDataKey(TableType.TABLE.getName(), catalogStr, null, results.getString(1));
                                                    reportTable = true;
                                                }
                                                if (!reportTable) continue block20;
                                                sortedRows.put(tablesKey, new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                                                break;
                                            }
                                            case VIEW: {
                                                if (!shouldReportViews) continue block20;
                                                row[3] = TableType.VIEW.asBytes();
                                                sortedRows.put(new TableMetaDataKey(TableType.VIEW.getName(), catalogStr, null, results.getString(1)), new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                                                break;
                                            }
                                            case SYSTEM_TABLE: {
                                                if (!shouldReportSystemTables) continue block20;
                                                row[3] = TableType.SYSTEM_TABLE.asBytes();
                                                sortedRows.put(new TableMetaDataKey(TableType.SYSTEM_TABLE.getName(), catalogStr, null, results.getString(1)), new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                                                break;
                                            }
                                            case SYSTEM_VIEW: {
                                                if (!shouldReportSystemViews) continue block20;
                                                row[3] = TableType.SYSTEM_VIEW.asBytes();
                                                sortedRows.put(new TableMetaDataKey(TableType.SYSTEM_VIEW.getName(), catalogStr, null, results.getString(1)), new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                                                break;
                                            }
                                            case LOCAL_TEMPORARY: {
                                                if (!shouldReportLocalTemporaries) continue block20;
                                                row[3] = TableType.LOCAL_TEMPORARY.asBytes();
                                                sortedRows.put(new TableMetaDataKey(TableType.LOCAL_TEMPORARY.getName(), catalogStr, null, results.getString(1)), new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                                                break;
                                            }
                                            default: {
                                                row[3] = TableType.TABLE.asBytes();
                                                sortedRows.put(new TableMetaDataKey(TableType.TABLE.getName(), catalogStr, null, results.getString(1)), new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                                                break;
                                            }
                                        }
                                        continue;
                                    }
                                    if (!shouldReportTables) continue;
                                    row[3] = TableType.TABLE.asBytes();
                                    sortedRows.put(new TableMetaDataKey(TableType.TABLE.getName(), catalogStr, null, results.getString(1)), new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                                }
                                Object var16_7 = null;
                                if (results == null) break block36;
                            }
                            catch (Throwable throwable) {
                                Object var16_8 = null;
                                if (results != null) {
                                    try {
                                        results.close();
                                    }
                                    catch (Exception ex) {
                                        // empty catch block
                                    }
                                    results = null;
                                }
                                throw throwable;
                            }
                            try {
                                results.close();
                            }
                            catch (Exception ex) {
                                // empty catch block
                            }
                            results = null;
                            {
                            }
                        }
                    }
                }.doForAll();
                Object var12_11 = null;
                if (stmt == null) break block7;
            }
            catch (Throwable throwable) {
                Object var12_12 = null;
                if (stmt == null) throw throwable;
                stmt.close();
                throw throwable;
            }
            stmt.close();
        }
        tuples.addAll(sortedRows.values());
        return this.buildResultSet(this.createTablesFields(), tuples);
    }

    protected Field[] createTablesFields() {
        Field[] fields = new Field[]{new Field("", "TABLE_CAT", 12, 255), new Field("", "TABLE_SCHEM", 12, 0), new Field("", "TABLE_NAME", 12, 255), new Field("", "TABLE_TYPE", 12, 5), new Field("", "REMARKS", 12, 0), new Field("", "TYPE_CAT", 12, 0), new Field("", "TYPE_SCHEM", 12, 0), new Field("", "TYPE_NAME", 12, 0), new Field("", "SELF_REFERENCING_COL_NAME", 12, 0), new Field("", "REF_GENERATION", 12, 0)};
        return fields;
    }

    @Override
    public ResultSet getTableTypes() throws SQLException {
        ArrayList<ResultSetRow> tuples = new ArrayList<ResultSetRow>();
        Field[] fields = new Field[]{new Field("", "TABLE_TYPE", 12, 256)};
        boolean minVersion5_0_1 = this.conn.versionMeetsMinimum(5, 0, 1);
        tuples.add(new ByteArrayRow(new byte[][]{TableType.LOCAL_TEMPORARY.asBytes()}, this.getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{TableType.SYSTEM_TABLE.asBytes()}, this.getExceptionInterceptor()));
        if (minVersion5_0_1) {
            tuples.add(new ByteArrayRow(new byte[][]{TableType.SYSTEM_VIEW.asBytes()}, this.getExceptionInterceptor()));
        }
        tuples.add(new ByteArrayRow(new byte[][]{TableType.TABLE.asBytes()}, this.getExceptionInterceptor()));
        if (minVersion5_0_1) {
            tuples.add(new ByteArrayRow(new byte[][]{TableType.VIEW.asBytes()}, this.getExceptionInterceptor()));
        }
        return this.buildResultSet(fields, tuples);
    }

    @Override
    public String getTimeDateFunctions() throws SQLException {
        return "DAYOFWEEK,WEEKDAY,DAYOFMONTH,DAYOFYEAR,MONTH,DAYNAME,MONTHNAME,QUARTER,WEEK,YEAR,HOUR,MINUTE,SECOND,PERIOD_ADD,PERIOD_DIFF,TO_DAYS,FROM_DAYS,DATE_FORMAT,TIME_FORMAT,CURDATE,CURRENT_DATE,CURTIME,CURRENT_TIME,NOW,SYSDATE,CURRENT_TIMESTAMP,UNIX_TIMESTAMP,FROM_UNIXTIME,SEC_TO_TIME,TIME_TO_SEC";
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException {
        Field[] fields = new Field[]{new Field("", "TYPE_NAME", 1, 32), new Field("", "DATA_TYPE", 4, 5), new Field("", "PRECISION", 4, 10), new Field("", "LITERAL_PREFIX", 1, 4), new Field("", "LITERAL_SUFFIX", 1, 4), new Field("", "CREATE_PARAMS", 1, 32), new Field("", "NULLABLE", 5, 5), new Field("", "CASE_SENSITIVE", 16, 3), new Field("", "SEARCHABLE", 5, 3), new Field("", "UNSIGNED_ATTRIBUTE", 16, 3), new Field("", "FIXED_PREC_SCALE", 16, 3), new Field("", "AUTO_INCREMENT", 16, 3), new Field("", "LOCAL_TYPE_NAME", 1, 32), new Field("", "MINIMUM_SCALE", 5, 5), new Field("", "MAXIMUM_SCALE", 5, 5), new Field("", "SQL_DATA_TYPE", 4, 10), new Field("", "SQL_DATETIME_SUB", 4, 10), new Field("", "NUM_PREC_RADIX", 4, 10)};
        Object rowVal = null;
        ArrayList<ResultSetRow> tuples = new ArrayList<ResultSetRow>();
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("BIT");
        rowVal[1] = Integer.toString(-7).getBytes();
        rowVal[2] = this.s2b("1");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("true");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("BIT");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("BOOL");
        rowVal[1] = Integer.toString(-7).getBytes();
        rowVal[2] = this.s2b("1");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("true");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("BOOL");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("TINYINT");
        rowVal[1] = Integer.toString(-6).getBytes();
        rowVal[2] = this.s2b("3");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M)] [UNSIGNED] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("true");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("TINYINT");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("TINYINT UNSIGNED");
        rowVal[1] = Integer.toString(-6).getBytes();
        rowVal[2] = this.s2b("3");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M)] [UNSIGNED] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("true");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("TINYINT UNSIGNED");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("BIGINT");
        rowVal[1] = Integer.toString(-5).getBytes();
        rowVal[2] = this.s2b("19");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M)] [UNSIGNED] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("true");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("BIGINT");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("BIGINT UNSIGNED");
        rowVal[1] = Integer.toString(-5).getBytes();
        rowVal[2] = this.s2b("20");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M)] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("true");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("BIGINT UNSIGNED");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("LONG VARBINARY");
        rowVal[1] = Integer.toString(-4).getBytes();
        rowVal[2] = this.s2b("16777215");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("true");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("LONG VARBINARY");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("MEDIUMBLOB");
        rowVal[1] = Integer.toString(-4).getBytes();
        rowVal[2] = this.s2b("16777215");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("true");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("MEDIUMBLOB");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("LONGBLOB");
        rowVal[1] = Integer.toString(-4).getBytes();
        rowVal[2] = Integer.toString(Integer.MAX_VALUE).getBytes();
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("true");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("LONGBLOB");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("BLOB");
        rowVal[1] = Integer.toString(-4).getBytes();
        rowVal[2] = this.s2b("65535");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("true");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("BLOB");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("TINYBLOB");
        rowVal[1] = Integer.toString(-4).getBytes();
        rowVal[2] = this.s2b("255");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("true");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("TINYBLOB");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("VARBINARY");
        rowVal[1] = Integer.toString(-3).getBytes();
        rowVal[2] = this.s2b(this.conn.versionMeetsMinimum(5, 0, 3) ? "65535" : "255");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("(M)");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("true");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("VARBINARY");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("BINARY");
        rowVal[1] = Integer.toString(-2).getBytes();
        rowVal[2] = this.s2b("255");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("(M)");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("true");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("BINARY");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("LONG VARCHAR");
        rowVal[1] = Integer.toString(-1).getBytes();
        rowVal[2] = this.s2b("16777215");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("LONG VARCHAR");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("MEDIUMTEXT");
        rowVal[1] = Integer.toString(-1).getBytes();
        rowVal[2] = this.s2b("16777215");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("MEDIUMTEXT");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("LONGTEXT");
        rowVal[1] = Integer.toString(-1).getBytes();
        rowVal[2] = Integer.toString(Integer.MAX_VALUE).getBytes();
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("LONGTEXT");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("TEXT");
        rowVal[1] = Integer.toString(-1).getBytes();
        rowVal[2] = this.s2b("65535");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("TEXT");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("TINYTEXT");
        rowVal[1] = Integer.toString(-1).getBytes();
        rowVal[2] = this.s2b("255");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("TINYTEXT");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("CHAR");
        rowVal[1] = Integer.toString(1).getBytes();
        rowVal[2] = this.s2b("255");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("(M)");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("CHAR");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        int decimalPrecision = 254;
        if (this.conn.versionMeetsMinimum(5, 0, 3)) {
            decimalPrecision = this.conn.versionMeetsMinimum(5, 0, 6) ? 65 : 64;
        }
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("NUMERIC");
        rowVal[1] = Integer.toString(2).getBytes();
        rowVal[2] = this.s2b(String.valueOf(decimalPrecision));
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M[,D])] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("NUMERIC");
        rowVal[13] = this.s2b("-308");
        rowVal[14] = this.s2b("308");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("DECIMAL");
        rowVal[1] = Integer.toString(3).getBytes();
        rowVal[2] = this.s2b(String.valueOf(decimalPrecision));
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M[,D])] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("DECIMAL");
        rowVal[13] = this.s2b("-308");
        rowVal[14] = this.s2b("308");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("INTEGER");
        rowVal[1] = Integer.toString(4).getBytes();
        rowVal[2] = this.s2b("10");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M)] [UNSIGNED] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("true");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("INTEGER");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("INTEGER UNSIGNED");
        rowVal[1] = Integer.toString(4).getBytes();
        rowVal[2] = this.s2b("10");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M)] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("true");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("INTEGER UNSIGNED");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("INT");
        rowVal[1] = Integer.toString(4).getBytes();
        rowVal[2] = this.s2b("10");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M)] [UNSIGNED] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("true");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("INT");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("INT UNSIGNED");
        rowVal[1] = Integer.toString(4).getBytes();
        rowVal[2] = this.s2b("10");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M)] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("true");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("INT UNSIGNED");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("MEDIUMINT");
        rowVal[1] = Integer.toString(4).getBytes();
        rowVal[2] = this.s2b("7");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M)] [UNSIGNED] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("true");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("MEDIUMINT");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("MEDIUMINT UNSIGNED");
        rowVal[1] = Integer.toString(4).getBytes();
        rowVal[2] = this.s2b("8");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M)] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("true");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("MEDIUMINT UNSIGNED");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("SMALLINT");
        rowVal[1] = Integer.toString(5).getBytes();
        rowVal[2] = this.s2b("5");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M)] [UNSIGNED] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("true");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("SMALLINT");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("SMALLINT UNSIGNED");
        rowVal[1] = Integer.toString(5).getBytes();
        rowVal[2] = this.s2b("5");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M)] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("true");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("SMALLINT UNSIGNED");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("FLOAT");
        rowVal[1] = Integer.toString(7).getBytes();
        rowVal[2] = this.s2b("10");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M,D)] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("FLOAT");
        rowVal[13] = this.s2b("-38");
        rowVal[14] = this.s2b("38");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("DOUBLE");
        rowVal[1] = Integer.toString(8).getBytes();
        rowVal[2] = this.s2b("17");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M,D)] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("DOUBLE");
        rowVal[13] = this.s2b("-308");
        rowVal[14] = this.s2b("308");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("DOUBLE PRECISION");
        rowVal[1] = Integer.toString(8).getBytes();
        rowVal[2] = this.s2b("17");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M,D)] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("DOUBLE PRECISION");
        rowVal[13] = this.s2b("-308");
        rowVal[14] = this.s2b("308");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("REAL");
        rowVal[1] = Integer.toString(8).getBytes();
        rowVal[2] = this.s2b("17");
        rowVal[3] = this.s2b("");
        rowVal[4] = this.s2b("");
        rowVal[5] = this.s2b("[(M,D)] [ZEROFILL]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("true");
        rowVal[12] = this.s2b("REAL");
        rowVal[13] = this.s2b("-308");
        rowVal[14] = this.s2b("308");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("VARCHAR");
        rowVal[1] = Integer.toString(12).getBytes();
        rowVal[2] = this.s2b(this.conn.versionMeetsMinimum(5, 0, 3) ? "65535" : "255");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("(M)");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("VARCHAR");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("ENUM");
        rowVal[1] = Integer.toString(12).getBytes();
        rowVal[2] = this.s2b("65535");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("ENUM");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("SET");
        rowVal[1] = Integer.toString(12).getBytes();
        rowVal[2] = this.s2b("64");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("SET");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("DATE");
        rowVal[1] = Integer.toString(91).getBytes();
        rowVal[2] = this.s2b("0");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("DATE");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("TIME");
        rowVal[1] = Integer.toString(92).getBytes();
        rowVal[2] = this.s2b("0");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("TIME");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("DATETIME");
        rowVal[1] = Integer.toString(93).getBytes();
        rowVal[2] = this.s2b("0");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("DATETIME");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = this.s2b("TIMESTAMP");
        rowVal[1] = Integer.toString(93).getBytes();
        rowVal[2] = this.s2b("0");
        rowVal[3] = this.s2b("'");
        rowVal[4] = this.s2b("'");
        rowVal[5] = this.s2b("[(M)]");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = this.s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = this.s2b("false");
        rowVal[10] = this.s2b("false");
        rowVal[11] = this.s2b("false");
        rowVal[12] = this.s2b("TIMESTAMP");
        rowVal[13] = this.s2b("0");
        rowVal[14] = this.s2b("0");
        rowVal[15] = this.s2b("0");
        rowVal[16] = this.s2b("0");
        rowVal[17] = this.s2b("10");
        tuples.add(new ByteArrayRow((byte[][])rowVal, this.getExceptionInterceptor()));
        return this.buildResultSet(fields, tuples);
    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
        Field[] fields = new Field[]{new Field("", "TYPE_CAT", 12, 32), new Field("", "TYPE_SCHEM", 12, 32), new Field("", "TYPE_NAME", 12, 32), new Field("", "CLASS_NAME", 12, 32), new Field("", "DATA_TYPE", 4, 10), new Field("", "REMARKS", 12, 32), new Field("", "BASE_TYPE", 5, 10)};
        ArrayList<ResultSetRow> tuples = new ArrayList<ResultSetRow>();
        return this.buildResultSet(fields, tuples);
    }

    @Override
    public String getURL() throws SQLException {
        return this.conn.getURL();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getUserName() throws SQLException {
        if (this.conn.getUseHostsInPrivileges()) {
            Exception ex22;
            String string;
            java.sql.Statement stmt;
            block12: {
                stmt = null;
                ResultSet rs = null;
                try {
                    stmt = this.conn.getMetadataSafeStatement();
                    rs = stmt.executeQuery("SELECT USER()");
                    rs.next();
                    string = rs.getString(1);
                    Object var5_4 = null;
                    if (rs == null) break block12;
                }
                catch (Throwable throwable) {
                    Exception ex22;
                    Object var5_5 = null;
                    if (rs != null) {
                        try {
                            rs.close();
                        }
                        catch (Exception ex22) {
                            AssertionFailedException.shouldNotHappen(ex22);
                        }
                        rs = null;
                    }
                    if (stmt != null) {
                        try {
                            stmt.close();
                        }
                        catch (Exception ex22) {
                            AssertionFailedException.shouldNotHappen(ex22);
                        }
                        stmt = null;
                    }
                    throw throwable;
                }
                try {
                    rs.close();
                }
                catch (Exception ex22) {
                    AssertionFailedException.shouldNotHappen(ex22);
                }
                rs = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (Exception ex22) {
                    AssertionFailedException.shouldNotHappen(ex22);
                }
                stmt = null;
            }
            return string;
        }
        return this.conn.getUser();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public ResultSet getVersionColumns(String catalog, String schema, final String table) throws SQLException {
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
        }
        Field[] fields = new Field[]{new Field("", "SCOPE", 5, 5), new Field("", "COLUMN_NAME", 1, 32), new Field("", "DATA_TYPE", 4, 5), new Field("", "TYPE_NAME", 1, 16), new Field("", "COLUMN_SIZE", 4, 16), new Field("", "BUFFER_LENGTH", 4, 16), new Field("", "DECIMAL_DIGITS", 5, 16), new Field("", "PSEUDO_COLUMN", 5, 5)};
        final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        final java.sql.Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            new IterateBlock<String>(this.getCatalogIterator(catalog)){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 * Loose catch block
                 */
                @Override
                void forEach(String catalogStr) throws SQLException {
                    block20: {
                        ResultSet results = null;
                        boolean with_where = DatabaseMetaData.this.conn.versionMeetsMinimum(5, 0, 0);
                        StringBuilder whereBuf = new StringBuilder(" Extra LIKE '%on update CURRENT_TIMESTAMP%'");
                        ArrayList<String> rsFields = new ArrayList<String>();
                        if (!DatabaseMetaData.this.conn.versionMeetsMinimum(5, 1, 23)) {
                            whereBuf = new StringBuilder();
                            boolean firstTime = true;
                            String query = "SHOW CREATE TABLE " + DatabaseMetaData.this.getFullyQualifiedName(catalogStr, table);
                            results = stmt.executeQuery(query);
                            while (results.next()) {
                                String createTableString = results.getString(2);
                                StringTokenizer lineTokenizer = new StringTokenizer(createTableString, "\n");
                                while (lineTokenizer.hasMoreTokens()) {
                                    String line = lineTokenizer.nextToken().trim();
                                    if (StringUtils.indexOfIgnoreCase(line, "on update CURRENT_TIMESTAMP") <= -1) continue;
                                    boolean usingBackTicks = true;
                                    int beginPos = line.indexOf(DatabaseMetaData.this.quotedId);
                                    if (beginPos == -1) {
                                        beginPos = line.indexOf("\"");
                                        usingBackTicks = false;
                                    }
                                    if (beginPos == -1) continue;
                                    int endPos = -1;
                                    endPos = usingBackTicks ? line.indexOf(DatabaseMetaData.this.quotedId, beginPos + 1) : line.indexOf("\"", beginPos + 1);
                                    if (endPos == -1) continue;
                                    if (with_where) {
                                        if (!firstTime) {
                                            whereBuf.append(" or");
                                        } else {
                                            firstTime = false;
                                        }
                                        whereBuf.append(" Field='");
                                        whereBuf.append(line.substring(beginPos + 1, endPos));
                                        whereBuf.append("'");
                                        continue;
                                    }
                                    rsFields.add(line.substring(beginPos + 1, endPos));
                                }
                            }
                        }
                        if (whereBuf.length() > 0 || rsFields.size() > 0) {
                            StringBuilder queryBuf = new StringBuilder("SHOW COLUMNS FROM ");
                            queryBuf.append(StringUtils.quoteIdentifier(table, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                            queryBuf.append(" FROM ");
                            queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                            if (with_where) {
                                queryBuf.append(" WHERE");
                                queryBuf.append(whereBuf.toString());
                            }
                            results = stmt.executeQuery(queryBuf.toString());
                            while (results.next()) {
                                if (!with_where && !rsFields.contains(results.getString("Field"))) continue;
                                TypeDescriptor typeDesc = new TypeDescriptor(results.getString("Type"), results.getString("Null"));
                                byte[][] rowVal = new byte[][]{null, results.getBytes("Field"), Short.toString(typeDesc.dataType).getBytes(), DatabaseMetaData.this.s2b(typeDesc.typeName), typeDesc.columnSize == null ? null : DatabaseMetaData.this.s2b(typeDesc.columnSize.toString()), DatabaseMetaData.this.s2b(Integer.toString(typeDesc.bufferLength)), typeDesc.decimalDigits == null ? null : DatabaseMetaData.this.s2b(typeDesc.decimalDigits.toString()), Integer.toString(1).getBytes()};
                                rows.add(new ByteArrayRow(rowVal, DatabaseMetaData.this.getExceptionInterceptor()));
                            }
                        }
                        Object var15_16 = null;
                        if (results == null) break block20;
                        try {
                            results.close();
                        }
                        catch (Exception ex) {
                            // empty catch block
                        }
                        results = null;
                        {
                            break block20;
                            catch (SQLException sqlEx) {
                                if (!"42S02".equals(sqlEx.getSQLState())) {
                                    throw sqlEx;
                                }
                                Object var15_17 = null;
                                if (results == null) break block20;
                                try {
                                    results.close();
                                }
                                catch (Exception ex) {
                                    // empty catch block
                                }
                                results = null;
                            }
                        }
                        catch (Throwable throwable) {
                            Object var15_18 = null;
                            if (results != null) {
                                try {
                                    results.close();
                                }
                                catch (Exception ex) {
                                    // empty catch block
                                }
                                results = null;
                            }
                            throw throwable;
                        }
                    }
                }
            }.doForAll();
            Object var8_7 = null;
            if (stmt == null) return this.buildResultSet(fields, rows);
        }
        catch (Throwable throwable) {
            Object var8_8 = null;
            if (stmt == null) throw throwable;
            stmt.close();
            throw throwable;
        }
        stmt.close();
        return this.buildResultSet(fields, rows);
    }

    @Override
    public boolean insertsAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException {
        return true;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return false;
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        return !this.conn.getEmulateLocators();
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        return true;
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        return false;
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 0, 2) && !this.conn.versionMeetsMinimum(4, 0, 11);
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        return false;
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        return !this.nullsAreSortedHigh();
    }

    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    protected LocalAndReferencedColumns parseTableStatusIntoLocalAndReferencedColumns(String keysComment) throws SQLException {
        String columnsDelimitter = ",";
        int indexOfOpenParenLocalColumns = StringUtils.indexOfIgnoreCase(0, keysComment, "(", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
        if (indexOfOpenParenLocalColumns == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find start of local columns list.", "S1000", this.getExceptionInterceptor());
        }
        String constraintName = StringUtils.unQuoteIdentifier(keysComment.substring(0, indexOfOpenParenLocalColumns).trim(), this.quotedId);
        String keysCommentTrimmed = (keysComment = keysComment.substring(indexOfOpenParenLocalColumns, keysComment.length())).trim();
        int indexOfCloseParenLocalColumns = StringUtils.indexOfIgnoreCase(0, keysCommentTrimmed, ")", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
        if (indexOfCloseParenLocalColumns == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find end of local columns list.", "S1000", this.getExceptionInterceptor());
        }
        String localColumnNamesString = keysCommentTrimmed.substring(1, indexOfCloseParenLocalColumns);
        int indexOfRefer = StringUtils.indexOfIgnoreCase(0, keysCommentTrimmed, "REFER ", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
        if (indexOfRefer == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find start of referenced tables list.", "S1000", this.getExceptionInterceptor());
        }
        int indexOfOpenParenReferCol = StringUtils.indexOfIgnoreCase(indexOfRefer, keysCommentTrimmed, "(", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__MRK_COM_WS);
        if (indexOfOpenParenReferCol == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find start of referenced columns list.", "S1000", this.getExceptionInterceptor());
        }
        String referCatalogTableString = keysCommentTrimmed.substring(indexOfRefer + "REFER ".length(), indexOfOpenParenReferCol);
        int indexOfSlash = StringUtils.indexOfIgnoreCase(0, referCatalogTableString, "/", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__MRK_COM_WS);
        if (indexOfSlash == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find name of referenced catalog.", "S1000", this.getExceptionInterceptor());
        }
        String referCatalog = StringUtils.unQuoteIdentifier(referCatalogTableString.substring(0, indexOfSlash), this.quotedId);
        String referTable = StringUtils.unQuoteIdentifier(referCatalogTableString.substring(indexOfSlash + 1).trim(), this.quotedId);
        int indexOfCloseParenRefer = StringUtils.indexOfIgnoreCase(indexOfOpenParenReferCol, keysCommentTrimmed, ")", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
        if (indexOfCloseParenRefer == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find end of referenced columns list.", "S1000", this.getExceptionInterceptor());
        }
        String referColumnNamesString = keysCommentTrimmed.substring(indexOfOpenParenReferCol + 1, indexOfCloseParenRefer);
        List<String> referColumnsList = StringUtils.split(referColumnNamesString, columnsDelimitter, this.quotedId, this.quotedId, false);
        List<String> localColumnsList = StringUtils.split(localColumnNamesString, columnsDelimitter, this.quotedId, this.quotedId, false);
        return new LocalAndReferencedColumns(localColumnsList, referColumnsList, constraintName, referCatalog, referTable);
    }

    protected byte[] s2b(String s) throws SQLException {
        if (s == null) {
            return null;
        }
        return StringUtils.getBytes(s, this.conn.getCharacterSetMetadata(), this.conn.getServerCharset(), this.conn.parserKnowsUnicode(), this.conn, this.getExceptionInterceptor());
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return this.conn.storesLowerCaseTableName();
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return this.conn.storesLowerCaseTableName();
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return !this.conn.storesLowerCaseTableName();
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return !this.conn.storesLowerCaseTableName();
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return this.conn.versionMeetsMinimum(3, 22, 0);
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return this.conn.versionMeetsMinimum(3, 22, 0);
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return this.conn.versionMeetsMinimum(3, 22, 0);
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return this.conn.versionMeetsMinimum(3, 22, 0);
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return this.conn.versionMeetsMinimum(3, 22, 0);
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsConvert() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        switch (fromType) {
            case -4: 
            case -3: 
            case -2: 
            case -1: 
            case 1: 
            case 12: {
                switch (toType) {
                    case -6: 
                    case -5: 
                    case -4: 
                    case -3: 
                    case -2: 
                    case -1: 
                    case 1: 
                    case 2: 
                    case 3: 
                    case 4: 
                    case 5: 
                    case 6: 
                    case 7: 
                    case 8: 
                    case 12: 
                    case 91: 
                    case 92: 
                    case 93: 
                    case 1111: {
                        return true;
                    }
                }
                return false;
            }
            case -7: {
                return false;
            }
            case -6: 
            case -5: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 7: 
            case 8: {
                switch (toType) {
                    case -6: 
                    case -5: 
                    case -4: 
                    case -3: 
                    case -2: 
                    case -1: 
                    case 1: 
                    case 2: 
                    case 3: 
                    case 4: 
                    case 5: 
                    case 6: 
                    case 7: 
                    case 8: 
                    case 12: {
                        return true;
                    }
                }
                return false;
            }
            case 0: {
                return false;
            }
            case 1111: {
                switch (toType) {
                    case -4: 
                    case -3: 
                    case -2: 
                    case -1: 
                    case 1: 
                    case 12: {
                        return true;
                    }
                }
                return false;
            }
            case 91: {
                switch (toType) {
                    case -4: 
                    case -3: 
                    case -2: 
                    case -1: 
                    case 1: 
                    case 12: {
                        return true;
                    }
                }
                return false;
            }
            case 92: {
                switch (toType) {
                    case -4: 
                    case -3: 
                    case -2: 
                    case -1: 
                    case 1: 
                    case 12: {
                        return true;
                    }
                }
                return false;
            }
            case 93: {
                switch (toType) {
                    case -4: 
                    case -3: 
                    case -2: 
                    case -1: 
                    case 1: 
                    case 12: 
                    case 91: 
                    case 92: {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsGetGeneratedKeys() {
        return true;
    }

    @Override
    public boolean supportsGroupBy() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return this.conn.getOverrideSupportsIntegrityEnhancementFacility();
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return !this.conn.lowerCaseTableNames();
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return !this.conn.lowerCaseTableNames();
    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsNamedParameters() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        switch (type) {
            case 1004: {
                if (concurrency == 1007 || concurrency == 1008) {
                    return true;
                }
                throw SQLError.createSQLException("Illegal arguments to supportsResultSetConcurrency()", "S1009", this.getExceptionInterceptor());
            }
            case 1003: {
                if (concurrency == 1007 || concurrency == 1008) {
                    return true;
                }
                throw SQLError.createSQLException("Illegal arguments to supportsResultSetConcurrency()", "S1009", this.getExceptionInterceptor());
            }
            case 1005: {
                return false;
            }
        }
        throw SQLError.createSQLException("Illegal arguments to supportsResultSetConcurrency()", "S1009", this.getExceptionInterceptor());
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        return holdability == 1;
    }

    @Override
    public boolean supportsResultSetType(int type) throws SQLException {
        return type == 1004;
    }

    @Override
    public boolean supportsSavepoints() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 0, 14) || this.conn.versionMeetsMinimum(4, 1, 1);
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 0, 0);
    }

    @Override
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        return this.conn.versionMeetsMinimum(5, 0, 0);
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        if (this.conn.supportsIsolationLevel()) {
            switch (level) {
                case 1: 
                case 2: 
                case 4: 
                case 8: {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        return this.conn.supportsTransactions();
    }

    @Override
    public boolean supportsUnion() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 0, 0);
    }

    @Override
    public boolean supportsUnionAll() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 0, 0);
    }

    @Override
    public boolean updatesAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        return false;
    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        Field[] fields = new Field[]{new Field("", "NAME", 12, 255), new Field("", "MAX_LEN", 4, 10), new Field("", "DEFAULT_VALUE", 12, 255), new Field("", "DESCRIPTION", 12, 255)};
        return DatabaseMetaData.buildResultSet(fields, new ArrayList<ResultSetRow>(), this.conn);
    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
        Field[] fields = this.createFunctionColumnsFields();
        return this.getProcedureOrFunctionColumns(fields, catalog, schemaPattern, functionNamePattern, columnNamePattern, false, true);
    }

    protected Field[] createFunctionColumnsFields() {
        Field[] fields = new Field[]{new Field("", "FUNCTION_CAT", 12, 512), new Field("", "FUNCTION_SCHEM", 12, 512), new Field("", "FUNCTION_NAME", 12, 512), new Field("", "COLUMN_NAME", 12, 512), new Field("", "COLUMN_TYPE", 12, 64), new Field("", "DATA_TYPE", 5, 6), new Field("", "TYPE_NAME", 12, 64), new Field("", "PRECISION", 4, 12), new Field("", "LENGTH", 4, 12), new Field("", "SCALE", 5, 12), new Field("", "RADIX", 5, 6), new Field("", "NULLABLE", 5, 6), new Field("", "REMARKS", 12, 512), new Field("", "CHAR_OCTET_LENGTH", 4, 32), new Field("", "ORDINAL_POSITION", 4, 32), new Field("", "IS_NULLABLE", 12, 12), new Field("", "SPECIFIC_NAME", 12, 64)};
        return fields;
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        Field[] fields = new Field[]{new Field("", "FUNCTION_CAT", 1, 255), new Field("", "FUNCTION_SCHEM", 1, 255), new Field("", "FUNCTION_NAME", 1, 255), new Field("", "REMARKS", 1, 255), new Field("", "FUNCTION_TYPE", 5, 6), new Field("", "SPECIFIC_NAME", 1, 255)};
        return this.getProceduresAndOrFunctions(fields, catalog, schemaPattern, functionNamePattern, false, true);
    }

    public boolean providesQueryObjectGenerator() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        Field[] fields = new Field[]{new Field("", "TABLE_SCHEM", 12, 255), new Field("", "TABLE_CATALOG", 12, 255)};
        return this.buildResultSet(fields, new ArrayList<ResultSetRow>());
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return true;
    }

    protected PreparedStatement prepareMetaDataSafeStatement(String sql) throws SQLException {
        PreparedStatement pStmt = this.conn.clientPrepareStatement(sql);
        if (pStmt.getMaxRows() != 0) {
            pStmt.setMaxRows(0);
        }
        ((Statement)((Object)pStmt)).setHoldResultsOpenOverClose(true);
        return pStmt;
    }

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        Field[] fields = new Field[]{new Field("", "TABLE_CAT", 12, 512), new Field("", "TABLE_SCHEM", 12, 512), new Field("", "TABLE_NAME", 12, 512), new Field("", "COLUMN_NAME", 12, 512), new Field("", "DATA_TYPE", 4, 12), new Field("", "COLUMN_SIZE", 4, 12), new Field("", "DECIMAL_DIGITS", 4, 12), new Field("", "NUM_PREC_RADIX", 4, 12), new Field("", "COLUMN_USAGE", 12, 512), new Field("", "REMARKS", 12, 512), new Field("", "CHAR_OCTET_LENGTH", 4, 12), new Field("", "IS_NULLABLE", 12, 512)};
        return this.buildResultSet(fields, new ArrayList<ResultSetRow>());
    }

    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return true;
    }

    static {
        if (Util.isJdbc4()) {
            try {
                JDBC_4_DBMD_SHOW_CTOR = Class.forName("com.mysql.jdbc.JDBC4DatabaseMetaData").getConstructor(MySQLConnection.class, String.class);
                JDBC_4_DBMD_IS_CTOR = Class.forName("com.mysql.jdbc.JDBC4DatabaseMetaDataUsingInfoSchema").getConstructor(MySQLConnection.class, String.class);
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
            JDBC_4_DBMD_IS_CTOR = null;
            JDBC_4_DBMD_SHOW_CTOR = null;
        }
        MYSQL_KEYWORDS = new String[]{"ACCESSIBLE", "ADD", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC", "ASENSITIVE", "BEFORE", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOTH", "BY", "CALL", "CASCADE", "CASE", "CHANGE", "CHAR", "CHARACTER", "CHECK", "COLLATE", "COLUMN", "CONDITION", "CONSTRAINT", "CONTINUE", "CONVERT", "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATABASE", "DATABASES", "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELAYED", "DELETE", "DESC", "DESCRIBE", "DETERMINISTIC", "DISTINCT", "DISTINCTROW", "DIV", "DOUBLE", "DROP", "DUAL", "EACH", "ELSE", "ELSEIF", "ENCLOSED", "ESCAPED", "EXISTS", "EXIT", "EXPLAIN", "FALSE", "FETCH", "FLOAT", "FLOAT4", "FLOAT8", "FOR", "FORCE", "FOREIGN", "FROM", "FULLTEXT", "GENERATED", "GET", "GRANT", "GROUP", "HAVING", "HIGH_PRIORITY", "HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND", "IF", "IGNORE", "IN", "INDEX", "INFILE", "INNER", "INOUT", "INSENSITIVE", "INSERT", "INT", "INT1", "INT2", "INT3", "INT4", "INT8", "INTEGER", "INTERVAL", "INTO", "IO_AFTER_GTIDS", "IO_BEFORE_GTIDS", "IS", "ITERATE", "JOIN", "KEY", "KEYS", "KILL", "LEADING", "LEAVE", "LEFT", "LIKE", "LIMIT", "LINEAR", "LINES", "LOAD", "LOCALTIME", "LOCALTIMESTAMP", "LOCK", "LONG", "LONGBLOB", "LONGTEXT", "LOOP", "LOW_PRIORITY", "MASTER_BIND", "MASTER_SSL_VERIFY_SERVER_CERT", "MATCH", "MAXVALUE", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MIDDLEINT", "MINUTE_MICROSECOND", "MINUTE_SECOND", "MOD", "MODIFIES", "NATURAL", "NOT", "NO_WRITE_TO_BINLOG", "NULL", "NUMERIC", "ON", "OPTIMIZE", "OPTIMIZER_COSTS", "OPTION", "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER", "OUTFILE", "PARTITION", "PRECISION", "PRIMARY", "PROCEDURE", "PURGE", "RANGE", "READ", "READS", "READ_WRITE", "REAL", "REFERENCES", "REGEXP", "RELEASE", "RENAME", "REPEAT", "REPLACE", "REQUIRE", "RESIGNAL", "RESTRICT", "RETURN", "REVOKE", "RIGHT", "RLIKE", "SCHEMA", "SCHEMAS", "SECOND_MICROSECOND", "SELECT", "SENSITIVE", "SEPARATOR", "SET", "SHOW", "SIGNAL", "SMALLINT", "SPATIAL", "SPECIFIC", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS", "SQL_SMALL_RESULT", "SSL", "STARTING", "STORED", "STRAIGHT_JOIN", "TABLE", "TERMINATED", "THEN", "TINYBLOB", "TINYINT", "TINYTEXT", "TO", "TRAILING", "TRIGGER", "TRUE", "UNDO", "UNION", "UNIQUE", "UNLOCK", "UNSIGNED", "UPDATE", "USAGE", "USE", "USING", "UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP", "VALUES", "VARBINARY", "VARCHAR", "VARCHARACTER", "VARYING", "VIRTUAL", "WHEN", "WHERE", "WHILE", "WITH", "WRITE", "XOR", "YEAR_MONTH", "ZEROFILL"};
        SQL92_KEYWORDS = new String[]{"ABSOLUTE", "ACTION", "ADD", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "AS", "ASC", "ASSERTION", "AT", "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", "BIT", "BIT_LENGTH", "BOTH", "BY", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CHAR", "CHARACTER", "CHARACTER_LENGTH", "CHAR_LENGTH", "CHECK", "CLOSE", "COALESCE", "COLLATE", "COLLATION", "COLUMN", "COMMIT", "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONTINUE", "CONVERT", "CORRESPONDING", "COUNT", "CREATE", "CROSS", "CURRENT", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DESC", "DESCRIBE", "DESCRIPTOR", "DIAGNOSTICS", "DISCONNECT", "DISTINCT", "DOMAIN", "DOUBLE", "DROP", "ELSE", "END", "END-EXEC", "ESCAPE", "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE", "EXISTS", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", "FROM", "FULL", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "HAVING", "HOUR", "IDENTITY", "IMMEDIATE", "IN", "INDICATOR", "INITIALLY", "INNER", "INPUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "ISOLATION", "JOIN", "KEY", "LANGUAGE", "LAST", "LEADING", "LEFT", "LEVEL", "LIKE", "LOCAL", "LOWER", "MATCH", "MAX", "MIN", "MINUTE", "MODULE", "MONTH", "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NEXT", "NO", "NOT", "NULL", "NULLIF", "NUMERIC", "OCTET_LENGTH", "OF", "ON", "ONLY", "OPEN", "OPTION", "OR", "ORDER", "OUTER", "OUTPUT", "OVERLAPS", "PAD", "PARTIAL", "POSITION", "PRECISION", "PREPARE", "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC", "READ", "REAL", "REFERENCES", "RELATIVE", "RESTRICT", "REVOKE", "RIGHT", "ROLLBACK", "ROWS", "SCHEMA", "SCROLL", "SECOND", "SECTION", "SELECT", "SESSION", "SESSION_USER", "SET", "SIZE", "SMALLINT", "SOME", "SPACE", "SQL", "SQLCODE", "SQLERROR", "SQLSTATE", "SUBSTRING", "SUM", "SYSTEM_USER", "TABLE", "TEMPORARY", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSACTION", "TRANSLATE", "TRANSLATION", "TRIM", "TRUE", "UNION", "UNIQUE", "UNKNOWN", "UPDATE", "UPPER", "USAGE", "USER", "USING", "VALUE", "VALUES", "VARCHAR", "VARYING", "VIEW", "WHEN", "WHENEVER", "WHERE", "WITH", "WORK", "WRITE", "YEAR", "ZONE"};
        SQL2003_KEYWORDS = new String[]{"ABS", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "ARRAY", "AS", "ASENSITIVE", "ASYMMETRIC", "AT", "ATOMIC", "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOOLEAN", "BOTH", "BY", "CALL", "CALLED", "CARDINALITY", "CASCADED", "CASE", "CAST", "CEIL", "CEILING", "CHAR", "CHARACTER", "CHARACTER_LENGTH", "CHAR_LENGTH", "CHECK", "CLOB", "CLOSE", "COALESCE", "COLLATE", "COLLECT", "COLUMN", "COMMIT", "CONDITION", "CONNECT", "CONSTRAINT", "CONVERT", "CORR", "CORRESPONDING", "COUNT", "COVAR_POP", "COVAR_SAMP", "CREATE", "CROSS", "CUBE", "CUME_DIST", "CURRENT", "CURRENT_DATE", "CURRENT_DEFAULT_TRANSFORM_GROUP", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_TRANSFORM_GROUP_FOR_TYPE", "CURRENT_USER", "CURSOR", "CYCLE", "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELETE", "DENSE_RANK", "DEREF", "DESCRIBE", "DETERMINISTIC", "DISCONNECT", "DISTINCT", "DOUBLE", "DROP", "DYNAMIC", "EACH", "ELEMENT", "ELSE", "END", "END-EXEC", "ESCAPE", "EVERY", "EXCEPT", "EXEC", "EXECUTE", "EXISTS", "EXP", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FILTER", "FLOAT", "FLOOR", "FOR", "FOREIGN", "FREE", "FROM", "FULL", "FUNCTION", "FUSION", "GET", "GLOBAL", "GRANT", "GROUP", "GROUPING", "HAVING", "HOLD", "HOUR", "IDENTITY", "IN", "INDICATOR", "INNER", "INOUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERSECTION", "INTERVAL", "INTO", "IS", "JOIN", "LANGUAGE", "LARGE", "LATERAL", "LEADING", "LEFT", "LIKE", "LN", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOWER", "MATCH", "MAX", "MEMBER", "MERGE", "METHOD", "MIN", "MINUTE", "MOD", "MODIFIES", "MODULE", "MONTH", "MULTISET", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NO", "NONE", "NORMALIZE", "NOT", "NULL", "NULLIF", "NUMERIC", "OCTET_LENGTH", "OF", "OLD", "ON", "ONLY", "OPEN", "OR", "ORDER", "OUT", "OUTER", "OVER", "OVERLAPS", "OVERLAY", "PARAMETER", "PARTITION", "PERCENTILE_CONT", "PERCENTILE_DISC", "PERCENT_RANK", "POSITION", "POWER", "PRECISION", "PREPARE", "PRIMARY", "PROCEDURE", "RANGE", "RANK", "READS", "REAL", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", "REGR_AVGX", "REGR_AVGY", "REGR_COUNT", "REGR_INTERCEPT", "REGR_R2", "REGR_SLOPE", "REGR_SXX", "REGR_SXY", "REGR_SYY", "RELEASE", "RESULT", "RETURN", "RETURNS", "REVOKE", "RIGHT", "ROLLBACK", "ROLLUP", "ROW", "ROWS", "ROW_NUMBER", "SAVEPOINT", "SCOPE", "SCROLL", "SEARCH", "SECOND", "SELECT", "SENSITIVE", "SESSION_USER", "SET", "SIMILAR", "SMALLINT", "SOME", "SPECIFIC", "SPECIFICTYPE", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQRT", "START", "STATIC", "STDDEV_POP", "STDDEV_SAMP", "SUBMULTISET", "SUBSTRING", "SUM", "SYMMETRIC", "SYSTEM", "SYSTEM_USER", "TABLE", "TABLESAMPLE", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSLATE", "TRANSLATION", "TREAT", "TRIGGER", "TRIM", "TRUE", "UESCAPE", "UNION", "UNIQUE", "UNKNOWN", "UNNEST", "UPDATE", "UPPER", "USER", "USING", "VALUE", "VALUES", "VARCHAR", "VARYING", "VAR_POP", "VAR_SAMP", "WHEN", "WHENEVER", "WHERE", "WIDTH_BUCKET", "WINDOW", "WITH", "WITHIN", "WITHOUT", "YEAR"};
        mysqlKeywords = null;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    protected static enum ProcedureType {
        PROCEDURE,
        FUNCTION;

    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    protected static enum TableType {
        LOCAL_TEMPORARY("LOCAL TEMPORARY"),
        SYSTEM_TABLE("SYSTEM TABLE"),
        SYSTEM_VIEW("SYSTEM VIEW"),
        TABLE("TABLE", new String[]{"BASE TABLE"}),
        VIEW("VIEW"),
        UNKNOWN("UNKNOWN");

        private String name;
        private byte[] nameAsBytes;
        private String[] synonyms;

        private TableType(String tableTypeName) {
            this(tableTypeName, null);
        }

        private TableType(String tableTypeName, String[] tableTypeSynonyms) {
            this.name = tableTypeName;
            this.nameAsBytes = tableTypeName.getBytes();
            this.synonyms = tableTypeSynonyms;
        }

        String getName() {
            return this.name;
        }

        byte[] asBytes() {
            return this.nameAsBytes;
        }

        boolean equalsTo(String tableTypeName) {
            return this.name.equalsIgnoreCase(tableTypeName);
        }

        static TableType getTableTypeEqualTo(String tableTypeName) {
            for (TableType tableType : TableType.values()) {
                if (!tableType.equalsTo(tableTypeName)) continue;
                return tableType;
            }
            return UNKNOWN;
        }

        boolean compliesWith(String tableTypeName) {
            if (this.equalsTo(tableTypeName)) {
                return true;
            }
            if (this.synonyms != null) {
                for (String synonym : this.synonyms) {
                    if (!synonym.equalsIgnoreCase(tableTypeName)) continue;
                    return true;
                }
            }
            return false;
        }

        static TableType getTableTypeCompliantWith(String tableTypeName) {
            for (TableType tableType : TableType.values()) {
                if (!tableType.compliesWith(tableTypeName)) continue;
                return tableType;
            }
            return UNKNOWN;
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    protected class ComparableWrapper<K, V>
    implements Comparable<ComparableWrapper<K, V>> {
        K key;
        V value;

        public ComparableWrapper(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }

        @Override
        public int compareTo(ComparableWrapper<K, V> other) {
            return ((Comparable)this.getKey()).compareTo(other.getKey());
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ComparableWrapper)) {
                return false;
            }
            K otherKey = ((ComparableWrapper)obj).getKey();
            return this.key.equals(otherKey);
        }

        public int hashCode() {
            assert (false) : "hashCode not designed";
            return 0;
        }

        public String toString() {
            return "{KEY:" + this.key + "; VALUE:" + this.value + "}";
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    protected class TableMetaDataKey
    implements Comparable<TableMetaDataKey> {
        String tableType;
        String tableCat;
        String tableSchem;
        String tableName;

        TableMetaDataKey(String tableType, String tableCat, String tableSchem, String tableName) {
            this.tableType = tableType == null ? "" : tableType;
            this.tableCat = tableCat == null ? "" : tableCat;
            this.tableSchem = tableSchem == null ? "" : tableSchem;
            this.tableName = tableName == null ? "" : tableName;
        }

        @Override
        public int compareTo(TableMetaDataKey tablesKey) {
            int compareResult = this.tableType.compareTo(tablesKey.tableType);
            if (compareResult != 0) {
                return compareResult;
            }
            compareResult = this.tableCat.compareTo(tablesKey.tableCat);
            if (compareResult != 0) {
                return compareResult;
            }
            compareResult = this.tableSchem.compareTo(tablesKey.tableSchem);
            if (compareResult != 0) {
                return compareResult;
            }
            return this.tableName.compareTo(tablesKey.tableName);
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof TableMetaDataKey)) {
                return false;
            }
            return this.compareTo((TableMetaDataKey)obj) == 0;
        }

        public int hashCode() {
            assert (false) : "hashCode not designed";
            return 0;
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    protected class IndexMetaDataKey
    implements Comparable<IndexMetaDataKey> {
        Boolean columnNonUnique;
        Short columnType;
        String columnIndexName;
        Short columnOrdinalPosition;

        IndexMetaDataKey(boolean columnNonUnique, short columnType, String columnIndexName, short columnOrdinalPosition) {
            this.columnNonUnique = columnNonUnique;
            this.columnType = columnType;
            this.columnIndexName = columnIndexName;
            this.columnOrdinalPosition = columnOrdinalPosition;
        }

        @Override
        public int compareTo(IndexMetaDataKey indexInfoKey) {
            int compareResult = this.columnNonUnique.compareTo(indexInfoKey.columnNonUnique);
            if (compareResult != 0) {
                return compareResult;
            }
            compareResult = this.columnType.compareTo(indexInfoKey.columnType);
            if (compareResult != 0) {
                return compareResult;
            }
            compareResult = this.columnIndexName.compareTo(indexInfoKey.columnIndexName);
            if (compareResult != 0) {
                return compareResult;
            }
            return this.columnOrdinalPosition.compareTo(indexInfoKey.columnOrdinalPosition);
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof IndexMetaDataKey)) {
                return false;
            }
            return this.compareTo((IndexMetaDataKey)obj) == 0;
        }

        public int hashCode() {
            assert (false) : "hashCode not designed";
            return 0;
        }
    }

    class TypeDescriptor {
        int bufferLength;
        int charOctetLength;
        Integer columnSize;
        short dataType;
        Integer decimalDigits;
        String isNullable;
        int nullability;
        int numPrecRadix = 10;
        String typeName;

        TypeDescriptor(String typeInfo, String nullabilityInfo) throws SQLException {
            if (typeInfo == null) {
                throw SQLError.createSQLException("NULL typeinfo not supported.", "S1009", DatabaseMetaData.this.getExceptionInterceptor());
            }
            String mysqlType = "";
            String fullMysqlType = null;
            mysqlType = typeInfo.indexOf("(") != -1 ? typeInfo.substring(0, typeInfo.indexOf("(")).trim() : typeInfo;
            int indexOfUnsignedInMysqlType = StringUtils.indexOfIgnoreCase(mysqlType, "unsigned");
            if (indexOfUnsignedInMysqlType != -1) {
                mysqlType = mysqlType.substring(0, indexOfUnsignedInMysqlType - 1);
            }
            boolean isUnsigned = false;
            if (StringUtils.indexOfIgnoreCase(typeInfo, "unsigned") != -1 && StringUtils.indexOfIgnoreCase(typeInfo, "set") != 0 && StringUtils.indexOfIgnoreCase(typeInfo, "enum") != 0) {
                fullMysqlType = mysqlType + " unsigned";
                isUnsigned = true;
            } else {
                fullMysqlType = mysqlType;
            }
            if (DatabaseMetaData.this.conn.getCapitalizeTypeNames()) {
                fullMysqlType = fullMysqlType.toUpperCase(Locale.ENGLISH);
            }
            this.dataType = (short)MysqlDefs.mysqlToJavaType(mysqlType);
            this.typeName = fullMysqlType;
            if (StringUtils.startsWithIgnoreCase(typeInfo, "enum")) {
                String temp = typeInfo.substring(typeInfo.indexOf("("), typeInfo.lastIndexOf(")"));
                StringTokenizer tokenizer = new StringTokenizer(temp, ",");
                int maxLength = 0;
                while (tokenizer.hasMoreTokens()) {
                    maxLength = Math.max(maxLength, tokenizer.nextToken().length() - 2);
                }
                this.columnSize = maxLength;
                this.decimalDigits = null;
            } else if (StringUtils.startsWithIgnoreCase(typeInfo, "set")) {
                String temp = typeInfo.substring(typeInfo.indexOf("(") + 1, typeInfo.lastIndexOf(")"));
                StringTokenizer tokenizer = new StringTokenizer(temp, ",");
                int maxLength = 0;
                int numElements = tokenizer.countTokens();
                if (numElements > 0) {
                    maxLength += numElements - 1;
                }
                while (tokenizer.hasMoreTokens()) {
                    String setMember = tokenizer.nextToken().trim();
                    if (setMember.startsWith("'") && setMember.endsWith("'")) {
                        maxLength += setMember.length() - 2;
                        continue;
                    }
                    maxLength += setMember.length();
                }
                this.columnSize = maxLength;
                this.decimalDigits = null;
            } else if (typeInfo.indexOf(",") != -1) {
                this.columnSize = Integer.valueOf(typeInfo.substring(typeInfo.indexOf("(") + 1, typeInfo.indexOf(",")).trim());
                this.decimalDigits = Integer.valueOf(typeInfo.substring(typeInfo.indexOf(",") + 1, typeInfo.indexOf(")")).trim());
            } else {
                this.columnSize = null;
                this.decimalDigits = null;
                if ((StringUtils.indexOfIgnoreCase(typeInfo, "char") != -1 || StringUtils.indexOfIgnoreCase(typeInfo, "text") != -1 || StringUtils.indexOfIgnoreCase(typeInfo, "blob") != -1 || StringUtils.indexOfIgnoreCase(typeInfo, "binary") != -1 || StringUtils.indexOfIgnoreCase(typeInfo, "bit") != -1) && typeInfo.indexOf("(") != -1) {
                    int endParenIndex = typeInfo.indexOf(")");
                    if (endParenIndex == -1) {
                        endParenIndex = typeInfo.length();
                    }
                    this.columnSize = Integer.valueOf(typeInfo.substring(typeInfo.indexOf("(") + 1, endParenIndex).trim());
                    if (DatabaseMetaData.this.conn.getTinyInt1isBit() && this.columnSize == 1 && StringUtils.startsWithIgnoreCase(typeInfo, 0, "tinyint")) {
                        if (DatabaseMetaData.this.conn.getTransformedBitIsBoolean()) {
                            this.dataType = (short)16;
                            this.typeName = "BOOLEAN";
                        } else {
                            this.dataType = (short)-7;
                            this.typeName = "BIT";
                        }
                    }
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "tinyint")) {
                    if (DatabaseMetaData.this.conn.getTinyInt1isBit() && typeInfo.indexOf("(1)") != -1) {
                        if (DatabaseMetaData.this.conn.getTransformedBitIsBoolean()) {
                            this.dataType = (short)16;
                            this.typeName = "BOOLEAN";
                        } else {
                            this.dataType = (short)-7;
                            this.typeName = "BIT";
                        }
                    } else {
                        this.columnSize = 3;
                        this.decimalDigits = 0;
                    }
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "smallint")) {
                    this.columnSize = 5;
                    this.decimalDigits = 0;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "mediumint")) {
                    this.columnSize = isUnsigned ? 8 : 7;
                    this.decimalDigits = 0;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "int")) {
                    this.columnSize = 10;
                    this.decimalDigits = 0;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "integer")) {
                    this.columnSize = 10;
                    this.decimalDigits = 0;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "bigint")) {
                    this.columnSize = isUnsigned ? 20 : 19;
                    this.decimalDigits = 0;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "int24")) {
                    this.columnSize = 19;
                    this.decimalDigits = 0;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "real")) {
                    this.columnSize = 12;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "float")) {
                    this.columnSize = 12;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "decimal")) {
                    this.columnSize = 12;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "numeric")) {
                    this.columnSize = 12;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "double")) {
                    this.columnSize = 22;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "char")) {
                    this.columnSize = 1;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "varchar")) {
                    this.columnSize = 255;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "timestamp")) {
                    this.columnSize = 19;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "datetime")) {
                    this.columnSize = 19;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "date")) {
                    this.columnSize = 10;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "time")) {
                    this.columnSize = 8;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "tinyblob")) {
                    this.columnSize = 255;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "blob")) {
                    this.columnSize = 65535;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "mediumblob")) {
                    this.columnSize = 0xFFFFFF;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "longblob")) {
                    this.columnSize = Integer.MAX_VALUE;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "tinytext")) {
                    this.columnSize = 255;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "text")) {
                    this.columnSize = 65535;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "mediumtext")) {
                    this.columnSize = 0xFFFFFF;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "longtext")) {
                    this.columnSize = Integer.MAX_VALUE;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "enum")) {
                    this.columnSize = 255;
                } else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "set")) {
                    this.columnSize = 255;
                }
            }
            this.bufferLength = MysqlIO.getMaxBuf();
            this.numPrecRadix = 10;
            if (nullabilityInfo != null) {
                if (nullabilityInfo.equals("YES")) {
                    this.nullability = 1;
                    this.isNullable = "YES";
                } else if (nullabilityInfo.equals("UNKNOWN")) {
                    this.nullability = 2;
                    this.isNullable = "";
                } else {
                    this.nullability = 0;
                    this.isNullable = "NO";
                }
            } else {
                this.nullability = 0;
                this.isNullable = "NO";
            }
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    protected class SingleStringIterator
    extends IteratorWithCleanup<String> {
        boolean onFirst;
        String value;

        SingleStringIterator(String s) {
            this.onFirst = true;
            this.value = s;
        }

        @Override
        void close() throws SQLException {
        }

        @Override
        boolean hasNext() throws SQLException {
            return this.onFirst;
        }

        @Override
        String next() throws SQLException {
            this.onFirst = false;
            return this.value;
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    protected class ResultSetIterator
    extends IteratorWithCleanup<String> {
        int colIndex;
        ResultSet resultSet;

        ResultSetIterator(ResultSet rs, int index) {
            this.resultSet = rs;
            this.colIndex = index;
        }

        @Override
        void close() throws SQLException {
            this.resultSet.close();
        }

        @Override
        boolean hasNext() throws SQLException {
            return this.resultSet.next();
        }

        @Override
        String next() throws SQLException {
            return this.resultSet.getObject(this.colIndex).toString();
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    class LocalAndReferencedColumns {
        String constraintName;
        List<String> localColumnsList;
        String referencedCatalog;
        List<String> referencedColumnsList;
        String referencedTable;

        LocalAndReferencedColumns(List<String> localColumns, List<String> refColumns, String constName, String refCatalog, String refTable) {
            this.localColumnsList = localColumns;
            this.referencedColumnsList = refColumns;
            this.constraintName = constName;
            this.referencedTable = refTable;
            this.referencedCatalog = refCatalog;
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    protected abstract class IteratorWithCleanup<T> {
        protected IteratorWithCleanup() {
        }

        abstract void close() throws SQLException;

        abstract boolean hasNext() throws SQLException;

        abstract T next() throws SQLException;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.CharsetMapping;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.MysqlDefs;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.SingleByteCharsetConverter;
import com.mysql.jdbc.StringUtils;
import java.io.UnsupportedEncodingException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.PatternSyntaxException;

public class Field {
    private static final int AUTO_INCREMENT_FLAG = 512;
    private static final int NO_CHARSET_INFO = -1;
    private byte[] buffer;
    private int collationIndex = 0;
    private String encoding = null;
    private int colDecimals;
    private short colFlag;
    private String collationName = null;
    private MySQLConnection connection = null;
    private String databaseName = null;
    private int databaseNameLength = -1;
    private int databaseNameStart = -1;
    protected int defaultValueLength = -1;
    protected int defaultValueStart = -1;
    private String fullName = null;
    private String fullOriginalName = null;
    private boolean isImplicitTempTable = false;
    private long length;
    private int mysqlType = -1;
    private String name;
    private int nameLength;
    private int nameStart;
    private String originalColumnName = null;
    private int originalColumnNameLength = -1;
    private int originalColumnNameStart = -1;
    private String originalTableName = null;
    private int originalTableNameLength = -1;
    private int originalTableNameStart = -1;
    private int precisionAdjustFactor = 0;
    private int sqlType = -1;
    private String tableName;
    private int tableNameLength;
    private int tableNameStart;
    private boolean useOldNameMetadata = false;
    private boolean isSingleBit;
    private int maxBytesPerChar;
    private final boolean valueNeedsQuoting;

    Field(MySQLConnection conn, byte[] buffer, int databaseNameStart, int databaseNameLength, int tableNameStart, int tableNameLength, int originalTableNameStart, int originalTableNameLength, int nameStart, int nameLength, int originalColumnNameStart, int originalColumnNameLength, long length, int mysqlType, short colFlag, int colDecimals, int defaultValueStart, int defaultValueLength, int charsetIndex) throws SQLException {
        boolean isFromFunction;
        this.connection = conn;
        this.buffer = buffer;
        this.nameStart = nameStart;
        this.nameLength = nameLength;
        this.tableNameStart = tableNameStart;
        this.tableNameLength = tableNameLength;
        this.length = length;
        this.colFlag = colFlag;
        this.colDecimals = colDecimals;
        this.mysqlType = mysqlType;
        this.databaseNameStart = databaseNameStart;
        this.databaseNameLength = databaseNameLength;
        this.originalTableNameStart = originalTableNameStart;
        this.originalTableNameLength = originalTableNameLength;
        this.originalColumnNameStart = originalColumnNameStart;
        this.originalColumnNameLength = originalColumnNameLength;
        this.defaultValueStart = defaultValueStart;
        this.defaultValueLength = defaultValueLength;
        this.collationIndex = charsetIndex;
        this.sqlType = MysqlDefs.mysqlToJavaType(this.mysqlType);
        this.checkForImplicitTemporaryTable();
        boolean bl = isFromFunction = this.originalTableNameLength == 0;
        if (this.mysqlType == 252) {
            if (this.connection.getBlobsAreStrings() || this.connection.getFunctionsNeverReturnBlobs() && isFromFunction) {
                this.sqlType = 12;
                this.mysqlType = 15;
            } else if (this.collationIndex == 63 || !this.connection.versionMeetsMinimum(4, 1, 0)) {
                if (this.connection.getUseBlobToStoreUTF8OutsideBMP() && this.shouldSetupForUtf8StringInBlob()) {
                    this.setupForUtf8StringInBlob();
                } else {
                    this.setBlobTypeBasedOnLength();
                    this.sqlType = MysqlDefs.mysqlToJavaType(this.mysqlType);
                }
            } else {
                this.mysqlType = 253;
                this.sqlType = -1;
            }
        }
        if (this.sqlType == -6 && this.length == 1L && this.connection.getTinyInt1isBit() && conn.getTinyInt1isBit()) {
            this.sqlType = conn.getTransformedBitIsBoolean() ? 16 : -7;
        }
        if (!this.isNativeNumericType() && !this.isNativeDateTimeType()) {
            this.encoding = this.connection.getEncodingForIndex(this.collationIndex);
            if ("UnicodeBig".equals(this.encoding)) {
                this.encoding = "UTF-16";
            }
            if (this.mysqlType == 245) {
                this.encoding = "UTF-8";
            }
            boolean isBinary = this.isBinary();
            if (this.connection.versionMeetsMinimum(4, 1, 0) && this.mysqlType == 253 && isBinary && this.collationIndex == 63) {
                if (this.connection.getFunctionsNeverReturnBlobs() && isFromFunction) {
                    this.sqlType = 12;
                    this.mysqlType = 15;
                } else if (this.isOpaqueBinary()) {
                    this.sqlType = -3;
                }
            }
            if (this.connection.versionMeetsMinimum(4, 1, 0) && this.mysqlType == 254 && isBinary && this.collationIndex == 63 && this.isOpaqueBinary() && !this.connection.getBlobsAreStrings()) {
                this.sqlType = -2;
            }
            if (this.mysqlType == 16) {
                boolean bl2 = this.isSingleBit = this.length == 0L || this.length == 1L && (this.connection.versionMeetsMinimum(5, 0, 21) || this.connection.versionMeetsMinimum(5, 1, 10));
                if (!this.isSingleBit) {
                    this.colFlag = (short)(this.colFlag | 0x80);
                    this.colFlag = (short)(this.colFlag | 0x10);
                    isBinary = true;
                }
            }
            if (this.sqlType == -4 && !isBinary) {
                this.sqlType = -1;
            } else if (this.sqlType == -3 && !isBinary) {
                this.sqlType = 12;
            }
        } else {
            this.encoding = "US-ASCII";
        }
        if (!this.isUnsigned()) {
            switch (this.mysqlType) {
                case 0: 
                case 246: {
                    this.precisionAdjustFactor = -1;
                    break;
                }
                case 4: 
                case 5: {
                    this.precisionAdjustFactor = 1;
                }
            }
        } else {
            switch (this.mysqlType) {
                case 4: 
                case 5: {
                    this.precisionAdjustFactor = 1;
                }
            }
        }
        this.valueNeedsQuoting = this.determineNeedsQuoting();
    }

    private boolean shouldSetupForUtf8StringInBlob() throws SQLException {
        block9: {
            String includePattern = this.connection.getUtf8OutsideBmpIncludedColumnNamePattern();
            String excludePattern = this.connection.getUtf8OutsideBmpExcludedColumnNamePattern();
            if (excludePattern != null && !StringUtils.isEmptyOrWhitespaceOnly(excludePattern)) {
                try {
                    if (!this.getOriginalName().matches(excludePattern)) break block9;
                    if (includePattern != null && !StringUtils.isEmptyOrWhitespaceOnly(includePattern)) {
                        try {
                            if (this.getOriginalName().matches(includePattern)) {
                                return true;
                            }
                        }
                        catch (PatternSyntaxException pse) {
                            SQLException sqlEx = SQLError.createSQLException("Illegal regex specified for \"utf8OutsideBmpIncludedColumnNamePattern\"", "S1009", this.connection.getExceptionInterceptor());
                            if (!this.connection.getParanoid()) {
                                sqlEx.initCause(pse);
                            }
                            throw sqlEx;
                        }
                    }
                    return false;
                }
                catch (PatternSyntaxException pse) {
                    SQLException sqlEx = SQLError.createSQLException("Illegal regex specified for \"utf8OutsideBmpExcludedColumnNamePattern\"", "S1009", this.connection.getExceptionInterceptor());
                    if (!this.connection.getParanoid()) {
                        sqlEx.initCause(pse);
                    }
                    throw sqlEx;
                }
            }
        }
        return true;
    }

    private void setupForUtf8StringInBlob() {
        if (this.length == 255L || this.length == 65535L) {
            this.mysqlType = 15;
            this.sqlType = 12;
        } else {
            this.mysqlType = 253;
            this.sqlType = -1;
        }
        this.collationIndex = 33;
    }

    Field(MySQLConnection conn, byte[] buffer, int nameStart, int nameLength, int tableNameStart, int tableNameLength, int length, int mysqlType, short colFlag, int colDecimals) throws SQLException {
        this(conn, buffer, -1, -1, tableNameStart, tableNameLength, -1, -1, nameStart, nameLength, -1, -1, length, mysqlType, colFlag, colDecimals, -1, -1, -1);
    }

    Field(String tableName, String columnName, int jdbcType, int length) {
        this.tableName = tableName;
        this.name = columnName;
        this.length = length;
        this.sqlType = jdbcType;
        this.colFlag = 0;
        this.colDecimals = 0;
        this.valueNeedsQuoting = this.determineNeedsQuoting();
    }

    Field(String tableName, String columnName, int charsetIndex, int jdbcType, int length) {
        this.tableName = tableName;
        this.name = columnName;
        this.length = length;
        this.sqlType = jdbcType;
        this.colFlag = 0;
        this.colDecimals = 0;
        this.collationIndex = charsetIndex;
        this.valueNeedsQuoting = this.determineNeedsQuoting();
        switch (this.sqlType) {
            case -3: 
            case -2: {
                this.colFlag = (short)(this.colFlag | 0x80);
                this.colFlag = (short)(this.colFlag | 0x10);
            }
        }
    }

    private void checkForImplicitTemporaryTable() {
        this.isImplicitTempTable = this.tableNameLength > 5 && this.buffer[this.tableNameStart] == 35 && this.buffer[this.tableNameStart + 1] == 115 && this.buffer[this.tableNameStart + 2] == 113 && this.buffer[this.tableNameStart + 3] == 108 && this.buffer[this.tableNameStart + 4] == 95;
    }

    public String getEncoding() throws SQLException {
        return this.encoding;
    }

    public void setEncoding(String javaEncodingName, Connection conn) throws SQLException {
        this.encoding = javaEncodingName;
        try {
            this.collationIndex = CharsetMapping.getCollationIndexForJavaEncoding(javaEncodingName, conn);
        }
        catch (RuntimeException ex) {
            SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized String getCollation() throws SQLException {
        block10: {
            block11: {
                Statement collationStmt;
                block9: {
                    if (this.collationName != null || this.connection == null || !this.connection.versionMeetsMinimum(4, 1, 0)) break block10;
                    if (!this.connection.getUseDynamicCharsetInfo()) break block11;
                    DatabaseMetaData dbmd = this.connection.getMetaData();
                    String quotedIdStr = dbmd.getIdentifierQuoteString();
                    if (" ".equals(quotedIdStr)) {
                        quotedIdStr = "";
                    }
                    String csCatalogName = this.getDatabaseName();
                    String csTableName = this.getOriginalTableName();
                    String csColumnName = this.getOriginalName();
                    if (csCatalogName == null || csCatalogName.length() == 0 || csTableName == null || csTableName.length() == 0 || csColumnName == null || csColumnName.length() == 0) break block10;
                    StringBuilder queryBuf = new StringBuilder(csCatalogName.length() + csTableName.length() + 28);
                    queryBuf.append("SHOW FULL COLUMNS FROM ");
                    queryBuf.append(quotedIdStr);
                    queryBuf.append(csCatalogName);
                    queryBuf.append(quotedIdStr);
                    queryBuf.append(".");
                    queryBuf.append(quotedIdStr);
                    queryBuf.append(csTableName);
                    queryBuf.append(quotedIdStr);
                    collationStmt = null;
                    ResultSet collationRs = null;
                    try {
                        collationStmt = this.connection.createStatement();
                        collationRs = collationStmt.executeQuery(queryBuf.toString());
                        while (collationRs.next()) {
                            if (!csColumnName.equals(collationRs.getString("Field"))) continue;
                            this.collationName = collationRs.getString("Collation");
                            break;
                        }
                        Object var10_11 = null;
                        if (collationRs == null) break block9;
                    }
                    catch (Throwable throwable) {
                        Object var10_12 = null;
                        if (collationRs != null) {
                            collationRs.close();
                            collationRs = null;
                        }
                        if (collationStmt != null) {
                            collationStmt.close();
                            collationStmt = null;
                        }
                        throw throwable;
                    }
                    collationRs.close();
                    collationRs = null;
                }
                if (collationStmt != null) {
                    collationStmt.close();
                    collationStmt = null;
                }
                break block10;
            }
            try {
                this.collationName = CharsetMapping.COLLATION_INDEX_TO_COLLATION_NAME[this.collationIndex];
            }
            catch (RuntimeException ex) {
                SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
                sqlEx.initCause(ex);
                throw sqlEx;
            }
        }
        return this.collationName;
    }

    public String getColumnLabel() throws SQLException {
        return this.getName();
    }

    public String getDatabaseName() throws SQLException {
        if (this.databaseName == null && this.databaseNameStart != -1 && this.databaseNameLength != -1) {
            this.databaseName = this.getStringFromBytes(this.databaseNameStart, this.databaseNameLength);
        }
        return this.databaseName;
    }

    int getDecimals() {
        return this.colDecimals;
    }

    public String getFullName() throws SQLException {
        if (this.fullName == null) {
            StringBuilder fullNameBuf = new StringBuilder(this.getTableName().length() + 1 + this.getName().length());
            fullNameBuf.append(this.tableName);
            fullNameBuf.append('.');
            fullNameBuf.append(this.name);
            this.fullName = fullNameBuf.toString();
            Object var1_1 = null;
        }
        return this.fullName;
    }

    public String getFullOriginalName() throws SQLException {
        this.getOriginalName();
        if (this.originalColumnName == null) {
            return null;
        }
        if (this.fullName == null) {
            StringBuilder fullOriginalNameBuf = new StringBuilder(this.getOriginalTableName().length() + 1 + this.getOriginalName().length());
            fullOriginalNameBuf.append(this.originalTableName);
            fullOriginalNameBuf.append('.');
            fullOriginalNameBuf.append(this.originalColumnName);
            this.fullOriginalName = fullOriginalNameBuf.toString();
            Object var1_1 = null;
        }
        return this.fullOriginalName;
    }

    public long getLength() {
        return this.length;
    }

    public synchronized int getMaxBytesPerCharacter() throws SQLException {
        if (this.maxBytesPerChar == 0) {
            this.maxBytesPerChar = this.connection.getMaxBytesPerChar(this.collationIndex, this.getEncoding());
        }
        return this.maxBytesPerChar;
    }

    public int getMysqlType() {
        return this.mysqlType;
    }

    public String getName() throws SQLException {
        if (this.name == null) {
            this.name = this.getStringFromBytes(this.nameStart, this.nameLength);
        }
        return this.name;
    }

    public String getNameNoAliases() throws SQLException {
        if (this.useOldNameMetadata) {
            return this.getName();
        }
        if (this.connection != null && this.connection.versionMeetsMinimum(4, 1, 0)) {
            return this.getOriginalName();
        }
        return this.getName();
    }

    public String getOriginalName() throws SQLException {
        if (this.originalColumnName == null && this.originalColumnNameStart != -1 && this.originalColumnNameLength != -1) {
            this.originalColumnName = this.getStringFromBytes(this.originalColumnNameStart, this.originalColumnNameLength);
        }
        return this.originalColumnName;
    }

    public String getOriginalTableName() throws SQLException {
        if (this.originalTableName == null && this.originalTableNameStart != -1 && this.originalTableNameLength != -1) {
            this.originalTableName = this.getStringFromBytes(this.originalTableNameStart, this.originalTableNameLength);
        }
        return this.originalTableName;
    }

    public int getPrecisionAdjustFactor() {
        return this.precisionAdjustFactor;
    }

    public int getSQLType() {
        return this.sqlType;
    }

    private String getStringFromBytes(int stringStart, int stringLength) throws SQLException {
        if (stringStart == -1 || stringLength == -1) {
            return null;
        }
        String stringVal = null;
        if (this.connection != null) {
            if (this.connection.getUseUnicode()) {
                String javaEncoding = this.connection.getCharacterSetMetadata();
                if (javaEncoding == null) {
                    javaEncoding = this.connection.getEncoding();
                }
                if (javaEncoding != null) {
                    SingleByteCharsetConverter converter = null;
                    if (this.connection != null) {
                        converter = this.connection.getCharsetConverter(javaEncoding);
                    }
                    if (converter != null) {
                        stringVal = converter.toString(this.buffer, stringStart, stringLength);
                    } else {
                        try {
                            stringVal = StringUtils.toString(this.buffer, stringStart, stringLength, javaEncoding);
                        }
                        catch (UnsupportedEncodingException ue) {
                            throw new RuntimeException(Messages.getString("Field.12") + javaEncoding + Messages.getString("Field.13"));
                        }
                    }
                } else {
                    stringVal = StringUtils.toAsciiString(this.buffer, stringStart, stringLength);
                }
            } else {
                stringVal = StringUtils.toAsciiString(this.buffer, stringStart, stringLength);
            }
        } else {
            stringVal = StringUtils.toAsciiString(this.buffer, stringStart, stringLength);
        }
        return stringVal;
    }

    public String getTable() throws SQLException {
        return this.getTableName();
    }

    public String getTableName() throws SQLException {
        if (this.tableName == null) {
            this.tableName = this.getStringFromBytes(this.tableNameStart, this.tableNameLength);
        }
        return this.tableName;
    }

    public String getTableNameNoAliases() throws SQLException {
        if (this.connection.versionMeetsMinimum(4, 1, 0)) {
            return this.getOriginalTableName();
        }
        return this.getTableName();
    }

    public boolean isAutoIncrement() {
        return (this.colFlag & 0x200) > 0;
    }

    public boolean isBinary() {
        return (this.colFlag & 0x80) > 0;
    }

    public boolean isBlob() {
        return (this.colFlag & 0x10) > 0;
    }

    private boolean isImplicitTemporaryTable() {
        return this.isImplicitTempTable;
    }

    public boolean isMultipleKey() {
        return (this.colFlag & 8) > 0;
    }

    boolean isNotNull() {
        return (this.colFlag & 1) > 0;
    }

    boolean isOpaqueBinary() throws SQLException {
        if (this.collationIndex == 63 && this.isBinary() && (this.getMysqlType() == 254 || this.getMysqlType() == 253)) {
            if (this.originalTableNameLength == 0 && this.connection != null && !this.connection.versionMeetsMinimum(5, 0, 25)) {
                return false;
            }
            return !this.isImplicitTemporaryTable();
        }
        return this.connection.versionMeetsMinimum(4, 1, 0) && "binary".equalsIgnoreCase(this.getEncoding());
    }

    public boolean isPrimaryKey() {
        return (this.colFlag & 2) > 0;
    }

    boolean isReadOnly() throws SQLException {
        if (this.connection.versionMeetsMinimum(4, 1, 0)) {
            String orgColumnName = this.getOriginalName();
            String orgTableName = this.getOriginalTableName();
            return orgColumnName == null || orgColumnName.length() <= 0 || orgTableName == null || orgTableName.length() <= 0;
        }
        return false;
    }

    public boolean isUniqueKey() {
        return (this.colFlag & 4) > 0;
    }

    public boolean isUnsigned() {
        return (this.colFlag & 0x20) > 0;
    }

    public void setUnsigned() {
        this.colFlag = (short)(this.colFlag | 0x20);
    }

    public boolean isZeroFill() {
        return (this.colFlag & 0x40) > 0;
    }

    private void setBlobTypeBasedOnLength() {
        if (this.length == 255L) {
            this.mysqlType = 249;
        } else if (this.length == 65535L) {
            this.mysqlType = 252;
        } else if (this.length == 0xFFFFFFL) {
            this.mysqlType = 250;
        } else if (this.length == 0xFFFFFFFFL) {
            this.mysqlType = 251;
        }
    }

    private boolean isNativeNumericType() {
        return this.mysqlType >= 1 && this.mysqlType <= 5 || this.mysqlType == 8 || this.mysqlType == 13;
    }

    private boolean isNativeDateTimeType() {
        return this.mysqlType == 10 || this.mysqlType == 14 || this.mysqlType == 12 || this.mysqlType == 11 || this.mysqlType == 7;
    }

    public void setConnection(MySQLConnection conn) {
        this.connection = conn;
        if (this.encoding == null || this.collationIndex == 0) {
            this.encoding = this.connection.getEncoding();
        }
    }

    void setMysqlType(int type) {
        this.mysqlType = type;
        this.sqlType = MysqlDefs.mysqlToJavaType(this.mysqlType);
    }

    protected void setUseOldNameMetadata(boolean useOldNameMetadata) {
        this.useOldNameMetadata = useOldNameMetadata;
    }

    public String toString() {
        try {
            StringBuilder asString = new StringBuilder();
            asString.append(super.toString());
            asString.append("[");
            asString.append("catalog=");
            asString.append(this.getDatabaseName());
            asString.append(",tableName=");
            asString.append(this.getTableName());
            asString.append(",originalTableName=");
            asString.append(this.getOriginalTableName());
            asString.append(",columnName=");
            asString.append(this.getName());
            asString.append(",originalColumnName=");
            asString.append(this.getOriginalName());
            asString.append(",mysqlType=");
            asString.append(this.getMysqlType());
            asString.append("(");
            asString.append(MysqlDefs.typeToName(this.getMysqlType()));
            asString.append(")");
            asString.append(",flags=");
            if (this.isAutoIncrement()) {
                asString.append(" AUTO_INCREMENT");
            }
            if (this.isPrimaryKey()) {
                asString.append(" PRIMARY_KEY");
            }
            if (this.isUniqueKey()) {
                asString.append(" UNIQUE_KEY");
            }
            if (this.isBinary()) {
                asString.append(" BINARY");
            }
            if (this.isBlob()) {
                asString.append(" BLOB");
            }
            if (this.isMultipleKey()) {
                asString.append(" MULTI_KEY");
            }
            if (this.isUnsigned()) {
                asString.append(" UNSIGNED");
            }
            if (this.isZeroFill()) {
                asString.append(" ZEROFILL");
            }
            asString.append(", charsetIndex=");
            asString.append(this.collationIndex);
            asString.append(", charsetName=");
            asString.append(this.encoding);
            asString.append("]");
            return asString.toString();
        }
        catch (Throwable t) {
            return super.toString();
        }
    }

    protected boolean isSingleBit() {
        return this.isSingleBit;
    }

    protected boolean getvalueNeedsQuoting() {
        return this.valueNeedsQuoting;
    }

    private boolean determineNeedsQuoting() {
        boolean retVal = false;
        switch (this.sqlType) {
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
                retVal = false;
                break;
            }
            default: {
                retVal = true;
            }
        }
        return retVal;
    }
}


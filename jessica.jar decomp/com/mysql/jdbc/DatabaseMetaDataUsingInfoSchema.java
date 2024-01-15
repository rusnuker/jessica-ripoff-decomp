/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.DatabaseMetaData;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.MysqlDefs;
import com.mysql.jdbc.MysqlIO;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.StringUtils;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DatabaseMetaDataUsingInfoSchema
extends DatabaseMetaData {
    private boolean hasReferentialConstraintsView;
    private final boolean hasParametersView;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected DatabaseMetaDataUsingInfoSchema(MySQLConnection connToSet, String databaseToSet) throws SQLException {
        super(connToSet, databaseToSet);
        this.hasReferentialConstraintsView = this.conn.versionMeetsMinimum(5, 1, 10);
        ResultSet rs = null;
        try {
            rs = super.getTables("INFORMATION_SCHEMA", null, "PARAMETERS", new String[0]);
            this.hasParametersView = rs.next();
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
    }

    protected ResultSet executeMetadataQuery(PreparedStatement pStmt) throws SQLException {
        ResultSet rs = pStmt.executeQuery();
        ((ResultSetInternalMethods)rs).setOwningStatement(null);
        return rs;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
        ResultSet resultSet;
        block8: {
            if (columnNamePattern == null) {
                if (this.conn.getNullNamePatternMatchesAll()) {
                    columnNamePattern = "%";
                } else {
                    throw SQLError.createSQLException("Column name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
                }
            }
            if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
                catalog = this.database;
            }
            String sql = "SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME,COLUMN_NAME, NULL AS GRANTOR, GRANTEE, PRIVILEGE_TYPE AS PRIVILEGE, IS_GRANTABLE FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES WHERE TABLE_SCHEMA LIKE ? AND TABLE_NAME =? AND COLUMN_NAME LIKE ? ORDER BY COLUMN_NAME, PRIVILEGE_TYPE";
            PreparedStatement pStmt = null;
            try {
                pStmt = this.prepareMetaDataSafeStatement(sql);
                if (catalog != null) {
                    pStmt.setString(1, catalog);
                } else {
                    pStmt.setString(1, "%");
                }
                pStmt.setString(2, table);
                pStmt.setString(3, columnNamePattern);
                ResultSet rs = this.executeMetadataQuery(pStmt);
                ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(new Field[]{new Field("", "TABLE_CAT", 1, 64), new Field("", "TABLE_SCHEM", 1, 1), new Field("", "TABLE_NAME", 1, 64), new Field("", "COLUMN_NAME", 1, 64), new Field("", "GRANTOR", 1, 77), new Field("", "GRANTEE", 1, 77), new Field("", "PRIVILEGE", 1, 64), new Field("", "IS_GRANTABLE", 1, 3)});
                resultSet = rs;
                Object var10_9 = null;
                if (pStmt == null) break block8;
            }
            catch (Throwable throwable) {
                block9: {
                    Object var10_10 = null;
                    if (pStmt == null) break block9;
                    pStmt.close();
                }
                throw throwable;
            }
            pStmt.close();
        }
        return resultSet;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ResultSet getColumns(String catalog, String schemaPattern, String tableName, String columnNamePattern) throws SQLException {
        ResultSet resultSet;
        block20: {
            if (columnNamePattern == null) {
                if (this.conn.getNullNamePatternMatchesAll()) {
                    columnNamePattern = "%";
                } else {
                    throw SQLError.createSQLException("Column name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
                }
            }
            if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
                catalog = this.database;
            }
            StringBuilder sqlBuf = new StringBuilder("SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME, COLUMN_NAME,");
            MysqlDefs.appendJdbcTypeMappingQuery(sqlBuf, "DATA_TYPE");
            sqlBuf.append(" AS DATA_TYPE, ");
            if (this.conn.getCapitalizeTypeNames()) {
                sqlBuf.append("UPPER(CASE WHEN LOCATE('unsigned', COLUMN_TYPE) != 0 AND LOCATE('unsigned', DATA_TYPE) = 0 AND LOCATE('set', DATA_TYPE) <> 1 AND LOCATE('enum', DATA_TYPE) <> 1 THEN CONCAT(DATA_TYPE, ' unsigned') ELSE DATA_TYPE END) AS TYPE_NAME,");
            } else {
                sqlBuf.append("CASE WHEN LOCATE('unsigned', COLUMN_TYPE) != 0 AND LOCATE('unsigned', DATA_TYPE) = 0 AND LOCATE('set', DATA_TYPE) <> 1 AND LOCATE('enum', DATA_TYPE) <> 1 THEN CONCAT(DATA_TYPE, ' unsigned') ELSE DATA_TYPE END AS TYPE_NAME,");
            }
            sqlBuf.append("CASE WHEN LCASE(DATA_TYPE)='date' THEN 10 WHEN LCASE(DATA_TYPE)='time' THEN 8 WHEN LCASE(DATA_TYPE)='datetime' THEN 19 WHEN LCASE(DATA_TYPE)='timestamp' THEN 19 WHEN CHARACTER_MAXIMUM_LENGTH IS NULL THEN NUMERIC_PRECISION WHEN CHARACTER_MAXIMUM_LENGTH > 2147483647 THEN 2147483647 ELSE CHARACTER_MAXIMUM_LENGTH END AS COLUMN_SIZE, " + MysqlIO.getMaxBuf() + " AS BUFFER_LENGTH," + "NUMERIC_SCALE AS DECIMAL_DIGITS," + "10 AS NUM_PREC_RADIX," + "CASE WHEN IS_NULLABLE='NO' THEN " + 0 + " ELSE CASE WHEN IS_NULLABLE='YES' THEN " + 1 + " ELSE " + 2 + " END END AS NULLABLE," + "COLUMN_COMMENT AS REMARKS," + "COLUMN_DEFAULT AS COLUMN_DEF," + "0 AS SQL_DATA_TYPE," + "0 AS SQL_DATETIME_SUB," + "CASE WHEN CHARACTER_OCTET_LENGTH > " + Integer.MAX_VALUE + " THEN " + Integer.MAX_VALUE + " ELSE CHARACTER_OCTET_LENGTH END AS CHAR_OCTET_LENGTH," + "ORDINAL_POSITION," + "IS_NULLABLE," + "NULL AS SCOPE_CATALOG," + "NULL AS SCOPE_SCHEMA," + "NULL AS SCOPE_TABLE," + "NULL AS SOURCE_DATA_TYPE," + "IF (EXTRA LIKE '%auto_increment%','YES','NO') AS IS_AUTOINCREMENT, " + "IF (EXTRA LIKE '%GENERATED%','YES','NO') AS IS_GENERATEDCOLUMN FROM INFORMATION_SCHEMA.COLUMNS WHERE ");
            boolean operatingOnInformationSchema = "information_schema".equalsIgnoreCase(catalog);
            if (catalog != null) {
                if (operatingOnInformationSchema || StringUtils.indexOfIgnoreCase(0, catalog, "%") == -1 && StringUtils.indexOfIgnoreCase(0, catalog, "_") == -1) {
                    sqlBuf.append("TABLE_SCHEMA = ? AND ");
                } else {
                    sqlBuf.append("TABLE_SCHEMA LIKE ? AND ");
                }
            } else {
                sqlBuf.append("TABLE_SCHEMA LIKE ? AND ");
            }
            if (tableName != null) {
                if (StringUtils.indexOfIgnoreCase(0, tableName, "%") == -1 && StringUtils.indexOfIgnoreCase(0, tableName, "_") == -1) {
                    sqlBuf.append("TABLE_NAME = ? AND ");
                } else {
                    sqlBuf.append("TABLE_NAME LIKE ? AND ");
                }
            } else {
                sqlBuf.append("TABLE_NAME LIKE ? AND ");
            }
            if (StringUtils.indexOfIgnoreCase(0, columnNamePattern, "%") == -1 && StringUtils.indexOfIgnoreCase(0, columnNamePattern, "_") == -1) {
                sqlBuf.append("COLUMN_NAME = ? ");
            } else {
                sqlBuf.append("COLUMN_NAME LIKE ? ");
            }
            sqlBuf.append("ORDER BY TABLE_SCHEMA, TABLE_NAME, ORDINAL_POSITION");
            PreparedStatement pStmt = null;
            try {
                pStmt = this.prepareMetaDataSafeStatement(sqlBuf.toString());
                if (catalog != null) {
                    pStmt.setString(1, catalog);
                } else {
                    pStmt.setString(1, "%");
                }
                pStmt.setString(2, tableName);
                pStmt.setString(3, columnNamePattern);
                ResultSet rs = this.executeMetadataQuery(pStmt);
                ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(this.createColumnsFields());
                resultSet = rs;
                Object var11_10 = null;
                if (pStmt == null) break block20;
            }
            catch (Throwable throwable) {
                block21: {
                    Object var11_11 = null;
                    if (pStmt == null) break block21;
                    pStmt.close();
                }
                throw throwable;
            }
            pStmt.close();
        }
        return resultSet;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ResultSet getCrossReference(String primaryCatalog, String primarySchema, String primaryTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
        ResultSet resultSet;
        block9: {
            if (primaryTable == null) {
                throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
            }
            if (primaryCatalog == null && this.conn.getNullCatalogMeansCurrent()) {
                primaryCatalog = this.database;
            }
            if (foreignCatalog == null && this.conn.getNullCatalogMeansCurrent()) {
                foreignCatalog = this.database;
            }
            String sql = "SELECT A.REFERENCED_TABLE_SCHEMA AS PKTABLE_CAT,NULL AS PKTABLE_SCHEM, A.REFERENCED_TABLE_NAME AS PKTABLE_NAME,A.REFERENCED_COLUMN_NAME AS PKCOLUMN_NAME, A.TABLE_SCHEMA AS FKTABLE_CAT, NULL AS FKTABLE_SCHEM, A.TABLE_NAME AS FKTABLE_NAME, A.COLUMN_NAME AS FKCOLUMN_NAME, A.ORDINAL_POSITION AS KEY_SEQ," + this.generateUpdateRuleClause() + " AS UPDATE_RULE," + this.generateDeleteRuleClause() + " AS DELETE_RULE," + "A.CONSTRAINT_NAME AS FK_NAME," + "(SELECT CONSTRAINT_NAME FROM" + " INFORMATION_SCHEMA.TABLE_CONSTRAINTS" + " WHERE TABLE_SCHEMA = A.REFERENCED_TABLE_SCHEMA AND" + " TABLE_NAME = A.REFERENCED_TABLE_NAME AND" + " CONSTRAINT_TYPE IN ('UNIQUE','PRIMARY KEY') LIMIT 1)" + " AS PK_NAME," + 7 + " AS DEFERRABILITY " + "FROM " + "INFORMATION_SCHEMA.KEY_COLUMN_USAGE A JOIN " + "INFORMATION_SCHEMA.TABLE_CONSTRAINTS B " + "USING (TABLE_SCHEMA, TABLE_NAME, CONSTRAINT_NAME) " + this.generateOptionalRefContraintsJoin() + "WHERE " + "B.CONSTRAINT_TYPE = 'FOREIGN KEY' " + "AND A.REFERENCED_TABLE_SCHEMA LIKE ? AND A.REFERENCED_TABLE_NAME=? " + "AND A.TABLE_SCHEMA LIKE ? AND A.TABLE_NAME=? ORDER BY A.TABLE_SCHEMA, A.TABLE_NAME, A.ORDINAL_POSITION";
            PreparedStatement pStmt = null;
            try {
                pStmt = this.prepareMetaDataSafeStatement(sql);
                if (primaryCatalog != null) {
                    pStmt.setString(1, primaryCatalog);
                } else {
                    pStmt.setString(1, "%");
                }
                pStmt.setString(2, primaryTable);
                if (foreignCatalog != null) {
                    pStmt.setString(3, foreignCatalog);
                } else {
                    pStmt.setString(3, "%");
                }
                pStmt.setString(4, foreignTable);
                ResultSet rs = this.executeMetadataQuery(pStmt);
                ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(this.createFkMetadataFields());
                resultSet = rs;
                Object var12_11 = null;
                if (pStmt == null) break block9;
            }
            catch (Throwable throwable) {
                block10: {
                    Object var12_12 = null;
                    if (pStmt == null) break block10;
                    pStmt.close();
                }
                throw throwable;
            }
            pStmt.close();
        }
        return resultSet;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        ResultSet resultSet;
        block6: {
            if (table == null) {
                throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
            }
            if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
                catalog = this.database;
            }
            String sql = "SELECT A.REFERENCED_TABLE_SCHEMA AS PKTABLE_CAT, NULL AS PKTABLE_SCHEM, A.REFERENCED_TABLE_NAME AS PKTABLE_NAME, A.REFERENCED_COLUMN_NAME AS PKCOLUMN_NAME, A.TABLE_SCHEMA AS FKTABLE_CAT, NULL AS FKTABLE_SCHEM, A.TABLE_NAME AS FKTABLE_NAME,A.COLUMN_NAME AS FKCOLUMN_NAME, A.ORDINAL_POSITION AS KEY_SEQ," + this.generateUpdateRuleClause() + " AS UPDATE_RULE," + this.generateDeleteRuleClause() + " AS DELETE_RULE," + "A.CONSTRAINT_NAME AS FK_NAME," + "(SELECT CONSTRAINT_NAME FROM" + " INFORMATION_SCHEMA.TABLE_CONSTRAINTS" + " WHERE TABLE_SCHEMA = A.REFERENCED_TABLE_SCHEMA AND" + " TABLE_NAME = A.REFERENCED_TABLE_NAME AND" + " CONSTRAINT_TYPE IN ('UNIQUE','PRIMARY KEY') LIMIT 1)" + " AS PK_NAME," + 7 + " AS DEFERRABILITY " + "FROM " + "INFORMATION_SCHEMA.KEY_COLUMN_USAGE A JOIN " + "INFORMATION_SCHEMA.TABLE_CONSTRAINTS B " + "USING (TABLE_SCHEMA, TABLE_NAME, CONSTRAINT_NAME) " + this.generateOptionalRefContraintsJoin() + "WHERE " + "B.CONSTRAINT_TYPE = 'FOREIGN KEY' " + "AND A.REFERENCED_TABLE_SCHEMA LIKE ? AND A.REFERENCED_TABLE_NAME=? " + "ORDER BY A.TABLE_SCHEMA, A.TABLE_NAME, A.ORDINAL_POSITION";
            PreparedStatement pStmt = null;
            try {
                pStmt = this.prepareMetaDataSafeStatement(sql);
                if (catalog != null) {
                    pStmt.setString(1, catalog);
                } else {
                    pStmt.setString(1, "%");
                }
                pStmt.setString(2, table);
                ResultSet rs = this.executeMetadataQuery(pStmt);
                ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(this.createFkMetadataFields());
                resultSet = rs;
                Object var9_8 = null;
                if (pStmt == null) break block6;
            }
            catch (Throwable throwable) {
                block7: {
                    Object var9_9 = null;
                    if (pStmt == null) break block7;
                    pStmt.close();
                }
                throw throwable;
            }
            pStmt.close();
        }
        return resultSet;
    }

    private String generateOptionalRefContraintsJoin() {
        return this.hasReferentialConstraintsView ? "JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS R ON (R.CONSTRAINT_NAME = B.CONSTRAINT_NAME AND R.TABLE_NAME = B.TABLE_NAME AND R.CONSTRAINT_SCHEMA = B.TABLE_SCHEMA) " : "";
    }

    private String generateDeleteRuleClause() {
        return this.hasReferentialConstraintsView ? "CASE WHEN R.DELETE_RULE='CASCADE' THEN " + String.valueOf(0) + " WHEN R.DELETE_RULE='SET NULL' THEN " + String.valueOf(2) + " WHEN R.DELETE_RULE='SET DEFAULT' THEN " + String.valueOf(4) + " WHEN R.DELETE_RULE='RESTRICT' THEN " + String.valueOf(1) + " WHEN R.DELETE_RULE='NO ACTION' THEN " + String.valueOf(3) + " ELSE " + String.valueOf(3) + " END " : String.valueOf(1);
    }

    private String generateUpdateRuleClause() {
        return this.hasReferentialConstraintsView ? "CASE WHEN R.UPDATE_RULE='CASCADE' THEN " + String.valueOf(0) + " WHEN R.UPDATE_RULE='SET NULL' THEN " + String.valueOf(2) + " WHEN R.UPDATE_RULE='SET DEFAULT' THEN " + String.valueOf(4) + " WHEN R.UPDATE_RULE='RESTRICT' THEN " + String.valueOf(1) + " WHEN R.UPDATE_RULE='NO ACTION' THEN " + String.valueOf(3) + " ELSE " + String.valueOf(3) + " END " : String.valueOf(1);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        ResultSet resultSet;
        block6: {
            if (table == null) {
                throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
            }
            if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
                catalog = this.database;
            }
            String sql = "SELECT A.REFERENCED_TABLE_SCHEMA AS PKTABLE_CAT, NULL AS PKTABLE_SCHEM, A.REFERENCED_TABLE_NAME AS PKTABLE_NAME,A.REFERENCED_COLUMN_NAME AS PKCOLUMN_NAME, A.TABLE_SCHEMA AS FKTABLE_CAT, NULL AS FKTABLE_SCHEM, A.TABLE_NAME AS FKTABLE_NAME, A.COLUMN_NAME AS FKCOLUMN_NAME, A.ORDINAL_POSITION AS KEY_SEQ," + this.generateUpdateRuleClause() + " AS UPDATE_RULE," + this.generateDeleteRuleClause() + " AS DELETE_RULE," + "A.CONSTRAINT_NAME AS FK_NAME," + "(SELECT CONSTRAINT_NAME FROM" + " INFORMATION_SCHEMA.TABLE_CONSTRAINTS" + " WHERE TABLE_SCHEMA = A.REFERENCED_TABLE_SCHEMA AND" + " TABLE_NAME = A.REFERENCED_TABLE_NAME AND" + " CONSTRAINT_TYPE IN ('UNIQUE','PRIMARY KEY') LIMIT 1)" + " AS PK_NAME," + 7 + " AS DEFERRABILITY " + "FROM " + "INFORMATION_SCHEMA.KEY_COLUMN_USAGE A " + "JOIN INFORMATION_SCHEMA.TABLE_CONSTRAINTS B USING " + "(CONSTRAINT_NAME, TABLE_NAME) " + this.generateOptionalRefContraintsJoin() + "WHERE " + "B.CONSTRAINT_TYPE = 'FOREIGN KEY' " + "AND A.TABLE_SCHEMA LIKE ? " + "AND A.TABLE_NAME=? " + "AND A.REFERENCED_TABLE_SCHEMA IS NOT NULL " + "ORDER BY A.REFERENCED_TABLE_SCHEMA, A.REFERENCED_TABLE_NAME, A.ORDINAL_POSITION";
            PreparedStatement pStmt = null;
            try {
                pStmt = this.prepareMetaDataSafeStatement(sql);
                if (catalog != null) {
                    pStmt.setString(1, catalog);
                } else {
                    pStmt.setString(1, "%");
                }
                pStmt.setString(2, table);
                ResultSet rs = this.executeMetadataQuery(pStmt);
                ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(this.createFkMetadataFields());
                resultSet = rs;
                Object var9_8 = null;
                if (pStmt == null) break block6;
            }
            catch (Throwable throwable) {
                block7: {
                    Object var9_9 = null;
                    if (pStmt == null) break block7;
                    pStmt.close();
                }
                throw throwable;
            }
            pStmt.close();
        }
        return resultSet;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
        ResultSet resultSet;
        block6: {
            StringBuilder sqlBuf = new StringBuilder("SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME, NON_UNIQUE,");
            sqlBuf.append("TABLE_SCHEMA AS INDEX_QUALIFIER, INDEX_NAME,3 AS TYPE, SEQ_IN_INDEX AS ORDINAL_POSITION, COLUMN_NAME,");
            sqlBuf.append("COLLATION AS ASC_OR_DESC, CARDINALITY, NULL AS PAGES, NULL AS FILTER_CONDITION FROM INFORMATION_SCHEMA.STATISTICS WHERE ");
            sqlBuf.append("TABLE_SCHEMA LIKE ? AND TABLE_NAME LIKE ?");
            if (unique) {
                sqlBuf.append(" AND NON_UNIQUE=0 ");
            }
            sqlBuf.append("ORDER BY NON_UNIQUE, INDEX_NAME, SEQ_IN_INDEX");
            PreparedStatement pStmt = null;
            try {
                if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
                    catalog = this.database;
                }
                pStmt = this.prepareMetaDataSafeStatement(sqlBuf.toString());
                if (catalog != null) {
                    pStmt.setString(1, catalog);
                } else {
                    pStmt.setString(1, "%");
                }
                pStmt.setString(2, table);
                ResultSet rs = this.executeMetadataQuery(pStmt);
                ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(this.createIndexInfoFields());
                resultSet = rs;
                Object var11_10 = null;
                if (pStmt == null) break block6;
            }
            catch (Throwable throwable) {
                block7: {
                    Object var11_11 = null;
                    if (pStmt == null) break block7;
                    pStmt.close();
                }
                throw throwable;
            }
            pStmt.close();
        }
        return resultSet;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
        ResultSet resultSet;
        block6: {
            if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
                catalog = this.database;
            }
            if (table == null) {
                throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
            }
            String sql = "SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME, COLUMN_NAME, SEQ_IN_INDEX AS KEY_SEQ, 'PRIMARY' AS PK_NAME FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA LIKE ? AND TABLE_NAME LIKE ? AND INDEX_NAME='PRIMARY' ORDER BY TABLE_SCHEMA, TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX";
            PreparedStatement pStmt = null;
            try {
                pStmt = this.prepareMetaDataSafeStatement(sql);
                if (catalog != null) {
                    pStmt.setString(1, catalog);
                } else {
                    pStmt.setString(1, "%");
                }
                pStmt.setString(2, table);
                ResultSet rs = this.executeMetadataQuery(pStmt);
                ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(new Field[]{new Field("", "TABLE_CAT", 1, 255), new Field("", "TABLE_SCHEM", 1, 0), new Field("", "TABLE_NAME", 1, 255), new Field("", "COLUMN_NAME", 1, 32), new Field("", "KEY_SEQ", 5, 5), new Field("", "PK_NAME", 1, 32)});
                resultSet = rs;
                Object var9_8 = null;
                if (pStmt == null) break block6;
            }
            catch (Throwable throwable) {
                block7: {
                    Object var9_9 = null;
                    if (pStmt == null) break block7;
                    pStmt.close();
                }
                throw throwable;
            }
            pStmt.close();
        }
        return resultSet;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
        ResultSet resultSet;
        block10: {
            if (procedureNamePattern == null || procedureNamePattern.length() == 0) {
                if (this.conn.getNullNamePatternMatchesAll()) {
                    procedureNamePattern = "%";
                } else {
                    throw SQLError.createSQLException("Procedure name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
                }
            }
            String db = null;
            if (catalog == null) {
                if (this.conn.getNullCatalogMeansCurrent()) {
                    db = this.database;
                }
            } else {
                db = catalog;
            }
            String sql = "SELECT ROUTINE_SCHEMA AS PROCEDURE_CAT, NULL AS PROCEDURE_SCHEM, ROUTINE_NAME AS PROCEDURE_NAME, NULL AS RESERVED_1, NULL AS RESERVED_2, NULL AS RESERVED_3, ROUTINE_COMMENT AS REMARKS, CASE WHEN ROUTINE_TYPE = 'PROCEDURE' THEN 1 WHEN ROUTINE_TYPE='FUNCTION' THEN 2 ELSE 0 END AS PROCEDURE_TYPE, ROUTINE_NAME AS SPECIFIC_NAME FROM INFORMATION_SCHEMA.ROUTINES WHERE " + this.getRoutineTypeConditionForGetProcedures() + "ROUTINE_SCHEMA LIKE ? AND ROUTINE_NAME LIKE ? ORDER BY ROUTINE_SCHEMA, ROUTINE_NAME, ROUTINE_TYPE";
            PreparedStatement pStmt = null;
            try {
                pStmt = this.prepareMetaDataSafeStatement(sql);
                if (db != null) {
                    pStmt.setString(1, db);
                } else {
                    pStmt.setString(1, "%");
                }
                pStmt.setString(2, procedureNamePattern);
                ResultSet rs = this.executeMetadataQuery(pStmt);
                ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(this.createFieldMetadataForGetProcedures());
                resultSet = rs;
                Object var10_9 = null;
                if (pStmt == null) break block10;
            }
            catch (Throwable throwable) {
                block11: {
                    Object var10_10 = null;
                    if (pStmt == null) break block11;
                    pStmt.close();
                }
                throw throwable;
            }
            pStmt.close();
        }
        return resultSet;
    }

    protected String getRoutineTypeConditionForGetProcedures() {
        return "";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
        ResultSet resultSet;
        block13: {
            if (!this.hasParametersView) {
                return this.getProcedureColumnsNoISParametersView(catalog, schemaPattern, procedureNamePattern, columnNamePattern);
            }
            if (procedureNamePattern == null || procedureNamePattern.length() == 0) {
                if (this.conn.getNullNamePatternMatchesAll()) {
                    procedureNamePattern = "%";
                } else {
                    throw SQLError.createSQLException("Procedure name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
                }
            }
            String db = null;
            if (catalog == null) {
                if (this.conn.getNullCatalogMeansCurrent()) {
                    db = this.database;
                }
            } else {
                db = catalog;
            }
            StringBuilder sqlBuf = new StringBuilder("SELECT SPECIFIC_SCHEMA AS PROCEDURE_CAT, NULL AS `PROCEDURE_SCHEM`, SPECIFIC_NAME AS `PROCEDURE_NAME`, IFNULL(PARAMETER_NAME, '') AS `COLUMN_NAME`, CASE WHEN PARAMETER_MODE = 'IN' THEN 1 WHEN PARAMETER_MODE = 'OUT' THEN 4 WHEN PARAMETER_MODE = 'INOUT' THEN 2 WHEN ORDINAL_POSITION = 0 THEN 5 ELSE 0 END AS `COLUMN_TYPE`, ");
            MysqlDefs.appendJdbcTypeMappingQuery(sqlBuf, "DATA_TYPE");
            sqlBuf.append(" AS `DATA_TYPE`, ");
            if (this.conn.getCapitalizeTypeNames()) {
                sqlBuf.append("UPPER(CASE WHEN LOCATE('unsigned', DATA_TYPE) != 0 AND LOCATE('unsigned', DATA_TYPE) = 0 THEN CONCAT(DATA_TYPE, ' unsigned') ELSE DATA_TYPE END) AS `TYPE_NAME`,");
            } else {
                sqlBuf.append("CASE WHEN LOCATE('unsigned', DATA_TYPE) != 0 AND LOCATE('unsigned', DATA_TYPE) = 0 THEN CONCAT(DATA_TYPE, ' unsigned') ELSE DATA_TYPE END AS `TYPE_NAME`,");
            }
            sqlBuf.append("NUMERIC_PRECISION AS `PRECISION`, ");
            sqlBuf.append("CASE WHEN LCASE(DATA_TYPE)='date' THEN 10 WHEN LCASE(DATA_TYPE)='time' THEN 8 WHEN LCASE(DATA_TYPE)='datetime' THEN 19 WHEN LCASE(DATA_TYPE)='timestamp' THEN 19 WHEN CHARACTER_MAXIMUM_LENGTH IS NULL THEN NUMERIC_PRECISION WHEN CHARACTER_MAXIMUM_LENGTH > 2147483647 THEN 2147483647 ELSE CHARACTER_MAXIMUM_LENGTH END AS LENGTH, ");
            sqlBuf.append("NUMERIC_SCALE AS `SCALE`, ");
            sqlBuf.append("10 AS RADIX,");
            sqlBuf.append("1 AS `NULLABLE`, NULL AS `REMARKS`, NULL AS `COLUMN_DEF`, NULL AS `SQL_DATA_TYPE`, NULL AS `SQL_DATETIME_SUB`, CHARACTER_OCTET_LENGTH AS `CHAR_OCTET_LENGTH`, ORDINAL_POSITION, 'YES' AS `IS_NULLABLE`, SPECIFIC_NAME FROM INFORMATION_SCHEMA.PARAMETERS WHERE " + this.getRoutineTypeConditionForGetProcedureColumns() + "SPECIFIC_SCHEMA LIKE ? AND SPECIFIC_NAME LIKE ? AND (PARAMETER_NAME LIKE ? OR PARAMETER_NAME IS NULL) " + "ORDER BY SPECIFIC_SCHEMA, SPECIFIC_NAME, ROUTINE_TYPE, ORDINAL_POSITION");
            PreparedStatement pStmt = null;
            try {
                pStmt = this.prepareMetaDataSafeStatement(sqlBuf.toString());
                if (db != null) {
                    pStmt.setString(1, db);
                } else {
                    pStmt.setString(1, "%");
                }
                pStmt.setString(2, procedureNamePattern);
                pStmt.setString(3, columnNamePattern);
                ResultSet rs = this.executeMetadataQuery(pStmt);
                ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(this.createProcedureColumnsFields());
                resultSet = rs;
                Object var11_10 = null;
                if (pStmt == null) break block13;
            }
            catch (Throwable throwable) {
                block14: {
                    Object var11_11 = null;
                    if (pStmt == null) break block14;
                    pStmt.close();
                }
                throw throwable;
            }
            pStmt.close();
        }
        return resultSet;
    }

    protected ResultSet getProcedureColumnsNoISParametersView(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
        return super.getProcedureColumns(catalog, schemaPattern, procedureNamePattern, columnNamePattern);
    }

    protected String getRoutineTypeConditionForGetProcedureColumns() {
        return "";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        ResultSet resultSet;
        block16: {
            List<String> parseList;
            if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
                catalog = this.database;
            }
            if (tableNamePattern == null) {
                if (this.conn.getNullNamePatternMatchesAll()) {
                    tableNamePattern = "%";
                } else {
                    throw SQLError.createSQLException("Table name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
                }
            }
            String tmpCat = "";
            if (catalog == null || catalog.length() == 0) {
                if (this.conn.getNullCatalogMeansCurrent()) {
                    tmpCat = this.database;
                }
            } else {
                tmpCat = catalog;
            }
            String tableNamePat = (parseList = StringUtils.splitDBdotName(tableNamePattern, tmpCat, this.quotedId, this.conn.isNoBackslashEscapesSet())).size() == 2 ? parseList.get(1) : tableNamePattern;
            PreparedStatement pStmt = null;
            String sql = "SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME, CASE WHEN TABLE_TYPE='BASE TABLE' THEN CASE WHEN TABLE_SCHEMA = 'mysql' OR TABLE_SCHEMA = 'performance_schema' THEN 'SYSTEM TABLE' ELSE 'TABLE' END WHEN TABLE_TYPE='TEMPORARY' THEN 'LOCAL_TEMPORARY' ELSE TABLE_TYPE END AS TABLE_TYPE, TABLE_COMMENT AS REMARKS, NULL AS TYPE_CAT, NULL AS TYPE_SCHEM, NULL AS TYPE_NAME, NULL AS SELF_REFERENCING_COL_NAME, NULL AS REF_GENERATION FROM INFORMATION_SCHEMA.TABLES WHERE ";
            boolean operatingOnInformationSchema = "information_schema".equalsIgnoreCase(catalog);
            sql = catalog != null ? (operatingOnInformationSchema || StringUtils.indexOfIgnoreCase(0, catalog, "%") == -1 && StringUtils.indexOfIgnoreCase(0, catalog, "_") == -1 ? sql + "TABLE_SCHEMA = ? " : sql + "TABLE_SCHEMA LIKE ? ") : sql + "TABLE_SCHEMA LIKE ? ";
            sql = tableNamePat != null ? (StringUtils.indexOfIgnoreCase(0, tableNamePat, "%") == -1 && StringUtils.indexOfIgnoreCase(0, tableNamePat, "_") == -1 ? sql + "AND TABLE_NAME = ? " : sql + "AND TABLE_NAME LIKE ? ") : sql + "AND TABLE_NAME LIKE ? ";
            sql = sql + "HAVING TABLE_TYPE IN (?,?,?,?,?) ";
            sql = sql + "ORDER BY TABLE_TYPE, TABLE_SCHEMA, TABLE_NAME";
            try {
                int i;
                pStmt = this.prepareMetaDataSafeStatement(sql);
                if (catalog != null) {
                    pStmt.setString(1, catalog);
                } else {
                    pStmt.setString(1, "%");
                }
                pStmt.setString(2, tableNamePat);
                if (types == null || types.length == 0) {
                    DatabaseMetaData.TableType[] tableTypes = DatabaseMetaData.TableType.values();
                    for (i = 0; i < 5; ++i) {
                        pStmt.setString(3 + i, tableTypes[i].getName());
                    }
                } else {
                    for (int i2 = 0; i2 < 5; ++i2) {
                        pStmt.setNull(3 + i2, 12);
                    }
                    int idx = 3;
                    for (i = 0; i < types.length; ++i) {
                        DatabaseMetaData.TableType tableType = DatabaseMetaData.TableType.getTableTypeEqualTo(types[i]);
                        if (tableType == DatabaseMetaData.TableType.UNKNOWN) continue;
                        pStmt.setString(idx++, tableType.getName());
                    }
                }
                ResultSet rs = this.executeMetadataQuery(pStmt);
                ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(this.createTablesFields());
                resultSet = rs;
                Object var15_16 = null;
                if (pStmt == null) break block16;
            }
            catch (Throwable throwable) {
                block17: {
                    Object var15_17 = null;
                    if (pStmt == null) break block17;
                    pStmt.close();
                }
                throw throwable;
            }
            pStmt.close();
        }
        return resultSet;
    }

    public boolean gethasParametersView() {
        return this.hasParametersView;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        ResultSet resultSet;
        block6: {
            if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
                catalog = this.database;
            }
            if (table == null) {
                throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
            }
            StringBuilder sqlBuf = new StringBuilder("SELECT NULL AS SCOPE, COLUMN_NAME, ");
            MysqlDefs.appendJdbcTypeMappingQuery(sqlBuf, "DATA_TYPE");
            sqlBuf.append(" AS DATA_TYPE, ");
            sqlBuf.append("COLUMN_TYPE AS TYPE_NAME, ");
            sqlBuf.append("CASE WHEN LCASE(DATA_TYPE)='date' THEN 10 WHEN LCASE(DATA_TYPE)='time' THEN 8 WHEN LCASE(DATA_TYPE)='datetime' THEN 19 WHEN LCASE(DATA_TYPE)='timestamp' THEN 19 WHEN CHARACTER_MAXIMUM_LENGTH IS NULL THEN NUMERIC_PRECISION WHEN CHARACTER_MAXIMUM_LENGTH > 2147483647 THEN 2147483647 ELSE CHARACTER_MAXIMUM_LENGTH END AS COLUMN_SIZE, ");
            sqlBuf.append(MysqlIO.getMaxBuf() + " AS BUFFER_LENGTH,NUMERIC_SCALE AS DECIMAL_DIGITS, " + Integer.toString(1) + " AS PSEUDO_COLUMN FROM INFORMATION_SCHEMA.COLUMNS " + "WHERE TABLE_SCHEMA LIKE ? AND TABLE_NAME LIKE ? AND EXTRA LIKE '%on update CURRENT_TIMESTAMP%'");
            PreparedStatement pStmt = null;
            try {
                pStmt = this.prepareMetaDataSafeStatement(sqlBuf.toString());
                if (catalog != null) {
                    pStmt.setString(1, catalog);
                } else {
                    pStmt.setString(1, "%");
                }
                pStmt.setString(2, table);
                ResultSet rs = this.executeMetadataQuery(pStmt);
                ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(new Field[]{new Field("", "SCOPE", 5, 5), new Field("", "COLUMN_NAME", 1, 32), new Field("", "DATA_TYPE", 4, 5), new Field("", "TYPE_NAME", 1, 16), new Field("", "COLUMN_SIZE", 4, 16), new Field("", "BUFFER_LENGTH", 4, 16), new Field("", "DECIMAL_DIGITS", 5, 16), new Field("", "PSEUDO_COLUMN", 5, 5)});
                resultSet = rs;
                Object var9_8 = null;
                if (pStmt == null) break block6;
            }
            catch (Throwable throwable) {
                block7: {
                    Object var9_9 = null;
                    if (pStmt == null) break block7;
                    pStmt.close();
                }
                throw throwable;
            }
            pStmt.close();
        }
        return resultSet;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
        ResultSet resultSet;
        block13: {
            if (!this.hasParametersView) {
                return super.getFunctionColumns(catalog, schemaPattern, functionNamePattern, columnNamePattern);
            }
            if (functionNamePattern == null || functionNamePattern.length() == 0) {
                if (this.conn.getNullNamePatternMatchesAll()) {
                    functionNamePattern = "%";
                } else {
                    throw SQLError.createSQLException("Procedure name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
                }
            }
            String db = null;
            if (catalog == null) {
                if (this.conn.getNullCatalogMeansCurrent()) {
                    db = this.database;
                }
            } else {
                db = catalog;
            }
            StringBuilder sqlBuf = new StringBuilder("SELECT SPECIFIC_SCHEMA AS FUNCTION_CAT, NULL AS `FUNCTION_SCHEM`, SPECIFIC_NAME AS `FUNCTION_NAME`, ");
            sqlBuf.append("IFNULL(PARAMETER_NAME, '') AS `COLUMN_NAME`, CASE WHEN PARAMETER_MODE = 'IN' THEN ");
            sqlBuf.append(this.getJDBC4FunctionConstant(JDBC4FunctionConstant.FUNCTION_COLUMN_IN));
            sqlBuf.append(" WHEN PARAMETER_MODE = 'OUT' THEN ");
            sqlBuf.append(this.getJDBC4FunctionConstant(JDBC4FunctionConstant.FUNCTION_COLUMN_OUT));
            sqlBuf.append(" WHEN PARAMETER_MODE = 'INOUT' THEN ");
            sqlBuf.append(this.getJDBC4FunctionConstant(JDBC4FunctionConstant.FUNCTION_COLUMN_INOUT));
            sqlBuf.append(" WHEN ORDINAL_POSITION = 0 THEN ");
            sqlBuf.append(this.getJDBC4FunctionConstant(JDBC4FunctionConstant.FUNCTION_COLUMN_RETURN));
            sqlBuf.append(" ELSE ");
            sqlBuf.append(this.getJDBC4FunctionConstant(JDBC4FunctionConstant.FUNCTION_COLUMN_UNKNOWN));
            sqlBuf.append(" END AS `COLUMN_TYPE`, ");
            MysqlDefs.appendJdbcTypeMappingQuery(sqlBuf, "DATA_TYPE");
            sqlBuf.append(" AS `DATA_TYPE`, ");
            if (this.conn.getCapitalizeTypeNames()) {
                sqlBuf.append("UPPER(CASE WHEN LOCATE('unsigned', DATA_TYPE) != 0 AND LOCATE('unsigned', DATA_TYPE) = 0 THEN CONCAT(DATA_TYPE, ' unsigned') ELSE DATA_TYPE END) AS `TYPE_NAME`,");
            } else {
                sqlBuf.append("CASE WHEN LOCATE('unsigned', DATA_TYPE) != 0 AND LOCATE('unsigned', DATA_TYPE) = 0 THEN CONCAT(DATA_TYPE, ' unsigned') ELSE DATA_TYPE END AS `TYPE_NAME`,");
            }
            sqlBuf.append("NUMERIC_PRECISION AS `PRECISION`, ");
            sqlBuf.append("CASE WHEN LCASE(DATA_TYPE)='date' THEN 10 WHEN LCASE(DATA_TYPE)='time' THEN 8 WHEN LCASE(DATA_TYPE)='datetime' THEN 19 WHEN LCASE(DATA_TYPE)='timestamp' THEN 19 WHEN CHARACTER_MAXIMUM_LENGTH IS NULL THEN NUMERIC_PRECISION WHEN CHARACTER_MAXIMUM_LENGTH > 2147483647 THEN 2147483647 ELSE CHARACTER_MAXIMUM_LENGTH END AS LENGTH, ");
            sqlBuf.append("NUMERIC_SCALE AS `SCALE`, ");
            sqlBuf.append("10 AS RADIX,");
            sqlBuf.append(this.getJDBC4FunctionConstant(JDBC4FunctionConstant.FUNCTION_NULLABLE) + " AS `NULLABLE`,  NULL AS `REMARKS`, " + "CHARACTER_OCTET_LENGTH AS `CHAR_OCTET_LENGTH`,  ORDINAL_POSITION, 'YES' AS `IS_NULLABLE`, SPECIFIC_NAME " + "FROM INFORMATION_SCHEMA.PARAMETERS WHERE " + "SPECIFIC_SCHEMA LIKE ? AND SPECIFIC_NAME LIKE ? AND (PARAMETER_NAME LIKE ? OR PARAMETER_NAME IS NULL) " + "AND ROUTINE_TYPE='FUNCTION' ORDER BY SPECIFIC_SCHEMA, SPECIFIC_NAME, ORDINAL_POSITION");
            PreparedStatement pStmt = null;
            try {
                pStmt = this.prepareMetaDataSafeStatement(sqlBuf.toString());
                if (db != null) {
                    pStmt.setString(1, db);
                } else {
                    pStmt.setString(1, "%");
                }
                pStmt.setString(2, functionNamePattern);
                pStmt.setString(3, columnNamePattern);
                ResultSet rs = this.executeMetadataQuery(pStmt);
                ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(this.createFunctionColumnsFields());
                resultSet = rs;
                Object var11_10 = null;
                if (pStmt == null) break block13;
            }
            catch (Throwable throwable) {
                block14: {
                    Object var11_11 = null;
                    if (pStmt == null) break block14;
                    pStmt.close();
                }
                throw throwable;
            }
            pStmt.close();
        }
        return resultSet;
    }

    protected int getJDBC4FunctionConstant(JDBC4FunctionConstant constant) {
        return 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        ResultSet resultSet;
        block8: {
            if (functionNamePattern == null || functionNamePattern.length() == 0) {
                if (this.conn.getNullNamePatternMatchesAll()) {
                    functionNamePattern = "%";
                } else {
                    throw SQLError.createSQLException("Function name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
                }
            }
            String db = null;
            if (catalog == null) {
                if (this.conn.getNullCatalogMeansCurrent()) {
                    db = this.database;
                }
            } else {
                db = catalog;
            }
            String sql = "SELECT ROUTINE_SCHEMA AS FUNCTION_CAT, NULL AS FUNCTION_SCHEM, ROUTINE_NAME AS FUNCTION_NAME, ROUTINE_COMMENT AS REMARKS, " + this.getJDBC4FunctionNoTableConstant() + " AS FUNCTION_TYPE, ROUTINE_NAME AS SPECIFIC_NAME FROM INFORMATION_SCHEMA.ROUTINES " + "WHERE ROUTINE_TYPE LIKE 'FUNCTION' AND ROUTINE_SCHEMA LIKE ? AND " + "ROUTINE_NAME LIKE ? ORDER BY FUNCTION_CAT, FUNCTION_SCHEM, FUNCTION_NAME, SPECIFIC_NAME";
            PreparedStatement pStmt = null;
            try {
                pStmt = this.prepareMetaDataSafeStatement(sql);
                pStmt.setString(1, db != null ? db : "%");
                pStmt.setString(2, functionNamePattern);
                ResultSet rs = this.executeMetadataQuery(pStmt);
                ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(new Field[]{new Field("", "FUNCTION_CAT", 1, 255), new Field("", "FUNCTION_SCHEM", 1, 255), new Field("", "FUNCTION_NAME", 1, 255), new Field("", "REMARKS", 1, 255), new Field("", "FUNCTION_TYPE", 5, 6), new Field("", "SPECIFIC_NAME", 1, 255)});
                resultSet = rs;
                Object var10_9 = null;
                if (pStmt == null) break block8;
            }
            catch (Throwable throwable) {
                block9: {
                    Object var10_10 = null;
                    if (pStmt == null) break block9;
                    pStmt.close();
                }
                throw throwable;
            }
            pStmt.close();
        }
        return resultSet;
    }

    protected int getJDBC4FunctionNoTableConstant() {
        return 0;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    protected static enum JDBC4FunctionConstant {
        FUNCTION_COLUMN_UNKNOWN,
        FUNCTION_COLUMN_IN,
        FUNCTION_COLUMN_INOUT,
        FUNCTION_COLUMN_OUT,
        FUNCTION_COLUMN_RETURN,
        FUNCTION_COLUMN_RESULT,
        FUNCTION_NO_NULLS,
        FUNCTION_NULLABLE,
        FUNCTION_NULLABLE_UNKNOWN;

    }
}


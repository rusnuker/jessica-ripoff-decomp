/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.DatabaseMetaData;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.SQLError;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

public class JDBC4DatabaseMetaData
extends DatabaseMetaData {
    public JDBC4DatabaseMetaData(MySQLConnection connToSet, String databaseToSet) {
        super(connToSet, databaseToSet);
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return RowIdLifetime.ROWID_UNSUPPORTED;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        try {
            return iface.cast(this);
        }
        catch (ClassCastException cce) {
            throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", this.conn.getExceptionInterceptor());
        }
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
        Field[] fields = this.createProcedureColumnsFields();
        return this.getProcedureOrFunctionColumns(fields, catalog, schemaPattern, procedureNamePattern, columnNamePattern, true, this.conn.getGetProceduresReturnsFunctions());
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
        Field[] fields = this.createFieldMetadataForGetProcedures();
        return this.getProceduresAndOrFunctions(fields, catalog, schemaPattern, procedureNamePattern, true, this.conn.getGetProceduresReturnsFunctions());
    }

    @Override
    protected int getJDBC4FunctionNoTableConstant() {
        return 1;
    }

    @Override
    protected int getColumnType(boolean isOutParam, boolean isInParam, boolean isReturnParam, boolean forGetFunctionColumns) {
        return JDBC4DatabaseMetaData.getProcedureOrFunctionColumnType(isOutParam, isInParam, isReturnParam, forGetFunctionColumns);
    }

    protected static int getProcedureOrFunctionColumnType(boolean isOutParam, boolean isInParam, boolean isReturnParam, boolean forGetFunctionColumns) {
        if (isInParam && isOutParam) {
            return forGetFunctionColumns ? 2 : 2;
        }
        if (isInParam) {
            return forGetFunctionColumns ? 1 : 1;
        }
        if (isOutParam) {
            return forGetFunctionColumns ? 3 : 4;
        }
        if (isReturnParam) {
            return forGetFunctionColumns ? 4 : 5;
        }
        return forGetFunctionColumns ? 0 : 0;
    }
}


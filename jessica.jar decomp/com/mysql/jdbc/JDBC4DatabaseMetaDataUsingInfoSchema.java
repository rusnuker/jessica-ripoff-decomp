/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.DatabaseMetaDataUsingInfoSchema;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.JDBC4DatabaseMetaData;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.SQLError;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

public class JDBC4DatabaseMetaDataUsingInfoSchema
extends DatabaseMetaDataUsingInfoSchema {
    public JDBC4DatabaseMetaDataUsingInfoSchema(MySQLConnection connToSet, String databaseToSet) throws SQLException {
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
    protected ResultSet getProcedureColumnsNoISParametersView(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
        Field[] fields = this.createProcedureColumnsFields();
        return this.getProcedureOrFunctionColumns(fields, catalog, schemaPattern, procedureNamePattern, columnNamePattern, true, this.conn.getGetProceduresReturnsFunctions());
    }

    @Override
    protected String getRoutineTypeConditionForGetProcedures() {
        return this.conn.getGetProceduresReturnsFunctions() ? "" : "ROUTINE_TYPE = 'PROCEDURE' AND ";
    }

    @Override
    protected String getRoutineTypeConditionForGetProcedureColumns() {
        return this.conn.getGetProceduresReturnsFunctions() ? "" : "ROUTINE_TYPE = 'PROCEDURE' AND ";
    }

    @Override
    protected int getJDBC4FunctionConstant(DatabaseMetaDataUsingInfoSchema.JDBC4FunctionConstant constant) {
        switch (constant) {
            case FUNCTION_COLUMN_IN: {
                return 1;
            }
            case FUNCTION_COLUMN_INOUT: {
                return 2;
            }
            case FUNCTION_COLUMN_OUT: {
                return 3;
            }
            case FUNCTION_COLUMN_RETURN: {
                return 4;
            }
            case FUNCTION_COLUMN_RESULT: {
                return 5;
            }
            case FUNCTION_COLUMN_UNKNOWN: {
                return 0;
            }
            case FUNCTION_NO_NULLS: {
                return 0;
            }
            case FUNCTION_NULLABLE: {
                return 1;
            }
            case FUNCTION_NULLABLE_UNKNOWN: {
                return 2;
            }
        }
        return -1;
    }

    @Override
    protected int getJDBC4FunctionNoTableConstant() {
        return 1;
    }

    @Override
    protected int getColumnType(boolean isOutParam, boolean isInParam, boolean isReturnParam, boolean forGetFunctionColumns) {
        return JDBC4DatabaseMetaData.getProcedureOrFunctionColumnType(isOutParam, isInParam, isReturnParam, forGetFunctionColumns);
    }
}


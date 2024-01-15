/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.SQLError;
import java.sql.ParameterMetaData;
import java.sql.SQLException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class MysqlParameterMetadata
implements ParameterMetaData {
    boolean returnSimpleMetadata = false;
    ResultSetMetaData metadata = null;
    int parameterCount = 0;
    private ExceptionInterceptor exceptionInterceptor;

    MysqlParameterMetadata(Field[] fieldInfo, int parameterCount, ExceptionInterceptor exceptionInterceptor) {
        this.metadata = new ResultSetMetaData(fieldInfo, false, true, exceptionInterceptor);
        this.parameterCount = parameterCount;
        this.exceptionInterceptor = exceptionInterceptor;
    }

    MysqlParameterMetadata(int count) {
        this.parameterCount = count;
        this.returnSimpleMetadata = true;
    }

    @Override
    public int getParameterCount() throws SQLException {
        return this.parameterCount;
    }

    @Override
    public int isNullable(int arg0) throws SQLException {
        this.checkAvailable();
        return this.metadata.isNullable(arg0);
    }

    private void checkAvailable() throws SQLException {
        if (this.metadata == null || this.metadata.fields == null) {
            throw SQLError.createSQLException("Parameter metadata not available for the given statement", "S1C00", this.exceptionInterceptor);
        }
    }

    @Override
    public boolean isSigned(int arg0) throws SQLException {
        if (this.returnSimpleMetadata) {
            this.checkBounds(arg0);
            return false;
        }
        this.checkAvailable();
        return this.metadata.isSigned(arg0);
    }

    @Override
    public int getPrecision(int arg0) throws SQLException {
        if (this.returnSimpleMetadata) {
            this.checkBounds(arg0);
            return 0;
        }
        this.checkAvailable();
        return this.metadata.getPrecision(arg0);
    }

    @Override
    public int getScale(int arg0) throws SQLException {
        if (this.returnSimpleMetadata) {
            this.checkBounds(arg0);
            return 0;
        }
        this.checkAvailable();
        return this.metadata.getScale(arg0);
    }

    @Override
    public int getParameterType(int arg0) throws SQLException {
        if (this.returnSimpleMetadata) {
            this.checkBounds(arg0);
            return 12;
        }
        this.checkAvailable();
        return this.metadata.getColumnType(arg0);
    }

    @Override
    public String getParameterTypeName(int arg0) throws SQLException {
        if (this.returnSimpleMetadata) {
            this.checkBounds(arg0);
            return "VARCHAR";
        }
        this.checkAvailable();
        return this.metadata.getColumnTypeName(arg0);
    }

    @Override
    public String getParameterClassName(int arg0) throws SQLException {
        if (this.returnSimpleMetadata) {
            this.checkBounds(arg0);
            return "java.lang.String";
        }
        this.checkAvailable();
        return this.metadata.getColumnClassName(arg0);
    }

    @Override
    public int getParameterMode(int arg0) throws SQLException {
        return 1;
    }

    private void checkBounds(int paramNumber) throws SQLException {
        if (paramNumber < 1) {
            throw SQLError.createSQLException("Parameter index of '" + paramNumber + "' is invalid.", "S1009", this.exceptionInterceptor);
        }
        if (paramNumber > this.parameterCount) {
            throw SQLError.createSQLException("Parameter index of '" + paramNumber + "' is greater than number of parameters, which is '" + this.parameterCount + "'.", "S1009", this.exceptionInterceptor);
        }
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
            throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", this.exceptionInterceptor);
        }
    }
}


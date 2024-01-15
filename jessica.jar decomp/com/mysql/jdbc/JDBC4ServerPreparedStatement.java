/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.JDBC4PreparedStatementHelper;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.ServerPreparedStatement;
import java.io.Reader;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;

public class JDBC4ServerPreparedStatement
extends ServerPreparedStatement {
    public JDBC4ServerPreparedStatement(MySQLConnection conn, String sql, String catalog, int resultSetType, int resultSetConcurrency) throws SQLException {
        super(conn, sql, catalog, resultSetType, resultSetConcurrency);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        if (!this.charEncoding.equalsIgnoreCase("UTF-8") && !this.charEncoding.equalsIgnoreCase("utf8")) {
            throw SQLError.createSQLException("Can not call setNCharacterStream() when connection character set isn't UTF-8", this.getExceptionInterceptor());
        }
        this.checkClosed();
        if (reader == null) {
            this.setNull(parameterIndex, -2);
        } else {
            ServerPreparedStatement.BindValue binding = this.getBinding(parameterIndex, true);
            this.resetToType(binding, 252);
            binding.value = reader;
            binding.isLongData = true;
            binding.bindLength = this.connection.getUseStreamLengthsInPrepStmts() ? length : -1L;
        }
    }

    @Override
    public void setNClob(int parameterIndex, NClob x) throws SQLException {
        this.setNClob(parameterIndex, x.getCharacterStream(), this.connection.getUseStreamLengthsInPrepStmts() ? x.length() : -1L);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        if (!this.charEncoding.equalsIgnoreCase("UTF-8") && !this.charEncoding.equalsIgnoreCase("utf8")) {
            throw SQLError.createSQLException("Can not call setNClob() when connection character set isn't UTF-8", this.getExceptionInterceptor());
        }
        this.checkClosed();
        if (reader == null) {
            this.setNull(parameterIndex, 2011);
        } else {
            ServerPreparedStatement.BindValue binding = this.getBinding(parameterIndex, true);
            this.resetToType(binding, 252);
            binding.value = reader;
            binding.isLongData = true;
            binding.bindLength = this.connection.getUseStreamLengthsInPrepStmts() ? length : -1L;
        }
    }

    @Override
    public void setNString(int parameterIndex, String x) throws SQLException {
        if (!this.charEncoding.equalsIgnoreCase("UTF-8") && !this.charEncoding.equalsIgnoreCase("utf8")) {
            throw SQLError.createSQLException("Can not call setNString() when connection character set isn't UTF-8", this.getExceptionInterceptor());
        }
        this.setString(parameterIndex, x);
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        JDBC4PreparedStatementHelper.setRowId(this, parameterIndex, x);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        JDBC4PreparedStatementHelper.setSQLXML(this, parameterIndex, xmlObject);
    }
}


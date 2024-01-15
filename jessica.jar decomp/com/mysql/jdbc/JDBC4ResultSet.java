/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Field;
import com.mysql.jdbc.JDBC4MysqlSQLXML;
import com.mysql.jdbc.JDBC4NClob;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.NotUpdatable;
import com.mysql.jdbc.ResultSetImpl;
import com.mysql.jdbc.RowData;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.StatementImpl;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Struct;

public class JDBC4ResultSet
extends ResultSetImpl {
    public JDBC4ResultSet(long updateCount, long updateID, MySQLConnection conn, StatementImpl creatorStmt) {
        super(updateCount, updateID, conn, creatorStmt);
    }

    public JDBC4ResultSet(String catalog, Field[] fields, RowData tuples, MySQLConnection conn, StatementImpl creatorStmt) throws SQLException {
        super(catalog, fields, tuples, conn, creatorStmt);
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        this.checkColumnBounds(columnIndex);
        String fieldEncoding = this.fields[columnIndex - 1].getEncoding();
        if (fieldEncoding == null || !fieldEncoding.equals("UTF-8")) {
            throw new SQLException("Can not call getNCharacterStream() when field's charset isn't UTF-8");
        }
        return this.getCharacterStream(columnIndex);
    }

    @Override
    public Reader getNCharacterStream(String columnName) throws SQLException {
        return this.getNCharacterStream(this.findColumn(columnName));
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        this.checkColumnBounds(columnIndex);
        String fieldEncoding = this.fields[columnIndex - 1].getEncoding();
        if (fieldEncoding == null || !fieldEncoding.equals("UTF-8")) {
            throw new SQLException("Can not call getNClob() when field's charset isn't UTF-8");
        }
        if (!this.isBinaryEncoded) {
            String asString = this.getStringForNClob(columnIndex);
            if (asString == null) {
                return null;
            }
            return new JDBC4NClob(asString, this.getExceptionInterceptor());
        }
        return this.getNativeNClob(columnIndex);
    }

    @Override
    public NClob getNClob(String columnName) throws SQLException {
        return this.getNClob(this.findColumn(columnName));
    }

    protected NClob getNativeNClob(int columnIndex) throws SQLException {
        String stringVal = this.getStringForNClob(columnIndex);
        if (stringVal == null) {
            return null;
        }
        return this.getNClobFromString(stringVal, columnIndex);
    }

    private String getStringForNClob(int columnIndex) throws SQLException {
        String asString = null;
        String forcedEncoding = "UTF-8";
        try {
            byte[] asBytes = null;
            asBytes = !this.isBinaryEncoded ? this.getBytes(columnIndex) : this.getNativeBytes(columnIndex, true);
            if (asBytes != null) {
                asString = new String(asBytes, forcedEncoding);
            }
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException("Unsupported character encoding " + forcedEncoding, "S1009", this.getExceptionInterceptor());
        }
        return asString;
    }

    private final NClob getNClobFromString(String stringVal, int columnIndex) throws SQLException {
        return new JDBC4NClob(stringVal, this.getExceptionInterceptor());
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        this.checkColumnBounds(columnIndex);
        String fieldEncoding = this.fields[columnIndex - 1].getEncoding();
        if (fieldEncoding == null || !fieldEncoding.equals("UTF-8")) {
            throw new SQLException("Can not call getNString() when field's charset isn't UTF-8");
        }
        return this.getString(columnIndex);
    }

    @Override
    public String getNString(String columnName) throws SQLException {
        return this.getNString(this.findColumn(columnName));
    }

    public void updateNCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateNCharacterStream(String columnName, Reader reader, int length) throws SQLException {
        this.updateNCharacterStream(this.findColumn(columnName), reader, length);
    }

    @Override
    public void updateNClob(String columnName, NClob nClob) throws SQLException {
        this.updateNClob(this.findColumn(columnName), nClob);
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateRowId(String columnName, RowId x) throws SQLException {
        this.updateRowId(this.findColumn(columnName), x);
    }

    @Override
    public int getHoldability() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        return this.getRowId(this.findColumn(columnLabel));
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        this.checkColumnBounds(columnIndex);
        return new JDBC4MysqlSQLXML(this, columnIndex, this.getExceptionInterceptor());
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return this.getSQLXML(this.findColumn(columnLabel));
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnLabel), x);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnLabel), x, length);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnLabel), x);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnLabel), x, length);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        this.updateBlob(this.findColumn(columnLabel), inputStream);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        this.updateBlob(this.findColumn(columnLabel), inputStream, length);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnLabel), reader);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnLabel), reader, length);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        this.updateClob(this.findColumn(columnLabel), reader);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        this.updateClob(this.findColumn(columnLabel), reader, length);
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        this.updateNCharacterStream(this.findColumn(columnLabel), reader);
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        this.updateNCharacterStream(this.findColumn(columnLabel), reader, length);
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        this.updateNClob(this.findColumn(columnLabel), reader);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        this.updateNClob(this.findColumn(columnLabel), reader, length);
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        this.updateNString(this.findColumn(columnLabel), nString);
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        this.updateSQLXML(this.findColumn(columnLabel), xmlObject);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        this.checkClosed();
        return iface.isInstance(this);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        try {
            return iface.cast(this);
        }
        catch (ClassCastException cce) {
            throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", this.getExceptionInterceptor());
        }
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        if (type == null) {
            throw SQLError.createSQLException("Type parameter can not be null", "S1009", this.getExceptionInterceptor());
        }
        if (type.equals(Struct.class)) {
            throw new SQLFeatureNotSupportedException();
        }
        if (type.equals(RowId.class)) {
            return (T)this.getRowId(columnIndex);
        }
        if (type.equals(NClob.class)) {
            return (T)this.getNClob(columnIndex);
        }
        if (type.equals(SQLXML.class)) {
            return (T)this.getSQLXML(columnIndex);
        }
        return super.getObject(columnIndex, type);
    }
}


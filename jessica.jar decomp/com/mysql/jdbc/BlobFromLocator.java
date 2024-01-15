/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.ResultSetImpl;
import com.mysql.jdbc.SQLError;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BlobFromLocator
implements Blob {
    private List<String> primaryKeyColumns = null;
    private List<String> primaryKeyValues = null;
    private ResultSetImpl creatorResultSet;
    private String blobColumnName = null;
    private String tableName = null;
    private int numColsInResultSet = 0;
    private int numPrimaryKeys = 0;
    private String quotedId;
    private ExceptionInterceptor exceptionInterceptor;

    BlobFromLocator(ResultSetImpl creatorResultSetToSet, int blobColumnIndex, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        this.exceptionInterceptor = exceptionInterceptor;
        this.creatorResultSet = creatorResultSetToSet;
        this.numColsInResultSet = this.creatorResultSet.fields.length;
        this.quotedId = this.creatorResultSet.connection.getMetaData().getIdentifierQuoteString();
        if (this.numColsInResultSet > 1) {
            this.primaryKeyColumns = new ArrayList<String>();
            this.primaryKeyValues = new ArrayList<String>();
            for (int i = 0; i < this.numColsInResultSet; ++i) {
                if (!this.creatorResultSet.fields[i].isPrimaryKey()) continue;
                StringBuilder keyName = new StringBuilder();
                keyName.append(this.quotedId);
                String originalColumnName = this.creatorResultSet.fields[i].getOriginalName();
                if (originalColumnName != null && originalColumnName.length() > 0) {
                    keyName.append(originalColumnName);
                } else {
                    keyName.append(this.creatorResultSet.fields[i].getName());
                }
                keyName.append(this.quotedId);
                this.primaryKeyColumns.add(keyName.toString());
                this.primaryKeyValues.add(this.creatorResultSet.getString(i + 1));
            }
        } else {
            this.notEnoughInformationInQuery();
        }
        this.numPrimaryKeys = this.primaryKeyColumns.size();
        if (this.numPrimaryKeys == 0) {
            this.notEnoughInformationInQuery();
        }
        if (this.creatorResultSet.fields[0].getOriginalTableName() != null) {
            StringBuilder tableNameBuffer = new StringBuilder();
            String databaseName = this.creatorResultSet.fields[0].getDatabaseName();
            if (databaseName != null && databaseName.length() > 0) {
                tableNameBuffer.append(this.quotedId);
                tableNameBuffer.append(databaseName);
                tableNameBuffer.append(this.quotedId);
                tableNameBuffer.append('.');
            }
            tableNameBuffer.append(this.quotedId);
            tableNameBuffer.append(this.creatorResultSet.fields[0].getOriginalTableName());
            tableNameBuffer.append(this.quotedId);
            this.tableName = tableNameBuffer.toString();
        } else {
            StringBuilder tableNameBuffer = new StringBuilder();
            tableNameBuffer.append(this.quotedId);
            tableNameBuffer.append(this.creatorResultSet.fields[0].getTableName());
            tableNameBuffer.append(this.quotedId);
            this.tableName = tableNameBuffer.toString();
        }
        this.blobColumnName = this.quotedId + this.creatorResultSet.getString(blobColumnIndex) + this.quotedId;
    }

    private void notEnoughInformationInQuery() throws SQLException {
        throw SQLError.createSQLException("Emulated BLOB locators must come from a ResultSet with only one table selected, and all primary keys selected", "S1000", this.exceptionInterceptor);
    }

    public OutputStream setBinaryStream(long indexToWriteAt) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public InputStream getBinaryStream() throws SQLException {
        return new BufferedInputStream(new LocatorInputStream(), this.creatorResultSet.connection.getLocatorFetchBufferSize());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int setBytes(long writeAt, byte[] bytes, int offset, int length) throws SQLException {
        block10: {
            int i;
            PreparedStatement pStmt = null;
            if (offset + length > bytes.length) {
                length = bytes.length - offset;
            }
            byte[] bytesToWrite = new byte[length];
            System.arraycopy(bytes, offset, bytesToWrite, 0, length);
            StringBuilder query = new StringBuilder("UPDATE ");
            query.append(this.tableName);
            query.append(" SET ");
            query.append(this.blobColumnName);
            query.append(" = INSERT(");
            query.append(this.blobColumnName);
            query.append(", ");
            query.append(writeAt);
            query.append(", ");
            query.append(length);
            query.append(", ?) WHERE ");
            query.append(this.primaryKeyColumns.get(0));
            query.append(" = ?");
            for (i = 1; i < this.numPrimaryKeys; ++i) {
                query.append(" AND ");
                query.append(this.primaryKeyColumns.get(i));
                query.append(" = ?");
            }
            try {
                pStmt = this.creatorResultSet.connection.prepareStatement(query.toString());
                pStmt.setBytes(1, bytesToWrite);
                for (i = 0; i < this.numPrimaryKeys; ++i) {
                    pStmt.setString(i + 2, this.primaryKeyValues.get(i));
                }
                int rowsUpdated = pStmt.executeUpdate();
                if (rowsUpdated != 1) {
                    throw SQLError.createSQLException("BLOB data not found! Did primary keys change?", "S1000", this.exceptionInterceptor);
                }
                Object var11_9 = null;
                if (pStmt == null) break block10;
            }
            catch (Throwable throwable) {
                Object var11_10 = null;
                if (pStmt != null) {
                    try {
                        pStmt.close();
                    }
                    catch (SQLException sqlEx) {
                        // empty catch block
                    }
                    pStmt = null;
                }
                throw throwable;
            }
            try {
                pStmt.close();
            }
            catch (SQLException sqlEx) {
                // empty catch block
            }
            pStmt = null;
            {
            }
        }
        return (int)this.length();
    }

    public int setBytes(long writeAt, byte[] bytes) throws SQLException {
        return this.setBytes(writeAt, bytes, 0, bytes.length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public byte[] getBytes(long pos, int length) throws SQLException {
        byte[] byArray;
        block5: {
            PreparedStatement pStmt = null;
            try {
                pStmt = this.createGetBytesStatement();
                byArray = this.getBytesInternal(pStmt, pos, length);
                Object var7_5 = null;
                if (pStmt == null) break block5;
            }
            catch (Throwable throwable) {
                block7: {
                    Object var7_6 = null;
                    if (pStmt == null) break block7;
                    try {
                        pStmt.close();
                    }
                    catch (SQLException sqlEx) {
                        // empty catch block
                    }
                    pStmt = null;
                }
                throw throwable;
            }
            try {
                pStmt.close();
            }
            catch (SQLException sqlEx) {
                // empty catch block
            }
            pStmt = null;
        }
        return byArray;
    }

    public long length() throws SQLException {
        block13: {
            SQLException sqlEx22;
            long l;
            PreparedStatement pStmt;
            block14: {
                int i;
                ResultSet blobRs = null;
                pStmt = null;
                StringBuilder query = new StringBuilder("SELECT LENGTH(");
                query.append(this.blobColumnName);
                query.append(") FROM ");
                query.append(this.tableName);
                query.append(" WHERE ");
                query.append(this.primaryKeyColumns.get(0));
                query.append(" = ?");
                for (i = 1; i < this.numPrimaryKeys; ++i) {
                    query.append(" AND ");
                    query.append(this.primaryKeyColumns.get(i));
                    query.append(" = ?");
                }
                try {
                    pStmt = this.creatorResultSet.connection.prepareStatement(query.toString());
                    for (i = 0; i < this.numPrimaryKeys; ++i) {
                        pStmt.setString(i + 1, this.primaryKeyValues.get(i));
                    }
                    blobRs = pStmt.executeQuery();
                    if (!blobRs.next()) break block13;
                    l = blobRs.getLong(1);
                    Object var7_6 = null;
                    if (blobRs == null) break block14;
                }
                catch (Throwable throwable) {
                    block17: {
                        SQLException sqlEx22;
                        Object var7_7 = null;
                        if (blobRs != null) {
                            try {
                                blobRs.close();
                            }
                            catch (SQLException sqlEx22) {
                                // empty catch block
                            }
                            blobRs = null;
                        }
                        if (pStmt == null) break block17;
                        try {
                            pStmt.close();
                        }
                        catch (SQLException sqlEx22) {
                            // empty catch block
                        }
                        pStmt = null;
                    }
                    throw throwable;
                }
                try {
                    blobRs.close();
                }
                catch (SQLException sqlEx22) {
                    // empty catch block
                }
                blobRs = null;
            }
            if (pStmt != null) {
                try {
                    pStmt.close();
                }
                catch (SQLException sqlEx22) {
                    // empty catch block
                }
                pStmt = null;
            }
            return l;
        }
        throw SQLError.createSQLException("BLOB data not found! Did primary keys change?", "S1000", this.exceptionInterceptor);
    }

    public long position(Blob pattern, long start) throws SQLException {
        return this.position(pattern.getBytes(0L, (int)pattern.length()), start);
    }

    public long position(byte[] pattern, long start) throws SQLException {
        block13: {
            SQLException sqlEx22;
            long l;
            PreparedStatement pStmt;
            block14: {
                int i;
                ResultSet blobRs = null;
                pStmt = null;
                StringBuilder query = new StringBuilder("SELECT LOCATE(");
                query.append("?, ");
                query.append(this.blobColumnName);
                query.append(", ");
                query.append(start);
                query.append(") FROM ");
                query.append(this.tableName);
                query.append(" WHERE ");
                query.append(this.primaryKeyColumns.get(0));
                query.append(" = ?");
                for (i = 1; i < this.numPrimaryKeys; ++i) {
                    query.append(" AND ");
                    query.append(this.primaryKeyColumns.get(i));
                    query.append(" = ?");
                }
                try {
                    pStmt = this.creatorResultSet.connection.prepareStatement(query.toString());
                    pStmt.setBytes(1, pattern);
                    for (i = 0; i < this.numPrimaryKeys; ++i) {
                        pStmt.setString(i + 2, this.primaryKeyValues.get(i));
                    }
                    blobRs = pStmt.executeQuery();
                    if (!blobRs.next()) break block13;
                    l = blobRs.getLong(1);
                    Object var10_8 = null;
                    if (blobRs == null) break block14;
                }
                catch (Throwable throwable) {
                    block17: {
                        SQLException sqlEx22;
                        Object var10_9 = null;
                        if (blobRs != null) {
                            try {
                                blobRs.close();
                            }
                            catch (SQLException sqlEx22) {
                                // empty catch block
                            }
                            blobRs = null;
                        }
                        if (pStmt == null) break block17;
                        try {
                            pStmt.close();
                        }
                        catch (SQLException sqlEx22) {
                            // empty catch block
                        }
                        pStmt = null;
                    }
                    throw throwable;
                }
                try {
                    blobRs.close();
                }
                catch (SQLException sqlEx22) {
                    // empty catch block
                }
                blobRs = null;
            }
            if (pStmt != null) {
                try {
                    pStmt.close();
                }
                catch (SQLException sqlEx22) {
                    // empty catch block
                }
                pStmt = null;
            }
            return l;
        }
        throw SQLError.createSQLException("BLOB data not found! Did primary keys change?", "S1000", this.exceptionInterceptor);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void truncate(long length) throws SQLException {
        block9: {
            int i;
            PreparedStatement pStmt = null;
            StringBuilder query = new StringBuilder("UPDATE ");
            query.append(this.tableName);
            query.append(" SET ");
            query.append(this.blobColumnName);
            query.append(" = LEFT(");
            query.append(this.blobColumnName);
            query.append(", ");
            query.append(length);
            query.append(") WHERE ");
            query.append(this.primaryKeyColumns.get(0));
            query.append(" = ?");
            for (i = 1; i < this.numPrimaryKeys; ++i) {
                query.append(" AND ");
                query.append(this.primaryKeyColumns.get(i));
                query.append(" = ?");
            }
            try {
                pStmt = this.creatorResultSet.connection.prepareStatement(query.toString());
                for (i = 0; i < this.numPrimaryKeys; ++i) {
                    pStmt.setString(i + 1, this.primaryKeyValues.get(i));
                }
                int rowsUpdated = pStmt.executeUpdate();
                if (rowsUpdated != 1) {
                    throw SQLError.createSQLException("BLOB data not found! Did primary keys change?", "S1000", this.exceptionInterceptor);
                }
                Object var7_5 = null;
                if (pStmt == null) break block9;
            }
            catch (Throwable throwable) {
                Object var7_6 = null;
                if (pStmt != null) {
                    try {
                        pStmt.close();
                    }
                    catch (SQLException sqlEx) {
                        // empty catch block
                    }
                    pStmt = null;
                }
                throw throwable;
            }
            try {
                pStmt.close();
            }
            catch (SQLException sqlEx) {
                // empty catch block
            }
            pStmt = null;
            {
            }
        }
    }

    PreparedStatement createGetBytesStatement() throws SQLException {
        StringBuilder query = new StringBuilder("SELECT SUBSTRING(");
        query.append(this.blobColumnName);
        query.append(", ");
        query.append("?");
        query.append(", ");
        query.append("?");
        query.append(") FROM ");
        query.append(this.tableName);
        query.append(" WHERE ");
        query.append(this.primaryKeyColumns.get(0));
        query.append(" = ?");
        for (int i = 1; i < this.numPrimaryKeys; ++i) {
            query.append(" AND ");
            query.append(this.primaryKeyColumns.get(i));
            query.append(" = ?");
        }
        return this.creatorResultSet.connection.prepareStatement(query.toString());
    }

    byte[] getBytesInternal(PreparedStatement pStmt, long pos, int length) throws SQLException {
        block7: {
            byte[] byArray;
            block8: {
                ResultSet blobRs = null;
                try {
                    pStmt.setLong(1, pos);
                    pStmt.setInt(2, length);
                    for (int i = 0; i < this.numPrimaryKeys; ++i) {
                        pStmt.setString(i + 3, this.primaryKeyValues.get(i));
                    }
                    blobRs = pStmt.executeQuery();
                    if (!blobRs.next()) break block7;
                    byArray = ((ResultSetImpl)blobRs).getBytes(1, true);
                    Object var8_7 = null;
                    if (blobRs == null) break block8;
                }
                catch (Throwable throwable) {
                    block10: {
                        Object var8_8 = null;
                        if (blobRs == null) break block10;
                        try {
                            blobRs.close();
                        }
                        catch (SQLException sqlEx) {
                            // empty catch block
                        }
                        blobRs = null;
                    }
                    throw throwable;
                }
                try {
                    blobRs.close();
                }
                catch (SQLException sqlEx) {
                    // empty catch block
                }
                blobRs = null;
            }
            return byArray;
        }
        throw SQLError.createSQLException("BLOB data not found! Did primary keys change?", "S1000", this.exceptionInterceptor);
    }

    public void free() throws SQLException {
        this.creatorResultSet = null;
        this.primaryKeyColumns = null;
        this.primaryKeyValues = null;
    }

    public InputStream getBinaryStream(long pos, long length) throws SQLException {
        return new LocatorInputStream(pos, length);
    }

    class LocatorInputStream
    extends InputStream {
        long currentPositionInBlob = 0L;
        long length = 0L;
        PreparedStatement pStmt = null;

        LocatorInputStream() throws SQLException {
            this.length = BlobFromLocator.this.length();
            this.pStmt = BlobFromLocator.this.createGetBytesStatement();
        }

        LocatorInputStream(long pos, long len) throws SQLException {
            this.length = pos + len;
            this.currentPositionInBlob = pos;
            long blobLength = BlobFromLocator.this.length();
            if (pos + len > blobLength) {
                throw SQLError.createSQLException(Messages.getString("Blob.invalidStreamLength", new Object[]{blobLength, pos, len}), "S1009", BlobFromLocator.this.exceptionInterceptor);
            }
            if (pos < 1L) {
                throw SQLError.createSQLException(Messages.getString("Blob.invalidStreamPos"), "S1009", BlobFromLocator.this.exceptionInterceptor);
            }
            if (pos > blobLength) {
                throw SQLError.createSQLException(Messages.getString("Blob.invalidStreamPos"), "S1009", BlobFromLocator.this.exceptionInterceptor);
            }
        }

        public int read() throws IOException {
            if (this.currentPositionInBlob + 1L > this.length) {
                return -1;
            }
            try {
                byte[] asBytes = BlobFromLocator.this.getBytesInternal(this.pStmt, this.currentPositionInBlob++ + 1L, 1);
                if (asBytes == null) {
                    return -1;
                }
                return asBytes[0];
            }
            catch (SQLException sqlEx) {
                throw new IOException(sqlEx.toString());
            }
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (this.currentPositionInBlob + 1L > this.length) {
                return -1;
            }
            try {
                byte[] asBytes = BlobFromLocator.this.getBytesInternal(this.pStmt, this.currentPositionInBlob + 1L, len);
                if (asBytes == null) {
                    return -1;
                }
                System.arraycopy(asBytes, 0, b, off, asBytes.length);
                this.currentPositionInBlob += (long)asBytes.length;
                return asBytes.length;
            }
            catch (SQLException sqlEx) {
                throw new IOException(sqlEx.toString());
            }
        }

        public int read(byte[] b) throws IOException {
            if (this.currentPositionInBlob + 1L > this.length) {
                return -1;
            }
            try {
                byte[] asBytes = BlobFromLocator.this.getBytesInternal(this.pStmt, this.currentPositionInBlob + 1L, b.length);
                if (asBytes == null) {
                    return -1;
                }
                System.arraycopy(asBytes, 0, b, 0, asBytes.length);
                this.currentPositionInBlob += (long)asBytes.length;
                return asBytes.length;
            }
            catch (SQLException sqlEx) {
                throw new IOException(sqlEx.toString());
            }
        }

        public void close() throws IOException {
            if (this.pStmt != null) {
                try {
                    this.pStmt.close();
                }
                catch (SQLException sqlEx) {
                    throw new IOException(sqlEx.toString());
                }
            }
            super.close();
        }
    }
}


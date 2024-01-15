/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.CachedResultSetMetaData;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.StatementImpl;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface ResultSetInternalMethods
extends ResultSet {
    public ResultSetInternalMethods copy() throws SQLException;

    public boolean reallyResult();

    public Object getObjectStoredProc(int var1, int var2) throws SQLException;

    public Object getObjectStoredProc(int var1, Map<Object, Object> var2, int var3) throws SQLException;

    public Object getObjectStoredProc(String var1, int var2) throws SQLException;

    public Object getObjectStoredProc(String var1, Map<Object, Object> var2, int var3) throws SQLException;

    public String getServerInfo();

    public long getUpdateCount();

    public long getUpdateID();

    public void realClose(boolean var1) throws SQLException;

    @Override
    public boolean isClosed() throws SQLException;

    public void setFirstCharOfQuery(char var1);

    public void setOwningStatement(StatementImpl var1);

    public char getFirstCharOfQuery();

    public void clearNextResult();

    public ResultSetInternalMethods getNextResultSet();

    public void setStatementUsedForFetchingRows(PreparedStatement var1);

    public void setWrapperStatement(Statement var1);

    public void buildIndexMapping() throws SQLException;

    public void initializeWithMetadata() throws SQLException;

    public void redefineFieldsForDBMD(Field[] var1);

    public void populateCachedMetaData(CachedResultSetMetaData var1) throws SQLException;

    public void initializeFromCachedMetaData(CachedResultSetMetaData var1);

    public int getBytesSize() throws SQLException;
}


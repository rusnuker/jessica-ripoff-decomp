/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.JDBC4ClientInfoProvider;
import com.mysql.jdbc.MySQLConnection;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Struct;
import java.util.Properties;

public interface JDBC4MySQLConnection
extends MySQLConnection {
    @Override
    public SQLXML createSQLXML() throws SQLException;

    @Override
    public Array createArrayOf(String var1, Object[] var2) throws SQLException;

    @Override
    public Struct createStruct(String var1, Object[] var2) throws SQLException;

    @Override
    public Properties getClientInfo() throws SQLException;

    @Override
    public String getClientInfo(String var1) throws SQLException;

    @Override
    public boolean isValid(int var1) throws SQLException;

    @Override
    public void setClientInfo(Properties var1) throws SQLClientInfoException;

    @Override
    public void setClientInfo(String var1, String var2) throws SQLClientInfoException;

    @Override
    public boolean isWrapperFor(Class<?> var1) throws SQLException;

    @Override
    public <T> T unwrap(Class<T> var1) throws SQLException;

    @Override
    public Blob createBlob();

    @Override
    public Clob createClob();

    @Override
    public NClob createNClob();

    public JDBC4ClientInfoProvider getClientInfoProviderImpl() throws SQLException;
}


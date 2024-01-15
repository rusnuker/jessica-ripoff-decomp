/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.PingTarget;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.Wrapper;
import java.io.InputStream;
import java.sql.SQLException;

public interface Statement
extends java.sql.Statement,
Wrapper {
    public void enableStreamingResults() throws SQLException;

    public void disableStreamingResults() throws SQLException;

    public void setLocalInfileInputStream(InputStream var1);

    public InputStream getLocalInfileInputStream();

    public void setPingTarget(PingTarget var1);

    public ExceptionInterceptor getExceptionInterceptor();

    public void removeOpenResultSet(ResultSetInternalMethods var1);

    public int getOpenResultSetCount();

    public void setHoldResultsOpenOverClose(boolean var1);
}


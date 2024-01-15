/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Extension;
import java.sql.SQLException;
import java.sql.Savepoint;

public interface ConnectionLifecycleInterceptor
extends Extension {
    public void close() throws SQLException;

    public boolean commit() throws SQLException;

    public boolean rollback() throws SQLException;

    public boolean rollback(Savepoint var1) throws SQLException;

    public boolean setAutoCommit(boolean var1) throws SQLException;

    public boolean setCatalog(String var1) throws SQLException;

    public boolean transactionBegun() throws SQLException;

    public boolean transactionCompleted() throws SQLException;
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.util;

import com.mysql.jdbc.Driver;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public abstract class BaseBugReport {
    private Connection conn;
    private Driver driver;

    public BaseBugReport() {
        try {
            this.driver = new Driver();
        }
        catch (SQLException ex) {
            throw new RuntimeException(ex.toString());
        }
    }

    public abstract void setUp() throws Exception;

    public abstract void tearDown() throws Exception;

    public abstract void runTest() throws Exception;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void run() throws Exception {
        try {
            this.setUp();
            this.runTest();
            Object var2_1 = null;
        }
        catch (Throwable throwable) {
            Object var2_2 = null;
            this.tearDown();
            throw throwable;
        }
        this.tearDown();
    }

    protected final void assertTrue(String message, boolean condition) throws Exception {
        if (!condition) {
            throw new Exception("Assertion failed: " + message);
        }
    }

    protected final void assertTrue(boolean condition) throws Exception {
        this.assertTrue("(no message given)", condition);
    }

    public String getUrl() {
        return "jdbc:mysql:///test";
    }

    public final synchronized Connection getConnection() throws SQLException {
        if (this.conn == null || this.conn.isClosed()) {
            this.conn = this.getNewConnection();
        }
        return this.conn;
    }

    public final synchronized Connection getNewConnection() throws SQLException {
        return this.getConnection(this.getUrl());
    }

    public final synchronized Connection getConnection(String url) throws SQLException {
        return this.getConnection(url, null);
    }

    public final synchronized Connection getConnection(String url, Properties props) throws SQLException {
        return this.driver.connect(url, props);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import java.sql.SQLException;

public class ProfilerEventHandlerFactory {
    private Connection ownerConnection = null;
    protected Log log = null;

    public static synchronized ProfilerEventHandler getInstance(MySQLConnection conn) throws SQLException {
        ProfilerEventHandler handler = conn.getProfilerEventHandlerInstance();
        if (handler == null) {
            handler = (ProfilerEventHandler)Util.getInstance(conn.getProfilerEventHandler(), new Class[0], new Object[0], conn.getExceptionInterceptor());
            conn.initializeExtension(handler);
            conn.setProfilerEventHandlerInstance(handler);
        }
        return handler;
    }

    public static synchronized void removeInstance(MySQLConnection conn) {
        ProfilerEventHandler handler = conn.getProfilerEventHandlerInstance();
        if (handler != null) {
            handler.destroy();
        }
    }

    private ProfilerEventHandlerFactory(Connection conn) {
        this.ownerConnection = conn;
        try {
            this.log = this.ownerConnection.getLog();
        }
        catch (SQLException sqlEx) {
            throw new RuntimeException("Unable to get logger from connection");
        }
    }
}


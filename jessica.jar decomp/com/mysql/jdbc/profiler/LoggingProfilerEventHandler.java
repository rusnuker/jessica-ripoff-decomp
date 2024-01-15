/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.profiler;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import java.sql.SQLException;
import java.util.Properties;

public class LoggingProfilerEventHandler
implements ProfilerEventHandler {
    private Log log;

    public void consumeEvent(ProfilerEvent evt) {
        if (evt.eventType == 0) {
            this.log.logWarn(evt);
        } else {
            this.log.logInfo(evt);
        }
    }

    public void destroy() {
        this.log = null;
    }

    public void init(Connection conn, Properties props) throws SQLException {
        this.log = conn.getLog();
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.interceptors;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StatementInterceptor;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class SessionAssociationInterceptor
implements StatementInterceptor {
    protected String currentSessionKey;
    protected static final ThreadLocal<String> sessionLocal = new ThreadLocal();

    public static final void setSessionKey(String key) {
        sessionLocal.set(key);
    }

    public static final void resetSessionKey() {
        sessionLocal.set(null);
    }

    public static final String getSessionKey() {
        return sessionLocal.get();
    }

    public boolean executeTopLevelOnly() {
        return true;
    }

    public void init(Connection conn, Properties props) throws SQLException {
    }

    public ResultSetInternalMethods postProcess(String sql, Statement interceptedStatement, ResultSetInternalMethods originalResultSet, Connection connection) throws SQLException {
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ResultSetInternalMethods preProcess(String sql, Statement interceptedStatement, Connection connection) throws SQLException {
        String key = SessionAssociationInterceptor.getSessionKey();
        if (key != null && !key.equals(this.currentSessionKey)) {
            PreparedStatement pstmt = connection.clientPrepareStatement("SET @mysql_proxy_session=?");
            try {
                pstmt.setString(1, key);
                pstmt.execute();
                Object var7_6 = null;
            }
            catch (Throwable throwable) {
                Object var7_7 = null;
                pstmt.close();
                throw throwable;
            }
            pstmt.close();
            this.currentSessionKey = key;
        }
        return null;
    }

    public void destroy() {
    }
}


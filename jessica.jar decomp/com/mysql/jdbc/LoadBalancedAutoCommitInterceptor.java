/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.LoadBalancedConnectionProxy;
import com.mysql.jdbc.LoadBalancedMySQLConnection;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StatementInterceptorV2;
import java.sql.SQLException;
import java.util.Properties;

public class LoadBalancedAutoCommitInterceptor
implements StatementInterceptorV2 {
    private int matchingAfterStatementCount = 0;
    private int matchingAfterStatementThreshold = 0;
    private String matchingAfterStatementRegex;
    private ConnectionImpl conn;
    private LoadBalancedConnectionProxy proxy = null;

    public void destroy() {
    }

    public boolean executeTopLevelOnly() {
        return false;
    }

    public void init(Connection connection, Properties props) throws SQLException {
        this.conn = (ConnectionImpl)connection;
        String autoCommitSwapThresholdAsString = props.getProperty("loadBalanceAutoCommitStatementThreshold", "0");
        try {
            this.matchingAfterStatementThreshold = Integer.parseInt(autoCommitSwapThresholdAsString);
        }
        catch (NumberFormatException nfe) {
            // empty catch block
        }
        String autoCommitSwapRegex = props.getProperty("loadBalanceAutoCommitStatementRegex", "");
        if ("".equals(autoCommitSwapRegex)) {
            return;
        }
        this.matchingAfterStatementRegex = autoCommitSwapRegex;
    }

    public ResultSetInternalMethods postProcess(String sql, Statement interceptedStatement, ResultSetInternalMethods originalResultSet, Connection connection, int warningCount, boolean noIndexUsed, boolean noGoodIndexUsed, SQLException statementException) throws SQLException {
        if (!this.conn.getAutoCommit()) {
            this.matchingAfterStatementCount = 0;
        } else {
            if (this.proxy == null && this.conn.isProxySet()) {
                MySQLConnection lcl_proxy;
                for (lcl_proxy = this.conn.getMultiHostSafeProxy(); lcl_proxy != null && !(lcl_proxy instanceof LoadBalancedMySQLConnection); lcl_proxy = lcl_proxy.getMultiHostSafeProxy()) {
                }
                if (lcl_proxy != null) {
                    this.proxy = ((LoadBalancedMySQLConnection)lcl_proxy).getThisAsProxy();
                }
            }
            if (this.proxy != null && (this.matchingAfterStatementRegex == null || sql.matches(this.matchingAfterStatementRegex))) {
                ++this.matchingAfterStatementCount;
            }
            if (this.matchingAfterStatementCount >= this.matchingAfterStatementThreshold) {
                this.matchingAfterStatementCount = 0;
                try {
                    if (this.proxy != null) {
                        this.proxy.pickNewConnection();
                    }
                }
                catch (SQLException e) {
                    // empty catch block
                }
            }
        }
        return originalResultSet;
    }

    public ResultSetInternalMethods preProcess(String sql, Statement interceptedStatement, Connection connection) throws SQLException {
        return null;
    }
}


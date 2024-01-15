/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.interceptors;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StatementInterceptor;
import com.mysql.jdbc.Util;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ServerStatusDiffInterceptor
implements StatementInterceptor {
    private Map<String, String> preExecuteValues = new HashMap<String, String>();
    private Map<String, String> postExecuteValues = new HashMap<String, String>();

    @Override
    public void init(Connection conn, Properties props) throws SQLException {
    }

    @Override
    public ResultSetInternalMethods postProcess(String sql, Statement interceptedStatement, ResultSetInternalMethods originalResultSet, Connection connection) throws SQLException {
        if (connection.versionMeetsMinimum(5, 0, 2)) {
            this.populateMapWithSessionStatusValues(connection, this.postExecuteValues);
            connection.getLog().logInfo("Server status change for statement:\n" + Util.calculateDifferences(this.preExecuteValues, this.postExecuteValues));
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void populateMapWithSessionStatusValues(Connection connection, Map<String, String> toPopulate) throws SQLException {
        java.sql.Statement stmt;
        block5: {
            stmt = null;
            ResultSet rs = null;
            try {
                toPopulate.clear();
                stmt = connection.createStatement();
                rs = stmt.executeQuery("SHOW SESSION STATUS");
                Util.resultSetToMap(toPopulate, rs);
                Object var6_5 = null;
                if (rs == null) break block5;
            }
            catch (Throwable throwable) {
                Object var6_6 = null;
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                throw throwable;
            }
            rs.close();
        }
        if (stmt != null) {
            stmt.close();
        }
    }

    @Override
    public ResultSetInternalMethods preProcess(String sql, Statement interceptedStatement, Connection connection) throws SQLException {
        if (connection.versionMeetsMinimum(5, 0, 2)) {
            this.populateMapWithSessionStatusValues(connection, this.preExecuteValues);
        }
        return null;
    }

    @Override
    public boolean executeTopLevelOnly() {
        return true;
    }

    @Override
    public void destroy() {
    }
}


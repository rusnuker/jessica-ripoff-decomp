/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.BalanceStrategy;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.LoadBalancedConnectionProxy;
import com.mysql.jdbc.SQLError;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class RandomBalanceStrategy
implements BalanceStrategy {
    @Override
    public void destroy() {
    }

    @Override
    public void init(Connection conn, Properties props) throws SQLException {
    }

    @Override
    public ConnectionImpl pickConnection(LoadBalancedConnectionProxy proxy, List<String> configuredHosts, Map<String, ConnectionImpl> liveConnections, long[] responseTimes, int numRetries) throws SQLException {
        int numHosts = configuredHosts.size();
        SQLException ex = null;
        ArrayList<String> whiteList = new ArrayList<String>(numHosts);
        whiteList.addAll(configuredHosts);
        Map<String, Long> blackList = proxy.getGlobalBlacklist();
        whiteList.removeAll(blackList.keySet());
        Map<String, Integer> whiteListMap = this.getArrayIndexMap(whiteList);
        int attempts = 0;
        while (attempts < numRetries) {
            int random = (int)Math.floor(Math.random() * (double)whiteList.size());
            if (whiteList.size() == 0) {
                throw SQLError.createSQLException("No hosts configured", null);
            }
            String hostPortSpec = (String)whiteList.get(random);
            ConnectionImpl conn = liveConnections.get(hostPortSpec);
            if (conn == null) {
                try {
                    conn = proxy.createConnectionForHost(hostPortSpec);
                }
                catch (SQLException sqlEx) {
                    ex = sqlEx;
                    if (proxy.shouldExceptionTriggerConnectionSwitch(sqlEx)) {
                        Integer whiteListIndex = whiteListMap.get(hostPortSpec);
                        if (whiteListIndex != null) {
                            whiteList.remove(whiteListIndex);
                            whiteListMap = this.getArrayIndexMap(whiteList);
                        }
                        proxy.addToGlobalBlacklist(hostPortSpec);
                        if (whiteList.size() != 0) continue;
                        ++attempts;
                        try {
                            Thread.sleep(250L);
                        }
                        catch (InterruptedException e) {
                            // empty catch block
                        }
                        whiteListMap = new HashMap<String, Integer>(numHosts);
                        whiteList.addAll(configuredHosts);
                        blackList = proxy.getGlobalBlacklist();
                        whiteList.removeAll(blackList.keySet());
                        whiteListMap = this.getArrayIndexMap(whiteList);
                        continue;
                    }
                    throw sqlEx;
                }
            }
            return conn;
        }
        if (ex != null) {
            throw ex;
        }
        return null;
    }

    private Map<String, Integer> getArrayIndexMap(List<String> l) {
        HashMap<String, Integer> m = new HashMap<String, Integer>(l.size());
        for (int i = 0; i < l.size(); ++i) {
            m.put(l.get(i), i);
        }
        return m;
    }
}


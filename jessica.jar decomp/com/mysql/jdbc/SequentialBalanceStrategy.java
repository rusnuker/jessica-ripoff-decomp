/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.BalanceStrategy;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.LoadBalancedConnectionProxy;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class SequentialBalanceStrategy
implements BalanceStrategy {
    private int currentHostIndex = -1;

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
        Map<String, Long> blackList = proxy.getGlobalBlacklist();
        int attempts = 0;
        while (attempts < numRetries) {
            if (numHosts == 1) {
                this.currentHostIndex = 0;
            } else if (this.currentHostIndex == -1) {
                int random;
                int i;
                for (i = random = (int)Math.floor(Math.random() * (double)numHosts); i < numHosts; ++i) {
                    if (blackList.containsKey(configuredHosts.get(i))) continue;
                    this.currentHostIndex = i;
                    break;
                }
                if (this.currentHostIndex == -1) {
                    for (i = 0; i < random; ++i) {
                        if (blackList.containsKey(configuredHosts.get(i))) continue;
                        this.currentHostIndex = i;
                        break;
                    }
                }
                if (this.currentHostIndex == -1) {
                    blackList = proxy.getGlobalBlacklist();
                    try {
                        Thread.sleep(250L);
                    }
                    catch (InterruptedException e) {}
                    continue;
                }
            } else {
                int i;
                boolean foundGoodHost = false;
                for (i = this.currentHostIndex + 1; i < numHosts; ++i) {
                    if (blackList.containsKey(configuredHosts.get(i))) continue;
                    this.currentHostIndex = i;
                    foundGoodHost = true;
                    break;
                }
                if (!foundGoodHost) {
                    for (i = 0; i < this.currentHostIndex; ++i) {
                        if (blackList.containsKey(configuredHosts.get(i))) continue;
                        this.currentHostIndex = i;
                        foundGoodHost = true;
                        break;
                    }
                }
                if (!foundGoodHost) {
                    blackList = proxy.getGlobalBlacklist();
                    try {
                        Thread.sleep(250L);
                    }
                    catch (InterruptedException e) {}
                    continue;
                }
            }
            String hostPortSpec = configuredHosts.get(this.currentHostIndex);
            ConnectionImpl conn = liveConnections.get(hostPortSpec);
            if (conn == null) {
                try {
                    conn = proxy.createConnectionForHost(hostPortSpec);
                }
                catch (SQLException sqlEx) {
                    ex = sqlEx;
                    if (proxy.shouldExceptionTriggerConnectionSwitch(sqlEx)) {
                        proxy.addToGlobalBlacklist(hostPortSpec);
                        try {
                            Thread.sleep(250L);
                        }
                        catch (InterruptedException e) {}
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
}


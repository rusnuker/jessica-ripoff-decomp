/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.Extension;
import com.mysql.jdbc.LoadBalancedConnectionProxy;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface BalanceStrategy
extends Extension {
    public ConnectionImpl pickConnection(LoadBalancedConnectionProxy var1, List<String> var2, Map<String, ConnectionImpl> var3, long[] var4, int var5) throws SQLException;
}


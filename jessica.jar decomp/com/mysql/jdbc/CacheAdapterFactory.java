/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.CacheAdapter;
import com.mysql.jdbc.Connection;
import java.sql.SQLException;
import java.util.Properties;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface CacheAdapterFactory<K, V> {
    public CacheAdapter<K, V> getInstance(Connection var1, String var2, int var3, int var4, Properties var5) throws SQLException;
}


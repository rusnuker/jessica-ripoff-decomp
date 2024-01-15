/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.CacheAdapter;
import com.mysql.jdbc.CacheAdapterFactory;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.util.LRUCache;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class PerConnectionLRUFactory
implements CacheAdapterFactory<String, PreparedStatement.ParseInfo> {
    @Override
    public CacheAdapter<String, PreparedStatement.ParseInfo> getInstance(Connection forConnection, String url, int cacheMaxSize, int maxKeySize, Properties connectionProperties) throws SQLException {
        return new PerConnectionLRU(forConnection, cacheMaxSize, maxKeySize);
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    class PerConnectionLRU
    implements CacheAdapter<String, PreparedStatement.ParseInfo> {
        private final int cacheSqlLimit;
        private final LRUCache cache;
        private final Connection conn;

        protected PerConnectionLRU(Connection forConnection, int cacheMaxSize, int maxKeySize) {
            int cacheSize = cacheMaxSize;
            this.cacheSqlLimit = maxKeySize;
            this.cache = new LRUCache(cacheSize);
            this.conn = forConnection;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public PreparedStatement.ParseInfo get(String key) {
            if (key == null || key.length() > this.cacheSqlLimit) {
                return null;
            }
            Object object = this.conn.getConnectionMutex();
            synchronized (object) {
                return (PreparedStatement.ParseInfo)this.cache.get(key);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void put(String key, PreparedStatement.ParseInfo value) {
            if (key == null || key.length() > this.cacheSqlLimit) {
                return;
            }
            Object object = this.conn.getConnectionMutex();
            synchronized (object) {
                this.cache.put(key, value);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void invalidate(String key) {
            Object object = this.conn.getConnectionMutex();
            synchronized (object) {
                this.cache.remove(key);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void invalidateAll(Set<String> keys) {
            Object object = this.conn.getConnectionMutex();
            synchronized (object) {
                for (String key : keys) {
                    this.cache.remove(key);
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void invalidateAll() {
            Object object = this.conn.getConnectionMutex();
            synchronized (object) {
                this.cache.clear();
            }
        }
    }
}


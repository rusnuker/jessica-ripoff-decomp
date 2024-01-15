/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.CacheAdapter;
import com.mysql.jdbc.CacheAdapterFactory;
import com.mysql.jdbc.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class PerVmServerConfigCacheFactory
implements CacheAdapterFactory<String, Map<String, String>> {
    static final ConcurrentHashMap<String, Map<String, String>> serverConfigByUrl = new ConcurrentHashMap();
    private static final CacheAdapter<String, Map<String, String>> serverConfigCache = new CacheAdapter<String, Map<String, String>>(){

        @Override
        public Map<String, String> get(String key) {
            return serverConfigByUrl.get(key);
        }

        @Override
        public void put(String key, Map<String, String> value) {
            serverConfigByUrl.putIfAbsent(key, value);
        }

        @Override
        public void invalidate(String key) {
            serverConfigByUrl.remove(key);
        }

        @Override
        public void invalidateAll(Set<String> keys) {
            for (String key : keys) {
                serverConfigByUrl.remove(key);
            }
        }

        @Override
        public void invalidateAll() {
            serverConfigByUrl.clear();
        }
    };

    @Override
    public CacheAdapter<String, Map<String, String>> getInstance(Connection forConn, String url, int cacheMaxSize, int maxKeySize, Properties connectionProperties) throws SQLException {
        return serverConfigCache;
    }
}


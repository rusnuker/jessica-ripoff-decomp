/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import java.util.Set;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface CacheAdapter<K, V> {
    public V get(K var1);

    public void put(K var1, V var2);

    public void invalidate(K var1);

    public void invalidateAll(Set<K> var1);

    public void invalidateAll();
}


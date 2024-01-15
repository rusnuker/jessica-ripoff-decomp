/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.util;

import java.util.LinkedHashMap;
import java.util.Map;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class LRUCache
extends LinkedHashMap<Object, Object> {
    private static final long serialVersionUID = 1L;
    protected int maxElements;

    public LRUCache(int maxSize) {
        super(maxSize, 0.75f, true);
        this.maxElements = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
        return this.size() > this.maxElements;
    }
}


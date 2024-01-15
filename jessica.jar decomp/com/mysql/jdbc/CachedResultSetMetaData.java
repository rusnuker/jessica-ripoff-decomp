/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Field;
import java.sql.ResultSetMetaData;
import java.util.Map;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CachedResultSetMetaData {
    Map<String, Integer> columnNameToIndex = null;
    Field[] fields;
    Map<String, Integer> fullColumnNameToIndex = null;
    ResultSetMetaData metadata;

    public Map<String, Integer> getColumnNameToIndex() {
        return this.columnNameToIndex;
    }

    public Field[] getFields() {
        return this.fields;
    }

    public Map<String, Integer> getFullColumnNameToIndex() {
        return this.fullColumnNameToIndex;
    }

    public ResultSetMetaData getMetadata() {
        return this.metadata;
    }
}


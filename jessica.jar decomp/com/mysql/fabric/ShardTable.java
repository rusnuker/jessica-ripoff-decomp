/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric;

public class ShardTable {
    private String database;
    private String table;
    private String column;

    public ShardTable(String database, String table, String column) {
        this.database = database;
        this.table = table;
        this.column = column;
    }

    public String getDatabase() {
        return this.database;
    }

    public String getTable() {
        return this.table;
    }

    public String getColumn() {
        return this.column;
    }
}


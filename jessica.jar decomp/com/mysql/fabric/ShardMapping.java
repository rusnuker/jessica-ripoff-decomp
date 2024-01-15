/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric;

import com.mysql.fabric.ShardIndex;
import com.mysql.fabric.ShardTable;
import com.mysql.fabric.ShardingType;
import java.util.Collections;
import java.util.Set;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class ShardMapping {
    private int mappingId;
    private ShardingType shardingType;
    private String globalGroupName;
    protected Set<ShardTable> shardTables;
    protected Set<ShardIndex> shardIndices;

    public ShardMapping(int mappingId, ShardingType shardingType, String globalGroupName, Set<ShardTable> shardTables, Set<ShardIndex> shardIndices) {
        this.mappingId = mappingId;
        this.shardingType = shardingType;
        this.globalGroupName = globalGroupName;
        this.shardTables = shardTables;
        this.shardIndices = shardIndices;
    }

    public String getGroupNameForKey(String key) {
        return this.getShardIndexForKey(key).getGroupName();
    }

    protected abstract ShardIndex getShardIndexForKey(String var1);

    public int getMappingId() {
        return this.mappingId;
    }

    public ShardingType getShardingType() {
        return this.shardingType;
    }

    public String getGlobalGroupName() {
        return this.globalGroupName;
    }

    public Set<ShardTable> getShardTables() {
        return Collections.unmodifiableSet(this.shardTables);
    }

    public Set<ShardIndex> getShardIndices() {
        return Collections.unmodifiableSet(this.shardIndices);
    }
}


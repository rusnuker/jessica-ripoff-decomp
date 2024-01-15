/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric;

import com.mysql.fabric.HashShardMapping;
import com.mysql.fabric.RangeShardMapping;
import com.mysql.fabric.ShardIndex;
import com.mysql.fabric.ShardMapping;
import com.mysql.fabric.ShardTable;
import com.mysql.fabric.ShardingType;
import java.util.Set;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ShardMappingFactory {
    public ShardMapping createShardMapping(int mappingId, ShardingType shardingType, String globalGroupName, Set<ShardTable> shardTables, Set<ShardIndex> shardIndices) {
        ShardMapping sm = null;
        switch (shardingType) {
            case RANGE: {
                sm = new RangeShardMapping(mappingId, shardingType, globalGroupName, shardTables, shardIndices);
                break;
            }
            case HASH: {
                sm = new HashShardMapping(mappingId, shardingType, globalGroupName, shardTables, shardIndices);
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid ShardingType");
            }
        }
        return sm;
    }
}


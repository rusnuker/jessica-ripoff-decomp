/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric;

import com.mysql.fabric.ShardIndex;
import com.mysql.fabric.ShardMapping;
import com.mysql.fabric.ShardTable;
import com.mysql.fabric.ShardingType;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class RangeShardMapping
extends ShardMapping {
    public RangeShardMapping(int mappingId, ShardingType shardingType, String globalGroupName, Set<ShardTable> shardTables, Set<ShardIndex> shardIndices) {
        super(mappingId, shardingType, globalGroupName, shardTables, new TreeSet<ShardIndex>(RangeShardIndexSorter.instance));
        this.shardIndices.addAll(shardIndices);
    }

    @Override
    protected ShardIndex getShardIndexForKey(String stringKey) {
        Integer key = -1;
        key = Integer.parseInt(stringKey);
        for (ShardIndex i : this.shardIndices) {
            Integer lowerBound = Integer.valueOf(i.getBound());
            if (key < lowerBound) continue;
            return i;
        }
        return null;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    private static class RangeShardIndexSorter
    implements Comparator<ShardIndex> {
        public static final RangeShardIndexSorter instance = new RangeShardIndexSorter();

        private RangeShardIndexSorter() {
        }

        @Override
        public int compare(ShardIndex i1, ShardIndex i2) {
            Integer bound1 = Integer.parseInt(i1.getBound());
            Integer bound2 = Integer.parseInt(i2.getBound());
            return bound2.compareTo(bound1);
        }
    }
}


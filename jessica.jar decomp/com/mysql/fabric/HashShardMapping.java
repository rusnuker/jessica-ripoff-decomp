/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric;

import com.mysql.fabric.ShardIndex;
import com.mysql.fabric.ShardMapping;
import com.mysql.fabric.ShardTable;
import com.mysql.fabric.ShardingType;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class HashShardMapping
extends ShardMapping {
    private static final MessageDigest md5Hasher;

    public HashShardMapping(int mappingId, ShardingType shardingType, String globalGroupName, Set<ShardTable> shardTables, Set<ShardIndex> shardIndices) {
        super(mappingId, shardingType, globalGroupName, shardTables, new TreeSet<ShardIndex>(ReverseShardIndexSorter.instance));
        this.shardIndices.addAll(shardIndices);
    }

    @Override
    protected ShardIndex getShardIndexForKey(String stringKey) {
        String hashedKey = new BigInteger(1, md5Hasher.digest(stringKey.getBytes())).toString(16).toUpperCase();
        for (int i = 0; i < 32 - hashedKey.length(); ++i) {
            hashedKey = "0" + hashedKey;
        }
        for (ShardIndex i : this.shardIndices) {
            if (i.getBound().compareTo(hashedKey) > 0) continue;
            return i;
        }
        return (ShardIndex)this.shardIndices.iterator().next();
    }

    static {
        try {
            md5Hasher = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    private static class ReverseShardIndexSorter
    implements Comparator<ShardIndex> {
        public static final ReverseShardIndexSorter instance = new ReverseShardIndexSorter();

        private ReverseShardIndexSorter() {
        }

        @Override
        public int compare(ShardIndex i1, ShardIndex i2) {
            return i2.getBound().compareTo(i1.getBound());
        }
    }
}


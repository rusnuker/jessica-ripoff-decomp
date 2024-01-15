/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

public class IntegerCache {
    private static final Integer[] CACHE = new Integer[65535];

    static {
        int i = 0;
        int j = CACHE.length;
        while (i < j) {
            IntegerCache.CACHE[i] = i;
            ++i;
        }
    }

    public static Integer getInteger(int value) {
        return value > 0 && value < CACHE.length ? CACHE[value] : value;
    }
}


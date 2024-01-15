/*
 * Decompiled with CFR 0.152.
 */
package optifine;

public class IntegerCache {
    private static final int CACHE_SIZE = 4096;
    private static final Integer[] cache = IntegerCache.makeCache(4096);

    private static Integer[] makeCache(int p_makeCache_0_) {
        Integer[] ainteger = new Integer[p_makeCache_0_];
        int i = 0;
        while (i < p_makeCache_0_) {
            ainteger[i] = new Integer(i);
            ++i;
        }
        return ainteger;
    }

    public static Integer valueOf(int p_valueOf_0_) {
        return p_valueOf_0_ >= 0 && p_valueOf_0_ < 4096 ? cache[p_valueOf_0_] : new Integer(p_valueOf_0_);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.util.List;
import java.util.Random;

public class WeightedRandom {
    public static int getTotalWeight(List<? extends Item> collection) {
        int i = 0;
        int j = 0;
        int k = collection.size();
        while (j < k) {
            Item weightedrandom$item = collection.get(j);
            i += weightedrandom$item.itemWeight;
            ++j;
        }
        return i;
    }

    public static <T extends Item> T getRandomItem(Random random, List<T> collection, int totalWeight) {
        if (totalWeight <= 0) {
            throw new IllegalArgumentException();
        }
        int i = random.nextInt(totalWeight);
        return WeightedRandom.getRandomItem(collection, i);
    }

    public static <T extends Item> T getRandomItem(List<T> collection, int weight) {
        int i = 0;
        int j = collection.size();
        while (i < j) {
            Item t = (Item)collection.get(i);
            if ((weight -= t.itemWeight) < 0) {
                return (T)t;
            }
            ++i;
        }
        return null;
    }

    public static <T extends Item> T getRandomItem(Random random, List<T> collection) {
        return WeightedRandom.getRandomItem(random, collection, WeightedRandom.getTotalWeight(collection));
    }

    public static class Item {
        protected int itemWeight;

        public Item(int itemWeightIn) {
            this.itemWeight = itemWeightIn;
        }
    }
}


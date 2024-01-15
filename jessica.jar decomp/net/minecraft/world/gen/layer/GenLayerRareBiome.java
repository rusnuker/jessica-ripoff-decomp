/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.layer;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerRareBiome
extends GenLayer {
    public GenLayerRareBiome(long p_i45478_1_, GenLayer p_i45478_3_) {
        super(p_i45478_1_);
        this.parent = p_i45478_3_;
    }

    @Override
    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
        int[] aint = this.parent.getInts(areaX - 1, areaY - 1, areaWidth + 2, areaHeight + 2);
        int[] aint1 = IntCache.getIntCache(areaWidth * areaHeight);
        int i = 0;
        while (i < areaHeight) {
            int j = 0;
            while (j < areaWidth) {
                this.initChunkSeed(j + areaX, i + areaY);
                int k = aint[j + 1 + (i + 1) * (areaWidth + 2)];
                aint1[j + i * areaWidth] = this.nextInt(57) == 0 ? (k == Biome.getIdForBiome(Biomes.PLAINS) ? Biome.getIdForBiome(Biomes.MUTATED_PLAINS) : k) : k;
                ++j;
            }
            ++i;
        }
        return aint1;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.layer;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeJungle;
import net.minecraft.world.biome.BiomeMesa;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerShore
extends GenLayer {
    public GenLayerShore(long p_i2130_1_, GenLayer p_i2130_3_) {
        super(p_i2130_1_);
        this.parent = p_i2130_3_;
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
                Biome biome = Biome.getBiome(k);
                if (k == Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND)) {
                    int j2 = aint[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                    int i3 = aint[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                    int l3 = aint[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                    int k4 = aint[j + 1 + (i + 1 + 1) * (areaWidth + 2)];
                    aint1[j + i * areaWidth] = j2 != Biome.getIdForBiome(Biomes.OCEAN) && i3 != Biome.getIdForBiome(Biomes.OCEAN) && l3 != Biome.getIdForBiome(Biomes.OCEAN) && k4 != Biome.getIdForBiome(Biomes.OCEAN) ? k : Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND_SHORE);
                } else if (biome != null && biome.getBiomeClass() == BiomeJungle.class) {
                    int i2 = aint[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                    int l2 = aint[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                    int k3 = aint[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                    int j4 = aint[j + 1 + (i + 1 + 1) * (areaWidth + 2)];
                    aint1[j + i * areaWidth] = this.isJungleCompatible(i2) && this.isJungleCompatible(l2) && this.isJungleCompatible(k3) && this.isJungleCompatible(j4) ? (!(GenLayerShore.isBiomeOceanic(i2) || GenLayerShore.isBiomeOceanic(l2) || GenLayerShore.isBiomeOceanic(k3) || GenLayerShore.isBiomeOceanic(j4)) ? k : Biome.getIdForBiome(Biomes.BEACH)) : Biome.getIdForBiome(Biomes.JUNGLE_EDGE);
                } else if (k != Biome.getIdForBiome(Biomes.EXTREME_HILLS) && k != Biome.getIdForBiome(Biomes.EXTREME_HILLS_WITH_TREES) && k != Biome.getIdForBiome(Biomes.EXTREME_HILLS_EDGE)) {
                    if (biome != null && biome.isSnowyBiome()) {
                        this.replaceIfNeighborOcean(aint, aint1, j, i, areaWidth, k, Biome.getIdForBiome(Biomes.COLD_BEACH));
                    } else if (k != Biome.getIdForBiome(Biomes.MESA) && k != Biome.getIdForBiome(Biomes.MESA_ROCK)) {
                        if (k != Biome.getIdForBiome(Biomes.OCEAN) && k != Biome.getIdForBiome(Biomes.DEEP_OCEAN) && k != Biome.getIdForBiome(Biomes.RIVER) && k != Biome.getIdForBiome(Biomes.SWAMPLAND)) {
                            int l1 = aint[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                            int k2 = aint[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                            int j3 = aint[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                            int i4 = aint[j + 1 + (i + 1 + 1) * (areaWidth + 2)];
                            aint1[j + i * areaWidth] = !(GenLayerShore.isBiomeOceanic(l1) || GenLayerShore.isBiomeOceanic(k2) || GenLayerShore.isBiomeOceanic(j3) || GenLayerShore.isBiomeOceanic(i4)) ? k : Biome.getIdForBiome(Biomes.BEACH);
                        } else {
                            aint1[j + i * areaWidth] = k;
                        }
                    } else {
                        int l = aint[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                        int i1 = aint[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                        int j1 = aint[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                        int k1 = aint[j + 1 + (i + 1 + 1) * (areaWidth + 2)];
                        aint1[j + i * areaWidth] = !(GenLayerShore.isBiomeOceanic(l) || GenLayerShore.isBiomeOceanic(i1) || GenLayerShore.isBiomeOceanic(j1) || GenLayerShore.isBiomeOceanic(k1)) ? (this.isMesa(l) && this.isMesa(i1) && this.isMesa(j1) && this.isMesa(k1) ? k : Biome.getIdForBiome(Biomes.DESERT)) : k;
                    }
                } else {
                    this.replaceIfNeighborOcean(aint, aint1, j, i, areaWidth, k, Biome.getIdForBiome(Biomes.STONE_BEACH));
                }
                ++j;
            }
            ++i;
        }
        return aint1;
    }

    private void replaceIfNeighborOcean(int[] p_151632_1_, int[] p_151632_2_, int p_151632_3_, int p_151632_4_, int p_151632_5_, int p_151632_6_, int p_151632_7_) {
        if (GenLayerShore.isBiomeOceanic(p_151632_6_)) {
            p_151632_2_[p_151632_3_ + p_151632_4_ * p_151632_5_] = p_151632_6_;
        } else {
            int i = p_151632_1_[p_151632_3_ + 1 + (p_151632_4_ + 1 - 1) * (p_151632_5_ + 2)];
            int j = p_151632_1_[p_151632_3_ + 1 + 1 + (p_151632_4_ + 1) * (p_151632_5_ + 2)];
            int k = p_151632_1_[p_151632_3_ + 1 - 1 + (p_151632_4_ + 1) * (p_151632_5_ + 2)];
            int l = p_151632_1_[p_151632_3_ + 1 + (p_151632_4_ + 1 + 1) * (p_151632_5_ + 2)];
            p_151632_2_[p_151632_3_ + p_151632_4_ * p_151632_5_] = !GenLayerShore.isBiomeOceanic(i) && !GenLayerShore.isBiomeOceanic(j) && !GenLayerShore.isBiomeOceanic(k) && !GenLayerShore.isBiomeOceanic(l) ? p_151632_6_ : p_151632_7_;
        }
    }

    private boolean isJungleCompatible(int p_151631_1_) {
        if (Biome.getBiome(p_151631_1_) != null && Biome.getBiome(p_151631_1_).getBiomeClass() == BiomeJungle.class) {
            return true;
        }
        return p_151631_1_ == Biome.getIdForBiome(Biomes.JUNGLE_EDGE) || p_151631_1_ == Biome.getIdForBiome(Biomes.JUNGLE) || p_151631_1_ == Biome.getIdForBiome(Biomes.JUNGLE_HILLS) || p_151631_1_ == Biome.getIdForBiome(Biomes.FOREST) || p_151631_1_ == Biome.getIdForBiome(Biomes.TAIGA) || GenLayerShore.isBiomeOceanic(p_151631_1_);
    }

    private boolean isMesa(int p_151633_1_) {
        return Biome.getBiome(p_151633_1_) instanceof BiomeMesa;
    }
}


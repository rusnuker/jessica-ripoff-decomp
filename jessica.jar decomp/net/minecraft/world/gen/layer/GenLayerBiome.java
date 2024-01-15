/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.layer;

import net.minecraft.init.Biomes;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerBiome
extends GenLayer {
    private Biome[] warmBiomes = new Biome[]{Biomes.DESERT, Biomes.DESERT, Biomes.DESERT, Biomes.SAVANNA, Biomes.SAVANNA, Biomes.PLAINS};
    private final Biome[] mediumBiomes = new Biome[]{Biomes.FOREST, Biomes.ROOFED_FOREST, Biomes.EXTREME_HILLS, Biomes.PLAINS, Biomes.BIRCH_FOREST, Biomes.SWAMPLAND};
    private final Biome[] coldBiomes = new Biome[]{Biomes.FOREST, Biomes.EXTREME_HILLS, Biomes.TAIGA, Biomes.PLAINS};
    private final Biome[] iceBiomes = new Biome[]{Biomes.ICE_PLAINS, Biomes.ICE_PLAINS, Biomes.ICE_PLAINS, Biomes.COLD_TAIGA};
    private final ChunkGeneratorSettings settings;

    public GenLayerBiome(long p_i45560_1_, GenLayer p_i45560_3_, WorldType p_i45560_4_, ChunkGeneratorSettings p_i45560_5_) {
        super(p_i45560_1_);
        this.parent = p_i45560_3_;
        if (p_i45560_4_ == WorldType.DEFAULT_1_1) {
            this.warmBiomes = new Biome[]{Biomes.DESERT, Biomes.FOREST, Biomes.EXTREME_HILLS, Biomes.SWAMPLAND, Biomes.PLAINS, Biomes.TAIGA};
            this.settings = null;
        } else {
            this.settings = p_i45560_5_;
        }
    }

    @Override
    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
        int[] aint = this.parent.getInts(areaX, areaY, areaWidth, areaHeight);
        int[] aint1 = IntCache.getIntCache(areaWidth * areaHeight);
        int i = 0;
        while (i < areaHeight) {
            int j = 0;
            while (j < areaWidth) {
                this.initChunkSeed(j + areaX, i + areaY);
                int k = aint[j + i * areaWidth];
                int l = (k & 0xF00) >> 8;
                aint1[j + i * areaWidth] = this.settings != null && this.settings.fixedBiome >= 0 ? this.settings.fixedBiome : (GenLayerBiome.isBiomeOceanic(k) ? k : (k == Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND) ? k : (k == 1 ? (l > 0 ? (this.nextInt(3) == 0 ? Biome.getIdForBiome(Biomes.MESA_CLEAR_ROCK) : Biome.getIdForBiome(Biomes.MESA_ROCK)) : Biome.getIdForBiome(this.warmBiomes[this.nextInt(this.warmBiomes.length)])) : (k == 2 ? (l > 0 ? Biome.getIdForBiome(Biomes.JUNGLE) : Biome.getIdForBiome(this.mediumBiomes[this.nextInt(this.mediumBiomes.length)])) : (k == 3 ? (l > 0 ? Biome.getIdForBiome(Biomes.REDWOOD_TAIGA) : Biome.getIdForBiome(this.coldBiomes[this.nextInt(this.coldBiomes.length)])) : ((k &= 0xFFFFF0FF) == 4 ? Biome.getIdForBiome(this.iceBiomes[this.nextInt(this.iceBiomes.length)]) : Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND)))))));
                ++j;
            }
            ++i;
        }
        return aint1;
    }
}


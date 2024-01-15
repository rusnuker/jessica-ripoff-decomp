/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.chunk.storage;

import net.minecraft.init.Biomes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.NibbleArrayReader;

public class ChunkLoader {
    public static AnvilConverterData load(NBTTagCompound nbt) {
        int i = nbt.getInteger("xPos");
        int j = nbt.getInteger("zPos");
        AnvilConverterData chunkloader$anvilconverterdata = new AnvilConverterData(i, j);
        chunkloader$anvilconverterdata.blocks = nbt.getByteArray("Blocks");
        chunkloader$anvilconverterdata.data = new NibbleArrayReader(nbt.getByteArray("Data"), 7);
        chunkloader$anvilconverterdata.skyLight = new NibbleArrayReader(nbt.getByteArray("SkyLight"), 7);
        chunkloader$anvilconverterdata.blockLight = new NibbleArrayReader(nbt.getByteArray("BlockLight"), 7);
        chunkloader$anvilconverterdata.heightmap = nbt.getByteArray("HeightMap");
        chunkloader$anvilconverterdata.terrainPopulated = nbt.getBoolean("TerrainPopulated");
        chunkloader$anvilconverterdata.entities = nbt.getTagList("Entities", 10);
        chunkloader$anvilconverterdata.tileEntities = nbt.getTagList("TileEntities", 10);
        chunkloader$anvilconverterdata.tileTicks = nbt.getTagList("TileTicks", 10);
        try {
            chunkloader$anvilconverterdata.lastUpdated = nbt.getLong("LastUpdate");
        }
        catch (ClassCastException var5) {
            chunkloader$anvilconverterdata.lastUpdated = nbt.getInteger("LastUpdate");
        }
        return chunkloader$anvilconverterdata;
    }

    public static void convertToAnvilFormat(AnvilConverterData converterData, NBTTagCompound compound, BiomeProvider provider) {
        compound.setInteger("xPos", converterData.x);
        compound.setInteger("zPos", converterData.z);
        compound.setLong("LastUpdate", converterData.lastUpdated);
        int[] aint = new int[converterData.heightmap.length];
        int i = 0;
        while (i < converterData.heightmap.length) {
            aint[i] = converterData.heightmap[i];
            ++i;
        }
        compound.setIntArray("HeightMap", aint);
        compound.setBoolean("TerrainPopulated", converterData.terrainPopulated);
        NBTTagList nbttaglist = new NBTTagList();
        int j = 0;
        while (j < 8) {
            boolean flag = true;
            int k = 0;
            while (k < 16 && flag) {
                int l = 0;
                while (l < 16 && flag) {
                    int i1 = 0;
                    while (i1 < 16) {
                        int j1 = k << 11 | i1 << 7 | l + (j << 4);
                        byte k1 = converterData.blocks[j1];
                        if (k1 != 0) {
                            flag = false;
                            break;
                        }
                        ++i1;
                    }
                    ++l;
                }
                ++k;
            }
            if (!flag) {
                byte[] abyte1 = new byte[4096];
                NibbleArray nibblearray = new NibbleArray();
                NibbleArray nibblearray1 = new NibbleArray();
                NibbleArray nibblearray2 = new NibbleArray();
                int j3 = 0;
                while (j3 < 16) {
                    int l1 = 0;
                    while (l1 < 16) {
                        int i2 = 0;
                        while (i2 < 16) {
                            int j2 = j3 << 11 | i2 << 7 | l1 + (j << 4);
                            byte k2 = converterData.blocks[j2];
                            abyte1[l1 << 8 | i2 << 4 | j3] = (byte)(k2 & 0xFF);
                            nibblearray.set(j3, l1, i2, converterData.data.get(j3, l1 + (j << 4), i2));
                            nibblearray1.set(j3, l1, i2, converterData.skyLight.get(j3, l1 + (j << 4), i2));
                            nibblearray2.set(j3, l1, i2, converterData.blockLight.get(j3, l1 + (j << 4), i2));
                            ++i2;
                        }
                        ++l1;
                    }
                    ++j3;
                }
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Y", (byte)(j & 0xFF));
                nbttagcompound.setByteArray("Blocks", abyte1);
                nbttagcompound.setByteArray("Data", nibblearray.getData());
                nbttagcompound.setByteArray("SkyLight", nibblearray1.getData());
                nbttagcompound.setByteArray("BlockLight", nibblearray2.getData());
                nbttaglist.appendTag(nbttagcompound);
            }
            ++j;
        }
        compound.setTag("Sections", nbttaglist);
        byte[] abyte = new byte[256];
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int l2 = 0;
        while (l2 < 16) {
            int i3 = 0;
            while (i3 < 16) {
                blockpos$mutableblockpos.setPos(converterData.x << 4 | l2, 0, converterData.z << 4 | i3);
                abyte[i3 << 4 | l2] = (byte)(Biome.getIdForBiome(provider.getBiome(blockpos$mutableblockpos, Biomes.DEFAULT)) & 0xFF);
                ++i3;
            }
            ++l2;
        }
        compound.setByteArray("Biomes", abyte);
        compound.setTag("Entities", converterData.entities);
        compound.setTag("TileEntities", converterData.tileEntities);
        if (converterData.tileTicks != null) {
            compound.setTag("TileTicks", converterData.tileTicks);
        }
    }

    public static class AnvilConverterData {
        public long lastUpdated;
        public boolean terrainPopulated;
        public byte[] heightmap;
        public NibbleArrayReader blockLight;
        public NibbleArrayReader skyLight;
        public NibbleArrayReader data;
        public byte[] blocks;
        public NBTTagList entities;
        public NBTTagList tileEntities;
        public NBTTagList tileTicks;
        public final int x;
        public final int z;

        public AnvilConverterData(int xIn, int zIn) {
            this.x = xIn;
            this.z = zIn;
        }
    }
}


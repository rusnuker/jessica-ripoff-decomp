/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenLakes
extends WorldGenerator {
    private final Block block;

    public WorldGenLakes(Block blockIn) {
        this.block = blockIn;
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        position = position.add(-8, 0, -8);
        while (position.getY() > 5 && worldIn.isAirBlock(position)) {
            position = position.down();
        }
        if (position.getY() <= 4) {
            return false;
        }
        position = position.down(4);
        boolean[] aboolean = new boolean[2048];
        int i = rand.nextInt(4) + 4;
        int j = 0;
        while (j < i) {
            double d0 = rand.nextDouble() * 6.0 + 3.0;
            double d1 = rand.nextDouble() * 4.0 + 2.0;
            double d2 = rand.nextDouble() * 6.0 + 3.0;
            double d3 = rand.nextDouble() * (16.0 - d0 - 2.0) + 1.0 + d0 / 2.0;
            double d4 = rand.nextDouble() * (8.0 - d1 - 4.0) + 2.0 + d1 / 2.0;
            double d5 = rand.nextDouble() * (16.0 - d2 - 2.0) + 1.0 + d2 / 2.0;
            int l = 1;
            while (l < 15) {
                int i1 = 1;
                while (i1 < 15) {
                    int j1 = 1;
                    while (j1 < 7) {
                        double d6 = ((double)l - d3) / (d0 / 2.0);
                        double d7 = ((double)j1 - d4) / (d1 / 2.0);
                        double d8 = ((double)i1 - d5) / (d2 / 2.0);
                        double d9 = d6 * d6 + d7 * d7 + d8 * d8;
                        if (d9 < 1.0) {
                            aboolean[(l * 16 + i1) * 8 + j1] = true;
                        }
                        ++j1;
                    }
                    ++i1;
                }
                ++l;
            }
            ++j;
        }
        int k1 = 0;
        while (k1 < 16) {
            int l2 = 0;
            while (l2 < 16) {
                int k = 0;
                while (k < 8) {
                    boolean flag;
                    boolean bl = flag = !aboolean[(k1 * 16 + l2) * 8 + k] && (k1 < 15 && aboolean[((k1 + 1) * 16 + l2) * 8 + k] || k1 > 0 && aboolean[((k1 - 1) * 16 + l2) * 8 + k] || l2 < 15 && aboolean[(k1 * 16 + l2 + 1) * 8 + k] || l2 > 0 && aboolean[(k1 * 16 + (l2 - 1)) * 8 + k] || k < 7 && aboolean[(k1 * 16 + l2) * 8 + k + 1] || k > 0 && aboolean[(k1 * 16 + l2) * 8 + (k - 1)]);
                    if (flag) {
                        Material material = worldIn.getBlockState(position.add(k1, k, l2)).getMaterial();
                        if (k >= 4 && material.isLiquid()) {
                            return false;
                        }
                        if (k < 4 && !material.isSolid() && worldIn.getBlockState(position.add(k1, k, l2)).getBlock() != this.block) {
                            return false;
                        }
                    }
                    ++k;
                }
                ++l2;
            }
            ++k1;
        }
        int l1 = 0;
        while (l1 < 16) {
            int i3 = 0;
            while (i3 < 16) {
                int i4 = 0;
                while (i4 < 8) {
                    if (aboolean[(l1 * 16 + i3) * 8 + i4]) {
                        worldIn.setBlockState(position.add(l1, i4, i3), i4 >= 4 ? Blocks.AIR.getDefaultState() : this.block.getDefaultState(), 2);
                    }
                    ++i4;
                }
                ++i3;
            }
            ++l1;
        }
        int i2 = 0;
        while (i2 < 16) {
            int j3 = 0;
            while (j3 < 16) {
                int j4 = 4;
                while (j4 < 8) {
                    BlockPos blockpos;
                    if (aboolean[(i2 * 16 + j3) * 8 + j4] && worldIn.getBlockState(blockpos = position.add(i2, j4 - 1, j3)).getBlock() == Blocks.DIRT && worldIn.getLightFor(EnumSkyBlock.SKY, position.add(i2, j4, j3)) > 0) {
                        Biome biome = worldIn.getBiome(blockpos);
                        if (biome.topBlock.getBlock() == Blocks.MYCELIUM) {
                            worldIn.setBlockState(blockpos, Blocks.MYCELIUM.getDefaultState(), 2);
                        } else {
                            worldIn.setBlockState(blockpos, Blocks.GRASS.getDefaultState(), 2);
                        }
                    }
                    ++j4;
                }
                ++j3;
            }
            ++i2;
        }
        if (this.block.getDefaultState().getMaterial() == Material.LAVA) {
            int j2 = 0;
            while (j2 < 16) {
                int k3 = 0;
                while (k3 < 16) {
                    int k4 = 0;
                    while (k4 < 8) {
                        boolean flag1;
                        boolean bl = flag1 = !aboolean[(j2 * 16 + k3) * 8 + k4] && (j2 < 15 && aboolean[((j2 + 1) * 16 + k3) * 8 + k4] || j2 > 0 && aboolean[((j2 - 1) * 16 + k3) * 8 + k4] || k3 < 15 && aboolean[(j2 * 16 + k3 + 1) * 8 + k4] || k3 > 0 && aboolean[(j2 * 16 + (k3 - 1)) * 8 + k4] || k4 < 7 && aboolean[(j2 * 16 + k3) * 8 + k4 + 1] || k4 > 0 && aboolean[(j2 * 16 + k3) * 8 + (k4 - 1)]);
                        if (flag1 && (k4 < 4 || rand.nextInt(2) != 0) && worldIn.getBlockState(position.add(j2, k4, k3)).getMaterial().isSolid()) {
                            worldIn.setBlockState(position.add(j2, k4, k3), Blocks.STONE.getDefaultState(), 2);
                        }
                        ++k4;
                    }
                    ++k3;
                }
                ++j2;
            }
        }
        if (this.block.getDefaultState().getMaterial() == Material.WATER) {
            int k2 = 0;
            while (k2 < 16) {
                int l3 = 0;
                while (l3 < 16) {
                    int l4 = 4;
                    if (worldIn.canBlockFreezeWater(position.add(k2, 4, l3))) {
                        worldIn.setBlockState(position.add(k2, 4, l3), Blocks.ICE.getDefaultState(), 2);
                    }
                    ++l3;
                }
                ++k2;
            }
        }
        return true;
    }
}


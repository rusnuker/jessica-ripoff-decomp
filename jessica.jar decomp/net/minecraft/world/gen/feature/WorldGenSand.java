/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenSand
extends WorldGenerator {
    private final Block block;
    private final int radius;

    public WorldGenSand(Block p_i45462_1_, int p_i45462_2_) {
        this.block = p_i45462_1_;
        this.radius = p_i45462_2_;
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        if (worldIn.getBlockState(position).getMaterial() != Material.WATER) {
            return false;
        }
        int i = rand.nextInt(this.radius - 2) + 2;
        int j = 2;
        int k = position.getX() - i;
        while (k <= position.getX() + i) {
            int l = position.getZ() - i;
            while (l <= position.getZ() + i) {
                int j1;
                int i1 = k - position.getX();
                if (i1 * i1 + (j1 = l - position.getZ()) * j1 <= i * i) {
                    int k1 = position.getY() - 2;
                    while (k1 <= position.getY() + 2) {
                        BlockPos blockpos = new BlockPos(k, k1, l);
                        Block block = worldIn.getBlockState(blockpos).getBlock();
                        if (block == Blocks.DIRT || block == Blocks.GRASS) {
                            worldIn.setBlockState(blockpos, this.block.getDefaultState(), 2);
                        }
                        ++k1;
                    }
                }
                ++l;
            }
            ++k;
        }
        return true;
    }
}


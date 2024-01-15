/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenIcePath
extends WorldGenerator {
    private final Block block = Blocks.PACKED_ICE;
    private final int basePathWidth;

    public WorldGenIcePath(int basePathWidthIn) {
        this.basePathWidth = basePathWidthIn;
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        while (worldIn.isAirBlock(position) && position.getY() > 2) {
            position = position.down();
        }
        if (worldIn.getBlockState(position).getBlock() != Blocks.SNOW) {
            return false;
        }
        int i = rand.nextInt(this.basePathWidth - 2) + 2;
        boolean j = true;
        int k = position.getX() - i;
        while (k <= position.getX() + i) {
            int l = position.getZ() - i;
            while (l <= position.getZ() + i) {
                int j1;
                int i1 = k - position.getX();
                if (i1 * i1 + (j1 = l - position.getZ()) * j1 <= i * i) {
                    int k1 = position.getY() - 1;
                    while (k1 <= position.getY() + 1) {
                        BlockPos blockpos = new BlockPos(k, k1, l);
                        Block block = worldIn.getBlockState(blockpos).getBlock();
                        if (block == Blocks.DIRT || block == Blocks.SNOW || block == Blocks.ICE) {
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


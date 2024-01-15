/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenReed
extends WorldGenerator {
    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        int i = 0;
        while (i < 20) {
            BlockPos blockpos1;
            BlockPos blockpos = position.add(rand.nextInt(4) - rand.nextInt(4), 0, rand.nextInt(4) - rand.nextInt(4));
            if (worldIn.isAirBlock(blockpos) && (worldIn.getBlockState((blockpos1 = blockpos.down()).west()).getMaterial() == Material.WATER || worldIn.getBlockState(blockpos1.east()).getMaterial() == Material.WATER || worldIn.getBlockState(blockpos1.north()).getMaterial() == Material.WATER || worldIn.getBlockState(blockpos1.south()).getMaterial() == Material.WATER)) {
                int j = 2 + rand.nextInt(rand.nextInt(3) + 1);
                int k = 0;
                while (k < j) {
                    if (Blocks.REEDS.canBlockStay(worldIn, blockpos)) {
                        worldIn.setBlockState(blockpos.up(k), Blocks.REEDS.getDefaultState(), 2);
                    }
                    ++k;
                }
            }
            ++i;
        }
        return true;
    }
}


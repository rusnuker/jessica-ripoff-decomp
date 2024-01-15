/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenGlowStone1
extends WorldGenerator {
    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        if (!worldIn.isAirBlock(position)) {
            return false;
        }
        if (worldIn.getBlockState(position.up()).getBlock() != Blocks.NETHERRACK) {
            return false;
        }
        worldIn.setBlockState(position, Blocks.GLOWSTONE.getDefaultState(), 2);
        int i = 0;
        while (i < 1500) {
            BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), -rand.nextInt(12), rand.nextInt(8) - rand.nextInt(8));
            if (worldIn.getBlockState(blockpos).getMaterial() == Material.AIR) {
                int j = 0;
                EnumFacing[] enumFacingArray = EnumFacing.values();
                int n = enumFacingArray.length;
                int n2 = 0;
                while (n2 < n) {
                    EnumFacing enumfacing = enumFacingArray[n2];
                    if (worldIn.getBlockState(blockpos.offset(enumfacing)).getBlock() == Blocks.GLOWSTONE) {
                        ++j;
                    }
                    if (j > 1) break;
                    ++n2;
                }
                if (j == 1) {
                    worldIn.setBlockState(blockpos, Blocks.GLOWSTONE.getDefaultState(), 2);
                }
            }
            ++i;
        }
        return true;
    }
}


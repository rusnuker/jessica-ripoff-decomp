/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Predicates
 */
package net.minecraft.world.gen.feature;

import com.google.common.base.Predicates;
import java.util.Random;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenDesertWells
extends WorldGenerator {
    private static final BlockStateMatcher IS_SAND = BlockStateMatcher.forBlock(Blocks.SAND).where(BlockSand.VARIANT, Predicates.equalTo((Object)BlockSand.EnumType.SAND));
    private final IBlockState sandSlab = Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SAND).withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM);
    private final IBlockState sandstone = Blocks.SANDSTONE.getDefaultState();
    private final IBlockState water = Blocks.FLOWING_WATER.getDefaultState();

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        while (worldIn.isAirBlock(position) && position.getY() > 2) {
            position = position.down();
        }
        if (!IS_SAND.apply(worldIn.getBlockState(position))) {
            return false;
        }
        int i = -2;
        while (i <= 2) {
            int j = -2;
            while (j <= 2) {
                if (worldIn.isAirBlock(position.add(i, -1, j)) && worldIn.isAirBlock(position.add(i, -2, j))) {
                    return false;
                }
                ++j;
            }
            ++i;
        }
        int l = -1;
        while (l <= 0) {
            int l1 = -2;
            while (l1 <= 2) {
                int k = -2;
                while (k <= 2) {
                    worldIn.setBlockState(position.add(l1, l, k), this.sandstone, 2);
                    ++k;
                }
                ++l1;
            }
            ++l;
        }
        worldIn.setBlockState(position, this.water, 2);
        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            worldIn.setBlockState(position.offset(enumfacing), this.water, 2);
        }
        int i1 = -2;
        while (i1 <= 2) {
            int i2 = -2;
            while (i2 <= 2) {
                if (i1 == -2 || i1 == 2 || i2 == -2 || i2 == 2) {
                    worldIn.setBlockState(position.add(i1, 1, i2), this.sandstone, 2);
                }
                ++i2;
            }
            ++i1;
        }
        worldIn.setBlockState(position.add(2, 1, 0), this.sandSlab, 2);
        worldIn.setBlockState(position.add(-2, 1, 0), this.sandSlab, 2);
        worldIn.setBlockState(position.add(0, 1, 2), this.sandSlab, 2);
        worldIn.setBlockState(position.add(0, 1, -2), this.sandSlab, 2);
        int j1 = -1;
        while (j1 <= 1) {
            int j2 = -1;
            while (j2 <= 1) {
                if (j1 == 0 && j2 == 0) {
                    worldIn.setBlockState(position.add(j1, 4, j2), this.sandstone, 2);
                } else {
                    worldIn.setBlockState(position.add(j1, 4, j2), this.sandSlab, 2);
                }
                ++j2;
            }
            ++j1;
        }
        int k1 = 1;
        while (k1 <= 3) {
            worldIn.setBlockState(position.add(-1, k1, -1), this.sandstone, 2);
            worldIn.setBlockState(position.add(-1, k1, 1), this.sandstone, 2);
            worldIn.setBlockState(position.add(1, k1, -1), this.sandstone, 2);
            worldIn.setBlockState(position.add(1, k1, 1), this.sandstone, 2);
            ++k1;
        }
        return true;
    }
}


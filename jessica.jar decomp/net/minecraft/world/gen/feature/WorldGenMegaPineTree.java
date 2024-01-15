/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenHugeTrees;

public class WorldGenMegaPineTree
extends WorldGenHugeTrees {
    private static final IBlockState TRUNK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
    private static final IBlockState LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLeaves.CHECK_DECAY, false);
    private static final IBlockState PODZOL = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
    private final boolean useBaseHeight;

    public WorldGenMegaPineTree(boolean notify, boolean p_i45457_2_) {
        super(notify, 13, 15, TRUNK, LEAF);
        this.useBaseHeight = p_i45457_2_;
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        int i = this.getHeight(rand);
        if (!this.ensureGrowable(worldIn, rand, position, i)) {
            return false;
        }
        this.createCrown(worldIn, position.getX(), position.getZ(), position.getY() + i, 0, rand);
        int j = 0;
        while (j < i) {
            IBlockState iblockstate = worldIn.getBlockState(position.up(j));
            if (iblockstate.getMaterial() == Material.AIR || iblockstate.getMaterial() == Material.LEAVES) {
                this.setBlockAndNotifyAdequately(worldIn, position.up(j), this.woodMetadata);
            }
            if (j < i - 1) {
                iblockstate = worldIn.getBlockState(position.add(1, j, 0));
                if (iblockstate.getMaterial() == Material.AIR || iblockstate.getMaterial() == Material.LEAVES) {
                    this.setBlockAndNotifyAdequately(worldIn, position.add(1, j, 0), this.woodMetadata);
                }
                if ((iblockstate = worldIn.getBlockState(position.add(1, j, 1))).getMaterial() == Material.AIR || iblockstate.getMaterial() == Material.LEAVES) {
                    this.setBlockAndNotifyAdequately(worldIn, position.add(1, j, 1), this.woodMetadata);
                }
                if ((iblockstate = worldIn.getBlockState(position.add(0, j, 1))).getMaterial() == Material.AIR || iblockstate.getMaterial() == Material.LEAVES) {
                    this.setBlockAndNotifyAdequately(worldIn, position.add(0, j, 1), this.woodMetadata);
                }
            }
            ++j;
        }
        return true;
    }

    private void createCrown(World worldIn, int x, int z, int y, int p_150541_5_, Random rand) {
        int i = rand.nextInt(5) + (this.useBaseHeight ? this.baseHeight : 3);
        int j = 0;
        int k = y - i;
        while (k <= y) {
            int l = y - k;
            int i1 = p_150541_5_ + MathHelper.floor((float)l / (float)i * 3.5f);
            this.growLeavesLayerStrict(worldIn, new BlockPos(x, k, z), i1 + (l > 0 && i1 == j && (k & 1) == 0 ? 1 : 0));
            j = i1;
            ++k;
        }
    }

    @Override
    public void generateSaplings(World worldIn, Random random, BlockPos pos) {
        this.placePodzolCircle(worldIn, pos.west().north());
        this.placePodzolCircle(worldIn, pos.east(2).north());
        this.placePodzolCircle(worldIn, pos.west().south(2));
        this.placePodzolCircle(worldIn, pos.east(2).south(2));
        int i = 0;
        while (i < 5) {
            int j = random.nextInt(64);
            int k = j % 8;
            int l = j / 8;
            if (k == 0 || k == 7 || l == 0 || l == 7) {
                this.placePodzolCircle(worldIn, pos.add(-3 + k, 0, -3 + l));
            }
            ++i;
        }
    }

    private void placePodzolCircle(World worldIn, BlockPos center) {
        int i = -2;
        while (i <= 2) {
            int j = -2;
            while (j <= 2) {
                if (Math.abs(i) != 2 || Math.abs(j) != 2) {
                    this.placePodzolAt(worldIn, center.add(i, 0, j));
                }
                ++j;
            }
            ++i;
        }
    }

    private void placePodzolAt(World worldIn, BlockPos pos) {
        int i = 2;
        while (i >= -3) {
            BlockPos blockpos = pos.up(i);
            IBlockState iblockstate = worldIn.getBlockState(blockpos);
            Block block = iblockstate.getBlock();
            if (block == Blocks.GRASS || block == Blocks.DIRT) {
                this.setBlockAndNotifyAdequately(worldIn, blockpos, PODZOL);
                break;
            }
            if (iblockstate.getMaterial() != Material.AIR && i < 0) break;
            --i;
        }
    }
}


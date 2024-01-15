/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class WorldGenCanopyTree
extends WorldGenAbstractTree {
    private static final IBlockState DARK_OAK_LOG = Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.DARK_OAK);
    private static final IBlockState DARK_OAK_LEAVES = Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.DARK_OAK).withProperty(BlockLeaves.CHECK_DECAY, false);

    public WorldGenCanopyTree(boolean notify) {
        super(notify);
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        int i = rand.nextInt(3) + rand.nextInt(2) + 6;
        int j = position.getX();
        int k = position.getY();
        int l = position.getZ();
        if (k >= 1 && k + i + 1 < 256) {
            BlockPos blockpos = position.down();
            Block block = worldIn.getBlockState(blockpos).getBlock();
            if (block != Blocks.GRASS && block != Blocks.DIRT) {
                return false;
            }
            if (!this.placeTreeOfHeight(worldIn, position, i)) {
                return false;
            }
            this.setDirtAt(worldIn, blockpos);
            this.setDirtAt(worldIn, blockpos.east());
            this.setDirtAt(worldIn, blockpos.south());
            this.setDirtAt(worldIn, blockpos.south().east());
            EnumFacing enumfacing = EnumFacing.Plane.HORIZONTAL.random(rand);
            int i1 = i - rand.nextInt(4);
            int j1 = 2 - rand.nextInt(3);
            int k1 = j;
            int l1 = l;
            int i2 = k + i - 1;
            int j2 = 0;
            while (j2 < i) {
                int k2;
                BlockPos blockpos1;
                Material material;
                if (j2 >= i1 && j1 > 0) {
                    k1 += enumfacing.getFrontOffsetX();
                    l1 += enumfacing.getFrontOffsetZ();
                    --j1;
                }
                if ((material = worldIn.getBlockState(blockpos1 = new BlockPos(k1, k2 = k + j2, l1)).getMaterial()) == Material.AIR || material == Material.LEAVES) {
                    this.placeLogAt(worldIn, blockpos1);
                    this.placeLogAt(worldIn, blockpos1.east());
                    this.placeLogAt(worldIn, blockpos1.south());
                    this.placeLogAt(worldIn, blockpos1.east().south());
                }
                ++j2;
            }
            int i3 = -2;
            while (i3 <= 0) {
                int l3 = -2;
                while (l3 <= 0) {
                    int k4 = -1;
                    this.placeLeafAt(worldIn, k1 + i3, i2 + k4, l1 + l3);
                    this.placeLeafAt(worldIn, 1 + k1 - i3, i2 + k4, l1 + l3);
                    this.placeLeafAt(worldIn, k1 + i3, i2 + k4, 1 + l1 - l3);
                    this.placeLeafAt(worldIn, 1 + k1 - i3, i2 + k4, 1 + l1 - l3);
                    if (!(i3 <= -2 && l3 <= -1 || i3 == -1 && l3 == -2)) {
                        k4 = 1;
                        this.placeLeafAt(worldIn, k1 + i3, i2 + k4, l1 + l3);
                        this.placeLeafAt(worldIn, 1 + k1 - i3, i2 + k4, l1 + l3);
                        this.placeLeafAt(worldIn, k1 + i3, i2 + k4, 1 + l1 - l3);
                        this.placeLeafAt(worldIn, 1 + k1 - i3, i2 + k4, 1 + l1 - l3);
                    }
                    ++l3;
                }
                ++i3;
            }
            if (rand.nextBoolean()) {
                this.placeLeafAt(worldIn, k1, i2 + 2, l1);
                this.placeLeafAt(worldIn, k1 + 1, i2 + 2, l1);
                this.placeLeafAt(worldIn, k1 + 1, i2 + 2, l1 + 1);
                this.placeLeafAt(worldIn, k1, i2 + 2, l1 + 1);
            }
            int j3 = -3;
            while (j3 <= 4) {
                int i4 = -3;
                while (i4 <= 4) {
                    if (!(j3 == -3 && i4 == -3 || j3 == -3 && i4 == 4 || j3 == 4 && i4 == -3 || j3 == 4 && i4 == 4 || Math.abs(j3) >= 3 && Math.abs(i4) >= 3)) {
                        this.placeLeafAt(worldIn, k1 + j3, i2, l1 + i4);
                    }
                    ++i4;
                }
                ++j3;
            }
            int k3 = -1;
            while (k3 <= 2) {
                int j4 = -1;
                while (j4 <= 2) {
                    if ((k3 < 0 || k3 > 1 || j4 < 0 || j4 > 1) && rand.nextInt(3) <= 0) {
                        int l4 = rand.nextInt(3) + 2;
                        int i5 = 0;
                        while (i5 < l4) {
                            this.placeLogAt(worldIn, new BlockPos(j + k3, i2 - i5 - 1, l + j4));
                            ++i5;
                        }
                        int j5 = -1;
                        while (j5 <= 1) {
                            int l2 = -1;
                            while (l2 <= 1) {
                                this.placeLeafAt(worldIn, k1 + k3 + j5, i2, l1 + j4 + l2);
                                ++l2;
                            }
                            ++j5;
                        }
                        int k5 = -2;
                        while (k5 <= 2) {
                            int l5 = -2;
                            while (l5 <= 2) {
                                if (Math.abs(k5) != 2 || Math.abs(l5) != 2) {
                                    this.placeLeafAt(worldIn, k1 + k3 + k5, i2 - 1, l1 + j4 + l5);
                                }
                                ++l5;
                            }
                            ++k5;
                        }
                    }
                    ++j4;
                }
                ++k3;
            }
            return true;
        }
        return false;
    }

    private boolean placeTreeOfHeight(World worldIn, BlockPos pos, int height) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int l = 0;
        while (l <= height + 1) {
            int i1 = 1;
            if (l == 0) {
                i1 = 0;
            }
            if (l >= height - 1) {
                i1 = 2;
            }
            int j1 = -i1;
            while (j1 <= i1) {
                int k1 = -i1;
                while (k1 <= i1) {
                    if (!this.canGrowInto(worldIn.getBlockState(blockpos$mutableblockpos.setPos(i + j1, j + l, k + k1)).getBlock())) {
                        return false;
                    }
                    ++k1;
                }
                ++j1;
            }
            ++l;
        }
        return true;
    }

    private void placeLogAt(World worldIn, BlockPos pos) {
        if (this.canGrowInto(worldIn.getBlockState(pos).getBlock())) {
            this.setBlockAndNotifyAdequately(worldIn, pos, DARK_OAK_LOG);
        }
    }

    private void placeLeafAt(World worldIn, int x, int y, int z) {
        BlockPos blockpos = new BlockPos(x, y, z);
        Material material = worldIn.getBlockState(blockpos).getMaterial();
        if (material == Material.AIR) {
            this.setBlockAndNotifyAdequately(worldIn, blockpos, DARK_OAK_LEAVES);
        }
    }
}


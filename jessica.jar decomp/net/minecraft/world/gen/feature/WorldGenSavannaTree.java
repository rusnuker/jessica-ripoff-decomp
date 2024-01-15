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

public class WorldGenSavannaTree
extends WorldGenAbstractTree {
    private static final IBlockState TRUNK = Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA);
    private static final IBlockState LEAF = Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(BlockLeaves.CHECK_DECAY, false);

    public WorldGenSavannaTree(boolean doBlockNotify) {
        super(doBlockNotify);
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        int i = rand.nextInt(3) + rand.nextInt(3) + 5;
        boolean flag = true;
        if (position.getY() >= 1 && position.getY() + i + 1 <= 256) {
            int j = position.getY();
            while (j <= position.getY() + 1 + i) {
                int k = 1;
                if (j == position.getY()) {
                    k = 0;
                }
                if (j >= position.getY() + 1 + i - 2) {
                    k = 2;
                }
                BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
                int l = position.getX() - k;
                while (l <= position.getX() + k && flag) {
                    int i1 = position.getZ() - k;
                    while (i1 <= position.getZ() + k && flag) {
                        if (j >= 0 && j < 256) {
                            if (!this.canGrowInto(worldIn.getBlockState(blockpos$mutableblockpos.setPos(l, j, i1)).getBlock())) {
                                flag = false;
                            }
                        } else {
                            flag = false;
                        }
                        ++i1;
                    }
                    ++l;
                }
                ++j;
            }
            if (!flag) {
                return false;
            }
            Block block = worldIn.getBlockState(position.down()).getBlock();
            if ((block == Blocks.GRASS || block == Blocks.DIRT) && position.getY() < 256 - i - 1) {
                this.setDirtAt(worldIn, position.down());
                EnumFacing enumfacing = EnumFacing.Plane.HORIZONTAL.random(rand);
                int k2 = i - rand.nextInt(4) - 1;
                int l2 = 3 - rand.nextInt(3);
                int i3 = position.getX();
                int j1 = position.getZ();
                int k1 = 0;
                int l1 = 0;
                while (l1 < i) {
                    BlockPos blockpos;
                    Material material;
                    int i2 = position.getY() + l1;
                    if (l1 >= k2 && l2 > 0) {
                        i3 += enumfacing.getFrontOffsetX();
                        j1 += enumfacing.getFrontOffsetZ();
                        --l2;
                    }
                    if ((material = worldIn.getBlockState(blockpos = new BlockPos(i3, i2, j1)).getMaterial()) == Material.AIR || material == Material.LEAVES) {
                        this.placeLogAt(worldIn, blockpos);
                        k1 = i2;
                    }
                    ++l1;
                }
                BlockPos blockpos2 = new BlockPos(i3, k1, j1);
                int j3 = -3;
                while (j3 <= 3) {
                    int i4 = -3;
                    while (i4 <= 3) {
                        if (Math.abs(j3) != 3 || Math.abs(i4) != 3) {
                            this.placeLeafAt(worldIn, blockpos2.add(j3, 0, i4));
                        }
                        ++i4;
                    }
                    ++j3;
                }
                blockpos2 = blockpos2.up();
                int k3 = -1;
                while (k3 <= 1) {
                    int j4 = -1;
                    while (j4 <= 1) {
                        this.placeLeafAt(worldIn, blockpos2.add(k3, 0, j4));
                        ++j4;
                    }
                    ++k3;
                }
                this.placeLeafAt(worldIn, blockpos2.east(2));
                this.placeLeafAt(worldIn, blockpos2.west(2));
                this.placeLeafAt(worldIn, blockpos2.south(2));
                this.placeLeafAt(worldIn, blockpos2.north(2));
                i3 = position.getX();
                j1 = position.getZ();
                EnumFacing enumfacing1 = EnumFacing.Plane.HORIZONTAL.random(rand);
                if (enumfacing1 != enumfacing) {
                    int l3 = k2 - rand.nextInt(2) - 1;
                    int k4 = 1 + rand.nextInt(3);
                    k1 = 0;
                    int l4 = l3;
                    while (l4 < i && k4 > 0) {
                        if (l4 >= 1) {
                            int j2 = position.getY() + l4;
                            BlockPos blockpos1 = new BlockPos(i3 += enumfacing1.getFrontOffsetX(), j2, j1 += enumfacing1.getFrontOffsetZ());
                            Material material1 = worldIn.getBlockState(blockpos1).getMaterial();
                            if (material1 == Material.AIR || material1 == Material.LEAVES) {
                                this.placeLogAt(worldIn, blockpos1);
                                k1 = j2;
                            }
                        }
                        ++l4;
                        --k4;
                    }
                    if (k1 > 0) {
                        BlockPos blockpos3 = new BlockPos(i3, k1, j1);
                        int i5 = -2;
                        while (i5 <= 2) {
                            int k5 = -2;
                            while (k5 <= 2) {
                                if (Math.abs(i5) != 2 || Math.abs(k5) != 2) {
                                    this.placeLeafAt(worldIn, blockpos3.add(i5, 0, k5));
                                }
                                ++k5;
                            }
                            ++i5;
                        }
                        blockpos3 = blockpos3.up();
                        int j5 = -1;
                        while (j5 <= 1) {
                            int l5 = -1;
                            while (l5 <= 1) {
                                this.placeLeafAt(worldIn, blockpos3.add(j5, 0, l5));
                                ++l5;
                            }
                            ++j5;
                        }
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }

    private void placeLogAt(World worldIn, BlockPos pos) {
        this.setBlockAndNotifyAdequately(worldIn, pos, TRUNK);
    }

    private void placeLeafAt(World worldIn, BlockPos pos) {
        Material material = worldIn.getBlockState(pos).getMaterial();
        if (material == Material.AIR || material == Material.LEAVES) {
            this.setBlockAndNotifyAdequately(worldIn, pos, LEAF);
        }
    }
}


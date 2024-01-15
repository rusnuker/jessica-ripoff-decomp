/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class WorldGenSwamp
extends WorldGenAbstractTree {
    private static final IBlockState TRUNK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
    private static final IBlockState LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockOldLeaf.CHECK_DECAY, false);

    public WorldGenSwamp() {
        super(false);
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        int i = rand.nextInt(4) + 5;
        while (worldIn.getBlockState(position.down()).getMaterial() == Material.WATER) {
            position = position.down();
        }
        boolean flag = true;
        if (position.getY() >= 1 && position.getY() + i + 1 <= 256) {
            int j = position.getY();
            while (j <= position.getY() + 1 + i) {
                int k = 1;
                if (j == position.getY()) {
                    k = 0;
                }
                if (j >= position.getY() + 1 + i - 2) {
                    k = 3;
                }
                BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
                int l = position.getX() - k;
                while (l <= position.getX() + k && flag) {
                    int i1 = position.getZ() - k;
                    while (i1 <= position.getZ() + k && flag) {
                        if (j >= 0 && j < 256) {
                            IBlockState iblockstate = worldIn.getBlockState(blockpos$mutableblockpos.setPos(l, j, i1));
                            Block block = iblockstate.getBlock();
                            if (iblockstate.getMaterial() != Material.AIR && iblockstate.getMaterial() != Material.LEAVES) {
                                if (block != Blocks.WATER && block != Blocks.FLOWING_WATER) {
                                    flag = false;
                                } else if (j > position.getY()) {
                                    flag = false;
                                }
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
            Block block1 = worldIn.getBlockState(position.down()).getBlock();
            if ((block1 == Blocks.GRASS || block1 == Blocks.DIRT) && position.getY() < 256 - i - 1) {
                this.setDirtAt(worldIn, position.down());
                int k1 = position.getY() - 3 + i;
                while (k1 <= position.getY() + i) {
                    int j2 = k1 - (position.getY() + i);
                    int l2 = 2 - j2 / 2;
                    int j3 = position.getX() - l2;
                    while (j3 <= position.getX() + l2) {
                        int k3 = j3 - position.getX();
                        int i4 = position.getZ() - l2;
                        while (i4 <= position.getZ() + l2) {
                            BlockPos blockpos;
                            int j1 = i4 - position.getZ();
                            if ((Math.abs(k3) != l2 || Math.abs(j1) != l2 || rand.nextInt(2) != 0 && j2 != 0) && !worldIn.getBlockState(blockpos = new BlockPos(j3, k1, i4)).isFullBlock()) {
                                this.setBlockAndNotifyAdequately(worldIn, blockpos, LEAF);
                            }
                            ++i4;
                        }
                        ++j3;
                    }
                    ++k1;
                }
                int l1 = 0;
                while (l1 < i) {
                    IBlockState iblockstate1 = worldIn.getBlockState(position.up(l1));
                    Block block2 = iblockstate1.getBlock();
                    if (iblockstate1.getMaterial() == Material.AIR || iblockstate1.getMaterial() == Material.LEAVES || block2 == Blocks.FLOWING_WATER || block2 == Blocks.WATER) {
                        this.setBlockAndNotifyAdequately(worldIn, position.up(l1), TRUNK);
                    }
                    ++l1;
                }
                int i2 = position.getY() - 3 + i;
                while (i2 <= position.getY() + i) {
                    int k2 = i2 - (position.getY() + i);
                    int i3 = 2 - k2 / 2;
                    BlockPos.MutableBlockPos blockpos$mutableblockpos1 = new BlockPos.MutableBlockPos();
                    int l3 = position.getX() - i3;
                    while (l3 <= position.getX() + i3) {
                        int j4 = position.getZ() - i3;
                        while (j4 <= position.getZ() + i3) {
                            blockpos$mutableblockpos1.setPos(l3, i2, j4);
                            if (worldIn.getBlockState(blockpos$mutableblockpos1).getMaterial() == Material.LEAVES) {
                                BlockPos blockpos3 = blockpos$mutableblockpos1.west();
                                BlockPos blockpos4 = blockpos$mutableblockpos1.east();
                                BlockPos blockpos1 = blockpos$mutableblockpos1.north();
                                BlockPos blockpos2 = blockpos$mutableblockpos1.south();
                                if (rand.nextInt(4) == 0 && worldIn.getBlockState(blockpos3).getMaterial() == Material.AIR) {
                                    this.addVine(worldIn, blockpos3, BlockVine.EAST);
                                }
                                if (rand.nextInt(4) == 0 && worldIn.getBlockState(blockpos4).getMaterial() == Material.AIR) {
                                    this.addVine(worldIn, blockpos4, BlockVine.WEST);
                                }
                                if (rand.nextInt(4) == 0 && worldIn.getBlockState(blockpos1).getMaterial() == Material.AIR) {
                                    this.addVine(worldIn, blockpos1, BlockVine.SOUTH);
                                }
                                if (rand.nextInt(4) == 0 && worldIn.getBlockState(blockpos2).getMaterial() == Material.AIR) {
                                    this.addVine(worldIn, blockpos2, BlockVine.NORTH);
                                }
                            }
                            ++j4;
                        }
                        ++l3;
                    }
                    ++i2;
                }
                return true;
            }
            return false;
        }
        return false;
    }

    private void addVine(World worldIn, BlockPos pos, PropertyBool prop) {
        IBlockState iblockstate = Blocks.VINE.getDefaultState().withProperty(prop, true);
        this.setBlockAndNotifyAdequately(worldIn, pos, iblockstate);
        int i = 4;
        BlockPos blockpos = pos.down();
        while (worldIn.getBlockState(blockpos).getMaterial() == Material.AIR && i > 0) {
            this.setBlockAndNotifyAdequately(worldIn, blockpos, iblockstate);
            blockpos = blockpos.down();
            --i;
        }
    }
}


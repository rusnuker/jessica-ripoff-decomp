/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class WorldGenTrees
extends WorldGenAbstractTree {
    private static final IBlockState DEFAULT_TRUNK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
    private static final IBlockState DEFAULT_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockLeaves.CHECK_DECAY, false);
    private final int minTreeHeight;
    private final boolean vinesGrow;
    private final IBlockState metaWood;
    private final IBlockState metaLeaves;

    public WorldGenTrees(boolean p_i2027_1_) {
        this(p_i2027_1_, 4, DEFAULT_TRUNK, DEFAULT_LEAF, false);
    }

    public WorldGenTrees(boolean p_i46446_1_, int p_i46446_2_, IBlockState p_i46446_3_, IBlockState p_i46446_4_, boolean p_i46446_5_) {
        super(p_i46446_1_);
        this.minTreeHeight = p_i46446_2_;
        this.metaWood = p_i46446_3_;
        this.metaLeaves = p_i46446_4_;
        this.vinesGrow = p_i46446_5_;
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        int i = rand.nextInt(3) + this.minTreeHeight;
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
            if ((block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.FARMLAND) && position.getY() < 256 - i - 1) {
                this.setDirtAt(worldIn, position.down());
                int k2 = 3;
                boolean l2 = false;
                int i3 = position.getY() - 3 + i;
                while (i3 <= position.getY() + i) {
                    int i4 = i3 - (position.getY() + i);
                    int j1 = 1 - i4 / 2;
                    int k1 = position.getX() - j1;
                    while (k1 <= position.getX() + j1) {
                        int l1 = k1 - position.getX();
                        int i2 = position.getZ() - j1;
                        while (i2 <= position.getZ() + j1) {
                            BlockPos blockpos;
                            Material material;
                            int j2 = i2 - position.getZ();
                            if ((Math.abs(l1) != j1 || Math.abs(j2) != j1 || rand.nextInt(2) != 0 && i4 != 0) && ((material = worldIn.getBlockState(blockpos = new BlockPos(k1, i3, i2)).getMaterial()) == Material.AIR || material == Material.LEAVES || material == Material.VINE)) {
                                this.setBlockAndNotifyAdequately(worldIn, blockpos, this.metaLeaves);
                            }
                            ++i2;
                        }
                        ++k1;
                    }
                    ++i3;
                }
                int j3 = 0;
                while (j3 < i) {
                    Material material1 = worldIn.getBlockState(position.up(j3)).getMaterial();
                    if (material1 == Material.AIR || material1 == Material.LEAVES || material1 == Material.VINE) {
                        this.setBlockAndNotifyAdequately(worldIn, position.up(j3), this.metaWood);
                        if (this.vinesGrow && j3 > 0) {
                            if (rand.nextInt(3) > 0 && worldIn.isAirBlock(position.add(-1, j3, 0))) {
                                this.addVine(worldIn, position.add(-1, j3, 0), BlockVine.EAST);
                            }
                            if (rand.nextInt(3) > 0 && worldIn.isAirBlock(position.add(1, j3, 0))) {
                                this.addVine(worldIn, position.add(1, j3, 0), BlockVine.WEST);
                            }
                            if (rand.nextInt(3) > 0 && worldIn.isAirBlock(position.add(0, j3, -1))) {
                                this.addVine(worldIn, position.add(0, j3, -1), BlockVine.SOUTH);
                            }
                            if (rand.nextInt(3) > 0 && worldIn.isAirBlock(position.add(0, j3, 1))) {
                                this.addVine(worldIn, position.add(0, j3, 1), BlockVine.NORTH);
                            }
                        }
                    }
                    ++j3;
                }
                if (this.vinesGrow) {
                    int k3 = position.getY() - 3 + i;
                    while (k3 <= position.getY() + i) {
                        int j4 = k3 - (position.getY() + i);
                        int k4 = 2 - j4 / 2;
                        BlockPos.MutableBlockPos blockpos$mutableblockpos1 = new BlockPos.MutableBlockPos();
                        int l4 = position.getX() - k4;
                        while (l4 <= position.getX() + k4) {
                            int i5 = position.getZ() - k4;
                            while (i5 <= position.getZ() + k4) {
                                blockpos$mutableblockpos1.setPos(l4, k3, i5);
                                if (worldIn.getBlockState(blockpos$mutableblockpos1).getMaterial() == Material.LEAVES) {
                                    BlockPos blockpos2 = blockpos$mutableblockpos1.west();
                                    BlockPos blockpos3 = blockpos$mutableblockpos1.east();
                                    BlockPos blockpos4 = blockpos$mutableblockpos1.north();
                                    BlockPos blockpos1 = blockpos$mutableblockpos1.south();
                                    if (rand.nextInt(4) == 0 && worldIn.getBlockState(blockpos2).getMaterial() == Material.AIR) {
                                        this.addHangingVine(worldIn, blockpos2, BlockVine.EAST);
                                    }
                                    if (rand.nextInt(4) == 0 && worldIn.getBlockState(blockpos3).getMaterial() == Material.AIR) {
                                        this.addHangingVine(worldIn, blockpos3, BlockVine.WEST);
                                    }
                                    if (rand.nextInt(4) == 0 && worldIn.getBlockState(blockpos4).getMaterial() == Material.AIR) {
                                        this.addHangingVine(worldIn, blockpos4, BlockVine.SOUTH);
                                    }
                                    if (rand.nextInt(4) == 0 && worldIn.getBlockState(blockpos1).getMaterial() == Material.AIR) {
                                        this.addHangingVine(worldIn, blockpos1, BlockVine.NORTH);
                                    }
                                }
                                ++i5;
                            }
                            ++l4;
                        }
                        ++k3;
                    }
                    if (rand.nextInt(5) == 0 && i > 5) {
                        int l3 = 0;
                        while (l3 < 2) {
                            for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                                if (rand.nextInt(4 - l3) != 0) continue;
                                EnumFacing enumfacing1 = enumfacing.getOpposite();
                                this.placeCocoa(worldIn, rand.nextInt(3), position.add(enumfacing1.getFrontOffsetX(), i - 5 + l3, enumfacing1.getFrontOffsetZ()), enumfacing);
                            }
                            ++l3;
                        }
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }

    private void placeCocoa(World worldIn, int p_181652_2_, BlockPos pos, EnumFacing side) {
        this.setBlockAndNotifyAdequately(worldIn, pos, Blocks.COCOA.getDefaultState().withProperty(BlockCocoa.AGE, p_181652_2_).withProperty(BlockCocoa.FACING, side));
    }

    private void addVine(World worldIn, BlockPos pos, PropertyBool prop) {
        this.setBlockAndNotifyAdequately(worldIn, pos, Blocks.VINE.getDefaultState().withProperty(prop, true));
    }

    private void addHangingVine(World worldIn, BlockPos pos, PropertyBool prop) {
        this.addVine(worldIn, pos, prop);
        int i = 4;
        BlockPos blockpos = pos.down();
        while (worldIn.getBlockState(blockpos).getMaterial() == Material.AIR && i > 0) {
            this.addVine(worldIn, blockpos, prop);
            blockpos = blockpos.down();
            --i;
        }
    }
}


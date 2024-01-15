/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class WorldGenTaiga2
extends WorldGenAbstractTree {
    private static final IBlockState TRUNK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
    private static final IBlockState LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLeaves.CHECK_DECAY, false);

    public WorldGenTaiga2(boolean p_i2025_1_) {
        super(p_i2025_1_);
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        int i = rand.nextInt(4) + 6;
        int j = 1 + rand.nextInt(2);
        int k = i - j;
        int l = 2 + rand.nextInt(2);
        boolean flag = true;
        if (position.getY() >= 1 && position.getY() + i + 1 <= 256) {
            int i1 = position.getY();
            while (i1 <= position.getY() + 1 + i && flag) {
                int j1 = i1 - position.getY() < j ? 0 : l;
                BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
                int k1 = position.getX() - j1;
                while (k1 <= position.getX() + j1 && flag) {
                    int l1 = position.getZ() - j1;
                    while (l1 <= position.getZ() + j1 && flag) {
                        if (i1 >= 0 && i1 < 256) {
                            Material material = worldIn.getBlockState(blockpos$mutableblockpos.setPos(k1, i1, l1)).getMaterial();
                            if (material != Material.AIR && material != Material.LEAVES) {
                                flag = false;
                            }
                        } else {
                            flag = false;
                        }
                        ++l1;
                    }
                    ++k1;
                }
                ++i1;
            }
            if (!flag) {
                return false;
            }
            Block block = worldIn.getBlockState(position.down()).getBlock();
            if ((block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.FARMLAND) && position.getY() < 256 - i - 1) {
                this.setDirtAt(worldIn, position.down());
                int i3 = rand.nextInt(2);
                int j3 = 1;
                int k3 = 0;
                int l3 = 0;
                while (l3 <= k) {
                    int j4 = position.getY() + i - l3;
                    int i2 = position.getX() - i3;
                    while (i2 <= position.getX() + i3) {
                        int j2 = i2 - position.getX();
                        int k2 = position.getZ() - i3;
                        while (k2 <= position.getZ() + i3) {
                            BlockPos blockpos;
                            int l2 = k2 - position.getZ();
                            if (!(Math.abs(j2) == i3 && Math.abs(l2) == i3 && i3 > 0 || worldIn.getBlockState(blockpos = new BlockPos(i2, j4, k2)).isFullBlock())) {
                                this.setBlockAndNotifyAdequately(worldIn, blockpos, LEAF);
                            }
                            ++k2;
                        }
                        ++i2;
                    }
                    if (i3 >= j3) {
                        i3 = k3;
                        k3 = 1;
                        if (++j3 > l) {
                            j3 = l;
                        }
                    } else {
                        ++i3;
                    }
                    ++l3;
                }
                int i4 = rand.nextInt(3);
                int k4 = 0;
                while (k4 < i - i4) {
                    Material material1 = worldIn.getBlockState(position.up(k4)).getMaterial();
                    if (material1 == Material.AIR || material1 == Material.LEAVES) {
                        this.setBlockAndNotifyAdequately(worldIn, position.up(k4), TRUNK);
                    }
                    ++k4;
                }
                return true;
            }
            return false;
        }
        return false;
    }
}


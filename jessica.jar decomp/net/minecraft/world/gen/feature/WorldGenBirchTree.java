/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class WorldGenBirchTree
extends WorldGenAbstractTree {
    private static final IBlockState LOG = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.BIRCH);
    private static final IBlockState LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.BIRCH).withProperty(BlockOldLeaf.CHECK_DECAY, false);
    private final boolean useExtraRandomHeight;

    public WorldGenBirchTree(boolean notify, boolean useExtraRandomHeightIn) {
        super(notify);
        this.useExtraRandomHeight = useExtraRandomHeightIn;
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        int i = rand.nextInt(3) + 5;
        if (this.useExtraRandomHeight) {
            i += rand.nextInt(7);
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
                int i2 = position.getY() - 3 + i;
                while (i2 <= position.getY() + i) {
                    int k2 = i2 - (position.getY() + i);
                    int l2 = 1 - k2 / 2;
                    int i3 = position.getX() - l2;
                    while (i3 <= position.getX() + l2) {
                        int j1 = i3 - position.getX();
                        int k1 = position.getZ() - l2;
                        while (k1 <= position.getZ() + l2) {
                            BlockPos blockpos;
                            Material material;
                            int l1 = k1 - position.getZ();
                            if ((Math.abs(j1) != l2 || Math.abs(l1) != l2 || rand.nextInt(2) != 0 && k2 != 0) && ((material = worldIn.getBlockState(blockpos = new BlockPos(i3, i2, k1)).getMaterial()) == Material.AIR || material == Material.LEAVES)) {
                                this.setBlockAndNotifyAdequately(worldIn, blockpos, LEAF);
                            }
                            ++k1;
                        }
                        ++i3;
                    }
                    ++i2;
                }
                int j2 = 0;
                while (j2 < i) {
                    Material material1 = worldIn.getBlockState(position.up(j2)).getMaterial();
                    if (material1 == Material.AIR || material1 == Material.LEAVES) {
                        this.setBlockAndNotifyAdequately(worldIn, position.up(j2), LOG);
                    }
                    ++j2;
                }
                return true;
            }
            return false;
        }
        return false;
    }
}


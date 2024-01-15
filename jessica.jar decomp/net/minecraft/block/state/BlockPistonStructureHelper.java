/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.block.state;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPistonStructureHelper {
    private final World world;
    private final BlockPos pistonPos;
    private final BlockPos blockToMove;
    private final EnumFacing moveDirection;
    private final List<BlockPos> toMove = Lists.newArrayList();
    private final List<BlockPos> toDestroy = Lists.newArrayList();

    public BlockPistonStructureHelper(World worldIn, BlockPos posIn, EnumFacing pistonFacing, boolean extending) {
        this.world = worldIn;
        this.pistonPos = posIn;
        if (extending) {
            this.moveDirection = pistonFacing;
            this.blockToMove = posIn.offset(pistonFacing);
        } else {
            this.moveDirection = pistonFacing.getOpposite();
            this.blockToMove = posIn.offset(pistonFacing, 2);
        }
    }

    public boolean canMove() {
        this.toMove.clear();
        this.toDestroy.clear();
        IBlockState iblockstate = this.world.getBlockState(this.blockToMove);
        if (!BlockPistonBase.canPush(iblockstate, this.world, this.blockToMove, this.moveDirection, false, this.moveDirection)) {
            if (iblockstate.getMobilityFlag() == EnumPushReaction.DESTROY) {
                this.toDestroy.add(this.blockToMove);
                return true;
            }
            return false;
        }
        if (!this.addBlockLine(this.blockToMove, this.moveDirection)) {
            return false;
        }
        int i = 0;
        while (i < this.toMove.size()) {
            BlockPos blockpos = this.toMove.get(i);
            if (this.world.getBlockState(blockpos).getBlock() == Blocks.SLIME_BLOCK && !this.addBranchingBlocks(blockpos)) {
                return false;
            }
            ++i;
        }
        return true;
    }

    /*
     * Unable to fully structure code
     */
    private boolean addBlockLine(BlockPos origin, EnumFacing p_177251_2_) {
        iblockstate = this.world.getBlockState(origin);
        block = iblockstate.getBlock();
        if (iblockstate.getMaterial() == Material.AIR) {
            return true;
        }
        if (!BlockPistonBase.canPush(iblockstate, this.world, origin, this.moveDirection, false, p_177251_2_)) {
            return true;
        }
        if (origin.equals(this.pistonPos)) {
            return true;
        }
        if (this.toMove.contains(origin)) {
            return true;
        }
        i = 1;
        if (i + this.toMove.size() <= 12) ** GOTO lbl20
        return false;
lbl-1000:
        // 1 sources

        {
            blockpos = origin.offset(this.moveDirection.getOpposite(), i);
            iblockstate = this.world.getBlockState(blockpos);
            block = iblockstate.getBlock();
            if (iblockstate.getMaterial() == Material.AIR || !BlockPistonBase.canPush(iblockstate, this.world, blockpos, this.moveDirection, false, this.moveDirection.getOpposite()) || blockpos.equals(this.pistonPos)) break;
            if (++i + this.toMove.size() <= 12) continue;
            return false;
lbl20:
            // 2 sources

            ** while (block == Blocks.SLIME_BLOCK)
        }
lbl21:
        // 2 sources

        i1 = 0;
        j = i - 1;
        while (j >= 0) {
            this.toMove.add(origin.offset(this.moveDirection.getOpposite(), j));
            ++i1;
            --j;
        }
        j1 = 1;
        while (true) {
            if ((k = this.toMove.indexOf(blockpos1 = origin.offset(this.moveDirection, j1))) > -1) {
                this.reorderListAtCollision(i1, k);
                l = 0;
                while (l <= k + i1) {
                    blockpos2 = this.toMove.get(l);
                    if (this.world.getBlockState(blockpos2).getBlock() == Blocks.SLIME_BLOCK && !this.addBranchingBlocks(blockpos2)) {
                        return false;
                    }
                    ++l;
                }
                return true;
            }
            iblockstate = this.world.getBlockState(blockpos1);
            if (iblockstate.getMaterial() == Material.AIR) {
                return true;
            }
            if (!BlockPistonBase.canPush(iblockstate, this.world, blockpos1, this.moveDirection, true, this.moveDirection) || blockpos1.equals(this.pistonPos)) {
                return false;
            }
            if (iblockstate.getMobilityFlag() == EnumPushReaction.DESTROY) {
                this.toDestroy.add(blockpos1);
                return true;
            }
            if (this.toMove.size() >= 12) {
                return false;
            }
            this.toMove.add(blockpos1);
            ++i1;
            ++j1;
        }
    }

    private void reorderListAtCollision(int p_177255_1_, int p_177255_2_) {
        ArrayList list = Lists.newArrayList();
        ArrayList list1 = Lists.newArrayList();
        ArrayList list2 = Lists.newArrayList();
        list.addAll(this.toMove.subList(0, p_177255_2_));
        list1.addAll(this.toMove.subList(this.toMove.size() - p_177255_1_, this.toMove.size()));
        list2.addAll(this.toMove.subList(p_177255_2_, this.toMove.size() - p_177255_1_));
        this.toMove.clear();
        this.toMove.addAll(list);
        this.toMove.addAll(list1);
        this.toMove.addAll(list2);
    }

    private boolean addBranchingBlocks(BlockPos p_177250_1_) {
        EnumFacing[] enumFacingArray = EnumFacing.values();
        int n = enumFacingArray.length;
        int n2 = 0;
        while (n2 < n) {
            EnumFacing enumfacing = enumFacingArray[n2];
            if (enumfacing.getAxis() != this.moveDirection.getAxis() && !this.addBlockLine(p_177250_1_.offset(enumfacing), enumfacing)) {
                return false;
            }
            ++n2;
        }
        return true;
    }

    public List<BlockPos> getBlocksToMove() {
        return this.toMove;
    }

    public List<BlockPos> getBlocksToDestroy() {
        return this.toDestroy;
    }
}


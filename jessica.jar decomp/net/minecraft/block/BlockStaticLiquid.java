/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockStaticLiquid
extends BlockLiquid {
    protected BlockStaticLiquid(Material materialIn) {
        super(materialIn);
        this.setTickRandomly(false);
        if (materialIn == Material.LAVA) {
            this.setTickRandomly(true);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos p_189540_5_) {
        if (!this.checkForMixing(worldIn, pos, state)) {
            this.updateLiquid(worldIn, pos, state);
        }
    }

    private void updateLiquid(World worldIn, BlockPos pos, IBlockState state) {
        BlockDynamicLiquid blockdynamicliquid = BlockStaticLiquid.getFlowingBlock(this.blockMaterial);
        worldIn.setBlockState(pos, blockdynamicliquid.getDefaultState().withProperty(LEVEL, state.getValue(LEVEL)), 2);
        worldIn.scheduleUpdate(pos, blockdynamicliquid, this.tickRate(worldIn));
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        block11: {
            if (this.blockMaterial != Material.LAVA || !worldIn.getGameRules().getBoolean("doFireTick")) break block11;
            int i = rand.nextInt(3);
            if (i > 0) {
                BlockPos blockpos = pos;
                int j = 0;
                while (j < i) {
                    if ((blockpos = blockpos.add(rand.nextInt(3) - 1, 1, rand.nextInt(3) - 1)).getY() >= 0 && blockpos.getY() < 256 && !worldIn.isBlockLoaded(blockpos)) {
                        return;
                    }
                    Block block = worldIn.getBlockState(blockpos).getBlock();
                    if (block.blockMaterial == Material.AIR) {
                        if (this.isSurroundingBlockFlammable(worldIn, blockpos)) {
                            worldIn.setBlockState(blockpos, Blocks.FIRE.getDefaultState());
                            return;
                        }
                    } else if (block.blockMaterial.blocksMovement()) {
                        return;
                    }
                    ++j;
                }
            } else {
                int k = 0;
                while (k < 3) {
                    BlockPos blockpos1 = pos.add(rand.nextInt(3) - 1, 0, rand.nextInt(3) - 1);
                    if (blockpos1.getY() >= 0 && blockpos1.getY() < 256 && !worldIn.isBlockLoaded(blockpos1)) {
                        return;
                    }
                    if (worldIn.isAirBlock(blockpos1.up()) && this.getCanBlockBurn(worldIn, blockpos1)) {
                        worldIn.setBlockState(blockpos1.up(), Blocks.FIRE.getDefaultState());
                    }
                    ++k;
                }
            }
        }
    }

    protected boolean isSurroundingBlockFlammable(World worldIn, BlockPos pos) {
        EnumFacing[] enumFacingArray = EnumFacing.values();
        int n = enumFacingArray.length;
        int n2 = 0;
        while (n2 < n) {
            EnumFacing enumfacing = enumFacingArray[n2];
            if (this.getCanBlockBurn(worldIn, pos.offset(enumfacing))) {
                return true;
            }
            ++n2;
        }
        return false;
    }

    private boolean getCanBlockBurn(World worldIn, BlockPos pos) {
        return pos.getY() >= 0 && pos.getY() < 256 && !worldIn.isBlockLoaded(pos) ? false : worldIn.getBlockState(pos).getMaterial().getCanBurn();
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockGrass
extends Block
implements IGrowable {
    public static final PropertyBool SNOWY = PropertyBool.create("snowy");

    protected BlockGrass() {
        super(Material.GRASS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(SNOWY, false));
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        Block block = worldIn.getBlockState(pos.up()).getBlock();
        return state.withProperty(SNOWY, block == Blocks.SNOW || block == Blocks.SNOW_LAYER);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote) {
            if (worldIn.getLightFromNeighbors(pos.up()) < 4 && worldIn.getBlockState(pos.up()).getLightOpacity() > 2) {
                worldIn.setBlockState(pos, Blocks.DIRT.getDefaultState());
            } else if (worldIn.getLightFromNeighbors(pos.up()) >= 9) {
                int i = 0;
                while (i < 4) {
                    BlockPos blockpos = pos.add(rand.nextInt(3) - 1, rand.nextInt(5) - 3, rand.nextInt(3) - 1);
                    if (blockpos.getY() >= 0 && blockpos.getY() < 256 && !worldIn.isBlockLoaded(blockpos)) {
                        return;
                    }
                    IBlockState iblockstate = worldIn.getBlockState(blockpos.up());
                    IBlockState iblockstate1 = worldIn.getBlockState(blockpos);
                    if (iblockstate1.getBlock() == Blocks.DIRT && iblockstate1.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT && worldIn.getLightFromNeighbors(blockpos.up()) >= 4 && iblockstate.getLightOpacity() <= 2) {
                        worldIn.setBlockState(blockpos, Blocks.GRASS.getDefaultState());
                    }
                    ++i;
                }
            }
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Blocks.DIRT.getItemDropped(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT), rand, fortune);
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        BlockPos blockpos = pos.up();
        int i = 0;
        while (i < 128) {
            BlockPos blockpos1 = blockpos;
            int j = 0;
            while (true) {
                if (j >= i / 16) {
                    if (worldIn.getBlockState((BlockPos)blockpos1).getBlock().blockMaterial != Material.AIR) break;
                    if (rand.nextInt(8) == 0) {
                        IBlockState iblockstate;
                        BlockFlower.EnumFlowerType blockflower$enumflowertype = worldIn.getBiome(blockpos1).pickRandomFlower(rand, blockpos1);
                        BlockFlower blockflower = blockflower$enumflowertype.getBlockType().getBlock();
                        if (!blockflower.canBlockStay(worldIn, blockpos1, iblockstate = blockflower.getDefaultState().withProperty(blockflower.getTypeProperty(), blockflower$enumflowertype))) break;
                        worldIn.setBlockState(blockpos1, iblockstate, 3);
                        break;
                    }
                    IBlockState iblockstate1 = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS);
                    if (!Blocks.TALLGRASS.canBlockStay(worldIn, blockpos1, iblockstate1)) break;
                    worldIn.setBlockState(blockpos1, iblockstate1, 3);
                    break;
                }
                if (worldIn.getBlockState((blockpos1 = blockpos1.add(rand.nextInt(3) - 1, (rand.nextInt(3) - 1) * rand.nextInt(3) / 2, rand.nextInt(3) - 1)).down()).getBlock() != Blocks.GRASS || worldIn.getBlockState(blockpos1).isNormalCube()) break;
                ++j;
            }
            ++i;
        }
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer((Block)this, SNOWY);
    }
}


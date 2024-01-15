/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.IGrowable;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenBirchTree;
import net.minecraft.world.gen.feature.WorldGenCanopyTree;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenerator;

public class BlockSapling
extends BlockBush
implements IGrowable {
    public static final PropertyEnum<BlockPlanks.EnumType> TYPE = PropertyEnum.create("type", BlockPlanks.EnumType.class);
    public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, 1);
    protected static final AxisAlignedBB SAPLING_AABB = new AxisAlignedBB(0.09999999403953552, 0.0, 0.09999999403953552, 0.9f, 0.8f, 0.9f);

    protected BlockSapling() {
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, BlockPlanks.EnumType.OAK).withProperty(STAGE, 0));
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SAPLING_AABB;
    }

    @Override
    public String getLocalizedName() {
        return I18n.translateToLocal(String.valueOf(this.getUnlocalizedName()) + "." + BlockPlanks.EnumType.OAK.getUnlocalizedName() + ".name");
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote) {
            super.updateTick(worldIn, pos, state, rand);
            if (worldIn.getLightFromNeighbors(pos.up()) >= 9 && rand.nextInt(7) == 0) {
                this.grow(worldIn, pos, state, rand);
            }
        }
    }

    public void grow(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (state.getValue(STAGE) == 0) {
            worldIn.setBlockState(pos, state.cycleProperty(STAGE), 4);
        } else {
            this.generateTree(worldIn, pos, state, rand);
        }
    }

    public void generateTree(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        WorldGenAbstractTree worldgenerator = rand.nextInt(10) == 0 ? new WorldGenBigTree(true) : new WorldGenTrees(true);
        int i = 0;
        int j = 0;
        boolean flag = false;
        switch (state.getValue(TYPE)) {
            case SPRUCE: {
                i = 0;
                block7: while (i >= -1) {
                    j = 0;
                    while (j >= -1) {
                        if (this.isTwoByTwoOfType(worldIn, pos, i, j, BlockPlanks.EnumType.SPRUCE)) {
                            worldgenerator = new WorldGenMegaPineTree(false, rand.nextBoolean());
                            flag = true;
                            break block7;
                        }
                        --j;
                    }
                    --i;
                }
                if (flag) break;
                i = 0;
                j = 0;
                worldgenerator = new WorldGenTaiga2(true);
                break;
            }
            case BIRCH: {
                worldgenerator = new WorldGenBirchTree(true, false);
                break;
            }
            case JUNGLE: {
                IBlockState iblockstate = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
                IBlockState iblockstate1 = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, false);
                i = 0;
                block9: while (i >= -1) {
                    j = 0;
                    while (j >= -1) {
                        if (this.isTwoByTwoOfType(worldIn, pos, i, j, BlockPlanks.EnumType.JUNGLE)) {
                            worldgenerator = new WorldGenMegaJungle(true, 10, 20, iblockstate, iblockstate1);
                            flag = true;
                            break block9;
                        }
                        --j;
                    }
                    --i;
                }
                if (flag) break;
                i = 0;
                j = 0;
                worldgenerator = new WorldGenTrees(true, 4 + rand.nextInt(7), iblockstate, iblockstate1, false);
                break;
            }
            case ACACIA: {
                worldgenerator = new WorldGenSavannaTree(true);
                break;
            }
            case DARK_OAK: {
                i = 0;
                block11: while (i >= -1) {
                    j = 0;
                    while (j >= -1) {
                        if (this.isTwoByTwoOfType(worldIn, pos, i, j, BlockPlanks.EnumType.DARK_OAK)) {
                            worldgenerator = new WorldGenCanopyTree(true);
                            flag = true;
                            break block11;
                        }
                        --j;
                    }
                    --i;
                }
                if (flag) break;
                return;
            }
        }
        IBlockState iblockstate2 = Blocks.AIR.getDefaultState();
        if (flag) {
            worldIn.setBlockState(pos.add(i, 0, j), iblockstate2, 4);
            worldIn.setBlockState(pos.add(i + 1, 0, j), iblockstate2, 4);
            worldIn.setBlockState(pos.add(i, 0, j + 1), iblockstate2, 4);
            worldIn.setBlockState(pos.add(i + 1, 0, j + 1), iblockstate2, 4);
        } else {
            worldIn.setBlockState(pos, iblockstate2, 4);
        }
        if (!((WorldGenerator)worldgenerator).generate(worldIn, rand, pos.add(i, 0, j))) {
            if (flag) {
                worldIn.setBlockState(pos.add(i, 0, j), state, 4);
                worldIn.setBlockState(pos.add(i + 1, 0, j), state, 4);
                worldIn.setBlockState(pos.add(i, 0, j + 1), state, 4);
                worldIn.setBlockState(pos.add(i + 1, 0, j + 1), state, 4);
            } else {
                worldIn.setBlockState(pos, state, 4);
            }
        }
    }

    private boolean isTwoByTwoOfType(World worldIn, BlockPos pos, int p_181624_3_, int p_181624_4_, BlockPlanks.EnumType type) {
        return this.isTypeAt(worldIn, pos.add(p_181624_3_, 0, p_181624_4_), type) && this.isTypeAt(worldIn, pos.add(p_181624_3_ + 1, 0, p_181624_4_), type) && this.isTypeAt(worldIn, pos.add(p_181624_3_, 0, p_181624_4_ + 1), type) && this.isTypeAt(worldIn, pos.add(p_181624_3_ + 1, 0, p_181624_4_ + 1), type);
    }

    public boolean isTypeAt(World worldIn, BlockPos pos, BlockPlanks.EnumType type) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        return iblockstate.getBlock() == this && iblockstate.getValue(TYPE) == type;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(TYPE).getMetadata();
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> tab) {
        BlockPlanks.EnumType[] enumTypeArray = BlockPlanks.EnumType.values();
        int n = enumTypeArray.length;
        int n2 = 0;
        while (n2 < n) {
            BlockPlanks.EnumType blockplanks$enumtype = enumTypeArray[n2];
            tab.add(new ItemStack(this, 1, blockplanks$enumtype.getMetadata()));
            ++n2;
        }
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return (double)worldIn.rand.nextFloat() < 0.45;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        this.grow(worldIn, pos, state, rand);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TYPE, BlockPlanks.EnumType.byMetadata(meta & 7)).withProperty(STAGE, (meta & 8) >> 3);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i |= state.getValue(TYPE).getMetadata();
        return i |= state.getValue(STAGE) << 3;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer((Block)this, TYPE, STAGE);
    }
}


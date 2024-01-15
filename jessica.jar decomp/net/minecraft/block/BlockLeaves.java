/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockLeaves
extends Block {
    public static final PropertyBool DECAYABLE = PropertyBool.create("decayable");
    public static final PropertyBool CHECK_DECAY = PropertyBool.create("check_decay");
    protected boolean leavesFancy;
    int[] surroundings;

    public BlockLeaves() {
        super(Material.LEAVES);
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setHardness(0.2f);
        this.setLightOpacity(1);
        this.setSoundType(SoundType.PLANT);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        int i1;
        int l;
        boolean i = true;
        int j = 2;
        int k = pos.getX();
        if (worldIn.isAreaLoaded(new BlockPos(k - 2, (l = pos.getY()) - 2, (i1 = pos.getZ()) - 2), new BlockPos(k + 2, l + 2, i1 + 2))) {
            int j1 = -1;
            while (j1 <= 1) {
                int k1 = -1;
                while (k1 <= 1) {
                    int l1 = -1;
                    while (l1 <= 1) {
                        BlockPos blockpos = pos.add(j1, k1, l1);
                        IBlockState iblockstate = worldIn.getBlockState(blockpos);
                        if (iblockstate.getMaterial() == Material.LEAVES && !iblockstate.getValue(CHECK_DECAY).booleanValue()) {
                            worldIn.setBlockState(blockpos, iblockstate.withProperty(CHECK_DECAY, true), 4);
                        }
                        ++l1;
                    }
                    ++k1;
                }
                ++j1;
            }
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote && state.getValue(CHECK_DECAY).booleanValue() && state.getValue(DECAYABLE).booleanValue()) {
            int l2;
            int i = 4;
            int j = 5;
            int k = pos.getX();
            int l = pos.getY();
            int i1 = pos.getZ();
            int j1 = 32;
            int k1 = 1024;
            int l1 = 16;
            if (this.surroundings == null) {
                this.surroundings = new int[32768];
            }
            if (worldIn.isAreaLoaded(new BlockPos(k - 5, l - 5, i1 - 5), new BlockPos(k + 5, l + 5, i1 + 5))) {
                BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
                int i2 = -4;
                while (i2 <= 4) {
                    int j2 = -4;
                    while (j2 <= 4) {
                        int k2 = -4;
                        while (k2 <= 4) {
                            IBlockState iblockstate = worldIn.getBlockState(blockpos$mutableblockpos.setPos(k + i2, l + j2, i1 + k2));
                            Block block = iblockstate.getBlock();
                            this.surroundings[(i2 + 16) * 1024 + (j2 + 16) * 32 + k2 + 16] = block != Blocks.LOG && block != Blocks.LOG2 ? (iblockstate.getMaterial() == Material.LEAVES ? -2 : -1) : 0;
                            ++k2;
                        }
                        ++j2;
                    }
                    ++i2;
                }
                int i3 = 1;
                while (i3 <= 4) {
                    int j3 = -4;
                    while (j3 <= 4) {
                        int k3 = -4;
                        while (k3 <= 4) {
                            int l3 = -4;
                            while (l3 <= 4) {
                                if (this.surroundings[(j3 + 16) * 1024 + (k3 + 16) * 32 + l3 + 16] == i3 - 1) {
                                    if (this.surroundings[(j3 + 16 - 1) * 1024 + (k3 + 16) * 32 + l3 + 16] == -2) {
                                        this.surroundings[(j3 + 16 - 1) * 1024 + (k3 + 16) * 32 + l3 + 16] = i3;
                                    }
                                    if (this.surroundings[(j3 + 16 + 1) * 1024 + (k3 + 16) * 32 + l3 + 16] == -2) {
                                        this.surroundings[(j3 + 16 + 1) * 1024 + (k3 + 16) * 32 + l3 + 16] = i3;
                                    }
                                    if (this.surroundings[(j3 + 16) * 1024 + (k3 + 16 - 1) * 32 + l3 + 16] == -2) {
                                        this.surroundings[(j3 + 16) * 1024 + (k3 + 16 - 1) * 32 + l3 + 16] = i3;
                                    }
                                    if (this.surroundings[(j3 + 16) * 1024 + (k3 + 16 + 1) * 32 + l3 + 16] == -2) {
                                        this.surroundings[(j3 + 16) * 1024 + (k3 + 16 + 1) * 32 + l3 + 16] = i3;
                                    }
                                    if (this.surroundings[(j3 + 16) * 1024 + (k3 + 16) * 32 + (l3 + 16 - 1)] == -2) {
                                        this.surroundings[(j3 + 16) * 1024 + (k3 + 16) * 32 + (l3 + 16 - 1)] = i3;
                                    }
                                    if (this.surroundings[(j3 + 16) * 1024 + (k3 + 16) * 32 + l3 + 16 + 1] == -2) {
                                        this.surroundings[(j3 + 16) * 1024 + (k3 + 16) * 32 + l3 + 16 + 1] = i3;
                                    }
                                }
                                ++l3;
                            }
                            ++k3;
                        }
                        ++j3;
                    }
                    ++i3;
                }
            }
            if ((l2 = this.surroundings[16912]) >= 0) {
                worldIn.setBlockState(pos, state.withProperty(CHECK_DECAY, false), 4);
            } else {
                this.destroy(worldIn, pos);
            }
        }
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (worldIn.isRainingAt(pos.up()) && !worldIn.getBlockState(pos.down()).isFullyOpaque() && rand.nextInt(15) == 1) {
            double d0 = (float)pos.getX() + rand.nextFloat();
            double d1 = (double)pos.getY() - 0.05;
            double d2 = (float)pos.getZ() + rand.nextFloat();
            worldIn.spawnParticle(EnumParticleTypes.DRIP_WATER, d0, d1, d2, 0.0, 0.0, 0.0, new int[0]);
        }
    }

    private void destroy(World worldIn, BlockPos pos) {
        this.dropBlockAsItem(worldIn, pos, worldIn.getBlockState(pos), 0);
        worldIn.setBlockToAir(pos);
    }

    @Override
    public int quantityDropped(Random random) {
        return random.nextInt(20) == 0 ? 1 : 0;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(Blocks.SAPLING);
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (!worldIn.isRemote) {
            int i = this.getSaplingDropChance(state);
            if (fortune > 0 && (i -= 2 << fortune) < 10) {
                i = 10;
            }
            if (worldIn.rand.nextInt(i) == 0) {
                Item item = this.getItemDropped(state, worldIn.rand, fortune);
                BlockLeaves.spawnAsEntity(worldIn, pos, new ItemStack(item, 1, this.damageDropped(state)));
            }
            i = 200;
            if (fortune > 0 && (i -= 10 << fortune) < 40) {
                i = 40;
            }
            this.dropApple(worldIn, pos, state, i);
        }
    }

    protected void dropApple(World worldIn, BlockPos pos, IBlockState state, int chance) {
    }

    protected int getSaplingDropChance(IBlockState state) {
        return 20;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return !this.leavesFancy;
    }

    public void setGraphicsLevel(boolean fancy) {
        this.leavesFancy = fancy;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return this.leavesFancy ? BlockRenderLayer.CUTOUT_MIPPED : BlockRenderLayer.SOLID;
    }

    @Override
    public boolean causesSuffocation(IBlockState p_176214_1_) {
        return false;
    }

    public abstract BlockPlanks.EnumType getWoodType(int var1);

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return !this.leavesFancy && blockAccess.getBlockState(pos.offset(side)).getBlock() == this ? false : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }
}


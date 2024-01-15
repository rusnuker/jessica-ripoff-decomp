/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.gen.structure;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.TemplateManager;

public abstract class StructureComponent {
    protected StructureBoundingBox boundingBox;
    @Nullable
    private EnumFacing coordBaseMode;
    private Mirror mirror;
    private Rotation rotation;
    protected int componentType;

    public StructureComponent() {
    }

    protected StructureComponent(int type) {
        this.componentType = type;
    }

    public final NBTTagCompound createStructureBaseNBT() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setString("id", MapGenStructureIO.getStructureComponentName(this));
        nbttagcompound.setTag("BB", this.boundingBox.toNBTTagIntArray());
        EnumFacing enumfacing = this.getCoordBaseMode();
        nbttagcompound.setInteger("O", enumfacing == null ? -1 : enumfacing.getHorizontalIndex());
        nbttagcompound.setInteger("GD", this.componentType);
        this.writeStructureToNBT(nbttagcompound);
        return nbttagcompound;
    }

    protected abstract void writeStructureToNBT(NBTTagCompound var1);

    public void readStructureBaseNBT(World worldIn, NBTTagCompound tagCompound) {
        int i;
        if (tagCompound.hasKey("BB")) {
            this.boundingBox = new StructureBoundingBox(tagCompound.getIntArray("BB"));
        }
        this.setCoordBaseMode((i = tagCompound.getInteger("O")) == -1 ? null : EnumFacing.getHorizontal(i));
        this.componentType = tagCompound.getInteger("GD");
        this.readStructureFromNBT(tagCompound, worldIn.getSaveHandler().getStructureTemplateManager());
    }

    protected abstract void readStructureFromNBT(NBTTagCompound var1, TemplateManager var2);

    public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand) {
    }

    public abstract boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3);

    public StructureBoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public int getComponentType() {
        return this.componentType;
    }

    public static StructureComponent findIntersecting(List<StructureComponent> listIn, StructureBoundingBox boundingboxIn) {
        for (StructureComponent structurecomponent : listIn) {
            if (structurecomponent.getBoundingBox() == null || !structurecomponent.getBoundingBox().intersectsWith(boundingboxIn)) continue;
            return structurecomponent;
        }
        return null;
    }

    protected boolean isLiquidInStructureBoundingBox(World worldIn, StructureBoundingBox boundingboxIn) {
        int i = Math.max(this.boundingBox.minX - 1, boundingboxIn.minX);
        int j = Math.max(this.boundingBox.minY - 1, boundingboxIn.minY);
        int k = Math.max(this.boundingBox.minZ - 1, boundingboxIn.minZ);
        int l = Math.min(this.boundingBox.maxX + 1, boundingboxIn.maxX);
        int i1 = Math.min(this.boundingBox.maxY + 1, boundingboxIn.maxY);
        int j1 = Math.min(this.boundingBox.maxZ + 1, boundingboxIn.maxZ);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int k1 = i;
        while (k1 <= l) {
            int l1 = k;
            while (l1 <= j1) {
                if (worldIn.getBlockState(blockpos$mutableblockpos.setPos(k1, j, l1)).getMaterial().isLiquid()) {
                    return true;
                }
                if (worldIn.getBlockState(blockpos$mutableblockpos.setPos(k1, i1, l1)).getMaterial().isLiquid()) {
                    return true;
                }
                ++l1;
            }
            ++k1;
        }
        int i2 = i;
        while (i2 <= l) {
            int k2 = j;
            while (k2 <= i1) {
                if (worldIn.getBlockState(blockpos$mutableblockpos.setPos(i2, k2, k)).getMaterial().isLiquid()) {
                    return true;
                }
                if (worldIn.getBlockState(blockpos$mutableblockpos.setPos(i2, k2, j1)).getMaterial().isLiquid()) {
                    return true;
                }
                ++k2;
            }
            ++i2;
        }
        int j2 = k;
        while (j2 <= j1) {
            int l2 = j;
            while (l2 <= i1) {
                if (worldIn.getBlockState(blockpos$mutableblockpos.setPos(i, l2, j2)).getMaterial().isLiquid()) {
                    return true;
                }
                if (worldIn.getBlockState(blockpos$mutableblockpos.setPos(l, l2, j2)).getMaterial().isLiquid()) {
                    return true;
                }
                ++l2;
            }
            ++j2;
        }
        return false;
    }

    protected int getXWithOffset(int x, int z) {
        EnumFacing enumfacing = this.getCoordBaseMode();
        if (enumfacing == null) {
            return x;
        }
        switch (enumfacing) {
            case NORTH: 
            case SOUTH: {
                return this.boundingBox.minX + x;
            }
            case WEST: {
                return this.boundingBox.maxX - z;
            }
            case EAST: {
                return this.boundingBox.minX + z;
            }
        }
        return x;
    }

    protected int getYWithOffset(int y) {
        return this.getCoordBaseMode() == null ? y : y + this.boundingBox.minY;
    }

    protected int getZWithOffset(int x, int z) {
        EnumFacing enumfacing = this.getCoordBaseMode();
        if (enumfacing == null) {
            return z;
        }
        switch (enumfacing) {
            case NORTH: {
                return this.boundingBox.maxZ - z;
            }
            case SOUTH: {
                return this.boundingBox.minZ + z;
            }
            case WEST: 
            case EAST: {
                return this.boundingBox.minZ + x;
            }
        }
        return z;
    }

    protected void setBlockState(World worldIn, IBlockState blockstateIn, int x, int y, int z, StructureBoundingBox boundingboxIn) {
        BlockPos blockpos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));
        if (boundingboxIn.isVecInside(blockpos)) {
            if (this.mirror != Mirror.NONE) {
                blockstateIn = blockstateIn.withMirror(this.mirror);
            }
            if (this.rotation != Rotation.NONE) {
                blockstateIn = blockstateIn.withRotation(this.rotation);
            }
            worldIn.setBlockState(blockpos, blockstateIn, 2);
        }
    }

    protected IBlockState getBlockStateFromPos(World worldIn, int x, int y, int z, StructureBoundingBox boundingboxIn) {
        int k;
        int j;
        int i = this.getXWithOffset(x, z);
        BlockPos blockpos = new BlockPos(i, j = this.getYWithOffset(y), k = this.getZWithOffset(x, z));
        return !boundingboxIn.isVecInside(blockpos) ? Blocks.AIR.getDefaultState() : worldIn.getBlockState(blockpos);
    }

    protected int func_189916_b(World p_189916_1_, int p_189916_2_, int p_189916_3_, int p_189916_4_, StructureBoundingBox p_189916_5_) {
        int k;
        int j;
        int i = this.getXWithOffset(p_189916_2_, p_189916_4_);
        BlockPos blockpos = new BlockPos(i, j = this.getYWithOffset(p_189916_3_ + 1), k = this.getZWithOffset(p_189916_2_, p_189916_4_));
        return !p_189916_5_.isVecInside(blockpos) ? EnumSkyBlock.SKY.defaultLightValue : p_189916_1_.getLightFor(EnumSkyBlock.SKY, blockpos);
    }

    protected void fillWithAir(World worldIn, StructureBoundingBox structurebb, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int i = minY;
        while (i <= maxY) {
            int j = minX;
            while (j <= maxX) {
                int k = minZ;
                while (k <= maxZ) {
                    this.setBlockState(worldIn, Blocks.AIR.getDefaultState(), j, i, k, structurebb);
                    ++k;
                }
                ++j;
            }
            ++i;
        }
    }

    protected void fillWithBlocks(World worldIn, StructureBoundingBox boundingboxIn, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, IBlockState boundaryBlockState, IBlockState insideBlockState, boolean existingOnly) {
        int i = yMin;
        while (i <= yMax) {
            int j = xMin;
            while (j <= xMax) {
                int k = zMin;
                while (k <= zMax) {
                    if (!existingOnly || this.getBlockStateFromPos(worldIn, j, i, k, boundingboxIn).getMaterial() != Material.AIR) {
                        if (i != yMin && i != yMax && j != xMin && j != xMax && k != zMin && k != zMax) {
                            this.setBlockState(worldIn, insideBlockState, j, i, k, boundingboxIn);
                        } else {
                            this.setBlockState(worldIn, boundaryBlockState, j, i, k, boundingboxIn);
                        }
                    }
                    ++k;
                }
                ++j;
            }
            ++i;
        }
    }

    protected void fillWithRandomizedBlocks(World worldIn, StructureBoundingBox boundingboxIn, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean alwaysReplace, Random rand, BlockSelector blockselector) {
        int i = minY;
        while (i <= maxY) {
            int j = minX;
            while (j <= maxX) {
                int k = minZ;
                while (k <= maxZ) {
                    if (!alwaysReplace || this.getBlockStateFromPos(worldIn, j, i, k, boundingboxIn).getMaterial() != Material.AIR) {
                        blockselector.selectBlocks(rand, j, i, k, i == minY || i == maxY || j == minX || j == maxX || k == minZ || k == maxZ);
                        this.setBlockState(worldIn, blockselector.getBlockState(), j, i, k, boundingboxIn);
                    }
                    ++k;
                }
                ++j;
            }
            ++i;
        }
    }

    protected void func_189914_a(World p_189914_1_, StructureBoundingBox p_189914_2_, Random p_189914_3_, float p_189914_4_, int p_189914_5_, int p_189914_6_, int p_189914_7_, int p_189914_8_, int p_189914_9_, int p_189914_10_, IBlockState p_189914_11_, IBlockState p_189914_12_, boolean p_189914_13_, int p_189914_14_) {
        int i = p_189914_6_;
        while (i <= p_189914_9_) {
            int j = p_189914_5_;
            while (j <= p_189914_8_) {
                int k = p_189914_7_;
                while (k <= p_189914_10_) {
                    if (!(!(p_189914_3_.nextFloat() <= p_189914_4_) || p_189914_13_ && this.getBlockStateFromPos(p_189914_1_, j, i, k, p_189914_2_).getMaterial() == Material.AIR || p_189914_14_ > 0 && this.func_189916_b(p_189914_1_, j, i, k, p_189914_2_) >= p_189914_14_)) {
                        if (i != p_189914_6_ && i != p_189914_9_ && j != p_189914_5_ && j != p_189914_8_ && k != p_189914_7_ && k != p_189914_10_) {
                            this.setBlockState(p_189914_1_, p_189914_12_, j, i, k, p_189914_2_);
                        } else {
                            this.setBlockState(p_189914_1_, p_189914_11_, j, i, k, p_189914_2_);
                        }
                    }
                    ++k;
                }
                ++j;
            }
            ++i;
        }
    }

    protected void randomlyPlaceBlock(World worldIn, StructureBoundingBox boundingboxIn, Random rand, float chance, int x, int y, int z, IBlockState blockstateIn) {
        if (rand.nextFloat() < chance) {
            this.setBlockState(worldIn, blockstateIn, x, y, z, boundingboxIn);
        }
    }

    protected void randomlyRareFillWithBlocks(World worldIn, StructureBoundingBox boundingboxIn, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, IBlockState blockstateIn, boolean excludeAir) {
        float f = maxX - minX + 1;
        float f1 = maxY - minY + 1;
        float f2 = maxZ - minZ + 1;
        float f3 = (float)minX + f / 2.0f;
        float f4 = (float)minZ + f2 / 2.0f;
        int i = minY;
        while (i <= maxY) {
            float f5 = (float)(i - minY) / f1;
            int j = minX;
            while (j <= maxX) {
                float f6 = ((float)j - f3) / (f * 0.5f);
                int k = minZ;
                while (k <= maxZ) {
                    float f8;
                    float f7 = ((float)k - f4) / (f2 * 0.5f);
                    if ((!excludeAir || this.getBlockStateFromPos(worldIn, j, i, k, boundingboxIn).getMaterial() != Material.AIR) && (f8 = f6 * f6 + f5 * f5 + f7 * f7) <= 1.05f) {
                        this.setBlockState(worldIn, blockstateIn, j, i, k, boundingboxIn);
                    }
                    ++k;
                }
                ++j;
            }
            ++i;
        }
    }

    protected void clearCurrentPositionBlocksUpwards(World worldIn, int x, int y, int z, StructureBoundingBox structurebb) {
        BlockPos blockpos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));
        if (structurebb.isVecInside(blockpos)) {
            while (!worldIn.isAirBlock(blockpos) && blockpos.getY() < 255) {
                worldIn.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 2);
                blockpos = blockpos.up();
            }
        }
    }

    protected void replaceAirAndLiquidDownwards(World worldIn, IBlockState blockstateIn, int x, int y, int z, StructureBoundingBox boundingboxIn) {
        int k;
        int j;
        int i = this.getXWithOffset(x, z);
        if (boundingboxIn.isVecInside(new BlockPos(i, j = this.getYWithOffset(y), k = this.getZWithOffset(x, z)))) {
            while ((worldIn.isAirBlock(new BlockPos(i, j, k)) || worldIn.getBlockState(new BlockPos(i, j, k)).getMaterial().isLiquid()) && j > 1) {
                worldIn.setBlockState(new BlockPos(i, j, k), blockstateIn, 2);
                --j;
            }
        }
    }

    protected boolean generateChest(World worldIn, StructureBoundingBox structurebb, Random randomIn, int x, int y, int z, ResourceLocation loot) {
        BlockPos blockpos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));
        return this.func_191080_a(worldIn, structurebb, randomIn, blockpos, loot, null);
    }

    protected boolean func_191080_a(World p_191080_1_, StructureBoundingBox p_191080_2_, Random p_191080_3_, BlockPos p_191080_4_, ResourceLocation p_191080_5_, @Nullable IBlockState p_191080_6_) {
        if (p_191080_2_.isVecInside(p_191080_4_) && p_191080_1_.getBlockState(p_191080_4_).getBlock() != Blocks.CHEST) {
            if (p_191080_6_ == null) {
                p_191080_6_ = Blocks.CHEST.correctFacing(p_191080_1_, p_191080_4_, Blocks.CHEST.getDefaultState());
            }
            p_191080_1_.setBlockState(p_191080_4_, p_191080_6_, 2);
            TileEntity tileentity = p_191080_1_.getTileEntity(p_191080_4_);
            if (tileentity instanceof TileEntityChest) {
                ((TileEntityChest)tileentity).setLootTable(p_191080_5_, p_191080_3_.nextLong());
            }
            return true;
        }
        return false;
    }

    protected boolean createDispenser(World p_189419_1_, StructureBoundingBox p_189419_2_, Random p_189419_3_, int p_189419_4_, int p_189419_5_, int p_189419_6_, EnumFacing p_189419_7_, ResourceLocation p_189419_8_) {
        BlockPos blockpos = new BlockPos(this.getXWithOffset(p_189419_4_, p_189419_6_), this.getYWithOffset(p_189419_5_), this.getZWithOffset(p_189419_4_, p_189419_6_));
        if (p_189419_2_.isVecInside(blockpos) && p_189419_1_.getBlockState(blockpos).getBlock() != Blocks.DISPENSER) {
            this.setBlockState(p_189419_1_, Blocks.DISPENSER.getDefaultState().withProperty(BlockDispenser.FACING, p_189419_7_), p_189419_4_, p_189419_5_, p_189419_6_, p_189419_2_);
            TileEntity tileentity = p_189419_1_.getTileEntity(blockpos);
            if (tileentity instanceof TileEntityDispenser) {
                ((TileEntityDispenser)tileentity).setLootTable(p_189419_8_, p_189419_3_.nextLong());
            }
            return true;
        }
        return false;
    }

    protected void func_189915_a(World p_189915_1_, StructureBoundingBox p_189915_2_, Random p_189915_3_, int p_189915_4_, int p_189915_5_, int p_189915_6_, EnumFacing p_189915_7_, BlockDoor p_189915_8_) {
        this.setBlockState(p_189915_1_, p_189915_8_.getDefaultState().withProperty(BlockDoor.FACING, p_189915_7_), p_189915_4_, p_189915_5_, p_189915_6_, p_189915_2_);
        this.setBlockState(p_189915_1_, p_189915_8_.getDefaultState().withProperty(BlockDoor.FACING, p_189915_7_).withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), p_189915_4_, p_189915_5_ + 1, p_189915_6_, p_189915_2_);
    }

    public void offset(int x, int y, int z) {
        this.boundingBox.offset(x, y, z);
    }

    @Nullable
    public EnumFacing getCoordBaseMode() {
        return this.coordBaseMode;
    }

    public void setCoordBaseMode(@Nullable EnumFacing facing) {
        this.coordBaseMode = facing;
        if (facing == null) {
            this.rotation = Rotation.NONE;
            this.mirror = Mirror.NONE;
        } else {
            switch (facing) {
                case SOUTH: {
                    this.mirror = Mirror.LEFT_RIGHT;
                    this.rotation = Rotation.NONE;
                    break;
                }
                case WEST: {
                    this.mirror = Mirror.LEFT_RIGHT;
                    this.rotation = Rotation.CLOCKWISE_90;
                    break;
                }
                case EAST: {
                    this.mirror = Mirror.NONE;
                    this.rotation = Rotation.CLOCKWISE_90;
                    break;
                }
                default: {
                    this.mirror = Mirror.NONE;
                    this.rotation = Rotation.NONE;
                }
            }
        }
    }

    public static abstract class BlockSelector {
        protected IBlockState blockstate = Blocks.AIR.getDefaultState();

        public abstract void selectBlocks(Random var1, int var2, int var3, int var4, boolean var5);

        public IBlockState getBlockState() {
            return this.blockstate;
        }
    }
}


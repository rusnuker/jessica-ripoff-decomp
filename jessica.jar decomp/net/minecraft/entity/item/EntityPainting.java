/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.entity.item;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityPainting
extends EntityHanging {
    public EnumArt art;

    public EntityPainting(World worldIn) {
        super(worldIn);
    }

    public EntityPainting(World worldIn, BlockPos pos, EnumFacing facing) {
        super(worldIn, pos);
        ArrayList list = Lists.newArrayList();
        int i = 0;
        EnumArt[] enumArtArray = EnumArt.values();
        int n = enumArtArray.length;
        int n2 = 0;
        while (n2 < n) {
            EnumArt entitypainting$enumart;
            this.art = entitypainting$enumart = enumArtArray[n2];
            this.updateFacingWithBoundingBox(facing);
            if (this.onValidSurface()) {
                list.add(entitypainting$enumart);
                int j = entitypainting$enumart.sizeX * entitypainting$enumart.sizeY;
                if (j > i) {
                    i = j;
                }
            }
            ++n2;
        }
        if (!list.isEmpty()) {
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                EnumArt entitypainting$enumart1 = (EnumArt)((Object)iterator.next());
                if (entitypainting$enumart1.sizeX * entitypainting$enumart1.sizeY >= i) continue;
                iterator.remove();
            }
            this.art = (EnumArt)((Object)list.get(this.rand.nextInt(list.size())));
        }
        this.updateFacingWithBoundingBox(facing);
    }

    public EntityPainting(World worldIn, BlockPos pos, EnumFacing facing, String title) {
        this(worldIn, pos, facing);
        EnumArt[] enumArtArray = EnumArt.values();
        int n = enumArtArray.length;
        int n2 = 0;
        while (n2 < n) {
            EnumArt entitypainting$enumart = enumArtArray[n2];
            if (entitypainting$enumart.title.equals(title)) {
                this.art = entitypainting$enumart;
                break;
            }
            ++n2;
        }
        this.updateFacingWithBoundingBox(facing);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setString("Motive", this.art.title);
        super.writeEntityToNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        String s = compound.getString("Motive");
        EnumArt[] enumArtArray = EnumArt.values();
        int n = enumArtArray.length;
        int n2 = 0;
        while (n2 < n) {
            EnumArt entitypainting$enumart = enumArtArray[n2];
            if (entitypainting$enumart.title.equals(s)) {
                this.art = entitypainting$enumart;
            }
            ++n2;
        }
        if (this.art == null) {
            this.art = EnumArt.KEBAB;
        }
        super.readEntityFromNBT(compound);
    }

    @Override
    public int getWidthPixels() {
        return this.art.sizeX;
    }

    @Override
    public int getHeightPixels() {
        return this.art.sizeY;
    }

    @Override
    public void onBroken(@Nullable Entity brokenEntity) {
        if (this.world.getGameRules().getBoolean("doEntityDrops")) {
            this.playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0f, 1.0f);
            if (brokenEntity instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer)brokenEntity;
                if (entityplayer.capabilities.isCreativeMode) {
                    return;
                }
            }
            this.entityDropItem(new ItemStack(Items.PAINTING), 0.0f);
        }
    }

    @Override
    public void playPlaceSound() {
        this.playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0f, 1.0f);
    }

    @Override
    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
        this.setPosition(x, y, z);
    }

    @Override
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        BlockPos blockpos = this.hangingPosition.add(x - this.posX, y - this.posY, z - this.posZ);
        this.setPosition(blockpos.getX(), blockpos.getY(), blockpos.getZ());
    }

    public static enum EnumArt {
        KEBAB("Kebab", 16, 16, 0, 0),
        AZTEC("Aztec", 16, 16, 16, 0),
        ALBAN("Alban", 16, 16, 32, 0),
        AZTEC_2("Aztec2", 16, 16, 48, 0),
        BOMB("Bomb", 16, 16, 64, 0),
        PLANT("Plant", 16, 16, 80, 0),
        WASTELAND("Wasteland", 16, 16, 96, 0),
        POOL("Pool", 32, 16, 0, 32),
        COURBET("Courbet", 32, 16, 32, 32),
        SEA("Sea", 32, 16, 64, 32),
        SUNSET("Sunset", 32, 16, 96, 32),
        CREEBET("Creebet", 32, 16, 128, 32),
        WANDERER("Wanderer", 16, 32, 0, 64),
        GRAHAM("Graham", 16, 32, 16, 64),
        MATCH("Match", 32, 32, 0, 128),
        BUST("Bust", 32, 32, 32, 128),
        STAGE("Stage", 32, 32, 64, 128),
        VOID("Void", 32, 32, 96, 128),
        SKULL_AND_ROSES("SkullAndRoses", 32, 32, 128, 128),
        WITHER("Wither", 32, 32, 160, 128),
        FIGHTERS("Fighters", 64, 32, 0, 96),
        POINTER("Pointer", 64, 64, 0, 192),
        PIGSCENE("Pigscene", 64, 64, 64, 192),
        BURNING_SKULL("BurningSkull", 64, 64, 128, 192),
        SKELETON("Skeleton", 64, 48, 192, 64),
        DONKEY_KONG("DonkeyKong", 64, 48, 192, 112);

        public static final int MAX_NAME_LENGTH;
        public final String title;
        public final int sizeX;
        public final int sizeY;
        public final int offsetX;
        public final int offsetY;

        static {
            MAX_NAME_LENGTH = "SkullAndRoses".length();
        }

        private EnumArt(String titleIn, int width, int height, int textureU, int textureV) {
            this.title = titleIn;
            this.sizeX = width;
            this.sizeY = height;
            this.offsetX = textureU;
            this.offsetY = textureV;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.storage;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface IPlayerFileData {
    public void writePlayerData(EntityPlayer var1);

    @Nullable
    public NBTTagCompound readPlayerData(EntityPlayer var1);

    public String[] getAvailablePlayerDat();
}


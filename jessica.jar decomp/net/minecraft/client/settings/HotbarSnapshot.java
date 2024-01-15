/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.settings;

import java.util.ArrayList;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class HotbarSnapshot
extends ArrayList<ItemStack> {
    public static final int field_192835_a = InventoryPlayer.getHotbarSize();

    public HotbarSnapshot() {
        this.ensureCapacity(field_192835_a);
        int i = 0;
        while (i < field_192835_a) {
            this.add(ItemStack.field_190927_a);
            ++i;
        }
    }

    public NBTTagList func_192834_a() {
        NBTTagList nbttaglist = new NBTTagList();
        int i = 0;
        while (i < field_192835_a) {
            nbttaglist.appendTag(((ItemStack)this.get(i)).writeToNBT(new NBTTagCompound()));
            ++i;
        }
        return nbttaglist;
    }

    public void func_192833_a(NBTTagList p_192833_1_) {
        int i = 0;
        while (i < field_192835_a) {
            this.set(i, new ItemStack(p_192833_1_.getCompoundTagAt(i)));
            ++i;
        }
    }

    @Override
    public boolean isEmpty() {
        int i = 0;
        while (i < field_192835_a) {
            if (!((ItemStack)this.get(i)).func_190926_b()) {
                return false;
            }
            ++i;
        }
        return true;
    }
}


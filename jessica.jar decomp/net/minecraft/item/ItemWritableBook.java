/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemWritableBook
extends Item {
    public ItemWritableBook() {
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World itemStackIn, EntityPlayer worldIn, EnumHand playerIn) {
        ItemStack itemstack = worldIn.getHeldItem(playerIn);
        worldIn.openBook(itemstack, playerIn);
        worldIn.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    public static boolean isNBTValid(NBTTagCompound nbt) {
        if (nbt == null) {
            return false;
        }
        if (!nbt.hasKey("pages", 9)) {
            return false;
        }
        NBTTagList nbttaglist = nbt.getTagList("pages", 8);
        int i = 0;
        while (i < nbttaglist.tagCount()) {
            String s = nbttaglist.getStringTagAt(i);
            if (s.length() > Short.MAX_VALUE) {
                return false;
            }
            ++i;
        }
        return true;
    }
}


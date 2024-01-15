/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotShulkerBox;
import net.minecraft.item.ItemStack;

public class ContainerShulkerBox
extends Container {
    private final IInventory field_190899_a;

    public ContainerShulkerBox(InventoryPlayer p_i47266_1_, IInventory p_i47266_2_, EntityPlayer p_i47266_3_) {
        this.field_190899_a = p_i47266_2_;
        p_i47266_2_.openInventory(p_i47266_3_);
        int i = 3;
        int j = 9;
        int k = 0;
        while (k < 3) {
            int l = 0;
            while (l < 9) {
                this.addSlotToContainer(new SlotShulkerBox(p_i47266_2_, l + k * 9, 8 + l * 18, 18 + k * 18));
                ++l;
            }
            ++k;
        }
        int i1 = 0;
        while (i1 < 3) {
            int k1 = 0;
            while (k1 < 9) {
                this.addSlotToContainer(new Slot(p_i47266_1_, k1 + i1 * 9 + 9, 8 + k1 * 18, 84 + i1 * 18));
                ++k1;
            }
            ++i1;
        }
        int j1 = 0;
        while (j1 < 9) {
            this.addSlotToContainer(new Slot(p_i47266_1_, j1, 8 + j1 * 18, 142));
            ++j1;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.field_190899_a.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.field_190927_a;
        Slot slot = (Slot)this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < this.field_190899_a.getSizeInventory() ? !this.mergeItemStack(itemstack1, this.field_190899_a.getSizeInventory(), this.inventorySlots.size(), true) : !this.mergeItemStack(itemstack1, 0, this.field_190899_a.getSizeInventory(), false)) {
                return ItemStack.field_190927_a;
            }
            if (itemstack1.func_190926_b()) {
                slot.putStack(ItemStack.field_190927_a);
            } else {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        this.field_190899_a.closeInventory(playerIn);
    }
}


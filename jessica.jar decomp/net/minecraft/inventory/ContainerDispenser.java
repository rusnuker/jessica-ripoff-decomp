/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerDispenser
extends Container {
    private final IInventory dispenserInventory;

    public ContainerDispenser(IInventory playerInventory, IInventory dispenserInventoryIn) {
        this.dispenserInventory = dispenserInventoryIn;
        int i = 0;
        while (i < 3) {
            int j = 0;
            while (j < 3) {
                this.addSlotToContainer(new Slot(dispenserInventoryIn, j + i * 3, 62 + j * 18, 17 + i * 18));
                ++j;
            }
            ++i;
        }
        int k = 0;
        while (k < 3) {
            int i1 = 0;
            while (i1 < 9) {
                this.addSlotToContainer(new Slot(playerInventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
                ++i1;
            }
            ++k;
        }
        int l = 0;
        while (l < 9) {
            this.addSlotToContainer(new Slot(playerInventory, l, 8 + l * 18, 142));
            ++l;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.dispenserInventory.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.field_190927_a;
        Slot slot = (Slot)this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < 9 ? !this.mergeItemStack(itemstack1, 9, 45, true) : !this.mergeItemStack(itemstack1, 0, 9, false)) {
                return ItemStack.field_190927_a;
            }
            if (itemstack1.func_190926_b()) {
                slot.putStack(ItemStack.field_190927_a);
            } else {
                slot.onSlotChanged();
            }
            if (itemstack1.func_190916_E() == itemstack.func_190916_E()) {
                return ItemStack.field_190927_a;
            }
            slot.func_190901_a(playerIn, itemstack1);
        }
        return itemstack;
    }
}


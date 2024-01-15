/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.inventory;

import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerHorseInventory
extends Container {
    private final IInventory horseInventory;
    private final AbstractHorse theHorse;

    public ContainerHorseInventory(IInventory playerInventory, IInventory horseInventoryIn, final AbstractHorse horse, EntityPlayer player) {
        this.horseInventory = horseInventoryIn;
        this.theHorse = horse;
        int i = 3;
        horseInventoryIn.openInventory(player);
        int j = -18;
        this.addSlotToContainer(new Slot(horseInventoryIn, 0, 8, 18){

            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() == Items.SADDLE && !this.getHasStack() && horse.func_190685_dA();
            }

            @Override
            public boolean canBeHovered() {
                return horse.func_190685_dA();
            }
        });
        this.addSlotToContainer(new Slot(horseInventoryIn, 1, 8, 36){

            @Override
            public boolean isItemValid(ItemStack stack) {
                return horse.func_190682_f(stack);
            }

            @Override
            public boolean canBeHovered() {
                return horse.func_190677_dK();
            }

            @Override
            public int getSlotStackLimit() {
                return 1;
            }
        });
        if (horse instanceof AbstractChestHorse && ((AbstractChestHorse)horse).func_190695_dh()) {
            int k = 0;
            while (k < 3) {
                int l = 0;
                while (l < ((AbstractChestHorse)horse).func_190696_dl()) {
                    this.addSlotToContainer(new Slot(horseInventoryIn, 2 + l + k * ((AbstractChestHorse)horse).func_190696_dl(), 80 + l * 18, 18 + k * 18));
                    ++l;
                }
                ++k;
            }
        }
        int i1 = 0;
        while (i1 < 3) {
            int k1 = 0;
            while (k1 < 9) {
                this.addSlotToContainer(new Slot(playerInventory, k1 + i1 * 9 + 9, 8 + k1 * 18, 102 + i1 * 18 + -18));
                ++k1;
            }
            ++i1;
        }
        int j1 = 0;
        while (j1 < 9) {
            this.addSlotToContainer(new Slot(playerInventory, j1, 8 + j1 * 18, 142));
            ++j1;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.horseInventory.isUsableByPlayer(playerIn) && this.theHorse.isEntityAlive() && this.theHorse.getDistanceToEntity(playerIn) < 8.0f;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.field_190927_a;
        Slot slot = (Slot)this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < this.horseInventory.getSizeInventory() ? !this.mergeItemStack(itemstack1, this.horseInventory.getSizeInventory(), this.inventorySlots.size(), true) : (this.getSlot(1).isItemValid(itemstack1) && !this.getSlot(1).getHasStack() ? !this.mergeItemStack(itemstack1, 1, 2, false) : (this.getSlot(0).isItemValid(itemstack1) ? !this.mergeItemStack(itemstack1, 0, 1, false) : this.horseInventory.getSizeInventory() <= 2 || !this.mergeItemStack(itemstack1, 2, this.horseInventory.getSizeInventory(), false)))) {
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
        this.horseInventory.closeInventory(playerIn);
    }
}


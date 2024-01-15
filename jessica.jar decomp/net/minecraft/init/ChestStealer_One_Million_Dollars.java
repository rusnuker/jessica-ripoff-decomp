/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.init;

import com.mysql.fabric.Category;
import com.mysql.fabric.Module;
import com.mysql.fabric.Wrapper;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.util.datafix.Timer2;

public class ChestStealer_One_Million_Dollars
extends Module {
    private Timer2 time = new Timer2();

    public ChestStealer_One_Million_Dollars() {
        super("ChestStealer", Category.Combat);
    }

    @Override
    public void onUpdate() {
        if (Wrapper.player().openContainer != null && Wrapper.player().openContainer instanceof ContainerChest) {
            ContainerChest container = (ContainerChest)Wrapper.player().openContainer;
            int i = 0;
            while (i < container.getLowerChestInventory().getSizeInventory()) {
                if (container.getLowerChestInventory().getStackInSlot(i).getItem() != Item.getItemById(0) && this.time.check(110.0f)) {
                    Wrapper.mc().playerController.windowClick(container.windowId, i, 0, ClickType.QUICK_MOVE, Wrapper.player());
                    this.time.reset();
                } else if (this.empty(container)) {
                    Wrapper.player().closeScreen();
                }
                ++i;
            }
        }
    }

    public boolean empty(Container container) {
        boolean voll = true;
        int i = 0;
        int slotAmount = container.inventorySlots.size() == 90 ? 54 : 27;
        while (i < slotAmount) {
            if (container.getSlot(i).getHasStack()) {
                voll = false;
            }
            ++i;
        }
        return voll;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.ints.IntListIterator
 *  javax.annotation.Nullable
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package net.minecraft.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.network.play.server.SPacketPlaceGhostRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerRecipeBookHelper {
    private final Logger field_194330_a = LogManager.getLogger();
    private final RecipeItemHelper field_194331_b = new RecipeItemHelper();
    private EntityPlayerMP field_194332_c;
    private IRecipe field_194333_d;
    private boolean field_194334_e;
    private InventoryCraftResult field_194335_f;
    private InventoryCrafting field_194336_g;
    private List<Slot> field_194337_h;

    public void func_194327_a(EntityPlayerMP p_194327_1_, @Nullable IRecipe p_194327_2_, boolean p_194327_3_) {
        if (p_194327_2_ != null && p_194327_1_.func_192037_E().func_193830_f(p_194327_2_)) {
            this.field_194332_c = p_194327_1_;
            this.field_194333_d = p_194327_2_;
            this.field_194334_e = p_194327_3_;
            this.field_194337_h = p_194327_1_.openContainer.inventorySlots;
            Container container = p_194327_1_.openContainer;
            this.field_194335_f = null;
            this.field_194336_g = null;
            if (container instanceof ContainerWorkbench) {
                this.field_194335_f = ((ContainerWorkbench)container).craftResult;
                this.field_194336_g = ((ContainerWorkbench)container).craftMatrix;
            } else if (container instanceof ContainerPlayer) {
                this.field_194335_f = ((ContainerPlayer)container).craftResult;
                this.field_194336_g = ((ContainerPlayer)container).craftMatrix;
            }
            if (this.field_194335_f != null && this.field_194336_g != null && (this.func_194328_c() || p_194327_1_.isCreative())) {
                this.field_194331_b.func_194119_a();
                p_194327_1_.inventory.func_194016_a(this.field_194331_b, false);
                this.field_194336_g.func_194018_a(this.field_194331_b);
                if (this.field_194331_b.func_194116_a(p_194327_2_, null)) {
                    this.func_194329_b();
                } else {
                    this.func_194326_a();
                    p_194327_1_.connection.sendPacket(new SPacketPlaceGhostRecipe(p_194327_1_.openContainer.windowId, p_194327_2_));
                }
                p_194327_1_.inventory.markDirty();
            }
        }
    }

    private void func_194326_a() {
        InventoryPlayer inventoryplayer = this.field_194332_c.inventory;
        int i = 0;
        while (i < this.field_194336_g.getSizeInventory()) {
            ItemStack itemstack = this.field_194336_g.getStackInSlot(i);
            if (!itemstack.func_190926_b()) {
                while (itemstack.func_190916_E() > 0) {
                    int j = inventoryplayer.storeItemStack(itemstack);
                    if (j == -1) {
                        j = inventoryplayer.getFirstEmptyStack();
                    }
                    ItemStack itemstack1 = itemstack.copy();
                    itemstack1.func_190920_e(1);
                    inventoryplayer.func_191971_c(j, itemstack1);
                    this.field_194336_g.decrStackSize(i, 1);
                }
            }
            ++i;
        }
        this.field_194336_g.clear();
        this.field_194335_f.clear();
    }

    private void func_194329_b() {
        int i1;
        IntArrayList intlist;
        boolean flag = this.field_194333_d.matches(this.field_194336_g, this.field_194332_c.world);
        int i = this.field_194331_b.func_194114_b(this.field_194333_d, null);
        if (flag) {
            boolean flag1 = true;
            int j = 0;
            while (j < this.field_194336_g.getSizeInventory()) {
                ItemStack itemstack = this.field_194336_g.getStackInSlot(j);
                if (!itemstack.func_190926_b() && Math.min(i, itemstack.getMaxStackSize()) > itemstack.func_190916_E()) {
                    flag1 = false;
                }
                ++j;
            }
            if (flag1) {
                return;
            }
        }
        if (this.field_194331_b.func_194118_a(this.field_194333_d, (IntList)(intlist = new IntArrayList()), i1 = this.func_194324_a(i, flag))) {
            int j1 = i1;
            IntListIterator intlistiterator = intlist.iterator();
            while (intlistiterator.hasNext()) {
                int k = (Integer)intlistiterator.next();
                int l = RecipeItemHelper.func_194115_b(k).getMaxStackSize();
                if (l >= j1) continue;
                j1 = l;
            }
            if (this.field_194331_b.func_194118_a(this.field_194333_d, (IntList)intlist, j1)) {
                this.func_194326_a();
                this.func_194323_a(j1, (IntList)intlist);
            }
        }
    }

    private int func_194324_a(int p_194324_1_, boolean p_194324_2_) {
        int i = 1;
        if (this.field_194334_e) {
            i = p_194324_1_;
        } else if (p_194324_2_) {
            i = 64;
            int j = 0;
            while (j < this.field_194336_g.getSizeInventory()) {
                ItemStack itemstack = this.field_194336_g.getStackInSlot(j);
                if (!itemstack.func_190926_b() && i > itemstack.func_190916_E()) {
                    i = itemstack.func_190916_E();
                }
                ++j;
            }
            if (i < 64) {
                ++i;
            }
        }
        return i;
    }

    private void func_194323_a(int p_194323_1_, IntList p_194323_2_) {
        int i = this.field_194336_g.getWidth();
        int j = this.field_194336_g.getHeight();
        if (this.field_194333_d instanceof ShapedRecipes) {
            ShapedRecipes shapedrecipes = (ShapedRecipes)this.field_194333_d;
            i = shapedrecipes.func_192403_f();
            j = shapedrecipes.func_192404_g();
        }
        int j1 = 1;
        IntListIterator iterator = p_194323_2_.iterator();
        int k = 0;
        while (k < this.field_194336_g.getWidth() && j != k) {
            int l = 0;
            while (l < this.field_194336_g.getHeight()) {
                if (i == l || !iterator.hasNext()) {
                    j1 += this.field_194336_g.getWidth() - l;
                    break;
                }
                Slot slot = this.field_194337_h.get(j1);
                ItemStack itemstack = RecipeItemHelper.func_194115_b((Integer)iterator.next());
                if (itemstack.func_190926_b()) {
                    ++j1;
                } else {
                    int i1 = 0;
                    while (i1 < p_194323_1_) {
                        this.func_194325_a(slot, itemstack);
                        ++i1;
                    }
                    ++j1;
                }
                ++l;
            }
            if (!iterator.hasNext()) break;
            ++k;
        }
    }

    private void func_194325_a(Slot p_194325_1_, ItemStack p_194325_2_) {
        ItemStack itemstack;
        InventoryPlayer inventoryplayer = this.field_194332_c.inventory;
        int i = inventoryplayer.func_194014_c(p_194325_2_);
        if (i != -1 && !(itemstack = inventoryplayer.getStackInSlot(i).copy()).func_190926_b()) {
            if (itemstack.func_190916_E() > 1) {
                inventoryplayer.decrStackSize(i, 1);
            } else {
                inventoryplayer.removeStackFromSlot(i);
            }
            itemstack.func_190920_e(1);
            if (p_194325_1_.getStack().func_190926_b()) {
                p_194325_1_.putStack(itemstack);
            } else {
                p_194325_1_.getStack().func_190917_f(1);
            }
        }
    }

    private boolean func_194328_c() {
        InventoryPlayer inventoryplayer = this.field_194332_c.inventory;
        int i = 0;
        while (i < this.field_194336_g.getSizeInventory()) {
            ItemStack itemstack = this.field_194336_g.getStackInSlot(i);
            if (!itemstack.func_190926_b()) {
                int j = inventoryplayer.storeItemStack(itemstack);
                if (j == -1) {
                    j = inventoryplayer.getFirstEmptyStack();
                }
                if (j == -1) {
                    return false;
                }
            }
            ++i;
        }
        return true;
    }
}


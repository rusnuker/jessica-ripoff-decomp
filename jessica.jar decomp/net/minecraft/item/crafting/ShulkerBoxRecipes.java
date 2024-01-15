/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item.crafting;

import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class ShulkerBoxRecipes {

    public static class ShulkerBoxColoring
    implements IRecipe {
        @Override
        public boolean matches(InventoryCrafting inv, World worldIn) {
            int i = 0;
            int j = 0;
            int k = 0;
            while (k < inv.getSizeInventory()) {
                ItemStack itemstack = inv.getStackInSlot(k);
                if (!itemstack.func_190926_b()) {
                    if (Block.getBlockFromItem(itemstack.getItem()) instanceof BlockShulkerBox) {
                        ++i;
                    } else {
                        if (itemstack.getItem() != Items.DYE) {
                            return false;
                        }
                        ++j;
                    }
                    if (j > 1 || i > 1) {
                        return false;
                    }
                }
                ++k;
            }
            return i == 1 && j == 1;
        }

        @Override
        public ItemStack getCraftingResult(InventoryCrafting inv) {
            ItemStack itemstack = ItemStack.field_190927_a;
            ItemStack itemstack1 = ItemStack.field_190927_a;
            int i = 0;
            while (i < inv.getSizeInventory()) {
                ItemStack itemstack2 = inv.getStackInSlot(i);
                if (!itemstack2.func_190926_b()) {
                    if (Block.getBlockFromItem(itemstack2.getItem()) instanceof BlockShulkerBox) {
                        itemstack = itemstack2;
                    } else if (itemstack2.getItem() == Items.DYE) {
                        itemstack1 = itemstack2;
                    }
                }
                ++i;
            }
            ItemStack itemstack3 = BlockShulkerBox.func_190953_b(EnumDyeColor.byDyeDamage(itemstack1.getMetadata()));
            if (itemstack.hasTagCompound()) {
                itemstack3.setTagCompound(itemstack.getTagCompound().copy());
            }
            return itemstack3;
        }

        @Override
        public ItemStack getRecipeOutput() {
            return ItemStack.field_190927_a;
        }

        @Override
        public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
            NonNullList<ItemStack> nonnulllist = NonNullList.func_191197_a(inv.getSizeInventory(), ItemStack.field_190927_a);
            int i = 0;
            while (i < nonnulllist.size()) {
                ItemStack itemstack = inv.getStackInSlot(i);
                if (itemstack.getItem().hasContainerItem()) {
                    nonnulllist.set(i, new ItemStack(itemstack.getItem().getContainerItem()));
                }
                ++i;
            }
            return nonnulllist;
        }

        @Override
        public boolean func_192399_d() {
            return true;
        }

        @Override
        public boolean func_194133_a(int p_194133_1_, int p_194133_2_) {
            return p_194133_1_ * p_194133_2_ >= 2;
        }
    }
}


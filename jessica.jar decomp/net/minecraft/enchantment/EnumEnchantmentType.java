/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.enchantment;

import net.minecraft.block.BlockPumpkin;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;

public enum EnumEnchantmentType {
    ALL{

        @Override
        public boolean canEnchantItem(Item itemIn) {
            EnumEnchantmentType[] enumEnchantmentTypeArray = EnumEnchantmentType.values();
            int n = enumEnchantmentTypeArray.length;
            int n2 = 0;
            while (n2 < n) {
                EnumEnchantmentType enumenchantmenttype = enumEnchantmentTypeArray[n2];
                if (enumenchantmenttype != ALL && enumenchantmenttype.canEnchantItem(itemIn)) {
                    return true;
                }
                ++n2;
            }
            return false;
        }
    }
    ,
    ARMOR{

        @Override
        public boolean canEnchantItem(Item itemIn) {
            return itemIn instanceof ItemArmor;
        }
    }
    ,
    ARMOR_FEET{

        @Override
        public boolean canEnchantItem(Item itemIn) {
            return itemIn instanceof ItemArmor && ((ItemArmor)itemIn).armorType == EntityEquipmentSlot.FEET;
        }
    }
    ,
    ARMOR_LEGS{

        @Override
        public boolean canEnchantItem(Item itemIn) {
            return itemIn instanceof ItemArmor && ((ItemArmor)itemIn).armorType == EntityEquipmentSlot.LEGS;
        }
    }
    ,
    ARMOR_CHEST{

        @Override
        public boolean canEnchantItem(Item itemIn) {
            return itemIn instanceof ItemArmor && ((ItemArmor)itemIn).armorType == EntityEquipmentSlot.CHEST;
        }
    }
    ,
    ARMOR_HEAD{

        @Override
        public boolean canEnchantItem(Item itemIn) {
            return itemIn instanceof ItemArmor && ((ItemArmor)itemIn).armorType == EntityEquipmentSlot.HEAD;
        }
    }
    ,
    WEAPON{

        @Override
        public boolean canEnchantItem(Item itemIn) {
            return itemIn instanceof ItemSword;
        }
    }
    ,
    DIGGER{

        @Override
        public boolean canEnchantItem(Item itemIn) {
            return itemIn instanceof ItemTool;
        }
    }
    ,
    FISHING_ROD{

        @Override
        public boolean canEnchantItem(Item itemIn) {
            return itemIn instanceof ItemFishingRod;
        }
    }
    ,
    BREAKABLE{

        @Override
        public boolean canEnchantItem(Item itemIn) {
            return itemIn.isDamageable();
        }
    }
    ,
    BOW{

        @Override
        public boolean canEnchantItem(Item itemIn) {
            return itemIn instanceof ItemBow;
        }
    }
    ,
    WEARABLE{

        @Override
        public boolean canEnchantItem(Item itemIn) {
            boolean flag;
            boolean bl = flag = itemIn instanceof ItemBlock && ((ItemBlock)itemIn).getBlock() instanceof BlockPumpkin;
            return itemIn instanceof ItemArmor || itemIn instanceof ItemElytra || itemIn instanceof ItemSkull || flag;
        }
    };


    private EnumEnchantmentType() {
    }

    public abstract boolean canEnchantItem(Item var1);

    /* synthetic */ EnumEnchantmentType(String string, int n, EnumEnchantmentType enumEnchantmentType) {
        this();
    }
}


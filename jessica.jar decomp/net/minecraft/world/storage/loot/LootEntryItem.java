/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonSerializationContext
 */
package net.minecraft.world.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Collection;
import java.util.Random;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;

public class LootEntryItem
extends LootEntry {
    protected final Item item;
    protected final LootFunction[] functions;

    public LootEntryItem(Item itemIn, int weightIn, int qualityIn, LootFunction[] functionsIn, LootCondition[] conditionsIn) {
        super(weightIn, qualityIn, conditionsIn);
        this.item = itemIn;
        this.functions = functionsIn;
    }

    @Override
    public void addLoot(Collection<ItemStack> stacks, Random rand, LootContext context) {
        ItemStack itemstack = new ItemStack(this.item);
        LootFunction[] lootFunctionArray = this.functions;
        int n = this.functions.length;
        int n2 = 0;
        while (n2 < n) {
            LootFunction lootfunction = lootFunctionArray[n2];
            if (LootConditionManager.testAllConditions(lootfunction.getConditions(), rand, context)) {
                itemstack = lootfunction.apply(itemstack, rand, context);
            }
            ++n2;
        }
        if (!itemstack.func_190926_b()) {
            if (itemstack.func_190916_E() < this.item.getItemStackLimit()) {
                stacks.add(itemstack);
            } else {
                int i = itemstack.func_190916_E();
                while (i > 0) {
                    ItemStack itemstack1 = itemstack.copy();
                    itemstack1.func_190920_e(Math.min(itemstack.getMaxStackSize(), i));
                    i -= itemstack1.func_190916_E();
                    stacks.add(itemstack1);
                }
            }
        }
    }

    @Override
    protected void serialize(JsonObject json, JsonSerializationContext context) {
        ResourceLocation resourcelocation;
        if (this.functions != null && this.functions.length > 0) {
            json.add("functions", context.serialize((Object)this.functions));
        }
        if ((resourcelocation = Item.REGISTRY.getNameForObject(this.item)) == null) {
            throw new IllegalArgumentException("Can't serialize unknown item " + this.item);
        }
        json.addProperty("name", resourcelocation.toString());
    }

    public static LootEntryItem deserialize(JsonObject object, JsonDeserializationContext deserializationContext, int weightIn, int qualityIn, LootCondition[] conditionsIn) {
        Item item = JsonUtils.getItem(object, "name");
        LootFunction[] alootfunction = object.has("functions") ? JsonUtils.deserializeClass(object, "functions", deserializationContext, LootFunction[].class) : new LootFunction[]{};
        return new LootEntryItem(item, weightIn, qualityIn, alootfunction, conditionsIn);
    }
}


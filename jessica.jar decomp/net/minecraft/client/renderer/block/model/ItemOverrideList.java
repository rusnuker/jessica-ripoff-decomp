/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ItemOverrideList {
    public static final ItemOverrideList NONE = new ItemOverrideList();
    private final List<ItemOverride> overrides = Lists.newArrayList();

    private ItemOverrideList() {
    }

    public ItemOverrideList(List<ItemOverride> overridesIn) {
        int i = overridesIn.size() - 1;
        while (i >= 0) {
            this.overrides.add(overridesIn.get(i));
            --i;
        }
    }

    @Nullable
    public ResourceLocation applyOverride(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
        if (!this.overrides.isEmpty()) {
            for (ItemOverride itemoverride : this.overrides) {
                if (!itemoverride.matchesItemStack(stack, worldIn, entityIn)) continue;
                return itemoverride.getLocation();
            }
        }
        return null;
    }
}


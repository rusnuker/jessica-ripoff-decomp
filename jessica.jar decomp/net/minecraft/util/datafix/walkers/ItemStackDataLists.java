/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.walkers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.walkers.Filtered;

public class ItemStackDataLists
extends Filtered {
    private final String[] matchingTags;

    public ItemStackDataLists(Class<?> p_i47310_1_, String ... p_i47310_2_) {
        super(p_i47310_1_);
        this.matchingTags = p_i47310_2_;
    }

    @Override
    NBTTagCompound filteredProcess(IDataFixer fixer, NBTTagCompound compound, int versionIn) {
        String[] stringArray = this.matchingTags;
        int n = this.matchingTags.length;
        int n2 = 0;
        while (n2 < n) {
            String s = stringArray[n2];
            compound = DataFixesManager.processInventory(fixer, compound, versionIn, s);
            ++n2;
        }
        return compound;
    }
}


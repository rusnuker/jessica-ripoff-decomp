/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.init;

import com.mysql.fabric.Category;
import com.mysql.fabric.Module;
import com.mysql.fabric.Wrapper;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class MiningFatigueEffect_One_Million_Dollars
extends Module {
    public MiningFatigueEffect_One_Million_Dollars() {
        super("MiningFatigue", Category.Effects);
    }

    @Override
    public void onEnable() {
        Wrapper.player().addPotionEffect(new PotionEffect(Potion.getPotionById(4), 9999999, 1));
    }

    @Override
    public void onDisable() {
        Wrapper.player().removePotionEffect(Potion.getPotionById(4));
    }
}


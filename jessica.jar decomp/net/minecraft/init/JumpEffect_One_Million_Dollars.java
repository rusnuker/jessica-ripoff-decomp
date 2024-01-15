/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.init;

import com.mysql.fabric.Category;
import com.mysql.fabric.Module;
import com.mysql.fabric.Wrapper;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class JumpEffect_One_Million_Dollars
extends Module {
    public JumpEffect_One_Million_Dollars() {
        super("JumpEffect", Category.Effects);
    }

    @Override
    public void onUpdate() {
        Wrapper.player().addPotionEffect(new PotionEffect(Potion.getPotionById(8), 9999999, 0));
    }

    @Override
    public void onDisable() {
        Wrapper.player().removePotionEffect(Potion.getPotionById(8));
    }
}


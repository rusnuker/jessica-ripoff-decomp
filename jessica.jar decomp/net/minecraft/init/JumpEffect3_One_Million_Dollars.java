/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.init;

import com.mysql.fabric.Category;
import com.mysql.fabric.Module;
import com.mysql.fabric.Wrapper;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class JumpEffect3_One_Million_Dollars
extends Module {
    public JumpEffect3_One_Million_Dollars() {
        super("JumpEffect 8", Category.Effects);
    }

    @Override
    public void onUpdate() {
        Wrapper.player().addPotionEffect(new PotionEffect(Potion.getPotionById(8), 9999999, 7));
    }

    @Override
    public void onDisable() {
        Wrapper.player().removePotionEffect(Potion.getPotionById(8));
    }
}


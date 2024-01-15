/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.init;

import com.mysql.fabric.Category;
import com.mysql.fabric.Module;
import com.mysql.fabric.Wrapper;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class SpeedEffect_One_Million_Dollars
extends Module {
    public SpeedEffect_One_Million_Dollars() {
        super("SpeedEffect", Category.Player);
    }

    @Override
    public void onEnable() {
        Wrapper.player().addPotionEffect(new PotionEffect(Potion.getPotionById(1), 9999999, 1));
    }

    @Override
    public void onUpdate() {
        if (!(Wrapper.getModule("AAC").isToggled() || Wrapper.getModule("Spartan").isToggled() || Wrapper.getModule("AAC").isToggled())) {
            Wrapper.player().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.14000000447034835);
        } else if (Wrapper.player().fallDistance > 0.0f) {
            Wrapper.player().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3900000044703484);
            Wrapper.player().motionY -= 0.05;
        } else {
            Wrapper.player().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.1f);
        }
    }

    @Override
    public void onDisable() {
        Wrapper.player().removePotionEffect(Potion.getPotionById(1));
        Wrapper.player().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.1f);
    }
}


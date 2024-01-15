/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.init;

import com.mysql.fabric.Category;
import com.mysql.fabric.Module;
import com.mysql.fabric.Wrapper;
import net.minecraft.util.EnumHand;
import net.minecraft.util.datafix.EntityUtils;
import net.minecraft.util.datafix.Timer2;

public class Glide_One_Million_Dollars
extends Module {
    public Timer2 ground = new Timer2();
    private static float speed = -1.0f;
    public static int tick = 0;
    public static boolean helpBool = true;
    public static int times = 0;

    public Glide_One_Million_Dollars() {
        super("Glide", Category.Player);
    }

    @Override
    public void onDisable() {
        tick = 0;
        helpBool = true;
    }

    @Override
    public void onEnable() {
        if (!Wrapper.getModule("AAC").isToggled() && Wrapper.player() != null) {
            EntityUtils.damagePlayer();
            Wrapper.player().motionX *= 0.2;
            Wrapper.player().motionZ *= 0.2;
            Wrapper.player().swingArm(EnumHand.MAIN_HAND);
        }
    }

    @Override
    public void onUpdate() {
        if (!Wrapper.getModule("AAC").isToggled()) {
            if (!Wrapper.player().capabilities.isFlying && Wrapper.player().fallDistance > 0.0f && !Wrapper.player().isSneaking()) {
                Wrapper.player().motionY = 0.0;
            }
            if (Wrapper.mc().gameSettings.keyBindSneak.pressed) {
                Wrapper.player().motionY = -0.11;
            }
            if (Wrapper.mc().gameSettings.keyBindJump.pressed) {
                Wrapper.player().motionY = 0.11;
            }
            if (this.ground.check(50.0f)) {
                Wrapper.player().onGround = false;
                this.ground.reset();
            }
        } else {
            if (Wrapper.player().onGround) {
                times = 0;
            }
            if (Wrapper.player().fallDistance > 0.0f && times <= 1) {
                if (tick > 0 && helpBool) {
                    Wrapper.player().motionY = 0.0;
                    tick = 0;
                } else {
                    ++tick;
                }
                if ((double)Wrapper.player().fallDistance >= 0.1) {
                    helpBool = false;
                }
                if ((double)Wrapper.player().fallDistance >= 0.4) {
                    helpBool = true;
                    Wrapper.player().fallDistance = 0.0f;
                }
            }
        }
    }
}


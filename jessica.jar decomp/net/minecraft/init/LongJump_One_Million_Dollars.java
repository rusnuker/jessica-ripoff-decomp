/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.init;

import com.darkmagician6.eventapi.EventTarget;
import com.mysql.fabric.Category;
import com.mysql.fabric.Module;
import com.mysql.fabric.Wrapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovementInput;
import net.minecraft.util.datafix.EntityUtils;
import net.minecraftforge.common.model.MoveEvent;

public class LongJump_One_Million_Dollars
extends Module {
    private double speed = 6.0;
    private int level = 1;
    private boolean disabling;
    private double moveSpeed = 0.2873;
    public static boolean canStep;
    private double lastDist;
    public static double yOffset;
    private boolean cancel;
    private boolean speedTick;
    private float speedTimer = 1.0f;
    private int timer;
    private boolean jump = false;
    public static float tick;
    public static boolean helpBool;
    public static int times;
    public double posY = -99999.0;

    static {
        tick = 0.0f;
        helpBool = true;
        times = 0;
    }

    public LongJump_One_Million_Dollars() {
        super("LongJump", Category.Player);
    }

    @Override
    @EventTarget
    public void onMotion(MoveEvent event) {
        if (Wrapper.mc().gameSettings.keyBindJump.pressed) {
            this.jump = true;
            this.speedTick = !this.speedTick;
            ++this.timer;
            this.timer %= 5;
            if (Wrapper.player().onGround && EntityUtils.isMoving(Wrapper.player())) {
                this.level = 2;
            }
            if (LongJump_One_Million_Dollars.round(Wrapper.player().posY - (double)((int)Wrapper.player().posY), 3) == LongJump_One_Million_Dollars.round(0.138, 3)) {
                EntityPlayerSP thePlayer = Wrapper.player();
                thePlayer.motionY -= 0.08;
                event.y -= 0.0931;
                EntityPlayerSP thePlayer2 = Wrapper.player();
                thePlayer2.posY -= 0.0931;
            }
            if (this.level == 1 && (Wrapper.player().moveForward != 0.0f || Wrapper.player().moveStrafing != 0.0f)) {
                this.level = 2;
                this.moveSpeed = 1.351 * this.getStandart() - 0.01;
            } else {
                if (this.level == 2 && !Wrapper.player().isCollidedHorizontally) {
                    this.level = 3;
                    Wrapper.player().motionY = 0.42;
                    event.y = 0.42;
                    this.moveSpeed *= 2.149;
                }
                if (this.level == 3) {
                    this.level = 4;
                    double difference = 0.66 * (this.lastDist - this.getStandart());
                    this.moveSpeed = (this.lastDist - difference) * 2.0;
                } else {
                    if (Wrapper.world().getCollisionBoxes(Wrapper.player(), Wrapper.player().boundingBox.offset(0.0, Wrapper.player().motionY, 0.0)).size() > 0 || Wrapper.player().isCollidedVertically) {
                        this.level = 1;
                    }
                    this.moveSpeed = this.lastDist - this.lastDist / 159.0;
                }
            }
            this.moveSpeed = Math.max(this.moveSpeed, this.getStandart());
            MovementInput movementInput = Wrapper.player().movementInput;
            float forward = movementInput.field_192832_b;
            float strafe = movementInput.moveStrafe;
            float yaw = Wrapper.player().rotationYaw;
            if (forward == 0.0f && strafe == 0.0f) {
                event.x = 0.0;
                event.z = 0.0;
            } else if (forward != 0.0f) {
                if (strafe >= 1.0f) {
                    yaw += (float)(forward > 0.0f ? -45 : 45);
                    strafe = 0.0f;
                } else if (strafe <= -1.0f) {
                    yaw += (float)(forward > 0.0f ? 45 : -45);
                    strafe = 0.0f;
                }
                if (forward > 0.0f) {
                    forward = 1.0f;
                } else if (forward < 0.0f) {
                    forward = -1.0f;
                }
            }
            double mx = Math.cos(Math.toRadians(yaw + 90.0f));
            double mz = Math.sin(Math.toRadians(yaw + 90.0f));
            double motionX = (double)forward * this.moveSpeed * mx + (double)strafe * this.moveSpeed * mz;
            double motionZ = (double)forward * this.moveSpeed * mz - (double)strafe * this.moveSpeed * mx;
            event.x = (double)forward * this.moveSpeed * mx + (double)strafe * this.moveSpeed * mz;
            event.z = (double)forward * this.moveSpeed * mz - (double)strafe * this.moveSpeed * mx;
            canStep = true;
            Wrapper.player().stepHeight = 0.6f;
            if (forward == 0.0f && strafe == 0.0f) {
                event.x = 0.0;
                event.z = 0.0;
            } else {
                boolean collideCheck = false;
                if (Wrapper.world().getCollisionBoxes(Wrapper.player(), Wrapper.player().boundingBox.expand(0.5, 0.0, 0.5)).size() > 0) {
                    collideCheck = true;
                }
                if (forward != 0.0f) {
                    if (strafe >= 1.0f) {
                        yaw += (float)(forward > 0.0f ? -45 : 45);
                        strafe = 0.0f;
                    } else if (strafe <= -1.0f) {
                        yaw += (float)(forward > 0.0f ? 45 : -45);
                        strafe = 0.0f;
                    }
                    if (forward > 0.0f) {
                        forward = 1.0f;
                    } else if (forward < 0.0f) {
                        float f = -1.0f;
                    }
                }
            }
        }
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void onUpdate() {
        double xDist = Wrapper.player().posX - Wrapper.player().prevPosX;
        double zDist = Wrapper.player().posZ - Wrapper.player().prevPosZ;
        this.lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
    }

    private double getStandart() {
        double baseSpeed = 0.2873;
        if (Wrapper.player().isPotionActive(Potion.getPotionById(1))) {
            int amplifier = Wrapper.player().getActivePotionEffect(Potion.getPotionById(1)).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (double)(amplifier + 1);
        }
        return baseSpeed;
    }

    @Override
    public void onDisable() {
        this.moveSpeed = this.getStandart();
        yOffset = 0.0;
        this.level = 0;
        this.disabling = false;
    }
}


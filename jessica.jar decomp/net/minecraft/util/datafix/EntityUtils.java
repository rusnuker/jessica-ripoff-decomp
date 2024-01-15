/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix;

import com.mysql.fabric.Wrapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.KillauraSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.datafix.FriendManager;
import net.minecraft.util.datafix.RenderUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class EntityUtils {
    public static float[] rotationsToBlock = null;

    public static float[] getRotationsNeeded(Entity entity) {
        double diffY;
        if (entity == null) {
            return null;
        }
        double diffX = entity.posX - Wrapper.player().posX;
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase entityLivingBase = (EntityLivingBase)entity;
            diffY = entityLivingBase.posY + (double)entityLivingBase.getEyeHeight() * 0.9 - (Wrapper.player().posY + (double)Minecraft.getMinecraft().player.getEyeHeight());
        } else {
            diffY = (entity.boundingBox.minY + entity.boundingBox.maxY) / 2.0 - (Wrapper.player().posY + (double)Minecraft.getMinecraft().player.getEyeHeight());
        }
        double diffZ = entity.posZ - Wrapper.player().posZ;
        double dist = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
        return new float[]{Wrapper.player().rotationYaw + MathHelper.wrapDegrees(yaw - Wrapper.player().rotationYaw), Wrapper.player().rotationPitch + MathHelper.wrapDegrees(pitch - Wrapper.player().rotationPitch)};
    }

    public static float Pitch(EntityLivingBase ent) {
        double x = ent.posX - Wrapper.mc().player.posX;
        double y = ent.posY - Wrapper.mc().player.posY;
        double z = ent.posZ - Wrapper.mc().player.posZ;
        double pitch = Math.asin(y /= (double)Wrapper.mc().player.getDistanceToEntity(ent)) * 57.0;
        pitch = -pitch;
        return (float)pitch;
    }

    public static float Yaw(EntityLivingBase ent) {
        double x = ent.posX - Wrapper.mc().player.posX;
        double y = ent.posY - Wrapper.mc().player.posY;
        double z = ent.posZ - Wrapper.mc().player.posZ;
        double yaw = Math.atan2(x, z) * 57.0;
        yaw = -yaw;
        return (float)yaw;
    }

    public static Vec3d getEyesPos() {
        return new Vec3d(Wrapper.player().posX, Wrapper.player().posY + (double)Wrapper.player().getEyeHeight(), Wrapper.player().posZ);
    }

    private static float[] getNeededRotations2(Vec3d vec) {
        Vec3d eyesPos = EntityUtils.getEyesPos();
        double diffX = vec.xCoord - eyesPos.xCoord;
        double diffY = vec.yCoord - eyesPos.yCoord;
        double diffZ = vec.zCoord - eyesPos.zCoord;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{Wrapper.player().rotationYaw + MathHelper.wrapDegrees(yaw - Wrapper.player().rotationYaw), Wrapper.player().rotationPitch + MathHelper.wrapDegrees(pitch - Wrapper.player().rotationPitch)};
    }

    public static void faceVectorPacketInstant(Vec3d vec) {
        rotationsToBlock = EntityUtils.getNeededRotations2(vec);
    }

    public static boolean isMoving(Entity e) {
        return e.motionX != 0.0 && e.motionZ != 0.0 && (e.motionY != 0.0 || e.motionY > 0.0);
    }

    public static void damagePlayer() {
        double x = Wrapper.player().posX;
        double y = Wrapper.player().posY;
        double z = Wrapper.player().posZ;
        int i = 0;
        while (i < 65) {
            Wrapper.sendPacket(new CPacketPlayer.Position(x, y + 0.049, z, false));
            Wrapper.sendPacket(new CPacketPlayer.Position(x, y, z, false));
            ++i;
        }
        Wrapper.sendPacket(new CPacketPlayer.Position(x, y, z, true));
    }

    public static float[] facePacketEntity(Entity par1Entity, float par2, float par3) {
        double var8;
        Random rand = new Random();
        double var4 = par1Entity.posX - Wrapper.player().posX;
        double var6 = par1Entity.posZ - Wrapper.player().posZ;
        if (par1Entity instanceof EntityLivingBase) {
            EntityLivingBase var10 = (EntityLivingBase)par1Entity;
            var8 = var10.posY + ((double)var10.getEyeHeight() - 0.6) - (Wrapper.player().posY + (double)Wrapper.player().getEyeHeight());
        } else {
            var8 = (par1Entity.boundingBox.minY + par1Entity.boundingBox.maxY) / 2.0 - (Wrapper.player().posY + (double)Wrapper.player().getEyeHeight());
        }
        double var14 = MathHelper.sqrt(var4 * var4 + var6 * var6);
        float var12 = (float)(Math.atan2(var6, var4) * 180.0 / Math.PI) - 90.0f;
        float var13 = (float)(-(Math.atan2(var8, var14) * 180.0 / Math.PI));
        return new float[]{EntityLiving.updateRotation(Wrapper.player().rotationYaw, var12, par2), EntityLiving.updateRotation(Wrapper.player().rotationPitch, var13, par3)};
    }

    public static EntityLivingBase getClosestEntityLiving() {
        EntityLivingBase closestEntity = null;
        for (Entity o : Wrapper.world().loadedEntityList) {
            EntityLivingBase e;
            if (KillauraSettings.players && o instanceof EntityPlayer && !((e = (EntityLivingBase)o) instanceof EntityPlayerSP) && (KillauraSettings.throughWalls ? !e.isDead : Wrapper.player().canEntityBeSeen(e)) && (double)RenderUtils.getDistanceFromMouse(e) <= KillauraSettings.FOV && (double)Wrapper.player().getDistanceToEntity(e) <= KillauraSettings.range && !e.isDead) {
                Wrapper.getFriends();
                if (!FriendManager.isFriend(((EntityPlayer)e).getName()) && (KillauraSettings.team ? (KillauraSettings.team ? !e.getTeam().isSameTeam(Wrapper.player().getTeam()) : !e.isDead) : !e.isDead && e.getHealth() != 0.0f) && (closestEntity == null || Wrapper.player().getDistanceToEntity(e) < Wrapper.player().getDistanceToEntity(closestEntity))) {
                    closestEntity = e;
                }
            }
            if (!KillauraSettings.mobs || !(o instanceof EntityLivingBase) || (e = (EntityLivingBase)o) instanceof EntityPlayer || !((double)RenderUtils.getDistanceFromMouse(e) <= KillauraSettings.FOV) || !((double)Wrapper.player().getDistanceToEntity(e) <= KillauraSettings.range) || e.isDead || e.getHealth() == 0.0f || closestEntity != null && !(Wrapper.player().getDistanceToEntity(e) < Wrapper.player().getDistanceToEntity(closestEntity)) || !(KillauraSettings.throughWalls ? !e.isDead : Wrapper.player().canEntityBeSeen(e))) continue;
            closestEntity = e;
        }
        return closestEntity;
    }

    public static EntityLivingBase getClosestPlayer() {
        EntityLivingBase closestEntity = null;
        for (Entity o : Wrapper.world().loadedEntityList) {
            EntityLivingBase e;
            if (!(o instanceof EntityPlayer) || (e = (EntityLivingBase)o) instanceof EntityPlayerSP || closestEntity != null && !(Wrapper.player().getDistanceToEntity(e) < Wrapper.player().getDistanceToEntity(closestEntity))) continue;
            closestEntity = e;
        }
        return closestEntity;
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}


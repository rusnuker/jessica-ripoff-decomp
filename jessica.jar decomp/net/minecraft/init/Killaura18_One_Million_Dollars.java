/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.init;

import com.mysql.fabric.Category;
import com.mysql.fabric.Module;
import com.mysql.fabric.Wrapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.KillauraSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.datafix.EntityUtils;
import net.minecraft.util.datafix.FriendManager;
import net.minecraft.util.datafix.RenderUtils;
import net.minecraft.util.datafix.RotationUtils;
import net.minecraft.util.datafix.Timer2;
import net.minecraft.util.math.MathHelper;

public class Killaura18_One_Million_Dollars
extends Module {
    public static boolean active = false;
    public int delay;
    public static ArrayList<Entity> PlayerAuraAAC = new ArrayList();
    public int delayPacket = 0;
    public static Entity entity = null;
    public static boolean blockHit = false;
    public static boolean timerTrue = false;
    private final Timer2 timer = new Timer2();
    private final Timer2 timer2 = new Timer2();
    public static boolean hit = false;
    public static Entity hitEntity = null;

    public Killaura18_One_Million_Dollars() {
        super("Killaura", Category.Combat);
    }

    @Override
    public void onDisable() {
        hit = false;
        active = false;
        PlayerAuraAAC.clear();
        timerTrue = false;
        super.onDisable();
    }

    @Override
    public void onEnable() {
        PlayerAuraAAC.clear();
        for (Object o : Wrapper.world().loadedEntityList) {
            EntityPlayer e;
            if (!(o instanceof EntityPlayer) || (e = (EntityPlayer)o) instanceof EntityPlayerSP || e.isDead || e.isInvisible()) continue;
            Wrapper.getFriends();
            if (FriendManager.isFriend(e.getName())) continue;
            PlayerAuraAAC.add(e);
        }
        super.onEnable();
    }

    @Override
    public void onUpdate() {
        if (Wrapper.getModule("AAC").isToggled() || Wrapper.getModule("Spartan").isToggled() || Wrapper.getModule("HardCombat").isToggled()) {
            this.KillauraAAC1();
            this.KillauraAAC2();
        } else {
            this.Killaura1();
        }
        super.onUpdate();
    }

    public void Killaura1() {
        try {
            EntityLivingBase e = EntityUtils.getClosestEntityLiving();
            if (e == null) {
                timerTrue = false;
                return;
            }
            timerTrue = true;
            this.faceEntity2(e, KillauraSettings.yaw, KillauraSettings.pitch);
            active = true;
            entity = e;
            float[] yp = EntityUtils.facePacketEntity(e, KillauraSettings.yaw, KillauraSettings.pitch);
            RotationUtils.set(yp[0], yp[1] + 4.0f);
            if (this.timer.check(1000.0f / KillauraSettings.KillauraSpeed) && e != null && Wrapper.mc().objectMouseOver.entityHit == e) {
                Wrapper.mc().playerController.attackEntity(Wrapper.player(), e);
                Wrapper.player().swingArm(EnumHand.MAIN_HAND);
                this.timer.reset();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Unable to fully structure code
     */
    public void Killaura5() {
        try {
            for (E o : Wrapper.world().loadedEntityList) {
                block6: {
                    if (!KillauraSettings.players || !(o instanceof EntityPlayer)) break block6;
                    e = (EntityPlayer)o;
                    if (e instanceof EntityPlayerSP || e.getHealth() == 0.0f || !((double)RenderUtils.getDistanceFromMouse(e) <= KillauraSettings.FOV) || !((double)Wrapper.player().getDistanceToEntity(e) <= KillauraSettings.range) || e.isDead) ** GOTO lbl-1000
                    Wrapper.getFriends();
                    if (!FriendManager.isFriend(e.getName()) && Wrapper.player().canEntityBeSeen(e) && (KillauraSettings.team != false ? e.getTeam().isSameTeam(Wrapper.player().getTeam()) == false : e.isDead == false)) {
                        this.faceEntity2(e, KillauraSettings.yaw, KillauraSettings.pitch);
                        Killaura18_One_Million_Dollars.active = true;
                    } else lbl-1000:
                    // 2 sources

                    {
                        Killaura18_One_Million_Dollars.active = false;
                    }
                }
                if (!KillauraSettings.mobs || !(o instanceof EntityLivingBase)) continue;
                e = (EntityLivingBase)o;
                if (!(e instanceof EntityPlayer) && e.getHealth() != 0.0f && (double)Wrapper.player().getDistanceToEntity(e) <= KillauraSettings.range && !e.isDead && Wrapper.player().canEntityBeSeen(e)) {
                    this.faceEntity2(e, KillauraSettings.yaw, KillauraSettings.pitch);
                    Killaura18_One_Million_Dollars.active = true;
                    continue;
                }
                Killaura18_One_Million_Dollars.active = false;
            }
        }
        catch (Exception var1_3) {
            // empty catch block
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void KillauraAAC1() {
        try {
            EntityLivingBase e;
            Object o;
            Random rand = new Random();
            if (!this.timer.check((float)(1000.0 / ((double)KillauraSettings.KillauraSpeed + 0.01 * (double)rand.nextInt(100))))) return;
            Iterator iterator = Wrapper.world().loadedEntityList.iterator();
            do {
                if (!iterator.hasNext()) {
                    return;
                }
                o = iterator.next();
                if (!KillauraSettings.players || !(o instanceof EntityPlayer) || (e = (EntityPlayer)o) instanceof EntityPlayerSP || !((double)Wrapper.player().getDistanceToEntity(e) <= KillauraSettings.range) || ((EntityPlayer)e).isDead || !PlayerAuraAAC.contains(e) || e.isInvisible()) continue;
                Wrapper.getFriends();
                if (FriendManager.isFriend(((EntityPlayer)e).getName()) || !Wrapper.player().canEntityBeSeen(e) || !((double)RenderUtils.getDistanceFromMouse(e) <= KillauraSettings.FOV) || !(KillauraSettings.team ? !((EntityPlayer)e).getTeam().isSameTeam(Wrapper.player().getTeam()) : !((EntityPlayer)e).isDead && Wrapper.mc().objectMouseOver.entityHit != null)) continue;
                Wrapper.mc().clickMouse();
                this.timer.reset();
                return;
            } while (!KillauraSettings.mobs || !(o instanceof EntityLivingBase) || (e = (EntityLivingBase)o) instanceof EntityPlayer || !((double)Wrapper.player().getDistanceToEntity(e) <= KillauraSettings.range) || e.isDead || !Wrapper.player().canEntityBeSeen(e) || !((double)RenderUtils.getDistanceFromMouse(e) <= KillauraSettings.FOV) || Wrapper.mc().objectMouseOver.entityHit == null);
            Wrapper.mc().clickMouse();
            this.timer.reset();
            return;
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void KillauraAAC5Packet() {
        try {
            EntityPlayer cEP = null;
            EntityLivingBase cELB = null;
            int i = 0;
            for (Entity o : Wrapper.world().loadedEntityList) {
                float[] yp;
                if (KillauraSettings.players && o != null && o instanceof EntityPlayer && !(o instanceof EntityPlayerSP) && !Wrapper.player().isDead && (double)Wrapper.player().getDistanceToEntity(o) <= KillauraSettings.range + 3.5 && PlayerAuraAAC.contains((EntityPlayer)o) && !o.isInvisible()) {
                    Wrapper.getFriends();
                    if (!FriendManager.isFriend(((EntityPlayer)o).getName()) && (KillauraSettings.throughWalls ? !o.isDead : Wrapper.player().canEntityBeSeen(o)) && (double)RenderUtils.getDistanceFromMouse(o) <= KillauraSettings.FOV && (KillauraSettings.team ? !((EntityPlayer)o).getTeam().isSameTeam(Wrapper.player().getTeam()) : !o.isDead && (cEP == null || Wrapper.player().getDistanceToEntity(o) < Wrapper.player().getDistanceToEntity(cEP)))) {
                        cEP = (EntityPlayer)o;
                    }
                }
                if (KillauraSettings.mobs && o != null && o instanceof EntityLivingBase && !(o instanceof EntityPlayer) && (cELB == null || Wrapper.player().getDistanceToEntity(o) < Wrapper.player().getDistanceToEntity(cELB)) && (double)Wrapper.player().getDistanceToEntity(o) <= KillauraSettings.range + 3.5 && !o.isDead && (double)RenderUtils.getDistanceFromMouse(o) <= KillauraSettings.FOV && (KillauraSettings.throughWalls ? !o.isDead : Wrapper.player().canEntityBeSeen(o))) {
                    cELB = (EntityLivingBase)o;
                }
                if (i != Wrapper.world().loadedEntityList.size() - 1) {
                    ++i;
                    continue;
                }
                if (cEP == null && cELB == null) {
                    active = false;
                    timerTrue = false;
                    RotationUtils.yaw = Wrapper.player().rotationYaw;
                    RotationUtils.pitch = Wrapper.player().rotationPitch;
                }
                if (KillauraSettings.players && cEP != null && (KillauraSettings.mobs ? cELB == null || Wrapper.player().getDistanceToEntity(cEP) <= Wrapper.player().getDistanceToEntity(cELB) : KillauraSettings.players)) {
                    entity = cEP;
                    timerTrue = true;
                    active = true;
                    yp = EntityUtils.facePacketEntity(entity, KillauraSettings.yaw, KillauraSettings.pitch);
                    RotationUtils.set(yp[0], yp[1] + 8.0f);
                    if (this.timer2.check((float)(1000.0 / ((double)KillauraSettings.KillauraSpeed + 0.01 * (double)new Random().nextInt(100)))) && hitEntity != null && (double)Wrapper.player().getDistanceToEntity(hitEntity) <= KillauraSettings.range) {
                        Wrapper.mc().playerController.attackEntity(Wrapper.player(), hitEntity);
                        Wrapper.player().swingArm(EnumHand.MAIN_HAND);
                        hit = false;
                        hitEntity = null;
                        this.timer2.reset();
                        break;
                    }
                }
                if (!KillauraSettings.mobs || cELB == null || !(KillauraSettings.players ? cEP == null || Wrapper.player().getDistanceToEntity(cELB) <= Wrapper.player().getDistanceToEntity(cEP) : KillauraSettings.mobs)) continue;
                entity = cELB;
                timerTrue = true;
                active = true;
                yp = EntityUtils.facePacketEntity(entity, KillauraSettings.yaw, KillauraSettings.pitch);
                RotationUtils.set(yp[0], yp[1]);
                if (!this.timer2.check((float)(1000.0 / ((double)KillauraSettings.KillauraSpeed + 0.01 * (double)new Random().nextInt(100)))) || hitEntity == null || !((double)Wrapper.player().getDistanceToEntity(hitEntity) <= KillauraSettings.range)) continue;
                Wrapper.mc().playerController.attackEntity(Wrapper.player(), hitEntity);
                Wrapper.player().swingArm(EnumHand.MAIN_HAND);
                hit = false;
                hitEntity = null;
                this.timer2.reset();
                break;
            }
            if (entity == null) {
                timerTrue = false;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    /*
     * Unable to fully structure code
     */
    public void KillauraAAC2() {
        block9: {
            if (!Killaura18_One_Million_Dollars.timerTrue) {
                Wrapper.player().rotationYaw2 = Wrapper.player().rotationYaw;
                Wrapper.player().rotationPitch2 = Wrapper.player().rotationPitch;
            }
            ent = null;
            try {
                if (!this.timer2.check((float)new Random().nextInt(20) / 100.0f)) break block9;
                for (E o : Wrapper.world().loadedEntityList) {
                    if (!KillauraSettings.players || !(o instanceof EntityPlayer) || (e = (EntityPlayer)o) instanceof EntityPlayerSP || !((double)Wrapper.player().getDistanceToEntity(e) <= KillauraSettings.range) || e.isDead || !Killaura18_One_Million_Dollars.PlayerAuraAAC.contains(e) || e.isInvisible()) ** GOTO lbl-1000
                    Wrapper.getFriends();
                    if (!FriendManager.isFriend(e.getName()) && Wrapper.player().canEntityBeSeen(e) && (double)RenderUtils.getDistanceFromMouse(e) <= KillauraSettings.FOV && !e.isInvisible() && (KillauraSettings.team != false ? e.getTeam().isSameTeam(Wrapper.player().getTeam()) == false : e.isDead == false)) {
                        ent = e;
                        if (Wrapper.mc().objectMouseOver.entityHit != e) {
                            Killaura18_One_Million_Dollars.timerTrue = true;
                            this.faceEntity2(e, KillauraSettings.yaw + (float)new Random().nextInt(1000) / 100.0f, KillauraSettings.pitch + (float)new Random().nextInt(1000) / 100.0f);
                        }
                        this.timer2.reset();
                    } else lbl-1000:
                    // 2 sources

                    {
                        if (!KillauraSettings.mobs || !(o instanceof EntityLivingBase) || (e = (EntityLivingBase)o) instanceof EntityPlayer || !((double)Wrapper.player().getDistanceToEntity(e) <= KillauraSettings.range) || e.isDead || !Wrapper.player().canEntityBeSeen(e) || !((double)RenderUtils.getDistanceFromMouse(e) <= KillauraSettings.FOV) || !(KillauraSettings.team != false ? e.getTeam().isSameTeam(Wrapper.player().getTeam()) == false : e.isDead == false)) continue;
                        ent = e;
                        if (Wrapper.mc().objectMouseOver.entityHit != e) {
                            Killaura18_One_Million_Dollars.timerTrue = true;
                            this.faceEntity2(e, KillauraSettings.yaw + (float)new Random().nextInt(1000) / 100.0f, KillauraSettings.pitch + (float)new Random().nextInt(1000) / 100.0f);
                        }
                        this.timer2.reset();
                    }
                    break;
                }
            }
            catch (Exception var2_4) {
                // empty catch block
            }
        }
        if (ent == null) {
            Killaura18_One_Million_Dollars.timerTrue = false;
        }
    }

    public void faceEntity2(Entity par1Entity, float par2, float par3) {
        double var8;
        Random rand = new Random();
        double var4 = par1Entity.posX - Wrapper.player().posX;
        double var6 = par1Entity.posZ - Wrapper.player().posZ;
        if (par1Entity instanceof EntityLivingBase) {
            EntityLivingBase var10 = (EntityLivingBase)par1Entity;
            var8 = var10.posY + ((double)var10.getEyeHeight() - (Wrapper.getModule("AAC").isToggled() || Wrapper.getModule("HardCombat").isToggled() || Wrapper.getModule("Spartan").isToggled() ? 0.1 + 0.01 * (double)rand.nextInt(40) : 0.6)) - (Wrapper.player().posY + (double)Wrapper.player().getEyeHeight());
        } else {
            var8 = (par1Entity.boundingBox.minY + par1Entity.boundingBox.maxY) / 2.0 - (Wrapper.player().posY + (double)Wrapper.player().getEyeHeight());
        }
        double var14 = MathHelper.sqrt(var4 * var4 + var6 * var6);
        float var12 = (float)(Math.atan2(var6, var4) * 180.0 / Math.PI) - 90.0f;
        float var13 = (float)(-(Math.atan2(var8, var14) * 180.0 / Math.PI));
        Wrapper.player().rotationPitch2 = EntityLiving.updateRotation(Wrapper.player().rotationPitch2, var13, par3);
        Wrapper.player().rotationYaw2 = EntityLiving.updateRotation(Wrapper.player().rotationYaw2, var12, par2);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.village;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;

public class VillageSiege {
    private final World worldObj;
    private boolean hasSetupSiege;
    private int siegeState = -1;
    private int siegeCount;
    private int nextSpawnTime;
    private Village theVillage;
    private int spawnX;
    private int spawnY;
    private int spawnZ;

    public VillageSiege(World worldIn) {
        this.worldObj = worldIn;
    }

    public void tick() {
        if (this.worldObj.isDaytime()) {
            this.siegeState = 0;
        } else if (this.siegeState != 2) {
            if (this.siegeState == 0) {
                float f = this.worldObj.getCelestialAngle(0.0f);
                if ((double)f < 0.5 || (double)f > 0.501) {
                    return;
                }
                this.siegeState = this.worldObj.rand.nextInt(10) == 0 ? 1 : 2;
                this.hasSetupSiege = false;
                if (this.siegeState == 2) {
                    return;
                }
            }
            if (this.siegeState != -1) {
                if (!this.hasSetupSiege) {
                    if (!this.trySetupSiege()) {
                        return;
                    }
                    this.hasSetupSiege = true;
                }
                if (this.nextSpawnTime > 0) {
                    --this.nextSpawnTime;
                } else {
                    this.nextSpawnTime = 2;
                    if (this.siegeCount > 0) {
                        this.spawnZombie();
                        --this.siegeCount;
                    } else {
                        this.siegeState = 2;
                    }
                }
            }
        }
    }

    private boolean trySetupSiege() {
        List<EntityPlayer> list = this.worldObj.playerEntities;
        Iterator<EntityPlayer> iterator = list.iterator();
        while (true) {
            if (!iterator.hasNext()) {
                return false;
            }
            EntityPlayer entityplayer = iterator.next();
            if (entityplayer.isSpectator()) continue;
            this.theVillage = this.worldObj.getVillageCollection().getNearestVillage(new BlockPos(entityplayer), 1);
            if (this.theVillage == null || this.theVillage.getNumVillageDoors() < 10 || this.theVillage.getTicksSinceLastDoorAdding() < 20 || this.theVillage.getNumVillagers() < 20) continue;
            BlockPos blockpos = this.theVillage.getCenter();
            float f = this.theVillage.getVillageRadius();
            boolean flag = false;
            int i = 0;
            while (i < 10) {
                float f1 = this.worldObj.rand.nextFloat() * ((float)Math.PI * 2);
                this.spawnX = blockpos.getX() + (int)((double)(MathHelper.cos(f1) * f) * 0.9);
                this.spawnY = blockpos.getY();
                this.spawnZ = blockpos.getZ() + (int)((double)(MathHelper.sin(f1) * f) * 0.9);
                flag = false;
                for (Village village : this.worldObj.getVillageCollection().getVillageList()) {
                    if (village == this.theVillage || !village.isBlockPosWithinSqVillageRadius(new BlockPos(this.spawnX, this.spawnY, this.spawnZ))) continue;
                    flag = true;
                    break;
                }
                if (!flag) break;
                ++i;
            }
            if (flag) {
                return false;
            }
            Vec3d vec3d = this.findRandomSpawnPos(new BlockPos(this.spawnX, this.spawnY, this.spawnZ));
            if (vec3d != null) break;
        }
        this.nextSpawnTime = 0;
        this.siegeCount = 20;
        return true;
    }

    private boolean spawnZombie() {
        EntityZombie entityzombie;
        Vec3d vec3d = this.findRandomSpawnPos(new BlockPos(this.spawnX, this.spawnY, this.spawnZ));
        if (vec3d == null) {
            return false;
        }
        try {
            entityzombie = new EntityZombie(this.worldObj);
            entityzombie.onInitialSpawn(this.worldObj.getDifficultyForLocation(new BlockPos(entityzombie)), null);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
        entityzombie.setLocationAndAngles(vec3d.xCoord, vec3d.yCoord, vec3d.zCoord, this.worldObj.rand.nextFloat() * 360.0f, 0.0f);
        this.worldObj.spawnEntityInWorld(entityzombie);
        BlockPos blockpos = this.theVillage.getCenter();
        entityzombie.setHomePosAndDistance(blockpos, this.theVillage.getVillageRadius());
        return true;
    }

    @Nullable
    private Vec3d findRandomSpawnPos(BlockPos pos) {
        int i = 0;
        while (i < 10) {
            BlockPos blockpos = pos.add(this.worldObj.rand.nextInt(16) - 8, this.worldObj.rand.nextInt(6) - 3, this.worldObj.rand.nextInt(16) - 8);
            if (this.theVillage.isBlockPosWithinSqVillageRadius(blockpos) && WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, this.worldObj, blockpos)) {
                return new Vec3d(blockpos.getX(), blockpos.getY(), blockpos.getZ());
            }
            ++i;
        }
        return null;
    }
}


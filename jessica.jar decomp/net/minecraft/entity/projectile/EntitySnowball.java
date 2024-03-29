/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntitySnowball
extends EntityThrowable {
    public EntitySnowball(World worldIn) {
        super(worldIn);
    }

    public EntitySnowball(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn, throwerIn);
    }

    public EntitySnowball(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    public static void registerFixesSnowball(DataFixer fixer) {
        EntityThrowable.registerFixesThrowable(fixer, "Snowball");
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 3) {
            int i = 0;
            while (i < 8) {
                this.world.spawnParticle(EnumParticleTypes.SNOWBALL, this.posX, this.posY, this.posZ, 0.0, 0.0, 0.0, new int[0]);
                ++i;
            }
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (result.entityHit != null) {
            int i = 0;
            if (result.entityHit instanceof EntityBlaze) {
                i = 3;
            }
            result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), i);
        }
        if (!this.world.isRemote) {
            this.world.setEntityState(this, (byte)3);
            this.setDead();
        }
    }
}


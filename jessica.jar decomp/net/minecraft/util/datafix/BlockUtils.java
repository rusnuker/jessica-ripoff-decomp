/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix;

import com.mysql.fabric.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPackedIce;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.datafix.EntityUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlockUtils {
    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static boolean isOnLiquid() {
        try {
            boolean onLiquid = false;
            int y = (int)(Wrapper.player().boundingBox.minY - 0.01);
            int x = MathHelper.floor(Wrapper.player().boundingBox.minX);
            block2: while (true) {
                if (x >= MathHelper.floor(Wrapper.player().boundingBox.maxX) + 1) {
                    return onLiquid;
                }
                int z = MathHelper.floor(Wrapper.player().boundingBox.minZ);
                while (true) {
                    if (z >= MathHelper.floor(Wrapper.player().boundingBox.maxZ) + 1) {
                        ++x;
                        continue block2;
                    }
                    Block block = Wrapper.world().getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (block != null && !(block instanceof BlockAir)) {
                        if (!(block instanceof BlockLiquid)) {
                            return false;
                        }
                        onLiquid = true;
                    }
                    ++z;
                }
                break;
            }
        }
        catch (Exception ex) {
            return false;
        }
    }

    public static boolean placeBlockScaffold(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(Wrapper.player().posX, Wrapper.player().posY + (double)Wrapper.player().getEyeHeight(), Wrapper.player().posZ);
        EnumFacing[] values = EnumFacing.values();
        int length = values.length;
        int i = 0;
        while (i < length) {
            Vec3d hitVec;
            EnumFacing side = values[i];
            BlockPos neighbor = pos.offset(side);
            EnumFacing side2 = side.getOpposite();
            Vec3d vec3d = new Vec3d(pos);
            Vec3d vec3d2 = new Vec3d(neighbor);
            if (eyesPos.squareDistanceTo(vec3d.addVector(0.5, 0.5, 0.5)) < eyesPos.squareDistanceTo(vec3d2.addVector(0.5, 0.5, 0.5)) && BlockUtils.canBeClicked(neighbor) && eyesPos.squareDistanceTo(hitVec = new Vec3d(neighbor).addVector(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5))) <= 18.0625) {
                EntityUtils.faceVectorPacketInstant(hitVec);
                Wrapper.player().swingArm(EnumHand.MAIN_HAND);
                Wrapper.mc().playerController.processRightClickBlock(Wrapper.player(), Wrapper.world(), neighbor, side2, hitVec, EnumHand.MAIN_HAND);
                Wrapper.mc().rightClickDelayTimer = 4;
                return true;
            }
            ++i;
        }
        return false;
    }

    public static boolean canBeClicked(BlockPos pos) {
        return Wrapper.world().getBlockState(pos).getBlock().canCollideCheck(Wrapper.world().getBlockState(pos), false);
    }

    public static void faceBlock(BlockPos blockPos) {
        double X = (double)blockPos.getX() + 0.5 - Wrapper.player().posX;
        double Y = (double)blockPos.getY() + 0.5 - (Wrapper.player().posY + (double)Wrapper.player().getEyeHeight());
        double Z = (double)blockPos.getZ() + 0.5 - Wrapper.player().posZ;
        double dist = MathHelper.sqrt(X * X + Z * Z);
        Wrapper.sendPacket(new CPacketPlayer.Rotation((float)(Math.atan2(Z, X) * 180.0 / Math.PI) - 90.0f, (float)(-(Math.atan2(Y, dist) * 180.0 / Math.PI)), Wrapper.player().onGround));
    }

    public static boolean isInLiquid() {
        boolean inLiquid = false;
        int y = (int)Wrapper.player().boundingBox.minY;
        int x = MathHelper.floor(Wrapper.player().boundingBox.minX);
        while (x < MathHelper.floor(Wrapper.player().boundingBox.maxX) + 1) {
            int z = MathHelper.floor(Wrapper.player().boundingBox.minZ);
            while (z < MathHelper.floor(Wrapper.player().boundingBox.maxZ) + 1) {
                Block block = Wrapper.world().getBlockState(new BlockPos(x, y, z)).getBlock();
                if (block != null && !(block instanceof BlockAir)) {
                    if (!(block instanceof BlockLiquid)) {
                        return false;
                    }
                    inLiquid = true;
                }
                ++z;
            }
            ++x;
        }
        return inLiquid;
    }

    public static boolean isOnIce() {
        boolean onIce = false;
        int y = (int)(Wrapper.player().boundingBox.minY - 1.0);
        int x = MathHelper.floor(Wrapper.player().boundingBox.minX);
        while (x < MathHelper.floor(Wrapper.player().boundingBox.maxX) + 1) {
            int z = MathHelper.floor(Wrapper.player().boundingBox.minZ);
            while (z < MathHelper.floor(Wrapper.player().boundingBox.maxZ) + 1) {
                Block block = Wrapper.world().getBlockState(new BlockPos(x, y, z)).getBlock();
                if (block != null && !(block instanceof BlockAir) && (block instanceof BlockPackedIce || block instanceof BlockIce)) {
                    onIce = true;
                }
                ++z;
            }
            ++x;
        }
        return onIce;
    }

    public static boolean isOnLadder() {
        boolean onLadder = false;
        int y = (int)(Wrapper.player().boundingBox.minY - 1.0);
        int x = MathHelper.floor(Wrapper.player().boundingBox.minX);
        while (x < MathHelper.floor(Wrapper.player().boundingBox.maxX) + 1) {
            int z = MathHelper.floor(Wrapper.player().boundingBox.minZ);
            while (z < MathHelper.floor(Wrapper.player().boundingBox.maxZ) + 1) {
                Block block = Wrapper.world().getBlockState(new BlockPos(x, y, z)).getBlock();
                if (block != null && !(block instanceof BlockAir)) {
                    if (!(block instanceof BlockLadder)) {
                        return false;
                    }
                    onLadder = true;
                }
                ++z;
            }
            ++x;
        }
        return onLadder || Wrapper.player().isOnLadder();
    }
}


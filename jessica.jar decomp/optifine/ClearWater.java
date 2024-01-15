/*
 * Decompiled with CFR 0.152.
 */
package optifine;

import net.minecraft.block.BlockAir;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import optifine.BlockPosM;
import optifine.Config;

public class ClearWater {
    public static void updateWaterOpacity(GameSettings p_updateWaterOpacity_0_, World p_updateWaterOpacity_1_) {
        Entity entity;
        IChunkProvider ichunkprovider;
        if (p_updateWaterOpacity_0_ != null) {
            int i = 3;
            if (p_updateWaterOpacity_0_.ofClearWater) {
                i = 1;
            }
            BlockAir.setLightOpacity(Blocks.WATER, i);
            BlockAir.setLightOpacity(Blocks.FLOWING_WATER, i);
        }
        if (p_updateWaterOpacity_1_ != null && (ichunkprovider = p_updateWaterOpacity_1_.getChunkProvider()) != null && (entity = Config.getMinecraft().getRenderViewEntity()) != null) {
            int j = (int)entity.posX / 16;
            int k = (int)entity.posZ / 16;
            int l = j - 512;
            int i1 = j + 512;
            int j1 = k - 512;
            int k1 = k + 512;
            int l1 = 0;
            int i2 = l;
            while (i2 < i1) {
                int j2 = j1;
                while (j2 < k1) {
                    Chunk chunk = ichunkprovider.getLoadedChunk(i2, j2);
                    if (chunk != null && !(chunk instanceof EmptyChunk)) {
                        int k2 = i2 << 4;
                        int l2 = j2 << 4;
                        int i3 = k2 + 16;
                        int j3 = l2 + 16;
                        BlockPosM blockposm = new BlockPosM(0, 0, 0);
                        BlockPosM blockposm1 = new BlockPosM(0, 0, 0);
                        int k3 = k2;
                        while (k3 < i3) {
                            int l3 = l2;
                            while (l3 < j3) {
                                blockposm.setXyz(k3, 0, l3);
                                BlockPos blockpos = p_updateWaterOpacity_1_.getPrecipitationHeight(blockposm);
                                int i4 = 0;
                                while (i4 < blockpos.getY()) {
                                    blockposm1.setXyz(k3, i4, l3);
                                    IBlockState iblockstate = p_updateWaterOpacity_1_.getBlockState(blockposm1);
                                    if (iblockstate.getMaterial() == Material.WATER) {
                                        p_updateWaterOpacity_1_.markBlocksDirtyVertical(k3, l3, blockposm1.getY(), blockpos.getY());
                                        ++l1;
                                        break;
                                    }
                                    ++i4;
                                }
                                ++l3;
                            }
                            ++k3;
                        }
                    }
                    ++j2;
                }
                ++i2;
            }
            if (l1 > 0) {
                String s = "server";
                if (Config.isMinecraftThread()) {
                    s = "client";
                }
                Config.dbg("ClearWater (" + s + ") relighted " + l1 + " chunks");
            }
        }
    }
}


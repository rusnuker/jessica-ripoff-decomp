/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class DebugRendererChunkBorder
implements DebugRenderer.IDebugRenderer {
    private final Minecraft minecraft;

    public DebugRendererChunkBorder(Minecraft minecraftIn) {
        this.minecraft = minecraftIn;
    }

    @Override
    public void render(float p_190060_1_, long p_190060_2_) {
        EntityPlayerSP entityplayer = this.minecraft.player;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        double d0 = entityplayer.lastTickPosX + (entityplayer.posX - entityplayer.lastTickPosX) * (double)p_190060_1_;
        double d1 = entityplayer.lastTickPosY + (entityplayer.posY - entityplayer.lastTickPosY) * (double)p_190060_1_;
        double d2 = entityplayer.lastTickPosZ + (entityplayer.posZ - entityplayer.lastTickPosZ) * (double)p_190060_1_;
        double d3 = 0.0 - d1;
        double d4 = 256.0 - d1;
        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();
        double d5 = (double)(entityplayer.chunkCoordX << 4) - d0;
        double d6 = (double)(entityplayer.chunkCoordZ << 4) - d2;
        GlStateManager.glLineWidth(1.0f);
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        int i = -16;
        while (i <= 32) {
            int j = -16;
            while (j <= 32) {
                bufferbuilder.pos(d5 + (double)i, d3, d6 + (double)j).color(1.0f, 0.0f, 0.0f, 0.0f).endVertex();
                bufferbuilder.pos(d5 + (double)i, d3, d6 + (double)j).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                bufferbuilder.pos(d5 + (double)i, d4, d6 + (double)j).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                bufferbuilder.pos(d5 + (double)i, d4, d6 + (double)j).color(1.0f, 0.0f, 0.0f, 0.0f).endVertex();
                j += 16;
            }
            i += 16;
        }
        int k = 2;
        while (k < 16) {
            bufferbuilder.pos(d5 + (double)k, d3, d6).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferbuilder.pos(d5 + (double)k, d3, d6).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5 + (double)k, d4, d6).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5 + (double)k, d4, d6).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferbuilder.pos(d5 + (double)k, d3, d6 + 16.0).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferbuilder.pos(d5 + (double)k, d3, d6 + 16.0).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5 + (double)k, d4, d6 + 16.0).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5 + (double)k, d4, d6 + 16.0).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            k += 2;
        }
        int l = 2;
        while (l < 16) {
            bufferbuilder.pos(d5, d3, d6 + (double)l).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferbuilder.pos(d5, d3, d6 + (double)l).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5, d4, d6 + (double)l).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5, d4, d6 + (double)l).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferbuilder.pos(d5 + 16.0, d3, d6 + (double)l).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferbuilder.pos(d5 + 16.0, d3, d6 + (double)l).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5 + 16.0, d4, d6 + (double)l).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5 + 16.0, d4, d6 + (double)l).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            l += 2;
        }
        int i1 = 0;
        while (i1 <= 256) {
            double d7 = (double)i1 - d1;
            bufferbuilder.pos(d5, d7, d6).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferbuilder.pos(d5, d7, d6).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5, d7, d6 + 16.0).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5 + 16.0, d7, d6 + 16.0).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5 + 16.0, d7, d6).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5, d7, d6).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5, d7, d6).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            i1 += 2;
        }
        tessellator.draw();
        GlStateManager.glLineWidth(2.0f);
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        int j1 = 0;
        while (j1 <= 16) {
            int l1 = 0;
            while (l1 <= 16) {
                bufferbuilder.pos(d5 + (double)j1, d3, d6 + (double)l1).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
                bufferbuilder.pos(d5 + (double)j1, d3, d6 + (double)l1).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
                bufferbuilder.pos(d5 + (double)j1, d4, d6 + (double)l1).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
                bufferbuilder.pos(d5 + (double)j1, d4, d6 + (double)l1).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
                l1 += 16;
            }
            j1 += 16;
        }
        int k1 = 0;
        while (k1 <= 256) {
            double d8 = (double)k1 - d1;
            bufferbuilder.pos(d5, d8, d6).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
            bufferbuilder.pos(d5, d8, d6).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5, d8, d6 + 16.0).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5 + 16.0, d8, d6 + 16.0).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5 + 16.0, d8, d6).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5, d8, d6).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferbuilder.pos(d5, d8, d6).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
            k1 += 16;
        }
        tessellator.draw();
        GlStateManager.glLineWidth(1.0f);
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
    }
}


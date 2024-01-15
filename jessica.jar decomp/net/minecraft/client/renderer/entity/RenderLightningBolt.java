/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.ResourceLocation;

public class RenderLightningBolt
extends Render<EntityLightningBolt> {
    public RenderLightningBolt(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(EntityLightningBolt entity, double x, double y, double z, float entityYaw, float partialTicks) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        double[] adouble = new double[8];
        double[] adouble1 = new double[8];
        double d0 = 0.0;
        double d1 = 0.0;
        Random random = new Random(entity.boltVertex);
        int i = 7;
        while (i >= 0) {
            adouble[i] = d0;
            adouble1[i] = d1;
            d0 += (double)(random.nextInt(11) - 5);
            d1 += (double)(random.nextInt(11) - 5);
            --i;
        }
        int k1 = 0;
        while (k1 < 4) {
            Random random1 = new Random(entity.boltVertex);
            int j = 0;
            while (j < 3) {
                int k = 7;
                int l = 0;
                if (j > 0) {
                    k = 7 - j;
                }
                if (j > 0) {
                    l = k - 2;
                }
                double d2 = adouble[k] - d0;
                double d3 = adouble1[k] - d1;
                int i1 = k;
                while (i1 >= l) {
                    double d4 = d2;
                    double d5 = d3;
                    if (j == 0) {
                        d2 += (double)(random1.nextInt(11) - 5);
                        d3 += (double)(random1.nextInt(11) - 5);
                    } else {
                        d2 += (double)(random1.nextInt(31) - 15);
                        d3 += (double)(random1.nextInt(31) - 15);
                    }
                    bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);
                    float f = 0.5f;
                    float f1 = 0.45f;
                    float f2 = 0.45f;
                    float f3 = 0.5f;
                    double d6 = 0.1 + (double)k1 * 0.2;
                    if (j == 0) {
                        d6 *= (double)i1 * 0.1 + 1.0;
                    }
                    double d7 = 0.1 + (double)k1 * 0.2;
                    if (j == 0) {
                        d7 *= (double)(i1 - 1) * 0.1 + 1.0;
                    }
                    int j1 = 0;
                    while (j1 < 5) {
                        double d8 = x + 0.5 - d6;
                        double d9 = z + 0.5 - d6;
                        if (j1 == 1 || j1 == 2) {
                            d8 += d6 * 2.0;
                        }
                        if (j1 == 2 || j1 == 3) {
                            d9 += d6 * 2.0;
                        }
                        double d10 = x + 0.5 - d7;
                        double d11 = z + 0.5 - d7;
                        if (j1 == 1 || j1 == 2) {
                            d10 += d7 * 2.0;
                        }
                        if (j1 == 2 || j1 == 3) {
                            d11 += d7 * 2.0;
                        }
                        bufferbuilder.pos(d10 + d2, y + (double)(i1 * 16), d11 + d3).color(0.45f, 0.45f, 0.5f, 0.3f).endVertex();
                        bufferbuilder.pos(d8 + d4, y + (double)((i1 + 1) * 16), d9 + d5).color(0.45f, 0.45f, 0.5f, 0.3f).endVertex();
                        ++j1;
                    }
                    tessellator.draw();
                    --i1;
                }
                ++j;
            }
            ++k1;
        }
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
    }

    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityLightningBolt entity) {
        return null;
    }
}


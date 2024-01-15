/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelBlaze
extends ModelBase {
    private final ModelRenderer[] blazeSticks = new ModelRenderer[12];
    private final ModelRenderer blazeHead;

    public ModelBlaze() {
        int i = 0;
        while (i < this.blazeSticks.length) {
            this.blazeSticks[i] = new ModelRenderer(this, 0, 16);
            this.blazeSticks[i].addBox(0.0f, 0.0f, 0.0f, 2, 8, 2);
            ++i;
        }
        this.blazeHead = new ModelRenderer(this, 0, 0);
        this.blazeHead.addBox(-4.0f, -4.0f, -4.0f, 8, 8, 8);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.blazeHead.render(scale);
        ModelRenderer[] modelRendererArray = this.blazeSticks;
        int n = this.blazeSticks.length;
        int n2 = 0;
        while (n2 < n) {
            ModelRenderer modelrenderer = modelRendererArray[n2];
            modelrenderer.render(scale);
            ++n2;
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        float f = ageInTicks * (float)Math.PI * -0.1f;
        int i = 0;
        while (i < 4) {
            this.blazeSticks[i].rotationPointY = -2.0f + MathHelper.cos(((float)(i * 2) + ageInTicks) * 0.25f);
            this.blazeSticks[i].rotationPointX = MathHelper.cos(f) * 9.0f;
            this.blazeSticks[i].rotationPointZ = MathHelper.sin(f) * 9.0f;
            f += 1.0f;
            ++i;
        }
        f = 0.7853982f + ageInTicks * (float)Math.PI * 0.03f;
        int j = 4;
        while (j < 8) {
            this.blazeSticks[j].rotationPointY = 2.0f + MathHelper.cos(((float)(j * 2) + ageInTicks) * 0.25f);
            this.blazeSticks[j].rotationPointX = MathHelper.cos(f) * 7.0f;
            this.blazeSticks[j].rotationPointZ = MathHelper.sin(f) * 7.0f;
            f += 1.0f;
            ++j;
        }
        f = 0.47123894f + ageInTicks * (float)Math.PI * -0.05f;
        int k = 8;
        while (k < 12) {
            this.blazeSticks[k].rotationPointY = 11.0f + MathHelper.cos(((float)k * 1.5f + ageInTicks) * 0.5f);
            this.blazeSticks[k].rotationPointX = MathHelper.cos(f) * 5.0f;
            this.blazeSticks[k].rotationPointZ = MathHelper.sin(f) * 5.0f;
            f += 1.0f;
            ++k;
        }
        this.blazeHead.rotateAngleY = netHeadYaw * ((float)Math.PI / 180);
        this.blazeHead.rotateAngleX = headPitch * ((float)Math.PI / 180);
    }
}


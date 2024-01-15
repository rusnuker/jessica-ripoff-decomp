/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelEnderMite
extends ModelBase {
    private static final int[][] BODY_SIZES = new int[][]{{4, 3, 2}, {6, 4, 5}, {3, 3, 1}, {1, 2, 1}};
    private static final int[][] BODY_TEXS;
    private static final int BODY_COUNT;
    private final ModelRenderer[] bodyParts = new ModelRenderer[BODY_COUNT];

    static {
        int[][] nArrayArray = new int[4][];
        nArrayArray[0] = new int[2];
        int[] nArray = new int[2];
        nArray[1] = 5;
        nArrayArray[1] = nArray;
        int[] nArray2 = new int[2];
        nArray2[1] = 14;
        nArrayArray[2] = nArray2;
        int[] nArray3 = new int[2];
        nArray3[1] = 18;
        nArrayArray[3] = nArray3;
        BODY_TEXS = nArrayArray;
        BODY_COUNT = BODY_SIZES.length;
    }

    public ModelEnderMite() {
        float f = -3.5f;
        int i = 0;
        while (i < this.bodyParts.length) {
            this.bodyParts[i] = new ModelRenderer(this, BODY_TEXS[i][0], BODY_TEXS[i][1]);
            this.bodyParts[i].addBox((float)BODY_SIZES[i][0] * -0.5f, 0.0f, (float)BODY_SIZES[i][2] * -0.5f, BODY_SIZES[i][0], BODY_SIZES[i][1], BODY_SIZES[i][2]);
            this.bodyParts[i].setRotationPoint(0.0f, 24 - BODY_SIZES[i][1], f);
            if (i < this.bodyParts.length - 1) {
                f += (float)(BODY_SIZES[i][2] + BODY_SIZES[i + 1][2]) * 0.5f;
            }
            ++i;
        }
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        ModelRenderer[] modelRendererArray = this.bodyParts;
        int n = this.bodyParts.length;
        int n2 = 0;
        while (n2 < n) {
            ModelRenderer modelrenderer = modelRendererArray[n2];
            modelrenderer.render(scale);
            ++n2;
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        int i = 0;
        while (i < this.bodyParts.length) {
            this.bodyParts[i].rotateAngleY = MathHelper.cos(ageInTicks * 0.9f + (float)i * 0.15f * (float)Math.PI) * (float)Math.PI * 0.01f * (float)(1 + Math.abs(i - 2));
            this.bodyParts[i].rotationPointX = MathHelper.sin(ageInTicks * 0.9f + (float)i * 0.15f * (float)Math.PI) * (float)Math.PI * 0.1f * (float)Math.abs(i - 2);
            ++i;
        }
    }
}


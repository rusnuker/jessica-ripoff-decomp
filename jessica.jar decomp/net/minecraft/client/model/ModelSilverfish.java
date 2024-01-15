/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelSilverfish
extends ModelBase {
    private final ModelRenderer[] silverfishBodyParts = new ModelRenderer[7];
    private final ModelRenderer[] silverfishWings;
    private final float[] zPlacement = new float[7];
    private static final int[][] SILVERFISH_BOX_LENGTH = new int[][]{{3, 2, 2}, {4, 3, 2}, {6, 4, 3}, {3, 3, 3}, {2, 2, 3}, {2, 1, 2}, {1, 1, 2}};
    private static final int[][] SILVERFISH_TEXTURE_POSITIONS;

    static {
        int[][] nArrayArray = new int[7][];
        nArrayArray[0] = new int[2];
        int[] nArray = new int[2];
        nArray[1] = 4;
        nArrayArray[1] = nArray;
        int[] nArray2 = new int[2];
        nArray2[1] = 9;
        nArrayArray[2] = nArray2;
        int[] nArray3 = new int[2];
        nArray3[1] = 16;
        nArrayArray[3] = nArray3;
        int[] nArray4 = new int[2];
        nArray4[1] = 22;
        nArrayArray[4] = nArray4;
        int[] nArray5 = new int[2];
        nArray5[0] = 11;
        nArrayArray[5] = nArray5;
        nArrayArray[6] = new int[]{13, 4};
        SILVERFISH_TEXTURE_POSITIONS = nArrayArray;
    }

    public ModelSilverfish() {
        float f = -3.5f;
        int i = 0;
        while (i < this.silverfishBodyParts.length) {
            this.silverfishBodyParts[i] = new ModelRenderer(this, SILVERFISH_TEXTURE_POSITIONS[i][0], SILVERFISH_TEXTURE_POSITIONS[i][1]);
            this.silverfishBodyParts[i].addBox((float)SILVERFISH_BOX_LENGTH[i][0] * -0.5f, 0.0f, (float)SILVERFISH_BOX_LENGTH[i][2] * -0.5f, SILVERFISH_BOX_LENGTH[i][0], SILVERFISH_BOX_LENGTH[i][1], SILVERFISH_BOX_LENGTH[i][2]);
            this.silverfishBodyParts[i].setRotationPoint(0.0f, 24 - SILVERFISH_BOX_LENGTH[i][1], f);
            this.zPlacement[i] = f;
            if (i < this.silverfishBodyParts.length - 1) {
                f += (float)(SILVERFISH_BOX_LENGTH[i][2] + SILVERFISH_BOX_LENGTH[i + 1][2]) * 0.5f;
            }
            ++i;
        }
        this.silverfishWings = new ModelRenderer[3];
        this.silverfishWings[0] = new ModelRenderer(this, 20, 0);
        this.silverfishWings[0].addBox(-5.0f, 0.0f, (float)SILVERFISH_BOX_LENGTH[2][2] * -0.5f, 10, 8, SILVERFISH_BOX_LENGTH[2][2]);
        this.silverfishWings[0].setRotationPoint(0.0f, 16.0f, this.zPlacement[2]);
        this.silverfishWings[1] = new ModelRenderer(this, 20, 11);
        this.silverfishWings[1].addBox(-3.0f, 0.0f, (float)SILVERFISH_BOX_LENGTH[4][2] * -0.5f, 6, 4, SILVERFISH_BOX_LENGTH[4][2]);
        this.silverfishWings[1].setRotationPoint(0.0f, 20.0f, this.zPlacement[4]);
        this.silverfishWings[2] = new ModelRenderer(this, 20, 18);
        this.silverfishWings[2].addBox(-3.0f, 0.0f, (float)SILVERFISH_BOX_LENGTH[4][2] * -0.5f, 6, 5, SILVERFISH_BOX_LENGTH[1][2]);
        this.silverfishWings[2].setRotationPoint(0.0f, 19.0f, this.zPlacement[1]);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        ModelRenderer[] modelRendererArray = this.silverfishBodyParts;
        int n = this.silverfishBodyParts.length;
        int n2 = 0;
        while (n2 < n) {
            ModelRenderer modelrenderer = modelRendererArray[n2];
            modelrenderer.render(scale);
            ++n2;
        }
        modelRendererArray = this.silverfishWings;
        n = this.silverfishWings.length;
        n2 = 0;
        while (n2 < n) {
            ModelRenderer modelrenderer1 = modelRendererArray[n2];
            modelrenderer1.render(scale);
            ++n2;
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        int i = 0;
        while (i < this.silverfishBodyParts.length) {
            this.silverfishBodyParts[i].rotateAngleY = MathHelper.cos(ageInTicks * 0.9f + (float)i * 0.15f * (float)Math.PI) * (float)Math.PI * 0.05f * (float)(1 + Math.abs(i - 2));
            this.silverfishBodyParts[i].rotationPointX = MathHelper.sin(ageInTicks * 0.9f + (float)i * 0.15f * (float)Math.PI) * (float)Math.PI * 0.2f * (float)Math.abs(i - 2);
            ++i;
        }
        this.silverfishWings[0].rotateAngleY = this.silverfishBodyParts[2].rotateAngleY;
        this.silverfishWings[1].rotateAngleY = this.silverfishBodyParts[4].rotateAngleY;
        this.silverfishWings[1].rotationPointX = this.silverfishBodyParts[4].rotationPointX;
        this.silverfishWings[2].rotateAngleY = this.silverfishBodyParts[1].rotateAngleY;
        this.silverfishWings[2].rotationPointX = this.silverfishBodyParts[1].rotationPointX;
    }
}


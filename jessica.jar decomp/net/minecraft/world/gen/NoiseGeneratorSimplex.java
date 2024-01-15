/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen;

import java.util.Random;

public class NoiseGeneratorSimplex {
    private static final int[][] grad3;
    public static final double SQRT_3;
    private final int[] p = new int[512];
    public double xo;
    public double yo;
    public double zo;
    private static final double F2;
    private static final double G2;

    static {
        int[][] nArrayArray = new int[12][];
        int[] nArray = new int[3];
        nArray[0] = 1;
        nArray[1] = 1;
        nArrayArray[0] = nArray;
        int[] nArray2 = new int[3];
        nArray2[0] = -1;
        nArray2[1] = 1;
        nArrayArray[1] = nArray2;
        int[] nArray3 = new int[3];
        nArray3[0] = 1;
        nArray3[1] = -1;
        nArrayArray[2] = nArray3;
        int[] nArray4 = new int[3];
        nArray4[0] = -1;
        nArray4[1] = -1;
        nArrayArray[3] = nArray4;
        int[] nArray5 = new int[3];
        nArray5[0] = 1;
        nArray5[2] = 1;
        nArrayArray[4] = nArray5;
        int[] nArray6 = new int[3];
        nArray6[0] = -1;
        nArray6[2] = 1;
        nArrayArray[5] = nArray6;
        int[] nArray7 = new int[3];
        nArray7[0] = 1;
        nArray7[2] = -1;
        nArrayArray[6] = nArray7;
        int[] nArray8 = new int[3];
        nArray8[0] = -1;
        nArray8[2] = -1;
        nArrayArray[7] = nArray8;
        int[] nArray9 = new int[3];
        nArray9[1] = 1;
        nArray9[2] = 1;
        nArrayArray[8] = nArray9;
        int[] nArray10 = new int[3];
        nArray10[1] = -1;
        nArray10[2] = 1;
        nArrayArray[9] = nArray10;
        int[] nArray11 = new int[3];
        nArray11[1] = 1;
        nArray11[2] = -1;
        nArrayArray[10] = nArray11;
        int[] nArray12 = new int[3];
        nArray12[1] = -1;
        nArray12[2] = -1;
        nArrayArray[11] = nArray12;
        grad3 = nArrayArray;
        SQRT_3 = Math.sqrt(3.0);
        F2 = 0.5 * (SQRT_3 - 1.0);
        G2 = (3.0 - SQRT_3) / 6.0;
    }

    public NoiseGeneratorSimplex() {
        this(new Random());
    }

    public NoiseGeneratorSimplex(Random p_i45471_1_) {
        this.xo = p_i45471_1_.nextDouble() * 256.0;
        this.yo = p_i45471_1_.nextDouble() * 256.0;
        this.zo = p_i45471_1_.nextDouble() * 256.0;
        int i = 0;
        while (i < 256) {
            this.p[i] = i++;
        }
        int l = 0;
        while (l < 256) {
            int j = p_i45471_1_.nextInt(256 - l) + l;
            int k = this.p[l];
            this.p[l] = this.p[j];
            this.p[j] = k;
            this.p[l + 256] = this.p[l];
            ++l;
        }
    }

    private static int fastFloor(double value) {
        return value > 0.0 ? (int)value : (int)value - 1;
    }

    private static double dot(int[] p_151604_0_, double p_151604_1_, double p_151604_3_) {
        return (double)p_151604_0_[0] * p_151604_1_ + (double)p_151604_0_[1] * p_151604_3_;
    }

    public double getValue(double p_151605_1_, double p_151605_3_) {
        double d2;
        double d1;
        double d0;
        int l;
        int k;
        double d8;
        double d10;
        double d5;
        int j;
        double d6;
        double d3 = 0.5 * (SQRT_3 - 1.0);
        double d4 = (p_151605_1_ + p_151605_3_) * d3;
        int i = NoiseGeneratorSimplex.fastFloor(p_151605_1_ + d4);
        double d7 = (double)i - (d6 = (double)(i + (j = NoiseGeneratorSimplex.fastFloor(p_151605_3_ + d4))) * (d5 = (3.0 - SQRT_3) / 6.0));
        double d9 = p_151605_1_ - d7;
        if (d9 > (d10 = p_151605_3_ - (d8 = (double)j - d6))) {
            k = 1;
            l = 0;
        } else {
            k = 0;
            l = 1;
        }
        double d11 = d9 - (double)k + d5;
        double d12 = d10 - (double)l + d5;
        double d13 = d9 - 1.0 + 2.0 * d5;
        double d14 = d10 - 1.0 + 2.0 * d5;
        int i1 = i & 0xFF;
        int j1 = j & 0xFF;
        int k1 = this.p[i1 + this.p[j1]] % 12;
        int l1 = this.p[i1 + k + this.p[j1 + l]] % 12;
        int i2 = this.p[i1 + 1 + this.p[j1 + 1]] % 12;
        double d15 = 0.5 - d9 * d9 - d10 * d10;
        if (d15 < 0.0) {
            d0 = 0.0;
        } else {
            d15 *= d15;
            d0 = d15 * d15 * NoiseGeneratorSimplex.dot(grad3[k1], d9, d10);
        }
        double d16 = 0.5 - d11 * d11 - d12 * d12;
        if (d16 < 0.0) {
            d1 = 0.0;
        } else {
            d16 *= d16;
            d1 = d16 * d16 * NoiseGeneratorSimplex.dot(grad3[l1], d11, d12);
        }
        double d17 = 0.5 - d13 * d13 - d14 * d14;
        if (d17 < 0.0) {
            d2 = 0.0;
        } else {
            d17 *= d17;
            d2 = d17 * d17 * NoiseGeneratorSimplex.dot(grad3[i2], d13, d14);
        }
        return 70.0 * (d0 + d1 + d2);
    }

    public void add(double[] p_151606_1_, double p_151606_2_, double p_151606_4_, int p_151606_6_, int p_151606_7_, double p_151606_8_, double p_151606_10_, double p_151606_12_) {
        int i = 0;
        int j = 0;
        while (j < p_151606_7_) {
            double d0 = (p_151606_4_ + (double)j) * p_151606_10_ + this.yo;
            int k = 0;
            while (k < p_151606_6_) {
                int i3;
                double d4;
                double d3;
                double d2;
                int k1;
                int j1;
                double d8;
                double d10;
                int i1;
                double d6;
                double d1 = (p_151606_2_ + (double)k) * p_151606_8_ + this.xo;
                double d5 = (d1 + d0) * F2;
                int l = NoiseGeneratorSimplex.fastFloor(d1 + d5);
                double d7 = (double)l - (d6 = (double)(l + (i1 = NoiseGeneratorSimplex.fastFloor(d0 + d5))) * G2);
                double d9 = d1 - d7;
                if (d9 > (d10 = d0 - (d8 = (double)i1 - d6))) {
                    j1 = 1;
                    k1 = 0;
                } else {
                    j1 = 0;
                    k1 = 1;
                }
                double d11 = d9 - (double)j1 + G2;
                double d12 = d10 - (double)k1 + G2;
                double d13 = d9 - 1.0 + 2.0 * G2;
                double d14 = d10 - 1.0 + 2.0 * G2;
                int l1 = l & 0xFF;
                int i2 = i1 & 0xFF;
                int j2 = this.p[l1 + this.p[i2]] % 12;
                int k2 = this.p[l1 + j1 + this.p[i2 + k1]] % 12;
                int l2 = this.p[l1 + 1 + this.p[i2 + 1]] % 12;
                double d15 = 0.5 - d9 * d9 - d10 * d10;
                if (d15 < 0.0) {
                    d2 = 0.0;
                } else {
                    d15 *= d15;
                    d2 = d15 * d15 * NoiseGeneratorSimplex.dot(grad3[j2], d9, d10);
                }
                double d16 = 0.5 - d11 * d11 - d12 * d12;
                if (d16 < 0.0) {
                    d3 = 0.0;
                } else {
                    d16 *= d16;
                    d3 = d16 * d16 * NoiseGeneratorSimplex.dot(grad3[k2], d11, d12);
                }
                double d17 = 0.5 - d13 * d13 - d14 * d14;
                if (d17 < 0.0) {
                    d4 = 0.0;
                } else {
                    d17 *= d17;
                    d4 = d17 * d17 * NoiseGeneratorSimplex.dot(grad3[l2], d13, d14);
                }
                int n = i3 = i++;
                p_151606_1_[n] = p_151606_1_[n] + 70.0 * (d2 + d3 + d4) * p_151606_12_;
                ++k;
            }
            ++j;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen;

import java.util.Random;
import net.minecraft.world.gen.NoiseGenerator;
import net.minecraft.world.gen.NoiseGeneratorSimplex;

public class NoiseGeneratorPerlin
extends NoiseGenerator {
    private final NoiseGeneratorSimplex[] noiseLevels;
    private final int levels;

    public NoiseGeneratorPerlin(Random p_i45470_1_, int p_i45470_2_) {
        this.levels = p_i45470_2_;
        this.noiseLevels = new NoiseGeneratorSimplex[p_i45470_2_];
        int i = 0;
        while (i < p_i45470_2_) {
            this.noiseLevels[i] = new NoiseGeneratorSimplex(p_i45470_1_);
            ++i;
        }
    }

    public double getValue(double p_151601_1_, double p_151601_3_) {
        double d0 = 0.0;
        double d1 = 1.0;
        int i = 0;
        while (i < this.levels) {
            d0 += this.noiseLevels[i].getValue(p_151601_1_ * d1, p_151601_3_ * d1) / d1;
            d1 /= 2.0;
            ++i;
        }
        return d0;
    }

    public double[] getRegion(double[] p_151599_1_, double p_151599_2_, double p_151599_4_, int p_151599_6_, int p_151599_7_, double p_151599_8_, double p_151599_10_, double p_151599_12_) {
        return this.getRegion(p_151599_1_, p_151599_2_, p_151599_4_, p_151599_6_, p_151599_7_, p_151599_8_, p_151599_10_, p_151599_12_, 0.5);
    }

    public double[] getRegion(double[] p_151600_1_, double p_151600_2_, double p_151600_4_, int p_151600_6_, int p_151600_7_, double p_151600_8_, double p_151600_10_, double p_151600_12_, double p_151600_14_) {
        if (p_151600_1_ != null && p_151600_1_.length >= p_151600_6_ * p_151600_7_) {
            int i = 0;
            while (i < p_151600_1_.length) {
                p_151600_1_[i] = 0.0;
                ++i;
            }
        } else {
            p_151600_1_ = new double[p_151600_6_ * p_151600_7_];
        }
        double d1 = 1.0;
        double d0 = 1.0;
        int j = 0;
        while (j < this.levels) {
            this.noiseLevels[j].add(p_151600_1_, p_151600_2_, p_151600_4_, p_151600_6_, p_151600_7_, p_151600_8_ * d0 * d1, p_151600_10_ * d0 * d1, 0.55 / d1);
            d0 *= p_151600_12_;
            d1 *= p_151600_14_;
            ++j;
        }
        return p_151600_1_;
    }
}


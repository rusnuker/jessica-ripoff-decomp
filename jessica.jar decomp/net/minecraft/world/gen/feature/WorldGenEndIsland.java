/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenEndIsland
extends WorldGenerator {
    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        float f = rand.nextInt(3) + 4;
        int i = 0;
        while (f > 0.5f) {
            int j = MathHelper.floor(-f);
            while (j <= MathHelper.ceil(f)) {
                int k = MathHelper.floor(-f);
                while (k <= MathHelper.ceil(f)) {
                    if ((float)(j * j + k * k) <= (f + 1.0f) * (f + 1.0f)) {
                        this.setBlockAndNotifyAdequately(worldIn, position.add(j, i, k), Blocks.END_STONE.getDefaultState());
                    }
                    ++k;
                }
                ++j;
            }
            f = (float)((double)f - ((double)rand.nextInt(2) + 0.5));
            --i;
        }
        return true;
    }
}


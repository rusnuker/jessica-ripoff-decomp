/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenDoublePlant
extends WorldGenerator {
    private BlockDoublePlant.EnumPlantType plantType;

    public void setPlantType(BlockDoublePlant.EnumPlantType plantTypeIn) {
        this.plantType = plantTypeIn;
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        boolean flag = false;
        int i = 0;
        while (i < 64) {
            BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
            if (worldIn.isAirBlock(blockpos) && (!worldIn.provider.getHasNoSky() || blockpos.getY() < 254) && Blocks.DOUBLE_PLANT.canPlaceBlockAt(worldIn, blockpos)) {
                Blocks.DOUBLE_PLANT.placeAt(worldIn, blockpos, this.plantType, 2);
                flag = true;
            }
            ++i;
        }
        return flag;
    }
}


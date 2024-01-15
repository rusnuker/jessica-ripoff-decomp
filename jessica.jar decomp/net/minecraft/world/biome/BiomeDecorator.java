/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenBush;
import net.minecraft.world.gen.feature.WorldGenCactus;
import net.minecraft.world.gen.feature.WorldGenClay;
import net.minecraft.world.gen.feature.WorldGenDeadBush;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenLiquids;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenPumpkin;
import net.minecraft.world.gen.feature.WorldGenReed;
import net.minecraft.world.gen.feature.WorldGenSand;
import net.minecraft.world.gen.feature.WorldGenWaterlily;
import net.minecraft.world.gen.feature.WorldGenerator;

public class BiomeDecorator {
    protected boolean decorating;
    protected BlockPos chunkPos;
    protected ChunkGeneratorSettings chunkProviderSettings;
    protected WorldGenerator clayGen = new WorldGenClay(4);
    protected WorldGenerator sandGen = new WorldGenSand(Blocks.SAND, 7);
    protected WorldGenerator gravelAsSandGen = new WorldGenSand(Blocks.GRAVEL, 6);
    protected WorldGenerator dirtGen;
    protected WorldGenerator gravelGen;
    protected WorldGenerator graniteGen;
    protected WorldGenerator dioriteGen;
    protected WorldGenerator andesiteGen;
    protected WorldGenerator coalGen;
    protected WorldGenerator ironGen;
    protected WorldGenerator goldGen;
    protected WorldGenerator redstoneGen;
    protected WorldGenerator diamondGen;
    protected WorldGenerator lapisGen;
    protected WorldGenFlowers yellowFlowerGen = new WorldGenFlowers(Blocks.YELLOW_FLOWER, BlockFlower.EnumFlowerType.DANDELION);
    protected WorldGenerator mushroomBrownGen = new WorldGenBush(Blocks.BROWN_MUSHROOM);
    protected WorldGenerator mushroomRedGen = new WorldGenBush(Blocks.RED_MUSHROOM);
    protected WorldGenerator bigMushroomGen = new WorldGenBigMushroom();
    protected WorldGenerator reedGen = new WorldGenReed();
    protected WorldGenerator cactusGen = new WorldGenCactus();
    protected WorldGenerator waterlilyGen = new WorldGenWaterlily();
    protected int waterlilyPerChunk;
    protected int treesPerChunk;
    protected float extraTreeChance = 0.1f;
    protected int flowersPerChunk = 2;
    protected int grassPerChunk = 1;
    protected int deadBushPerChunk;
    protected int mushroomsPerChunk;
    protected int reedsPerChunk;
    protected int cactiPerChunk;
    protected int sandPerChunk = 1;
    protected int sandPerChunk2 = 3;
    protected int clayPerChunk = 1;
    protected int bigMushroomsPerChunk;
    public boolean generateLakes = true;

    public void decorate(World worldIn, Random random, Biome biome, BlockPos pos) {
        if (this.decorating) {
            throw new RuntimeException("Already decorating");
        }
        this.chunkProviderSettings = ChunkGeneratorSettings.Factory.jsonToFactory(worldIn.getWorldInfo().getGeneratorOptions()).build();
        this.chunkPos = pos;
        this.dirtGen = new WorldGenMinable(Blocks.DIRT.getDefaultState(), this.chunkProviderSettings.dirtSize);
        this.gravelGen = new WorldGenMinable(Blocks.GRAVEL.getDefaultState(), this.chunkProviderSettings.gravelSize);
        this.graniteGen = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE), this.chunkProviderSettings.graniteSize);
        this.dioriteGen = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE), this.chunkProviderSettings.dioriteSize);
        this.andesiteGen = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), this.chunkProviderSettings.andesiteSize);
        this.coalGen = new WorldGenMinable(Blocks.COAL_ORE.getDefaultState(), this.chunkProviderSettings.coalSize);
        this.ironGen = new WorldGenMinable(Blocks.IRON_ORE.getDefaultState(), this.chunkProviderSettings.ironSize);
        this.goldGen = new WorldGenMinable(Blocks.GOLD_ORE.getDefaultState(), this.chunkProviderSettings.goldSize);
        this.redstoneGen = new WorldGenMinable(Blocks.REDSTONE_ORE.getDefaultState(), this.chunkProviderSettings.redstoneSize);
        this.diamondGen = new WorldGenMinable(Blocks.DIAMOND_ORE.getDefaultState(), this.chunkProviderSettings.diamondSize);
        this.lapisGen = new WorldGenMinable(Blocks.LAPIS_ORE.getDefaultState(), this.chunkProviderSettings.lapisSize);
        this.genDecorations(biome, worldIn, random);
        this.decorating = false;
    }

    protected void genDecorations(Biome biomeIn, World worldIn, Random random) {
        int k9;
        int i5;
        int j13;
        int l8;
        int j4;
        int k12;
        int k8;
        int i4;
        int j12;
        this.generateOres(worldIn, random);
        int i = 0;
        while (i < this.sandPerChunk2) {
            int j = random.nextInt(16) + 8;
            int k = random.nextInt(16) + 8;
            this.sandGen.generate(worldIn, random, worldIn.getTopSolidOrLiquidBlock(this.chunkPos.add(j, 0, k)));
            ++i;
        }
        int i1 = 0;
        while (i1 < this.clayPerChunk) {
            int l1 = random.nextInt(16) + 8;
            int i6 = random.nextInt(16) + 8;
            this.clayGen.generate(worldIn, random, worldIn.getTopSolidOrLiquidBlock(this.chunkPos.add(l1, 0, i6)));
            ++i1;
        }
        int j1 = 0;
        while (j1 < this.sandPerChunk) {
            int i2 = random.nextInt(16) + 8;
            int j6 = random.nextInt(16) + 8;
            this.gravelAsSandGen.generate(worldIn, random, worldIn.getTopSolidOrLiquidBlock(this.chunkPos.add(i2, 0, j6)));
            ++j1;
        }
        int k1 = this.treesPerChunk;
        if (random.nextFloat() < this.extraTreeChance) {
            ++k1;
        }
        int j2 = 0;
        while (j2 < k1) {
            int k6 = random.nextInt(16) + 8;
            int l = random.nextInt(16) + 8;
            WorldGenAbstractTree worldgenabstracttree = biomeIn.genBigTreeChance(random);
            worldgenabstracttree.setDecorationDefaults();
            BlockPos blockpos = worldIn.getHeight(this.chunkPos.add(k6, 0, l));
            if (worldgenabstracttree.generate(worldIn, random, blockpos)) {
                worldgenabstracttree.generateSaplings(worldIn, random, blockpos);
            }
            ++j2;
        }
        int k2 = 0;
        while (k2 < this.bigMushroomsPerChunk) {
            int l6 = random.nextInt(16) + 8;
            int k10 = random.nextInt(16) + 8;
            this.bigMushroomGen.generate(worldIn, random, worldIn.getHeight(this.chunkPos.add(l6, 0, k10)));
            ++k2;
        }
        int l2 = 0;
        while (l2 < this.flowersPerChunk) {
            int k17;
            BlockPos blockpos1;
            BlockFlower.EnumFlowerType blockflower$enumflowertype;
            BlockFlower blockflower;
            int l10;
            int i7 = random.nextInt(16) + 8;
            int j14 = worldIn.getHeight(this.chunkPos.add(i7, 0, l10 = random.nextInt(16) + 8)).getY() + 32;
            if (j14 > 0 && (blockflower = (blockflower$enumflowertype = biomeIn.pickRandomFlower(random, blockpos1 = this.chunkPos.add(i7, k17 = random.nextInt(j14), l10))).getBlockType().getBlock()).getDefaultState().getMaterial() != Material.AIR) {
                this.yellowFlowerGen.setGeneratedBlock(blockflower, blockflower$enumflowertype);
                this.yellowFlowerGen.generate(worldIn, random, blockpos1);
            }
            ++l2;
        }
        int i3 = 0;
        while (i3 < this.grassPerChunk) {
            int i11;
            int j7 = random.nextInt(16) + 8;
            int k14 = worldIn.getHeight(this.chunkPos.add(j7, 0, i11 = random.nextInt(16) + 8)).getY() * 2;
            if (k14 > 0) {
                int l17 = random.nextInt(k14);
                biomeIn.getRandomWorldGenForGrass(random).generate(worldIn, random, this.chunkPos.add(j7, l17, i11));
            }
            ++i3;
        }
        int j3 = 0;
        while (j3 < this.deadBushPerChunk) {
            int j11;
            int k7 = random.nextInt(16) + 8;
            int l14 = worldIn.getHeight(this.chunkPos.add(k7, 0, j11 = random.nextInt(16) + 8)).getY() * 2;
            if (l14 > 0) {
                int i18 = random.nextInt(l14);
                new WorldGenDeadBush().generate(worldIn, random, this.chunkPos.add(k7, i18, j11));
            }
            ++j3;
        }
        int k3 = 0;
        while (k3 < this.waterlilyPerChunk) {
            int k11;
            int l7 = random.nextInt(16) + 8;
            int i15 = worldIn.getHeight(this.chunkPos.add(l7, 0, k11 = random.nextInt(16) + 8)).getY() * 2;
            if (i15 > 0) {
                int j18 = random.nextInt(i15);
                BlockPos blockpos4 = this.chunkPos.add(l7, j18, k11);
                while (blockpos4.getY() > 0) {
                    BlockPos blockpos7 = blockpos4.down();
                    if (!worldIn.isAirBlock(blockpos7)) break;
                    blockpos4 = blockpos7;
                }
                this.waterlilyGen.generate(worldIn, random, blockpos4);
            }
            ++k3;
        }
        int l3 = 0;
        while (l3 < this.mushroomsPerChunk) {
            int i12;
            int j8;
            int j15;
            if (random.nextInt(4) == 0) {
                int i8 = random.nextInt(16) + 8;
                int l11 = random.nextInt(16) + 8;
                BlockPos blockpos2 = worldIn.getHeight(this.chunkPos.add(i8, 0, l11));
                this.mushroomBrownGen.generate(worldIn, random, blockpos2);
            }
            if (random.nextInt(8) == 0 && (j15 = worldIn.getHeight(this.chunkPos.add(j8 = random.nextInt(16) + 8, 0, i12 = random.nextInt(16) + 8)).getY() * 2) > 0) {
                int k18 = random.nextInt(j15);
                BlockPos blockpos5 = this.chunkPos.add(j8, k18, i12);
                this.mushroomRedGen.generate(worldIn, random, blockpos5);
            }
            ++l3;
        }
        if (random.nextInt(4) == 0 && (j12 = worldIn.getHeight(this.chunkPos.add(i4 = random.nextInt(16) + 8, 0, k8 = random.nextInt(16) + 8)).getY() * 2) > 0) {
            int k15 = random.nextInt(j12);
            this.mushroomBrownGen.generate(worldIn, random, this.chunkPos.add(i4, k15, k8));
        }
        if (random.nextInt(8) == 0 && (k12 = worldIn.getHeight(this.chunkPos.add(j4 = random.nextInt(16) + 8, 0, l8 = random.nextInt(16) + 8)).getY() * 2) > 0) {
            int l15 = random.nextInt(k12);
            this.mushroomRedGen.generate(worldIn, random, this.chunkPos.add(j4, l15, l8));
        }
        int k4 = 0;
        while (k4 < this.reedsPerChunk) {
            int l12;
            int i9 = random.nextInt(16) + 8;
            int i16 = worldIn.getHeight(this.chunkPos.add(i9, 0, l12 = random.nextInt(16) + 8)).getY() * 2;
            if (i16 > 0) {
                int l18 = random.nextInt(i16);
                this.reedGen.generate(worldIn, random, this.chunkPos.add(i9, l18, l12));
            }
            ++k4;
        }
        int l4 = 0;
        while (l4 < 10) {
            int i13;
            int j9 = random.nextInt(16) + 8;
            int j16 = worldIn.getHeight(this.chunkPos.add(j9, 0, i13 = random.nextInt(16) + 8)).getY() * 2;
            if (j16 > 0) {
                int i19 = random.nextInt(j16);
                this.reedGen.generate(worldIn, random, this.chunkPos.add(j9, i19, i13));
            }
            ++l4;
        }
        if (random.nextInt(32) == 0 && (j13 = worldIn.getHeight(this.chunkPos.add(i5 = random.nextInt(16) + 8, 0, k9 = random.nextInt(16) + 8)).getY() * 2) > 0) {
            int k16 = random.nextInt(j13);
            new WorldGenPumpkin().generate(worldIn, random, this.chunkPos.add(i5, k16, k9));
        }
        int j5 = 0;
        while (j5 < this.cactiPerChunk) {
            int k13;
            int l9 = random.nextInt(16) + 8;
            int l16 = worldIn.getHeight(this.chunkPos.add(l9, 0, k13 = random.nextInt(16) + 8)).getY() * 2;
            if (l16 > 0) {
                int j19 = random.nextInt(l16);
                this.cactusGen.generate(worldIn, random, this.chunkPos.add(l9, j19, k13));
            }
            ++j5;
        }
        if (this.generateLakes) {
            int k5 = 0;
            while (k5 < 50) {
                int i10 = random.nextInt(16) + 8;
                int l13 = random.nextInt(16) + 8;
                int i17 = random.nextInt(248) + 8;
                if (i17 > 0) {
                    int k19 = random.nextInt(i17);
                    BlockPos blockpos6 = this.chunkPos.add(i10, k19, l13);
                    new WorldGenLiquids(Blocks.FLOWING_WATER).generate(worldIn, random, blockpos6);
                }
                ++k5;
            }
            int l5 = 0;
            while (l5 < 20) {
                int j10 = random.nextInt(16) + 8;
                int i14 = random.nextInt(16) + 8;
                int j17 = random.nextInt(random.nextInt(random.nextInt(240) + 8) + 8);
                BlockPos blockpos3 = this.chunkPos.add(j10, j17, i14);
                new WorldGenLiquids(Blocks.FLOWING_LAVA).generate(worldIn, random, blockpos3);
                ++l5;
            }
        }
    }

    protected void generateOres(World worldIn, Random random) {
        this.genStandardOre1(worldIn, random, this.chunkProviderSettings.dirtCount, this.dirtGen, this.chunkProviderSettings.dirtMinHeight, this.chunkProviderSettings.dirtMaxHeight);
        this.genStandardOre1(worldIn, random, this.chunkProviderSettings.gravelCount, this.gravelGen, this.chunkProviderSettings.gravelMinHeight, this.chunkProviderSettings.gravelMaxHeight);
        this.genStandardOre1(worldIn, random, this.chunkProviderSettings.dioriteCount, this.dioriteGen, this.chunkProviderSettings.dioriteMinHeight, this.chunkProviderSettings.dioriteMaxHeight);
        this.genStandardOre1(worldIn, random, this.chunkProviderSettings.graniteCount, this.graniteGen, this.chunkProviderSettings.graniteMinHeight, this.chunkProviderSettings.graniteMaxHeight);
        this.genStandardOre1(worldIn, random, this.chunkProviderSettings.andesiteCount, this.andesiteGen, this.chunkProviderSettings.andesiteMinHeight, this.chunkProviderSettings.andesiteMaxHeight);
        this.genStandardOre1(worldIn, random, this.chunkProviderSettings.coalCount, this.coalGen, this.chunkProviderSettings.coalMinHeight, this.chunkProviderSettings.coalMaxHeight);
        this.genStandardOre1(worldIn, random, this.chunkProviderSettings.ironCount, this.ironGen, this.chunkProviderSettings.ironMinHeight, this.chunkProviderSettings.ironMaxHeight);
        this.genStandardOre1(worldIn, random, this.chunkProviderSettings.goldCount, this.goldGen, this.chunkProviderSettings.goldMinHeight, this.chunkProviderSettings.goldMaxHeight);
        this.genStandardOre1(worldIn, random, this.chunkProviderSettings.redstoneCount, this.redstoneGen, this.chunkProviderSettings.redstoneMinHeight, this.chunkProviderSettings.redstoneMaxHeight);
        this.genStandardOre1(worldIn, random, this.chunkProviderSettings.diamondCount, this.diamondGen, this.chunkProviderSettings.diamondMinHeight, this.chunkProviderSettings.diamondMaxHeight);
        this.genStandardOre2(worldIn, random, this.chunkProviderSettings.lapisCount, this.lapisGen, this.chunkProviderSettings.lapisCenterHeight, this.chunkProviderSettings.lapisSpread);
    }

    protected void genStandardOre1(World worldIn, Random random, int blockCount, WorldGenerator generator, int minHeight, int maxHeight) {
        if (maxHeight < minHeight) {
            int i = minHeight;
            minHeight = maxHeight;
            maxHeight = i;
        } else if (maxHeight == minHeight) {
            if (minHeight < 255) {
                ++maxHeight;
            } else {
                --minHeight;
            }
        }
        int j = 0;
        while (j < blockCount) {
            BlockPos blockpos = this.chunkPos.add(random.nextInt(16), random.nextInt(maxHeight - minHeight) + minHeight, random.nextInt(16));
            generator.generate(worldIn, random, blockpos);
            ++j;
        }
    }

    protected void genStandardOre2(World worldIn, Random random, int blockCount, WorldGenerator generator, int centerHeight, int spread) {
        int i = 0;
        while (i < blockCount) {
            BlockPos blockpos = this.chunkPos.add(random.nextInt(16), random.nextInt(spread) + random.nextInt(spread) + centerHeight - spread, random.nextInt(16));
            generator.generate(worldIn, random, blockpos);
            ++i;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class ChunkPrimer {
    private static final IBlockState DEFAULT_STATE = Blocks.AIR.getDefaultState();
    private final char[] data = new char[65536];

    public IBlockState getBlockState(int x, int y, int z) {
        IBlockState iblockstate = Block.BLOCK_STATE_IDS.getByValue(this.data[ChunkPrimer.getBlockIndex(x, y, z)]);
        return iblockstate == null ? DEFAULT_STATE : iblockstate;
    }

    public void setBlockState(int x, int y, int z, IBlockState state) {
        this.data[ChunkPrimer.getBlockIndex((int)x, (int)y, (int)z)] = (char)Block.BLOCK_STATE_IDS.get(state);
    }

    private static int getBlockIndex(int x, int y, int z) {
        return x << 12 | z << 8 | y;
    }

    public int findGroundBlockIdx(int x, int z) {
        int i = (x << 12 | z << 8) + 256 - 1;
        int j = 255;
        while (j >= 0) {
            IBlockState iblockstate = Block.BLOCK_STATE_IDS.getByValue(this.data[i + j]);
            if (iblockstate != null && iblockstate != DEFAULT_STATE) {
                return j;
            }
            --j;
        }
        return 0;
    }
}


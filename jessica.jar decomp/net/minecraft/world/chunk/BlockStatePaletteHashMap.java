/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.chunk;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.world.chunk.IBlockStatePalette;
import net.minecraft.world.chunk.IBlockStatePaletteResizer;

public class BlockStatePaletteHashMap
implements IBlockStatePalette {
    private final IntIdentityHashBiMap<IBlockState> statePaletteMap;
    private final IBlockStatePaletteResizer paletteResizer;
    private final int bits;

    public BlockStatePaletteHashMap(int bitsIn, IBlockStatePaletteResizer p_i47089_2_) {
        this.bits = bitsIn;
        this.paletteResizer = p_i47089_2_;
        this.statePaletteMap = new IntIdentityHashBiMap(1 << bitsIn);
    }

    @Override
    public int idFor(IBlockState state) {
        int i = this.statePaletteMap.getId(state);
        if (i == -1 && (i = this.statePaletteMap.add(state)) >= 1 << this.bits) {
            i = this.paletteResizer.onResize(this.bits + 1, state);
        }
        return i;
    }

    @Override
    @Nullable
    public IBlockState getBlockState(int indexKey) {
        return this.statePaletteMap.get(indexKey);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.statePaletteMap.clear();
        int i = buf.readVarIntFromBuffer();
        int j = 0;
        while (j < i) {
            this.statePaletteMap.add(Block.BLOCK_STATE_IDS.getByValue(buf.readVarIntFromBuffer()));
            ++j;
        }
    }

    @Override
    public void write(PacketBuffer buf) {
        int i = this.statePaletteMap.size();
        buf.writeVarIntToBuffer(i);
        int j = 0;
        while (j < i) {
            buf.writeVarIntToBuffer(Block.BLOCK_STATE_IDS.get(this.statePaletteMap.get(j)));
            ++j;
        }
    }

    @Override
    public int getSerializedState() {
        int i = PacketBuffer.getVarIntSize(this.statePaletteMap.size());
        int j = 0;
        while (j < this.statePaletteMap.size()) {
            i += PacketBuffer.getVarIntSize(Block.BLOCK_STATE_IDS.get(this.statePaletteMap.get(j)));
            ++j;
        }
        return i;
    }
}


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
import net.minecraft.world.chunk.IBlockStatePalette;
import net.minecraft.world.chunk.IBlockStatePaletteResizer;

public class BlockStatePaletteLinear
implements IBlockStatePalette {
    private final IBlockState[] states;
    private final IBlockStatePaletteResizer resizeHandler;
    private final int bits;
    private int arraySize;

    public BlockStatePaletteLinear(int p_i47088_1_, IBlockStatePaletteResizer p_i47088_2_) {
        this.states = new IBlockState[1 << p_i47088_1_];
        this.bits = p_i47088_1_;
        this.resizeHandler = p_i47088_2_;
    }

    @Override
    public int idFor(IBlockState state) {
        int j;
        int i = 0;
        while (i < this.arraySize) {
            if (this.states[i] == state) {
                return i;
            }
            ++i;
        }
        if ((j = this.arraySize++) < this.states.length) {
            this.states[j] = state;
            return j;
        }
        return this.resizeHandler.onResize(this.bits + 1, state);
    }

    @Override
    @Nullable
    public IBlockState getBlockState(int indexKey) {
        return indexKey >= 0 && indexKey < this.arraySize ? this.states[indexKey] : null;
    }

    @Override
    public void read(PacketBuffer buf) {
        this.arraySize = buf.readVarIntFromBuffer();
        int i = 0;
        while (i < this.arraySize) {
            this.states[i] = Block.BLOCK_STATE_IDS.getByValue(buf.readVarIntFromBuffer());
            ++i;
        }
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeVarIntToBuffer(this.arraySize);
        int i = 0;
        while (i < this.arraySize) {
            buf.writeVarIntToBuffer(Block.BLOCK_STATE_IDS.get(this.states[i]));
            ++i;
        }
    }

    @Override
    public int getSerializedState() {
        int i = PacketBuffer.getVarIntSize(this.arraySize);
        int j = 0;
        while (j < this.arraySize) {
            i += PacketBuffer.getVarIntSize(Block.BLOCK_STATE_IDS.get(this.states[j]));
            ++j;
        }
        return i;
    }
}


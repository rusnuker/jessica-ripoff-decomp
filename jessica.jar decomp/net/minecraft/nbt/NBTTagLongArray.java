/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTSizeTracker;

public class NBTTagLongArray
extends NBTBase {
    private long[] field_193587_b;

    NBTTagLongArray() {
    }

    public NBTTagLongArray(long[] p_i47524_1_) {
        this.field_193587_b = p_i47524_1_;
    }

    public NBTTagLongArray(List<Long> p_i47525_1_) {
        this(NBTTagLongArray.func_193586_a(p_i47525_1_));
    }

    private static long[] func_193586_a(List<Long> p_193586_0_) {
        long[] along = new long[p_193586_0_.size()];
        int i = 0;
        while (i < p_193586_0_.size()) {
            Long olong = p_193586_0_.get(i);
            along[i] = olong == null ? 0L : olong;
            ++i;
        }
        return along;
    }

    @Override
    void write(DataOutput output) throws IOException {
        output.writeInt(this.field_193587_b.length);
        long[] lArray = this.field_193587_b;
        int n = this.field_193587_b.length;
        int n2 = 0;
        while (n2 < n) {
            long i = lArray[n2];
            output.writeLong(i);
            ++n2;
        }
    }

    @Override
    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        sizeTracker.read(192L);
        int i = input.readInt();
        sizeTracker.read(64 * i);
        this.field_193587_b = new long[i];
        int j = 0;
        while (j < i) {
            this.field_193587_b[j] = input.readLong();
            ++j;
        }
    }

    @Override
    public byte getId() {
        return 12;
    }

    @Override
    public String toString() {
        StringBuilder stringbuilder = new StringBuilder("[L;");
        int i = 0;
        while (i < this.field_193587_b.length) {
            if (i != 0) {
                stringbuilder.append(',');
            }
            stringbuilder.append(this.field_193587_b[i]).append('L');
            ++i;
        }
        return stringbuilder.append(']').toString();
    }

    @Override
    public NBTTagLongArray copy() {
        long[] along = new long[this.field_193587_b.length];
        System.arraycopy(this.field_193587_b, 0, along, 0, this.field_193587_b.length);
        return new NBTTagLongArray(along);
    }

    @Override
    public boolean equals(Object p_equals_1_) {
        return super.equals(p_equals_1_) && Arrays.equals(this.field_193587_b, ((NBTTagLongArray)p_equals_1_).field_193587_b);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Arrays.hashCode(this.field_193587_b);
    }
}


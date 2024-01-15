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

public class NBTTagIntArray
extends NBTBase {
    private int[] intArray;

    NBTTagIntArray() {
    }

    public NBTTagIntArray(int[] p_i45132_1_) {
        this.intArray = p_i45132_1_;
    }

    public NBTTagIntArray(List<Integer> p_i47528_1_) {
        this(NBTTagIntArray.func_193584_a(p_i47528_1_));
    }

    private static int[] func_193584_a(List<Integer> p_193584_0_) {
        int[] aint = new int[p_193584_0_.size()];
        int i = 0;
        while (i < p_193584_0_.size()) {
            Integer integer = p_193584_0_.get(i);
            aint[i] = integer == null ? 0 : integer;
            ++i;
        }
        return aint;
    }

    @Override
    void write(DataOutput output) throws IOException {
        output.writeInt(this.intArray.length);
        int[] nArray = this.intArray;
        int n = this.intArray.length;
        int n2 = 0;
        while (n2 < n) {
            int i = nArray[n2];
            output.writeInt(i);
            ++n2;
        }
    }

    @Override
    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        sizeTracker.read(192L);
        int i = input.readInt();
        sizeTracker.read(32 * i);
        this.intArray = new int[i];
        int j = 0;
        while (j < i) {
            this.intArray[j] = input.readInt();
            ++j;
        }
    }

    @Override
    public byte getId() {
        return 11;
    }

    @Override
    public String toString() {
        StringBuilder stringbuilder = new StringBuilder("[I;");
        int i = 0;
        while (i < this.intArray.length) {
            if (i != 0) {
                stringbuilder.append(',');
            }
            stringbuilder.append(this.intArray[i]);
            ++i;
        }
        return stringbuilder.append(']').toString();
    }

    @Override
    public NBTTagIntArray copy() {
        int[] aint = new int[this.intArray.length];
        System.arraycopy(this.intArray, 0, aint, 0, this.intArray.length);
        return new NBTTagIntArray(aint);
    }

    @Override
    public boolean equals(Object p_equals_1_) {
        return super.equals(p_equals_1_) && Arrays.equals(this.intArray, ((NBTTagIntArray)p_equals_1_).intArray);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Arrays.hashCode(this.intArray);
    }

    public int[] getIntArray() {
        return this.intArray;
    }
}


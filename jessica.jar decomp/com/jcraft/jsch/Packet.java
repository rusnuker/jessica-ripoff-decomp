/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.Random;

public class Packet {
    private static Random random = null;
    Buffer buffer;
    byte[] ba4 = new byte[4];

    static void setRandom(Random foo) {
        random = foo;
    }

    public Packet(Buffer buffer) {
        this.buffer = buffer;
    }

    public void reset() {
        this.buffer.index = 5;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void padding(int bsize) {
        int len = this.buffer.index;
        int pad = -len & bsize - 1;
        if (pad < bsize) {
            pad += bsize;
        }
        len = len + pad - 4;
        this.ba4[0] = (byte)(len >>> 24);
        this.ba4[1] = (byte)(len >>> 16);
        this.ba4[2] = (byte)(len >>> 8);
        this.ba4[3] = (byte)len;
        System.arraycopy(this.ba4, 0, this.buffer.buffer, 0, 4);
        this.buffer.buffer[4] = (byte)pad;
        Random random = Packet.random;
        synchronized (random) {
            Packet.random.fill(this.buffer.buffer, this.buffer.index, pad);
        }
        this.buffer.skip(pad);
    }

    int shift(int len, int bsize, int mac) {
        int s = len + 5 + 9;
        int pad = -s & bsize - 1;
        if (pad < bsize) {
            pad += bsize;
        }
        s += pad;
        s += mac;
        if (this.buffer.buffer.length < (s += 32) + this.buffer.index - 5 - 9 - len) {
            byte[] foo = new byte[s + this.buffer.index - 5 - 9 - len];
            System.arraycopy(this.buffer.buffer, 0, foo, 0, this.buffer.buffer.length);
            this.buffer.buffer = foo;
        }
        System.arraycopy(this.buffer.buffer, len + 5 + 9, this.buffer.buffer, s, this.buffer.index - 5 - 9 - len);
        this.buffer.index = 10;
        this.buffer.putInt(len);
        this.buffer.index = len + 5 + 9;
        return s;
    }

    void unshift(byte command, int recipient, int s, int len) {
        System.arraycopy(this.buffer.buffer, s, this.buffer.buffer, 14, len);
        this.buffer.buffer[5] = command;
        this.buffer.index = 6;
        this.buffer.putInt(recipient);
        this.buffer.putInt(len);
        this.buffer.index = len + 5 + 9;
    }

    Buffer getBuffer() {
        return this.buffer;
    }
}


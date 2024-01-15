/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix;

public class Timer2 {
    private long previousTime = -1L;

    public boolean check(float milliseconds) {
        return (float)(this.getCurrentTime() - this.previousTime) >= milliseconds;
    }

    public void reset() {
        this.previousTime = this.getCurrentTime();
    }

    public short convert(float perSecond) {
        return (short)(1000.0f / perSecond);
    }

    public long get() {
        return this.previousTime;
    }

    protected long getCurrentTime() {
        return System.currentTimeMillis();
    }
}


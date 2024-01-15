/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.io;

import java.util.EventObject;

public class CopyStreamEvent
extends EventObject {
    public static final long UNKNOWN_STREAM_SIZE = -1L;
    private int bytesTransferred;
    private long totalBytesTransferred;
    private long streamSize;

    public CopyStreamEvent(Object source, long totalBytesTransferred, int bytesTransferred, long streamSize) {
        super(source);
        this.bytesTransferred = bytesTransferred;
        this.totalBytesTransferred = totalBytesTransferred;
        this.streamSize = streamSize;
    }

    public int getBytesTransferred() {
        return this.bytesTransferred;
    }

    public long getTotalBytesTransferred() {
        return this.totalBytesTransferred;
    }

    public long getStreamSize() {
        return this.streamSize;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.WriterWatcher;
import java.io.CharArrayWriter;

class WatchableWriter
extends CharArrayWriter {
    private WriterWatcher watcher;

    WatchableWriter() {
    }

    public void close() {
        super.close();
        if (this.watcher != null) {
            this.watcher.writerClosed(this);
        }
    }

    public void setWatcher(WriterWatcher watcher) {
        this.watcher = watcher;
    }
}


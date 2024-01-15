/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.OutputStreamWatcher;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

class WatchableOutputStream
extends ByteArrayOutputStream {
    private OutputStreamWatcher watcher;

    WatchableOutputStream() {
    }

    public void close() throws IOException {
        super.close();
        if (this.watcher != null) {
            this.watcher.streamClosed(this);
        }
    }

    public void setWatcher(OutputStreamWatcher watcher) {
        this.watcher = watcher;
    }
}


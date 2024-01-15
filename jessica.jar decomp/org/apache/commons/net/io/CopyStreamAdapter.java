/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.io;

import java.util.Enumeration;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.commons.net.util.ListenerList;

public class CopyStreamAdapter
implements CopyStreamListener {
    private ListenerList internalListeners = new ListenerList();

    public void bytesTransferred(CopyStreamEvent event) {
        this.bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
    }

    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
        Enumeration listeners = this.internalListeners.getListeners();
        CopyStreamEvent event = new CopyStreamEvent(this, totalBytesTransferred, bytesTransferred, streamSize);
        while (listeners.hasMoreElements()) {
            ((CopyStreamListener)listeners.nextElement()).bytesTransferred(event);
        }
    }

    public void addCopyStreamListener(CopyStreamListener listener) {
        this.internalListeners.addListener(listener);
    }

    public void removeCopyStreamListener(CopyStreamListener listener) {
        this.internalListeners.removeListener(listener);
    }
}


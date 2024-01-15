/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net;

import java.io.Serializable;
import java.util.Enumeration;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.util.ListenerList;

public class ProtocolCommandSupport
implements Serializable {
    private Object __source;
    private ListenerList __listeners = new ListenerList();

    public ProtocolCommandSupport(Object source) {
        this.__source = source;
    }

    public void fireCommandSent(String command, String message) {
        Enumeration en = this.__listeners.getListeners();
        ProtocolCommandEvent event = new ProtocolCommandEvent(this.__source, command, message);
        while (en.hasMoreElements()) {
            ProtocolCommandListener listener = (ProtocolCommandListener)en.nextElement();
            listener.protocolCommandSent(event);
        }
    }

    public void fireReplyReceived(int replyCode, String message) {
        Enumeration en = this.__listeners.getListeners();
        ProtocolCommandEvent event = new ProtocolCommandEvent(this.__source, replyCode, message);
        while (en.hasMoreElements()) {
            ProtocolCommandListener listener = (ProtocolCommandListener)en.nextElement();
            listener.protocolReplyReceived(event);
        }
    }

    public void addProtocolCommandListener(ProtocolCommandListener listener) {
        this.__listeners.addListener(listener);
    }

    public void removeProtocolCommandListener(ProtocolCommandListener listener) {
        this.__listeners.removeListener(listener);
    }

    public int getListenerCount() {
        return this.__listeners.getListenerCount();
    }
}


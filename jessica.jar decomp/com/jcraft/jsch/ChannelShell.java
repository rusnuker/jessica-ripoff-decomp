/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.ChannelSession;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Request;
import com.jcraft.jsch.RequestShell;
import com.jcraft.jsch.Session;

public class ChannelShell
extends ChannelSession {
    ChannelShell() {
        this.pty = true;
    }

    public void start() throws JSchException {
        Session _session = this.getSession();
        try {
            this.sendRequests();
            RequestShell request = new RequestShell();
            ((Request)request).request(_session, this);
        }
        catch (Exception e) {
            if (e instanceof JSchException) {
                throw (JSchException)e;
            }
            if (e instanceof Throwable) {
                throw new JSchException("ChannelShell", e);
            }
            throw new JSchException("ChannelShell");
        }
        if (this.io.in != null) {
            this.thread = new Thread(this);
            this.thread.setName("Shell for " + _session.host);
            if (_session.daemon_thread) {
                this.thread.setDaemon(_session.daemon_thread);
            }
            this.thread.start();
        }
    }

    void init() throws JSchException {
        this.io.setInputStream(this.getSession().in);
        this.io.setOutputStream(this.getSession().out);
    }
}


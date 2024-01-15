/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.ChannelSession;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Request;
import com.jcraft.jsch.RequestPtyReq;
import com.jcraft.jsch.RequestSubsystem;
import com.jcraft.jsch.RequestX11;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ChannelSubsystem
extends ChannelSession {
    boolean xforwading = false;
    boolean pty = false;
    boolean want_reply = true;
    String subsystem = "";

    public void setXForwarding(boolean foo) {
        this.xforwading = foo;
    }

    public void setPty(boolean foo) {
        this.pty = foo;
    }

    public void setWantReply(boolean foo) {
        this.want_reply = foo;
    }

    public void setSubsystem(String foo) {
        this.subsystem = foo;
    }

    public void start() throws JSchException {
        Session _session = this.getSession();
        try {
            Request request;
            if (this.xforwading) {
                request = new RequestX11();
                request.request(_session, this);
            }
            if (this.pty) {
                request = new RequestPtyReq();
                request.request(_session, this);
            }
            request = new RequestSubsystem();
            ((RequestSubsystem)request).request(_session, this, this.subsystem, this.want_reply);
        }
        catch (Exception e) {
            if (e instanceof JSchException) {
                throw (JSchException)e;
            }
            if (e instanceof Throwable) {
                throw new JSchException("ChannelSubsystem", e);
            }
            throw new JSchException("ChannelSubsystem");
        }
        if (this.io.in != null) {
            this.thread = new Thread(this);
            this.thread.setName("Subsystem for " + _session.host);
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

    public void setErrStream(OutputStream out) {
        this.setExtOutputStream(out);
    }

    public InputStream getErrStream() throws IOException {
        return this.getExtInputStream();
    }
}


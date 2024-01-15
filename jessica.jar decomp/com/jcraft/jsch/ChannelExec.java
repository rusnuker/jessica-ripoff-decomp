/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.ChannelSession;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Request;
import com.jcraft.jsch.RequestExec;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Util;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ChannelExec
extends ChannelSession {
    byte[] command = new byte[0];

    public void start() throws JSchException {
        Session _session = this.getSession();
        try {
            this.sendRequests();
            RequestExec request = new RequestExec(this.command);
            ((Request)request).request(_session, this);
        }
        catch (Exception e) {
            if (e instanceof JSchException) {
                throw (JSchException)e;
            }
            if (e instanceof Throwable) {
                throw new JSchException("ChannelExec", e);
            }
            throw new JSchException("ChannelExec");
        }
        if (this.io.in != null) {
            this.thread = new Thread(this);
            this.thread.setName("Exec thread " + _session.getHost());
            if (_session.daemon_thread) {
                this.thread.setDaemon(_session.daemon_thread);
            }
            this.thread.start();
        }
    }

    public void setCommand(String command) {
        this.command = Util.str2byte(command);
    }

    public void setCommand(byte[] command) {
        this.command = command;
    }

    void init() throws JSchException {
        this.io.setInputStream(this.getSession().in);
        this.io.setOutputStream(this.getSession().out);
    }

    public void setErrStream(OutputStream out) {
        this.setExtOutputStream(out);
    }

    public void setErrStream(OutputStream out, boolean dontclose) {
        this.setExtOutputStream(out, dontclose);
    }

    public InputStream getErrStream() throws IOException {
        return this.getExtInputStream();
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.IO;
import com.jcraft.jsch.Packet;
import com.jcraft.jsch.Request;
import com.jcraft.jsch.RequestAgentForwarding;
import com.jcraft.jsch.RequestEnv;
import com.jcraft.jsch.RequestPtyReq;
import com.jcraft.jsch.RequestWindowChange;
import com.jcraft.jsch.RequestX11;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Util;
import java.util.Enumeration;
import java.util.Hashtable;

class ChannelSession
extends Channel {
    private static byte[] _session = Util.str2byte("session");
    protected boolean agent_forwarding = false;
    protected boolean xforwading = false;
    protected Hashtable env = null;
    protected boolean pty = false;
    protected String ttype = "vt100";
    protected int tcol = 80;
    protected int trow = 24;
    protected int twp = 640;
    protected int thp = 480;
    protected byte[] terminal_mode = null;

    ChannelSession() {
        this.type = _session;
        this.io = new IO();
    }

    public void setAgentForwarding(boolean enable) {
        this.agent_forwarding = enable;
    }

    public void setXForwarding(boolean enable) {
        this.xforwading = enable;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setEnv(Hashtable env) {
        ChannelSession channelSession = this;
        synchronized (channelSession) {
            this.env = env;
        }
    }

    public void setEnv(String name, String value) {
        this.setEnv(Util.str2byte(name), Util.str2byte(value));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setEnv(byte[] name, byte[] value) {
        ChannelSession channelSession = this;
        synchronized (channelSession) {
            this.getEnv().put(name, value);
        }
    }

    private Hashtable getEnv() {
        if (this.env == null) {
            this.env = new Hashtable();
        }
        return this.env;
    }

    public void setPty(boolean enable) {
        this.pty = enable;
    }

    public void setTerminalMode(byte[] terminal_mode) {
        this.terminal_mode = terminal_mode;
    }

    public void setPtySize(int col, int row, int wp, int hp) {
        this.setPtyType(this.ttype, col, row, wp, hp);
        if (!this.pty || !this.isConnected()) {
            return;
        }
        try {
            RequestWindowChange request = new RequestWindowChange();
            request.setSize(col, row, wp, hp);
            request.request(this.getSession(), this);
        }
        catch (Exception e) {
            // empty catch block
        }
    }

    public void setPtyType(String ttype) {
        this.setPtyType(ttype, 80, 24, 640, 480);
    }

    public void setPtyType(String ttype, int col, int row, int wp, int hp) {
        this.ttype = ttype;
        this.tcol = col;
        this.trow = row;
        this.twp = wp;
        this.thp = hp;
    }

    protected void sendRequests() throws Exception {
        Request request;
        Session _session = this.getSession();
        if (this.agent_forwarding) {
            request = new RequestAgentForwarding();
            request.request(_session, this);
        }
        if (this.xforwading) {
            request = new RequestX11();
            request.request(_session, this);
        }
        if (this.pty) {
            request = new RequestPtyReq();
            ((RequestPtyReq)request).setTType(this.ttype);
            ((RequestPtyReq)request).setTSize(this.tcol, this.trow, this.twp, this.thp);
            if (this.terminal_mode != null) {
                ((RequestPtyReq)request).setTerminalMode(this.terminal_mode);
            }
            request.request(_session, this);
        }
        if (this.env != null) {
            Enumeration _env = this.env.keys();
            while (_env.hasMoreElements()) {
                Object name = _env.nextElement();
                Object value = this.env.get(name);
                request = new RequestEnv();
                ((RequestEnv)request).setEnv(this.toByteArray(name), this.toByteArray(value));
                request.request(_session, this);
            }
        }
    }

    private byte[] toByteArray(Object o) {
        if (o instanceof String) {
            return Util.str2byte((String)o);
        }
        return (byte[])o;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void run() {
        Buffer buf = new Buffer(this.rmpsize);
        Packet packet = new Packet(buf);
        int i = -1;
        try {
            while (this.isConnected() && this.thread != null && this.io != null && this.io.in != null) {
                i = this.io.in.read(buf.buffer, 14, buf.buffer.length - 14 - 128);
                if (i == 0) continue;
                if (i == -1) {
                    this.eof();
                } else if (!this.close) {
                    packet.reset();
                    buf.putByte((byte)94);
                    buf.putInt(this.recipient);
                    buf.putInt(i);
                    buf.skip(i);
                    this.getSession().write(packet, this, i);
                    continue;
                }
                break;
            }
        }
        catch (Exception e) {
            // empty catch block
        }
        Thread _thread = this.thread;
        if (_thread != null) {
            Thread thread = _thread;
            synchronized (thread) {
                _thread.notifyAll();
            }
        }
        this.thread = null;
    }
}


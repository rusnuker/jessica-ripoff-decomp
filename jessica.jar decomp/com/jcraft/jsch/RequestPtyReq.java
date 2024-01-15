/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Packet;
import com.jcraft.jsch.Request;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Util;

class RequestPtyReq
extends Request {
    private String ttype = "vt100";
    private int tcol = 80;
    private int trow = 24;
    private int twp = 640;
    private int thp = 480;
    private byte[] terminal_mode = Util.empty;

    RequestPtyReq() {
    }

    void setCode(String cookie) {
    }

    void setTType(String ttype) {
        this.ttype = ttype;
    }

    void setTerminalMode(byte[] terminal_mode) {
        this.terminal_mode = terminal_mode;
    }

    void setTSize(int tcol, int trow, int twp, int thp) {
        this.tcol = tcol;
        this.trow = trow;
        this.twp = twp;
        this.thp = thp;
    }

    public void request(Session session, Channel channel) throws Exception {
        super.request(session, channel);
        Buffer buf = new Buffer();
        Packet packet = new Packet(buf);
        packet.reset();
        buf.putByte((byte)98);
        buf.putInt(channel.getRecipient());
        buf.putString(Util.str2byte("pty-req"));
        buf.putByte((byte)(this.waitForReply() ? 1 : 0));
        buf.putString(Util.str2byte(this.ttype));
        buf.putInt(this.tcol);
        buf.putInt(this.trow);
        buf.putInt(this.twp);
        buf.putInt(this.thp);
        buf.putString(this.terminal_mode);
        this.write(packet);
    }
}


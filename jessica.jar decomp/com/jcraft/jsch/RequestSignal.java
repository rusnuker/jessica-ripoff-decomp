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

class RequestSignal
extends Request {
    private String signal = "KILL";

    RequestSignal() {
    }

    public void setSignal(String foo) {
        this.signal = foo;
    }

    public void request(Session session, Channel channel) throws Exception {
        super.request(session, channel);
        Buffer buf = new Buffer();
        Packet packet = new Packet(buf);
        packet.reset();
        buf.putByte((byte)98);
        buf.putInt(channel.getRecipient());
        buf.putString(Util.str2byte("signal"));
        buf.putByte((byte)(this.waitForReply() ? 1 : 0));
        buf.putString(Util.str2byte(this.signal));
        this.write(packet);
    }
}


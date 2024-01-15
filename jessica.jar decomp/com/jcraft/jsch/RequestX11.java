/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelX11;
import com.jcraft.jsch.Packet;
import com.jcraft.jsch.Request;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Util;

class RequestX11
extends Request {
    RequestX11() {
    }

    public void setCookie(String cookie) {
        ChannelX11.cookie = Util.str2byte(cookie);
    }

    public void request(Session session, Channel channel) throws Exception {
        super.request(session, channel);
        Buffer buf = new Buffer();
        Packet packet = new Packet(buf);
        packet.reset();
        buf.putByte((byte)98);
        buf.putInt(channel.getRecipient());
        buf.putString(Util.str2byte("x11-req"));
        buf.putByte((byte)(this.waitForReply() ? 1 : 0));
        buf.putByte((byte)0);
        buf.putString(Util.str2byte("MIT-MAGIC-COOKIE-1"));
        buf.putString(ChannelX11.getFakedCookie(session));
        buf.putInt(0);
        this.write(packet);
        session.x11_forwarding = true;
    }
}


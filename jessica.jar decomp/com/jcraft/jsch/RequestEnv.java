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

class RequestEnv
extends Request {
    byte[] name = new byte[0];
    byte[] value = new byte[0];

    RequestEnv() {
    }

    void setEnv(byte[] name, byte[] value) {
        this.name = name;
        this.value = value;
    }

    public void request(Session session, Channel channel) throws Exception {
        super.request(session, channel);
        Buffer buf = new Buffer();
        Packet packet = new Packet(buf);
        packet.reset();
        buf.putByte((byte)98);
        buf.putInt(channel.getRecipient());
        buf.putString(Util.str2byte("env"));
        buf.putByte((byte)(this.waitForReply() ? 1 : 0));
        buf.putString(this.name);
        buf.putString(this.value);
        this.write(packet);
    }
}


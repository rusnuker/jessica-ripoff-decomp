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

class RequestExec
extends Request {
    private byte[] command = new byte[0];

    RequestExec(byte[] command) {
        this.command = command;
    }

    public void request(Session session, Channel channel) throws Exception {
        super.request(session, channel);
        Buffer buf = new Buffer();
        Packet packet = new Packet(buf);
        packet.reset();
        buf.putByte((byte)98);
        buf.putInt(channel.getRecipient());
        buf.putString(Util.str2byte("exec"));
        buf.putByte((byte)(this.waitForReply() ? 1 : 0));
        buf.checkFreeSize(4 + this.command.length);
        buf.putString(this.command);
        this.write(packet);
    }
}


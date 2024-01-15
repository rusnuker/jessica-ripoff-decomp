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

public class RequestSubsystem
extends Request {
    private String subsystem = null;

    public void request(Session session, Channel channel, String subsystem, boolean want_reply) throws Exception {
        this.setReply(want_reply);
        this.subsystem = subsystem;
        this.request(session, channel);
    }

    public void request(Session session, Channel channel) throws Exception {
        super.request(session, channel);
        Buffer buf = new Buffer();
        Packet packet = new Packet(buf);
        packet.reset();
        buf.putByte((byte)98);
        buf.putInt(channel.getRecipient());
        buf.putString(Util.str2byte("subsystem"));
        buf.putByte((byte)(this.waitForReply() ? 1 : 0));
        buf.putString(Util.str2byte(this.subsystem));
        this.write(packet);
    }
}


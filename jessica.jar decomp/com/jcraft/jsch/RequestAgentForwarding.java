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

class RequestAgentForwarding
extends Request {
    RequestAgentForwarding() {
    }

    public void request(Session session, Channel channel) throws Exception {
        super.request(session, channel);
        this.setReply(false);
        Buffer buf = new Buffer();
        Packet packet = new Packet(buf);
        packet.reset();
        buf.putByte((byte)98);
        buf.putInt(channel.getRecipient());
        buf.putString(Util.str2byte("auth-agent-req@openssh.com"));
        buf.putByte((byte)(this.waitForReply() ? 1 : 0));
        this.write(packet);
        session.agent_forwarding = true;
    }
}


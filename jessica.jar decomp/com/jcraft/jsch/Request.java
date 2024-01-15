/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Packet;
import com.jcraft.jsch.Session;

abstract class Request {
    private boolean reply = false;
    private Session session = null;
    private Channel channel = null;

    Request() {
    }

    void request(Session session, Channel channel) throws Exception {
        this.session = session;
        this.channel = channel;
        if (channel.connectTimeout > 0) {
            this.setReply(true);
        }
    }

    boolean waitForReply() {
        return this.reply;
    }

    void setReply(boolean reply) {
        this.reply = reply;
    }

    void write(Packet packet) throws Exception {
        if (this.reply) {
            this.channel.reply = -1;
        }
        this.session.write(packet);
        if (this.reply) {
            long start = System.currentTimeMillis();
            long timeout = this.channel.connectTimeout;
            while (this.channel.isConnected() && this.channel.reply == -1) {
                try {
                    Thread.sleep(10L);
                }
                catch (Exception ee) {
                    // empty catch block
                }
                if (timeout <= 0L || System.currentTimeMillis() - start <= timeout) continue;
                this.channel.reply = 0;
                throw new JSchException("channel request: timeout");
            }
            if (this.channel.reply == 0) {
                throw new JSchException("failed to send channel request");
            }
        }
    }
}


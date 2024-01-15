/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.IO;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Packet;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Util;
import java.io.InputStream;
import java.io.OutputStream;

public class ChannelDirectTCPIP
extends Channel {
    private static final int LOCAL_WINDOW_SIZE_MAX = 131072;
    private static final int LOCAL_MAXIMUM_PACKET_SIZE = 16384;
    private static final byte[] _type = Util.str2byte("direct-tcpip");
    String host;
    int port;
    String originator_IP_address = "127.0.0.1";
    int originator_port = 0;

    ChannelDirectTCPIP() {
        this.type = _type;
        this.setLocalWindowSizeMax(131072);
        this.setLocalWindowSize(131072);
        this.setLocalPacketSize(16384);
    }

    void init() {
        this.io = new IO();
    }

    public void connect(int connectTimeout) throws JSchException {
        block6: {
            this.connectTimeout = connectTimeout;
            try {
                Session _session = this.getSession();
                if (!_session.isConnected()) {
                    throw new JSchException("session is down");
                }
                if (this.io.in != null) {
                    this.thread = new Thread(this);
                    this.thread.setName("DirectTCPIP thread " + _session.getHost());
                    if (_session.daemon_thread) {
                        this.thread.setDaemon(_session.daemon_thread);
                    }
                    this.thread.start();
                } else {
                    this.sendChannelOpen();
                }
            }
            catch (Exception e) {
                this.io.close();
                this.io = null;
                Channel.del(this);
                if (!(e instanceof JSchException)) break block6;
                throw (JSchException)e;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void run() {
        try {
            this.sendChannelOpen();
            Buffer buf = new Buffer(this.rmpsize);
            Packet packet = new Packet(buf);
            Session _session = this.getSession();
            int i = 0;
            while (this.isConnected() && this.thread != null && this.io != null && this.io.in != null) {
                i = this.io.in.read(buf.buffer, 14, buf.buffer.length - 14 - 128);
                if (i <= 0) {
                    this.eof();
                    break;
                }
                packet.reset();
                buf.putByte((byte)94);
                buf.putInt(this.recipient);
                buf.putInt(i);
                buf.skip(i);
                ChannelDirectTCPIP channelDirectTCPIP = this;
                synchronized (channelDirectTCPIP) {
                    if (this.close) {
                        break;
                    }
                    _session.write(packet, this, i);
                }
            }
        }
        catch (Exception e) {
            if (!this.connected) {
                this.connected = true;
            }
            this.disconnect();
            return;
        }
        this.eof();
        this.disconnect();
    }

    public void setInputStream(InputStream in) {
        this.io.setInputStream(in);
    }

    public void setOutputStream(OutputStream out) {
        this.io.setOutputStream(out);
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setOrgIPAddress(String foo) {
        this.originator_IP_address = foo;
    }

    public void setOrgPort(int foo) {
        this.originator_port = foo;
    }

    protected Packet genChannelOpenPacket() {
        Buffer buf = new Buffer(50 + this.host.length() + this.originator_IP_address.length() + 128);
        Packet packet = new Packet(buf);
        packet.reset();
        buf.putByte((byte)90);
        buf.putString(this.type);
        buf.putInt(this.id);
        buf.putInt(this.lwsize);
        buf.putInt(this.lmpsize);
        buf.putString(Util.str2byte(this.host));
        buf.putInt(this.port);
        buf.putString(Util.str2byte(this.originator_IP_address));
        buf.putInt(this.originator_port);
        return packet;
    }
}


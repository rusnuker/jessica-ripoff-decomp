/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.IO;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Packet;
import com.jcraft.jsch.Random;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Util;
import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;

class ChannelX11
extends Channel {
    private static final int LOCAL_WINDOW_SIZE_MAX = 131072;
    private static final int LOCAL_MAXIMUM_PACKET_SIZE = 16384;
    private static final int TIMEOUT = 10000;
    private static String host = "127.0.0.1";
    private static int port = 6000;
    private boolean init = true;
    static byte[] cookie = null;
    private static byte[] cookie_hex = null;
    private static Hashtable faked_cookie_pool = new Hashtable();
    private static Hashtable faked_cookie_hex_pool = new Hashtable();
    private static byte[] table = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};
    private Socket socket = null;
    private byte[] cache = new byte[0];

    static int revtable(byte foo) {
        for (int i = 0; i < table.length; ++i) {
            if (table[i] != foo) continue;
            return i;
        }
        return 0;
    }

    static void setCookie(String foo) {
        cookie_hex = Util.str2byte(foo);
        cookie = new byte[16];
        for (int i = 0; i < 16; ++i) {
            ChannelX11.cookie[i] = (byte)(ChannelX11.revtable(cookie_hex[i * 2]) << 4 & 0xF0 | ChannelX11.revtable(cookie_hex[i * 2 + 1]) & 0xF);
        }
    }

    static void setHost(String foo) {
        host = foo;
    }

    static void setPort(int foo) {
        port = foo;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static byte[] getFakedCookie(Session session) {
        Hashtable hashtable = faked_cookie_hex_pool;
        synchronized (hashtable) {
            byte[] foo = (byte[])faked_cookie_hex_pool.get(session);
            if (foo == null) {
                Random random = Session.random;
                foo = new byte[16];
                Random random2 = random;
                synchronized (random2) {
                    random.fill(foo, 0, 16);
                }
                faked_cookie_pool.put(session, foo);
                byte[] bar = new byte[32];
                for (int i = 0; i < 16; ++i) {
                    bar[2 * i] = table[foo[i] >>> 4 & 0xF];
                    bar[2 * i + 1] = table[foo[i] & 0xF];
                }
                faked_cookie_hex_pool.put(session, bar);
                foo = bar;
            }
            return foo;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void removeFakedCookie(Session session) {
        Hashtable hashtable = faked_cookie_hex_pool;
        synchronized (hashtable) {
            faked_cookie_hex_pool.remove(session);
            faked_cookie_pool.remove(session);
        }
    }

    ChannelX11() {
        this.setLocalWindowSizeMax(131072);
        this.setLocalWindowSize(131072);
        this.setLocalPacketSize(16384);
        this.type = Util.str2byte("x11");
        this.connected = true;
    }

    public void run() {
        try {
            this.socket = Util.createSocket(host, port, 10000);
            this.socket.setTcpNoDelay(true);
            this.io = new IO();
            this.io.setInputStream(this.socket.getInputStream());
            this.io.setOutputStream(this.socket.getOutputStream());
            this.sendOpenConfirmation();
        }
        catch (Exception e) {
            this.sendOpenFailure(1);
            this.close = true;
            this.disconnect();
            return;
        }
        this.thread = Thread.currentThread();
        Buffer buf = new Buffer(this.rmpsize);
        Packet packet = new Packet(buf);
        int i = 0;
        try {
            while (this.thread != null && this.io != null && this.io.in != null) {
                i = this.io.in.read(buf.buffer, 14, buf.buffer.length - 14 - 128);
                if (i <= 0) {
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
        this.disconnect();
    }

    private byte[] addCache(byte[] foo, int s, int l) {
        byte[] bar = new byte[this.cache.length + l];
        System.arraycopy(foo, s, bar, this.cache.length, l);
        if (this.cache.length > 0) {
            System.arraycopy(this.cache, 0, bar, 0, this.cache.length);
        }
        this.cache = bar;
        return this.cache;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void write(byte[] foo, int s, int l) throws IOException {
        if (this.init) {
            Session _session = null;
            try {
                _session = this.getSession();
            }
            catch (JSchException e) {
                throw new IOException(e.toString());
            }
            foo = this.addCache(foo, s, l);
            s = 0;
            l = foo.length;
            if (l < 9) {
                return;
            }
            int plen = (foo[s + 6] & 0xFF) * 256 + (foo[s + 7] & 0xFF);
            int dlen = (foo[s + 8] & 0xFF) * 256 + (foo[s + 9] & 0xFF);
            if ((foo[s] & 0xFF) != 66 && (foo[s] & 0xFF) == 108) {
                plen = plen >>> 8 & 0xFF | plen << 8 & 0xFF00;
                dlen = dlen >>> 8 & 0xFF | dlen << 8 & 0xFF00;
            }
            if (l < 12 + plen + (-plen & 3) + dlen) {
                return;
            }
            byte[] bar = new byte[dlen];
            System.arraycopy(foo, s + 12 + plen + (-plen & 3), bar, 0, dlen);
            byte[] faked_cookie = null;
            Hashtable hashtable = faked_cookie_pool;
            synchronized (hashtable) {
                faked_cookie = (byte[])faked_cookie_pool.get(_session);
            }
            if (ChannelX11.equals(bar, faked_cookie)) {
                if (cookie != null) {
                    System.arraycopy(cookie, 0, foo, s + 12 + plen + (-plen & 3), dlen);
                }
            } else {
                this.thread = null;
                this.eof();
                this.io.close();
                this.disconnect();
            }
            this.init = false;
            this.io.put(foo, s, l);
            this.cache = null;
            return;
        }
        this.io.put(foo, s, l);
    }

    private static boolean equals(byte[] foo, byte[] bar) {
        if (foo.length != bar.length) {
            return false;
        }
        for (int i = 0; i < foo.length; ++i) {
            if (foo[i] == bar[i]) continue;
            return false;
        }
        return true;
    }
}


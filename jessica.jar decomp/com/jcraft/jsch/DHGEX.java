/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.DH;
import com.jcraft.jsch.HASH;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyExchange;
import com.jcraft.jsch.Packet;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Util;

public class DHGEX
extends KeyExchange {
    private static final int SSH_MSG_KEX_DH_GEX_GROUP = 31;
    private static final int SSH_MSG_KEX_DH_GEX_INIT = 32;
    private static final int SSH_MSG_KEX_DH_GEX_REPLY = 33;
    private static final int SSH_MSG_KEX_DH_GEX_REQUEST = 34;
    static int min = 1024;
    static int preferred = 1024;
    int max = 1024;
    private int state;
    DH dh;
    byte[] V_S;
    byte[] V_C;
    byte[] I_S;
    byte[] I_C;
    private Buffer buf;
    private Packet packet;
    private byte[] p;
    private byte[] g;
    private byte[] e;
    protected String hash = "sha-1";

    public void init(Session session, byte[] V_S, byte[] V_C, byte[] I_S, byte[] I_C) throws Exception {
        Class<?> c;
        this.session = session;
        this.V_S = V_S;
        this.V_C = V_C;
        this.I_S = I_S;
        this.I_C = I_C;
        try {
            c = Class.forName(session.getConfig(this.hash));
            this.sha = (HASH)c.newInstance();
            this.sha.init();
        }
        catch (Exception e) {
            System.err.println(e);
        }
        this.buf = new Buffer();
        this.packet = new Packet(this.buf);
        c = Class.forName(session.getConfig("dh"));
        preferred = this.max = this.check2048(c, this.max);
        this.dh = (DH)c.newInstance();
        this.dh.init();
        this.packet.reset();
        this.buf.putByte((byte)34);
        this.buf.putInt(min);
        this.buf.putInt(preferred);
        this.buf.putInt(this.max);
        session.write(this.packet);
        if (JSch.getLogger().isEnabled(1)) {
            JSch.getLogger().log(1, "SSH_MSG_KEX_DH_GEX_REQUEST(" + min + "<" + preferred + "<" + this.max + ") sent");
            JSch.getLogger().log(1, "expecting SSH_MSG_KEX_DH_GEX_GROUP");
        }
        this.state = 31;
    }

    public boolean next(Buffer _buf) throws Exception {
        switch (this.state) {
            case 31: {
                _buf.getInt();
                _buf.getByte();
                int j = _buf.getByte();
                if (j != 31) {
                    System.err.println("type: must be SSH_MSG_KEX_DH_GEX_GROUP " + j);
                    return false;
                }
                this.p = _buf.getMPInt();
                this.g = _buf.getMPInt();
                this.dh.setP(this.p);
                this.dh.setG(this.g);
                this.e = this.dh.getE();
                this.packet.reset();
                this.buf.putByte((byte)32);
                this.buf.putMPInt(this.e);
                this.session.write(this.packet);
                if (JSch.getLogger().isEnabled(1)) {
                    JSch.getLogger().log(1, "SSH_MSG_KEX_DH_GEX_INIT sent");
                    JSch.getLogger().log(1, "expecting SSH_MSG_KEX_DH_GEX_REPLY");
                }
                this.state = 33;
                return true;
            }
            case 33: {
                int j = _buf.getInt();
                j = _buf.getByte();
                j = _buf.getByte();
                if (j != 33) {
                    System.err.println("type: must be SSH_MSG_KEX_DH_GEX_REPLY " + j);
                    return false;
                }
                this.K_S = _buf.getString();
                byte[] f = _buf.getMPInt();
                byte[] sig_of_H = _buf.getString();
                this.dh.setF(f);
                this.dh.checkRange();
                this.K = this.normalize(this.dh.getK());
                this.buf.reset();
                this.buf.putString(this.V_C);
                this.buf.putString(this.V_S);
                this.buf.putString(this.I_C);
                this.buf.putString(this.I_S);
                this.buf.putString(this.K_S);
                this.buf.putInt(min);
                this.buf.putInt(preferred);
                this.buf.putInt(this.max);
                this.buf.putMPInt(this.p);
                this.buf.putMPInt(this.g);
                this.buf.putMPInt(this.e);
                this.buf.putMPInt(f);
                this.buf.putMPInt(this.K);
                byte[] foo = new byte[this.buf.getLength()];
                this.buf.getByte(foo);
                this.sha.update(foo, 0, foo.length);
                this.H = this.sha.digest();
                int i = 0;
                j = 0;
                j = this.K_S[i++] << 24 & 0xFF000000 | this.K_S[i++] << 16 & 0xFF0000 | this.K_S[i++] << 8 & 0xFF00 | this.K_S[i++] & 0xFF;
                String alg = Util.byte2str(this.K_S, i, j);
                boolean result = this.verify(alg, this.K_S, i += j, sig_of_H);
                this.state = 0;
                return result;
            }
        }
        return false;
    }

    public int getState() {
        return this.state;
    }

    protected int check2048(Class c, int _max) throws Exception {
        DH dh = (DH)c.newInstance();
        dh.init();
        byte[] foo = new byte[257];
        foo[1] = -35;
        foo[256] = 115;
        dh.setP(foo);
        byte[] bar = new byte[]{2};
        dh.setG(bar);
        try {
            dh.getE();
            _max = 2048;
        }
        catch (Exception e) {
            // empty catch block
        }
        return _max;
    }
}


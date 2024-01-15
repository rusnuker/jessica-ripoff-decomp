/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.ECDH;
import com.jcraft.jsch.HASH;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyExchange;
import com.jcraft.jsch.KeyPairECDSA;
import com.jcraft.jsch.Packet;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Util;

public abstract class DHECN
extends KeyExchange {
    private static final int SSH_MSG_KEX_ECDH_INIT = 30;
    private static final int SSH_MSG_KEX_ECDH_REPLY = 31;
    private int state;
    byte[] Q_C;
    byte[] V_S;
    byte[] V_C;
    byte[] I_S;
    byte[] I_C;
    byte[] e;
    private Buffer buf;
    private Packet packet;
    private ECDH ecdh;
    protected String sha_name;
    protected int key_size;

    public void init(Session session, byte[] V_S, byte[] V_C, byte[] I_S, byte[] I_C) throws Exception {
        Class<?> c;
        this.session = session;
        this.V_S = V_S;
        this.V_C = V_C;
        this.I_S = I_S;
        this.I_C = I_C;
        try {
            c = Class.forName(session.getConfig(this.sha_name));
            this.sha = (HASH)c.newInstance();
            this.sha.init();
        }
        catch (Exception e) {
            System.err.println(e);
        }
        this.buf = new Buffer();
        this.packet = new Packet(this.buf);
        this.packet.reset();
        this.buf.putByte((byte)30);
        try {
            c = Class.forName(session.getConfig("ecdh-sha2-nistp"));
            this.ecdh = (ECDH)c.newInstance();
            this.ecdh.init(this.key_size);
            this.Q_C = this.ecdh.getQ();
            this.buf.putString(this.Q_C);
        }
        catch (Exception e) {
            if (e instanceof Throwable) {
                throw new JSchException(e.toString(), e);
            }
            throw new JSchException(e.toString());
        }
        if (V_S == null) {
            return;
        }
        session.write(this.packet);
        if (JSch.getLogger().isEnabled(1)) {
            JSch.getLogger().log(1, "SSH_MSG_KEX_ECDH_INIT sent");
            JSch.getLogger().log(1, "expecting SSH_MSG_KEX_ECDH_REPLY");
        }
        this.state = 31;
    }

    public boolean next(Buffer _buf) throws Exception {
        switch (this.state) {
            case 31: {
                int j = _buf.getInt();
                j = _buf.getByte();
                j = _buf.getByte();
                if (j != 31) {
                    System.err.println("type: must be 31 " + j);
                    return false;
                }
                this.K_S = _buf.getString();
                byte[] Q_S = _buf.getString();
                byte[][] r_s = KeyPairECDSA.fromPoint(Q_S);
                if (!this.ecdh.validate(r_s[0], r_s[1])) {
                    return false;
                }
                this.K = this.ecdh.getSecret(r_s[0], r_s[1]);
                this.K = this.normalize(this.K);
                byte[] sig_of_H = _buf.getString();
                this.buf.reset();
                this.buf.putString(this.V_C);
                this.buf.putString(this.V_S);
                this.buf.putString(this.I_C);
                this.buf.putString(this.I_S);
                this.buf.putString(this.K_S);
                this.buf.putString(this.Q_C);
                this.buf.putString(Q_S);
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
}


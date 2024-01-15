/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Identity;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Packet;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.jcraft.jsch.Util;
import java.io.IOException;
import java.util.Vector;

class ChannelAgentForwarding
extends Channel {
    private static final int LOCAL_WINDOW_SIZE_MAX = 131072;
    private static final int LOCAL_MAXIMUM_PACKET_SIZE = 16384;
    private final byte SSH_AGENTC_REQUEST_RSA_IDENTITIES = 1;
    private final byte SSH_AGENT_RSA_IDENTITIES_ANSWER = (byte)2;
    private final byte SSH_AGENTC_RSA_CHALLENGE = (byte)3;
    private final byte SSH_AGENT_RSA_RESPONSE = (byte)4;
    private final byte SSH_AGENT_FAILURE = (byte)5;
    private final byte SSH_AGENT_SUCCESS = (byte)6;
    private final byte SSH_AGENTC_ADD_RSA_IDENTITY = (byte)7;
    private final byte SSH_AGENTC_REMOVE_RSA_IDENTITY = (byte)8;
    private final byte SSH_AGENTC_REMOVE_ALL_RSA_IDENTITIES = (byte)9;
    private final byte SSH2_AGENTC_REQUEST_IDENTITIES = (byte)11;
    private final byte SSH2_AGENT_IDENTITIES_ANSWER = (byte)12;
    private final byte SSH2_AGENTC_SIGN_REQUEST = (byte)13;
    private final byte SSH2_AGENT_SIGN_RESPONSE = (byte)14;
    private final byte SSH2_AGENTC_ADD_IDENTITY = (byte)17;
    private final byte SSH2_AGENTC_REMOVE_IDENTITY = (byte)18;
    private final byte SSH2_AGENTC_REMOVE_ALL_IDENTITIES = (byte)19;
    private final byte SSH2_AGENT_FAILURE = (byte)30;
    boolean init = true;
    private Buffer rbuf = null;
    private Buffer wbuf = null;
    private Packet packet = null;
    private Buffer mbuf = null;

    ChannelAgentForwarding() {
        this.setLocalWindowSizeMax(131072);
        this.setLocalWindowSize(131072);
        this.setLocalPacketSize(16384);
        this.type = Util.str2byte("auth-agent@openssh.com");
        this.rbuf = new Buffer();
        this.rbuf.reset();
        this.mbuf = new Buffer();
        this.connected = true;
    }

    public void run() {
        try {
            this.sendOpenConfirmation();
        }
        catch (Exception e) {
            this.close = true;
            this.disconnect();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void write(byte[] foo, int s, int l) throws IOException {
        if (this.packet == null) {
            this.wbuf = new Buffer(this.rmpsize);
            this.packet = new Packet(this.wbuf);
        }
        this.rbuf.shift();
        if (this.rbuf.buffer.length < this.rbuf.index + l) {
            byte[] newbuf = new byte[this.rbuf.s + l];
            System.arraycopy(this.rbuf.buffer, 0, newbuf, 0, this.rbuf.buffer.length);
            this.rbuf.buffer = newbuf;
        }
        this.rbuf.putByte(foo, s, l);
        int mlen = this.rbuf.getInt();
        if (mlen > this.rbuf.getLength()) {
            this.rbuf.s -= 4;
            return;
        }
        int typ = this.rbuf.getByte();
        Session _session = null;
        try {
            _session = this.getSession();
        }
        catch (JSchException e) {
            throw new IOException(e.toString());
        }
        IdentityRepository irepo = _session.getIdentityRepository();
        UserInfo userinfo = _session.getUserInfo();
        this.mbuf.reset();
        if (typ == 11) {
            Vector identities;
            this.mbuf.putByte((byte)12);
            Vector vector = identities = irepo.getIdentities();
            synchronized (vector) {
                Identity identity;
                int i;
                int count = 0;
                for (i = 0; i < identities.size(); ++i) {
                    identity = (Identity)identities.elementAt(i);
                    if (identity.getPublicKeyBlob() == null) continue;
                    ++count;
                }
                this.mbuf.putInt(count);
                for (i = 0; i < identities.size(); ++i) {
                    identity = (Identity)identities.elementAt(i);
                    byte[] pubkeyblob = identity.getPublicKeyBlob();
                    if (pubkeyblob == null) continue;
                    this.mbuf.putString(pubkeyblob);
                    this.mbuf.putString(Util.empty);
                }
            }
        }
        if (typ == 1) {
            this.mbuf.putByte((byte)2);
            this.mbuf.putInt(0);
        } else if (typ == 13) {
            byte[] blob = this.rbuf.getString();
            byte[] data = this.rbuf.getString();
            int flags = this.rbuf.getInt();
            Vector identities = irepo.getIdentities();
            Identity identity = null;
            Vector pubkeyblob = identities;
            synchronized (pubkeyblob) {
                for (int i = 0; i < identities.size(); ++i) {
                    Identity _identity = (Identity)identities.elementAt(i);
                    if (_identity.getPublicKeyBlob() == null || !Util.array_equals(blob, _identity.getPublicKeyBlob())) continue;
                    if (_identity.isEncrypted()) {
                        String _passphrase;
                        if (userinfo == null) continue;
                        while (_identity.isEncrypted() && userinfo.promptPassphrase("Passphrase for " + _identity.getName()) && (_passphrase = userinfo.getPassphrase()) != null) {
                            byte[] passphrase = Util.str2byte(_passphrase);
                            try {
                                if (!_identity.setPassphrase(passphrase)) continue;
                            }
                            catch (JSchException e) {}
                            break;
                        }
                    }
                    if (_identity.isEncrypted()) continue;
                    identity = _identity;
                    break;
                }
            }
            byte[] signature = null;
            if (identity != null) {
                signature = identity.getSignature(data);
            }
            if (signature == null) {
                this.mbuf.putByte((byte)30);
            } else {
                this.mbuf.putByte((byte)14);
                this.mbuf.putString(signature);
            }
        } else if (typ == 18) {
            byte[] blob = this.rbuf.getString();
            irepo.remove(blob);
            this.mbuf.putByte((byte)6);
        } else if (typ == 9) {
            this.mbuf.putByte((byte)6);
        } else if (typ == 19) {
            irepo.removeAll();
            this.mbuf.putByte((byte)6);
        } else if (typ == 17) {
            int fooo = this.rbuf.getLength();
            byte[] tmp = new byte[fooo];
            this.rbuf.getByte(tmp);
            boolean result = irepo.add(tmp);
            this.mbuf.putByte(result ? (byte)6 : 5);
        } else {
            this.rbuf.skip(this.rbuf.getLength() - 1);
            this.mbuf.putByte((byte)5);
        }
        byte[] response = new byte[this.mbuf.getLength()];
        this.mbuf.getByte(response);
        this.send(response);
    }

    private void send(byte[] message) {
        this.packet.reset();
        this.wbuf.putByte((byte)94);
        this.wbuf.putInt(this.recipient);
        this.wbuf.putInt(4 + message.length);
        this.wbuf.putString(message);
        try {
            this.getSession().write(this.packet, this, 4 + message.length);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    void eof_remote() {
        super.eof_remote();
        this.eof();
    }
}


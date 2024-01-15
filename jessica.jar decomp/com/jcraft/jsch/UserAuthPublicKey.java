/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Identity;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSchAuthCancelException;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.JSchPartialAuthException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserAuth;
import com.jcraft.jsch.Util;
import java.util.Vector;

class UserAuthPublicKey
extends UserAuth {
    UserAuthPublicKey() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean start(Session session) throws Exception {
        super.start(session);
        Vector identities = session.getIdentityRepository().getIdentities();
        byte[] passphrase = null;
        byte[] _username = null;
        Vector vector = identities;
        synchronized (vector) {
            if (identities.size() <= 0) {
                return false;
            }
            _username = Util.str2byte(this.username);
            for (int i = 0; i < identities.size(); ++i) {
                int command;
                if (session.auth_failures >= session.max_auth_tries) {
                    return false;
                }
                Identity identity = (Identity)identities.elementAt(i);
                byte[] pubkeyblob = identity.getPublicKeyBlob();
                if (pubkeyblob != null) {
                    this.packet.reset();
                    this.buf.putByte((byte)50);
                    this.buf.putString(_username);
                    this.buf.putString(Util.str2byte("ssh-connection"));
                    this.buf.putString(Util.str2byte("publickey"));
                    this.buf.putByte((byte)0);
                    this.buf.putString(Util.str2byte(identity.getAlgName()));
                    this.buf.putString(pubkeyblob);
                    session.write(this.packet);
                    while (true) {
                        this.buf = session.read(this.buf);
                        command = this.buf.getCommand() & 0xFF;
                        if (command == 60 || command == 51 || command != 53) break;
                        this.buf.getInt();
                        this.buf.getByte();
                        this.buf.getByte();
                        byte[] _message = this.buf.getString();
                        byte[] lang = this.buf.getString();
                        String message = Util.byte2str(_message);
                        if (this.userinfo == null) continue;
                        this.userinfo.showMessage(message);
                    }
                    if (command != 60) continue;
                }
                int count = 5;
                do {
                    if (identity.isEncrypted() && passphrase == null) {
                        if (this.userinfo == null) {
                            throw new JSchException("USERAUTH fail");
                        }
                        if (identity.isEncrypted() && !this.userinfo.promptPassphrase("Passphrase for " + identity.getName())) {
                            throw new JSchAuthCancelException("publickey");
                        }
                        String _passphrase = this.userinfo.getPassphrase();
                        if (_passphrase != null) {
                            passphrase = Util.str2byte(_passphrase);
                        }
                    }
                    if ((!identity.isEncrypted() || passphrase != null) && identity.setPassphrase(passphrase)) {
                        if (passphrase == null || !(session.getIdentityRepository() instanceof IdentityRepository.Wrapper)) break;
                        ((IdentityRepository.Wrapper)session.getIdentityRepository()).check();
                        break;
                    }
                    Util.bzero(passphrase);
                    passphrase = null;
                } while (--count != 0);
                Util.bzero(passphrase);
                passphrase = null;
                if (identity.isEncrypted()) continue;
                if (pubkeyblob == null) {
                    pubkeyblob = identity.getPublicKeyBlob();
                }
                if (pubkeyblob == null) continue;
                this.packet.reset();
                this.buf.putByte((byte)50);
                this.buf.putString(_username);
                this.buf.putString(Util.str2byte("ssh-connection"));
                this.buf.putString(Util.str2byte("publickey"));
                this.buf.putByte((byte)1);
                this.buf.putString(Util.str2byte(identity.getAlgName()));
                this.buf.putString(pubkeyblob);
                byte[] sid = session.getSessionId();
                int sidlen = sid.length;
                byte[] tmp = new byte[4 + sidlen + this.buf.index - 5];
                tmp[0] = (byte)(sidlen >>> 24);
                tmp[1] = (byte)(sidlen >>> 16);
                tmp[2] = (byte)(sidlen >>> 8);
                tmp[3] = (byte)sidlen;
                System.arraycopy(sid, 0, tmp, 4, sidlen);
                System.arraycopy(this.buf.buffer, 5, tmp, 4 + sidlen, this.buf.index - 5);
                byte[] signature = identity.getSignature(tmp);
                if (signature == null) break;
                this.buf.putString(signature);
                session.write(this.packet);
                while (true) {
                    this.buf = session.read(this.buf);
                    command = this.buf.getCommand() & 0xFF;
                    if (command == 52) {
                        return true;
                    }
                    if (command != 53) break;
                    this.buf.getInt();
                    this.buf.getByte();
                    this.buf.getByte();
                    byte[] _message = this.buf.getString();
                    byte[] lang = this.buf.getString();
                    String message = Util.byte2str(_message);
                    if (this.userinfo == null) continue;
                    this.userinfo.showMessage(message);
                }
                if (command != 51) continue;
                this.buf.getInt();
                this.buf.getByte();
                this.buf.getByte();
                byte[] foo = this.buf.getString();
                int partial_success = this.buf.getByte();
                if (partial_success != 0) {
                    throw new JSchPartialAuthException(Util.byte2str(foo));
                }
                ++session.auth_failures;
            }
        }
        return false;
    }
}


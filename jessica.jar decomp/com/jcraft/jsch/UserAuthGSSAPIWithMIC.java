/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.GSSContext;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.JSchPartialAuthException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserAuth;
import com.jcraft.jsch.Util;

public class UserAuthGSSAPIWithMIC
extends UserAuth {
    private static final int SSH_MSG_USERAUTH_GSSAPI_RESPONSE = 60;
    private static final int SSH_MSG_USERAUTH_GSSAPI_TOKEN = 61;
    private static final int SSH_MSG_USERAUTH_GSSAPI_EXCHANGE_COMPLETE = 63;
    private static final int SSH_MSG_USERAUTH_GSSAPI_ERROR = 64;
    private static final int SSH_MSG_USERAUTH_GSSAPI_ERRTOK = 65;
    private static final int SSH_MSG_USERAUTH_GSSAPI_MIC = 66;
    private static final byte[][] supported_oid = new byte[][]{{6, 9, 42, -122, 72, -122, -9, 18, 1, 2, 2}};
    private static final String[] supported_method = new String[]{"gssapi-with-mic.krb5"};

    public boolean start(Session session) throws Exception {
        int command;
        String method;
        byte[] _username;
        block22: {
            super.start(session);
            _username = Util.str2byte(this.username);
            this.packet.reset();
            this.buf.putByte((byte)50);
            this.buf.putString(_username);
            this.buf.putString(Util.str2byte("ssh-connection"));
            this.buf.putString(Util.str2byte("gssapi-with-mic"));
            this.buf.putInt(supported_oid.length);
            for (int i = 0; i < supported_oid.length; ++i) {
                this.buf.putString(supported_oid[i]);
            }
            session.write(this.packet);
            method = null;
            while (true) {
                this.buf = session.read(this.buf);
                command = this.buf.getCommand() & 0xFF;
                if (command == 51) {
                    return false;
                }
                if (command == 60) {
                    this.buf.getInt();
                    this.buf.getByte();
                    this.buf.getByte();
                    byte[] message = this.buf.getString();
                    for (int i = 0; i < supported_oid.length; ++i) {
                        if (!Util.array_equals(message, supported_oid[i])) continue;
                        method = supported_method[i];
                        break;
                    }
                    if (method == null) {
                        return false;
                    }
                    break block22;
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
            return false;
        }
        GSSContext context = null;
        try {
            Class<?> c = Class.forName(session.getConfig(method));
            context = (GSSContext)c.newInstance();
        }
        catch (Exception e) {
            return false;
        }
        try {
            context.create(this.username, session.host);
        }
        catch (JSchException e) {
            return false;
        }
        byte[] token = new byte[]{};
        while (!context.isEstablished()) {
            try {
                token = context.init(token, 0, token.length);
            }
            catch (JSchException e) {
                return false;
            }
            if (token != null) {
                this.packet.reset();
                this.buf.putByte((byte)61);
                this.buf.putString(token);
                session.write(this.packet);
            }
            if (context.isEstablished()) continue;
            this.buf = session.read(this.buf);
            command = this.buf.getCommand() & 0xFF;
            if (command == 64) {
                this.buf = session.read(this.buf);
                command = this.buf.getCommand() & 0xFF;
            } else if (command == 65) {
                this.buf = session.read(this.buf);
                command = this.buf.getCommand() & 0xFF;
            }
            if (command == 51) {
                return false;
            }
            this.buf.getInt();
            this.buf.getByte();
            this.buf.getByte();
            token = this.buf.getString();
        }
        Buffer mbuf = new Buffer();
        mbuf.putString(session.getSessionId());
        mbuf.putByte((byte)50);
        mbuf.putString(_username);
        mbuf.putString(Util.str2byte("ssh-connection"));
        mbuf.putString(Util.str2byte("gssapi-with-mic"));
        byte[] mic = context.getMIC(mbuf.buffer, 0, mbuf.getLength());
        if (mic == null) {
            return false;
        }
        this.packet.reset();
        this.buf.putByte((byte)66);
        this.buf.putString(mic);
        session.write(this.packet);
        context.dispose();
        this.buf = session.read(this.buf);
        command = this.buf.getCommand() & 0xFF;
        if (command == 52) {
            return true;
        }
        if (command == 51) {
            this.buf.getInt();
            this.buf.getByte();
            this.buf.getByte();
            byte[] foo = this.buf.getString();
            int partial_success = this.buf.getByte();
            if (partial_success != 0) {
                throw new JSchPartialAuthException(Util.byte2str(foo));
            }
        }
        return false;
    }
}


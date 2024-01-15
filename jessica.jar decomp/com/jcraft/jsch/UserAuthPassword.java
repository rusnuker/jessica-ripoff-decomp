/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.JSchAuthCancelException;
import com.jcraft.jsch.JSchPartialAuthException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserAuth;
import com.jcraft.jsch.Util;

class UserAuthPassword
extends UserAuth {
    private final int SSH_MSG_USERAUTH_PASSWD_CHANGEREQ = 60;

    UserAuthPassword() {
    }

    public boolean start(Session session) throws Exception {
        super.start(session);
        byte[] password = session.password;
        String dest = this.username + "@" + session.host;
        if (session.port != 22) {
            dest = dest + ":" + session.port;
        }
        try {
            while (true) {
                int command;
                if (session.auth_failures >= session.max_auth_tries) {
                    boolean bl = false;
                    return bl;
                }
                if (password == null) {
                    if (this.userinfo == null) {
                        boolean bl = false;
                        return bl;
                    }
                    if (!this.userinfo.promptPassword("Password for " + dest)) {
                        throw new JSchAuthCancelException("password");
                    }
                    String _password = this.userinfo.getPassword();
                    if (_password == null) {
                        throw new JSchAuthCancelException("password");
                    }
                    password = Util.str2byte(_password);
                }
                byte[] _username = null;
                _username = Util.str2byte(this.username);
                this.packet.reset();
                this.buf.putByte((byte)50);
                this.buf.putString(_username);
                this.buf.putString(Util.str2byte("ssh-connection"));
                this.buf.putString(Util.str2byte("password"));
                this.buf.putByte((byte)0);
                this.buf.putString(password);
                session.write(this.packet);
                while (true) {
                    this.buf = session.read(this.buf);
                    command = this.buf.getCommand() & 0xFF;
                    if (command == 52) {
                        boolean bl = true;
                        return bl;
                    }
                    if (command == 53) {
                        this.buf.getInt();
                        this.buf.getByte();
                        this.buf.getByte();
                        byte[] _message = this.buf.getString();
                        byte[] lang = this.buf.getString();
                        String message = Util.byte2str(_message);
                        if (this.userinfo == null) continue;
                        this.userinfo.showMessage(message);
                        continue;
                    }
                    if (command != 60) break;
                    this.buf.getInt();
                    this.buf.getByte();
                    this.buf.getByte();
                    byte[] instruction = this.buf.getString();
                    byte[] tag = this.buf.getString();
                    if (this.userinfo == null || !(this.userinfo instanceof UIKeyboardInteractive)) {
                        if (this.userinfo != null) {
                            this.userinfo.showMessage("Password must be changed.");
                        }
                        boolean message = false;
                        return message;
                    }
                    UIKeyboardInteractive kbi = (UIKeyboardInteractive)((Object)this.userinfo);
                    String name = "Password Change Required";
                    String[] prompt = new String[]{"New Password: "};
                    boolean[] echo = new boolean[]{false};
                    String[] response = kbi.promptKeyboardInteractive(dest, name, Util.byte2str(instruction), prompt, echo);
                    if (response == null) {
                        throw new JSchAuthCancelException("password");
                    }
                    byte[] newpassword = Util.str2byte(response[0]);
                    this.packet.reset();
                    this.buf.putByte((byte)50);
                    this.buf.putString(_username);
                    this.buf.putString(Util.str2byte("ssh-connection"));
                    this.buf.putString(Util.str2byte("password"));
                    this.buf.putByte((byte)1);
                    this.buf.putString(password);
                    this.buf.putString(newpassword);
                    Util.bzero(newpassword);
                    response = null;
                    session.write(this.packet);
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
                    ++session.auth_failures;
                } else {
                    boolean bl = false;
                    return bl;
                }
                if (password == null) continue;
                Util.bzero(password);
                password = null;
            }
        }
        finally {
            if (password != null) {
                Util.bzero(password);
                password = null;
            }
        }
    }
}


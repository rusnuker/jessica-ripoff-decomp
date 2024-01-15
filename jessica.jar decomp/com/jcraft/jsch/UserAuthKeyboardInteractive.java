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

class UserAuthKeyboardInteractive
extends UserAuth {
    UserAuthKeyboardInteractive() {
    }

    public boolean start(Session session) throws Exception {
        super.start(session);
        if (this.userinfo != null && !(this.userinfo instanceof UIKeyboardInteractive)) {
            return false;
        }
        String dest = this.username + "@" + session.host;
        if (session.port != 22) {
            dest = dest + ":" + session.port;
        }
        byte[] password = session.password;
        boolean cancel = false;
        byte[] _username = null;
        _username = Util.str2byte(this.username);
        block0: do {
            if (session.auth_failures >= session.max_auth_tries) {
                return false;
            }
            this.packet.reset();
            this.buf.putByte((byte)50);
            this.buf.putString(_username);
            this.buf.putString(Util.str2byte("ssh-connection"));
            this.buf.putString(Util.str2byte("keyboard-interactive"));
            this.buf.putString(Util.empty);
            this.buf.putString(Util.empty);
            session.write(this.packet);
            boolean firsttime = true;
            while (true) {
                UIKeyboardInteractive kbi;
                String[] _response;
                this.buf = session.read(this.buf);
                int command = this.buf.getCommand() & 0xFF;
                if (command == 52) {
                    return true;
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
                if (command == 51) {
                    this.buf.getInt();
                    this.buf.getByte();
                    this.buf.getByte();
                    byte[] foo = this.buf.getString();
                    int partial_success = this.buf.getByte();
                    if (partial_success != 0) {
                        throw new JSchPartialAuthException(Util.byte2str(foo));
                    }
                    if (firsttime) {
                        return false;
                    }
                    ++session.auth_failures;
                    continue block0;
                }
                if (command != 60) break;
                firsttime = false;
                this.buf.getInt();
                this.buf.getByte();
                this.buf.getByte();
                String name = Util.byte2str(this.buf.getString());
                String instruction = Util.byte2str(this.buf.getString());
                String languate_tag = Util.byte2str(this.buf.getString());
                int num = this.buf.getInt();
                String[] prompt = new String[num];
                boolean[] echo = new boolean[num];
                for (int i = 0; i < num; ++i) {
                    prompt[i] = Util.byte2str(this.buf.getString());
                    echo[i] = this.buf.getByte() != 0;
                }
                Object response = null;
                if (password != null && prompt.length == 1 && !echo[0] && prompt[0].toLowerCase().indexOf("password:") >= 0) {
                    response = new byte[1][];
                    response[0] = password;
                    password = null;
                } else if ((num > 0 || name.length() > 0 || instruction.length() > 0) && this.userinfo != null && (_response = (kbi = (UIKeyboardInteractive)((Object)this.userinfo)).promptKeyboardInteractive(dest, name, instruction, prompt, echo)) != null) {
                    response = new byte[_response.length][];
                    for (int i = 0; i < _response.length; ++i) {
                        response[i] = Util.str2byte(_response[i]);
                    }
                }
                this.packet.reset();
                this.buf.putByte((byte)61);
                if (num > 0 && (response == null || num != ((byte[][])response).length)) {
                    if (response == null) {
                        this.buf.putInt(num);
                        for (int i = 0; i < num; ++i) {
                            this.buf.putString(Util.empty);
                        }
                    } else {
                        this.buf.putInt(0);
                    }
                    if (response == null) {
                        cancel = true;
                    }
                } else {
                    this.buf.putInt(num);
                    for (int i = 0; i < num; ++i) {
                        this.buf.putString(response[i]);
                    }
                }
                session.write(this.packet);
            }
            return false;
        } while (!cancel);
        throw new JSchAuthCancelException("keyboard-interactive");
    }
}


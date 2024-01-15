/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.Packet;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public abstract class UserAuth {
    protected static final int SSH_MSG_USERAUTH_REQUEST = 50;
    protected static final int SSH_MSG_USERAUTH_FAILURE = 51;
    protected static final int SSH_MSG_USERAUTH_SUCCESS = 52;
    protected static final int SSH_MSG_USERAUTH_BANNER = 53;
    protected static final int SSH_MSG_USERAUTH_INFO_REQUEST = 60;
    protected static final int SSH_MSG_USERAUTH_INFO_RESPONSE = 61;
    protected static final int SSH_MSG_USERAUTH_PK_OK = 60;
    protected UserInfo userinfo;
    protected Packet packet;
    protected Buffer buf;
    protected String username;

    public boolean start(Session session) throws Exception {
        this.userinfo = session.getUserInfo();
        this.packet = session.packet;
        this.buf = this.packet.getBuffer();
        this.username = session.getUserName();
        return true;
    }
}


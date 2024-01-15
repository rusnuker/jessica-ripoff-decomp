/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.ChannelForwardedTCPIP;
import java.io.InputStream;
import java.io.OutputStream;

public interface ForwardedTCPIPDaemon
extends Runnable {
    public void setChannel(ChannelForwardedTCPIP var1, InputStream var2, OutputStream var3);

    public void setArg(Object[] var1);
}


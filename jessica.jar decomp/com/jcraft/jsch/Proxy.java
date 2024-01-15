/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.SocketFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public interface Proxy {
    public void connect(SocketFactory var1, String var2, int var3, int var4) throws Exception;

    public InputStream getInputStream();

    public OutputStream getOutputStream();

    public Socket getSocket();

    public void close();
}


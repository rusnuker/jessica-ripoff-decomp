/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public interface SocketFactory {
    public Socket createSocket(String var1, int var2) throws IOException, UnknownHostException;

    public InputStream getInputStream(Socket var1) throws IOException;

    public OutputStream getOutputStream(Socket var1) throws IOException;
}


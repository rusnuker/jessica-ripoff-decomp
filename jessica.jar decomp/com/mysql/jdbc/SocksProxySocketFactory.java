/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.StandardSocketFactory;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.Properties;

public class SocksProxySocketFactory
extends StandardSocketFactory {
    public static int SOCKS_DEFAULT_PORT = 1080;

    protected Socket createSocket(Properties props) {
        String socksProxyHost = props.getProperty("socksProxyHost");
        String socksProxyPortString = props.getProperty("socksProxyPort", String.valueOf(SOCKS_DEFAULT_PORT));
        int socksProxyPort = SOCKS_DEFAULT_PORT;
        try {
            socksProxyPort = Integer.valueOf(socksProxyPortString);
        }
        catch (NumberFormatException ex) {
            // empty catch block
        }
        return new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socksProxyHost, socksProxyPort)));
    }
}


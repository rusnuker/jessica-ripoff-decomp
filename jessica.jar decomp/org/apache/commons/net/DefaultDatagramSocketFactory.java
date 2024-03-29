/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import org.apache.commons.net.DatagramSocketFactory;

public class DefaultDatagramSocketFactory
implements DatagramSocketFactory {
    public DatagramSocket createDatagramSocket() throws SocketException {
        return new DatagramSocket();
    }

    public DatagramSocket createDatagramSocket(int port) throws SocketException {
        return new DatagramSocket(port);
    }

    public DatagramSocket createDatagramSocket(int port, InetAddress laddr) throws SocketException {
        return new DatagramSocket(port, laddr);
    }
}


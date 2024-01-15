/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.ChannelDirectTCPIP;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ServerSocketFactory;
import com.jcraft.jsch.Session;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

class PortWatcher
implements Runnable {
    private static Vector pool = new Vector();
    private static InetAddress anyLocalAddress = null;
    Session session;
    int lport;
    int rport;
    String host;
    InetAddress boundaddress;
    Runnable thread;
    ServerSocket ss;
    int connectTimeout = 0;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static String[] getPortForwarding(Session session) {
        int i;
        Vector<String> foo = new Vector<String>();
        Vector vector = pool;
        synchronized (vector) {
            for (i = 0; i < pool.size(); ++i) {
                PortWatcher p = (PortWatcher)pool.elementAt(i);
                if (p.session != session) continue;
                foo.addElement(p.lport + ":" + p.host + ":" + p.rport);
            }
        }
        String[] bar = new String[foo.size()];
        for (i = 0; i < foo.size(); ++i) {
            bar[i] = (String)foo.elementAt(i);
        }
        return bar;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static PortWatcher getPort(Session session, String address, int lport) throws JSchException {
        InetAddress addr;
        try {
            addr = InetAddress.getByName(address);
        }
        catch (UnknownHostException uhe) {
            throw new JSchException("PortForwardingL: invalid address " + address + " specified.", uhe);
        }
        Vector vector = pool;
        synchronized (vector) {
            for (int i = 0; i < pool.size(); ++i) {
                PortWatcher p = (PortWatcher)pool.elementAt(i);
                if (p.session != session || p.lport != lport || (anyLocalAddress == null || !p.boundaddress.equals(anyLocalAddress)) && !p.boundaddress.equals(addr)) continue;
                return p;
            }
            return null;
        }
    }

    private static String normalize(String address) {
        if (address != null) {
            if (address.length() == 0 || address.equals("*")) {
                address = "0.0.0.0";
            } else if (address.equals("localhost")) {
                address = "127.0.0.1";
            }
        }
        return address;
    }

    static PortWatcher addPort(Session session, String address, int lport, String host, int rport, ServerSocketFactory ssf) throws JSchException {
        if (PortWatcher.getPort(session, address = PortWatcher.normalize(address), lport) != null) {
            throw new JSchException("PortForwardingL: local port " + address + ":" + lport + " is already registered.");
        }
        PortWatcher pw = new PortWatcher(session, address, lport, host, rport, ssf);
        pool.addElement(pw);
        return pw;
    }

    static void delPort(Session session, String address, int lport) throws JSchException {
        PortWatcher pw = PortWatcher.getPort(session, address = PortWatcher.normalize(address), lport);
        if (pw == null) {
            throw new JSchException("PortForwardingL: local port " + address + ":" + lport + " is not registered.");
        }
        pw.delete();
        pool.removeElement(pw);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void delPort(Session session) {
        Vector vector = pool;
        synchronized (vector) {
            PortWatcher p;
            int i;
            PortWatcher[] foo = new PortWatcher[pool.size()];
            int count = 0;
            for (i = 0; i < pool.size(); ++i) {
                p = (PortWatcher)pool.elementAt(i);
                if (p.session != session) continue;
                p.delete();
                foo[count++] = p;
            }
            for (i = 0; i < count; ++i) {
                p = foo[i];
                pool.removeElement(p);
            }
        }
    }

    PortWatcher(Session session, String address, int lport, String host, int rport, ServerSocketFactory factory) throws JSchException {
        int assigned;
        this.session = session;
        this.lport = lport;
        this.host = host;
        this.rport = rport;
        try {
            this.boundaddress = InetAddress.getByName(address);
            this.ss = factory == null ? new ServerSocket(lport, 0, this.boundaddress) : factory.createServerSocket(lport, 0, this.boundaddress);
        }
        catch (Exception e) {
            String message = "PortForwardingL: local port " + address + ":" + lport + " cannot be bound.";
            if (e instanceof Throwable) {
                throw new JSchException(message, e);
            }
            throw new JSchException(message);
        }
        if (lport == 0 && (assigned = this.ss.getLocalPort()) != -1) {
            this.lport = assigned;
        }
    }

    public void run() {
        this.thread = this;
        try {
            while (this.thread != null) {
                Socket socket = this.ss.accept();
                socket.setTcpNoDelay(true);
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                ChannelDirectTCPIP channel = new ChannelDirectTCPIP();
                channel.init();
                channel.setInputStream(in);
                channel.setOutputStream(out);
                this.session.addChannel(channel);
                channel.setHost(this.host);
                channel.setPort(this.rport);
                channel.setOrgIPAddress(socket.getInetAddress().getHostAddress());
                channel.setOrgPort(socket.getPort());
                channel.connect(this.connectTimeout);
                if (channel.exitstatus == -1) continue;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.delete();
    }

    void delete() {
        this.thread = null;
        try {
            if (this.ss != null) {
                this.ss.close();
            }
            this.ss = null;
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    static {
        try {
            anyLocalAddress = InetAddress.getByName("0.0.0.0");
        }
        catch (UnknownHostException unknownHostException) {
            // empty catch block
        }
    }
}


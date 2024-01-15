/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ForwardedTCPIPDaemon;
import com.jcraft.jsch.IO;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Packet;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SocketFactory;
import com.jcraft.jsch.Util;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.util.Vector;

public class ChannelForwardedTCPIP
extends Channel {
    private static Vector pool = new Vector();
    private static final int LOCAL_WINDOW_SIZE_MAX = 131072;
    private static final int LOCAL_MAXIMUM_PACKET_SIZE = 16384;
    private static final int TIMEOUT = 10000;
    private Socket socket = null;
    private ForwardedTCPIPDaemon daemon = null;
    private Config config = null;

    ChannelForwardedTCPIP() {
        this.setLocalWindowSizeMax(131072);
        this.setLocalWindowSize(131072);
        this.setLocalPacketSize(16384);
        this.io = new IO();
        this.connected = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void run() {
        try {
            Config _config;
            if (this.config instanceof ConfigDaemon) {
                _config = (ConfigDaemon)this.config;
                Class<?> c = Class.forName(_config.target);
                this.daemon = (ForwardedTCPIPDaemon)c.newInstance();
                PipedOutputStream out = new PipedOutputStream();
                this.io.setInputStream(new Channel.PassiveInputStream((Channel)this, out, 32768), false);
                this.daemon.setChannel(this, this.getInputStream(), out);
                this.daemon.setArg(_config.arg);
                new Thread(this.daemon).start();
            } else {
                _config = (ConfigLHost)this.config;
                this.socket = ((ConfigLHost)_config).factory == null ? Util.createSocket(((ConfigLHost)_config).target, ((ConfigLHost)_config).lport, 10000) : ((ConfigLHost)_config).factory.createSocket(((ConfigLHost)_config).target, ((ConfigLHost)_config).lport);
                this.socket.setTcpNoDelay(true);
                this.io.setInputStream(this.socket.getInputStream());
                this.io.setOutputStream(this.socket.getOutputStream());
            }
            this.sendOpenConfirmation();
        }
        catch (Exception e) {
            this.sendOpenFailure(1);
            this.close = true;
            this.disconnect();
            return;
        }
        this.thread = Thread.currentThread();
        Buffer buf = new Buffer(this.rmpsize);
        Packet packet = new Packet(buf);
        int i = 0;
        try {
            Session _session = this.getSession();
            while (this.thread != null && this.io != null && this.io.in != null) {
                i = this.io.in.read(buf.buffer, 14, buf.buffer.length - 14 - 128);
                if (i <= 0) {
                    this.eof();
                    break;
                }
                packet.reset();
                buf.putByte((byte)94);
                buf.putInt(this.recipient);
                buf.putInt(i);
                buf.skip(i);
                ChannelForwardedTCPIP channelForwardedTCPIP = this;
                synchronized (channelForwardedTCPIP) {
                    if (this.close) {
                        break;
                    }
                    _session.write(packet, this, i);
                }
            }
        }
        catch (Exception e) {
            // empty catch block
        }
        this.disconnect();
    }

    void getData(Buffer buf) {
        this.setRecipient(buf.getInt());
        this.setRemoteWindowSize(buf.getUInt());
        this.setRemotePacketSize(buf.getInt());
        byte[] addr = buf.getString();
        int port = buf.getInt();
        byte[] orgaddr = buf.getString();
        int orgport = buf.getInt();
        Session _session = null;
        try {
            _session = this.getSession();
        }
        catch (JSchException e) {
            // empty catch block
        }
        this.config = ChannelForwardedTCPIP.getPort(_session, Util.byte2str(addr), port);
        if (this.config == null) {
            this.config = ChannelForwardedTCPIP.getPort(_session, null, port);
        }
        if (this.config == null && JSch.getLogger().isEnabled(3)) {
            JSch.getLogger().log(3, "ChannelForwardedTCPIP: " + Util.byte2str(addr) + ":" + port + " is not registered.");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Config getPort(Session session, String address_to_bind, int rport) {
        Vector vector = pool;
        synchronized (vector) {
            for (int i = 0; i < pool.size(); ++i) {
                Config bar = (Config)pool.elementAt(i);
                if (bar.session != session || bar.rport != rport && (bar.rport != 0 || bar.allocated_rport != rport) || address_to_bind != null && !bar.address_to_bind.equals(address_to_bind)) continue;
                return bar;
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static String[] getPortForwarding(Session session) {
        int i;
        Vector<String> foo = new Vector<String>();
        Vector vector = pool;
        synchronized (vector) {
            for (i = 0; i < pool.size(); ++i) {
                Config config = (Config)pool.elementAt(i);
                if (config instanceof ConfigDaemon) {
                    foo.addElement(config.allocated_rport + ":" + config.target + ":");
                    continue;
                }
                foo.addElement(config.allocated_rport + ":" + config.target + ":" + ((ConfigLHost)config).lport);
            }
        }
        String[] bar = new String[foo.size()];
        for (i = 0; i < foo.size(); ++i) {
            bar[i] = (String)foo.elementAt(i);
        }
        return bar;
    }

    static String normalize(String address) {
        if (address == null) {
            return "localhost";
        }
        if (address.length() == 0 || address.equals("*")) {
            return "";
        }
        return address;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void addPort(Session session, String _address_to_bind, int port, int allocated_port, String target, int lport, SocketFactory factory) throws JSchException {
        String address_to_bind = ChannelForwardedTCPIP.normalize(_address_to_bind);
        Vector vector = pool;
        synchronized (vector) {
            if (ChannelForwardedTCPIP.getPort(session, address_to_bind, port) != null) {
                throw new JSchException("PortForwardingR: remote port " + port + " is already registered.");
            }
            ConfigLHost config = new ConfigLHost();
            config.session = session;
            config.rport = port;
            config.allocated_rport = allocated_port;
            config.target = target;
            config.lport = lport;
            config.address_to_bind = address_to_bind;
            config.factory = factory;
            pool.addElement(config);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void addPort(Session session, String _address_to_bind, int port, int allocated_port, String daemon, Object[] arg) throws JSchException {
        String address_to_bind = ChannelForwardedTCPIP.normalize(_address_to_bind);
        Vector vector = pool;
        synchronized (vector) {
            if (ChannelForwardedTCPIP.getPort(session, address_to_bind, port) != null) {
                throw new JSchException("PortForwardingR: remote port " + port + " is already registered.");
            }
            ConfigDaemon config = new ConfigDaemon();
            config.session = session;
            config.rport = port;
            config.allocated_rport = port;
            config.target = daemon;
            config.arg = arg;
            config.address_to_bind = address_to_bind;
            pool.addElement(config);
        }
    }

    static void delPort(ChannelForwardedTCPIP c) {
        Session _session = null;
        try {
            _session = c.getSession();
        }
        catch (JSchException jSchException) {
            // empty catch block
        }
        if (_session != null && c.config != null) {
            ChannelForwardedTCPIP.delPort(_session, c.config.rport);
        }
    }

    static void delPort(Session session, int rport) {
        ChannelForwardedTCPIP.delPort(session, null, rport);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void delPort(Session session, String address_to_bind, int rport) {
        Vector vector = pool;
        synchronized (vector) {
            Config foo = ChannelForwardedTCPIP.getPort(session, ChannelForwardedTCPIP.normalize(address_to_bind), rport);
            if (foo == null) {
                foo = ChannelForwardedTCPIP.getPort(session, null, rport);
            }
            if (foo == null) {
                return;
            }
            pool.removeElement(foo);
            if (address_to_bind == null) {
                address_to_bind = foo.address_to_bind;
            }
            if (address_to_bind == null) {
                address_to_bind = "0.0.0.0";
            }
        }
        Buffer buf = new Buffer(100);
        Packet packet = new Packet(buf);
        try {
            packet.reset();
            buf.putByte((byte)80);
            buf.putString(Util.str2byte("cancel-tcpip-forward"));
            buf.putByte((byte)0);
            buf.putString(Util.str2byte(address_to_bind));
            buf.putInt(rport);
            session.write(packet);
        }
        catch (Exception e) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void delPort(Session session) {
        int[] rport = null;
        int count = 0;
        Vector vector = pool;
        synchronized (vector) {
            rport = new int[pool.size()];
            for (int i = 0; i < pool.size(); ++i) {
                Config config = (Config)pool.elementAt(i);
                if (config.session != session) continue;
                rport[count++] = config.rport;
            }
        }
        for (int i = 0; i < count; ++i) {
            ChannelForwardedTCPIP.delPort(session, rport[i]);
        }
    }

    public int getRemotePort() {
        return this.config != null ? this.config.rport : 0;
    }

    private void setSocketFactory(SocketFactory factory) {
        if (this.config != null && this.config instanceof ConfigLHost) {
            ((ConfigLHost)this.config).factory = factory;
        }
    }

    static class ConfigLHost
    extends Config {
        int lport;
        SocketFactory factory;

        ConfigLHost() {
        }
    }

    static class ConfigDaemon
    extends Config {
        Object[] arg;

        ConfigDaemon() {
        }
    }

    static abstract class Config {
        Session session;
        int rport;
        int allocated_rport;
        String address_to_bind;
        String target;

        Config() {
        }
    }
}


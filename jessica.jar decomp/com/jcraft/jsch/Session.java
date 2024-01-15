/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelDirectTCPIP;
import com.jcraft.jsch.ChannelForwardedTCPIP;
import com.jcraft.jsch.ChannelSession;
import com.jcraft.jsch.ChannelX11;
import com.jcraft.jsch.Cipher;
import com.jcraft.jsch.Compression;
import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.HASH;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.IO;
import com.jcraft.jsch.IdentityFile;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchAuthCancelException;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.JSchPartialAuthException;
import com.jcraft.jsch.KeyExchange;
import com.jcraft.jsch.KnownHosts;
import com.jcraft.jsch.MAC;
import com.jcraft.jsch.Packet;
import com.jcraft.jsch.PortWatcher;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.Random;
import com.jcraft.jsch.ServerSocketFactory;
import com.jcraft.jsch.Signature;
import com.jcraft.jsch.SocketFactory;
import com.jcraft.jsch.UserAuth;
import com.jcraft.jsch.UserAuthNone;
import com.jcraft.jsch.UserInfo;
import com.jcraft.jsch.Util;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

public class Session
implements Runnable {
    static final int SSH_MSG_DISCONNECT = 1;
    static final int SSH_MSG_IGNORE = 2;
    static final int SSH_MSG_UNIMPLEMENTED = 3;
    static final int SSH_MSG_DEBUG = 4;
    static final int SSH_MSG_SERVICE_REQUEST = 5;
    static final int SSH_MSG_SERVICE_ACCEPT = 6;
    static final int SSH_MSG_KEXINIT = 20;
    static final int SSH_MSG_NEWKEYS = 21;
    static final int SSH_MSG_KEXDH_INIT = 30;
    static final int SSH_MSG_KEXDH_REPLY = 31;
    static final int SSH_MSG_KEX_DH_GEX_GROUP = 31;
    static final int SSH_MSG_KEX_DH_GEX_INIT = 32;
    static final int SSH_MSG_KEX_DH_GEX_REPLY = 33;
    static final int SSH_MSG_KEX_DH_GEX_REQUEST = 34;
    static final int SSH_MSG_GLOBAL_REQUEST = 80;
    static final int SSH_MSG_REQUEST_SUCCESS = 81;
    static final int SSH_MSG_REQUEST_FAILURE = 82;
    static final int SSH_MSG_CHANNEL_OPEN = 90;
    static final int SSH_MSG_CHANNEL_OPEN_CONFIRMATION = 91;
    static final int SSH_MSG_CHANNEL_OPEN_FAILURE = 92;
    static final int SSH_MSG_CHANNEL_WINDOW_ADJUST = 93;
    static final int SSH_MSG_CHANNEL_DATA = 94;
    static final int SSH_MSG_CHANNEL_EXTENDED_DATA = 95;
    static final int SSH_MSG_CHANNEL_EOF = 96;
    static final int SSH_MSG_CHANNEL_CLOSE = 97;
    static final int SSH_MSG_CHANNEL_REQUEST = 98;
    static final int SSH_MSG_CHANNEL_SUCCESS = 99;
    static final int SSH_MSG_CHANNEL_FAILURE = 100;
    private static final int PACKET_MAX_SIZE = 262144;
    private byte[] V_S;
    private byte[] V_C = Util.str2byte("SSH-2.0-JSCH-0.1.54");
    private byte[] I_C;
    private byte[] I_S;
    private byte[] K_S;
    private byte[] session_id;
    private byte[] IVc2s;
    private byte[] IVs2c;
    private byte[] Ec2s;
    private byte[] Es2c;
    private byte[] MACc2s;
    private byte[] MACs2c;
    private int seqi = 0;
    private int seqo = 0;
    String[] guess = null;
    private Cipher s2ccipher;
    private Cipher c2scipher;
    private MAC s2cmac;
    private MAC c2smac;
    private byte[] s2cmac_result1;
    private byte[] s2cmac_result2;
    private Compression deflater;
    private Compression inflater;
    private IO io;
    private Socket socket;
    private int timeout = 0;
    private volatile boolean isConnected = false;
    private boolean isAuthed = false;
    private Thread connectThread = null;
    private Object lock = new Object();
    boolean x11_forwarding = false;
    boolean agent_forwarding = false;
    InputStream in = null;
    OutputStream out = null;
    static Random random;
    Buffer buf;
    Packet packet;
    SocketFactory socket_factory = null;
    static final int buffer_margin = 128;
    private Hashtable config = null;
    private Proxy proxy = null;
    private UserInfo userinfo;
    private String hostKeyAlias = null;
    private int serverAliveInterval = 0;
    private int serverAliveCountMax = 1;
    private IdentityRepository identityRepository = null;
    private HostKeyRepository hostkeyRepository = null;
    protected boolean daemon_thread = false;
    private long kex_start_time = 0L;
    int max_auth_tries = 6;
    int auth_failures = 0;
    String host = "127.0.0.1";
    String org_host = "127.0.0.1";
    int port = 22;
    String username = null;
    byte[] password = null;
    JSch jsch;
    private volatile boolean in_kex = false;
    private volatile boolean in_prompt = false;
    int[] uncompress_len = new int[1];
    int[] compress_len = new int[1];
    private int s2ccipher_size = 8;
    private int c2scipher_size = 8;
    Runnable thread;
    private GlobalRequestReply grr = new GlobalRequestReply();
    private static final byte[] keepalivemsg;
    private static final byte[] nomoresessions;
    private HostKey hostkey = null;

    Session(JSch jsch, String username, String host, int port) throws JSchException {
        this.jsch = jsch;
        this.buf = new Buffer();
        this.packet = new Packet(this.buf);
        this.username = username;
        this.org_host = this.host = host;
        this.port = port;
        this.applyConfig();
        if (this.username == null) {
            try {
                this.username = (String)System.getProperties().get("user.name");
            }
            catch (SecurityException e) {
                // empty catch block
            }
        }
        if (this.username == null) {
            throw new JSchException("username is not given.");
        }
    }

    public void connect() throws JSchException {
        this.connect(this.timeout);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void connect(int connectTimeout) throws JSchException {
        if (this.isConnected) {
            throw new JSchException("session is already connected");
        }
        this.io = new IO();
        if (random == null) {
            try {
                Class<?> c = Class.forName(this.getConfig("random"));
                random = (Random)c.newInstance();
            }
            catch (Exception e) {
                throw new JSchException(e.toString(), e);
            }
        }
        Packet.setRandom(random);
        if (JSch.getLogger().isEnabled(1)) {
            JSch.getLogger().log(1, "Connecting to " + this.host + " port " + this.port);
        }
        try {
            int i;
            Object in;
            if (this.proxy == null) {
                OutputStream out;
                if (this.socket_factory == null) {
                    this.socket = Util.createSocket(this.host, this.port, connectTimeout);
                    in = this.socket.getInputStream();
                    out = this.socket.getOutputStream();
                } else {
                    this.socket = this.socket_factory.createSocket(this.host, this.port);
                    in = this.socket_factory.getInputStream(this.socket);
                    out = this.socket_factory.getOutputStream(this.socket);
                }
                this.socket.setTcpNoDelay(true);
                this.io.setInputStream((InputStream)in);
                this.io.setOutputStream(out);
            } else {
                in = this.proxy;
                synchronized (in) {
                    this.proxy.connect(this.socket_factory, this.host, this.port, connectTimeout);
                    this.io.setInputStream(this.proxy.getInputStream());
                    this.io.setOutputStream(this.proxy.getOutputStream());
                    this.socket = this.proxy.getSocket();
                }
            }
            if (connectTimeout > 0 && this.socket != null) {
                this.socket.setSoTimeout(connectTimeout);
            }
            this.isConnected = true;
            if (JSch.getLogger().isEnabled(1)) {
                JSch.getLogger().log(1, "Connection established");
            }
            this.jsch.addSession(this);
            byte[] foo = new byte[this.V_C.length + 1];
            System.arraycopy(this.V_C, 0, foo, 0, this.V_C.length);
            foo[foo.length - 1] = 10;
            this.io.put(foo, 0, foo.length);
            do {
                int j = 0;
                for (i = 0; i < this.buf.buffer.length && (j = this.io.getByte()) >= 0; ++i) {
                    this.buf.buffer[i] = (byte)j;
                    if (j != 10) continue;
                }
                if (j < 0) {
                    throw new JSchException("connection is closed by foreign host");
                }
                if (this.buf.buffer[i - 1] != 10 || --i <= 0 || this.buf.buffer[i - 1] != 13) continue;
                --i;
            } while (i <= 3 || i != this.buf.buffer.length && (this.buf.buffer[0] != 83 || this.buf.buffer[1] != 83 || this.buf.buffer[2] != 72 || this.buf.buffer[3] != 45));
            if (i == this.buf.buffer.length || i < 7 || this.buf.buffer[4] == 49 && this.buf.buffer[6] != 57) {
                throw new JSchException("invalid server's version string");
            }
            this.V_S = new byte[i];
            System.arraycopy(this.buf.buffer, 0, this.V_S, 0, i);
            if (JSch.getLogger().isEnabled(1)) {
                JSch.getLogger().log(1, "Remote version string: " + Util.byte2str(this.V_S));
                JSch.getLogger().log(1, "Local version string: " + Util.byte2str(this.V_C));
            }
            this.send_kexinit();
            this.buf = this.read(this.buf);
            if (this.buf.getCommand() != 20) {
                this.in_kex = false;
                throw new JSchException("invalid protocol: " + this.buf.getCommand());
            }
            if (JSch.getLogger().isEnabled(1)) {
                JSch.getLogger().log(1, "SSH_MSG_KEXINIT received");
            }
            KeyExchange kex = this.receive_kexinit(this.buf);
            do {
                this.buf = this.read(this.buf);
                if (kex.getState() == this.buf.getCommand()) {
                    this.kex_start_time = System.currentTimeMillis();
                    boolean result = kex.next(this.buf);
                    if (result) continue;
                    this.in_kex = false;
                    throw new JSchException("verify: " + result);
                }
                this.in_kex = false;
                throw new JSchException("invalid protocol(kex): " + this.buf.getCommand());
            } while (kex.getState() != 0);
            try {
                long tmp = System.currentTimeMillis();
                this.in_prompt = true;
                this.checkHost(this.host, this.port, kex);
                this.in_prompt = false;
                this.kex_start_time += System.currentTimeMillis() - tmp;
            }
            catch (JSchException ee) {
                this.in_kex = false;
                this.in_prompt = false;
                throw ee;
            }
            this.send_newkeys();
            this.buf = this.read(this.buf);
            if (this.buf.getCommand() == 21) {
                if (JSch.getLogger().isEnabled(1)) {
                    JSch.getLogger().log(1, "SSH_MSG_NEWKEYS received");
                }
            } else {
                this.in_kex = false;
                throw new JSchException("invalid protocol(newkyes): " + this.buf.getCommand());
            }
            this.receive_newkeys(this.buf, kex);
            try {
                String s = this.getConfig("MaxAuthTries");
                if (s != null) {
                    this.max_auth_tries = Integer.parseInt(s);
                }
            }
            catch (NumberFormatException e) {
                throw new JSchException("MaxAuthTries: " + this.getConfig("MaxAuthTries"), e);
            }
            boolean auth = false;
            boolean auth_cancel = false;
            UserAuth ua = null;
            try {
                Class<?> c = Class.forName(this.getConfig("userauth.none"));
                ua = (UserAuth)c.newInstance();
            }
            catch (Exception e) {
                throw new JSchException(e.toString(), e);
            }
            auth = ua.start(this);
            String cmethods = this.getConfig("PreferredAuthentications");
            String[] cmethoda = Util.split(cmethods, ",");
            String smethods = null;
            if (!auth) {
                smethods = ((UserAuthNone)ua).getMethods();
                smethods = smethods != null ? smethods.toLowerCase() : cmethods;
            }
            String[] smethoda = Util.split(smethods, ",");
            int methodi = 0;
            while (!auth && cmethoda != null && methodi < cmethoda.length) {
                String method;
                block69: {
                    method = cmethoda[methodi++];
                    boolean acceptable = false;
                    for (int k = 0; k < smethoda.length; ++k) {
                        if (!smethoda[k].equals(method)) continue;
                        acceptable = true;
                        break;
                    }
                    if (!acceptable) continue;
                    if (JSch.getLogger().isEnabled(1)) {
                        String str = "Authentications that can continue: ";
                        for (int k = methodi - 1; k < cmethoda.length; ++k) {
                            str = str + cmethoda[k];
                            if (k + 1 >= cmethoda.length) continue;
                            str = str + ",";
                        }
                        JSch.getLogger().log(1, str);
                        JSch.getLogger().log(1, "Next authentication method: " + method);
                    }
                    ua = null;
                    try {
                        Class<?> c = null;
                        if (this.getConfig("userauth." + method) != null) {
                            c = Class.forName(this.getConfig("userauth." + method));
                            ua = (UserAuth)c.newInstance();
                        }
                    }
                    catch (Exception e) {
                        if (!JSch.getLogger().isEnabled(2)) break block69;
                        JSch.getLogger().log(2, "failed to load " + method + " method");
                    }
                }
                if (ua == null) continue;
                auth_cancel = false;
                try {
                    auth = ua.start(this);
                    if (!auth || !JSch.getLogger().isEnabled(1)) continue;
                    JSch.getLogger().log(1, "Authentication succeeded (" + method + ").");
                }
                catch (JSchAuthCancelException ee) {
                    auth_cancel = true;
                }
                catch (JSchPartialAuthException ee) {
                    String tmp = smethods;
                    smethods = ee.getMethods();
                    smethoda = Util.split(smethods, ",");
                    if (!tmp.equals(smethods)) {
                        methodi = 0;
                    }
                    auth_cancel = false;
                }
                catch (RuntimeException ee) {
                    throw ee;
                }
                catch (JSchException ee) {
                    throw ee;
                }
                catch (Exception ee) {
                    if (!JSch.getLogger().isEnabled(2)) break;
                    JSch.getLogger().log(2, "an exception during authentication\n" + ee.toString());
                    break;
                }
            }
            if (!auth) {
                if (this.auth_failures >= this.max_auth_tries && JSch.getLogger().isEnabled(1)) {
                    JSch.getLogger().log(1, "Login trials exceeds " + this.max_auth_tries);
                }
                if (auth_cancel) {
                    throw new JSchException("Auth cancel");
                }
                throw new JSchException("Auth fail");
            }
            if (this.socket != null && (connectTimeout > 0 || this.timeout > 0)) {
                this.socket.setSoTimeout(this.timeout);
            }
            this.isAuthed = true;
            Object object = this.lock;
            synchronized (object) {
                if (this.isConnected) {
                    this.connectThread = new Thread(this);
                    this.connectThread.setName("Connect thread " + this.host + " session");
                    if (this.daemon_thread) {
                        this.connectThread.setDaemon(this.daemon_thread);
                    }
                    this.connectThread.start();
                    this.requestPortForwarding();
                }
            }
        }
        catch (Exception e) {
            this.in_kex = false;
            try {
                if (this.isConnected) {
                    String message = e.toString();
                    this.packet.reset();
                    this.buf.checkFreeSize(13 + message.length() + 2 + 128);
                    this.buf.putByte((byte)1);
                    this.buf.putInt(3);
                    this.buf.putString(Util.str2byte(message));
                    this.buf.putString(Util.str2byte("en"));
                    this.write(this.packet);
                }
            }
            catch (Exception ee) {
                // empty catch block
            }
            try {
                this.disconnect();
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.isConnected = false;
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            if (e instanceof JSchException) {
                throw (JSchException)e;
            }
            throw new JSchException("Session.connect: " + e);
        }
        finally {
            Util.bzero(this.password);
            this.password = null;
        }
    }

    private KeyExchange receive_kexinit(Buffer buf) throws Exception {
        int j = buf.getInt();
        if (j != buf.getLength()) {
            buf.getByte();
            this.I_S = new byte[buf.index - 5];
        } else {
            this.I_S = new byte[j - 1 - buf.getByte()];
        }
        System.arraycopy(buf.buffer, buf.s, this.I_S, 0, this.I_S.length);
        if (!this.in_kex) {
            this.send_kexinit();
        }
        this.guess = KeyExchange.guess(this.I_S, this.I_C);
        if (this.guess == null) {
            throw new JSchException("Algorithm negotiation fail");
        }
        if (!this.isAuthed && (this.guess[2].equals("none") || this.guess[3].equals("none"))) {
            throw new JSchException("NONE Cipher should not be chosen before authentification is successed.");
        }
        KeyExchange kex = null;
        try {
            Class<?> c = Class.forName(this.getConfig(this.guess[0]));
            kex = (KeyExchange)c.newInstance();
        }
        catch (Exception e) {
            throw new JSchException(e.toString(), e);
        }
        kex.init(this, this.V_S, this.V_C, this.I_S, this.I_C);
        return kex;
    }

    public void rekey() throws Exception {
        this.send_kexinit();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void send_kexinit() throws Exception {
        if (this.in_kex) {
            return;
        }
        String cipherc2s = this.getConfig("cipher.c2s");
        String ciphers2c = this.getConfig("cipher.s2c");
        String[] not_available_ciphers = this.checkCiphers(this.getConfig("CheckCiphers"));
        if (not_available_ciphers != null && not_available_ciphers.length > 0) {
            cipherc2s = Util.diffString(cipherc2s, not_available_ciphers);
            ciphers2c = Util.diffString(ciphers2c, not_available_ciphers);
            if (cipherc2s == null || ciphers2c == null) {
                throw new JSchException("There are not any available ciphers.");
            }
        }
        String kex = this.getConfig("kex");
        String[] not_available_kexes = this.checkKexes(this.getConfig("CheckKexes"));
        if (not_available_kexes != null && not_available_kexes.length > 0 && (kex = Util.diffString(kex, not_available_kexes)) == null) {
            throw new JSchException("There are not any available kexes.");
        }
        String server_host_key = this.getConfig("server_host_key");
        String[] not_available_shks = this.checkSignatures(this.getConfig("CheckSignatures"));
        if (not_available_shks != null && not_available_shks.length > 0 && (server_host_key = Util.diffString(server_host_key, not_available_shks)) == null) {
            throw new JSchException("There are not any available sig algorithm.");
        }
        this.in_kex = true;
        this.kex_start_time = System.currentTimeMillis();
        Buffer buf = new Buffer();
        Packet packet = new Packet(buf);
        packet.reset();
        buf.putByte((byte)20);
        Random random = Session.random;
        synchronized (random) {
            Session.random.fill(buf.buffer, buf.index, 16);
            buf.skip(16);
        }
        buf.putString(Util.str2byte(kex));
        buf.putString(Util.str2byte(server_host_key));
        buf.putString(Util.str2byte(cipherc2s));
        buf.putString(Util.str2byte(ciphers2c));
        buf.putString(Util.str2byte(this.getConfig("mac.c2s")));
        buf.putString(Util.str2byte(this.getConfig("mac.s2c")));
        buf.putString(Util.str2byte(this.getConfig("compression.c2s")));
        buf.putString(Util.str2byte(this.getConfig("compression.s2c")));
        buf.putString(Util.str2byte(this.getConfig("lang.c2s")));
        buf.putString(Util.str2byte(this.getConfig("lang.s2c")));
        buf.putByte((byte)0);
        buf.putInt(0);
        buf.setOffSet(5);
        this.I_C = new byte[buf.getLength()];
        buf.getByte(this.I_C);
        this.write(packet);
        if (JSch.getLogger().isEnabled(1)) {
            JSch.getLogger().log(1, "SSH_MSG_KEXINIT sent");
        }
    }

    private void send_newkeys() throws Exception {
        this.packet.reset();
        this.buf.putByte((byte)21);
        this.write(this.packet);
        if (JSch.getLogger().isEnabled(1)) {
            JSch.getLogger().log(1, "SSH_MSG_NEWKEYS sent");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void checkHost(String chost, int port, KeyExchange kex) throws JSchException {
        String shkc = this.getConfig("StrictHostKeyChecking");
        if (this.hostKeyAlias != null) {
            chost = this.hostKeyAlias;
        }
        byte[] K_S = kex.getHostKey();
        String key_type = kex.getKeyType();
        String key_fprint = kex.getFingerPrint();
        if (this.hostKeyAlias == null && port != 22) {
            chost = "[" + chost + "]:" + port;
        }
        HostKeyRepository hkr = this.getHostKeyRepository();
        String hkh = this.getConfig("HashKnownHosts");
        this.hostkey = hkh.equals("yes") && hkr instanceof KnownHosts ? ((KnownHosts)hkr).createHashedHostKey(chost, K_S) : new HostKey(chost, K_S);
        int i = 0;
        HostKeyRepository hostKeyRepository = hkr;
        synchronized (hostKeyRepository) {
            i = hkr.check(chost, K_S);
        }
        boolean insert = false;
        if ((shkc.equals("ask") || shkc.equals("yes")) && i == 2) {
            Object message;
            String file = null;
            HostKeyRepository hostKeyRepository2 = hkr;
            synchronized (hostKeyRepository2) {
                file = hkr.getKnownHostsRepositoryID();
            }
            if (file == null) {
                file = "known_hosts";
            }
            boolean b = false;
            if (this.userinfo != null) {
                message = "WARNING: REMOTE HOST IDENTIFICATION HAS CHANGED!\nIT IS POSSIBLE THAT SOMEONE IS DOING SOMETHING NASTY!\nSomeone could be eavesdropping on you right now (man-in-the-middle attack)!\nIt is also possible that the " + key_type + " host key has just been changed.\n" + "The fingerprint for the " + key_type + " key sent by the remote host " + chost + " is\n" + key_fprint + ".\n" + "Please contact your system administrator.\n" + "Add correct host key in " + file + " to get rid of this message.";
                if (shkc.equals("ask")) {
                    b = this.userinfo.promptYesNo((String)message + "\nDo you want to delete the old key and insert the new key?");
                } else {
                    this.userinfo.showMessage((String)message);
                }
            }
            if (!b) {
                throw new JSchException("HostKey has been changed: " + chost);
            }
            message = hkr;
            synchronized (message) {
                hkr.remove(chost, kex.getKeyAlgorithName(), null);
                insert = true;
            }
        }
        if ((shkc.equals("ask") || shkc.equals("yes")) && i != 0 && !insert) {
            if (shkc.equals("yes")) {
                throw new JSchException("reject HostKey: " + this.host);
            }
            if (this.userinfo != null) {
                boolean foo = this.userinfo.promptYesNo("The authenticity of host '" + this.host + "' can't be established.\n" + key_type + " key fingerprint is " + key_fprint + ".\n" + "Are you sure you want to continue connecting?");
                if (!foo) {
                    throw new JSchException("reject HostKey: " + this.host);
                }
                insert = true;
            } else {
                if (i == 1) {
                    throw new JSchException("UnknownHostKey: " + this.host + ". " + key_type + " key fingerprint is " + key_fprint);
                }
                throw new JSchException("HostKey has been changed: " + this.host);
            }
        }
        if (shkc.equals("no") && 1 == i) {
            insert = true;
        }
        if (i == 0) {
            HostKey[] keys = hkr.getHostKey(chost, kex.getKeyAlgorithName());
            String _key = Util.byte2str(Util.toBase64(K_S, 0, K_S.length));
            for (int j = 0; j < keys.length; ++j) {
                if (!keys[i].getKey().equals(_key) || !keys[j].getMarker().equals("@revoked")) continue;
                if (this.userinfo != null) {
                    this.userinfo.showMessage("The " + key_type + " host key for " + this.host + " is marked as revoked.\n" + "This could mean that a stolen key is being used to " + "impersonate this host.");
                }
                if (JSch.getLogger().isEnabled(1)) {
                    JSch.getLogger().log(1, "Host '" + this.host + "' has provided revoked key.");
                }
                throw new JSchException("revoked HostKey: " + this.host);
            }
        }
        if (i == 0 && JSch.getLogger().isEnabled(1)) {
            JSch.getLogger().log(1, "Host '" + this.host + "' is known and matches the " + key_type + " host key");
        }
        if (insert && JSch.getLogger().isEnabled(2)) {
            JSch.getLogger().log(2, "Permanently added '" + this.host + "' (" + key_type + ") to the list of known hosts.");
        }
        if (insert) {
            HostKeyRepository hostKeyRepository3 = hkr;
            synchronized (hostKeyRepository3) {
                hkr.add(this.hostkey, this.userinfo);
            }
        }
    }

    public Channel openChannel(String type) throws JSchException {
        if (!this.isConnected) {
            throw new JSchException("session is down");
        }
        try {
            Channel channel = Channel.getChannel(type);
            this.addChannel(channel);
            channel.init();
            if (channel instanceof ChannelSession) {
                this.applyConfigChannel((ChannelSession)channel);
            }
            return channel;
        }
        catch (Exception exception) {
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void encode(Packet packet) throws Exception {
        if (this.deflater != null) {
            this.compress_len[0] = packet.buffer.index;
            packet.buffer.buffer = this.deflater.compress(packet.buffer.buffer, 5, this.compress_len);
            packet.buffer.index = this.compress_len[0];
        }
        if (this.c2scipher != null) {
            packet.padding(this.c2scipher_size);
            byte pad = packet.buffer.buffer[4];
            Random random = Session.random;
            synchronized (random) {
                Session.random.fill(packet.buffer.buffer, packet.buffer.index - pad, pad);
            }
        } else {
            packet.padding(8);
        }
        if (this.c2smac != null) {
            this.c2smac.update(this.seqo);
            this.c2smac.update(packet.buffer.buffer, 0, packet.buffer.index);
            this.c2smac.doFinal(packet.buffer.buffer, packet.buffer.index);
        }
        if (this.c2scipher != null) {
            byte[] buf = packet.buffer.buffer;
            this.c2scipher.update(buf, 0, packet.buffer.index, buf, 0);
        }
        if (this.c2smac != null) {
            packet.buffer.skip(this.c2smac.getBlockSize());
        }
    }

    public Buffer read(Buffer buf) throws Exception {
        block18: {
            int type;
            int j = 0;
            while (true) {
                int need;
                buf.reset();
                this.io.getByte(buf.buffer, buf.index, this.s2ccipher_size);
                buf.index += this.s2ccipher_size;
                if (this.s2ccipher != null) {
                    this.s2ccipher.update(buf.buffer, 0, this.s2ccipher_size, buf.buffer, 0);
                }
                if ((j = buf.buffer[0] << 24 & 0xFF000000 | buf.buffer[1] << 16 & 0xFF0000 | buf.buffer[2] << 8 & 0xFF00 | buf.buffer[3] & 0xFF) < 5 || j > 262144) {
                    this.start_discard(buf, this.s2ccipher, this.s2cmac, j, 262144);
                }
                if (buf.index + (need = j + 4 - this.s2ccipher_size) > buf.buffer.length) {
                    byte[] foo = new byte[buf.index + need];
                    System.arraycopy(buf.buffer, 0, foo, 0, buf.index);
                    buf.buffer = foo;
                }
                if (need % this.s2ccipher_size != 0) {
                    String message = "Bad packet length " + need;
                    if (JSch.getLogger().isEnabled(4)) {
                        JSch.getLogger().log(4, message);
                    }
                    this.start_discard(buf, this.s2ccipher, this.s2cmac, j, 262144 - this.s2ccipher_size);
                }
                if (need > 0) {
                    this.io.getByte(buf.buffer, buf.index, need);
                    buf.index += need;
                    if (this.s2ccipher != null) {
                        this.s2ccipher.update(buf.buffer, this.s2ccipher_size, need, buf.buffer, this.s2ccipher_size);
                    }
                }
                if (this.s2cmac != null) {
                    this.s2cmac.update(this.seqi);
                    this.s2cmac.update(buf.buffer, 0, buf.index);
                    this.s2cmac.doFinal(this.s2cmac_result1, 0);
                    this.io.getByte(this.s2cmac_result2, 0, this.s2cmac_result2.length);
                    if (!Arrays.equals(this.s2cmac_result1, this.s2cmac_result2)) {
                        if (need > 262144) {
                            throw new IOException("MAC Error");
                        }
                        this.start_discard(buf, this.s2ccipher, this.s2cmac, j, 262144 - need);
                        continue;
                    }
                }
                ++this.seqi;
                if (this.inflater != null) {
                    byte pad = buf.buffer[4];
                    this.uncompress_len[0] = buf.index - 5 - pad;
                    byte[] foo = this.inflater.uncompress(buf.buffer, 5, this.uncompress_len);
                    if (foo == null) {
                        System.err.println("fail in inflater");
                        break block18;
                    }
                    buf.buffer = foo;
                    buf.index = 5 + this.uncompress_len[0];
                }
                if ((type = buf.getCommand() & 0xFF) == 1) {
                    buf.rewind();
                    buf.getInt();
                    buf.getShort();
                    int reason_code = buf.getInt();
                    byte[] description = buf.getString();
                    byte[] language_tag = buf.getString();
                    throw new JSchException("SSH_MSG_DISCONNECT: " + reason_code + " " + Util.byte2str(description) + " " + Util.byte2str(language_tag));
                }
                if (type == 2) continue;
                if (type == 3) {
                    buf.rewind();
                    buf.getInt();
                    buf.getShort();
                    int reason_id = buf.getInt();
                    if (!JSch.getLogger().isEnabled(1)) continue;
                    JSch.getLogger().log(1, "Received SSH_MSG_UNIMPLEMENTED for " + reason_id);
                    continue;
                }
                if (type == 4) {
                    buf.rewind();
                    buf.getInt();
                    buf.getShort();
                    continue;
                }
                if (type != 93) break;
                buf.rewind();
                buf.getInt();
                buf.getShort();
                Channel c = Channel.getChannel(buf.getInt(), this);
                if (c == null) continue;
                c.addRemoteWindowSize(buf.getUInt());
            }
            if (type == 52) {
                this.isAuthed = true;
                if (this.inflater == null && this.deflater == null) {
                    String method = this.guess[6];
                    this.initDeflater(method);
                    method = this.guess[7];
                    this.initInflater(method);
                }
            }
        }
        buf.rewind();
        return buf;
    }

    private void start_discard(Buffer buf, Cipher cipher, MAC mac, int packet_length, int discard) throws JSchException, IOException {
        MAC discard_mac = null;
        if (!cipher.isCBC()) {
            throw new JSchException("Packet corrupt");
        }
        if (packet_length != 262144 && mac != null) {
            discard_mac = mac;
        }
        discard -= buf.index;
        while (discard > 0) {
            buf.reset();
            int len = discard > buf.buffer.length ? buf.buffer.length : discard;
            this.io.getByte(buf.buffer, 0, len);
            if (discard_mac != null) {
                discard_mac.update(buf.buffer, 0, len);
            }
            discard -= len;
        }
        if (discard_mac != null) {
            discard_mac.doFinal(buf.buffer, 0);
        }
        throw new JSchException("Packet corrupt");
    }

    byte[] getSessionId() {
        return this.session_id;
    }

    private void receive_newkeys(Buffer buf, KeyExchange kex) throws Exception {
        this.updateKeys(kex);
        this.in_kex = false;
    }

    private void updateKeys(KeyExchange kex) throws Exception {
        int j;
        byte[] K = kex.getK();
        byte[] H = kex.getH();
        HASH hash = kex.getHash();
        if (this.session_id == null) {
            this.session_id = new byte[H.length];
            System.arraycopy(H, 0, this.session_id, 0, H.length);
        }
        this.buf.reset();
        this.buf.putMPInt(K);
        this.buf.putByte(H);
        this.buf.putByte((byte)65);
        this.buf.putByte(this.session_id);
        hash.update(this.buf.buffer, 0, this.buf.index);
        this.IVc2s = hash.digest();
        int n = j = this.buf.index - this.session_id.length - 1;
        this.buf.buffer[n] = (byte)(this.buf.buffer[n] + 1);
        hash.update(this.buf.buffer, 0, this.buf.index);
        this.IVs2c = hash.digest();
        int n2 = j;
        this.buf.buffer[n2] = (byte)(this.buf.buffer[n2] + 1);
        hash.update(this.buf.buffer, 0, this.buf.index);
        this.Ec2s = hash.digest();
        int n3 = j;
        this.buf.buffer[n3] = (byte)(this.buf.buffer[n3] + 1);
        hash.update(this.buf.buffer, 0, this.buf.index);
        this.Es2c = hash.digest();
        int n4 = j;
        this.buf.buffer[n4] = (byte)(this.buf.buffer[n4] + 1);
        hash.update(this.buf.buffer, 0, this.buf.index);
        this.MACc2s = hash.digest();
        int n5 = j;
        this.buf.buffer[n5] = (byte)(this.buf.buffer[n5] + 1);
        hash.update(this.buf.buffer, 0, this.buf.index);
        this.MACs2c = hash.digest();
        try {
            byte[] bar;
            byte[] foo;
            String method = this.guess[3];
            Class<?> c = Class.forName(this.getConfig(method));
            this.s2ccipher = (Cipher)c.newInstance();
            while (this.s2ccipher.getBlockSize() > this.Es2c.length) {
                this.buf.reset();
                this.buf.putMPInt(K);
                this.buf.putByte(H);
                this.buf.putByte(this.Es2c);
                hash.update(this.buf.buffer, 0, this.buf.index);
                foo = hash.digest();
                bar = new byte[this.Es2c.length + foo.length];
                System.arraycopy(this.Es2c, 0, bar, 0, this.Es2c.length);
                System.arraycopy(foo, 0, bar, this.Es2c.length, foo.length);
                this.Es2c = bar;
            }
            this.s2ccipher.init(1, this.Es2c, this.IVs2c);
            this.s2ccipher_size = this.s2ccipher.getIVSize();
            method = this.guess[5];
            c = Class.forName(this.getConfig(method));
            this.s2cmac = (MAC)c.newInstance();
            this.MACs2c = this.expandKey(this.buf, K, H, this.MACs2c, hash, this.s2cmac.getBlockSize());
            this.s2cmac.init(this.MACs2c);
            this.s2cmac_result1 = new byte[this.s2cmac.getBlockSize()];
            this.s2cmac_result2 = new byte[this.s2cmac.getBlockSize()];
            method = this.guess[2];
            c = Class.forName(this.getConfig(method));
            this.c2scipher = (Cipher)c.newInstance();
            while (this.c2scipher.getBlockSize() > this.Ec2s.length) {
                this.buf.reset();
                this.buf.putMPInt(K);
                this.buf.putByte(H);
                this.buf.putByte(this.Ec2s);
                hash.update(this.buf.buffer, 0, this.buf.index);
                foo = hash.digest();
                bar = new byte[this.Ec2s.length + foo.length];
                System.arraycopy(this.Ec2s, 0, bar, 0, this.Ec2s.length);
                System.arraycopy(foo, 0, bar, this.Ec2s.length, foo.length);
                this.Ec2s = bar;
            }
            this.c2scipher.init(0, this.Ec2s, this.IVc2s);
            this.c2scipher_size = this.c2scipher.getIVSize();
            method = this.guess[4];
            c = Class.forName(this.getConfig(method));
            this.c2smac = (MAC)c.newInstance();
            this.MACc2s = this.expandKey(this.buf, K, H, this.MACc2s, hash, this.c2smac.getBlockSize());
            this.c2smac.init(this.MACc2s);
            method = this.guess[6];
            this.initDeflater(method);
            method = this.guess[7];
            this.initInflater(method);
        }
        catch (Exception e) {
            if (e instanceof JSchException) {
                throw e;
            }
            throw new JSchException(e.toString(), e);
        }
    }

    private byte[] expandKey(Buffer buf, byte[] K, byte[] H, byte[] key, HASH hash, int required_length) throws Exception {
        byte[] result = key;
        int size = hash.getBlockSize();
        while (result.length < required_length) {
            buf.reset();
            buf.putMPInt(K);
            buf.putByte(H);
            buf.putByte(result);
            hash.update(buf.buffer, 0, buf.index);
            byte[] tmp = new byte[result.length + size];
            System.arraycopy(result, 0, tmp, 0, result.length);
            System.arraycopy(hash.digest(), 0, tmp, result.length, size);
            Util.bzero(result);
            result = tmp;
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void write(Packet packet, Channel c, int length) throws Exception {
        long t = this.getTimeout();
        while (true) {
            if (this.in_kex) {
                if (t > 0L && System.currentTimeMillis() - this.kex_start_time > t) {
                    throw new JSchException("timeout in waiting for rekeying process.");
                }
                try {
                    Thread.sleep(10L);
                }
                catch (InterruptedException e) {}
                continue;
            }
            Channel e = c;
            synchronized (e) {
                if (c.rwsize < (long)length) {
                    try {
                        ++c.notifyme;
                        c.wait(100L);
                    }
                    catch (InterruptedException e2) {
                    }
                    finally {
                        --c.notifyme;
                    }
                }
                if (this.in_kex) {
                    continue;
                }
                if (c.rwsize >= (long)length) {
                    c.rwsize -= (long)length;
                    break;
                }
            }
            if (c.close || !c.isConnected()) {
                throw new IOException("channel is broken");
            }
            boolean sendit = false;
            int s = 0;
            byte command = 0;
            int recipient = -1;
            Channel channel = c;
            synchronized (channel) {
                if (c.rwsize > 0L) {
                    long len = c.rwsize;
                    if (len > (long)length) {
                        len = length;
                    }
                    if (len != (long)length) {
                        s = packet.shift((int)len, this.c2scipher != null ? this.c2scipher_size : 8, this.c2smac != null ? this.c2smac.getBlockSize() : 0);
                    }
                    command = packet.buffer.getCommand();
                    recipient = c.getRecipient();
                    length = (int)((long)length - len);
                    c.rwsize -= len;
                    sendit = true;
                }
            }
            if (sendit) {
                this._write(packet);
                if (length == 0) {
                    return;
                }
                packet.unshift(command, recipient, s, length);
            }
            channel = c;
            synchronized (channel) {
                if (this.in_kex) {
                    continue;
                }
                if (c.rwsize >= (long)length) {
                    c.rwsize -= (long)length;
                    break;
                }
            }
        }
        this._write(packet);
    }

    public void write(Packet packet) throws Exception {
        long t = this.getTimeout();
        while (this.in_kex) {
            if (t > 0L && System.currentTimeMillis() - this.kex_start_time > t && !this.in_prompt) {
                throw new JSchException("timeout in waiting for rekeying process.");
            }
            byte command = packet.buffer.getCommand();
            if (command == 20 || command == 21 || command == 30 || command == 31 || command == 31 || command == 32 || command == 33 || command == 34 || command == 1) break;
            try {
                Thread.sleep(10L);
            }
            catch (InterruptedException e) {}
        }
        this._write(packet);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void _write(Packet packet) throws Exception {
        Object object = this.lock;
        synchronized (object) {
            this.encode(packet);
            if (this.io != null) {
                this.io.put(packet);
                ++this.seqo;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void run() {
        block42: {
            this.thread = this;
            Buffer buf = new Buffer();
            Packet packet = new Packet(buf);
            int i = 0;
            int[] start = new int[1];
            int[] length = new int[1];
            KeyExchange kex = null;
            int stimeout = 0;
            try {
                block32: while (this.isConnected && this.thread != null) {
                    try {
                        buf = this.read(buf);
                        stimeout = 0;
                    }
                    catch (InterruptedIOException ee) {
                        if (!this.in_kex && stimeout < this.serverAliveCountMax) {
                            this.sendKeepAliveMsg();
                            ++stimeout;
                            continue;
                        }
                        if (this.in_kex && stimeout < this.serverAliveCountMax) {
                            ++stimeout;
                            continue;
                        }
                        throw ee;
                    }
                    int msgType = buf.getCommand() & 0xFF;
                    if (kex != null && kex.getState() == msgType) {
                        this.kex_start_time = System.currentTimeMillis();
                        boolean result = kex.next(buf);
                        if (result) continue;
                        throw new JSchException("verify: " + result);
                    }
                    switch (msgType) {
                        case 20: {
                            kex = this.receive_kexinit(buf);
                            break;
                        }
                        case 21: {
                            this.send_newkeys();
                            this.receive_newkeys(buf, kex);
                            kex = null;
                            break;
                        }
                        case 94: {
                            buf.getInt();
                            buf.getByte();
                            buf.getByte();
                            i = buf.getInt();
                            Channel channel = Channel.getChannel(i, this);
                            byte[] foo = buf.getString(start, length);
                            if (channel == null || length[0] == 0) break;
                            try {
                                channel.write(foo, start[0], length[0]);
                            }
                            catch (Exception e) {
                                try {
                                    channel.disconnect();
                                }
                                catch (Exception ee) {}
                                continue block32;
                            }
                            int len = length[0];
                            channel.setLocalWindowSize(channel.lwsize - len);
                            if (channel.lwsize >= channel.lwsize_max / 2) continue block32;
                            packet.reset();
                            buf.putByte((byte)93);
                            buf.putInt(channel.getRecipient());
                            buf.putInt(channel.lwsize_max - channel.lwsize);
                            Channel ee = channel;
                            synchronized (ee) {
                                if (!channel.close) {
                                    this.write(packet);
                                }
                            }
                            channel.setLocalWindowSize(channel.lwsize_max);
                            break;
                        }
                        case 95: {
                            buf.getInt();
                            buf.getShort();
                            i = buf.getInt();
                            Channel channel = Channel.getChannel(i, this);
                            buf.getInt();
                            byte[] foo = buf.getString(start, length);
                            if (channel == null || length[0] == 0) break;
                            channel.write_ext(foo, start[0], length[0]);
                            int len = length[0];
                            channel.setLocalWindowSize(channel.lwsize - len);
                            if (channel.lwsize >= channel.lwsize_max / 2) continue block32;
                            packet.reset();
                            buf.putByte((byte)93);
                            buf.putInt(channel.getRecipient());
                            buf.putInt(channel.lwsize_max - channel.lwsize);
                            Channel ee = channel;
                            synchronized (ee) {
                                if (!channel.close) {
                                    this.write(packet);
                                }
                            }
                            channel.setLocalWindowSize(channel.lwsize_max);
                            break;
                        }
                        case 93: {
                            buf.getInt();
                            buf.getShort();
                            i = buf.getInt();
                            Channel channel = Channel.getChannel(i, this);
                            if (channel == null) break;
                            channel.addRemoteWindowSize(buf.getUInt());
                            break;
                        }
                        case 96: {
                            buf.getInt();
                            buf.getShort();
                            i = buf.getInt();
                            Channel channel = Channel.getChannel(i, this);
                            if (channel == null) continue block32;
                            channel.eof_remote();
                            break;
                        }
                        case 97: {
                            buf.getInt();
                            buf.getShort();
                            i = buf.getInt();
                            Channel channel = Channel.getChannel(i, this);
                            if (channel == null) continue block32;
                            channel.disconnect();
                            break;
                        }
                        case 91: {
                            buf.getInt();
                            buf.getShort();
                            i = buf.getInt();
                            Channel channel = Channel.getChannel(i, this);
                            int r = buf.getInt();
                            long rws = buf.getUInt();
                            int rps = buf.getInt();
                            if (channel == null) continue block32;
                            channel.setRemoteWindowSize(rws);
                            channel.setRemotePacketSize(rps);
                            channel.open_confirmation = true;
                            channel.setRecipient(r);
                            break;
                        }
                        case 92: {
                            buf.getInt();
                            buf.getShort();
                            i = buf.getInt();
                            Channel channel = Channel.getChannel(i, this);
                            if (channel == null) continue block32;
                            int reason_code = buf.getInt();
                            channel.setExitStatus(reason_code);
                            channel.close = true;
                            channel.eof_remote = true;
                            channel.setRecipient(0);
                            break;
                        }
                        case 98: {
                            boolean reply;
                            buf.getInt();
                            buf.getShort();
                            i = buf.getInt();
                            byte[] foo = buf.getString();
                            boolean bl = reply = buf.getByte() != 0;
                            Channel channel = Channel.getChannel(i, this);
                            if (channel == null) continue block32;
                            int reply_type = 100;
                            if (Util.byte2str(foo).equals("exit-status")) {
                                i = buf.getInt();
                                channel.setExitStatus(i);
                                reply_type = 99;
                            }
                            if (!reply) continue block32;
                            packet.reset();
                            buf.putByte((byte)reply_type);
                            buf.putInt(channel.getRecipient());
                            this.write(packet);
                            break;
                        }
                        case 90: {
                            buf.getInt();
                            buf.getShort();
                            byte[] foo = buf.getString();
                            String ctyp = Util.byte2str(foo);
                            if (!("forwarded-tcpip".equals(ctyp) || "x11".equals(ctyp) && this.x11_forwarding || "auth-agent@openssh.com".equals(ctyp) && this.agent_forwarding)) {
                                packet.reset();
                                buf.putByte((byte)92);
                                buf.putInt(buf.getInt());
                                buf.putInt(1);
                                buf.putString(Util.empty);
                                buf.putString(Util.empty);
                                this.write(packet);
                                break;
                            }
                            Channel channel = Channel.getChannel(ctyp);
                            this.addChannel(channel);
                            channel.getData(buf);
                            channel.init();
                            Thread tmp = new Thread(channel);
                            tmp.setName("Channel " + ctyp + " " + this.host);
                            if (this.daemon_thread) {
                                tmp.setDaemon(this.daemon_thread);
                            }
                            tmp.start();
                            break;
                        }
                        case 99: {
                            buf.getInt();
                            buf.getShort();
                            i = buf.getInt();
                            Channel channel = Channel.getChannel(i, this);
                            if (channel == null) break;
                            channel.reply = 1;
                            break;
                        }
                        case 100: {
                            buf.getInt();
                            buf.getShort();
                            i = buf.getInt();
                            Channel channel = Channel.getChannel(i, this);
                            if (channel == null) break;
                            channel.reply = 0;
                            break;
                        }
                        case 80: {
                            buf.getInt();
                            buf.getShort();
                            byte[] foo = buf.getString();
                            boolean reply = buf.getByte() != 0;
                            if (!reply) continue block32;
                            packet.reset();
                            buf.putByte((byte)82);
                            this.write(packet);
                            break;
                        }
                        case 81: 
                        case 82: {
                            Thread t = this.grr.getThread();
                            if (t == null) continue block32;
                            this.grr.setReply(msgType == 81 ? 1 : 0);
                            if (msgType == 81 && this.grr.getPort() == 0) {
                                buf.getInt();
                                buf.getShort();
                                this.grr.setPort(buf.getInt());
                            }
                            t.interrupt();
                            break;
                        }
                        default: {
                            throw new IOException("Unknown SSH message type " + msgType);
                        }
                    }
                }
            }
            catch (Exception e) {
                this.in_kex = false;
                if (!JSch.getLogger().isEnabled(1)) break block42;
                JSch.getLogger().log(1, "Caught an exception, leaving main loop due to " + e.getMessage());
            }
        }
        try {
            this.disconnect();
        }
        catch (NullPointerException e) {
        }
        catch (Exception e) {
            // empty catch block
        }
        this.isConnected = false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void disconnect() {
        block17: {
            if (!this.isConnected) {
                return;
            }
            if (JSch.getLogger().isEnabled(1)) {
                JSch.getLogger().log(1, "Disconnecting from " + this.host + " port " + this.port);
            }
            Channel.disconnect(this);
            this.isConnected = false;
            PortWatcher.delPort(this);
            ChannelForwardedTCPIP.delPort(this);
            ChannelX11.removeFakedCookie(this);
            Object object = this.lock;
            synchronized (object) {
                if (this.connectThread != null) {
                    Thread.yield();
                    this.connectThread.interrupt();
                    this.connectThread = null;
                }
            }
            this.thread = null;
            try {
                if (this.io != null) {
                    if (this.io.in != null) {
                        this.io.in.close();
                    }
                    if (this.io.out != null) {
                        this.io.out.close();
                    }
                    if (this.io.out_ext != null) {
                        this.io.out_ext.close();
                    }
                }
                if (this.proxy == null) {
                    if (this.socket != null) {
                        this.socket.close();
                    }
                    break block17;
                }
                object = this.proxy;
                synchronized (object) {
                    this.proxy.close();
                }
                this.proxy = null;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        this.io = null;
        this.socket = null;
        this.jsch.removeSession(this);
    }

    public int setPortForwardingL(int lport, String host, int rport) throws JSchException {
        return this.setPortForwardingL("127.0.0.1", lport, host, rport);
    }

    public int setPortForwardingL(String bind_address, int lport, String host, int rport) throws JSchException {
        return this.setPortForwardingL(bind_address, lport, host, rport, null);
    }

    public int setPortForwardingL(String bind_address, int lport, String host, int rport, ServerSocketFactory ssf) throws JSchException {
        return this.setPortForwardingL(bind_address, lport, host, rport, ssf, 0);
    }

    public int setPortForwardingL(String bind_address, int lport, String host, int rport, ServerSocketFactory ssf, int connectTimeout) throws JSchException {
        PortWatcher pw = PortWatcher.addPort(this, bind_address, lport, host, rport, ssf);
        pw.setConnectTimeout(connectTimeout);
        Thread tmp = new Thread(pw);
        tmp.setName("PortWatcher Thread for " + host);
        if (this.daemon_thread) {
            tmp.setDaemon(this.daemon_thread);
        }
        tmp.start();
        return pw.lport;
    }

    public void delPortForwardingL(int lport) throws JSchException {
        this.delPortForwardingL("127.0.0.1", lport);
    }

    public void delPortForwardingL(String bind_address, int lport) throws JSchException {
        PortWatcher.delPort(this, bind_address, lport);
    }

    public String[] getPortForwardingL() throws JSchException {
        return PortWatcher.getPortForwarding(this);
    }

    public void setPortForwardingR(int rport, String host, int lport) throws JSchException {
        this.setPortForwardingR(null, rport, host, lport, null);
    }

    public void setPortForwardingR(String bind_address, int rport, String host, int lport) throws JSchException {
        this.setPortForwardingR(bind_address, rport, host, lport, null);
    }

    public void setPortForwardingR(int rport, String host, int lport, SocketFactory sf) throws JSchException {
        this.setPortForwardingR(null, rport, host, lport, sf);
    }

    public void setPortForwardingR(String bind_address, int rport, String host, int lport, SocketFactory sf) throws JSchException {
        int allocated = this._setPortForwardingR(bind_address, rport);
        ChannelForwardedTCPIP.addPort(this, bind_address, rport, allocated, host, lport, sf);
    }

    public void setPortForwardingR(int rport, String daemon) throws JSchException {
        this.setPortForwardingR(null, rport, daemon, null);
    }

    public void setPortForwardingR(int rport, String daemon, Object[] arg) throws JSchException {
        this.setPortForwardingR(null, rport, daemon, arg);
    }

    public void setPortForwardingR(String bind_address, int rport, String daemon, Object[] arg) throws JSchException {
        int allocated = this._setPortForwardingR(bind_address, rport);
        ChannelForwardedTCPIP.addPort(this, bind_address, rport, allocated, daemon, arg);
    }

    public String[] getPortForwardingR() throws JSchException {
        return ChannelForwardedTCPIP.getPortForwarding(this);
    }

    private Forwarding parseForwarding(String conf) throws JSchException {
        String[] tmp = conf.split(" ");
        if (tmp.length > 1) {
            Vector<String> foo = new Vector<String>();
            for (int i = 0; i < tmp.length; ++i) {
                if (tmp[i].length() == 0) continue;
                foo.addElement(tmp[i].trim());
            }
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < foo.size(); ++i) {
                sb.append((String)foo.elementAt(i));
                if (i + 1 >= foo.size()) continue;
                sb.append(":");
            }
            conf = sb.toString();
        }
        String org = conf;
        Forwarding f = new Forwarding();
        try {
            if (conf.lastIndexOf(":") == -1) {
                throw new JSchException("parseForwarding: " + org);
            }
            f.hostport = Integer.parseInt(conf.substring(conf.lastIndexOf(":") + 1));
            if ((conf = conf.substring(0, conf.lastIndexOf(":"))).lastIndexOf(":") == -1) {
                throw new JSchException("parseForwarding: " + org);
            }
            f.host = conf.substring(conf.lastIndexOf(":") + 1);
            if ((conf = conf.substring(0, conf.lastIndexOf(":"))).lastIndexOf(":") != -1) {
                f.port = Integer.parseInt(conf.substring(conf.lastIndexOf(":") + 1));
                if ((conf = conf.substring(0, conf.lastIndexOf(":"))).length() == 0 || conf.equals("*")) {
                    conf = "0.0.0.0";
                }
                if (conf.equals("localhost")) {
                    conf = "127.0.0.1";
                }
                f.bind_address = conf;
            } else {
                f.port = Integer.parseInt(conf);
                f.bind_address = "127.0.0.1";
            }
        }
        catch (NumberFormatException e) {
            throw new JSchException("parseForwarding: " + e.toString());
        }
        return f;
    }

    public int setPortForwardingL(String conf) throws JSchException {
        Forwarding f = this.parseForwarding(conf);
        return this.setPortForwardingL(f.bind_address, f.port, f.host, f.hostport);
    }

    public int setPortForwardingR(String conf) throws JSchException {
        Forwarding f = this.parseForwarding(conf);
        int allocated = this._setPortForwardingR(f.bind_address, f.port);
        ChannelForwardedTCPIP.addPort(this, f.bind_address, f.port, allocated, f.host, f.hostport, null);
        return allocated;
    }

    public Channel getStreamForwarder(String host, int port) throws JSchException {
        ChannelDirectTCPIP channel = new ChannelDirectTCPIP();
        channel.init();
        this.addChannel(channel);
        channel.setHost(host);
        channel.setPort(port);
        return channel;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int _setPortForwardingR(String bind_address, int rport) throws JSchException {
        GlobalRequestReply globalRequestReply = this.grr;
        synchronized (globalRequestReply) {
            Buffer buf = new Buffer(100);
            Packet packet = new Packet(buf);
            String address_to_bind = ChannelForwardedTCPIP.normalize(bind_address);
            this.grr.setThread(Thread.currentThread());
            this.grr.setPort(rport);
            try {
                packet.reset();
                buf.putByte((byte)80);
                buf.putString(Util.str2byte("tcpip-forward"));
                buf.putByte((byte)1);
                buf.putString(Util.str2byte(address_to_bind));
                buf.putInt(rport);
                this.write(packet);
            }
            catch (Exception e) {
                this.grr.setThread(null);
                if (e instanceof Throwable) {
                    throw new JSchException(e.toString(), e);
                }
                throw new JSchException(e.toString());
            }
            int reply = this.grr.getReply();
            for (int count = 0; count < 10 && reply == -1; ++count) {
                try {
                    Thread.sleep(1000L);
                }
                catch (Exception e) {
                    // empty catch block
                }
                reply = this.grr.getReply();
            }
            this.grr.setThread(null);
            if (reply != 1) {
                throw new JSchException("remote port forwarding failed for listen port " + rport);
            }
            rport = this.grr.getPort();
        }
        return rport;
    }

    public void delPortForwardingR(int rport) throws JSchException {
        this.delPortForwardingR(null, rport);
    }

    public void delPortForwardingR(String bind_address, int rport) throws JSchException {
        ChannelForwardedTCPIP.delPort(this, bind_address, rport);
    }

    private void initDeflater(String method) throws JSchException {
        if (method.equals("none")) {
            this.deflater = null;
            return;
        }
        String foo = this.getConfig(method);
        if (foo != null && (method.equals("zlib") || this.isAuthed && method.equals("zlib@openssh.com"))) {
            try {
                Class<?> c = Class.forName(foo);
                this.deflater = (Compression)c.newInstance();
                int level = 6;
                try {
                    level = Integer.parseInt(this.getConfig("compression_level"));
                }
                catch (Exception ee) {
                    // empty catch block
                }
                this.deflater.init(1, level);
            }
            catch (NoClassDefFoundError ee) {
                throw new JSchException(ee.toString(), ee);
            }
            catch (Exception ee) {
                throw new JSchException(ee.toString(), ee);
            }
        }
    }

    private void initInflater(String method) throws JSchException {
        if (method.equals("none")) {
            this.inflater = null;
            return;
        }
        String foo = this.getConfig(method);
        if (foo != null && (method.equals("zlib") || this.isAuthed && method.equals("zlib@openssh.com"))) {
            try {
                Class<?> c = Class.forName(foo);
                this.inflater = (Compression)c.newInstance();
                this.inflater.init(0, 0);
            }
            catch (Exception ee) {
                throw new JSchException(ee.toString(), ee);
            }
        }
    }

    void addChannel(Channel channel) {
        channel.setSession(this);
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    void setUserName(String username) {
        this.username = username;
    }

    public void setUserInfo(UserInfo userinfo) {
        this.userinfo = userinfo;
    }

    public UserInfo getUserInfo() {
        return this.userinfo;
    }

    public void setInputStream(InputStream in) {
        this.in = in;
    }

    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    public void setX11Host(String host) {
        ChannelX11.setHost(host);
    }

    public void setX11Port(int port) {
        ChannelX11.setPort(port);
    }

    public void setX11Cookie(String cookie) {
        ChannelX11.setCookie(cookie);
    }

    public void setPassword(String password) {
        if (password != null) {
            this.password = Util.str2byte(password);
        }
    }

    public void setPassword(byte[] password) {
        if (password != null) {
            this.password = new byte[password.length];
            System.arraycopy(password, 0, this.password, 0, password.length);
        }
    }

    public void setConfig(Properties newconf) {
        this.setConfig((Hashtable)newconf);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setConfig(Hashtable newconf) {
        Object object = this.lock;
        synchronized (object) {
            if (this.config == null) {
                this.config = new Hashtable();
            }
            Enumeration e = newconf.keys();
            while (e.hasMoreElements()) {
                String key = (String)e.nextElement();
                this.config.put(key, (String)newconf.get(key));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setConfig(String key, String value) {
        Object object = this.lock;
        synchronized (object) {
            if (this.config == null) {
                this.config = new Hashtable();
            }
            this.config.put(key, value);
        }
    }

    public String getConfig(String key) {
        String foo = null;
        if (this.config != null && (foo = (String)this.config.get(key)) instanceof String) {
            return foo;
        }
        foo = JSch.getConfig(key);
        if (foo instanceof String) {
            return foo;
        }
        return null;
    }

    public void setSocketFactory(SocketFactory sfactory) {
        this.socket_factory = sfactory;
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int timeout) throws JSchException {
        if (this.socket == null) {
            if (timeout < 0) {
                throw new JSchException("invalid timeout value");
            }
            this.timeout = timeout;
            return;
        }
        try {
            this.socket.setSoTimeout(timeout);
            this.timeout = timeout;
        }
        catch (Exception e) {
            if (e instanceof Throwable) {
                throw new JSchException(e.toString(), e);
            }
            throw new JSchException(e.toString());
        }
    }

    public String getServerVersion() {
        return Util.byte2str(this.V_S);
    }

    public String getClientVersion() {
        return Util.byte2str(this.V_C);
    }

    public void setClientVersion(String cv) {
        this.V_C = Util.str2byte(cv);
    }

    public void sendIgnore() throws Exception {
        Buffer buf = new Buffer();
        Packet packet = new Packet(buf);
        packet.reset();
        buf.putByte((byte)2);
        this.write(packet);
    }

    public void sendKeepAliveMsg() throws Exception {
        Buffer buf = new Buffer();
        Packet packet = new Packet(buf);
        packet.reset();
        buf.putByte((byte)80);
        buf.putString(keepalivemsg);
        buf.putByte((byte)1);
        this.write(packet);
    }

    public void noMoreSessionChannels() throws Exception {
        Buffer buf = new Buffer();
        Packet packet = new Packet(buf);
        packet.reset();
        buf.putByte((byte)80);
        buf.putString(nomoresessions);
        buf.putByte((byte)0);
        this.write(packet);
    }

    public HostKey getHostKey() {
        return this.hostkey;
    }

    public String getHost() {
        return this.host;
    }

    public String getUserName() {
        return this.username;
    }

    public int getPort() {
        return this.port;
    }

    public void setHostKeyAlias(String hostKeyAlias) {
        this.hostKeyAlias = hostKeyAlias;
    }

    public String getHostKeyAlias() {
        return this.hostKeyAlias;
    }

    public void setServerAliveInterval(int interval) throws JSchException {
        this.setTimeout(interval);
        this.serverAliveInterval = interval;
    }

    public int getServerAliveInterval() {
        return this.serverAliveInterval;
    }

    public void setServerAliveCountMax(int count) {
        this.serverAliveCountMax = count;
    }

    public int getServerAliveCountMax() {
        return this.serverAliveCountMax;
    }

    public void setDaemonThread(boolean enable) {
        this.daemon_thread = enable;
    }

    private String[] checkCiphers(String ciphers) {
        if (ciphers == null || ciphers.length() == 0) {
            return null;
        }
        if (JSch.getLogger().isEnabled(1)) {
            JSch.getLogger().log(1, "CheckCiphers: " + ciphers);
        }
        String cipherc2s = this.getConfig("cipher.c2s");
        String ciphers2c = this.getConfig("cipher.s2c");
        Vector<String> result = new Vector<String>();
        String[] _ciphers = Util.split(ciphers, ",");
        for (int i = 0; i < _ciphers.length; ++i) {
            String cipher = _ciphers[i];
            if (ciphers2c.indexOf(cipher) == -1 && cipherc2s.indexOf(cipher) == -1 || Session.checkCipher(this.getConfig(cipher))) continue;
            result.addElement(cipher);
        }
        if (result.size() == 0) {
            return null;
        }
        String[] foo = new String[result.size()];
        System.arraycopy(result.toArray(), 0, foo, 0, result.size());
        if (JSch.getLogger().isEnabled(1)) {
            for (int i = 0; i < foo.length; ++i) {
                JSch.getLogger().log(1, foo[i] + " is not available.");
            }
        }
        return foo;
    }

    static boolean checkCipher(String cipher) {
        try {
            Class<?> c = Class.forName(cipher);
            Cipher _c = (Cipher)c.newInstance();
            _c.init(0, new byte[_c.getBlockSize()], new byte[_c.getIVSize()]);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    private String[] checkKexes(String kexes) {
        if (kexes == null || kexes.length() == 0) {
            return null;
        }
        if (JSch.getLogger().isEnabled(1)) {
            JSch.getLogger().log(1, "CheckKexes: " + kexes);
        }
        Vector<String> result = new Vector<String>();
        String[] _kexes = Util.split(kexes, ",");
        for (int i = 0; i < _kexes.length; ++i) {
            if (Session.checkKex(this, this.getConfig(_kexes[i]))) continue;
            result.addElement(_kexes[i]);
        }
        if (result.size() == 0) {
            return null;
        }
        String[] foo = new String[result.size()];
        System.arraycopy(result.toArray(), 0, foo, 0, result.size());
        if (JSch.getLogger().isEnabled(1)) {
            for (int i = 0; i < foo.length; ++i) {
                JSch.getLogger().log(1, foo[i] + " is not available.");
            }
        }
        return foo;
    }

    static boolean checkKex(Session s, String kex) {
        try {
            Class<?> c = Class.forName(kex);
            KeyExchange _c = (KeyExchange)c.newInstance();
            _c.init(s, null, null, null, null);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    private String[] checkSignatures(String sigs) {
        if (sigs == null || sigs.length() == 0) {
            return null;
        }
        if (JSch.getLogger().isEnabled(1)) {
            JSch.getLogger().log(1, "CheckSignatures: " + sigs);
        }
        Vector<String> result = new Vector<String>();
        String[] _sigs = Util.split(sigs, ",");
        for (int i = 0; i < _sigs.length; ++i) {
            try {
                Class<?> c = Class.forName(JSch.getConfig(_sigs[i]));
                Signature sig = (Signature)c.newInstance();
                sig.init();
                continue;
            }
            catch (Exception e) {
                result.addElement(_sigs[i]);
            }
        }
        if (result.size() == 0) {
            return null;
        }
        String[] foo = new String[result.size()];
        System.arraycopy(result.toArray(), 0, foo, 0, result.size());
        if (JSch.getLogger().isEnabled(1)) {
            for (int i = 0; i < foo.length; ++i) {
                JSch.getLogger().log(1, foo[i] + " is not available.");
            }
        }
        return foo;
    }

    public void setIdentityRepository(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    IdentityRepository getIdentityRepository() {
        if (this.identityRepository == null) {
            return this.jsch.getIdentityRepository();
        }
        return this.identityRepository;
    }

    public void setHostKeyRepository(HostKeyRepository hostkeyRepository) {
        this.hostkeyRepository = hostkeyRepository;
    }

    public HostKeyRepository getHostKeyRepository() {
        if (this.hostkeyRepository == null) {
            return this.jsch.getHostKeyRepository();
        }
        return this.hostkeyRepository;
    }

    private void applyConfig() throws JSchException {
        String[] values;
        int port;
        ConfigRepository configRepository = this.jsch.getConfigRepository();
        if (configRepository == null) {
            return;
        }
        ConfigRepository.Config config = configRepository.getConfig(this.org_host);
        String value = null;
        value = config.getUser();
        if (value != null) {
            this.username = value;
        }
        if ((value = config.getHostname()) != null) {
            this.host = value;
        }
        if ((port = config.getPort()) != -1) {
            this.port = port;
        }
        this.checkConfig(config, "kex");
        this.checkConfig(config, "server_host_key");
        this.checkConfig(config, "cipher.c2s");
        this.checkConfig(config, "cipher.s2c");
        this.checkConfig(config, "mac.c2s");
        this.checkConfig(config, "mac.s2c");
        this.checkConfig(config, "compression.c2s");
        this.checkConfig(config, "compression.s2c");
        this.checkConfig(config, "compression_level");
        this.checkConfig(config, "StrictHostKeyChecking");
        this.checkConfig(config, "HashKnownHosts");
        this.checkConfig(config, "PreferredAuthentications");
        this.checkConfig(config, "MaxAuthTries");
        this.checkConfig(config, "ClearAllForwardings");
        value = config.getValue("HostKeyAlias");
        if (value != null) {
            this.setHostKeyAlias(value);
        }
        if ((value = config.getValue("UserKnownHostsFile")) != null) {
            KnownHosts kh = new KnownHosts(this.jsch);
            kh.setKnownHosts(value);
            this.setHostKeyRepository(kh);
        }
        if ((values = config.getValues("IdentityFile")) != null) {
            String[] global = configRepository.getConfig("").getValues("IdentityFile");
            if (global != null) {
                for (int i = 0; i < global.length; ++i) {
                    this.jsch.addIdentity(global[i]);
                }
            } else {
                global = new String[]{};
            }
            if (values.length - global.length > 0) {
                IdentityRepository.Wrapper ir = new IdentityRepository.Wrapper(this.jsch.getIdentityRepository(), true);
                for (int i = 0; i < values.length; ++i) {
                    String ifile = values[i];
                    for (int j = 0; j < global.length; ++j) {
                        if (!ifile.equals(global[j])) continue;
                        ifile = null;
                        break;
                    }
                    if (ifile == null) continue;
                    IdentityFile identity = IdentityFile.newInstance(ifile, null, this.jsch);
                    ir.add(identity);
                }
                this.setIdentityRepository(ir);
            }
        }
        if ((value = config.getValue("ServerAliveInterval")) != null) {
            try {
                this.setServerAliveInterval(Integer.parseInt(value));
            }
            catch (NumberFormatException e) {
                // empty catch block
            }
        }
        if ((value = config.getValue("ConnectTimeout")) != null) {
            try {
                this.setTimeout(Integer.parseInt(value));
            }
            catch (NumberFormatException e) {
                // empty catch block
            }
        }
        if ((value = config.getValue("MaxAuthTries")) != null) {
            this.setConfig("MaxAuthTries", value);
        }
        if ((value = config.getValue("ClearAllForwardings")) != null) {
            this.setConfig("ClearAllForwardings", value);
        }
    }

    private void applyConfigChannel(ChannelSession channel) throws JSchException {
        ConfigRepository configRepository = this.jsch.getConfigRepository();
        if (configRepository == null) {
            return;
        }
        ConfigRepository.Config config = configRepository.getConfig(this.org_host);
        String value = null;
        value = config.getValue("ForwardAgent");
        if (value != null) {
            channel.setAgentForwarding(value.equals("yes"));
        }
        if ((value = config.getValue("RequestTTY")) != null) {
            channel.setPty(value.equals("yes"));
        }
    }

    private void requestPortForwarding() throws JSchException {
        int i;
        if (this.getConfig("ClearAllForwardings").equals("yes")) {
            return;
        }
        ConfigRepository configRepository = this.jsch.getConfigRepository();
        if (configRepository == null) {
            return;
        }
        ConfigRepository.Config config = configRepository.getConfig(this.org_host);
        String[] values = config.getValues("LocalForward");
        if (values != null) {
            for (i = 0; i < values.length; ++i) {
                this.setPortForwardingL(values[i]);
            }
        }
        if ((values = config.getValues("RemoteForward")) != null) {
            for (i = 0; i < values.length; ++i) {
                this.setPortForwardingR(values[i]);
            }
        }
    }

    private void checkConfig(ConfigRepository.Config config, String key) {
        String value = config.getValue(key);
        if (value != null) {
            this.setConfig(key, value);
        }
    }

    static {
        keepalivemsg = Util.str2byte("keepalive@jcraft.com");
        nomoresessions = Util.str2byte("no-more-sessions@openssh.com");
    }

    private class GlobalRequestReply {
        private Thread thread = null;
        private int reply = -1;
        private int port = 0;

        private GlobalRequestReply() {
        }

        void setThread(Thread thread) {
            this.thread = thread;
            this.reply = -1;
        }

        Thread getThread() {
            return this.thread;
        }

        void setReply(int reply) {
            this.reply = reply;
        }

        int getReply() {
            return this.reply;
        }

        int getPort() {
            return this.port;
        }

        void setPort(int port) {
            this.port = port;
        }
    }

    private class Forwarding {
        String bind_address = null;
        int port = -1;
        String host = null;
        int hostport = -1;

        private Forwarding() {
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.SocketFactory;
import com.jcraft.jsch.Util;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ProxySOCKS4
implements Proxy {
    private static int DEFAULTPORT = 1080;
    private String proxy_host;
    private int proxy_port;
    private InputStream in;
    private OutputStream out;
    private Socket socket;
    private String user;
    private String passwd;

    public ProxySOCKS4(String proxy_host) {
        int port = DEFAULTPORT;
        String host = proxy_host;
        if (proxy_host.indexOf(58) != -1) {
            try {
                host = proxy_host.substring(0, proxy_host.indexOf(58));
                port = Integer.parseInt(proxy_host.substring(proxy_host.indexOf(58) + 1));
            }
            catch (Exception e) {
                // empty catch block
            }
        }
        this.proxy_host = host;
        this.proxy_port = port;
    }

    public ProxySOCKS4(String proxy_host, int proxy_port) {
        this.proxy_host = proxy_host;
        this.proxy_port = proxy_port;
    }

    public void setUserPasswd(String user, String passwd) {
        this.user = user;
        this.passwd = passwd;
    }

    public void connect(SocketFactory socket_factory, String host, int port, int timeout) throws JSchException {
        block17: {
            try {
                int i;
                if (socket_factory == null) {
                    this.socket = Util.createSocket(this.proxy_host, this.proxy_port, timeout);
                    this.in = this.socket.getInputStream();
                    this.out = this.socket.getOutputStream();
                } else {
                    this.socket = socket_factory.createSocket(this.proxy_host, this.proxy_port);
                    this.in = socket_factory.getInputStream(this.socket);
                    this.out = socket_factory.getOutputStream(this.socket);
                }
                if (timeout > 0) {
                    this.socket.setSoTimeout(timeout);
                }
                this.socket.setTcpNoDelay(true);
                byte[] buf = new byte[1024];
                int index = 0;
                index = 0;
                buf[index++] = 4;
                buf[index++] = 1;
                buf[index++] = (byte)(port >>> 8);
                buf[index++] = (byte)(port & 0xFF);
                try {
                    InetAddress addr = InetAddress.getByName(host);
                    byte[] byteAddress = addr.getAddress();
                    for (i = 0; i < byteAddress.length; ++i) {
                        buf[index++] = byteAddress[i];
                    }
                }
                catch (UnknownHostException uhe) {
                    throw new JSchException("ProxySOCKS4: " + uhe.toString(), uhe);
                }
                if (this.user != null) {
                    System.arraycopy(Util.str2byte(this.user), 0, buf, index, this.user.length());
                    index += this.user.length();
                }
                buf[index++] = 0;
                this.out.write(buf, 0, index);
                int len = 8;
                for (int s = 0; s < len; s += i) {
                    i = this.in.read(buf, s, len - s);
                    if (i > 0) continue;
                    throw new JSchException("ProxySOCKS4: stream is closed");
                }
                if (buf[0] != 0) {
                    throw new JSchException("ProxySOCKS4: server returns VN " + buf[0]);
                }
                if (buf[1] == 90) break block17;
                try {
                    this.socket.close();
                }
                catch (Exception eee) {
                    // empty catch block
                }
                String message = "ProxySOCKS4: server returns CD " + buf[1];
                throw new JSchException(message);
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                try {
                    if (this.socket != null) {
                        this.socket.close();
                    }
                }
                catch (Exception eee) {
                    // empty catch block
                }
                throw new JSchException("ProxySOCKS4: " + e.toString());
            }
        }
    }

    public InputStream getInputStream() {
        return this.in;
    }

    public OutputStream getOutputStream() {
        return this.out;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void close() {
        try {
            if (this.in != null) {
                this.in.close();
            }
            if (this.out != null) {
                this.out.close();
            }
            if (this.socket != null) {
                this.socket.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.in = null;
        this.out = null;
        this.socket = null;
    }

    public static int getDefaultPort() {
        return DEFAULTPORT;
    }
}


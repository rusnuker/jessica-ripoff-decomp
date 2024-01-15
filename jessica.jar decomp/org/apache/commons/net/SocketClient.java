/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import org.apache.commons.net.DefaultSocketFactory;
import org.apache.commons.net.SocketFactory;

public abstract class SocketClient {
    public static final String NETASCII_EOL = "\r\n";
    private static final SocketFactory __DEFAULT_SOCKET_FACTORY = new DefaultSocketFactory();
    protected int _timeout_ = 0;
    protected Socket _socket_ = null;
    protected boolean _isConnected_ = false;
    protected int _defaultPort_ = 0;
    protected InputStream _input_ = null;
    protected OutputStream _output_ = null;
    protected SocketFactory _socketFactory_ = __DEFAULT_SOCKET_FACTORY;

    protected void _connectAction_() throws IOException {
        this._socket_.setSoTimeout(this._timeout_);
        this._input_ = this._socket_.getInputStream();
        this._output_ = this._socket_.getOutputStream();
        this._isConnected_ = true;
    }

    public void connect(InetAddress host, int port) throws SocketException, IOException {
        this._socket_ = this._socketFactory_.createSocket(host, port);
        this._connectAction_();
    }

    public void connect(String hostname, int port) throws SocketException, IOException {
        this._socket_ = this._socketFactory_.createSocket(hostname, port);
        this._connectAction_();
    }

    public void connect(InetAddress host, int port, InetAddress localAddr, int localPort) throws SocketException, IOException {
        this._socket_ = this._socketFactory_.createSocket(host, port, localAddr, localPort);
        this._connectAction_();
    }

    public void connect(String hostname, int port, InetAddress localAddr, int localPort) throws SocketException, IOException {
        this._socket_ = this._socketFactory_.createSocket(hostname, port, localAddr, localPort);
        this._connectAction_();
    }

    public void connect(InetAddress host) throws SocketException, IOException {
        this.connect(host, this._defaultPort_);
    }

    public void connect(String hostname) throws SocketException, IOException {
        this.connect(hostname, this._defaultPort_);
    }

    public void disconnect() throws IOException {
        this._socket_.close();
        this._input_.close();
        this._output_.close();
        this._socket_ = null;
        this._input_ = null;
        this._output_ = null;
        this._isConnected_ = false;
    }

    public boolean isConnected() {
        return this._isConnected_;
    }

    public void setDefaultPort(int port) {
        this._defaultPort_ = port;
    }

    public int getDefaultPort() {
        return this._defaultPort_;
    }

    public void setDefaultTimeout(int timeout) {
        this._timeout_ = timeout;
    }

    public int getDefaultTimeout() {
        return this._timeout_;
    }

    public void setSoTimeout(int timeout) throws SocketException {
        this._socket_.setSoTimeout(timeout);
    }

    public int getSoTimeout() throws SocketException {
        return this._socket_.getSoTimeout();
    }

    public void setTcpNoDelay(boolean on) throws SocketException {
        this._socket_.setTcpNoDelay(on);
    }

    public boolean getTcpNoDelay() throws SocketException {
        return this._socket_.getTcpNoDelay();
    }

    public void setSoLinger(boolean on, int val) throws SocketException {
        this._socket_.setSoLinger(on, val);
    }

    public int getSoLinger() throws SocketException {
        return this._socket_.getSoLinger();
    }

    public int getLocalPort() {
        return this._socket_.getLocalPort();
    }

    public InetAddress getLocalAddress() {
        return this._socket_.getLocalAddress();
    }

    public int getRemotePort() {
        return this._socket_.getPort();
    }

    public InetAddress getRemoteAddress() {
        return this._socket_.getInetAddress();
    }

    public boolean verifyRemote(Socket socket) {
        InetAddress host1 = socket.getInetAddress();
        InetAddress host2 = this.getRemoteAddress();
        return host1.equals(host2);
    }

    public void setSocketFactory(SocketFactory factory) {
        this._socketFactory_ = factory == null ? __DEFAULT_SOCKET_FACTORY : factory;
    }
}


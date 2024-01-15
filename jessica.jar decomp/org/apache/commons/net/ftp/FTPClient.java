/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import org.apache.commons.net.MalformedServerReplyException;
import org.apache.commons.net.ftp.Configurable;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPFileList;
import org.apache.commons.net.ftp.FTPFileListParser;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory;
import org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory;
import org.apache.commons.net.io.FromNetASCIIInputStream;
import org.apache.commons.net.io.SocketInputStream;
import org.apache.commons.net.io.SocketOutputStream;
import org.apache.commons.net.io.ToNetASCIIOutputStream;
import org.apache.commons.net.io.Util;

public class FTPClient
extends FTP
implements Configurable {
    public static final int ACTIVE_LOCAL_DATA_CONNECTION_MODE = 0;
    public static final int ACTIVE_REMOTE_DATA_CONNECTION_MODE = 1;
    public static final int PASSIVE_LOCAL_DATA_CONNECTION_MODE = 2;
    public static final int PASSIVE_REMOTE_DATA_CONNECTION_MODE = 3;
    private int __dataConnectionMode;
    private int __dataTimeout;
    private int __passivePort;
    private String __passiveHost;
    private int __fileType;
    private int __fileFormat;
    private int __fileStructure;
    private int __fileTransferMode;
    private boolean __remoteVerificationEnabled;
    private long __restartOffset;
    private FTPFileEntryParserFactory __parserFactory;
    private int __bufferSize;
    private String __systemName;
    private FTPFileEntryParser __entryParser;
    private FTPClientConfig __configuration;

    public FTPClient() {
        this.__initDefaults();
        this.__dataTimeout = -1;
        this.__remoteVerificationEnabled = true;
        this.__parserFactory = new DefaultFTPFileEntryParserFactory();
        this.__configuration = null;
    }

    private void __initDefaults() {
        this.__dataConnectionMode = 0;
        this.__passiveHost = null;
        this.__passivePort = -1;
        this.__fileType = 0;
        this.__fileStructure = 7;
        this.__fileFormat = 4;
        this.__fileTransferMode = 10;
        this.__restartOffset = 0L;
        this.__systemName = null;
        this.__entryParser = null;
        this.__bufferSize = 1024;
    }

    private String __parsePathname(String reply) {
        int begin = reply.indexOf(34) + 1;
        int end = reply.indexOf(34, begin);
        return reply.substring(begin, end);
    }

    private void __parsePassiveModeReply(String reply) throws MalformedServerReplyException {
        reply = reply.substring(reply.indexOf(40) + 1, reply.indexOf(41)).trim();
        StringBuffer host = new StringBuffer(24);
        int lastIndex = 0;
        int index = reply.indexOf(44);
        host.append(reply.substring(lastIndex, index));
        for (int i = 0; i < 3; ++i) {
            host.append('.');
            lastIndex = index + 1;
            index = reply.indexOf(44, lastIndex);
            host.append(reply.substring(lastIndex, index));
        }
        lastIndex = index + 1;
        index = reply.indexOf(44, lastIndex);
        String octet1 = reply.substring(lastIndex, index);
        String octet2 = reply.substring(index + 1);
        try {
            index = Integer.parseInt(octet1);
            lastIndex = Integer.parseInt(octet2);
        }
        catch (NumberFormatException e) {
            throw new MalformedServerReplyException("Could not parse passive host information.\nServer Reply: " + reply);
        }
        index <<= 8;
        this.__passiveHost = host.toString();
        this.__passivePort = index |= lastIndex;
    }

    private boolean __storeFile(int command, String remote, InputStream local) throws IOException {
        Socket socket = this._openDataConnection_(command, remote);
        if (socket == null) {
            return false;
        }
        FilterOutputStream output = new BufferedOutputStream(socket.getOutputStream(), this.getBufferSize());
        if (this.__fileType == 0) {
            output = new ToNetASCIIOutputStream(output);
        }
        try {
            Util.copyStream(local, output, this.getBufferSize(), -1L, null, false);
        }
        catch (IOException e) {
            try {
                socket.close();
            }
            catch (IOException f) {
                // empty catch block
            }
            throw e;
        }
        ((OutputStream)output).close();
        socket.close();
        return this.completePendingCommand();
    }

    private OutputStream __storeFileStream(int command, String remote) throws IOException {
        Socket socket = this._openDataConnection_(command, remote);
        if (socket == null) {
            return null;
        }
        OutputStream output = socket.getOutputStream();
        if (this.__fileType == 0) {
            output = new BufferedOutputStream(output, this.getBufferSize());
            output = new ToNetASCIIOutputStream(output);
        }
        return new SocketOutputStream(socket, output);
    }

    protected Socket _openDataConnection_(int command, String arg) throws IOException {
        Socket socket;
        if (this.__dataConnectionMode != 0 && this.__dataConnectionMode != 2) {
            return null;
        }
        if (this.__dataConnectionMode == 0) {
            ServerSocket server = this._socketFactory_.createServerSocket(0, 1, this.getLocalAddress());
            if (!FTPReply.isPositiveCompletion(this.port(this.getLocalAddress(), server.getLocalPort()))) {
                server.close();
                return null;
            }
            if (this.__restartOffset > 0L && !this.restart(this.__restartOffset)) {
                server.close();
                return null;
            }
            if (!FTPReply.isPositivePreliminary(this.sendCommand(command, arg))) {
                server.close();
                return null;
            }
            if (this.__dataTimeout >= 0) {
                server.setSoTimeout(this.__dataTimeout);
            }
            socket = server.accept();
            server.close();
        } else {
            if (this.pasv() != 227) {
                return null;
            }
            this.__parsePassiveModeReply((String)this._replyLines.elementAt(0));
            socket = this._socketFactory_.createSocket(this.__passiveHost, this.__passivePort);
            if (this.__restartOffset > 0L && !this.restart(this.__restartOffset)) {
                socket.close();
                return null;
            }
            if (!FTPReply.isPositivePreliminary(this.sendCommand(command, arg))) {
                socket.close();
                return null;
            }
        }
        if (this.__remoteVerificationEnabled && !this.verifyRemote(socket)) {
            InetAddress host1 = socket.getInetAddress();
            InetAddress host2 = this.getRemoteAddress();
            socket.close();
            throw new IOException("Host attempting data connection " + host1.getHostAddress() + " is not same as server " + host2.getHostAddress());
        }
        if (this.__dataTimeout >= 0) {
            socket.setSoTimeout(this.__dataTimeout);
        }
        return socket;
    }

    protected void _connectAction_() throws IOException {
        super._connectAction_();
        this.__initDefaults();
    }

    public void setDataTimeout(int timeout) {
        this.__dataTimeout = timeout;
    }

    public void setParserFactory(FTPFileEntryParserFactory parserFactory) {
        this.__parserFactory = parserFactory;
    }

    public void disconnect() throws IOException {
        super.disconnect();
        this.__initDefaults();
    }

    public void setRemoteVerificationEnabled(boolean enable) {
        this.__remoteVerificationEnabled = enable;
    }

    public boolean isRemoteVerificationEnabled() {
        return this.__remoteVerificationEnabled;
    }

    public boolean login(String username, String password) throws IOException {
        this.user(username);
        if (FTPReply.isPositiveCompletion(this._replyCode)) {
            return true;
        }
        if (!FTPReply.isPositiveIntermediate(this._replyCode)) {
            return false;
        }
        return FTPReply.isPositiveCompletion(this.pass(password));
    }

    public boolean login(String username, String password, String account) throws IOException {
        this.user(username);
        if (FTPReply.isPositiveCompletion(this._replyCode)) {
            return true;
        }
        if (!FTPReply.isPositiveIntermediate(this._replyCode)) {
            return false;
        }
        this.pass(password);
        if (FTPReply.isPositiveCompletion(this._replyCode)) {
            return true;
        }
        if (!FTPReply.isPositiveIntermediate(this._replyCode)) {
            return false;
        }
        return FTPReply.isPositiveCompletion(this.acct(account));
    }

    public boolean logout() throws IOException {
        return FTPReply.isPositiveCompletion(this.quit());
    }

    public boolean changeWorkingDirectory(String pathname) throws IOException {
        return FTPReply.isPositiveCompletion(this.cwd(pathname));
    }

    public boolean changeToParentDirectory() throws IOException {
        return FTPReply.isPositiveCompletion(this.cdup());
    }

    public boolean structureMount(String pathname) throws IOException {
        return FTPReply.isPositiveCompletion(this.smnt(pathname));
    }

    boolean reinitialize() throws IOException {
        this.rein();
        if (FTPReply.isPositiveCompletion(this._replyCode) || FTPReply.isPositivePreliminary(this._replyCode) && FTPReply.isPositiveCompletion(this.getReply())) {
            this.__initDefaults();
            return true;
        }
        return false;
    }

    public void enterLocalActiveMode() {
        this.__dataConnectionMode = 0;
        this.__passiveHost = null;
        this.__passivePort = -1;
    }

    public void enterLocalPassiveMode() {
        this.__dataConnectionMode = 2;
        this.__passiveHost = null;
        this.__passivePort = -1;
    }

    public boolean enterRemoteActiveMode(InetAddress host, int port) throws IOException {
        if (FTPReply.isPositiveCompletion(this.port(host, port))) {
            this.__dataConnectionMode = 1;
            this.__passiveHost = null;
            this.__passivePort = -1;
            return true;
        }
        return false;
    }

    public boolean enterRemotePassiveMode() throws IOException {
        if (this.pasv() != 227) {
            return false;
        }
        this.__dataConnectionMode = 3;
        this.__parsePassiveModeReply((String)this._replyLines.elementAt(0));
        return true;
    }

    public String getPassiveHost() {
        return this.__passiveHost;
    }

    public int getPassivePort() {
        return this.__passivePort;
    }

    public int getDataConnectionMode() {
        return this.__dataConnectionMode;
    }

    public boolean setFileType(int fileType) throws IOException {
        if (FTPReply.isPositiveCompletion(this.type(fileType))) {
            this.__fileType = fileType;
            this.__fileFormat = 4;
            return true;
        }
        return false;
    }

    public boolean setFileType(int fileType, int formatOrByteSize) throws IOException {
        if (FTPReply.isPositiveCompletion(this.type(fileType, formatOrByteSize))) {
            this.__fileType = fileType;
            this.__fileFormat = formatOrByteSize;
            return true;
        }
        return false;
    }

    public boolean setFileStructure(int structure) throws IOException {
        if (FTPReply.isPositiveCompletion(this.stru(structure))) {
            this.__fileStructure = structure;
            return true;
        }
        return false;
    }

    public boolean setFileTransferMode(int mode) throws IOException {
        if (FTPReply.isPositiveCompletion(this.mode(mode))) {
            this.__fileTransferMode = mode;
            return true;
        }
        return false;
    }

    public boolean remoteRetrieve(String filename) throws IOException {
        if (this.__dataConnectionMode == 1 || this.__dataConnectionMode == 3) {
            return FTPReply.isPositivePreliminary(this.retr(filename));
        }
        return false;
    }

    public boolean remoteStore(String filename) throws IOException {
        if (this.__dataConnectionMode == 1 || this.__dataConnectionMode == 3) {
            return FTPReply.isPositivePreliminary(this.stor(filename));
        }
        return false;
    }

    public boolean remoteStoreUnique(String filename) throws IOException {
        if (this.__dataConnectionMode == 1 || this.__dataConnectionMode == 3) {
            return FTPReply.isPositivePreliminary(this.stou(filename));
        }
        return false;
    }

    public boolean remoteStoreUnique() throws IOException {
        if (this.__dataConnectionMode == 1 || this.__dataConnectionMode == 3) {
            return FTPReply.isPositivePreliminary(this.stou());
        }
        return false;
    }

    public boolean remoteAppend(String filename) throws IOException {
        if (this.__dataConnectionMode == 1 || this.__dataConnectionMode == 3) {
            return FTPReply.isPositivePreliminary(this.stor(filename));
        }
        return false;
    }

    public boolean completePendingCommand() throws IOException {
        return FTPReply.isPositiveCompletion(this.getReply());
    }

    public boolean retrieveFile(String remote, OutputStream local) throws IOException {
        Socket socket = this._openDataConnection_(13, remote);
        if (socket == null) {
            return false;
        }
        FilterInputStream input = new BufferedInputStream(socket.getInputStream(), this.getBufferSize());
        if (this.__fileType == 0) {
            input = new FromNetASCIIInputStream(input);
        }
        try {
            Util.copyStream(input, local, this.getBufferSize(), -1L, null, false);
        }
        catch (IOException e) {
            try {
                socket.close();
            }
            catch (IOException f) {
                // empty catch block
            }
            throw e;
        }
        socket.close();
        return this.completePendingCommand();
    }

    public InputStream retrieveFileStream(String remote) throws IOException {
        Socket socket = this._openDataConnection_(13, remote);
        if (socket == null) {
            return null;
        }
        InputStream input = socket.getInputStream();
        if (this.__fileType == 0) {
            input = new BufferedInputStream(input, this.getBufferSize());
            input = new FromNetASCIIInputStream(input);
        }
        return new SocketInputStream(socket, input);
    }

    public boolean storeFile(String remote, InputStream local) throws IOException {
        return this.__storeFile(14, remote, local);
    }

    public OutputStream storeFileStream(String remote) throws IOException {
        return this.__storeFileStream(14, remote);
    }

    public boolean appendFile(String remote, InputStream local) throws IOException {
        return this.__storeFile(16, remote, local);
    }

    public OutputStream appendFileStream(String remote) throws IOException {
        return this.__storeFileStream(16, remote);
    }

    public boolean storeUniqueFile(String remote, InputStream local) throws IOException {
        return this.__storeFile(15, remote, local);
    }

    public OutputStream storeUniqueFileStream(String remote) throws IOException {
        return this.__storeFileStream(15, remote);
    }

    public boolean storeUniqueFile(InputStream local) throws IOException {
        return this.__storeFile(15, null, local);
    }

    public OutputStream storeUniqueFileStream() throws IOException {
        return this.__storeFileStream(15, null);
    }

    public boolean allocate(int bytes) throws IOException {
        return FTPReply.isPositiveCompletion(this.allo(bytes));
    }

    public boolean allocate(int bytes, int recordSize) throws IOException {
        return FTPReply.isPositiveCompletion(this.allo(bytes, recordSize));
    }

    private boolean restart(long offset) throws IOException {
        this.__restartOffset = 0L;
        return FTPReply.isPositiveIntermediate(this.rest(Long.toString(offset)));
    }

    public void setRestartOffset(long offset) {
        if (offset >= 0L) {
            this.__restartOffset = offset;
        }
    }

    public long getRestartOffset() {
        return this.__restartOffset;
    }

    public boolean rename(String from, String to) throws IOException {
        if (!FTPReply.isPositiveIntermediate(this.rnfr(from))) {
            return false;
        }
        return FTPReply.isPositiveCompletion(this.rnto(to));
    }

    public boolean abort() throws IOException {
        return FTPReply.isPositiveCompletion(this.abor());
    }

    public boolean deleteFile(String pathname) throws IOException {
        return FTPReply.isPositiveCompletion(this.dele(pathname));
    }

    public boolean removeDirectory(String pathname) throws IOException {
        return FTPReply.isPositiveCompletion(this.rmd(pathname));
    }

    public boolean makeDirectory(String pathname) throws IOException {
        return FTPReply.isPositiveCompletion(this.mkd(pathname));
    }

    public String printWorkingDirectory() throws IOException {
        if (this.pwd() != 257) {
            return null;
        }
        return this.__parsePathname((String)this._replyLines.elementAt(0));
    }

    public boolean sendSiteCommand(String arguments) throws IOException {
        return FTPReply.isPositiveCompletion(this.site(arguments));
    }

    public String getSystemName() throws IOException {
        if (this.__systemName == null && FTPReply.isPositiveCompletion(this.syst())) {
            this.__systemName = ((String)this._replyLines.elementAt(0)).substring(4);
        }
        return this.__systemName;
    }

    public String listHelp() throws IOException {
        if (FTPReply.isPositiveCompletion(this.help())) {
            return this.getReplyString();
        }
        return null;
    }

    public String listHelp(String command) throws IOException {
        if (FTPReply.isPositiveCompletion(this.help(command))) {
            return this.getReplyString();
        }
        return null;
    }

    public boolean sendNoOp() throws IOException {
        return FTPReply.isPositiveCompletion(this.noop());
    }

    public String[] listNames(String pathname) throws IOException {
        String line;
        Socket socket = this._openDataConnection_(27, pathname);
        if (socket == null) {
            return null;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), this.getControlEncoding()));
        Vector<String> results = new Vector<String>();
        while ((line = reader.readLine()) != null) {
            results.addElement(line);
        }
        reader.close();
        socket.close();
        if (this.completePendingCommand()) {
            Object[] result = new String[results.size()];
            results.copyInto(result);
            return result;
        }
        return null;
    }

    public String[] listNames() throws IOException {
        return this.listNames(null);
    }

    public FTPFile[] listFiles(String parserKey, String pathname) throws IOException {
        FTPListParseEngine engine = this.initiateListParsing(parserKey, pathname);
        return engine.getFiles();
    }

    public FTPFile[] listFiles(String pathname) throws IOException {
        String key = null;
        FTPListParseEngine engine = this.initiateListParsing(key, pathname);
        return engine.getFiles();
    }

    public FTPFile[] listFiles() throws IOException {
        return this.listFiles((String)null);
    }

    public FTPListParseEngine initiateListParsing() throws IOException {
        return this.initiateListParsing(null);
    }

    public FTPListParseEngine initiateListParsing(String pathname) throws IOException {
        String key = null;
        return this.initiateListParsing(key, pathname);
    }

    public FTPListParseEngine initiateListParsing(String parserKey, String pathname) throws IOException {
        if (this.__entryParser == null) {
            this.__entryParser = null != parserKey ? this.__parserFactory.createFileEntryParser(parserKey) : (null != this.__configuration ? this.__parserFactory.createFileEntryParser(this.__configuration) : this.__parserFactory.createFileEntryParser(this.getSystemName()));
        }
        return this.initiateListParsing(this.__entryParser, pathname);
    }

    private FTPListParseEngine initiateListParsing(FTPFileEntryParser parser, String pathname) throws IOException {
        FTPListParseEngine engine = new FTPListParseEngine(parser);
        Socket socket = this._openDataConnection_(26, pathname);
        if (socket == null) {
            return engine;
        }
        engine.readServerList(socket.getInputStream(), this.getControlEncoding());
        socket.close();
        this.completePendingCommand();
        return engine;
    }

    public String getStatus() throws IOException {
        if (FTPReply.isPositiveCompletion(this.stat())) {
            return this.getReplyString();
        }
        return null;
    }

    public String getStatus(String pathname) throws IOException {
        if (FTPReply.isPositiveCompletion(this.stat(pathname))) {
            return this.getReplyString();
        }
        return null;
    }

    public FTPFile[] listFiles(FTPFileListParser parser, String pathname) throws IOException {
        Socket socket = this._openDataConnection_(26, pathname);
        if (socket == null) {
            return new FTPFile[0];
        }
        FTPFile[] results = parser.parseFileList(socket.getInputStream(), this.getControlEncoding());
        socket.close();
        this.completePendingCommand();
        return results;
    }

    public FTPFile[] listFiles(FTPFileListParser parser) throws IOException {
        return this.listFiles(parser, null);
    }

    public FTPFileList createFileList(FTPFileEntryParser parser) throws IOException {
        return this.createFileList(null, parser);
    }

    public FTPFileList createFileList(String pathname, FTPFileEntryParser parser) throws IOException {
        Socket socket = this._openDataConnection_(26, pathname);
        if (socket == null) {
            return null;
        }
        FTPFileList list = FTPFileList.create(socket.getInputStream(), parser);
        socket.close();
        this.completePendingCommand();
        return list;
    }

    public void setBufferSize(int bufSize) {
        this.__bufferSize = bufSize;
    }

    public int getBufferSize() {
        return this.__bufferSize;
    }

    public void configure(FTPClientConfig config) {
        this.__configuration = config;
    }
}


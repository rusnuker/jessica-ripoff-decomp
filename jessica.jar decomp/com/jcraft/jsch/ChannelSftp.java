/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSession;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Packet;
import com.jcraft.jsch.Request;
import com.jcraft.jsch.RequestSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jcraft.jsch.SftpStatVFS;
import com.jcraft.jsch.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.Hashtable;
import java.util.Vector;

public class ChannelSftp
extends ChannelSession {
    private static final int LOCAL_MAXIMUM_PACKET_SIZE = 32768;
    private static final int LOCAL_WINDOW_SIZE_MAX = 0x200000;
    private static final byte SSH_FXP_INIT = 1;
    private static final byte SSH_FXP_VERSION = 2;
    private static final byte SSH_FXP_OPEN = 3;
    private static final byte SSH_FXP_CLOSE = 4;
    private static final byte SSH_FXP_READ = 5;
    private static final byte SSH_FXP_WRITE = 6;
    private static final byte SSH_FXP_LSTAT = 7;
    private static final byte SSH_FXP_FSTAT = 8;
    private static final byte SSH_FXP_SETSTAT = 9;
    private static final byte SSH_FXP_FSETSTAT = 10;
    private static final byte SSH_FXP_OPENDIR = 11;
    private static final byte SSH_FXP_READDIR = 12;
    private static final byte SSH_FXP_REMOVE = 13;
    private static final byte SSH_FXP_MKDIR = 14;
    private static final byte SSH_FXP_RMDIR = 15;
    private static final byte SSH_FXP_REALPATH = 16;
    private static final byte SSH_FXP_STAT = 17;
    private static final byte SSH_FXP_RENAME = 18;
    private static final byte SSH_FXP_READLINK = 19;
    private static final byte SSH_FXP_SYMLINK = 20;
    private static final byte SSH_FXP_STATUS = 101;
    private static final byte SSH_FXP_HANDLE = 102;
    private static final byte SSH_FXP_DATA = 103;
    private static final byte SSH_FXP_NAME = 104;
    private static final byte SSH_FXP_ATTRS = 105;
    private static final byte SSH_FXP_EXTENDED = -56;
    private static final byte SSH_FXP_EXTENDED_REPLY = -55;
    private static final int SSH_FXF_READ = 1;
    private static final int SSH_FXF_WRITE = 2;
    private static final int SSH_FXF_APPEND = 4;
    private static final int SSH_FXF_CREAT = 8;
    private static final int SSH_FXF_TRUNC = 16;
    private static final int SSH_FXF_EXCL = 32;
    private static final int SSH_FILEXFER_ATTR_SIZE = 1;
    private static final int SSH_FILEXFER_ATTR_UIDGID = 2;
    private static final int SSH_FILEXFER_ATTR_PERMISSIONS = 4;
    private static final int SSH_FILEXFER_ATTR_ACMODTIME = 8;
    private static final int SSH_FILEXFER_ATTR_EXTENDED = Integer.MIN_VALUE;
    public static final int SSH_FX_OK = 0;
    public static final int SSH_FX_EOF = 1;
    public static final int SSH_FX_NO_SUCH_FILE = 2;
    public static final int SSH_FX_PERMISSION_DENIED = 3;
    public static final int SSH_FX_FAILURE = 4;
    public static final int SSH_FX_BAD_MESSAGE = 5;
    public static final int SSH_FX_NO_CONNECTION = 6;
    public static final int SSH_FX_CONNECTION_LOST = 7;
    public static final int SSH_FX_OP_UNSUPPORTED = 8;
    private static final int MAX_MSG_LENGTH = 262144;
    public static final int OVERWRITE = 0;
    public static final int RESUME = 1;
    public static final int APPEND = 2;
    private boolean interactive = false;
    private int seq = 1;
    private int[] ackid = new int[1];
    private Buffer buf;
    private Packet packet;
    private Buffer obuf;
    private Packet opacket;
    private int client_version = 3;
    private int server_version = 3;
    private String version = String.valueOf(this.client_version);
    private Hashtable extensions = null;
    private InputStream io_in = null;
    private boolean extension_posix_rename = false;
    private boolean extension_statvfs = false;
    private boolean extension_hardlink = false;
    private static final String file_separator = File.separator;
    private static final char file_separatorc = File.separatorChar;
    private static boolean fs_is_bs = (byte)File.separatorChar == 92;
    private String cwd;
    private String home;
    private String lcwd;
    private static final String UTF8 = "UTF-8";
    private String fEncoding = "UTF-8";
    private boolean fEncoding_is_utf8 = true;
    private RequestQueue rq = new RequestQueue(16);

    public void setBulkRequests(int bulk_requests) throws JSchException {
        if (bulk_requests <= 0) {
            throw new JSchException("setBulkRequests: " + bulk_requests + " must be greater than 0.");
        }
        this.rq = new RequestQueue(bulk_requests);
    }

    public int getBulkRequests() {
        return this.rq.size();
    }

    public ChannelSftp() {
        this.setLocalWindowSizeMax(0x200000);
        this.setLocalWindowSize(0x200000);
        this.setLocalPacketSize(32768);
    }

    void init() {
    }

    public void start() throws JSchException {
        try {
            int length;
            PipedOutputStream pos = new PipedOutputStream();
            this.io.setOutputStream(pos);
            Channel.MyPipedInputStream pis = new Channel.MyPipedInputStream((Channel)this, pos, this.rmpsize);
            this.io.setInputStream(pis);
            this.io_in = this.io.in;
            if (this.io_in == null) {
                throw new JSchException("channel is down");
            }
            RequestSftp request = new RequestSftp();
            ((Request)request).request(this.getSession(), this);
            this.buf = new Buffer(this.lmpsize);
            this.packet = new Packet(this.buf);
            this.obuf = new Buffer(this.rmpsize);
            this.opacket = new Packet(this.obuf);
            boolean i = false;
            this.sendINIT();
            Header header = new Header();
            header = this.header(this.buf, header);
            if (length > 262144) {
                throw new SftpException(4, "Received message is too long: " + length);
            }
            int type = header.type;
            this.server_version = header.rid;
            this.extensions = new Hashtable();
            if (length > 0) {
                this.fill(this.buf, length);
                byte[] extension_name = null;
                byte[] extension_data = null;
                for (length = header.length; length > 0; length -= 4 + extension_data.length) {
                    extension_name = this.buf.getString();
                    length -= 4 + extension_name.length;
                    extension_data = this.buf.getString();
                    this.extensions.put(Util.byte2str(extension_name), Util.byte2str(extension_data));
                }
            }
            if (this.extensions.get("posix-rename@openssh.com") != null && this.extensions.get("posix-rename@openssh.com").equals("1")) {
                this.extension_posix_rename = true;
            }
            if (this.extensions.get("statvfs@openssh.com") != null && this.extensions.get("statvfs@openssh.com").equals("2")) {
                this.extension_statvfs = true;
            }
            if (this.extensions.get("hardlink@openssh.com") != null && this.extensions.get("hardlink@openssh.com").equals("1")) {
                this.extension_hardlink = true;
            }
            this.lcwd = new File(".").getCanonicalPath();
        }
        catch (Exception e) {
            if (e instanceof JSchException) {
                throw (JSchException)e;
            }
            if (e instanceof Throwable) {
                throw new JSchException(e.toString(), e);
            }
            throw new JSchException(e.toString());
        }
    }

    public void quit() {
        this.disconnect();
    }

    public void exit() {
        this.disconnect();
    }

    public void lcd(String path) throws SftpException {
        if (new File(path = this.localAbsolutePath(path)).isDirectory()) {
            try {
                path = new File(path).getCanonicalPath();
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.lcwd = path;
            return;
        }
        throw new SftpException(2, "No such directory");
    }

    public void cd(String path) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            path = this.remoteAbsolutePath(path);
            path = this.isUnique(path);
            byte[] str = this._realpath(path);
            SftpATTRS attr = this._stat(str);
            if ((attr.getFlags() & 4) == 0) {
                throw new SftpException(4, "Can't change directory: " + path);
            }
            if (!attr.isDir()) {
                throw new SftpException(4, "Can't change directory: " + path);
            }
            this.setCwd(Util.byte2str(str, this.fEncoding));
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public void put(String src, String dst) throws SftpException {
        this.put(src, dst, null, 0);
    }

    public void put(String src, String dst, int mode) throws SftpException {
        this.put(src, dst, null, mode);
    }

    public void put(String src, String dst, SftpProgressMonitor monitor) throws SftpException {
        this.put(src, dst, monitor, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void put(String src, String dst, SftpProgressMonitor monitor, int mode) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            src = this.localAbsolutePath(src);
            dst = this.remoteAbsolutePath(dst);
            Vector v = this.glob_remote(dst);
            int vsize = v.size();
            if (vsize != 1) {
                if (vsize == 0) {
                    if (this.isPattern(dst)) {
                        throw new SftpException(4, dst);
                    }
                    dst = Util.unquote(dst);
                }
                throw new SftpException(4, v.toString());
            }
            dst = (String)v.elementAt(0);
            boolean isRemoteDir = this.isRemoteDir(dst);
            v = this.glob_local(src);
            vsize = v.size();
            StringBuffer dstsb = null;
            if (isRemoteDir) {
                if (!dst.endsWith("/")) {
                    dst = dst + "/";
                }
                dstsb = new StringBuffer(dst);
            } else if (vsize > 1) {
                throw new SftpException(4, "Copying multiple files, but the destination is missing or a file.");
            }
            for (int j = 0; j < vsize; ++j) {
                String _src = (String)v.elementAt(j);
                String _dst = null;
                if (isRemoteDir) {
                    int ii;
                    int i = _src.lastIndexOf(file_separatorc);
                    if (fs_is_bs && (ii = _src.lastIndexOf(47)) != -1 && ii > i) {
                        i = ii;
                    }
                    if (i == -1) {
                        dstsb.append(_src);
                    } else {
                        dstsb.append(_src.substring(i + 1));
                    }
                    _dst = dstsb.toString();
                    dstsb.delete(dst.length(), _dst.length());
                } else {
                    _dst = dst;
                }
                long size_of_dst = 0L;
                if (mode == 1) {
                    try {
                        SftpATTRS attr = this._stat(_dst);
                        size_of_dst = attr.getSize();
                    }
                    catch (Exception eee) {
                        // empty catch block
                    }
                    long size_of_src = new File(_src).length();
                    if (size_of_src < size_of_dst) {
                        throw new SftpException(4, "failed to resume for " + _dst);
                    }
                    if (size_of_src == size_of_dst) {
                        return;
                    }
                }
                if (monitor != null) {
                    monitor.init(0, _src, _dst, new File(_src).length());
                    if (mode == 1) {
                        monitor.count(size_of_dst);
                    }
                }
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(_src);
                    this._put(fis, _dst, monitor, mode);
                    continue;
                }
                finally {
                    if (fis != null) {
                        fis.close();
                    }
                }
            }
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, e.toString(), e);
            }
            throw new SftpException(4, e.toString());
        }
    }

    public void put(InputStream src, String dst) throws SftpException {
        this.put(src, dst, null, 0);
    }

    public void put(InputStream src, String dst, int mode) throws SftpException {
        this.put(src, dst, null, mode);
    }

    public void put(InputStream src, String dst, SftpProgressMonitor monitor) throws SftpException {
        this.put(src, dst, monitor, 0);
    }

    public void put(InputStream src, String dst, SftpProgressMonitor monitor, int mode) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            dst = this.remoteAbsolutePath(dst);
            Vector v = this.glob_remote(dst);
            int vsize = v.size();
            if (vsize != 1) {
                if (vsize == 0) {
                    if (this.isPattern(dst)) {
                        throw new SftpException(4, dst);
                    }
                    dst = Util.unquote(dst);
                }
                throw new SftpException(4, v.toString());
            }
            dst = (String)v.elementAt(0);
            if (monitor != null) {
                monitor.init(0, "-", dst, -1L);
            }
            this._put(src, dst, monitor, mode);
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                if (((SftpException)e).id == 4 && this.isRemoteDir(dst)) {
                    throw new SftpException(4, dst + " is a directory");
                }
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, e.toString(), e);
            }
            throw new SftpException(4, e.toString());
        }
    }

    public void _put(InputStream src, String dst, SftpProgressMonitor monitor, int mode) throws SftpException {
        try {
            int count;
            long skipped;
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            byte[] dstb = Util.str2byte(dst, this.fEncoding);
            long skip = 0L;
            if (mode == 1 || mode == 2) {
                try {
                    SftpATTRS attr = this._stat(dstb);
                    skip = attr.getSize();
                }
                catch (Exception eee) {
                    // empty catch block
                }
            }
            if (mode == 1 && skip > 0L && (skipped = src.skip(skip)) < skip) {
                throw new SftpException(4, "failed to resume for " + dst);
            }
            if (mode == 0) {
                this.sendOPENW(dstb);
            } else {
                this.sendOPENA(dstb);
            }
            Header header = new Header();
            header = this.header(this.buf, header);
            int length = header.length;
            int type = header.type;
            this.fill(this.buf, length);
            if (type != 101 && type != 102) {
                throw new SftpException(4, "invalid type=" + type);
            }
            if (type == 101) {
                int i = this.buf.getInt();
                this.throwStatusError(this.buf, i);
            }
            byte[] handle = this.buf.getString();
            byte[] data = null;
            boolean dontcopy = true;
            if (!dontcopy) {
                data = new byte[this.obuf.buffer.length - (39 + handle.length + 128)];
            }
            long offset = 0L;
            if (mode == 1 || mode == 2) {
                offset += skip;
            }
            int startid = this.seq;
            int ackcount = 0;
            int _s = 0;
            int _datalen = 0;
            if (!dontcopy) {
                _datalen = data.length;
            } else {
                data = this.obuf.buffer;
                _s = 39 + handle.length;
                _datalen = this.obuf.buffer.length - _s - 128;
            }
            int bulk_requests = this.rq.size();
            do {
                int nread = 0;
                count = 0;
                int s = _s;
                int datalen = _datalen;
                do {
                    if ((nread = src.read(data, s, datalen)) <= 0) continue;
                    s += nread;
                    datalen -= nread;
                    count += nread;
                } while (datalen > 0 && nread > 0);
                if (count <= 0) break;
                int foo = count;
                while (foo > 0) {
                    if (this.seq - 1 == startid || this.seq - startid - ackcount >= bulk_requests) {
                        while (this.seq - startid - ackcount >= bulk_requests && this.checkStatus(this.ackid, header)) {
                            int _ackid = this.ackid[0];
                            if (startid > _ackid || _ackid > this.seq - 1) {
                                if (_ackid == this.seq) {
                                    System.err.println("ack error: startid=" + startid + " seq=" + this.seq + " _ackid=" + _ackid);
                                } else {
                                    throw new SftpException(4, "ack error: startid=" + startid + " seq=" + this.seq + " _ackid=" + _ackid);
                                }
                            }
                            ++ackcount;
                        }
                    }
                    if (dontcopy) {
                        foo -= this.sendWRITE(handle, offset, data, 0, foo);
                        if (data == this.obuf.buffer) continue;
                        data = this.obuf.buffer;
                        _datalen = this.obuf.buffer.length - _s - 128;
                        continue;
                    }
                    foo -= this.sendWRITE(handle, offset, data, _s, foo);
                }
                offset += (long)count;
            } while (monitor == null || monitor.count(count));
            int _ackcount = this.seq - startid;
            while (_ackcount > ackcount && this.checkStatus(null, header)) {
                ++ackcount;
            }
            if (monitor != null) {
                monitor.end();
            }
            this._sendCLOSE(handle, header);
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, e.toString(), e);
            }
            throw new SftpException(4, e.toString());
        }
    }

    public OutputStream put(String dst) throws SftpException {
        return this.put(dst, (SftpProgressMonitor)null, 0);
    }

    public OutputStream put(String dst, int mode) throws SftpException {
        return this.put(dst, (SftpProgressMonitor)null, mode);
    }

    public OutputStream put(String dst, SftpProgressMonitor monitor, int mode) throws SftpException {
        return this.put(dst, monitor, mode, 0L);
    }

    public OutputStream put(String dst, final SftpProgressMonitor monitor, int mode, long offset) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            dst = this.remoteAbsolutePath(dst);
            dst = this.isUnique(dst);
            if (this.isRemoteDir(dst)) {
                throw new SftpException(4, dst + " is a directory");
            }
            byte[] dstb = Util.str2byte(dst, this.fEncoding);
            long skip = 0L;
            if (mode == 1 || mode == 2) {
                try {
                    SftpATTRS attr = this._stat(dstb);
                    skip = attr.getSize();
                }
                catch (Exception eee) {
                    // empty catch block
                }
            }
            if (monitor != null) {
                monitor.init(0, "-", dst, -1L);
            }
            if (mode == 0) {
                this.sendOPENW(dstb);
            } else {
                this.sendOPENA(dstb);
            }
            Header header = new Header();
            header = this.header(this.buf, header);
            int length = header.length;
            int type = header.type;
            this.fill(this.buf, length);
            if (type != 101 && type != 102) {
                throw new SftpException(4, "");
            }
            if (type == 101) {
                int i = this.buf.getInt();
                this.throwStatusError(this.buf, i);
            }
            final byte[] handle = this.buf.getString();
            if (mode == 1 || mode == 2) {
                offset += skip;
            }
            final long[] _offset = new long[]{offset};
            OutputStream out = new OutputStream(){
                private boolean init = true;
                private boolean isClosed = false;
                private int[] ackid = new int[1];
                private int startid = 0;
                private int _ackid = 0;
                private int ackcount = 0;
                private int writecount = 0;
                private Header header = new Header();
                byte[] _data = new byte[1];

                public void write(byte[] d) throws IOException {
                    this.write(d, 0, d.length);
                }

                public void write(byte[] d, int s, int len) throws IOException {
                    if (this.init) {
                        this.startid = ChannelSftp.this.seq;
                        this._ackid = ChannelSftp.this.seq;
                        this.init = false;
                    }
                    if (this.isClosed) {
                        throw new IOException("stream already closed");
                    }
                    try {
                        int _len = len;
                        while (_len > 0) {
                            int sent = ChannelSftp.this.sendWRITE(handle, _offset[0], d, s, _len);
                            ++this.writecount;
                            _offset[0] = _offset[0] + (long)sent;
                            s += sent;
                            _len -= sent;
                            if (ChannelSftp.this.seq - 1 != this.startid && ChannelSftp.this.io_in.available() < 1024) continue;
                            while (ChannelSftp.this.io_in.available() > 0 && ChannelSftp.this.checkStatus(this.ackid, this.header)) {
                                this._ackid = this.ackid[0];
                                if (this.startid > this._ackid || this._ackid > ChannelSftp.this.seq - 1) {
                                    throw new SftpException(4, "");
                                }
                                ++this.ackcount;
                            }
                        }
                        if (monitor != null && !monitor.count(len)) {
                            this.close();
                            throw new IOException("canceled");
                        }
                    }
                    catch (IOException e) {
                        throw e;
                    }
                    catch (Exception e) {
                        throw new IOException(e.toString());
                    }
                }

                public void write(int foo) throws IOException {
                    this._data[0] = (byte)foo;
                    this.write(this._data, 0, 1);
                }

                public void flush() throws IOException {
                    if (this.isClosed) {
                        throw new IOException("stream already closed");
                    }
                    if (!this.init) {
                        try {
                            while (this.writecount > this.ackcount && ChannelSftp.this.checkStatus(null, this.header)) {
                                ++this.ackcount;
                            }
                        }
                        catch (SftpException e) {
                            throw new IOException(e.toString());
                        }
                    }
                }

                public void close() throws IOException {
                    if (this.isClosed) {
                        return;
                    }
                    this.flush();
                    if (monitor != null) {
                        monitor.end();
                    }
                    try {
                        ChannelSftp.this._sendCLOSE(handle, this.header);
                    }
                    catch (IOException e) {
                        throw e;
                    }
                    catch (Exception e) {
                        throw new IOException(e.toString());
                    }
                    this.isClosed = true;
                }
            };
            return out;
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public void get(String src, String dst) throws SftpException {
        this.get(src, dst, null, 0);
    }

    public void get(String src, String dst, SftpProgressMonitor monitor) throws SftpException {
        this.get(src, dst, monitor, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void get(String src, String dst, SftpProgressMonitor monitor, int mode) throws SftpException {
        boolean _dstExist = false;
        String _dst = null;
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            src = this.remoteAbsolutePath(src);
            dst = this.localAbsolutePath(dst);
            Vector v = this.glob_remote(src);
            int vsize = v.size();
            if (vsize == 0) {
                throw new SftpException(2, "No such file");
            }
            File dstFile = new File(dst);
            boolean isDstDir = dstFile.isDirectory();
            StringBuffer dstsb = null;
            if (isDstDir) {
                if (!dst.endsWith(file_separator)) {
                    dst = dst + file_separator;
                }
                dstsb = new StringBuffer(dst);
            } else if (vsize > 1) {
                throw new SftpException(4, "Copying multiple files, but destination is missing or a file.");
            }
            for (int j = 0; j < vsize; ++j) {
                String _src = (String)v.elementAt(j);
                SftpATTRS attr = this._stat(_src);
                if (attr.isDir()) {
                    throw new SftpException(4, "not supported to get directory " + _src);
                }
                _dst = null;
                if (isDstDir) {
                    int i = _src.lastIndexOf(47);
                    if (i == -1) {
                        dstsb.append(_src);
                    } else {
                        dstsb.append(_src.substring(i + 1));
                    }
                    _dst = dstsb.toString();
                    if (_dst.indexOf("..") != -1) {
                        String dstc = new File(dst).getCanonicalPath();
                        String _dstc = new File(_dst).getCanonicalPath();
                        if (_dstc.length() <= dstc.length() || !_dstc.substring(0, dstc.length() + 1).equals(dstc + file_separator)) {
                            throw new SftpException(4, "writing to an unexpected file " + _src);
                        }
                    }
                    dstsb.delete(dst.length(), _dst.length());
                } else {
                    _dst = dst;
                }
                File _dstFile = new File(_dst);
                if (mode == 1) {
                    long size_of_src = attr.getSize();
                    long size_of_dst = _dstFile.length();
                    if (size_of_dst > size_of_src) {
                        throw new SftpException(4, "failed to resume for " + _dst);
                    }
                    if (size_of_dst == size_of_src) {
                        return;
                    }
                }
                if (monitor != null) {
                    monitor.init(1, _src, _dst, attr.getSize());
                    if (mode == 1) {
                        monitor.count(_dstFile.length());
                    }
                }
                FileOutputStream fos = null;
                _dstExist = _dstFile.exists();
                try {
                    fos = mode == 0 ? new FileOutputStream(_dst) : new FileOutputStream(_dst, true);
                    this._get(_src, fos, monitor, mode, new File(_dst).length());
                    continue;
                }
                finally {
                    if (fos != null) {
                        fos.close();
                    }
                }
            }
        }
        catch (Exception e) {
            File _dstFile;
            if (!_dstExist && _dst != null && (_dstFile = new File(_dst)).exists() && _dstFile.length() == 0L) {
                _dstFile.delete();
            }
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public void get(String src, OutputStream dst) throws SftpException {
        this.get(src, dst, null, 0, 0L);
    }

    public void get(String src, OutputStream dst, SftpProgressMonitor monitor) throws SftpException {
        this.get(src, dst, monitor, 0, 0L);
    }

    public void get(String src, OutputStream dst, SftpProgressMonitor monitor, int mode, long skip) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            src = this.remoteAbsolutePath(src);
            src = this.isUnique(src);
            if (monitor != null) {
                SftpATTRS attr = this._stat(src);
                monitor.init(1, src, "??", attr.getSize());
                if (mode == 1) {
                    monitor.count(skip);
                }
            }
            this._get(src, dst, monitor, mode, skip);
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    private void _get(String src, OutputStream dst, SftpProgressMonitor monitor, int mode, long skip) throws SftpException {
        byte[] srcb = Util.str2byte(src, this.fEncoding);
        try {
            this.sendOPENR(srcb);
            Header header = new Header();
            header = this.header(this.buf, header);
            int length = header.length;
            int type = header.type;
            this.fill(this.buf, length);
            if (type != 101 && type != 102) {
                throw new SftpException(4, "");
            }
            if (type == 101) {
                int i = this.buf.getInt();
                this.throwStatusError(this.buf, i);
            }
            byte[] handle = this.buf.getString();
            long offset = 0L;
            if (mode == 1) {
                offset += skip;
            }
            int request_max = 1;
            this.rq.init();
            long request_offset = offset;
            int request_len = this.buf.buffer.length - 13;
            if (this.server_version == 0) {
                request_len = 1024;
            }
            block4: while (true) {
                int data_len;
                if (this.rq.count() < request_max) {
                    this.sendREAD(handle, request_offset, request_len, this.rq);
                    request_offset += (long)request_len;
                    continue;
                }
                header = this.header(this.buf, header);
                length = header.length;
                type = header.type;
                RequestQueue.Request rr = null;
                try {
                    rr = this.rq.get(header.rid);
                }
                catch (RequestQueue.OutOfOrderException e) {
                    request_offset = e.offset;
                    this.skip(header.length);
                    this.rq.cancel(header, this.buf);
                    continue;
                }
                if (type == 101) {
                    this.fill(this.buf, length);
                    int i = this.buf.getInt();
                    if (i == 1) break;
                    this.throwStatusError(this.buf, i);
                }
                if (type != 103) break;
                this.buf.rewind();
                this.fill(this.buf.buffer, 0, 4);
                int length_of_data = this.buf.getInt();
                int optional_data = (length -= 4) - length_of_data;
                for (int foo = length_of_data; foo > 0; foo -= data_len) {
                    int bar = foo;
                    if (bar > this.buf.buffer.length) {
                        bar = this.buf.buffer.length;
                    }
                    if ((data_len = this.io_in.read(this.buf.buffer, 0, bar)) < 0) break block4;
                    dst.write(this.buf.buffer, 0, data_len);
                    offset += (long)data_len;
                    if (monitor == null || monitor.count(data_len)) continue;
                    this.skip(foo);
                    if (optional_data <= 0) break block4;
                    this.skip(optional_data);
                    break block4;
                }
                if (optional_data > 0) {
                    this.skip(optional_data);
                }
                if ((long)length_of_data < rr.length) {
                    this.rq.cancel(header, this.buf);
                    this.sendREAD(handle, rr.offset + (long)length_of_data, (int)(rr.length - (long)length_of_data), this.rq);
                    request_offset = rr.offset + rr.length;
                }
                if (request_max >= this.rq.size()) continue;
                ++request_max;
            }
            dst.flush();
            if (monitor != null) {
                monitor.end();
            }
            this.rq.cancel(header, this.buf);
            this._sendCLOSE(handle, header);
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public InputStream get(String src) throws SftpException {
        return this.get(src, null, 0L);
    }

    public InputStream get(String src, SftpProgressMonitor monitor) throws SftpException {
        return this.get(src, monitor, 0L);
    }

    public InputStream get(String src, int mode) throws SftpException {
        return this.get(src, null, 0L);
    }

    public InputStream get(String src, SftpProgressMonitor monitor, int mode) throws SftpException {
        return this.get(src, monitor, 0L);
    }

    public InputStream get(String src, final SftpProgressMonitor monitor, final long skip) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            src = this.remoteAbsolutePath(src);
            src = this.isUnique(src);
            byte[] srcb = Util.str2byte(src, this.fEncoding);
            SftpATTRS attr = this._stat(srcb);
            if (monitor != null) {
                monitor.init(1, src, "??", attr.getSize());
            }
            this.sendOPENR(srcb);
            Header header = new Header();
            header = this.header(this.buf, header);
            int length = header.length;
            int type = header.type;
            this.fill(this.buf, length);
            if (type != 101 && type != 102) {
                throw new SftpException(4, "");
            }
            if (type == 101) {
                int i = this.buf.getInt();
                this.throwStatusError(this.buf, i);
            }
            final byte[] handle = this.buf.getString();
            this.rq.init();
            InputStream in = new InputStream(){
                long offset;
                boolean closed;
                int rest_length;
                byte[] _data;
                byte[] rest_byte;
                Header header;
                int request_max;
                long request_offset;
                {
                    this.offset = skip;
                    this.closed = false;
                    this.rest_length = 0;
                    this._data = new byte[1];
                    this.rest_byte = new byte[1024];
                    this.header = new Header();
                    this.request_max = 1;
                    this.request_offset = this.offset;
                }

                public int read() throws IOException {
                    if (this.closed) {
                        return -1;
                    }
                    int i = this.read(this._data, 0, 1);
                    if (i == -1) {
                        return -1;
                    }
                    return this._data[0] & 0xFF;
                }

                public int read(byte[] d) throws IOException {
                    if (this.closed) {
                        return -1;
                    }
                    return this.read(d, 0, d.length);
                }

                public int read(byte[] d, int s, int len) throws IOException {
                    if (this.closed) {
                        return -1;
                    }
                    if (d == null) {
                        throw new NullPointerException();
                    }
                    if (s < 0 || len < 0 || s + len > d.length) {
                        throw new IndexOutOfBoundsException();
                    }
                    if (len == 0) {
                        return 0;
                    }
                    if (this.rest_length > 0) {
                        int foo = this.rest_length;
                        if (foo > len) {
                            foo = len;
                        }
                        System.arraycopy(this.rest_byte, 0, d, s, foo);
                        if (foo != this.rest_length) {
                            System.arraycopy(this.rest_byte, foo, this.rest_byte, 0, this.rest_length - foo);
                        }
                        if (monitor != null && !monitor.count(foo)) {
                            this.close();
                            return -1;
                        }
                        this.rest_length -= foo;
                        return foo;
                    }
                    if (((ChannelSftp)ChannelSftp.this).buf.buffer.length - 13 < len) {
                        len = ((ChannelSftp)ChannelSftp.this).buf.buffer.length - 13;
                    }
                    if (ChannelSftp.this.server_version == 0 && len > 1024) {
                        len = 1024;
                    }
                    if (ChannelSftp.this.rq.count() != 0) {
                        // empty if block
                    }
                    int request_len = ((ChannelSftp)ChannelSftp.this).buf.buffer.length - 13;
                    if (ChannelSftp.this.server_version == 0) {
                        request_len = 1024;
                    }
                    while (ChannelSftp.this.rq.count() < this.request_max) {
                        try {
                            ChannelSftp.this.sendREAD(handle, this.request_offset, request_len, ChannelSftp.this.rq);
                        }
                        catch (Exception e) {
                            throw new IOException("error");
                        }
                        this.request_offset += (long)request_len;
                    }
                    this.header = ChannelSftp.this.header(ChannelSftp.this.buf, this.header);
                    this.rest_length = this.header.length;
                    int type = this.header.type;
                    int id = this.header.rid;
                    RequestQueue.Request rr = null;
                    try {
                        rr = ChannelSftp.this.rq.get(this.header.rid);
                    }
                    catch (RequestQueue.OutOfOrderException e) {
                        this.request_offset = e.offset;
                        this.skip(this.header.length);
                        ChannelSftp.this.rq.cancel(this.header, ChannelSftp.this.buf);
                        return 0;
                    }
                    catch (SftpException e) {
                        throw new IOException("error: " + e.toString());
                    }
                    if (type != 101 && type != 103) {
                        throw new IOException("error");
                    }
                    if (type == 101) {
                        ChannelSftp.this.fill(ChannelSftp.this.buf, this.rest_length);
                        int i = ChannelSftp.this.buf.getInt();
                        this.rest_length = 0;
                        if (i == 1) {
                            this.close();
                            return -1;
                        }
                        throw new IOException("error");
                    }
                    ChannelSftp.this.buf.rewind();
                    ChannelSftp.this.fill(((ChannelSftp)ChannelSftp.this).buf.buffer, 0, 4);
                    int length_of_data = ChannelSftp.this.buf.getInt();
                    this.rest_length -= 4;
                    int optional_data = this.rest_length - length_of_data;
                    this.offset += (long)length_of_data;
                    int foo = length_of_data;
                    if (foo > 0) {
                        int i;
                        int bar = foo;
                        if (bar > len) {
                            bar = len;
                        }
                        if ((i = ChannelSftp.this.io_in.read(d, s, bar)) < 0) {
                            return -1;
                        }
                        this.rest_length = foo -= i;
                        if (foo > 0) {
                            int j;
                            if (this.rest_byte.length < foo) {
                                this.rest_byte = new byte[foo];
                            }
                            int _s = 0;
                            for (int _len = foo; _len > 0 && (j = ChannelSftp.this.io_in.read(this.rest_byte, _s, _len)) > 0; _len -= j) {
                                _s += j;
                            }
                        }
                        if (optional_data > 0) {
                            ChannelSftp.this.io_in.skip(optional_data);
                        }
                        if ((long)length_of_data < rr.length) {
                            ChannelSftp.this.rq.cancel(this.header, ChannelSftp.this.buf);
                            try {
                                ChannelSftp.this.sendREAD(handle, rr.offset + (long)length_of_data, (int)(rr.length - (long)length_of_data), ChannelSftp.this.rq);
                            }
                            catch (Exception e) {
                                throw new IOException("error");
                            }
                            this.request_offset = rr.offset + rr.length;
                        }
                        if (this.request_max < ChannelSftp.this.rq.size()) {
                            ++this.request_max;
                        }
                        if (monitor != null && !monitor.count(i)) {
                            this.close();
                            return -1;
                        }
                        return i;
                    }
                    return 0;
                }

                public void close() throws IOException {
                    if (this.closed) {
                        return;
                    }
                    this.closed = true;
                    if (monitor != null) {
                        monitor.end();
                    }
                    ChannelSftp.this.rq.cancel(this.header, ChannelSftp.this.buf);
                    try {
                        ChannelSftp.this._sendCLOSE(handle, this.header);
                    }
                    catch (Exception e) {
                        throw new IOException("error");
                    }
                }
            };
            return in;
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public Vector ls(String path) throws SftpException {
        final Vector v = new Vector();
        LsEntrySelector selector = new LsEntrySelector(){

            public int select(LsEntry entry) {
                v.addElement(entry);
                return 0;
            }
        };
        this.ls(path, selector);
        return v;
    }

    public void ls(String path, LsEntrySelector selector) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            path = this.remoteAbsolutePath(path);
            byte[] pattern = null;
            Vector v = new Vector();
            int foo = path.lastIndexOf(47);
            String dir = path.substring(0, foo == 0 ? 1 : foo);
            String _pattern = path.substring(foo + 1);
            dir = Util.unquote(dir);
            byte[][] _pattern_utf8 = new byte[1][];
            boolean pattern_has_wildcard = this.isPattern(_pattern, _pattern_utf8);
            if (pattern_has_wildcard) {
                pattern = _pattern_utf8[0];
            } else {
                String upath = Util.unquote(path);
                SftpATTRS attr = this._stat(upath);
                if (attr.isDir()) {
                    pattern = null;
                    dir = upath;
                } else if (this.fEncoding_is_utf8) {
                    pattern = _pattern_utf8[0];
                    pattern = Util.unquote(pattern);
                } else {
                    _pattern = Util.unquote(_pattern);
                    pattern = Util.str2byte(_pattern, this.fEncoding);
                }
            }
            this.sendOPENDIR(Util.str2byte(dir, this.fEncoding));
            Header header = new Header();
            header = this.header(this.buf, header);
            int length = header.length;
            int type = header.type;
            this.fill(this.buf, length);
            if (type != 101 && type != 102) {
                throw new SftpException(4, "");
            }
            if (type == 101) {
                int i = this.buf.getInt();
                this.throwStatusError(this.buf, i);
            }
            int cancel = 0;
            byte[] handle = this.buf.getString();
            while (cancel == 0) {
                this.sendREADDIR(handle);
                header = this.header(this.buf, header);
                length = header.length;
                type = header.type;
                if (type != 101 && type != 104) {
                    throw new SftpException(4, "");
                }
                if (type == 101) {
                    this.fill(this.buf, length);
                    int i = this.buf.getInt();
                    if (i == 1) break;
                    this.throwStatusError(this.buf, i);
                }
                this.buf.rewind();
                this.fill(this.buf.buffer, 0, 4);
                length -= 4;
                int count = this.buf.getInt();
                this.buf.reset();
                while (count > 0) {
                    if (length > 0) {
                        this.buf.shift();
                        int j = this.buf.buffer.length > this.buf.index + length ? length : this.buf.buffer.length - this.buf.index;
                        int i = this.fill(this.buf.buffer, this.buf.index, j);
                        this.buf.index += i;
                        length -= i;
                    }
                    byte[] filename = this.buf.getString();
                    byte[] longname = null;
                    if (this.server_version <= 3) {
                        longname = this.buf.getString();
                    }
                    SftpATTRS attrs = SftpATTRS.getATTR(this.buf);
                    if (cancel == 1) {
                        --count;
                        continue;
                    }
                    boolean find = false;
                    String f = null;
                    if (pattern == null) {
                        find = true;
                    } else if (!pattern_has_wildcard) {
                        find = Util.array_equals(pattern, filename);
                    } else {
                        byte[] _filename = filename;
                        if (!this.fEncoding_is_utf8) {
                            f = Util.byte2str(_filename, this.fEncoding);
                            _filename = Util.str2byte(f, UTF8);
                        }
                        find = Util.glob(pattern, _filename);
                    }
                    if (find) {
                        if (f == null) {
                            f = Util.byte2str(filename, this.fEncoding);
                        }
                        String l = null;
                        l = longname == null ? attrs.toString() + " " + f : Util.byte2str(longname, this.fEncoding);
                        cancel = selector.select(new LsEntry(f, l, attrs));
                    }
                    --count;
                }
            }
            this._sendCLOSE(handle, header);
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public String readlink(String path) throws SftpException {
        try {
            if (this.server_version < 3) {
                throw new SftpException(8, "The remote sshd is too old to support symlink operation.");
            }
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            path = this.remoteAbsolutePath(path);
            path = this.isUnique(path);
            this.sendREADLINK(Util.str2byte(path, this.fEncoding));
            Header header = new Header();
            header = this.header(this.buf, header);
            int length = header.length;
            int type = header.type;
            this.fill(this.buf, length);
            if (type != 101 && type != 104) {
                throw new SftpException(4, "");
            }
            if (type == 104) {
                int count = this.buf.getInt();
                byte[] filename = null;
                for (int i = 0; i < count; ++i) {
                    filename = this.buf.getString();
                    if (this.server_version <= 3) {
                        byte[] longname = this.buf.getString();
                    }
                    SftpATTRS.getATTR(this.buf);
                }
                return Util.byte2str(filename, this.fEncoding);
            }
            int i = this.buf.getInt();
            this.throwStatusError(this.buf, i);
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
        return null;
    }

    public void symlink(String oldpath, String newpath) throws SftpException {
        if (this.server_version < 3) {
            throw new SftpException(8, "The remote sshd is too old to support symlink operation.");
        }
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            String _oldpath = this.remoteAbsolutePath(oldpath);
            newpath = this.remoteAbsolutePath(newpath);
            _oldpath = this.isUnique(_oldpath);
            if (oldpath.charAt(0) != '/') {
                String cwd = this.getCwd();
                oldpath = _oldpath.substring(cwd.length() + (cwd.endsWith("/") ? 0 : 1));
            } else {
                oldpath = _oldpath;
            }
            if (this.isPattern(newpath)) {
                throw new SftpException(4, newpath);
            }
            newpath = Util.unquote(newpath);
            this.sendSYMLINK(Util.str2byte(oldpath, this.fEncoding), Util.str2byte(newpath, this.fEncoding));
            Header header = new Header();
            header = this.header(this.buf, header);
            int length = header.length;
            int type = header.type;
            this.fill(this.buf, length);
            if (type != 101) {
                throw new SftpException(4, "");
            }
            int i = this.buf.getInt();
            if (i == 0) {
                return;
            }
            this.throwStatusError(this.buf, i);
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public void hardlink(String oldpath, String newpath) throws SftpException {
        if (!this.extension_hardlink) {
            throw new SftpException(8, "hardlink@openssh.com is not supported");
        }
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            String _oldpath = this.remoteAbsolutePath(oldpath);
            newpath = this.remoteAbsolutePath(newpath);
            _oldpath = this.isUnique(_oldpath);
            if (oldpath.charAt(0) != '/') {
                String cwd = this.getCwd();
                oldpath = _oldpath.substring(cwd.length() + (cwd.endsWith("/") ? 0 : 1));
            } else {
                oldpath = _oldpath;
            }
            if (this.isPattern(newpath)) {
                throw new SftpException(4, newpath);
            }
            newpath = Util.unquote(newpath);
            this.sendHARDLINK(Util.str2byte(oldpath, this.fEncoding), Util.str2byte(newpath, this.fEncoding));
            Header header = new Header();
            header = this.header(this.buf, header);
            int length = header.length;
            int type = header.type;
            this.fill(this.buf, length);
            if (type != 101) {
                throw new SftpException(4, "");
            }
            int i = this.buf.getInt();
            if (i == 0) {
                return;
            }
            this.throwStatusError(this.buf, i);
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public void rename(String oldpath, String newpath) throws SftpException {
        if (this.server_version < 2) {
            throw new SftpException(8, "The remote sshd is too old to support rename operation.");
        }
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            oldpath = this.remoteAbsolutePath(oldpath);
            newpath = this.remoteAbsolutePath(newpath);
            oldpath = this.isUnique(oldpath);
            Vector v = this.glob_remote(newpath);
            int vsize = v.size();
            if (vsize >= 2) {
                throw new SftpException(4, v.toString());
            }
            if (vsize == 1) {
                newpath = (String)v.elementAt(0);
            } else {
                if (this.isPattern(newpath)) {
                    throw new SftpException(4, newpath);
                }
                newpath = Util.unquote(newpath);
            }
            this.sendRENAME(Util.str2byte(oldpath, this.fEncoding), Util.str2byte(newpath, this.fEncoding));
            Header header = new Header();
            header = this.header(this.buf, header);
            int length = header.length;
            int type = header.type;
            this.fill(this.buf, length);
            if (type != 101) {
                throw new SftpException(4, "");
            }
            int i = this.buf.getInt();
            if (i == 0) {
                return;
            }
            this.throwStatusError(this.buf, i);
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public void rm(String path) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            path = this.remoteAbsolutePath(path);
            Vector v = this.glob_remote(path);
            int vsize = v.size();
            Header header = new Header();
            for (int j = 0; j < vsize; ++j) {
                path = (String)v.elementAt(j);
                this.sendREMOVE(Util.str2byte(path, this.fEncoding));
                header = this.header(this.buf, header);
                int length = header.length;
                int type = header.type;
                this.fill(this.buf, length);
                if (type != 101) {
                    throw new SftpException(4, "");
                }
                int i = this.buf.getInt();
                if (i == 0) continue;
                this.throwStatusError(this.buf, i);
            }
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    private boolean isRemoteDir(String path) {
        try {
            this.sendSTAT(Util.str2byte(path, this.fEncoding));
            Header header = new Header();
            header = this.header(this.buf, header);
            int length = header.length;
            int type = header.type;
            this.fill(this.buf, length);
            if (type != 105) {
                return false;
            }
            SftpATTRS attr = SftpATTRS.getATTR(this.buf);
            return attr.isDir();
        }
        catch (Exception exception) {
            return false;
        }
    }

    public void chgrp(int gid, String path) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            path = this.remoteAbsolutePath(path);
            Vector v = this.glob_remote(path);
            int vsize = v.size();
            for (int j = 0; j < vsize; ++j) {
                path = (String)v.elementAt(j);
                SftpATTRS attr = this._stat(path);
                attr.setFLAGS(0);
                attr.setUIDGID(attr.uid, gid);
                this._setStat(path, attr);
            }
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public void chown(int uid, String path) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            path = this.remoteAbsolutePath(path);
            Vector v = this.glob_remote(path);
            int vsize = v.size();
            for (int j = 0; j < vsize; ++j) {
                path = (String)v.elementAt(j);
                SftpATTRS attr = this._stat(path);
                attr.setFLAGS(0);
                attr.setUIDGID(uid, attr.gid);
                this._setStat(path, attr);
            }
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public void chmod(int permissions, String path) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            path = this.remoteAbsolutePath(path);
            Vector v = this.glob_remote(path);
            int vsize = v.size();
            for (int j = 0; j < vsize; ++j) {
                path = (String)v.elementAt(j);
                SftpATTRS attr = this._stat(path);
                attr.setFLAGS(0);
                attr.setPERMISSIONS(permissions);
                this._setStat(path, attr);
            }
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public void setMtime(String path, int mtime) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            path = this.remoteAbsolutePath(path);
            Vector v = this.glob_remote(path);
            int vsize = v.size();
            for (int j = 0; j < vsize; ++j) {
                path = (String)v.elementAt(j);
                SftpATTRS attr = this._stat(path);
                attr.setFLAGS(0);
                attr.setACMODTIME(attr.getATime(), mtime);
                this._setStat(path, attr);
            }
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public void rmdir(String path) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            path = this.remoteAbsolutePath(path);
            Vector v = this.glob_remote(path);
            int vsize = v.size();
            Header header = new Header();
            for (int j = 0; j < vsize; ++j) {
                path = (String)v.elementAt(j);
                this.sendRMDIR(Util.str2byte(path, this.fEncoding));
                header = this.header(this.buf, header);
                int length = header.length;
                int type = header.type;
                this.fill(this.buf, length);
                if (type != 101) {
                    throw new SftpException(4, "");
                }
                int i = this.buf.getInt();
                if (i == 0) continue;
                this.throwStatusError(this.buf, i);
            }
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public void mkdir(String path) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            path = this.remoteAbsolutePath(path);
            this.sendMKDIR(Util.str2byte(path, this.fEncoding), null);
            Header header = new Header();
            header = this.header(this.buf, header);
            int length = header.length;
            int type = header.type;
            this.fill(this.buf, length);
            if (type != 101) {
                throw new SftpException(4, "");
            }
            int i = this.buf.getInt();
            if (i == 0) {
                return;
            }
            this.throwStatusError(this.buf, i);
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public SftpATTRS stat(String path) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            path = this.remoteAbsolutePath(path);
            path = this.isUnique(path);
            return this._stat(path);
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    private SftpATTRS _stat(byte[] path) throws SftpException {
        try {
            this.sendSTAT(path);
            Header header = new Header();
            header = this.header(this.buf, header);
            int length = header.length;
            int type = header.type;
            this.fill(this.buf, length);
            if (type != 105) {
                if (type == 101) {
                    int i = this.buf.getInt();
                    this.throwStatusError(this.buf, i);
                }
                throw new SftpException(4, "");
            }
            SftpATTRS attr = SftpATTRS.getATTR(this.buf);
            return attr;
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    private SftpATTRS _stat(String path) throws SftpException {
        return this._stat(Util.str2byte(path, this.fEncoding));
    }

    public SftpStatVFS statVFS(String path) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            path = this.remoteAbsolutePath(path);
            path = this.isUnique(path);
            return this._statVFS(path);
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    private SftpStatVFS _statVFS(byte[] path) throws SftpException {
        if (!this.extension_statvfs) {
            throw new SftpException(8, "statvfs@openssh.com is not supported");
        }
        try {
            this.sendSTATVFS(path);
            Header header = new Header();
            header = this.header(this.buf, header);
            int length = header.length;
            int type = header.type;
            this.fill(this.buf, length);
            if (type != 201) {
                if (type == 101) {
                    int i = this.buf.getInt();
                    this.throwStatusError(this.buf, i);
                }
                throw new SftpException(4, "");
            }
            SftpStatVFS stat = SftpStatVFS.getStatVFS(this.buf);
            return stat;
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    private SftpStatVFS _statVFS(String path) throws SftpException {
        return this._statVFS(Util.str2byte(path, this.fEncoding));
    }

    public SftpATTRS lstat(String path) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            path = this.remoteAbsolutePath(path);
            path = this.isUnique(path);
            return this._lstat(path);
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    private SftpATTRS _lstat(String path) throws SftpException {
        try {
            this.sendLSTAT(Util.str2byte(path, this.fEncoding));
            Header header = new Header();
            header = this.header(this.buf, header);
            int length = header.length;
            int type = header.type;
            this.fill(this.buf, length);
            if (type != 105) {
                if (type == 101) {
                    int i = this.buf.getInt();
                    this.throwStatusError(this.buf, i);
                }
                throw new SftpException(4, "");
            }
            SftpATTRS attr = SftpATTRS.getATTR(this.buf);
            return attr;
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    private byte[] _realpath(String path) throws SftpException, IOException, Exception {
        int i;
        this.sendREALPATH(Util.str2byte(path, this.fEncoding));
        Header header = new Header();
        header = this.header(this.buf, header);
        int length = header.length;
        int type = header.type;
        this.fill(this.buf, length);
        if (type != 101 && type != 104) {
            throw new SftpException(4, "");
        }
        if (type == 101) {
            i = this.buf.getInt();
            this.throwStatusError(this.buf, i);
        }
        i = this.buf.getInt();
        byte[] str = null;
        while (i-- > 0) {
            str = this.buf.getString();
            if (this.server_version <= 3) {
                byte[] lname = this.buf.getString();
            }
            SftpATTRS attr = SftpATTRS.getATTR(this.buf);
        }
        return str;
    }

    public void setStat(String path, SftpATTRS attr) throws SftpException {
        try {
            ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
            path = this.remoteAbsolutePath(path);
            Vector v = this.glob_remote(path);
            int vsize = v.size();
            for (int j = 0; j < vsize; ++j) {
                path = (String)v.elementAt(j);
                this._setStat(path, attr);
            }
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    private void _setStat(String path, SftpATTRS attr) throws SftpException {
        try {
            this.sendSETSTAT(Util.str2byte(path, this.fEncoding), attr);
            Header header = new Header();
            header = this.header(this.buf, header);
            int length = header.length;
            int type = header.type;
            this.fill(this.buf, length);
            if (type != 101) {
                throw new SftpException(4, "");
            }
            int i = this.buf.getInt();
            if (i != 0) {
                this.throwStatusError(this.buf, i);
            }
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public String pwd() throws SftpException {
        return this.getCwd();
    }

    public String lpwd() {
        return this.lcwd;
    }

    public String version() {
        return this.version;
    }

    public String getHome() throws SftpException {
        if (this.home == null) {
            try {
                ((Channel.MyPipedInputStream)this.io_in).updateReadSide();
                byte[] _home = this._realpath("");
                this.home = Util.byte2str(_home, this.fEncoding);
            }
            catch (Exception e) {
                if (e instanceof SftpException) {
                    throw (SftpException)e;
                }
                if (e instanceof Throwable) {
                    throw new SftpException(4, "", e);
                }
                throw new SftpException(4, "");
            }
        }
        return this.home;
    }

    private String getCwd() throws SftpException {
        if (this.cwd == null) {
            this.cwd = this.getHome();
        }
        return this.cwd;
    }

    private void setCwd(String cwd) {
        this.cwd = cwd;
    }

    private void read(byte[] buf, int s, int l) throws IOException, SftpException {
        int i = 0;
        while (l > 0) {
            i = this.io_in.read(buf, s, l);
            if (i <= 0) {
                throw new SftpException(4, "");
            }
            s += i;
            l -= i;
        }
    }

    private boolean checkStatus(int[] ackid, Header header) throws IOException, SftpException {
        header = this.header(this.buf, header);
        int length = header.length;
        int type = header.type;
        if (ackid != null) {
            ackid[0] = header.rid;
        }
        this.fill(this.buf, length);
        if (type != 101) {
            throw new SftpException(4, "");
        }
        int i = this.buf.getInt();
        if (i != 0) {
            this.throwStatusError(this.buf, i);
        }
        return true;
    }

    private boolean _sendCLOSE(byte[] handle, Header header) throws Exception {
        this.sendCLOSE(handle);
        return this.checkStatus(null, header);
    }

    private void sendINIT() throws Exception {
        this.packet.reset();
        this.putHEAD((byte)1, 5);
        this.buf.putInt(3);
        this.getSession().write(this.packet, this, 9);
    }

    private void sendREALPATH(byte[] path) throws Exception {
        this.sendPacketPath((byte)16, path);
    }

    private void sendSTAT(byte[] path) throws Exception {
        this.sendPacketPath((byte)17, path);
    }

    private void sendSTATVFS(byte[] path) throws Exception {
        this.sendPacketPath((byte)0, path, "statvfs@openssh.com");
    }

    private void sendLSTAT(byte[] path) throws Exception {
        this.sendPacketPath((byte)7, path);
    }

    private void sendFSTAT(byte[] handle) throws Exception {
        this.sendPacketPath((byte)8, handle);
    }

    private void sendSETSTAT(byte[] path, SftpATTRS attr) throws Exception {
        this.packet.reset();
        this.putHEAD((byte)9, 9 + path.length + attr.length());
        this.buf.putInt(this.seq++);
        this.buf.putString(path);
        attr.dump(this.buf);
        this.getSession().write(this.packet, this, 9 + path.length + attr.length() + 4);
    }

    private void sendREMOVE(byte[] path) throws Exception {
        this.sendPacketPath((byte)13, path);
    }

    private void sendMKDIR(byte[] path, SftpATTRS attr) throws Exception {
        this.packet.reset();
        this.putHEAD((byte)14, 9 + path.length + (attr != null ? attr.length() : 4));
        this.buf.putInt(this.seq++);
        this.buf.putString(path);
        if (attr != null) {
            attr.dump(this.buf);
        } else {
            this.buf.putInt(0);
        }
        this.getSession().write(this.packet, this, 9 + path.length + (attr != null ? attr.length() : 4) + 4);
    }

    private void sendRMDIR(byte[] path) throws Exception {
        this.sendPacketPath((byte)15, path);
    }

    private void sendSYMLINK(byte[] p1, byte[] p2) throws Exception {
        this.sendPacketPath((byte)20, p1, p2);
    }

    private void sendHARDLINK(byte[] p1, byte[] p2) throws Exception {
        this.sendPacketPath((byte)0, p1, p2, "hardlink@openssh.com");
    }

    private void sendREADLINK(byte[] path) throws Exception {
        this.sendPacketPath((byte)19, path);
    }

    private void sendOPENDIR(byte[] path) throws Exception {
        this.sendPacketPath((byte)11, path);
    }

    private void sendREADDIR(byte[] path) throws Exception {
        this.sendPacketPath((byte)12, path);
    }

    private void sendRENAME(byte[] p1, byte[] p2) throws Exception {
        this.sendPacketPath((byte)18, p1, p2, this.extension_posix_rename ? "posix-rename@openssh.com" : null);
    }

    private void sendCLOSE(byte[] path) throws Exception {
        this.sendPacketPath((byte)4, path);
    }

    private void sendOPENR(byte[] path) throws Exception {
        this.sendOPEN(path, 1);
    }

    private void sendOPENW(byte[] path) throws Exception {
        this.sendOPEN(path, 26);
    }

    private void sendOPENA(byte[] path) throws Exception {
        this.sendOPEN(path, 10);
    }

    private void sendOPEN(byte[] path, int mode) throws Exception {
        this.packet.reset();
        this.putHEAD((byte)3, 17 + path.length);
        this.buf.putInt(this.seq++);
        this.buf.putString(path);
        this.buf.putInt(mode);
        this.buf.putInt(0);
        this.getSession().write(this.packet, this, 17 + path.length + 4);
    }

    private void sendPacketPath(byte fxp, byte[] path) throws Exception {
        this.sendPacketPath(fxp, path, (String)null);
    }

    private void sendPacketPath(byte fxp, byte[] path, String extension) throws Exception {
        this.packet.reset();
        int len = 9 + path.length;
        if (extension == null) {
            this.putHEAD(fxp, len);
            this.buf.putInt(this.seq++);
        } else {
            this.putHEAD((byte)-56, len += 4 + extension.length());
            this.buf.putInt(this.seq++);
            this.buf.putString(Util.str2byte(extension));
        }
        this.buf.putString(path);
        this.getSession().write(this.packet, this, len + 4);
    }

    private void sendPacketPath(byte fxp, byte[] p1, byte[] p2) throws Exception {
        this.sendPacketPath(fxp, p1, p2, null);
    }

    private void sendPacketPath(byte fxp, byte[] p1, byte[] p2, String extension) throws Exception {
        this.packet.reset();
        int len = 13 + p1.length + p2.length;
        if (extension == null) {
            this.putHEAD(fxp, len);
            this.buf.putInt(this.seq++);
        } else {
            this.putHEAD((byte)-56, len += 4 + extension.length());
            this.buf.putInt(this.seq++);
            this.buf.putString(Util.str2byte(extension));
        }
        this.buf.putString(p1);
        this.buf.putString(p2);
        this.getSession().write(this.packet, this, len + 4);
    }

    private int sendWRITE(byte[] handle, long offset, byte[] data, int start, int length) throws Exception {
        int _length = length;
        this.opacket.reset();
        if (this.obuf.buffer.length < this.obuf.index + 13 + 21 + handle.length + length + 128) {
            _length = this.obuf.buffer.length - (this.obuf.index + 13 + 21 + handle.length + 128);
        }
        this.putHEAD(this.obuf, (byte)6, 21 + handle.length + _length);
        this.obuf.putInt(this.seq++);
        this.obuf.putString(handle);
        this.obuf.putLong(offset);
        if (this.obuf.buffer != data) {
            this.obuf.putString(data, start, _length);
        } else {
            this.obuf.putInt(_length);
            this.obuf.skip(_length);
        }
        this.getSession().write(this.opacket, this, 21 + handle.length + _length + 4);
        return _length;
    }

    private void sendREAD(byte[] handle, long offset, int length) throws Exception {
        this.sendREAD(handle, offset, length, null);
    }

    private void sendREAD(byte[] handle, long offset, int length, RequestQueue rrq) throws Exception {
        this.packet.reset();
        this.putHEAD((byte)5, 21 + handle.length);
        this.buf.putInt(this.seq++);
        this.buf.putString(handle);
        this.buf.putLong(offset);
        this.buf.putInt(length);
        this.getSession().write(this.packet, this, 21 + handle.length + 4);
        if (rrq != null) {
            rrq.add(this.seq - 1, offset, length);
        }
    }

    private void putHEAD(Buffer buf, byte type, int length) throws Exception {
        buf.putByte((byte)94);
        buf.putInt(this.recipient);
        buf.putInt(length + 4);
        buf.putInt(length);
        buf.putByte(type);
    }

    private void putHEAD(byte type, int length) throws Exception {
        this.putHEAD(this.buf, type, length);
    }

    /*
     * Unable to fully structure code
     */
    private Vector glob_remote(String _path) throws Exception {
        v = new Vector<String>();
        i = 0;
        foo = _path.lastIndexOf(47);
        if (foo < 0) {
            v.addElement(Util.unquote(_path));
            return v;
        }
        dir = _path.substring(0, foo == 0 ? 1 : foo);
        _pattern = _path.substring(foo + 1);
        dir = Util.unquote(dir);
        pattern = null;
        _pattern_utf8 = new byte[1][];
        pattern_has_wildcard = this.isPattern(_pattern, _pattern_utf8);
        if (!pattern_has_wildcard) {
            if (!dir.equals("/")) {
                dir = dir + "/";
            }
            v.addElement(dir + Util.unquote(_pattern));
            return v;
        }
        pattern = _pattern_utf8[0];
        this.sendOPENDIR(Util.str2byte(dir, this.fEncoding));
        header = new Header();
        header = this.header(this.buf, header);
        length = header.length;
        type = header.type;
        this.fill(this.buf, length);
        if (type != 101 && type != 102) {
            throw new SftpException(4, "");
        }
        if (type == 101) {
            i = this.buf.getInt();
            this.throwStatusError(this.buf, i);
        }
        handle = this.buf.getString();
        pdir = null;
        block0: while (true) {
            this.sendREADDIR(handle);
            header = this.header(this.buf, header);
            length = header.length;
            type = header.type;
            if (type != 101 && type != 104) {
                throw new SftpException(4, "");
            }
            if (type == 101) break;
            this.buf.rewind();
            this.fill(this.buf.buffer, 0, 4);
            length -= 4;
            count = this.buf.getInt();
            this.buf.reset();
            while (true) {
                if (count <= 0) continue block0;
                if (length > 0) {
                    this.buf.shift();
                    j = this.buf.buffer.length > this.buf.index + length ? length : this.buf.buffer.length - this.buf.index;
                    i = this.io_in.read(this.buf.buffer, this.buf.index, j);
                    if (i <= 0) ** break;
                    this.buf.index += i;
                    length -= i;
                }
                filename = this.buf.getString();
                if (this.server_version <= 3) {
                    var16_16 = this.buf.getString();
                }
                attrs = SftpATTRS.getATTR(this.buf);
                _filename = filename;
                f = null;
                found = false;
                if (!this.fEncoding_is_utf8) {
                    f = Util.byte2str(filename, this.fEncoding);
                    _filename = Util.str2byte(f, "UTF-8");
                }
                if (found = Util.glob(pattern, _filename)) {
                    if (f == null) {
                        f = Util.byte2str(filename, this.fEncoding);
                    }
                    if (pdir == null && !(pdir = dir).endsWith("/")) {
                        pdir = pdir + "/";
                    }
                    v.addElement(pdir + f);
                }
                --count;
            }
            break;
        }
        this.fill(this.buf, length);
        if (this._sendCLOSE(handle, header)) {
            return v;
        }
        return null;
    }

    private boolean isPattern(byte[] path) {
        int length = path.length;
        for (int i = 0; i < length; ++i) {
            if (path[i] == 42 || path[i] == 63) {
                return true;
            }
            if (path[i] != 92 || i + 1 >= length) continue;
            ++i;
        }
        return false;
    }

    private Vector glob_local(String _path) throws Exception {
        byte[] dir;
        Vector<String> v = new Vector<String>();
        byte[] path = Util.str2byte(_path, UTF8);
        int i = path.length - 1;
        while (i >= 0) {
            if (path[i] != 42 && path[i] != 63) {
                --i;
                continue;
            }
            if (fs_is_bs || i <= 0 || path[i - 1] != 92 || --i <= 0 || path[i - 1] != 92) break;
            --i;
            --i;
        }
        if (i < 0) {
            v.addElement(fs_is_bs ? _path : Util.unquote(_path));
            return v;
        }
        while (!(i < 0 || path[i] == file_separatorc || fs_is_bs && path[i] == 47)) {
            --i;
        }
        if (i < 0) {
            v.addElement(fs_is_bs ? _path : Util.unquote(_path));
            return v;
        }
        if (i == 0) {
            dir = new byte[]{(byte)file_separatorc};
        } else {
            dir = new byte[i];
            System.arraycopy(path, 0, dir, 0, i);
        }
        byte[] pattern = new byte[path.length - i - 1];
        System.arraycopy(path, i + 1, pattern, 0, pattern.length);
        try {
            String[] children = new File(Util.byte2str(dir, UTF8)).list();
            String pdir = Util.byte2str(dir) + file_separator;
            for (int j = 0; j < children.length; ++j) {
                if (!Util.glob(pattern, Util.str2byte(children[j], UTF8))) continue;
                v.addElement(pdir + children[j]);
            }
        }
        catch (Exception e) {
            // empty catch block
        }
        return v;
    }

    private void throwStatusError(Buffer buf, int i) throws SftpException {
        if (this.server_version >= 3 && buf.getLength() >= 4) {
            byte[] str = buf.getString();
            throw new SftpException(i, Util.byte2str(str, UTF8));
        }
        throw new SftpException(i, "Failure");
    }

    private static boolean isLocalAbsolutePath(String path) {
        return new File(path).isAbsolute();
    }

    public void disconnect() {
        super.disconnect();
    }

    private boolean isPattern(String path, byte[][] utf8) {
        byte[] _path = Util.str2byte(path, UTF8);
        if (utf8 != null) {
            utf8[0] = _path;
        }
        return this.isPattern(_path);
    }

    private boolean isPattern(String path) {
        return this.isPattern(path, null);
    }

    private void fill(Buffer buf, int len) throws IOException {
        buf.reset();
        this.fill(buf.buffer, 0, len);
        buf.skip(len);
    }

    private int fill(byte[] buf, int s, int len) throws IOException {
        int i = 0;
        int foo = s;
        while (len > 0) {
            i = this.io_in.read(buf, s, len);
            if (i <= 0) {
                throw new IOException("inputstream is closed");
            }
            s += i;
            len -= i;
        }
        return s - foo;
    }

    private void skip(long foo) throws IOException {
        long bar;
        while (foo > 0L && (bar = this.io_in.skip(foo)) > 0L) {
            foo -= bar;
        }
    }

    private Header header(Buffer buf, Header header) throws IOException {
        buf.rewind();
        int i = this.fill(buf.buffer, 0, 9);
        header.length = buf.getInt() - 5;
        header.type = buf.getByte() & 0xFF;
        header.rid = buf.getInt();
        return header;
    }

    private String remoteAbsolutePath(String path) throws SftpException {
        if (path.charAt(0) == '/') {
            return path;
        }
        String cwd = this.getCwd();
        if (cwd.endsWith("/")) {
            return cwd + path;
        }
        return cwd + "/" + path;
    }

    private String localAbsolutePath(String path) {
        if (ChannelSftp.isLocalAbsolutePath(path)) {
            return path;
        }
        if (this.lcwd.endsWith(file_separator)) {
            return this.lcwd + path;
        }
        return this.lcwd + file_separator + path;
    }

    private String isUnique(String path) throws SftpException, Exception {
        Vector v = this.glob_remote(path);
        if (v.size() != 1) {
            throw new SftpException(4, path + " is not unique: " + v.toString());
        }
        return (String)v.elementAt(0);
    }

    public int getServerVersion() throws SftpException {
        if (!this.isConnected()) {
            throw new SftpException(4, "The channel is not connected.");
        }
        return this.server_version;
    }

    public void setFilenameEncoding(String encoding) throws SftpException {
        int sversion = this.getServerVersion();
        if (3 <= sversion && sversion <= 5 && !encoding.equals(UTF8)) {
            throw new SftpException(4, "The encoding can not be changed for this sftp server.");
        }
        if (encoding.equals(UTF8)) {
            encoding = UTF8;
        }
        this.fEncoding = encoding;
        this.fEncoding_is_utf8 = this.fEncoding.equals(UTF8);
    }

    public String getExtension(String key) {
        if (this.extensions == null) {
            return null;
        }
        return (String)this.extensions.get(key);
    }

    public String realpath(String path) throws SftpException {
        try {
            byte[] _path = this._realpath(this.remoteAbsolutePath(path));
            return Util.byte2str(_path, this.fEncoding);
        }
        catch (Exception e) {
            if (e instanceof SftpException) {
                throw (SftpException)e;
            }
            if (e instanceof Throwable) {
                throw new SftpException(4, "", e);
            }
            throw new SftpException(4, "");
        }
    }

    public static interface LsEntrySelector {
        public static final int CONTINUE = 0;
        public static final int BREAK = 1;

        public int select(LsEntry var1);
    }

    public class LsEntry
    implements Comparable {
        private String filename;
        private String longname;
        private SftpATTRS attrs;

        LsEntry(String filename, String longname, SftpATTRS attrs) {
            this.setFilename(filename);
            this.setLongname(longname);
            this.setAttrs(attrs);
        }

        public String getFilename() {
            return this.filename;
        }

        void setFilename(String filename) {
            this.filename = filename;
        }

        public String getLongname() {
            return this.longname;
        }

        void setLongname(String longname) {
            this.longname = longname;
        }

        public SftpATTRS getAttrs() {
            return this.attrs;
        }

        void setAttrs(SftpATTRS attrs) {
            this.attrs = attrs;
        }

        public String toString() {
            return this.longname;
        }

        public int compareTo(Object o) throws ClassCastException {
            if (o instanceof LsEntry) {
                return this.filename.compareTo(((LsEntry)o).getFilename());
            }
            throw new ClassCastException("a decendent of LsEntry must be given.");
        }
    }

    class Header {
        int length;
        int type;
        int rid;

        Header() {
        }
    }

    private class RequestQueue {
        Request[] rrq = null;
        int head;
        int count;

        RequestQueue(int size) {
            this.rrq = new Request[size];
            for (int i = 0; i < this.rrq.length; ++i) {
                this.rrq[i] = new Request();
            }
            this.init();
        }

        void init() {
            this.count = 0;
            this.head = 0;
        }

        void add(int id, long offset, int length) {
            int tail;
            if (this.count == 0) {
                this.head = 0;
            }
            if ((tail = this.head + this.count) >= this.rrq.length) {
                tail -= this.rrq.length;
            }
            this.rrq[tail].id = id;
            this.rrq[tail].offset = offset;
            this.rrq[tail].length = length;
            ++this.count;
        }

        Request get(int id) throws OutOfOrderException, SftpException {
            --this.count;
            int i = this.head++;
            if (this.head == this.rrq.length) {
                this.head = 0;
            }
            if (this.rrq[i].id != id) {
                long offset = this.getOffset();
                boolean find = false;
                for (int j = 0; j < this.rrq.length; ++j) {
                    if (this.rrq[j].id != id) continue;
                    find = true;
                    this.rrq[j].id = 0;
                    break;
                }
                if (find) {
                    throw new OutOfOrderException(offset);
                }
                throw new SftpException(4, "RequestQueue: unknown request id " + id);
            }
            this.rrq[i].id = 0;
            return this.rrq[i];
        }

        int count() {
            return this.count;
        }

        int size() {
            return this.rrq.length;
        }

        void cancel(Header header, Buffer buf) throws IOException {
            int _count = this.count;
            for (int i = 0; i < _count; ++i) {
                header = ChannelSftp.this.header(buf, header);
                int length = header.length;
                for (int j = 0; j < this.rrq.length; ++j) {
                    if (this.rrq[j].id != header.rid) continue;
                    this.rrq[j].id = 0;
                    break;
                }
                ChannelSftp.this.skip(length);
            }
            this.init();
        }

        long getOffset() {
            long result = Long.MAX_VALUE;
            for (int i = 0; i < this.rrq.length; ++i) {
                if (this.rrq[i].id == 0 || result <= this.rrq[i].offset) continue;
                result = this.rrq[i].offset;
            }
            return result;
        }

        class Request {
            int id;
            long offset;
            long length;

            Request() {
            }
        }

        class OutOfOrderException
        extends Exception {
            long offset;

            OutOfOrderException(long offset) {
                this.offset = offset;
            }
        }
    }
}


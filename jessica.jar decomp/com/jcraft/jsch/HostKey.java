/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.HASH;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Util;

public class HostKey {
    private static final byte[][] names = new byte[][]{Util.str2byte("ssh-dss"), Util.str2byte("ssh-rsa"), Util.str2byte("ecdsa-sha2-nistp256"), Util.str2byte("ecdsa-sha2-nistp384"), Util.str2byte("ecdsa-sha2-nistp521")};
    protected static final int GUESS = 0;
    public static final int SSHDSS = 1;
    public static final int SSHRSA = 2;
    public static final int ECDSA256 = 3;
    public static final int ECDSA384 = 4;
    public static final int ECDSA521 = 5;
    static final int UNKNOWN = 6;
    protected String marker;
    protected String host;
    protected int type;
    protected byte[] key;
    protected String comment;

    public HostKey(String host, byte[] key) throws JSchException {
        this(host, 0, key);
    }

    public HostKey(String host, int type, byte[] key) throws JSchException {
        this(host, type, key, null);
    }

    public HostKey(String host, int type, byte[] key, String comment) throws JSchException {
        this("", host, type, key, comment);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public HostKey(String marker, String host, int type, byte[] key, String comment) throws JSchException {
        this.marker = marker;
        this.host = host;
        if (type == 0) {
            if (key[8] == 100) {
                this.type = 1;
            } else if (key[8] == 114) {
                this.type = 2;
            } else if (key[8] == 97 && key[20] == 50) {
                this.type = 3;
            } else if (key[8] == 97 && key[20] == 51) {
                this.type = 4;
            } else {
                if (key[8] != 97 || key[20] != 53) throw new JSchException("invalid key type");
                this.type = 5;
            }
        } else {
            this.type = type;
        }
        this.key = key;
        this.comment = comment;
    }

    public String getHost() {
        return this.host;
    }

    public String getType() {
        if (this.type == 1 || this.type == 2 || this.type == 3 || this.type == 4 || this.type == 5) {
            return Util.byte2str(names[this.type - 1]);
        }
        return "UNKNOWN";
    }

    protected static int name2type(String name) {
        for (int i = 0; i < names.length; ++i) {
            if (!Util.byte2str(names[i]).equals(name)) continue;
            return i + 1;
        }
        return 6;
    }

    public String getKey() {
        return Util.byte2str(Util.toBase64(this.key, 0, this.key.length));
    }

    public String getFingerPrint(JSch jsch) {
        HASH hash = null;
        try {
            Class<?> c = Class.forName(JSch.getConfig("md5"));
            hash = (HASH)c.newInstance();
        }
        catch (Exception e) {
            System.err.println("getFingerPrint: " + e);
        }
        return Util.getFingerPrint(hash, this.key);
    }

    public String getComment() {
        return this.comment;
    }

    public String getMarker() {
        return this.marker;
    }

    boolean isMatched(String _host) {
        return this.isIncluded(_host);
    }

    private boolean isIncluded(String _host) {
        int i = 0;
        String hosts = this.host;
        int hostslen = hosts.length();
        int hostlen = _host.length();
        while (i < hostslen) {
            int j = hosts.indexOf(44, i);
            if (j == -1) {
                if (hostlen != hostslen - i) {
                    return false;
                }
                return hosts.regionMatches(true, i, _host, 0, hostlen);
            }
            if (hostlen == j - i && hosts.regionMatches(true, i, _host, 0, hostlen)) {
                return true;
            }
            i = j + 1;
        }
        return false;
    }
}


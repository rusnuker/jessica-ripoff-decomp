/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.MAC;
import com.jcraft.jsch.Random;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.jcraft.jsch.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Vector;

public class KnownHosts
implements HostKeyRepository {
    private static final String _known_hosts = "known_hosts";
    private JSch jsch = null;
    private String known_hosts = null;
    private Vector pool = null;
    private MAC hmacsha1 = null;
    private static final byte[] space = new byte[]{32};
    private static final byte[] cr = Util.str2byte("\n");

    KnownHosts(JSch jsch) {
        this.jsch = jsch;
        this.hmacsha1 = this.getHMACSHA1();
        this.pool = new Vector();
    }

    void setKnownHosts(String filename) throws JSchException {
        try {
            this.known_hosts = filename;
            FileInputStream fis = new FileInputStream(Util.checkTilde(filename));
            this.setKnownHosts(fis);
        }
        catch (FileNotFoundException fileNotFoundException) {
            // empty catch block
        }
    }

    void setKnownHosts(InputStream input) throws JSchException {
        this.pool.removeAllElements();
        StringBuffer sb = new StringBuffer();
        boolean error = false;
        try {
            InputStream fis = input;
            String key = null;
            byte[] buf = new byte[1024];
            int bufl = 0;
            block9: while (true) {
                byte i;
                int j;
                bufl = 0;
                while (true) {
                    if ((j = fis.read()) == -1) {
                        if (bufl != 0) break;
                        break block9;
                    }
                    if (j == 13) continue;
                    if (j == 10) break;
                    if (buf.length <= bufl) {
                        if (bufl > 10240) break;
                        byte[] newbuf = new byte[buf.length * 2];
                        System.arraycopy(buf, 0, newbuf, 0, buf.length);
                        buf = newbuf;
                    }
                    buf[bufl++] = (byte)j;
                }
                for (j = 0; j < bufl; ++j) {
                    i = buf[j];
                    if (i == 32 || i == 9) {
                        continue;
                    }
                    if (i != 35) break;
                    this.addInvalidLine(Util.byte2str(buf, 0, bufl));
                    continue block9;
                }
                if (j >= bufl) {
                    this.addInvalidLine(Util.byte2str(buf, 0, bufl));
                    continue;
                }
                sb.setLength(0);
                while (j < bufl && (i = buf[j++]) != 32 && i != 9) {
                    sb.append((char)i);
                }
                String host = sb.toString();
                if (j >= bufl || host.length() == 0) {
                    this.addInvalidLine(Util.byte2str(buf, 0, bufl));
                    continue;
                }
                while (j < bufl && ((i = buf[j]) == 32 || i == 9)) {
                    ++j;
                }
                String marker = "";
                if (host.charAt(0) == '@') {
                    marker = host;
                    sb.setLength(0);
                    while (j < bufl && (i = buf[j++]) != 32 && i != 9) {
                        sb.append((char)i);
                    }
                    host = sb.toString();
                    if (j >= bufl || host.length() == 0) {
                        this.addInvalidLine(Util.byte2str(buf, 0, bufl));
                        continue;
                    }
                    while (j < bufl && ((i = buf[j]) == 32 || i == 9)) {
                        ++j;
                    }
                }
                sb.setLength(0);
                int type = -1;
                while (j < bufl && (i = buf[j++]) != 32 && i != 9) {
                    sb.append((char)i);
                }
                String tmp = sb.toString();
                if (HostKey.name2type(tmp) != 6) {
                    type = HostKey.name2type(tmp);
                } else {
                    j = bufl;
                }
                if (j >= bufl) {
                    this.addInvalidLine(Util.byte2str(buf, 0, bufl));
                    continue;
                }
                while (j < bufl && ((i = buf[j]) == 32 || i == 9)) {
                    ++j;
                }
                sb.setLength(0);
                while (j < bufl) {
                    if ((i = buf[j++]) == 13) continue;
                    if (i == 10 || i == 32 || i == 9) break;
                    sb.append((char)i);
                }
                if ((key = sb.toString()).length() == 0) {
                    this.addInvalidLine(Util.byte2str(buf, 0, bufl));
                    continue;
                }
                while (j < bufl && ((i = buf[j]) == 32 || i == 9)) {
                    ++j;
                }
                String comment = null;
                if (j < bufl) {
                    sb.setLength(0);
                    while (j < bufl) {
                        if ((i = buf[j++]) == 13) continue;
                        if (i == 10) break;
                        sb.append((char)i);
                    }
                    comment = sb.toString();
                }
                HashedHostKey hk = null;
                hk = new HashedHostKey(marker, host, type, Util.fromBase64(Util.str2byte(key), 0, key.length()), comment);
                this.pool.addElement(hk);
            }
            if (error) {
                throw new JSchException("KnownHosts: invalid format");
            }
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
        finally {
            try {
                input.close();
            }
            catch (IOException e) {
                throw new JSchException(e.toString(), e);
            }
        }
    }

    private void addInvalidLine(String line) throws JSchException {
        HostKey hk = new HostKey(line, 6, null);
        this.pool.addElement(hk);
    }

    String getKnownHostsFile() {
        return this.known_hosts;
    }

    public String getKnownHostsRepositoryID() {
        return this.known_hosts;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int check(String host, byte[] key) {
        int result = 1;
        if (host == null) {
            return result;
        }
        HostKey hk = null;
        try {
            hk = new HostKey(host, 0, key);
        }
        catch (JSchException e) {
            return result;
        }
        Vector vector = this.pool;
        synchronized (vector) {
            for (int i = 0; i < this.pool.size(); ++i) {
                HostKey _hk = (HostKey)this.pool.elementAt(i);
                if (!_hk.isMatched(host) || _hk.type != hk.type) continue;
                if (Util.array_equals(_hk.key, key)) {
                    return 0;
                }
                result = 2;
            }
        }
        if (result == 1 && host.startsWith("[") && host.indexOf("]:") > 1) {
            return this.check(host.substring(1, host.indexOf("]:")), key);
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void add(HostKey hostkey, UserInfo userinfo) {
        int type = hostkey.type;
        String host = hostkey.getHost();
        byte[] key = hostkey.key;
        HostKey hk = null;
        Vector vector = this.pool;
        synchronized (vector) {
            for (int i = 0; i < this.pool.size(); ++i) {
                hk = (HostKey)this.pool.elementAt(i);
                if (hk.isMatched(host) && hk.type != type) continue;
            }
        }
        hk = hostkey;
        this.pool.addElement(hk);
        String bar = this.getKnownHostsRepositoryID();
        if (bar != null) {
            boolean foo = true;
            File goo = new File(Util.checkTilde(bar));
            if (!goo.exists()) {
                foo = false;
                if (userinfo != null) {
                    foo = userinfo.promptYesNo(bar + " does not exist.\n" + "Are you sure you want to create it?");
                    goo = goo.getParentFile();
                    if (foo && goo != null && !goo.exists() && (foo = userinfo.promptYesNo("The parent directory " + goo + " does not exist.\n" + "Are you sure you want to create it?"))) {
                        if (!goo.mkdirs()) {
                            userinfo.showMessage(goo + " has not been created.");
                            foo = false;
                        } else {
                            userinfo.showMessage(goo + " has been succesfully created.\nPlease check its access permission.");
                        }
                    }
                    if (goo == null) {
                        foo = false;
                    }
                }
            }
            if (foo) {
                try {
                    this.sync(bar);
                }
                catch (Exception e) {
                    System.err.println("sync known_hosts: " + e);
                }
            }
        }
    }

    public HostKey[] getHostKey() {
        return this.getHostKey(null, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public HostKey[] getHostKey(String host, String type) {
        Vector vector = this.pool;
        synchronized (vector) {
            HostKey[] tmp;
            ArrayList<HostKey> v = new ArrayList<HostKey>();
            for (int i = 0; i < this.pool.size(); ++i) {
                HostKey hk = (HostKey)this.pool.elementAt(i);
                if (hk.type == 6 || host != null && (!hk.isMatched(host) || type != null && !hk.getType().equals(type))) continue;
                v.add(hk);
            }
            HostKey[] foo = new HostKey[v.size()];
            for (int i = 0; i < v.size(); ++i) {
                foo[i] = (HostKey)v.get(i);
            }
            if (host != null && host.startsWith("[") && host.indexOf("]:") > 1 && (tmp = this.getHostKey(host.substring(1, host.indexOf("]:")), type)).length > 0) {
                HostKey[] bar = new HostKey[foo.length + tmp.length];
                System.arraycopy(foo, 0, bar, 0, foo.length);
                System.arraycopy(tmp, 0, bar, foo.length, tmp.length);
                foo = bar;
            }
            return foo;
        }
    }

    public void remove(String host, String type) {
        this.remove(host, type, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void remove(String host, String type, byte[] key) {
        boolean sync = false;
        Vector vector = this.pool;
        synchronized (vector) {
            for (int i = 0; i < this.pool.size(); ++i) {
                HostKey hk = (HostKey)this.pool.elementAt(i);
                if (host != null && (!hk.isMatched(host) || type != null && (!hk.getType().equals(type) || key != null && !Util.array_equals(key, hk.key)))) continue;
                String hosts = hk.getHost();
                if (hosts.equals(host) || hk instanceof HashedHostKey && ((HashedHostKey)hk).isHashed()) {
                    this.pool.removeElement(hk);
                } else {
                    hk.host = this.deleteSubString(hosts, host);
                }
                sync = true;
            }
        }
        if (sync) {
            try {
                this.sync();
            }
            catch (Exception e) {
                // empty catch block
            }
        }
    }

    protected void sync() throws IOException {
        if (this.known_hosts != null) {
            this.sync(this.known_hosts);
        }
    }

    protected synchronized void sync(String foo) throws IOException {
        if (foo == null) {
            return;
        }
        FileOutputStream fos = new FileOutputStream(Util.checkTilde(foo));
        this.dump(fos);
        fos.close();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void dump(OutputStream out) throws IOException {
        try {
            Vector vector = this.pool;
            synchronized (vector) {
                for (int i = 0; i < this.pool.size(); ++i) {
                    HostKey hk = (HostKey)this.pool.elementAt(i);
                    String marker = hk.getMarker();
                    String host = hk.getHost();
                    String type = hk.getType();
                    String comment = hk.getComment();
                    if (type.equals("UNKNOWN")) {
                        out.write(Util.str2byte(host));
                        out.write(cr);
                        continue;
                    }
                    if (marker.length() != 0) {
                        out.write(Util.str2byte(marker));
                        out.write(space);
                    }
                    out.write(Util.str2byte(host));
                    out.write(space);
                    out.write(Util.str2byte(type));
                    out.write(space);
                    out.write(Util.str2byte(hk.getKey()));
                    if (comment != null) {
                        out.write(space);
                        out.write(Util.str2byte(comment));
                    }
                    out.write(cr);
                }
            }
        }
        catch (Exception e) {
            System.err.println(e);
        }
    }

    private String deleteSubString(String hosts, String host) {
        int j;
        int i = 0;
        int hostlen = host.length();
        int hostslen = hosts.length();
        while (i < hostslen && (j = hosts.indexOf(44, i)) != -1) {
            if (!host.equals(hosts.substring(i, j))) {
                i = j + 1;
                continue;
            }
            return hosts.substring(0, i) + hosts.substring(j + 1);
        }
        if (hosts.endsWith(host) && hostslen - i == hostlen) {
            return hosts.substring(0, hostlen == hostslen ? 0 : hostslen - hostlen - 1);
        }
        return hosts;
    }

    private MAC getHMACSHA1() {
        if (this.hmacsha1 == null) {
            try {
                Class<?> c = Class.forName(JSch.getConfig("hmac-sha1"));
                this.hmacsha1 = (MAC)c.newInstance();
            }
            catch (Exception e) {
                System.err.println("hmacsha1: " + e);
            }
        }
        return this.hmacsha1;
    }

    HostKey createHashedHostKey(String host, byte[] key) throws JSchException {
        HashedHostKey hhk = new HashedHostKey(host, key);
        hhk.hash();
        return hhk;
    }

    class HashedHostKey
    extends HostKey {
        private static final String HASH_MAGIC = "|1|";
        private static final String HASH_DELIM = "|";
        private boolean hashed;
        byte[] salt;
        byte[] hash;

        HashedHostKey(String host, byte[] key) throws JSchException {
            this(host, 0, key);
        }

        HashedHostKey(String host, int type, byte[] key) throws JSchException {
            this("", host, type, key, null);
        }

        HashedHostKey(String marker, String host, int type, byte[] key, String comment) throws JSchException {
            super(marker, host, type, key, comment);
            this.hashed = false;
            this.salt = null;
            this.hash = null;
            if (this.host.startsWith(HASH_MAGIC) && this.host.substring(HASH_MAGIC.length()).indexOf(HASH_DELIM) > 0) {
                String data = this.host.substring(HASH_MAGIC.length());
                String _salt = data.substring(0, data.indexOf(HASH_DELIM));
                String _hash = data.substring(data.indexOf(HASH_DELIM) + 1);
                this.salt = Util.fromBase64(Util.str2byte(_salt), 0, _salt.length());
                this.hash = Util.fromBase64(Util.str2byte(_hash), 0, _hash.length());
                if (this.salt.length != 20 || this.hash.length != 20) {
                    this.salt = null;
                    this.hash = null;
                    return;
                }
                this.hashed = true;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        boolean isMatched(String _host) {
            if (!this.hashed) {
                return super.isMatched(_host);
            }
            MAC macsha1 = KnownHosts.this.getHMACSHA1();
            try {
                MAC mAC = macsha1;
                synchronized (mAC) {
                    macsha1.init(this.salt);
                    byte[] foo = Util.str2byte(_host);
                    macsha1.update(foo, 0, foo.length);
                    byte[] bar = new byte[macsha1.getBlockSize()];
                    macsha1.doFinal(bar, 0);
                    return Util.array_equals(this.hash, bar);
                }
            }
            catch (Exception e) {
                System.out.println(e);
                return false;
            }
        }

        boolean isHashed() {
            return this.hashed;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void hash() {
            Object random;
            if (this.hashed) {
                return;
            }
            MAC macsha1 = KnownHosts.this.getHMACSHA1();
            if (this.salt == null) {
                random = Session.random;
                Random random2 = random;
                synchronized (random2) {
                    this.salt = new byte[macsha1.getBlockSize()];
                    random.fill(this.salt, 0, this.salt.length);
                }
            }
            try {
                random = macsha1;
                synchronized (random) {
                    macsha1.init(this.salt);
                    byte[] foo = Util.str2byte(this.host);
                    macsha1.update(foo, 0, foo.length);
                    this.hash = new byte[macsha1.getBlockSize()];
                    macsha1.doFinal(this.hash, 0);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.host = HASH_MAGIC + Util.byte2str(Util.toBase64(this.salt, 0, this.salt.length)) + HASH_DELIM + Util.byte2str(Util.toBase64(this.hash, 0, this.hash.length));
            this.hashed = true;
        }
    }
}


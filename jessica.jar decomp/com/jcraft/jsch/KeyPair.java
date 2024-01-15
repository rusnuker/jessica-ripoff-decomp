/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.Cipher;
import com.jcraft.jsch.HASH;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPairDSA;
import com.jcraft.jsch.KeyPairECDSA;
import com.jcraft.jsch.KeyPairPKCS8;
import com.jcraft.jsch.KeyPairRSA;
import com.jcraft.jsch.Random;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Signature;
import com.jcraft.jsch.Util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;

public abstract class KeyPair {
    public static final int ERROR = 0;
    public static final int DSA = 1;
    public static final int RSA = 2;
    public static final int ECDSA = 3;
    public static final int UNKNOWN = 4;
    static final int VENDOR_OPENSSH = 0;
    static final int VENDOR_FSECURE = 1;
    static final int VENDOR_PUTTY = 2;
    static final int VENDOR_PKCS8 = 3;
    int vendor = 0;
    private static final byte[] cr = Util.str2byte("\n");
    protected String publicKeyComment = "no comment";
    JSch jsch = null;
    private Cipher cipher;
    private HASH hash;
    private Random random;
    private byte[] passphrase;
    static byte[][] header = new byte[][]{Util.str2byte("Proc-Type: 4,ENCRYPTED"), Util.str2byte("DEK-Info: DES-EDE3-CBC,")};
    private static byte[] space = Util.str2byte(" ");
    protected boolean encrypted = false;
    protected byte[] data = null;
    private byte[] iv = null;
    private byte[] publickeyblob = null;
    private static final String[] header1 = new String[]{"PuTTY-User-Key-File-2: ", "Encryption: ", "Comment: ", "Public-Lines: "};
    private static final String[] header2 = new String[]{"Private-Lines: "};
    private static final String[] header3 = new String[]{"Private-MAC: "};

    public static KeyPair genKeyPair(JSch jsch, int type) throws JSchException {
        return KeyPair.genKeyPair(jsch, type, 1024);
    }

    public static KeyPair genKeyPair(JSch jsch, int type, int key_size) throws JSchException {
        KeyPair kpair = null;
        if (type == 1) {
            kpair = new KeyPairDSA(jsch);
        } else if (type == 2) {
            kpair = new KeyPairRSA(jsch);
        } else if (type == 3) {
            kpair = new KeyPairECDSA(jsch);
        }
        if (kpair != null) {
            kpair.generate(key_size);
        }
        return kpair;
    }

    abstract void generate(int var1) throws JSchException;

    abstract byte[] getBegin();

    abstract byte[] getEnd();

    abstract int getKeySize();

    public abstract byte[] getSignature(byte[] var1);

    public abstract Signature getVerifier();

    public abstract byte[] forSSHAgent() throws JSchException;

    public String getPublicKeyComment() {
        return this.publicKeyComment;
    }

    public void setPublicKeyComment(String publicKeyComment) {
        this.publicKeyComment = publicKeyComment;
    }

    public KeyPair(JSch jsch) {
        this.jsch = jsch;
    }

    abstract byte[] getPrivateKey();

    public void writePrivateKey(OutputStream out) {
        this.writePrivateKey(out, null);
    }

    public void writePrivateKey(OutputStream out, byte[] passphrase) {
        byte[][] _iv;
        byte[] plain;
        byte[] encoded;
        if (passphrase == null) {
            passphrase = this.passphrase;
        }
        if ((encoded = this.encrypt(plain = this.getPrivateKey(), _iv = new byte[1][], passphrase)) != plain) {
            Util.bzero(plain);
        }
        byte[] iv = _iv[0];
        byte[] prv = Util.toBase64(encoded, 0, encoded.length);
        try {
            int i;
            out.write(this.getBegin());
            out.write(cr);
            if (passphrase != null) {
                out.write(header[0]);
                out.write(cr);
                out.write(header[1]);
                for (i = 0; i < iv.length; ++i) {
                    out.write(KeyPair.b2a((byte)(iv[i] >>> 4 & 0xF)));
                    out.write(KeyPair.b2a((byte)(iv[i] & 0xF)));
                }
                out.write(cr);
                out.write(cr);
            }
            for (i = 0; i < prv.length; i += 64) {
                if (i + 64 < prv.length) {
                    out.write(prv, i, 64);
                    out.write(cr);
                    continue;
                }
                out.write(prv, i, prv.length - i);
                out.write(cr);
                break;
            }
            out.write(this.getEnd());
            out.write(cr);
        }
        catch (Exception e) {
            // empty catch block
        }
    }

    abstract byte[] getKeyTypeName();

    public abstract int getKeyType();

    public byte[] getPublicKeyBlob() {
        return this.publickeyblob;
    }

    public void writePublicKey(OutputStream out, String comment) {
        byte[] pubblob = this.getPublicKeyBlob();
        byte[] pub = Util.toBase64(pubblob, 0, pubblob.length);
        try {
            out.write(this.getKeyTypeName());
            out.write(space);
            out.write(pub, 0, pub.length);
            out.write(space);
            out.write(Util.str2byte(comment));
            out.write(cr);
        }
        catch (Exception e) {
            // empty catch block
        }
    }

    public void writePublicKey(String name, String comment) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(name);
        this.writePublicKey(fos, comment);
        fos.close();
    }

    public void writeSECSHPublicKey(OutputStream out, String comment) {
        byte[] pubblob = this.getPublicKeyBlob();
        byte[] pub = Util.toBase64(pubblob, 0, pubblob.length);
        try {
            int len;
            out.write(Util.str2byte("---- BEGIN SSH2 PUBLIC KEY ----"));
            out.write(cr);
            out.write(Util.str2byte("Comment: \"" + comment + "\""));
            out.write(cr);
            for (int index = 0; index < pub.length; index += len) {
                len = 70;
                if (pub.length - index < len) {
                    len = pub.length - index;
                }
                out.write(pub, index, len);
                out.write(cr);
            }
            out.write(Util.str2byte("---- END SSH2 PUBLIC KEY ----"));
            out.write(cr);
        }
        catch (Exception e) {
            // empty catch block
        }
    }

    public void writeSECSHPublicKey(String name, String comment) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(name);
        this.writeSECSHPublicKey(fos, comment);
        fos.close();
    }

    public void writePrivateKey(String name) throws FileNotFoundException, IOException {
        this.writePrivateKey(name, null);
    }

    public void writePrivateKey(String name, byte[] passphrase) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(name);
        this.writePrivateKey(fos, passphrase);
        fos.close();
    }

    public String getFingerPrint() {
        byte[] kblob;
        if (this.hash == null) {
            this.hash = this.genHash();
        }
        if ((kblob = this.getPublicKeyBlob()) == null) {
            return null;
        }
        return Util.getFingerPrint(this.hash, kblob);
    }

    private byte[] encrypt(byte[] plain, byte[][] _iv, byte[] passphrase) {
        if (passphrase == null) {
            return plain;
        }
        if (this.cipher == null) {
            this.cipher = this.genCipher();
        }
        _iv[0] = new byte[this.cipher.getIVSize()];
        byte[] iv = _iv[0];
        if (this.random == null) {
            this.random = this.genRandom();
        }
        this.random.fill(iv, 0, iv.length);
        byte[] key = this.genKey(passphrase, iv);
        byte[] encoded = plain;
        int bsize = this.cipher.getIVSize();
        byte[] foo = new byte[(encoded.length / bsize + 1) * bsize];
        System.arraycopy(encoded, 0, foo, 0, encoded.length);
        int padding = bsize - encoded.length % bsize;
        for (int i = foo.length - 1; foo.length - padding <= i; --i) {
            foo[i] = (byte)padding;
        }
        encoded = foo;
        try {
            this.cipher.init(0, key, iv);
            this.cipher.update(encoded, 0, encoded.length, encoded, 0);
        }
        catch (Exception e) {
            // empty catch block
        }
        Util.bzero(key);
        return encoded;
    }

    abstract boolean parse(byte[] var1);

    private byte[] decrypt(byte[] data, byte[] passphrase, byte[] iv) {
        try {
            byte[] key = this.genKey(passphrase, iv);
            this.cipher.init(1, key, iv);
            Util.bzero(key);
            byte[] plain = new byte[data.length];
            this.cipher.update(data, 0, data.length, plain, 0);
            return plain;
        }
        catch (Exception e) {
            return null;
        }
    }

    int writeSEQUENCE(byte[] buf, int index, int len) {
        buf[index++] = 48;
        index = this.writeLength(buf, index, len);
        return index;
    }

    int writeINTEGER(byte[] buf, int index, byte[] data) {
        buf[index++] = 2;
        index = this.writeLength(buf, index, data.length);
        System.arraycopy(data, 0, buf, index, data.length);
        return index += data.length;
    }

    int writeOCTETSTRING(byte[] buf, int index, byte[] data) {
        buf[index++] = 4;
        index = this.writeLength(buf, index, data.length);
        System.arraycopy(data, 0, buf, index, data.length);
        return index += data.length;
    }

    int writeDATA(byte[] buf, byte n, int index, byte[] data) {
        buf[index++] = n;
        index = this.writeLength(buf, index, data.length);
        System.arraycopy(data, 0, buf, index, data.length);
        return index += data.length;
    }

    int countLength(int len) {
        int i = 1;
        if (len <= 127) {
            return i;
        }
        while (len > 0) {
            len >>>= 8;
            ++i;
        }
        return i;
    }

    int writeLength(byte[] data, int index, int len) {
        int i = this.countLength(len) - 1;
        if (i == 0) {
            data[index++] = (byte)len;
            return index;
        }
        data[index++] = (byte)(0x80 | i);
        int j = index + i;
        while (i > 0) {
            data[index + i - 1] = (byte)(len & 0xFF);
            len >>>= 8;
            --i;
        }
        return j;
    }

    private Random genRandom() {
        if (this.random == null) {
            try {
                Class<?> c = Class.forName(JSch.getConfig("random"));
                this.random = (Random)c.newInstance();
            }
            catch (Exception e) {
                System.err.println("connect: random " + e);
            }
        }
        return this.random;
    }

    private HASH genHash() {
        try {
            Class<?> c = Class.forName(JSch.getConfig("md5"));
            this.hash = (HASH)c.newInstance();
            this.hash.init();
        }
        catch (Exception exception) {
            // empty catch block
        }
        return this.hash;
    }

    private Cipher genCipher() {
        try {
            Class<?> c = Class.forName(JSch.getConfig("3des-cbc"));
            this.cipher = (Cipher)c.newInstance();
        }
        catch (Exception exception) {
            // empty catch block
        }
        return this.cipher;
    }

    synchronized byte[] genKey(byte[] passphrase, byte[] iv) {
        if (this.cipher == null) {
            this.cipher = this.genCipher();
        }
        if (this.hash == null) {
            this.hash = this.genHash();
        }
        byte[] key = new byte[this.cipher.getBlockSize()];
        int hsize = this.hash.getBlockSize();
        byte[] hn = new byte[key.length / hsize * hsize + (key.length % hsize == 0 ? 0 : hsize)];
        try {
            byte[] tmp = null;
            if (this.vendor == 0) {
                int index = 0;
                while (index + hsize <= hn.length) {
                    if (tmp != null) {
                        this.hash.update(tmp, 0, tmp.length);
                    }
                    this.hash.update(passphrase, 0, passphrase.length);
                    this.hash.update(iv, 0, iv.length > 8 ? 8 : iv.length);
                    tmp = this.hash.digest();
                    System.arraycopy(tmp, 0, hn, index, tmp.length);
                    index += tmp.length;
                }
                System.arraycopy(hn, 0, key, 0, key.length);
            } else if (this.vendor == 1) {
                int index = 0;
                while (index + hsize <= hn.length) {
                    if (tmp != null) {
                        this.hash.update(tmp, 0, tmp.length);
                    }
                    this.hash.update(passphrase, 0, passphrase.length);
                    tmp = this.hash.digest();
                    System.arraycopy(tmp, 0, hn, index, tmp.length);
                    index += tmp.length;
                }
                System.arraycopy(hn, 0, key, 0, key.length);
            } else if (this.vendor == 2) {
                Class<?> c = Class.forName(JSch.getConfig("sha-1"));
                HASH sha1 = (HASH)c.newInstance();
                tmp = new byte[4];
                key = new byte[40];
                for (int i = 0; i < 2; ++i) {
                    sha1.init();
                    tmp[3] = (byte)i;
                    sha1.update(tmp, 0, tmp.length);
                    sha1.update(passphrase, 0, passphrase.length);
                    System.arraycopy(sha1.digest(), 0, key, i * 20, 20);
                }
            }
        }
        catch (Exception e) {
            System.err.println(e);
        }
        return key;
    }

    public void setPassphrase(String passphrase) {
        if (passphrase == null || passphrase.length() == 0) {
            this.setPassphrase((byte[])null);
        } else {
            this.setPassphrase(Util.str2byte(passphrase));
        }
    }

    public void setPassphrase(byte[] passphrase) {
        if (passphrase != null && passphrase.length == 0) {
            passphrase = null;
        }
        this.passphrase = passphrase;
    }

    public boolean isEncrypted() {
        return this.encrypted;
    }

    public boolean decrypt(String _passphrase) {
        if (_passphrase == null || _passphrase.length() == 0) {
            return !this.encrypted;
        }
        return this.decrypt(Util.str2byte(_passphrase));
    }

    public boolean decrypt(byte[] _passphrase) {
        if (!this.encrypted) {
            return true;
        }
        if (_passphrase == null) {
            return !this.encrypted;
        }
        byte[] bar = new byte[_passphrase.length];
        System.arraycopy(_passphrase, 0, bar, 0, bar.length);
        _passphrase = bar;
        byte[] foo = this.decrypt(this.data, _passphrase, this.iv);
        Util.bzero(_passphrase);
        if (this.parse(foo)) {
            this.encrypted = false;
        }
        return !this.encrypted;
    }

    public static KeyPair load(JSch jsch, String prvkey) throws JSchException {
        String pubkey = prvkey + ".pub";
        if (!new File(pubkey).exists()) {
            pubkey = null;
        }
        return KeyPair.load(jsch, prvkey, pubkey);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static KeyPair load(JSch jsch, String prvfile, String pubfile) throws JSchException {
        byte[] pubkey;
        byte[] prvkey;
        block8: {
            prvkey = null;
            pubkey = null;
            try {
                prvkey = Util.fromFile(prvfile);
            }
            catch (IOException e) {
                throw new JSchException(e.toString(), e);
            }
            String _pubfile = pubfile;
            if (pubfile == null) {
                _pubfile = prvfile + ".pub";
            }
            try {
                pubkey = Util.fromFile(_pubfile);
            }
            catch (IOException e) {
                if (pubfile == null) break block8;
                throw new JSchException(e.toString(), e);
            }
        }
        try {
            KeyPair keyPair = KeyPair.load(jsch, prvkey, pubkey);
            return keyPair;
        }
        finally {
            Util.bzero(prvkey);
        }
    }

    public static KeyPair load(JSch jsch, byte[] prvkey, byte[] pubkey) throws JSchException {
        Cipher cipher;
        String publicKeyComment;
        int vendor;
        int type;
        byte[] publickeyblob;
        byte[] data;
        boolean encrypted;
        byte[] iv;
        block104: {
            iv = new byte[8];
            encrypted = true;
            data = null;
            publickeyblob = null;
            type = 0;
            vendor = 0;
            publicKeyComment = "";
            cipher = null;
            if (pubkey == null && prvkey != null && prvkey.length > 11 && prvkey[0] == 0 && prvkey[1] == 0 && prvkey[2] == 0 && (prvkey[3] == 7 || prvkey[3] == 19)) {
                Buffer buf = new Buffer(prvkey);
                buf.skip(prvkey.length);
                String _type = new String(buf.getString());
                buf.rewind();
                KeyPair kpair = null;
                if (_type.equals("ssh-rsa")) {
                    kpair = KeyPairRSA.fromSSHAgent(jsch, buf);
                } else if (_type.equals("ssh-dss")) {
                    kpair = KeyPairDSA.fromSSHAgent(jsch, buf);
                } else if (_type.equals("ecdsa-sha2-nistp256") || _type.equals("ecdsa-sha2-nistp384") || _type.equals("ecdsa-sha2-nistp512")) {
                    kpair = KeyPairECDSA.fromSSHAgent(jsch, buf);
                } else {
                    throw new JSchException("privatekey: invalid key " + new String(prvkey, 4, 7));
                }
                return kpair;
            }
            try {
                int i;
                KeyPair ppk;
                byte[] buf = prvkey;
                if (buf != null && (ppk = KeyPair.loadPPK(jsch, buf)) != null) {
                    return ppk;
                }
                int len = buf != null ? buf.length : 0;
                for (i = 0; i < len && (buf[i] != 45 || i + 4 >= len || buf[i + 1] != 45 || buf[i + 2] != 45 || buf[i + 3] != 45 || buf[i + 4] != 45); ++i) {
                }
                while (i < len) {
                    if (buf[i] == 66 && i + 3 < len && buf[i + 1] == 69 && buf[i + 2] == 71 && buf[i + 3] == 73) {
                        if ((i += 6) + 2 >= len) {
                            throw new JSchException("invalid privatekey: " + prvkey);
                        }
                        if (buf[i] == 68 && buf[i + 1] == 83 && buf[i + 2] == 65) {
                            type = 1;
                        } else if (buf[i] == 82 && buf[i + 1] == 83 && buf[i + 2] == 65) {
                            type = 2;
                        } else if (buf[i] == 69 && buf[i + 1] == 67) {
                            type = 3;
                        } else if (buf[i] == 83 && buf[i + 1] == 83 && buf[i + 2] == 72) {
                            type = 4;
                            vendor = 1;
                        } else if (i + 6 < len && buf[i] == 80 && buf[i + 1] == 82 && buf[i + 2] == 73 && buf[i + 3] == 86 && buf[i + 4] == 65 && buf[i + 5] == 84 && buf[i + 6] == 69) {
                            type = 4;
                            vendor = 3;
                            encrypted = false;
                            i += 3;
                        } else if (i + 8 < len && buf[i] == 69 && buf[i + 1] == 78 && buf[i + 2] == 67 && buf[i + 3] == 82 && buf[i + 4] == 89 && buf[i + 5] == 80 && buf[i + 6] == 84 && buf[i + 7] == 69 && buf[i + 8] == 68) {
                            type = 4;
                            vendor = 3;
                            i += 5;
                        } else {
                            throw new JSchException("invalid privatekey: " + prvkey);
                        }
                        i += 3;
                        continue;
                    }
                    if (buf[i] == 65 && i + 7 < len && buf[i + 1] == 69 && buf[i + 2] == 83 && buf[i + 3] == 45 && buf[i + 4] == 50 && buf[i + 5] == 53 && buf[i + 6] == 54 && buf[i + 7] == 45) {
                        i += 8;
                        if (Session.checkCipher(JSch.getConfig("aes256-cbc"))) {
                            Class<?> c = Class.forName(JSch.getConfig("aes256-cbc"));
                            cipher = (Cipher)c.newInstance();
                            iv = new byte[cipher.getIVSize()];
                            continue;
                        }
                        throw new JSchException("privatekey: aes256-cbc is not available " + prvkey);
                    }
                    if (buf[i] == 65 && i + 7 < len && buf[i + 1] == 69 && buf[i + 2] == 83 && buf[i + 3] == 45 && buf[i + 4] == 49 && buf[i + 5] == 57 && buf[i + 6] == 50 && buf[i + 7] == 45) {
                        i += 8;
                        if (Session.checkCipher(JSch.getConfig("aes192-cbc"))) {
                            Class<?> c = Class.forName(JSch.getConfig("aes192-cbc"));
                            cipher = (Cipher)c.newInstance();
                            iv = new byte[cipher.getIVSize()];
                            continue;
                        }
                        throw new JSchException("privatekey: aes192-cbc is not available " + prvkey);
                    }
                    if (buf[i] == 65 && i + 7 < len && buf[i + 1] == 69 && buf[i + 2] == 83 && buf[i + 3] == 45 && buf[i + 4] == 49 && buf[i + 5] == 50 && buf[i + 6] == 56 && buf[i + 7] == 45) {
                        i += 8;
                        if (Session.checkCipher(JSch.getConfig("aes128-cbc"))) {
                            Class<?> c = Class.forName(JSch.getConfig("aes128-cbc"));
                            cipher = (Cipher)c.newInstance();
                            iv = new byte[cipher.getIVSize()];
                            continue;
                        }
                        throw new JSchException("privatekey: aes128-cbc is not available " + prvkey);
                    }
                    if (buf[i] == 67 && i + 3 < len && buf[i + 1] == 66 && buf[i + 2] == 67 && buf[i + 3] == 44) {
                        i += 4;
                        for (int ii = 0; ii < iv.length; ++ii) {
                            iv[ii] = (byte)((KeyPair.a2b(buf[i++]) << 4 & 0xF0) + (KeyPair.a2b(buf[i++]) & 0xF));
                        }
                        continue;
                    }
                    if (buf[i] == 13 && i + 1 < buf.length && buf[i + 1] == 10) {
                        ++i;
                        continue;
                    }
                    if (buf[i] == 10 && i + 1 < buf.length) {
                        if (buf[i + 1] == 10) {
                            i += 2;
                            break;
                        }
                        if (buf[i + 1] == 13 && i + 2 < buf.length && buf[i + 2] == 10) {
                            i += 3;
                            break;
                        }
                        boolean inheader = false;
                        for (int j = i + 1; j < buf.length && buf[j] != 10; ++j) {
                            if (buf[j] != 58) continue;
                            inheader = true;
                            break;
                        }
                        if (!inheader) {
                            ++i;
                            if (vendor == 3) break;
                            encrypted = false;
                            break;
                        }
                    }
                    ++i;
                }
                if (buf != null) {
                    if (type == 0) {
                        throw new JSchException("invalid privatekey: " + prvkey);
                    }
                    int start = i;
                    while (i < len && buf[i] != 45) {
                        ++i;
                    }
                    if (len - i == 0 || i - start == 0) {
                        throw new JSchException("invalid privatekey: " + prvkey);
                    }
                    byte[] tmp = new byte[i - start];
                    System.arraycopy(buf, start, tmp, 0, tmp.length);
                    byte[] _buf = tmp;
                    start = 0;
                    i = 0;
                    int _len = _buf.length;
                    while (i < _len) {
                        if (_buf[i] == 10) {
                            boolean xd = _buf[i - 1] == 13;
                            System.arraycopy(_buf, i + 1, _buf, i - (xd ? 1 : 0), _len - (i + 1));
                            if (xd) {
                                --_len;
                            }
                            --_len;
                            continue;
                        }
                        if (_buf[i] == 45) break;
                        ++i;
                    }
                    if (i - start > 0) {
                        data = Util.fromBase64(_buf, start, i - start);
                    }
                    Util.bzero(_buf);
                }
                if (data != null && data.length > 4 && data[0] == 63 && data[1] == 111 && data[2] == -7 && data[3] == -21) {
                    Buffer _buf = new Buffer(data);
                    _buf.getInt();
                    _buf.getInt();
                    byte[] _type = _buf.getString();
                    String _cipher = Util.byte2str(_buf.getString());
                    if (_cipher.equals("3des-cbc")) {
                        _buf.getInt();
                        byte[] foo = new byte[data.length - _buf.getOffSet()];
                        _buf.getByte(foo);
                        data = foo;
                        encrypted = true;
                        throw new JSchException("unknown privatekey format: " + prvkey);
                    }
                    if (_cipher.equals("none")) {
                        _buf.getInt();
                        _buf.getInt();
                        encrypted = false;
                        byte[] foo = new byte[data.length - _buf.getOffSet()];
                        _buf.getByte(foo);
                        data = foo;
                    }
                }
                if (pubkey == null) break block104;
                try {
                    buf = pubkey;
                    len = buf.length;
                    if (buf.length > 4 && buf[0] == 45 && buf[1] == 45 && buf[2] == 45 && buf[3] == 45) {
                        boolean valid = true;
                        i = 0;
                        while (buf.length > ++i && buf[i] != 10) {
                        }
                        if (buf.length <= i) {
                            valid = false;
                        }
                        while (valid) {
                            if (buf[i] == 10) {
                                boolean inheader = false;
                                for (int j = i + 1; j < buf.length && buf[j] != 10; ++j) {
                                    if (buf[j] != 58) continue;
                                    inheader = true;
                                    break;
                                }
                                if (!inheader) {
                                    ++i;
                                    break;
                                }
                            }
                            ++i;
                        }
                        if (buf.length <= i) {
                            valid = false;
                        }
                        int start = i;
                        while (valid && i < len) {
                            if (buf[i] == 10) {
                                System.arraycopy(buf, i + 1, buf, i, len - i - 1);
                                --len;
                                continue;
                            }
                            if (buf[i] == 45) break;
                            ++i;
                        }
                        if (valid) {
                            publickeyblob = Util.fromBase64(buf, start, i - start);
                            if (prvkey == null || type == 4) {
                                if (publickeyblob[8] == 100) {
                                    type = 1;
                                } else if (publickeyblob[8] == 114) {
                                    type = 2;
                                }
                            }
                        }
                    } else if (buf[0] == 115 && buf[1] == 115 && buf[2] == 104 && buf[3] == 45) {
                        if (prvkey == null && buf.length > 7) {
                            if (buf[4] == 100) {
                                type = 1;
                            } else if (buf[4] == 114) {
                                type = 2;
                            }
                        }
                        for (i = 0; i < len && buf[i] != 32; ++i) {
                        }
                        if (++i < len) {
                            int start = i;
                            while (i < len && buf[i] != 32) {
                                ++i;
                            }
                            publickeyblob = Util.fromBase64(buf, start, i - start);
                        }
                        if (i++ < len) {
                            int start = i;
                            while (i < len && buf[i] != 10) {
                                ++i;
                            }
                            if (i > 0 && buf[i - 1] == 13) {
                                --i;
                            }
                            if (start < i) {
                                publicKeyComment = new String(buf, start, i - start);
                            }
                        }
                    } else if (buf[0] == 101 && buf[1] == 99 && buf[2] == 100 && buf[3] == 115) {
                        if (prvkey == null && buf.length > 7) {
                            type = 3;
                        }
                        for (i = 0; i < len && buf[i] != 32; ++i) {
                        }
                        if (++i < len) {
                            int start = i;
                            while (i < len && buf[i] != 32) {
                                ++i;
                            }
                            publickeyblob = Util.fromBase64(buf, start, i - start);
                        }
                        if (i++ < len) {
                            int start = i;
                            while (i < len && buf[i] != 10) {
                                ++i;
                            }
                            if (i > 0 && buf[i - 1] == 13) {
                                --i;
                            }
                            if (start < i) {
                                publicKeyComment = new String(buf, start, i - start);
                            }
                        }
                    }
                }
                catch (Exception ee) {}
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
        KeyPair kpair = null;
        if (type == 1) {
            kpair = new KeyPairDSA(jsch);
        } else if (type == 2) {
            kpair = new KeyPairRSA(jsch);
        } else if (type == 3) {
            kpair = new KeyPairECDSA(jsch);
        } else if (vendor == 3) {
            kpair = new KeyPairPKCS8(jsch);
        }
        if (kpair != null) {
            kpair.encrypted = encrypted;
            kpair.publickeyblob = publickeyblob;
            kpair.vendor = vendor;
            kpair.publicKeyComment = publicKeyComment;
            kpair.cipher = cipher;
            if (encrypted) {
                kpair.encrypted = true;
                kpair.iv = iv;
                kpair.data = data;
            } else {
                if (kpair.parse(data)) {
                    kpair.encrypted = false;
                    return kpair;
                }
                throw new JSchException("invalid privatekey: " + prvkey);
            }
        }
        return kpair;
    }

    private static byte a2b(byte c) {
        if (48 <= c && c <= 57) {
            return (byte)(c - 48);
        }
        return (byte)(c - 97 + 10);
    }

    private static byte b2a(byte c) {
        if (0 <= c && c <= 9) {
            return (byte)(c + 48);
        }
        return (byte)(c - 10 + 65);
    }

    public void dispose() {
        Util.bzero(this.passphrase);
    }

    public void finalize() {
        this.dispose();
    }

    static KeyPair loadPPK(JSch jsch, byte[] buf) throws JSchException {
        Buffer _buf;
        byte[] pubkey = null;
        byte[] prvkey = null;
        int lines = 0;
        Buffer buffer = new Buffer(buf);
        Hashtable v = new Hashtable();
        while (KeyPair.parseHeader(buffer, v)) {
        }
        String typ = (String)v.get("PuTTY-User-Key-File-2");
        if (typ == null) {
            return null;
        }
        lines = Integer.parseInt((String)v.get("Public-Lines"));
        pubkey = KeyPair.parseLines(buffer, lines);
        while (KeyPair.parseHeader(buffer, v)) {
        }
        lines = Integer.parseInt((String)v.get("Private-Lines"));
        prvkey = KeyPair.parseLines(buffer, lines);
        while (KeyPair.parseHeader(buffer, v)) {
        }
        prvkey = Util.fromBase64(prvkey, 0, prvkey.length);
        pubkey = Util.fromBase64(pubkey, 0, pubkey.length);
        KeyPair kpair = null;
        if (typ.equals("ssh-rsa")) {
            _buf = new Buffer(pubkey);
            _buf.skip(pubkey.length);
            int len = _buf.getInt();
            _buf.getByte(new byte[len]);
            byte[] pub_array = new byte[_buf.getInt()];
            _buf.getByte(pub_array);
            byte[] n_array = new byte[_buf.getInt()];
            _buf.getByte(n_array);
            kpair = new KeyPairRSA(jsch, n_array, pub_array, null);
        } else if (typ.equals("ssh-dss")) {
            _buf = new Buffer(pubkey);
            _buf.skip(pubkey.length);
            int len = _buf.getInt();
            _buf.getByte(new byte[len]);
            byte[] p_array = new byte[_buf.getInt()];
            _buf.getByte(p_array);
            byte[] q_array = new byte[_buf.getInt()];
            _buf.getByte(q_array);
            byte[] g_array = new byte[_buf.getInt()];
            _buf.getByte(g_array);
            byte[] y_array = new byte[_buf.getInt()];
            _buf.getByte(y_array);
            kpair = new KeyPairDSA(jsch, p_array, q_array, g_array, y_array, null);
        } else {
            return null;
        }
        if (kpair == null) {
            return null;
        }
        kpair.encrypted = !v.get("Encryption").equals("none");
        kpair.vendor = 2;
        kpair.publicKeyComment = (String)v.get("Comment");
        if (kpair.encrypted) {
            if (Session.checkCipher(JSch.getConfig("aes256-cbc"))) {
                try {
                    Class<?> c = Class.forName(JSch.getConfig("aes256-cbc"));
                    kpair.cipher = (Cipher)c.newInstance();
                    kpair.iv = new byte[kpair.cipher.getIVSize()];
                }
                catch (Exception e) {
                    throw new JSchException("The cipher 'aes256-cbc' is required, but it is not available.");
                }
            } else {
                throw new JSchException("The cipher 'aes256-cbc' is required, but it is not available.");
            }
            kpair.data = prvkey;
        } else {
            kpair.data = prvkey;
            kpair.parse(prvkey);
        }
        return kpair;
    }

    private static byte[] parseLines(Buffer buffer, int lines) {
        byte[] buf = buffer.buffer;
        int index = buffer.index;
        byte[] data = null;
        int i = index;
        while (lines-- > 0) {
            while (buf.length > i) {
                if (buf[i++] != 13) continue;
                if (data == null) {
                    data = new byte[i - index - 1];
                    System.arraycopy(buf, index, data, 0, i - index - 1);
                    break;
                }
                byte[] tmp = new byte[data.length + i - index - 1];
                System.arraycopy(data, 0, tmp, 0, data.length);
                System.arraycopy(buf, index, tmp, data.length, i - index - 1);
                for (int j = 0; j < data.length; ++j) {
                    data[j] = 0;
                }
                data = tmp;
                break;
            }
            if (buf[i] == 10) {
                // empty if block
            }
            index = ++i;
        }
        if (data != null) {
            buffer.index = index;
        }
        return data;
    }

    private static boolean parseHeader(Buffer buffer, Hashtable v) {
        int i;
        byte[] buf = buffer.buffer;
        int index = buffer.index;
        String key = null;
        String value = null;
        for (i = index; i < buf.length && buf[i] != 13; ++i) {
            if (buf[i] != 58) continue;
            key = new String(buf, index, i - index);
            if (++i < buf.length && buf[i] == 32) {
                ++i;
            }
            index = i;
            break;
        }
        if (key == null) {
            return false;
        }
        for (i = index; i < buf.length; ++i) {
            if (buf[i] != 13) continue;
            value = new String(buf, index, i - index);
            if (++i < buf.length && buf[i] == 10) {
                ++i;
            }
            index = i;
            break;
        }
        if (value != null) {
            v.put(key, value);
            buffer.index = index;
        }
        return key != null && value != null;
    }

    void copy(KeyPair kpair) {
        this.publickeyblob = kpair.publickeyblob;
        this.vendor = kpair.vendor;
        this.publicKeyComment = kpair.publicKeyComment;
        this.cipher = kpair.cipher;
    }

    class ASN1 {
        byte[] buf;
        int start;
        int length;

        ASN1(byte[] buf) throws ASN1Exception {
            this(buf, 0, buf.length);
        }

        ASN1(byte[] buf, int start, int length) throws ASN1Exception {
            this.buf = buf;
            this.start = start;
            this.length = length;
            if (start + length > buf.length) {
                throw new ASN1Exception();
            }
        }

        int getType() {
            return this.buf[this.start] & 0xFF;
        }

        boolean isSEQUENCE() {
            return this.getType() == 48;
        }

        boolean isINTEGER() {
            return this.getType() == 2;
        }

        boolean isOBJECT() {
            return this.getType() == 6;
        }

        boolean isOCTETSTRING() {
            return this.getType() == 4;
        }

        private int getLength(int[] indexp) {
            int length;
            int index = indexp[0];
            if (((length = this.buf[index++] & 0xFF) & 0x80) != 0) {
                int foo = length & 0x7F;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (this.buf[index++] & 0xFF);
                }
            }
            indexp[0] = index;
            return length;
        }

        byte[] getContent() {
            int[] indexp = new int[]{this.start + 1};
            int length = this.getLength(indexp);
            int index = indexp[0];
            byte[] tmp = new byte[length];
            System.arraycopy(this.buf, index, tmp, 0, tmp.length);
            return tmp;
        }

        ASN1[] getContents() throws ASN1Exception {
            int l;
            byte typ = this.buf[this.start];
            int[] indexp = new int[]{this.start + 1};
            if (typ == 5) {
                return new ASN1[0];
            }
            int index = indexp[0];
            Vector<ASN1> values = new Vector<ASN1>();
            for (int length = this.getLength(indexp); length > 0; length -= l) {
                --length;
                int tmp = ++index;
                indexp[0] = index;
                l = this.getLength(indexp);
                index = indexp[0];
                length -= index - tmp;
                values.addElement(new ASN1(this.buf, tmp - 1, 1 + (index - tmp) + l));
                index += l;
            }
            ASN1[] result = new ASN1[values.size()];
            for (int i = 0; i < values.size(); ++i) {
                result[i] = (ASN1)values.elementAt(i);
            }
            return result;
        }
    }

    class ASN1Exception
    extends Exception {
        ASN1Exception() {
        }
    }
}


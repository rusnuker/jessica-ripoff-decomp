/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.KeyPairGenECDSA;
import com.jcraft.jsch.Signature;
import com.jcraft.jsch.SignatureECDSA;
import com.jcraft.jsch.Util;

public class KeyPairECDSA
extends KeyPair {
    private static byte[][] oids = new byte[][]{{6, 8, 42, -122, 72, -50, 61, 3, 1, 7}, {6, 5, 43, -127, 4, 0, 34}, {6, 5, 43, -127, 4, 0, 35}};
    private static String[] names = new String[]{"nistp256", "nistp384", "nistp521"};
    private byte[] name = Util.str2byte(names[0]);
    private byte[] r_array;
    private byte[] s_array;
    private byte[] prv_array;
    private int key_size = 256;
    private static final byte[] begin = Util.str2byte("-----BEGIN EC PRIVATE KEY-----");
    private static final byte[] end = Util.str2byte("-----END EC PRIVATE KEY-----");

    public KeyPairECDSA(JSch jsch) {
        this(jsch, null, null, null, null);
    }

    public KeyPairECDSA(JSch jsch, byte[] name, byte[] r_array, byte[] s_array, byte[] prv_array) {
        super(jsch);
        if (name != null) {
            this.name = name;
        }
        this.r_array = r_array;
        this.s_array = s_array;
        this.prv_array = prv_array;
        if (prv_array != null) {
            this.key_size = prv_array.length >= 64 ? 521 : (prv_array.length >= 48 ? 384 : 256);
        }
    }

    void generate(int key_size) throws JSchException {
        this.key_size = key_size;
        try {
            Class<?> c = Class.forName(JSch.getConfig("keypairgen.ecdsa"));
            KeyPairGenECDSA keypairgen = (KeyPairGenECDSA)c.newInstance();
            keypairgen.init(key_size);
            this.prv_array = keypairgen.getD();
            this.r_array = keypairgen.getR();
            this.s_array = keypairgen.getS();
            this.name = Util.str2byte(names[this.prv_array.length >= 64 ? 2 : (this.prv_array.length >= 48 ? 1 : 0)]);
            Object var3_4 = null;
        }
        catch (Exception e) {
            if (e instanceof Throwable) {
                throw new JSchException(e.toString(), e);
            }
            throw new JSchException(e.toString());
        }
    }

    byte[] getBegin() {
        return begin;
    }

    byte[] getEnd() {
        return end;
    }

    byte[] getPrivateKey() {
        byte[] tmp = new byte[]{1};
        byte[] oid = oids[this.r_array.length >= 64 ? 2 : (this.r_array.length >= 48 ? 1 : 0)];
        byte[] point = KeyPairECDSA.toPoint(this.r_array, this.s_array);
        int bar = (point.length + 1 & 0x80) == 0 ? 3 : 4;
        byte[] foo = new byte[point.length + bar];
        System.arraycopy(point, 0, foo, bar, point.length);
        foo[0] = 3;
        if (bar == 3) {
            foo[1] = (byte)(point.length + 1);
        } else {
            foo[1] = -127;
            foo[2] = (byte)(point.length + 1);
        }
        point = foo;
        int content = 1 + this.countLength(tmp.length) + tmp.length + 1 + this.countLength(this.prv_array.length) + this.prv_array.length + 1 + this.countLength(oid.length) + oid.length + 1 + this.countLength(point.length) + point.length;
        int total = 1 + this.countLength(content) + content;
        byte[] plain = new byte[total];
        int index = 0;
        index = this.writeSEQUENCE(plain, index, content);
        index = this.writeINTEGER(plain, index, tmp);
        index = this.writeOCTETSTRING(plain, index, this.prv_array);
        index = this.writeDATA(plain, (byte)-96, index, oid);
        index = this.writeDATA(plain, (byte)-95, index, point);
        return plain;
    }

    boolean parse(byte[] plain) {
        try {
            int foo;
            if (this.vendor == 1) {
                return false;
            }
            if (this.vendor == 2) {
                return false;
            }
            int index = 0;
            int length = 0;
            if (plain[index] != 48) {
                return false;
            }
            int n = ++index;
            ++index;
            length = plain[n] & 0xFF;
            if ((length & 0x80) != 0) {
                foo = length & 0x7F;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xFF);
                }
            }
            if (plain[index] != 2) {
                return false;
            }
            int n2 = ++index;
            ++index;
            length = plain[n2] & 0xFF;
            if ((length & 0x80) != 0) {
                foo = length & 0x7F;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xFF);
                }
            }
            index += length;
            int n3 = ++index;
            ++index;
            length = plain[n3] & 0xFF;
            if ((length & 0x80) != 0) {
                foo = length & 0x7F;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xFF);
                }
            }
            this.prv_array = new byte[length];
            System.arraycopy(plain, index, this.prv_array, 0, length);
            index += length;
            int n4 = ++index;
            ++index;
            length = plain[n4] & 0xFF;
            if ((length & 0x80) != 0) {
                foo = length & 0x7F;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xFF);
                }
            }
            byte[] oid_array = new byte[length];
            System.arraycopy(plain, index, oid_array, 0, length);
            index += length;
            for (int i = 0; i < oids.length; ++i) {
                if (!Util.array_equals(oids[i], oid_array)) continue;
                this.name = Util.str2byte(names[i]);
                break;
            }
            int n5 = ++index;
            ++index;
            length = plain[n5] & 0xFF;
            if ((length & 0x80) != 0) {
                int foo2 = length & 0x7F;
                length = 0;
                while (foo2-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xFF);
                }
            }
            byte[] Q_array = new byte[length];
            System.arraycopy(plain, index, Q_array, 0, length);
            index += length;
            byte[][] tmp = KeyPairECDSA.fromPoint(Q_array);
            this.r_array = tmp[0];
            this.s_array = tmp[1];
            if (this.prv_array != null) {
                this.key_size = this.prv_array.length >= 64 ? 521 : (this.prv_array.length >= 48 ? 384 : 256);
            }
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public byte[] getPublicKeyBlob() {
        byte[] foo = super.getPublicKeyBlob();
        if (foo != null) {
            return foo;
        }
        if (this.r_array == null) {
            return null;
        }
        byte[][] tmp = new byte[][]{Util.str2byte("ecdsa-sha2-" + new String(this.name)), this.name, new byte[1 + this.r_array.length + this.s_array.length]};
        tmp[2][0] = 4;
        System.arraycopy(this.r_array, 0, tmp[2], 1, this.r_array.length);
        System.arraycopy(this.s_array, 0, tmp[2], 1 + this.r_array.length, this.s_array.length);
        return Buffer.fromBytes((byte[][])tmp).buffer;
    }

    byte[] getKeyTypeName() {
        return Util.str2byte("ecdsa-sha2-" + new String(this.name));
    }

    public int getKeyType() {
        return 3;
    }

    public int getKeySize() {
        return this.key_size;
    }

    public byte[] getSignature(byte[] data) {
        try {
            Class<?> c = Class.forName(JSch.getConfig("signature.ecdsa"));
            SignatureECDSA ecdsa = (SignatureECDSA)c.newInstance();
            ecdsa.init();
            ecdsa.setPrvKey(this.prv_array);
            ecdsa.update(data);
            byte[] sig = ecdsa.sign();
            byte[][] tmp = new byte[][]{Util.str2byte("ecdsa-sha2-" + new String(this.name)), sig};
            return Buffer.fromBytes((byte[][])tmp).buffer;
        }
        catch (Exception exception) {
            return null;
        }
    }

    public Signature getVerifier() {
        try {
            Class<?> c = Class.forName(JSch.getConfig("signature.ecdsa"));
            SignatureECDSA ecdsa = (SignatureECDSA)c.newInstance();
            ecdsa.init();
            if (this.r_array == null && this.s_array == null && this.getPublicKeyBlob() != null) {
                Buffer buf = new Buffer(this.getPublicKeyBlob());
                buf.getString();
                buf.getString();
                byte[][] tmp = KeyPairECDSA.fromPoint(buf.getString());
                this.r_array = tmp[0];
                this.s_array = tmp[1];
            }
            ecdsa.setPubKey(this.r_array, this.s_array);
            return ecdsa;
        }
        catch (Exception exception) {
            return null;
        }
    }

    static KeyPair fromSSHAgent(JSch jsch, Buffer buf) throws JSchException {
        byte[][] tmp = buf.getBytes(5, "invalid key format");
        byte[] name = tmp[1];
        byte[][] foo = KeyPairECDSA.fromPoint(tmp[2]);
        byte[] r_array = foo[0];
        byte[] s_array = foo[1];
        byte[] prv_array = tmp[3];
        KeyPairECDSA kpair = new KeyPairECDSA(jsch, name, r_array, s_array, prv_array);
        kpair.publicKeyComment = new String(tmp[4]);
        kpair.vendor = 0;
        return kpair;
    }

    public byte[] forSSHAgent() throws JSchException {
        if (this.isEncrypted()) {
            throw new JSchException("key is encrypted.");
        }
        Buffer buf = new Buffer();
        buf.putString(Util.str2byte("ecdsa-sha2-" + new String(this.name)));
        buf.putString(this.name);
        buf.putString(KeyPairECDSA.toPoint(this.r_array, this.s_array));
        buf.putString(this.prv_array);
        buf.putString(Util.str2byte(this.publicKeyComment));
        byte[] result = new byte[buf.getLength()];
        buf.getByte(result, 0, result.length);
        return result;
    }

    static byte[] toPoint(byte[] r_array, byte[] s_array) {
        byte[] tmp = new byte[1 + r_array.length + s_array.length];
        tmp[0] = 4;
        System.arraycopy(r_array, 0, tmp, 1, r_array.length);
        System.arraycopy(s_array, 0, tmp, 1 + r_array.length, s_array.length);
        return tmp;
    }

    static byte[][] fromPoint(byte[] point) {
        int i = 0;
        while (point[i] != 4) {
            ++i;
        }
        byte[][] tmp = new byte[2][];
        byte[] r_array = new byte[(point.length - ++i) / 2];
        byte[] s_array = new byte[(point.length - i) / 2];
        System.arraycopy(point, i, r_array, 0, r_array.length);
        System.arraycopy(point, i + r_array.length, s_array, 0, s_array.length);
        tmp[0] = r_array;
        tmp[1] = s_array;
        return tmp;
    }

    public void dispose() {
        super.dispose();
        Util.bzero(this.prv_array);
    }
}


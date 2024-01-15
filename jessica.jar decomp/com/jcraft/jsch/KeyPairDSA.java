/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.KeyPairGenDSA;
import com.jcraft.jsch.Signature;
import com.jcraft.jsch.SignatureDSA;
import com.jcraft.jsch.Util;
import java.math.BigInteger;

public class KeyPairDSA
extends KeyPair {
    private byte[] P_array;
    private byte[] Q_array;
    private byte[] G_array;
    private byte[] pub_array;
    private byte[] prv_array;
    private int key_size = 1024;
    private static final byte[] begin = Util.str2byte("-----BEGIN DSA PRIVATE KEY-----");
    private static final byte[] end = Util.str2byte("-----END DSA PRIVATE KEY-----");
    private static final byte[] sshdss = Util.str2byte("ssh-dss");

    public KeyPairDSA(JSch jsch) {
        this(jsch, null, null, null, null, null);
    }

    public KeyPairDSA(JSch jsch, byte[] P_array, byte[] Q_array, byte[] G_array, byte[] pub_array, byte[] prv_array) {
        super(jsch);
        this.P_array = P_array;
        this.Q_array = Q_array;
        this.G_array = G_array;
        this.pub_array = pub_array;
        this.prv_array = prv_array;
        if (P_array != null) {
            this.key_size = new BigInteger(P_array).bitLength();
        }
    }

    void generate(int key_size) throws JSchException {
        this.key_size = key_size;
        try {
            Class<?> c = Class.forName(JSch.getConfig("keypairgen.dsa"));
            KeyPairGenDSA keypairgen = (KeyPairGenDSA)c.newInstance();
            keypairgen.init(key_size);
            this.P_array = keypairgen.getP();
            this.Q_array = keypairgen.getQ();
            this.G_array = keypairgen.getG();
            this.pub_array = keypairgen.getY();
            this.prv_array = keypairgen.getX();
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
        int content = 1 + this.countLength(1) + 1 + 1 + this.countLength(this.P_array.length) + this.P_array.length + 1 + this.countLength(this.Q_array.length) + this.Q_array.length + 1 + this.countLength(this.G_array.length) + this.G_array.length + 1 + this.countLength(this.pub_array.length) + this.pub_array.length + 1 + this.countLength(this.prv_array.length) + this.prv_array.length;
        int total = 1 + this.countLength(content) + content;
        byte[] plain = new byte[total];
        int index = 0;
        index = this.writeSEQUENCE(plain, index, content);
        index = this.writeINTEGER(plain, index, new byte[1]);
        index = this.writeINTEGER(plain, index, this.P_array);
        index = this.writeINTEGER(plain, index, this.Q_array);
        index = this.writeINTEGER(plain, index, this.G_array);
        index = this.writeINTEGER(plain, index, this.pub_array);
        index = this.writeINTEGER(plain, index, this.prv_array);
        return plain;
    }

    boolean parse(byte[] plain) {
        try {
            int foo;
            if (this.vendor == 1) {
                if (plain[0] != 48) {
                    Buffer buf = new Buffer(plain);
                    buf.getInt();
                    this.P_array = buf.getMPIntBits();
                    this.G_array = buf.getMPIntBits();
                    this.Q_array = buf.getMPIntBits();
                    this.pub_array = buf.getMPIntBits();
                    this.prv_array = buf.getMPIntBits();
                    if (this.P_array != null) {
                        this.key_size = new BigInteger(this.P_array).bitLength();
                    }
                    return true;
                }
                return false;
            }
            if (this.vendor == 2) {
                Buffer buf = new Buffer(plain);
                buf.skip(plain.length);
                try {
                    byte[][] tmp = buf.getBytes(1, "");
                    this.prv_array = tmp[0];
                }
                catch (JSchException e) {
                    return false;
                }
                return true;
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
            this.P_array = new byte[length];
            System.arraycopy(plain, index, this.P_array, 0, length);
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
            this.Q_array = new byte[length];
            System.arraycopy(plain, index, this.Q_array, 0, length);
            index += length;
            int n5 = ++index;
            ++index;
            length = plain[n5] & 0xFF;
            if ((length & 0x80) != 0) {
                foo = length & 0x7F;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xFF);
                }
            }
            this.G_array = new byte[length];
            System.arraycopy(plain, index, this.G_array, 0, length);
            index += length;
            int n6 = ++index;
            ++index;
            length = plain[n6] & 0xFF;
            if ((length & 0x80) != 0) {
                foo = length & 0x7F;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xFF);
                }
            }
            this.pub_array = new byte[length];
            System.arraycopy(plain, index, this.pub_array, 0, length);
            index += length;
            int n7 = ++index;
            ++index;
            length = plain[n7] & 0xFF;
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
            if (this.P_array != null) {
                this.key_size = new BigInteger(this.P_array).bitLength();
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
        if (this.P_array == null) {
            return null;
        }
        byte[][] tmp = new byte[][]{sshdss, this.P_array, this.Q_array, this.G_array, this.pub_array};
        return Buffer.fromBytes((byte[][])tmp).buffer;
    }

    byte[] getKeyTypeName() {
        return sshdss;
    }

    public int getKeyType() {
        return 1;
    }

    public int getKeySize() {
        return this.key_size;
    }

    public byte[] getSignature(byte[] data) {
        try {
            Class<?> c = Class.forName(JSch.getConfig("signature.dss"));
            SignatureDSA dsa = (SignatureDSA)c.newInstance();
            dsa.init();
            dsa.setPrvKey(this.prv_array, this.P_array, this.Q_array, this.G_array);
            dsa.update(data);
            byte[] sig = dsa.sign();
            byte[][] tmp = new byte[][]{sshdss, sig};
            return Buffer.fromBytes((byte[][])tmp).buffer;
        }
        catch (Exception exception) {
            return null;
        }
    }

    public Signature getVerifier() {
        try {
            Class<?> c = Class.forName(JSch.getConfig("signature.dss"));
            SignatureDSA dsa = (SignatureDSA)c.newInstance();
            dsa.init();
            if (this.pub_array == null && this.P_array == null && this.getPublicKeyBlob() != null) {
                Buffer buf = new Buffer(this.getPublicKeyBlob());
                buf.getString();
                this.P_array = buf.getString();
                this.Q_array = buf.getString();
                this.G_array = buf.getString();
                this.pub_array = buf.getString();
            }
            dsa.setPubKey(this.pub_array, this.P_array, this.Q_array, this.G_array);
            return dsa;
        }
        catch (Exception exception) {
            return null;
        }
    }

    static KeyPair fromSSHAgent(JSch jsch, Buffer buf) throws JSchException {
        byte[][] tmp = buf.getBytes(7, "invalid key format");
        byte[] P_array = tmp[1];
        byte[] Q_array = tmp[2];
        byte[] G_array = tmp[3];
        byte[] pub_array = tmp[4];
        byte[] prv_array = tmp[5];
        KeyPairDSA kpair = new KeyPairDSA(jsch, P_array, Q_array, G_array, pub_array, prv_array);
        kpair.publicKeyComment = new String(tmp[6]);
        kpair.vendor = 0;
        return kpair;
    }

    public byte[] forSSHAgent() throws JSchException {
        if (this.isEncrypted()) {
            throw new JSchException("key is encrypted.");
        }
        Buffer buf = new Buffer();
        buf.putString(sshdss);
        buf.putString(this.P_array);
        buf.putString(this.Q_array);
        buf.putString(this.G_array);
        buf.putString(this.pub_array);
        buf.putString(this.prv_array);
        buf.putString(Util.str2byte(this.publicKeyComment));
        byte[] result = new byte[buf.getLength()];
        buf.getByte(result, 0, result.length);
        return result;
    }

    public void dispose() {
        super.dispose();
        Util.bzero(this.prv_array);
    }
}


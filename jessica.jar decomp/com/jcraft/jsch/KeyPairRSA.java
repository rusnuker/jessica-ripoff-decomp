/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.KeyPairGenRSA;
import com.jcraft.jsch.Signature;
import com.jcraft.jsch.SignatureRSA;
import com.jcraft.jsch.Util;
import java.math.BigInteger;

public class KeyPairRSA
extends KeyPair {
    private byte[] n_array;
    private byte[] pub_array;
    private byte[] prv_array;
    private byte[] p_array;
    private byte[] q_array;
    private byte[] ep_array;
    private byte[] eq_array;
    private byte[] c_array;
    private int key_size = 1024;
    private static final byte[] begin = Util.str2byte("-----BEGIN RSA PRIVATE KEY-----");
    private static final byte[] end = Util.str2byte("-----END RSA PRIVATE KEY-----");
    private static final byte[] sshrsa = Util.str2byte("ssh-rsa");

    public KeyPairRSA(JSch jsch) {
        this(jsch, null, null, null);
    }

    public KeyPairRSA(JSch jsch, byte[] n_array, byte[] pub_array, byte[] prv_array) {
        super(jsch);
        this.n_array = n_array;
        this.pub_array = pub_array;
        this.prv_array = prv_array;
        if (n_array != null) {
            this.key_size = new BigInteger(n_array).bitLength();
        }
    }

    void generate(int key_size) throws JSchException {
        this.key_size = key_size;
        try {
            Class<?> c = Class.forName(JSch.getConfig("keypairgen.rsa"));
            KeyPairGenRSA keypairgen = (KeyPairGenRSA)c.newInstance();
            keypairgen.init(key_size);
            this.pub_array = keypairgen.getE();
            this.prv_array = keypairgen.getD();
            this.n_array = keypairgen.getN();
            this.p_array = keypairgen.getP();
            this.q_array = keypairgen.getQ();
            this.ep_array = keypairgen.getEP();
            this.eq_array = keypairgen.getEQ();
            this.c_array = keypairgen.getC();
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
        int content = 1 + this.countLength(1) + 1 + 1 + this.countLength(this.n_array.length) + this.n_array.length + 1 + this.countLength(this.pub_array.length) + this.pub_array.length + 1 + this.countLength(this.prv_array.length) + this.prv_array.length + 1 + this.countLength(this.p_array.length) + this.p_array.length + 1 + this.countLength(this.q_array.length) + this.q_array.length + 1 + this.countLength(this.ep_array.length) + this.ep_array.length + 1 + this.countLength(this.eq_array.length) + this.eq_array.length + 1 + this.countLength(this.c_array.length) + this.c_array.length;
        int total = 1 + this.countLength(content) + content;
        byte[] plain = new byte[total];
        int index = 0;
        index = this.writeSEQUENCE(plain, index, content);
        index = this.writeINTEGER(plain, index, new byte[1]);
        index = this.writeINTEGER(plain, index, this.n_array);
        index = this.writeINTEGER(plain, index, this.pub_array);
        index = this.writeINTEGER(plain, index, this.prv_array);
        index = this.writeINTEGER(plain, index, this.p_array);
        index = this.writeINTEGER(plain, index, this.q_array);
        index = this.writeINTEGER(plain, index, this.ep_array);
        index = this.writeINTEGER(plain, index, this.eq_array);
        index = this.writeINTEGER(plain, index, this.c_array);
        return plain;
    }

    boolean parse(byte[] plain) {
        try {
            int foo;
            int index = 0;
            int length = 0;
            if (this.vendor == 2) {
                Buffer buf = new Buffer(plain);
                buf.skip(plain.length);
                try {
                    byte[][] tmp = buf.getBytes(4, "");
                    this.prv_array = tmp[0];
                    this.p_array = tmp[1];
                    this.q_array = tmp[2];
                    this.c_array = tmp[3];
                }
                catch (JSchException e) {
                    return false;
                }
                this.getEPArray();
                this.getEQArray();
                return true;
            }
            if (this.vendor == 1) {
                if (plain[index] != 48) {
                    Buffer buf = new Buffer(plain);
                    this.pub_array = buf.getMPIntBits();
                    this.prv_array = buf.getMPIntBits();
                    this.n_array = buf.getMPIntBits();
                    byte[] u_array = buf.getMPIntBits();
                    this.p_array = buf.getMPIntBits();
                    this.q_array = buf.getMPIntBits();
                    if (this.n_array != null) {
                        this.key_size = new BigInteger(this.n_array).bitLength();
                    }
                    this.getEPArray();
                    this.getEQArray();
                    this.getCArray();
                    return true;
                }
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
            this.n_array = new byte[length];
            System.arraycopy(plain, index, this.n_array, 0, length);
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
            this.pub_array = new byte[length];
            System.arraycopy(plain, index, this.pub_array, 0, length);
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
            this.prv_array = new byte[length];
            System.arraycopy(plain, index, this.prv_array, 0, length);
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
            this.p_array = new byte[length];
            System.arraycopy(plain, index, this.p_array, 0, length);
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
            this.q_array = new byte[length];
            System.arraycopy(plain, index, this.q_array, 0, length);
            index += length;
            int n8 = ++index;
            ++index;
            length = plain[n8] & 0xFF;
            if ((length & 0x80) != 0) {
                foo = length & 0x7F;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xFF);
                }
            }
            this.ep_array = new byte[length];
            System.arraycopy(plain, index, this.ep_array, 0, length);
            index += length;
            int n9 = ++index;
            ++index;
            length = plain[n9] & 0xFF;
            if ((length & 0x80) != 0) {
                foo = length & 0x7F;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xFF);
                }
            }
            this.eq_array = new byte[length];
            System.arraycopy(plain, index, this.eq_array, 0, length);
            index += length;
            int n10 = ++index;
            ++index;
            length = plain[n10] & 0xFF;
            if ((length & 0x80) != 0) {
                foo = length & 0x7F;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xFF);
                }
            }
            this.c_array = new byte[length];
            System.arraycopy(plain, index, this.c_array, 0, length);
            index += length;
            if (this.n_array != null) {
                this.key_size = new BigInteger(this.n_array).bitLength();
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
        if (this.pub_array == null) {
            return null;
        }
        byte[][] tmp = new byte[][]{sshrsa, this.pub_array, this.n_array};
        return Buffer.fromBytes((byte[][])tmp).buffer;
    }

    byte[] getKeyTypeName() {
        return sshrsa;
    }

    public int getKeyType() {
        return 2;
    }

    public int getKeySize() {
        return this.key_size;
    }

    public byte[] getSignature(byte[] data) {
        try {
            Class<?> c = Class.forName(JSch.getConfig("signature.rsa"));
            SignatureRSA rsa = (SignatureRSA)c.newInstance();
            rsa.init();
            rsa.setPrvKey(this.prv_array, this.n_array);
            rsa.update(data);
            byte[] sig = rsa.sign();
            byte[][] tmp = new byte[][]{sshrsa, sig};
            return Buffer.fromBytes((byte[][])tmp).buffer;
        }
        catch (Exception exception) {
            return null;
        }
    }

    public Signature getVerifier() {
        try {
            Class<?> c = Class.forName(JSch.getConfig("signature.rsa"));
            SignatureRSA rsa = (SignatureRSA)c.newInstance();
            rsa.init();
            if (this.pub_array == null && this.n_array == null && this.getPublicKeyBlob() != null) {
                Buffer buf = new Buffer(this.getPublicKeyBlob());
                buf.getString();
                this.pub_array = buf.getString();
                this.n_array = buf.getString();
            }
            rsa.setPubKey(this.pub_array, this.n_array);
            return rsa;
        }
        catch (Exception exception) {
            return null;
        }
    }

    static KeyPair fromSSHAgent(JSch jsch, Buffer buf) throws JSchException {
        byte[][] tmp = buf.getBytes(8, "invalid key format");
        byte[] n_array = tmp[1];
        byte[] pub_array = tmp[2];
        byte[] prv_array = tmp[3];
        KeyPairRSA kpair = new KeyPairRSA(jsch, n_array, pub_array, prv_array);
        kpair.c_array = tmp[4];
        kpair.p_array = tmp[5];
        kpair.q_array = tmp[6];
        kpair.publicKeyComment = new String(tmp[7]);
        kpair.vendor = 0;
        return kpair;
    }

    public byte[] forSSHAgent() throws JSchException {
        if (this.isEncrypted()) {
            throw new JSchException("key is encrypted.");
        }
        Buffer buf = new Buffer();
        buf.putString(sshrsa);
        buf.putString(this.n_array);
        buf.putString(this.pub_array);
        buf.putString(this.prv_array);
        buf.putString(this.getCArray());
        buf.putString(this.p_array);
        buf.putString(this.q_array);
        buf.putString(Util.str2byte(this.publicKeyComment));
        byte[] result = new byte[buf.getLength()];
        buf.getByte(result, 0, result.length);
        return result;
    }

    private byte[] getEPArray() {
        if (this.ep_array == null) {
            this.ep_array = new BigInteger(this.prv_array).mod(new BigInteger(this.p_array).subtract(BigInteger.ONE)).toByteArray();
        }
        return this.ep_array;
    }

    private byte[] getEQArray() {
        if (this.eq_array == null) {
            this.eq_array = new BigInteger(this.prv_array).mod(new BigInteger(this.q_array).subtract(BigInteger.ONE)).toByteArray();
        }
        return this.eq_array;
    }

    private byte[] getCArray() {
        if (this.c_array == null) {
            this.c_array = new BigInteger(this.q_array).modInverse(new BigInteger(this.p_array)).toByteArray();
        }
        return this.c_array;
    }

    public void dispose() {
        super.dispose();
        Util.bzero(this.prv_array);
    }
}


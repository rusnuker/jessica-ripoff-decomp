/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Cipher;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.KeyPairDSA;
import com.jcraft.jsch.KeyPairRSA;
import com.jcraft.jsch.PBKDF;
import com.jcraft.jsch.Signature;
import com.jcraft.jsch.Util;
import java.math.BigInteger;
import java.util.Vector;

public class KeyPairPKCS8
extends KeyPair {
    private static final byte[] rsaEncryption = new byte[]{42, -122, 72, -122, -9, 13, 1, 1, 1};
    private static final byte[] dsaEncryption = new byte[]{42, -122, 72, -50, 56, 4, 1};
    private static final byte[] pbes2 = new byte[]{42, -122, 72, -122, -9, 13, 1, 5, 13};
    private static final byte[] pbkdf2 = new byte[]{42, -122, 72, -122, -9, 13, 1, 5, 12};
    private static final byte[] aes128cbc = new byte[]{96, -122, 72, 1, 101, 3, 4, 1, 2};
    private static final byte[] aes192cbc = new byte[]{96, -122, 72, 1, 101, 3, 4, 1, 22};
    private static final byte[] aes256cbc = new byte[]{96, -122, 72, 1, 101, 3, 4, 1, 42};
    private static final byte[] pbeWithMD5AndDESCBC = new byte[]{42, -122, 72, -122, -9, 13, 1, 5, 3};
    private KeyPair kpair = null;
    private static final byte[] begin = Util.str2byte("-----BEGIN DSA PRIVATE KEY-----");
    private static final byte[] end = Util.str2byte("-----END DSA PRIVATE KEY-----");

    public KeyPairPKCS8(JSch jsch) {
        super(jsch);
    }

    void generate(int key_size) throws JSchException {
    }

    byte[] getBegin() {
        return begin;
    }

    byte[] getEnd() {
        return end;
    }

    byte[] getPrivateKey() {
        return null;
    }

    boolean parse(byte[] plain) {
        try {
            Vector<byte[]> values = new Vector<byte[]>();
            KeyPair.ASN1[] contents = null;
            KeyPair.ASN1 asn1 = new KeyPair.ASN1(this, plain);
            contents = asn1.getContents();
            KeyPair.ASN1 privateKeyAlgorithm = contents[1];
            KeyPair.ASN1 privateKey = contents[2];
            contents = privateKeyAlgorithm.getContents();
            byte[] privateKeyAlgorithmID = contents[0].getContent();
            contents = contents[1].getContents();
            if (contents.length > 0) {
                for (int i = 0; i < contents.length; ++i) {
                    values.addElement(contents[i].getContent());
                }
            }
            byte[] _data = privateKey.getContent();
            KeyPair _kpair = null;
            if (Util.array_equals(privateKeyAlgorithmID, rsaEncryption)) {
                _kpair = new KeyPairRSA(this.jsch);
                _kpair.copy(this);
                if (_kpair.parse(_data)) {
                    this.kpair = _kpair;
                }
            } else if (Util.array_equals(privateKeyAlgorithmID, dsaEncryption)) {
                asn1 = new KeyPair.ASN1(this, _data);
                if (values.size() == 0) {
                    contents = asn1.getContents();
                    byte[] bar = contents[1].getContent();
                    contents = contents[0].getContents();
                    for (int i = 0; i < contents.length; ++i) {
                        values.addElement(contents[i].getContent());
                    }
                    values.addElement(bar);
                } else {
                    values.addElement(asn1.getContent());
                }
                byte[] P_array = (byte[])values.elementAt(0);
                byte[] Q_array = (byte[])values.elementAt(1);
                byte[] G_array = (byte[])values.elementAt(2);
                byte[] prv_array = (byte[])values.elementAt(3);
                byte[] pub_array = new BigInteger(G_array).modPow(new BigInteger(prv_array), new BigInteger(P_array)).toByteArray();
                KeyPairDSA _key = new KeyPairDSA(this.jsch, P_array, Q_array, G_array, pub_array, prv_array);
                plain = _key.getPrivateKey();
                _kpair = new KeyPairDSA(this.jsch);
                _kpair.copy(this);
                if (_kpair.parse(plain)) {
                    this.kpair = _kpair;
                }
            }
        }
        catch (KeyPair.ASN1Exception e) {
            return false;
        }
        catch (Exception e) {
            return false;
        }
        return this.kpair != null;
    }

    public byte[] getPublicKeyBlob() {
        return this.kpair.getPublicKeyBlob();
    }

    byte[] getKeyTypeName() {
        return this.kpair.getKeyTypeName();
    }

    public int getKeyType() {
        return this.kpair.getKeyType();
    }

    public int getKeySize() {
        return this.kpair.getKeySize();
    }

    public byte[] getSignature(byte[] data) {
        return this.kpair.getSignature(data);
    }

    public Signature getVerifier() {
        return this.kpair.getVerifier();
    }

    public byte[] forSSHAgent() throws JSchException {
        return this.kpair.forSSHAgent();
    }

    public boolean decrypt(byte[] _passphrase) {
        if (!this.isEncrypted()) {
            return true;
        }
        if (_passphrase == null) {
            return !this.isEncrypted();
        }
        try {
            KeyPair.ASN1[] contents = null;
            KeyPair.ASN1 asn1 = new KeyPair.ASN1(this, this.data);
            contents = asn1.getContents();
            byte[] _data = contents[1].getContent();
            KeyPair.ASN1 pbes = contents[0];
            contents = pbes.getContents();
            byte[] pbesid = contents[0].getContent();
            KeyPair.ASN1 pbesparam = contents[1];
            byte[] salt = null;
            int iterations = 0;
            byte[] iv = null;
            byte[] encryptfuncid = null;
            if (!Util.array_equals(pbesid, pbes2)) {
                if (Util.array_equals(pbesid, pbeWithMD5AndDESCBC)) {
                    return false;
                }
                return false;
            }
            contents = pbesparam.getContents();
            KeyPair.ASN1 pbkdf = contents[0];
            KeyPair.ASN1 encryptfunc = contents[1];
            contents = pbkdf.getContents();
            byte[] pbkdfid = contents[0].getContent();
            KeyPair.ASN1 pbkdffunc = contents[1];
            contents = pbkdffunc.getContents();
            salt = contents[0].getContent();
            iterations = Integer.parseInt(new BigInteger(contents[1].getContent()).toString());
            contents = encryptfunc.getContents();
            encryptfuncid = contents[0].getContent();
            iv = contents[1].getContent();
            Cipher cipher = this.getCipher(encryptfuncid);
            if (cipher == null) {
                return false;
            }
            byte[] key = null;
            try {
                Class<?> c = Class.forName(JSch.getConfig("pbkdf"));
                PBKDF tmp = (PBKDF)c.newInstance();
                key = tmp.getKey(_passphrase, salt, iterations, cipher.getBlockSize());
            }
            catch (Exception ee) {
                // empty catch block
            }
            if (key == null) {
                return false;
            }
            cipher.init(1, key, iv);
            Util.bzero(key);
            byte[] plain = new byte[_data.length];
            cipher.update(_data, 0, _data.length, plain, 0);
            if (this.parse(plain)) {
                this.encrypted = false;
                return true;
            }
        }
        catch (KeyPair.ASN1Exception e) {
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    Cipher getCipher(byte[] id) {
        Cipher cipher;
        block7: {
            cipher = null;
            String name = null;
            try {
                if (Util.array_equals(id, aes128cbc)) {
                    name = "aes128-cbc";
                } else if (Util.array_equals(id, aes192cbc)) {
                    name = "aes192-cbc";
                } else if (Util.array_equals(id, aes256cbc)) {
                    name = "aes256-cbc";
                }
                Class<?> c = Class.forName(JSch.getConfig(name));
                cipher = (Cipher)c.newInstance();
            }
            catch (Exception e) {
                if (!JSch.getLogger().isEnabled(4)) break block7;
                String message = "";
                message = name == null ? "unknown oid: " + Util.toHex(id) : "function " + name + " is not supported";
                JSch.getLogger().log(4, "PKCS8: " + message);
            }
        }
        return cipher;
    }
}


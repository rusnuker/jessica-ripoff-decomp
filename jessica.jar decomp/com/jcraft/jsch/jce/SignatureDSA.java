/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jce;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;

public class SignatureDSA
implements com.jcraft.jsch.SignatureDSA {
    Signature signature;
    KeyFactory keyFactory;

    public void init() throws Exception {
        this.signature = Signature.getInstance("SHA1withDSA");
        this.keyFactory = KeyFactory.getInstance("DSA");
    }

    public void setPubKey(byte[] y, byte[] p, byte[] q, byte[] g) throws Exception {
        DSAPublicKeySpec dsaPubKeySpec = new DSAPublicKeySpec(new BigInteger(y), new BigInteger(p), new BigInteger(q), new BigInteger(g));
        PublicKey pubKey = this.keyFactory.generatePublic(dsaPubKeySpec);
        this.signature.initVerify(pubKey);
    }

    public void setPrvKey(byte[] x, byte[] p, byte[] q, byte[] g) throws Exception {
        DSAPrivateKeySpec dsaPrivKeySpec = new DSAPrivateKeySpec(new BigInteger(x), new BigInteger(p), new BigInteger(q), new BigInteger(g));
        PrivateKey prvKey = this.keyFactory.generatePrivate(dsaPrivKeySpec);
        this.signature.initSign(prvKey);
    }

    public byte[] sign() throws Exception {
        byte[] sig = this.signature.sign();
        int len = 0;
        int index = 3;
        len = sig[index++] & 0xFF;
        byte[] r = new byte[len];
        System.arraycopy(sig, index, r, 0, r.length);
        index = index + len + 1;
        len = sig[index++] & 0xFF;
        byte[] s = new byte[len];
        System.arraycopy(sig, index, s, 0, s.length);
        byte[] result = new byte[40];
        System.arraycopy(r, r.length > 20 ? 1 : 0, result, r.length > 20 ? 0 : 20 - r.length, r.length > 20 ? 20 : r.length);
        System.arraycopy(s, s.length > 20 ? 1 : 0, result, s.length > 20 ? 20 : 40 - s.length, s.length > 20 ? 20 : s.length);
        return result;
    }

    public void update(byte[] foo) throws Exception {
        this.signature.update(foo);
    }

    public boolean verify(byte[] sig) throws Exception {
        byte[] tmp;
        int i = 0;
        int j = 0;
        if (sig[0] == 0 && sig[1] == 0 && sig[2] == 0) {
            j = sig[i++] << 24 & 0xFF000000 | sig[i++] << 16 & 0xFF0000 | sig[i++] << 8 & 0xFF00 | sig[i++] & 0xFF;
            i += j;
            j = sig[i++] << 24 & 0xFF000000 | sig[i++] << 16 & 0xFF0000 | sig[i++] << 8 & 0xFF00 | sig[i++] & 0xFF;
            tmp = new byte[j];
            System.arraycopy(sig, i, tmp, 0, j);
            sig = tmp;
        }
        int frst = (sig[0] & 0x80) != 0 ? 1 : 0;
        int scnd = (sig[20] & 0x80) != 0 ? 1 : 0;
        int length = sig.length + 6 + frst + scnd;
        tmp = new byte[length];
        tmp[0] = 48;
        tmp[1] = 44;
        tmp[1] = (byte)(tmp[1] + frst);
        tmp[1] = (byte)(tmp[1] + scnd);
        tmp[2] = 2;
        tmp[3] = 20;
        tmp[3] = (byte)(tmp[3] + frst);
        System.arraycopy(sig, 0, tmp, 4 + frst, 20);
        tmp[4 + tmp[3]] = 2;
        tmp[5 + tmp[3]] = 20;
        int n = 5 + tmp[3];
        tmp[n] = (byte)(tmp[n] + scnd);
        System.arraycopy(sig, 20, tmp, 6 + tmp[3] + scnd, 20);
        sig = tmp;
        return this.signature.verify(sig);
    }
}


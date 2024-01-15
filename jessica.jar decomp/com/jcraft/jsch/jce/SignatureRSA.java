/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jce;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class SignatureRSA
implements com.jcraft.jsch.SignatureRSA {
    Signature signature;
    KeyFactory keyFactory;

    public void init() throws Exception {
        this.signature = Signature.getInstance("SHA1withRSA");
        this.keyFactory = KeyFactory.getInstance("RSA");
    }

    public void setPubKey(byte[] e, byte[] n) throws Exception {
        RSAPublicKeySpec rsaPubKeySpec = new RSAPublicKeySpec(new BigInteger(n), new BigInteger(e));
        PublicKey pubKey = this.keyFactory.generatePublic(rsaPubKeySpec);
        this.signature.initVerify(pubKey);
    }

    public void setPrvKey(byte[] d, byte[] n) throws Exception {
        RSAPrivateKeySpec rsaPrivKeySpec = new RSAPrivateKeySpec(new BigInteger(n), new BigInteger(d));
        PrivateKey prvKey = this.keyFactory.generatePrivate(rsaPrivKeySpec);
        this.signature.initSign(prvKey);
    }

    public byte[] sign() throws Exception {
        byte[] sig = this.signature.sign();
        return sig;
    }

    public void update(byte[] foo) throws Exception {
        this.signature.update(foo);
    }

    public boolean verify(byte[] sig) throws Exception {
        int i = 0;
        int j = 0;
        if (sig[0] == 0 && sig[1] == 0 && sig[2] == 0) {
            j = sig[i++] << 24 & 0xFF000000 | sig[i++] << 16 & 0xFF0000 | sig[i++] << 8 & 0xFF00 | sig[i++] & 0xFF;
            i += j;
            j = sig[i++] << 24 & 0xFF000000 | sig[i++] << 16 & 0xFF0000 | sig[i++] << 8 & 0xFF00 | sig[i++] & 0xFF;
            byte[] tmp = new byte[j];
            System.arraycopy(sig, i, tmp, 0, j);
            sig = tmp;
        }
        return this.signature.verify(sig);
    }
}


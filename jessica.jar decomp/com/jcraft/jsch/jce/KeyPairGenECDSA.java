/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jce;

import com.jcraft.jsch.JSchException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;

public class KeyPairGenECDSA
implements com.jcraft.jsch.KeyPairGenECDSA {
    byte[] d;
    byte[] r;
    byte[] s;
    ECPublicKey pubKey;
    ECPrivateKey prvKey;
    ECParameterSpec params;

    public void init(int key_size) throws Exception {
        String name = null;
        if (key_size == 256) {
            name = "secp256r1";
        } else if (key_size == 384) {
            name = "secp384r1";
        } else if (key_size == 521) {
            name = "secp521r1";
        } else {
            throw new JSchException("unsupported key size: " + key_size);
        }
        for (int i = 0; i < 1000; ++i) {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecsp = new ECGenParameterSpec(name);
            kpg.initialize(ecsp);
            KeyPair kp = kpg.genKeyPair();
            this.prvKey = (ECPrivateKey)kp.getPrivate();
            this.pubKey = (ECPublicKey)kp.getPublic();
            this.params = this.pubKey.getParams();
            this.d = this.prvKey.getS().toByteArray();
            ECPoint w = this.pubKey.getW();
            this.r = w.getAffineX().toByteArray();
            this.s = w.getAffineY().toByteArray();
            if (this.r.length == this.s.length && (key_size == 256 && this.r.length == 32 || key_size == 384 && this.r.length == 48 || key_size == 521 && this.r.length == 66)) break;
        }
        if (this.d.length < this.r.length) {
            this.d = this.insert0(this.d);
        }
    }

    public byte[] getD() {
        return this.d;
    }

    public byte[] getR() {
        return this.r;
    }

    public byte[] getS() {
        return this.s;
    }

    ECPublicKey getPublicKey() {
        return this.pubKey;
    }

    ECPrivateKey getPrivateKey() {
        return this.prvKey;
    }

    private byte[] insert0(byte[] buf) {
        byte[] tmp = new byte[buf.length + 1];
        System.arraycopy(buf, 0, tmp, 1, buf.length);
        this.bzero(buf);
        return tmp;
    }

    private byte[] chop0(byte[] buf) {
        if (buf[0] != 0 || (buf[1] & 0x80) == 0) {
            return buf;
        }
        byte[] tmp = new byte[buf.length - 1];
        System.arraycopy(buf, 1, tmp, 0, tmp.length);
        this.bzero(buf);
        return tmp;
    }

    private void bzero(byte[] buf) {
        for (int i = 0; i < buf.length; ++i) {
            buf[i] = 0;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.HASH;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SignatureDSA;
import com.jcraft.jsch.SignatureECDSA;
import com.jcraft.jsch.SignatureRSA;
import com.jcraft.jsch.Util;

public abstract class KeyExchange {
    static final int PROPOSAL_KEX_ALGS = 0;
    static final int PROPOSAL_SERVER_HOST_KEY_ALGS = 1;
    static final int PROPOSAL_ENC_ALGS_CTOS = 2;
    static final int PROPOSAL_ENC_ALGS_STOC = 3;
    static final int PROPOSAL_MAC_ALGS_CTOS = 4;
    static final int PROPOSAL_MAC_ALGS_STOC = 5;
    static final int PROPOSAL_COMP_ALGS_CTOS = 6;
    static final int PROPOSAL_COMP_ALGS_STOC = 7;
    static final int PROPOSAL_LANG_CTOS = 8;
    static final int PROPOSAL_LANG_STOC = 9;
    static final int PROPOSAL_MAX = 10;
    static String kex = "diffie-hellman-group1-sha1";
    static String server_host_key = "ssh-rsa,ssh-dss";
    static String enc_c2s = "blowfish-cbc";
    static String enc_s2c = "blowfish-cbc";
    static String mac_c2s = "hmac-md5";
    static String mac_s2c = "hmac-md5";
    static String lang_c2s = "";
    static String lang_s2c = "";
    public static final int STATE_END = 0;
    protected Session session = null;
    protected HASH sha = null;
    protected byte[] K = null;
    protected byte[] H = null;
    protected byte[] K_S = null;
    protected final int RSA = 0;
    protected final int DSS = 1;
    protected final int ECDSA = 2;
    private int type = 0;
    private String key_alg_name = "";

    public abstract void init(Session var1, byte[] var2, byte[] var3, byte[] var4, byte[] var5) throws Exception;

    public abstract boolean next(Buffer var1) throws Exception;

    public abstract int getState();

    public String getKeyType() {
        if (this.type == 1) {
            return "DSA";
        }
        if (this.type == 0) {
            return "RSA";
        }
        return "ECDSA";
    }

    public String getKeyAlgorithName() {
        return this.key_alg_name;
    }

    protected static String[] guess(byte[] I_S, byte[] I_C) {
        int i;
        String[] guess = new String[10];
        Buffer sb = new Buffer(I_S);
        sb.setOffSet(17);
        Buffer cb = new Buffer(I_C);
        cb.setOffSet(17);
        if (JSch.getLogger().isEnabled(1)) {
            for (i = 0; i < 10; ++i) {
                JSch.getLogger().log(1, "kex: server: " + Util.byte2str(sb.getString()));
            }
            for (i = 0; i < 10; ++i) {
                JSch.getLogger().log(1, "kex: client: " + Util.byte2str(cb.getString()));
            }
            sb.setOffSet(17);
            cb.setOffSet(17);
        }
        for (i = 0; i < 10; ++i) {
            byte[] sp = sb.getString();
            byte[] cp = cb.getString();
            int j = 0;
            int k = 0;
            block3: while (j < cp.length) {
                while (j < cp.length && cp[j] != 44) {
                    ++j;
                }
                if (k == j) {
                    return null;
                }
                String algorithm = Util.byte2str(cp, k, j - k);
                int l = 0;
                int m = 0;
                while (l < sp.length) {
                    while (l < sp.length && sp[l] != 44) {
                        ++l;
                    }
                    if (m == l) {
                        return null;
                    }
                    if (algorithm.equals(Util.byte2str(sp, m, l - m))) {
                        guess[i] = algorithm;
                        break block3;
                    }
                    m = ++l;
                }
                k = ++j;
            }
            if (j == 0) {
                guess[i] = "";
                continue;
            }
            if (guess[i] != null) continue;
            return null;
        }
        if (JSch.getLogger().isEnabled(1)) {
            JSch.getLogger().log(1, "kex: server->client " + guess[3] + " " + guess[5] + " " + guess[7]);
            JSch.getLogger().log(1, "kex: client->server " + guess[2] + " " + guess[4] + " " + guess[6]);
        }
        return guess;
    }

    public String getFingerPrint() {
        HASH hash = null;
        try {
            Class<?> c = Class.forName(this.session.getConfig("md5"));
            hash = (HASH)c.newInstance();
        }
        catch (Exception e) {
            System.err.println("getFingerPrint: " + e);
        }
        return Util.getFingerPrint(hash, this.getHostKey());
    }

    byte[] getK() {
        return this.K;
    }

    byte[] getH() {
        return this.H;
    }

    HASH getHash() {
        return this.sha;
    }

    byte[] getHostKey() {
        return this.K_S;
    }

    protected byte[] normalize(byte[] secret) {
        if (secret.length > 1 && secret[0] == 0 && (secret[1] & 0x80) == 0) {
            byte[] tmp = new byte[secret.length - 1];
            System.arraycopy(secret, 1, tmp, 0, tmp.length);
            return this.normalize(tmp);
        }
        return secret;
    }

    protected boolean verify(String alg, byte[] K_S, int index, byte[] sig_of_H) throws Exception {
        int i = index;
        boolean result = false;
        if (alg.equals("ssh-rsa")) {
            this.type = 0;
            this.key_alg_name = alg;
            int j = K_S[i++] << 24 & 0xFF000000 | K_S[i++] << 16 & 0xFF0000 | K_S[i++] << 8 & 0xFF00 | K_S[i++] & 0xFF;
            byte[] tmp = new byte[j];
            System.arraycopy(K_S, i, tmp, 0, j);
            i += j;
            byte[] ee = tmp;
            j = K_S[i++] << 24 & 0xFF000000 | K_S[i++] << 16 & 0xFF0000 | K_S[i++] << 8 & 0xFF00 | K_S[i++] & 0xFF;
            tmp = new byte[j];
            System.arraycopy(K_S, i, tmp, 0, j);
            i += j;
            byte[] n = tmp;
            SignatureRSA sig = null;
            try {
                Class<?> c = Class.forName(this.session.getConfig("signature.rsa"));
                sig = (SignatureRSA)c.newInstance();
                sig.init();
            }
            catch (Exception e) {
                System.err.println(e);
            }
            sig.setPubKey(ee, n);
            sig.update(this.H);
            result = sig.verify(sig_of_H);
            if (JSch.getLogger().isEnabled(1)) {
                JSch.getLogger().log(1, "ssh_rsa_verify: signature " + result);
            }
        } else if (alg.equals("ssh-dss")) {
            byte[] q = null;
            this.type = 1;
            this.key_alg_name = alg;
            int j = K_S[i++] << 24 & 0xFF000000 | K_S[i++] << 16 & 0xFF0000 | K_S[i++] << 8 & 0xFF00 | K_S[i++] & 0xFF;
            byte[] tmp = new byte[j];
            System.arraycopy(K_S, i, tmp, 0, j);
            i += j;
            byte[] p = tmp;
            j = K_S[i++] << 24 & 0xFF000000 | K_S[i++] << 16 & 0xFF0000 | K_S[i++] << 8 & 0xFF00 | K_S[i++] & 0xFF;
            tmp = new byte[j];
            System.arraycopy(K_S, i, tmp, 0, j);
            i += j;
            q = tmp;
            j = K_S[i++] << 24 & 0xFF000000 | K_S[i++] << 16 & 0xFF0000 | K_S[i++] << 8 & 0xFF00 | K_S[i++] & 0xFF;
            tmp = new byte[j];
            System.arraycopy(K_S, i, tmp, 0, j);
            i += j;
            byte[] g = tmp;
            j = K_S[i++] << 24 & 0xFF000000 | K_S[i++] << 16 & 0xFF0000 | K_S[i++] << 8 & 0xFF00 | K_S[i++] & 0xFF;
            tmp = new byte[j];
            System.arraycopy(K_S, i, tmp, 0, j);
            i += j;
            byte[] f = tmp;
            SignatureDSA sig = null;
            try {
                Class<?> c = Class.forName(this.session.getConfig("signature.dss"));
                sig = (SignatureDSA)c.newInstance();
                sig.init();
            }
            catch (Exception e) {
                System.err.println(e);
            }
            sig.setPubKey(f, p, q, g);
            sig.update(this.H);
            result = sig.verify(sig_of_H);
            if (JSch.getLogger().isEnabled(1)) {
                JSch.getLogger().log(1, "ssh_dss_verify: signature " + result);
            }
        } else if (alg.equals("ecdsa-sha2-nistp256") || alg.equals("ecdsa-sha2-nistp384") || alg.equals("ecdsa-sha2-nistp521")) {
            this.type = 2;
            this.key_alg_name = alg;
            int j = K_S[i++] << 24 & 0xFF000000 | K_S[i++] << 16 & 0xFF0000 | K_S[i++] << 8 & 0xFF00 | K_S[i++] & 0xFF;
            byte[] tmp = new byte[j];
            System.arraycopy(K_S, i, tmp, 0, j);
            i += j;
            j = K_S[i++] << 24 & 0xFF000000 | K_S[i++] << 16 & 0xFF0000 | K_S[i++] << 8 & 0xFF00 | K_S[i++] & 0xFF;
            tmp = new byte[(j - 1) / 2];
            System.arraycopy(K_S, ++i, tmp, 0, tmp.length);
            byte[] r = tmp;
            tmp = new byte[(j - 1) / 2];
            System.arraycopy(K_S, i += (j - 1) / 2, tmp, 0, tmp.length);
            i += (j - 1) / 2;
            byte[] s = tmp;
            SignatureECDSA sig = null;
            try {
                Class<?> c = Class.forName(this.session.getConfig("signature.ecdsa"));
                sig = (SignatureECDSA)c.newInstance();
                sig.init();
            }
            catch (Exception e) {
                System.err.println(e);
            }
            sig.setPubKey(r, s);
            sig.update(this.H);
            result = sig.verify(sig_of_H);
        } else {
            System.err.println("unknown alg");
        }
        return result;
    }
}


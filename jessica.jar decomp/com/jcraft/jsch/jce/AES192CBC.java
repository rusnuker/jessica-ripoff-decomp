/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jce;

import com.jcraft.jsch.Cipher;
import java.security.Key;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES192CBC
implements Cipher {
    private static final int ivsize = 16;
    private static final int bsize = 24;
    private javax.crypto.Cipher cipher;

    public int getIVSize() {
        return 16;
    }

    public int getBlockSize() {
        return 24;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void init(int mode, byte[] key, byte[] iv) throws Exception {
        byte[] tmp;
        String pad = "NoPadding";
        if (iv.length > 16) {
            tmp = new byte[16];
            System.arraycopy(iv, 0, tmp, 0, tmp.length);
            iv = tmp;
        }
        if (key.length > 24) {
            tmp = new byte[24];
            System.arraycopy(key, 0, tmp, 0, tmp.length);
            key = tmp;
        }
        try {
            SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
            this.cipher = javax.crypto.Cipher.getInstance("AES/CBC/" + pad);
            Class clazz = javax.crypto.Cipher.class;
            synchronized (clazz) {
                this.cipher.init(mode == 0 ? 1 : 2, (Key)keyspec, new IvParameterSpec(iv));
            }
        }
        catch (Exception e) {
            this.cipher = null;
            throw e;
        }
    }

    public void update(byte[] foo, int s1, int len, byte[] bar, int s2) throws Exception {
        this.cipher.update(foo, s1, len, bar, s2);
    }

    public boolean isCBC() {
        return true;
    }
}


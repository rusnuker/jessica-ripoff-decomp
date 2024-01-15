/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jce;

import com.jcraft.jsch.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ARCFOUR256
implements Cipher {
    private static final int ivsize = 8;
    private static final int bsize = 32;
    private static final int skip = 1536;
    private javax.crypto.Cipher cipher;

    public int getIVSize() {
        return 8;
    }

    public int getBlockSize() {
        return 32;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void init(int mode, byte[] key, byte[] iv) throws Exception {
        if (key.length > 32) {
            byte[] tmp = new byte[32];
            System.arraycopy(key, 0, tmp, 0, tmp.length);
            key = tmp;
        }
        try {
            this.cipher = javax.crypto.Cipher.getInstance("RC4");
            SecretKeySpec _key = new SecretKeySpec(key, "RC4");
            Class clazz = javax.crypto.Cipher.class;
            synchronized (clazz) {
                this.cipher.init(mode == 0 ? 1 : 2, _key);
            }
            byte[] foo = new byte[1];
            for (int i = 0; i < 1536; ++i) {
                this.cipher.update(foo, 0, 1, foo, 0);
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
        return false;
    }
}


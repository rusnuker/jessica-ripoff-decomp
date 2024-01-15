/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jce;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PBKDF
implements com.jcraft.jsch.PBKDF {
    public byte[] getKey(byte[] _pass, byte[] salt, int iterations, int size) {
        char[] pass = new char[_pass.length];
        for (int i = 0; i < _pass.length; ++i) {
            pass[i] = (char)(_pass[i] & 0xFF);
        }
        try {
            PBEKeySpec spec = new PBEKeySpec(pass, salt, iterations, size * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] key = skf.generateSecret(spec).getEncoded();
            return key;
        }
        catch (InvalidKeySpecException e) {
        }
        catch (NoSuchAlgorithmException e) {
            // empty catch block
        }
        return null;
    }
}


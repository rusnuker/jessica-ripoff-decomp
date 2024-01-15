/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Identity;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

class IdentityFile
implements Identity {
    private JSch jsch;
    private KeyPair kpair;
    private String identity;

    static IdentityFile newInstance(String prvfile, String pubfile, JSch jsch) throws JSchException {
        KeyPair kpair = KeyPair.load(jsch, prvfile, pubfile);
        return new IdentityFile(jsch, prvfile, kpair);
    }

    static IdentityFile newInstance(String name, byte[] prvkey, byte[] pubkey, JSch jsch) throws JSchException {
        KeyPair kpair = KeyPair.load(jsch, prvkey, pubkey);
        return new IdentityFile(jsch, name, kpair);
    }

    private IdentityFile(JSch jsch, String name, KeyPair kpair) throws JSchException {
        this.jsch = jsch;
        this.identity = name;
        this.kpair = kpair;
    }

    public boolean setPassphrase(byte[] passphrase) throws JSchException {
        return this.kpair.decrypt(passphrase);
    }

    public byte[] getPublicKeyBlob() {
        return this.kpair.getPublicKeyBlob();
    }

    public byte[] getSignature(byte[] data) {
        return this.kpair.getSignature(data);
    }

    public boolean decrypt() {
        throw new RuntimeException("not implemented");
    }

    public String getAlgName() {
        return new String(this.kpair.getKeyTypeName());
    }

    public String getName() {
        return this.identity;
    }

    public boolean isEncrypted() {
        return this.kpair.isEncrypted();
    }

    public void clear() {
        this.kpair.dispose();
        this.kpair = null;
    }

    public KeyPair getKeyPair() {
        return this.kpair;
    }
}


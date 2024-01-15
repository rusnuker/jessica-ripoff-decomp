/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.Identity;
import com.jcraft.jsch.IdentityFile;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KnownHosts;
import com.jcraft.jsch.LocalIdentityRepository;
import com.jcraft.jsch.Logger;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Util;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class JSch {
    public static final String VERSION = "0.1.54";
    static Hashtable config = new Hashtable();
    private Vector sessionPool = new Vector();
    private IdentityRepository defaultIdentityRepository;
    private IdentityRepository identityRepository = this.defaultIdentityRepository = new LocalIdentityRepository(this);
    private ConfigRepository configRepository = null;
    private HostKeyRepository known_hosts = null;
    private static final Logger DEVNULL;
    static Logger logger;

    public synchronized void setIdentityRepository(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository == null ? this.defaultIdentityRepository : identityRepository;
    }

    public synchronized IdentityRepository getIdentityRepository() {
        return this.identityRepository;
    }

    public ConfigRepository getConfigRepository() {
        return this.configRepository;
    }

    public void setConfigRepository(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public Session getSession(String host) throws JSchException {
        return this.getSession(null, host, 22);
    }

    public Session getSession(String username, String host) throws JSchException {
        return this.getSession(username, host, 22);
    }

    public Session getSession(String username, String host, int port) throws JSchException {
        if (host == null) {
            throw new JSchException("host must not be null.");
        }
        Session s = new Session(this, username, host, port);
        return s;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void addSession(Session session) {
        Vector vector = this.sessionPool;
        synchronized (vector) {
            this.sessionPool.addElement(session);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected boolean removeSession(Session session) {
        Vector vector = this.sessionPool;
        synchronized (vector) {
            return this.sessionPool.remove(session);
        }
    }

    public void setHostKeyRepository(HostKeyRepository hkrepo) {
        this.known_hosts = hkrepo;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setKnownHosts(String filename) throws JSchException {
        if (this.known_hosts == null) {
            this.known_hosts = new KnownHosts(this);
        }
        if (this.known_hosts instanceof KnownHosts) {
            HostKeyRepository hostKeyRepository = this.known_hosts;
            synchronized (hostKeyRepository) {
                ((KnownHosts)this.known_hosts).setKnownHosts(filename);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setKnownHosts(InputStream stream) throws JSchException {
        if (this.known_hosts == null) {
            this.known_hosts = new KnownHosts(this);
        }
        if (this.known_hosts instanceof KnownHosts) {
            HostKeyRepository hostKeyRepository = this.known_hosts;
            synchronized (hostKeyRepository) {
                ((KnownHosts)this.known_hosts).setKnownHosts(stream);
            }
        }
    }

    public HostKeyRepository getHostKeyRepository() {
        if (this.known_hosts == null) {
            this.known_hosts = new KnownHosts(this);
        }
        return this.known_hosts;
    }

    public void addIdentity(String prvkey) throws JSchException {
        this.addIdentity(prvkey, (byte[])null);
    }

    public void addIdentity(String prvkey, String passphrase) throws JSchException {
        byte[] _passphrase = null;
        if (passphrase != null) {
            _passphrase = Util.str2byte(passphrase);
        }
        this.addIdentity(prvkey, _passphrase);
        if (_passphrase != null) {
            Util.bzero(_passphrase);
        }
    }

    public void addIdentity(String prvkey, byte[] passphrase) throws JSchException {
        IdentityFile identity = IdentityFile.newInstance(prvkey, null, this);
        this.addIdentity(identity, passphrase);
    }

    public void addIdentity(String prvkey, String pubkey, byte[] passphrase) throws JSchException {
        IdentityFile identity = IdentityFile.newInstance(prvkey, pubkey, this);
        this.addIdentity(identity, passphrase);
    }

    public void addIdentity(String name, byte[] prvkey, byte[] pubkey, byte[] passphrase) throws JSchException {
        IdentityFile identity = IdentityFile.newInstance(name, prvkey, pubkey, this);
        this.addIdentity(identity, passphrase);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addIdentity(Identity identity, byte[] passphrase) throws JSchException {
        if (passphrase != null) {
            try {
                byte[] goo = new byte[passphrase.length];
                System.arraycopy(passphrase, 0, goo, 0, passphrase.length);
                passphrase = goo;
                identity.setPassphrase(passphrase);
            }
            finally {
                Util.bzero(passphrase);
            }
        }
        if (this.identityRepository instanceof LocalIdentityRepository) {
            ((LocalIdentityRepository)this.identityRepository).add(identity);
        } else if (identity instanceof IdentityFile && !identity.isEncrypted()) {
            this.identityRepository.add(((IdentityFile)identity).getKeyPair().forSSHAgent());
        } else {
            JSch jSch = this;
            synchronized (jSch) {
                if (!(this.identityRepository instanceof IdentityRepository.Wrapper)) {
                    this.setIdentityRepository(new IdentityRepository.Wrapper(this.identityRepository));
                }
            }
            ((IdentityRepository.Wrapper)this.identityRepository).add(identity);
        }
    }

    public void removeIdentity(String name) throws JSchException {
        Vector identities = this.identityRepository.getIdentities();
        for (int i = 0; i < identities.size(); ++i) {
            Identity identity = (Identity)identities.elementAt(i);
            if (!identity.getName().equals(name)) continue;
            if (this.identityRepository instanceof LocalIdentityRepository) {
                ((LocalIdentityRepository)this.identityRepository).remove(identity);
                continue;
            }
            this.identityRepository.remove(identity.getPublicKeyBlob());
        }
    }

    public void removeIdentity(Identity identity) throws JSchException {
        this.identityRepository.remove(identity.getPublicKeyBlob());
    }

    public Vector getIdentityNames() throws JSchException {
        Vector<String> foo = new Vector<String>();
        Vector identities = this.identityRepository.getIdentities();
        for (int i = 0; i < identities.size(); ++i) {
            Identity identity = (Identity)identities.elementAt(i);
            foo.addElement(identity.getName());
        }
        return foo;
    }

    public void removeAllIdentity() throws JSchException {
        this.identityRepository.removeAll();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getConfig(String key) {
        Hashtable hashtable = config;
        synchronized (hashtable) {
            return (String)config.get(key);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setConfig(Hashtable newconf) {
        Hashtable hashtable = config;
        synchronized (hashtable) {
            Enumeration e = newconf.keys();
            while (e.hasMoreElements()) {
                String key = (String)e.nextElement();
                config.put(key, (String)newconf.get(key));
            }
        }
    }

    public static void setConfig(String key, String value) {
        config.put(key, value);
    }

    public static void setLogger(Logger logger) {
        if (logger == null) {
            logger = DEVNULL;
        }
        JSch.logger = logger;
    }

    static Logger getLogger() {
        return logger;
    }

    static {
        config.put("kex", "ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha256,diffie-hellman-group-exchange-sha1,diffie-hellman-group1-sha1");
        config.put("server_host_key", "ssh-rsa,ssh-dss,ecdsa-sha2-nistp256,ecdsa-sha2-nistp384,ecdsa-sha2-nistp521");
        config.put("cipher.s2c", "aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc,aes192-ctr,aes192-cbc,aes256-ctr,aes256-cbc");
        config.put("cipher.c2s", "aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc,aes192-ctr,aes192-cbc,aes256-ctr,aes256-cbc");
        config.put("mac.s2c", "hmac-md5,hmac-sha1,hmac-sha2-256,hmac-sha1-96,hmac-md5-96");
        config.put("mac.c2s", "hmac-md5,hmac-sha1,hmac-sha2-256,hmac-sha1-96,hmac-md5-96");
        config.put("compression.s2c", "none");
        config.put("compression.c2s", "none");
        config.put("lang.s2c", "");
        config.put("lang.c2s", "");
        config.put("compression_level", "6");
        config.put("diffie-hellman-group-exchange-sha1", "com.jcraft.jsch.DHGEX");
        config.put("diffie-hellman-group1-sha1", "com.jcraft.jsch.DHG1");
        config.put("diffie-hellman-group14-sha1", "com.jcraft.jsch.DHG14");
        config.put("diffie-hellman-group-exchange-sha256", "com.jcraft.jsch.DHGEX256");
        config.put("ecdsa-sha2-nistp256", "com.jcraft.jsch.jce.SignatureECDSA");
        config.put("ecdsa-sha2-nistp384", "com.jcraft.jsch.jce.SignatureECDSA");
        config.put("ecdsa-sha2-nistp521", "com.jcraft.jsch.jce.SignatureECDSA");
        config.put("ecdh-sha2-nistp256", "com.jcraft.jsch.DHEC256");
        config.put("ecdh-sha2-nistp384", "com.jcraft.jsch.DHEC384");
        config.put("ecdh-sha2-nistp521", "com.jcraft.jsch.DHEC521");
        config.put("ecdh-sha2-nistp", "com.jcraft.jsch.jce.ECDHN");
        config.put("dh", "com.jcraft.jsch.jce.DH");
        config.put("3des-cbc", "com.jcraft.jsch.jce.TripleDESCBC");
        config.put("blowfish-cbc", "com.jcraft.jsch.jce.BlowfishCBC");
        config.put("hmac-sha1", "com.jcraft.jsch.jce.HMACSHA1");
        config.put("hmac-sha1-96", "com.jcraft.jsch.jce.HMACSHA196");
        config.put("hmac-sha2-256", "com.jcraft.jsch.jce.HMACSHA256");
        config.put("hmac-md5", "com.jcraft.jsch.jce.HMACMD5");
        config.put("hmac-md5-96", "com.jcraft.jsch.jce.HMACMD596");
        config.put("sha-1", "com.jcraft.jsch.jce.SHA1");
        config.put("sha-256", "com.jcraft.jsch.jce.SHA256");
        config.put("sha-384", "com.jcraft.jsch.jce.SHA384");
        config.put("sha-512", "com.jcraft.jsch.jce.SHA512");
        config.put("md5", "com.jcraft.jsch.jce.MD5");
        config.put("signature.dss", "com.jcraft.jsch.jce.SignatureDSA");
        config.put("signature.rsa", "com.jcraft.jsch.jce.SignatureRSA");
        config.put("signature.ecdsa", "com.jcraft.jsch.jce.SignatureECDSA");
        config.put("keypairgen.dsa", "com.jcraft.jsch.jce.KeyPairGenDSA");
        config.put("keypairgen.rsa", "com.jcraft.jsch.jce.KeyPairGenRSA");
        config.put("keypairgen.ecdsa", "com.jcraft.jsch.jce.KeyPairGenECDSA");
        config.put("random", "com.jcraft.jsch.jce.Random");
        config.put("none", "com.jcraft.jsch.CipherNone");
        config.put("aes128-cbc", "com.jcraft.jsch.jce.AES128CBC");
        config.put("aes192-cbc", "com.jcraft.jsch.jce.AES192CBC");
        config.put("aes256-cbc", "com.jcraft.jsch.jce.AES256CBC");
        config.put("aes128-ctr", "com.jcraft.jsch.jce.AES128CTR");
        config.put("aes192-ctr", "com.jcraft.jsch.jce.AES192CTR");
        config.put("aes256-ctr", "com.jcraft.jsch.jce.AES256CTR");
        config.put("3des-ctr", "com.jcraft.jsch.jce.TripleDESCTR");
        config.put("arcfour", "com.jcraft.jsch.jce.ARCFOUR");
        config.put("arcfour128", "com.jcraft.jsch.jce.ARCFOUR128");
        config.put("arcfour256", "com.jcraft.jsch.jce.ARCFOUR256");
        config.put("userauth.none", "com.jcraft.jsch.UserAuthNone");
        config.put("userauth.password", "com.jcraft.jsch.UserAuthPassword");
        config.put("userauth.keyboard-interactive", "com.jcraft.jsch.UserAuthKeyboardInteractive");
        config.put("userauth.publickey", "com.jcraft.jsch.UserAuthPublicKey");
        config.put("userauth.gssapi-with-mic", "com.jcraft.jsch.UserAuthGSSAPIWithMIC");
        config.put("gssapi-with-mic.krb5", "com.jcraft.jsch.jgss.GSSContextKrb5");
        config.put("zlib", "com.jcraft.jsch.jcraft.Compression");
        config.put("zlib@openssh.com", "com.jcraft.jsch.jcraft.Compression");
        config.put("pbkdf", "com.jcraft.jsch.jce.PBKDF");
        config.put("StrictHostKeyChecking", "ask");
        config.put("HashKnownHosts", "no");
        config.put("PreferredAuthentications", "gssapi-with-mic,publickey,keyboard-interactive,password");
        config.put("CheckCiphers", "aes256-ctr,aes192-ctr,aes128-ctr,aes256-cbc,aes192-cbc,aes128-cbc,3des-ctr,arcfour,arcfour128,arcfour256");
        config.put("CheckKexes", "diffie-hellman-group14-sha1,ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521");
        config.put("CheckSignatures", "ecdsa-sha2-nistp256,ecdsa-sha2-nistp384,ecdsa-sha2-nistp521");
        config.put("MaxAuthTries", "6");
        config.put("ClearAllForwardings", "no");
        logger = DEVNULL = new Logger(){

            public boolean isEnabled(int level) {
                return false;
            }

            public void log(int level, String message) {
            }
        };
    }
}


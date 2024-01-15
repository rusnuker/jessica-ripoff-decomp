/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jgss;

import com.jcraft.jsch.GSSContext;
import com.jcraft.jsch.JSchException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.Oid;

public class GSSContextKrb5
implements GSSContext {
    private static final String pUseSubjectCredsOnly = "javax.security.auth.useSubjectCredsOnly";
    private static String useSubjectCredsOnly = GSSContextKrb5.getSystemProperty("javax.security.auth.useSubjectCredsOnly");
    private org.ietf.jgss.GSSContext context = null;

    public void create(String user, String host) throws JSchException {
        try {
            Oid krb5 = new Oid("1.2.840.113554.1.2.2");
            Oid principalName = new Oid("1.2.840.113554.1.2.2.1");
            GSSManager mgr = GSSManager.getInstance();
            GSSCredential crd = null;
            String cname = host;
            try {
                cname = InetAddress.getByName(cname).getCanonicalHostName();
            }
            catch (UnknownHostException e) {
                // empty catch block
            }
            GSSName _host = mgr.createName("host/" + cname, principalName);
            this.context = mgr.createContext(_host, krb5, crd, 0);
            this.context.requestMutualAuth(true);
            this.context.requestConf(true);
            this.context.requestInteg(true);
            this.context.requestCredDeleg(true);
            this.context.requestAnonymity(false);
            return;
        }
        catch (GSSException ex) {
            throw new JSchException(ex.toString());
        }
    }

    public boolean isEstablished() {
        return this.context.isEstablished();
    }

    public byte[] init(byte[] token, int s, int l) throws JSchException {
        try {
            if (useSubjectCredsOnly == null) {
                GSSContextKrb5.setSystemProperty(pUseSubjectCredsOnly, "false");
            }
            byte[] byArray = this.context.initSecContext(token, 0, l);
            return byArray;
        }
        catch (GSSException ex) {
            throw new JSchException(ex.toString());
        }
        catch (SecurityException ex) {
            throw new JSchException(ex.toString());
        }
        finally {
            if (useSubjectCredsOnly == null) {
                GSSContextKrb5.setSystemProperty(pUseSubjectCredsOnly, "true");
            }
        }
    }

    public byte[] getMIC(byte[] message, int s, int l) {
        try {
            MessageProp prop = new MessageProp(0, true);
            return this.context.getMIC(message, s, l, prop);
        }
        catch (GSSException ex) {
            return null;
        }
    }

    public void dispose() {
        try {
            this.context.dispose();
        }
        catch (GSSException gSSException) {
            // empty catch block
        }
    }

    private static String getSystemProperty(String key) {
        try {
            return System.getProperty(key);
        }
        catch (Exception e) {
            return null;
        }
    }

    private static void setSystemProperty(String key, String value) {
        try {
            System.setProperty(key, value);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}


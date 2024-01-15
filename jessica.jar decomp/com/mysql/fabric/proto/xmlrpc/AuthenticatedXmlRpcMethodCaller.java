/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.proto.xmlrpc;

import com.mysql.fabric.FabricCommunicationException;
import com.mysql.fabric.proto.xmlrpc.DigestAuthentication;
import com.mysql.fabric.proto.xmlrpc.XmlRpcMethodCaller;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class AuthenticatedXmlRpcMethodCaller
implements XmlRpcMethodCaller {
    private XmlRpcMethodCaller underlyingCaller;
    private String url;
    private String username;
    private String password;

    public AuthenticatedXmlRpcMethodCaller(XmlRpcMethodCaller underlyingCaller, String url, String username, String password) {
        this.underlyingCaller = underlyingCaller;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public void setHeader(String name, String value) {
        this.underlyingCaller.setHeader(name, value);
    }

    @Override
    public void clearHeader(String name) {
        this.underlyingCaller.clearHeader(name);
    }

    @Override
    public List<?> call(String methodName, Object[] args) throws FabricCommunicationException {
        String authenticateHeader;
        try {
            authenticateHeader = DigestAuthentication.getChallengeHeader(this.url);
        }
        catch (IOException ex) {
            throw new FabricCommunicationException("Unable to obtain challenge header for authentication", ex);
        }
        Map<String, String> digestChallenge = DigestAuthentication.parseDigestChallenge(authenticateHeader);
        String authorizationHeader = DigestAuthentication.generateAuthorizationHeader(digestChallenge, this.username, this.password);
        this.underlyingCaller.setHeader("Authorization", authorizationHeader);
        return this.underlyingCaller.call(methodName, args);
    }
}


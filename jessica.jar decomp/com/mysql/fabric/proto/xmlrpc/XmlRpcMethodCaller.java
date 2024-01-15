/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.proto.xmlrpc;

import com.mysql.fabric.FabricCommunicationException;
import java.util.List;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface XmlRpcMethodCaller {
    public List<?> call(String var1, Object[] var2) throws FabricCommunicationException;

    public void setHeader(String var1, String var2);

    public void clearHeader(String var1);
}


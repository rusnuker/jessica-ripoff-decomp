/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.proto.xmlrpc;

import com.mysql.fabric.FabricCommunicationException;
import com.mysql.fabric.proto.xmlrpc.XmlRpcMethodCaller;
import com.mysql.fabric.xmlrpc.Client;
import com.mysql.fabric.xmlrpc.base.Array;
import com.mysql.fabric.xmlrpc.base.Member;
import com.mysql.fabric.xmlrpc.base.MethodCall;
import com.mysql.fabric.xmlrpc.base.MethodResponse;
import com.mysql.fabric.xmlrpc.base.Param;
import com.mysql.fabric.xmlrpc.base.Params;
import com.mysql.fabric.xmlrpc.base.Struct;
import com.mysql.fabric.xmlrpc.base.Value;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class InternalXmlRpcMethodCaller
implements XmlRpcMethodCaller {
    private Client xmlRpcClient;

    public InternalXmlRpcMethodCaller(String url) throws FabricCommunicationException {
        try {
            this.xmlRpcClient = new Client(url);
        }
        catch (MalformedURLException ex) {
            throw new FabricCommunicationException(ex);
        }
    }

    private Object unwrapValue(Value v) {
        if (v.getType() == 8) {
            return this.methodResponseArrayToList((Array)v.getValue());
        }
        if (v.getType() == 7) {
            HashMap<String, Object> s = new HashMap<String, Object>();
            for (Member m : ((Struct)v.getValue()).getMember()) {
                s.put(m.getName(), this.unwrapValue(m.getValue()));
            }
            return s;
        }
        return v.getValue();
    }

    private List<Object> methodResponseArrayToList(Array array) {
        ArrayList<Object> result = new ArrayList<Object>();
        for (Value v : array.getData().getValue()) {
            result.add(this.unwrapValue(v));
        }
        return result;
    }

    @Override
    public void setHeader(String name, String value) {
        this.xmlRpcClient.setHeader(name, value);
    }

    @Override
    public void clearHeader(String name) {
        this.xmlRpcClient.clearHeader(name);
    }

    public List<Object> call(String methodName, Object[] args) throws FabricCommunicationException {
        MethodCall methodCall = new MethodCall();
        Params p = new Params();
        if (args == null) {
            args = new Object[]{};
        }
        for (int i = 0; i < args.length; ++i) {
            if (args[i] == null) {
                throw new NullPointerException("nil args unsupported");
            }
            if (String.class.isAssignableFrom(args[i].getClass())) {
                p.addParam(new Param(new Value((String)args[i])));
                continue;
            }
            if (Double.class.isAssignableFrom(args[i].getClass())) {
                p.addParam(new Param(new Value((Double)args[i])));
                continue;
            }
            if (Integer.class.isAssignableFrom(args[i].getClass())) {
                p.addParam(new Param(new Value((Integer)args[i])));
                continue;
            }
            throw new IllegalArgumentException("Unknown argument type: " + args[i].getClass());
        }
        methodCall.setMethodName(methodName);
        methodCall.setParams(p);
        try {
            MethodResponse resp = this.xmlRpcClient.execute(methodCall);
            return this.methodResponseArrayToList((Array)resp.getParams().getParam().get(0).getValue().getValue());
        }
        catch (Exception ex) {
            throw new FabricCommunicationException("Error during call to `" + methodName + "' (args=" + Arrays.toString(args) + ")", ex);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.commons.net.SocketClient;

public class FingerClient
extends SocketClient {
    public static final int DEFAULT_PORT = 79;
    private static final String __LONG_FLAG = "/W ";
    private transient StringBuffer __query = new StringBuffer(64);
    private transient char[] __buffer = new char[1024];

    public FingerClient() {
        this.setDefaultPort(79);
    }

    public String query(boolean longOutput, String username) throws IOException {
        int read;
        StringBuffer result = new StringBuffer(this.__buffer.length);
        BufferedReader input = new BufferedReader(new InputStreamReader(this.getInputStream(longOutput, username)));
        while ((read = input.read(this.__buffer, 0, this.__buffer.length)) > 0) {
            result.append(this.__buffer, 0, read);
        }
        input.close();
        return result.toString();
    }

    public String query(boolean longOutput) throws IOException {
        return this.query(longOutput, "");
    }

    public InputStream getInputStream(boolean longOutput, String username) throws IOException {
        this.__query.setLength(0);
        if (longOutput) {
            this.__query.append(__LONG_FLAG);
        }
        this.__query.append(username);
        this.__query.append("\r\n");
        DataOutputStream output = new DataOutputStream(new BufferedOutputStream(this._output_, 1024));
        output.writeBytes(this.__query.toString());
        output.flush();
        return this._input_;
    }

    public InputStream getInputStream(boolean longOutput) throws IOException {
        return this.getInputStream(longOutput, "");
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net;

import java.io.InputStream;
import org.apache.commons.net.DiscardTCPClient;

public final class EchoTCPClient
extends DiscardTCPClient {
    public static final int DEFAULT_PORT = 7;

    public EchoTCPClient() {
        this.setDefaultPort(7);
    }

    public InputStream getInputStream() {
        return this._input_;
    }
}


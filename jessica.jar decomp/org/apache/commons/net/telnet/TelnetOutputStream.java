/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.telnet;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetOption;

final class TelnetOutputStream
extends OutputStream {
    private TelnetClient __client;
    private boolean __convertCRtoCRLF = true;
    private boolean __lastWasCR = false;

    TelnetOutputStream(TelnetClient client) {
        this.__client = client;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void write(int ch) throws IOException {
        TelnetClient telnetClient = this.__client;
        synchronized (telnetClient) {
            ch &= 0xFF;
            if (this.__client._requestedWont(TelnetOption.BINARY)) {
                if (this.__lastWasCR) {
                    if (this.__convertCRtoCRLF) {
                        this.__client._sendByte(10);
                        if (ch == 10) {
                            this.__lastWasCR = false;
                            return;
                        }
                    } else if (ch != 10) {
                        this.__client._sendByte(0);
                    }
                }
                this.__lastWasCR = false;
                switch (ch) {
                    case 13: {
                        this.__client._sendByte(13);
                        this.__lastWasCR = true;
                        break;
                    }
                    case 255: {
                        this.__client._sendByte(255);
                        this.__client._sendByte(255);
                        break;
                    }
                    default: {
                        this.__client._sendByte(ch);
                        break;
                    }
                }
            } else if (ch == 255) {
                this.__client._sendByte(ch);
                this.__client._sendByte(255);
            } else {
                this.__client._sendByte(ch);
            }
        }
    }

    public void write(byte[] buffer) throws IOException {
        this.write(buffer, 0, buffer.length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void write(byte[] buffer, int offset, int length) throws IOException {
        TelnetClient telnetClient = this.__client;
        synchronized (telnetClient) {
            while (length-- > 0) {
                this.write(buffer[offset++]);
            }
        }
    }

    public void flush() throws IOException {
        this.__client._flushOutputStream();
    }

    public void close() throws IOException {
        this.__client._closeOutputStream();
    }
}


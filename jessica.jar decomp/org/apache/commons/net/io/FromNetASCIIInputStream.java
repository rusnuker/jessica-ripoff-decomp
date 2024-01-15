/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public final class FromNetASCIIInputStream
extends PushbackInputStream {
    static final boolean _noConversionRequired;
    static final String _lineSeparator;
    static final byte[] _lineSeparatorBytes;
    private int __length = 0;

    public static final boolean isConversionRequired() {
        return !_noConversionRequired;
    }

    public FromNetASCIIInputStream(InputStream input) {
        super(input, _lineSeparatorBytes.length + 1);
    }

    private int __read() throws IOException {
        int ch = super.read();
        if (ch == 13) {
            ch = super.read();
            if (ch == 10) {
                this.unread(_lineSeparatorBytes);
                ch = super.read();
                --this.__length;
            } else {
                if (ch != -1) {
                    this.unread(ch);
                }
                return 13;
            }
        }
        return ch;
    }

    public int read() throws IOException {
        if (_noConversionRequired) {
            return super.read();
        }
        return this.__read();
    }

    public int read(byte[] buffer) throws IOException {
        return this.read(buffer, 0, buffer.length);
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (length < 1) {
            return 0;
        }
        int ch = this.available();
        int n = this.__length = length > ch ? ch : length;
        if (this.__length < 1) {
            this.__length = 1;
        }
        if (_noConversionRequired) {
            return super.read(buffer, offset, this.__length);
        }
        ch = this.__read();
        if (ch == -1) {
            return -1;
        }
        int off = offset;
        do {
            buffer[offset++] = (byte)ch;
        } while (--this.__length > 0 && (ch = this.__read()) != -1);
        return offset - off;
    }

    public int available() throws IOException {
        return this.buf.length - this.pos + this.in.available();
    }

    static {
        _lineSeparator = System.getProperty("line.separator");
        _noConversionRequired = _lineSeparator.equals("\r\n");
        _lineSeparatorBytes = _lineSeparator.getBytes();
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.io;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

public final class DotTerminatedMessageReader
extends Reader {
    private static final String LS = System.getProperty("line.separator");
    private static final char[] LS_CHARS = LS.toCharArray();
    private boolean atBeginning = true;
    private boolean eof = false;
    private int pos;
    private char[] internalBuffer = new char[LS_CHARS.length + 3];
    private PushbackReader internalReader;

    public DotTerminatedMessageReader(Reader reader) {
        super(reader);
        this.pos = this.internalBuffer.length;
        this.internalReader = new PushbackReader(reader);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int read() throws IOException {
        Object object = this.lock;
        synchronized (object) {
            if (this.pos < this.internalBuffer.length) {
                return this.internalBuffer[this.pos++];
            }
            if (this.eof) {
                return -1;
            }
            int ch = this.internalReader.read();
            if (ch == -1) {
                this.eof = true;
                return -1;
            }
            if (this.atBeginning) {
                this.atBeginning = false;
                if (ch == 46) {
                    ch = this.internalReader.read();
                    if (ch != 46) {
                        this.eof = true;
                        this.internalReader.read();
                        return -1;
                    }
                    return 46;
                }
            }
            if (ch == 13) {
                ch = this.internalReader.read();
                if (ch == 10) {
                    ch = this.internalReader.read();
                    if (ch == 46) {
                        ch = this.internalReader.read();
                        if (ch != 46) {
                            this.internalReader.read();
                            this.eof = true;
                        } else {
                            this.internalBuffer[--this.pos] = (char)ch;
                        }
                    } else {
                        this.internalReader.unread(ch);
                    }
                    this.pos -= LS_CHARS.length;
                    System.arraycopy(LS_CHARS, 0, this.internalBuffer, this.pos, LS_CHARS.length);
                    ch = this.internalBuffer[this.pos++];
                } else {
                    this.internalBuffer[--this.pos] = (char)ch;
                    return 13;
                }
            }
            return ch;
        }
    }

    public int read(char[] buffer) throws IOException {
        return this.read(buffer, 0, buffer.length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int read(char[] buffer, int offset, int length) throws IOException {
        Object object = this.lock;
        synchronized (object) {
            if (length < 1) {
                return 0;
            }
            int ch = this.read();
            if (ch == -1) {
                return -1;
            }
            int off = offset;
            do {
                buffer[offset++] = (char)ch;
            } while (--length > 0 && (ch = this.read()) != -1);
            return offset - off;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean ready() throws IOException {
        Object object = this.lock;
        synchronized (object) {
            return this.pos < this.internalBuffer.length || this.internalReader.ready();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() throws IOException {
        Object object = this.lock;
        synchronized (object) {
            if (this.internalReader == null) {
                return;
            }
            if (!this.eof) {
                while (this.read() != -1) {
                }
            }
            this.eof = true;
            this.atBeginning = false;
            this.pos = this.internalBuffer.length;
            this.internalReader = null;
        }
    }
}


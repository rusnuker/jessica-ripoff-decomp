/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Packet;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

public class IO {
    InputStream in;
    OutputStream out;
    OutputStream out_ext;
    private boolean in_dontclose = false;
    private boolean out_dontclose = false;
    private boolean out_ext_dontclose = false;

    void setOutputStream(OutputStream out) {
        this.out = out;
    }

    void setOutputStream(OutputStream out, boolean dontclose) {
        this.out_dontclose = dontclose;
        this.setOutputStream(out);
    }

    void setExtOutputStream(OutputStream out) {
        this.out_ext = out;
    }

    void setExtOutputStream(OutputStream out, boolean dontclose) {
        this.out_ext_dontclose = dontclose;
        this.setExtOutputStream(out);
    }

    void setInputStream(InputStream in) {
        this.in = in;
    }

    void setInputStream(InputStream in, boolean dontclose) {
        this.in_dontclose = dontclose;
        this.setInputStream(in);
    }

    public void put(Packet p) throws IOException, SocketException {
        this.out.write(p.buffer.buffer, 0, p.buffer.index);
        this.out.flush();
    }

    void put(byte[] array, int begin, int length) throws IOException {
        this.out.write(array, begin, length);
        this.out.flush();
    }

    void put_ext(byte[] array, int begin, int length) throws IOException {
        this.out_ext.write(array, begin, length);
        this.out_ext.flush();
    }

    int getByte() throws IOException {
        return this.in.read();
    }

    void getByte(byte[] array) throws IOException {
        this.getByte(array, 0, array.length);
    }

    void getByte(byte[] array, int begin, int length) throws IOException {
        int completed;
        do {
            if ((completed = this.in.read(array, begin, length)) < 0) {
                throw new IOException("End of IO Stream Read");
            }
            begin += completed;
        } while ((length -= completed) > 0);
    }

    void out_close() {
        try {
            if (this.out != null && !this.out_dontclose) {
                this.out.close();
            }
            this.out = null;
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void close() {
        try {
            if (this.in != null && !this.in_dontclose) {
                this.in.close();
            }
            this.in = null;
        }
        catch (Exception ee) {
            // empty catch block
        }
        this.out_close();
        try {
            if (this.out_ext != null && !this.out_ext_dontclose) {
                this.out_ext.close();
            }
            this.out_ext = null;
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

class NetworkResources {
    private final Socket mysqlConnection;
    private final InputStream mysqlInput;
    private final OutputStream mysqlOutput;

    protected NetworkResources(Socket mysqlConnection, InputStream mysqlInput, OutputStream mysqlOutput) {
        this.mysqlConnection = mysqlConnection;
        this.mysqlInput = mysqlInput;
        this.mysqlOutput = mysqlOutput;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected final void forceClose() {
        block19: {
            block18: {
                try {
                    try {
                        if (this.mysqlInput != null) {
                            this.mysqlInput.close();
                        }
                        Object var2_1 = null;
                        if (this.mysqlConnection == null || this.mysqlConnection.isClosed() || this.mysqlConnection.isInputShutdown()) break block18;
                    }
                    catch (Throwable throwable) {
                        Object var2_2 = null;
                        if (this.mysqlConnection == null) throw throwable;
                        if (this.mysqlConnection.isClosed()) throw throwable;
                        if (this.mysqlConnection.isInputShutdown()) throw throwable;
                        try {
                            this.mysqlConnection.shutdownInput();
                            throw throwable;
                        }
                        catch (UnsupportedOperationException unsupportedOperationException) {
                            // empty catch block
                        }
                        throw throwable;
                    }
                    try {}
                    catch (UnsupportedOperationException unsupportedOperationException) {}
                    this.mysqlConnection.shutdownInput();
                }
                catch (IOException ioEx) {
                    // empty catch block
                }
            }
            try {
                try {
                    if (this.mysqlOutput != null) {
                        this.mysqlOutput.close();
                    }
                    Object var5_9 = null;
                    if (this.mysqlConnection == null || this.mysqlConnection.isClosed() || this.mysqlConnection.isOutputShutdown()) break block19;
                }
                catch (Throwable throwable) {
                    Object var5_10 = null;
                    if (this.mysqlConnection == null) throw throwable;
                    if (this.mysqlConnection.isClosed()) throw throwable;
                    if (this.mysqlConnection.isOutputShutdown()) throw throwable;
                    try {
                        this.mysqlConnection.shutdownOutput();
                        throw throwable;
                    }
                    catch (UnsupportedOperationException ex) {
                        // empty catch block
                    }
                    throw throwable;
                }
                try {}
                catch (UnsupportedOperationException ex) {}
                this.mysqlConnection.shutdownOutput();
            }
            catch (IOException ioEx) {
                // empty catch block
            }
        }
        try {
            if (this.mysqlConnection == null) return;
            this.mysqlConnection.close();
            return;
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}


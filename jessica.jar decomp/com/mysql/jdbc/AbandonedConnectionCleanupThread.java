/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.NonRegisteringDriver;
import java.lang.ref.Reference;

public class AbandonedConnectionCleanupThread
extends Thread {
    private static boolean running = true;
    private static Thread threadRef = null;

    public AbandonedConnectionCleanupThread() {
        super("Abandoned connection cleanup thread");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void run() {
        threadRef = this;
        while (running) {
            try {
                Object var3_3;
                Reference<ConnectionImpl> ref = NonRegisteringDriver.refQueue.remove(100L);
                if (ref == null) continue;
                try {
                    ((NonRegisteringDriver.ConnectionPhantomReference)ref).cleanup();
                    var3_3 = null;
                    NonRegisteringDriver.connectionPhantomRefs.remove(ref);
                }
                catch (Throwable throwable) {
                    var3_3 = null;
                    NonRegisteringDriver.connectionPhantomRefs.remove(ref);
                    throw throwable;
                }
                {
                }
            }
            catch (Exception exception) {
            }
        }
    }

    public static void shutdown() throws InterruptedException {
        running = false;
        if (threadRef != null) {
            threadRef.interrupt();
            threadRef.join();
            threadRef = null;
        }
    }
}


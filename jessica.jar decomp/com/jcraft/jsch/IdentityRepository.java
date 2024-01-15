/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Identity;
import com.jcraft.jsch.IdentityFile;
import com.jcraft.jsch.JSchException;
import java.util.Vector;

public interface IdentityRepository {
    public static final int UNAVAILABLE = 0;
    public static final int NOTRUNNING = 1;
    public static final int RUNNING = 2;

    public String getName();

    public int getStatus();

    public Vector getIdentities();

    public boolean add(byte[] var1);

    public boolean remove(byte[] var1);

    public void removeAll();

    public static class Wrapper
    implements IdentityRepository {
        private IdentityRepository ir;
        private Vector cache = new Vector();
        private boolean keep_in_cache = false;

        Wrapper(IdentityRepository ir) {
            this(ir, false);
        }

        Wrapper(IdentityRepository ir, boolean keep_in_cache) {
            this.ir = ir;
            this.keep_in_cache = keep_in_cache;
        }

        public String getName() {
            return this.ir.getName();
        }

        public int getStatus() {
            return this.ir.getStatus();
        }

        public boolean add(byte[] identity) {
            return this.ir.add(identity);
        }

        public boolean remove(byte[] blob) {
            return this.ir.remove(blob);
        }

        public void removeAll() {
            this.cache.removeAllElements();
            this.ir.removeAll();
        }

        public Vector getIdentities() {
            Vector<Identity> result = new Vector<Identity>();
            for (int i = 0; i < this.cache.size(); ++i) {
                Identity identity = (Identity)this.cache.elementAt(i);
                result.add(identity);
            }
            Vector tmp = this.ir.getIdentities();
            for (int i = 0; i < tmp.size(); ++i) {
                result.add((Identity)tmp.elementAt(i));
            }
            return result;
        }

        void add(Identity identity) {
            if (!this.keep_in_cache && !identity.isEncrypted() && identity instanceof IdentityFile) {
                try {
                    this.ir.add(((IdentityFile)identity).getKeyPair().forSSHAgent());
                }
                catch (JSchException jSchException) {}
            } else {
                this.cache.addElement(identity);
            }
        }

        void check() {
            if (this.cache.size() > 0) {
                Object[] identities = this.cache.toArray();
                for (int i = 0; i < identities.length; ++i) {
                    Identity identity = (Identity)identities[i];
                    this.cache.removeElement(identity);
                    this.add(identity);
                }
            }
        }
    }
}


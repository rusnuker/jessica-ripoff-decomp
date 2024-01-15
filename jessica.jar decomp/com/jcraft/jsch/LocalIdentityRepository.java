/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Identity;
import com.jcraft.jsch.IdentityFile;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Util;
import java.util.Vector;

class LocalIdentityRepository
implements IdentityRepository {
    private static final String name = "Local Identity Repository";
    private Vector identities = new Vector();
    private JSch jsch;

    LocalIdentityRepository(JSch jsch) {
        this.jsch = jsch;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return 2;
    }

    public synchronized Vector getIdentities() {
        this.removeDupulicates();
        Vector v = new Vector();
        for (int i = 0; i < this.identities.size(); ++i) {
            v.addElement(this.identities.elementAt(i));
        }
        return v;
    }

    public synchronized void add(Identity identity) {
        if (!this.identities.contains(identity)) {
            byte[] blob1 = identity.getPublicKeyBlob();
            if (blob1 == null) {
                this.identities.addElement(identity);
                return;
            }
            for (int i = 0; i < this.identities.size(); ++i) {
                byte[] blob2 = ((Identity)this.identities.elementAt(i)).getPublicKeyBlob();
                if (blob2 == null || !Util.array_equals(blob1, blob2)) continue;
                if (!identity.isEncrypted() && ((Identity)this.identities.elementAt(i)).isEncrypted()) {
                    this.remove(blob2);
                    continue;
                }
                return;
            }
            this.identities.addElement(identity);
        }
    }

    public synchronized boolean add(byte[] identity) {
        try {
            IdentityFile _identity = IdentityFile.newInstance("from remote:", identity, null, this.jsch);
            this.add(_identity);
            return true;
        }
        catch (JSchException e) {
            return false;
        }
    }

    synchronized void remove(Identity identity) {
        if (this.identities.contains(identity)) {
            this.identities.removeElement(identity);
            identity.clear();
        } else {
            this.remove(identity.getPublicKeyBlob());
        }
    }

    public synchronized boolean remove(byte[] blob) {
        if (blob == null) {
            return false;
        }
        for (int i = 0; i < this.identities.size(); ++i) {
            Identity _identity = (Identity)this.identities.elementAt(i);
            byte[] _blob = _identity.getPublicKeyBlob();
            if (_blob == null || !Util.array_equals(blob, _blob)) continue;
            this.identities.removeElement(_identity);
            _identity.clear();
            return true;
        }
        return false;
    }

    public synchronized void removeAll() {
        for (int i = 0; i < this.identities.size(); ++i) {
            Identity identity = (Identity)this.identities.elementAt(i);
            identity.clear();
        }
        this.identities.removeAllElements();
    }

    private void removeDupulicates() {
        int i;
        Vector<byte[]> v = new Vector<byte[]>();
        int len = this.identities.size();
        if (len == 0) {
            return;
        }
        block0: for (i = 0; i < len; ++i) {
            Identity foo = (Identity)this.identities.elementAt(i);
            byte[] foo_blob = foo.getPublicKeyBlob();
            if (foo_blob == null) continue;
            for (int j = i + 1; j < len; ++j) {
                Identity bar = (Identity)this.identities.elementAt(j);
                byte[] bar_blob = bar.getPublicKeyBlob();
                if (bar_blob == null || !Util.array_equals(foo_blob, bar_blob) || foo.isEncrypted() != bar.isEncrypted()) continue;
                v.addElement(foo_blob);
                continue block0;
            }
        }
        for (i = 0; i < v.size(); ++i) {
            this.remove((byte[])v.elementAt(i));
        }
    }
}


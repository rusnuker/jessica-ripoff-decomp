/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.UserInfo;

public interface HostKeyRepository {
    public static final int OK = 0;
    public static final int NOT_INCLUDED = 1;
    public static final int CHANGED = 2;

    public int check(String var1, byte[] var2);

    public void add(HostKey var1, UserInfo var2);

    public void remove(String var1, String var2);

    public void remove(String var1, String var2, byte[] var3);

    public String getKnownHostsRepositoryID();

    public HostKey[] getHostKey();

    public HostKey[] getHostKey(String var1, String var2);
}


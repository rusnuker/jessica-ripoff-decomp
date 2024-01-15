/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

public interface UserInfo {
    public String getPassphrase();

    public String getPassword();

    public boolean promptPassword(String var1);

    public boolean promptPassphrase(String var1);

    public boolean promptYesNo(String var1);

    public void showMessage(String var1);
}


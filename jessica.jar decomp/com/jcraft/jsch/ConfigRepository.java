/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

public interface ConfigRepository {
    public static final Config defaultConfig = new Config(){

        public String getHostname() {
            return null;
        }

        public String getUser() {
            return null;
        }

        public int getPort() {
            return -1;
        }

        public String getValue(String key) {
            return null;
        }

        public String[] getValues(String key) {
            return null;
        }
    };
    public static final ConfigRepository nullConfig = new ConfigRepository(){

        public Config getConfig(String host) {
            return defaultConfig;
        }
    };

    public Config getConfig(String var1);

    public static interface Config {
        public String getHostname();

        public String getUser();

        public int getPort();

        public String getValue(String var1);

        public String[] getValues(String var1);
    }
}


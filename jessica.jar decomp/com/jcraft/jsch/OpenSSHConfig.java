/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.Util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Vector;

public class OpenSSHConfig
implements ConfigRepository {
    private final Hashtable config = new Hashtable();
    private final Vector hosts = new Vector();
    private static final Hashtable keymap = new Hashtable();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static OpenSSHConfig parse(String conf) throws IOException {
        StringReader r = new StringReader(conf);
        try {
            OpenSSHConfig openSSHConfig = new OpenSSHConfig(r);
            return openSSHConfig;
        }
        finally {
            ((Reader)r).close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static OpenSSHConfig parseFile(String file) throws IOException {
        FileReader r = new FileReader(Util.checkTilde(file));
        try {
            OpenSSHConfig openSSHConfig = new OpenSSHConfig(r);
            return openSSHConfig;
        }
        finally {
            ((Reader)r).close();
        }
    }

    OpenSSHConfig(Reader r) throws IOException {
        this._parse(r);
    }

    private void _parse(Reader r) throws IOException {
        BufferedReader br = new BufferedReader(r);
        String host = "";
        Vector<String[]> kv = new Vector<String[]>();
        String l = null;
        while ((l = br.readLine()) != null) {
            if ((l = l.trim()).length() == 0 || l.startsWith("#")) continue;
            String[] key_value = l.split("[= \t]", 2);
            for (int i = 0; i < key_value.length; ++i) {
                key_value[i] = key_value[i].trim();
            }
            if (key_value.length <= 1) continue;
            if (key_value[0].equals("Host")) {
                this.config.put(host, kv);
                this.hosts.addElement(host);
                host = key_value[1];
                kv = new Vector();
                continue;
            }
            kv.addElement(key_value);
        }
        this.config.put(host, kv);
        this.hosts.addElement(host);
    }

    public ConfigRepository.Config getConfig(String host) {
        return new MyConfig(host);
    }

    static {
        keymap.put("kex", "KexAlgorithms");
        keymap.put("server_host_key", "HostKeyAlgorithms");
        keymap.put("cipher.c2s", "Ciphers");
        keymap.put("cipher.s2c", "Ciphers");
        keymap.put("mac.c2s", "Macs");
        keymap.put("mac.s2c", "Macs");
        keymap.put("compression.s2c", "Compression");
        keymap.put("compression.c2s", "Compression");
        keymap.put("compression_level", "CompressionLevel");
        keymap.put("MaxAuthTries", "NumberOfPasswordPrompts");
    }

    class MyConfig
    implements ConfigRepository.Config {
        private String host;
        private Vector _configs = new Vector();

        MyConfig(String host) {
            this.host = host;
            this._configs.addElement(OpenSSHConfig.this.config.get(""));
            byte[] _host = Util.str2byte(host);
            if (OpenSSHConfig.this.hosts.size() > 1) {
                for (int i = 1; i < OpenSSHConfig.this.hosts.size(); ++i) {
                    String[] patterns = ((String)OpenSSHConfig.this.hosts.elementAt(i)).split("[ \t]");
                    for (int j = 0; j < patterns.length; ++j) {
                        boolean negate = false;
                        String foo = patterns[j].trim();
                        if (foo.startsWith("!")) {
                            negate = true;
                            foo = foo.substring(1).trim();
                        }
                        if (Util.glob(Util.str2byte(foo), _host)) {
                            if (negate) continue;
                            this._configs.addElement(OpenSSHConfig.this.config.get((String)OpenSSHConfig.this.hosts.elementAt(i)));
                            continue;
                        }
                        if (!negate) continue;
                        this._configs.addElement(OpenSSHConfig.this.config.get((String)OpenSSHConfig.this.hosts.elementAt(i)));
                    }
                }
            }
        }

        private String find(String key) {
            if (keymap.get(key) != null) {
                key = (String)keymap.get(key);
            }
            key = key.toUpperCase();
            String value = null;
            for (int i = 0; i < this._configs.size(); ++i) {
                Vector v = (Vector)this._configs.elementAt(i);
                for (int j = 0; j < v.size(); ++j) {
                    String[] kv = (String[])v.elementAt(j);
                    if (!kv[0].toUpperCase().equals(key)) continue;
                    value = kv[1];
                    break;
                }
                if (value != null) break;
            }
            return value;
        }

        private String[] multiFind(String key) {
            key = key.toUpperCase();
            Vector<String> value = new Vector<String>();
            for (int i = 0; i < this._configs.size(); ++i) {
                Vector v = (Vector)this._configs.elementAt(i);
                for (int j = 0; j < v.size(); ++j) {
                    String foo;
                    String[] kv = (String[])v.elementAt(j);
                    if (!kv[0].toUpperCase().equals(key) || (foo = kv[1]) == null) continue;
                    value.remove(foo);
                    value.addElement(foo);
                }
            }
            String[] result = new String[value.size()];
            value.toArray(result);
            return result;
        }

        public String getHostname() {
            return this.find("Hostname");
        }

        public String getUser() {
            return this.find("User");
        }

        public int getPort() {
            String foo = this.find("Port");
            int port = -1;
            try {
                port = Integer.parseInt(foo);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
            return port;
        }

        public String getValue(String key) {
            if (key.equals("compression.s2c") || key.equals("compression.c2s")) {
                String foo = this.find(key);
                if (foo == null || foo.equals("no")) {
                    return "none,zlib@openssh.com,zlib";
                }
                return "zlib@openssh.com,zlib,none";
            }
            return this.find(key);
        }

        public String[] getValues(String key) {
            return this.multiFind(key);
        }
    }
}


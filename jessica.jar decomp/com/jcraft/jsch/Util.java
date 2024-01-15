/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.HASH;
import com.jcraft.jsch.JSchException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Vector;

class Util {
    private static final byte[] b64 = Util.str2byte("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=");
    private static String[] chars = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
    static final byte[] empty = Util.str2byte("");

    Util() {
    }

    private static byte val(byte foo) {
        if (foo == 61) {
            return 0;
        }
        for (int j = 0; j < b64.length; ++j) {
            if (foo != b64[j]) continue;
            return (byte)j;
        }
        return 0;
    }

    static byte[] fromBase64(byte[] buf, int start, int length) throws JSchException {
        try {
            byte[] foo = new byte[length];
            int j = 0;
            for (int i = start; i < start + length; i += 4) {
                foo[j] = (byte)(Util.val(buf[i]) << 2 | (Util.val(buf[i + 1]) & 0x30) >>> 4);
                if (buf[i + 2] == 61) {
                    ++j;
                    break;
                }
                foo[j + 1] = (byte)((Util.val(buf[i + 1]) & 0xF) << 4 | (Util.val(buf[i + 2]) & 0x3C) >>> 2);
                if (buf[i + 3] == 61) {
                    j += 2;
                    break;
                }
                foo[j + 2] = (byte)((Util.val(buf[i + 2]) & 3) << 6 | Util.val(buf[i + 3]) & 0x3F);
                j += 3;
            }
            byte[] bar = new byte[j];
            System.arraycopy(foo, 0, bar, 0, j);
            return bar;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new JSchException("fromBase64: invalid base64 data", e);
        }
    }

    static byte[] toBase64(byte[] buf, int start, int length) {
        int k;
        int j;
        byte[] tmp = new byte[length * 2];
        int foo = length / 3 * 3 + start;
        int i = 0;
        for (j = start; j < foo; j += 3) {
            k = buf[j] >>> 2 & 0x3F;
            tmp[i++] = b64[k];
            k = (buf[j] & 3) << 4 | buf[j + 1] >>> 4 & 0xF;
            tmp[i++] = b64[k];
            k = (buf[j + 1] & 0xF) << 2 | buf[j + 2] >>> 6 & 3;
            tmp[i++] = b64[k];
            k = buf[j + 2] & 0x3F;
            tmp[i++] = b64[k];
        }
        if ((foo = start + length - foo) == 1) {
            k = buf[j] >>> 2 & 0x3F;
            tmp[i++] = b64[k];
            k = (buf[j] & 3) << 4 & 0x3F;
            tmp[i++] = b64[k];
            tmp[i++] = 61;
            tmp[i++] = 61;
        } else if (foo == 2) {
            k = buf[j] >>> 2 & 0x3F;
            tmp[i++] = b64[k];
            k = (buf[j] & 3) << 4 | buf[j + 1] >>> 4 & 0xF;
            tmp[i++] = b64[k];
            k = (buf[j + 1] & 0xF) << 2 & 0x3F;
            tmp[i++] = b64[k];
            tmp[i++] = 61;
        }
        byte[] bar = new byte[i];
        System.arraycopy(tmp, 0, bar, 0, i);
        return bar;
    }

    static String[] split(String foo, String split) {
        int index;
        if (foo == null) {
            return null;
        }
        byte[] buf = Util.str2byte(foo);
        Vector<String> bar = new Vector<String>();
        int start = 0;
        while ((index = foo.indexOf(split, start)) >= 0) {
            bar.addElement(Util.byte2str(buf, start, index - start));
            start = index + 1;
        }
        bar.addElement(Util.byte2str(buf, start, buf.length - start));
        String[] result = new String[bar.size()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = (String)bar.elementAt(i);
        }
        return result;
    }

    static boolean glob(byte[] pattern, byte[] name) {
        return Util.glob0(pattern, 0, name, 0);
    }

    private static boolean glob0(byte[] pattern, int pattern_index, byte[] name, int name_index) {
        if (name.length > 0 && name[0] == 46) {
            if (pattern.length > 0 && pattern[0] == 46) {
                if (pattern.length == 2 && pattern[1] == 42) {
                    return true;
                }
                return Util.glob(pattern, pattern_index + 1, name, name_index + 1);
            }
            return false;
        }
        return Util.glob(pattern, pattern_index, name, name_index);
    }

    private static boolean glob(byte[] pattern, int pattern_index, byte[] name, int name_index) {
        int patternlen = pattern.length;
        if (patternlen == 0) {
            return false;
        }
        int namelen = name.length;
        int i = pattern_index;
        int j = name_index;
        while (i < patternlen && j < namelen) {
            if (pattern[i] == 92) {
                if (i + 1 == patternlen) {
                    return false;
                }
                if (pattern[++i] != name[j]) {
                    return false;
                }
                i += Util.skipUTF8Char(pattern[i]);
                j += Util.skipUTF8Char(name[j]);
                continue;
            }
            if (pattern[i] == 42) {
                while (i < patternlen && pattern[i] == 42) {
                    ++i;
                }
                if (patternlen == i) {
                    return true;
                }
                byte foo = pattern[i];
                if (foo == 63) {
                    while (j < namelen) {
                        if (Util.glob(pattern, i, name, j)) {
                            return true;
                        }
                        j += Util.skipUTF8Char(name[j]);
                    }
                    return false;
                }
                if (foo == 92) {
                    if (i + 1 == patternlen) {
                        return false;
                    }
                    foo = pattern[++i];
                    while (j < namelen) {
                        if (foo == name[j] && Util.glob(pattern, i + Util.skipUTF8Char(foo), name, j + Util.skipUTF8Char(name[j]))) {
                            return true;
                        }
                        j += Util.skipUTF8Char(name[j]);
                    }
                    return false;
                }
                while (j < namelen) {
                    if (foo == name[j] && Util.glob(pattern, i, name, j)) {
                        return true;
                    }
                    j += Util.skipUTF8Char(name[j]);
                }
                return false;
            }
            if (pattern[i] == 63) {
                ++i;
                j += Util.skipUTF8Char(name[j]);
                continue;
            }
            if (pattern[i] != name[j]) {
                return false;
            }
            i += Util.skipUTF8Char(pattern[i]);
            if ((j += Util.skipUTF8Char(name[j])) < namelen) continue;
            if (i >= patternlen) {
                return true;
            }
            if (pattern[i] != 42) continue;
        }
        if (i == patternlen && j == namelen) {
            return true;
        }
        if (j >= namelen && pattern[i] == 42) {
            boolean ok = true;
            while (i < patternlen) {
                if (pattern[i++] == 42) continue;
                ok = false;
                break;
            }
            return ok;
        }
        return false;
    }

    static String quote(String path) {
        byte[] _path = Util.str2byte(path);
        int count = 0;
        for (int i = 0; i < _path.length; ++i) {
            byte b = _path[i];
            if (b != 92 && b != 63 && b != 42) continue;
            ++count;
        }
        if (count == 0) {
            return path;
        }
        byte[] _path2 = new byte[_path.length + count];
        int j = 0;
        for (int i = 0; i < _path.length; ++i) {
            byte b = _path[i];
            if (b == 92 || b == 63 || b == 42) {
                _path2[j++] = 92;
            }
            _path2[j++] = b;
        }
        return Util.byte2str(_path2);
    }

    static String unquote(String path) {
        byte[] bar;
        byte[] foo = Util.str2byte(path);
        if (foo.length == (bar = Util.unquote(foo)).length) {
            return path;
        }
        return Util.byte2str(bar);
    }

    static byte[] unquote(byte[] path) {
        int pathlen = path.length;
        int i = 0;
        while (i < pathlen) {
            if (path[i] == 92) {
                if (i + 1 == pathlen) break;
                System.arraycopy(path, i + 1, path, i, path.length - (i + 1));
                --pathlen;
                ++i;
                continue;
            }
            ++i;
        }
        if (pathlen == path.length) {
            return path;
        }
        byte[] foo = new byte[pathlen];
        System.arraycopy(path, 0, foo, 0, pathlen);
        return foo;
    }

    static String getFingerPrint(HASH hash, byte[] data) {
        try {
            hash.init();
            hash.update(data, 0, data.length);
            byte[] foo = hash.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < foo.length; ++i) {
                int bar = foo[i] & 0xFF;
                sb.append(chars[bar >>> 4 & 0xF]);
                sb.append(chars[bar & 0xF]);
                if (i + 1 >= foo.length) continue;
                sb.append(":");
            }
            return sb.toString();
        }
        catch (Exception e) {
            return "???";
        }
    }

    static boolean array_equals(byte[] foo, byte[] bar) {
        int i = foo.length;
        if (i != bar.length) {
            return false;
        }
        for (int j = 0; j < i; ++j) {
            if (foo[j] == bar[j]) continue;
            return false;
        }
        return true;
    }

    static Socket createSocket(String host, int port, int timeout) throws JSchException {
        Socket socket = null;
        if (timeout == 0) {
            try {
                socket = new Socket(host, port);
                return socket;
            }
            catch (Exception e) {
                String message = e.toString();
                if (e instanceof Throwable) {
                    throw new JSchException(message, e);
                }
                throw new JSchException(message);
            }
        }
        final String _host = host;
        final int _port = port;
        final Socket[] sockp = new Socket[1];
        final Exception[] ee = new Exception[1];
        String message = "";
        Thread tmp = new Thread(new Runnable(){

            public void run() {
                sockp[0] = null;
                try {
                    sockp[0] = new Socket(_host, _port);
                }
                catch (Exception e) {
                    ee[0] = e;
                    if (sockp[0] != null && sockp[0].isConnected()) {
                        try {
                            sockp[0].close();
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    sockp[0] = null;
                }
            }
        });
        tmp.setName("Opening Socket " + host);
        tmp.start();
        try {
            tmp.join(timeout);
            message = "timeout: ";
        }
        catch (InterruptedException eee) {
            // empty catch block
        }
        if (sockp[0] == null || !sockp[0].isConnected()) {
            message = message + "socket is not established";
            if (ee[0] != null) {
                message = ee[0].toString();
            }
            tmp.interrupt();
            tmp = null;
            throw new JSchException(message, ee[0]);
        }
        socket = sockp[0];
        return socket;
    }

    static byte[] str2byte(String str, String encoding) {
        if (str == null) {
            return null;
        }
        try {
            return str.getBytes(encoding);
        }
        catch (UnsupportedEncodingException e) {
            return str.getBytes();
        }
    }

    static byte[] str2byte(String str) {
        return Util.str2byte(str, "UTF-8");
    }

    static String byte2str(byte[] str, String encoding) {
        return Util.byte2str(str, 0, str.length, encoding);
    }

    static String byte2str(byte[] str, int s, int l, String encoding) {
        try {
            return new String(str, s, l, encoding);
        }
        catch (UnsupportedEncodingException e) {
            return new String(str, s, l);
        }
    }

    static String byte2str(byte[] str) {
        return Util.byte2str(str, 0, str.length, "UTF-8");
    }

    static String byte2str(byte[] str, int s, int l) {
        return Util.byte2str(str, s, l, "UTF-8");
    }

    static String toHex(byte[] str) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length; ++i) {
            String foo = Integer.toHexString(str[i] & 0xFF);
            sb.append("0x" + (foo.length() == 1 ? "0" : "") + foo);
            if (i + 1 >= str.length) continue;
            sb.append(":");
        }
        return sb.toString();
    }

    static void bzero(byte[] foo) {
        if (foo == null) {
            return;
        }
        for (int i = 0; i < foo.length; ++i) {
            foo[i] = 0;
        }
    }

    static String diffString(String str, String[] not_available) {
        String[] stra = Util.split(str, ",");
        String result = null;
        block0: for (int i = 0; i < stra.length; ++i) {
            for (int j = 0; j < not_available.length; ++j) {
                if (stra[i].equals(not_available[j])) continue block0;
            }
            result = result == null ? stra[i] : result + "," + stra[i];
        }
        return result;
    }

    static String checkTilde(String str) {
        try {
            if (str.startsWith("~")) {
                str = str.replace("~", System.getProperty("user.home"));
            }
        }
        catch (SecurityException securityException) {
            // empty catch block
        }
        return str;
    }

    private static int skipUTF8Char(byte b) {
        if ((byte)(b & 0x80) == 0) {
            return 1;
        }
        if ((byte)(b & 0xE0) == -64) {
            return 2;
        }
        if ((byte)(b & 0xF0) == -32) {
            return 3;
        }
        return 1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static byte[] fromFile(String _file) throws IOException {
        _file = Util.checkTilde(_file);
        File file = new File(_file);
        FileInputStream fis = new FileInputStream(_file);
        try {
            int i;
            byte[] result = new byte[(int)file.length()];
            int len = 0;
            while ((i = fis.read(result, len, result.length - len)) > 0) {
                len += i;
            }
            fis.close();
            byte[] byArray = result;
            return byArray;
        }
        finally {
            if (fis != null) {
                fis.close();
            }
        }
    }
}


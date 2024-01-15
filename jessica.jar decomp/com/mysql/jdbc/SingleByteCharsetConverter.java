/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.CharsetMapping;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.SQLError;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SingleByteCharsetConverter {
    private static final int BYTE_RANGE = 256;
    private static byte[] allBytes;
    private static final Map<String, SingleByteCharsetConverter> CONVERTER_MAP;
    private static final byte[] EMPTY_BYTE_ARRAY;
    private static byte[] unknownCharsMap;
    private char[] byteToChars = new char[256];
    private byte[] charToByteMap = new byte[65536];

    public static synchronized SingleByteCharsetConverter getInstance(String encodingName, Connection conn) throws UnsupportedEncodingException, SQLException {
        SingleByteCharsetConverter instance = CONVERTER_MAP.get(encodingName);
        if (instance == null) {
            instance = SingleByteCharsetConverter.initCharset(encodingName);
        }
        return instance;
    }

    public static SingleByteCharsetConverter initCharset(String javaEncodingName) throws UnsupportedEncodingException, SQLException {
        try {
            if (CharsetMapping.isMultibyteCharset(javaEncodingName)) {
                return null;
            }
        }
        catch (RuntimeException ex) {
            SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
            sqlEx.initCause(ex);
            throw sqlEx;
        }
        SingleByteCharsetConverter converter = new SingleByteCharsetConverter(javaEncodingName);
        CONVERTER_MAP.put(javaEncodingName, converter);
        return converter;
    }

    public static String toStringDefaultEncoding(byte[] buffer, int startPos, int length) {
        return new String(buffer, startPos, length);
    }

    private SingleByteCharsetConverter(String encodingName) throws UnsupportedEncodingException {
        String allBytesString = new String(allBytes, 0, 256, encodingName);
        int allBytesLen = allBytesString.length();
        System.arraycopy(unknownCharsMap, 0, this.charToByteMap, 0, this.charToByteMap.length);
        for (int i = 0; i < 256 && i < allBytesLen; ++i) {
            char c;
            this.byteToChars[i] = c = allBytesString.charAt(i);
            this.charToByteMap[c] = allBytes[i];
        }
    }

    public final byte[] toBytes(char[] c) {
        if (c == null) {
            return null;
        }
        int length = c.length;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; ++i) {
            bytes[i] = this.charToByteMap[c[i]];
        }
        return bytes;
    }

    public final byte[] toBytesWrapped(char[] c, char beginWrap, char endWrap) {
        if (c == null) {
            return null;
        }
        int length = c.length + 2;
        int charLength = c.length;
        byte[] bytes = new byte[length];
        bytes[0] = this.charToByteMap[beginWrap];
        for (int i = 0; i < charLength; ++i) {
            bytes[i + 1] = this.charToByteMap[c[i]];
        }
        bytes[length - 1] = this.charToByteMap[endWrap];
        return bytes;
    }

    public final byte[] toBytes(char[] chars, int offset, int length) {
        if (chars == null) {
            return null;
        }
        if (length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; ++i) {
            bytes[i] = this.charToByteMap[chars[i + offset]];
        }
        return bytes;
    }

    public final byte[] toBytes(String s) {
        if (s == null) {
            return null;
        }
        int length = s.length();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; ++i) {
            bytes[i] = this.charToByteMap[s.charAt(i)];
        }
        return bytes;
    }

    public final byte[] toBytesWrapped(String s, char beginWrap, char endWrap) {
        if (s == null) {
            return null;
        }
        int stringLength = s.length();
        int length = stringLength + 2;
        byte[] bytes = new byte[length];
        bytes[0] = this.charToByteMap[beginWrap];
        for (int i = 0; i < stringLength; ++i) {
            bytes[i + 1] = this.charToByteMap[s.charAt(i)];
        }
        bytes[length - 1] = this.charToByteMap[endWrap];
        return bytes;
    }

    public final byte[] toBytes(String s, int offset, int length) {
        if (s == null) {
            return null;
        }
        if (length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; ++i) {
            char c = s.charAt(i + offset);
            bytes[i] = this.charToByteMap[c];
        }
        return bytes;
    }

    public final String toString(byte[] buffer) {
        return this.toString(buffer, 0, buffer.length);
    }

    public final String toString(byte[] buffer, int startPos, int length) {
        char[] charArray = new char[length];
        int readpoint = startPos;
        for (int i = 0; i < length; ++i) {
            charArray[i] = this.byteToChars[buffer[readpoint] - -128];
            ++readpoint;
        }
        return new String(charArray);
    }

    static {
        int i;
        allBytes = new byte[256];
        CONVERTER_MAP = new HashMap<String, SingleByteCharsetConverter>();
        EMPTY_BYTE_ARRAY = new byte[0];
        unknownCharsMap = new byte[65536];
        for (i = -128; i <= 127; ++i) {
            SingleByteCharsetConverter.allBytes[i - -128] = (byte)i;
        }
        for (i = 0; i < unknownCharsMap.length; ++i) {
            SingleByteCharsetConverter.unknownCharsMap[i] = 63;
        }
    }
}


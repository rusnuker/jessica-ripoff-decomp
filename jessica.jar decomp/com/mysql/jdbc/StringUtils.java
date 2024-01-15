/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.CharsetMapping;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.SingleByteCharsetConverter;
import com.mysql.jdbc.Util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class StringUtils {
    public static final Set<SearchMode> SEARCH_MODE__ALL = Collections.unmodifiableSet(EnumSet.allOf(SearchMode.class));
    public static final Set<SearchMode> SEARCH_MODE__MRK_COM_WS = Collections.unmodifiableSet(EnumSet.of(SearchMode.SKIP_BETWEEN_MARKERS, SearchMode.SKIP_BLOCK_COMMENTS, SearchMode.SKIP_LINE_COMMENTS, SearchMode.SKIP_WHITE_SPACE));
    public static final Set<SearchMode> SEARCH_MODE__BSESC_COM_WS = Collections.unmodifiableSet(EnumSet.of(SearchMode.ALLOW_BACKSLASH_ESCAPE, SearchMode.SKIP_BLOCK_COMMENTS, SearchMode.SKIP_LINE_COMMENTS, SearchMode.SKIP_WHITE_SPACE));
    public static final Set<SearchMode> SEARCH_MODE__BSESC_MRK_WS = Collections.unmodifiableSet(EnumSet.of(SearchMode.ALLOW_BACKSLASH_ESCAPE, SearchMode.SKIP_BETWEEN_MARKERS, SearchMode.SKIP_WHITE_SPACE));
    public static final Set<SearchMode> SEARCH_MODE__COM_WS = Collections.unmodifiableSet(EnumSet.of(SearchMode.SKIP_BLOCK_COMMENTS, SearchMode.SKIP_LINE_COMMENTS, SearchMode.SKIP_WHITE_SPACE));
    public static final Set<SearchMode> SEARCH_MODE__MRK_WS = Collections.unmodifiableSet(EnumSet.of(SearchMode.SKIP_BETWEEN_MARKERS, SearchMode.SKIP_WHITE_SPACE));
    public static final Set<SearchMode> SEARCH_MODE__NONE = Collections.unmodifiableSet(EnumSet.noneOf(SearchMode.class));
    private static final int NON_COMMENTS_MYSQL_VERSION_REF_LENGTH = 5;
    private static final int BYTE_RANGE = 256;
    private static byte[] allBytes = new byte[256];
    private static char[] byteToChars = new char[256];
    private static Method toPlainStringMethod;
    static final int WILD_COMPARE_MATCH_NO_WILD = 0;
    static final int WILD_COMPARE_MATCH_WITH_WILD = 1;
    static final int WILD_COMPARE_NO_MATCH = -1;
    private static final ConcurrentHashMap<String, Charset> charsetsByAlias;
    private static final String platformEncoding;
    private static final String VALID_ID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789$_#@";
    private static final char[] HEX_DIGITS;

    static Charset findCharset(String alias) throws UnsupportedEncodingException {
        try {
            Charset oldCs;
            Charset cs = charsetsByAlias.get(alias);
            if (cs == null && (oldCs = charsetsByAlias.putIfAbsent(alias, cs = Charset.forName(alias))) != null) {
                cs = oldCs;
            }
            return cs;
        }
        catch (UnsupportedCharsetException uce) {
            throw new UnsupportedEncodingException(alias);
        }
        catch (IllegalCharsetNameException icne) {
            throw new UnsupportedEncodingException(alias);
        }
        catch (IllegalArgumentException iae) {
            throw new UnsupportedEncodingException(alias);
        }
    }

    public static String consistentToString(BigDecimal decimal) {
        if (decimal == null) {
            return null;
        }
        if (toPlainStringMethod != null) {
            try {
                return (String)toPlainStringMethod.invoke(decimal, null);
            }
            catch (InvocationTargetException invokeEx) {
            }
            catch (IllegalAccessException illegalAccessException) {
                // empty catch block
            }
        }
        return decimal.toString();
    }

    public static String dumpAsHex(byte[] byteBuffer, int length) {
        int i;
        StringBuilder outputBuilder = new StringBuilder(length * 4);
        int p = 0;
        int rows = length / 8;
        for (int i2 = 0; i2 < rows && p < length; ++i2) {
            int j;
            int ptemp = p;
            for (j = 0; j < 8; ++j) {
                String hexVal = Integer.toHexString(byteBuffer[ptemp] & 0xFF);
                if (hexVal.length() == 1) {
                    hexVal = "0" + hexVal;
                }
                outputBuilder.append(hexVal + " ");
                ++ptemp;
            }
            outputBuilder.append("    ");
            for (j = 0; j < 8; ++j) {
                int b = 0xFF & byteBuffer[p];
                if (b > 32 && b < 127) {
                    outputBuilder.append((char)b + " ");
                } else {
                    outputBuilder.append(". ");
                }
                ++p;
            }
            outputBuilder.append("\n");
        }
        int n = 0;
        for (i = p; i < length; ++i) {
            String hexVal = Integer.toHexString(byteBuffer[i] & 0xFF);
            if (hexVal.length() == 1) {
                hexVal = "0" + hexVal;
            }
            outputBuilder.append(hexVal + " ");
            ++n;
        }
        for (i = n; i < 8; ++i) {
            outputBuilder.append("   ");
        }
        outputBuilder.append("    ");
        for (i = p; i < length; ++i) {
            int b = 0xFF & byteBuffer[i];
            if (b > 32 && b < 127) {
                outputBuilder.append((char)b + " ");
                continue;
            }
            outputBuilder.append(". ");
        }
        outputBuilder.append("\n");
        return outputBuilder.toString();
    }

    private static boolean endsWith(byte[] dataFrom, String suffix) {
        for (int i = 1; i <= suffix.length(); ++i) {
            int dfOffset = dataFrom.length - i;
            int suffixOffset = suffix.length() - i;
            if (dataFrom[dfOffset] == suffix.charAt(suffixOffset)) continue;
            return false;
        }
        return true;
    }

    public static byte[] escapeEasternUnicodeByteStream(byte[] origBytes, String origString) {
        if (origBytes == null) {
            return null;
        }
        if (origBytes.length == 0) {
            return new byte[0];
        }
        int bytesLen = origBytes.length;
        int bufIndex = 0;
        int strIndex = 0;
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream(bytesLen);
        while (true) {
            if (origString.charAt(strIndex) == '\\') {
                bytesOut.write(origBytes[bufIndex++]);
            } else {
                int hiByte;
                int loByte = origBytes[bufIndex];
                if (loByte < 0) {
                    loByte += 256;
                }
                bytesOut.write(loByte);
                if (loByte >= 128) {
                    if (bufIndex < bytesLen - 1) {
                        hiByte = origBytes[bufIndex + 1];
                        if (hiByte < 0) {
                            hiByte += 256;
                        }
                        bytesOut.write(hiByte);
                        ++bufIndex;
                        if (hiByte == 92) {
                            bytesOut.write(hiByte);
                        }
                    }
                } else if (loByte == 92 && bufIndex < bytesLen - 1) {
                    hiByte = origBytes[bufIndex + 1];
                    if (hiByte < 0) {
                        hiByte += 256;
                    }
                    if (hiByte == 98) {
                        bytesOut.write(92);
                        bytesOut.write(98);
                        ++bufIndex;
                    }
                }
                ++bufIndex;
            }
            if (bufIndex >= bytesLen) break;
            ++strIndex;
        }
        return bytesOut.toByteArray();
    }

    public static char firstNonWsCharUc(String searchIn) {
        return StringUtils.firstNonWsCharUc(searchIn, 0);
    }

    public static char firstNonWsCharUc(String searchIn, int startAt) {
        if (searchIn == null) {
            return '\u0000';
        }
        int length = searchIn.length();
        for (int i = startAt; i < length; ++i) {
            char c = searchIn.charAt(i);
            if (Character.isWhitespace(c)) continue;
            return Character.toUpperCase(c);
        }
        return '\u0000';
    }

    public static char firstAlphaCharUc(String searchIn, int startAt) {
        if (searchIn == null) {
            return '\u0000';
        }
        int length = searchIn.length();
        for (int i = startAt; i < length; ++i) {
            char c = searchIn.charAt(i);
            if (!Character.isLetter(c)) continue;
            return Character.toUpperCase(c);
        }
        return '\u0000';
    }

    public static String fixDecimalExponent(String dString) {
        char maybeMinusChar;
        int ePos = dString.indexOf(69);
        if (ePos == -1) {
            ePos = dString.indexOf(101);
        }
        if (ePos != -1 && dString.length() > ePos + 1 && (maybeMinusChar = dString.charAt(ePos + 1)) != '-' && maybeMinusChar != '+') {
            StringBuilder strBuilder = new StringBuilder(dString.length() + 1);
            strBuilder.append(dString.substring(0, ePos + 1));
            strBuilder.append('+');
            strBuilder.append(dString.substring(ePos + 1, dString.length()));
            dString = strBuilder.toString();
        }
        return dString;
    }

    public static byte[] getBytes(char[] c, SingleByteCharsetConverter converter, String encoding, String serverEncoding, boolean parserKnowsUnicode, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            byte[] b;
            if (converter != null) {
                b = converter.toBytes(c);
            } else if (encoding == null) {
                b = StringUtils.getBytes(c);
            } else {
                b = StringUtils.getBytes(c, encoding);
                if (!parserKnowsUnicode && CharsetMapping.requiresEscapeEasternUnicode(encoding) && !encoding.equalsIgnoreCase(serverEncoding)) {
                    b = StringUtils.escapeEasternUnicodeByteStream(b, new String(c));
                }
            }
            return b;
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException(Messages.getString("StringUtils.0") + encoding + Messages.getString("StringUtils.1"), "S1009", exceptionInterceptor);
        }
    }

    public static byte[] getBytes(char[] c, SingleByteCharsetConverter converter, String encoding, String serverEncoding, int offset, int length, boolean parserKnowsUnicode, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            byte[] b;
            if (converter != null) {
                b = converter.toBytes(c, offset, length);
            } else if (encoding == null) {
                b = StringUtils.getBytes(c, offset, length);
            } else {
                b = StringUtils.getBytes(c, offset, length, encoding);
                if (!parserKnowsUnicode && CharsetMapping.requiresEscapeEasternUnicode(encoding) && !encoding.equalsIgnoreCase(serverEncoding)) {
                    b = StringUtils.escapeEasternUnicodeByteStream(b, new String(c, offset, length));
                }
            }
            return b;
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException(Messages.getString("StringUtils.0") + encoding + Messages.getString("StringUtils.1"), "S1009", exceptionInterceptor);
        }
    }

    public static byte[] getBytes(char[] c, String encoding, String serverEncoding, boolean parserKnowsUnicode, MySQLConnection conn, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            SingleByteCharsetConverter converter = conn != null ? conn.getCharsetConverter(encoding) : SingleByteCharsetConverter.getInstance(encoding, null);
            return StringUtils.getBytes(c, converter, encoding, serverEncoding, parserKnowsUnicode, exceptionInterceptor);
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException(Messages.getString("StringUtils.0") + encoding + Messages.getString("StringUtils.1"), "S1009", exceptionInterceptor);
        }
    }

    public static byte[] getBytes(String s, SingleByteCharsetConverter converter, String encoding, String serverEncoding, boolean parserKnowsUnicode, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            byte[] b;
            if (converter != null) {
                b = converter.toBytes(s);
            } else if (encoding == null) {
                b = StringUtils.getBytes(s);
            } else {
                b = StringUtils.getBytes(s, encoding);
                if (!parserKnowsUnicode && CharsetMapping.requiresEscapeEasternUnicode(encoding) && !encoding.equalsIgnoreCase(serverEncoding)) {
                    b = StringUtils.escapeEasternUnicodeByteStream(b, s);
                }
            }
            return b;
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException(Messages.getString("StringUtils.5") + encoding + Messages.getString("StringUtils.6"), "S1009", exceptionInterceptor);
        }
    }

    public static byte[] getBytes(String s, SingleByteCharsetConverter converter, String encoding, String serverEncoding, int offset, int length, boolean parserKnowsUnicode, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            byte[] b;
            if (converter != null) {
                b = converter.toBytes(s, offset, length);
            } else if (encoding == null) {
                b = StringUtils.getBytes(s, offset, length);
            } else {
                s = s.substring(offset, offset + length);
                b = StringUtils.getBytes(s, encoding);
                if (!parserKnowsUnicode && CharsetMapping.requiresEscapeEasternUnicode(encoding) && !encoding.equalsIgnoreCase(serverEncoding)) {
                    b = StringUtils.escapeEasternUnicodeByteStream(b, s);
                }
            }
            return b;
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException(Messages.getString("StringUtils.5") + encoding + Messages.getString("StringUtils.6"), "S1009", exceptionInterceptor);
        }
    }

    public static byte[] getBytes(String s, String encoding, String serverEncoding, boolean parserKnowsUnicode, MySQLConnection conn, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            SingleByteCharsetConverter converter = conn != null ? conn.getCharsetConverter(encoding) : SingleByteCharsetConverter.getInstance(encoding, null);
            return StringUtils.getBytes(s, converter, encoding, serverEncoding, parserKnowsUnicode, exceptionInterceptor);
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException(Messages.getString("StringUtils.5") + encoding + Messages.getString("StringUtils.6"), "S1009", exceptionInterceptor);
        }
    }

    public static final byte[] getBytes(String s, String encoding, String serverEncoding, int offset, int length, boolean parserKnowsUnicode, MySQLConnection conn, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            SingleByteCharsetConverter converter = conn != null ? conn.getCharsetConverter(encoding) : SingleByteCharsetConverter.getInstance(encoding, null);
            return StringUtils.getBytes(s, converter, encoding, serverEncoding, offset, length, parserKnowsUnicode, exceptionInterceptor);
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException(Messages.getString("StringUtils.5") + encoding + Messages.getString("StringUtils.6"), "S1009", exceptionInterceptor);
        }
    }

    public static byte[] getBytesWrapped(String s, char beginWrap, char endWrap, SingleByteCharsetConverter converter, String encoding, String serverEncoding, boolean parserKnowsUnicode, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            byte[] b;
            if (converter != null) {
                b = converter.toBytesWrapped(s, beginWrap, endWrap);
            } else if (encoding == null) {
                StringBuilder strBuilder = new StringBuilder(s.length() + 2);
                strBuilder.append(beginWrap);
                strBuilder.append(s);
                strBuilder.append(endWrap);
                b = StringUtils.getBytes(strBuilder.toString());
            } else {
                StringBuilder strBuilder = new StringBuilder(s.length() + 2);
                strBuilder.append(beginWrap);
                strBuilder.append(s);
                strBuilder.append(endWrap);
                s = strBuilder.toString();
                b = StringUtils.getBytes(s, encoding);
                if (!parserKnowsUnicode && CharsetMapping.requiresEscapeEasternUnicode(encoding) && !encoding.equalsIgnoreCase(serverEncoding)) {
                    b = StringUtils.escapeEasternUnicodeByteStream(b, s);
                }
            }
            return b;
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException(Messages.getString("StringUtils.10") + encoding + Messages.getString("StringUtils.11"), "S1009", exceptionInterceptor);
        }
    }

    public static int getInt(byte[] buf) throws NumberFormatException {
        return StringUtils.getInt(buf, 0, buf.length);
    }

    public static int getInt(byte[] buf, int offset, int endPos) throws NumberFormatException {
        int s;
        char base = '\n';
        for (s = offset; s < endPos && Character.isWhitespace((char)buf[s]); ++s) {
        }
        if (s == endPos) {
            throw new NumberFormatException(StringUtils.toString(buf));
        }
        boolean negative = false;
        if ((char)buf[s] == '-') {
            negative = true;
            ++s;
        } else if ((char)buf[s] == '+') {
            ++s;
        }
        int save = s;
        int cutoff = Integer.MAX_VALUE / base;
        int cutlim = Integer.MAX_VALUE % base;
        if (negative) {
            ++cutlim;
        }
        boolean overflow = false;
        int i = 0;
        while (s < endPos) {
            char c = (char)buf[s];
            if (Character.isDigit(c)) {
                c = (char)(c - 48);
            } else {
                if (!Character.isLetter(c)) break;
                c = (char)(Character.toUpperCase(c) - 65 + 10);
            }
            if (c >= base) break;
            if (i > cutoff || i == cutoff && c > cutlim) {
                overflow = true;
            } else {
                i *= base;
                i += c;
            }
            ++s;
        }
        if (s == save) {
            throw new NumberFormatException(StringUtils.toString(buf));
        }
        if (overflow) {
            throw new NumberFormatException(StringUtils.toString(buf));
        }
        return negative ? -i : i;
    }

    public static long getLong(byte[] buf) throws NumberFormatException {
        return StringUtils.getLong(buf, 0, buf.length);
    }

    public static long getLong(byte[] buf, int offset, int endpos) throws NumberFormatException {
        int s;
        char base = '\n';
        for (s = offset; s < endpos && Character.isWhitespace((char)buf[s]); ++s) {
        }
        if (s == endpos) {
            throw new NumberFormatException(StringUtils.toString(buf));
        }
        boolean negative = false;
        if ((char)buf[s] == '-') {
            negative = true;
            ++s;
        } else if ((char)buf[s] == '+') {
            ++s;
        }
        int save = s;
        long cutoff = Long.MAX_VALUE / (long)base;
        long cutlim = (int)(Long.MAX_VALUE % (long)base);
        if (negative) {
            ++cutlim;
        }
        boolean overflow = false;
        long i = 0L;
        while (s < endpos) {
            char c = (char)buf[s];
            if (Character.isDigit(c)) {
                c = (char)(c - 48);
            } else {
                if (!Character.isLetter(c)) break;
                c = (char)(Character.toUpperCase(c) - 65 + 10);
            }
            if (c >= base) break;
            if (i > cutoff || i == cutoff && (long)c > cutlim) {
                overflow = true;
            } else {
                i *= (long)base;
                i += (long)c;
            }
            ++s;
        }
        if (s == save) {
            throw new NumberFormatException(StringUtils.toString(buf));
        }
        if (overflow) {
            throw new NumberFormatException(StringUtils.toString(buf));
        }
        return negative ? -i : i;
    }

    public static short getShort(byte[] buf) throws NumberFormatException {
        return StringUtils.getShort(buf, 0, buf.length);
    }

    public static short getShort(byte[] buf, int offset, int endpos) throws NumberFormatException {
        int s;
        char base = '\n';
        for (s = offset; s < endpos && Character.isWhitespace((char)buf[s]); ++s) {
        }
        if (s == endpos) {
            throw new NumberFormatException(StringUtils.toString(buf));
        }
        boolean negative = false;
        if ((char)buf[s] == '-') {
            negative = true;
            ++s;
        } else if ((char)buf[s] == '+') {
            ++s;
        }
        int save = s;
        short cutoff = (short)(Short.MAX_VALUE / base);
        short cutlim = (short)(Short.MAX_VALUE % base);
        if (negative) {
            cutlim = (short)(cutlim + 1);
        }
        boolean overflow = false;
        short i = 0;
        while (s < endpos) {
            char c = (char)buf[s];
            if (Character.isDigit(c)) {
                c = (char)(c - 48);
            } else {
                if (!Character.isLetter(c)) break;
                c = (char)(Character.toUpperCase(c) - 65 + 10);
            }
            if (c >= base) break;
            if (i > cutoff || i == cutoff && c > cutlim) {
                overflow = true;
            } else {
                i = (short)(i * base);
                i = (short)(i + c);
            }
            ++s;
        }
        if (s == save) {
            throw new NumberFormatException(StringUtils.toString(buf));
        }
        if (overflow) {
            throw new NumberFormatException(StringUtils.toString(buf));
        }
        return negative ? (short)(-i) : i;
    }

    public static int indexOfIgnoreCase(String searchIn, String searchFor) {
        return StringUtils.indexOfIgnoreCase(0, searchIn, searchFor);
    }

    public static int indexOfIgnoreCase(int startingPosition, String searchIn, String searchFor) {
        int searchForLength;
        if (searchIn == null || searchFor == null) {
            return -1;
        }
        int searchInLength = searchIn.length();
        int stopSearchingAt = searchInLength - (searchForLength = searchFor.length());
        if (startingPosition > stopSearchingAt || searchForLength == 0) {
            return -1;
        }
        char firstCharOfSearchForUc = Character.toUpperCase(searchFor.charAt(0));
        char firstCharOfSearchForLc = Character.toLowerCase(searchFor.charAt(0));
        for (int i = startingPosition; i <= stopSearchingAt; ++i) {
            if (StringUtils.isCharAtPosNotEqualIgnoreCase(searchIn, i, firstCharOfSearchForUc, firstCharOfSearchForLc)) {
                while (++i <= stopSearchingAt && StringUtils.isCharAtPosNotEqualIgnoreCase(searchIn, i, firstCharOfSearchForUc, firstCharOfSearchForLc)) {
                }
            }
            if (i > stopSearchingAt || !StringUtils.startsWithIgnoreCase(searchIn, i, searchFor)) continue;
            return i;
        }
        return -1;
    }

    public static int indexOfIgnoreCase(int startingPosition, String searchIn, String[] searchForSequence, String openingMarkers, String closingMarkers, Set<SearchMode> searchMode) {
        if (searchIn == null || searchForSequence == null) {
            return -1;
        }
        int searchInLength = searchIn.length();
        int searchForLength = 0;
        for (String searchForPart : searchForSequence) {
            searchForLength += searchForPart.length();
        }
        if (searchForLength == 0) {
            return -1;
        }
        int searchForWordsCount = searchForSequence.length;
        int stopSearchingAt = searchInLength - (searchForLength += searchForWordsCount > 0 ? searchForWordsCount - 1 : 0);
        if (startingPosition > stopSearchingAt) {
            return -1;
        }
        if (searchMode.contains((Object)SearchMode.SKIP_BETWEEN_MARKERS) && (openingMarkers == null || closingMarkers == null || openingMarkers.length() != closingMarkers.length())) {
            throw new IllegalArgumentException(Messages.getString("StringUtils.15", new String[]{openingMarkers, closingMarkers}));
        }
        if (Character.isWhitespace(searchForSequence[0].charAt(0)) && searchMode.contains((Object)SearchMode.SKIP_WHITE_SPACE)) {
            searchMode = EnumSet.copyOf(searchMode);
            searchMode.remove((Object)SearchMode.SKIP_WHITE_SPACE);
        }
        EnumSet<SearchMode> searchMode2 = EnumSet.of(SearchMode.SKIP_WHITE_SPACE);
        searchMode2.addAll(searchMode);
        searchMode2.remove((Object)SearchMode.SKIP_BETWEEN_MARKERS);
        for (int positionOfFirstWord = startingPosition; positionOfFirstWord <= stopSearchingAt; ++positionOfFirstWord) {
            if ((positionOfFirstWord = StringUtils.indexOfIgnoreCase(positionOfFirstWord, searchIn, searchForSequence[0], openingMarkers, closingMarkers, searchMode)) == -1 || positionOfFirstWord > stopSearchingAt) {
                return -1;
            }
            int startingPositionForNextWord = positionOfFirstWord + searchForSequence[0].length();
            int wc = 0;
            boolean match = true;
            while (++wc < searchForWordsCount && match) {
                int positionOfNextWord = StringUtils.indexOfNextChar(startingPositionForNextWord, searchInLength - 1, searchIn, null, null, searchMode2);
                if (startingPositionForNextWord == positionOfNextWord || !StringUtils.startsWithIgnoreCase(searchIn, positionOfNextWord, searchForSequence[wc])) {
                    match = false;
                    continue;
                }
                startingPositionForNextWord = positionOfNextWord + searchForSequence[wc].length();
            }
            if (!match) continue;
            return positionOfFirstWord;
        }
        return -1;
    }

    public static int indexOfIgnoreCase(int startingPosition, String searchIn, String searchFor, String openingMarkers, String closingMarkers, Set<SearchMode> searchMode) {
        int searchForLength;
        if (searchIn == null || searchFor == null) {
            return -1;
        }
        int searchInLength = searchIn.length();
        int stopSearchingAt = searchInLength - (searchForLength = searchFor.length());
        if (startingPosition > stopSearchingAt || searchForLength == 0) {
            return -1;
        }
        if (searchMode.contains((Object)SearchMode.SKIP_BETWEEN_MARKERS) && (openingMarkers == null || closingMarkers == null || openingMarkers.length() != closingMarkers.length())) {
            throw new IllegalArgumentException(Messages.getString("StringUtils.15", new String[]{openingMarkers, closingMarkers}));
        }
        char firstCharOfSearchForUc = Character.toUpperCase(searchFor.charAt(0));
        char firstCharOfSearchForLc = Character.toLowerCase(searchFor.charAt(0));
        if (Character.isWhitespace(firstCharOfSearchForLc) && searchMode.contains((Object)SearchMode.SKIP_WHITE_SPACE)) {
            searchMode = EnumSet.copyOf(searchMode);
            searchMode.remove((Object)SearchMode.SKIP_WHITE_SPACE);
        }
        for (int i = startingPosition; i <= stopSearchingAt; ++i) {
            if ((i = StringUtils.indexOfNextChar(i, stopSearchingAt, searchIn, openingMarkers, closingMarkers, searchMode)) == -1) {
                return -1;
            }
            char c = searchIn.charAt(i);
            if (!StringUtils.isCharEqualIgnoreCase(c, firstCharOfSearchForUc, firstCharOfSearchForLc) || !StringUtils.startsWithIgnoreCase(searchIn, i, searchFor)) continue;
            return i;
        }
        return -1;
    }

    private static int indexOfNextChar(int startingPosition, int stopPosition, String searchIn, String openingMarkers, String closingMarkers, Set<SearchMode> searchMode) {
        if (searchIn == null) {
            return -1;
        }
        int searchInLength = searchIn.length();
        if (startingPosition >= searchInLength) {
            return -1;
        }
        char c0 = '\u0000';
        char c1 = searchIn.charAt(startingPosition);
        char c2 = startingPosition + 1 < searchInLength ? searchIn.charAt(startingPosition + 1) : (char)'\u0000';
        for (int i = startingPosition; i <= stopPosition; ++i) {
            c0 = c1;
            c1 = c2;
            c2 = i + 2 < searchInLength ? searchIn.charAt(i + 2) : (char)'\u0000';
            boolean dashDashCommentImmediateEnd = false;
            int markerIndex = -1;
            if (searchMode.contains((Object)SearchMode.ALLOW_BACKSLASH_ESCAPE) && c0 == '\\') {
                c1 = c2;
                c2 = ++i + 2 < searchInLength ? searchIn.charAt(i + 2) : (char)'\u0000';
                continue;
            }
            if (searchMode.contains((Object)SearchMode.SKIP_BETWEEN_MARKERS) && (markerIndex = openingMarkers.indexOf(c0)) != -1) {
                int nestedMarkersCount = 0;
                char openingMarker = c0;
                char closingMarker = closingMarkers.charAt(markerIndex);
                while (++i <= stopPosition && ((c0 = searchIn.charAt(i)) != closingMarker || nestedMarkersCount != 0)) {
                    if (c0 == openingMarker) {
                        ++nestedMarkersCount;
                        continue;
                    }
                    if (c0 == closingMarker) {
                        --nestedMarkersCount;
                        continue;
                    }
                    if (!searchMode.contains((Object)SearchMode.ALLOW_BACKSLASH_ESCAPE) || c0 != '\\') continue;
                    ++i;
                }
                c1 = i + 1 < searchInLength ? searchIn.charAt(i + 1) : (char)'\u0000';
                c2 = i + 2 < searchInLength ? searchIn.charAt(i + 2) : (char)'\u0000';
                continue;
            }
            if (searchMode.contains((Object)SearchMode.SKIP_BLOCK_COMMENTS) && c0 == '/' && c1 == '*') {
                if (c2 != '!') {
                    ++i;
                    while (++i <= stopPosition && (searchIn.charAt(i) != '*' || (i + 1 < searchInLength ? (int)searchIn.charAt(i + 1) : 0) != 47)) {
                    }
                    ++i;
                } else {
                    int j;
                    ++i;
                    ++i;
                    for (j = 1; j <= 5 && i + j < searchInLength && Character.isDigit(searchIn.charAt(i + j)); ++j) {
                    }
                    if (j == 5) {
                        i += 5;
                    }
                }
                c1 = i + 1 < searchInLength ? searchIn.charAt(i + 1) : (char)'\u0000';
                c2 = i + 2 < searchInLength ? searchIn.charAt(i + 2) : (char)'\u0000';
                continue;
            }
            if (searchMode.contains((Object)SearchMode.SKIP_BLOCK_COMMENTS) && c0 == '*' && c1 == '/') {
                c1 = c2;
                c2 = ++i + 2 < searchInLength ? searchIn.charAt(i + 2) : (char)'\u0000';
                continue;
            }
            if (searchMode.contains((Object)SearchMode.SKIP_LINE_COMMENTS) && (c0 == '-' && c1 == '-' && (Character.isWhitespace(c2) || (dashDashCommentImmediateEnd = c2 == ';') || c2 == '\u0000') || c0 == '#')) {
                if (dashDashCommentImmediateEnd) {
                    ++i;
                    c1 = ++i + 1 < searchInLength ? searchIn.charAt(i + 1) : (char)'\u0000';
                    c2 = i + 2 < searchInLength ? searchIn.charAt(i + 2) : (char)'\u0000';
                    continue;
                }
                while (++i <= stopPosition && (c0 = searchIn.charAt(i)) != '\n' && c0 != '\r') {
                }
                char c = c1 = i + 1 < searchInLength ? searchIn.charAt(i + 1) : (char)'\u0000';
                if (c0 == '\r' && c1 == '\n') {
                    c1 = ++i + 1 < searchInLength ? searchIn.charAt(i + 1) : (char)'\u0000';
                }
                c2 = i + 2 < searchInLength ? searchIn.charAt(i + 2) : (char)'\u0000';
                continue;
            }
            if (searchMode.contains((Object)SearchMode.SKIP_WHITE_SPACE) && Character.isWhitespace(c0)) continue;
            return i;
        }
        return -1;
    }

    private static boolean isCharAtPosNotEqualIgnoreCase(String searchIn, int pos, char firstCharOfSearchForUc, char firstCharOfSearchForLc) {
        return Character.toLowerCase(searchIn.charAt(pos)) != firstCharOfSearchForLc && Character.toUpperCase(searchIn.charAt(pos)) != firstCharOfSearchForUc;
    }

    private static boolean isCharEqualIgnoreCase(char charToCompare, char compareToCharUC, char compareToCharLC) {
        return Character.toLowerCase(charToCompare) == compareToCharLC || Character.toUpperCase(charToCompare) == compareToCharUC;
    }

    public static List<String> split(String stringToSplit, String delimiter, boolean trim) {
        if (stringToSplit == null) {
            return new ArrayList<String>();
        }
        if (delimiter == null) {
            throw new IllegalArgumentException();
        }
        StringTokenizer tokenizer = new StringTokenizer(stringToSplit, delimiter, false);
        ArrayList<String> splitTokens = new ArrayList<String>(tokenizer.countTokens());
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (trim) {
                token = token.trim();
            }
            splitTokens.add(token);
        }
        return splitTokens;
    }

    public static List<String> split(String stringToSplit, String delimiter, String markers, String markerCloses, boolean trim) {
        String token;
        if (stringToSplit == null) {
            return new ArrayList<String>();
        }
        if (delimiter == null) {
            throw new IllegalArgumentException();
        }
        int delimPos = 0;
        int currentPos = 0;
        ArrayList<String> splitTokens = new ArrayList<String>();
        while ((delimPos = StringUtils.indexOfIgnoreCase(currentPos, stringToSplit, delimiter, markers, markerCloses, SEARCH_MODE__MRK_COM_WS)) != -1) {
            token = stringToSplit.substring(currentPos, delimPos);
            if (trim) {
                token = token.trim();
            }
            splitTokens.add(token);
            currentPos = delimPos + 1;
        }
        if (currentPos < stringToSplit.length()) {
            token = stringToSplit.substring(currentPos);
            if (trim) {
                token = token.trim();
            }
            splitTokens.add(token);
        }
        return splitTokens;
    }

    private static boolean startsWith(byte[] dataFrom, String chars) {
        int charsLength = chars.length();
        if (dataFrom.length < charsLength) {
            return false;
        }
        for (int i = 0; i < charsLength; ++i) {
            if (dataFrom[i] == chars.charAt(i)) continue;
            return false;
        }
        return true;
    }

    public static boolean startsWithIgnoreCase(String searchIn, int startAt, String searchFor) {
        return searchIn.regionMatches(true, startAt, searchFor, 0, searchFor.length());
    }

    public static boolean startsWithIgnoreCase(String searchIn, String searchFor) {
        return StringUtils.startsWithIgnoreCase(searchIn, 0, searchFor);
    }

    public static boolean startsWithIgnoreCaseAndNonAlphaNumeric(String searchIn, String searchFor) {
        char c;
        int beginPos;
        if (searchIn == null) {
            return searchFor == null;
        }
        int inLength = searchIn.length();
        for (beginPos = 0; beginPos < inLength && !Character.isLetterOrDigit(c = searchIn.charAt(beginPos)); ++beginPos) {
        }
        return StringUtils.startsWithIgnoreCase(searchIn, beginPos, searchFor);
    }

    public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor) {
        return StringUtils.startsWithIgnoreCaseAndWs(searchIn, searchFor, 0);
    }

    public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor, int beginPos) {
        if (searchIn == null) {
            return searchFor == null;
        }
        int inLength = searchIn.length();
        while (beginPos < inLength && Character.isWhitespace(searchIn.charAt(beginPos))) {
            ++beginPos;
        }
        return StringUtils.startsWithIgnoreCase(searchIn, beginPos, searchFor);
    }

    public static int startsWithIgnoreCaseAndWs(String searchIn, String[] searchFor) {
        for (int i = 0; i < searchFor.length; ++i) {
            if (!StringUtils.startsWithIgnoreCaseAndWs(searchIn, searchFor[i], 0)) continue;
            return i;
        }
        return -1;
    }

    public static byte[] stripEnclosure(byte[] source, String prefix, String suffix) {
        if (source.length >= prefix.length() + suffix.length() && StringUtils.startsWith(source, prefix) && StringUtils.endsWith(source, suffix)) {
            int totalToStrip = prefix.length() + suffix.length();
            int enclosedLength = source.length - totalToStrip;
            byte[] enclosed = new byte[enclosedLength];
            int startPos = prefix.length();
            int numToCopy = enclosed.length;
            System.arraycopy(source, startPos, enclosed, 0, numToCopy);
            return enclosed;
        }
        return source;
    }

    public static String toAsciiString(byte[] buffer) {
        return StringUtils.toAsciiString(buffer, 0, buffer.length);
    }

    public static String toAsciiString(byte[] buffer, int startPos, int length) {
        char[] charArray = new char[length];
        int readpoint = startPos;
        for (int i = 0; i < length; ++i) {
            charArray[i] = (char)buffer[readpoint];
            ++readpoint;
        }
        return new String(charArray);
    }

    public static int wildCompare(String searchIn, String searchForWildcard) {
        if (searchIn == null || searchForWildcard == null) {
            return -1;
        }
        if (searchForWildcard.equals("%")) {
            return 1;
        }
        int result = -1;
        char wildcardMany = '%';
        char wildcardOne = '_';
        char wildcardEscape = '\\';
        int searchForPos = 0;
        int searchForEnd = searchForWildcard.length();
        int searchInPos = 0;
        int searchInEnd = searchIn.length();
        while (searchForPos != searchForEnd) {
            char wildstrChar = searchForWildcard.charAt(searchForPos);
            while (searchForWildcard.charAt(searchForPos) != wildcardMany && wildstrChar != wildcardOne) {
                if (searchForWildcard.charAt(searchForPos) == wildcardEscape && searchForPos + 1 != searchForEnd) {
                    ++searchForPos;
                }
                if (searchInPos == searchInEnd || Character.toUpperCase(searchForWildcard.charAt(searchForPos++)) != Character.toUpperCase(searchIn.charAt(searchInPos++))) {
                    return 1;
                }
                if (searchForPos == searchForEnd) {
                    return searchInPos != searchInEnd ? 1 : 0;
                }
                result = 1;
            }
            if (searchForWildcard.charAt(searchForPos) == wildcardOne) {
                do {
                    if (searchInPos == searchInEnd) {
                        return result;
                    }
                    ++searchInPos;
                } while (++searchForPos < searchForEnd && searchForWildcard.charAt(searchForPos) == wildcardOne);
                if (searchForPos == searchForEnd) break;
            }
            if (searchForWildcard.charAt(searchForPos) != wildcardMany) continue;
            ++searchForPos;
            while (searchForPos != searchForEnd) {
                if (searchForWildcard.charAt(searchForPos) != wildcardMany) {
                    if (searchForWildcard.charAt(searchForPos) != wildcardOne) break;
                    if (searchInPos == searchInEnd) {
                        return -1;
                    }
                    ++searchInPos;
                }
                ++searchForPos;
            }
            if (searchForPos == searchForEnd) {
                return 0;
            }
            if (searchInPos == searchInEnd) {
                return -1;
            }
            char cmp = searchForWildcard.charAt(searchForPos);
            if (cmp == wildcardEscape && searchForPos + 1 != searchForEnd) {
                cmp = searchForWildcard.charAt(++searchForPos);
            }
            ++searchForPos;
            while (true) {
                if (searchInPos != searchInEnd && Character.toUpperCase(searchIn.charAt(searchInPos)) != Character.toUpperCase(cmp)) {
                    ++searchInPos;
                    continue;
                }
                if (searchInPos++ == searchInEnd) {
                    return -1;
                }
                int tmp = StringUtils.wildCompare(searchIn, searchForWildcard);
                if (tmp <= 0) {
                    return tmp;
                }
                if (searchInPos == searchInEnd || searchForWildcard.charAt(0) == wildcardMany) break;
            }
            return -1;
        }
        return searchInPos != searchInEnd ? 1 : 0;
    }

    static byte[] s2b(String s, MySQLConnection conn) throws SQLException {
        if (s == null) {
            return null;
        }
        if (conn != null && conn.getUseUnicode()) {
            try {
                String encoding = conn.getEncoding();
                if (encoding == null) {
                    return s.getBytes();
                }
                SingleByteCharsetConverter converter = conn.getCharsetConverter(encoding);
                if (converter != null) {
                    return converter.toBytes(s);
                }
                return s.getBytes(encoding);
            }
            catch (UnsupportedEncodingException E) {
                return s.getBytes();
            }
        }
        return s.getBytes();
    }

    public static int lastIndexOf(byte[] s, char c) {
        if (s == null) {
            return -1;
        }
        for (int i = s.length - 1; i >= 0; --i) {
            if (s[i] != c) continue;
            return i;
        }
        return -1;
    }

    public static int indexOf(byte[] s, char c) {
        if (s == null) {
            return -1;
        }
        int length = s.length;
        for (int i = 0; i < length; ++i) {
            if (s[i] != c) continue;
            return i;
        }
        return -1;
    }

    public static boolean isNullOrEmpty(String toTest) {
        return toTest == null || toTest.length() == 0;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static String stripComments(String src, String stringOpens, String stringCloses, boolean slashStarComments, boolean slashSlashComments, boolean hashComments, boolean dashDashComments) {
        if (src == null) {
            return null;
        }
        StringBuilder strBuilder = new StringBuilder(src.length());
        StringReader sourceReader = new StringReader(src);
        char contextMarker = '\u0000';
        boolean escaped = false;
        int markerTypeFound = -1;
        int ind = 0;
        char currentChar = '\u0000';
        try {
            block2: while ((currentChar = sourceReader.read()) != '\uffffffff') {
                char prevChar;
                block22: {
                    block23: {
                        block20: {
                            block21: {
                                if (markerTypeFound != -1 && currentChar == stringCloses.charAt(markerTypeFound) && !escaped) {
                                    contextMarker = '\u0000';
                                    markerTypeFound = -1;
                                } else {
                                    ind = stringOpens.indexOf(currentChar);
                                    if (ind != -1 && !escaped && contextMarker == '\u0000') {
                                        markerTypeFound = ind;
                                        contextMarker = currentChar;
                                    }
                                }
                                if (contextMarker != '\u0000' || currentChar != '/' || !slashSlashComments && !slashStarComments) break block20;
                                currentChar = sourceReader.read();
                                if (currentChar != '*' || !slashStarComments) break block21;
                                prevChar = '\u0000';
                                break block22;
                            }
                            if (currentChar == '/' && slashSlashComments) {
                                while ((currentChar = sourceReader.read()) != '\n' && currentChar != '\r' && currentChar >= '\u0000') {
                                }
                            }
                            break block23;
                        }
                        if (contextMarker == '\u0000' && currentChar == '#' && hashComments) {
                            while ((currentChar = sourceReader.read()) != '\n' && currentChar != '\r' && currentChar >= '\u0000') {
                            }
                        } else if (contextMarker == '\u0000' && currentChar == '-' && dashDashComments) {
                            currentChar = sourceReader.read();
                            if (currentChar == '\uffffffff' || currentChar != '-') {
                                strBuilder.append('-');
                                if (currentChar == '\uffffffff') continue;
                                strBuilder.append((int)currentChar);
                                continue;
                            }
                            while ((currentChar = sourceReader.read()) != '\n' && currentChar != '\r' && currentChar >= '\u0000') {
                            }
                        }
                    }
                    if (currentChar == '\uffffffff') continue;
                    strBuilder.append(currentChar);
                    continue;
                }
                while ((currentChar = sourceReader.read()) != '/' || prevChar != '*') {
                    if (currentChar == '\r') {
                        currentChar = sourceReader.read();
                        if (currentChar == '\n') {
                            currentChar = sourceReader.read();
                        }
                    } else if (currentChar == '\n') {
                        currentChar = sourceReader.read();
                    }
                    if (currentChar < '\u0000') continue block2;
                    prevChar = currentChar;
                }
            }
            return strBuilder.toString();
        }
        catch (IOException ioEx) {
            // empty catch block
        }
        return strBuilder.toString();
    }

    public static String sanitizeProcOrFuncName(String src) {
        if (src == null || src.equals("%")) {
            return null;
        }
        return src;
    }

    public static List<String> splitDBdotName(String src, String cat, String quotId, boolean isNoBslashEscSet) {
        if (src == null || src.equals("%")) {
            return new ArrayList<String>();
        }
        boolean isQuoted = StringUtils.indexOfIgnoreCase(0, src, quotId) > -1;
        String retval = src;
        String tmpCat = cat;
        int trueDotIndex = -1;
        trueDotIndex = !" ".equals(quotId) ? (isQuoted ? StringUtils.indexOfIgnoreCase(0, retval, quotId + "." + quotId) : StringUtils.indexOfIgnoreCase(0, retval, ".")) : retval.indexOf(".");
        ArrayList<String> retTokens = new ArrayList<String>(2);
        if (trueDotIndex != -1) {
            if (isQuoted) {
                tmpCat = StringUtils.toString(StringUtils.stripEnclosure(retval.substring(0, trueDotIndex + 1).getBytes(), quotId, quotId));
                if (StringUtils.startsWithIgnoreCaseAndWs(tmpCat, quotId)) {
                    tmpCat = tmpCat.substring(1, tmpCat.length() - 1);
                }
                retval = retval.substring(trueDotIndex + 2);
                retval = StringUtils.toString(StringUtils.stripEnclosure(retval.getBytes(), quotId, quotId));
            } else {
                tmpCat = retval.substring(0, trueDotIndex);
                retval = retval.substring(trueDotIndex + 1);
            }
        } else {
            retval = StringUtils.toString(StringUtils.stripEnclosure(retval.getBytes(), quotId, quotId));
        }
        retTokens.add(tmpCat);
        retTokens.add(retval);
        return retTokens;
    }

    public static boolean isEmptyOrWhitespaceOnly(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        int length = str.length();
        for (int i = 0; i < length; ++i) {
            if (Character.isWhitespace(str.charAt(i))) continue;
            return false;
        }
        return true;
    }

    public static String escapeQuote(String src, String quotChar) {
        if (src == null) {
            return null;
        }
        src = StringUtils.toString(StringUtils.stripEnclosure(src.getBytes(), quotChar, quotChar));
        int lastNdx = src.indexOf(quotChar);
        String tmpSrc = src.substring(0, lastNdx);
        tmpSrc = tmpSrc + quotChar + quotChar;
        String tmpRest = src.substring(lastNdx + 1, src.length());
        lastNdx = tmpRest.indexOf(quotChar);
        while (lastNdx > -1) {
            tmpSrc = tmpSrc + tmpRest.substring(0, lastNdx);
            tmpSrc = tmpSrc + quotChar + quotChar;
            tmpRest = tmpRest.substring(lastNdx + 1, tmpRest.length());
            lastNdx = tmpRest.indexOf(quotChar);
        }
        src = tmpSrc = tmpSrc + tmpRest;
        return src;
    }

    public static String quoteIdentifier(String identifier, String quoteChar, boolean isPedantic) {
        if (identifier == null) {
            return null;
        }
        identifier = identifier.trim();
        int quoteCharLength = quoteChar.length();
        if (quoteCharLength == 0 || " ".equals(quoteChar)) {
            return identifier;
        }
        if (!isPedantic && identifier.startsWith(quoteChar) && identifier.endsWith(quoteChar)) {
            int quoteCharNextExpectedPos;
            int quoteCharNextPosition;
            String identifierQuoteTrimmed = identifier.substring(quoteCharLength, identifier.length() - quoteCharLength);
            int quoteCharPos = identifierQuoteTrimmed.indexOf(quoteChar);
            while (quoteCharPos >= 0 && (quoteCharNextPosition = identifierQuoteTrimmed.indexOf(quoteChar, quoteCharNextExpectedPos = quoteCharPos + quoteCharLength)) == quoteCharNextExpectedPos) {
                quoteCharPos = identifierQuoteTrimmed.indexOf(quoteChar, quoteCharNextPosition + quoteCharLength);
            }
            if (quoteCharPos < 0) {
                return identifier;
            }
        }
        return quoteChar + identifier.replaceAll(quoteChar, quoteChar + quoteChar) + quoteChar;
    }

    public static String quoteIdentifier(String identifier, boolean isPedantic) {
        return StringUtils.quoteIdentifier(identifier, "`", isPedantic);
    }

    public static String unQuoteIdentifier(String identifier, String quoteChar) {
        if (identifier == null) {
            return null;
        }
        identifier = identifier.trim();
        int quoteCharLength = quoteChar.length();
        if (quoteCharLength == 0 || " ".equals(quoteChar)) {
            return identifier;
        }
        if (identifier.startsWith(quoteChar) && identifier.endsWith(quoteChar)) {
            String identifierQuoteTrimmed = identifier.substring(quoteCharLength, identifier.length() - quoteCharLength);
            int quoteCharPos = identifierQuoteTrimmed.indexOf(quoteChar);
            while (quoteCharPos >= 0) {
                int quoteCharNextExpectedPos = quoteCharPos + quoteCharLength;
                int quoteCharNextPosition = identifierQuoteTrimmed.indexOf(quoteChar, quoteCharNextExpectedPos);
                if (quoteCharNextPosition == quoteCharNextExpectedPos) {
                    quoteCharPos = identifierQuoteTrimmed.indexOf(quoteChar, quoteCharNextPosition + quoteCharLength);
                    continue;
                }
                return identifier;
            }
            return identifier.substring(quoteCharLength, identifier.length() - quoteCharLength).replaceAll(quoteChar + quoteChar, quoteChar);
        }
        return identifier;
    }

    public static int indexOfQuoteDoubleAware(String searchIn, String quoteChar, int startFrom) {
        if (searchIn == null || quoteChar == null || quoteChar.length() == 0 || startFrom > searchIn.length()) {
            return -1;
        }
        int lastIndex = searchIn.length() - 1;
        int beginPos = startFrom;
        int pos = -1;
        boolean next = true;
        while (next) {
            pos = searchIn.indexOf(quoteChar, beginPos);
            if (pos == -1 || pos == lastIndex || !searchIn.startsWith(quoteChar, pos + 1)) {
                next = false;
                continue;
            }
            beginPos = pos + 2;
        }
        return pos;
    }

    public static String toString(byte[] value, int offset, int length, String encoding) throws UnsupportedEncodingException {
        Charset cs = StringUtils.findCharset(encoding);
        return cs.decode(ByteBuffer.wrap(value, offset, length)).toString();
    }

    public static String toString(byte[] value, String encoding) throws UnsupportedEncodingException {
        Charset cs = StringUtils.findCharset(encoding);
        return cs.decode(ByteBuffer.wrap(value)).toString();
    }

    public static String toString(byte[] value, int offset, int length) {
        try {
            Charset cs = StringUtils.findCharset(platformEncoding);
            return cs.decode(ByteBuffer.wrap(value, offset, length)).toString();
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            return null;
        }
    }

    public static String toString(byte[] value) {
        try {
            Charset cs = StringUtils.findCharset(platformEncoding);
            return cs.decode(ByteBuffer.wrap(value)).toString();
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            return null;
        }
    }

    public static byte[] getBytes(char[] value) {
        try {
            return StringUtils.getBytes(value, 0, value.length, platformEncoding);
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            return null;
        }
    }

    public static byte[] getBytes(char[] value, int offset, int length) {
        try {
            return StringUtils.getBytes(value, offset, length, platformEncoding);
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            return null;
        }
    }

    public static byte[] getBytes(char[] value, String encoding) throws UnsupportedEncodingException {
        return StringUtils.getBytes(value, 0, value.length, encoding);
    }

    public static byte[] getBytes(char[] value, int offset, int length, String encoding) throws UnsupportedEncodingException {
        Charset cs = StringUtils.findCharset(encoding);
        ByteBuffer buf = cs.encode(CharBuffer.wrap(value, offset, length));
        int encodedLen = buf.limit();
        byte[] asBytes = new byte[encodedLen];
        buf.get(asBytes, 0, encodedLen);
        return asBytes;
    }

    public static byte[] getBytes(String value) {
        try {
            return StringUtils.getBytes(value, 0, value.length(), platformEncoding);
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            return null;
        }
    }

    public static byte[] getBytes(String value, int offset, int length) {
        try {
            return StringUtils.getBytes(value, offset, length, platformEncoding);
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            return null;
        }
    }

    public static byte[] getBytes(String value, String encoding) throws UnsupportedEncodingException {
        return StringUtils.getBytes(value, 0, value.length(), encoding);
    }

    public static byte[] getBytes(String value, int offset, int length, String encoding) throws UnsupportedEncodingException {
        if (!Util.isJdbc4()) {
            if (offset != 0 || length != value.length()) {
                return value.substring(offset, offset + length).getBytes(encoding);
            }
            return value.getBytes(encoding);
        }
        Charset cs = StringUtils.findCharset(encoding);
        ByteBuffer buf = cs.encode(CharBuffer.wrap(value.toCharArray(), offset, length));
        int encodedLen = buf.limit();
        byte[] asBytes = new byte[encodedLen];
        buf.get(asBytes, 0, encodedLen);
        return asBytes;
    }

    public static final boolean isValidIdChar(char c) {
        return VALID_ID_CHARS.indexOf(c) != -1;
    }

    public static void appendAsHex(StringBuilder builder, byte[] bytes) {
        builder.append("0x");
        for (byte b : bytes) {
            builder.append(HEX_DIGITS[b >>> 4 & 0xF]).append(HEX_DIGITS[b & 0xF]);
        }
    }

    public static void appendAsHex(StringBuilder builder, int value) {
        if (value == 0) {
            builder.append("0x0");
            return;
        }
        int shift = 32;
        boolean nonZeroFound = false;
        builder.append("0x");
        do {
            byte nibble = (byte)(value >>> (shift -= 4) & 0xF);
            if (nonZeroFound) {
                builder.append(HEX_DIGITS[nibble]);
                continue;
            }
            if (nibble == 0) continue;
            builder.append(HEX_DIGITS[nibble]);
            nonZeroFound = true;
        } while (shift != 0);
    }

    public static byte[] getBytesNullTerminated(String value, String encoding) throws UnsupportedEncodingException {
        Charset cs = StringUtils.findCharset(encoding);
        ByteBuffer buf = cs.encode(value);
        int encodedLen = buf.limit();
        byte[] asBytes = new byte[encodedLen + 1];
        buf.get(asBytes, 0, encodedLen);
        asBytes[encodedLen] = 0;
        return asBytes;
    }

    public static boolean isStrictlyNumeric(CharSequence cs) {
        if (cs == null || cs.length() == 0) {
            return false;
        }
        for (int i = 0; i < cs.length(); ++i) {
            if (Character.isDigit(cs.charAt(i))) continue;
            return false;
        }
        return true;
    }

    static {
        charsetsByAlias = new ConcurrentHashMap();
        platformEncoding = System.getProperty("file.encoding");
        for (int i = -128; i <= 127; ++i) {
            StringUtils.allBytes[i - -128] = (byte)i;
        }
        String allBytesString = new String(allBytes, 0, 255);
        int allBytesStringLen = allBytesString.length();
        for (int i = 0; i < 255 && i < allBytesStringLen; ++i) {
            StringUtils.byteToChars[i] = allBytesString.charAt(i);
        }
        try {
            toPlainStringMethod = BigDecimal.class.getMethod("toPlainString", new Class[0]);
        }
        catch (NoSuchMethodException noSuchMethodException) {
            // empty catch block
        }
        HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum SearchMode {
        ALLOW_BACKSLASH_ESCAPE,
        SKIP_BETWEEN_MARKERS,
        SKIP_BLOCK_COMMENTS,
        SKIP_LINE_COMMENTS,
        SKIP_WHITE_SPACE;

    }
}


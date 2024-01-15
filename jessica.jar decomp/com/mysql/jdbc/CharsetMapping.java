/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Collation;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.MysqlCharset;
import com.mysql.jdbc.SQLError;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CharsetMapping {
    public static final int MAP_SIZE = 255;
    public static final String[] COLLATION_INDEX_TO_COLLATION_NAME;
    public static final MysqlCharset[] COLLATION_INDEX_TO_CHARSET;
    public static final Map<String, MysqlCharset> CHARSET_NAME_TO_CHARSET;
    public static final Map<String, Integer> CHARSET_NAME_TO_COLLATION_INDEX;
    private static final Map<String, List<MysqlCharset>> JAVA_ENCODING_UC_TO_MYSQL_CHARSET;
    private static final Set<String> MULTIBYTE_ENCODINGS;
    private static final Map<String, String> ERROR_MESSAGE_FILE_TO_MYSQL_CHARSET;
    private static final Set<String> ESCAPE_ENCODINGS;
    public static final Set<Integer> UTF8MB4_INDEXES;
    private static final String MYSQL_CHARSET_NAME_armscii8 = "armscii8";
    private static final String MYSQL_CHARSET_NAME_ascii = "ascii";
    private static final String MYSQL_CHARSET_NAME_big5 = "big5";
    private static final String MYSQL_CHARSET_NAME_binary = "binary";
    private static final String MYSQL_CHARSET_NAME_cp1250 = "cp1250";
    private static final String MYSQL_CHARSET_NAME_cp1251 = "cp1251";
    private static final String MYSQL_CHARSET_NAME_cp1256 = "cp1256";
    private static final String MYSQL_CHARSET_NAME_cp1257 = "cp1257";
    private static final String MYSQL_CHARSET_NAME_cp850 = "cp850";
    private static final String MYSQL_CHARSET_NAME_cp852 = "cp852";
    private static final String MYSQL_CHARSET_NAME_cp866 = "cp866";
    private static final String MYSQL_CHARSET_NAME_cp932 = "cp932";
    private static final String MYSQL_CHARSET_NAME_dec8 = "dec8";
    private static final String MYSQL_CHARSET_NAME_eucjpms = "eucjpms";
    private static final String MYSQL_CHARSET_NAME_euckr = "euckr";
    private static final String MYSQL_CHARSET_NAME_gb18030 = "gb18030";
    private static final String MYSQL_CHARSET_NAME_gb2312 = "gb2312";
    private static final String MYSQL_CHARSET_NAME_gbk = "gbk";
    private static final String MYSQL_CHARSET_NAME_geostd8 = "geostd8";
    private static final String MYSQL_CHARSET_NAME_greek = "greek";
    private static final String MYSQL_CHARSET_NAME_hebrew = "hebrew";
    private static final String MYSQL_CHARSET_NAME_hp8 = "hp8";
    private static final String MYSQL_CHARSET_NAME_keybcs2 = "keybcs2";
    private static final String MYSQL_CHARSET_NAME_koi8r = "koi8r";
    private static final String MYSQL_CHARSET_NAME_koi8u = "koi8u";
    private static final String MYSQL_CHARSET_NAME_latin1 = "latin1";
    private static final String MYSQL_CHARSET_NAME_latin2 = "latin2";
    private static final String MYSQL_CHARSET_NAME_latin5 = "latin5";
    private static final String MYSQL_CHARSET_NAME_latin7 = "latin7";
    private static final String MYSQL_CHARSET_NAME_macce = "macce";
    private static final String MYSQL_CHARSET_NAME_macroman = "macroman";
    private static final String MYSQL_CHARSET_NAME_sjis = "sjis";
    private static final String MYSQL_CHARSET_NAME_swe7 = "swe7";
    private static final String MYSQL_CHARSET_NAME_tis620 = "tis620";
    private static final String MYSQL_CHARSET_NAME_ucs2 = "ucs2";
    private static final String MYSQL_CHARSET_NAME_ujis = "ujis";
    private static final String MYSQL_CHARSET_NAME_utf16 = "utf16";
    private static final String MYSQL_CHARSET_NAME_utf16le = "utf16le";
    private static final String MYSQL_CHARSET_NAME_utf32 = "utf32";
    private static final String MYSQL_CHARSET_NAME_utf8 = "utf8";
    private static final String MYSQL_CHARSET_NAME_utf8mb4 = "utf8mb4";
    private static final String MYSQL_4_0_CHARSET_NAME_cp1251cias = "cp1251cias";
    private static final String MYSQL_4_0_CHARSET_NAME_cp1251csas = "cp1251csas";
    private static final String MYSQL_4_0_CHARSET_NAME_croat = "croat";
    private static final String MYSQL_4_0_CHARSET_NAME_czech = "czech";
    private static final String MYSQL_4_0_CHARSET_NAME_danish = "danish";
    private static final String MYSQL_4_0_CHARSET_NAME_dos = "dos";
    private static final String MYSQL_4_0_CHARSET_NAME_estonia = "estonia";
    private static final String MYSQL_4_0_CHARSET_NAME_euc_kr = "euc_kr";
    private static final String MYSQL_4_0_CHARSET_NAME_german1 = "german1";
    private static final String MYSQL_4_0_CHARSET_NAME_hungarian = "hungarian";
    private static final String MYSQL_4_0_CHARSET_NAME_koi8_ru = "koi8_ru";
    private static final String MYSQL_4_0_CHARSET_NAME_koi8_ukr = "koi8_ukr";
    private static final String MYSQL_4_0_CHARSET_NAME_latin1_de = "latin1_de";
    private static final String MYSQL_4_0_CHARSET_NAME_latvian = "latvian";
    private static final String MYSQL_4_0_CHARSET_NAME_latvian1 = "latvian1";
    private static final String MYSQL_4_0_CHARSET_NAME_usa7 = "usa7";
    private static final String MYSQL_4_0_CHARSET_NAME_win1250 = "win1250";
    private static final String MYSQL_4_0_CHARSET_NAME_win1251 = "win1251";
    private static final String MYSQL_4_0_CHARSET_NAME_win1251ukr = "win1251ukr";
    private static final String NOT_USED = "latin1";
    public static final int MYSQL_COLLATION_INDEX_utf8 = 33;
    public static final int MYSQL_COLLATION_INDEX_binary = 63;
    private static int numberOfEncodingsConfigured;

    public static final String getMysqlCharsetForJavaEncoding(String javaEncoding, Connection conn) throws SQLException {
        try {
            List<MysqlCharset> mysqlCharsets = JAVA_ENCODING_UC_TO_MYSQL_CHARSET.get(javaEncoding.toUpperCase(Locale.ENGLISH));
            if (mysqlCharsets != null) {
                Iterator<MysqlCharset> iter = mysqlCharsets.iterator();
                MysqlCharset versionedProp = null;
                while (iter.hasNext()) {
                    MysqlCharset charset = iter.next();
                    if (conn == null) {
                        return charset.charsetName;
                    }
                    if (versionedProp != null && versionedProp.major >= charset.major && versionedProp.minor >= charset.minor && versionedProp.subminor >= charset.subminor && versionedProp.priority >= charset.priority || !charset.isOkayForVersion(conn)) continue;
                    versionedProp = charset;
                }
                if (versionedProp != null) {
                    return versionedProp.charsetName;
                }
            }
            return null;
        }
        catch (SQLException ex) {
            throw ex;
        }
        catch (RuntimeException ex) {
            SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }

    public static int getCollationIndexForJavaEncoding(String javaEncoding, java.sql.Connection conn) throws SQLException {
        Integer ci;
        String charsetName = CharsetMapping.getMysqlCharsetForJavaEncoding(javaEncoding, (Connection)conn);
        if (charsetName != null && (ci = CHARSET_NAME_TO_COLLATION_INDEX.get(charsetName)) != null) {
            return ci;
        }
        return 0;
    }

    public static String getMysqlCharsetNameForCollationIndex(Integer collationIndex) {
        if (collationIndex != null && collationIndex > 0 && collationIndex < 255) {
            return CharsetMapping.COLLATION_INDEX_TO_CHARSET[collationIndex.intValue()].charsetName;
        }
        return null;
    }

    public static String getJavaEncodingForMysqlCharset(String mysqlCharsetName, String javaEncoding) {
        String res = javaEncoding;
        MysqlCharset cs = CHARSET_NAME_TO_CHARSET.get(mysqlCharsetName);
        if (cs != null) {
            res = cs.getMatchingJavaEncoding(javaEncoding);
        }
        return res;
    }

    public static String getJavaEncodingForMysqlCharset(String mysqlCharsetName) {
        return CharsetMapping.getJavaEncodingForMysqlCharset(mysqlCharsetName, null);
    }

    public static String getJavaEncodingForCollationIndex(Integer collationIndex, String javaEncoding) {
        if (collationIndex != null && collationIndex > 0 && collationIndex < 255) {
            MysqlCharset cs = COLLATION_INDEX_TO_CHARSET[collationIndex];
            return cs.getMatchingJavaEncoding(javaEncoding);
        }
        return null;
    }

    public static String getJavaEncodingForCollationIndex(Integer collationIndex) {
        return CharsetMapping.getJavaEncodingForCollationIndex(collationIndex, null);
    }

    static final int getNumberOfCharsetsConfigured() {
        return numberOfEncodingsConfigured;
    }

    static final String getCharacterEncodingForErrorMessages(ConnectionImpl conn) throws SQLException {
        int lastSlashIndex;
        if (conn.versionMeetsMinimum(5, 5, 0)) {
            String javaEncoding;
            String errorMessageCharsetName = conn.getServerVariable("jdbc.local.character_set_results");
            if (errorMessageCharsetName != null && (javaEncoding = CharsetMapping.getJavaEncodingForMysqlCharset(errorMessageCharsetName)) != null) {
                return javaEncoding;
            }
            return "UTF-8";
        }
        String errorMessageFile = conn.getServerVariable("language");
        if (errorMessageFile == null || errorMessageFile.length() == 0) {
            return "Cp1252";
        }
        int endWithoutSlash = errorMessageFile.length();
        if (errorMessageFile.endsWith("/") || errorMessageFile.endsWith("\\")) {
            --endWithoutSlash;
        }
        if ((lastSlashIndex = errorMessageFile.lastIndexOf(47, endWithoutSlash - 1)) == -1) {
            lastSlashIndex = errorMessageFile.lastIndexOf(92, endWithoutSlash - 1);
        }
        if (lastSlashIndex == -1) {
            lastSlashIndex = 0;
        }
        if (lastSlashIndex == endWithoutSlash || endWithoutSlash < lastSlashIndex) {
            return "Cp1252";
        }
        String errorMessageEncodingMysql = ERROR_MESSAGE_FILE_TO_MYSQL_CHARSET.get(errorMessageFile = errorMessageFile.substring(lastSlashIndex + 1, endWithoutSlash));
        if (errorMessageEncodingMysql == null) {
            return "Cp1252";
        }
        String javaEncoding = CharsetMapping.getJavaEncodingForMysqlCharset(errorMessageEncodingMysql);
        if (javaEncoding == null) {
            return "Cp1252";
        }
        return javaEncoding;
    }

    static final boolean requiresEscapeEasternUnicode(String javaEncodingName) {
        return ESCAPE_ENCODINGS.contains(javaEncodingName.toUpperCase(Locale.ENGLISH));
    }

    public static final boolean isMultibyteCharset(String javaEncodingName) {
        return MULTIBYTE_ENCODINGS.contains(javaEncodingName.toUpperCase(Locale.ENGLISH));
    }

    public static int getMblen(String charsetName) {
        MysqlCharset cs;
        if (charsetName != null && (cs = CHARSET_NAME_TO_CHARSET.get(charsetName)) != null) {
            return cs.mblen;
        }
        return 0;
    }

    static {
        int i;
        numberOfEncodingsConfigured = 0;
        MysqlCharset[] charset = new MysqlCharset[]{new MysqlCharset(MYSQL_4_0_CHARSET_NAME_usa7, 1, 0, new String[]{"US-ASCII"}, 4, 0), new MysqlCharset(MYSQL_CHARSET_NAME_ascii, 1, 0, new String[]{"US-ASCII", "ASCII"}), new MysqlCharset(MYSQL_CHARSET_NAME_big5, 2, 0, new String[]{"Big5"}), new MysqlCharset(MYSQL_CHARSET_NAME_gbk, 2, 0, new String[]{"GBK"}), new MysqlCharset(MYSQL_CHARSET_NAME_sjis, 2, 0, new String[]{"SHIFT_JIS", "Cp943", "WINDOWS-31J"}), new MysqlCharset(MYSQL_CHARSET_NAME_cp932, 2, 1, new String[]{"WINDOWS-31J"}), new MysqlCharset(MYSQL_CHARSET_NAME_gb2312, 2, 0, new String[]{"GB2312"}), new MysqlCharset(MYSQL_CHARSET_NAME_ujis, 3, 0, new String[]{"EUC_JP"}), new MysqlCharset(MYSQL_CHARSET_NAME_eucjpms, 3, 0, new String[]{"EUC_JP_Solaris"}, 5, 0, 3), new MysqlCharset(MYSQL_CHARSET_NAME_gb18030, 4, 0, new String[]{"GB18030"}, 5, 7, 4), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_euc_kr, 2, 0, new String[]{"EUC_KR"}, 4, 0), new MysqlCharset(MYSQL_CHARSET_NAME_euckr, 2, 0, new String[]{"EUC-KR"}), new MysqlCharset("latin1", 1, 1, new String[]{"Cp1252", "ISO8859_1"}), new MysqlCharset(MYSQL_CHARSET_NAME_swe7, 1, 0, new String[]{"Cp1252"}), new MysqlCharset(MYSQL_CHARSET_NAME_hp8, 1, 0, new String[]{"Cp1252"}), new MysqlCharset(MYSQL_CHARSET_NAME_dec8, 1, 0, new String[]{"Cp1252"}), new MysqlCharset(MYSQL_CHARSET_NAME_armscii8, 1, 0, new String[]{"Cp1252"}), new MysqlCharset(MYSQL_CHARSET_NAME_geostd8, 1, 0, new String[]{"Cp1252"}), new MysqlCharset(MYSQL_CHARSET_NAME_latin2, 1, 0, new String[]{"ISO8859_2"}), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_czech, 1, 0, new String[]{"ISO8859_2"}, 4, 0), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_hungarian, 1, 0, new String[]{"ISO8859_2"}, 4, 0), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_croat, 1, 0, new String[]{"ISO8859_2"}, 4, 0), new MysqlCharset(MYSQL_CHARSET_NAME_greek, 1, 0, new String[]{"ISO8859_7", MYSQL_CHARSET_NAME_greek}), new MysqlCharset(MYSQL_CHARSET_NAME_latin7, 1, 0, new String[]{"ISO-8859-13"}), new MysqlCharset(MYSQL_CHARSET_NAME_hebrew, 1, 0, new String[]{"ISO8859_8"}), new MysqlCharset(MYSQL_CHARSET_NAME_latin5, 1, 0, new String[]{"ISO8859_9"}), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_latvian, 1, 0, new String[]{"ISO8859_13"}, 4, 0), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_latvian1, 1, 0, new String[]{"ISO8859_13"}, 4, 0), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_estonia, 1, 1, new String[]{"ISO8859_13"}, 4, 0), new MysqlCharset(MYSQL_CHARSET_NAME_cp850, 1, 0, new String[]{"Cp850", "Cp437"}), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_dos, 1, 0, new String[]{"Cp850", "Cp437"}, 4, 0), new MysqlCharset(MYSQL_CHARSET_NAME_cp852, 1, 0, new String[]{"Cp852"}), new MysqlCharset(MYSQL_CHARSET_NAME_keybcs2, 1, 0, new String[]{"Cp852"}), new MysqlCharset(MYSQL_CHARSET_NAME_cp866, 1, 0, new String[]{"Cp866"}), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_koi8_ru, 1, 0, new String[]{"KOI8_R"}, 4, 0), new MysqlCharset(MYSQL_CHARSET_NAME_koi8r, 1, 1, new String[]{"KOI8_R"}), new MysqlCharset(MYSQL_CHARSET_NAME_koi8u, 1, 0, new String[]{"KOI8_R"}), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_koi8_ukr, 1, 0, new String[]{"KOI8_R"}, 4, 0), new MysqlCharset(MYSQL_CHARSET_NAME_tis620, 1, 0, new String[]{"TIS620"}), new MysqlCharset(MYSQL_CHARSET_NAME_cp1250, 1, 0, new String[]{"Cp1250"}), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_win1250, 1, 0, new String[]{"Cp1250"}, 4, 0), new MysqlCharset(MYSQL_CHARSET_NAME_cp1251, 1, 1, new String[]{"Cp1251"}), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_win1251, 1, 0, new String[]{"Cp1251"}, 4, 0), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_cp1251cias, 1, 0, new String[]{"Cp1251"}, 4, 0), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_cp1251csas, 1, 0, new String[]{"Cp1251"}, 4, 0), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_win1251ukr, 1, 0, new String[]{"Cp1251"}, 4, 0), new MysqlCharset(MYSQL_CHARSET_NAME_cp1256, 1, 0, new String[]{"Cp1256"}), new MysqlCharset(MYSQL_CHARSET_NAME_cp1257, 1, 0, new String[]{"Cp1257"}), new MysqlCharset(MYSQL_CHARSET_NAME_macroman, 1, 0, new String[]{"MacRoman"}), new MysqlCharset(MYSQL_CHARSET_NAME_macce, 1, 0, new String[]{"MacCentralEurope"}), new MysqlCharset(MYSQL_CHARSET_NAME_utf8, 3, 1, new String[]{"UTF-8"}), new MysqlCharset(MYSQL_CHARSET_NAME_utf8mb4, 4, 0, new String[]{"UTF-8"}), new MysqlCharset(MYSQL_CHARSET_NAME_ucs2, 2, 0, new String[]{"UnicodeBig"}), new MysqlCharset(MYSQL_CHARSET_NAME_binary, 1, 1, new String[]{"ISO8859_1"}), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_latin1_de, 1, 0, new String[]{"ISO8859_1"}, 4, 0), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_german1, 1, 0, new String[]{"ISO8859_1"}, 4, 0), new MysqlCharset(MYSQL_4_0_CHARSET_NAME_danish, 1, 0, new String[]{"ISO8859_1"}, 4, 0), new MysqlCharset(MYSQL_CHARSET_NAME_utf16, 4, 0, new String[]{"UTF-16"}), new MysqlCharset(MYSQL_CHARSET_NAME_utf16le, 4, 0, new String[]{"UTF-16LE"}), new MysqlCharset(MYSQL_CHARSET_NAME_utf32, 4, 0, new String[]{"UTF-32"})};
        HashMap<String, MysqlCharset> charsetNameToMysqlCharsetMap = new HashMap<String, MysqlCharset>();
        HashMap<String, ArrayList<MysqlCharset>> javaUcToMysqlCharsetMap = new HashMap<String, ArrayList<MysqlCharset>>();
        HashSet<String> tempMultibyteEncodings = new HashSet<String>();
        HashSet<String> tempEscapeEncodings = new HashSet<String>();
        for (int i2 = 0; i2 < charset.length; ++i2) {
            String charsetName = charset[i2].charsetName;
            charsetNameToMysqlCharsetMap.put(charsetName, charset[i2]);
            numberOfEncodingsConfigured += charset[i2].javaEncodingsUc.size();
            for (String encUC : charset[i2].javaEncodingsUc) {
                ArrayList<MysqlCharset> charsets = (ArrayList<MysqlCharset>)javaUcToMysqlCharsetMap.get(encUC);
                if (charsets == null) {
                    charsets = new ArrayList<MysqlCharset>();
                    javaUcToMysqlCharsetMap.put(encUC, charsets);
                }
                charsets.add(charset[i2]);
                if (charset[i2].mblen <= 1) continue;
                tempMultibyteEncodings.add(encUC);
            }
            if (!charsetName.equals(MYSQL_CHARSET_NAME_big5) && !charsetName.equals(MYSQL_CHARSET_NAME_gbk) && !charsetName.equals(MYSQL_CHARSET_NAME_sjis)) continue;
            tempEscapeEncodings.addAll(charset[i2].javaEncodingsUc);
        }
        CHARSET_NAME_TO_CHARSET = Collections.unmodifiableMap(charsetNameToMysqlCharsetMap);
        JAVA_ENCODING_UC_TO_MYSQL_CHARSET = Collections.unmodifiableMap(javaUcToMysqlCharsetMap);
        MULTIBYTE_ENCODINGS = Collections.unmodifiableSet(tempMultibyteEncodings);
        ESCAPE_ENCODINGS = Collections.unmodifiableSet(tempEscapeEncodings);
        Collation[] collation = new Collation[255];
        collation[1] = new Collation(1, "big5_chinese_ci", 1, MYSQL_CHARSET_NAME_big5);
        collation[84] = new Collation(84, "big5_bin", 0, MYSQL_CHARSET_NAME_big5);
        collation[2] = new Collation(2, "latin2_czech_cs", 0, MYSQL_CHARSET_NAME_latin2);
        collation[9] = new Collation(9, "latin2_general_ci", 1, MYSQL_CHARSET_NAME_latin2);
        collation[21] = new Collation(21, "latin2_hungarian_ci", 0, MYSQL_CHARSET_NAME_latin2);
        collation[27] = new Collation(27, "latin2_croatian_ci", 0, MYSQL_CHARSET_NAME_latin2);
        collation[77] = new Collation(77, "latin2_bin", 0, MYSQL_CHARSET_NAME_latin2);
        collation[4] = new Collation(4, "cp850_general_ci", 1, MYSQL_CHARSET_NAME_cp850);
        collation[80] = new Collation(80, "cp850_bin", 0, MYSQL_CHARSET_NAME_cp850);
        collation[5] = new Collation(5, "latin1_german1_ci", 1, "latin1");
        collation[8] = new Collation(8, "latin1_swedish_ci", 0, "latin1");
        collation[15] = new Collation(15, "latin1_danish_ci", 0, "latin1");
        collation[31] = new Collation(31, "latin1_german2_ci", 0, "latin1");
        collation[47] = new Collation(47, "latin1_bin", 0, "latin1");
        collation[48] = new Collation(48, "latin1_general_ci", 0, "latin1");
        collation[49] = new Collation(49, "latin1_general_cs", 0, "latin1");
        collation[76] = new Collation(76, "not_implemented", 0, "latin1");
        collation[94] = new Collation(94, "latin1_spanish_ci", 0, "latin1");
        collation[100] = new Collation(100, "not_implemented", 0, "latin1");
        collation[125] = new Collation(125, "not_implemented", 0, "latin1");
        collation[126] = new Collation(126, "not_implemented", 0, "latin1");
        collation[127] = new Collation(127, "not_implemented", 0, "latin1");
        collation[152] = new Collation(152, "not_implemented", 0, "latin1");
        collation[153] = new Collation(153, "not_implemented", 0, "latin1");
        collation[154] = new Collation(154, "not_implemented", 0, "latin1");
        collation[155] = new Collation(155, "not_implemented", 0, "latin1");
        collation[156] = new Collation(156, "not_implemented", 0, "latin1");
        collation[157] = new Collation(157, "not_implemented", 0, "latin1");
        collation[158] = new Collation(158, "not_implemented", 0, "latin1");
        collation[184] = new Collation(184, "not_implemented", 0, "latin1");
        collation[185] = new Collation(185, "not_implemented", 0, "latin1");
        collation[186] = new Collation(186, "not_implemented", 0, "latin1");
        collation[187] = new Collation(187, "not_implemented", 0, "latin1");
        collation[188] = new Collation(188, "not_implemented", 0, "latin1");
        collation[189] = new Collation(189, "not_implemented", 0, "latin1");
        collation[190] = new Collation(190, "not_implemented", 0, "latin1");
        collation[191] = new Collation(191, "not_implemented", 0, "latin1");
        collation[216] = new Collation(216, "not_implemented", 0, "latin1");
        collation[217] = new Collation(217, "not_implemented", 0, "latin1");
        collation[218] = new Collation(218, "not_implemented", 0, "latin1");
        collation[219] = new Collation(219, "not_implemented", 0, "latin1");
        collation[220] = new Collation(220, "not_implemented", 0, "latin1");
        collation[221] = new Collation(221, "not_implemented", 0, "latin1");
        collation[222] = new Collation(222, "not_implemented", 0, "latin1");
        collation[248] = new Collation(248, "gb18030_chinese_ci", 1, MYSQL_CHARSET_NAME_gb18030);
        collation[249] = new Collation(249, "gb18030_bin", 0, MYSQL_CHARSET_NAME_gb18030);
        collation[250] = new Collation(250, "gb18030_unicode_520_ci", 0, MYSQL_CHARSET_NAME_gb18030);
        collation[251] = new Collation(251, "not_implemented", 0, "latin1");
        collation[252] = new Collation(252, "not_implemented", 0, "latin1");
        collation[253] = new Collation(253, "not_implemented", 0, "latin1");
        collation[254] = new Collation(254, "not_implemented", 0, "latin1");
        collation[10] = new Collation(10, "swe7_swedish_ci", 0, MYSQL_CHARSET_NAME_swe7);
        collation[82] = new Collation(82, "swe7_bin", 0, MYSQL_CHARSET_NAME_swe7);
        collation[6] = new Collation(6, "hp8_english_ci", 0, MYSQL_CHARSET_NAME_hp8);
        collation[72] = new Collation(72, "hp8_bin", 0, MYSQL_CHARSET_NAME_hp8);
        collation[3] = new Collation(3, "dec8_swedish_ci", 0, MYSQL_CHARSET_NAME_dec8);
        collation[69] = new Collation(69, "dec8_bin", 0, MYSQL_CHARSET_NAME_dec8);
        collation[32] = new Collation(32, "armscii8_general_ci", 0, MYSQL_CHARSET_NAME_armscii8);
        collation[64] = new Collation(64, "armscii8_bin", 0, MYSQL_CHARSET_NAME_armscii8);
        collation[92] = new Collation(92, "geostd8_general_ci", 0, MYSQL_CHARSET_NAME_geostd8);
        collation[93] = new Collation(93, "geostd8_bin", 0, MYSQL_CHARSET_NAME_geostd8);
        collation[7] = new Collation(7, "koi8r_general_ci", 0, MYSQL_CHARSET_NAME_koi8r);
        collation[74] = new Collation(74, "koi8r_bin", 0, MYSQL_CHARSET_NAME_koi8r);
        collation[11] = new Collation(11, "ascii_general_ci", 0, MYSQL_CHARSET_NAME_ascii);
        collation[65] = new Collation(65, "ascii_bin", 0, MYSQL_CHARSET_NAME_ascii);
        collation[12] = new Collation(12, "ujis_japanese_ci", 0, MYSQL_CHARSET_NAME_ujis);
        collation[91] = new Collation(91, "ujis_bin", 0, MYSQL_CHARSET_NAME_ujis);
        collation[13] = new Collation(13, "sjis_japanese_ci", 0, MYSQL_CHARSET_NAME_sjis);
        collation[14] = new Collation(14, "cp1251_bulgarian_ci", 0, MYSQL_CHARSET_NAME_cp1251);
        collation[16] = new Collation(16, "hebrew_general_ci", 0, MYSQL_CHARSET_NAME_hebrew);
        collation[17] = new Collation(17, "latin1_german1_ci", 0, MYSQL_4_0_CHARSET_NAME_win1251);
        collation[18] = new Collation(18, "tis620_thai_ci", 0, MYSQL_CHARSET_NAME_tis620);
        collation[19] = new Collation(19, "euckr_korean_ci", 0, MYSQL_CHARSET_NAME_euckr);
        collation[20] = new Collation(20, "latin7_estonian_cs", 0, MYSQL_CHARSET_NAME_latin7);
        collation[22] = new Collation(22, "koi8u_general_ci", 0, MYSQL_CHARSET_NAME_koi8u);
        collation[23] = new Collation(23, "cp1251_ukrainian_ci", 0, MYSQL_CHARSET_NAME_cp1251);
        collation[24] = new Collation(24, "gb2312_chinese_ci", 0, MYSQL_CHARSET_NAME_gb2312);
        collation[25] = new Collation(25, "greek_general_ci", 0, MYSQL_CHARSET_NAME_greek);
        collation[26] = new Collation(26, "cp1250_general_ci", 1, MYSQL_CHARSET_NAME_cp1250);
        collation[28] = new Collation(28, "gbk_chinese_ci", 1, MYSQL_CHARSET_NAME_gbk);
        collation[29] = new Collation(29, "cp1257_lithuanian_ci", 0, MYSQL_CHARSET_NAME_cp1257);
        collation[30] = new Collation(30, "latin5_turkish_ci", 1, MYSQL_CHARSET_NAME_latin5);
        collation[33] = new Collation(33, "utf8_general_ci", 1, MYSQL_CHARSET_NAME_utf8);
        collation[34] = new Collation(34, "cp1250_czech_cs", 0, MYSQL_CHARSET_NAME_cp1250);
        collation[35] = new Collation(35, "ucs2_general_ci", 1, MYSQL_CHARSET_NAME_ucs2);
        collation[36] = new Collation(36, "cp866_general_ci", 1, MYSQL_CHARSET_NAME_cp866);
        collation[37] = new Collation(37, "keybcs2_general_ci", 1, MYSQL_CHARSET_NAME_keybcs2);
        collation[38] = new Collation(38, "macce_general_ci", 1, MYSQL_CHARSET_NAME_macce);
        collation[39] = new Collation(39, "macroman_general_ci", 1, MYSQL_CHARSET_NAME_macroman);
        collation[40] = new Collation(40, "cp852_general_ci", 1, MYSQL_CHARSET_NAME_cp852);
        collation[41] = new Collation(41, "latin7_general_ci", 1, MYSQL_CHARSET_NAME_latin7);
        collation[42] = new Collation(42, "latin7_general_cs", 0, MYSQL_CHARSET_NAME_latin7);
        collation[43] = new Collation(43, "macce_bin", 0, MYSQL_CHARSET_NAME_macce);
        collation[44] = new Collation(44, "cp1250_croatian_ci", 0, MYSQL_CHARSET_NAME_cp1250);
        collation[45] = new Collation(45, "utf8mb4_general_ci", 1, MYSQL_CHARSET_NAME_utf8mb4);
        collation[46] = new Collation(46, "utf8mb4_bin", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[50] = new Collation(50, "cp1251_bin", 0, MYSQL_CHARSET_NAME_cp1251);
        collation[51] = new Collation(51, "cp1251_general_ci", 1, MYSQL_CHARSET_NAME_cp1251);
        collation[52] = new Collation(52, "cp1251_general_cs", 0, MYSQL_CHARSET_NAME_cp1251);
        collation[53] = new Collation(53, "macroman_bin", 0, MYSQL_CHARSET_NAME_macroman);
        collation[54] = new Collation(54, "utf16_general_ci", 1, MYSQL_CHARSET_NAME_utf16);
        collation[55] = new Collation(55, "utf16_bin", 0, MYSQL_CHARSET_NAME_utf16);
        collation[56] = new Collation(56, "utf16le_general_ci", 1, MYSQL_CHARSET_NAME_utf16le);
        collation[57] = new Collation(57, "cp1256_general_ci", 1, MYSQL_CHARSET_NAME_cp1256);
        collation[58] = new Collation(58, "cp1257_bin", 0, MYSQL_CHARSET_NAME_cp1257);
        collation[59] = new Collation(59, "cp1257_general_ci", 1, MYSQL_CHARSET_NAME_cp1257);
        collation[60] = new Collation(60, "utf32_general_ci", 1, MYSQL_CHARSET_NAME_utf32);
        collation[61] = new Collation(61, "utf32_bin", 0, MYSQL_CHARSET_NAME_utf32);
        collation[62] = new Collation(62, "utf16le_bin", 0, MYSQL_CHARSET_NAME_utf16le);
        collation[63] = new Collation(63, MYSQL_CHARSET_NAME_binary, 1, MYSQL_CHARSET_NAME_binary);
        collation[66] = new Collation(66, "cp1250_bin", 0, MYSQL_CHARSET_NAME_cp1250);
        collation[67] = new Collation(67, "cp1256_bin", 0, MYSQL_CHARSET_NAME_cp1256);
        collation[68] = new Collation(68, "cp866_bin", 0, MYSQL_CHARSET_NAME_cp866);
        collation[70] = new Collation(70, "greek_bin", 0, MYSQL_CHARSET_NAME_greek);
        collation[71] = new Collation(71, "hebrew_bin", 0, MYSQL_CHARSET_NAME_hebrew);
        collation[73] = new Collation(73, "keybcs2_bin", 0, MYSQL_CHARSET_NAME_keybcs2);
        collation[75] = new Collation(75, "koi8u_bin", 0, MYSQL_CHARSET_NAME_koi8u);
        collation[78] = new Collation(78, "latin5_bin", 0, MYSQL_CHARSET_NAME_latin5);
        collation[79] = new Collation(79, "latin7_bin", 0, MYSQL_CHARSET_NAME_latin7);
        collation[81] = new Collation(81, "cp852_bin", 0, MYSQL_CHARSET_NAME_cp852);
        collation[83] = new Collation(83, "utf8_bin", 0, MYSQL_CHARSET_NAME_utf8);
        collation[85] = new Collation(85, "euckr_bin", 0, MYSQL_CHARSET_NAME_euckr);
        collation[86] = new Collation(86, "gb2312_bin", 0, MYSQL_CHARSET_NAME_gb2312);
        collation[87] = new Collation(87, "gbk_bin", 0, MYSQL_CHARSET_NAME_gbk);
        collation[88] = new Collation(88, "sjis_bin", 0, MYSQL_CHARSET_NAME_sjis);
        collation[89] = new Collation(89, "tis620_bin", 0, MYSQL_CHARSET_NAME_tis620);
        collation[90] = new Collation(90, "ucs2_bin", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[95] = new Collation(95, "cp932_japanese_ci", 1, MYSQL_CHARSET_NAME_cp932);
        collation[96] = new Collation(96, "cp932_bin", 0, MYSQL_CHARSET_NAME_cp932);
        collation[97] = new Collation(97, "eucjpms_japanese_ci", 1, MYSQL_CHARSET_NAME_eucjpms);
        collation[98] = new Collation(98, "eucjpms_bin", 0, MYSQL_CHARSET_NAME_eucjpms);
        collation[99] = new Collation(99, "cp1250_polish_ci", 0, MYSQL_CHARSET_NAME_cp1250);
        collation[101] = new Collation(101, "utf16_unicode_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[102] = new Collation(102, "utf16_icelandic_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[103] = new Collation(103, "utf16_latvian_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[104] = new Collation(104, "utf16_romanian_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[105] = new Collation(105, "utf16_slovenian_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[106] = new Collation(106, "utf16_polish_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[107] = new Collation(107, "utf16_estonian_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[108] = new Collation(108, "utf16_spanish_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[109] = new Collation(109, "utf16_swedish_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[110] = new Collation(110, "utf16_turkish_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[111] = new Collation(111, "utf16_czech_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[112] = new Collation(112, "utf16_danish_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[113] = new Collation(113, "utf16_lithuanian_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[114] = new Collation(114, "utf16_slovak_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[115] = new Collation(115, "utf16_spanish2_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[116] = new Collation(116, "utf16_roman_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[117] = new Collation(117, "utf16_persian_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[118] = new Collation(118, "utf16_esperanto_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[119] = new Collation(119, "utf16_hungarian_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[120] = new Collation(120, "utf16_sinhala_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[121] = new Collation(121, "utf16_german2_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[122] = new Collation(122, "utf16_croatian_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[123] = new Collation(123, "utf16_unicode_520_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[124] = new Collation(124, "utf16_vietnamese_ci", 0, MYSQL_CHARSET_NAME_utf16);
        collation[128] = new Collation(128, "ucs2_unicode_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[129] = new Collation(129, "ucs2_icelandic_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[130] = new Collation(130, "ucs2_latvian_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[131] = new Collation(131, "ucs2_romanian_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[132] = new Collation(132, "ucs2_slovenian_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[133] = new Collation(133, "ucs2_polish_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[134] = new Collation(134, "ucs2_estonian_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[135] = new Collation(135, "ucs2_spanish_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[136] = new Collation(136, "ucs2_swedish_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[137] = new Collation(137, "ucs2_turkish_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[138] = new Collation(138, "ucs2_czech_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[139] = new Collation(139, "ucs2_danish_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[140] = new Collation(140, "ucs2_lithuanian_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[141] = new Collation(141, "ucs2_slovak_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[142] = new Collation(142, "ucs2_spanish2_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[143] = new Collation(143, "ucs2_roman_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[144] = new Collation(144, "ucs2_persian_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[145] = new Collation(145, "ucs2_esperanto_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[146] = new Collation(146, "ucs2_hungarian_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[147] = new Collation(147, "ucs2_sinhala_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[148] = new Collation(148, "ucs2_german2_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[149] = new Collation(149, "ucs2_croatian_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[150] = new Collation(150, "ucs2_unicode_520_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[151] = new Collation(151, "ucs2_vietnamese_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[159] = new Collation(159, "ucs2_general_mysql500_ci", 0, MYSQL_CHARSET_NAME_ucs2);
        collation[160] = new Collation(160, "utf32_unicode_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[161] = new Collation(161, "utf32_icelandic_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[162] = new Collation(162, "utf32_latvian_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[163] = new Collation(163, "utf32_romanian_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[164] = new Collation(164, "utf32_slovenian_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[165] = new Collation(165, "utf32_polish_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[166] = new Collation(166, "utf32_estonian_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[167] = new Collation(167, "utf32_spanish_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[168] = new Collation(168, "utf32_swedish_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[169] = new Collation(169, "utf32_turkish_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[170] = new Collation(170, "utf32_czech_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[171] = new Collation(171, "utf32_danish_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[172] = new Collation(172, "utf32_lithuanian_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[173] = new Collation(173, "utf32_slovak_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[174] = new Collation(174, "utf32_spanish2_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[175] = new Collation(175, "utf32_roman_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[176] = new Collation(176, "utf32_persian_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[177] = new Collation(177, "utf32_esperanto_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[178] = new Collation(178, "utf32_hungarian_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[179] = new Collation(179, "utf32_sinhala_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[180] = new Collation(180, "utf32_german2_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[181] = new Collation(181, "utf32_croatian_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[182] = new Collation(182, "utf32_unicode_520_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[183] = new Collation(183, "utf32_vietnamese_ci", 0, MYSQL_CHARSET_NAME_utf32);
        collation[192] = new Collation(192, "utf8_unicode_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[193] = new Collation(193, "utf8_icelandic_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[194] = new Collation(194, "utf8_latvian_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[195] = new Collation(195, "utf8_romanian_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[196] = new Collation(196, "utf8_slovenian_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[197] = new Collation(197, "utf8_polish_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[198] = new Collation(198, "utf8_estonian_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[199] = new Collation(199, "utf8_spanish_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[200] = new Collation(200, "utf8_swedish_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[201] = new Collation(201, "utf8_turkish_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[202] = new Collation(202, "utf8_czech_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[203] = new Collation(203, "utf8_danish_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[204] = new Collation(204, "utf8_lithuanian_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[205] = new Collation(205, "utf8_slovak_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[206] = new Collation(206, "utf8_spanish2_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[207] = new Collation(207, "utf8_roman_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[208] = new Collation(208, "utf8_persian_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[209] = new Collation(209, "utf8_esperanto_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[210] = new Collation(210, "utf8_hungarian_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[211] = new Collation(211, "utf8_sinhala_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[212] = new Collation(212, "utf8_german2_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[213] = new Collation(213, "utf8_croatian_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[214] = new Collation(214, "utf8_unicode_520_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[215] = new Collation(215, "utf8_vietnamese_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[223] = new Collation(223, "utf8_general_mysql500_ci", 0, MYSQL_CHARSET_NAME_utf8);
        collation[224] = new Collation(224, "utf8mb4_unicode_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[225] = new Collation(225, "utf8mb4_icelandic_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[226] = new Collation(226, "utf8mb4_latvian_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[227] = new Collation(227, "utf8mb4_romanian_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[228] = new Collation(228, "utf8mb4_slovenian_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[229] = new Collation(229, "utf8mb4_polish_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[230] = new Collation(230, "utf8mb4_estonian_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[231] = new Collation(231, "utf8mb4_spanish_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[232] = new Collation(232, "utf8mb4_swedish_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[233] = new Collation(233, "utf8mb4_turkish_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[234] = new Collation(234, "utf8mb4_czech_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[235] = new Collation(235, "utf8mb4_danish_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[236] = new Collation(236, "utf8mb4_lithuanian_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[237] = new Collation(237, "utf8mb4_slovak_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[238] = new Collation(238, "utf8mb4_spanish2_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[239] = new Collation(239, "utf8mb4_roman_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[240] = new Collation(240, "utf8mb4_persian_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[241] = new Collation(241, "utf8mb4_esperanto_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[242] = new Collation(242, "utf8mb4_hungarian_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[243] = new Collation(243, "utf8mb4_sinhala_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[244] = new Collation(244, "utf8mb4_german2_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[245] = new Collation(245, "utf8mb4_croatian_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[246] = new Collation(246, "utf8mb4_unicode_520_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        collation[247] = new Collation(247, "utf8mb4_vietnamese_ci", 0, MYSQL_CHARSET_NAME_utf8mb4);
        COLLATION_INDEX_TO_COLLATION_NAME = new String[255];
        COLLATION_INDEX_TO_CHARSET = new MysqlCharset[255];
        TreeMap<String, Integer> charsetNameToCollationIndexMap = new TreeMap<String, Integer>();
        TreeMap<String, Integer> charsetNameToCollationPriorityMap = new TreeMap<String, Integer>();
        HashSet<Integer> tempUTF8MB4Indexes = new HashSet<Integer>();
        for (i = 1; i < 255; ++i) {
            CharsetMapping.COLLATION_INDEX_TO_COLLATION_NAME[i] = collation[i].collationName;
            CharsetMapping.COLLATION_INDEX_TO_CHARSET[i] = collation[i].mysqlCharset;
            String charsetName = collation[i].mysqlCharset.charsetName;
            if (!charsetNameToCollationIndexMap.containsKey(charsetName) || (Integer)charsetNameToCollationPriorityMap.get(charsetName) < collation[i].priority) {
                charsetNameToCollationIndexMap.put(charsetName, i);
                charsetNameToCollationPriorityMap.put(charsetName, collation[i].priority);
            }
            if (!charsetName.equals(MYSQL_CHARSET_NAME_utf8mb4)) continue;
            tempUTF8MB4Indexes.add(i);
        }
        for (i = 1; i < 255; ++i) {
            if (COLLATION_INDEX_TO_COLLATION_NAME[i] == null) {
                throw new RuntimeException("Assertion failure: No mapping from charset index " + i + " to a mysql collation");
            }
            if (COLLATION_INDEX_TO_COLLATION_NAME[i] != null) continue;
            throw new RuntimeException("Assertion failure: No mapping from charset index " + i + " to a Java character set");
        }
        CHARSET_NAME_TO_COLLATION_INDEX = Collections.unmodifiableMap(charsetNameToCollationIndexMap);
        UTF8MB4_INDEXES = Collections.unmodifiableSet(tempUTF8MB4Indexes);
        HashMap<String, String> tempMap = new HashMap<String, String>();
        tempMap.put(MYSQL_4_0_CHARSET_NAME_czech, MYSQL_CHARSET_NAME_latin2);
        tempMap.put(MYSQL_4_0_CHARSET_NAME_danish, "latin1");
        tempMap.put("dutch", "latin1");
        tempMap.put("english", "latin1");
        tempMap.put("estonian", MYSQL_CHARSET_NAME_latin7);
        tempMap.put("french", "latin1");
        tempMap.put("german", "latin1");
        tempMap.put(MYSQL_CHARSET_NAME_greek, MYSQL_CHARSET_NAME_greek);
        tempMap.put(MYSQL_4_0_CHARSET_NAME_hungarian, MYSQL_CHARSET_NAME_latin2);
        tempMap.put("italian", "latin1");
        tempMap.put("japanese", MYSQL_CHARSET_NAME_ujis);
        tempMap.put("japanese-sjis", MYSQL_CHARSET_NAME_sjis);
        tempMap.put("korean", MYSQL_CHARSET_NAME_euckr);
        tempMap.put("norwegian", "latin1");
        tempMap.put("norwegian-ny", "latin1");
        tempMap.put("polish", MYSQL_CHARSET_NAME_latin2);
        tempMap.put("portuguese", "latin1");
        tempMap.put("romanian", MYSQL_CHARSET_NAME_latin2);
        tempMap.put("russian", MYSQL_CHARSET_NAME_koi8r);
        tempMap.put("serbian", MYSQL_CHARSET_NAME_cp1250);
        tempMap.put("slovak", MYSQL_CHARSET_NAME_latin2);
        tempMap.put("spanish", "latin1");
        tempMap.put("swedish", "latin1");
        tempMap.put("ukrainian", MYSQL_CHARSET_NAME_koi8u);
        ERROR_MESSAGE_FILE_TO_MYSQL_CHARSET = Collections.unmodifiableMap(tempMap);
    }
}


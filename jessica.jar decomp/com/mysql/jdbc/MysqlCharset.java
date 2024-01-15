/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Connection;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

class MysqlCharset {
    public final String charsetName;
    public final int mblen;
    public final int priority;
    public final List<String> javaEncodingsUc = new ArrayList<String>();
    public int major = 4;
    public int minor = 1;
    public int subminor = 0;

    public MysqlCharset(String charsetName, int mblen, int priority, String[] javaEncodings) {
        this.charsetName = charsetName;
        this.mblen = mblen;
        this.priority = priority;
        for (int i = 0; i < javaEncodings.length; ++i) {
            String encoding = javaEncodings[i];
            try {
                Charset cs = Charset.forName(encoding);
                this.addEncodingMapping(cs.name());
                Set<String> als = cs.aliases();
                Iterator<String> ali = als.iterator();
                while (ali.hasNext()) {
                    this.addEncodingMapping(ali.next());
                }
                continue;
            }
            catch (Exception e) {
                if (mblen != 1) continue;
                this.addEncodingMapping(encoding);
            }
        }
        if (this.javaEncodingsUc.size() == 0) {
            if (mblen > 1) {
                this.addEncodingMapping("UTF-8");
            } else {
                this.addEncodingMapping("Cp1252");
            }
        }
    }

    private void addEncodingMapping(String encoding) {
        String encodingUc = encoding.toUpperCase(Locale.ENGLISH);
        if (!this.javaEncodingsUc.contains(encodingUc)) {
            this.javaEncodingsUc.add(encodingUc);
        }
    }

    public MysqlCharset(String charsetName, int mblen, int priority, String[] javaEncodings, int major, int minor) {
        this(charsetName, mblen, priority, javaEncodings);
        this.major = major;
        this.minor = minor;
    }

    public MysqlCharset(String charsetName, int mblen, int priority, String[] javaEncodings, int major, int minor, int subminor) {
        this(charsetName, mblen, priority, javaEncodings);
        this.major = major;
        this.minor = minor;
        this.subminor = subminor;
    }

    public String toString() {
        StringBuilder asString = new StringBuilder();
        asString.append("[");
        asString.append("charsetName=");
        asString.append(this.charsetName);
        asString.append(",mblen=");
        asString.append(this.mblen);
        asString.append("]");
        return asString.toString();
    }

    boolean isOkayForVersion(Connection conn) throws SQLException {
        return conn.versionMeetsMinimum(this.major, this.minor, this.subminor);
    }

    String getMatchingJavaEncoding(String javaEncoding) {
        if (javaEncoding != null && this.javaEncodingsUc.contains(javaEncoding.toUpperCase(Locale.ENGLISH))) {
            return javaEncoding;
        }
        return this.javaEncodingsUc.get(0);
    }
}


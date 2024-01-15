/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.ftp.parser;

import java.text.ParseException;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.parser.ConfigurableFTPFileEntryParserImpl;

public class NTFTPEntryParser
extends ConfigurableFTPFileEntryParserImpl {
    private static final String DEFAULT_DATE_FORMAT = "MM-dd-yy hh:mma";
    private static final String REGEX = "(\\S+)\\s+(\\S+)\\s+(<DIR>)?\\s*([0-9]+)?\\s+(\\S.*)";

    public NTFTPEntryParser() {
        this((FTPClientConfig)null);
    }

    public NTFTPEntryParser(FTPClientConfig config) {
        super(REGEX);
        this.configure(config);
    }

    public FTPFile parseFTPEntry(String entry) {
        FTPFile f = new FTPFile();
        f.setRawListing(entry);
        if (this.matches(entry)) {
            String datestr = this.group(1) + " " + this.group(2);
            String dirString = this.group(3);
            String size = this.group(4);
            String name = this.group(5);
            try {
                f.setTimestamp(super.parseTimestamp(datestr));
            }
            catch (ParseException e) {
                return null;
            }
            if (null == name || name.equals(".") || name.equals("..")) {
                return null;
            }
            f.setName(name);
            if ("<DIR>".equals(dirString)) {
                f.setType(1);
                f.setSize(0L);
            } else {
                f.setType(0);
                if (null != size) {
                    f.setSize(Long.parseLong(size));
                }
            }
            return f;
        }
        return null;
    }

    public FTPClientConfig getDefaultConfiguration() {
        return new FTPClientConfig("WINDOWS", DEFAULT_DATE_FORMAT, null, null, null, null);
    }
}


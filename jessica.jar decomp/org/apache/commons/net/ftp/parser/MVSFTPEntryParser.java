/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.ftp.parser;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.parser.ConfigurableFTPFileEntryParserImpl;

public class MVSFTPEntryParser
extends ConfigurableFTPFileEntryParserImpl {
    private static final String REGEX = "(.*)\\s+([^\\s]+)\\s*";
    static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd";

    public MVSFTPEntryParser() {
        super(REGEX);
    }

    public FTPFile parseFTPEntry(String entry) {
        FTPFile f = null;
        if (this.matches(entry)) {
            f = new FTPFile();
            String dataSetName = this.group(2);
            f.setType(0);
            f.setName(dataSetName);
            return f;
        }
        return null;
    }

    protected FTPClientConfig getDefaultConfiguration() {
        return new FTPClientConfig("MVS", DEFAULT_DATE_FORMAT, null, null, null, null);
    }
}


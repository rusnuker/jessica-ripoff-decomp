/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.ftp.parser;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPFileEntryParserImpl;

public class CompositeFileEntryParser
extends FTPFileEntryParserImpl {
    private final FTPFileEntryParser[] ftpFileEntryParsers;
    private FTPFileEntryParser cachedFtpFileEntryParser = null;

    public CompositeFileEntryParser(FTPFileEntryParser[] ftpFileEntryParsers) {
        this.ftpFileEntryParsers = ftpFileEntryParsers;
    }

    public FTPFile parseFTPEntry(String listEntry) {
        if (this.cachedFtpFileEntryParser != null) {
            FTPFile matched = this.cachedFtpFileEntryParser.parseFTPEntry(listEntry);
            if (matched != null) {
                return matched;
            }
        } else {
            for (int iterParser = 0; iterParser < this.ftpFileEntryParsers.length; ++iterParser) {
                FTPFileEntryParser ftpFileEntryParser = this.ftpFileEntryParsers[iterParser];
                FTPFile matched = ftpFileEntryParser.parseFTPEntry(listEntry);
                if (matched == null) continue;
                this.cachedFtpFileEntryParser = ftpFileEntryParser;
                return matched;
            }
        }
        return null;
    }
}


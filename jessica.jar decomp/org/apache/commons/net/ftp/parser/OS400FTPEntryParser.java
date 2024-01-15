/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.ftp.parser;

import java.text.ParseException;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.parser.ConfigurableFTPFileEntryParserImpl;

public class OS400FTPEntryParser
extends ConfigurableFTPFileEntryParserImpl {
    private static final String DEFAULT_DATE_FORMAT = "yy/MM/dd HH:mm:ss";
    private static final String REGEX = "(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\*\\S+)\\s+(\\S+/?)\\s*";

    public OS400FTPEntryParser() {
        this((FTPClientConfig)null);
    }

    public OS400FTPEntryParser(FTPClientConfig config) {
        super(REGEX);
        this.configure(config);
    }

    public FTPFile parseFTPEntry(String entry) {
        FTPFile file = new FTPFile();
        file.setRawListing(entry);
        if (this.matches(entry)) {
            int pos;
            String usr = this.group(1);
            String filesize = this.group(2);
            String datestr = this.group(3) + " " + this.group(4);
            String typeStr = this.group(5);
            String name = this.group(6);
            try {
                file.setTimestamp(super.parseTimestamp(datestr));
            }
            catch (ParseException e) {
                return null;
            }
            int type = typeStr.equalsIgnoreCase("*STMF") ? 0 : (typeStr.equalsIgnoreCase("*DIR") ? 1 : 3);
            file.setType(type);
            file.setUser(usr);
            try {
                file.setSize(Long.parseLong(filesize));
            }
            catch (NumberFormatException e) {
                // empty catch block
            }
            if (name.endsWith("/")) {
                name = name.substring(0, name.length() - 1);
            }
            if ((pos = name.lastIndexOf(47)) > -1) {
                name = name.substring(pos + 1);
            }
            file.setName(name);
            return file;
        }
        return null;
    }

    protected FTPClientConfig getDefaultConfiguration() {
        return new FTPClientConfig("OS/400", DEFAULT_DATE_FORMAT, null, null, null, null);
    }
}


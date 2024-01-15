/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.ftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPFileList;
import org.apache.commons.net.ftp.FTPFileListParser;

public abstract class FTPFileEntryParserImpl
implements FTPFileEntryParser,
FTPFileListParser {
    public FTPFile[] parseFileList(InputStream listStream, String encoding) throws IOException {
        FTPFileList ffl = FTPFileList.create(listStream, this, encoding);
        return ffl.getFiles();
    }

    public FTPFile[] parseFileList(InputStream listStream) throws IOException {
        return this.parseFileList(listStream, null);
    }

    public String readNextEntry(BufferedReader reader) throws IOException {
        return reader.readLine();
    }

    public List preParse(List original) {
        String entry;
        Iterator it = original.iterator();
        while (it.hasNext() && null == this.parseFTPEntry(entry = (String)it.next())) {
            it.remove();
        }
        return original;
    }
}


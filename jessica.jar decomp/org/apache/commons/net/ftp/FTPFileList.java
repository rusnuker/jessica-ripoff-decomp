/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.ftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPFileIterator;

public class FTPFileList {
    private LinkedList lines = null;
    private FTPFileEntryParser parser;
    private static final int EMPTY_DIR = -2;

    private FTPFileList(FTPFileEntryParser parser, String encoding) {
        this.parser = parser;
        this.lines = new LinkedList();
    }

    public static FTPFileList create(InputStream stream, FTPFileEntryParser parser, String encoding) throws IOException {
        FTPFileList list = new FTPFileList(parser, encoding);
        list.readStream(stream, encoding);
        parser.preParse(list.lines);
        return list;
    }

    public static FTPFileList create(InputStream stream, FTPFileEntryParser parser) throws IOException {
        return FTPFileList.create(stream, parser, null);
    }

    public void readStream(InputStream stream, String encoding) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, encoding));
        String line = this.parser.readNextEntry(reader);
        while (line != null) {
            this.lines.add(line);
            line = this.parser.readNextEntry(reader);
        }
        reader.close();
    }

    public void readStream(InputStream stream) throws IOException {
        this.readStream(stream, null);
    }

    FTPFileEntryParser getParser() {
        return this.parser;
    }

    List getLines() {
        return this.lines;
    }

    public FTPFileIterator iterator() {
        return new FTPFileIterator(this);
    }

    public FTPFileIterator iterator(FTPFileEntryParser parser) {
        return new FTPFileIterator(this, parser);
    }

    public FTPFile[] getFiles() {
        return this.iterator().getFiles();
    }
}


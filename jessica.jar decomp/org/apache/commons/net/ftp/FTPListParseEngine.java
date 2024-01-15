/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.ftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

public class FTPListParseEngine {
    private List entries = new LinkedList();
    private ListIterator _internalIterator = this.entries.listIterator();
    FTPFileEntryParser parser = null;

    public FTPListParseEngine(FTPFileEntryParser parser) {
        this.parser = parser;
    }

    public void readServerList(InputStream stream, String encoding) throws IOException {
        this.entries = new LinkedList();
        this.readStream(stream, encoding);
        this.parser.preParse(this.entries);
        this.resetIterator();
    }

    public void readServerList(InputStream stream) throws IOException {
        this.readServerList(stream, null);
    }

    private void readStream(InputStream stream, String encoding) throws IOException {
        BufferedReader reader = encoding == null ? new BufferedReader(new InputStreamReader(stream)) : new BufferedReader(new InputStreamReader(stream, encoding));
        String line = this.parser.readNextEntry(reader);
        while (line != null) {
            this.entries.add(line);
            line = this.parser.readNextEntry(reader);
        }
        reader.close();
    }

    public FTPFile[] getNext(int quantityRequested) {
        LinkedList<FTPFile> tmpResults = new LinkedList<FTPFile>();
        for (int count = quantityRequested; count > 0 && this._internalIterator.hasNext(); --count) {
            String entry = (String)this._internalIterator.next();
            FTPFile temp = this.parser.parseFTPEntry(entry);
            tmpResults.add(temp);
        }
        return tmpResults.toArray(new FTPFile[0]);
    }

    public FTPFile[] getPrevious(int quantityRequested) {
        LinkedList<FTPFile> tmpResults = new LinkedList<FTPFile>();
        for (int count = quantityRequested; count > 0 && this._internalIterator.hasPrevious(); --count) {
            String entry = (String)this._internalIterator.previous();
            FTPFile temp = this.parser.parseFTPEntry(entry);
            tmpResults.add(0, temp);
        }
        return tmpResults.toArray(new FTPFile[0]);
    }

    public FTPFile[] getFiles() throws IOException {
        LinkedList<FTPFile> tmpResults = new LinkedList<FTPFile>();
        Iterator iter = this.entries.iterator();
        while (iter.hasNext()) {
            String entry = (String)iter.next();
            FTPFile temp = this.parser.parseFTPEntry(entry);
            tmpResults.add(temp);
        }
        return tmpResults.toArray(new FTPFile[0]);
    }

    public boolean hasNext() {
        return this._internalIterator.hasNext();
    }

    public boolean hasPrevious() {
        return this._internalIterator.hasPrevious();
    }

    public void resetIterator() {
        this._internalIterator = this.entries.listIterator();
    }
}


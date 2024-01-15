/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.ftp.parser;

import org.apache.commons.net.ftp.Configurable;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.parser.CompositeFileEntryParser;
import org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory;
import org.apache.commons.net.ftp.parser.MVSFTPEntryParser;
import org.apache.commons.net.ftp.parser.NTFTPEntryParser;
import org.apache.commons.net.ftp.parser.OS2FTPEntryParser;
import org.apache.commons.net.ftp.parser.OS400FTPEntryParser;
import org.apache.commons.net.ftp.parser.ParserInitializationException;
import org.apache.commons.net.ftp.parser.UnixFTPEntryParser;
import org.apache.commons.net.ftp.parser.VMSVersioningFTPEntryParser;

public class DefaultFTPFileEntryParserFactory
implements FTPFileEntryParserFactory {
    private FTPClientConfig config = null;

    public FTPFileEntryParser createFileEntryParser(String key) {
        Class<?> parserClass = null;
        FTPFileEntryParser parser = null;
        try {
            parserClass = Class.forName(key);
            parser = (FTPFileEntryParser)parserClass.newInstance();
        }
        catch (ClassNotFoundException e) {
            String ukey = null;
            if (null != key) {
                ukey = key.toUpperCase();
            }
            if (ukey.indexOf("UNIX") >= 0) {
                parser = this.createUnixFTPEntryParser();
            }
            if (ukey.indexOf("VMS") >= 0) {
                parser = this.createVMSVersioningFTPEntryParser();
            }
            if (ukey.indexOf("WINDOWS") >= 0) {
                parser = this.createNTFTPEntryParser();
            }
            if (ukey.indexOf("OS/2") >= 0) {
                parser = this.createOS2FTPEntryParser();
            }
            if (ukey.indexOf("OS/400") >= 0) {
                parser = this.createOS400FTPEntryParser();
            }
            if (ukey.indexOf("MVS") >= 0) {
                parser = this.createMVSEntryParser();
            }
            throw new ParserInitializationException("Unknown parser type: " + key);
        }
        catch (ClassCastException e) {
            throw new ParserInitializationException(parserClass.getName() + " does not implement the interface " + "org.apache.commons.net.ftp.FTPFileEntryParser.", e);
        }
        catch (Throwable e) {
            throw new ParserInitializationException("Error initializing parser", e);
        }
        if (parser instanceof Configurable) {
            ((Configurable)((Object)parser)).configure(this.config);
        }
        return parser;
    }

    public FTPFileEntryParser createFileEntryParser(FTPClientConfig config) throws ParserInitializationException {
        this.config = config;
        String key = config.getServerSystemKey();
        return this.createFileEntryParser(key);
    }

    public FTPFileEntryParser createUnixFTPEntryParser() {
        return new UnixFTPEntryParser();
    }

    public FTPFileEntryParser createVMSVersioningFTPEntryParser() {
        return new VMSVersioningFTPEntryParser();
    }

    public FTPFileEntryParser createNTFTPEntryParser() {
        if (this.config != null && "WINDOWS".equals(this.config.getServerSystemKey())) {
            return new NTFTPEntryParser();
        }
        return new CompositeFileEntryParser(new FTPFileEntryParser[]{new NTFTPEntryParser(), new UnixFTPEntryParser()});
    }

    public FTPFileEntryParser createOS2FTPEntryParser() {
        return new OS2FTPEntryParser();
    }

    public FTPFileEntryParser createOS400FTPEntryParser() {
        if (this.config != null && "OS/400".equals(this.config.getServerSystemKey())) {
            return new OS400FTPEntryParser();
        }
        return new CompositeFileEntryParser(new FTPFileEntryParser[]{new OS400FTPEntryParser(), new UnixFTPEntryParser()});
    }

    public FTPFileEntryParser createMVSEntryParser() {
        return new MVSFTPEntryParser();
    }
}


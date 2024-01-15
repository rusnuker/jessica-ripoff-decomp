/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.oro.text.regex.MalformedPatternException
 *  org.apache.oro.text.regex.MatchResult
 *  org.apache.oro.text.regex.Pattern
 *  org.apache.oro.text.regex.Perl5Compiler
 *  org.apache.oro.text.regex.Perl5Matcher
 */
package org.apache.commons.net.ftp.parser;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.parser.VMSFTPEntryParser;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

public class VMSVersioningFTPEntryParser
extends VMSFTPEntryParser {
    private Perl5Matcher _preparse_matcher_;
    private Pattern _preparse_pattern_;
    private static final String PRE_PARSE_REGEX = "(.*);([0-9]+)\\s*.*";

    public VMSVersioningFTPEntryParser() {
        this((FTPClientConfig)null);
    }

    public VMSVersioningFTPEntryParser(FTPClientConfig config) {
        this.configure(config);
        try {
            this._preparse_matcher_ = new Perl5Matcher();
            this._preparse_pattern_ = new Perl5Compiler().compile(PRE_PARSE_REGEX);
        }
        catch (MalformedPatternException e) {
            throw new IllegalArgumentException("Unparseable regex supplied:  (.*);([0-9]+)\\s*.*");
        }
    }

    public List preParse(List original) {
        NameVersion existing;
        NameVersion nv;
        String version;
        String name;
        MatchResult result;
        String entry;
        original = super.preParse(original);
        HashMap<String, NameVersion> existingEntries = new HashMap<String, NameVersion>();
        ListIterator iter = original.listIterator();
        while (iter.hasNext()) {
            entry = ((String)iter.next()).trim();
            result = null;
            if (!this._preparse_matcher_.matches(entry, this._preparse_pattern_)) continue;
            result = this._preparse_matcher_.getMatch();
            name = result.group(1);
            version = result.group(2);
            nv = new NameVersion(name, version);
            existing = (NameVersion)existingEntries.get(name);
            if (null != existing && nv.versionNumber < existing.versionNumber) {
                iter.remove();
                continue;
            }
            existingEntries.put(name, nv);
        }
        while (iter.hasPrevious()) {
            entry = ((String)iter.previous()).trim();
            result = null;
            if (!this._preparse_matcher_.matches(entry, this._preparse_pattern_)) continue;
            result = this._preparse_matcher_.getMatch();
            name = result.group(1);
            version = result.group(2);
            nv = new NameVersion(name, version);
            existing = (NameVersion)existingEntries.get(name);
            if (null == existing || nv.versionNumber >= existing.versionNumber) continue;
            iter.remove();
        }
        return original;
    }

    protected boolean isVersioning() {
        return true;
    }

    private class NameVersion {
        String name;
        int versionNumber;

        NameVersion(String name, String vers) {
            this.name = name;
            this.versionNumber = Integer.parseInt(vers);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.oro.text.regex.MalformedPatternException
 *  org.apache.oro.text.regex.MatchResult
 *  org.apache.oro.text.regex.Pattern
 *  org.apache.oro.text.regex.PatternMatcher
 *  org.apache.oro.text.regex.Perl5Compiler
 *  org.apache.oro.text.regex.Perl5Matcher
 */
package org.apache.commons.net.ftp.parser;

import org.apache.commons.net.ftp.FTPFileEntryParserImpl;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

public abstract class RegexFTPFileEntryParserImpl
extends FTPFileEntryParserImpl {
    private Pattern pattern = null;
    private MatchResult result = null;
    protected PatternMatcher _matcher_ = null;

    public RegexFTPFileEntryParserImpl(String regex) {
        try {
            this._matcher_ = new Perl5Matcher();
            this.pattern = new Perl5Compiler().compile(regex);
        }
        catch (MalformedPatternException e) {
            throw new IllegalArgumentException("Unparseable regex supplied:  " + regex);
        }
    }

    public boolean matches(String s) {
        this.result = null;
        if (this._matcher_.matches(s.trim(), this.pattern)) {
            this.result = this._matcher_.getMatch();
        }
        return null != this.result;
    }

    public int getGroupCnt() {
        if (this.result == null) {
            return 0;
        }
        return this.result.groups();
    }

    public String group(int matchnum) {
        if (this.result == null) {
            return null;
        }
        return this.result.group(matchnum);
    }

    public String getGroupsAsString() {
        StringBuffer b = new StringBuffer();
        for (int i = 1; i <= this.result.groups(); ++i) {
            b.append(i).append(") ").append(this.result.group(i)).append(System.getProperty("line.separator"));
        }
        return b.toString();
    }
}


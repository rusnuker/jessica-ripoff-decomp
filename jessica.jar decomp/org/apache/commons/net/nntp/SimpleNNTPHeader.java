/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.nntp;

public class SimpleNNTPHeader {
    private String __subject;
    private String __from;
    private StringBuffer __newsgroups;
    private StringBuffer __headerFields;
    private int __newsgroupCount;

    public SimpleNNTPHeader(String from, String subject) {
        this.__from = from;
        this.__subject = subject;
        this.__newsgroups = new StringBuffer();
        this.__headerFields = new StringBuffer();
        this.__newsgroupCount = 0;
    }

    public void addNewsgroup(String newsgroup) {
        if (this.__newsgroupCount++ > 0) {
            this.__newsgroups.append(',');
        }
        this.__newsgroups.append(newsgroup);
    }

    public void addHeaderField(String headerField, String value) {
        this.__headerFields.append(headerField);
        this.__headerFields.append(": ");
        this.__headerFields.append(value);
        this.__headerFields.append('\n');
    }

    public String getFromAddress() {
        return this.__from;
    }

    public String getSubject() {
        return this.__subject;
    }

    public String getNewsgroups() {
        return this.__newsgroups.toString();
    }

    public String toString() {
        StringBuffer header = new StringBuffer();
        header.append("From: ");
        header.append(this.__from);
        header.append("\nNewsgroups: ");
        header.append(this.__newsgroups.toString());
        header.append("\nSubject: ");
        header.append(this.__subject);
        header.append('\n');
        if (this.__headerFields.length() > 0) {
            header.append(this.__headerFields.toString());
        }
        header.append('\n');
        return header.toString();
    }
}


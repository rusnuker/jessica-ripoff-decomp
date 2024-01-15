/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.ftp.parser;

public class ParserInitializationException
extends RuntimeException {
    private final Throwable rootCause;

    public ParserInitializationException(String message) {
        super(message);
        this.rootCause = null;
    }

    public ParserInitializationException(String message, Throwable rootCause) {
        super(message);
        this.rootCause = rootCause;
    }

    public Throwable getRootCause() {
        return this.rootCause;
    }
}


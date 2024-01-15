/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.exceptions;

import com.mysql.jdbc.exceptions.MySQLTransientException;

public class MySQLTimeoutException
extends MySQLTransientException {
    static final long serialVersionUID = -789621240523230339L;

    public MySQLTimeoutException(String reason, String SQLState, int vendorCode) {
        super(reason, SQLState, vendorCode);
    }

    public MySQLTimeoutException(String reason, String SQLState) {
        super(reason, SQLState);
    }

    public MySQLTimeoutException(String reason) {
        super(reason);
    }

    public MySQLTimeoutException() {
        super("Statement cancelled due to timeout or client request");
    }
}


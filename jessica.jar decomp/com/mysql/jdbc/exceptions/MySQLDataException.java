/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.exceptions;

import com.mysql.jdbc.exceptions.MySQLNonTransientException;

public class MySQLDataException
extends MySQLNonTransientException {
    static final long serialVersionUID = 4317904269797988676L;

    public MySQLDataException() {
    }

    public MySQLDataException(String reason, String SQLState, int vendorCode) {
        super(reason, SQLState, vendorCode);
    }

    public MySQLDataException(String reason, String SQLState) {
        super(reason, SQLState);
    }

    public MySQLDataException(String reason) {
        super(reason);
    }
}


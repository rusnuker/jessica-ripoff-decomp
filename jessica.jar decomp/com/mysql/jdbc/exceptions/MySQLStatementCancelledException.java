/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.exceptions;

import com.mysql.jdbc.exceptions.MySQLNonTransientException;

public class MySQLStatementCancelledException
extends MySQLNonTransientException {
    static final long serialVersionUID = -8762717748377197378L;

    public MySQLStatementCancelledException(String reason, String SQLState, int vendorCode) {
        super(reason, SQLState, vendorCode);
    }

    public MySQLStatementCancelledException(String reason, String SQLState) {
        super(reason, SQLState);
    }

    public MySQLStatementCancelledException(String reason) {
        super(reason);
    }

    public MySQLStatementCancelledException() {
        super("Statement cancelled due to client request");
    }
}


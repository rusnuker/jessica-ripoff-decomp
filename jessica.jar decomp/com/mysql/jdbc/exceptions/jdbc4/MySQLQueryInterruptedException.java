/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.exceptions.jdbc4;

import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientException;

public class MySQLQueryInterruptedException
extends MySQLNonTransientException {
    private static final long serialVersionUID = -8714521137662613517L;

    public MySQLQueryInterruptedException() {
    }

    public MySQLQueryInterruptedException(String reason, String SQLState, int vendorCode) {
        super(reason, SQLState, vendorCode);
    }

    public MySQLQueryInterruptedException(String reason, String SQLState) {
        super(reason, SQLState);
    }

    public MySQLQueryInterruptedException(String reason) {
        super(reason);
    }
}


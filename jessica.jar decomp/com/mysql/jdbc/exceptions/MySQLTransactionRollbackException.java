/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.exceptions;

import com.mysql.jdbc.exceptions.DeadlockTimeoutRollbackMarker;
import com.mysql.jdbc.exceptions.MySQLTransientException;

public class MySQLTransactionRollbackException
extends MySQLTransientException
implements DeadlockTimeoutRollbackMarker {
    static final long serialVersionUID = 6034999468737801730L;

    public MySQLTransactionRollbackException(String reason, String SQLState, int vendorCode) {
        super(reason, SQLState, vendorCode);
    }

    public MySQLTransactionRollbackException(String reason, String SQLState) {
        super(reason, SQLState);
    }

    public MySQLTransactionRollbackException(String reason) {
        super(reason);
    }

    public MySQLTransactionRollbackException() {
    }
}


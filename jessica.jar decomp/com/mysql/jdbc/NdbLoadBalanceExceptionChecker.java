/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.StandardLoadBalanceExceptionChecker;
import java.sql.SQLException;

public class NdbLoadBalanceExceptionChecker
extends StandardLoadBalanceExceptionChecker {
    public boolean shouldExceptionTriggerFailover(SQLException ex) {
        return super.shouldExceptionTriggerFailover(ex) || this.checkNdbException(ex);
    }

    private boolean checkNdbException(SQLException ex) {
        return ex.getMessage().startsWith("Lock wait timeout exceeded") || ex.getMessage().startsWith("Got temporary error") && ex.getMessage().endsWith("from NDB");
    }
}


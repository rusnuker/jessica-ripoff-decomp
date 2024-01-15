/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Extension;
import java.sql.SQLException;

public interface LoadBalanceExceptionChecker
extends Extension {
    public boolean shouldExceptionTriggerFailover(SQLException var1);
}


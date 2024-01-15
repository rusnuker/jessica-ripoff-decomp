/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jboss.resource.adapter.jdbc.vendor.MySQLExceptionSorter
 */
package com.mysql.jdbc.integration.jboss;

import java.sql.SQLException;
import org.jboss.resource.adapter.jdbc.vendor.MySQLExceptionSorter;

public final class ExtendedMysqlExceptionSorter
extends MySQLExceptionSorter {
    static final long serialVersionUID = -2454582336945931069L;

    public boolean isExceptionFatal(SQLException ex) {
        String sqlState = ex.getSQLState();
        if (sqlState != null && sqlState.startsWith("08")) {
            return true;
        }
        return super.isExceptionFatal(ex);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jboss.resource.adapter.jdbc.ValidConnectionChecker
 */
package com.mysql.jdbc.integration.jboss;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.jboss.resource.adapter.jdbc.ValidConnectionChecker;

public final class MysqlValidConnectionChecker
implements ValidConnectionChecker,
Serializable {
    private static final long serialVersionUID = 8909421133577519177L;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public SQLException isValidConnection(Connection conn) {
        SQLException sQLException;
        Statement pingStatement = null;
        try {
            try {
                pingStatement = conn.createStatement();
                pingStatement.executeQuery("/* ping */ SELECT 1").close();
                sQLException = null;
                Object var6_5 = null;
                if (pingStatement == null) return sQLException;
            }
            catch (SQLException sqlEx) {
                SQLException sQLException2 = sqlEx;
                Object var6_6 = null;
                if (pingStatement == null) return sQLException2;
                try {
                    pingStatement.close();
                    return sQLException2;
                }
                catch (SQLException sqlEx2) {
                    // empty catch block
                }
                return sQLException2;
            }
        }
        catch (Throwable throwable) {
            Object var6_7 = null;
            if (pingStatement == null) throw throwable;
            try {}
            catch (SQLException sqlEx2) {
                throw throwable;
            }
            pingStatement.close();
            throw throwable;
        }
        try {}
        catch (SQLException sqlEx2) {
            // empty catch block
            return sQLException;
        }
        pingStatement.close();
        return sQLException;
    }
}


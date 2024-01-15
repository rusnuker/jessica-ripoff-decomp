/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.NonRegisteringReplicationDriver;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ReplicationDriver
extends NonRegisteringReplicationDriver
implements Driver {
    static {
        try {
            DriverManager.registerDriver(new NonRegisteringReplicationDriver());
        }
        catch (SQLException E) {
            throw new RuntimeException("Can't register driver!");
        }
    }
}


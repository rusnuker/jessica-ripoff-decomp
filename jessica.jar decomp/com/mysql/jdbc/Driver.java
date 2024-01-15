/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.NonRegisteringDriver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Driver
extends NonRegisteringDriver
implements java.sql.Driver {
    static {
        try {
            DriverManager.registerDriver(new Driver());
        }
        catch (SQLException E) {
            throw new RuntimeException("Can't register driver!");
        }
    }
}


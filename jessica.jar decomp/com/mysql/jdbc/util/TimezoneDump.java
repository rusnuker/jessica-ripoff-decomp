/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.util;

import com.mysql.jdbc.TimeUtil;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class TimezoneDump {
    private static final String DEFAULT_URL = "jdbc:mysql:///test";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void main(String[] args) throws Exception {
        String jdbcUrl = DEFAULT_URL;
        if (args.length == 1 && args[0] != null) {
            jdbcUrl = args[0];
        }
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        ResultSet rs = null;
        try {
            rs = DriverManager.getConnection(jdbcUrl).createStatement().executeQuery("SHOW VARIABLES LIKE 'timezone'");
            while (rs.next()) {
                String timezoneFromServer = rs.getString(2);
                System.out.println("MySQL timezone name: " + timezoneFromServer);
                String canonicalTimezone = TimeUtil.getCanonicalTimezone(timezoneFromServer, null);
                System.out.println("Java timezone name: " + canonicalTimezone);
            }
            Object var6_5 = null;
            if (rs == null) return;
        }
        catch (Throwable throwable) {
            Object var6_6 = null;
            if (rs == null) throw throwable;
            rs.close();
            throw throwable;
        }
        rs.close();
    }
}


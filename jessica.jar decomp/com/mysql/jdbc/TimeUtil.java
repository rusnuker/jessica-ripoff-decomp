/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.SQLError;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.TimeZone;

public class TimeUtil {
    static final TimeZone GMT_TIMEZONE;
    private static final TimeZone DEFAULT_TIMEZONE;
    private static final String TIME_ZONE_MAPPINGS_RESOURCE = "/com/mysql/jdbc/TimeZoneMapping.properties";
    private static Properties timeZoneMappings;
    protected static final Method systemNanoTimeMethod;

    public static boolean nanoTimeAvailable() {
        return systemNanoTimeMethod != null;
    }

    public static final TimeZone getDefaultTimeZone(boolean useCache) {
        return (TimeZone)(useCache ? DEFAULT_TIMEZONE.clone() : TimeZone.getDefault().clone());
    }

    public static long getCurrentTimeNanosOrMillis() {
        if (systemNanoTimeMethod != null) {
            try {
                return (Long)systemNanoTimeMethod.invoke(null, null);
            }
            catch (IllegalArgumentException e) {
            }
            catch (IllegalAccessException e) {
            }
            catch (InvocationTargetException invocationTargetException) {
                // empty catch block
            }
        }
        return System.currentTimeMillis();
    }

    public static Time changeTimezone(MySQLConnection conn, Calendar sessionCalendar, Calendar targetCalendar, Time t, TimeZone fromTz, TimeZone toTz, boolean rollForward) {
        if (conn != null) {
            if (conn.getUseTimezone() && !conn.getNoTimezoneConversionForTimeType()) {
                Calendar fromCal = Calendar.getInstance(fromTz);
                fromCal.setTime(t);
                int fromOffset = fromCal.get(15) + fromCal.get(16);
                Calendar toCal = Calendar.getInstance(toTz);
                toCal.setTime(t);
                int toOffset = toCal.get(15) + toCal.get(16);
                int offsetDiff = fromOffset - toOffset;
                long toTime = toCal.getTime().getTime();
                toTime = rollForward ? (toTime += (long)offsetDiff) : (toTime -= (long)offsetDiff);
                Time changedTime = new Time(toTime);
                return changedTime;
            }
            if (conn.getUseJDBCCompliantTimezoneShift() && targetCalendar != null) {
                Time adjustedTime = new Time(TimeUtil.jdbcCompliantZoneShift(sessionCalendar, targetCalendar, t));
                return adjustedTime;
            }
        }
        return t;
    }

    public static Timestamp changeTimezone(MySQLConnection conn, Calendar sessionCalendar, Calendar targetCalendar, Timestamp tstamp, TimeZone fromTz, TimeZone toTz, boolean rollForward) {
        if (conn != null) {
            if (conn.getUseTimezone()) {
                Calendar fromCal = Calendar.getInstance(fromTz);
                fromCal.setTime(tstamp);
                int fromOffset = fromCal.get(15) + fromCal.get(16);
                Calendar toCal = Calendar.getInstance(toTz);
                toCal.setTime(tstamp);
                int toOffset = toCal.get(15) + toCal.get(16);
                int offsetDiff = fromOffset - toOffset;
                long toTime = toCal.getTime().getTime();
                toTime = rollForward ? (toTime += (long)offsetDiff) : (toTime -= (long)offsetDiff);
                Timestamp changedTimestamp = new Timestamp(toTime);
                return changedTimestamp;
            }
            if (conn.getUseJDBCCompliantTimezoneShift() && targetCalendar != null) {
                Timestamp adjustedTimestamp = new Timestamp(TimeUtil.jdbcCompliantZoneShift(sessionCalendar, targetCalendar, tstamp));
                adjustedTimestamp.setNanos(tstamp.getNanos());
                return adjustedTimestamp;
            }
        }
        return tstamp;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static long jdbcCompliantZoneShift(Calendar sessionCalendar, Calendar targetCalendar, java.util.Date dt) {
        if (sessionCalendar == null) {
            sessionCalendar = new GregorianCalendar();
        }
        Calendar calendar = sessionCalendar;
        synchronized (calendar) {
            java.util.Date origCalDate = targetCalendar.getTime();
            java.util.Date origSessionDate = sessionCalendar.getTime();
            try {
                sessionCalendar.setTime(dt);
                targetCalendar.set(1, sessionCalendar.get(1));
                targetCalendar.set(2, sessionCalendar.get(2));
                targetCalendar.set(5, sessionCalendar.get(5));
                targetCalendar.set(11, sessionCalendar.get(11));
                targetCalendar.set(12, sessionCalendar.get(12));
                targetCalendar.set(13, sessionCalendar.get(13));
                targetCalendar.set(14, sessionCalendar.get(14));
                long l = targetCalendar.getTime().getTime();
                Object var9_7 = null;
                sessionCalendar.setTime(origSessionDate);
                targetCalendar.setTime(origCalDate);
                return l;
            }
            catch (Throwable throwable) {
                Object var9_8 = null;
                sessionCalendar.setTime(origSessionDate);
                targetCalendar.setTime(origCalDate);
                throw throwable;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static final Date fastDateCreate(boolean useGmtConversion, Calendar gmtCalIfNeeded, Calendar cal, int year, int month, int day) {
        Calendar dateCal = cal;
        if (useGmtConversion) {
            if (gmtCalIfNeeded == null) {
                gmtCalIfNeeded = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            }
            dateCal = gmtCalIfNeeded;
        }
        Calendar calendar = dateCal;
        synchronized (calendar) {
            java.util.Date origCalDate = dateCal.getTime();
            try {
                dateCal.clear();
                dateCal.set(14, 0);
                dateCal.set(year, month - 1, day, 0, 0, 0);
                long dateAsMillis = dateCal.getTimeInMillis();
                Date date = new Date(dateAsMillis);
                Object var13_11 = null;
                dateCal.setTime(origCalDate);
                return date;
            }
            catch (Throwable throwable) {
                Object var13_12 = null;
                dateCal.setTime(origCalDate);
                throw throwable;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static final Date fastDateCreate(int year, int month, int day, Calendar targetCalendar) {
        Calendar dateCal;
        Calendar calendar = dateCal = targetCalendar == null ? new GregorianCalendar() : targetCalendar;
        synchronized (calendar) {
            java.util.Date origCalDate = dateCal.getTime();
            try {
                dateCal.clear();
                dateCal.set(year, month - 1, day, 0, 0, 0);
                dateCal.set(14, 0);
                long dateAsMillis = dateCal.getTimeInMillis();
                Date date = new Date(dateAsMillis);
                Object var11_9 = null;
                dateCal.setTime(origCalDate);
                return date;
            }
            catch (Throwable throwable) {
                Object var11_10 = null;
                dateCal.setTime(origCalDate);
                throw throwable;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static final Time fastTimeCreate(Calendar cal, int hour, int minute, int second, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        if (hour < 0) throw SQLError.createSQLException("Illegal hour value '" + hour + "' for java.sql.Time type in value '" + TimeUtil.timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        if (hour > 24) {
            throw SQLError.createSQLException("Illegal hour value '" + hour + "' for java.sql.Time type in value '" + TimeUtil.timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        }
        if (minute < 0) throw SQLError.createSQLException("Illegal minute value '" + minute + "' for java.sql.Time type in value '" + TimeUtil.timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        if (minute > 59) {
            throw SQLError.createSQLException("Illegal minute value '" + minute + "' for java.sql.Time type in value '" + TimeUtil.timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        }
        if (second < 0) throw SQLError.createSQLException("Illegal minute value '" + second + "' for java.sql.Time type in value '" + TimeUtil.timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        if (second > 59) {
            throw SQLError.createSQLException("Illegal minute value '" + second + "' for java.sql.Time type in value '" + TimeUtil.timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        }
        Calendar calendar = cal;
        synchronized (calendar) {
            java.util.Date origCalDate = cal.getTime();
            try {
                cal.clear();
                cal.set(1970, 0, 1, hour, minute, second);
                long timeAsMillis = cal.getTimeInMillis();
                Time time = new Time(timeAsMillis);
                Object var11_9 = null;
                cal.setTime(origCalDate);
                return time;
            }
            catch (Throwable throwable) {
                Object var11_10 = null;
                cal.setTime(origCalDate);
                throw throwable;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static final Time fastTimeCreate(int hour, int minute, int second, Calendar targetCalendar, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        Calendar cal;
        if (hour < 0) throw SQLError.createSQLException("Illegal hour value '" + hour + "' for java.sql.Time type in value '" + TimeUtil.timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        if (hour > 23) {
            throw SQLError.createSQLException("Illegal hour value '" + hour + "' for java.sql.Time type in value '" + TimeUtil.timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        }
        if (minute < 0) throw SQLError.createSQLException("Illegal minute value '" + minute + "' for java.sql.Time type in value '" + TimeUtil.timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        if (minute > 59) {
            throw SQLError.createSQLException("Illegal minute value '" + minute + "' for java.sql.Time type in value '" + TimeUtil.timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        }
        if (second < 0) throw SQLError.createSQLException("Illegal minute value '" + second + "' for java.sql.Time type in value '" + TimeUtil.timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        if (second > 59) {
            throw SQLError.createSQLException("Illegal minute value '" + second + "' for java.sql.Time type in value '" + TimeUtil.timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        }
        Calendar calendar = cal = targetCalendar == null ? new GregorianCalendar() : targetCalendar;
        synchronized (calendar) {
            java.util.Date origCalDate = cal.getTime();
            try {
                cal.clear();
                cal.set(1970, 0, 1, hour, minute, second);
                long timeAsMillis = cal.getTimeInMillis();
                Time time = new Time(timeAsMillis);
                Object var12_10 = null;
                cal.setTime(origCalDate);
                return time;
            }
            catch (Throwable throwable) {
                Object var12_11 = null;
                cal.setTime(origCalDate);
                throw throwable;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static final Timestamp fastTimestampCreate(boolean useGmtConversion, Calendar gmtCalIfNeeded, Calendar cal, int year, int month, int day, int hour, int minute, int seconds, int secondsPart) {
        Calendar calendar = cal;
        synchronized (calendar) {
            java.util.Date origCalDate = cal.getTime();
            try {
                cal.clear();
                cal.set(year, month - 1, day, hour, minute, seconds);
                int offsetDiff = 0;
                if (useGmtConversion) {
                    int fromOffset = cal.get(15) + cal.get(16);
                    if (gmtCalIfNeeded == null) {
                        gmtCalIfNeeded = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                    }
                    gmtCalIfNeeded.clear();
                    gmtCalIfNeeded.setTimeInMillis(cal.getTimeInMillis());
                    int toOffset = gmtCalIfNeeded.get(15) + gmtCalIfNeeded.get(16);
                    offsetDiff = fromOffset - toOffset;
                }
                if (secondsPart != 0) {
                    cal.set(14, secondsPart / 1000000);
                }
                long tsAsMillis = cal.getTimeInMillis();
                Timestamp ts = new Timestamp(tsAsMillis + (long)offsetDiff);
                ts.setNanos(secondsPart);
                Timestamp timestamp = ts;
                Object var18_18 = null;
                cal.setTime(origCalDate);
                return timestamp;
            }
            catch (Throwable throwable) {
                Object var18_19 = null;
                cal.setTime(origCalDate);
                throw throwable;
            }
        }
    }

    static final Timestamp fastTimestampCreate(TimeZone tz, int year, int month, int day, int hour, int minute, int seconds, int secondsPart) {
        GregorianCalendar cal = tz == null ? new GregorianCalendar() : new GregorianCalendar(tz);
        cal.clear();
        cal.set(year, month - 1, day, hour, minute, seconds);
        long tsAsMillis = cal.getTimeInMillis();
        Timestamp ts = new Timestamp(tsAsMillis);
        ts.setNanos(secondsPart);
        return ts;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getCanonicalTimezone(String timezoneStr, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        if (timezoneStr == null) {
            return null;
        }
        if ((timezoneStr = timezoneStr.trim()).length() > 2 && (timezoneStr.charAt(0) == '+' || timezoneStr.charAt(0) == '-') && Character.isDigit(timezoneStr.charAt(1))) {
            return "GMT" + timezoneStr;
        }
        Class<TimeUtil> clazz = TimeUtil.class;
        synchronized (TimeUtil.class) {
            if (timeZoneMappings == null) {
                TimeUtil.loadTimeZoneMappings(exceptionInterceptor);
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            String canonicalTz = timeZoneMappings.getProperty(timezoneStr);
            if (canonicalTz != null) {
                return canonicalTz;
            }
            throw SQLError.createSQLException(Messages.getString("TimeUtil.UnrecognizedTimezoneId", new Object[]{timezoneStr}), "01S00", exceptionInterceptor);
        }
    }

    private static String timeFormattedString(int hours, int minutes, int seconds) {
        StringBuilder buf = new StringBuilder(8);
        if (hours < 10) {
            buf.append("0");
        }
        buf.append(hours);
        buf.append(":");
        if (minutes < 10) {
            buf.append("0");
        }
        buf.append(minutes);
        buf.append(":");
        if (seconds < 10) {
            buf.append("0");
        }
        buf.append(seconds);
        return buf.toString();
    }

    public static String formatNanos(int nanos, boolean serverSupportsFracSecs, boolean usingMicros) {
        if (nanos > 999999999) {
            nanos %= 100000000;
        }
        if (usingMicros) {
            nanos /= 1000;
        }
        if (!serverSupportsFracSecs || nanos == 0) {
            return "0";
        }
        int digitCount = usingMicros ? 6 : 9;
        String nanosString = Integer.toString(nanos);
        String zeroPadding = usingMicros ? "000000" : "000000000";
        nanosString = zeroPadding.substring(0, digitCount - nanosString.length()) + nanosString;
        int pos = digitCount - 1;
        while (nanosString.charAt(pos) == '0') {
            --pos;
        }
        nanosString = nanosString.substring(0, pos + 1);
        return nanosString;
    }

    private static void loadTimeZoneMappings(ExceptionInterceptor exceptionInterceptor) throws SQLException {
        timeZoneMappings = new Properties();
        try {
            timeZoneMappings.load(TimeUtil.class.getResourceAsStream(TIME_ZONE_MAPPINGS_RESOURCE));
        }
        catch (IOException e) {
            throw SQLError.createSQLException(Messages.getString("TimeUtil.LoadTimeZoneMappingError"), "01S00", exceptionInterceptor);
        }
        for (String tz : TimeZone.getAvailableIDs()) {
            if (timeZoneMappings.containsKey(tz)) continue;
            timeZoneMappings.put(tz, tz);
        }
    }

    public static Timestamp truncateFractionalSeconds(Timestamp timestamp) {
        Timestamp truncatedTimestamp = new Timestamp(timestamp.getTime());
        truncatedTimestamp.setNanos(0);
        return truncatedTimestamp;
    }

    static {
        Method aMethod;
        GMT_TIMEZONE = TimeZone.getTimeZone("GMT");
        DEFAULT_TIMEZONE = TimeZone.getDefault();
        timeZoneMappings = null;
        try {
            aMethod = System.class.getMethod("nanoTime", null);
        }
        catch (SecurityException e) {
            aMethod = null;
        }
        catch (NoSuchMethodException e) {
            aMethod = null;
        }
        systemNanoTimeMethod = aMethod;
    }
}


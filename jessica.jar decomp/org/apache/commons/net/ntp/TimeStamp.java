/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.ntp;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeStamp
implements Serializable,
Comparable {
    protected static final long msb0baseTime = 2085978496000L;
    protected static final long msb1baseTime = -2208988800000L;
    public static final String NTP_DATE_FORMAT = "EEE, MMM dd yyyy HH:mm:ss.SSS";
    private static SoftReference simpleFormatter = null;
    private static SoftReference utcFormatter = null;
    private long ntpTime;
    private static final long serialVersionUID = 8139806907588338737L;

    public TimeStamp(long ntpTime) {
        this.ntpTime = ntpTime;
    }

    public TimeStamp(String s) throws NumberFormatException {
        this.ntpTime = TimeStamp.decodeNtpHexString(s);
    }

    public TimeStamp(Date d) {
        this.ntpTime = d == null ? 0L : TimeStamp.toNtpTime(d.getTime());
    }

    public long ntpValue() {
        return this.ntpTime;
    }

    public long getSeconds() {
        return this.ntpTime >>> 32 & 0xFFFFFFFFL;
    }

    public long getFraction() {
        return this.ntpTime & 0xFFFFFFFFL;
    }

    public long getTime() {
        return TimeStamp.getTime(this.ntpTime);
    }

    public Date getDate() {
        long time = TimeStamp.getTime(this.ntpTime);
        return new Date(time);
    }

    public static long getTime(long ntpTimeValue) {
        long seconds = ntpTimeValue >>> 32 & 0xFFFFFFFFL;
        long fraction = ntpTimeValue & 0xFFFFFFFFL;
        fraction = Math.round(1000.0 * (double)fraction / 4.294967296E9);
        long msb = seconds & 0x80000000L;
        if (msb == 0L) {
            return 2085978496000L + seconds * 1000L + fraction;
        }
        return -2208988800000L + seconds * 1000L + fraction;
    }

    public static TimeStamp getNtpTime(long date) {
        return new TimeStamp(TimeStamp.toNtpTime(date));
    }

    public static TimeStamp getCurrentTime() {
        return TimeStamp.getNtpTime(System.currentTimeMillis());
    }

    protected static long decodeNtpHexString(String s) throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        }
        int ind = s.indexOf(46);
        if (ind == -1) {
            if (s.length() == 0) {
                return 0L;
            }
            return Long.parseLong(s, 16) << 32;
        }
        return Long.parseLong(s.substring(0, ind), 16) << 32 | Long.parseLong(s.substring(ind + 1), 16);
    }

    public static TimeStamp parseNtpString(String s) throws NumberFormatException {
        return new TimeStamp(TimeStamp.decodeNtpHexString(s));
    }

    protected static long toNtpTime(long t) {
        boolean useBase1 = t < 2085978496000L;
        long baseTime = useBase1 ? t - -2208988800000L : t - 2085978496000L;
        long seconds = baseTime / 1000L;
        long fraction = baseTime % 1000L * 0x100000000L / 1000L;
        if (useBase1) {
            seconds |= 0x80000000L;
        }
        long time = seconds << 32 | fraction;
        return time;
    }

    public int hashCode() {
        return (int)(this.ntpTime ^ this.ntpTime >>> 32);
    }

    public boolean equals(Object obj) {
        if (obj instanceof TimeStamp) {
            return this.ntpTime == ((TimeStamp)obj).ntpValue();
        }
        return false;
    }

    public String toString() {
        return TimeStamp.toString(this.ntpTime);
    }

    private static void appendHexString(StringBuffer buf, long l) {
        String s = Long.toHexString(l);
        for (int i = s.length(); i < 8; ++i) {
            buf.append('0');
        }
        buf.append(s);
    }

    public static String toString(long ntpTime) {
        StringBuffer buf = new StringBuffer();
        TimeStamp.appendHexString(buf, ntpTime >>> 32 & 0xFFFFFFFFL);
        buf.append('.');
        TimeStamp.appendHexString(buf, ntpTime & 0xFFFFFFFFL);
        return buf.toString();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String toDateString() {
        DateFormat formatter = null;
        if (simpleFormatter != null) {
            formatter = (DateFormat)simpleFormatter.get();
        }
        if (formatter == null) {
            formatter = new SimpleDateFormat(NTP_DATE_FORMAT, Locale.US);
            formatter.setTimeZone(TimeZone.getDefault());
            simpleFormatter = new SoftReference<DateFormat>(formatter);
        }
        Date ntpDate = this.getDate();
        DateFormat dateFormat = formatter;
        synchronized (dateFormat) {
            return formatter.format(ntpDate);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String toUTCString() {
        DateFormat formatter = null;
        if (utcFormatter != null) {
            formatter = (DateFormat)utcFormatter.get();
        }
        if (formatter == null) {
            formatter = new SimpleDateFormat("EEE, MMM dd yyyy HH:mm:ss.SSS 'UTC'", Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            utcFormatter = new SoftReference<DateFormat>(formatter);
        }
        Date ntpDate = this.getDate();
        DateFormat dateFormat = formatter;
        synchronized (dateFormat) {
            return formatter.format(ntpDate);
        }
    }

    public int compareTo(TimeStamp anotherTimeStamp) {
        long thisVal = this.ntpTime;
        long anotherVal = anotherTimeStamp.ntpTime;
        return thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1);
    }

    public int compareTo(Object o) {
        return this.compareTo((TimeStamp)o);
    }
}


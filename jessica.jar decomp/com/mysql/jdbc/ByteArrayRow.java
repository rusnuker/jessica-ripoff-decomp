/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.ResultSetImpl;
import com.mysql.jdbc.ResultSetRow;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

public class ByteArrayRow
extends ResultSetRow {
    byte[][] internalRowData;

    public ByteArrayRow(byte[][] internalRowData, ExceptionInterceptor exceptionInterceptor) {
        super(exceptionInterceptor);
        this.internalRowData = internalRowData;
    }

    public byte[] getColumnValue(int index) throws SQLException {
        return this.internalRowData[index];
    }

    public void setColumnValue(int index, byte[] value) throws SQLException {
        this.internalRowData[index] = value;
    }

    public String getString(int index, String encoding, MySQLConnection conn) throws SQLException {
        byte[] columnData = this.internalRowData[index];
        if (columnData == null) {
            return null;
        }
        return this.getString(encoding, conn, columnData, 0, columnData.length);
    }

    public boolean isNull(int index) throws SQLException {
        return this.internalRowData[index] == null;
    }

    public boolean isFloatingPointNumber(int index) throws SQLException {
        byte[] numAsBytes = this.internalRowData[index];
        if (this.internalRowData[index] == null || this.internalRowData[index].length == 0) {
            return false;
        }
        for (int i = 0; i < numAsBytes.length; ++i) {
            if ((char)numAsBytes[i] != 'e' && (char)numAsBytes[i] != 'E') continue;
            return true;
        }
        return false;
    }

    public long length(int index) throws SQLException {
        if (this.internalRowData[index] == null) {
            return 0L;
        }
        return this.internalRowData[index].length;
    }

    public int getInt(int columnIndex) {
        if (this.internalRowData[columnIndex] == null) {
            return 0;
        }
        return StringUtils.getInt(this.internalRowData[columnIndex]);
    }

    public long getLong(int columnIndex) {
        if (this.internalRowData[columnIndex] == null) {
            return 0L;
        }
        return StringUtils.getLong(this.internalRowData[columnIndex]);
    }

    public Timestamp getTimestampFast(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward, MySQLConnection conn, ResultSetImpl rs) throws SQLException {
        byte[] columnValue = this.internalRowData[columnIndex];
        if (columnValue == null) {
            return null;
        }
        return this.getTimestampFast(columnIndex, this.internalRowData[columnIndex], 0, columnValue.length, targetCalendar, tz, rollForward, conn, rs);
    }

    public double getNativeDouble(int columnIndex) throws SQLException {
        if (this.internalRowData[columnIndex] == null) {
            return 0.0;
        }
        return this.getNativeDouble(this.internalRowData[columnIndex], 0);
    }

    public float getNativeFloat(int columnIndex) throws SQLException {
        if (this.internalRowData[columnIndex] == null) {
            return 0.0f;
        }
        return this.getNativeFloat(this.internalRowData[columnIndex], 0);
    }

    public int getNativeInt(int columnIndex) throws SQLException {
        if (this.internalRowData[columnIndex] == null) {
            return 0;
        }
        return this.getNativeInt(this.internalRowData[columnIndex], 0);
    }

    public long getNativeLong(int columnIndex) throws SQLException {
        if (this.internalRowData[columnIndex] == null) {
            return 0L;
        }
        return this.getNativeLong(this.internalRowData[columnIndex], 0);
    }

    public short getNativeShort(int columnIndex) throws SQLException {
        if (this.internalRowData[columnIndex] == null) {
            return 0;
        }
        return this.getNativeShort(this.internalRowData[columnIndex], 0);
    }

    public Timestamp getNativeTimestamp(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward, MySQLConnection conn, ResultSetImpl rs) throws SQLException {
        byte[] bits = this.internalRowData[columnIndex];
        if (bits == null) {
            return null;
        }
        return this.getNativeTimestamp(bits, 0, bits.length, targetCalendar, tz, rollForward, conn, rs);
    }

    public void closeOpenStreams() {
    }

    public InputStream getBinaryInputStream(int columnIndex) throws SQLException {
        if (this.internalRowData[columnIndex] == null) {
            return null;
        }
        return new ByteArrayInputStream(this.internalRowData[columnIndex]);
    }

    public Reader getReader(int columnIndex) throws SQLException {
        InputStream stream = this.getBinaryInputStream(columnIndex);
        if (stream == null) {
            return null;
        }
        try {
            return new InputStreamReader(stream, this.metadata[columnIndex].getEncoding());
        }
        catch (UnsupportedEncodingException e) {
            SQLException sqlEx = SQLError.createSQLException("", this.exceptionInterceptor);
            sqlEx.initCause(e);
            throw sqlEx;
        }
    }

    public Time getTimeFast(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward, MySQLConnection conn, ResultSetImpl rs) throws SQLException {
        byte[] columnValue = this.internalRowData[columnIndex];
        if (columnValue == null) {
            return null;
        }
        return this.getTimeFast(columnIndex, this.internalRowData[columnIndex], 0, columnValue.length, targetCalendar, tz, rollForward, conn, rs);
    }

    public Date getDateFast(int columnIndex, MySQLConnection conn, ResultSetImpl rs, Calendar targetCalendar) throws SQLException {
        byte[] columnValue = this.internalRowData[columnIndex];
        if (columnValue == null) {
            return null;
        }
        return this.getDateFast(columnIndex, this.internalRowData[columnIndex], 0, columnValue.length, conn, rs, targetCalendar);
    }

    public Object getNativeDateTimeValue(int columnIndex, Calendar targetCalendar, int jdbcType, int mysqlType, TimeZone tz, boolean rollForward, MySQLConnection conn, ResultSetImpl rs) throws SQLException {
        byte[] columnValue = this.internalRowData[columnIndex];
        if (columnValue == null) {
            return null;
        }
        return this.getNativeDateTimeValue(columnIndex, columnValue, 0, columnValue.length, targetCalendar, jdbcType, mysqlType, tz, rollForward, conn, rs);
    }

    public Date getNativeDate(int columnIndex, MySQLConnection conn, ResultSetImpl rs, Calendar cal) throws SQLException {
        byte[] columnValue = this.internalRowData[columnIndex];
        if (columnValue == null) {
            return null;
        }
        return this.getNativeDate(columnIndex, columnValue, 0, columnValue.length, conn, rs, cal);
    }

    public Time getNativeTime(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward, MySQLConnection conn, ResultSetImpl rs) throws SQLException {
        byte[] columnValue = this.internalRowData[columnIndex];
        if (columnValue == null) {
            return null;
        }
        return this.getNativeTime(columnIndex, columnValue, 0, columnValue.length, targetCalendar, tz, rollForward, conn, rs);
    }

    public int getBytesSize() {
        if (this.internalRowData == null) {
            return 0;
        }
        int bytesSize = 0;
        for (int i = 0; i < this.internalRowData.length; ++i) {
            if (this.internalRowData[i] == null) continue;
            bytesSize += this.internalRowData[i].length;
        }
        return bytesSize;
    }
}


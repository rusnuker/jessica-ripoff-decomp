/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Field;
import com.mysql.jdbc.JDBC4ResultSet;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.NotUpdatable;
import com.mysql.jdbc.RowData;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.StatementImpl;
import java.sql.SQLException;
import java.sql.SQLType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeParseException;

public class JDBC42ResultSet
extends JDBC4ResultSet {
    public JDBC42ResultSet(long updateCount, long updateID, MySQLConnection conn, StatementImpl creatorStmt) {
        super(updateCount, updateID, conn, creatorStmt);
    }

    public JDBC42ResultSet(String catalog, Field[] fields, RowData tuples, MySQLConnection conn, StatementImpl creatorStmt) throws SQLException {
        super(catalog, fields, tuples, conn, creatorStmt);
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        if (type == null) {
            throw SQLError.createSQLException("Type parameter can not be null", "S1009", this.getExceptionInterceptor());
        }
        if (type.equals(LocalDate.class)) {
            return type.cast(this.getDate(columnIndex).toLocalDate());
        }
        if (type.equals(LocalDateTime.class)) {
            return type.cast(this.getTimestamp(columnIndex).toLocalDateTime());
        }
        if (type.equals(LocalTime.class)) {
            return type.cast(this.getTime(columnIndex).toLocalTime());
        }
        if (type.equals(OffsetDateTime.class)) {
            try {
                return type.cast(OffsetDateTime.parse(this.getString(columnIndex)));
            }
            catch (DateTimeParseException dateTimeParseException) {
            }
        } else if (type.equals(OffsetTime.class)) {
            try {
                return type.cast(OffsetTime.parse(this.getString(columnIndex)));
            }
            catch (DateTimeParseException dateTimeParseException) {
                // empty catch block
            }
        }
        return super.getObject(columnIndex, type);
    }

    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateObject(String columnLabel, Object x, SQLType targetSqlType) throws SQLException {
        throw new NotUpdatable();
    }

    @Override
    public void updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        throw new NotUpdatable();
    }
}


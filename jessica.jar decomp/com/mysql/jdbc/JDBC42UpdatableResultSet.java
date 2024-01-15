/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Field;
import com.mysql.jdbc.JDBC42Helper;
import com.mysql.jdbc.JDBC4UpdatableResultSet;
import com.mysql.jdbc.MySQLConnection;
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

public class JDBC42UpdatableResultSet
extends JDBC4UpdatableResultSet {
    public JDBC42UpdatableResultSet(String catalog, Field[] fields, RowData tuples, MySQLConnection conn, StatementImpl creatorStmt) throws SQLException {
        super(catalog, fields, tuples, conn, creatorStmt);
    }

    private int translateAndCheckSqlType(SQLType sqlType) throws SQLException {
        return JDBC42Helper.translateAndCheckSqlType(sqlType, this.getExceptionInterceptor());
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
    public synchronized void updateObject(int columnIndex, Object x) throws SQLException {
        super.updateObject(columnIndex, JDBC42Helper.convertJavaTimeToJavaSql(x));
    }

    @Override
    public synchronized void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        super.updateObject(columnIndex, JDBC42Helper.convertJavaTimeToJavaSql(x), scaleOrLength);
    }

    @Override
    public synchronized void updateObject(String columnLabel, Object x) throws SQLException {
        super.updateObject(columnLabel, JDBC42Helper.convertJavaTimeToJavaSql(x));
    }

    @Override
    public synchronized void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        super.updateObject(columnLabel, JDBC42Helper.convertJavaTimeToJavaSql(x), scaleOrLength);
    }

    @Override
    public synchronized void updateObject(int columnIndex, Object x, SQLType targetSqlType) throws SQLException {
        super.updateObjectInternal(columnIndex, JDBC42Helper.convertJavaTimeToJavaSql(x), this.translateAndCheckSqlType(targetSqlType), 0);
    }

    @Override
    public synchronized void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        super.updateObjectInternal(columnIndex, JDBC42Helper.convertJavaTimeToJavaSql(x), this.translateAndCheckSqlType(targetSqlType), scaleOrLength);
    }

    @Override
    public synchronized void updateObject(String columnLabel, Object x, SQLType targetSqlType) throws SQLException {
        super.updateObjectInternal(this.findColumn(columnLabel), JDBC42Helper.convertJavaTimeToJavaSql(x), this.translateAndCheckSqlType(targetSqlType), 0);
    }

    @Override
    public synchronized void updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        super.updateObjectInternal(this.findColumn(columnLabel), JDBC42Helper.convertJavaTimeToJavaSql(x), this.translateAndCheckSqlType(targetSqlType), scaleOrLength);
    }
}


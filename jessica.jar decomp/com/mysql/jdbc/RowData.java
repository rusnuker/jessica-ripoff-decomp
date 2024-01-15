/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Field;
import com.mysql.jdbc.ResultSetImpl;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.ResultSetRow;
import java.sql.SQLException;

public interface RowData {
    public static final int RESULT_SET_SIZE_UNKNOWN = -1;

    public void addRow(ResultSetRow var1) throws SQLException;

    public void afterLast() throws SQLException;

    public void beforeFirst() throws SQLException;

    public void beforeLast() throws SQLException;

    public void close() throws SQLException;

    public ResultSetRow getAt(int var1) throws SQLException;

    public int getCurrentRowNumber() throws SQLException;

    public ResultSetInternalMethods getOwner();

    public boolean hasNext() throws SQLException;

    public boolean isAfterLast() throws SQLException;

    public boolean isBeforeFirst() throws SQLException;

    public boolean isDynamic() throws SQLException;

    public boolean isEmpty() throws SQLException;

    public boolean isFirst() throws SQLException;

    public boolean isLast() throws SQLException;

    public void moveRowRelative(int var1) throws SQLException;

    public ResultSetRow next() throws SQLException;

    public void removeRow(int var1) throws SQLException;

    public void setCurrentRow(int var1) throws SQLException;

    public void setOwner(ResultSetImpl var1);

    public int size() throws SQLException;

    public boolean wasEmpty();

    public void setMetadata(Field[] var1);
}


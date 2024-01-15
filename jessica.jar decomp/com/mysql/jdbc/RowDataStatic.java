/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Field;
import com.mysql.jdbc.ResultSetImpl;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.ResultSetRow;
import com.mysql.jdbc.RowData;
import java.sql.SQLException;
import java.util.List;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class RowDataStatic
implements RowData {
    private Field[] metadata;
    private int index = -1;
    ResultSetImpl owner;
    private List<ResultSetRow> rows;

    public RowDataStatic(List<ResultSetRow> rows) {
        this.rows = rows;
    }

    @Override
    public void addRow(ResultSetRow row) {
        this.rows.add(row);
    }

    @Override
    public void afterLast() {
        if (this.rows.size() > 0) {
            this.index = this.rows.size();
        }
    }

    @Override
    public void beforeFirst() {
        if (this.rows.size() > 0) {
            this.index = -1;
        }
    }

    @Override
    public void beforeLast() {
        if (this.rows.size() > 0) {
            this.index = this.rows.size() - 2;
        }
    }

    @Override
    public void close() {
    }

    @Override
    public ResultSetRow getAt(int atIndex) throws SQLException {
        if (atIndex < 0 || atIndex >= this.rows.size()) {
            return null;
        }
        return this.rows.get(atIndex).setMetadata(this.metadata);
    }

    @Override
    public int getCurrentRowNumber() {
        return this.index;
    }

    @Override
    public ResultSetInternalMethods getOwner() {
        return this.owner;
    }

    @Override
    public boolean hasNext() {
        boolean hasMore = this.index + 1 < this.rows.size();
        return hasMore;
    }

    @Override
    public boolean isAfterLast() {
        return this.index >= this.rows.size() && this.rows.size() != 0;
    }

    @Override
    public boolean isBeforeFirst() {
        return this.index == -1 && this.rows.size() != 0;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.rows.size() == 0;
    }

    @Override
    public boolean isFirst() {
        return this.index == 0;
    }

    @Override
    public boolean isLast() {
        if (this.rows.size() == 0) {
            return false;
        }
        return this.index == this.rows.size() - 1;
    }

    @Override
    public void moveRowRelative(int rowsToMove) {
        if (this.rows.size() > 0) {
            this.index += rowsToMove;
            if (this.index < -1) {
                this.beforeFirst();
            } else if (this.index > this.rows.size()) {
                this.afterLast();
            }
        }
    }

    @Override
    public ResultSetRow next() throws SQLException {
        ++this.index;
        if (this.index > this.rows.size()) {
            this.afterLast();
        } else if (this.index < this.rows.size()) {
            ResultSetRow row = this.rows.get(this.index);
            return row.setMetadata(this.metadata);
        }
        return null;
    }

    @Override
    public void removeRow(int atIndex) {
        this.rows.remove(atIndex);
    }

    @Override
    public void setCurrentRow(int newIndex) {
        this.index = newIndex;
    }

    @Override
    public void setOwner(ResultSetImpl rs) {
        this.owner = rs;
    }

    @Override
    public int size() {
        return this.rows.size();
    }

    @Override
    public boolean wasEmpty() {
        return this.rows != null && this.rows.size() == 0;
    }

    @Override
    public void setMetadata(Field[] metadata) {
        this.metadata = metadata;
    }
}


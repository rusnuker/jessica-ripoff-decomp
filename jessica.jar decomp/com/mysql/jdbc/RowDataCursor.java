/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Field;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MysqlIO;
import com.mysql.jdbc.OperationNotSupportedException;
import com.mysql.jdbc.ResultSetImpl;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.ResultSetRow;
import com.mysql.jdbc.RowData;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.ServerPreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowDataCursor
implements RowData {
    private static final int BEFORE_START_OF_ROWS = -1;
    private List<ResultSetRow> fetchedRows;
    private int currentPositionInEntireResult = -1;
    private int currentPositionInFetchedRows = -1;
    private ResultSetImpl owner;
    private boolean lastRowFetched = false;
    private Field[] metadata;
    private MysqlIO mysql;
    private long statementIdOnServer;
    private ServerPreparedStatement prepStmt;
    private static final int SERVER_STATUS_LAST_ROW_SENT = 128;
    private boolean firstFetchCompleted = false;
    private boolean wasEmpty = false;
    private boolean useBufferRowExplicit = false;

    public RowDataCursor(MysqlIO ioChannel, ServerPreparedStatement creatingStatement, Field[] metadata) {
        this.metadata = metadata;
        this.mysql = ioChannel;
        this.statementIdOnServer = creatingStatement.getServerStatementId();
        this.prepStmt = creatingStatement;
        this.useBufferRowExplicit = MysqlIO.useBufferRowExplicit(this.metadata);
    }

    public boolean isAfterLast() {
        return this.lastRowFetched && this.currentPositionInFetchedRows > this.fetchedRows.size();
    }

    public ResultSetRow getAt(int ind) throws SQLException {
        this.notSupported();
        return null;
    }

    public boolean isBeforeFirst() throws SQLException {
        return this.currentPositionInEntireResult < 0;
    }

    public void setCurrentRow(int rowNumber) throws SQLException {
        this.notSupported();
    }

    public int getCurrentRowNumber() throws SQLException {
        return this.currentPositionInEntireResult + 1;
    }

    public boolean isDynamic() {
        return true;
    }

    public boolean isEmpty() throws SQLException {
        return this.isBeforeFirst() && this.isAfterLast();
    }

    public boolean isFirst() throws SQLException {
        return this.currentPositionInEntireResult == 0;
    }

    public boolean isLast() throws SQLException {
        return this.lastRowFetched && this.currentPositionInFetchedRows == this.fetchedRows.size() - 1;
    }

    public void addRow(ResultSetRow row) throws SQLException {
        this.notSupported();
    }

    public void afterLast() throws SQLException {
        this.notSupported();
    }

    public void beforeFirst() throws SQLException {
        this.notSupported();
    }

    public void beforeLast() throws SQLException {
        this.notSupported();
    }

    public void close() throws SQLException {
        this.metadata = null;
        this.owner = null;
    }

    public boolean hasNext() throws SQLException {
        int maxRows;
        if (this.fetchedRows != null && this.fetchedRows.size() == 0) {
            return false;
        }
        if (this.owner != null && this.owner.owningStatement != null && (maxRows = this.owner.owningStatement.maxRows) != -1 && this.currentPositionInEntireResult + 1 > maxRows) {
            return false;
        }
        if (this.currentPositionInEntireResult != -1) {
            if (this.currentPositionInFetchedRows < this.fetchedRows.size() - 1) {
                return true;
            }
            if (this.currentPositionInFetchedRows == this.fetchedRows.size() && this.lastRowFetched) {
                return false;
            }
            this.fetchMoreRows();
            return this.fetchedRows.size() > 0;
        }
        this.fetchMoreRows();
        return this.fetchedRows.size() > 0;
    }

    public void moveRowRelative(int rows) throws SQLException {
        this.notSupported();
    }

    public ResultSetRow next() throws SQLException {
        if (this.fetchedRows == null && this.currentPositionInEntireResult != -1) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Operation_not_allowed_after_ResultSet_closed_144"), "S1000", this.mysql.getExceptionInterceptor());
        }
        if (!this.hasNext()) {
            return null;
        }
        ++this.currentPositionInEntireResult;
        ++this.currentPositionInFetchedRows;
        if (this.fetchedRows != null && this.fetchedRows.size() == 0) {
            return null;
        }
        if (this.fetchedRows == null || this.currentPositionInFetchedRows > this.fetchedRows.size() - 1) {
            this.fetchMoreRows();
            this.currentPositionInFetchedRows = 0;
        }
        ResultSetRow row = this.fetchedRows.get(this.currentPositionInFetchedRows);
        row.setMetadata(this.metadata);
        return row;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void fetchMoreRows() throws SQLException {
        if (this.lastRowFetched) {
            this.fetchedRows = new ArrayList<ResultSetRow>(0);
            return;
        }
        Object object = this.owner.connection.getConnectionMutex();
        synchronized (object) {
            int numRowsToFetch;
            boolean oldFirstFetchCompleted = this.firstFetchCompleted;
            if (!this.firstFetchCompleted) {
                this.firstFetchCompleted = true;
            }
            if ((numRowsToFetch = this.owner.getFetchSize()) == 0) {
                numRowsToFetch = this.prepStmt.getFetchSize();
            }
            if (numRowsToFetch == Integer.MIN_VALUE) {
                numRowsToFetch = 1;
            }
            this.fetchedRows = this.mysql.fetchRowsViaCursor(this.fetchedRows, this.statementIdOnServer, this.metadata, numRowsToFetch, this.useBufferRowExplicit);
            this.currentPositionInFetchedRows = -1;
            if ((this.mysql.getServerStatus() & 0x80) != 0) {
                this.lastRowFetched = true;
                if (!oldFirstFetchCompleted && this.fetchedRows.size() == 0) {
                    this.wasEmpty = true;
                }
            }
        }
    }

    public void removeRow(int ind) throws SQLException {
        this.notSupported();
    }

    public int size() {
        return -1;
    }

    protected void nextRecord() throws SQLException {
    }

    private void notSupported() throws SQLException {
        throw new OperationNotSupportedException();
    }

    public void setOwner(ResultSetImpl rs) {
        this.owner = rs;
    }

    public ResultSetInternalMethods getOwner() {
        return this.owner;
    }

    public boolean wasEmpty() {
        return this.wasEmpty;
    }

    public void setMetadata(Field[] metadata) {
        this.metadata = metadata;
    }
}


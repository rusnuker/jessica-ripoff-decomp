/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.jdbc2.optional.SuspendableXAConnection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.StatementEvent;
import javax.sql.StatementEventListener;

public class JDBC4SuspendableXAConnection
extends SuspendableXAConnection {
    private final Map<StatementEventListener, StatementEventListener> statementEventListeners = new HashMap<StatementEventListener, StatementEventListener>();

    public JDBC4SuspendableXAConnection(Connection connection) throws SQLException {
        super(connection);
    }

    @Override
    public synchronized void close() throws SQLException {
        super.close();
        this.statementEventListeners.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addStatementEventListener(StatementEventListener listener) {
        Map<StatementEventListener, StatementEventListener> map = this.statementEventListeners;
        synchronized (map) {
            this.statementEventListeners.put(listener, listener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeStatementEventListener(StatementEventListener listener) {
        Map<StatementEventListener, StatementEventListener> map = this.statementEventListeners;
        synchronized (map) {
            this.statementEventListeners.remove(listener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void fireStatementEvent(StatementEvent event) throws SQLException {
        Map<StatementEventListener, StatementEventListener> map = this.statementEventListeners;
        synchronized (map) {
            for (StatementEventListener listener : this.statementEventListeners.keySet()) {
                listener.statementClosed(event);
            }
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.CommunicationsException;
import com.mysql.jdbc.MySQLConnection;

public class ConnectionFeatureNotAvailableException
extends CommunicationsException {
    static final long serialVersionUID = -5065030488729238287L;

    public ConnectionFeatureNotAvailableException(MySQLConnection conn, long lastPacketSentTimeMs, Exception underlyingException) {
        super(conn, lastPacketSentTimeMs, 0L, underlyingException);
    }

    public String getMessage() {
        return "Feature not available in this distribution of Connector/J";
    }

    public String getSQLState() {
        return "01S00";
    }
}


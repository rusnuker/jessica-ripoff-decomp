/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.StreamingNotifiable;
import java.sql.SQLException;

public class CommunicationsException
extends SQLException
implements StreamingNotifiable {
    static final long serialVersionUID = 3193864990663398317L;
    private String exceptionMessage = null;

    public CommunicationsException(MySQLConnection conn, long lastPacketSentTimeMs, long lastPacketReceivedTimeMs, Exception underlyingException) {
        this.exceptionMessage = SQLError.createLinkFailureMessageBasedOnHeuristics(conn, lastPacketSentTimeMs, lastPacketReceivedTimeMs, underlyingException);
        if (underlyingException != null) {
            this.initCause(underlyingException);
        }
    }

    public String getMessage() {
        return this.exceptionMessage;
    }

    public String getSQLState() {
        return "08S01";
    }

    public void setWasStreamingResults() {
        this.exceptionMessage = Messages.getString("CommunicationsException.ClientWasStreaming");
    }
}


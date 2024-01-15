/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.CommunicationsException;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.MysqlDataTruncation;
import com.mysql.jdbc.MysqlErrorNumbers;
import com.mysql.jdbc.NotImplemented;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.exceptions.MySQLDataException;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.MySQLNonTransientConnectionException;
import com.mysql.jdbc.exceptions.MySQLQueryInterruptedException;
import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import com.mysql.jdbc.exceptions.MySQLTransactionRollbackException;
import com.mysql.jdbc.exceptions.MySQLTransientConnectionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.BindException;
import java.sql.BatchUpdateException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

public class SQLError {
    static final int ER_WARNING_NOT_COMPLETE_ROLLBACK = 1196;
    private static Map<Integer, String> mysqlToSql99State;
    private static Map<Integer, String> mysqlToSqlState;
    public static final String SQL_STATE_WARNING = "01000";
    public static final String SQL_STATE_DISCONNECT_ERROR = "01002";
    public static final String SQL_STATE_DATE_TRUNCATED = "01004";
    public static final String SQL_STATE_PRIVILEGE_NOT_REVOKED = "01006";
    public static final String SQL_STATE_NO_DATA = "02000";
    public static final String SQL_STATE_WRONG_NO_OF_PARAMETERS = "07001";
    public static final String SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE = "08001";
    public static final String SQL_STATE_CONNECTION_IN_USE = "08002";
    public static final String SQL_STATE_CONNECTION_NOT_OPEN = "08003";
    public static final String SQL_STATE_CONNECTION_REJECTED = "08004";
    public static final String SQL_STATE_CONNECTION_FAILURE = "08006";
    public static final String SQL_STATE_TRANSACTION_RESOLUTION_UNKNOWN = "08007";
    public static final String SQL_STATE_COMMUNICATION_LINK_FAILURE = "08S01";
    public static final String SQL_STATE_FEATURE_NOT_SUPPORTED = "0A000";
    public static final String SQL_STATE_CARDINALITY_VIOLATION = "21000";
    public static final String SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST = "21S01";
    public static final String SQL_STATE_STRING_DATA_RIGHT_TRUNCATION = "22001";
    public static final String SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE = "22003";
    public static final String SQL_STATE_INVALID_DATETIME_FORMAT = "22007";
    public static final String SQL_STATE_DATETIME_FIELD_OVERFLOW = "22008";
    public static final String SQL_STATE_DIVISION_BY_ZERO = "22012";
    public static final String SQL_STATE_INVALID_CHARACTER_VALUE_FOR_CAST = "22018";
    public static final String SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION = "23000";
    public static final String SQL_STATE_INVALID_CURSOR_STATE = "24000";
    public static final String SQL_STATE_INVALID_TRANSACTION_STATE = "25000";
    public static final String SQL_STATE_INVALID_AUTH_SPEC = "28000";
    public static final String SQL_STATE_INVALID_TRANSACTION_TERMINATION = "2D000";
    public static final String SQL_STATE_INVALID_CONDITION_NUMBER = "35000";
    public static final String SQL_STATE_INVALID_CATALOG_NAME = "3D000";
    public static final String SQL_STATE_ROLLBACK_SERIALIZATION_FAILURE = "40001";
    public static final String SQL_STATE_SYNTAX_ERROR = "42000";
    public static final String SQL_STATE_ER_TABLE_EXISTS_ERROR = "42S01";
    public static final String SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND = "42S02";
    public static final String SQL_STATE_ER_NO_SUCH_INDEX = "42S12";
    public static final String SQL_STATE_ER_DUP_FIELDNAME = "42S21";
    public static final String SQL_STATE_ER_BAD_FIELD_ERROR = "42S22";
    public static final String SQL_STATE_INVALID_CONNECTION_ATTRIBUTE = "01S00";
    public static final String SQL_STATE_ERROR_IN_ROW = "01S01";
    public static final String SQL_STATE_NO_ROWS_UPDATED_OR_DELETED = "01S03";
    public static final String SQL_STATE_MORE_THAN_ONE_ROW_UPDATED_OR_DELETED = "01S04";
    public static final String SQL_STATE_RESIGNAL_WHEN_HANDLER_NOT_ACTIVE = "0K000";
    public static final String SQL_STATE_STACKED_DIAGNOSTICS_ACCESSED_WITHOUT_ACTIVE_HANDLER = "0Z002";
    public static final String SQL_STATE_CASE_NOT_FOUND_FOR_CASE_STATEMENT = "20000";
    public static final String SQL_STATE_NULL_VALUE_NOT_ALLOWED = "22004";
    public static final String SQL_STATE_INVALID_LOGARITHM_ARGUMENT = "2201E";
    public static final String SQL_STATE_ACTIVE_SQL_TRANSACTION = "25001";
    public static final String SQL_STATE_READ_ONLY_SQL_TRANSACTION = "25006";
    public static final String SQL_STATE_SRE_PROHIBITED_SQL_STATEMENT_ATTEMPTED = "2F003";
    public static final String SQL_STATE_SRE_FUNCTION_EXECUTED_NO_RETURN_STATEMENT = "2F005";
    public static final String SQL_STATE_ER_QUERY_INTERRUPTED = "70100";
    public static final String SQL_STATE_BASE_TABLE_OR_VIEW_ALREADY_EXISTS = "S0001";
    public static final String SQL_STATE_BASE_TABLE_NOT_FOUND = "S0002";
    public static final String SQL_STATE_INDEX_ALREADY_EXISTS = "S0011";
    public static final String SQL_STATE_INDEX_NOT_FOUND = "S0012";
    public static final String SQL_STATE_COLUMN_ALREADY_EXISTS = "S0021";
    public static final String SQL_STATE_COLUMN_NOT_FOUND = "S0022";
    public static final String SQL_STATE_NO_DEFAULT_FOR_COLUMN = "S0023";
    public static final String SQL_STATE_GENERAL_ERROR = "S1000";
    public static final String SQL_STATE_MEMORY_ALLOCATION_FAILURE = "S1001";
    public static final String SQL_STATE_INVALID_COLUMN_NUMBER = "S1002";
    public static final String SQL_STATE_ILLEGAL_ARGUMENT = "S1009";
    public static final String SQL_STATE_DRIVER_NOT_CAPABLE = "S1C00";
    public static final String SQL_STATE_TIMEOUT_EXPIRED = "S1T00";
    public static final String SQL_STATE_CLI_SPECIFIC_CONDITION = "HY000";
    public static final String SQL_STATE_MEMORY_ALLOCATION_ERROR = "HY001";
    public static final String SQL_STATE_XA_RBROLLBACK = "XA100";
    public static final String SQL_STATE_XA_RBDEADLOCK = "XA102";
    public static final String SQL_STATE_XA_RBTIMEOUT = "XA106";
    public static final String SQL_STATE_XA_RMERR = "XAE03";
    public static final String SQL_STATE_XAER_NOTA = "XAE04";
    public static final String SQL_STATE_XAER_INVAL = "XAE05";
    public static final String SQL_STATE_XAER_RMFAIL = "XAE07";
    public static final String SQL_STATE_XAER_DUPID = "XAE08";
    public static final String SQL_STATE_XAER_OUTSIDE = "XAE09";
    private static Map<String, String> sqlStateMessages;
    private static final long DEFAULT_WAIT_TIMEOUT_SECONDS = 28800L;
    private static final int DUE_TO_TIMEOUT_FALSE = 0;
    private static final int DUE_TO_TIMEOUT_MAYBE = 2;
    private static final int DUE_TO_TIMEOUT_TRUE = 1;
    private static final Constructor<?> JDBC_4_COMMUNICATIONS_EXCEPTION_CTOR;

    static SQLWarning convertShowWarningsToSQLWarnings(Connection connection) throws SQLException {
        return SQLError.convertShowWarningsToSQLWarnings(connection, 0, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static SQLWarning convertShowWarningsToSQLWarnings(Connection connection, int warningCountIfKnown, boolean forTruncationOnly) throws SQLException {
        SQLException sqlEx22;
        SQLException reThrow2;
        SQLWarning sQLWarning;
        Statement stmt;
        block21: {
            stmt = null;
            ResultSet warnRs = null;
            SQLWarning currentWarning = null;
            try {
                if (warningCountIfKnown < 100) {
                    stmt = connection.createStatement();
                    if (stmt.getMaxRows() != 0) {
                        stmt.setMaxRows(0);
                    }
                } else {
                    stmt = connection.createStatement(1003, 1007);
                    stmt.setFetchSize(Integer.MIN_VALUE);
                }
                warnRs = stmt.executeQuery("SHOW WARNINGS");
                while (warnRs.next()) {
                    int code = warnRs.getInt("Code");
                    if (forTruncationOnly) {
                        if (code != 1265 && code != 1264) continue;
                        MysqlDataTruncation newTruncation = new MysqlDataTruncation(warnRs.getString("Message"), 0, false, false, 0, 0, code);
                        if (currentWarning == null) {
                            currentWarning = newTruncation;
                            continue;
                        }
                        currentWarning.setNextWarning(newTruncation);
                        continue;
                    }
                    String message = warnRs.getString("Message");
                    SQLWarning newWarning = new SQLWarning(message, SQLError.mysqlToSqlState(code, connection.getUseSqlStateCodes()), code);
                    if (currentWarning == null) {
                        currentWarning = newWarning;
                        continue;
                    }
                    currentWarning.setNextWarning(newWarning);
                }
                if (forTruncationOnly && currentWarning != null) {
                    throw currentWarning;
                }
                sQLWarning = currentWarning;
                Object var10_10 = null;
                reThrow2 = null;
                if (warnRs == null) break block21;
            }
            catch (Throwable throwable) {
                SQLException sqlEx22;
                Object var10_11 = null;
                SQLException reThrow2 = null;
                if (warnRs != null) {
                    try {
                        warnRs.close();
                    }
                    catch (SQLException sqlEx22) {
                        reThrow2 = sqlEx22;
                    }
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    }
                    catch (SQLException sqlEx22) {
                        reThrow2 = sqlEx22;
                    }
                }
                if (reThrow2 != null) {
                    throw reThrow2;
                }
                throw throwable;
            }
            try {
                warnRs.close();
            }
            catch (SQLException sqlEx22) {
                reThrow2 = sqlEx22;
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            }
            catch (SQLException sqlEx22) {
                reThrow2 = sqlEx22;
            }
        }
        if (reThrow2 != null) {
            throw reThrow2;
        }
        return sQLWarning;
    }

    public static void dumpSqlStatesMappingsAsXml() throws Exception {
        TreeMap<Integer, Integer> allErrorNumbers = new TreeMap<Integer, Integer>();
        HashMap<Object, String> mysqlErrorNumbersToNames = new HashMap<Object, String>();
        for (Integer errorNumber : mysqlToSql99State.keySet()) {
            allErrorNumbers.put(errorNumber, errorNumber);
        }
        for (Integer errorNumber : mysqlToSqlState.keySet()) {
            allErrorNumbers.put(errorNumber, errorNumber);
        }
        Field[] possibleFields = MysqlErrorNumbers.class.getDeclaredFields();
        for (int i = 0; i < possibleFields.length; ++i) {
            String fieldName = possibleFields[i].getName();
            if (!fieldName.startsWith("ER_")) continue;
            mysqlErrorNumbersToNames.put(possibleFields[i].get(null), fieldName);
        }
        System.out.println("<ErrorMappings>");
        for (Integer errorNumber : allErrorNumbers.keySet()) {
            String sql92State = SQLError.mysqlToSql99(errorNumber);
            String oldSqlState = SQLError.mysqlToXOpen(errorNumber);
            System.out.println("   <ErrorMapping mysqlErrorNumber=\"" + errorNumber + "\" mysqlErrorName=\"" + (String)mysqlErrorNumbersToNames.get(errorNumber) + "\" legacySqlState=\"" + (oldSqlState == null ? "" : oldSqlState) + "\" sql92SqlState=\"" + (sql92State == null ? "" : sql92State) + "\"/>");
        }
        System.out.println("</ErrorMappings>");
    }

    static String get(String stateCode) {
        return sqlStateMessages.get(stateCode);
    }

    private static String mysqlToSql99(int errno) {
        Integer err = errno;
        if (mysqlToSql99State.containsKey(err)) {
            return mysqlToSql99State.get(err);
        }
        return SQL_STATE_CLI_SPECIFIC_CONDITION;
    }

    static String mysqlToSqlState(int errno, boolean useSql92States) {
        if (useSql92States) {
            return SQLError.mysqlToSql99(errno);
        }
        return SQLError.mysqlToXOpen(errno);
    }

    private static String mysqlToXOpen(int errno) {
        Integer err = errno;
        if (mysqlToSqlState.containsKey(err)) {
            return mysqlToSqlState.get(err);
        }
        return SQL_STATE_GENERAL_ERROR;
    }

    public static SQLException createSQLException(String message, String sqlState, ExceptionInterceptor interceptor) {
        return SQLError.createSQLException(message, sqlState, 0, interceptor);
    }

    public static SQLException createSQLException(String message, ExceptionInterceptor interceptor) {
        return SQLError.createSQLException(message, interceptor, null);
    }

    public static SQLException createSQLException(String message, ExceptionInterceptor interceptor, Connection conn) {
        SQLException sqlEx = new SQLException(message);
        return SQLError.runThroughExceptionInterceptor(interceptor, sqlEx, conn);
    }

    public static SQLException createSQLException(String message, String sqlState, Throwable cause, ExceptionInterceptor interceptor) {
        return SQLError.createSQLException(message, sqlState, cause, interceptor, null);
    }

    public static SQLException createSQLException(String message, String sqlState, Throwable cause, ExceptionInterceptor interceptor, Connection conn) {
        SQLException sqlEx = SQLError.createSQLException(message, sqlState, null);
        if (sqlEx.getCause() == null) {
            sqlEx.initCause(cause);
        }
        return SQLError.runThroughExceptionInterceptor(interceptor, sqlEx, conn);
    }

    public static SQLException createSQLException(String message, String sqlState, int vendorErrorCode, ExceptionInterceptor interceptor) {
        return SQLError.createSQLException(message, sqlState, vendorErrorCode, false, interceptor);
    }

    public static SQLException createSQLException(String message, String sqlState, int vendorErrorCode, boolean isTransient, ExceptionInterceptor interceptor) {
        return SQLError.createSQLException(message, sqlState, vendorErrorCode, isTransient, interceptor, null);
    }

    public static SQLException createSQLException(String message, String sqlState, int vendorErrorCode, boolean isTransient, ExceptionInterceptor interceptor, Connection conn) {
        try {
            SQLException sqlEx = null;
            sqlEx = sqlState != null ? (sqlState.startsWith("08") ? (isTransient ? (!Util.isJdbc4() ? new MySQLTransientConnectionException(message, sqlState, vendorErrorCode) : (SQLException)Util.getInstance("com.mysql.jdbc.exceptions.jdbc4.MySQLTransientConnectionException", new Class[]{String.class, String.class, Integer.TYPE}, new Object[]{message, sqlState, vendorErrorCode}, interceptor)) : (!Util.isJdbc4() ? new MySQLNonTransientConnectionException(message, sqlState, vendorErrorCode) : (SQLException)Util.getInstance("com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException", new Class[]{String.class, String.class, Integer.TYPE}, new Object[]{message, sqlState, vendorErrorCode}, interceptor))) : (sqlState.startsWith("22") ? (!Util.isJdbc4() ? new MySQLDataException(message, sqlState, vendorErrorCode) : (SQLException)Util.getInstance("com.mysql.jdbc.exceptions.jdbc4.MySQLDataException", new Class[]{String.class, String.class, Integer.TYPE}, new Object[]{message, sqlState, vendorErrorCode}, interceptor)) : (sqlState.startsWith("23") ? (!Util.isJdbc4() ? new MySQLIntegrityConstraintViolationException(message, sqlState, vendorErrorCode) : (SQLException)Util.getInstance("com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException", new Class[]{String.class, String.class, Integer.TYPE}, new Object[]{message, sqlState, vendorErrorCode}, interceptor)) : (sqlState.startsWith("42") ? (!Util.isJdbc4() ? new MySQLSyntaxErrorException(message, sqlState, vendorErrorCode) : (SQLException)Util.getInstance("com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException", new Class[]{String.class, String.class, Integer.TYPE}, new Object[]{message, sqlState, vendorErrorCode}, interceptor)) : (sqlState.startsWith("40") ? (!Util.isJdbc4() ? new MySQLTransactionRollbackException(message, sqlState, vendorErrorCode) : (SQLException)Util.getInstance("com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException", new Class[]{String.class, String.class, Integer.TYPE}, new Object[]{message, sqlState, vendorErrorCode}, interceptor)) : (sqlState.startsWith(SQL_STATE_ER_QUERY_INTERRUPTED) ? (!Util.isJdbc4() ? new MySQLQueryInterruptedException(message, sqlState, vendorErrorCode) : (SQLException)Util.getInstance("com.mysql.jdbc.exceptions.jdbc4.MySQLQueryInterruptedException", new Class[]{String.class, String.class, Integer.TYPE}, new Object[]{message, sqlState, vendorErrorCode}, interceptor)) : new SQLException(message, sqlState, vendorErrorCode))))))) : new SQLException(message, sqlState, vendorErrorCode);
            return SQLError.runThroughExceptionInterceptor(interceptor, sqlEx, conn);
        }
        catch (SQLException sqlEx) {
            SQLException unexpectedEx = new SQLException("Unable to create correct SQLException class instance, error class/codes may be incorrect. Reason: " + Util.stackTraceToString(sqlEx), SQL_STATE_GENERAL_ERROR);
            return SQLError.runThroughExceptionInterceptor(interceptor, unexpectedEx, conn);
        }
    }

    public static SQLException createCommunicationsException(MySQLConnection conn, long lastPacketSentTimeMs, long lastPacketReceivedTimeMs, Exception underlyingException, ExceptionInterceptor interceptor) {
        SQLException exToReturn = null;
        if (!Util.isJdbc4()) {
            exToReturn = new CommunicationsException(conn, lastPacketSentTimeMs, lastPacketReceivedTimeMs, underlyingException);
        } else {
            try {
                exToReturn = (SQLException)Util.handleNewInstance(JDBC_4_COMMUNICATIONS_EXCEPTION_CTOR, new Object[]{conn, lastPacketSentTimeMs, lastPacketReceivedTimeMs, underlyingException}, interceptor);
            }
            catch (SQLException sqlEx) {
                return sqlEx;
            }
        }
        return SQLError.runThroughExceptionInterceptor(interceptor, exToReturn, conn);
    }

    public static String createLinkFailureMessageBasedOnHeuristics(MySQLConnection conn, long lastPacketSentTimeMs, long lastPacketReceivedTimeMs, Exception underlyingException) {
        Object[] timingInfo;
        long serverTimeoutSeconds = 0L;
        boolean isInteractiveClient = false;
        if (conn != null) {
            isInteractiveClient = conn.getInteractiveClient();
            String serverTimeoutSecondsStr = null;
            serverTimeoutSecondsStr = isInteractiveClient ? conn.getServerVariable("interactive_timeout") : conn.getServerVariable("wait_timeout");
            if (serverTimeoutSecondsStr != null) {
                try {
                    serverTimeoutSeconds = Long.parseLong(serverTimeoutSecondsStr);
                }
                catch (NumberFormatException nfe) {
                    serverTimeoutSeconds = 0L;
                }
            }
        }
        StringBuilder exceptionMessageBuf = new StringBuilder();
        long nowMs = System.currentTimeMillis();
        if (lastPacketSentTimeMs == 0L) {
            lastPacketSentTimeMs = nowMs;
        }
        long timeSinceLastPacketSentMs = nowMs - lastPacketSentTimeMs;
        long timeSinceLastPacketSeconds = timeSinceLastPacketSentMs / 1000L;
        long timeSinceLastPacketReceivedMs = nowMs - lastPacketReceivedTimeMs;
        int dueToTimeout = 0;
        StringBuilder timeoutMessageBuf = null;
        if (serverTimeoutSeconds != 0L) {
            if (timeSinceLastPacketSeconds > serverTimeoutSeconds) {
                dueToTimeout = 1;
                timeoutMessageBuf = new StringBuilder();
                timeoutMessageBuf.append(Messages.getString("CommunicationsException.2"));
                if (!isInteractiveClient) {
                    timeoutMessageBuf.append(Messages.getString("CommunicationsException.3"));
                } else {
                    timeoutMessageBuf.append(Messages.getString("CommunicationsException.4"));
                }
            }
        } else if (timeSinceLastPacketSeconds > 28800L) {
            dueToTimeout = 2;
            timeoutMessageBuf = new StringBuilder();
            timeoutMessageBuf.append(Messages.getString("CommunicationsException.5"));
            timeoutMessageBuf.append(Messages.getString("CommunicationsException.6"));
            timeoutMessageBuf.append(Messages.getString("CommunicationsException.7"));
            timeoutMessageBuf.append(Messages.getString("CommunicationsException.8"));
        }
        if (dueToTimeout == 1 || dueToTimeout == 2) {
            if (lastPacketReceivedTimeMs != 0L) {
                timingInfo = new Object[]{timeSinceLastPacketReceivedMs, timeSinceLastPacketSentMs};
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfo", timingInfo));
            } else {
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfoNoRecv", new Object[]{timeSinceLastPacketSentMs}));
            }
            if (timeoutMessageBuf != null) {
                exceptionMessageBuf.append((CharSequence)timeoutMessageBuf);
            }
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.11"));
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.12"));
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.13"));
        } else if (underlyingException instanceof BindException) {
            if (conn.getLocalSocketAddress() != null && !Util.interfaceExists(conn.getLocalSocketAddress())) {
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.LocalSocketAddressNotAvailable"));
            } else {
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.TooManyClientConnections"));
            }
        }
        if (exceptionMessageBuf.length() == 0) {
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.20"));
            if (conn != null && conn.getMaintainTimeStats() && !conn.getParanoid()) {
                exceptionMessageBuf.append("\n\n");
                if (lastPacketReceivedTimeMs != 0L) {
                    timingInfo = new Object[]{timeSinceLastPacketReceivedMs, timeSinceLastPacketSentMs};
                    exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfo", timingInfo));
                } else {
                    exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfoNoRecv", new Object[]{timeSinceLastPacketSentMs}));
                }
            }
        }
        return exceptionMessageBuf.toString();
    }

    private static SQLException runThroughExceptionInterceptor(ExceptionInterceptor exInterceptor, SQLException sqlEx, Connection conn) {
        SQLException interceptedEx;
        if (exInterceptor != null && (interceptedEx = exInterceptor.interceptException(sqlEx, conn)) != null) {
            return interceptedEx;
        }
        return sqlEx;
    }

    public static SQLException createBatchUpdateException(SQLException underlyingEx, long[] updateCounts, ExceptionInterceptor interceptor) throws SQLException {
        SQLException newEx;
        if (Util.isJdbc42()) {
            newEx = (SQLException)Util.getInstance("java.sql.BatchUpdateException", new Class[]{String.class, String.class, Integer.TYPE, long[].class, Throwable.class}, new Object[]{underlyingEx.getMessage(), underlyingEx.getSQLState(), underlyingEx.getErrorCode(), updateCounts, underlyingEx}, interceptor);
        } else {
            newEx = new BatchUpdateException(underlyingEx.getMessage(), underlyingEx.getSQLState(), underlyingEx.getErrorCode(), Util.truncateAndConvertToInt(updateCounts));
            newEx.initCause(underlyingEx);
        }
        return SQLError.runThroughExceptionInterceptor(interceptor, newEx, null);
    }

    public static SQLException createSQLFeatureNotSupportedException() throws SQLException {
        SQLException newEx = Util.isJdbc4() ? (SQLException)Util.getInstance("java.sql.SQLFeatureNotSupportedException", null, null, null) : new NotImplemented();
        return newEx;
    }

    public static SQLException createSQLFeatureNotSupportedException(String message, String sqlState, ExceptionInterceptor interceptor) throws SQLException {
        SQLException newEx = Util.isJdbc4() ? (SQLException)Util.getInstance("java.sql.SQLFeatureNotSupportedException", new Class[]{String.class, String.class}, new Object[]{message, sqlState}, interceptor) : new NotImplemented();
        return SQLError.runThroughExceptionInterceptor(interceptor, newEx, null);
    }

    static {
        if (Util.isJdbc4()) {
            try {
                JDBC_4_COMMUNICATIONS_EXCEPTION_CTOR = Class.forName("com.mysql.jdbc.exceptions.jdbc4.CommunicationsException").getConstructor(MySQLConnection.class, Long.TYPE, Long.TYPE, Exception.class);
            }
            catch (SecurityException e) {
                throw new RuntimeException(e);
            }
            catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            JDBC_4_COMMUNICATIONS_EXCEPTION_CTOR = null;
        }
        sqlStateMessages = new HashMap<String, String>();
        sqlStateMessages.put(SQL_STATE_DISCONNECT_ERROR, Messages.getString("SQLError.35"));
        sqlStateMessages.put(SQL_STATE_DATE_TRUNCATED, Messages.getString("SQLError.36"));
        sqlStateMessages.put(SQL_STATE_PRIVILEGE_NOT_REVOKED, Messages.getString("SQLError.37"));
        sqlStateMessages.put(SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, Messages.getString("SQLError.38"));
        sqlStateMessages.put(SQL_STATE_ERROR_IN_ROW, Messages.getString("SQLError.39"));
        sqlStateMessages.put(SQL_STATE_NO_ROWS_UPDATED_OR_DELETED, Messages.getString("SQLError.40"));
        sqlStateMessages.put(SQL_STATE_MORE_THAN_ONE_ROW_UPDATED_OR_DELETED, Messages.getString("SQLError.41"));
        sqlStateMessages.put(SQL_STATE_WRONG_NO_OF_PARAMETERS, Messages.getString("SQLError.42"));
        sqlStateMessages.put(SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE, Messages.getString("SQLError.43"));
        sqlStateMessages.put(SQL_STATE_CONNECTION_IN_USE, Messages.getString("SQLError.44"));
        sqlStateMessages.put(SQL_STATE_CONNECTION_NOT_OPEN, Messages.getString("SQLError.45"));
        sqlStateMessages.put(SQL_STATE_CONNECTION_REJECTED, Messages.getString("SQLError.46"));
        sqlStateMessages.put(SQL_STATE_TRANSACTION_RESOLUTION_UNKNOWN, Messages.getString("SQLError.47"));
        sqlStateMessages.put(SQL_STATE_COMMUNICATION_LINK_FAILURE, Messages.getString("SQLError.48"));
        sqlStateMessages.put(SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST, Messages.getString("SQLError.49"));
        sqlStateMessages.put(SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE, Messages.getString("SQLError.50"));
        sqlStateMessages.put(SQL_STATE_DATETIME_FIELD_OVERFLOW, Messages.getString("SQLError.51"));
        sqlStateMessages.put(SQL_STATE_DIVISION_BY_ZERO, Messages.getString("SQLError.52"));
        sqlStateMessages.put(SQL_STATE_ROLLBACK_SERIALIZATION_FAILURE, Messages.getString("SQLError.53"));
        sqlStateMessages.put(SQL_STATE_INVALID_AUTH_SPEC, Messages.getString("SQLError.54"));
        sqlStateMessages.put(SQL_STATE_SYNTAX_ERROR, Messages.getString("SQLError.55"));
        sqlStateMessages.put(SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND, Messages.getString("SQLError.56"));
        sqlStateMessages.put(SQL_STATE_BASE_TABLE_OR_VIEW_ALREADY_EXISTS, Messages.getString("SQLError.57"));
        sqlStateMessages.put(SQL_STATE_BASE_TABLE_NOT_FOUND, Messages.getString("SQLError.58"));
        sqlStateMessages.put(SQL_STATE_INDEX_ALREADY_EXISTS, Messages.getString("SQLError.59"));
        sqlStateMessages.put(SQL_STATE_INDEX_NOT_FOUND, Messages.getString("SQLError.60"));
        sqlStateMessages.put(SQL_STATE_COLUMN_ALREADY_EXISTS, Messages.getString("SQLError.61"));
        sqlStateMessages.put(SQL_STATE_COLUMN_NOT_FOUND, Messages.getString("SQLError.62"));
        sqlStateMessages.put(SQL_STATE_NO_DEFAULT_FOR_COLUMN, Messages.getString("SQLError.63"));
        sqlStateMessages.put(SQL_STATE_GENERAL_ERROR, Messages.getString("SQLError.64"));
        sqlStateMessages.put(SQL_STATE_MEMORY_ALLOCATION_FAILURE, Messages.getString("SQLError.65"));
        sqlStateMessages.put(SQL_STATE_INVALID_COLUMN_NUMBER, Messages.getString("SQLError.66"));
        sqlStateMessages.put(SQL_STATE_ILLEGAL_ARGUMENT, Messages.getString("SQLError.67"));
        sqlStateMessages.put(SQL_STATE_DRIVER_NOT_CAPABLE, Messages.getString("SQLError.68"));
        sqlStateMessages.put(SQL_STATE_TIMEOUT_EXPIRED, Messages.getString("SQLError.69"));
        mysqlToSqlState = new Hashtable<Integer, String>();
        mysqlToSqlState.put(1249, SQL_STATE_WARNING);
        mysqlToSqlState.put(1261, SQL_STATE_WARNING);
        mysqlToSqlState.put(1262, SQL_STATE_WARNING);
        mysqlToSqlState.put(1265, SQL_STATE_WARNING);
        mysqlToSqlState.put(1311, SQL_STATE_WARNING);
        mysqlToSqlState.put(1642, SQL_STATE_WARNING);
        mysqlToSqlState.put(1040, SQL_STATE_CONNECTION_REJECTED);
        mysqlToSqlState.put(1251, SQL_STATE_CONNECTION_REJECTED);
        mysqlToSqlState.put(1042, SQL_STATE_CONNECTION_REJECTED);
        mysqlToSqlState.put(1043, SQL_STATE_CONNECTION_REJECTED);
        mysqlToSqlState.put(1129, SQL_STATE_CONNECTION_REJECTED);
        mysqlToSqlState.put(1130, SQL_STATE_CONNECTION_REJECTED);
        mysqlToSqlState.put(1047, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1053, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1080, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1081, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1152, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1153, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1154, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1155, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1156, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1157, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1158, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1159, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1160, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1161, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1184, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1189, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1190, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1218, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(1312, SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSqlState.put(1314, SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSqlState.put(1335, SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSqlState.put(1336, SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSqlState.put(1415, SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSqlState.put(1845, SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSqlState.put(1846, SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSqlState.put(1044, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1049, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1055, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1056, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1057, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1059, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1060, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1061, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1062, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1063, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1064, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1065, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1066, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1067, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1068, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1069, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1070, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1071, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1072, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1073, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1074, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1075, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1082, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1083, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1084, SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(1090, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1091, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1101, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1102, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1103, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1104, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1106, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1107, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1110, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1112, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1113, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1115, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1118, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1120, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1121, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1131, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1132, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1133, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1139, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1140, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1141, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1142, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1143, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1144, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1145, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1147, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1148, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1149, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1162, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1163, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1164, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1166, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1167, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1170, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1171, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1172, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1173, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1176, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1177, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1178, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1203, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1211, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1226, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1227, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1230, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1231, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1232, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1234, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1235, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1239, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1248, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1250, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1252, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1253, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1280, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1281, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1286, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1304, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1305, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1308, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1309, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1310, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1313, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1315, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1316, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1318, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1319, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1320, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1322, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1323, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1324, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1327, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1330, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1331, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1332, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1333, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1337, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1338, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1370, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1403, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1407, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1410, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1413, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1414, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1425, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1426, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1427, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1437, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1439, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1453, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1458, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1460, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1461, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1463, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1582, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1583, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1584, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1630, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1641, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1687, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1701, SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(1222, SQL_STATE_CARDINALITY_VIOLATION);
        mysqlToSqlState.put(1241, SQL_STATE_CARDINALITY_VIOLATION);
        mysqlToSqlState.put(1242, SQL_STATE_CARDINALITY_VIOLATION);
        mysqlToSqlState.put(1022, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(1048, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(1052, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(1169, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(1216, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(1217, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(1451, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(1452, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(1557, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(1586, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(1761, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(1762, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(1859, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(1406, SQL_STATE_STRING_DATA_RIGHT_TRUNCATION);
        mysqlToSqlState.put(1264, SQL_STATE_WARNING);
        mysqlToSqlState.put(1416, SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE);
        mysqlToSqlState.put(1690, SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE);
        mysqlToSqlState.put(1292, SQL_STATE_INVALID_DATETIME_FORMAT);
        mysqlToSqlState.put(1367, SQL_STATE_INVALID_DATETIME_FORMAT);
        mysqlToSqlState.put(1441, SQL_STATE_DATETIME_FIELD_OVERFLOW);
        mysqlToSqlState.put(1365, SQL_STATE_DIVISION_BY_ZERO);
        mysqlToSqlState.put(1325, SQL_STATE_INVALID_CURSOR_STATE);
        mysqlToSqlState.put(1326, SQL_STATE_INVALID_CURSOR_STATE);
        mysqlToSqlState.put(1179, SQL_STATE_INVALID_TRANSACTION_STATE);
        mysqlToSqlState.put(1207, SQL_STATE_INVALID_TRANSACTION_STATE);
        mysqlToSqlState.put(1045, SQL_STATE_INVALID_AUTH_SPEC);
        mysqlToSqlState.put(1698, SQL_STATE_INVALID_AUTH_SPEC);
        mysqlToSqlState.put(1873, SQL_STATE_INVALID_AUTH_SPEC);
        mysqlToSqlState.put(1758, SQL_STATE_INVALID_CONDITION_NUMBER);
        mysqlToSqlState.put(1046, SQL_STATE_INVALID_CATALOG_NAME);
        mysqlToSqlState.put(1058, SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST);
        mysqlToSqlState.put(1136, SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST);
        mysqlToSqlState.put(1050, SQL_STATE_ER_TABLE_EXISTS_ERROR);
        mysqlToSqlState.put(1051, SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND);
        mysqlToSqlState.put(1109, SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND);
        mysqlToSqlState.put(1146, SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND);
        mysqlToSqlState.put(1054, SQL_STATE_COLUMN_NOT_FOUND);
        mysqlToSqlState.put(1247, SQL_STATE_ER_BAD_FIELD_ERROR);
        mysqlToSqlState.put(1037, SQL_STATE_MEMORY_ALLOCATION_FAILURE);
        mysqlToSqlState.put(1038, SQL_STATE_MEMORY_ALLOCATION_FAILURE);
        mysqlToSqlState.put(1205, SQL_STATE_ROLLBACK_SERIALIZATION_FAILURE);
        mysqlToSqlState.put(1213, SQL_STATE_ROLLBACK_SERIALIZATION_FAILURE);
        mysqlToSql99State = new HashMap<Integer, String>();
        mysqlToSql99State.put(1249, SQL_STATE_WARNING);
        mysqlToSql99State.put(1261, SQL_STATE_WARNING);
        mysqlToSql99State.put(1262, SQL_STATE_WARNING);
        mysqlToSql99State.put(1265, SQL_STATE_WARNING);
        mysqlToSql99State.put(1263, SQL_STATE_WARNING);
        mysqlToSql99State.put(1264, SQL_STATE_WARNING);
        mysqlToSql99State.put(1311, SQL_STATE_WARNING);
        mysqlToSql99State.put(1642, SQL_STATE_WARNING);
        mysqlToSql99State.put(1329, SQL_STATE_NO_DATA);
        mysqlToSql99State.put(1643, SQL_STATE_NO_DATA);
        mysqlToSql99State.put(1040, SQL_STATE_CONNECTION_REJECTED);
        mysqlToSql99State.put(1251, SQL_STATE_CONNECTION_REJECTED);
        mysqlToSql99State.put(1042, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1043, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1047, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1053, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1080, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1081, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1152, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1153, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1154, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1155, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1156, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1157, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1158, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1159, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1160, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1161, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1184, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1189, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1190, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1218, SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(1312, SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSql99State.put(1314, SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSql99State.put(1335, SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSql99State.put(1336, SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSql99State.put(1415, SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSql99State.put(1845, SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSql99State.put(1846, SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSql99State.put(1044, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1049, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1055, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1056, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1057, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1059, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1061, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1063, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1064, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1065, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1066, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1067, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1068, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1069, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1070, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1071, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1072, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1073, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1074, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1075, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1083, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1084, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1090, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1091, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1101, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1102, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1103, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1104, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1106, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1107, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1110, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1112, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1113, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1115, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1118, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1120, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1121, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1131, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1132, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1133, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1139, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1140, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1141, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1142, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1143, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1144, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1145, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1147, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1148, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1149, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1162, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1163, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1164, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1166, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1167, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1170, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1171, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1172, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1173, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1176, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1177, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1178, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1203, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1211, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1226, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1227, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1230, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1231, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1232, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1234, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1235, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1239, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1248, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1250, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1252, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1253, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1280, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1281, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1286, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1304, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1305, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1308, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1309, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1310, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1313, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1315, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1316, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1318, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1319, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1320, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1322, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1323, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1324, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1327, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1330, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1331, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1332, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1333, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1337, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1338, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1370, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1403, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1407, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1410, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1413, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1414, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1425, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1426, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1427, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1437, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1439, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1453, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1458, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1460, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1461, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1463, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1582, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1583, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1584, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1630, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1641, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1687, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1701, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1222, SQL_STATE_CARDINALITY_VIOLATION);
        mysqlToSql99State.put(1241, SQL_STATE_CARDINALITY_VIOLATION);
        mysqlToSql99State.put(1242, SQL_STATE_CARDINALITY_VIOLATION);
        mysqlToSql99State.put(1022, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(1048, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(1052, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(1062, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(1169, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(1216, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(1217, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(1451, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(1452, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(1557, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(1586, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(1761, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(1762, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(1859, SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(1406, SQL_STATE_STRING_DATA_RIGHT_TRUNCATION);
        mysqlToSql99State.put(1416, SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE);
        mysqlToSql99State.put(1690, SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE);
        mysqlToSql99State.put(1292, SQL_STATE_INVALID_DATETIME_FORMAT);
        mysqlToSql99State.put(1367, SQL_STATE_INVALID_DATETIME_FORMAT);
        mysqlToSql99State.put(1441, SQL_STATE_DATETIME_FIELD_OVERFLOW);
        mysqlToSql99State.put(1365, SQL_STATE_DIVISION_BY_ZERO);
        mysqlToSql99State.put(1325, SQL_STATE_INVALID_CURSOR_STATE);
        mysqlToSql99State.put(1326, SQL_STATE_INVALID_CURSOR_STATE);
        mysqlToSql99State.put(1179, SQL_STATE_INVALID_TRANSACTION_STATE);
        mysqlToSql99State.put(1207, SQL_STATE_INVALID_TRANSACTION_STATE);
        mysqlToSql99State.put(1045, SQL_STATE_INVALID_AUTH_SPEC);
        mysqlToSql99State.put(1698, SQL_STATE_INVALID_AUTH_SPEC);
        mysqlToSql99State.put(1873, SQL_STATE_INVALID_AUTH_SPEC);
        mysqlToSql99State.put(1758, SQL_STATE_INVALID_CONDITION_NUMBER);
        mysqlToSql99State.put(1046, SQL_STATE_INVALID_CATALOG_NAME);
        mysqlToSql99State.put(1645, SQL_STATE_RESIGNAL_WHEN_HANDLER_NOT_ACTIVE);
        mysqlToSql99State.put(1887, SQL_STATE_STACKED_DIAGNOSTICS_ACCESSED_WITHOUT_ACTIVE_HANDLER);
        mysqlToSql99State.put(1339, SQL_STATE_CASE_NOT_FOUND_FOR_CASE_STATEMENT);
        mysqlToSql99State.put(1058, SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST);
        mysqlToSql99State.put(1136, SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST);
        mysqlToSql99State.put(1138, SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(1903, SQL_STATE_INVALID_LOGARITHM_ARGUMENT);
        mysqlToSql99State.put(1568, SQL_STATE_ACTIVE_SQL_TRANSACTION);
        mysqlToSql99State.put(1792, SQL_STATE_READ_ONLY_SQL_TRANSACTION);
        mysqlToSql99State.put(1303, SQL_STATE_SRE_PROHIBITED_SQL_STATEMENT_ATTEMPTED);
        mysqlToSql99State.put(1321, SQL_STATE_SRE_FUNCTION_EXECUTED_NO_RETURN_STATEMENT);
        mysqlToSql99State.put(1050, SQL_STATE_ER_TABLE_EXISTS_ERROR);
        mysqlToSql99State.put(1051, SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND);
        mysqlToSql99State.put(1109, SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND);
        mysqlToSql99State.put(1146, SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND);
        mysqlToSql99State.put(1082, SQL_STATE_ER_NO_SUCH_INDEX);
        mysqlToSql99State.put(1060, SQL_STATE_ER_DUP_FIELDNAME);
        mysqlToSql99State.put(1054, SQL_STATE_ER_BAD_FIELD_ERROR);
        mysqlToSql99State.put(1247, SQL_STATE_ER_BAD_FIELD_ERROR);
        mysqlToSql99State.put(1317, SQL_STATE_ER_QUERY_INTERRUPTED);
        mysqlToSql99State.put(1037, SQL_STATE_MEMORY_ALLOCATION_ERROR);
        mysqlToSql99State.put(1038, SQL_STATE_MEMORY_ALLOCATION_ERROR);
        mysqlToSql99State.put(1402, SQL_STATE_XA_RBROLLBACK);
        mysqlToSql99State.put(1614, SQL_STATE_XA_RBDEADLOCK);
        mysqlToSql99State.put(1613, SQL_STATE_XA_RBTIMEOUT);
        mysqlToSql99State.put(1401, SQL_STATE_XA_RMERR);
        mysqlToSql99State.put(1397, SQL_STATE_XAER_NOTA);
        mysqlToSql99State.put(1398, SQL_STATE_XAER_INVAL);
        mysqlToSql99State.put(1399, SQL_STATE_XAER_RMFAIL);
        mysqlToSql99State.put(1440, SQL_STATE_XAER_DUPID);
        mysqlToSql99State.put(1400, SQL_STATE_XAER_OUTSIDE);
        mysqlToSql99State.put(1205, SQL_STATE_ROLLBACK_SERIALIZATION_FAILURE);
        mysqlToSql99State.put(1213, SQL_STATE_ROLLBACK_SERIALIZATION_FAILURE);
    }
}


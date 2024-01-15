/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.AssertionFailedException;
import com.mysql.jdbc.ByteArrayRow;
import com.mysql.jdbc.DatabaseMetaData;
import com.mysql.jdbc.DatabaseMetaDataUsingInfoSchema;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ResultSetImpl;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.ResultSetRow;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.StatementImpl;
import com.mysql.jdbc.StringUtils;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.Wrapper;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CallableStatement
extends PreparedStatement
implements java.sql.CallableStatement {
    protected static final Constructor<?> JDBC_4_CSTMT_2_ARGS_CTOR;
    protected static final Constructor<?> JDBC_4_CSTMT_4_ARGS_CTOR;
    private static final int NOT_OUTPUT_PARAMETER_INDICATOR = Integer.MIN_VALUE;
    private static final String PARAMETER_NAMESPACE_PREFIX = "@com_mysql_jdbc_outparam_";
    private boolean callingStoredFunction = false;
    private ResultSetInternalMethods functionReturnValueResults;
    private boolean hasOutputParams = false;
    private ResultSetInternalMethods outputParameterResults;
    protected boolean outputParamWasNull = false;
    private int[] parameterIndexToRsIndex;
    protected CallableStatementParamInfo paramInfo;
    private CallableStatementParam returnValueParam;
    private int[] placeholderToParameterIndexMap;

    private static String mangleParameterName(String origParameterName) {
        if (origParameterName == null) {
            return null;
        }
        int offset = 0;
        if (origParameterName.length() > 0 && origParameterName.charAt(0) == '@') {
            offset = 1;
        }
        StringBuilder paramNameBuf = new StringBuilder(PARAMETER_NAMESPACE_PREFIX.length() + origParameterName.length());
        paramNameBuf.append(PARAMETER_NAMESPACE_PREFIX);
        paramNameBuf.append(origParameterName.substring(offset));
        return paramNameBuf.toString();
    }

    public CallableStatement(MySQLConnection conn, CallableStatementParamInfo paramInfo) throws SQLException {
        super(conn, paramInfo.nativeSql, paramInfo.catalogInUse);
        this.paramInfo = paramInfo;
        this.callingStoredFunction = this.paramInfo.isFunctionCall;
        if (this.callingStoredFunction) {
            ++this.parameterCount;
        }
        this.retrieveGeneratedKeys = true;
    }

    protected static CallableStatement getInstance(MySQLConnection conn, String sql, String catalog, boolean isFunctionCall) throws SQLException {
        if (!Util.isJdbc4()) {
            return new CallableStatement(conn, sql, catalog, isFunctionCall);
        }
        return (CallableStatement)Util.handleNewInstance(JDBC_4_CSTMT_4_ARGS_CTOR, new Object[]{conn, sql, catalog, isFunctionCall}, conn.getExceptionInterceptor());
    }

    protected static CallableStatement getInstance(MySQLConnection conn, CallableStatementParamInfo paramInfo) throws SQLException {
        if (!Util.isJdbc4()) {
            return new CallableStatement(conn, paramInfo);
        }
        return (CallableStatement)Util.handleNewInstance(JDBC_4_CSTMT_2_ARGS_CTOR, new Object[]{conn, paramInfo}, conn.getExceptionInterceptor());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void generateParameterMap() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.paramInfo == null) {
                return;
            }
            int parameterCountFromMetaData = this.paramInfo.getParameterCount();
            if (this.callingStoredFunction) {
                --parameterCountFromMetaData;
            }
            if (this.paramInfo != null && this.parameterCount != parameterCountFromMetaData) {
                int parenClosePos;
                int parenOpenPos;
                int startPos;
                this.placeholderToParameterIndexMap = new int[this.parameterCount];
                int n = startPos = this.callingStoredFunction ? StringUtils.indexOfIgnoreCase(this.originalSql, "SELECT") : StringUtils.indexOfIgnoreCase(this.originalSql, "CALL");
                if (startPos != -1 && (parenOpenPos = this.originalSql.indexOf(40, startPos + 4)) != -1 && (parenClosePos = StringUtils.indexOfIgnoreCase(parenOpenPos, this.originalSql, ")", "'", "'", StringUtils.SEARCH_MODE__ALL)) != -1) {
                    List<String> parsedParameters = StringUtils.split(this.originalSql.substring(parenOpenPos + 1, parenClosePos), ",", "'\"", "'\"", true);
                    int numParsedParameters = parsedParameters.size();
                    if (numParsedParameters != this.parameterCount) {
                        // empty if block
                    }
                    int placeholderCount = 0;
                    for (int i = 0; i < numParsedParameters; ++i) {
                        if (!parsedParameters.get(i).equals("?")) continue;
                        this.placeholderToParameterIndexMap[placeholderCount++] = i;
                    }
                }
            }
        }
    }

    public CallableStatement(MySQLConnection conn, String sql, String catalog, boolean isFunctionCall) throws SQLException {
        super(conn, sql, catalog);
        this.callingStoredFunction = isFunctionCall;
        if (!this.callingStoredFunction) {
            if (!StringUtils.startsWithIgnoreCaseAndWs(sql, "CALL")) {
                this.fakeParameterTypes(false);
            } else {
                this.determineParameterTypes();
            }
            this.generateParameterMap();
        } else {
            this.determineParameterTypes();
            this.generateParameterMap();
            ++this.parameterCount;
        }
        this.retrieveGeneratedKeys = true;
    }

    @Override
    public void addBatch() throws SQLException {
        this.setOutParams();
        super.addBatch();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private CallableStatementParam checkIsOutputParam(int paramIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.callingStoredFunction) {
                if (paramIndex == 1) {
                    if (this.returnValueParam == null) {
                        this.returnValueParam = new CallableStatementParam("", 0, false, true, 12, "VARCHAR", 0, 0, 2, 5);
                    }
                    return this.returnValueParam;
                }
                --paramIndex;
            }
            this.checkParameterIndexBounds(paramIndex);
            int localParamIndex = paramIndex - 1;
            if (this.placeholderToParameterIndexMap != null) {
                localParamIndex = this.placeholderToParameterIndexMap[localParamIndex];
            }
            CallableStatementParam paramDescriptor = this.paramInfo.getParameter(localParamIndex);
            if (this.connection.getNoAccessToProcedureBodies()) {
                paramDescriptor.isOut = true;
                paramDescriptor.isIn = true;
                paramDescriptor.inOutModifier = 2;
            } else if (!paramDescriptor.isOut) {
                throw SQLError.createSQLException(Messages.getString("CallableStatement.9") + paramIndex + Messages.getString("CallableStatement.10"), "S1009", this.getExceptionInterceptor());
            }
            this.hasOutputParams = true;
            return paramDescriptor;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void checkParameterIndexBounds(int paramIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.paramInfo.checkBounds(paramIndex);
        }
    }

    private void checkStreamability() throws SQLException {
        if (this.hasOutputParams && this.createStreamingResultSet()) {
            throw SQLError.createSQLException(Messages.getString("CallableStatement.14"), "S1C00", this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void clearParameters() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            super.clearParameters();
            try {
                if (this.outputParameterResults != null) {
                    this.outputParameterResults.close();
                }
                Object var3_2 = null;
                this.outputParameterResults = null;
            }
            catch (Throwable throwable) {
                Object var3_3 = null;
                this.outputParameterResults = null;
                throw throwable;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void fakeParameterTypes(boolean isReallyProcedure) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            Field[] fields = new Field[]{new Field("", "PROCEDURE_CAT", 1, 0), new Field("", "PROCEDURE_SCHEM", 1, 0), new Field("", "PROCEDURE_NAME", 1, 0), new Field("", "COLUMN_NAME", 1, 0), new Field("", "COLUMN_TYPE", 1, 0), new Field("", "DATA_TYPE", 5, 0), new Field("", "TYPE_NAME", 1, 0), new Field("", "PRECISION", 4, 0), new Field("", "LENGTH", 4, 0), new Field("", "SCALE", 5, 0), new Field("", "RADIX", 5, 0), new Field("", "NULLABLE", 5, 0), new Field("", "REMARKS", 1, 0)};
            String procName = isReallyProcedure ? this.extractProcedureName() : null;
            byte[] procNameAsBytes = null;
            try {
                procNameAsBytes = procName == null ? null : StringUtils.getBytes(procName, "UTF-8");
            }
            catch (UnsupportedEncodingException ueEx) {
                procNameAsBytes = StringUtils.s2b(procName, this.connection);
            }
            ArrayList<ResultSetRow> resultRows = new ArrayList<ResultSetRow>();
            for (int i = 0; i < this.parameterCount; ++i) {
                byte[][] row = new byte[][]{null, null, procNameAsBytes, StringUtils.s2b(String.valueOf(i), this.connection), StringUtils.s2b(String.valueOf(1), this.connection), StringUtils.s2b(String.valueOf(12), this.connection), StringUtils.s2b("VARCHAR", this.connection), StringUtils.s2b(Integer.toString(65535), this.connection), StringUtils.s2b(Integer.toString(65535), this.connection), StringUtils.s2b(Integer.toString(0), this.connection), StringUtils.s2b(Integer.toString(10), this.connection), StringUtils.s2b(Integer.toString(2), this.connection), null};
                resultRows.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
            }
            ResultSet paramTypesRs = DatabaseMetaData.buildResultSet(fields, resultRows, this.connection);
            this.convertGetProcedureColumnsToInternalDescriptors(paramTypesRs);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void determineParameterTypes() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            SQLException sqlExRethrow2;
            block20: {
                ResultSet paramTypesRs = null;
                try {
                    String procName = this.extractProcedureName();
                    String quotedId = "";
                    try {
                        quotedId = this.connection.supportsQuotedIdentifiers() ? this.connection.getMetaData().getIdentifierQuoteString() : "";
                    }
                    catch (SQLException sqlEx) {
                        AssertionFailedException.shouldNotHappen(sqlEx);
                    }
                    List<String> parseList = StringUtils.splitDBdotName(procName, "", quotedId, this.connection.isNoBackslashEscapesSet());
                    String tmpCatalog = "";
                    if (parseList.size() == 2) {
                        tmpCatalog = parseList.get(0);
                        procName = parseList.get(1);
                    }
                    java.sql.DatabaseMetaData dbmd = this.connection.getMetaData();
                    boolean useCatalog = false;
                    if (tmpCatalog.length() <= 0) {
                        useCatalog = true;
                    }
                    paramTypesRs = dbmd.getProcedureColumns(this.connection.versionMeetsMinimum(5, 0, 2) && useCatalog ? this.currentCatalog : tmpCatalog, null, procName, "%");
                    boolean hasResults = false;
                    try {
                        if (paramTypesRs.next()) {
                            paramTypesRs.previous();
                            hasResults = true;
                        }
                    }
                    catch (Exception e) {
                        // empty catch block
                    }
                    if (hasResults) {
                        this.convertGetProcedureColumnsToInternalDescriptors(paramTypesRs);
                    } else {
                        this.fakeParameterTypes(true);
                    }
                    Object var12_12 = null;
                    sqlExRethrow2 = null;
                    if (paramTypesRs == null) break block20;
                }
                catch (Throwable throwable) {
                    Object var12_13 = null;
                    SQLException sqlExRethrow2 = null;
                    if (paramTypesRs != null) {
                        try {
                            paramTypesRs.close();
                        }
                        catch (SQLException sqlEx) {
                            sqlExRethrow2 = sqlEx;
                        }
                        paramTypesRs = null;
                    }
                    if (sqlExRethrow2 != null) {
                        throw sqlExRethrow2;
                    }
                    throw throwable;
                }
                try {
                    paramTypesRs.close();
                }
                catch (SQLException sqlEx) {
                    sqlExRethrow2 = sqlEx;
                }
                paramTypesRs = null;
            }
            if (sqlExRethrow2 != null) {
                throw sqlExRethrow2;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void convertGetProcedureColumnsToInternalDescriptors(ResultSet paramTypesRs) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.paramInfo = new CallableStatementParamInfo(paramTypesRs);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean execute() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            boolean returnVal = false;
            this.checkStreamability();
            this.setInOutParamsOnServer();
            this.setOutParams();
            returnVal = super.execute();
            if (this.callingStoredFunction) {
                this.functionReturnValueResults = this.results;
                this.functionReturnValueResults.next();
                this.results = null;
            }
            this.retrieveOutParams();
            if (!this.callingStoredFunction) {
                return returnVal;
            }
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ResultSet executeQuery() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.checkStreamability();
            ResultSet execResults = null;
            this.setInOutParamsOnServer();
            this.setOutParams();
            execResults = super.executeQuery();
            this.retrieveOutParams();
            return execResults;
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        return Util.truncateAndConvertToInt(this.executeLargeUpdate());
    }

    private String extractProcedureName() throws SQLException {
        String sanitizedSql = StringUtils.stripComments(this.originalSql, "`\"'", "`\"'", true, false, true, true);
        int endCallIndex = StringUtils.indexOfIgnoreCase(sanitizedSql, "CALL ");
        int offset = 5;
        if (endCallIndex == -1) {
            endCallIndex = StringUtils.indexOfIgnoreCase(sanitizedSql, "SELECT ");
            offset = 7;
        }
        if (endCallIndex != -1) {
            char c;
            StringBuilder nameBuf = new StringBuilder();
            String trimmedStatement = sanitizedSql.substring(endCallIndex + offset).trim();
            int statementLength = trimmedStatement.length();
            for (int i = 0; i < statementLength && !Character.isWhitespace(c = trimmedStatement.charAt(i)) && c != '(' && c != '?'; ++i) {
                nameBuf.append(c);
            }
            return nameBuf.toString();
        }
        throw SQLError.createSQLException(Messages.getString("CallableStatement.1"), "S1000", this.getExceptionInterceptor());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected String fixParameterName(String paramNameIn) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (!(paramNameIn != null && paramNameIn.length() != 0 || this.hasParametersView())) {
                throw SQLError.createSQLException(Messages.getString("CallableStatement.0") + paramNameIn == null ? Messages.getString("CallableStatement.15") : Messages.getString("CallableStatement.16"), "S1009", this.getExceptionInterceptor());
            }
            if (paramNameIn == null && this.hasParametersView()) {
                paramNameIn = "nullpn";
            }
            if (this.connection.getNoAccessToProcedureBodies()) {
                throw SQLError.createSQLException("No access to parameters by name when connection has been configured not to access procedure bodies", "S1009", this.getExceptionInterceptor());
            }
            return CallableStatement.mangleParameterName(paramNameIn);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Array getArray(int i) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(i);
            Array retValue = rs.getArray(this.mapOutputParameterIndexToRsIndex(i));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Array getArray(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Array retValue = rs.getArray(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            BigDecimal retValue = rs.getBigDecimal(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    @Deprecated
    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            BigDecimal retValue = rs.getBigDecimal(this.mapOutputParameterIndexToRsIndex(parameterIndex), scale);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            BigDecimal retValue = rs.getBigDecimal(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Blob getBlob(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Blob retValue = rs.getBlob(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Blob getBlob(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Blob retValue = rs.getBlob(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean getBoolean(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            boolean retValue = rs.getBoolean(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean getBoolean(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            boolean retValue = rs.getBoolean(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public byte getByte(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            byte retValue = rs.getByte(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public byte getByte(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            byte retValue = rs.getByte(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public byte[] getBytes(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            byte[] retValue = rs.getBytes(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public byte[] getBytes(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            byte[] retValue = rs.getBytes(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Clob getClob(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Clob retValue = rs.getClob(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Clob getClob(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Clob retValue = rs.getClob(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Date getDate(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Date retValue = rs.getDate(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Date retValue = rs.getDate(this.mapOutputParameterIndexToRsIndex(parameterIndex), cal);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Date getDate(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Date retValue = rs.getDate(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Date getDate(String parameterName, Calendar cal) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Date retValue = rs.getDate(this.fixParameterName(parameterName), cal);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public double getDouble(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            double retValue = rs.getDouble(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public double getDouble(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            double retValue = rs.getDouble(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public float getFloat(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            float retValue = rs.getFloat(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public float getFloat(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            float retValue = rs.getFloat(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getInt(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            int retValue = rs.getInt(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getInt(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            int retValue = rs.getInt(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long getLong(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            long retValue = rs.getLong(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long getLong(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            long retValue = rs.getLong(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    protected int getNamedParamIndex(String paramName, boolean forOut) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.connection.getNoAccessToProcedureBodies()) {
                throw SQLError.createSQLException("No access to parameters by name when connection has been configured not to access procedure bodies", "S1009", this.getExceptionInterceptor());
            }
            if (paramName == null || paramName.length() == 0) {
                throw SQLError.createSQLException(Messages.getString("CallableStatement.2"), "S1009", this.getExceptionInterceptor());
            }
            if (this.paramInfo == null) {
                throw SQLError.createSQLException(Messages.getString("CallableStatement.3") + paramName + Messages.getString("CallableStatement.4"), "S1009", this.getExceptionInterceptor());
            }
            CallableStatementParam namedParamInfo = this.paramInfo.getParameter(paramName);
            if (forOut && !namedParamInfo.isOut) {
                throw SQLError.createSQLException(Messages.getString("CallableStatement.5") + paramName + Messages.getString("CallableStatement.6"), "S1009", this.getExceptionInterceptor());
            }
            if (this.placeholderToParameterIndexMap == null) {
                return namedParamInfo.index + 1;
            }
            for (int i = 0; i < this.placeholderToParameterIndexMap.length; ++i) {
                if (this.placeholderToParameterIndexMap[i] != namedParamInfo.index) continue;
                return i + 1;
            }
            throw SQLError.createSQLException("Can't find local placeholder mapping for parameter named \"" + paramName + "\".", "S1009", this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Object getObject(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            CallableStatementParam paramDescriptor = this.checkIsOutputParam(parameterIndex);
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Object retVal = rs.getObjectStoredProc(this.mapOutputParameterIndexToRsIndex(parameterIndex), paramDescriptor.desiredJdbcType);
            this.outputParamWasNull = rs.wasNull();
            return retVal;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Object retVal = rs.getObject(this.mapOutputParameterIndexToRsIndex(parameterIndex), map);
            this.outputParamWasNull = rs.wasNull();
            return retVal;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Object getObject(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Object retValue = rs.getObject(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Object retValue = rs.getObject(this.fixParameterName(parameterName), map);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            T retVal = ((ResultSetImpl)rs).getObject(this.mapOutputParameterIndexToRsIndex(parameterIndex), type);
            this.outputParamWasNull = rs.wasNull();
            return retVal;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            T retValue = ((ResultSetImpl)rs).getObject(this.fixParameterName(parameterName), type);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected ResultSetInternalMethods getOutputParameters(int paramIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.outputParamWasNull = false;
            if (paramIndex == 1 && this.callingStoredFunction && this.returnValueParam != null) {
                return this.functionReturnValueResults;
            }
            if (this.outputParameterResults == null) {
                if (this.paramInfo.numberOfParameters() == 0) {
                    throw SQLError.createSQLException(Messages.getString("CallableStatement.7"), "S1009", this.getExceptionInterceptor());
                }
                throw SQLError.createSQLException(Messages.getString("CallableStatement.8"), "S1000", this.getExceptionInterceptor());
            }
            return this.outputParameterResults;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.placeholderToParameterIndexMap == null) {
                return this.paramInfo;
            }
            return new CallableStatementParamInfo(this.paramInfo);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Ref getRef(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Ref retValue = rs.getRef(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Ref getRef(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Ref retValue = rs.getRef(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public short getShort(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            short retValue = rs.getShort(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public short getShort(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            short retValue = rs.getShort(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getString(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            String retValue = rs.getString(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getString(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            String retValue = rs.getString(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Time getTime(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Time retValue = rs.getTime(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Time retValue = rs.getTime(this.mapOutputParameterIndexToRsIndex(parameterIndex), cal);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Time getTime(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Time retValue = rs.getTime(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Time getTime(String parameterName, Calendar cal) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Time retValue = rs.getTime(this.fixParameterName(parameterName), cal);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Timestamp retValue = rs.getTimestamp(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Timestamp retValue = rs.getTimestamp(this.mapOutputParameterIndexToRsIndex(parameterIndex), cal);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Timestamp getTimestamp(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Timestamp retValue = rs.getTimestamp(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Timestamp retValue = rs.getTimestamp(this.fixParameterName(parameterName), cal);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public URL getURL(int parameterIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            URL retValue = rs.getURL(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public URL getURL(String parameterName) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            URL retValue = rs.getURL(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected int mapOutputParameterIndexToRsIndex(int paramIndex) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            int rsIndex;
            if (this.returnValueParam != null && paramIndex == 1) {
                return 1;
            }
            this.checkParameterIndexBounds(paramIndex);
            int localParamIndex = paramIndex - 1;
            if (this.placeholderToParameterIndexMap != null) {
                localParamIndex = this.placeholderToParameterIndexMap[localParamIndex];
            }
            if ((rsIndex = this.parameterIndexToRsIndex[localParamIndex]) == Integer.MIN_VALUE) {
                throw SQLError.createSQLException(Messages.getString("CallableStatement.21") + paramIndex + Messages.getString("CallableStatement.22"), "S1009", this.getExceptionInterceptor());
            }
            return rsIndex + 1;
        }
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
        CallableStatementParam paramDescriptor = this.checkIsOutputParam(parameterIndex);
        paramDescriptor.desiredJdbcType = sqlType;
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
        this.registerOutParameter(parameterIndex, sqlType);
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
        this.checkIsOutputParam(parameterIndex);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.registerOutParameter(this.getNamedParamIndex(parameterName, true), sqlType);
        }
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
        this.registerOutParameter(this.getNamedParamIndex(parameterName, true), sqlType);
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
        this.registerOutParameter(this.getNamedParamIndex(parameterName, true), sqlType, typeName);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void retrieveOutParams() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            block16: {
                int numParameters = this.paramInfo.numberOfParameters();
                this.parameterIndexToRsIndex = new int[numParameters];
                for (int i = 0; i < numParameters; ++i) {
                    this.parameterIndexToRsIndex[i] = Integer.MIN_VALUE;
                }
                int localParamIndex = 0;
                if (numParameters > 0) {
                    StringBuilder outParameterQuery = new StringBuilder("SELECT ");
                    boolean firstParam = true;
                    boolean hadOutputParams = false;
                    Iterator<CallableStatementParam> paramIter = this.paramInfo.iterator();
                    while (paramIter.hasNext()) {
                        CallableStatementParam retrParamInfo = paramIter.next();
                        if (!retrParamInfo.isOut) continue;
                        hadOutputParams = true;
                        this.parameterIndexToRsIndex[retrParamInfo.index] = localParamIndex++;
                        if (retrParamInfo.paramName == null && this.hasParametersView()) {
                            retrParamInfo.paramName = "nullnp" + retrParamInfo.index;
                        }
                        String outParameterName = CallableStatement.mangleParameterName(retrParamInfo.paramName);
                        if (!firstParam) {
                            outParameterQuery.append(",");
                        } else {
                            firstParam = false;
                        }
                        if (!outParameterName.startsWith("@")) {
                            outParameterQuery.append('@');
                        }
                        outParameterQuery.append(outParameterName);
                    }
                    if (hadOutputParams) {
                        Statement outParameterStmt = null;
                        ResultSet outParamRs = null;
                        try {
                            outParameterStmt = this.connection.createStatement();
                            outParamRs = outParameterStmt.executeQuery(outParameterQuery.toString());
                            this.outputParameterResults = ((ResultSetInternalMethods)outParamRs).copy();
                            if (!this.outputParameterResults.next()) {
                                this.outputParameterResults.close();
                                this.outputParameterResults = null;
                            }
                            Object var11_10 = null;
                            if (outParameterStmt == null) return;
                        }
                        catch (Throwable throwable) {
                            Object var11_11 = null;
                            if (outParameterStmt == null) throw throwable;
                            outParameterStmt.close();
                            throw throwable;
                        }
                        outParameterStmt.close();
                        {
                            break block16;
                        }
                    }
                    this.outputParameterResults = null;
                } else {
                    this.outputParameterResults = null;
                }
            }
            return;
        }
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
        this.setAsciiStream(this.getNamedParamIndex(parameterName, false), x, length);
    }

    @Override
    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        this.setBigDecimal(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
        this.setBinaryStream(this.getNamedParamIndex(parameterName, false), x, length);
    }

    @Override
    public void setBoolean(String parameterName, boolean x) throws SQLException {
        this.setBoolean(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setByte(String parameterName, byte x) throws SQLException {
        this.setByte(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setBytes(String parameterName, byte[] x) throws SQLException {
        this.setBytes(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
        this.setCharacterStream(this.getNamedParamIndex(parameterName, false), reader, length);
    }

    @Override
    public void setDate(String parameterName, Date x) throws SQLException {
        this.setDate(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
        this.setDate(this.getNamedParamIndex(parameterName, false), x, cal);
    }

    @Override
    public void setDouble(String parameterName, double x) throws SQLException {
        this.setDouble(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setFloat(String parameterName, float x) throws SQLException {
        this.setFloat(this.getNamedParamIndex(parameterName, false), x);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setInOutParamsOnServer() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.paramInfo.numParameters > 0) {
                Iterator<CallableStatementParam> paramIter = this.paramInfo.iterator();
                while (paramIter.hasNext()) {
                    Object var10_9;
                    CallableStatementParam inParamInfo = paramIter.next();
                    if (!inParamInfo.isOut || !inParamInfo.isIn) continue;
                    if (inParamInfo.paramName == null && this.hasParametersView()) {
                        inParamInfo.paramName = "nullnp" + inParamInfo.index;
                    }
                    String inOutParameterName = CallableStatement.mangleParameterName(inParamInfo.paramName);
                    StringBuilder queryBuf = new StringBuilder(4 + inOutParameterName.length() + 1 + 1);
                    queryBuf.append("SET ");
                    queryBuf.append(inOutParameterName);
                    queryBuf.append("=?");
                    StatementImpl setPstmt = null;
                    try {
                        setPstmt = ((Wrapper)((Object)this.connection.clientPrepareStatement(queryBuf.toString()))).unwrap(PreparedStatement.class);
                        if (this.isNull[inParamInfo.index]) {
                            ((PreparedStatement)setPstmt).setBytesNoEscapeNoQuotes(1, "NULL".getBytes());
                        } else {
                            byte[] parameterAsBytes = this.getBytesRepresentation(inParamInfo.index);
                            if (parameterAsBytes != null) {
                                if (parameterAsBytes.length > 8 && parameterAsBytes[0] == 95 && parameterAsBytes[1] == 98 && parameterAsBytes[2] == 105 && parameterAsBytes[3] == 110 && parameterAsBytes[4] == 97 && parameterAsBytes[5] == 114 && parameterAsBytes[6] == 121 && parameterAsBytes[7] == 39) {
                                    ((PreparedStatement)setPstmt).setBytesNoEscapeNoQuotes(1, parameterAsBytes);
                                } else {
                                    int sqlType = inParamInfo.desiredJdbcType;
                                    switch (sqlType) {
                                        case -7: 
                                        case -4: 
                                        case -3: 
                                        case -2: 
                                        case 2000: 
                                        case 2004: {
                                            ((PreparedStatement)setPstmt).setBytes(1, parameterAsBytes);
                                            break;
                                        }
                                        default: {
                                            ((PreparedStatement)setPstmt).setBytesNoEscape(1, parameterAsBytes);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                ((PreparedStatement)setPstmt).setNull(1, 0);
                            }
                        }
                        ((PreparedStatement)setPstmt).executeUpdate();
                        var10_9 = null;
                        if (setPstmt == null) continue;
                    }
                    catch (Throwable throwable) {
                        var10_9 = null;
                        if (setPstmt != null) {
                            setPstmt.close();
                        }
                        throw throwable;
                    }
                    setPstmt.close();
                    {
                    }
                }
            }
        }
    }

    @Override
    public void setInt(String parameterName, int x) throws SQLException {
        this.setInt(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setLong(String parameterName, long x) throws SQLException {
        this.setLong(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setNull(String parameterName, int sqlType) throws SQLException {
        this.setNull(this.getNamedParamIndex(parameterName, false), sqlType);
    }

    @Override
    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        this.setNull(this.getNamedParamIndex(parameterName, false), sqlType, typeName);
    }

    @Override
    public void setObject(String parameterName, Object x) throws SQLException {
        this.setObject(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
        this.setObject(this.getNamedParamIndex(parameterName, false), x, targetSqlType);
    }

    @Override
    public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setOutParams() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            if (this.paramInfo.numParameters > 0) {
                Iterator<CallableStatementParam> paramIter = this.paramInfo.iterator();
                while (paramIter.hasNext()) {
                    CallableStatementParam outParamInfo = paramIter.next();
                    if (this.callingStoredFunction || !outParamInfo.isOut) continue;
                    if (outParamInfo.paramName == null && this.hasParametersView()) {
                        outParamInfo.paramName = "nullnp" + outParamInfo.index;
                    }
                    String outParameterName = CallableStatement.mangleParameterName(outParamInfo.paramName);
                    int outParamIndex = 0;
                    if (this.placeholderToParameterIndexMap == null) {
                        outParamIndex = outParamInfo.index + 1;
                    } else {
                        boolean found = false;
                        for (int i = 0; i < this.placeholderToParameterIndexMap.length; ++i) {
                            if (this.placeholderToParameterIndexMap[i] != outParamInfo.index) continue;
                            outParamIndex = i + 1;
                            found = true;
                            break;
                        }
                        if (!found) {
                            throw SQLError.createSQLException(Messages.getString("CallableStatement.21") + outParamInfo.paramName + Messages.getString("CallableStatement.22"), "S1009", this.getExceptionInterceptor());
                        }
                    }
                    this.setBytesNoEscapeNoQuotes(outParamIndex, StringUtils.getBytes(outParameterName, this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor()));
                }
            }
        }
    }

    @Override
    public void setShort(String parameterName, short x) throws SQLException {
        this.setShort(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setString(String parameterName, String x) throws SQLException {
        this.setString(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setTime(String parameterName, Time x) throws SQLException {
        this.setTime(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
        this.setTime(this.getNamedParamIndex(parameterName, false), x, cal);
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        this.setTimestamp(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
        this.setTimestamp(this.getNamedParamIndex(parameterName, false), x, cal);
    }

    @Override
    public void setURL(String parameterName, URL val) throws SQLException {
        this.setURL(this.getNamedParamIndex(parameterName, false), val);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean wasNull() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            return this.outputParamWasNull;
        }
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return Util.truncateAndConvertToInt(this.executeLargeBatch());
    }

    @Override
    protected int getParameterIndexOffset() {
        if (this.callingStoredFunction) {
            return -1;
        }
        return super.getParameterIndexOffset();
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        this.setAsciiStream(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
        this.setAsciiStream(this.getNamedParamIndex(parameterName, false), x, length);
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        this.setBinaryStream(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
        this.setBinaryStream(this.getNamedParamIndex(parameterName, false), x, length);
    }

    @Override
    public void setBlob(String parameterName, Blob x) throws SQLException {
        this.setBlob(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        this.setBlob(this.getNamedParamIndex(parameterName, false), inputStream);
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        this.setBlob(this.getNamedParamIndex(parameterName, false), inputStream, length);
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        this.setCharacterStream(this.getNamedParamIndex(parameterName, false), reader);
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        this.setCharacterStream(this.getNamedParamIndex(parameterName, false), reader, length);
    }

    @Override
    public void setClob(String parameterName, Clob x) throws SQLException {
        this.setClob(this.getNamedParamIndex(parameterName, false), x);
    }

    @Override
    public void setClob(String parameterName, Reader reader) throws SQLException {
        this.setClob(this.getNamedParamIndex(parameterName, false), reader);
    }

    @Override
    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        this.setClob(this.getNamedParamIndex(parameterName, false), reader, length);
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
        this.setNCharacterStream(this.getNamedParamIndex(parameterName, false), value);
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
        this.setNCharacterStream(this.getNamedParamIndex(parameterName, false), value, length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private boolean checkReadOnlyProcedure() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            block20: {
                Statement ps;
                ResultSet rs;
                block17: {
                    boolean bl;
                    block18: {
                        if (this.connection.getNoAccessToProcedureBodies()) {
                            return false;
                        }
                        if (this.paramInfo.isReadOnlySafeChecked) {
                            return this.paramInfo.isReadOnlySafeProcedure;
                        }
                        rs = null;
                        ps = null;
                        try {
                            try {
                                String sqlDataAccess;
                                String procName = this.extractProcedureName();
                                String catalog = this.currentCatalog;
                                if (procName.indexOf(".") != -1) {
                                    catalog = procName.substring(0, procName.indexOf("."));
                                    if (StringUtils.startsWithIgnoreCaseAndWs(catalog, "`") && catalog.trim().endsWith("`")) {
                                        catalog = catalog.substring(1, catalog.length() - 1);
                                    }
                                    procName = procName.substring(procName.indexOf(".") + 1);
                                    procName = StringUtils.toString(StringUtils.stripEnclosure(StringUtils.getBytes(procName), "`", "`"));
                                }
                                ps = this.connection.prepareStatement("SELECT SQL_DATA_ACCESS FROM information_schema.routines WHERE routine_schema = ? AND routine_name = ?");
                                ps.setMaxRows(0);
                                ps.setFetchSize(0);
                                ps.setString(1, catalog);
                                ps.setString(2, procName);
                                rs = ps.executeQuery();
                                if (!rs.next() || !"READS SQL DATA".equalsIgnoreCase(sqlDataAccess = rs.getString(1)) && !"NO SQL".equalsIgnoreCase(sqlDataAccess)) break block17;
                                CallableStatementParamInfo callableStatementParamInfo = this.paramInfo;
                                synchronized (callableStatementParamInfo) {
                                    this.paramInfo.isReadOnlySafeChecked = true;
                                    this.paramInfo.isReadOnlySafeProcedure = true;
                                }
                                bl = true;
                                Object var10_10 = null;
                                if (rs == null) break block18;
                            }
                            catch (SQLException e) {
                                Object var10_12 = null;
                                if (rs != null) {
                                    rs.close();
                                }
                                if (ps == null) break block20;
                                ps.close();
                                break block20;
                            }
                        }
                        catch (Throwable throwable) {
                            Object var10_13 = null;
                            if (rs != null) {
                                rs.close();
                            }
                            if (ps == null) throw throwable;
                            ps.close();
                            throw throwable;
                        }
                        rs.close();
                    }
                    if (ps == null) return bl;
                    ps.close();
                    return bl;
                }
                Object var10_11 = null;
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
            this.paramInfo.isReadOnlySafeChecked = false;
            this.paramInfo.isReadOnlySafeProcedure = false;
            return false;
        }
    }

    @Override
    protected boolean checkReadOnlySafeStatement() throws SQLException {
        return super.checkReadOnlySafeStatement() || this.checkReadOnlyProcedure();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean hasParametersView() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            try {
                if (this.connection.versionMeetsMinimum(5, 5, 0)) {
                    DatabaseMetaDataUsingInfoSchema dbmd1 = new DatabaseMetaDataUsingInfoSchema(this.connection, this.connection.getCatalog());
                    return dbmd1.gethasParametersView();
                }
                return false;
            }
            catch (SQLException e) {
                return false;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long executeLargeUpdate() throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            long returnVal = -1L;
            this.checkStreamability();
            if (this.callingStoredFunction) {
                this.execute();
                return -1L;
            }
            this.setInOutParamsOnServer();
            this.setOutParams();
            returnVal = super.executeLargeUpdate();
            this.retrieveOutParams();
            return returnVal;
        }
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        if (this.hasOutputParams) {
            throw SQLError.createSQLException("Can't call executeBatch() on CallableStatement with OUTPUT parameters", "S1009", this.getExceptionInterceptor());
        }
        return super.executeLargeBatch();
    }

    static {
        if (Util.isJdbc4()) {
            try {
                String jdbc4ClassName = Util.isJdbc42() ? "com.mysql.jdbc.JDBC42CallableStatement" : "com.mysql.jdbc.JDBC4CallableStatement";
                JDBC_4_CSTMT_2_ARGS_CTOR = Class.forName(jdbc4ClassName).getConstructor(MySQLConnection.class, CallableStatementParamInfo.class);
                JDBC_4_CSTMT_4_ARGS_CTOR = Class.forName(jdbc4ClassName).getConstructor(MySQLConnection.class, String.class, String.class, Boolean.TYPE);
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
            JDBC_4_CSTMT_4_ARGS_CTOR = null;
            JDBC_4_CSTMT_2_ARGS_CTOR = null;
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    protected class CallableStatementParamInfo
    implements ParameterMetaData {
        String catalogInUse;
        boolean isFunctionCall;
        String nativeSql;
        int numParameters;
        List<CallableStatementParam> parameterList;
        Map<String, CallableStatementParam> parameterMap;
        boolean isReadOnlySafeProcedure = false;
        boolean isReadOnlySafeChecked = false;

        CallableStatementParamInfo(CallableStatementParamInfo fullParamInfo) {
            this.nativeSql = CallableStatement.this.originalSql;
            this.catalogInUse = CallableStatement.this.currentCatalog;
            this.isFunctionCall = fullParamInfo.isFunctionCall;
            int[] localParameterMap = CallableStatement.this.placeholderToParameterIndexMap;
            int parameterMapLength = localParameterMap.length;
            this.isReadOnlySafeProcedure = fullParamInfo.isReadOnlySafeProcedure;
            this.isReadOnlySafeChecked = fullParamInfo.isReadOnlySafeChecked;
            this.parameterList = new ArrayList<CallableStatementParam>(fullParamInfo.numParameters);
            this.parameterMap = new HashMap<String, CallableStatementParam>(fullParamInfo.numParameters);
            if (this.isFunctionCall) {
                this.parameterList.add(fullParamInfo.parameterList.get(0));
            }
            int offset = this.isFunctionCall ? 1 : 0;
            for (int i = 0; i < parameterMapLength; ++i) {
                if (localParameterMap[i] == 0) continue;
                CallableStatementParam param = fullParamInfo.parameterList.get(localParameterMap[i] + offset);
                this.parameterList.add(param);
                this.parameterMap.put(param.paramName, param);
            }
            this.numParameters = this.parameterList.size();
        }

        CallableStatementParamInfo(ResultSet paramTypesRs) throws SQLException {
            boolean hadRows = paramTypesRs.last();
            this.nativeSql = CallableStatement.this.originalSql;
            this.catalogInUse = CallableStatement.this.currentCatalog;
            this.isFunctionCall = CallableStatement.this.callingStoredFunction;
            if (hadRows) {
                this.numParameters = paramTypesRs.getRow();
                this.parameterList = new ArrayList<CallableStatementParam>(this.numParameters);
                this.parameterMap = new HashMap<String, CallableStatementParam>(this.numParameters);
                paramTypesRs.beforeFirst();
                this.addParametersFromDBMD(paramTypesRs);
            } else {
                this.numParameters = 0;
            }
            if (this.isFunctionCall) {
                ++this.numParameters;
            }
        }

        private void addParametersFromDBMD(ResultSet paramTypesRs) throws SQLException {
            int i = 0;
            while (paramTypesRs.next()) {
                String paramName = paramTypesRs.getString(4);
                int inOutModifier = paramTypesRs.getInt(5);
                boolean isOutParameter = false;
                boolean isInParameter = false;
                if (i == 0 && this.isFunctionCall) {
                    isOutParameter = true;
                    isInParameter = false;
                } else if (inOutModifier == 2) {
                    isOutParameter = true;
                    isInParameter = true;
                } else if (inOutModifier == 1) {
                    isOutParameter = false;
                    isInParameter = true;
                } else if (inOutModifier == 4) {
                    isOutParameter = true;
                    isInParameter = false;
                }
                int jdbcType = paramTypesRs.getInt(6);
                String typeName = paramTypesRs.getString(7);
                int precision = paramTypesRs.getInt(8);
                int scale = paramTypesRs.getInt(10);
                short nullability = paramTypesRs.getShort(12);
                CallableStatementParam paramInfoToAdd = new CallableStatementParam(paramName, i++, isInParameter, isOutParameter, jdbcType, typeName, precision, scale, nullability, inOutModifier);
                this.parameterList.add(paramInfoToAdd);
                this.parameterMap.put(paramName, paramInfoToAdd);
            }
        }

        protected void checkBounds(int paramIndex) throws SQLException {
            int localParamIndex = paramIndex - 1;
            if (paramIndex < 0 || localParamIndex >= this.numParameters) {
                throw SQLError.createSQLException(Messages.getString("CallableStatement.11") + paramIndex + Messages.getString("CallableStatement.12") + this.numParameters + Messages.getString("CallableStatement.13"), "S1009", CallableStatement.this.getExceptionInterceptor());
            }
        }

        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        CallableStatementParam getParameter(int index) {
            return this.parameterList.get(index);
        }

        CallableStatementParam getParameter(String name) {
            return this.parameterMap.get(name);
        }

        @Override
        public String getParameterClassName(int arg0) throws SQLException {
            String mysqlTypeName = this.getParameterTypeName(arg0);
            boolean isBinaryOrBlob = StringUtils.indexOfIgnoreCase(mysqlTypeName, "BLOB") != -1 || StringUtils.indexOfIgnoreCase(mysqlTypeName, "BINARY") != -1;
            boolean isUnsigned = StringUtils.indexOfIgnoreCase(mysqlTypeName, "UNSIGNED") != -1;
            int mysqlTypeIfKnown = 0;
            if (StringUtils.startsWithIgnoreCase(mysqlTypeName, "MEDIUMINT")) {
                mysqlTypeIfKnown = 9;
            }
            return ResultSetMetaData.getClassNameForJavaType(this.getParameterType(arg0), isUnsigned, mysqlTypeIfKnown, isBinaryOrBlob, false, CallableStatement.this.connection.getYearIsDateType());
        }

        @Override
        public int getParameterCount() throws SQLException {
            if (this.parameterList == null) {
                return 0;
            }
            return this.parameterList.size();
        }

        @Override
        public int getParameterMode(int arg0) throws SQLException {
            this.checkBounds(arg0);
            return this.getParameter((int)(arg0 - 1)).inOutModifier;
        }

        @Override
        public int getParameterType(int arg0) throws SQLException {
            this.checkBounds(arg0);
            return this.getParameter((int)(arg0 - 1)).jdbcType;
        }

        @Override
        public String getParameterTypeName(int arg0) throws SQLException {
            this.checkBounds(arg0);
            return this.getParameter((int)(arg0 - 1)).typeName;
        }

        @Override
        public int getPrecision(int arg0) throws SQLException {
            this.checkBounds(arg0);
            return this.getParameter((int)(arg0 - 1)).precision;
        }

        @Override
        public int getScale(int arg0) throws SQLException {
            this.checkBounds(arg0);
            return this.getParameter((int)(arg0 - 1)).scale;
        }

        @Override
        public int isNullable(int arg0) throws SQLException {
            this.checkBounds(arg0);
            return this.getParameter((int)(arg0 - 1)).nullability;
        }

        @Override
        public boolean isSigned(int arg0) throws SQLException {
            this.checkBounds(arg0);
            return false;
        }

        Iterator<CallableStatementParam> iterator() {
            return this.parameterList.iterator();
        }

        int numberOfParameters() {
            return this.numParameters;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            CallableStatement.this.checkClosed();
            return iface.isInstance(this);
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            try {
                return iface.cast(this);
            }
            catch (ClassCastException cce) {
                throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", CallableStatement.this.getExceptionInterceptor());
            }
        }
    }

    protected static class CallableStatementParam {
        int desiredJdbcType;
        int index;
        int inOutModifier;
        boolean isIn;
        boolean isOut;
        int jdbcType;
        short nullability;
        String paramName;
        int precision;
        int scale;
        String typeName;

        CallableStatementParam(String name, int idx, boolean in, boolean out, int jdbcType, String typeName, int precision, int scale, short nullability, int inOutModifier) {
            this.paramName = name;
            this.isIn = in;
            this.isOut = out;
            this.index = idx;
            this.jdbcType = jdbcType;
            this.typeName = typeName;
            this.precision = precision;
            this.scale = scale;
            this.nullability = nullability;
            this.inOutModifier = inOutModifier;
        }

        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.ConnectionProperties;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.PerConnectionLRUFactory;
import com.mysql.jdbc.PerVmServerConfigCacheFactory;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.SocksProxySocketFactory;
import com.mysql.jdbc.StandardSocketFactory;
import com.mysql.jdbc.StringUtils;
import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.log.StandardLogger;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

public class ConnectionPropertiesImpl
implements Serializable,
ConnectionProperties {
    private static final long serialVersionUID = 4257801713007640580L;
    private static final String CONNECTION_AND_AUTH_CATEGORY = Messages.getString("ConnectionProperties.categoryConnectionAuthentication");
    private static final String NETWORK_CATEGORY = Messages.getString("ConnectionProperties.categoryNetworking");
    private static final String DEBUGING_PROFILING_CATEGORY = Messages.getString("ConnectionProperties.categoryDebuggingProfiling");
    private static final String HA_CATEGORY = Messages.getString("ConnectionProperties.categorryHA");
    private static final String MISC_CATEGORY = Messages.getString("ConnectionProperties.categoryMisc");
    private static final String PERFORMANCE_CATEGORY = Messages.getString("ConnectionProperties.categoryPerformance");
    private static final String SECURITY_CATEGORY = Messages.getString("ConnectionProperties.categorySecurity");
    private static final String[] PROPERTY_CATEGORIES = new String[]{CONNECTION_AND_AUTH_CATEGORY, NETWORK_CATEGORY, HA_CATEGORY, SECURITY_CATEGORY, PERFORMANCE_CATEGORY, DEBUGING_PROFILING_CATEGORY, MISC_CATEGORY};
    private static final ArrayList<Field> PROPERTY_LIST = new ArrayList();
    private static final String STANDARD_LOGGER_NAME = StandardLogger.class.getName();
    protected static final String ZERO_DATETIME_BEHAVIOR_CONVERT_TO_NULL = "convertToNull";
    protected static final String ZERO_DATETIME_BEHAVIOR_EXCEPTION = "exception";
    protected static final String ZERO_DATETIME_BEHAVIOR_ROUND = "round";
    private BooleanConnectionProperty allowLoadLocalInfile = new BooleanConnectionProperty("allowLoadLocalInfile", true, Messages.getString("ConnectionProperties.loadDataLocal"), "3.0.3", SECURITY_CATEGORY, Integer.MAX_VALUE);
    private BooleanConnectionProperty allowMultiQueries = new BooleanConnectionProperty("allowMultiQueries", false, Messages.getString("ConnectionProperties.allowMultiQueries"), "3.1.1", SECURITY_CATEGORY, 1);
    private BooleanConnectionProperty allowNanAndInf = new BooleanConnectionProperty("allowNanAndInf", false, Messages.getString("ConnectionProperties.allowNANandINF"), "3.1.5", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty allowUrlInLocalInfile = new BooleanConnectionProperty("allowUrlInLocalInfile", false, Messages.getString("ConnectionProperties.allowUrlInLoadLocal"), "3.1.4", SECURITY_CATEGORY, Integer.MAX_VALUE);
    private BooleanConnectionProperty alwaysSendSetIsolation = new BooleanConnectionProperty("alwaysSendSetIsolation", true, Messages.getString("ConnectionProperties.alwaysSendSetIsolation"), "3.1.7", PERFORMANCE_CATEGORY, Integer.MAX_VALUE);
    private BooleanConnectionProperty autoClosePStmtStreams = new BooleanConnectionProperty("autoClosePStmtStreams", false, Messages.getString("ConnectionProperties.autoClosePstmtStreams"), "3.1.12", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty allowMasterDownConnections = new BooleanConnectionProperty("allowMasterDownConnections", false, Messages.getString("ConnectionProperties.allowMasterDownConnections"), "5.1.27", HA_CATEGORY, Integer.MAX_VALUE);
    private BooleanConnectionProperty allowSlaveDownConnections = new BooleanConnectionProperty("allowSlaveDownConnections", false, Messages.getString("ConnectionProperties.allowSlaveDownConnections"), "5.1.38", HA_CATEGORY, Integer.MAX_VALUE);
    private BooleanConnectionProperty readFromMasterWhenNoSlaves = new BooleanConnectionProperty("readFromMasterWhenNoSlaves", false, Messages.getString("ConnectionProperties.readFromMasterWhenNoSlaves"), "5.1.38", HA_CATEGORY, Integer.MAX_VALUE);
    private BooleanConnectionProperty autoDeserialize = new BooleanConnectionProperty("autoDeserialize", false, Messages.getString("ConnectionProperties.autoDeserialize"), "3.1.5", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty autoGenerateTestcaseScript = new BooleanConnectionProperty("autoGenerateTestcaseScript", false, Messages.getString("ConnectionProperties.autoGenerateTestcaseScript"), "3.1.9", DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
    private boolean autoGenerateTestcaseScriptAsBoolean = false;
    private BooleanConnectionProperty autoReconnect = new BooleanConnectionProperty("autoReconnect", false, Messages.getString("ConnectionProperties.autoReconnect"), "1.1", HA_CATEGORY, 0);
    private BooleanConnectionProperty autoReconnectForPools = new BooleanConnectionProperty("autoReconnectForPools", false, Messages.getString("ConnectionProperties.autoReconnectForPools"), "3.1.3", HA_CATEGORY, 1);
    private boolean autoReconnectForPoolsAsBoolean = false;
    private MemorySizeConnectionProperty blobSendChunkSize = new MemorySizeConnectionProperty("blobSendChunkSize", 0x100000, 0, 0, Messages.getString("ConnectionProperties.blobSendChunkSize"), "3.1.9", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty autoSlowLog = new BooleanConnectionProperty("autoSlowLog", true, Messages.getString("ConnectionProperties.autoSlowLog"), "5.1.4", DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty blobsAreStrings = new BooleanConnectionProperty("blobsAreStrings", false, "Should the driver always treat BLOBs as Strings - specifically to work around dubious metadata returned by the server for GROUP BY clauses?", "5.0.8", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty functionsNeverReturnBlobs = new BooleanConnectionProperty("functionsNeverReturnBlobs", false, "Should the driver always treat data from functions returning BLOBs as Strings - specifically to work around dubious metadata returned by the server for GROUP BY clauses?", "5.0.8", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty cacheCallableStatements = new BooleanConnectionProperty("cacheCallableStmts", false, Messages.getString("ConnectionProperties.cacheCallableStatements"), "3.1.2", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty cachePreparedStatements = new BooleanConnectionProperty("cachePrepStmts", false, Messages.getString("ConnectionProperties.cachePrepStmts"), "3.0.10", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty cacheResultSetMetadata = new BooleanConnectionProperty("cacheResultSetMetadata", false, Messages.getString("ConnectionProperties.cacheRSMetadata"), "3.1.1", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private boolean cacheResultSetMetaDataAsBoolean;
    private StringConnectionProperty serverConfigCacheFactory = new StringConnectionProperty("serverConfigCacheFactory", PerVmServerConfigCacheFactory.class.getName(), Messages.getString("ConnectionProperties.serverConfigCacheFactory"), "5.1.1", PERFORMANCE_CATEGORY, 12);
    private BooleanConnectionProperty cacheServerConfiguration = new BooleanConnectionProperty("cacheServerConfiguration", false, Messages.getString("ConnectionProperties.cacheServerConfiguration"), "3.1.5", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private IntegerConnectionProperty callableStatementCacheSize = new IntegerConnectionProperty("callableStmtCacheSize", 100, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.callableStmtCacheSize"), "3.1.2", PERFORMANCE_CATEGORY, 5);
    private BooleanConnectionProperty capitalizeTypeNames = new BooleanConnectionProperty("capitalizeTypeNames", true, Messages.getString("ConnectionProperties.capitalizeTypeNames"), "2.0.7", MISC_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty characterEncoding = new StringConnectionProperty("characterEncoding", null, Messages.getString("ConnectionProperties.characterEncoding"), "1.1g", MISC_CATEGORY, 5);
    private String characterEncodingAsString = null;
    protected boolean characterEncodingIsAliasForSjis = false;
    private StringConnectionProperty characterSetResults = new StringConnectionProperty("characterSetResults", null, Messages.getString("ConnectionProperties.characterSetResults"), "3.0.13", MISC_CATEGORY, 6);
    private StringConnectionProperty connectionAttributes = new StringConnectionProperty("connectionAttributes", null, Messages.getString("ConnectionProperties.connectionAttributes"), "5.1.25", MISC_CATEGORY, 7);
    private StringConnectionProperty clientInfoProvider = new StringConnectionProperty("clientInfoProvider", "com.mysql.jdbc.JDBC4CommentClientInfoProvider", Messages.getString("ConnectionProperties.clientInfoProvider"), "5.1.0", DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty clobberStreamingResults = new BooleanConnectionProperty("clobberStreamingResults", false, Messages.getString("ConnectionProperties.clobberStreamingResults"), "3.0.9", MISC_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty clobCharacterEncoding = new StringConnectionProperty("clobCharacterEncoding", null, Messages.getString("ConnectionProperties.clobCharacterEncoding"), "5.0.0", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty compensateOnDuplicateKeyUpdateCounts = new BooleanConnectionProperty("compensateOnDuplicateKeyUpdateCounts", false, Messages.getString("ConnectionProperties.compensateOnDuplicateKeyUpdateCounts"), "5.1.7", MISC_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty connectionCollation = new StringConnectionProperty("connectionCollation", null, Messages.getString("ConnectionProperties.connectionCollation"), "3.0.13", MISC_CATEGORY, 7);
    private StringConnectionProperty connectionLifecycleInterceptors = new StringConnectionProperty("connectionLifecycleInterceptors", null, Messages.getString("ConnectionProperties.connectionLifecycleInterceptors"), "5.1.4", CONNECTION_AND_AUTH_CATEGORY, Integer.MAX_VALUE);
    private IntegerConnectionProperty connectTimeout = new IntegerConnectionProperty("connectTimeout", 0, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.connectTimeout"), "3.0.1", CONNECTION_AND_AUTH_CATEGORY, 9);
    private BooleanConnectionProperty continueBatchOnError = new BooleanConnectionProperty("continueBatchOnError", true, Messages.getString("ConnectionProperties.continueBatchOnError"), "3.0.3", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty createDatabaseIfNotExist = new BooleanConnectionProperty("createDatabaseIfNotExist", false, Messages.getString("ConnectionProperties.createDatabaseIfNotExist"), "3.1.9", MISC_CATEGORY, Integer.MIN_VALUE);
    private IntegerConnectionProperty defaultFetchSize = new IntegerConnectionProperty("defaultFetchSize", 0, Messages.getString("ConnectionProperties.defaultFetchSize"), "3.1.9", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty detectServerPreparedStmts = new BooleanConnectionProperty("useServerPrepStmts", false, Messages.getString("ConnectionProperties.useServerPrepStmts"), "3.1.0", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty dontTrackOpenResources = new BooleanConnectionProperty("dontTrackOpenResources", false, Messages.getString("ConnectionProperties.dontTrackOpenResources"), "3.1.7", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty dumpQueriesOnException = new BooleanConnectionProperty("dumpQueriesOnException", false, Messages.getString("ConnectionProperties.dumpQueriesOnException"), "3.1.3", DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty dynamicCalendars = new BooleanConnectionProperty("dynamicCalendars", false, Messages.getString("ConnectionProperties.dynamicCalendars"), "3.1.5", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty elideSetAutoCommits = new BooleanConnectionProperty("elideSetAutoCommits", false, Messages.getString("ConnectionProperties.eliseSetAutoCommit"), "3.1.3", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty emptyStringsConvertToZero = new BooleanConnectionProperty("emptyStringsConvertToZero", true, Messages.getString("ConnectionProperties.emptyStringsConvertToZero"), "3.1.8", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty emulateLocators = new BooleanConnectionProperty("emulateLocators", false, Messages.getString("ConnectionProperties.emulateLocators"), "3.1.0", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty emulateUnsupportedPstmts = new BooleanConnectionProperty("emulateUnsupportedPstmts", true, Messages.getString("ConnectionProperties.emulateUnsupportedPstmts"), "3.1.7", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty enablePacketDebug = new BooleanConnectionProperty("enablePacketDebug", false, Messages.getString("ConnectionProperties.enablePacketDebug"), "3.1.3", DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty enableQueryTimeouts = new BooleanConnectionProperty("enableQueryTimeouts", true, Messages.getString("ConnectionProperties.enableQueryTimeouts"), "5.0.6", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty explainSlowQueries = new BooleanConnectionProperty("explainSlowQueries", false, Messages.getString("ConnectionProperties.explainSlowQueries"), "3.1.2", DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty exceptionInterceptors = new StringConnectionProperty("exceptionInterceptors", null, Messages.getString("ConnectionProperties.exceptionInterceptors"), "5.1.8", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty failOverReadOnly = new BooleanConnectionProperty("failOverReadOnly", true, Messages.getString("ConnectionProperties.failoverReadOnly"), "3.0.12", HA_CATEGORY, 2);
    private BooleanConnectionProperty gatherPerformanceMetrics = new BooleanConnectionProperty("gatherPerfMetrics", false, Messages.getString("ConnectionProperties.gatherPerfMetrics"), "3.1.2", DEBUGING_PROFILING_CATEGORY, 1);
    private BooleanConnectionProperty generateSimpleParameterMetadata = new BooleanConnectionProperty("generateSimpleParameterMetadata", false, Messages.getString("ConnectionProperties.generateSimpleParameterMetadata"), "5.0.5", MISC_CATEGORY, Integer.MIN_VALUE);
    private boolean highAvailabilityAsBoolean = false;
    private BooleanConnectionProperty holdResultsOpenOverStatementClose = new BooleanConnectionProperty("holdResultsOpenOverStatementClose", false, Messages.getString("ConnectionProperties.holdRSOpenOverStmtClose"), "3.1.7", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty includeInnodbStatusInDeadlockExceptions = new BooleanConnectionProperty("includeInnodbStatusInDeadlockExceptions", false, Messages.getString("ConnectionProperties.includeInnodbStatusInDeadlockExceptions"), "5.0.7", DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty includeThreadDumpInDeadlockExceptions = new BooleanConnectionProperty("includeThreadDumpInDeadlockExceptions", false, Messages.getString("ConnectionProperties.includeThreadDumpInDeadlockExceptions"), "5.1.15", DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty includeThreadNamesAsStatementComment = new BooleanConnectionProperty("includeThreadNamesAsStatementComment", false, Messages.getString("ConnectionProperties.includeThreadNamesAsStatementComment"), "5.1.15", DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty ignoreNonTxTables = new BooleanConnectionProperty("ignoreNonTxTables", false, Messages.getString("ConnectionProperties.ignoreNonTxTables"), "3.0.9", MISC_CATEGORY, Integer.MIN_VALUE);
    private IntegerConnectionProperty initialTimeout = new IntegerConnectionProperty("initialTimeout", 2, 1, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.initialTimeout"), "1.1", HA_CATEGORY, 5);
    private BooleanConnectionProperty isInteractiveClient = new BooleanConnectionProperty("interactiveClient", false, Messages.getString("ConnectionProperties.interactiveClient"), "3.1.0", CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty jdbcCompliantTruncation = new BooleanConnectionProperty("jdbcCompliantTruncation", true, Messages.getString("ConnectionProperties.jdbcCompliantTruncation"), "3.1.2", MISC_CATEGORY, Integer.MIN_VALUE);
    private boolean jdbcCompliantTruncationForReads = this.jdbcCompliantTruncation.getValueAsBoolean();
    protected MemorySizeConnectionProperty largeRowSizeThreshold = new MemorySizeConnectionProperty("largeRowSizeThreshold", 2048, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.largeRowSizeThreshold"), "5.1.1", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty loadBalanceStrategy = new StringConnectionProperty("loadBalanceStrategy", "random", null, Messages.getString("ConnectionProperties.loadBalanceStrategy"), "5.0.6", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private IntegerConnectionProperty loadBalanceBlacklistTimeout = new IntegerConnectionProperty("loadBalanceBlacklistTimeout", 0, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.loadBalanceBlacklistTimeout"), "5.1.0", MISC_CATEGORY, Integer.MIN_VALUE);
    private IntegerConnectionProperty loadBalancePingTimeout = new IntegerConnectionProperty("loadBalancePingTimeout", 0, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.loadBalancePingTimeout"), "5.1.13", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty loadBalanceValidateConnectionOnSwapServer = new BooleanConnectionProperty("loadBalanceValidateConnectionOnSwapServer", false, Messages.getString("ConnectionProperties.loadBalanceValidateConnectionOnSwapServer"), "5.1.13", MISC_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty loadBalanceConnectionGroup = new StringConnectionProperty("loadBalanceConnectionGroup", null, Messages.getString("ConnectionProperties.loadBalanceConnectionGroup"), "5.1.13", MISC_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty loadBalanceExceptionChecker = new StringConnectionProperty("loadBalanceExceptionChecker", "com.mysql.jdbc.StandardLoadBalanceExceptionChecker", null, Messages.getString("ConnectionProperties.loadBalanceExceptionChecker"), "5.1.13", MISC_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty loadBalanceSQLStateFailover = new StringConnectionProperty("loadBalanceSQLStateFailover", null, Messages.getString("ConnectionProperties.loadBalanceSQLStateFailover"), "5.1.13", MISC_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty loadBalanceSQLExceptionSubclassFailover = new StringConnectionProperty("loadBalanceSQLExceptionSubclassFailover", null, Messages.getString("ConnectionProperties.loadBalanceSQLExceptionSubclassFailover"), "5.1.13", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty loadBalanceEnableJMX = new BooleanConnectionProperty("loadBalanceEnableJMX", false, Messages.getString("ConnectionProperties.loadBalanceEnableJMX"), "5.1.13", MISC_CATEGORY, Integer.MAX_VALUE);
    private IntegerConnectionProperty loadBalanceHostRemovalGracePeriod = new IntegerConnectionProperty("loadBalanceHostRemovalGracePeriod", 15000, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.loadBalanceHostRemovalGracePeriod"), "5.1.39", MISC_CATEGORY, Integer.MAX_VALUE);
    private StringConnectionProperty loadBalanceAutoCommitStatementRegex = new StringConnectionProperty("loadBalanceAutoCommitStatementRegex", null, Messages.getString("ConnectionProperties.loadBalanceAutoCommitStatementRegex"), "5.1.15", MISC_CATEGORY, Integer.MIN_VALUE);
    private IntegerConnectionProperty loadBalanceAutoCommitStatementThreshold = new IntegerConnectionProperty("loadBalanceAutoCommitStatementThreshold", 0, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.loadBalanceAutoCommitStatementThreshold"), "5.1.15", MISC_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty localSocketAddress = new StringConnectionProperty("localSocketAddress", null, Messages.getString("ConnectionProperties.localSocketAddress"), "5.0.5", CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
    private MemorySizeConnectionProperty locatorFetchBufferSize = new MemorySizeConnectionProperty("locatorFetchBufferSize", 0x100000, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.locatorFetchBufferSize"), "3.2.1", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty loggerClassName = new StringConnectionProperty("logger", STANDARD_LOGGER_NAME, Messages.getString("ConnectionProperties.logger", new Object[]{Log.class.getName(), STANDARD_LOGGER_NAME}), "3.1.1", DEBUGING_PROFILING_CATEGORY, 0);
    private BooleanConnectionProperty logSlowQueries = new BooleanConnectionProperty("logSlowQueries", false, Messages.getString("ConnectionProperties.logSlowQueries"), "3.1.2", DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty logXaCommands = new BooleanConnectionProperty("logXaCommands", false, Messages.getString("ConnectionProperties.logXaCommands"), "5.0.5", DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty maintainTimeStats = new BooleanConnectionProperty("maintainTimeStats", true, Messages.getString("ConnectionProperties.maintainTimeStats"), "3.1.9", PERFORMANCE_CATEGORY, Integer.MAX_VALUE);
    private boolean maintainTimeStatsAsBoolean = true;
    private IntegerConnectionProperty maxQuerySizeToLog = new IntegerConnectionProperty("maxQuerySizeToLog", 2048, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.maxQuerySizeToLog"), "3.1.3", DEBUGING_PROFILING_CATEGORY, 4);
    private IntegerConnectionProperty maxReconnects = new IntegerConnectionProperty("maxReconnects", 3, 1, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.maxReconnects"), "1.1", HA_CATEGORY, 4);
    private IntegerConnectionProperty retriesAllDown = new IntegerConnectionProperty("retriesAllDown", 120, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.retriesAllDown"), "5.1.6", HA_CATEGORY, 4);
    private IntegerConnectionProperty maxRows = new IntegerConnectionProperty("maxRows", -1, -1, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.maxRows"), Messages.getString("ConnectionProperties.allVersions"), MISC_CATEGORY, Integer.MIN_VALUE);
    private int maxRowsAsInt = -1;
    private IntegerConnectionProperty metadataCacheSize = new IntegerConnectionProperty("metadataCacheSize", 50, 1, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.metadataCacheSize"), "3.1.1", PERFORMANCE_CATEGORY, 5);
    private IntegerConnectionProperty netTimeoutForStreamingResults = new IntegerConnectionProperty("netTimeoutForStreamingResults", 600, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.netTimeoutForStreamingResults"), "5.1.0", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty noAccessToProcedureBodies = new BooleanConnectionProperty("noAccessToProcedureBodies", false, "When determining procedure parameter types for CallableStatements, and the connected user  can't access procedure bodies through \"SHOW CREATE PROCEDURE\" or select on mysql.proc  should the driver instead create basic metadata (all parameters reported as IN VARCHARs, but allowing registerOutParameter() to be called on them anyway) instead of throwing an exception?", "5.0.3", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty noDatetimeStringSync = new BooleanConnectionProperty("noDatetimeStringSync", false, Messages.getString("ConnectionProperties.noDatetimeStringSync"), "3.1.7", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty noTimezoneConversionForTimeType = new BooleanConnectionProperty("noTimezoneConversionForTimeType", false, Messages.getString("ConnectionProperties.noTzConversionForTimeType"), "5.0.0", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty noTimezoneConversionForDateType = new BooleanConnectionProperty("noTimezoneConversionForDateType", true, Messages.getString("ConnectionProperties.noTzConversionForDateType"), "5.1.35", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty cacheDefaultTimezone = new BooleanConnectionProperty("cacheDefaultTimezone", true, Messages.getString("ConnectionProperties.cacheDefaultTimezone"), "5.1.35", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty nullCatalogMeansCurrent = new BooleanConnectionProperty("nullCatalogMeansCurrent", true, Messages.getString("ConnectionProperties.nullCatalogMeansCurrent"), "3.1.8", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty nullNamePatternMatchesAll = new BooleanConnectionProperty("nullNamePatternMatchesAll", true, Messages.getString("ConnectionProperties.nullNamePatternMatchesAll"), "3.1.8", MISC_CATEGORY, Integer.MIN_VALUE);
    private IntegerConnectionProperty packetDebugBufferSize = new IntegerConnectionProperty("packetDebugBufferSize", 20, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.packetDebugBufferSize"), "3.1.3", DEBUGING_PROFILING_CATEGORY, 7);
    private BooleanConnectionProperty padCharsWithSpace = new BooleanConnectionProperty("padCharsWithSpace", false, Messages.getString("ConnectionProperties.padCharsWithSpace"), "5.0.6", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty paranoid = new BooleanConnectionProperty("paranoid", false, Messages.getString("ConnectionProperties.paranoid"), "3.0.1", SECURITY_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty pedantic = new BooleanConnectionProperty("pedantic", false, Messages.getString("ConnectionProperties.pedantic"), "3.0.0", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty pinGlobalTxToPhysicalConnection = new BooleanConnectionProperty("pinGlobalTxToPhysicalConnection", false, Messages.getString("ConnectionProperties.pinGlobalTxToPhysicalConnection"), "5.0.1", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty populateInsertRowWithDefaultValues = new BooleanConnectionProperty("populateInsertRowWithDefaultValues", false, Messages.getString("ConnectionProperties.populateInsertRowWithDefaultValues"), "5.0.5", MISC_CATEGORY, Integer.MIN_VALUE);
    private IntegerConnectionProperty preparedStatementCacheSize = new IntegerConnectionProperty("prepStmtCacheSize", 25, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.prepStmtCacheSize"), "3.0.10", PERFORMANCE_CATEGORY, 10);
    private IntegerConnectionProperty preparedStatementCacheSqlLimit = new IntegerConnectionProperty("prepStmtCacheSqlLimit", 256, 1, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.prepStmtCacheSqlLimit"), "3.0.10", PERFORMANCE_CATEGORY, 11);
    private StringConnectionProperty parseInfoCacheFactory = new StringConnectionProperty("parseInfoCacheFactory", PerConnectionLRUFactory.class.getName(), Messages.getString("ConnectionProperties.parseInfoCacheFactory"), "5.1.1", PERFORMANCE_CATEGORY, 12);
    private BooleanConnectionProperty processEscapeCodesForPrepStmts = new BooleanConnectionProperty("processEscapeCodesForPrepStmts", true, Messages.getString("ConnectionProperties.processEscapeCodesForPrepStmts"), "3.1.12", MISC_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty profilerEventHandler = new StringConnectionProperty("profilerEventHandler", "com.mysql.jdbc.profiler.LoggingProfilerEventHandler", Messages.getString("ConnectionProperties.profilerEventHandler"), "5.1.6", DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty profileSql = new StringConnectionProperty("profileSql", null, Messages.getString("ConnectionProperties.profileSqlDeprecated"), "2.0.14", DEBUGING_PROFILING_CATEGORY, 3);
    private BooleanConnectionProperty profileSQL = new BooleanConnectionProperty("profileSQL", false, Messages.getString("ConnectionProperties.profileSQL"), "3.1.0", DEBUGING_PROFILING_CATEGORY, 1);
    private boolean profileSQLAsBoolean = false;
    private StringConnectionProperty propertiesTransform = new StringConnectionProperty("propertiesTransform", null, Messages.getString("ConnectionProperties.connectionPropertiesTransform"), "3.1.4", CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
    private IntegerConnectionProperty queriesBeforeRetryMaster = new IntegerConnectionProperty("queriesBeforeRetryMaster", 50, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.queriesBeforeRetryMaster"), "3.0.2", HA_CATEGORY, 7);
    private BooleanConnectionProperty queryTimeoutKillsConnection = new BooleanConnectionProperty("queryTimeoutKillsConnection", false, Messages.getString("ConnectionProperties.queryTimeoutKillsConnection"), "5.1.9", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty reconnectAtTxEnd = new BooleanConnectionProperty("reconnectAtTxEnd", false, Messages.getString("ConnectionProperties.reconnectAtTxEnd"), "3.0.10", HA_CATEGORY, 4);
    private boolean reconnectTxAtEndAsBoolean = false;
    private BooleanConnectionProperty relaxAutoCommit = new BooleanConnectionProperty("relaxAutoCommit", false, Messages.getString("ConnectionProperties.relaxAutoCommit"), "2.0.13", MISC_CATEGORY, Integer.MIN_VALUE);
    private IntegerConnectionProperty reportMetricsIntervalMillis = new IntegerConnectionProperty("reportMetricsIntervalMillis", 30000, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.reportMetricsIntervalMillis"), "3.1.2", DEBUGING_PROFILING_CATEGORY, 3);
    private BooleanConnectionProperty requireSSL = new BooleanConnectionProperty("requireSSL", false, Messages.getString("ConnectionProperties.requireSSL"), "3.1.0", SECURITY_CATEGORY, 3);
    private StringConnectionProperty resourceId = new StringConnectionProperty("resourceId", null, Messages.getString("ConnectionProperties.resourceId"), "5.0.1", HA_CATEGORY, Integer.MIN_VALUE);
    private IntegerConnectionProperty resultSetSizeThreshold = new IntegerConnectionProperty("resultSetSizeThreshold", 100, Messages.getString("ConnectionProperties.resultSetSizeThreshold"), "5.0.5", DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty retainStatementAfterResultSetClose = new BooleanConnectionProperty("retainStatementAfterResultSetClose", false, Messages.getString("ConnectionProperties.retainStatementAfterResultSetClose"), "3.1.11", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty rewriteBatchedStatements = new BooleanConnectionProperty("rewriteBatchedStatements", false, Messages.getString("ConnectionProperties.rewriteBatchedStatements"), "3.1.13", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty rollbackOnPooledClose = new BooleanConnectionProperty("rollbackOnPooledClose", true, Messages.getString("ConnectionProperties.rollbackOnPooledClose"), "3.0.15", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty roundRobinLoadBalance = new BooleanConnectionProperty("roundRobinLoadBalance", false, Messages.getString("ConnectionProperties.roundRobinLoadBalance"), "3.1.2", HA_CATEGORY, 5);
    private BooleanConnectionProperty runningCTS13 = new BooleanConnectionProperty("runningCTS13", false, Messages.getString("ConnectionProperties.runningCTS13"), "3.1.7", MISC_CATEGORY, Integer.MIN_VALUE);
    private IntegerConnectionProperty secondsBeforeRetryMaster = new IntegerConnectionProperty("secondsBeforeRetryMaster", 30, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.secondsBeforeRetryMaster"), "3.0.2", HA_CATEGORY, 8);
    private IntegerConnectionProperty selfDestructOnPingSecondsLifetime = new IntegerConnectionProperty("selfDestructOnPingSecondsLifetime", 0, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.selfDestructOnPingSecondsLifetime"), "5.1.6", HA_CATEGORY, Integer.MAX_VALUE);
    private IntegerConnectionProperty selfDestructOnPingMaxOperations = new IntegerConnectionProperty("selfDestructOnPingMaxOperations", 0, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.selfDestructOnPingMaxOperations"), "5.1.6", HA_CATEGORY, Integer.MAX_VALUE);
    private BooleanConnectionProperty replicationEnableJMX = new BooleanConnectionProperty("replicationEnableJMX", false, Messages.getString("ConnectionProperties.loadBalanceEnableJMX"), "5.1.27", HA_CATEGORY, Integer.MAX_VALUE);
    private StringConnectionProperty serverTimezone = new StringConnectionProperty("serverTimezone", null, Messages.getString("ConnectionProperties.serverTimezone"), "3.0.2", MISC_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty sessionVariables = new StringConnectionProperty("sessionVariables", null, Messages.getString("ConnectionProperties.sessionVariables"), "3.1.8", MISC_CATEGORY, Integer.MAX_VALUE);
    private IntegerConnectionProperty slowQueryThresholdMillis = new IntegerConnectionProperty("slowQueryThresholdMillis", 2000, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.slowQueryThresholdMillis"), "3.1.2", DEBUGING_PROFILING_CATEGORY, 9);
    private LongConnectionProperty slowQueryThresholdNanos = new LongConnectionProperty("slowQueryThresholdNanos", 0L, Messages.getString("ConnectionProperties.slowQueryThresholdNanos"), "5.0.7", DEBUGING_PROFILING_CATEGORY, 10);
    private StringConnectionProperty socketFactoryClassName = new StringConnectionProperty("socketFactory", StandardSocketFactory.class.getName(), Messages.getString("ConnectionProperties.socketFactory"), "3.0.3", CONNECTION_AND_AUTH_CATEGORY, 4);
    private StringConnectionProperty socksProxyHost = new StringConnectionProperty("socksProxyHost", null, Messages.getString("ConnectionProperties.socksProxyHost"), "5.1.34", NETWORK_CATEGORY, 1);
    private IntegerConnectionProperty socksProxyPort = new IntegerConnectionProperty("socksProxyPort", SocksProxySocketFactory.SOCKS_DEFAULT_PORT, 0, 65535, Messages.getString("ConnectionProperties.socksProxyPort"), "5.1.34", NETWORK_CATEGORY, 2);
    private IntegerConnectionProperty socketTimeout = new IntegerConnectionProperty("socketTimeout", 0, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.socketTimeout"), "3.0.1", CONNECTION_AND_AUTH_CATEGORY, 10);
    private StringConnectionProperty statementInterceptors = new StringConnectionProperty("statementInterceptors", null, Messages.getString("ConnectionProperties.statementInterceptors"), "5.1.1", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty strictFloatingPoint = new BooleanConnectionProperty("strictFloatingPoint", false, Messages.getString("ConnectionProperties.strictFloatingPoint"), "3.0.0", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty strictUpdates = new BooleanConnectionProperty("strictUpdates", true, Messages.getString("ConnectionProperties.strictUpdates"), "3.0.4", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty overrideSupportsIntegrityEnhancementFacility = new BooleanConnectionProperty("overrideSupportsIntegrityEnhancementFacility", false, Messages.getString("ConnectionProperties.overrideSupportsIEF"), "3.1.12", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty tcpNoDelay = new BooleanConnectionProperty("tcpNoDelay", Boolean.valueOf("true"), Messages.getString("ConnectionProperties.tcpNoDelay"), "5.0.7", NETWORK_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty tcpKeepAlive = new BooleanConnectionProperty("tcpKeepAlive", Boolean.valueOf("true"), Messages.getString("ConnectionProperties.tcpKeepAlive"), "5.0.7", NETWORK_CATEGORY, Integer.MIN_VALUE);
    private IntegerConnectionProperty tcpRcvBuf = new IntegerConnectionProperty("tcpRcvBuf", Integer.parseInt("0"), 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.tcpSoRcvBuf"), "5.0.7", NETWORK_CATEGORY, Integer.MIN_VALUE);
    private IntegerConnectionProperty tcpSndBuf = new IntegerConnectionProperty("tcpSndBuf", Integer.parseInt("0"), 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.tcpSoSndBuf"), "5.0.7", NETWORK_CATEGORY, Integer.MIN_VALUE);
    private IntegerConnectionProperty tcpTrafficClass = new IntegerConnectionProperty("tcpTrafficClass", Integer.parseInt("0"), 0, 255, Messages.getString("ConnectionProperties.tcpTrafficClass"), "5.0.7", NETWORK_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty tinyInt1isBit = new BooleanConnectionProperty("tinyInt1isBit", true, Messages.getString("ConnectionProperties.tinyInt1isBit"), "3.0.16", MISC_CATEGORY, Integer.MIN_VALUE);
    protected BooleanConnectionProperty traceProtocol = new BooleanConnectionProperty("traceProtocol", false, Messages.getString("ConnectionProperties.traceProtocol"), "3.1.2", DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty treatUtilDateAsTimestamp = new BooleanConnectionProperty("treatUtilDateAsTimestamp", true, Messages.getString("ConnectionProperties.treatUtilDateAsTimestamp"), "5.0.5", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty transformedBitIsBoolean = new BooleanConnectionProperty("transformedBitIsBoolean", false, Messages.getString("ConnectionProperties.transformedBitIsBoolean"), "3.1.9", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useBlobToStoreUTF8OutsideBMP = new BooleanConnectionProperty("useBlobToStoreUTF8OutsideBMP", false, Messages.getString("ConnectionProperties.useBlobToStoreUTF8OutsideBMP"), "5.1.3", MISC_CATEGORY, 128);
    private StringConnectionProperty utf8OutsideBmpExcludedColumnNamePattern = new StringConnectionProperty("utf8OutsideBmpExcludedColumnNamePattern", null, Messages.getString("ConnectionProperties.utf8OutsideBmpExcludedColumnNamePattern"), "5.1.3", MISC_CATEGORY, 129);
    private StringConnectionProperty utf8OutsideBmpIncludedColumnNamePattern = new StringConnectionProperty("utf8OutsideBmpIncludedColumnNamePattern", null, Messages.getString("ConnectionProperties.utf8OutsideBmpIncludedColumnNamePattern"), "5.1.3", MISC_CATEGORY, 129);
    private BooleanConnectionProperty useCompression = new BooleanConnectionProperty("useCompression", false, Messages.getString("ConnectionProperties.useCompression"), "3.0.17", CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useColumnNamesInFindColumn = new BooleanConnectionProperty("useColumnNamesInFindColumn", false, Messages.getString("ConnectionProperties.useColumnNamesInFindColumn"), "5.1.7", MISC_CATEGORY, Integer.MAX_VALUE);
    private StringConnectionProperty useConfigs = new StringConnectionProperty("useConfigs", null, Messages.getString("ConnectionProperties.useConfigs"), "3.1.5", CONNECTION_AND_AUTH_CATEGORY, Integer.MAX_VALUE);
    private BooleanConnectionProperty useCursorFetch = new BooleanConnectionProperty("useCursorFetch", false, Messages.getString("ConnectionProperties.useCursorFetch"), "5.0.0", PERFORMANCE_CATEGORY, Integer.MAX_VALUE);
    private BooleanConnectionProperty useDynamicCharsetInfo = new BooleanConnectionProperty("useDynamicCharsetInfo", true, Messages.getString("ConnectionProperties.useDynamicCharsetInfo"), "5.0.6", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useDirectRowUnpack = new BooleanConnectionProperty("useDirectRowUnpack", true, "Use newer result set row unpacking code that skips a copy from network buffers  to a MySQL packet instance and instead reads directly into the result set row data buffers.", "5.1.1", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useFastIntParsing = new BooleanConnectionProperty("useFastIntParsing", true, Messages.getString("ConnectionProperties.useFastIntParsing"), "3.1.4", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useFastDateParsing = new BooleanConnectionProperty("useFastDateParsing", true, Messages.getString("ConnectionProperties.useFastDateParsing"), "5.0.5", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useHostsInPrivileges = new BooleanConnectionProperty("useHostsInPrivileges", true, Messages.getString("ConnectionProperties.useHostsInPrivileges"), "3.0.2", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useInformationSchema = new BooleanConnectionProperty("useInformationSchema", false, Messages.getString("ConnectionProperties.useInformationSchema"), "5.0.0", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useJDBCCompliantTimezoneShift = new BooleanConnectionProperty("useJDBCCompliantTimezoneShift", false, Messages.getString("ConnectionProperties.useJDBCCompliantTimezoneShift"), "5.0.0", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useLocalSessionState = new BooleanConnectionProperty("useLocalSessionState", false, Messages.getString("ConnectionProperties.useLocalSessionState"), "3.1.7", PERFORMANCE_CATEGORY, 5);
    private BooleanConnectionProperty useLocalTransactionState = new BooleanConnectionProperty("useLocalTransactionState", false, Messages.getString("ConnectionProperties.useLocalTransactionState"), "5.1.7", PERFORMANCE_CATEGORY, 6);
    private BooleanConnectionProperty useLegacyDatetimeCode = new BooleanConnectionProperty("useLegacyDatetimeCode", true, Messages.getString("ConnectionProperties.useLegacyDatetimeCode"), "5.1.6", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty sendFractionalSeconds = new BooleanConnectionProperty("sendFractionalSeconds", true, Messages.getString("ConnectionProperties.sendFractionalSeconds"), "5.1.37", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useNanosForElapsedTime = new BooleanConnectionProperty("useNanosForElapsedTime", false, Messages.getString("ConnectionProperties.useNanosForElapsedTime"), "5.0.7", DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useOldAliasMetadataBehavior = new BooleanConnectionProperty("useOldAliasMetadataBehavior", false, Messages.getString("ConnectionProperties.useOldAliasMetadataBehavior"), "5.0.4", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useOldUTF8Behavior = new BooleanConnectionProperty("useOldUTF8Behavior", false, Messages.getString("ConnectionProperties.useOldUtf8Behavior"), "3.1.6", MISC_CATEGORY, Integer.MIN_VALUE);
    private boolean useOldUTF8BehaviorAsBoolean = false;
    private BooleanConnectionProperty useOnlyServerErrorMessages = new BooleanConnectionProperty("useOnlyServerErrorMessages", true, Messages.getString("ConnectionProperties.useOnlyServerErrorMessages"), "3.0.15", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useReadAheadInput = new BooleanConnectionProperty("useReadAheadInput", true, Messages.getString("ConnectionProperties.useReadAheadInput"), "3.1.5", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useSqlStateCodes = new BooleanConnectionProperty("useSqlStateCodes", true, Messages.getString("ConnectionProperties.useSqlStateCodes"), "3.1.3", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useSSL = new BooleanConnectionProperty("useSSL", false, Messages.getString("ConnectionProperties.useSSL"), "3.0.2", SECURITY_CATEGORY, 2);
    private BooleanConnectionProperty useSSPSCompatibleTimezoneShift = new BooleanConnectionProperty("useSSPSCompatibleTimezoneShift", false, Messages.getString("ConnectionProperties.useSSPSCompatibleTimezoneShift"), "5.0.5", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useStreamLengthsInPrepStmts = new BooleanConnectionProperty("useStreamLengthsInPrepStmts", true, Messages.getString("ConnectionProperties.useStreamLengthsInPrepStmts"), "3.0.2", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useTimezone = new BooleanConnectionProperty("useTimezone", false, Messages.getString("ConnectionProperties.useTimezone"), "3.0.2", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useUltraDevWorkAround = new BooleanConnectionProperty("ultraDevHack", false, Messages.getString("ConnectionProperties.ultraDevHack"), "2.0.3", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useUnbufferedInput = new BooleanConnectionProperty("useUnbufferedInput", true, Messages.getString("ConnectionProperties.useUnbufferedInput"), "3.0.11", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useUnicode = new BooleanConnectionProperty("useUnicode", true, Messages.getString("ConnectionProperties.useUnicode"), "1.1g", MISC_CATEGORY, 0);
    private boolean useUnicodeAsBoolean = true;
    private BooleanConnectionProperty useUsageAdvisor = new BooleanConnectionProperty("useUsageAdvisor", false, Messages.getString("ConnectionProperties.useUsageAdvisor"), "3.1.1", DEBUGING_PROFILING_CATEGORY, 10);
    private boolean useUsageAdvisorAsBoolean = false;
    private BooleanConnectionProperty yearIsDateType = new BooleanConnectionProperty("yearIsDateType", true, Messages.getString("ConnectionProperties.yearIsDateType"), "3.1.9", MISC_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty zeroDateTimeBehavior = new StringConnectionProperty("zeroDateTimeBehavior", "exception", new String[]{"exception", "round", "convertToNull"}, Messages.getString("ConnectionProperties.zeroDateTimeBehavior", new Object[]{"exception", "round", "convertToNull"}), "3.1.4", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useJvmCharsetConverters = new BooleanConnectionProperty("useJvmCharsetConverters", false, Messages.getString("ConnectionProperties.useJvmCharsetConverters"), "5.0.1", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty useGmtMillisForDatetimes = new BooleanConnectionProperty("useGmtMillisForDatetimes", false, Messages.getString("ConnectionProperties.useGmtMillisForDatetimes"), "3.1.12", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty dumpMetadataOnColumnNotFound = new BooleanConnectionProperty("dumpMetadataOnColumnNotFound", false, Messages.getString("ConnectionProperties.dumpMetadataOnColumnNotFound"), "3.1.13", DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty clientCertificateKeyStoreUrl = new StringConnectionProperty("clientCertificateKeyStoreUrl", null, Messages.getString("ConnectionProperties.clientCertificateKeyStoreUrl"), "5.1.0", SECURITY_CATEGORY, 5);
    private StringConnectionProperty trustCertificateKeyStoreUrl = new StringConnectionProperty("trustCertificateKeyStoreUrl", null, Messages.getString("ConnectionProperties.trustCertificateKeyStoreUrl"), "5.1.0", SECURITY_CATEGORY, 8);
    private StringConnectionProperty clientCertificateKeyStoreType = new StringConnectionProperty("clientCertificateKeyStoreType", "JKS", Messages.getString("ConnectionProperties.clientCertificateKeyStoreType"), "5.1.0", SECURITY_CATEGORY, 6);
    private StringConnectionProperty clientCertificateKeyStorePassword = new StringConnectionProperty("clientCertificateKeyStorePassword", null, Messages.getString("ConnectionProperties.clientCertificateKeyStorePassword"), "5.1.0", SECURITY_CATEGORY, 7);
    private StringConnectionProperty trustCertificateKeyStoreType = new StringConnectionProperty("trustCertificateKeyStoreType", "JKS", Messages.getString("ConnectionProperties.trustCertificateKeyStoreType"), "5.1.0", SECURITY_CATEGORY, 9);
    private StringConnectionProperty trustCertificateKeyStorePassword = new StringConnectionProperty("trustCertificateKeyStorePassword", null, Messages.getString("ConnectionProperties.trustCertificateKeyStorePassword"), "5.1.0", SECURITY_CATEGORY, 10);
    private BooleanConnectionProperty verifyServerCertificate = new BooleanConnectionProperty("verifyServerCertificate", true, Messages.getString("ConnectionProperties.verifyServerCertificate"), "5.1.6", SECURITY_CATEGORY, 4);
    private BooleanConnectionProperty useAffectedRows = new BooleanConnectionProperty("useAffectedRows", false, Messages.getString("ConnectionProperties.useAffectedRows"), "5.1.7", MISC_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty passwordCharacterEncoding = new StringConnectionProperty("passwordCharacterEncoding", null, Messages.getString("ConnectionProperties.passwordCharacterEncoding"), "5.1.7", SECURITY_CATEGORY, Integer.MIN_VALUE);
    private IntegerConnectionProperty maxAllowedPacket = new IntegerConnectionProperty("maxAllowedPacket", -1, Messages.getString("ConnectionProperties.maxAllowedPacket"), "5.1.8", NETWORK_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty authenticationPlugins = new StringConnectionProperty("authenticationPlugins", null, Messages.getString("ConnectionProperties.authenticationPlugins"), "5.1.19", CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty disabledAuthenticationPlugins = new StringConnectionProperty("disabledAuthenticationPlugins", null, Messages.getString("ConnectionProperties.disabledAuthenticationPlugins"), "5.1.19", CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty defaultAuthenticationPlugin = new StringConnectionProperty("defaultAuthenticationPlugin", "com.mysql.jdbc.authentication.MysqlNativePasswordPlugin", Messages.getString("ConnectionProperties.defaultAuthenticationPlugin"), "5.1.19", CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty disconnectOnExpiredPasswords = new BooleanConnectionProperty("disconnectOnExpiredPasswords", true, Messages.getString("ConnectionProperties.disconnectOnExpiredPasswords"), "5.1.23", CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty getProceduresReturnsFunctions = new BooleanConnectionProperty("getProceduresReturnsFunctions", true, Messages.getString("ConnectionProperties.getProceduresReturnsFunctions"), "5.1.26", MISC_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty detectCustomCollations = new BooleanConnectionProperty("detectCustomCollations", false, Messages.getString("ConnectionProperties.detectCustomCollations"), "5.1.29", MISC_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty serverRSAPublicKeyFile = new StringConnectionProperty("serverRSAPublicKeyFile", null, Messages.getString("ConnectionProperties.serverRSAPublicKeyFile"), "5.1.31", SECURITY_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty allowPublicKeyRetrieval = new BooleanConnectionProperty("allowPublicKeyRetrieval", false, Messages.getString("ConnectionProperties.allowPublicKeyRetrieval"), "5.1.31", SECURITY_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty dontCheckOnDuplicateKeyUpdateInSQL = new BooleanConnectionProperty("dontCheckOnDuplicateKeyUpdateInSQL", false, Messages.getString("ConnectionProperties.dontCheckOnDuplicateKeyUpdateInSQL"), "5.1.32", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private BooleanConnectionProperty readOnlyPropagatesToServer = new BooleanConnectionProperty("readOnlyPropagatesToServer", true, Messages.getString("ConnectionProperties.readOnlyPropagatesToServer"), "5.1.35", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    private StringConnectionProperty enabledSSLCipherSuites = new StringConnectionProperty("enabledSSLCipherSuites", null, Messages.getString("ConnectionProperties.enabledSSLCipherSuites"), "5.1.35", SECURITY_CATEGORY, 11);
    private BooleanConnectionProperty enableEscapeProcessing = new BooleanConnectionProperty("enableEscapeProcessing", true, Messages.getString("ConnectionProperties.enableEscapeProcessing"), "5.1.37", PERFORMANCE_CATEGORY, Integer.MIN_VALUE);

    public ExceptionInterceptor getExceptionInterceptor() {
        return null;
    }

    protected static DriverPropertyInfo[] exposeAsDriverPropertyInfo(Properties info, int slotsToReserve) throws SQLException {
        return new ConnectionPropertiesImpl(){
            private static final long serialVersionUID = 4257801713007640581L;
        }.exposeAsDriverPropertyInfoInternal(info, slotsToReserve);
    }

    protected DriverPropertyInfo[] exposeAsDriverPropertyInfoInternal(Properties info, int slotsToReserve) throws SQLException {
        this.initializeProperties(info);
        int numProperties = PROPERTY_LIST.size();
        int listSize = numProperties + slotsToReserve;
        DriverPropertyInfo[] driverProperties = new DriverPropertyInfo[listSize];
        for (int i = slotsToReserve; i < listSize; ++i) {
            Field propertyField = PROPERTY_LIST.get(i - slotsToReserve);
            try {
                ConnectionProperty propToExpose = (ConnectionProperty)propertyField.get(this);
                if (info != null) {
                    propToExpose.initializeFrom(info, this.getExceptionInterceptor());
                }
                driverProperties[i] = propToExpose.getAsDriverPropertyInfo();
                continue;
            }
            catch (IllegalAccessException iae) {
                throw SQLError.createSQLException(Messages.getString("ConnectionProperties.InternalPropertiesFailure"), "S1000", this.getExceptionInterceptor());
            }
        }
        return driverProperties;
    }

    protected Properties exposeAsProperties(Properties info) throws SQLException {
        if (info == null) {
            info = new Properties();
        }
        int numPropertiesToSet = PROPERTY_LIST.size();
        for (int i = 0; i < numPropertiesToSet; ++i) {
            Field propertyField = PROPERTY_LIST.get(i);
            try {
                ConnectionProperty propToGet = (ConnectionProperty)propertyField.get(this);
                Object propValue = propToGet.getValueAsObject();
                if (propValue == null) continue;
                info.setProperty(propToGet.getPropertyName(), propValue.toString());
                continue;
            }
            catch (IllegalAccessException iae) {
                throw SQLError.createSQLException("Internal properties failure", "S1000", this.getExceptionInterceptor());
            }
        }
        return info;
    }

    public String exposeAsXml() throws SQLException {
        StringBuilder xmlBuf = new StringBuilder();
        xmlBuf.append("<ConnectionProperties>");
        int numPropertiesToSet = PROPERTY_LIST.size();
        int numCategories = PROPERTY_CATEGORIES.length;
        HashMap<String, XmlMap> propertyListByCategory = new HashMap<String, XmlMap>();
        for (int i = 0; i < numCategories; ++i) {
            propertyListByCategory.put(PROPERTY_CATEGORIES[i], new XmlMap());
        }
        StringConnectionProperty userProp = new StringConnectionProperty("user", null, Messages.getString("ConnectionProperties.Username"), Messages.getString("ConnectionProperties.allVersions"), CONNECTION_AND_AUTH_CATEGORY, -2147483647);
        StringConnectionProperty passwordProp = new StringConnectionProperty("password", null, Messages.getString("ConnectionProperties.Password"), Messages.getString("ConnectionProperties.allVersions"), CONNECTION_AND_AUTH_CATEGORY, -2147483646);
        XmlMap connectionSortMaps = (XmlMap)propertyListByCategory.get(CONNECTION_AND_AUTH_CATEGORY);
        TreeMap<String, StringConnectionProperty> userMap = new TreeMap<String, StringConnectionProperty>();
        userMap.put(userProp.getPropertyName(), userProp);
        connectionSortMaps.ordered.put(userProp.getOrder(), userMap);
        TreeMap<String, StringConnectionProperty> passwordMap = new TreeMap<String, StringConnectionProperty>();
        passwordMap.put(passwordProp.getPropertyName(), passwordProp);
        connectionSortMaps.ordered.put(new Integer(passwordProp.getOrder()), passwordMap);
        try {
            for (int i = 0; i < numPropertiesToSet; ++i) {
                Field propertyField = PROPERTY_LIST.get(i);
                ConnectionProperty propToGet = (ConnectionProperty)propertyField.get(this);
                XmlMap sortMaps = (XmlMap)propertyListByCategory.get(propToGet.getCategoryName());
                int orderInCategory = propToGet.getOrder();
                if (orderInCategory == Integer.MIN_VALUE) {
                    sortMaps.alpha.put(propToGet.getPropertyName(), propToGet);
                    continue;
                }
                Integer order = orderInCategory;
                Map<String, ConnectionProperty> orderMap = sortMaps.ordered.get(order);
                if (orderMap == null) {
                    orderMap = new TreeMap<String, ConnectionProperty>();
                    sortMaps.ordered.put(order, orderMap);
                }
                orderMap.put(propToGet.getPropertyName(), propToGet);
            }
            for (int j = 0; j < numCategories; ++j) {
                XmlMap sortMaps = (XmlMap)propertyListByCategory.get(PROPERTY_CATEGORIES[j]);
                xmlBuf.append("\n <PropertyCategory name=\"");
                xmlBuf.append(PROPERTY_CATEGORIES[j]);
                xmlBuf.append("\">");
                for (Map<String, ConnectionProperty> orderedEl : sortMaps.ordered.values()) {
                    for (ConnectionProperty propToGet : orderedEl.values()) {
                        xmlBuf.append("\n  <Property name=\"");
                        xmlBuf.append(propToGet.getPropertyName());
                        xmlBuf.append("\" required=\"");
                        xmlBuf.append(propToGet.required ? "Yes" : "No");
                        xmlBuf.append("\" default=\"");
                        if (propToGet.getDefaultValue() != null) {
                            xmlBuf.append(propToGet.getDefaultValue());
                        }
                        xmlBuf.append("\" sortOrder=\"");
                        xmlBuf.append(propToGet.getOrder());
                        xmlBuf.append("\" since=\"");
                        xmlBuf.append(propToGet.sinceVersion);
                        xmlBuf.append("\">\n");
                        xmlBuf.append("    ");
                        String escapedDescription = propToGet.description;
                        escapedDescription = escapedDescription.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                        xmlBuf.append(escapedDescription);
                        xmlBuf.append("\n  </Property>");
                    }
                }
                for (ConnectionProperty propToGet : sortMaps.alpha.values()) {
                    xmlBuf.append("\n  <Property name=\"");
                    xmlBuf.append(propToGet.getPropertyName());
                    xmlBuf.append("\" required=\"");
                    xmlBuf.append(propToGet.required ? "Yes" : "No");
                    xmlBuf.append("\" default=\"");
                    if (propToGet.getDefaultValue() != null) {
                        xmlBuf.append(propToGet.getDefaultValue());
                    }
                    xmlBuf.append("\" sortOrder=\"alpha\" since=\"");
                    xmlBuf.append(propToGet.sinceVersion);
                    xmlBuf.append("\">\n");
                    xmlBuf.append("    ");
                    xmlBuf.append(propToGet.description);
                    xmlBuf.append("\n  </Property>");
                }
                xmlBuf.append("\n </PropertyCategory>");
            }
        }
        catch (IllegalAccessException iae) {
            throw SQLError.createSQLException("Internal properties failure", "S1000", this.getExceptionInterceptor());
        }
        xmlBuf.append("\n</ConnectionProperties>");
        return xmlBuf.toString();
    }

    public boolean getAllowLoadLocalInfile() {
        return this.allowLoadLocalInfile.getValueAsBoolean();
    }

    public boolean getAllowMultiQueries() {
        return this.allowMultiQueries.getValueAsBoolean();
    }

    public boolean getAllowNanAndInf() {
        return this.allowNanAndInf.getValueAsBoolean();
    }

    public boolean getAllowUrlInLocalInfile() {
        return this.allowUrlInLocalInfile.getValueAsBoolean();
    }

    public boolean getAlwaysSendSetIsolation() {
        return this.alwaysSendSetIsolation.getValueAsBoolean();
    }

    public boolean getAutoDeserialize() {
        return this.autoDeserialize.getValueAsBoolean();
    }

    public boolean getAutoGenerateTestcaseScript() {
        return this.autoGenerateTestcaseScriptAsBoolean;
    }

    public boolean getAutoReconnectForPools() {
        return this.autoReconnectForPoolsAsBoolean;
    }

    public int getBlobSendChunkSize() {
        return this.blobSendChunkSize.getValueAsInt();
    }

    public boolean getCacheCallableStatements() {
        return this.cacheCallableStatements.getValueAsBoolean();
    }

    public boolean getCachePreparedStatements() {
        return (Boolean)this.cachePreparedStatements.getValueAsObject();
    }

    public boolean getCacheResultSetMetadata() {
        return this.cacheResultSetMetaDataAsBoolean;
    }

    public boolean getCacheServerConfiguration() {
        return this.cacheServerConfiguration.getValueAsBoolean();
    }

    public int getCallableStatementCacheSize() {
        return this.callableStatementCacheSize.getValueAsInt();
    }

    public boolean getCapitalizeTypeNames() {
        return this.capitalizeTypeNames.getValueAsBoolean();
    }

    public String getCharacterSetResults() {
        return this.characterSetResults.getValueAsString();
    }

    public String getConnectionAttributes() {
        return this.connectionAttributes.getValueAsString();
    }

    public void setConnectionAttributes(String val) {
        this.connectionAttributes.setValue(val);
    }

    public boolean getClobberStreamingResults() {
        return this.clobberStreamingResults.getValueAsBoolean();
    }

    public String getClobCharacterEncoding() {
        return this.clobCharacterEncoding.getValueAsString();
    }

    public String getConnectionCollation() {
        return this.connectionCollation.getValueAsString();
    }

    public int getConnectTimeout() {
        return this.connectTimeout.getValueAsInt();
    }

    public boolean getContinueBatchOnError() {
        return this.continueBatchOnError.getValueAsBoolean();
    }

    public boolean getCreateDatabaseIfNotExist() {
        return this.createDatabaseIfNotExist.getValueAsBoolean();
    }

    public int getDefaultFetchSize() {
        return this.defaultFetchSize.getValueAsInt();
    }

    public boolean getDontTrackOpenResources() {
        return this.dontTrackOpenResources.getValueAsBoolean();
    }

    public boolean getDumpQueriesOnException() {
        return this.dumpQueriesOnException.getValueAsBoolean();
    }

    public boolean getDynamicCalendars() {
        return this.dynamicCalendars.getValueAsBoolean();
    }

    public boolean getElideSetAutoCommits() {
        return this.elideSetAutoCommits.getValueAsBoolean();
    }

    public boolean getEmptyStringsConvertToZero() {
        return this.emptyStringsConvertToZero.getValueAsBoolean();
    }

    public boolean getEmulateLocators() {
        return this.emulateLocators.getValueAsBoolean();
    }

    public boolean getEmulateUnsupportedPstmts() {
        return this.emulateUnsupportedPstmts.getValueAsBoolean();
    }

    public boolean getEnablePacketDebug() {
        return this.enablePacketDebug.getValueAsBoolean();
    }

    public String getEncoding() {
        return this.characterEncodingAsString;
    }

    public boolean getExplainSlowQueries() {
        return this.explainSlowQueries.getValueAsBoolean();
    }

    public boolean getFailOverReadOnly() {
        return this.failOverReadOnly.getValueAsBoolean();
    }

    public boolean getGatherPerformanceMetrics() {
        return this.gatherPerformanceMetrics.getValueAsBoolean();
    }

    protected boolean getHighAvailability() {
        return this.highAvailabilityAsBoolean;
    }

    public boolean getHoldResultsOpenOverStatementClose() {
        return this.holdResultsOpenOverStatementClose.getValueAsBoolean();
    }

    public boolean getIgnoreNonTxTables() {
        return this.ignoreNonTxTables.getValueAsBoolean();
    }

    public int getInitialTimeout() {
        return this.initialTimeout.getValueAsInt();
    }

    public boolean getInteractiveClient() {
        return this.isInteractiveClient.getValueAsBoolean();
    }

    public boolean getIsInteractiveClient() {
        return this.isInteractiveClient.getValueAsBoolean();
    }

    public boolean getJdbcCompliantTruncation() {
        return this.jdbcCompliantTruncation.getValueAsBoolean();
    }

    public int getLocatorFetchBufferSize() {
        return this.locatorFetchBufferSize.getValueAsInt();
    }

    public String getLogger() {
        return this.loggerClassName.getValueAsString();
    }

    public String getLoggerClassName() {
        return this.loggerClassName.getValueAsString();
    }

    public boolean getLogSlowQueries() {
        return this.logSlowQueries.getValueAsBoolean();
    }

    public boolean getMaintainTimeStats() {
        return this.maintainTimeStatsAsBoolean;
    }

    public int getMaxQuerySizeToLog() {
        return this.maxQuerySizeToLog.getValueAsInt();
    }

    public int getMaxReconnects() {
        return this.maxReconnects.getValueAsInt();
    }

    public int getMaxRows() {
        return this.maxRowsAsInt;
    }

    public int getMetadataCacheSize() {
        return this.metadataCacheSize.getValueAsInt();
    }

    public boolean getNoDatetimeStringSync() {
        return this.noDatetimeStringSync.getValueAsBoolean();
    }

    public boolean getNullCatalogMeansCurrent() {
        return this.nullCatalogMeansCurrent.getValueAsBoolean();
    }

    public boolean getNullNamePatternMatchesAll() {
        return this.nullNamePatternMatchesAll.getValueAsBoolean();
    }

    public int getPacketDebugBufferSize() {
        return this.packetDebugBufferSize.getValueAsInt();
    }

    public boolean getParanoid() {
        return this.paranoid.getValueAsBoolean();
    }

    public boolean getPedantic() {
        return this.pedantic.getValueAsBoolean();
    }

    public int getPreparedStatementCacheSize() {
        return (Integer)this.preparedStatementCacheSize.getValueAsObject();
    }

    public int getPreparedStatementCacheSqlLimit() {
        return (Integer)this.preparedStatementCacheSqlLimit.getValueAsObject();
    }

    public boolean getProfileSql() {
        return this.profileSQLAsBoolean;
    }

    public boolean getProfileSQL() {
        return this.profileSQL.getValueAsBoolean();
    }

    public String getPropertiesTransform() {
        return this.propertiesTransform.getValueAsString();
    }

    public int getQueriesBeforeRetryMaster() {
        return this.queriesBeforeRetryMaster.getValueAsInt();
    }

    public boolean getReconnectAtTxEnd() {
        return this.reconnectTxAtEndAsBoolean;
    }

    public boolean getRelaxAutoCommit() {
        return this.relaxAutoCommit.getValueAsBoolean();
    }

    public int getReportMetricsIntervalMillis() {
        return this.reportMetricsIntervalMillis.getValueAsInt();
    }

    public boolean getRequireSSL() {
        return this.requireSSL.getValueAsBoolean();
    }

    public boolean getRetainStatementAfterResultSetClose() {
        return this.retainStatementAfterResultSetClose.getValueAsBoolean();
    }

    public boolean getRollbackOnPooledClose() {
        return this.rollbackOnPooledClose.getValueAsBoolean();
    }

    public boolean getRoundRobinLoadBalance() {
        return this.roundRobinLoadBalance.getValueAsBoolean();
    }

    public boolean getRunningCTS13() {
        return this.runningCTS13.getValueAsBoolean();
    }

    public int getSecondsBeforeRetryMaster() {
        return this.secondsBeforeRetryMaster.getValueAsInt();
    }

    public String getServerTimezone() {
        return this.serverTimezone.getValueAsString();
    }

    public String getSessionVariables() {
        return this.sessionVariables.getValueAsString();
    }

    public int getSlowQueryThresholdMillis() {
        return this.slowQueryThresholdMillis.getValueAsInt();
    }

    public String getSocketFactoryClassName() {
        return this.socketFactoryClassName.getValueAsString();
    }

    public int getSocketTimeout() {
        return this.socketTimeout.getValueAsInt();
    }

    public boolean getStrictFloatingPoint() {
        return this.strictFloatingPoint.getValueAsBoolean();
    }

    public boolean getStrictUpdates() {
        return this.strictUpdates.getValueAsBoolean();
    }

    public boolean getTinyInt1isBit() {
        return this.tinyInt1isBit.getValueAsBoolean();
    }

    public boolean getTraceProtocol() {
        return this.traceProtocol.getValueAsBoolean();
    }

    public boolean getTransformedBitIsBoolean() {
        return this.transformedBitIsBoolean.getValueAsBoolean();
    }

    public boolean getUseCompression() {
        return this.useCompression.getValueAsBoolean();
    }

    public boolean getUseFastIntParsing() {
        return this.useFastIntParsing.getValueAsBoolean();
    }

    public boolean getUseHostsInPrivileges() {
        return this.useHostsInPrivileges.getValueAsBoolean();
    }

    public boolean getUseInformationSchema() {
        return this.useInformationSchema.getValueAsBoolean();
    }

    public boolean getUseLocalSessionState() {
        return this.useLocalSessionState.getValueAsBoolean();
    }

    public boolean getUseOldUTF8Behavior() {
        return this.useOldUTF8BehaviorAsBoolean;
    }

    public boolean getUseOnlyServerErrorMessages() {
        return this.useOnlyServerErrorMessages.getValueAsBoolean();
    }

    public boolean getUseReadAheadInput() {
        return this.useReadAheadInput.getValueAsBoolean();
    }

    public boolean getUseServerPreparedStmts() {
        return this.detectServerPreparedStmts.getValueAsBoolean();
    }

    public boolean getUseSqlStateCodes() {
        return this.useSqlStateCodes.getValueAsBoolean();
    }

    public boolean getUseSSL() {
        return this.useSSL.getValueAsBoolean();
    }

    public boolean isUseSSLExplicit() {
        return this.useSSL.wasExplicitlySet;
    }

    public boolean getUseStreamLengthsInPrepStmts() {
        return this.useStreamLengthsInPrepStmts.getValueAsBoolean();
    }

    public boolean getUseTimezone() {
        return this.useTimezone.getValueAsBoolean();
    }

    public boolean getUseUltraDevWorkAround() {
        return this.useUltraDevWorkAround.getValueAsBoolean();
    }

    public boolean getUseUnbufferedInput() {
        return this.useUnbufferedInput.getValueAsBoolean();
    }

    public boolean getUseUnicode() {
        return this.useUnicodeAsBoolean;
    }

    public boolean getUseUsageAdvisor() {
        return this.useUsageAdvisorAsBoolean;
    }

    public boolean getYearIsDateType() {
        return this.yearIsDateType.getValueAsBoolean();
    }

    public String getZeroDateTimeBehavior() {
        return this.zeroDateTimeBehavior.getValueAsString();
    }

    protected void initializeFromRef(Reference ref) throws SQLException {
        int numPropertiesToSet = PROPERTY_LIST.size();
        for (int i = 0; i < numPropertiesToSet; ++i) {
            Field propertyField = PROPERTY_LIST.get(i);
            try {
                ConnectionProperty propToSet = (ConnectionProperty)propertyField.get(this);
                if (ref == null) continue;
                propToSet.initializeFrom(ref, this.getExceptionInterceptor());
                continue;
            }
            catch (IllegalAccessException iae) {
                throw SQLError.createSQLException("Internal properties failure", "S1000", this.getExceptionInterceptor());
            }
        }
        this.postInitialization();
    }

    protected void initializeProperties(Properties info) throws SQLException {
        if (info != null) {
            String profileSqlLc = info.getProperty("profileSql");
            if (profileSqlLc != null) {
                info.put("profileSQL", profileSqlLc);
            }
            Properties infoCopy = (Properties)info.clone();
            infoCopy.remove("HOST");
            infoCopy.remove("user");
            infoCopy.remove("password");
            infoCopy.remove("DBNAME");
            infoCopy.remove("PORT");
            infoCopy.remove("profileSql");
            int numPropertiesToSet = PROPERTY_LIST.size();
            for (int i = 0; i < numPropertiesToSet; ++i) {
                Field propertyField = PROPERTY_LIST.get(i);
                try {
                    ConnectionProperty propToSet = (ConnectionProperty)propertyField.get(this);
                    propToSet.initializeFrom(infoCopy, this.getExceptionInterceptor());
                    continue;
                }
                catch (IllegalAccessException iae) {
                    throw SQLError.createSQLException(Messages.getString("ConnectionProperties.unableToInitDriverProperties") + iae.toString(), "S1000", this.getExceptionInterceptor());
                }
            }
            this.postInitialization();
        }
    }

    protected void postInitialization() throws SQLException {
        String testEncoding;
        if (this.profileSql.getValueAsObject() != null) {
            this.profileSQL.initializeFrom(this.profileSql.getValueAsObject().toString(), this.getExceptionInterceptor());
        }
        this.reconnectTxAtEndAsBoolean = (Boolean)this.reconnectAtTxEnd.getValueAsObject();
        if (this.getMaxRows() == 0) {
            this.maxRows.setValueAsObject(-1);
        }
        if ((testEncoding = (String)this.characterEncoding.getValueAsObject()) != null) {
            try {
                String testString = "abc";
                StringUtils.getBytes(testString, testEncoding);
            }
            catch (UnsupportedEncodingException UE) {
                throw SQLError.createSQLException(Messages.getString("ConnectionProperties.unsupportedCharacterEncoding", new Object[]{testEncoding}), "0S100", this.getExceptionInterceptor());
            }
        }
        if (((Boolean)this.cacheResultSetMetadata.getValueAsObject()).booleanValue()) {
            try {
                Class.forName("java.util.LinkedHashMap");
            }
            catch (ClassNotFoundException cnfe) {
                this.cacheResultSetMetadata.setValue(false);
            }
        }
        this.cacheResultSetMetaDataAsBoolean = this.cacheResultSetMetadata.getValueAsBoolean();
        this.useUnicodeAsBoolean = this.useUnicode.getValueAsBoolean();
        this.characterEncodingAsString = (String)this.characterEncoding.getValueAsObject();
        this.highAvailabilityAsBoolean = this.autoReconnect.getValueAsBoolean();
        this.autoReconnectForPoolsAsBoolean = this.autoReconnectForPools.getValueAsBoolean();
        this.maxRowsAsInt = (Integer)this.maxRows.getValueAsObject();
        this.profileSQLAsBoolean = this.profileSQL.getValueAsBoolean();
        this.useUsageAdvisorAsBoolean = this.useUsageAdvisor.getValueAsBoolean();
        this.useOldUTF8BehaviorAsBoolean = this.useOldUTF8Behavior.getValueAsBoolean();
        this.autoGenerateTestcaseScriptAsBoolean = this.autoGenerateTestcaseScript.getValueAsBoolean();
        this.maintainTimeStatsAsBoolean = this.maintainTimeStats.getValueAsBoolean();
        this.jdbcCompliantTruncationForReads = this.getJdbcCompliantTruncation();
        if (this.getUseCursorFetch()) {
            this.setDetectServerPreparedStmts(true);
        }
    }

    public void setAllowLoadLocalInfile(boolean property) {
        this.allowLoadLocalInfile.setValue(property);
    }

    public void setAllowMultiQueries(boolean property) {
        this.allowMultiQueries.setValue(property);
    }

    public void setAllowNanAndInf(boolean flag) {
        this.allowNanAndInf.setValue(flag);
    }

    public void setAllowUrlInLocalInfile(boolean flag) {
        this.allowUrlInLocalInfile.setValue(flag);
    }

    public void setAlwaysSendSetIsolation(boolean flag) {
        this.alwaysSendSetIsolation.setValue(flag);
    }

    public void setAutoDeserialize(boolean flag) {
        this.autoDeserialize.setValue(flag);
    }

    public void setAutoGenerateTestcaseScript(boolean flag) {
        this.autoGenerateTestcaseScript.setValue(flag);
        this.autoGenerateTestcaseScriptAsBoolean = this.autoGenerateTestcaseScript.getValueAsBoolean();
    }

    public void setAutoReconnect(boolean flag) {
        this.autoReconnect.setValue(flag);
    }

    public void setAutoReconnectForConnectionPools(boolean property) {
        this.autoReconnectForPools.setValue(property);
        this.autoReconnectForPoolsAsBoolean = this.autoReconnectForPools.getValueAsBoolean();
    }

    public void setAutoReconnectForPools(boolean flag) {
        this.autoReconnectForPools.setValue(flag);
    }

    public void setBlobSendChunkSize(String value) throws SQLException {
        this.blobSendChunkSize.setValue(value, this.getExceptionInterceptor());
    }

    public void setCacheCallableStatements(boolean flag) {
        this.cacheCallableStatements.setValue(flag);
    }

    public void setCachePreparedStatements(boolean flag) {
        this.cachePreparedStatements.setValue(flag);
    }

    public void setCacheResultSetMetadata(boolean property) {
        this.cacheResultSetMetadata.setValue(property);
        this.cacheResultSetMetaDataAsBoolean = this.cacheResultSetMetadata.getValueAsBoolean();
    }

    public void setCacheServerConfiguration(boolean flag) {
        this.cacheServerConfiguration.setValue(flag);
    }

    public void setCallableStatementCacheSize(int size) throws SQLException {
        this.callableStatementCacheSize.setValue(size, this.getExceptionInterceptor());
    }

    public void setCapitalizeDBMDTypes(boolean property) {
        this.capitalizeTypeNames.setValue(property);
    }

    public void setCapitalizeTypeNames(boolean flag) {
        this.capitalizeTypeNames.setValue(flag);
    }

    public void setCharacterEncoding(String encoding) {
        this.characterEncoding.setValue(encoding);
    }

    public void setCharacterSetResults(String characterSet) {
        this.characterSetResults.setValue(characterSet);
    }

    public void setClobberStreamingResults(boolean flag) {
        this.clobberStreamingResults.setValue(flag);
    }

    public void setClobCharacterEncoding(String encoding) {
        this.clobCharacterEncoding.setValue(encoding);
    }

    public void setConnectionCollation(String collation) {
        this.connectionCollation.setValue(collation);
    }

    public void setConnectTimeout(int timeoutMs) throws SQLException {
        this.connectTimeout.setValue(timeoutMs, this.getExceptionInterceptor());
    }

    public void setContinueBatchOnError(boolean property) {
        this.continueBatchOnError.setValue(property);
    }

    public void setCreateDatabaseIfNotExist(boolean flag) {
        this.createDatabaseIfNotExist.setValue(flag);
    }

    public void setDefaultFetchSize(int n) throws SQLException {
        this.defaultFetchSize.setValue(n, this.getExceptionInterceptor());
    }

    public void setDetectServerPreparedStmts(boolean property) {
        this.detectServerPreparedStmts.setValue(property);
    }

    public void setDontTrackOpenResources(boolean flag) {
        this.dontTrackOpenResources.setValue(flag);
    }

    public void setDumpQueriesOnException(boolean flag) {
        this.dumpQueriesOnException.setValue(flag);
    }

    public void setDynamicCalendars(boolean flag) {
        this.dynamicCalendars.setValue(flag);
    }

    public void setElideSetAutoCommits(boolean flag) {
        this.elideSetAutoCommits.setValue(flag);
    }

    public void setEmptyStringsConvertToZero(boolean flag) {
        this.emptyStringsConvertToZero.setValue(flag);
    }

    public void setEmulateLocators(boolean property) {
        this.emulateLocators.setValue(property);
    }

    public void setEmulateUnsupportedPstmts(boolean flag) {
        this.emulateUnsupportedPstmts.setValue(flag);
    }

    public void setEnablePacketDebug(boolean flag) {
        this.enablePacketDebug.setValue(flag);
    }

    public void setEncoding(String property) {
        this.characterEncoding.setValue(property);
        this.characterEncodingAsString = this.characterEncoding.getValueAsString();
    }

    public void setExplainSlowQueries(boolean flag) {
        this.explainSlowQueries.setValue(flag);
    }

    public void setFailOverReadOnly(boolean flag) {
        this.failOverReadOnly.setValue(flag);
    }

    public void setGatherPerformanceMetrics(boolean flag) {
        this.gatherPerformanceMetrics.setValue(flag);
    }

    protected void setHighAvailability(boolean property) {
        this.autoReconnect.setValue(property);
        this.highAvailabilityAsBoolean = this.autoReconnect.getValueAsBoolean();
    }

    public void setHoldResultsOpenOverStatementClose(boolean flag) {
        this.holdResultsOpenOverStatementClose.setValue(flag);
    }

    public void setIgnoreNonTxTables(boolean property) {
        this.ignoreNonTxTables.setValue(property);
    }

    public void setInitialTimeout(int property) throws SQLException {
        this.initialTimeout.setValue(property, this.getExceptionInterceptor());
    }

    public void setIsInteractiveClient(boolean property) {
        this.isInteractiveClient.setValue(property);
    }

    public void setJdbcCompliantTruncation(boolean flag) {
        this.jdbcCompliantTruncation.setValue(flag);
    }

    public void setLocatorFetchBufferSize(String value) throws SQLException {
        this.locatorFetchBufferSize.setValue(value, this.getExceptionInterceptor());
    }

    public void setLogger(String property) {
        this.loggerClassName.setValueAsObject(property);
    }

    public void setLoggerClassName(String className) {
        this.loggerClassName.setValue(className);
    }

    public void setLogSlowQueries(boolean flag) {
        this.logSlowQueries.setValue(flag);
    }

    public void setMaintainTimeStats(boolean flag) {
        this.maintainTimeStats.setValue(flag);
        this.maintainTimeStatsAsBoolean = this.maintainTimeStats.getValueAsBoolean();
    }

    public void setMaxQuerySizeToLog(int sizeInBytes) throws SQLException {
        this.maxQuerySizeToLog.setValue(sizeInBytes, this.getExceptionInterceptor());
    }

    public void setMaxReconnects(int property) throws SQLException {
        this.maxReconnects.setValue(property, this.getExceptionInterceptor());
    }

    public void setMaxRows(int property) throws SQLException {
        this.maxRows.setValue(property, this.getExceptionInterceptor());
        this.maxRowsAsInt = this.maxRows.getValueAsInt();
    }

    public void setMetadataCacheSize(int value) throws SQLException {
        this.metadataCacheSize.setValue(value, this.getExceptionInterceptor());
    }

    public void setNoDatetimeStringSync(boolean flag) {
        this.noDatetimeStringSync.setValue(flag);
    }

    public void setNullCatalogMeansCurrent(boolean value) {
        this.nullCatalogMeansCurrent.setValue(value);
    }

    public void setNullNamePatternMatchesAll(boolean value) {
        this.nullNamePatternMatchesAll.setValue(value);
    }

    public void setPacketDebugBufferSize(int size) throws SQLException {
        this.packetDebugBufferSize.setValue(size, this.getExceptionInterceptor());
    }

    public void setParanoid(boolean property) {
        this.paranoid.setValue(property);
    }

    public void setPedantic(boolean property) {
        this.pedantic.setValue(property);
    }

    public void setPreparedStatementCacheSize(int cacheSize) throws SQLException {
        this.preparedStatementCacheSize.setValue(cacheSize, this.getExceptionInterceptor());
    }

    public void setPreparedStatementCacheSqlLimit(int cacheSqlLimit) throws SQLException {
        this.preparedStatementCacheSqlLimit.setValue(cacheSqlLimit, this.getExceptionInterceptor());
    }

    public void setProfileSql(boolean property) {
        this.profileSQL.setValue(property);
        this.profileSQLAsBoolean = this.profileSQL.getValueAsBoolean();
    }

    public void setProfileSQL(boolean flag) {
        this.profileSQL.setValue(flag);
    }

    public void setPropertiesTransform(String value) {
        this.propertiesTransform.setValue(value);
    }

    public void setQueriesBeforeRetryMaster(int property) throws SQLException {
        this.queriesBeforeRetryMaster.setValue(property, this.getExceptionInterceptor());
    }

    public void setReconnectAtTxEnd(boolean property) {
        this.reconnectAtTxEnd.setValue(property);
        this.reconnectTxAtEndAsBoolean = this.reconnectAtTxEnd.getValueAsBoolean();
    }

    public void setRelaxAutoCommit(boolean property) {
        this.relaxAutoCommit.setValue(property);
    }

    public void setReportMetricsIntervalMillis(int millis) throws SQLException {
        this.reportMetricsIntervalMillis.setValue(millis, this.getExceptionInterceptor());
    }

    public void setRequireSSL(boolean property) {
        this.requireSSL.setValue(property);
    }

    public void setRetainStatementAfterResultSetClose(boolean flag) {
        this.retainStatementAfterResultSetClose.setValue(flag);
    }

    public void setRollbackOnPooledClose(boolean flag) {
        this.rollbackOnPooledClose.setValue(flag);
    }

    public void setRoundRobinLoadBalance(boolean flag) {
        this.roundRobinLoadBalance.setValue(flag);
    }

    public void setRunningCTS13(boolean flag) {
        this.runningCTS13.setValue(flag);
    }

    public void setSecondsBeforeRetryMaster(int property) throws SQLException {
        this.secondsBeforeRetryMaster.setValue(property, this.getExceptionInterceptor());
    }

    public void setServerTimezone(String property) {
        this.serverTimezone.setValue(property);
    }

    public void setSessionVariables(String variables) {
        this.sessionVariables.setValue(variables);
    }

    public void setSlowQueryThresholdMillis(int millis) throws SQLException {
        this.slowQueryThresholdMillis.setValue(millis, this.getExceptionInterceptor());
    }

    public void setSocketFactoryClassName(String property) {
        this.socketFactoryClassName.setValue(property);
    }

    public void setSocketTimeout(int property) throws SQLException {
        this.socketTimeout.setValue(property, this.getExceptionInterceptor());
    }

    public void setStrictFloatingPoint(boolean property) {
        this.strictFloatingPoint.setValue(property);
    }

    public void setStrictUpdates(boolean property) {
        this.strictUpdates.setValue(property);
    }

    public void setTinyInt1isBit(boolean flag) {
        this.tinyInt1isBit.setValue(flag);
    }

    public void setTraceProtocol(boolean flag) {
        this.traceProtocol.setValue(flag);
    }

    public void setTransformedBitIsBoolean(boolean flag) {
        this.transformedBitIsBoolean.setValue(flag);
    }

    public void setUseCompression(boolean property) {
        this.useCompression.setValue(property);
    }

    public void setUseFastIntParsing(boolean flag) {
        this.useFastIntParsing.setValue(flag);
    }

    public void setUseHostsInPrivileges(boolean property) {
        this.useHostsInPrivileges.setValue(property);
    }

    public void setUseInformationSchema(boolean flag) {
        this.useInformationSchema.setValue(flag);
    }

    public void setUseLocalSessionState(boolean flag) {
        this.useLocalSessionState.setValue(flag);
    }

    public void setUseOldUTF8Behavior(boolean flag) {
        this.useOldUTF8Behavior.setValue(flag);
        this.useOldUTF8BehaviorAsBoolean = this.useOldUTF8Behavior.getValueAsBoolean();
    }

    public void setUseOnlyServerErrorMessages(boolean flag) {
        this.useOnlyServerErrorMessages.setValue(flag);
    }

    public void setUseReadAheadInput(boolean flag) {
        this.useReadAheadInput.setValue(flag);
    }

    public void setUseServerPreparedStmts(boolean flag) {
        this.detectServerPreparedStmts.setValue(flag);
    }

    public void setUseSqlStateCodes(boolean flag) {
        this.useSqlStateCodes.setValue(flag);
    }

    public void setUseSSL(boolean property) {
        this.useSSL.setValue(property);
    }

    public void setUseStreamLengthsInPrepStmts(boolean property) {
        this.useStreamLengthsInPrepStmts.setValue(property);
    }

    public void setUseTimezone(boolean property) {
        this.useTimezone.setValue(property);
    }

    public void setUseUltraDevWorkAround(boolean property) {
        this.useUltraDevWorkAround.setValue(property);
    }

    public void setUseUnbufferedInput(boolean flag) {
        this.useUnbufferedInput.setValue(flag);
    }

    public void setUseUnicode(boolean flag) {
        this.useUnicode.setValue(flag);
        this.useUnicodeAsBoolean = this.useUnicode.getValueAsBoolean();
    }

    public void setUseUsageAdvisor(boolean useUsageAdvisorFlag) {
        this.useUsageAdvisor.setValue(useUsageAdvisorFlag);
        this.useUsageAdvisorAsBoolean = this.useUsageAdvisor.getValueAsBoolean();
    }

    public void setYearIsDateType(boolean flag) {
        this.yearIsDateType.setValue(flag);
    }

    public void setZeroDateTimeBehavior(String behavior) {
        this.zeroDateTimeBehavior.setValue(behavior);
    }

    protected void storeToRef(Reference ref) throws SQLException {
        int numPropertiesToSet = PROPERTY_LIST.size();
        for (int i = 0; i < numPropertiesToSet; ++i) {
            Field propertyField = PROPERTY_LIST.get(i);
            try {
                ConnectionProperty propToStore = (ConnectionProperty)propertyField.get(this);
                if (ref == null) continue;
                propToStore.storeTo(ref);
                continue;
            }
            catch (IllegalAccessException iae) {
                throw SQLError.createSQLException(Messages.getString("ConnectionProperties.errorNotExpected"), this.getExceptionInterceptor());
            }
        }
    }

    public boolean useUnbufferedInput() {
        return this.useUnbufferedInput.getValueAsBoolean();
    }

    public boolean getUseCursorFetch() {
        return this.useCursorFetch.getValueAsBoolean();
    }

    public void setUseCursorFetch(boolean flag) {
        this.useCursorFetch.setValue(flag);
    }

    public boolean getOverrideSupportsIntegrityEnhancementFacility() {
        return this.overrideSupportsIntegrityEnhancementFacility.getValueAsBoolean();
    }

    public void setOverrideSupportsIntegrityEnhancementFacility(boolean flag) {
        this.overrideSupportsIntegrityEnhancementFacility.setValue(flag);
    }

    public boolean getNoTimezoneConversionForTimeType() {
        return this.noTimezoneConversionForTimeType.getValueAsBoolean();
    }

    public void setNoTimezoneConversionForTimeType(boolean flag) {
        this.noTimezoneConversionForTimeType.setValue(flag);
    }

    public boolean getNoTimezoneConversionForDateType() {
        return this.noTimezoneConversionForDateType.getValueAsBoolean();
    }

    public void setNoTimezoneConversionForDateType(boolean flag) {
        this.noTimezoneConversionForDateType.setValue(flag);
    }

    public boolean getCacheDefaultTimezone() {
        return this.cacheDefaultTimezone.getValueAsBoolean();
    }

    public void setCacheDefaultTimezone(boolean flag) {
        this.cacheDefaultTimezone.setValue(flag);
    }

    public boolean getUseJDBCCompliantTimezoneShift() {
        return this.useJDBCCompliantTimezoneShift.getValueAsBoolean();
    }

    public void setUseJDBCCompliantTimezoneShift(boolean flag) {
        this.useJDBCCompliantTimezoneShift.setValue(flag);
    }

    public boolean getAutoClosePStmtStreams() {
        return this.autoClosePStmtStreams.getValueAsBoolean();
    }

    public void setAutoClosePStmtStreams(boolean flag) {
        this.autoClosePStmtStreams.setValue(flag);
    }

    public boolean getProcessEscapeCodesForPrepStmts() {
        return this.processEscapeCodesForPrepStmts.getValueAsBoolean();
    }

    public void setProcessEscapeCodesForPrepStmts(boolean flag) {
        this.processEscapeCodesForPrepStmts.setValue(flag);
    }

    public boolean getUseGmtMillisForDatetimes() {
        return this.useGmtMillisForDatetimes.getValueAsBoolean();
    }

    public void setUseGmtMillisForDatetimes(boolean flag) {
        this.useGmtMillisForDatetimes.setValue(flag);
    }

    public boolean getDumpMetadataOnColumnNotFound() {
        return this.dumpMetadataOnColumnNotFound.getValueAsBoolean();
    }

    public void setDumpMetadataOnColumnNotFound(boolean flag) {
        this.dumpMetadataOnColumnNotFound.setValue(flag);
    }

    public String getResourceId() {
        return this.resourceId.getValueAsString();
    }

    public void setResourceId(String resourceId) {
        this.resourceId.setValue(resourceId);
    }

    public boolean getRewriteBatchedStatements() {
        return this.rewriteBatchedStatements.getValueAsBoolean();
    }

    public void setRewriteBatchedStatements(boolean flag) {
        this.rewriteBatchedStatements.setValue(flag);
    }

    public boolean getJdbcCompliantTruncationForReads() {
        return this.jdbcCompliantTruncationForReads;
    }

    public void setJdbcCompliantTruncationForReads(boolean jdbcCompliantTruncationForReads) {
        this.jdbcCompliantTruncationForReads = jdbcCompliantTruncationForReads;
    }

    public boolean getUseJvmCharsetConverters() {
        return this.useJvmCharsetConverters.getValueAsBoolean();
    }

    public void setUseJvmCharsetConverters(boolean flag) {
        this.useJvmCharsetConverters.setValue(flag);
    }

    public boolean getPinGlobalTxToPhysicalConnection() {
        return this.pinGlobalTxToPhysicalConnection.getValueAsBoolean();
    }

    public void setPinGlobalTxToPhysicalConnection(boolean flag) {
        this.pinGlobalTxToPhysicalConnection.setValue(flag);
    }

    public void setGatherPerfMetrics(boolean flag) {
        this.setGatherPerformanceMetrics(flag);
    }

    public boolean getGatherPerfMetrics() {
        return this.getGatherPerformanceMetrics();
    }

    public void setUltraDevHack(boolean flag) {
        this.setUseUltraDevWorkAround(flag);
    }

    public boolean getUltraDevHack() {
        return this.getUseUltraDevWorkAround();
    }

    public void setInteractiveClient(boolean property) {
        this.setIsInteractiveClient(property);
    }

    public void setSocketFactory(String name) {
        this.setSocketFactoryClassName(name);
    }

    public String getSocketFactory() {
        return this.getSocketFactoryClassName();
    }

    public void setUseServerPrepStmts(boolean flag) {
        this.setUseServerPreparedStmts(flag);
    }

    public boolean getUseServerPrepStmts() {
        return this.getUseServerPreparedStmts();
    }

    public void setCacheCallableStmts(boolean flag) {
        this.setCacheCallableStatements(flag);
    }

    public boolean getCacheCallableStmts() {
        return this.getCacheCallableStatements();
    }

    public void setCachePrepStmts(boolean flag) {
        this.setCachePreparedStatements(flag);
    }

    public boolean getCachePrepStmts() {
        return this.getCachePreparedStatements();
    }

    public void setCallableStmtCacheSize(int cacheSize) throws SQLException {
        this.setCallableStatementCacheSize(cacheSize);
    }

    public int getCallableStmtCacheSize() {
        return this.getCallableStatementCacheSize();
    }

    public void setPrepStmtCacheSize(int cacheSize) throws SQLException {
        this.setPreparedStatementCacheSize(cacheSize);
    }

    public int getPrepStmtCacheSize() {
        return this.getPreparedStatementCacheSize();
    }

    public void setPrepStmtCacheSqlLimit(int sqlLimit) throws SQLException {
        this.setPreparedStatementCacheSqlLimit(sqlLimit);
    }

    public int getPrepStmtCacheSqlLimit() {
        return this.getPreparedStatementCacheSqlLimit();
    }

    public boolean getNoAccessToProcedureBodies() {
        return this.noAccessToProcedureBodies.getValueAsBoolean();
    }

    public void setNoAccessToProcedureBodies(boolean flag) {
        this.noAccessToProcedureBodies.setValue(flag);
    }

    public boolean getUseOldAliasMetadataBehavior() {
        return this.useOldAliasMetadataBehavior.getValueAsBoolean();
    }

    public void setUseOldAliasMetadataBehavior(boolean flag) {
        this.useOldAliasMetadataBehavior.setValue(flag);
    }

    public String getClientCertificateKeyStorePassword() {
        return this.clientCertificateKeyStorePassword.getValueAsString();
    }

    public void setClientCertificateKeyStorePassword(String value) {
        this.clientCertificateKeyStorePassword.setValue(value);
    }

    public String getClientCertificateKeyStoreType() {
        return this.clientCertificateKeyStoreType.getValueAsString();
    }

    public void setClientCertificateKeyStoreType(String value) {
        this.clientCertificateKeyStoreType.setValue(value);
    }

    public String getClientCertificateKeyStoreUrl() {
        return this.clientCertificateKeyStoreUrl.getValueAsString();
    }

    public void setClientCertificateKeyStoreUrl(String value) {
        this.clientCertificateKeyStoreUrl.setValue(value);
    }

    public String getTrustCertificateKeyStorePassword() {
        return this.trustCertificateKeyStorePassword.getValueAsString();
    }

    public void setTrustCertificateKeyStorePassword(String value) {
        this.trustCertificateKeyStorePassword.setValue(value);
    }

    public String getTrustCertificateKeyStoreType() {
        return this.trustCertificateKeyStoreType.getValueAsString();
    }

    public void setTrustCertificateKeyStoreType(String value) {
        this.trustCertificateKeyStoreType.setValue(value);
    }

    public String getTrustCertificateKeyStoreUrl() {
        return this.trustCertificateKeyStoreUrl.getValueAsString();
    }

    public void setTrustCertificateKeyStoreUrl(String value) {
        this.trustCertificateKeyStoreUrl.setValue(value);
    }

    public boolean getUseSSPSCompatibleTimezoneShift() {
        return this.useSSPSCompatibleTimezoneShift.getValueAsBoolean();
    }

    public void setUseSSPSCompatibleTimezoneShift(boolean flag) {
        this.useSSPSCompatibleTimezoneShift.setValue(flag);
    }

    public boolean getTreatUtilDateAsTimestamp() {
        return this.treatUtilDateAsTimestamp.getValueAsBoolean();
    }

    public void setTreatUtilDateAsTimestamp(boolean flag) {
        this.treatUtilDateAsTimestamp.setValue(flag);
    }

    public boolean getUseFastDateParsing() {
        return this.useFastDateParsing.getValueAsBoolean();
    }

    public void setUseFastDateParsing(boolean flag) {
        this.useFastDateParsing.setValue(flag);
    }

    public String getLocalSocketAddress() {
        return this.localSocketAddress.getValueAsString();
    }

    public void setLocalSocketAddress(String address) {
        this.localSocketAddress.setValue(address);
    }

    public void setUseConfigs(String configs) {
        this.useConfigs.setValue(configs);
    }

    public String getUseConfigs() {
        return this.useConfigs.getValueAsString();
    }

    public boolean getGenerateSimpleParameterMetadata() {
        return this.generateSimpleParameterMetadata.getValueAsBoolean();
    }

    public void setGenerateSimpleParameterMetadata(boolean flag) {
        this.generateSimpleParameterMetadata.setValue(flag);
    }

    public boolean getLogXaCommands() {
        return this.logXaCommands.getValueAsBoolean();
    }

    public void setLogXaCommands(boolean flag) {
        this.logXaCommands.setValue(flag);
    }

    public int getResultSetSizeThreshold() {
        return this.resultSetSizeThreshold.getValueAsInt();
    }

    public void setResultSetSizeThreshold(int threshold) throws SQLException {
        this.resultSetSizeThreshold.setValue(threshold, this.getExceptionInterceptor());
    }

    public int getNetTimeoutForStreamingResults() {
        return this.netTimeoutForStreamingResults.getValueAsInt();
    }

    public void setNetTimeoutForStreamingResults(int value) throws SQLException {
        this.netTimeoutForStreamingResults.setValue(value, this.getExceptionInterceptor());
    }

    public boolean getEnableQueryTimeouts() {
        return this.enableQueryTimeouts.getValueAsBoolean();
    }

    public void setEnableQueryTimeouts(boolean flag) {
        this.enableQueryTimeouts.setValue(flag);
    }

    public boolean getPadCharsWithSpace() {
        return this.padCharsWithSpace.getValueAsBoolean();
    }

    public void setPadCharsWithSpace(boolean flag) {
        this.padCharsWithSpace.setValue(flag);
    }

    public boolean getUseDynamicCharsetInfo() {
        return this.useDynamicCharsetInfo.getValueAsBoolean();
    }

    public void setUseDynamicCharsetInfo(boolean flag) {
        this.useDynamicCharsetInfo.setValue(flag);
    }

    public String getClientInfoProvider() {
        return this.clientInfoProvider.getValueAsString();
    }

    public void setClientInfoProvider(String classname) {
        this.clientInfoProvider.setValue(classname);
    }

    public boolean getPopulateInsertRowWithDefaultValues() {
        return this.populateInsertRowWithDefaultValues.getValueAsBoolean();
    }

    public void setPopulateInsertRowWithDefaultValues(boolean flag) {
        this.populateInsertRowWithDefaultValues.setValue(flag);
    }

    public String getLoadBalanceStrategy() {
        return this.loadBalanceStrategy.getValueAsString();
    }

    public void setLoadBalanceStrategy(String strategy) {
        this.loadBalanceStrategy.setValue(strategy);
    }

    public boolean getTcpNoDelay() {
        return this.tcpNoDelay.getValueAsBoolean();
    }

    public void setTcpNoDelay(boolean flag) {
        this.tcpNoDelay.setValue(flag);
    }

    public boolean getTcpKeepAlive() {
        return this.tcpKeepAlive.getValueAsBoolean();
    }

    public void setTcpKeepAlive(boolean flag) {
        this.tcpKeepAlive.setValue(flag);
    }

    public int getTcpRcvBuf() {
        return this.tcpRcvBuf.getValueAsInt();
    }

    public void setTcpRcvBuf(int bufSize) throws SQLException {
        this.tcpRcvBuf.setValue(bufSize, this.getExceptionInterceptor());
    }

    public int getTcpSndBuf() {
        return this.tcpSndBuf.getValueAsInt();
    }

    public void setTcpSndBuf(int bufSize) throws SQLException {
        this.tcpSndBuf.setValue(bufSize, this.getExceptionInterceptor());
    }

    public int getTcpTrafficClass() {
        return this.tcpTrafficClass.getValueAsInt();
    }

    public void setTcpTrafficClass(int classFlags) throws SQLException {
        this.tcpTrafficClass.setValue(classFlags, this.getExceptionInterceptor());
    }

    public boolean getUseNanosForElapsedTime() {
        return this.useNanosForElapsedTime.getValueAsBoolean();
    }

    public void setUseNanosForElapsedTime(boolean flag) {
        this.useNanosForElapsedTime.setValue(flag);
    }

    public long getSlowQueryThresholdNanos() {
        return this.slowQueryThresholdNanos.getValueAsLong();
    }

    public void setSlowQueryThresholdNanos(long nanos) throws SQLException {
        this.slowQueryThresholdNanos.setValue(nanos, this.getExceptionInterceptor());
    }

    public String getStatementInterceptors() {
        return this.statementInterceptors.getValueAsString();
    }

    public void setStatementInterceptors(String value) {
        this.statementInterceptors.setValue(value);
    }

    public boolean getUseDirectRowUnpack() {
        return this.useDirectRowUnpack.getValueAsBoolean();
    }

    public void setUseDirectRowUnpack(boolean flag) {
        this.useDirectRowUnpack.setValue(flag);
    }

    public String getLargeRowSizeThreshold() {
        return this.largeRowSizeThreshold.getValueAsString();
    }

    public void setLargeRowSizeThreshold(String value) throws SQLException {
        this.largeRowSizeThreshold.setValue(value, this.getExceptionInterceptor());
    }

    public boolean getUseBlobToStoreUTF8OutsideBMP() {
        return this.useBlobToStoreUTF8OutsideBMP.getValueAsBoolean();
    }

    public void setUseBlobToStoreUTF8OutsideBMP(boolean flag) {
        this.useBlobToStoreUTF8OutsideBMP.setValue(flag);
    }

    public String getUtf8OutsideBmpExcludedColumnNamePattern() {
        return this.utf8OutsideBmpExcludedColumnNamePattern.getValueAsString();
    }

    public void setUtf8OutsideBmpExcludedColumnNamePattern(String regexPattern) {
        this.utf8OutsideBmpExcludedColumnNamePattern.setValue(regexPattern);
    }

    public String getUtf8OutsideBmpIncludedColumnNamePattern() {
        return this.utf8OutsideBmpIncludedColumnNamePattern.getValueAsString();
    }

    public void setUtf8OutsideBmpIncludedColumnNamePattern(String regexPattern) {
        this.utf8OutsideBmpIncludedColumnNamePattern.setValue(regexPattern);
    }

    public boolean getIncludeInnodbStatusInDeadlockExceptions() {
        return this.includeInnodbStatusInDeadlockExceptions.getValueAsBoolean();
    }

    public void setIncludeInnodbStatusInDeadlockExceptions(boolean flag) {
        this.includeInnodbStatusInDeadlockExceptions.setValue(flag);
    }

    public boolean getBlobsAreStrings() {
        return this.blobsAreStrings.getValueAsBoolean();
    }

    public void setBlobsAreStrings(boolean flag) {
        this.blobsAreStrings.setValue(flag);
    }

    public boolean getFunctionsNeverReturnBlobs() {
        return this.functionsNeverReturnBlobs.getValueAsBoolean();
    }

    public void setFunctionsNeverReturnBlobs(boolean flag) {
        this.functionsNeverReturnBlobs.setValue(flag);
    }

    public boolean getAutoSlowLog() {
        return this.autoSlowLog.getValueAsBoolean();
    }

    public void setAutoSlowLog(boolean flag) {
        this.autoSlowLog.setValue(flag);
    }

    public String getConnectionLifecycleInterceptors() {
        return this.connectionLifecycleInterceptors.getValueAsString();
    }

    public void setConnectionLifecycleInterceptors(String interceptors) {
        this.connectionLifecycleInterceptors.setValue(interceptors);
    }

    public String getProfilerEventHandler() {
        return this.profilerEventHandler.getValueAsString();
    }

    public void setProfilerEventHandler(String handler) {
        this.profilerEventHandler.setValue(handler);
    }

    public boolean getVerifyServerCertificate() {
        return this.verifyServerCertificate.getValueAsBoolean();
    }

    public void setVerifyServerCertificate(boolean flag) {
        this.verifyServerCertificate.setValue(flag);
    }

    public boolean getUseLegacyDatetimeCode() {
        return this.useLegacyDatetimeCode.getValueAsBoolean();
    }

    public void setUseLegacyDatetimeCode(boolean flag) {
        this.useLegacyDatetimeCode.setValue(flag);
    }

    public boolean getSendFractionalSeconds() {
        return this.sendFractionalSeconds.getValueAsBoolean();
    }

    public void setSendFractionalSeconds(boolean flag) {
        this.sendFractionalSeconds.setValue(flag);
    }

    public int getSelfDestructOnPingSecondsLifetime() {
        return this.selfDestructOnPingSecondsLifetime.getValueAsInt();
    }

    public void setSelfDestructOnPingSecondsLifetime(int seconds) throws SQLException {
        this.selfDestructOnPingSecondsLifetime.setValue(seconds, this.getExceptionInterceptor());
    }

    public int getSelfDestructOnPingMaxOperations() {
        return this.selfDestructOnPingMaxOperations.getValueAsInt();
    }

    public void setSelfDestructOnPingMaxOperations(int maxOperations) throws SQLException {
        this.selfDestructOnPingMaxOperations.setValue(maxOperations, this.getExceptionInterceptor());
    }

    public boolean getUseColumnNamesInFindColumn() {
        return this.useColumnNamesInFindColumn.getValueAsBoolean();
    }

    public void setUseColumnNamesInFindColumn(boolean flag) {
        this.useColumnNamesInFindColumn.setValue(flag);
    }

    public boolean getUseLocalTransactionState() {
        return this.useLocalTransactionState.getValueAsBoolean();
    }

    public void setUseLocalTransactionState(boolean flag) {
        this.useLocalTransactionState.setValue(flag);
    }

    public boolean getCompensateOnDuplicateKeyUpdateCounts() {
        return this.compensateOnDuplicateKeyUpdateCounts.getValueAsBoolean();
    }

    public void setCompensateOnDuplicateKeyUpdateCounts(boolean flag) {
        this.compensateOnDuplicateKeyUpdateCounts.setValue(flag);
    }

    public int getLoadBalanceBlacklistTimeout() {
        return this.loadBalanceBlacklistTimeout.getValueAsInt();
    }

    public void setLoadBalanceBlacklistTimeout(int loadBalanceBlacklistTimeout) throws SQLException {
        this.loadBalanceBlacklistTimeout.setValue(loadBalanceBlacklistTimeout, this.getExceptionInterceptor());
    }

    public int getLoadBalancePingTimeout() {
        return this.loadBalancePingTimeout.getValueAsInt();
    }

    public void setLoadBalancePingTimeout(int loadBalancePingTimeout) throws SQLException {
        this.loadBalancePingTimeout.setValue(loadBalancePingTimeout, this.getExceptionInterceptor());
    }

    public void setRetriesAllDown(int retriesAllDown) throws SQLException {
        this.retriesAllDown.setValue(retriesAllDown, this.getExceptionInterceptor());
    }

    public int getRetriesAllDown() {
        return this.retriesAllDown.getValueAsInt();
    }

    public void setUseAffectedRows(boolean flag) {
        this.useAffectedRows.setValue(flag);
    }

    public boolean getUseAffectedRows() {
        return this.useAffectedRows.getValueAsBoolean();
    }

    public void setPasswordCharacterEncoding(String characterSet) {
        this.passwordCharacterEncoding.setValue(characterSet);
    }

    public String getPasswordCharacterEncoding() {
        String encoding = this.passwordCharacterEncoding.getValueAsString();
        if (encoding != null) {
            return encoding;
        }
        if (this.getUseUnicode() && (encoding = this.getEncoding()) != null) {
            return encoding;
        }
        return "UTF-8";
    }

    public void setExceptionInterceptors(String exceptionInterceptors) {
        this.exceptionInterceptors.setValue(exceptionInterceptors);
    }

    public String getExceptionInterceptors() {
        return this.exceptionInterceptors.getValueAsString();
    }

    public void setMaxAllowedPacket(int max) throws SQLException {
        this.maxAllowedPacket.setValue(max, this.getExceptionInterceptor());
    }

    public int getMaxAllowedPacket() {
        return this.maxAllowedPacket.getValueAsInt();
    }

    public boolean getQueryTimeoutKillsConnection() {
        return this.queryTimeoutKillsConnection.getValueAsBoolean();
    }

    public void setQueryTimeoutKillsConnection(boolean queryTimeoutKillsConnection) {
        this.queryTimeoutKillsConnection.setValue(queryTimeoutKillsConnection);
    }

    public boolean getLoadBalanceValidateConnectionOnSwapServer() {
        return this.loadBalanceValidateConnectionOnSwapServer.getValueAsBoolean();
    }

    public void setLoadBalanceValidateConnectionOnSwapServer(boolean loadBalanceValidateConnectionOnSwapServer) {
        this.loadBalanceValidateConnectionOnSwapServer.setValue(loadBalanceValidateConnectionOnSwapServer);
    }

    public String getLoadBalanceConnectionGroup() {
        return this.loadBalanceConnectionGroup.getValueAsString();
    }

    public void setLoadBalanceConnectionGroup(String loadBalanceConnectionGroup) {
        this.loadBalanceConnectionGroup.setValue(loadBalanceConnectionGroup);
    }

    public String getLoadBalanceExceptionChecker() {
        return this.loadBalanceExceptionChecker.getValueAsString();
    }

    public void setLoadBalanceExceptionChecker(String loadBalanceExceptionChecker) {
        this.loadBalanceExceptionChecker.setValue(loadBalanceExceptionChecker);
    }

    public String getLoadBalanceSQLStateFailover() {
        return this.loadBalanceSQLStateFailover.getValueAsString();
    }

    public void setLoadBalanceSQLStateFailover(String loadBalanceSQLStateFailover) {
        this.loadBalanceSQLStateFailover.setValue(loadBalanceSQLStateFailover);
    }

    public String getLoadBalanceSQLExceptionSubclassFailover() {
        return this.loadBalanceSQLExceptionSubclassFailover.getValueAsString();
    }

    public void setLoadBalanceSQLExceptionSubclassFailover(String loadBalanceSQLExceptionSubclassFailover) {
        this.loadBalanceSQLExceptionSubclassFailover.setValue(loadBalanceSQLExceptionSubclassFailover);
    }

    public boolean getLoadBalanceEnableJMX() {
        return this.loadBalanceEnableJMX.getValueAsBoolean();
    }

    public void setLoadBalanceEnableJMX(boolean loadBalanceEnableJMX) {
        this.loadBalanceEnableJMX.setValue(loadBalanceEnableJMX);
    }

    public void setLoadBalanceHostRemovalGracePeriod(int loadBalanceHostRemovalGracePeriod) throws SQLException {
        this.loadBalanceHostRemovalGracePeriod.setValue(loadBalanceHostRemovalGracePeriod, this.getExceptionInterceptor());
    }

    public int getLoadBalanceHostRemovalGracePeriod() {
        return this.loadBalanceHostRemovalGracePeriod.getValueAsInt();
    }

    public void setLoadBalanceAutoCommitStatementThreshold(int loadBalanceAutoCommitStatementThreshold) throws SQLException {
        this.loadBalanceAutoCommitStatementThreshold.setValue(loadBalanceAutoCommitStatementThreshold, this.getExceptionInterceptor());
    }

    public int getLoadBalanceAutoCommitStatementThreshold() {
        return this.loadBalanceAutoCommitStatementThreshold.getValueAsInt();
    }

    public void setLoadBalanceAutoCommitStatementRegex(String loadBalanceAutoCommitStatementRegex) {
        this.loadBalanceAutoCommitStatementRegex.setValue(loadBalanceAutoCommitStatementRegex);
    }

    public String getLoadBalanceAutoCommitStatementRegex() {
        return this.loadBalanceAutoCommitStatementRegex.getValueAsString();
    }

    public void setIncludeThreadDumpInDeadlockExceptions(boolean flag) {
        this.includeThreadDumpInDeadlockExceptions.setValue(flag);
    }

    public boolean getIncludeThreadDumpInDeadlockExceptions() {
        return this.includeThreadDumpInDeadlockExceptions.getValueAsBoolean();
    }

    public void setIncludeThreadNamesAsStatementComment(boolean flag) {
        this.includeThreadNamesAsStatementComment.setValue(flag);
    }

    public boolean getIncludeThreadNamesAsStatementComment() {
        return this.includeThreadNamesAsStatementComment.getValueAsBoolean();
    }

    public void setAuthenticationPlugins(String authenticationPlugins) {
        this.authenticationPlugins.setValue(authenticationPlugins);
    }

    public String getAuthenticationPlugins() {
        return this.authenticationPlugins.getValueAsString();
    }

    public void setDisabledAuthenticationPlugins(String disabledAuthenticationPlugins) {
        this.disabledAuthenticationPlugins.setValue(disabledAuthenticationPlugins);
    }

    public String getDisabledAuthenticationPlugins() {
        return this.disabledAuthenticationPlugins.getValueAsString();
    }

    public void setDefaultAuthenticationPlugin(String defaultAuthenticationPlugin) {
        this.defaultAuthenticationPlugin.setValue(defaultAuthenticationPlugin);
    }

    public String getDefaultAuthenticationPlugin() {
        return this.defaultAuthenticationPlugin.getValueAsString();
    }

    public void setParseInfoCacheFactory(String factoryClassname) {
        this.parseInfoCacheFactory.setValue(factoryClassname);
    }

    public String getParseInfoCacheFactory() {
        return this.parseInfoCacheFactory.getValueAsString();
    }

    public void setServerConfigCacheFactory(String factoryClassname) {
        this.serverConfigCacheFactory.setValue(factoryClassname);
    }

    public String getServerConfigCacheFactory() {
        return this.serverConfigCacheFactory.getValueAsString();
    }

    public void setDisconnectOnExpiredPasswords(boolean disconnectOnExpiredPasswords) {
        this.disconnectOnExpiredPasswords.setValue(disconnectOnExpiredPasswords);
    }

    public boolean getDisconnectOnExpiredPasswords() {
        return this.disconnectOnExpiredPasswords.getValueAsBoolean();
    }

    public boolean getAllowMasterDownConnections() {
        return this.allowMasterDownConnections.getValueAsBoolean();
    }

    public void setAllowMasterDownConnections(boolean connectIfMasterDown) {
        this.allowMasterDownConnections.setValue(connectIfMasterDown);
    }

    public boolean getAllowSlaveDownConnections() {
        return this.allowSlaveDownConnections.getValueAsBoolean();
    }

    public void setAllowSlaveDownConnections(boolean connectIfSlaveDown) {
        this.allowSlaveDownConnections.setValue(connectIfSlaveDown);
    }

    public boolean getReadFromMasterWhenNoSlaves() {
        return this.readFromMasterWhenNoSlaves.getValueAsBoolean();
    }

    public void setReadFromMasterWhenNoSlaves(boolean useMasterIfSlavesDown) {
        this.readFromMasterWhenNoSlaves.setValue(useMasterIfSlavesDown);
    }

    public boolean getReplicationEnableJMX() {
        return this.replicationEnableJMX.getValueAsBoolean();
    }

    public void setReplicationEnableJMX(boolean replicationEnableJMX) {
        this.replicationEnableJMX.setValue(replicationEnableJMX);
    }

    public void setGetProceduresReturnsFunctions(boolean getProcedureReturnsFunctions) {
        this.getProceduresReturnsFunctions.setValue(getProcedureReturnsFunctions);
    }

    public boolean getGetProceduresReturnsFunctions() {
        return this.getProceduresReturnsFunctions.getValueAsBoolean();
    }

    public void setDetectCustomCollations(boolean detectCustomCollations) {
        this.detectCustomCollations.setValue(detectCustomCollations);
    }

    public boolean getDetectCustomCollations() {
        return this.detectCustomCollations.getValueAsBoolean();
    }

    public String getServerRSAPublicKeyFile() {
        return this.serverRSAPublicKeyFile.getValueAsString();
    }

    public void setServerRSAPublicKeyFile(String serverRSAPublicKeyFile) throws SQLException {
        if (this.serverRSAPublicKeyFile.getUpdateCount() > 0) {
            throw SQLError.createSQLException(Messages.getString("ConnectionProperties.dynamicChangeIsNotAllowed", new Object[]{"'serverRSAPublicKeyFile'"}), "S1009", null);
        }
        this.serverRSAPublicKeyFile.setValue(serverRSAPublicKeyFile);
    }

    public boolean getAllowPublicKeyRetrieval() {
        return this.allowPublicKeyRetrieval.getValueAsBoolean();
    }

    public void setAllowPublicKeyRetrieval(boolean allowPublicKeyRetrieval) throws SQLException {
        if (this.allowPublicKeyRetrieval.getUpdateCount() > 0) {
            throw SQLError.createSQLException(Messages.getString("ConnectionProperties.dynamicChangeIsNotAllowed", new Object[]{"'allowPublicKeyRetrieval'"}), "S1009", null);
        }
        this.allowPublicKeyRetrieval.setValue(allowPublicKeyRetrieval);
    }

    public void setDontCheckOnDuplicateKeyUpdateInSQL(boolean dontCheckOnDuplicateKeyUpdateInSQL) {
        this.dontCheckOnDuplicateKeyUpdateInSQL.setValue(dontCheckOnDuplicateKeyUpdateInSQL);
    }

    public boolean getDontCheckOnDuplicateKeyUpdateInSQL() {
        return this.dontCheckOnDuplicateKeyUpdateInSQL.getValueAsBoolean();
    }

    public void setSocksProxyHost(String socksProxyHost) {
        this.socksProxyHost.setValue(socksProxyHost);
    }

    public String getSocksProxyHost() {
        return this.socksProxyHost.getValueAsString();
    }

    public void setSocksProxyPort(int socksProxyPort) throws SQLException {
        this.socksProxyPort.setValue(socksProxyPort, null);
    }

    public int getSocksProxyPort() {
        return this.socksProxyPort.getValueAsInt();
    }

    public boolean getReadOnlyPropagatesToServer() {
        return this.readOnlyPropagatesToServer.getValueAsBoolean();
    }

    public void setReadOnlyPropagatesToServer(boolean flag) {
        this.readOnlyPropagatesToServer.setValue(flag);
    }

    public String getEnabledSSLCipherSuites() {
        return this.enabledSSLCipherSuites.getValueAsString();
    }

    public void setEnabledSSLCipherSuites(String cipherSuites) {
        this.enabledSSLCipherSuites.setValue(cipherSuites);
    }

    public boolean getEnableEscapeProcessing() {
        return this.enableEscapeProcessing.getValueAsBoolean();
    }

    public void setEnableEscapeProcessing(boolean flag) {
        this.enableEscapeProcessing.setValue(flag);
    }

    static {
        try {
            Field[] declaredFields = ConnectionPropertiesImpl.class.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; ++i) {
                if (!ConnectionProperty.class.isAssignableFrom(declaredFields[i].getType())) continue;
                PROPERTY_LIST.add(declaredFields[i]);
            }
        }
        catch (Exception ex) {
            RuntimeException rtEx = new RuntimeException();
            rtEx.initCause(ex);
            throw rtEx;
        }
    }

    class XmlMap {
        protected Map<Integer, Map<String, ConnectionProperty>> ordered = new TreeMap<Integer, Map<String, ConnectionProperty>>();
        protected Map<String, ConnectionProperty> alpha = new TreeMap<String, ConnectionProperty>();

        XmlMap() {
        }
    }

    static class StringConnectionProperty
    extends ConnectionProperty
    implements Serializable {
        private static final long serialVersionUID = 5432127962785948272L;

        StringConnectionProperty(String propertyNameToSet, String defaultValueToSet, String descriptionToSet, String sinceVersionToSet, String category, int orderInCategory) {
            this(propertyNameToSet, defaultValueToSet, null, descriptionToSet, sinceVersionToSet, category, orderInCategory);
        }

        StringConnectionProperty(String propertyNameToSet, String defaultValueToSet, String[] allowableValuesToSet, String descriptionToSet, String sinceVersionToSet, String category, int orderInCategory) {
            super(propertyNameToSet, defaultValueToSet, allowableValuesToSet, 0, 0, descriptionToSet, sinceVersionToSet, category, orderInCategory);
        }

        String getValueAsString() {
            return (String)this.valueAsObject;
        }

        boolean hasValueConstraints() {
            return this.allowableValues != null && this.allowableValues.length > 0;
        }

        void initializeFrom(String extractedValue, ExceptionInterceptor exceptionInterceptor) throws SQLException {
            if (extractedValue != null) {
                this.validateStringValues(extractedValue, exceptionInterceptor);
                this.valueAsObject = extractedValue;
                this.wasExplicitlySet = true;
            } else {
                this.valueAsObject = this.defaultValue;
            }
            ++this.updateCount;
        }

        boolean isRangeBased() {
            return false;
        }

        void setValue(String valueFlag) {
            this.valueAsObject = valueFlag;
            this.wasExplicitlySet = true;
            ++this.updateCount;
        }
    }

    static class MemorySizeConnectionProperty
    extends IntegerConnectionProperty
    implements Serializable {
        private static final long serialVersionUID = 7351065128998572656L;
        private String valueAsString;

        MemorySizeConnectionProperty(String propertyNameToSet, int defaultValueToSet, int lowerBoundToSet, int upperBoundToSet, String descriptionToSet, String sinceVersionToSet, String category, int orderInCategory) {
            super(propertyNameToSet, defaultValueToSet, lowerBoundToSet, upperBoundToSet, descriptionToSet, sinceVersionToSet, category, orderInCategory);
        }

        void initializeFrom(String extractedValue, ExceptionInterceptor exceptionInterceptor) throws SQLException {
            this.valueAsString = extractedValue;
            this.multiplier = 1;
            if (extractedValue != null) {
                if (extractedValue.endsWith("k") || extractedValue.endsWith("K") || extractedValue.endsWith("kb") || extractedValue.endsWith("Kb") || extractedValue.endsWith("kB") || extractedValue.endsWith("KB")) {
                    this.multiplier = 1024;
                    int indexOfK = StringUtils.indexOfIgnoreCase(extractedValue, "k");
                    extractedValue = extractedValue.substring(0, indexOfK);
                } else if (extractedValue.endsWith("m") || extractedValue.endsWith("M") || extractedValue.endsWith("mb") || extractedValue.endsWith("Mb") || extractedValue.endsWith("mB") || extractedValue.endsWith("MB")) {
                    this.multiplier = 0x100000;
                    int indexOfM = StringUtils.indexOfIgnoreCase(extractedValue, "m");
                    extractedValue = extractedValue.substring(0, indexOfM);
                } else if (extractedValue.endsWith("g") || extractedValue.endsWith("G") || extractedValue.endsWith("gb") || extractedValue.endsWith("Gb") || extractedValue.endsWith("gB") || extractedValue.endsWith("GB")) {
                    this.multiplier = 0x40000000;
                    int indexOfG = StringUtils.indexOfIgnoreCase(extractedValue, "g");
                    extractedValue = extractedValue.substring(0, indexOfG);
                }
            }
            super.initializeFrom(extractedValue, exceptionInterceptor);
        }

        void setValue(String value, ExceptionInterceptor exceptionInterceptor) throws SQLException {
            this.initializeFrom(value, exceptionInterceptor);
        }

        String getValueAsString() {
            return this.valueAsString;
        }
    }

    public static class LongConnectionProperty
    extends IntegerConnectionProperty {
        private static final long serialVersionUID = 6068572984340480895L;

        LongConnectionProperty(String propertyNameToSet, long defaultValueToSet, long lowerBoundToSet, long upperBoundToSet, String descriptionToSet, String sinceVersionToSet, String category, int orderInCategory) {
            super(propertyNameToSet, defaultValueToSet, null, (int)lowerBoundToSet, (int)upperBoundToSet, descriptionToSet, sinceVersionToSet, category, orderInCategory);
        }

        LongConnectionProperty(String propertyNameToSet, long defaultValueToSet, String descriptionToSet, String sinceVersionToSet, String category, int orderInCategory) {
            this(propertyNameToSet, defaultValueToSet, 0L, 0L, descriptionToSet, sinceVersionToSet, category, orderInCategory);
        }

        void setValue(long longValue, ExceptionInterceptor exceptionInterceptor) throws SQLException {
            this.setValue(longValue, null, exceptionInterceptor);
        }

        void setValue(long longValue, String valueAsString, ExceptionInterceptor exceptionInterceptor) throws SQLException {
            if (this.isRangeBased() && (longValue < (long)this.getLowerBound() || longValue > (long)this.getUpperBound())) {
                throw SQLError.createSQLException("The connection property '" + this.getPropertyName() + "' only accepts long integer values in the range of " + this.getLowerBound() + " - " + this.getUpperBound() + ", the value '" + (valueAsString == null ? Long.valueOf(longValue) : valueAsString) + "' exceeds this range.", "S1009", exceptionInterceptor);
            }
            this.valueAsObject = longValue;
            this.wasExplicitlySet = true;
            ++this.updateCount;
        }

        long getValueAsLong() {
            return (Long)this.valueAsObject;
        }

        void initializeFrom(String extractedValue, ExceptionInterceptor exceptionInterceptor) throws SQLException {
            if (extractedValue != null) {
                try {
                    long longValue = Double.valueOf(extractedValue).longValue();
                    this.setValue(longValue, extractedValue, exceptionInterceptor);
                }
                catch (NumberFormatException nfe) {
                    throw SQLError.createSQLException("The connection property '" + this.getPropertyName() + "' only accepts long integer values. The value '" + extractedValue + "' can not be converted to a long integer.", "S1009", exceptionInterceptor);
                }
            } else {
                this.valueAsObject = this.defaultValue;
            }
            ++this.updateCount;
        }
    }

    static class IntegerConnectionProperty
    extends ConnectionProperty
    implements Serializable {
        private static final long serialVersionUID = -3004305481796850832L;
        int multiplier = 1;

        public IntegerConnectionProperty(String propertyNameToSet, Object defaultValueToSet, String[] allowableValuesToSet, int lowerBoundToSet, int upperBoundToSet, String descriptionToSet, String sinceVersionToSet, String category, int orderInCategory) {
            super(propertyNameToSet, defaultValueToSet, allowableValuesToSet, lowerBoundToSet, upperBoundToSet, descriptionToSet, sinceVersionToSet, category, orderInCategory);
        }

        IntegerConnectionProperty(String propertyNameToSet, int defaultValueToSet, int lowerBoundToSet, int upperBoundToSet, String descriptionToSet, String sinceVersionToSet, String category, int orderInCategory) {
            super(propertyNameToSet, defaultValueToSet, null, lowerBoundToSet, upperBoundToSet, descriptionToSet, sinceVersionToSet, category, orderInCategory);
        }

        IntegerConnectionProperty(String propertyNameToSet, int defaultValueToSet, String descriptionToSet, String sinceVersionToSet, String category, int orderInCategory) {
            this(propertyNameToSet, defaultValueToSet, 0, 0, descriptionToSet, sinceVersionToSet, category, orderInCategory);
        }

        String[] getAllowableValues() {
            return null;
        }

        int getLowerBound() {
            return this.lowerBound;
        }

        int getUpperBound() {
            return this.upperBound;
        }

        int getValueAsInt() {
            return (Integer)this.valueAsObject;
        }

        boolean hasValueConstraints() {
            return false;
        }

        void initializeFrom(String extractedValue, ExceptionInterceptor exceptionInterceptor) throws SQLException {
            if (extractedValue != null) {
                try {
                    int intValue = (int)(Double.valueOf(extractedValue) * (double)this.multiplier);
                    this.setValue(intValue, extractedValue, exceptionInterceptor);
                }
                catch (NumberFormatException nfe) {
                    throw SQLError.createSQLException("The connection property '" + this.getPropertyName() + "' only accepts integer values. The value '" + extractedValue + "' can not be converted to an integer.", "S1009", exceptionInterceptor);
                }
            } else {
                this.valueAsObject = this.defaultValue;
            }
            ++this.updateCount;
        }

        boolean isRangeBased() {
            return this.getUpperBound() != this.getLowerBound();
        }

        void setValue(int intValue, ExceptionInterceptor exceptionInterceptor) throws SQLException {
            this.setValue(intValue, null, exceptionInterceptor);
        }

        void setValue(int intValue, String valueAsString, ExceptionInterceptor exceptionInterceptor) throws SQLException {
            if (this.isRangeBased() && (intValue < this.getLowerBound() || intValue > this.getUpperBound())) {
                throw SQLError.createSQLException("The connection property '" + this.getPropertyName() + "' only accepts integer values in the range of " + this.getLowerBound() + " - " + this.getUpperBound() + ", the value '" + (valueAsString == null ? Integer.valueOf(intValue) : valueAsString) + "' exceeds this range.", "S1009", exceptionInterceptor);
            }
            this.valueAsObject = intValue;
            this.wasExplicitlySet = true;
            ++this.updateCount;
        }
    }

    static abstract class ConnectionProperty
    implements Serializable {
        static final long serialVersionUID = -6644853639584478367L;
        String[] allowableValues;
        String categoryName;
        Object defaultValue;
        int lowerBound;
        int order;
        String propertyName;
        String sinceVersion;
        int upperBound;
        Object valueAsObject;
        boolean required;
        String description;
        int updateCount = 0;
        boolean wasExplicitlySet = false;

        public ConnectionProperty() {
        }

        ConnectionProperty(String propertyNameToSet, Object defaultValueToSet, String[] allowableValuesToSet, int lowerBoundToSet, int upperBoundToSet, String descriptionToSet, String sinceVersionToSet, String category, int orderInCategory) {
            this.description = descriptionToSet;
            this.propertyName = propertyNameToSet;
            this.defaultValue = defaultValueToSet;
            this.valueAsObject = defaultValueToSet;
            this.allowableValues = allowableValuesToSet;
            this.lowerBound = lowerBoundToSet;
            this.upperBound = upperBoundToSet;
            this.required = false;
            this.sinceVersion = sinceVersionToSet;
            this.categoryName = category;
            this.order = orderInCategory;
        }

        String[] getAllowableValues() {
            return this.allowableValues;
        }

        String getCategoryName() {
            return this.categoryName;
        }

        Object getDefaultValue() {
            return this.defaultValue;
        }

        int getLowerBound() {
            return this.lowerBound;
        }

        int getOrder() {
            return this.order;
        }

        String getPropertyName() {
            return this.propertyName;
        }

        int getUpperBound() {
            return this.upperBound;
        }

        Object getValueAsObject() {
            return this.valueAsObject;
        }

        int getUpdateCount() {
            return this.updateCount;
        }

        boolean isExplicitlySet() {
            return this.wasExplicitlySet;
        }

        abstract boolean hasValueConstraints();

        void initializeFrom(Properties extractFrom, ExceptionInterceptor exceptionInterceptor) throws SQLException {
            String extractedValue = extractFrom.getProperty(this.getPropertyName());
            extractFrom.remove(this.getPropertyName());
            this.initializeFrom(extractedValue, exceptionInterceptor);
        }

        void initializeFrom(Reference ref, ExceptionInterceptor exceptionInterceptor) throws SQLException {
            RefAddr refAddr = ref.get(this.getPropertyName());
            if (refAddr != null) {
                String refContentAsString = (String)refAddr.getContent();
                this.initializeFrom(refContentAsString, exceptionInterceptor);
            }
        }

        abstract void initializeFrom(String var1, ExceptionInterceptor var2) throws SQLException;

        abstract boolean isRangeBased();

        void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        void setOrder(int order) {
            this.order = order;
        }

        void setValueAsObject(Object obj) {
            this.valueAsObject = obj;
            ++this.updateCount;
        }

        void storeTo(Reference ref) {
            if (this.getValueAsObject() != null) {
                ref.add(new StringRefAddr(this.getPropertyName(), this.getValueAsObject().toString()));
            }
        }

        DriverPropertyInfo getAsDriverPropertyInfo() {
            DriverPropertyInfo dpi = new DriverPropertyInfo(this.propertyName, null);
            dpi.choices = this.getAllowableValues();
            dpi.value = this.valueAsObject != null ? this.valueAsObject.toString() : null;
            dpi.required = this.required;
            dpi.description = this.description;
            return dpi;
        }

        void validateStringValues(String valueToValidate, ExceptionInterceptor exceptionInterceptor) throws SQLException {
            String[] validateAgainst = this.getAllowableValues();
            if (valueToValidate == null) {
                return;
            }
            if (validateAgainst == null || validateAgainst.length == 0) {
                return;
            }
            for (int i = 0; i < validateAgainst.length; ++i) {
                if (validateAgainst[i] == null || !validateAgainst[i].equalsIgnoreCase(valueToValidate)) continue;
                return;
            }
            StringBuilder errorMessageBuf = new StringBuilder();
            errorMessageBuf.append("The connection property '");
            errorMessageBuf.append(this.getPropertyName());
            errorMessageBuf.append("' only accepts values of the form: ");
            if (validateAgainst.length != 0) {
                errorMessageBuf.append("'");
                errorMessageBuf.append(validateAgainst[0]);
                errorMessageBuf.append("'");
                for (int i = 1; i < validateAgainst.length - 1; ++i) {
                    errorMessageBuf.append(", ");
                    errorMessageBuf.append("'");
                    errorMessageBuf.append(validateAgainst[i]);
                    errorMessageBuf.append("'");
                }
                errorMessageBuf.append(" or '");
                errorMessageBuf.append(validateAgainst[validateAgainst.length - 1]);
                errorMessageBuf.append("'");
            }
            errorMessageBuf.append(". The value '");
            errorMessageBuf.append(valueToValidate);
            errorMessageBuf.append("' is not in this set.");
            throw SQLError.createSQLException(errorMessageBuf.toString(), "S1009", exceptionInterceptor);
        }
    }

    static class BooleanConnectionProperty
    extends ConnectionProperty
    implements Serializable {
        private static final long serialVersionUID = 2540132501709159404L;

        BooleanConnectionProperty(String propertyNameToSet, boolean defaultValueToSet, String descriptionToSet, String sinceVersionToSet, String category, int orderInCategory) {
            super(propertyNameToSet, defaultValueToSet, null, 0, 0, descriptionToSet, sinceVersionToSet, category, orderInCategory);
        }

        String[] getAllowableValues() {
            return new String[]{"true", "false", "yes", "no"};
        }

        boolean getValueAsBoolean() {
            return (Boolean)this.valueAsObject;
        }

        boolean hasValueConstraints() {
            return true;
        }

        void initializeFrom(String extractedValue, ExceptionInterceptor exceptionInterceptor) throws SQLException {
            if (extractedValue != null) {
                this.validateStringValues(extractedValue, exceptionInterceptor);
                this.valueAsObject = extractedValue.equalsIgnoreCase("TRUE") || extractedValue.equalsIgnoreCase("YES");
                this.wasExplicitlySet = true;
            } else {
                this.valueAsObject = this.defaultValue;
            }
            ++this.updateCount;
        }

        boolean isRangeBased() {
            return false;
        }

        void setValue(boolean valueFlag) {
            this.valueAsObject = valueFlag;
            this.wasExplicitlySet = true;
            ++this.updateCount;
        }
    }
}


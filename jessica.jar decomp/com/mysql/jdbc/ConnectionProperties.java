/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.ExceptionInterceptor;
import java.sql.SQLException;

public interface ConnectionProperties {
    public String exposeAsXml() throws SQLException;

    public boolean getAllowLoadLocalInfile();

    public boolean getAllowMultiQueries();

    public boolean getAllowNanAndInf();

    public boolean getAllowUrlInLocalInfile();

    public boolean getAlwaysSendSetIsolation();

    public boolean getAutoDeserialize();

    public boolean getAutoGenerateTestcaseScript();

    public boolean getAutoReconnectForPools();

    public int getBlobSendChunkSize();

    public boolean getCacheCallableStatements();

    public boolean getCachePreparedStatements();

    public boolean getCacheResultSetMetadata();

    public boolean getCacheServerConfiguration();

    public int getCallableStatementCacheSize();

    public boolean getCapitalizeTypeNames();

    public String getCharacterSetResults();

    public boolean getClobberStreamingResults();

    public String getClobCharacterEncoding();

    public String getConnectionCollation();

    public int getConnectTimeout();

    public boolean getContinueBatchOnError();

    public boolean getCreateDatabaseIfNotExist();

    public int getDefaultFetchSize();

    public boolean getDontTrackOpenResources();

    public boolean getDumpQueriesOnException();

    public boolean getDynamicCalendars();

    public boolean getElideSetAutoCommits();

    public boolean getEmptyStringsConvertToZero();

    public boolean getEmulateLocators();

    public boolean getEmulateUnsupportedPstmts();

    public boolean getEnablePacketDebug();

    public String getEncoding();

    public boolean getExplainSlowQueries();

    public boolean getFailOverReadOnly();

    public boolean getGatherPerformanceMetrics();

    public boolean getHoldResultsOpenOverStatementClose();

    public boolean getIgnoreNonTxTables();

    public int getInitialTimeout();

    public boolean getInteractiveClient();

    public boolean getIsInteractiveClient();

    public boolean getJdbcCompliantTruncation();

    public int getLocatorFetchBufferSize();

    public String getLogger();

    public String getLoggerClassName();

    public boolean getLogSlowQueries();

    public boolean getMaintainTimeStats();

    public int getMaxQuerySizeToLog();

    public int getMaxReconnects();

    public int getMaxRows();

    public int getMetadataCacheSize();

    public boolean getNoDatetimeStringSync();

    public boolean getNullCatalogMeansCurrent();

    public boolean getNullNamePatternMatchesAll();

    public int getPacketDebugBufferSize();

    public boolean getParanoid();

    public boolean getPedantic();

    public int getPreparedStatementCacheSize();

    public int getPreparedStatementCacheSqlLimit();

    public boolean getProfileSql();

    public boolean getProfileSQL();

    public String getPropertiesTransform();

    public int getQueriesBeforeRetryMaster();

    public boolean getReconnectAtTxEnd();

    public boolean getRelaxAutoCommit();

    public int getReportMetricsIntervalMillis();

    public boolean getRequireSSL();

    public boolean getRollbackOnPooledClose();

    public boolean getRoundRobinLoadBalance();

    public boolean getRunningCTS13();

    public int getSecondsBeforeRetryMaster();

    public String getServerTimezone();

    public String getSessionVariables();

    public int getSlowQueryThresholdMillis();

    public String getSocketFactoryClassName();

    public int getSocketTimeout();

    public boolean getStrictFloatingPoint();

    public boolean getStrictUpdates();

    public boolean getTinyInt1isBit();

    public boolean getTraceProtocol();

    public boolean getTransformedBitIsBoolean();

    public boolean getUseCompression();

    public boolean getUseFastIntParsing();

    public boolean getUseHostsInPrivileges();

    public boolean getUseInformationSchema();

    public boolean getUseLocalSessionState();

    public boolean getUseOldUTF8Behavior();

    public boolean getUseOnlyServerErrorMessages();

    public boolean getUseReadAheadInput();

    public boolean getUseServerPreparedStmts();

    public boolean getUseSqlStateCodes();

    public boolean getUseSSL();

    public boolean isUseSSLExplicit();

    public boolean getUseStreamLengthsInPrepStmts();

    public boolean getUseTimezone();

    public boolean getUseUltraDevWorkAround();

    public boolean getUseUnbufferedInput();

    public boolean getUseUnicode();

    public boolean getUseUsageAdvisor();

    public boolean getYearIsDateType();

    public String getZeroDateTimeBehavior();

    public void setAllowLoadLocalInfile(boolean var1);

    public void setAllowMultiQueries(boolean var1);

    public void setAllowNanAndInf(boolean var1);

    public void setAllowUrlInLocalInfile(boolean var1);

    public void setAlwaysSendSetIsolation(boolean var1);

    public void setAutoDeserialize(boolean var1);

    public void setAutoGenerateTestcaseScript(boolean var1);

    public void setAutoReconnect(boolean var1);

    public void setAutoReconnectForConnectionPools(boolean var1);

    public void setAutoReconnectForPools(boolean var1);

    public void setBlobSendChunkSize(String var1) throws SQLException;

    public void setCacheCallableStatements(boolean var1);

    public void setCachePreparedStatements(boolean var1);

    public void setCacheResultSetMetadata(boolean var1);

    public void setCacheServerConfiguration(boolean var1);

    public void setCallableStatementCacheSize(int var1) throws SQLException;

    public void setCapitalizeDBMDTypes(boolean var1);

    public void setCapitalizeTypeNames(boolean var1);

    public void setCharacterEncoding(String var1);

    public void setCharacterSetResults(String var1);

    public void setClobberStreamingResults(boolean var1);

    public void setClobCharacterEncoding(String var1);

    public void setConnectionCollation(String var1);

    public void setConnectTimeout(int var1) throws SQLException;

    public void setContinueBatchOnError(boolean var1);

    public void setCreateDatabaseIfNotExist(boolean var1);

    public void setDefaultFetchSize(int var1) throws SQLException;

    public void setDetectServerPreparedStmts(boolean var1);

    public void setDontTrackOpenResources(boolean var1);

    public void setDumpQueriesOnException(boolean var1);

    public void setDynamicCalendars(boolean var1);

    public void setElideSetAutoCommits(boolean var1);

    public void setEmptyStringsConvertToZero(boolean var1);

    public void setEmulateLocators(boolean var1);

    public void setEmulateUnsupportedPstmts(boolean var1);

    public void setEnablePacketDebug(boolean var1);

    public void setEncoding(String var1);

    public void setExplainSlowQueries(boolean var1);

    public void setFailOverReadOnly(boolean var1);

    public void setGatherPerformanceMetrics(boolean var1);

    public void setHoldResultsOpenOverStatementClose(boolean var1);

    public void setIgnoreNonTxTables(boolean var1);

    public void setInitialTimeout(int var1) throws SQLException;

    public void setIsInteractiveClient(boolean var1);

    public void setJdbcCompliantTruncation(boolean var1);

    public void setLocatorFetchBufferSize(String var1) throws SQLException;

    public void setLogger(String var1);

    public void setLoggerClassName(String var1);

    public void setLogSlowQueries(boolean var1);

    public void setMaintainTimeStats(boolean var1);

    public void setMaxQuerySizeToLog(int var1) throws SQLException;

    public void setMaxReconnects(int var1) throws SQLException;

    public void setMaxRows(int var1) throws SQLException;

    public void setMetadataCacheSize(int var1) throws SQLException;

    public void setNoDatetimeStringSync(boolean var1);

    public void setNullCatalogMeansCurrent(boolean var1);

    public void setNullNamePatternMatchesAll(boolean var1);

    public void setPacketDebugBufferSize(int var1) throws SQLException;

    public void setParanoid(boolean var1);

    public void setPedantic(boolean var1);

    public void setPreparedStatementCacheSize(int var1) throws SQLException;

    public void setPreparedStatementCacheSqlLimit(int var1) throws SQLException;

    public void setProfileSql(boolean var1);

    public void setProfileSQL(boolean var1);

    public void setPropertiesTransform(String var1);

    public void setQueriesBeforeRetryMaster(int var1) throws SQLException;

    public void setReconnectAtTxEnd(boolean var1);

    public void setRelaxAutoCommit(boolean var1);

    public void setReportMetricsIntervalMillis(int var1) throws SQLException;

    public void setRequireSSL(boolean var1);

    public void setRetainStatementAfterResultSetClose(boolean var1);

    public void setRollbackOnPooledClose(boolean var1);

    public void setRoundRobinLoadBalance(boolean var1);

    public void setRunningCTS13(boolean var1);

    public void setSecondsBeforeRetryMaster(int var1) throws SQLException;

    public void setServerTimezone(String var1);

    public void setSessionVariables(String var1);

    public void setSlowQueryThresholdMillis(int var1) throws SQLException;

    public void setSocketFactoryClassName(String var1);

    public void setSocketTimeout(int var1) throws SQLException;

    public void setStrictFloatingPoint(boolean var1);

    public void setStrictUpdates(boolean var1);

    public void setTinyInt1isBit(boolean var1);

    public void setTraceProtocol(boolean var1);

    public void setTransformedBitIsBoolean(boolean var1);

    public void setUseCompression(boolean var1);

    public void setUseFastIntParsing(boolean var1);

    public void setUseHostsInPrivileges(boolean var1);

    public void setUseInformationSchema(boolean var1);

    public void setUseLocalSessionState(boolean var1);

    public void setUseOldUTF8Behavior(boolean var1);

    public void setUseOnlyServerErrorMessages(boolean var1);

    public void setUseReadAheadInput(boolean var1);

    public void setUseServerPreparedStmts(boolean var1);

    public void setUseSqlStateCodes(boolean var1);

    public void setUseSSL(boolean var1);

    public void setUseStreamLengthsInPrepStmts(boolean var1);

    public void setUseTimezone(boolean var1);

    public void setUseUltraDevWorkAround(boolean var1);

    public void setUseUnbufferedInput(boolean var1);

    public void setUseUnicode(boolean var1);

    public void setUseUsageAdvisor(boolean var1);

    public void setYearIsDateType(boolean var1);

    public void setZeroDateTimeBehavior(String var1);

    public boolean useUnbufferedInput();

    public boolean getUseCursorFetch();

    public void setUseCursorFetch(boolean var1);

    public boolean getOverrideSupportsIntegrityEnhancementFacility();

    public void setOverrideSupportsIntegrityEnhancementFacility(boolean var1);

    public boolean getNoTimezoneConversionForTimeType();

    public void setNoTimezoneConversionForTimeType(boolean var1);

    public boolean getNoTimezoneConversionForDateType();

    public void setNoTimezoneConversionForDateType(boolean var1);

    public boolean getCacheDefaultTimezone();

    public void setCacheDefaultTimezone(boolean var1);

    public boolean getUseJDBCCompliantTimezoneShift();

    public void setUseJDBCCompliantTimezoneShift(boolean var1);

    public boolean getAutoClosePStmtStreams();

    public void setAutoClosePStmtStreams(boolean var1);

    public boolean getProcessEscapeCodesForPrepStmts();

    public void setProcessEscapeCodesForPrepStmts(boolean var1);

    public boolean getUseGmtMillisForDatetimes();

    public void setUseGmtMillisForDatetimes(boolean var1);

    public boolean getDumpMetadataOnColumnNotFound();

    public void setDumpMetadataOnColumnNotFound(boolean var1);

    public String getResourceId();

    public void setResourceId(String var1);

    public boolean getRewriteBatchedStatements();

    public void setRewriteBatchedStatements(boolean var1);

    public boolean getJdbcCompliantTruncationForReads();

    public void setJdbcCompliantTruncationForReads(boolean var1);

    public boolean getUseJvmCharsetConverters();

    public void setUseJvmCharsetConverters(boolean var1);

    public boolean getPinGlobalTxToPhysicalConnection();

    public void setPinGlobalTxToPhysicalConnection(boolean var1);

    public void setGatherPerfMetrics(boolean var1);

    public boolean getGatherPerfMetrics();

    public void setUltraDevHack(boolean var1);

    public boolean getUltraDevHack();

    public void setInteractiveClient(boolean var1);

    public void setSocketFactory(String var1);

    public String getSocketFactory();

    public void setUseServerPrepStmts(boolean var1);

    public boolean getUseServerPrepStmts();

    public void setCacheCallableStmts(boolean var1);

    public boolean getCacheCallableStmts();

    public void setCachePrepStmts(boolean var1);

    public boolean getCachePrepStmts();

    public void setCallableStmtCacheSize(int var1) throws SQLException;

    public int getCallableStmtCacheSize();

    public void setPrepStmtCacheSize(int var1) throws SQLException;

    public int getPrepStmtCacheSize();

    public void setPrepStmtCacheSqlLimit(int var1) throws SQLException;

    public int getPrepStmtCacheSqlLimit();

    public boolean getNoAccessToProcedureBodies();

    public void setNoAccessToProcedureBodies(boolean var1);

    public boolean getUseOldAliasMetadataBehavior();

    public void setUseOldAliasMetadataBehavior(boolean var1);

    public String getClientCertificateKeyStorePassword();

    public void setClientCertificateKeyStorePassword(String var1);

    public String getClientCertificateKeyStoreType();

    public void setClientCertificateKeyStoreType(String var1);

    public String getClientCertificateKeyStoreUrl();

    public void setClientCertificateKeyStoreUrl(String var1);

    public String getTrustCertificateKeyStorePassword();

    public void setTrustCertificateKeyStorePassword(String var1);

    public String getTrustCertificateKeyStoreType();

    public void setTrustCertificateKeyStoreType(String var1);

    public String getTrustCertificateKeyStoreUrl();

    public void setTrustCertificateKeyStoreUrl(String var1);

    public boolean getUseSSPSCompatibleTimezoneShift();

    public void setUseSSPSCompatibleTimezoneShift(boolean var1);

    public boolean getTreatUtilDateAsTimestamp();

    public void setTreatUtilDateAsTimestamp(boolean var1);

    public boolean getUseFastDateParsing();

    public void setUseFastDateParsing(boolean var1);

    public String getLocalSocketAddress();

    public void setLocalSocketAddress(String var1);

    public void setUseConfigs(String var1);

    public String getUseConfigs();

    public boolean getGenerateSimpleParameterMetadata();

    public void setGenerateSimpleParameterMetadata(boolean var1);

    public boolean getLogXaCommands();

    public void setLogXaCommands(boolean var1);

    public int getResultSetSizeThreshold();

    public void setResultSetSizeThreshold(int var1) throws SQLException;

    public int getNetTimeoutForStreamingResults();

    public void setNetTimeoutForStreamingResults(int var1) throws SQLException;

    public boolean getEnableQueryTimeouts();

    public void setEnableQueryTimeouts(boolean var1);

    public boolean getPadCharsWithSpace();

    public void setPadCharsWithSpace(boolean var1);

    public boolean getUseDynamicCharsetInfo();

    public void setUseDynamicCharsetInfo(boolean var1);

    public String getClientInfoProvider();

    public void setClientInfoProvider(String var1);

    public boolean getPopulateInsertRowWithDefaultValues();

    public void setPopulateInsertRowWithDefaultValues(boolean var1);

    public String getLoadBalanceStrategy();

    public void setLoadBalanceStrategy(String var1);

    public boolean getTcpNoDelay();

    public void setTcpNoDelay(boolean var1);

    public boolean getTcpKeepAlive();

    public void setTcpKeepAlive(boolean var1);

    public int getTcpRcvBuf();

    public void setTcpRcvBuf(int var1) throws SQLException;

    public int getTcpSndBuf();

    public void setTcpSndBuf(int var1) throws SQLException;

    public int getTcpTrafficClass();

    public void setTcpTrafficClass(int var1) throws SQLException;

    public boolean getUseNanosForElapsedTime();

    public void setUseNanosForElapsedTime(boolean var1);

    public long getSlowQueryThresholdNanos();

    public void setSlowQueryThresholdNanos(long var1) throws SQLException;

    public String getStatementInterceptors();

    public void setStatementInterceptors(String var1);

    public boolean getUseDirectRowUnpack();

    public void setUseDirectRowUnpack(boolean var1);

    public String getLargeRowSizeThreshold();

    public void setLargeRowSizeThreshold(String var1) throws SQLException;

    public boolean getUseBlobToStoreUTF8OutsideBMP();

    public void setUseBlobToStoreUTF8OutsideBMP(boolean var1);

    public String getUtf8OutsideBmpExcludedColumnNamePattern();

    public void setUtf8OutsideBmpExcludedColumnNamePattern(String var1);

    public String getUtf8OutsideBmpIncludedColumnNamePattern();

    public void setUtf8OutsideBmpIncludedColumnNamePattern(String var1);

    public boolean getIncludeInnodbStatusInDeadlockExceptions();

    public void setIncludeInnodbStatusInDeadlockExceptions(boolean var1);

    public boolean getIncludeThreadDumpInDeadlockExceptions();

    public void setIncludeThreadDumpInDeadlockExceptions(boolean var1);

    public boolean getIncludeThreadNamesAsStatementComment();

    public void setIncludeThreadNamesAsStatementComment(boolean var1);

    public boolean getBlobsAreStrings();

    public void setBlobsAreStrings(boolean var1);

    public boolean getFunctionsNeverReturnBlobs();

    public void setFunctionsNeverReturnBlobs(boolean var1);

    public boolean getAutoSlowLog();

    public void setAutoSlowLog(boolean var1);

    public String getConnectionLifecycleInterceptors();

    public void setConnectionLifecycleInterceptors(String var1);

    public String getProfilerEventHandler();

    public void setProfilerEventHandler(String var1);

    public boolean getVerifyServerCertificate();

    public void setVerifyServerCertificate(boolean var1);

    public boolean getUseLegacyDatetimeCode();

    public void setUseLegacyDatetimeCode(boolean var1);

    public boolean getSendFractionalSeconds();

    public void setSendFractionalSeconds(boolean var1);

    public int getSelfDestructOnPingSecondsLifetime();

    public void setSelfDestructOnPingSecondsLifetime(int var1) throws SQLException;

    public int getSelfDestructOnPingMaxOperations();

    public void setSelfDestructOnPingMaxOperations(int var1) throws SQLException;

    public boolean getUseColumnNamesInFindColumn();

    public void setUseColumnNamesInFindColumn(boolean var1);

    public boolean getUseLocalTransactionState();

    public void setUseLocalTransactionState(boolean var1);

    public boolean getCompensateOnDuplicateKeyUpdateCounts();

    public void setCompensateOnDuplicateKeyUpdateCounts(boolean var1);

    public void setUseAffectedRows(boolean var1);

    public boolean getUseAffectedRows();

    public void setPasswordCharacterEncoding(String var1);

    public String getPasswordCharacterEncoding();

    public int getLoadBalanceBlacklistTimeout();

    public void setLoadBalanceBlacklistTimeout(int var1) throws SQLException;

    public void setRetriesAllDown(int var1) throws SQLException;

    public int getRetriesAllDown();

    public ExceptionInterceptor getExceptionInterceptor();

    public void setExceptionInterceptors(String var1);

    public String getExceptionInterceptors();

    public boolean getQueryTimeoutKillsConnection();

    public void setQueryTimeoutKillsConnection(boolean var1);

    public int getMaxAllowedPacket();

    public boolean getRetainStatementAfterResultSetClose();

    public int getLoadBalancePingTimeout();

    public void setLoadBalancePingTimeout(int var1) throws SQLException;

    public boolean getLoadBalanceValidateConnectionOnSwapServer();

    public void setLoadBalanceValidateConnectionOnSwapServer(boolean var1);

    public String getLoadBalanceConnectionGroup();

    public void setLoadBalanceConnectionGroup(String var1);

    public String getLoadBalanceExceptionChecker();

    public void setLoadBalanceExceptionChecker(String var1);

    public String getLoadBalanceSQLStateFailover();

    public void setLoadBalanceSQLStateFailover(String var1);

    public String getLoadBalanceSQLExceptionSubclassFailover();

    public void setLoadBalanceSQLExceptionSubclassFailover(String var1);

    public boolean getLoadBalanceEnableJMX();

    public void setLoadBalanceEnableJMX(boolean var1);

    public void setLoadBalanceHostRemovalGracePeriod(int var1) throws SQLException;

    public int getLoadBalanceHostRemovalGracePeriod();

    public void setLoadBalanceAutoCommitStatementThreshold(int var1) throws SQLException;

    public int getLoadBalanceAutoCommitStatementThreshold();

    public void setLoadBalanceAutoCommitStatementRegex(String var1);

    public String getLoadBalanceAutoCommitStatementRegex();

    public void setAuthenticationPlugins(String var1);

    public String getAuthenticationPlugins();

    public void setDisabledAuthenticationPlugins(String var1);

    public String getDisabledAuthenticationPlugins();

    public void setDefaultAuthenticationPlugin(String var1);

    public String getDefaultAuthenticationPlugin();

    public void setParseInfoCacheFactory(String var1);

    public String getParseInfoCacheFactory();

    public void setServerConfigCacheFactory(String var1);

    public String getServerConfigCacheFactory();

    public void setDisconnectOnExpiredPasswords(boolean var1);

    public boolean getDisconnectOnExpiredPasswords();

    public boolean getAllowMasterDownConnections();

    public void setAllowMasterDownConnections(boolean var1);

    public boolean getAllowSlaveDownConnections();

    public void setAllowSlaveDownConnections(boolean var1);

    public boolean getReadFromMasterWhenNoSlaves();

    public void setReadFromMasterWhenNoSlaves(boolean var1);

    public boolean getReplicationEnableJMX();

    public void setReplicationEnableJMX(boolean var1);

    public void setGetProceduresReturnsFunctions(boolean var1);

    public boolean getGetProceduresReturnsFunctions();

    public void setDetectCustomCollations(boolean var1);

    public boolean getDetectCustomCollations();

    public String getConnectionAttributes() throws SQLException;

    public String getServerRSAPublicKeyFile();

    public void setServerRSAPublicKeyFile(String var1) throws SQLException;

    public boolean getAllowPublicKeyRetrieval();

    public void setAllowPublicKeyRetrieval(boolean var1) throws SQLException;

    public void setDontCheckOnDuplicateKeyUpdateInSQL(boolean var1);

    public boolean getDontCheckOnDuplicateKeyUpdateInSQL();

    public void setSocksProxyHost(String var1);

    public String getSocksProxyHost();

    public void setSocksProxyPort(int var1) throws SQLException;

    public int getSocksProxyPort();

    public boolean getReadOnlyPropagatesToServer();

    public void setReadOnlyPropagatesToServer(boolean var1);

    public String getEnabledSSLCipherSuites();

    public void setEnabledSSLCipherSuites(String var1);

    public boolean getEnableEscapeProcessing();

    public void setEnableEscapeProcessing(boolean var1);
}


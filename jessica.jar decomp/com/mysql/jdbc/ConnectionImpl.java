/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Buffer;
import com.mysql.jdbc.CacheAdapter;
import com.mysql.jdbc.CacheAdapterFactory;
import com.mysql.jdbc.CachedResultSetMetaData;
import com.mysql.jdbc.CallableStatement;
import com.mysql.jdbc.CharsetMapping;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ConnectionLifecycleInterceptor;
import com.mysql.jdbc.ConnectionPropertiesImpl;
import com.mysql.jdbc.Constants;
import com.mysql.jdbc.DatabaseMetaData;
import com.mysql.jdbc.EscapeProcessor;
import com.mysql.jdbc.EscapeProcessorResult;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.Extension;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.IterateBlock;
import com.mysql.jdbc.LicenseConfiguration;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MultiHostMySQLConnection;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.MysqlIO;
import com.mysql.jdbc.MysqlSavepoint;
import com.mysql.jdbc.NamedPipeSocketFactory;
import com.mysql.jdbc.NoSubInterceptorWrapper;
import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ProfilerEventHandlerFactory;
import com.mysql.jdbc.ReflectiveStatementInterceptorAdapter;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.ServerPreparedStatement;
import com.mysql.jdbc.SingleByteCharsetConverter;
import com.mysql.jdbc.SocketFactory;
import com.mysql.jdbc.SocketMetadata;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StatementImpl;
import com.mysql.jdbc.StatementInterceptor;
import com.mysql.jdbc.StatementInterceptorV2;
import com.mysql.jdbc.StringUtils;
import com.mysql.jdbc.TimeUtil;
import com.mysql.jdbc.UpdatableResultSet;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.V1toV2StatementInterceptorAdapter;
import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.log.LogFactory;
import com.mysql.jdbc.log.LogUtils;
import com.mysql.jdbc.log.NullLogger;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import com.mysql.jdbc.util.LRUCache;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLPermission;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Stack;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ConnectionImpl
extends ConnectionPropertiesImpl
implements MySQLConnection {
    private static final long serialVersionUID = 2877471301981509474L;
    private static final SQLPermission SET_NETWORK_TIMEOUT_PERM = new SQLPermission("setNetworkTimeout");
    private static final SQLPermission ABORT_PERM = new SQLPermission("abort");
    public static final String JDBC_LOCAL_CHARACTER_SET_RESULTS = "jdbc.local.character_set_results";
    private MySQLConnection proxy = null;
    private InvocationHandler realProxy = null;
    private static final Object CHARSET_CONVERTER_NOT_AVAILABLE_MARKER = new Object();
    public static Map<?, ?> charsetMap;
    protected static final String DEFAULT_LOGGER_CLASS = "com.mysql.jdbc.log.StandardLogger";
    private static final int HISTOGRAM_BUCKETS = 20;
    private static final String LOGGER_INSTANCE_NAME = "MySQL";
    private static Map<String, Integer> mapTransIsolationNameToValue;
    private static final Log NULL_LOGGER;
    protected static Map<?, ?> roundRobinStatsMap;
    private static final Map<String, Map<Number, String>> dynamicIndexToCollationMapByUrl;
    private static final Map<String, Map<Integer, String>> dynamicIndexToCharsetMapByUrl;
    private static final Map<String, Map<Integer, String>> customIndexToCharsetMapByUrl;
    private static final Map<String, Map<String, Integer>> customCharsetToMblenMapByUrl;
    private CacheAdapter<String, Map<String, String>> serverConfigCache;
    private long queryTimeCount;
    private double queryTimeSum;
    private double queryTimeSumSquares;
    private double queryTimeMean;
    private transient Timer cancelTimer;
    private List<Extension> connectionLifecycleInterceptors;
    private static final Constructor<?> JDBC_4_CONNECTION_CTOR;
    private static final int DEFAULT_RESULT_SET_TYPE = 1003;
    private static final int DEFAULT_RESULT_SET_CONCURRENCY = 1007;
    private static final Random random;
    private boolean autoCommit = true;
    private CacheAdapter<String, PreparedStatement.ParseInfo> cachedPreparedStatementParams;
    private String characterSetMetadata = null;
    private String characterSetResultsOnServer = null;
    private Map<String, Object> charsetConverterMap = new HashMap<String, Object>(CharsetMapping.getNumberOfCharsetsConfigured());
    private long connectionCreationTimeMillis = 0L;
    private long connectionId;
    private String database = null;
    private java.sql.DatabaseMetaData dbmd = null;
    private TimeZone defaultTimeZone;
    private ProfilerEventHandler eventSink;
    private Throwable forceClosedReason;
    private boolean hasIsolationLevels = false;
    private boolean hasQuotedIdentifiers = false;
    private String host = null;
    public Map<Integer, String> indexToMysqlCharset = new HashMap<Integer, String>();
    public Map<Integer, String> indexToCustomMysqlCharset = null;
    private Map<String, Integer> mysqlCharsetToCustomMblen = null;
    private transient MysqlIO io = null;
    private boolean isClientTzUTC = false;
    private boolean isClosed = true;
    private boolean isInGlobalTx = false;
    private boolean isRunningOnJDK13 = false;
    private int isolationLevel = 2;
    private boolean isServerTzUTC = false;
    private long lastQueryFinishedTime = 0L;
    private transient Log log = NULL_LOGGER;
    private long longestQueryTimeMs = 0L;
    private boolean lowerCaseTableNames = false;
    private long maximumNumberTablesAccessed = 0L;
    private int sessionMaxRows = -1;
    private long metricsLastReportedMs;
    private long minimumNumberTablesAccessed = Long.MAX_VALUE;
    private String myURL = null;
    private boolean needsPing = false;
    private int netBufferLength = 16384;
    private boolean noBackslashEscapes = false;
    private long numberOfPreparedExecutes = 0L;
    private long numberOfPrepares = 0L;
    private long numberOfQueriesIssued = 0L;
    private long numberOfResultSetsCreated = 0L;
    private long[] numTablesMetricsHistBreakpoints;
    private int[] numTablesMetricsHistCounts;
    private long[] oldHistBreakpoints = null;
    private int[] oldHistCounts = null;
    private final CopyOnWriteArrayList<Statement> openStatements = new CopyOnWriteArrayList();
    private LRUCache parsedCallableStatementCache;
    private boolean parserKnowsUnicode = false;
    private String password = null;
    private long[] perfMetricsHistBreakpoints;
    private int[] perfMetricsHistCounts;
    private String pointOfOrigin;
    private int port = 3306;
    protected Properties props = null;
    private boolean readInfoMsg = false;
    private boolean readOnly = false;
    protected LRUCache resultSetMetadataCache;
    private TimeZone serverTimezoneTZ = null;
    private Map<String, String> serverVariables = null;
    private long shortestQueryTimeMs = Long.MAX_VALUE;
    private double totalQueryTimeMs = 0.0;
    private boolean transactionsSupported = false;
    private Map<String, Class<?>> typeMap;
    private boolean useAnsiQuotes = false;
    private String user = null;
    private boolean useServerPreparedStmts = false;
    private LRUCache serverSideStatementCheckCache;
    private LRUCache serverSideStatementCache;
    private Calendar sessionCalendar;
    private Calendar utcCalendar;
    private String origHostToConnectTo;
    private int origPortToConnectTo;
    private String origDatabaseToConnectTo;
    private String errorMessageEncoding = "Cp1252";
    private boolean usePlatformCharsetConverters;
    private boolean hasTriedMasterFlag = false;
    private String statementComment = null;
    private boolean storesLowerCaseTableName;
    private List<StatementInterceptorV2> statementInterceptors;
    private boolean requiresEscapingEncoder;
    private String hostPortPair;
    private static final String SERVER_VERSION_STRING_VAR_NAME = "server_version_string";
    private int autoIncrementIncrement = 0;
    private ExceptionInterceptor exceptionInterceptor;

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public String getHostPortPair() {
        return this.hostPortPair != null ? this.hostPortPair : this.host + ":" + this.port;
    }

    @Override
    public boolean isProxySet() {
        return this.proxy != null;
    }

    @Override
    public void setProxy(MySQLConnection proxy) {
        this.proxy = proxy;
        this.realProxy = this.proxy instanceof MultiHostMySQLConnection ? ((MultiHostMySQLConnection)proxy).getThisAsProxy() : null;
    }

    private MySQLConnection getProxy() {
        return this.proxy != null ? this.proxy : this;
    }

    @Override
    @Deprecated
    public MySQLConnection getLoadBalanceSafeProxy() {
        return this.getMultiHostSafeProxy();
    }

    @Override
    public MySQLConnection getMultiHostSafeProxy() {
        return this.getProxy();
    }

    @Override
    public Object getConnectionMutex() {
        return this.realProxy != null ? this.realProxy : this.getProxy();
    }

    protected static SQLException appendMessageToException(SQLException sqlEx, String messageToAppend, ExceptionInterceptor interceptor) {
        String origMessage = sqlEx.getMessage();
        String sqlState = sqlEx.getSQLState();
        int vendorErrorCode = sqlEx.getErrorCode();
        StringBuilder messageBuf = new StringBuilder(origMessage.length() + messageToAppend.length());
        messageBuf.append(origMessage);
        messageBuf.append(messageToAppend);
        SQLException sqlExceptionWithNewMessage = SQLError.createSQLException(messageBuf.toString(), sqlState, vendorErrorCode, interceptor);
        try {
            Method getStackTraceMethod = null;
            Method setStackTraceMethod = null;
            Object theStackTraceAsObject = null;
            Class<?> stackTraceElementClass = Class.forName("java.lang.StackTraceElement");
            Class<?> stackTraceElementArrayClass = Array.newInstance(stackTraceElementClass, new int[]{0}).getClass();
            getStackTraceMethod = Throwable.class.getMethod("getStackTrace", new Class[0]);
            setStackTraceMethod = Throwable.class.getMethod("setStackTrace", stackTraceElementArrayClass);
            if (getStackTraceMethod != null && setStackTraceMethod != null) {
                theStackTraceAsObject = getStackTraceMethod.invoke(sqlEx, new Object[0]);
                setStackTraceMethod.invoke(sqlExceptionWithNewMessage, theStackTraceAsObject);
            }
        }
        catch (NoClassDefFoundError noClassDefFound) {
        }
        catch (NoSuchMethodException noSuchMethodEx) {
        }
        catch (Throwable catchAll) {
            // empty catch block
        }
        return sqlExceptionWithNewMessage;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Timer getCancelTimer() {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            if (this.cancelTimer == null) {
                boolean createdNamedTimer = false;
                try {
                    Constructor ctr = Timer.class.getConstructor(String.class, Boolean.TYPE);
                    this.cancelTimer = (Timer)ctr.newInstance("MySQL Statement Cancellation Timer", Boolean.TRUE);
                    createdNamedTimer = true;
                }
                catch (Throwable t) {
                    createdNamedTimer = false;
                }
                if (!createdNamedTimer) {
                    this.cancelTimer = new Timer(true);
                }
            }
            return this.cancelTimer;
        }
    }

    protected static Connection getInstance(String hostToConnectTo, int portToConnectTo, Properties info, String databaseToConnectTo, String url) throws SQLException {
        if (!Util.isJdbc4()) {
            return new ConnectionImpl(hostToConnectTo, portToConnectTo, info, databaseToConnectTo, url);
        }
        return (Connection)Util.handleNewInstance(JDBC_4_CONNECTION_CTOR, new Object[]{hostToConnectTo, portToConnectTo, info, databaseToConnectTo, url}, null);
    }

    protected static synchronized int getNextRoundRobinHostIndex(String url, List<?> hostList) {
        int indexRange = hostList.size();
        int index = random.nextInt(indexRange);
        return index;
    }

    private static boolean nullSafeCompare(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null && s2 != null) {
            return false;
        }
        return s1 != null && s1.equals(s2);
    }

    protected ConnectionImpl() {
    }

    public ConnectionImpl(String hostToConnectTo, int portToConnectTo, Properties info, String databaseToConnectTo, String url) throws SQLException {
        this.connectionCreationTimeMillis = System.currentTimeMillis();
        if (databaseToConnectTo == null) {
            databaseToConnectTo = "";
        }
        this.origHostToConnectTo = hostToConnectTo;
        this.origPortToConnectTo = portToConnectTo;
        this.origDatabaseToConnectTo = databaseToConnectTo;
        try {
            Blob.class.getMethod("truncate", Long.TYPE);
            this.isRunningOnJDK13 = false;
        }
        catch (NoSuchMethodException nsme) {
            this.isRunningOnJDK13 = true;
        }
        this.sessionCalendar = new GregorianCalendar();
        this.utcCalendar = new GregorianCalendar();
        this.utcCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.log = LogFactory.getLogger(this.getLogger(), LOGGER_INSTANCE_NAME, this.getExceptionInterceptor());
        if (NonRegisteringDriver.isHostPropertiesList(hostToConnectTo)) {
            Properties hostSpecificProps = NonRegisteringDriver.expandHostKeyValues(hostToConnectTo);
            Enumeration<?> propertyNames = hostSpecificProps.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = propertyNames.nextElement().toString();
                String propertyValue = hostSpecificProps.getProperty(propertyName);
                info.setProperty(propertyName, propertyValue);
            }
        } else if (hostToConnectTo == null) {
            this.host = "localhost";
            this.hostPortPair = this.host + ":" + portToConnectTo;
        } else {
            this.host = hostToConnectTo;
            this.hostPortPair = hostToConnectTo.indexOf(":") == -1 ? this.host + ":" + portToConnectTo : this.host;
        }
        this.port = portToConnectTo;
        this.database = databaseToConnectTo;
        this.myURL = url;
        this.user = info.getProperty("user");
        this.password = info.getProperty("password");
        if (this.user == null || this.user.equals("")) {
            this.user = "";
        }
        if (this.password == null) {
            this.password = "";
        }
        this.props = info;
        this.initializeDriverProperties(info);
        this.defaultTimeZone = TimeUtil.getDefaultTimeZone(this.getCacheDefaultTimezone());
        this.isClientTzUTC = !this.defaultTimeZone.useDaylightTime() && this.defaultTimeZone.getRawOffset() == 0;
        this.pointOfOrigin = this.getUseUsageAdvisor() ? LogUtils.findCallingClassAndMethod(new Throwable()) : "";
        try {
            this.dbmd = this.getMetaData(false, false);
            this.initializeSafeStatementInterceptors();
            this.createNewIO(false);
            this.unSafeStatementInterceptors();
        }
        catch (SQLException ex) {
            this.cleanup(ex);
            throw ex;
        }
        catch (Exception ex) {
            this.cleanup(ex);
            StringBuilder mesg = new StringBuilder(128);
            if (!this.getParanoid()) {
                mesg.append("Cannot connect to MySQL server on ");
                mesg.append(this.host);
                mesg.append(":");
                mesg.append(this.port);
                mesg.append(".\n\n");
                mesg.append("Make sure that there is a MySQL server ");
                mesg.append("running on the machine/port you are trying ");
                mesg.append("to connect to and that the machine this software is running on ");
                mesg.append("is able to connect to this host/port (i.e. not firewalled). ");
                mesg.append("Also make sure that the server has not been started with the --skip-networking ");
                mesg.append("flag.\n\n");
            } else {
                mesg.append("Unable to connect to database.");
            }
            SQLException sqlEx = SQLError.createSQLException(mesg.toString(), "08S01", this.getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
        NonRegisteringDriver.trackConnection(this);
    }

    @Override
    public void unSafeStatementInterceptors() throws SQLException {
        ArrayList<StatementInterceptorV2> unSafedStatementInterceptors = new ArrayList<StatementInterceptorV2>(this.statementInterceptors.size());
        for (int i = 0; i < this.statementInterceptors.size(); ++i) {
            NoSubInterceptorWrapper wrappedInterceptor = (NoSubInterceptorWrapper)this.statementInterceptors.get(i);
            unSafedStatementInterceptors.add(wrappedInterceptor.getUnderlyingInterceptor());
        }
        this.statementInterceptors = unSafedStatementInterceptors;
        if (this.io != null) {
            this.io.setStatementInterceptors(this.statementInterceptors);
        }
    }

    @Override
    public void initializeSafeStatementInterceptors() throws SQLException {
        this.isClosed = false;
        List<Extension> unwrappedInterceptors = Util.loadExtensions(this, this.props, this.getStatementInterceptors(), "MysqlIo.BadStatementInterceptor", this.getExceptionInterceptor());
        this.statementInterceptors = new ArrayList<StatementInterceptorV2>(unwrappedInterceptors.size());
        for (int i = 0; i < unwrappedInterceptors.size(); ++i) {
            Extension interceptor = unwrappedInterceptors.get(i);
            if (interceptor instanceof StatementInterceptor) {
                if (ReflectiveStatementInterceptorAdapter.getV2PostProcessMethod(interceptor.getClass()) != null) {
                    this.statementInterceptors.add(new NoSubInterceptorWrapper(new ReflectiveStatementInterceptorAdapter((StatementInterceptor)interceptor)));
                    continue;
                }
                this.statementInterceptors.add(new NoSubInterceptorWrapper(new V1toV2StatementInterceptorAdapter((StatementInterceptor)interceptor)));
                continue;
            }
            this.statementInterceptors.add(new NoSubInterceptorWrapper((StatementInterceptorV2)interceptor));
        }
    }

    @Override
    public List<StatementInterceptorV2> getStatementInterceptorsInstances() {
        return this.statementInterceptors;
    }

    private void addToHistogram(int[] histogramCounts, long[] histogramBreakpoints, long value, int numberOfTimes, long currentLowerBound, long currentUpperBound) {
        if (histogramCounts == null) {
            this.createInitialHistogram(histogramBreakpoints, currentLowerBound, currentUpperBound);
        } else {
            for (int i = 0; i < 20; ++i) {
                if (histogramBreakpoints[i] < value) continue;
                int n = i;
                histogramCounts[n] = histogramCounts[n] + numberOfTimes;
                break;
            }
        }
    }

    private void addToPerformanceHistogram(long value, int numberOfTimes) {
        this.checkAndCreatePerformanceHistogram();
        this.addToHistogram(this.perfMetricsHistCounts, this.perfMetricsHistBreakpoints, value, numberOfTimes, this.shortestQueryTimeMs == Long.MAX_VALUE ? 0L : this.shortestQueryTimeMs, this.longestQueryTimeMs);
    }

    private void addToTablesAccessedHistogram(long value, int numberOfTimes) {
        this.checkAndCreateTablesAccessedHistogram();
        this.addToHistogram(this.numTablesMetricsHistCounts, this.numTablesMetricsHistBreakpoints, value, numberOfTimes, this.minimumNumberTablesAccessed == Long.MAX_VALUE ? 0L : this.minimumNumberTablesAccessed, this.maximumNumberTablesAccessed);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    private void buildCollationMapping() throws SQLException {
        Map<String, Integer> customMblen;
        Map<Integer, String> customCharset;
        Map<Object, Object> indexToCharset;
        block43: {
            block44: {
                SQLException sqlE22;
                ResultSet results;
                java.sql.Statement stmt;
                block42: {
                    Map<Number, String> sortedCollationMap;
                    block41: {
                        block40: {
                            indexToCharset = null;
                            sortedCollationMap = null;
                            customCharset = null;
                            customMblen = null;
                            if (this.getCacheServerConfiguration()) {
                                Map<String, Map<Integer, String>> map = dynamicIndexToCharsetMapByUrl;
                                synchronized (map) {
                                    indexToCharset = dynamicIndexToCharsetMapByUrl.get(this.getURL());
                                    sortedCollationMap = dynamicIndexToCollationMapByUrl.get(this.getURL());
                                    customCharset = customIndexToCharsetMapByUrl.get(this.getURL());
                                    customMblen = customCharsetToMblenMapByUrl.get(this.getURL());
                                }
                            }
                            if (indexToCharset != null) break block43;
                            indexToCharset = new HashMap();
                            if (!this.versionMeetsMinimum(4, 1, 0) || !this.getDetectCustomCollations()) break block44;
                            stmt = null;
                            results = null;
                            sortedCollationMap = new TreeMap<Number, String>();
                            customCharset = new HashMap<Integer, String>();
                            customMblen = new HashMap<String, Integer>();
                            stmt = this.getMetadataSafeStatement();
                            try {
                                results = stmt.executeQuery("SHOW COLLATION");
                                if (this.versionMeetsMinimum(5, 0, 0)) {
                                    Util.resultSetToMap(sortedCollationMap, results, 3, 2);
                                } else {
                                    while (results.next()) {
                                        sortedCollationMap.put(results.getLong(3), results.getString(2));
                                    }
                                }
                            }
                            catch (SQLException ex) {
                                if (ex.getErrorCode() == 1820 && !this.getDisconnectOnExpiredPasswords()) break block40;
                                throw ex;
                            }
                        }
                        for (Map.Entry<Number, String> indexEntry : sortedCollationMap.entrySet()) {
                            int collationIndex = indexEntry.getKey().intValue();
                            String charsetName = indexEntry.getValue();
                            indexToCharset.put(collationIndex, charsetName);
                            if (collationIndex >= 255 || !charsetName.equals(CharsetMapping.getMysqlCharsetNameForCollationIndex(collationIndex))) {
                                customCharset.put(collationIndex, charsetName);
                            }
                            if (CharsetMapping.CHARSET_NAME_TO_CHARSET.containsKey(charsetName)) continue;
                            customMblen.put(charsetName, null);
                        }
                        if (customMblen.size() > 0) {
                            try {
                                results = stmt.executeQuery("SHOW CHARACTER SET");
                                while (results.next()) {
                                    String charsetName = results.getString("Charset");
                                    if (!customMblen.containsKey(charsetName)) continue;
                                    customMblen.put(charsetName, results.getInt("Maxlen"));
                                }
                            }
                            catch (SQLException ex) {
                                if (ex.getErrorCode() == 1820 && !this.getDisconnectOnExpiredPasswords()) break block41;
                                throw ex;
                            }
                        }
                    }
                    if (this.getCacheServerConfiguration()) {
                        Map<String, Map<Integer, String>> ex = dynamicIndexToCharsetMapByUrl;
                        synchronized (ex) {
                            dynamicIndexToCharsetMapByUrl.put(this.getURL(), indexToCharset);
                            dynamicIndexToCollationMapByUrl.put(this.getURL(), sortedCollationMap);
                            customIndexToCharsetMapByUrl.put(this.getURL(), customCharset);
                            customCharsetToMblenMapByUrl.put(this.getURL(), customMblen);
                        }
                    }
                    Object var13_20 = null;
                    if (results == null) break block42;
                    try {
                        results.close();
                    }
                    catch (SQLException sqlE22) {
                        // empty catch block
                    }
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    }
                    catch (SQLException sqlE22) {}
                }
                break block43;
                {
                    catch (SQLException ex) {
                        throw ex;
                    }
                    catch (RuntimeException ex) {
                        SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
                        sqlEx.initCause(ex);
                        throw sqlEx;
                    }
                }
                catch (Throwable throwable) {
                    SQLException sqlE22;
                    Object var13_21 = null;
                    if (results != null) {
                        try {
                            results.close();
                        }
                        catch (SQLException sqlE22) {
                            // empty catch block
                        }
                    }
                    if (stmt != null) {
                        try {
                            stmt.close();
                        }
                        catch (SQLException sqlE22) {
                            // empty catch block
                        }
                    }
                    throw throwable;
                }
            }
            for (int i = 1; i < 255; ++i) {
                indexToCharset.put(i, CharsetMapping.getMysqlCharsetNameForCollationIndex(i));
            }
            if (this.getCacheServerConfiguration()) {
                Map<String, Map<Integer, String>> map = dynamicIndexToCharsetMapByUrl;
                synchronized (map) {
                    dynamicIndexToCharsetMapByUrl.put(this.getURL(), indexToCharset);
                }
            }
        }
        this.indexToMysqlCharset = Collections.unmodifiableMap(indexToCharset);
        if (customCharset != null) {
            this.indexToCustomMysqlCharset = Collections.unmodifiableMap(customCharset);
        }
        if (customMblen != null) {
            this.mysqlCharsetToCustomMblen = Collections.unmodifiableMap(customMblen);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean canHandleAsServerPreparedStatement(String sql) throws SQLException {
        if (sql == null || sql.length() == 0) {
            return true;
        }
        if (!this.useServerPreparedStmts) {
            return false;
        }
        if (this.getCachePreparedStatements()) {
            LRUCache lRUCache = this.serverSideStatementCheckCache;
            synchronized (lRUCache) {
                Boolean flag = (Boolean)this.serverSideStatementCheckCache.get(sql);
                if (flag != null) {
                    return flag;
                }
                boolean canHandle = this.canHandleAsServerPreparedStatementNoCache(sql);
                if (sql.length() < this.getPreparedStatementCacheSqlLimit()) {
                    this.serverSideStatementCheckCache.put(sql, canHandle ? Boolean.TRUE : Boolean.FALSE);
                }
                return canHandle;
            }
        }
        return this.canHandleAsServerPreparedStatementNoCache(sql);
    }

    private boolean canHandleAsServerPreparedStatementNoCache(String sql) throws SQLException {
        if (StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "CALL")) {
            return false;
        }
        boolean canHandleAsStatement = true;
        if (!this.versionMeetsMinimum(5, 0, 7) && (StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "SELECT") || StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "DELETE") || StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "INSERT") || StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "UPDATE") || StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "REPLACE"))) {
            int limitStart;
            int currentPos = 0;
            int statementLength = sql.length();
            int lastPosToLook = statementLength - 7;
            boolean allowBackslashEscapes = !this.noBackslashEscapes;
            String quoteChar = this.useAnsiQuotes ? "\"" : "'";
            boolean foundLimitWithPlaceholder = false;
            block0: while (currentPos < lastPosToLook && (limitStart = StringUtils.indexOfIgnoreCase(currentPos, sql, "LIMIT ", quoteChar, quoteChar, allowBackslashEscapes ? StringUtils.SEARCH_MODE__ALL : StringUtils.SEARCH_MODE__MRK_COM_WS)) != -1) {
                char c;
                for (currentPos = limitStart + 7; currentPos < statementLength && (Character.isDigit(c = sql.charAt(currentPos)) || Character.isWhitespace(c) || c == ',' || c == '?'); ++currentPos) {
                    if (c != '?') continue;
                    foundLimitWithPlaceholder = true;
                    continue block0;
                }
            }
            canHandleAsStatement = !foundLimitWithPlaceholder;
        } else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "XA ")) {
            canHandleAsStatement = false;
        } else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "CREATE TABLE")) {
            canHandleAsStatement = false;
        } else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "DO")) {
            canHandleAsStatement = false;
        } else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "SET")) {
            canHandleAsStatement = false;
        } else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "SHOW WARNINGS") && this.versionMeetsMinimum(5, 7, 2)) {
            canHandleAsStatement = false;
        }
        return canHandleAsStatement;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void changeUser(String userName, String newPassword) throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            this.checkClosed();
            if (userName == null || userName.equals("")) {
                userName = "";
            }
            if (newPassword == null) {
                newPassword = "";
            }
            this.sessionMaxRows = -1;
            try {
                this.io.changeUser(userName, newPassword, this.database);
            }
            catch (SQLException ex) {
                if (this.versionMeetsMinimum(5, 6, 13) && "28000".equals(ex.getSQLState())) {
                    this.cleanup(ex);
                }
                throw ex;
            }
            this.user = userName;
            this.password = newPassword;
            if (this.versionMeetsMinimum(4, 1, 0)) {
                this.configureClientCharacterSet(true);
            }
            this.setSessionVariables();
            this.setupServerForTruncationChecks();
        }
    }

    private boolean characterSetNamesMatches(String mysqlEncodingName) {
        return mysqlEncodingName != null && mysqlEncodingName.equalsIgnoreCase(this.serverVariables.get("character_set_client")) && mysqlEncodingName.equalsIgnoreCase(this.serverVariables.get("character_set_connection"));
    }

    private void checkAndCreatePerformanceHistogram() {
        if (this.perfMetricsHistCounts == null) {
            this.perfMetricsHistCounts = new int[20];
        }
        if (this.perfMetricsHistBreakpoints == null) {
            this.perfMetricsHistBreakpoints = new long[20];
        }
    }

    private void checkAndCreateTablesAccessedHistogram() {
        if (this.numTablesMetricsHistCounts == null) {
            this.numTablesMetricsHistCounts = new int[20];
        }
        if (this.numTablesMetricsHistBreakpoints == null) {
            this.numTablesMetricsHistBreakpoints = new long[20];
        }
    }

    @Override
    public void checkClosed() throws SQLException {
        if (this.isClosed) {
            this.throwConnectionClosedException();
        }
    }

    @Override
    public void throwConnectionClosedException() throws SQLException {
        SQLException ex = SQLError.createSQLException("No operations allowed after connection closed.", "08003", this.getExceptionInterceptor());
        if (this.forceClosedReason != null) {
            ex.initCause(this.forceClosedReason);
        }
        throw ex;
    }

    private void checkServerEncoding() throws SQLException {
        SingleByteCharsetConverter converter;
        if (this.getUseUnicode() && this.getEncoding() != null) {
            return;
        }
        String serverCharset = this.serverVariables.get("character_set");
        if (serverCharset == null) {
            serverCharset = this.serverVariables.get("character_set_server");
        }
        String mappedServerEncoding = null;
        if (serverCharset != null) {
            try {
                mappedServerEncoding = CharsetMapping.getJavaEncodingForMysqlCharset(serverCharset);
            }
            catch (RuntimeException ex) {
                SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
                sqlEx.initCause(ex);
                throw sqlEx;
            }
        }
        if (!this.getUseUnicode() && mappedServerEncoding != null && (converter = this.getCharsetConverter(mappedServerEncoding)) != null) {
            this.setUseUnicode(true);
            this.setEncoding(mappedServerEncoding);
            return;
        }
        if (serverCharset != null) {
            if (mappedServerEncoding == null && Character.isLowerCase(serverCharset.charAt(0))) {
                char[] ach = serverCharset.toCharArray();
                ach[0] = Character.toUpperCase(serverCharset.charAt(0));
                this.setEncoding(new String(ach));
            }
            if (mappedServerEncoding == null) {
                throw SQLError.createSQLException("Unknown character encoding on server '" + serverCharset + "', use 'characterEncoding=' property " + " to provide correct mapping", "01S00", this.getExceptionInterceptor());
            }
            try {
                StringUtils.getBytes("abc", mappedServerEncoding);
                this.setEncoding(mappedServerEncoding);
                this.setUseUnicode(true);
            }
            catch (UnsupportedEncodingException UE) {
                throw SQLError.createSQLException("The driver can not map the character encoding '" + this.getEncoding() + "' that your server is using " + "to a character encoding your JVM understands. You can specify this mapping manually by adding \"useUnicode=true\" " + "as well as \"characterEncoding=[an_encoding_your_jvm_understands]\" to your JDBC URL.", "0S100", this.getExceptionInterceptor());
            }
        }
    }

    private void checkTransactionIsolationLevel() throws SQLException {
        Integer intTI;
        String txIsolationName = null;
        txIsolationName = this.versionMeetsMinimum(4, 0, 3) ? "tx_isolation" : "transaction_isolation";
        String s = this.serverVariables.get(txIsolationName);
        if (s != null && (intTI = mapTransIsolationNameToValue.get(s)) != null) {
            this.isolationLevel = intTI;
        }
    }

    @Override
    public void abortInternal() throws SQLException {
        if (this.io != null) {
            try {
                this.io.forceClose();
                this.io.releaseResources();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.io = null;
        }
        this.isClosed = true;
    }

    private void cleanup(Throwable whyCleanedUp) {
        try {
            if (this.io != null) {
                if (this.isClosed()) {
                    this.io.forceClose();
                } else {
                    this.realClose(false, false, false, whyCleanedUp);
                }
            }
        }
        catch (SQLException sQLException) {
            // empty catch block
        }
        this.isClosed = true;
    }

    @Override
    @Deprecated
    public void clearHasTriedMaster() {
        this.hasTriedMasterFlag = false;
    }

    @Override
    public void clearWarnings() throws SQLException {
    }

    @Override
    public java.sql.PreparedStatement clientPrepareStatement(String sql) throws SQLException {
        return this.clientPrepareStatement(sql, 1003, 1007);
    }

    @Override
    public java.sql.PreparedStatement clientPrepareStatement(String sql, int autoGenKeyIndex) throws SQLException {
        java.sql.PreparedStatement pStmt = this.clientPrepareStatement(sql);
        ((PreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyIndex == 1);
        return pStmt;
    }

    @Override
    public java.sql.PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return this.clientPrepareStatement(sql, resultSetType, resultSetConcurrency, true);
    }

    public java.sql.PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, boolean processEscapeCodesIfNeeded) throws SQLException {
        this.checkClosed();
        String nativeSql = processEscapeCodesIfNeeded && this.getProcessEscapeCodesForPrepStmts() ? this.nativeSQL(sql) : sql;
        PreparedStatement pStmt = null;
        if (this.getCachePreparedStatements()) {
            PreparedStatement.ParseInfo pStmtInfo = this.cachedPreparedStatementParams.get(nativeSql);
            if (pStmtInfo == null) {
                pStmt = PreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.database);
                this.cachedPreparedStatementParams.put(nativeSql, pStmt.getParseInfo());
            } else {
                pStmt = PreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.database, pStmtInfo);
            }
        } else {
            pStmt = PreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.database);
        }
        pStmt.setResultSetType(resultSetType);
        pStmt.setResultSetConcurrency(resultSetConcurrency);
        return pStmt;
    }

    @Override
    public java.sql.PreparedStatement clientPrepareStatement(String sql, int[] autoGenKeyIndexes) throws SQLException {
        PreparedStatement pStmt = (PreparedStatement)this.clientPrepareStatement(sql);
        pStmt.setRetrieveGeneratedKeys(autoGenKeyIndexes != null && autoGenKeyIndexes.length > 0);
        return pStmt;
    }

    @Override
    public java.sql.PreparedStatement clientPrepareStatement(String sql, String[] autoGenKeyColNames) throws SQLException {
        PreparedStatement pStmt = (PreparedStatement)this.clientPrepareStatement(sql);
        pStmt.setRetrieveGeneratedKeys(autoGenKeyColNames != null && autoGenKeyColNames.length > 0);
        return pStmt;
    }

    @Override
    public java.sql.PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return this.clientPrepareStatement(sql, resultSetType, resultSetConcurrency, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            if (this.connectionLifecycleInterceptors != null) {
                new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()){

                    @Override
                    void forEach(Extension each) throws SQLException {
                        ((ConnectionLifecycleInterceptor)each).close();
                    }
                }.doForAll();
            }
            this.realClose(true, true, false, null);
        }
    }

    private void closeAllOpenStatements() throws SQLException {
        SQLException postponedException = null;
        for (Statement stmt : this.openStatements) {
            try {
                ((StatementImpl)stmt).realClose(false, true);
            }
            catch (SQLException sqlEx) {
                postponedException = sqlEx;
            }
        }
        if (postponedException != null) {
            throw postponedException;
        }
    }

    private void closeStatement(java.sql.Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            }
            catch (SQLException sQLException) {
                // empty catch block
            }
            stmt = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void commit() throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            block11: {
                block12: {
                    block10: {
                        this.checkClosed();
                        try {
                            try {
                                if (this.connectionLifecycleInterceptors != null) {
                                    IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()){

                                        @Override
                                        void forEach(Extension each) throws SQLException {
                                            if (!((ConnectionLifecycleInterceptor)each).commit()) {
                                                this.stopIterating = true;
                                            }
                                        }
                                    };
                                    iter.doForAll();
                                    if (!iter.fullIteration()) {
                                        Object var4_4 = null;
                                        break block10;
                                    }
                                }
                                if (this.autoCommit && !this.getRelaxAutoCommit()) {
                                    throw SQLError.createSQLException("Can't call commit when autocommit=true", this.getExceptionInterceptor());
                                }
                                if (!this.transactionsSupported) break block11;
                                if (this.getUseLocalTransactionState() && this.versionMeetsMinimum(5, 0, 0) && !this.io.inTransactionOnServer()) {
                                    break block12;
                                }
                                this.execSQL(null, "commit", -1, null, 1003, 1007, false, this.database, null, false);
                                break block11;
                            }
                            catch (SQLException sqlException) {
                                if (!"08S01".equals(sqlException.getSQLState())) throw sqlException;
                                throw SQLError.createSQLException("Communications link failure during commit(). Transaction resolution unknown.", "08007", this.getExceptionInterceptor());
                            }
                        }
                        catch (Throwable throwable) {
                            Object var4_7 = null;
                            this.needsPing = this.getReconnectAtTxEnd();
                            throw throwable;
                        }
                    }
                    this.needsPing = this.getReconnectAtTxEnd();
                    return;
                }
                Object var4_5 = null;
                this.needsPing = this.getReconnectAtTxEnd();
                return;
            }
            Object var4_6 = null;
            this.needsPing = this.getReconnectAtTxEnd();
            return;
        }
    }

    private void configureCharsetProperties() throws SQLException {
        if (this.getEncoding() != null) {
            try {
                String testString = "abc";
                StringUtils.getBytes(testString, this.getEncoding());
            }
            catch (UnsupportedEncodingException UE) {
                String oldEncoding = this.getEncoding();
                try {
                    this.setEncoding(CharsetMapping.getJavaEncodingForMysqlCharset(oldEncoding));
                }
                catch (RuntimeException ex) {
                    SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
                    sqlEx.initCause(ex);
                    throw sqlEx;
                }
                if (this.getEncoding() == null) {
                    throw SQLError.createSQLException("Java does not support the MySQL character encoding '" + oldEncoding + "'.", "01S00", this.getExceptionInterceptor());
                }
                try {
                    String testString = "abc";
                    StringUtils.getBytes(testString, this.getEncoding());
                }
                catch (UnsupportedEncodingException encodingEx) {
                    throw SQLError.createSQLException("Unsupported character encoding '" + this.getEncoding() + "'.", "01S00", this.getExceptionInterceptor());
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    private boolean configureClientCharacterSet(boolean dontCheckServerMatch) throws SQLException {
        realJavaEncoding = this.getEncoding();
        characterSetAlreadyConfigured = false;
        try {
            if (this.versionMeetsMinimum(4, 1, 0)) {
                characterSetAlreadyConfigured = true;
                this.setUseUnicode(true);
                this.configureCharsetProperties();
                realJavaEncoding = this.getEncoding();
                try {
                    if (this.props != null && this.props.getProperty("com.mysql.jdbc.faultInjection.serverCharsetIndex") != null) {
                        this.io.serverCharsetIndex = Integer.parseInt(this.props.getProperty("com.mysql.jdbc.faultInjection.serverCharsetIndex"));
                    }
                    if ((serverEncodingToSet = CharsetMapping.getJavaEncodingForCollationIndex(this.io.serverCharsetIndex)) == null || serverEncodingToSet.length() == 0) {
                        if (realJavaEncoding != null) {
                            this.setEncoding(realJavaEncoding);
                        } else {
                            throw SQLError.createSQLException("Unknown initial character set index '" + this.io.serverCharsetIndex + "' received from server. Initial client character set can be forced via the 'characterEncoding' property.", "S1000", this.getExceptionInterceptor());
                        }
                    }
                    if (this.versionMeetsMinimum(4, 1, 0) && "ISO8859_1".equalsIgnoreCase(serverEncodingToSet)) {
                        serverEncodingToSet = "Cp1252";
                    }
                    if ("UnicodeBig".equalsIgnoreCase(serverEncodingToSet) || "UTF-16".equalsIgnoreCase(serverEncodingToSet) || "UTF-16LE".equalsIgnoreCase(serverEncodingToSet) || "UTF-32".equalsIgnoreCase(serverEncodingToSet)) {
                        serverEncodingToSet = "UTF-8";
                    }
                    this.setEncoding(serverEncodingToSet);
                }
                catch (ArrayIndexOutOfBoundsException outOfBoundsEx) {
                    if (realJavaEncoding != null) {
                        this.setEncoding(realJavaEncoding);
                    }
                    throw SQLError.createSQLException("Unknown initial character set index '" + this.io.serverCharsetIndex + "' received from server. Initial client character set can be forced via the 'characterEncoding' property.", "S1000", this.getExceptionInterceptor());
                }
                catch (SQLException ex) {
                    throw ex;
                }
                catch (RuntimeException ex) {
                    sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
                    sqlEx.initCause(ex);
                    throw sqlEx;
                }
                if (this.getEncoding() == null) {
                    this.setEncoding("ISO8859_1");
                }
                if (this.getUseUnicode()) {
                    if (realJavaEncoding != null) {
                        if (realJavaEncoding.equalsIgnoreCase("UTF-8") || realJavaEncoding.equalsIgnoreCase("UTF8")) {
                            utf8mb4Supported = this.versionMeetsMinimum(5, 5, 2);
                            v0 = useutf8mb4 = utf8mb4Supported != false && CharsetMapping.UTF8MB4_INDEXES.contains(this.io.serverCharsetIndex) != false;
                            if (!this.getUseOldUTF8Behavior()) {
                                if (dontCheckServerMatch || !this.characterSetNamesMatches("utf8") || utf8mb4Supported && !this.characterSetNamesMatches("utf8mb4")) {
                                    this.execSQL(null, "SET NAMES " + (useutf8mb4 != false ? "utf8mb4" : "utf8"), -1, null, 1003, 1007, false, this.database, null, false);
                                    this.serverVariables.put("character_set_client", useutf8mb4 != false ? "utf8mb4" : "utf8");
                                    this.serverVariables.put("character_set_connection", useutf8mb4 != false ? "utf8mb4" : "utf8");
                                }
                            } else {
                                this.execSQL(null, "SET NAMES latin1", -1, null, 1003, 1007, false, this.database, null, false);
                                this.serverVariables.put("character_set_client", "latin1");
                                this.serverVariables.put("character_set_connection", "latin1");
                            }
                            this.setEncoding(realJavaEncoding);
                        } else {
                            mysqlCharsetName = CharsetMapping.getMysqlCharsetForJavaEncoding(realJavaEncoding.toUpperCase(Locale.ENGLISH), this);
                            if (mysqlCharsetName != null && (dontCheckServerMatch || !this.characterSetNamesMatches(mysqlCharsetName))) {
                                this.execSQL(null, "SET NAMES " + mysqlCharsetName, -1, null, 1003, 1007, false, this.database, null, false);
                                this.serverVariables.put("character_set_client", mysqlCharsetName);
                                this.serverVariables.put("character_set_connection", mysqlCharsetName);
                            }
                            this.setEncoding(realJavaEncoding);
                        }
                    } else if (this.getEncoding() != null) {
                        block61: {
                            mysqlCharsetName = this.getServerCharset();
                            if (this.getUseOldUTF8Behavior()) {
                                mysqlCharsetName = "latin1";
                            }
                            ucs2 = false;
                            if ("ucs2".equalsIgnoreCase(mysqlCharsetName) || "utf16".equalsIgnoreCase(mysqlCharsetName) || "utf16le".equalsIgnoreCase(mysqlCharsetName) || "utf32".equalsIgnoreCase(mysqlCharsetName)) {
                                mysqlCharsetName = "utf8";
                                ucs2 = true;
                                if (this.getCharacterSetResults() == null) {
                                    this.setCharacterSetResults("UTF-8");
                                }
                            }
                            if (dontCheckServerMatch || !this.characterSetNamesMatches(mysqlCharsetName) || ucs2) {
                                try {
                                    this.execSQL(null, "SET NAMES " + mysqlCharsetName, -1, null, 1003, 1007, false, this.database, null, false);
                                    this.serverVariables.put("character_set_client", mysqlCharsetName);
                                    this.serverVariables.put("character_set_connection", mysqlCharsetName);
                                }
                                catch (SQLException ex) {
                                    if (ex.getErrorCode() == 1820 && !this.getDisconnectOnExpiredPasswords()) break block61;
                                    throw ex;
                                }
                            }
                        }
                        realJavaEncoding = this.getEncoding();
                    }
                }
                onServer = null;
                isNullOnServer = false;
                if (this.serverVariables != null) {
                    onServer = this.serverVariables.get("character_set_results");
                    v1 = isNullOnServer = onServer == null || "NULL".equalsIgnoreCase(onServer) != false || onServer.length() == 0;
                }
                if (this.getCharacterSetResults() == null) {
                    if (!isNullOnServer) {
                        block62: {
                            try {
                                this.execSQL(null, "SET character_set_results = NULL", -1, null, 1003, 1007, false, this.database, null, false);
                            }
                            catch (SQLException ex) {
                                if (ex.getErrorCode() == 1820 && !this.getDisconnectOnExpiredPasswords()) break block62;
                                throw ex;
                            }
                        }
                        this.serverVariables.put("jdbc.local.character_set_results", null);
                    } else {
                        this.serverVariables.put("jdbc.local.character_set_results", onServer);
                    }
                } else {
                    block63: {
                        if (this.getUseOldUTF8Behavior()) {
                            try {
                                this.execSQL(null, "SET NAMES latin1", -1, null, 1003, 1007, false, this.database, null, false);
                                this.serverVariables.put("character_set_client", "latin1");
                                this.serverVariables.put("character_set_connection", "latin1");
                            }
                            catch (SQLException ex) {
                                if (ex.getErrorCode() == 1820 && !this.getDisconnectOnExpiredPasswords()) break block63;
                                throw ex;
                            }
                        }
                    }
                    charsetResults = this.getCharacterSetResults();
                    mysqlEncodingName = null;
                    mysqlEncodingName = "UTF-8".equalsIgnoreCase(charsetResults) != false || "UTF8".equalsIgnoreCase(charsetResults) != false ? "utf8" : ("null".equalsIgnoreCase(charsetResults) != false ? "NULL" : CharsetMapping.getMysqlCharsetForJavaEncoding(charsetResults.toUpperCase(Locale.ENGLISH), this));
                    if (mysqlEncodingName == null) {
                        throw SQLError.createSQLException("Can't map " + charsetResults + " given for characterSetResults to a supported MySQL encoding.", "S1009", this.getExceptionInterceptor());
                    }
                    if (!mysqlEncodingName.equalsIgnoreCase(this.serverVariables.get("character_set_results"))) {
                        block64: {
                            setBuf = new StringBuilder("SET character_set_results = ".length() + mysqlEncodingName.length());
                            setBuf.append("SET character_set_results = ").append(mysqlEncodingName);
                            try {
                                this.execSQL(null, setBuf.toString(), -1, null, 1003, 1007, false, this.database, null, false);
                            }
                            catch (SQLException ex) {
                                if (ex.getErrorCode() == 1820 && !this.getDisconnectOnExpiredPasswords()) break block64;
                                throw ex;
                            }
                        }
                        this.serverVariables.put("jdbc.local.character_set_results", mysqlEncodingName);
                        if (this.versionMeetsMinimum(5, 5, 0)) {
                            this.errorMessageEncoding = charsetResults;
                        }
                    } else {
                        this.serverVariables.put("jdbc.local.character_set_results", onServer);
                    }
                }
                if (this.getConnectionCollation() != null) {
                    setBuf = new StringBuilder("SET collation_connection = ".length() + this.getConnectionCollation().length());
                    setBuf.append("SET collation_connection = ").append(this.getConnectionCollation());
                    try {
                        this.execSQL(null, setBuf.toString(), -1, null, 1003, 1007, false, this.database, null, false);
                    }
                    catch (SQLException ex) {
                        if (ex.getErrorCode() == 1820 && !this.getDisconnectOnExpiredPasswords()) ** GOTO lbl165
                        throw ex;
                    }
                }
            } else {
                realJavaEncoding = this.getEncoding();
            }
lbl165:
            // 4 sources

            var11_24 = null;
        }
        catch (Throwable var10_26) {
            var11_25 = null;
            this.setEncoding(realJavaEncoding);
            throw var10_26;
        }
        this.setEncoding(realJavaEncoding);
        try {
            enc = Charset.forName(this.getEncoding()).newEncoder();
            cbuf = CharBuffer.allocate(1);
            bbuf = ByteBuffer.allocate(1);
            cbuf.put("\u00a5");
            cbuf.position(0);
            enc.encode(cbuf, bbuf, true);
            if (bbuf.get(0) == 92) {
                this.requiresEscapingEncoder = true;
            } else {
                cbuf.clear();
                bbuf.clear();
                cbuf.put("\u20a9");
                cbuf.position(0);
                enc.encode(cbuf, bbuf, true);
                if (bbuf.get(0) == 92) {
                    this.requiresEscapingEncoder = true;
                }
            }
        }
        catch (UnsupportedCharsetException ucex) {
            try {
                bbuf = StringUtils.getBytes("\u00a5", this.getEncoding());
                if (bbuf[0] == 92) {
                    this.requiresEscapingEncoder = true;
                } else {
                    bbuf = StringUtils.getBytes("\u20a9", this.getEncoding());
                    if (bbuf[0] == 92) {
                        this.requiresEscapingEncoder = true;
                    }
                }
            }
            catch (UnsupportedEncodingException ueex) {
                throw SQLError.createSQLException("Unable to use encoding: " + this.getEncoding(), "S1000", ueex, this.getExceptionInterceptor());
            }
        }
        return characterSetAlreadyConfigured;
    }

    private void configureTimezone() throws SQLException {
        String configuredTimeZoneOnServer = this.serverVariables.get("timezone");
        if (configuredTimeZoneOnServer == null && "SYSTEM".equalsIgnoreCase(configuredTimeZoneOnServer = this.serverVariables.get("time_zone"))) {
            configuredTimeZoneOnServer = this.serverVariables.get("system_time_zone");
        }
        String canonicalTimezone = this.getServerTimezone();
        if (!(!this.getUseTimezone() && this.getUseLegacyDatetimeCode() || configuredTimeZoneOnServer == null || canonicalTimezone != null && !StringUtils.isEmptyOrWhitespaceOnly(canonicalTimezone))) {
            try {
                canonicalTimezone = TimeUtil.getCanonicalTimezone(configuredTimeZoneOnServer, this.getExceptionInterceptor());
            }
            catch (IllegalArgumentException iae) {
                throw SQLError.createSQLException(iae.getMessage(), "S1000", this.getExceptionInterceptor());
            }
        }
        if (canonicalTimezone != null && canonicalTimezone.length() > 0) {
            this.serverTimezoneTZ = TimeZone.getTimeZone(canonicalTimezone);
            if (!canonicalTimezone.equalsIgnoreCase("GMT") && this.serverTimezoneTZ.getID().equals("GMT")) {
                throw SQLError.createSQLException("No timezone mapping entry for '" + canonicalTimezone + "'", "S1009", this.getExceptionInterceptor());
            }
            this.isServerTzUTC = !this.serverTimezoneTZ.useDaylightTime() && this.serverTimezoneTZ.getRawOffset() == 0;
        }
    }

    private void createInitialHistogram(long[] breakpoints, long lowerBound, long upperBound) {
        double bucketSize = ((double)upperBound - (double)lowerBound) / 20.0 * 1.25;
        if (bucketSize < 1.0) {
            bucketSize = 1.0;
        }
        for (int i = 0; i < 20; ++i) {
            breakpoints[i] = lowerBound;
            lowerBound = (long)((double)lowerBound + bucketSize);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void createNewIO(boolean isForReconnect) throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            Properties mergedProps = this.exposeAsProperties(this.props);
            if (!this.getHighAvailability()) {
                this.connectOneTryOnly(isForReconnect, mergedProps);
                return;
            }
            this.connectWithRetries(isForReconnect, mergedProps);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void connectWithRetries(boolean isForReconnect, Properties mergedProps) throws SQLException {
        double timeout = this.getInitialTimeout();
        boolean connectionGood = false;
        Exception connectionException = null;
        for (int attemptCount = 0; attemptCount < this.getMaxReconnects() && !connectionGood; ++attemptCount) {
            try {
                String oldCatalog;
                boolean oldReadOnly;
                int oldIsolationLevel;
                boolean oldAutoCommit;
                if (this.io != null) {
                    this.io.forceClose();
                }
                this.coreConnect(mergedProps);
                this.pingInternal(false, 0);
                Object object = this.getConnectionMutex();
                synchronized (object) {
                    this.connectionId = this.io.getThreadId();
                    this.isClosed = false;
                    oldAutoCommit = this.getAutoCommit();
                    oldIsolationLevel = this.isolationLevel;
                    oldReadOnly = this.isReadOnly(false);
                    oldCatalog = this.getCatalog();
                    this.io.setStatementInterceptors(this.statementInterceptors);
                }
                this.initializePropsFromServer();
                if (isForReconnect) {
                    this.setAutoCommit(oldAutoCommit);
                    if (this.hasIsolationLevels) {
                        this.setTransactionIsolation(oldIsolationLevel);
                    }
                    this.setCatalog(oldCatalog);
                    this.setReadOnly(oldReadOnly);
                }
                connectionGood = true;
                break;
            }
            catch (Exception EEE) {
                connectionException = EEE;
                connectionGood = false;
                if (connectionGood) break;
                if (attemptCount <= 0) continue;
                try {
                    Thread.sleep((long)timeout * 1000L);
                }
                catch (InterruptedException IE) {
                    // empty catch block
                }
                continue;
            }
        }
        if (!connectionGood) {
            SQLException chainedEx = SQLError.createSQLException(Messages.getString("Connection.UnableToConnectWithRetries", new Object[]{this.getMaxReconnects()}), "08001", this.getExceptionInterceptor());
            chainedEx.initCause(connectionException);
            throw chainedEx;
        }
        if (this.getParanoid() && !this.getHighAvailability()) {
            this.password = null;
            this.user = null;
        }
        if (isForReconnect) {
            Iterator<Statement> statementIter = this.openStatements.iterator();
            Stack<Statement> serverPreparedStatements = null;
            while (statementIter.hasNext()) {
                Statement statementObj = statementIter.next();
                if (!(statementObj instanceof ServerPreparedStatement)) continue;
                if (serverPreparedStatements == null) {
                    serverPreparedStatements = new Stack<Statement>();
                }
                serverPreparedStatements.add(statementObj);
            }
            if (serverPreparedStatements != null) {
                while (!serverPreparedStatements.isEmpty()) {
                    ((ServerPreparedStatement)serverPreparedStatements.pop()).rePrepare();
                }
            }
        }
    }

    private void coreConnect(Properties mergedProps) throws SQLException, IOException {
        int newPort = 3306;
        String newHost = "localhost";
        String protocol = mergedProps.getProperty("PROTOCOL");
        if (protocol != null) {
            if ("tcp".equalsIgnoreCase(protocol)) {
                newHost = this.normalizeHost(mergedProps.getProperty("HOST"));
                newPort = this.parsePortNumber(mergedProps.getProperty("PORT", "3306"));
            } else if ("pipe".equalsIgnoreCase(protocol)) {
                this.setSocketFactoryClassName(NamedPipeSocketFactory.class.getName());
                String path = mergedProps.getProperty("PATH");
                if (path != null) {
                    mergedProps.setProperty("namedPipePath", path);
                }
            } else {
                newHost = this.normalizeHost(mergedProps.getProperty("HOST"));
                newPort = this.parsePortNumber(mergedProps.getProperty("PORT", "3306"));
            }
        } else {
            String[] parsedHostPortPair = NonRegisteringDriver.parseHostPortPair(this.hostPortPair);
            newHost = parsedHostPortPair[0];
            newHost = this.normalizeHost(newHost);
            if (parsedHostPortPair[1] != null) {
                newPort = this.parsePortNumber(parsedHostPortPair[1]);
            }
        }
        this.port = newPort;
        this.host = newHost;
        this.sessionMaxRows = -1;
        this.io = new MysqlIO(newHost, newPort, mergedProps, this.getSocketFactoryClassName(), this.getProxy(), this.getSocketTimeout(), this.largeRowSizeThreshold.getValueAsInt());
        this.io.doHandshake(this.user, this.password, this.database);
        if (this.versionMeetsMinimum(5, 5, 0)) {
            this.errorMessageEncoding = this.io.getEncodingForHandshake();
        }
    }

    private String normalizeHost(String hostname) {
        if (hostname == null || StringUtils.isEmptyOrWhitespaceOnly(hostname)) {
            return "localhost";
        }
        return hostname;
    }

    private int parsePortNumber(String portAsString) throws SQLException {
        int portNumber = 3306;
        try {
            portNumber = Integer.parseInt(portAsString);
        }
        catch (NumberFormatException nfe) {
            throw SQLError.createSQLException("Illegal connection port value '" + portAsString + "'", "01S00", this.getExceptionInterceptor());
        }
        return portNumber;
    }

    private void connectOneTryOnly(boolean isForReconnect, Properties mergedProps) throws SQLException {
        Exception connectionNotEstablishedBecause = null;
        try {
            this.coreConnect(mergedProps);
            this.connectionId = this.io.getThreadId();
            this.isClosed = false;
            boolean oldAutoCommit = this.getAutoCommit();
            int oldIsolationLevel = this.isolationLevel;
            boolean oldReadOnly = this.isReadOnly(false);
            String oldCatalog = this.getCatalog();
            this.io.setStatementInterceptors(this.statementInterceptors);
            this.initializePropsFromServer();
            if (isForReconnect) {
                this.setAutoCommit(oldAutoCommit);
                if (this.hasIsolationLevels) {
                    this.setTransactionIsolation(oldIsolationLevel);
                }
                this.setCatalog(oldCatalog);
                this.setReadOnly(oldReadOnly);
            }
            return;
        }
        catch (Exception EEE) {
            if (EEE instanceof SQLException && ((SQLException)EEE).getErrorCode() == 1820 && !this.getDisconnectOnExpiredPasswords()) {
                return;
            }
            if (this.io != null) {
                this.io.forceClose();
            }
            connectionNotEstablishedBecause = EEE;
            if (EEE instanceof SQLException) {
                throw (SQLException)EEE;
            }
            SQLException chainedEx = SQLError.createSQLException(Messages.getString("Connection.UnableToConnect"), "08001", this.getExceptionInterceptor());
            chainedEx.initCause(connectionNotEstablishedBecause);
            throw chainedEx;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void createPreparedStatementCaches() throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            int cacheSize = this.getPreparedStatementCacheSize();
            try {
                Class<?> factoryClass = Class.forName(this.getParseInfoCacheFactory());
                CacheAdapterFactory cacheFactory = (CacheAdapterFactory)factoryClass.newInstance();
                this.cachedPreparedStatementParams = cacheFactory.getInstance(this, this.myURL, this.getPreparedStatementCacheSize(), this.getPreparedStatementCacheSqlLimit(), this.props);
            }
            catch (ClassNotFoundException e) {
                SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantFindCacheFactory", new Object[]{this.getParseInfoCacheFactory(), "parseInfoCacheFactory"}), this.getExceptionInterceptor());
                sqlEx.initCause(e);
                throw sqlEx;
            }
            catch (InstantiationException e) {
                SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantLoadCacheFactory", new Object[]{this.getParseInfoCacheFactory(), "parseInfoCacheFactory"}), this.getExceptionInterceptor());
                sqlEx.initCause(e);
                throw sqlEx;
            }
            catch (IllegalAccessException e) {
                SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantLoadCacheFactory", new Object[]{this.getParseInfoCacheFactory(), "parseInfoCacheFactory"}), this.getExceptionInterceptor());
                sqlEx.initCause(e);
                throw sqlEx;
            }
            if (this.getUseServerPreparedStmts()) {
                this.serverSideStatementCheckCache = new LRUCache(cacheSize);
                this.serverSideStatementCache = new LRUCache(cacheSize){
                    private static final long serialVersionUID = 7692318650375988114L;

                    @Override
                    protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
                        if (this.maxElements <= 1) {
                            return false;
                        }
                        boolean removeIt = super.removeEldestEntry(eldest);
                        if (removeIt) {
                            ServerPreparedStatement ps = (ServerPreparedStatement)eldest.getValue();
                            ps.isCached = false;
                            ps.setClosed(false);
                            try {
                                ps.close();
                            }
                            catch (SQLException sqlEx) {
                                // empty catch block
                            }
                        }
                        return removeIt;
                    }
                };
            }
        }
    }

    @Override
    public java.sql.Statement createStatement() throws SQLException {
        return this.createStatement(1003, 1007);
    }

    @Override
    public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        this.checkClosed();
        StatementImpl stmt = new StatementImpl(this.getMultiHostSafeProxy(), this.database);
        stmt.setResultSetType(resultSetType);
        stmt.setResultSetConcurrency(resultSetConcurrency);
        return stmt;
    }

    @Override
    public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (this.getPedantic() && resultSetHoldability != 1) {
            throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009", this.getExceptionInterceptor());
        }
        return this.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public void dumpTestcaseQuery(String query) {
        System.err.println(query);
    }

    @Override
    public Connection duplicate() throws SQLException {
        return new ConnectionImpl(this.origHostToConnectTo, this.origPortToConnectTo, this.props, this.origDatabaseToConnectTo, this.myURL);
    }

    @Override
    public ResultSetInternalMethods execSQL(StatementImpl callingStatement, String sql, int maxRows, Buffer packet, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Field[] cachedMetadata) throws SQLException {
        return this.execSQL(callingStatement, sql, maxRows, packet, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata, false);
    }

    /*
     * Exception decompiling
     */
    @Override
    public ResultSetInternalMethods execSQL(StatementImpl callingStatement, String sql, int maxRows, Buffer packet, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Field[] cachedMetadata, boolean isBatch) throws SQLException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [0[TRYBLOCK], 2[TRYBLOCK]], but top level block is 4[TRYBLOCK]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    @Override
    public String extractSqlFromPacket(String possibleSqlQuery, Buffer queryPacket, int endOfQueryPacketPosition) throws SQLException {
        String extractedSql = null;
        if (possibleSqlQuery != null) {
            if (possibleSqlQuery.length() > this.getMaxQuerySizeToLog()) {
                StringBuilder truncatedQueryBuf = new StringBuilder(possibleSqlQuery.substring(0, this.getMaxQuerySizeToLog()));
                truncatedQueryBuf.append(Messages.getString("MysqlIO.25"));
                extractedSql = truncatedQueryBuf.toString();
            } else {
                extractedSql = possibleSqlQuery;
            }
        }
        if (extractedSql == null) {
            int extractPosition = endOfQueryPacketPosition;
            boolean truncated = false;
            if (endOfQueryPacketPosition > this.getMaxQuerySizeToLog()) {
                extractPosition = this.getMaxQuerySizeToLog();
                truncated = true;
            }
            extractedSql = StringUtils.toString(queryPacket.getByteBuffer(), 5, extractPosition - 5);
            if (truncated) {
                extractedSql = extractedSql + Messages.getString("MysqlIO.25");
            }
        }
        return extractedSql;
    }

    @Override
    public StringBuilder generateConnectionCommentBlock(StringBuilder buf) {
        buf.append("/* conn id ");
        buf.append(this.getId());
        buf.append(" clock: ");
        buf.append(System.currentTimeMillis());
        buf.append(" */ ");
        return buf;
    }

    @Override
    public int getActiveStatementCount() {
        return this.openStatements.size();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean getAutoCommit() throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            return this.autoCommit;
        }
    }

    @Override
    public Calendar getCalendarInstanceForSessionOrNew() {
        if (this.getDynamicCalendars()) {
            return Calendar.getInstance();
        }
        return this.getSessionLockedCalendar();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getCatalog() throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            return this.database;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getCharacterSetMetadata() {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            return this.characterSetMetadata;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public SingleByteCharsetConverter getCharsetConverter(String javaEncodingName) throws SQLException {
        if (javaEncodingName == null) {
            return null;
        }
        if (this.usePlatformCharsetConverters) {
            return null;
        }
        SingleByteCharsetConverter converter = null;
        Map<String, Object> map = this.charsetConverterMap;
        synchronized (map) {
            Object asObject = this.charsetConverterMap.get(javaEncodingName);
            if (asObject == CHARSET_CONVERTER_NOT_AVAILABLE_MARKER) {
                return null;
            }
            converter = (SingleByteCharsetConverter)asObject;
            if (converter == null) {
                try {
                    converter = SingleByteCharsetConverter.getInstance(javaEncodingName, this);
                    if (converter == null) {
                        this.charsetConverterMap.put(javaEncodingName, CHARSET_CONVERTER_NOT_AVAILABLE_MARKER);
                    } else {
                        this.charsetConverterMap.put(javaEncodingName, converter);
                    }
                }
                catch (UnsupportedEncodingException unsupEncEx) {
                    this.charsetConverterMap.put(javaEncodingName, CHARSET_CONVERTER_NOT_AVAILABLE_MARKER);
                    converter = null;
                }
            }
        }
        return converter;
    }

    @Override
    @Deprecated
    public String getCharsetNameForIndex(int charsetIndex) throws SQLException {
        return this.getEncodingForIndex(charsetIndex);
    }

    @Override
    public String getEncodingForIndex(int charsetIndex) throws SQLException {
        String javaEncoding = null;
        if (this.getUseOldUTF8Behavior()) {
            return this.getEncoding();
        }
        if (charsetIndex != -1) {
            try {
                if (this.indexToMysqlCharset.size() > 0) {
                    javaEncoding = CharsetMapping.getJavaEncodingForMysqlCharset(this.indexToMysqlCharset.get(charsetIndex), this.getEncoding());
                }
                if (javaEncoding == null) {
                    javaEncoding = CharsetMapping.getJavaEncodingForCollationIndex(charsetIndex, this.getEncoding());
                }
            }
            catch (ArrayIndexOutOfBoundsException outOfBoundsEx) {
                throw SQLError.createSQLException("Unknown character set index for field '" + charsetIndex + "' received from server.", "S1000", this.getExceptionInterceptor());
            }
            catch (RuntimeException ex) {
                SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
                sqlEx.initCause(ex);
                throw sqlEx;
            }
            if (javaEncoding == null) {
                javaEncoding = this.getEncoding();
            }
        } else {
            javaEncoding = this.getEncoding();
        }
        return javaEncoding;
    }

    @Override
    public TimeZone getDefaultTimeZone() {
        return this.getCacheDefaultTimezone() ? this.defaultTimeZone : TimeUtil.getDefaultTimeZone(false);
    }

    @Override
    public String getErrorMessageEncoding() {
        return this.errorMessageEncoding;
    }

    @Override
    public int getHoldability() throws SQLException {
        return 2;
    }

    @Override
    public long getId() {
        return this.connectionId;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long getIdleFor() {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            if (this.lastQueryFinishedTime == 0L) {
                return 0L;
            }
            long now = System.currentTimeMillis();
            long idleTime = now - this.lastQueryFinishedTime;
            return idleTime;
        }
    }

    @Override
    public MysqlIO getIO() throws SQLException {
        if (this.io == null || this.isClosed) {
            throw SQLError.createSQLException("Operation not allowed on closed connection", "08003", this.getExceptionInterceptor());
        }
        return this.io;
    }

    @Override
    public Log getLog() throws SQLException {
        return this.log;
    }

    @Override
    public int getMaxBytesPerChar(String javaCharsetName) throws SQLException {
        return this.getMaxBytesPerChar(null, javaCharsetName);
    }

    @Override
    public int getMaxBytesPerChar(Integer charsetIndex, String javaCharsetName) throws SQLException {
        String charset = null;
        int res = 1;
        try {
            if (this.indexToCustomMysqlCharset != null) {
                charset = this.indexToCustomMysqlCharset.get(charsetIndex);
            }
            if (charset == null) {
                charset = CharsetMapping.getMysqlCharsetNameForCollationIndex(charsetIndex);
            }
            if (charset == null) {
                charset = CharsetMapping.getMysqlCharsetForJavaEncoding(javaCharsetName, this);
            }
            Integer mblen = null;
            if (this.mysqlCharsetToCustomMblen != null) {
                mblen = this.mysqlCharsetToCustomMblen.get(charset);
            }
            if (mblen == null) {
                mblen = CharsetMapping.getMblen(charset);
            }
            if (mblen != null) {
                res = mblen;
            }
        }
        catch (SQLException ex) {
            throw ex;
        }
        catch (RuntimeException ex) {
            SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
            sqlEx.initCause(ex);
            throw sqlEx;
        }
        return res;
    }

    @Override
    public java.sql.DatabaseMetaData getMetaData() throws SQLException {
        return this.getMetaData(true, true);
    }

    private java.sql.DatabaseMetaData getMetaData(boolean checkClosed, boolean checkForInfoSchema) throws SQLException {
        if (checkClosed) {
            this.checkClosed();
        }
        return DatabaseMetaData.getInstance(this.getMultiHostSafeProxy(), this.database, checkForInfoSchema);
    }

    @Override
    public java.sql.Statement getMetadataSafeStatement() throws SQLException {
        java.sql.Statement stmt = this.createStatement();
        if (stmt.getMaxRows() != 0) {
            stmt.setMaxRows(0);
        }
        stmt.setEscapeProcessing(false);
        if (stmt.getFetchSize() != 0) {
            stmt.setFetchSize(0);
        }
        return stmt;
    }

    @Override
    public int getNetBufferLength() {
        return this.netBufferLength;
    }

    @Override
    @Deprecated
    public String getServerCharacterEncoding() {
        return this.getServerCharset();
    }

    @Override
    public String getServerCharset() {
        if (this.io.versionMeetsMinimum(4, 1, 0)) {
            String charset = null;
            if (this.indexToCustomMysqlCharset != null) {
                charset = this.indexToCustomMysqlCharset.get(this.io.serverCharsetIndex);
            }
            if (charset == null) {
                charset = CharsetMapping.getMysqlCharsetNameForCollationIndex(this.io.serverCharsetIndex);
            }
            return charset != null ? charset : this.serverVariables.get("character_set_server");
        }
        return this.serverVariables.get("character_set");
    }

    @Override
    public int getServerMajorVersion() {
        return this.io.getServerMajorVersion();
    }

    @Override
    public int getServerMinorVersion() {
        return this.io.getServerMinorVersion();
    }

    @Override
    public int getServerSubMinorVersion() {
        return this.io.getServerSubMinorVersion();
    }

    @Override
    public TimeZone getServerTimezoneTZ() {
        return this.serverTimezoneTZ;
    }

    @Override
    public String getServerVariable(String variableName) {
        if (this.serverVariables != null) {
            return this.serverVariables.get(variableName);
        }
        return null;
    }

    @Override
    public String getServerVersion() {
        return this.io.getServerVersion();
    }

    @Override
    public Calendar getSessionLockedCalendar() {
        return this.sessionCalendar;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    @Override
    public int getTransactionIsolation() throws SQLException {
        var1_1 = this.getConnectionMutex();
        synchronized (var1_1) {
            block23: {
                block19: {
                    if (!this.hasIsolationLevels || this.getUseLocalSessionState()) break block22;
                    stmt = null;
                    rs = null;
                    stmt = this.getMetadataSafeStatement();
                    query = null;
                    offset = 0;
                    if (this.versionMeetsMinimum(4, 0, 3)) {
                        query = "SELECT @@session.tx_isolation";
                        offset = 1;
                    } else {
                        query = "SHOW VARIABLES LIKE 'transaction_isolation'";
                        offset = 2;
                    }
                    rs = stmt.executeQuery(query);
                    if (!rs.next()) ** GOTO lbl46
                    s = rs.getString(offset);
                    if (s == null || (intTI = ConnectionImpl.mapTransIsolationNameToValue.get(s)) == null) ** break block18
                    var8_8 = intTI;
                    var10_9 = null;
                    if (rs == null) break block19;
                    try {
                        rs.close();
                    }
                    catch (Exception ex) {
                        // empty catch block
                    }
                    rs = null;
                }
                if (stmt == null) break block23;
                try {
                    stmt.close();
                }
                catch (Exception ex) {
                    // empty catch block
                }
                stmt = null;
            }
            return var8_8;
            {
                block22: {
                    try {
                        throw SQLError.createSQLException("Could not map transaction isolation '" + s + " to a valid JDBC level.", "S1000", this.getExceptionInterceptor());
lbl46:
                        // 1 sources

                        throw SQLError.createSQLException("Could not retrieve transaction isolation level from server", "S1000", this.getExceptionInterceptor());
                    }
                    catch (Throwable var9_13) {
                        var10_10 = null;
                        if (rs != null) {
                            try {
                                rs.close();
                            }
                            catch (Exception ex) {
                                // empty catch block
                            }
                            rs = null;
                        }
                        if (stmt != null) {
                            try {
                                stmt.close();
                            }
                            catch (Exception ex) {
                                // empty catch block
                            }
                            stmt = null;
                        }
                        throw var9_13;
                    }
                }
                return this.isolationLevel;
            }
            finally {
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            if (this.typeMap == null) {
                this.typeMap = new HashMap();
            }
            return this.typeMap;
        }
    }

    @Override
    public String getURL() {
        return this.myURL;
    }

    @Override
    public String getUser() {
        return this.user;
    }

    @Override
    public Calendar getUtcCalendar() {
        return this.utcCalendar;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public boolean hasSameProperties(Connection c) {
        return this.props.equals(c.getProperties());
    }

    @Override
    public Properties getProperties() {
        return this.props;
    }

    @Override
    @Deprecated
    public boolean hasTriedMaster() {
        return this.hasTriedMasterFlag;
    }

    @Override
    public void incrementNumberOfPreparedExecutes() {
        if (this.getGatherPerformanceMetrics()) {
            ++this.numberOfPreparedExecutes;
            ++this.numberOfQueriesIssued;
        }
    }

    @Override
    public void incrementNumberOfPrepares() {
        if (this.getGatherPerformanceMetrics()) {
            ++this.numberOfPrepares;
        }
    }

    @Override
    public void incrementNumberOfResultSetsCreated() {
        if (this.getGatherPerformanceMetrics()) {
            ++this.numberOfResultSetsCreated;
        }
    }

    private void initializeDriverProperties(Properties info) throws SQLException {
        this.initializeProperties(info);
        String exceptionInterceptorClasses = this.getExceptionInterceptors();
        if (exceptionInterceptorClasses != null && !"".equals(exceptionInterceptorClasses)) {
            this.exceptionInterceptor = new ExceptionInterceptorChain(exceptionInterceptorClasses);
        }
        this.usePlatformCharsetConverters = this.getUseJvmCharsetConverters();
        this.log = LogFactory.getLogger(this.getLogger(), LOGGER_INSTANCE_NAME, this.getExceptionInterceptor());
        if (this.getProfileSql() || this.getUseUsageAdvisor()) {
            this.eventSink = ProfilerEventHandlerFactory.getInstance(this.getMultiHostSafeProxy());
        }
        if (this.getCachePreparedStatements()) {
            this.createPreparedStatementCaches();
        }
        if (this.getNoDatetimeStringSync() && this.getUseTimezone()) {
            throw SQLError.createSQLException("Can't enable noDatetimeStringSync and useTimezone configuration properties at the same time", "01S00", this.getExceptionInterceptor());
        }
        if (this.getCacheCallableStatements()) {
            this.parsedCallableStatementCache = new LRUCache(this.getCallableStatementCacheSize());
        }
        if (this.getAllowMultiQueries()) {
            this.setCacheResultSetMetadata(false);
        }
        if (this.getCacheResultSetMetadata()) {
            this.resultSetMetadataCache = new LRUCache(this.getMetadataCacheSize());
        }
        if (this.getSocksProxyHost() != null) {
            this.setSocketFactoryClassName("com.mysql.jdbc.SocksProxySocketFactory");
        }
    }

    /*
     * Unable to fully structure code
     */
    private void initializePropsFromServer() throws SQLException {
        connectionInterceptorClasses = this.getConnectionLifecycleInterceptors();
        this.connectionLifecycleInterceptors = null;
        if (connectionInterceptorClasses != null) {
            this.connectionLifecycleInterceptors = Util.loadExtensions(this, this.props, connectionInterceptorClasses, "Connection.badLifecycleInterceptor", this.getExceptionInterceptor());
        }
        this.setSessionVariables();
        if (!this.versionMeetsMinimum(4, 1, 0)) {
            this.setTransformedBitIsBoolean(false);
        }
        this.parserKnowsUnicode = this.versionMeetsMinimum(4, 1, 0);
        if (this.getUseServerPreparedStmts() && this.versionMeetsMinimum(4, 1, 0)) {
            this.useServerPreparedStmts = true;
            if (this.versionMeetsMinimum(5, 0, 0) && !this.versionMeetsMinimum(5, 0, 3)) {
                this.useServerPreparedStmts = false;
            }
        }
        if (this.versionMeetsMinimum(3, 21, 22)) {
            this.loadServerVariables();
            this.autoIncrementIncrement = this.versionMeetsMinimum(5, 0, 2) != false ? this.getServerVariableAsInt("auto_increment_increment", 1) : 1;
            this.buildCollationMapping();
            LicenseConfiguration.checkLicenseType(this.serverVariables);
            lowerCaseTables = this.serverVariables.get("lower_case_table_names");
            this.lowerCaseTableNames = "on".equalsIgnoreCase(lowerCaseTables) != false || "1".equalsIgnoreCase(lowerCaseTables) != false || "2".equalsIgnoreCase(lowerCaseTables) != false;
            this.storesLowerCaseTableName = "1".equalsIgnoreCase(lowerCaseTables) != false || "on".equalsIgnoreCase(lowerCaseTables) != false;
            this.configureTimezone();
            if (this.serverVariables.containsKey("max_allowed_packet")) {
                serverMaxAllowedPacket = this.getServerVariableAsInt("max_allowed_packet", -1);
                if (serverMaxAllowedPacket != -1 && (serverMaxAllowedPacket < this.getMaxAllowedPacket() || this.getMaxAllowedPacket() <= 0)) {
                    this.setMaxAllowedPacket(serverMaxAllowedPacket);
                } else if (serverMaxAllowedPacket == -1 && this.getMaxAllowedPacket() == -1) {
                    this.setMaxAllowedPacket(65535);
                }
                if (this.getUseServerPrepStmts()) {
                    preferredBlobSendChunkSize = this.getBlobSendChunkSize();
                    packetHeaderSize = 8203;
                    allowedBlobSendChunkSize = Math.min(preferredBlobSendChunkSize, this.getMaxAllowedPacket()) - packetHeaderSize;
                    if (allowedBlobSendChunkSize <= 0) {
                        throw SQLError.createSQLException("Connection setting too low for 'maxAllowedPacket'. When 'useServerPrepStmts=true', 'maxAllowedPacket' must be higher than " + packetHeaderSize + ". Check also 'max_allowed_packet' in MySQL configuration files.", "01S00", this.getExceptionInterceptor());
                    }
                    this.setBlobSendChunkSize(String.valueOf(allowedBlobSendChunkSize));
                }
            }
            if (this.serverVariables.containsKey("net_buffer_length")) {
                this.netBufferLength = this.getServerVariableAsInt("net_buffer_length", 16384);
            }
            this.checkTransactionIsolationLevel();
            if (!this.versionMeetsMinimum(4, 1, 0)) {
                this.checkServerEncoding();
            }
            this.io.checkForCharsetMismatch();
            if (this.serverVariables.containsKey("sql_mode")) {
                sqlModeAsString = this.serverVariables.get("sql_mode");
                if (StringUtils.isStrictlyNumeric(sqlModeAsString)) {
                    this.useAnsiQuotes = (Integer.parseInt(sqlModeAsString) & 4) > 0;
                } else if (sqlModeAsString != null) {
                    this.useAnsiQuotes = sqlModeAsString.indexOf("ANSI_QUOTES") != -1;
                    this.noBackslashEscapes = sqlModeAsString.indexOf("NO_BACKSLASH_ESCAPES") != -1;
                }
            }
        }
        overrideDefaultAutocommit = this.isAutoCommitNonDefaultOnServer();
        this.configureClientCharacterSet(false);
        try {
            this.errorMessageEncoding = CharsetMapping.getCharacterEncodingForErrorMessages(this);
        }
        catch (SQLException ex) {
            throw ex;
        }
        catch (RuntimeException ex) {
            sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
            sqlEx.initCause(ex);
            throw sqlEx;
        }
        if (this.versionMeetsMinimum(3, 23, 15)) {
            this.transactionsSupported = true;
            if (!overrideDefaultAutocommit) {
                try {
                    this.setAutoCommit(true);
                }
                catch (SQLException ex) {
                    if (ex.getErrorCode() == 1820 && !this.getDisconnectOnExpiredPasswords()) ** GOTO lbl74
                    throw ex;
                }
            }
        } else {
            this.transactionsSupported = false;
        }
lbl74:
        // 4 sources

        this.hasIsolationLevels = this.versionMeetsMinimum(3, 23, 36) != false;
        this.hasQuotedIdentifiers = this.versionMeetsMinimum(3, 23, 6);
        this.io.resetMaxBuf();
        if (this.io.versionMeetsMinimum(4, 1, 0)) {
            characterSetResultsOnServerMysql = this.serverVariables.get("jdbc.local.character_set_results");
            if (characterSetResultsOnServerMysql == null || StringUtils.startsWithIgnoreCaseAndWs(characterSetResultsOnServerMysql, "NULL") || characterSetResultsOnServerMysql.length() == 0) {
                defaultMetadataCharsetMysql = this.serverVariables.get("character_set_system");
                defaultMetadataCharset = null;
                defaultMetadataCharset = defaultMetadataCharsetMysql != null ? CharsetMapping.getJavaEncodingForMysqlCharset(defaultMetadataCharsetMysql) : "UTF-8";
                this.characterSetMetadata = defaultMetadataCharset;
            } else {
                this.characterSetMetadata = this.characterSetResultsOnServer = CharsetMapping.getJavaEncodingForMysqlCharset(characterSetResultsOnServerMysql);
            }
        } else {
            this.characterSetMetadata = this.getEncoding();
        }
        if (this.versionMeetsMinimum(4, 1, 0) && !this.versionMeetsMinimum(4, 1, 10) && this.getAllowMultiQueries() && this.isQueryCacheEnabled()) {
            this.setAllowMultiQueries(false);
        }
        if (this.versionMeetsMinimum(5, 0, 0) && (this.getUseLocalTransactionState() || this.getElideSetAutoCommits()) && this.isQueryCacheEnabled() && !this.versionMeetsMinimum(5, 1, 32)) {
            this.setUseLocalTransactionState(false);
            this.setElideSetAutoCommits(false);
        }
        this.setupServerForTruncationChecks();
    }

    private boolean isQueryCacheEnabled() {
        return "ON".equalsIgnoreCase(this.serverVariables.get("query_cache_type")) && !"0".equalsIgnoreCase(this.serverVariables.get("query_cache_size"));
    }

    private int getServerVariableAsInt(String variableName, int fallbackValue) throws SQLException {
        try {
            return Integer.parseInt(this.serverVariables.get(variableName));
        }
        catch (NumberFormatException nfe) {
            this.getLog().logWarn(Messages.getString("Connection.BadValueInServerVariables", new Object[]{variableName, this.serverVariables.get(variableName), fallbackValue}));
            return fallbackValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean isAutoCommitNonDefaultOnServer() throws SQLException {
        boolean overrideDefaultAutocommit;
        block15: {
            block16: {
                SQLException sqlEx22;
                java.sql.Statement stmt;
                block14: {
                    overrideDefaultAutocommit = false;
                    String initConnectValue = this.serverVariables.get("init_connect");
                    if (!this.versionMeetsMinimum(4, 1, 2) || initConnectValue == null || initConnectValue.length() <= 0) break block15;
                    if (this.getElideSetAutoCommits()) break block16;
                    ResultSet rs = null;
                    stmt = null;
                    try {
                        stmt = this.getMetadataSafeStatement();
                        rs = stmt.executeQuery("SELECT @@session.autocommit");
                        if (rs.next()) {
                            this.autoCommit = rs.getBoolean(1);
                            if (!this.autoCommit) {
                                overrideDefaultAutocommit = true;
                            }
                        }
                        Object var6_5 = null;
                        if (rs == null) break block14;
                    }
                    catch (Throwable throwable) {
                        SQLException sqlEx22;
                        Object var6_6 = null;
                        if (rs != null) {
                            try {
                                rs.close();
                            }
                            catch (SQLException sqlEx22) {
                                // empty catch block
                            }
                        }
                        if (stmt != null) {
                            try {
                                stmt.close();
                            }
                            catch (SQLException sqlEx22) {
                                // empty catch block
                            }
                        }
                        throw throwable;
                    }
                    try {
                        rs.close();
                    }
                    catch (SQLException sqlEx22) {
                        // empty catch block
                    }
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    }
                    catch (SQLException sqlEx22) {}
                }
                break block15;
            }
            if (this.getIO().isSetNeededForAutoCommitMode(true)) {
                this.autoCommit = false;
                overrideDefaultAutocommit = true;
            }
        }
        return overrideDefaultAutocommit;
    }

    @Override
    public boolean isClientTzUTC() {
        return this.isClientTzUTC;
    }

    @Override
    public boolean isClosed() {
        return this.isClosed;
    }

    @Override
    public boolean isCursorFetchEnabled() throws SQLException {
        return this.versionMeetsMinimum(5, 0, 2) && this.getUseCursorFetch();
    }

    @Override
    public boolean isInGlobalTx() {
        return this.isInGlobalTx;
    }

    @Override
    public boolean isMasterConnection() {
        return false;
    }

    @Override
    public boolean isNoBackslashEscapesSet() {
        return this.noBackslashEscapes;
    }

    @Override
    public boolean isReadInfoMsgEnabled() {
        return this.readInfoMsg;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return this.isReadOnly(true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    @Override
    public boolean isReadOnly(boolean useSessionStatus) throws SQLException {
        block24: {
            Exception ex22;
            ResultSet rs;
            java.sql.Statement stmt;
            block21: {
                block20: {
                    block16: {
                        Exception ex22;
                        boolean bl;
                        block17: {
                            if (!useSessionStatus || this.isClosed || !this.versionMeetsMinimum(5, 6, 5) || this.getUseLocalSessionState() || !this.getReadOnlyPropagatesToServer()) break block24;
                            stmt = null;
                            rs = null;
                            stmt = this.getMetadataSafeStatement();
                            rs = stmt.executeQuery("select @@session.tx_read_only");
                            if (!rs.next()) break block16;
                            bl = rs.getInt(1) != 0;
                            Object var6_6 = null;
                            if (rs == null) break block17;
                            try {
                                rs.close();
                            }
                            catch (Exception ex22) {
                                // empty catch block
                            }
                            rs = null;
                        }
                        if (stmt != null) {
                            try {
                                stmt.close();
                            }
                            catch (Exception ex22) {
                                // empty catch block
                            }
                            stmt = null;
                        }
                        return bl;
                    }
                    break block20;
                    {
                        catch (SQLException ex1) {
                            if (ex1.getErrorCode() == 1820 && !this.getDisconnectOnExpiredPasswords()) break block20;
                            throw SQLError.createSQLException("Could not retrieve transation read-only status server", "S1000", ex1, this.getExceptionInterceptor());
                        }
                    }
                }
                Object var6_7 = null;
                if (rs == null) break block21;
                try {
                    rs.close();
                }
                catch (Exception ex22) {
                    // empty catch block
                }
                rs = null;
            }
            if (stmt == null) break block24;
            try {
                stmt.close();
            }
            catch (Exception ex22) {
                // empty catch block
            }
            stmt = null;
            {
            }
            catch (Throwable throwable) {
                Object var6_8 = null;
                if (rs != null) {
                    try {
                        rs.close();
                    }
                    catch (Exception ex22) {
                        // empty catch block
                    }
                    rs = null;
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    }
                    catch (Exception ex22) {
                        // empty catch block
                    }
                    stmt = null;
                }
                throw throwable;
            }
        }
        return this.readOnly;
    }

    @Override
    public boolean isRunningOnJDK13() {
        return this.isRunningOnJDK13;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isSameResource(Connection otherConnection) {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            if (otherConnection == null) {
                return false;
            }
            boolean directCompare = true;
            String otherHost = ((ConnectionImpl)otherConnection).origHostToConnectTo;
            String otherOrigDatabase = ((ConnectionImpl)otherConnection).origDatabaseToConnectTo;
            String otherCurrentCatalog = ((ConnectionImpl)otherConnection).database;
            if (!ConnectionImpl.nullSafeCompare(otherHost, this.origHostToConnectTo)) {
                directCompare = false;
            } else if (otherHost != null && otherHost.indexOf(44) == -1 && otherHost.indexOf(58) == -1) {
                boolean bl = directCompare = ((ConnectionImpl)otherConnection).origPortToConnectTo == this.origPortToConnectTo;
            }
            if (!(!directCompare || ConnectionImpl.nullSafeCompare(otherOrigDatabase, this.origDatabaseToConnectTo) && ConnectionImpl.nullSafeCompare(otherCurrentCatalog, this.database))) {
                directCompare = false;
            }
            if (directCompare) {
                return true;
            }
            String otherResourceId = ((ConnectionImpl)otherConnection).getResourceId();
            String myResourceId = this.getResourceId();
            return (otherResourceId != null || myResourceId != null) && (directCompare = ConnectionImpl.nullSafeCompare(otherResourceId, myResourceId));
            {
            }
        }
    }

    @Override
    public boolean isServerTzUTC() {
        return this.isServerTzUTC;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void createConfigCacheIfNeeded() throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            if (this.serverConfigCache != null) {
                return;
            }
            try {
                Class<?> factoryClass = Class.forName(this.getServerConfigCacheFactory());
                CacheAdapterFactory cacheFactory = (CacheAdapterFactory)factoryClass.newInstance();
                this.serverConfigCache = cacheFactory.getInstance(this, this.myURL, Integer.MAX_VALUE, Integer.MAX_VALUE, this.props);
                ExceptionInterceptor evictOnCommsError = new ExceptionInterceptor(){

                    public void init(Connection conn, Properties config) throws SQLException {
                    }

                    public void destroy() {
                    }

                    public SQLException interceptException(SQLException sqlEx, Connection conn) {
                        if (sqlEx.getSQLState() != null && sqlEx.getSQLState().startsWith("08")) {
                            ConnectionImpl.this.serverConfigCache.invalidate(ConnectionImpl.this.getURL());
                        }
                        return null;
                    }
                };
                if (this.exceptionInterceptor == null) {
                    this.exceptionInterceptor = evictOnCommsError;
                } else {
                    ((ExceptionInterceptorChain)this.exceptionInterceptor).addRingZero(evictOnCommsError);
                }
            }
            catch (ClassNotFoundException e) {
                SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantFindCacheFactory", new Object[]{this.getParseInfoCacheFactory(), "parseInfoCacheFactory"}), this.getExceptionInterceptor());
                sqlEx.initCause(e);
                throw sqlEx;
            }
            catch (InstantiationException e) {
                SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantLoadCacheFactory", new Object[]{this.getParseInfoCacheFactory(), "parseInfoCacheFactory"}), this.getExceptionInterceptor());
                sqlEx.initCause(e);
                throw sqlEx;
            }
            catch (IllegalAccessException e) {
                SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantLoadCacheFactory", new Object[]{this.getParseInfoCacheFactory(), "parseInfoCacheFactory"}), this.getExceptionInterceptor());
                sqlEx.initCause(e);
                throw sqlEx;
            }
        }
    }

    /*
     * Loose catch block
     */
    private void loadServerVariables() throws SQLException {
        block30: {
            SQLException sqlE22;
            ResultSet results;
            java.sql.Statement stmt;
            block29: {
                block28: {
                    block27: {
                        if (this.getCacheServerConfiguration()) {
                            this.createConfigCacheIfNeeded();
                            Map<String, String> cachedVariableMap = this.serverConfigCache.get(this.getURL());
                            if (cachedVariableMap != null) {
                                String cachedServerVersion = cachedVariableMap.get(SERVER_VERSION_STRING_VAR_NAME);
                                if (cachedServerVersion != null && this.io.getServerVersion() != null && cachedServerVersion.equals(this.io.getServerVersion())) {
                                    this.serverVariables = cachedVariableMap;
                                    return;
                                }
                                this.serverConfigCache.invalidate(this.getURL());
                            }
                        }
                        stmt = null;
                        results = null;
                        stmt = this.getMetadataSafeStatement();
                        String version = this.dbmd.getDriverVersion();
                        if (version != null && version.indexOf(42) != -1) {
                            StringBuilder buf = new StringBuilder(version.length() + 10);
                            for (int i = 0; i < version.length(); ++i) {
                                char c = version.charAt(i);
                                if (c == '*') {
                                    buf.append("[star]");
                                    continue;
                                }
                                buf.append(c);
                            }
                            version = buf.toString();
                        }
                        String versionComment = this.getParanoid() || version == null ? "" : "/* " + version + " */";
                        this.serverVariables = new HashMap<String, String>();
                        try {
                            if (this.versionMeetsMinimum(5, 1, 0)) {
                                StringBuilder queryBuf = new StringBuilder(versionComment).append("SELECT");
                                queryBuf.append("  @@session.auto_increment_increment AS auto_increment_increment");
                                queryBuf.append(", @@character_set_client AS character_set_client");
                                queryBuf.append(", @@character_set_connection AS character_set_connection");
                                queryBuf.append(", @@character_set_results AS character_set_results");
                                queryBuf.append(", @@character_set_server AS character_set_server");
                                queryBuf.append(", @@init_connect AS init_connect");
                                queryBuf.append(", @@interactive_timeout AS interactive_timeout");
                                if (!this.versionMeetsMinimum(5, 5, 0)) {
                                    queryBuf.append(", @@language AS language");
                                }
                                queryBuf.append(", @@license AS license");
                                queryBuf.append(", @@lower_case_table_names AS lower_case_table_names");
                                queryBuf.append(", @@max_allowed_packet AS max_allowed_packet");
                                queryBuf.append(", @@net_buffer_length AS net_buffer_length");
                                queryBuf.append(", @@net_write_timeout AS net_write_timeout");
                                queryBuf.append(", @@query_cache_size AS query_cache_size");
                                queryBuf.append(", @@query_cache_type AS query_cache_type");
                                queryBuf.append(", @@sql_mode AS sql_mode");
                                queryBuf.append(", @@system_time_zone AS system_time_zone");
                                queryBuf.append(", @@time_zone AS time_zone");
                                queryBuf.append(", @@tx_isolation AS tx_isolation");
                                queryBuf.append(", @@wait_timeout AS wait_timeout");
                                results = stmt.executeQuery(queryBuf.toString());
                                if (results.next()) {
                                    ResultSetMetaData rsmd = results.getMetaData();
                                    for (int i = 1; i <= rsmd.getColumnCount(); ++i) {
                                        this.serverVariables.put(rsmd.getColumnLabel(i), results.getString(i));
                                    }
                                }
                            } else {
                                results = stmt.executeQuery(versionComment + "SHOW VARIABLES");
                                while (results.next()) {
                                    this.serverVariables.put(results.getString(1), results.getString(2));
                                }
                            }
                            results.close();
                            results = null;
                        }
                        catch (SQLException ex) {
                            if (ex.getErrorCode() == 1820 && !this.getDisconnectOnExpiredPasswords()) break block27;
                            throw ex;
                        }
                    }
                    if (!this.getCacheServerConfiguration()) break block28;
                    this.serverVariables.put(SERVER_VERSION_STRING_VAR_NAME, this.io.getServerVersion());
                    this.serverConfigCache.put(this.getURL(), this.serverVariables);
                }
                Object var9_12 = null;
                if (results == null) break block29;
                try {
                    results.close();
                }
                catch (SQLException sqlE22) {
                    // empty catch block
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException sqlE22) {}
            }
            break block30;
            {
                catch (SQLException e) {
                    throw e;
                }
            }
            catch (Throwable throwable) {
                SQLException sqlE22;
                Object var9_13 = null;
                if (results != null) {
                    try {
                        results.close();
                    }
                    catch (SQLException sqlE22) {
                        // empty catch block
                    }
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    }
                    catch (SQLException sqlE22) {
                        // empty catch block
                    }
                }
                throw throwable;
            }
        }
    }

    @Override
    public int getAutoIncrementIncrement() {
        return this.autoIncrementIncrement;
    }

    @Override
    public boolean lowerCaseTableNames() {
        return this.lowerCaseTableNames;
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        if (sql == null) {
            return null;
        }
        Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, this.serverSupportsConvertFn(), this.getMultiHostSafeProxy());
        if (escapedSqlResult instanceof String) {
            return (String)escapedSqlResult;
        }
        return ((EscapeProcessorResult)escapedSqlResult).escapedSql;
    }

    private CallableStatement parseCallableStatement(String sql) throws SQLException {
        Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, this.serverSupportsConvertFn(), this.getMultiHostSafeProxy());
        boolean isFunctionCall = false;
        String parsedSql = null;
        if (escapedSqlResult instanceof EscapeProcessorResult) {
            parsedSql = ((EscapeProcessorResult)escapedSqlResult).escapedSql;
            isFunctionCall = ((EscapeProcessorResult)escapedSqlResult).callingStoredFunction;
        } else {
            parsedSql = (String)escapedSqlResult;
            isFunctionCall = false;
        }
        return CallableStatement.getInstance(this.getMultiHostSafeProxy(), parsedSql, this.database, isFunctionCall);
    }

    @Override
    public boolean parserKnowsUnicode() {
        return this.parserKnowsUnicode;
    }

    @Override
    public void ping() throws SQLException {
        this.pingInternal(true, 0);
    }

    @Override
    public void pingInternal(boolean checkForClosedConnection, int timeoutMillis) throws SQLException {
        if (checkForClosedConnection) {
            this.checkClosed();
        }
        long pingMillisLifetime = this.getSelfDestructOnPingSecondsLifetime();
        int pingMaxOperations = this.getSelfDestructOnPingMaxOperations();
        if (pingMillisLifetime > 0L && System.currentTimeMillis() - this.connectionCreationTimeMillis > pingMillisLifetime || pingMaxOperations > 0 && pingMaxOperations <= this.io.getCommandCount()) {
            this.close();
            throw SQLError.createSQLException(Messages.getString("Connection.exceededConnectionLifetime"), "08S01", this.getExceptionInterceptor());
        }
        this.io.sendCommand(14, null, null, false, null, timeoutMillis);
    }

    @Override
    public java.sql.CallableStatement prepareCall(String sql) throws SQLException {
        return this.prepareCall(sql, 1003, 1007);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        if (this.versionMeetsMinimum(5, 0, 0)) {
            CallableStatement cStmt = null;
            if (!this.getCacheCallableStatements()) {
                cStmt = this.parseCallableStatement(sql);
            } else {
                LRUCache lRUCache = this.parsedCallableStatementCache;
                synchronized (lRUCache) {
                    CompoundCacheKey key = new CompoundCacheKey(this.getCatalog(), sql);
                    CallableStatement.CallableStatementParamInfo cachedParamInfo = (CallableStatement.CallableStatementParamInfo)this.parsedCallableStatementCache.get(key);
                    if (cachedParamInfo != null) {
                        cStmt = CallableStatement.getInstance(this.getMultiHostSafeProxy(), cachedParamInfo);
                    } else {
                        CallableStatement callableStatement = cStmt = this.parseCallableStatement(sql);
                        synchronized (callableStatement) {
                            cachedParamInfo = cStmt.paramInfo;
                        }
                        this.parsedCallableStatementCache.put(key, cachedParamInfo);
                    }
                }
            }
            cStmt.setResultSetType(resultSetType);
            cStmt.setResultSetConcurrency(resultSetConcurrency);
            return cStmt;
        }
        throw SQLError.createSQLException("Callable statements not supported.", "S1C00", this.getExceptionInterceptor());
    }

    @Override
    public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (this.getPedantic() && resultSetHoldability != 1) {
            throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009", this.getExceptionInterceptor());
        }
        CallableStatement cStmt = (CallableStatement)this.prepareCall(sql, resultSetType, resultSetConcurrency);
        return cStmt;
    }

    @Override
    public java.sql.PreparedStatement prepareStatement(String sql) throws SQLException {
        return this.prepareStatement(sql, 1003, 1007);
    }

    @Override
    public java.sql.PreparedStatement prepareStatement(String sql, int autoGenKeyIndex) throws SQLException {
        java.sql.PreparedStatement pStmt = this.prepareStatement(sql);
        ((PreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyIndex == 1);
        return pStmt;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            String nativeSql;
            this.checkClosed();
            PreparedStatement pStmt = null;
            boolean canServerPrepare = true;
            String string = nativeSql = this.getProcessEscapeCodesForPrepStmts() ? this.nativeSQL(sql) : sql;
            if (this.useServerPreparedStmts && this.getEmulateUnsupportedPstmts()) {
                canServerPrepare = this.canHandleAsServerPreparedStatement(nativeSql);
            }
            if (this.useServerPreparedStmts && canServerPrepare) {
                if (this.getCachePreparedStatements()) {
                    LRUCache lRUCache = this.serverSideStatementCache;
                    synchronized (lRUCache) {
                        pStmt = (ServerPreparedStatement)this.serverSideStatementCache.remove(sql);
                        if (pStmt != null) {
                            ((ServerPreparedStatement)pStmt).setClosed(false);
                            pStmt.clearParameters();
                        }
                        if (pStmt == null) {
                            try {
                                pStmt = ServerPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.database, resultSetType, resultSetConcurrency);
                                if (sql.length() < this.getPreparedStatementCacheSqlLimit()) {
                                    ((ServerPreparedStatement)pStmt).isCached = true;
                                }
                                pStmt.setResultSetType(resultSetType);
                                pStmt.setResultSetConcurrency(resultSetConcurrency);
                            }
                            catch (SQLException sqlEx) {
                                if (this.getEmulateUnsupportedPstmts()) {
                                    pStmt = (PreparedStatement)this.clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
                                    if (sql.length() < this.getPreparedStatementCacheSqlLimit()) {
                                        this.serverSideStatementCheckCache.put(sql, Boolean.FALSE);
                                    }
                                }
                                throw sqlEx;
                            }
                        }
                    }
                }
                try {
                    pStmt = ServerPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.database, resultSetType, resultSetConcurrency);
                    pStmt.setResultSetType(resultSetType);
                    pStmt.setResultSetConcurrency(resultSetConcurrency);
                }
                catch (SQLException sqlEx) {
                    if (this.getEmulateUnsupportedPstmts()) {
                        pStmt = (PreparedStatement)this.clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
                    }
                    throw sqlEx;
                }
            } else {
                pStmt = (PreparedStatement)this.clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
            }
            return pStmt;
        }
    }

    @Override
    public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (this.getPedantic() && resultSetHoldability != 1) {
            throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009", this.getExceptionInterceptor());
        }
        return this.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public java.sql.PreparedStatement prepareStatement(String sql, int[] autoGenKeyIndexes) throws SQLException {
        java.sql.PreparedStatement pStmt = this.prepareStatement(sql);
        ((PreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyIndexes != null && autoGenKeyIndexes.length > 0);
        return pStmt;
    }

    @Override
    public java.sql.PreparedStatement prepareStatement(String sql, String[] autoGenKeyColNames) throws SQLException {
        java.sql.PreparedStatement pStmt = this.prepareStatement(sql);
        ((PreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyColNames != null && autoGenKeyColNames.length > 0);
        return pStmt;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void realClose(boolean calledExplicitly, boolean issueRollback, boolean skipLocalTeardown, Throwable reason) throws SQLException {
        SQLException sqlEx = null;
        if (this.isClosed()) {
            return;
        }
        this.forceClosedReason = reason;
        try {
            if (!skipLocalTeardown) {
                if (!this.getAutoCommit() && issueRollback) {
                    try {
                        this.rollback();
                    }
                    catch (SQLException ex) {
                        sqlEx = ex;
                    }
                }
                this.reportMetrics();
                if (this.getUseUsageAdvisor()) {
                    long connectionLifeTime;
                    if (!calledExplicitly) {
                        String message = "Connection implicitly closed by Driver. You should call Connection.close() from your code to free resources more efficiently and avoid resource leaks.";
                        this.eventSink.consumeEvent(new ProfilerEvent(0, "", this.getCatalog(), this.getId(), -1, -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, message));
                    }
                    if ((connectionLifeTime = System.currentTimeMillis() - this.connectionCreationTimeMillis) < 500L) {
                        String message = "Connection lifetime of < .5 seconds. You might be un-necessarily creating short-lived connections and should investigate connection pooling to be more efficient.";
                        this.eventSink.consumeEvent(new ProfilerEvent(0, "", this.getCatalog(), this.getId(), -1, -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, message));
                    }
                }
                try {
                    this.closeAllOpenStatements();
                }
                catch (SQLException ex) {
                    sqlEx = ex;
                }
                if (this.io != null) {
                    try {
                        this.io.quit();
                    }
                    catch (Exception e) {}
                }
            } else {
                this.io.forceClose();
            }
            if (this.statementInterceptors != null) {
                for (int i = 0; i < this.statementInterceptors.size(); ++i) {
                    this.statementInterceptors.get(i).destroy();
                }
            }
            if (this.exceptionInterceptor != null) {
                this.exceptionInterceptor.destroy();
            }
            Object var10_14 = null;
            this.openStatements.clear();
            if (this.io != null) {
                this.io.releaseResources();
                this.io = null;
            }
            this.statementInterceptors = null;
            this.exceptionInterceptor = null;
        }
        catch (Throwable throwable) {
            Object var10_15 = null;
            this.openStatements.clear();
            if (this.io != null) {
                this.io.releaseResources();
                this.io = null;
            }
            this.statementInterceptors = null;
            this.exceptionInterceptor = null;
            ProfilerEventHandlerFactory.removeInstance(this);
            Object object = this.getConnectionMutex();
            synchronized (object) {
                if (this.cancelTimer != null) {
                    this.cancelTimer.cancel();
                }
            }
            this.isClosed = true;
            throw throwable;
        }
        ProfilerEventHandlerFactory.removeInstance(this);
        Object object = this.getConnectionMutex();
        synchronized (object) {
            if (this.cancelTimer != null) {
                this.cancelTimer.cancel();
            }
        }
        this.isClosed = true;
        if (sqlEx != null) {
            throw sqlEx;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void recachePreparedStatement(ServerPreparedStatement pstmt) throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            if (this.getCachePreparedStatements() && pstmt.isPoolable()) {
                LRUCache lRUCache = this.serverSideStatementCache;
                synchronized (lRUCache) {
                    this.serverSideStatementCache.put(pstmt.originalSql, pstmt);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void decachePreparedStatement(ServerPreparedStatement pstmt) throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            if (this.getCachePreparedStatements() && pstmt.isPoolable()) {
                LRUCache lRUCache = this.serverSideStatementCache;
                synchronized (lRUCache) {
                    this.serverSideStatementCache.remove(pstmt.originalSql);
                }
            }
        }
    }

    @Override
    public void registerQueryExecutionTime(long queryTimeMs) {
        if (queryTimeMs > this.longestQueryTimeMs) {
            this.longestQueryTimeMs = queryTimeMs;
            this.repartitionPerformanceHistogram();
        }
        this.addToPerformanceHistogram(queryTimeMs, 1);
        if (queryTimeMs < this.shortestQueryTimeMs) {
            this.shortestQueryTimeMs = queryTimeMs == 0L ? 1L : queryTimeMs;
        }
        ++this.numberOfQueriesIssued;
        this.totalQueryTimeMs += (double)queryTimeMs;
    }

    @Override
    public void registerStatement(Statement stmt) {
        this.openStatements.addIfAbsent(stmt);
    }

    @Override
    public void releaseSavepoint(Savepoint arg0) throws SQLException {
    }

    private void repartitionHistogram(int[] histCounts, long[] histBreakpoints, long currentLowerBound, long currentUpperBound) {
        if (this.oldHistCounts == null) {
            this.oldHistCounts = new int[histCounts.length];
            this.oldHistBreakpoints = new long[histBreakpoints.length];
        }
        System.arraycopy(histCounts, 0, this.oldHistCounts, 0, histCounts.length);
        System.arraycopy(histBreakpoints, 0, this.oldHistBreakpoints, 0, histBreakpoints.length);
        this.createInitialHistogram(histBreakpoints, currentLowerBound, currentUpperBound);
        for (int i = 0; i < 20; ++i) {
            this.addToHistogram(histCounts, histBreakpoints, this.oldHistBreakpoints[i], this.oldHistCounts[i], currentLowerBound, currentUpperBound);
        }
    }

    private void repartitionPerformanceHistogram() {
        this.checkAndCreatePerformanceHistogram();
        this.repartitionHistogram(this.perfMetricsHistCounts, this.perfMetricsHistBreakpoints, this.shortestQueryTimeMs == Long.MAX_VALUE ? 0L : this.shortestQueryTimeMs, this.longestQueryTimeMs);
    }

    private void repartitionTablesAccessedHistogram() {
        this.checkAndCreateTablesAccessedHistogram();
        this.repartitionHistogram(this.numTablesMetricsHistCounts, this.numTablesMetricsHistBreakpoints, this.minimumNumberTablesAccessed == Long.MAX_VALUE ? 0L : this.minimumNumberTablesAccessed, this.maximumNumberTablesAccessed);
    }

    private void reportMetrics() {
        if (this.getGatherPerformanceMetrics()) {
            int j;
            int numPointsToGraph;
            int i;
            int highestCount;
            int maxNumPoints;
            StringBuilder logMessage = new StringBuilder(256);
            logMessage.append("** Performance Metrics Report **\n");
            logMessage.append("\nLongest reported query: " + this.longestQueryTimeMs + " ms");
            logMessage.append("\nShortest reported query: " + this.shortestQueryTimeMs + " ms");
            logMessage.append("\nAverage query execution time: " + this.totalQueryTimeMs / (double)this.numberOfQueriesIssued + " ms");
            logMessage.append("\nNumber of statements executed: " + this.numberOfQueriesIssued);
            logMessage.append("\nNumber of result sets created: " + this.numberOfResultSetsCreated);
            logMessage.append("\nNumber of statements prepared: " + this.numberOfPrepares);
            logMessage.append("\nNumber of prepared statement executions: " + this.numberOfPreparedExecutes);
            if (this.perfMetricsHistBreakpoints != null) {
                logMessage.append("\n\n\tTiming Histogram:\n");
                maxNumPoints = 20;
                highestCount = Integer.MIN_VALUE;
                for (i = 0; i < 20; ++i) {
                    if (this.perfMetricsHistCounts[i] <= highestCount) continue;
                    highestCount = this.perfMetricsHistCounts[i];
                }
                if (highestCount == 0) {
                    highestCount = 1;
                }
                for (i = 0; i < 19; ++i) {
                    if (i == 0) {
                        logMessage.append("\n\tless than " + this.perfMetricsHistBreakpoints[i + 1] + " ms: \t" + this.perfMetricsHistCounts[i]);
                    } else {
                        logMessage.append("\n\tbetween " + this.perfMetricsHistBreakpoints[i] + " and " + this.perfMetricsHistBreakpoints[i + 1] + " ms: \t" + this.perfMetricsHistCounts[i]);
                    }
                    logMessage.append("\t");
                    numPointsToGraph = (int)((double)maxNumPoints * ((double)this.perfMetricsHistCounts[i] / (double)highestCount));
                    for (j = 0; j < numPointsToGraph; ++j) {
                        logMessage.append("*");
                    }
                    if (this.longestQueryTimeMs < (long)this.perfMetricsHistCounts[i + 1]) break;
                }
                if (this.perfMetricsHistBreakpoints[18] < this.longestQueryTimeMs) {
                    logMessage.append("\n\tbetween ");
                    logMessage.append(this.perfMetricsHistBreakpoints[18]);
                    logMessage.append(" and ");
                    logMessage.append(this.perfMetricsHistBreakpoints[19]);
                    logMessage.append(" ms: \t");
                    logMessage.append(this.perfMetricsHistCounts[19]);
                }
            }
            if (this.numTablesMetricsHistBreakpoints != null) {
                logMessage.append("\n\n\tTable Join Histogram:\n");
                maxNumPoints = 20;
                highestCount = Integer.MIN_VALUE;
                for (i = 0; i < 20; ++i) {
                    if (this.numTablesMetricsHistCounts[i] <= highestCount) continue;
                    highestCount = this.numTablesMetricsHistCounts[i];
                }
                if (highestCount == 0) {
                    highestCount = 1;
                }
                for (i = 0; i < 19; ++i) {
                    if (i == 0) {
                        logMessage.append("\n\t" + this.numTablesMetricsHistBreakpoints[i + 1] + " tables or less: \t\t" + this.numTablesMetricsHistCounts[i]);
                    } else {
                        logMessage.append("\n\tbetween " + this.numTablesMetricsHistBreakpoints[i] + " and " + this.numTablesMetricsHistBreakpoints[i + 1] + " tables: \t" + this.numTablesMetricsHistCounts[i]);
                    }
                    logMessage.append("\t");
                    numPointsToGraph = (int)((double)maxNumPoints * ((double)this.numTablesMetricsHistCounts[i] / (double)highestCount));
                    for (j = 0; j < numPointsToGraph; ++j) {
                        logMessage.append("*");
                    }
                    if (this.maximumNumberTablesAccessed < this.numTablesMetricsHistBreakpoints[i + 1]) break;
                }
                if (this.numTablesMetricsHistBreakpoints[18] < this.maximumNumberTablesAccessed) {
                    logMessage.append("\n\tbetween ");
                    logMessage.append(this.numTablesMetricsHistBreakpoints[18]);
                    logMessage.append(" and ");
                    logMessage.append(this.numTablesMetricsHistBreakpoints[19]);
                    logMessage.append(" tables: ");
                    logMessage.append(this.numTablesMetricsHistCounts[19]);
                }
            }
            this.log.logInfo(logMessage);
            this.metricsLastReportedMs = System.currentTimeMillis();
        }
    }

    protected void reportMetricsIfNeeded() {
        if (this.getGatherPerformanceMetrics() && System.currentTimeMillis() - this.metricsLastReportedMs > (long)this.getReportMetricsIntervalMillis()) {
            this.reportMetrics();
        }
    }

    @Override
    public void reportNumberOfTablesAccessed(int numTablesAccessed) {
        if ((long)numTablesAccessed < this.minimumNumberTablesAccessed) {
            this.minimumNumberTablesAccessed = numTablesAccessed;
        }
        if ((long)numTablesAccessed > this.maximumNumberTablesAccessed) {
            this.maximumNumberTablesAccessed = numTablesAccessed;
            this.repartitionTablesAccessedHistogram();
        }
        this.addToTablesAccessedHistogram(numTablesAccessed, 1);
    }

    @Override
    public void resetServerState() throws SQLException {
        if (!this.getParanoid() && this.io != null && this.versionMeetsMinimum(4, 0, 6)) {
            this.changeUser(this.user, this.password);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void rollback() throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            block12: {
                block11: {
                    this.checkClosed();
                    try {
                        try {
                            if (this.connectionLifecycleInterceptors != null) {
                                IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()){

                                    @Override
                                    void forEach(Extension each) throws SQLException {
                                        if (!((ConnectionLifecycleInterceptor)each).rollback()) {
                                            this.stopIterating = true;
                                        }
                                    }
                                };
                                iter.doForAll();
                                if (!iter.fullIteration()) {
                                    Object var4_5 = null;
                                    break block11;
                                }
                            }
                            if (this.autoCommit && !this.getRelaxAutoCommit()) {
                                throw SQLError.createSQLException("Can't call rollback when autocommit=true", "08003", this.getExceptionInterceptor());
                            }
                            if (!this.transactionsSupported) break block12;
                            try {
                                this.rollbackNoChecks();
                                break block12;
                            }
                            catch (SQLException sqlEx) {
                                if (!this.getIgnoreNonTxTables()) throw sqlEx;
                                if (sqlEx.getErrorCode() != 1196) throw sqlEx;
                                Object var4_6 = null;
                                this.needsPing = this.getReconnectAtTxEnd();
                                return;
                            }
                        }
                        catch (SQLException sqlException) {
                            if (!"08S01".equals(sqlException.getSQLState())) throw sqlException;
                            throw SQLError.createSQLException("Communications link failure during rollback(). Transaction resolution unknown.", "08007", this.getExceptionInterceptor());
                        }
                    }
                    catch (Throwable throwable) {
                        Object var4_8 = null;
                        this.needsPing = this.getReconnectAtTxEnd();
                        throw throwable;
                    }
                }
                this.needsPing = this.getReconnectAtTxEnd();
                return;
            }
            Object var4_7 = null;
            this.needsPing = this.getReconnectAtTxEnd();
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void rollback(final Savepoint savepoint) throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            block14: {
                block13: {
                    if (!this.versionMeetsMinimum(4, 0, 14)) {
                        if (!this.versionMeetsMinimum(4, 1, 1)) throw SQLError.createSQLFeatureNotSupportedException();
                    }
                    this.checkClosed();
                    try {
                        if (this.connectionLifecycleInterceptors != null) {
                            IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()){

                                @Override
                                void forEach(Extension each) throws SQLException {
                                    if (!((ConnectionLifecycleInterceptor)each).rollback(savepoint)) {
                                        this.stopIterating = true;
                                    }
                                }
                            };
                            iter.doForAll();
                            if (!iter.fullIteration()) {
                                Object var12_4 = null;
                                break block13;
                            }
                        }
                        StringBuilder rollbackQuery = new StringBuilder("ROLLBACK TO SAVEPOINT ");
                        rollbackQuery.append('`');
                        rollbackQuery.append(savepoint.getSavepointName());
                        rollbackQuery.append('`');
                        java.sql.Statement stmt = null;
                        try {
                            try {
                                stmt = this.getMetadataSafeStatement();
                                stmt.executeUpdate(rollbackQuery.toString());
                            }
                            catch (SQLException sqlEx) {
                                int indexOfError153;
                                String msg;
                                int errno = sqlEx.getErrorCode();
                                if (errno == 1181 && (msg = sqlEx.getMessage()) != null && (indexOfError153 = msg.indexOf("153")) != -1) {
                                    throw SQLError.createSQLException("Savepoint '" + savepoint.getSavepointName() + "' does not exist", "S1009", errno, this.getExceptionInterceptor());
                                }
                                if (this.getIgnoreNonTxTables() && sqlEx.getErrorCode() != 1196) {
                                    throw sqlEx;
                                }
                                if (!"08S01".equals(sqlEx.getSQLState())) throw sqlEx;
                                throw SQLError.createSQLException("Communications link failure during rollback(). Transaction resolution unknown.", "08007", this.getExceptionInterceptor());
                            }
                            Object var10_8 = null;
                            this.closeStatement(stmt);
                            break block14;
                        }
                        catch (Throwable throwable) {
                            Object var10_9 = null;
                            this.closeStatement(stmt);
                            throw throwable;
                        }
                    }
                    catch (Throwable throwable) {
                        Object var12_6 = null;
                        this.needsPing = this.getReconnectAtTxEnd();
                        throw throwable;
                    }
                }
                this.needsPing = this.getReconnectAtTxEnd();
                return;
            }
            Object var12_5 = null;
            this.needsPing = this.getReconnectAtTxEnd();
            return;
        }
    }

    private void rollbackNoChecks() throws SQLException {
        if (this.getUseLocalTransactionState() && this.versionMeetsMinimum(5, 0, 0) && !this.io.inTransactionOnServer()) {
            return;
        }
        this.execSQL(null, "rollback", -1, null, 1003, 1007, false, this.database, null, false);
    }

    @Override
    public java.sql.PreparedStatement serverPrepareStatement(String sql) throws SQLException {
        String nativeSql = this.getProcessEscapeCodesForPrepStmts() ? this.nativeSQL(sql) : sql;
        return ServerPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.getCatalog(), 1003, 1007);
    }

    @Override
    public java.sql.PreparedStatement serverPrepareStatement(String sql, int autoGenKeyIndex) throws SQLException {
        String nativeSql = this.getProcessEscapeCodesForPrepStmts() ? this.nativeSQL(sql) : sql;
        ServerPreparedStatement pStmt = ServerPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.getCatalog(), 1003, 1007);
        pStmt.setRetrieveGeneratedKeys(autoGenKeyIndex == 1);
        return pStmt;
    }

    @Override
    public java.sql.PreparedStatement serverPrepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        String nativeSql = this.getProcessEscapeCodesForPrepStmts() ? this.nativeSQL(sql) : sql;
        return ServerPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.getCatalog(), resultSetType, resultSetConcurrency);
    }

    @Override
    public java.sql.PreparedStatement serverPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (this.getPedantic() && resultSetHoldability != 1) {
            throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009", this.getExceptionInterceptor());
        }
        return this.serverPrepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public java.sql.PreparedStatement serverPrepareStatement(String sql, int[] autoGenKeyIndexes) throws SQLException {
        PreparedStatement pStmt = (PreparedStatement)this.serverPrepareStatement(sql);
        pStmt.setRetrieveGeneratedKeys(autoGenKeyIndexes != null && autoGenKeyIndexes.length > 0);
        return pStmt;
    }

    @Override
    public java.sql.PreparedStatement serverPrepareStatement(String sql, String[] autoGenKeyColNames) throws SQLException {
        PreparedStatement pStmt = (PreparedStatement)this.serverPrepareStatement(sql);
        pStmt.setRetrieveGeneratedKeys(autoGenKeyColNames != null && autoGenKeyColNames.length > 0);
        return pStmt;
    }

    @Override
    public boolean serverSupportsConvertFn() throws SQLException {
        return this.versionMeetsMinimum(4, 0, 2);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setAutoCommit(final boolean autoCommitFlag) throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            this.checkClosed();
            if (this.connectionLifecycleInterceptors != null) {
                IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()){

                    @Override
                    void forEach(Extension each) throws SQLException {
                        if (!((ConnectionLifecycleInterceptor)each).setAutoCommit(autoCommitFlag)) {
                            this.stopIterating = true;
                        }
                    }
                };
                iter.doForAll();
                if (!iter.fullIteration()) {
                    return;
                }
            }
            if (this.getAutoReconnectForPools()) {
                this.setHighAvailability(true);
            }
            try {
                if (this.transactionsSupported) {
                    boolean needsSetOnServer = true;
                    if (this.getUseLocalSessionState() && this.autoCommit == autoCommitFlag) {
                        needsSetOnServer = false;
                    } else if (!this.getHighAvailability()) {
                        needsSetOnServer = this.getIO().isSetNeededForAutoCommitMode(autoCommitFlag);
                    }
                    this.autoCommit = autoCommitFlag;
                    if (needsSetOnServer) {
                        this.execSQL(null, autoCommitFlag ? "SET autocommit=1" : "SET autocommit=0", -1, null, 1003, 1007, false, this.database, null, false);
                    }
                } else {
                    if (!autoCommitFlag && !this.getRelaxAutoCommit()) {
                        throw SQLError.createSQLException("MySQL Versions Older than 3.23.15 do not support transactions", "08003", this.getExceptionInterceptor());
                    }
                    this.autoCommit = autoCommitFlag;
                }
                Object var5_5 = null;
            }
            catch (Throwable throwable) {
                Object var5_6 = null;
                if (this.getAutoReconnectForPools()) {
                    this.setHighAvailability(false);
                }
                throw throwable;
            }
            if (this.getAutoReconnectForPools()) {
                this.setHighAvailability(false);
            }
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setCatalog(final String catalog) throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            String quotedId;
            this.checkClosed();
            if (catalog == null) {
                throw SQLError.createSQLException("Catalog can not be null", "S1009", this.getExceptionInterceptor());
            }
            if (this.connectionLifecycleInterceptors != null) {
                IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()){

                    @Override
                    void forEach(Extension each) throws SQLException {
                        if (!((ConnectionLifecycleInterceptor)each).setCatalog(catalog)) {
                            this.stopIterating = true;
                        }
                    }
                };
                iter.doForAll();
                if (!iter.fullIteration()) {
                    return;
                }
            }
            if (this.getUseLocalSessionState()) {
                if (this.lowerCaseTableNames) {
                    if (this.database.equalsIgnoreCase(catalog)) {
                        return;
                    }
                } else if (this.database.equals(catalog)) {
                    return;
                }
            }
            if ((quotedId = this.dbmd.getIdentifierQuoteString()) == null || quotedId.equals(" ")) {
                quotedId = "";
            }
            StringBuilder query = new StringBuilder("USE ");
            query.append(StringUtils.quoteIdentifier(catalog, quotedId, this.getPedantic()));
            this.execSQL(null, query.toString(), -1, null, 1003, 1007, false, this.database, null, false);
            this.database = catalog;
        }
    }

    @Override
    public void setFailedOver(boolean flag) {
    }

    @Override
    public void setHoldability(int arg0) throws SQLException {
    }

    @Override
    public void setInGlobalTx(boolean flag) {
        this.isInGlobalTx = flag;
    }

    @Override
    @Deprecated
    public void setPreferSlaveDuringFailover(boolean flag) {
    }

    @Override
    public void setReadInfoMsgEnabled(boolean flag) {
        this.readInfoMsg = flag;
    }

    @Override
    public void setReadOnly(boolean readOnlyFlag) throws SQLException {
        this.checkClosed();
        this.setReadOnlyInternal(readOnlyFlag);
    }

    @Override
    public void setReadOnlyInternal(boolean readOnlyFlag) throws SQLException {
        if (this.getReadOnlyPropagatesToServer() && this.versionMeetsMinimum(5, 6, 5) && (!this.getUseLocalSessionState() || readOnlyFlag != this.readOnly)) {
            this.execSQL(null, "set session transaction " + (readOnlyFlag ? "read only" : "read write"), -1, null, 1003, 1007, false, this.database, null, false);
        }
        this.readOnly = readOnlyFlag;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        MysqlSavepoint savepoint = new MysqlSavepoint(this.getExceptionInterceptor());
        this.setSavepoint(savepoint);
        return savepoint;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setSavepoint(MysqlSavepoint savepoint) throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            if (this.versionMeetsMinimum(4, 0, 14) || this.versionMeetsMinimum(4, 1, 1)) {
                this.checkClosed();
                StringBuilder savePointQuery = new StringBuilder("SAVEPOINT ");
                savePointQuery.append('`');
                savePointQuery.append(savepoint.getSavepointName());
                savePointQuery.append('`');
                java.sql.Statement stmt = null;
                try {
                    stmt = this.getMetadataSafeStatement();
                    stmt.executeUpdate(savePointQuery.toString());
                    Object var6_5 = null;
                    this.closeStatement(stmt);
                }
                catch (Throwable throwable) {
                    Object var6_6 = null;
                    this.closeStatement(stmt);
                    throw throwable;
                }
            } else {
                throw SQLError.createSQLFeatureNotSupportedException();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            MysqlSavepoint savepoint = new MysqlSavepoint(name, this.getExceptionInterceptor());
            this.setSavepoint(savepoint);
            return savepoint;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void setSessionVariables() throws SQLException {
        if (!this.versionMeetsMinimum(4, 0, 0) || this.getSessionVariables() == null) return;
        List<String> variablesToSet = StringUtils.split(this.getSessionVariables(), ",", "\"'", "\"'", false);
        int numVariablesToSet = variablesToSet.size();
        java.sql.Statement stmt = null;
        try {
            stmt = this.getMetadataSafeStatement();
            for (int i = 0; i < numVariablesToSet; ++i) {
                String variableValuePair = variablesToSet.get(i);
                if (variableValuePair.startsWith("@")) {
                    stmt.executeUpdate("SET " + variableValuePair);
                    continue;
                }
                stmt.executeUpdate("SET SESSION " + variableValuePair);
            }
            Object var7_6 = null;
            if (stmt == null) return;
        }
        catch (Throwable throwable) {
            Object var7_7 = null;
            if (stmt == null) throw throwable;
            stmt.close();
            throw throwable;
        }
        stmt.close();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            this.checkClosed();
            if (this.hasIsolationLevels) {
                String sql = null;
                boolean shouldSendSet = false;
                if (this.getAlwaysSendSetIsolation()) {
                    shouldSendSet = true;
                } else if (level != this.isolationLevel) {
                    shouldSendSet = true;
                }
                if (this.getUseLocalSessionState()) {
                    boolean bl = shouldSendSet = this.isolationLevel != level;
                }
                if (shouldSendSet) {
                    switch (level) {
                        case 0: {
                            throw SQLError.createSQLException("Transaction isolation level NONE not supported by MySQL", this.getExceptionInterceptor());
                        }
                        case 2: {
                            sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED";
                            break;
                        }
                        case 1: {
                            sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";
                            break;
                        }
                        case 4: {
                            sql = "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ";
                            break;
                        }
                        case 8: {
                            sql = "SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE";
                            break;
                        }
                        default: {
                            throw SQLError.createSQLException("Unsupported transaction isolation level '" + level + "'", "S1C00", this.getExceptionInterceptor());
                        }
                    }
                    this.execSQL(null, sql, -1, null, 1003, 1007, false, this.database, null, false);
                    this.isolationLevel = level;
                }
            } else {
                throw SQLError.createSQLException("Transaction Isolation Levels are not supported on MySQL versions older than 3.23.36.", "S1C00", this.getExceptionInterceptor());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            this.typeMap = map;
        }
    }

    private void setupServerForTruncationChecks() throws SQLException {
        if (this.getJdbcCompliantTruncation() && this.versionMeetsMinimum(5, 0, 2)) {
            boolean strictTransTablesIsSet;
            String currentSqlMode = this.serverVariables.get("sql_mode");
            boolean bl = strictTransTablesIsSet = StringUtils.indexOfIgnoreCase(currentSqlMode, "STRICT_TRANS_TABLES") != -1;
            if (currentSqlMode == null || currentSqlMode.length() == 0 || !strictTransTablesIsSet) {
                StringBuilder commandBuf = new StringBuilder("SET sql_mode='");
                if (currentSqlMode != null && currentSqlMode.length() > 0) {
                    commandBuf.append(currentSqlMode);
                    commandBuf.append(",");
                }
                commandBuf.append("STRICT_TRANS_TABLES'");
                this.execSQL(null, commandBuf.toString(), -1, null, 1003, 1007, false, this.database, null, false);
                this.setJdbcCompliantTruncation(false);
            } else if (strictTransTablesIsSet) {
                this.setJdbcCompliantTruncation(false);
            }
        }
    }

    @Override
    public void shutdownServer() throws SQLException {
        try {
            this.io.sendCommand(8, null, null, false, null, 0);
        }
        catch (Exception ex) {
            SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.UnhandledExceptionDuringShutdown"), "S1000", this.getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }

    @Override
    public boolean supportsIsolationLevel() {
        return this.hasIsolationLevels;
    }

    @Override
    public boolean supportsQuotedIdentifiers() {
        return this.hasQuotedIdentifiers;
    }

    @Override
    public boolean supportsTransactions() {
        return this.transactionsSupported;
    }

    @Override
    public void unregisterStatement(Statement stmt) {
        this.openStatements.remove(stmt);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean useAnsiQuotedIdentifiers() {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            return this.useAnsiQuotes;
        }
    }

    @Override
    public boolean versionMeetsMinimum(int major, int minor, int subminor) throws SQLException {
        this.checkClosed();
        return this.io.versionMeetsMinimum(major, minor, subminor);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public CachedResultSetMetaData getCachedMetaData(String sql) {
        if (this.resultSetMetadataCache != null) {
            LRUCache lRUCache = this.resultSetMetadataCache;
            synchronized (lRUCache) {
                return (CachedResultSetMetaData)this.resultSetMetadataCache.get(sql);
            }
        }
        return null;
    }

    @Override
    public void initializeResultsMetadataFromCache(String sql, CachedResultSetMetaData cachedMetaData, ResultSetInternalMethods resultSet) throws SQLException {
        if (cachedMetaData == null) {
            cachedMetaData = new CachedResultSetMetaData();
            resultSet.buildIndexMapping();
            resultSet.initializeWithMetadata();
            if (resultSet instanceof UpdatableResultSet) {
                ((UpdatableResultSet)resultSet).checkUpdatability();
            }
            resultSet.populateCachedMetaData(cachedMetaData);
            this.resultSetMetadataCache.put(sql, cachedMetaData);
        } else {
            resultSet.initializeFromCachedMetaData(cachedMetaData);
            resultSet.initializeWithMetadata();
            if (resultSet instanceof UpdatableResultSet) {
                ((UpdatableResultSet)resultSet).checkUpdatability();
            }
        }
    }

    @Override
    public String getStatementComment() {
        return this.statementComment;
    }

    @Override
    public void setStatementComment(String comment) {
        this.statementComment = comment;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void reportQueryTime(long millisOrNanos) {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            ++this.queryTimeCount;
            this.queryTimeSum += (double)millisOrNanos;
            this.queryTimeSumSquares += (double)(millisOrNanos * millisOrNanos);
            this.queryTimeMean = (this.queryTimeMean * (double)(this.queryTimeCount - 1L) + (double)millisOrNanos) / (double)this.queryTimeCount;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isAbonormallyLongQuery(long millisOrNanos) {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            if (this.queryTimeCount < 15L) {
                return false;
            }
            double stddev = Math.sqrt((this.queryTimeSumSquares - this.queryTimeSum * this.queryTimeSum / (double)this.queryTimeCount) / (double)(this.queryTimeCount - 1L));
            boolean bl = (double)millisOrNanos > this.queryTimeMean + 5.0 * stddev;
            return bl;
        }
    }

    @Override
    public void initializeExtension(Extension ex) throws SQLException {
        ex.init(this, this.props);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void transactionBegun() throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            if (this.connectionLifecycleInterceptors != null) {
                IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()){

                    @Override
                    void forEach(Extension each) throws SQLException {
                        ((ConnectionLifecycleInterceptor)each).transactionBegun();
                    }
                };
                iter.doForAll();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void transactionCompleted() throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            if (this.connectionLifecycleInterceptors != null) {
                IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()){

                    @Override
                    void forEach(Extension each) throws SQLException {
                        ((ConnectionLifecycleInterceptor)each).transactionCompleted();
                    }
                };
                iter.doForAll();
            }
        }
    }

    @Override
    public boolean storesLowerCaseTableName() {
        return this.storesLowerCaseTableName;
    }

    @Override
    public ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }

    @Override
    public boolean getRequiresEscapingEncoder() {
        return this.requiresEscapingEncoder;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isServerLocal() throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            SocketFactory factory = this.getIO().socketFactory;
            if (factory instanceof SocketMetadata) {
                return ((SocketMetadata)((Object)factory)).isLocallyConnected(this);
            }
            this.getLog().logWarn(Messages.getString("Connection.NoMetadataOnSocketFactory"));
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getSessionMaxRows() {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            return this.sessionMaxRows;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setSessionMaxRows(int max) throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            if (this.sessionMaxRows != max) {
                this.sessionMaxRows = max;
                this.execSQL(null, "SET SQL_SELECT_LIMIT=" + (this.sessionMaxRows == -1 ? "DEFAULT" : Integer.valueOf(this.sessionMaxRows)), -1, null, 1003, 1007, false, this.database, null, false);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setSchema(String schema) throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            this.checkClosed();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getSchema() throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            this.checkClosed();
            return null;
        }
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkPermission(ABORT_PERM);
        }
        if (executor == null) {
            throw SQLError.createSQLException("Executor can not be null", "S1009", this.getExceptionInterceptor());
        }
        executor.execute(new Runnable(){

            public void run() {
                try {
                    ConnectionImpl.this.abortInternal();
                }
                catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setNetworkTimeout(Executor executor, final int milliseconds) throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            SecurityManager sec = System.getSecurityManager();
            if (sec != null) {
                sec.checkPermission(SET_NETWORK_TIMEOUT_PERM);
            }
            if (executor == null) {
                throw SQLError.createSQLException("Executor can not be null", "S1009", this.getExceptionInterceptor());
            }
            this.checkClosed();
            final MysqlIO mysqlIo = this.io;
            executor.execute(new Runnable(){

                public void run() {
                    try {
                        ConnectionImpl.this.setSocketTimeout(milliseconds);
                        mysqlIo.setSocketTimeout(milliseconds);
                    }
                    catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getNetworkTimeout() throws SQLException {
        Object object = this.getConnectionMutex();
        synchronized (object) {
            this.checkClosed();
            return this.getSocketTimeout();
        }
    }

    @Override
    public ProfilerEventHandler getProfilerEventHandlerInstance() {
        return this.eventSink;
    }

    @Override
    public void setProfilerEventHandlerInstance(ProfilerEventHandler h) {
        this.eventSink = h;
    }

    static {
        mapTransIsolationNameToValue = null;
        NULL_LOGGER = new NullLogger(LOGGER_INSTANCE_NAME);
        dynamicIndexToCollationMapByUrl = new HashMap<String, Map<Number, String>>();
        dynamicIndexToCharsetMapByUrl = new HashMap<String, Map<Integer, String>>();
        customIndexToCharsetMapByUrl = new HashMap<String, Map<Integer, String>>();
        customCharsetToMblenMapByUrl = new HashMap<String, Map<String, Integer>>();
        mapTransIsolationNameToValue = new HashMap<String, Integer>(8);
        mapTransIsolationNameToValue.put("READ-UNCOMMITED", 1);
        mapTransIsolationNameToValue.put("READ-UNCOMMITTED", 1);
        mapTransIsolationNameToValue.put("READ-COMMITTED", 2);
        mapTransIsolationNameToValue.put("REPEATABLE-READ", 4);
        mapTransIsolationNameToValue.put("SERIALIZABLE", 8);
        if (Util.isJdbc4()) {
            try {
                JDBC_4_CONNECTION_CTOR = Class.forName("com.mysql.jdbc.JDBC4Connection").getConstructor(String.class, Integer.TYPE, Properties.class, String.class, String.class);
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
            JDBC_4_CONNECTION_CTOR = null;
        }
        random = new Random();
    }

    static class CompoundCacheKey {
        String componentOne;
        String componentTwo;
        int hashCode;

        CompoundCacheKey(String partOne, String partTwo) {
            this.componentOne = partOne;
            this.componentTwo = partTwo;
            this.hashCode = ((this.componentOne != null ? this.componentOne : "") + this.componentTwo).hashCode();
        }

        public boolean equals(Object obj) {
            if (obj instanceof CompoundCacheKey) {
                CompoundCacheKey another = (CompoundCacheKey)obj;
                boolean firstPartEqual = false;
                firstPartEqual = this.componentOne == null ? another.componentOne == null : this.componentOne.equals(another.componentOne);
                return firstPartEqual && this.componentTwo.equals(another.componentTwo);
            }
            return false;
        }

        public int hashCode() {
            return this.hashCode;
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public class ExceptionInterceptorChain
    implements ExceptionInterceptor {
        private List<Extension> interceptors;

        ExceptionInterceptorChain(String interceptorClasses) throws SQLException {
            this.interceptors = Util.loadExtensions(ConnectionImpl.this, ConnectionImpl.this.props, interceptorClasses, "Connection.BadExceptionInterceptor", this);
        }

        void addRingZero(ExceptionInterceptor interceptor) throws SQLException {
            this.interceptors.add(0, interceptor);
        }

        @Override
        public SQLException interceptException(SQLException sqlEx, Connection conn) {
            if (this.interceptors != null) {
                Iterator<Extension> iter = this.interceptors.iterator();
                while (iter.hasNext()) {
                    sqlEx = ((ExceptionInterceptor)iter.next()).interceptException(sqlEx, ConnectionImpl.this);
                }
            }
            return sqlEx;
        }

        @Override
        public void destroy() {
            if (this.interceptors != null) {
                Iterator<Extension> iter = this.interceptors.iterator();
                while (iter.hasNext()) {
                    ((ExceptionInterceptor)iter.next()).destroy();
                }
            }
        }

        @Override
        public void init(Connection conn, Properties properties) throws SQLException {
            if (this.interceptors != null) {
                Iterator<Extension> iter = this.interceptors.iterator();
                while (iter.hasNext()) {
                    ((ExceptionInterceptor)iter.next()).init(conn, properties);
                }
            }
        }

        public List<Extension> getInterceptors() {
            return this.interceptors;
        }
    }
}


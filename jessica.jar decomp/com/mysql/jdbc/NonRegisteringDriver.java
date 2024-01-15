/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.AbandonedConnectionCleanupThread;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.ConnectionPropertiesImpl;
import com.mysql.jdbc.ConnectionPropertiesTransform;
import com.mysql.jdbc.FailoverConnectionProxy;
import com.mysql.jdbc.LoadBalancedConnectionProxy;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.NetworkResources;
import com.mysql.jdbc.ReplicationConnectionProxy;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.StringUtils;
import com.mysql.jdbc.Util;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.net.URLDecoder;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class NonRegisteringDriver
implements Driver {
    private static final String ALLOWED_QUOTES = "\"'";
    private static final String REPLICATION_URL_PREFIX = "jdbc:mysql:replication://";
    private static final String URL_PREFIX = "jdbc:mysql://";
    private static final String MXJ_URL_PREFIX = "jdbc:mysql:mxj://";
    public static final String LOADBALANCE_URL_PREFIX = "jdbc:mysql:loadbalance://";
    protected static final ConcurrentHashMap<ConnectionPhantomReference, ConnectionPhantomReference> connectionPhantomRefs = new ConcurrentHashMap();
    protected static final ReferenceQueue<ConnectionImpl> refQueue = new ReferenceQueue();
    public static final String OS = NonRegisteringDriver.getOSName();
    public static final String PLATFORM = NonRegisteringDriver.getPlatform();
    public static final String LICENSE = "GPL";
    public static final String RUNTIME_VENDOR = System.getProperty("java.vendor");
    public static final String RUNTIME_VERSION = System.getProperty("java.version");
    public static final String VERSION = "5.1.40";
    public static final String NAME = "MySQL Connector Java";
    public static final String DBNAME_PROPERTY_KEY = "DBNAME";
    public static final boolean DEBUG = false;
    public static final int HOST_NAME_INDEX = 0;
    public static final String HOST_PROPERTY_KEY = "HOST";
    public static final String NUM_HOSTS_PROPERTY_KEY = "NUM_HOSTS";
    public static final String PASSWORD_PROPERTY_KEY = "password";
    public static final int PORT_NUMBER_INDEX = 1;
    public static final String PORT_PROPERTY_KEY = "PORT";
    public static final String PROPERTIES_TRANSFORM_KEY = "propertiesTransform";
    public static final boolean TRACE = false;
    public static final String USE_CONFIG_PROPERTY_KEY = "useConfigs";
    public static final String USER_PROPERTY_KEY = "user";
    public static final String PROTOCOL_PROPERTY_KEY = "PROTOCOL";
    public static final String PATH_PROPERTY_KEY = "PATH";

    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static String getPlatform() {
        return System.getProperty("os.arch");
    }

    static int getMajorVersionInternal() {
        return NonRegisteringDriver.safeIntParse("5");
    }

    static int getMinorVersionInternal() {
        return NonRegisteringDriver.safeIntParse("1");
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected static String[] parseHostPortPair(String hostPortPair) throws SQLException {
        String[] splitValues = new String[2];
        if (StringUtils.startsWithIgnoreCaseAndWs(hostPortPair, "address=")) {
            splitValues[0] = hostPortPair.trim();
            splitValues[1] = null;
            return splitValues;
        }
        int portIndex = hostPortPair.indexOf(":");
        String hostname = null;
        if (portIndex != -1) {
            if (portIndex + 1 >= hostPortPair.length()) throw SQLError.createSQLException(Messages.getString("NonRegisteringDriver.37"), "01S00", null);
            String portAsString = hostPortPair.substring(portIndex + 1);
            splitValues[0] = hostname = hostPortPair.substring(0, portIndex);
            splitValues[1] = portAsString;
            return splitValues;
        } else {
            splitValues[0] = hostPortPair;
            splitValues[1] = null;
        }
        return splitValues;
    }

    private static int safeIntParse(String intAsString) {
        try {
            return Integer.parseInt(intAsString);
        }
        catch (NumberFormatException nfe) {
            return 0;
        }
    }

    public boolean acceptsURL(String url) throws SQLException {
        if (url == null) {
            throw SQLError.createSQLException(Messages.getString("NonRegisteringDriver.1"), "08001", null);
        }
        return this.parseURL(url, null) != null;
    }

    public java.sql.Connection connect(String url, Properties info) throws SQLException {
        if (url == null) {
            throw SQLError.createSQLException(Messages.getString("NonRegisteringDriver.1"), "08001", null);
        }
        if (StringUtils.startsWithIgnoreCase(url, LOADBALANCE_URL_PREFIX)) {
            return this.connectLoadBalanced(url, info);
        }
        if (StringUtils.startsWithIgnoreCase(url, REPLICATION_URL_PREFIX)) {
            return this.connectReplicationConnection(url, info);
        }
        Properties props = null;
        props = this.parseURL(url, info);
        if (props == null) {
            return null;
        }
        if (!"1".equals(props.getProperty(NUM_HOSTS_PROPERTY_KEY))) {
            return this.connectFailover(url, info);
        }
        try {
            Connection newConn = ConnectionImpl.getInstance(this.host(props), this.port(props), props, this.database(props), url);
            return newConn;
        }
        catch (SQLException sqlEx) {
            throw sqlEx;
        }
        catch (Exception ex) {
            SQLException sqlEx = SQLError.createSQLException(Messages.getString("NonRegisteringDriver.17") + ex.toString() + Messages.getString("NonRegisteringDriver.18"), "08001", null);
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }

    protected static void trackConnection(Connection newConn) {
        ConnectionPhantomReference phantomRef = new ConnectionPhantomReference((ConnectionImpl)newConn, refQueue);
        connectionPhantomRefs.put(phantomRef, phantomRef);
    }

    private java.sql.Connection connectLoadBalanced(String url, Properties info) throws SQLException {
        Properties parsedProps = this.parseURL(url, info);
        if (parsedProps == null) {
            return null;
        }
        parsedProps.remove("roundRobinLoadBalance");
        int numHosts = Integer.parseInt(parsedProps.getProperty(NUM_HOSTS_PROPERTY_KEY));
        ArrayList<String> hostList = new ArrayList<String>();
        for (int i = 0; i < numHosts; ++i) {
            int index = i + 1;
            hostList.add(parsedProps.getProperty("HOST." + index) + ":" + parsedProps.getProperty("PORT." + index));
        }
        return LoadBalancedConnectionProxy.createProxyInstance(hostList, parsedProps);
    }

    private java.sql.Connection connectFailover(String url, Properties info) throws SQLException {
        Properties parsedProps = this.parseURL(url, info);
        if (parsedProps == null) {
            return null;
        }
        parsedProps.remove("roundRobinLoadBalance");
        int numHosts = Integer.parseInt(parsedProps.getProperty(NUM_HOSTS_PROPERTY_KEY));
        ArrayList<String> hostList = new ArrayList<String>();
        for (int i = 0; i < numHosts; ++i) {
            int index = i + 1;
            hostList.add(parsedProps.getProperty("HOST." + index) + ":" + parsedProps.getProperty("PORT." + index));
        }
        return FailoverConnectionProxy.createProxyInstance(hostList, parsedProps);
    }

    protected java.sql.Connection connectReplicationConnection(String url, Properties info) throws SQLException {
        Properties parsedProps = this.parseURL(url, info);
        if (parsedProps == null) {
            return null;
        }
        Properties masterProps = (Properties)parsedProps.clone();
        Properties slavesProps = (Properties)parsedProps.clone();
        slavesProps.setProperty("com.mysql.jdbc.ReplicationConnection.isSlave", "true");
        int numHosts = Integer.parseInt(parsedProps.getProperty(NUM_HOSTS_PROPERTY_KEY));
        if (numHosts < 2) {
            throw SQLError.createSQLException("Must specify at least one slave host to connect to for master/slave replication load-balancing functionality", "01S00", null);
        }
        ArrayList<String> slaveHostList = new ArrayList<String>();
        ArrayList<String> masterHostList = new ArrayList<String>();
        String firstHost = masterProps.getProperty("HOST.1") + ":" + masterProps.getProperty("PORT.1");
        boolean usesExplicitServerType = NonRegisteringDriver.isHostPropertiesList(firstHost);
        for (int i = 0; i < numHosts; ++i) {
            int index = i + 1;
            masterProps.remove("HOST." + index);
            masterProps.remove("PORT." + index);
            slavesProps.remove("HOST." + index);
            slavesProps.remove("PORT." + index);
            String host = parsedProps.getProperty("HOST." + index);
            String port = parsedProps.getProperty("PORT." + index);
            if (usesExplicitServerType) {
                if (this.isHostMaster(host)) {
                    masterHostList.add(host);
                    continue;
                }
                slaveHostList.add(host);
                continue;
            }
            if (i == 0) {
                masterHostList.add(host + ":" + port);
                continue;
            }
            slaveHostList.add(host + ":" + port);
        }
        slavesProps.remove(NUM_HOSTS_PROPERTY_KEY);
        masterProps.remove(NUM_HOSTS_PROPERTY_KEY);
        masterProps.remove(HOST_PROPERTY_KEY);
        masterProps.remove(PORT_PROPERTY_KEY);
        slavesProps.remove(HOST_PROPERTY_KEY);
        slavesProps.remove(PORT_PROPERTY_KEY);
        return ReplicationConnectionProxy.createProxyInstance(masterHostList, masterProps, slaveHostList, slavesProps);
    }

    private boolean isHostMaster(String host) {
        Properties hostSpecificProps;
        return NonRegisteringDriver.isHostPropertiesList(host) && (hostSpecificProps = NonRegisteringDriver.expandHostKeyValues(host)).containsKey("type") && "master".equalsIgnoreCase(hostSpecificProps.get("type").toString());
    }

    public String database(Properties props) {
        return props.getProperty(DBNAME_PROPERTY_KEY);
    }

    public int getMajorVersion() {
        return NonRegisteringDriver.getMajorVersionInternal();
    }

    public int getMinorVersion() {
        return NonRegisteringDriver.getMinorVersionInternal();
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        if (info == null) {
            info = new Properties();
        }
        if (url != null && url.startsWith(URL_PREFIX)) {
            info = this.parseURL(url, info);
        }
        DriverPropertyInfo hostProp = new DriverPropertyInfo(HOST_PROPERTY_KEY, info.getProperty(HOST_PROPERTY_KEY));
        hostProp.required = true;
        hostProp.description = Messages.getString("NonRegisteringDriver.3");
        DriverPropertyInfo portProp = new DriverPropertyInfo(PORT_PROPERTY_KEY, info.getProperty(PORT_PROPERTY_KEY, "3306"));
        portProp.required = false;
        portProp.description = Messages.getString("NonRegisteringDriver.7");
        DriverPropertyInfo dbProp = new DriverPropertyInfo(DBNAME_PROPERTY_KEY, info.getProperty(DBNAME_PROPERTY_KEY));
        dbProp.required = false;
        dbProp.description = "Database name";
        DriverPropertyInfo userProp = new DriverPropertyInfo(USER_PROPERTY_KEY, info.getProperty(USER_PROPERTY_KEY));
        userProp.required = true;
        userProp.description = Messages.getString("NonRegisteringDriver.13");
        DriverPropertyInfo passwordProp = new DriverPropertyInfo(PASSWORD_PROPERTY_KEY, info.getProperty(PASSWORD_PROPERTY_KEY));
        passwordProp.required = true;
        passwordProp.description = Messages.getString("NonRegisteringDriver.16");
        DriverPropertyInfo[] dpi = ConnectionPropertiesImpl.exposeAsDriverPropertyInfo(info, 5);
        dpi[0] = hostProp;
        dpi[1] = portProp;
        dpi[2] = dbProp;
        dpi[3] = userProp;
        dpi[4] = passwordProp;
        return dpi;
    }

    public String host(Properties props) {
        return props.getProperty(HOST_PROPERTY_KEY, "localhost");
    }

    public boolean jdbcCompliant() {
        return false;
    }

    public Properties parseURL(String url, Properties defaults) throws SQLException {
        int index;
        Properties urlProps;
        Properties properties = urlProps = defaults != null ? new Properties(defaults) : new Properties();
        if (url == null) {
            return null;
        }
        if (!(StringUtils.startsWithIgnoreCase(url, URL_PREFIX) || StringUtils.startsWithIgnoreCase(url, MXJ_URL_PREFIX) || StringUtils.startsWithIgnoreCase(url, LOADBALANCE_URL_PREFIX) || StringUtils.startsWithIgnoreCase(url, REPLICATION_URL_PREFIX))) {
            return null;
        }
        int beginningOfSlashes = url.indexOf("//");
        if (StringUtils.startsWithIgnoreCase(url, MXJ_URL_PREFIX)) {
            urlProps.setProperty("socketFactory", "com.mysql.management.driverlaunched.ServerLauncherSocketFactory");
        }
        if ((index = url.indexOf("?")) != -1) {
            String paramString = url.substring(index + 1, url.length());
            url = url.substring(0, index);
            StringTokenizer queryParams = new StringTokenizer(paramString, "&");
            while (queryParams.hasMoreTokens()) {
                String parameterValuePair = queryParams.nextToken();
                int indexOfEquals = StringUtils.indexOfIgnoreCase(0, parameterValuePair, "=");
                String parameter = null;
                String value = null;
                if (indexOfEquals != -1) {
                    parameter = parameterValuePair.substring(0, indexOfEquals);
                    if (indexOfEquals + 1 < parameterValuePair.length()) {
                        value = parameterValuePair.substring(indexOfEquals + 1);
                    }
                }
                if (value == null || value.length() <= 0 || parameter == null || parameter.length() <= 0) continue;
                try {
                    urlProps.setProperty(parameter, URLDecoder.decode(value, "UTF-8"));
                }
                catch (UnsupportedEncodingException badEncoding) {
                    urlProps.setProperty(parameter, URLDecoder.decode(value));
                }
                catch (NoSuchMethodError nsme) {
                    urlProps.setProperty(parameter, URLDecoder.decode(value));
                }
            }
        }
        url = url.substring(beginningOfSlashes + 2);
        String hostStuff = null;
        int slashIndex = StringUtils.indexOfIgnoreCase(0, url, "/", ALLOWED_QUOTES, ALLOWED_QUOTES, StringUtils.SEARCH_MODE__ALL);
        if (slashIndex != -1) {
            hostStuff = url.substring(0, slashIndex);
            if (slashIndex + 1 < url.length()) {
                urlProps.put(DBNAME_PROPERTY_KEY, url.substring(slashIndex + 1, url.length()));
            }
        } else {
            hostStuff = url;
        }
        int numHosts = 0;
        if (hostStuff != null && hostStuff.trim().length() > 0) {
            List<String> hosts = StringUtils.split(hostStuff, ",", ALLOWED_QUOTES, ALLOWED_QUOTES, false);
            for (String hostAndPort : hosts) {
                ++numHosts;
                String[] hostPortPair = NonRegisteringDriver.parseHostPortPair(hostAndPort);
                if (hostPortPair[0] != null && hostPortPair[0].trim().length() > 0) {
                    urlProps.setProperty("HOST." + numHosts, hostPortPair[0]);
                } else {
                    urlProps.setProperty("HOST." + numHosts, "localhost");
                }
                if (hostPortPair[1] != null) {
                    urlProps.setProperty("PORT." + numHosts, hostPortPair[1]);
                    continue;
                }
                urlProps.setProperty("PORT." + numHosts, "3306");
            }
        } else {
            numHosts = 1;
            urlProps.setProperty("HOST.1", "localhost");
            urlProps.setProperty("PORT.1", "3306");
        }
        urlProps.setProperty(NUM_HOSTS_PROPERTY_KEY, String.valueOf(numHosts));
        urlProps.setProperty(HOST_PROPERTY_KEY, urlProps.getProperty("HOST.1"));
        urlProps.setProperty(PORT_PROPERTY_KEY, urlProps.getProperty("PORT.1"));
        String propertiesTransformClassName = urlProps.getProperty(PROPERTIES_TRANSFORM_KEY);
        if (propertiesTransformClassName != null) {
            try {
                ConnectionPropertiesTransform propTransformer = (ConnectionPropertiesTransform)Class.forName(propertiesTransformClassName).newInstance();
                urlProps = propTransformer.transformProperties(urlProps);
            }
            catch (InstantiationException e) {
                throw SQLError.createSQLException("Unable to create properties transform instance '" + propertiesTransformClassName + "' due to underlying exception: " + e.toString(), "01S00", null);
            }
            catch (IllegalAccessException e) {
                throw SQLError.createSQLException("Unable to create properties transform instance '" + propertiesTransformClassName + "' due to underlying exception: " + e.toString(), "01S00", null);
            }
            catch (ClassNotFoundException e) {
                throw SQLError.createSQLException("Unable to create properties transform instance '" + propertiesTransformClassName + "' due to underlying exception: " + e.toString(), "01S00", null);
            }
        }
        if (Util.isColdFusion() && urlProps.getProperty("autoConfigureForColdFusion", "true").equalsIgnoreCase("true")) {
            String configs = urlProps.getProperty(USE_CONFIG_PROPERTY_KEY);
            StringBuilder newConfigs = new StringBuilder();
            if (configs != null) {
                newConfigs.append(configs);
                newConfigs.append(",");
            }
            newConfigs.append("coldFusion");
            urlProps.setProperty(USE_CONFIG_PROPERTY_KEY, newConfigs.toString());
        }
        String configNames = null;
        if (defaults != null) {
            configNames = defaults.getProperty(USE_CONFIG_PROPERTY_KEY);
        }
        if (configNames == null) {
            configNames = urlProps.getProperty(USE_CONFIG_PROPERTY_KEY);
        }
        if (configNames != null) {
            List<String> splitNames = StringUtils.split(configNames, ",", true);
            Properties configProps = new Properties();
            for (String configName : splitNames) {
                try {
                    InputStream configAsStream = this.getClass().getResourceAsStream("configs/" + configName + ".properties");
                    if (configAsStream == null) {
                        throw SQLError.createSQLException("Can't find configuration template named '" + configName + "'", "01S00", null);
                    }
                    configProps.load(configAsStream);
                }
                catch (IOException ioEx) {
                    SQLException sqlEx = SQLError.createSQLException("Unable to load configuration template '" + configName + "' due to underlying IOException: " + ioEx, "01S00", null);
                    sqlEx.initCause(ioEx);
                    throw sqlEx;
                }
            }
            Iterator propsIter = urlProps.keySet().iterator();
            while (propsIter.hasNext()) {
                String key = propsIter.next().toString();
                String property = urlProps.getProperty(key);
                configProps.setProperty(key, property);
            }
            urlProps = configProps;
        }
        if (defaults != null) {
            Iterator propsIter = defaults.keySet().iterator();
            while (propsIter.hasNext()) {
                String key = propsIter.next().toString();
                if (key.equals(NUM_HOSTS_PROPERTY_KEY)) continue;
                String property = defaults.getProperty(key);
                urlProps.setProperty(key, property);
            }
        }
        return urlProps;
    }

    public int port(Properties props) {
        return Integer.parseInt(props.getProperty(PORT_PROPERTY_KEY, "3306"));
    }

    public String property(String name, Properties props) {
        return props.getProperty(name);
    }

    public static Properties expandHostKeyValues(String host) {
        Properties hostProps = new Properties();
        if (NonRegisteringDriver.isHostPropertiesList(host)) {
            host = host.substring("address=".length() + 1);
            List<String> hostPropsList = StringUtils.split(host, ")", "'\"", "'\"", true);
            for (String propDef : hostPropsList) {
                String value;
                if (propDef.startsWith("(")) {
                    propDef = propDef.substring(1);
                }
                List<String> kvp = StringUtils.split(propDef, "=", "'\"", "'\"", true);
                String key = kvp.get(0);
                String string = value = kvp.size() > 1 ? kvp.get(1) : null;
                if (value != null && (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                if (value == null) continue;
                if (HOST_PROPERTY_KEY.equalsIgnoreCase(key) || DBNAME_PROPERTY_KEY.equalsIgnoreCase(key) || PORT_PROPERTY_KEY.equalsIgnoreCase(key) || PROTOCOL_PROPERTY_KEY.equalsIgnoreCase(key) || PATH_PROPERTY_KEY.equalsIgnoreCase(key)) {
                    key = key.toUpperCase(Locale.ENGLISH);
                } else if (USER_PROPERTY_KEY.equalsIgnoreCase(key) || PASSWORD_PROPERTY_KEY.equalsIgnoreCase(key)) {
                    key = key.toLowerCase(Locale.ENGLISH);
                }
                hostProps.setProperty(key, value);
            }
        }
        return hostProps;
    }

    public static boolean isHostPropertiesList(String host) {
        return host != null && StringUtils.startsWithIgnoreCase(host, "address=");
    }

    static {
        AbandonedConnectionCleanupThread referenceThread = new AbandonedConnectionCleanupThread();
        referenceThread.setDaemon(true);
        referenceThread.start();
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static class ConnectionPhantomReference
    extends PhantomReference<ConnectionImpl> {
        private NetworkResources io;

        ConnectionPhantomReference(ConnectionImpl connectionImpl, ReferenceQueue<ConnectionImpl> q) {
            super(connectionImpl, q);
            try {
                this.io = connectionImpl.getIO().getNetworkResources();
            }
            catch (SQLException sQLException) {
                // empty catch block
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void cleanup() {
            if (this.io != null) {
                try {
                    this.io.forceClose();
                    Object var2_1 = null;
                    this.io = null;
                }
                catch (Throwable throwable) {
                    Object var2_2 = null;
                    this.io = null;
                    throw throwable;
                }
            }
        }
    }
}


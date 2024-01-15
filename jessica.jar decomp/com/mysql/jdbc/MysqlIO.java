/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.AuthenticationPlugin;
import com.mysql.jdbc.Buffer;
import com.mysql.jdbc.BufferRow;
import com.mysql.jdbc.ByteArrayRow;
import com.mysql.jdbc.CharsetMapping;
import com.mysql.jdbc.CompressedInputStream;
import com.mysql.jdbc.ConnectionFeatureNotAvailableException;
import com.mysql.jdbc.Constants;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.ExportControlled;
import com.mysql.jdbc.Extension;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.MysqlDataTruncation;
import com.mysql.jdbc.NetworkResources;
import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.jdbc.PacketTooBigException;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ProfilerEventHandlerFactory;
import com.mysql.jdbc.ResultSetImpl;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.ResultSetRow;
import com.mysql.jdbc.RowData;
import com.mysql.jdbc.RowDataCursor;
import com.mysql.jdbc.RowDataDynamic;
import com.mysql.jdbc.RowDataStatic;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.Security;
import com.mysql.jdbc.ServerPreparedStatement;
import com.mysql.jdbc.SocketFactory;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StatementImpl;
import com.mysql.jdbc.StatementInterceptorV2;
import com.mysql.jdbc.StringUtils;
import com.mysql.jdbc.TimeUtil;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.authentication.MysqlClearPasswordPlugin;
import com.mysql.jdbc.authentication.MysqlNativePasswordPlugin;
import com.mysql.jdbc.authentication.MysqlOldPasswordPlugin;
import com.mysql.jdbc.authentication.Sha256PasswordPlugin;
import com.mysql.jdbc.exceptions.MySQLStatementCancelledException;
import com.mysql.jdbc.exceptions.MySQLTimeoutException;
import com.mysql.jdbc.log.LogUtils;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import com.mysql.jdbc.util.ReadAheadInputStream;
import com.mysql.jdbc.util.ResultSetUtil;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.Deflater;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class MysqlIO {
    private static final String CODE_PAGE_1252 = "Cp1252";
    protected static final int NULL_LENGTH = -1;
    protected static final int COMP_HEADER_LENGTH = 3;
    protected static final int MIN_COMPRESS_LEN = 50;
    protected static final int HEADER_LENGTH = 4;
    protected static final int AUTH_411_OVERHEAD = 33;
    public static final int SEED_LENGTH = 20;
    private static int maxBufferSize = 65535;
    private static final String NONE = "none";
    private static final int CLIENT_LONG_PASSWORD = 1;
    private static final int CLIENT_FOUND_ROWS = 2;
    private static final int CLIENT_LONG_FLAG = 4;
    protected static final int CLIENT_CONNECT_WITH_DB = 8;
    private static final int CLIENT_COMPRESS = 32;
    private static final int CLIENT_LOCAL_FILES = 128;
    private static final int CLIENT_PROTOCOL_41 = 512;
    private static final int CLIENT_INTERACTIVE = 1024;
    protected static final int CLIENT_SSL = 2048;
    private static final int CLIENT_TRANSACTIONS = 8192;
    protected static final int CLIENT_RESERVED = 16384;
    protected static final int CLIENT_SECURE_CONNECTION = 32768;
    private static final int CLIENT_MULTI_STATEMENTS = 65536;
    private static final int CLIENT_MULTI_RESULTS = 131072;
    private static final int CLIENT_PLUGIN_AUTH = 524288;
    private static final int CLIENT_CONNECT_ATTRS = 0x100000;
    private static final int CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA = 0x200000;
    private static final int CLIENT_CAN_HANDLE_EXPIRED_PASSWORD = 0x400000;
    private static final int CLIENT_SESSION_TRACK = 0x800000;
    private static final int CLIENT_DEPRECATE_EOF = 0x1000000;
    private static final int SERVER_STATUS_IN_TRANS = 1;
    private static final int SERVER_STATUS_AUTOCOMMIT = 2;
    static final int SERVER_MORE_RESULTS_EXISTS = 8;
    private static final int SERVER_QUERY_NO_GOOD_INDEX_USED = 16;
    private static final int SERVER_QUERY_NO_INDEX_USED = 32;
    private static final int SERVER_QUERY_WAS_SLOW = 2048;
    private static final int SERVER_STATUS_CURSOR_EXISTS = 64;
    private static final String FALSE_SCRAMBLE = "xxxxxxxx";
    protected static final int MAX_QUERY_SIZE_TO_LOG = 1024;
    protected static final int MAX_QUERY_SIZE_TO_EXPLAIN = 0x100000;
    protected static final int INITIAL_PACKET_SIZE = 1024;
    private static String jvmPlatformCharset = null;
    protected static final String ZERO_DATE_VALUE_MARKER = "0000-00-00";
    protected static final String ZERO_DATETIME_VALUE_MARKER = "0000-00-00 00:00:00";
    private static final String EXPLAINABLE_STATEMENT = "SELECT";
    private static final String[] EXPLAINABLE_STATEMENT_EXTENSION = new String[]{"INSERT", "UPDATE", "REPLACE", "DELETE"};
    private static final int MAX_PACKET_DUMP_LENGTH = 1024;
    private boolean packetSequenceReset = false;
    protected int serverCharsetIndex;
    private Buffer reusablePacket = null;
    private Buffer sendPacket = null;
    private Buffer sharedSendPacket = null;
    protected BufferedOutputStream mysqlOutput = null;
    protected MySQLConnection connection;
    private Deflater deflater = null;
    protected InputStream mysqlInput = null;
    private LinkedList<StringBuilder> packetDebugRingBuffer = null;
    private RowData streamingData = null;
    public Socket mysqlConnection = null;
    protected SocketFactory socketFactory = null;
    private SoftReference<Buffer> loadFileBufRef;
    private SoftReference<Buffer> splitBufRef;
    private SoftReference<Buffer> compressBufRef;
    protected String host = null;
    protected String seed;
    private String serverVersion = null;
    private String socketFactoryClassName = null;
    private byte[] packetHeaderBuf = new byte[4];
    private boolean colDecimalNeedsBump = false;
    private boolean hadWarnings = false;
    private boolean has41NewNewProt = false;
    private boolean hasLongColumnInfo = false;
    private boolean isInteractiveClient = false;
    private boolean logSlowQueries = false;
    private boolean platformDbCharsetMatches = true;
    private boolean profileSql = false;
    private boolean queryBadIndexUsed = false;
    private boolean queryNoIndexUsed = false;
    private boolean serverQueryWasSlow = false;
    private boolean use41Extensions = false;
    private boolean useCompression = false;
    private boolean useNewLargePackets = false;
    private boolean useNewUpdateCounts = false;
    private byte packetSequence = 0;
    private byte compressedPacketSequence = 0;
    private byte readPacketSequence = (byte)-1;
    private boolean checkPacketSequence = false;
    private byte protocolVersion = 0;
    private int maxAllowedPacket = 0x100000;
    protected int maxThreeBytes = 16581375;
    protected int port = 3306;
    protected int serverCapabilities;
    private int serverMajorVersion = 0;
    private int serverMinorVersion = 0;
    private int oldServerStatus = 0;
    private int serverStatus = 0;
    private int serverSubMinorVersion = 0;
    private int warningCount = 0;
    protected long clientParam = 0L;
    protected long lastPacketSentTimeMs = 0L;
    protected long lastPacketReceivedTimeMs = 0L;
    private boolean traceProtocol = false;
    private boolean enablePacketDebug = false;
    private boolean useConnectWithDb;
    private boolean needToGrabQueryFromPacket;
    private boolean autoGenerateTestcaseScript;
    private long threadId;
    private boolean useNanosForElapsedTime;
    private long slowQueryThreshold;
    private String queryTimingUnits;
    private boolean useDirectRowUnpack = true;
    private int useBufferRowSizeThreshold;
    private int commandCount = 0;
    private List<StatementInterceptorV2> statementInterceptors;
    private ExceptionInterceptor exceptionInterceptor;
    private int authPluginDataLength = 0;
    private Map<String, AuthenticationPlugin> authenticationPlugins = null;
    private List<String> disabledAuthenticationPlugins = null;
    private String clientDefaultAuthenticationPlugin = null;
    private String clientDefaultAuthenticationPluginName = null;
    private String serverDefaultAuthenticationPluginName = null;
    private int statementExecutionDepth = 0;
    private boolean useAutoSlowLog;

    public MysqlIO(String host, int port, Properties props, String socketFactoryClassName, MySQLConnection conn, int socketTimeout, int useBufferRowSizeThreshold) throws IOException, SQLException {
        this.connection = conn;
        if (this.connection.getEnablePacketDebug()) {
            this.packetDebugRingBuffer = new LinkedList();
        }
        this.traceProtocol = this.connection.getTraceProtocol();
        this.useAutoSlowLog = this.connection.getAutoSlowLog();
        this.useBufferRowSizeThreshold = useBufferRowSizeThreshold;
        this.useDirectRowUnpack = this.connection.getUseDirectRowUnpack();
        this.logSlowQueries = this.connection.getLogSlowQueries();
        this.reusablePacket = new Buffer(1024);
        this.sendPacket = new Buffer(1024);
        this.port = port;
        this.host = host;
        this.socketFactoryClassName = socketFactoryClassName;
        this.socketFactory = this.createSocketFactory();
        this.exceptionInterceptor = this.connection.getExceptionInterceptor();
        try {
            this.mysqlConnection = this.socketFactory.connect(this.host, this.port, props);
            if (socketTimeout != 0) {
                try {
                    this.mysqlConnection.setSoTimeout(socketTimeout);
                }
                catch (Exception ex) {
                    // empty catch block
                }
            }
            this.mysqlConnection = this.socketFactory.beforeHandshake();
            this.mysqlInput = this.connection.getUseReadAheadInput() ? new ReadAheadInputStream(this.mysqlConnection.getInputStream(), 16384, this.connection.getTraceProtocol(), this.connection.getLog()) : (this.connection.useUnbufferedInput() ? this.mysqlConnection.getInputStream() : new BufferedInputStream(this.mysqlConnection.getInputStream(), 16384));
            this.mysqlOutput = new BufferedOutputStream(this.mysqlConnection.getOutputStream(), 16384);
            this.isInteractiveClient = this.connection.getInteractiveClient();
            this.profileSql = this.connection.getProfileSql();
            this.autoGenerateTestcaseScript = this.connection.getAutoGenerateTestcaseScript();
            boolean bl = this.needToGrabQueryFromPacket = this.profileSql || this.logSlowQueries || this.autoGenerateTestcaseScript;
            if (this.connection.getUseNanosForElapsedTime() && TimeUtil.nanoTimeAvailable()) {
                this.useNanosForElapsedTime = true;
                this.queryTimingUnits = Messages.getString("Nanoseconds");
            } else {
                this.queryTimingUnits = Messages.getString("Milliseconds");
            }
            if (this.connection.getLogSlowQueries()) {
                this.calculateSlowQueryThreshold();
            }
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, 0L, 0L, ioEx, this.getExceptionInterceptor());
        }
    }

    public boolean hasLongColumnInfo() {
        return this.hasLongColumnInfo;
    }

    protected boolean isDataAvailable() throws SQLException {
        try {
            return this.mysqlInput.available() > 0;
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
    }

    protected long getLastPacketSentTimeMs() {
        return this.lastPacketSentTimeMs;
    }

    protected long getLastPacketReceivedTimeMs() {
        return this.lastPacketReceivedTimeMs;
    }

    protected ResultSetImpl getResultSet(StatementImpl callingStatement, long columnCount, int maxRows, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, boolean isBinaryEncoded, Field[] metadataFromCache) throws SQLException {
        int i;
        Field[] fields = null;
        if (metadataFromCache == null) {
            fields = new Field[(int)columnCount];
            i = 0;
            while ((long)i < columnCount) {
                Buffer fieldPacket = null;
                fieldPacket = this.readPacket();
                fields[i] = this.unpackField(fieldPacket, false);
                ++i;
            }
        } else {
            i = 0;
            while ((long)i < columnCount) {
                this.skipPacket();
                ++i;
            }
        }
        if (!this.isEOFDeprecated() || this.connection.versionMeetsMinimum(5, 0, 2) && callingStatement != null && isBinaryEncoded && callingStatement.isCursorRequired()) {
            Buffer packet = this.reuseAndReadPacket(this.reusablePacket);
            this.readServerStatusForResultSets(packet);
        }
        if (this.connection.versionMeetsMinimum(5, 0, 2) && this.connection.getUseCursorFetch() && isBinaryEncoded && callingStatement != null && callingStatement.getFetchSize() != 0 && callingStatement.getResultSetType() == 1003) {
            ServerPreparedStatement prepStmt = (ServerPreparedStatement)callingStatement;
            boolean usingCursor = true;
            if (this.connection.versionMeetsMinimum(5, 0, 5)) {
                boolean bl = usingCursor = (this.serverStatus & 0x40) != 0;
            }
            if (usingCursor) {
                RowDataCursor rows = new RowDataCursor(this, prepStmt, fields);
                ResultSetImpl rs = this.buildResultSetWithRows(callingStatement, catalog, fields, rows, resultSetType, resultSetConcurrency, isBinaryEncoded);
                if (usingCursor) {
                    rs.setFetchSize(callingStatement.getFetchSize());
                }
                return rs;
            }
        }
        RowData rowData = null;
        if (!streamResults) {
            rowData = this.readSingleRowSet(columnCount, maxRows, resultSetConcurrency, isBinaryEncoded, metadataFromCache == null ? fields : metadataFromCache);
        } else {
            this.streamingData = rowData = new RowDataDynamic(this, (int)columnCount, metadataFromCache == null ? fields : metadataFromCache, isBinaryEncoded);
        }
        ResultSetImpl rs = this.buildResultSetWithRows(callingStatement, catalog, metadataFromCache == null ? fields : metadataFromCache, rowData, resultSetType, resultSetConcurrency, isBinaryEncoded);
        return rs;
    }

    protected NetworkResources getNetworkResources() {
        return new NetworkResources(this.mysqlConnection, this.mysqlInput, this.mysqlOutput);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void forceClose() {
        try {
            this.getNetworkResources().forceClose();
            Object var2_1 = null;
            this.mysqlConnection = null;
            this.mysqlInput = null;
            this.mysqlOutput = null;
        }
        catch (Throwable throwable) {
            Object var2_2 = null;
            this.mysqlConnection = null;
            this.mysqlInput = null;
            this.mysqlOutput = null;
            throw throwable;
        }
    }

    protected final void skipPacket() throws SQLException {
        try {
            int lengthRead = this.readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
            if (lengthRead < 4) {
                this.forceClose();
                throw new IOException(Messages.getString("MysqlIO.1"));
            }
            int packetLength = (this.packetHeaderBuf[0] & 0xFF) + ((this.packetHeaderBuf[1] & 0xFF) << 8) + ((this.packetHeaderBuf[2] & 0xFF) << 16);
            if (this.traceProtocol) {
                StringBuilder traceMessageBuf = new StringBuilder();
                traceMessageBuf.append(Messages.getString("MysqlIO.2"));
                traceMessageBuf.append(packetLength);
                traceMessageBuf.append(Messages.getString("MysqlIO.3"));
                traceMessageBuf.append(StringUtils.dumpAsHex(this.packetHeaderBuf, 4));
                this.connection.getLog().logTrace(traceMessageBuf.toString());
            }
            byte multiPacketSeq = this.packetHeaderBuf[3];
            if (!this.packetSequenceReset) {
                if (this.enablePacketDebug && this.checkPacketSequence) {
                    this.checkPacketSequencing(multiPacketSeq);
                }
            } else {
                this.packetSequenceReset = false;
            }
            this.readPacketSequence = multiPacketSeq;
            this.skipFully(this.mysqlInput, packetLength);
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
        catch (OutOfMemoryError oom) {
            try {
                this.connection.realClose(false, false, true, oom);
            }
            catch (Exception exception) {
                // empty catch block
            }
            throw oom;
        }
    }

    protected final Buffer readPacket() throws SQLException {
        try {
            int lengthRead = this.readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
            if (lengthRead < 4) {
                this.forceClose();
                throw new IOException(Messages.getString("MysqlIO.1"));
            }
            int packetLength = (this.packetHeaderBuf[0] & 0xFF) + ((this.packetHeaderBuf[1] & 0xFF) << 8) + ((this.packetHeaderBuf[2] & 0xFF) << 16);
            if (packetLength > this.maxAllowedPacket) {
                throw new PacketTooBigException(packetLength, this.maxAllowedPacket);
            }
            if (this.traceProtocol) {
                StringBuilder traceMessageBuf = new StringBuilder();
                traceMessageBuf.append(Messages.getString("MysqlIO.2"));
                traceMessageBuf.append(packetLength);
                traceMessageBuf.append(Messages.getString("MysqlIO.3"));
                traceMessageBuf.append(StringUtils.dumpAsHex(this.packetHeaderBuf, 4));
                this.connection.getLog().logTrace(traceMessageBuf.toString());
            }
            byte multiPacketSeq = this.packetHeaderBuf[3];
            if (!this.packetSequenceReset) {
                if (this.enablePacketDebug && this.checkPacketSequence) {
                    this.checkPacketSequencing(multiPacketSeq);
                }
            } else {
                this.packetSequenceReset = false;
            }
            this.readPacketSequence = multiPacketSeq;
            byte[] buffer = new byte[packetLength];
            int numBytesRead = this.readFully(this.mysqlInput, buffer, 0, packetLength);
            if (numBytesRead != packetLength) {
                throw new IOException("Short read, expected " + packetLength + " bytes, only read " + numBytesRead);
            }
            Buffer packet = new Buffer(buffer);
            if (this.traceProtocol) {
                StringBuilder traceMessageBuf = new StringBuilder();
                traceMessageBuf.append(Messages.getString("MysqlIO.4"));
                traceMessageBuf.append(MysqlIO.getPacketDumpToLog(packet, packetLength));
                this.connection.getLog().logTrace(traceMessageBuf.toString());
            }
            if (this.enablePacketDebug) {
                this.enqueuePacketForDebugging(false, false, 0, this.packetHeaderBuf, packet);
            }
            if (this.connection.getMaintainTimeStats()) {
                this.lastPacketReceivedTimeMs = System.currentTimeMillis();
            }
            return packet;
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
        catch (OutOfMemoryError oom) {
            try {
                this.connection.realClose(false, false, true, oom);
            }
            catch (Exception exception) {
                // empty catch block
            }
            throw oom;
        }
    }

    protected final Field unpackField(Buffer packet, boolean extractDefaultValues) throws SQLException {
        if (this.use41Extensions) {
            if (this.has41NewNewProt) {
                int catalogNameStart = packet.getPosition() + 1;
                int catalogNameLength = packet.fastSkipLenString();
                catalogNameStart = this.adjustStartForFieldLength(catalogNameStart, catalogNameLength);
            }
            int databaseNameStart = packet.getPosition() + 1;
            int databaseNameLength = packet.fastSkipLenString();
            databaseNameStart = this.adjustStartForFieldLength(databaseNameStart, databaseNameLength);
            int tableNameStart = packet.getPosition() + 1;
            int tableNameLength = packet.fastSkipLenString();
            tableNameStart = this.adjustStartForFieldLength(tableNameStart, tableNameLength);
            int originalTableNameStart = packet.getPosition() + 1;
            int originalTableNameLength = packet.fastSkipLenString();
            originalTableNameStart = this.adjustStartForFieldLength(originalTableNameStart, originalTableNameLength);
            int nameStart = packet.getPosition() + 1;
            int nameLength = packet.fastSkipLenString();
            nameStart = this.adjustStartForFieldLength(nameStart, nameLength);
            int originalColumnNameStart = packet.getPosition() + 1;
            int originalColumnNameLength = packet.fastSkipLenString();
            originalColumnNameStart = this.adjustStartForFieldLength(originalColumnNameStart, originalColumnNameLength);
            packet.readByte();
            short charSetNumber = (short)packet.readInt();
            long colLength = 0L;
            colLength = this.has41NewNewProt ? packet.readLong() : (long)packet.readLongInt();
            int colType = packet.readByte() & 0xFF;
            short colFlag = 0;
            colFlag = this.hasLongColumnInfo ? (short)packet.readInt() : (short)(packet.readByte() & 0xFF);
            int colDecimals = packet.readByte() & 0xFF;
            int defaultValueStart = -1;
            int defaultValueLength = -1;
            if (extractDefaultValues) {
                defaultValueStart = packet.getPosition() + 1;
                defaultValueLength = packet.fastSkipLenString();
            }
            Field field = new Field(this.connection, packet.getByteBuffer(), databaseNameStart, databaseNameLength, tableNameStart, tableNameLength, originalTableNameStart, originalTableNameLength, nameStart, nameLength, originalColumnNameStart, originalColumnNameLength, colLength, colType, colFlag, colDecimals, defaultValueStart, defaultValueLength, charSetNumber);
            return field;
        }
        int tableNameStart = packet.getPosition() + 1;
        int tableNameLength = packet.fastSkipLenString();
        tableNameStart = this.adjustStartForFieldLength(tableNameStart, tableNameLength);
        int nameStart = packet.getPosition() + 1;
        int nameLength = packet.fastSkipLenString();
        nameStart = this.adjustStartForFieldLength(nameStart, nameLength);
        int colLength = packet.readnBytes();
        int colType = packet.readnBytes();
        packet.readByte();
        short colFlag = 0;
        colFlag = this.hasLongColumnInfo ? (short)packet.readInt() : (short)(packet.readByte() & 0xFF);
        int colDecimals = packet.readByte() & 0xFF;
        if (this.colDecimalNeedsBump) {
            ++colDecimals;
        }
        Field field = new Field(this.connection, packet.getByteBuffer(), nameStart, nameLength, tableNameStart, tableNameLength, colLength, colType, colFlag, colDecimals);
        return field;
    }

    private int adjustStartForFieldLength(int nameStart, int nameLength) {
        if (nameLength < 251) {
            return nameStart;
        }
        if (nameLength >= 251 && nameLength < 65536) {
            return nameStart + 2;
        }
        if (nameLength >= 65536 && nameLength < 0x1000000) {
            return nameStart + 3;
        }
        return nameStart + 8;
    }

    protected boolean isSetNeededForAutoCommitMode(boolean autoCommitFlag) {
        if (this.use41Extensions && this.connection.getElideSetAutoCommits()) {
            boolean autoCommitModeOnServer;
            boolean bl = autoCommitModeOnServer = (this.serverStatus & 2) != 0;
            if (!autoCommitFlag && this.versionMeetsMinimum(5, 0, 0)) {
                return !this.inTransactionOnServer();
            }
            return autoCommitModeOnServer != autoCommitFlag;
        }
        return true;
    }

    protected boolean inTransactionOnServer() {
        return (this.serverStatus & 1) != 0;
    }

    protected void changeUser(String userName, String password, String database) throws SQLException {
        this.packetSequence = (byte)-1;
        this.compressedPacketSequence = (byte)-1;
        int passwordLength = 16;
        int userLength = userName != null ? userName.length() : 0;
        int databaseLength = database != null ? database.length() : 0;
        int packLength = (userLength + passwordLength + databaseLength) * 3 + 7 + 4 + 33;
        if ((this.serverCapabilities & 0x80000) != 0) {
            this.proceedHandshakeWithPluggableAuthentication(userName, password, database, null);
        } else if ((this.serverCapabilities & 0x8000) != 0) {
            Buffer changeUserPacket = new Buffer(packLength + 1);
            changeUserPacket.writeByte((byte)17);
            if (this.versionMeetsMinimum(4, 1, 1)) {
                this.secureAuth411(changeUserPacket, packLength, userName, password, database, false);
            } else {
                this.secureAuth(changeUserPacket, packLength, userName, password, database, false);
            }
        } else {
            boolean localUseConnectWithDb;
            Buffer packet = new Buffer(packLength);
            packet.writeByte((byte)17);
            packet.writeString(userName);
            if (this.protocolVersion > 9) {
                packet.writeString(Util.newCrypt(password, this.seed, this.connection.getPasswordCharacterEncoding()));
            } else {
                packet.writeString(Util.oldCrypt(password, this.seed));
            }
            boolean bl = localUseConnectWithDb = this.useConnectWithDb && database != null && database.length() > 0;
            if (localUseConnectWithDb) {
                packet.writeString(database);
            }
            this.send(packet, packet.getPosition());
            this.checkErrorPacket();
            if (!localUseConnectWithDb) {
                this.changeDatabaseTo(database);
            }
        }
    }

    protected Buffer checkErrorPacket() throws SQLException {
        return this.checkErrorPacket(-1);
    }

    protected void checkForCharsetMismatch() {
        if (this.connection.getUseUnicode() && this.connection.getEncoding() != null) {
            String encodingToCheck = jvmPlatformCharset;
            if (encodingToCheck == null) {
                encodingToCheck = System.getProperty("file.encoding");
            }
            this.platformDbCharsetMatches = encodingToCheck == null ? false : encodingToCheck.equals(this.connection.getEncoding());
        }
    }

    protected void clearInputStream() throws SQLException {
        try {
            int len;
            while ((len = this.mysqlInput.available()) > 0 && this.mysqlInput.skip(len) > 0L) {
            }
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
    }

    protected void resetReadPacketSequence() {
        this.readPacketSequence = 0;
    }

    protected void dumpPacketRingBuffer() throws SQLException {
        if (this.packetDebugRingBuffer != null && this.connection.getEnablePacketDebug()) {
            StringBuilder dumpBuffer = new StringBuilder();
            dumpBuffer.append("Last " + this.packetDebugRingBuffer.size() + " packets received from server, from oldest->newest:\n");
            dumpBuffer.append("\n");
            Iterator ringBufIter = this.packetDebugRingBuffer.iterator();
            while (ringBufIter.hasNext()) {
                dumpBuffer.append((CharSequence)ringBufIter.next());
                dumpBuffer.append("\n");
            }
            this.connection.getLog().logTrace(dumpBuffer.toString());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected void explainSlowQuery(byte[] querySQL, String truncatedQuery) throws SQLException {
        PreparedStatement stmt;
        block8: {
            if (!StringUtils.startsWithIgnoreCaseAndWs(truncatedQuery, EXPLAINABLE_STATEMENT)) {
                if (!this.versionMeetsMinimum(5, 6, 3)) return;
                if (StringUtils.startsWithIgnoreCaseAndWs(truncatedQuery, EXPLAINABLE_STATEMENT_EXTENSION) == -1) return;
            }
            stmt = null;
            ResultSet rs = null;
            try {
                try {
                    stmt = (PreparedStatement)this.connection.clientPrepareStatement("EXPLAIN ?");
                    stmt.setBytesNoEscapeNoQuotes(1, querySQL);
                    rs = stmt.executeQuery();
                    StringBuilder explainResults = new StringBuilder(Messages.getString("MysqlIO.8") + truncatedQuery + Messages.getString("MysqlIO.9"));
                    ResultSetUtil.appendResultSetSlashGStyle(explainResults, rs);
                    this.connection.getLog().logWarn(explainResults.toString());
                }
                catch (SQLException sqlEx) {
                    Object var7_8 = null;
                    if (rs != null) {
                        rs.close();
                    }
                    if (stmt == null) return;
                    stmt.close();
                    return;
                }
                Object var7_7 = null;
                if (rs == null) break block8;
            }
            catch (Throwable throwable) {
                Object var7_9 = null;
                if (rs != null) {
                    rs.close();
                }
                if (stmt == null) throw throwable;
                stmt.close();
                throw throwable;
            }
            rs.close();
        }
        if (stmt == null) return;
        stmt.close();
    }

    static int getMaxBuf() {
        return maxBufferSize;
    }

    final int getServerMajorVersion() {
        return this.serverMajorVersion;
    }

    final int getServerMinorVersion() {
        return this.serverMinorVersion;
    }

    final int getServerSubMinorVersion() {
        return this.serverSubMinorVersion;
    }

    String getServerVersion() {
        return this.serverVersion;
    }

    void doHandshake(String user, String password, String database) throws SQLException {
        this.checkPacketSequence = false;
        this.readPacketSequence = 0;
        Buffer buf = this.readPacket();
        this.protocolVersion = buf.readByte();
        if (this.protocolVersion == -1) {
            try {
                this.mysqlConnection.close();
            }
            catch (Exception e) {
                // empty catch block
            }
            int errno = 2000;
            errno = buf.readInt();
            String serverErrorMessage = buf.readString("ASCII", this.getExceptionInterceptor());
            StringBuilder errorBuf = new StringBuilder(Messages.getString("MysqlIO.10"));
            errorBuf.append(serverErrorMessage);
            errorBuf.append("\"");
            String xOpen = SQLError.mysqlToSqlState(errno, this.connection.getUseSqlStateCodes());
            throw SQLError.createSQLException(SQLError.get(xOpen) + ", " + errorBuf.toString(), xOpen, errno, this.getExceptionInterceptor());
        }
        this.serverVersion = buf.readString("ASCII", this.getExceptionInterceptor());
        int point = this.serverVersion.indexOf(46);
        if (point != -1) {
            try {
                int n;
                this.serverMajorVersion = n = Integer.parseInt(this.serverVersion.substring(0, point));
            }
            catch (NumberFormatException NFE1) {
                // empty catch block
            }
            String remaining = this.serverVersion.substring(point + 1, this.serverVersion.length());
            point = remaining.indexOf(46);
            if (point != -1) {
                int pos;
                try {
                    int n;
                    this.serverMinorVersion = n = Integer.parseInt(remaining.substring(0, point));
                }
                catch (NumberFormatException nfe) {
                    // empty catch block
                }
                remaining = remaining.substring(point + 1, remaining.length());
                for (pos = 0; pos < remaining.length() && remaining.charAt(pos) >= '0' && remaining.charAt(pos) <= '9'; ++pos) {
                }
                try {
                    int n;
                    this.serverSubMinorVersion = n = Integer.parseInt(remaining.substring(0, pos));
                }
                catch (NumberFormatException nfe) {
                    // empty catch block
                }
            }
        }
        if (this.versionMeetsMinimum(4, 0, 8)) {
            this.maxThreeBytes = 0xFFFFFF;
            this.useNewLargePackets = true;
        } else {
            this.maxThreeBytes = 16581375;
            this.useNewLargePackets = false;
        }
        this.colDecimalNeedsBump = this.versionMeetsMinimum(3, 23, 0);
        this.colDecimalNeedsBump = !this.versionMeetsMinimum(3, 23, 15);
        this.useNewUpdateCounts = this.versionMeetsMinimum(3, 22, 5);
        this.threadId = buf.readLong();
        if (this.protocolVersion > 9) {
            this.seed = buf.readString("ASCII", this.getExceptionInterceptor(), 8);
            buf.readByte();
        } else {
            this.seed = buf.readString("ASCII", this.getExceptionInterceptor());
        }
        this.serverCapabilities = 0;
        if (buf.getPosition() < buf.getBufLength()) {
            this.serverCapabilities = buf.readInt();
        }
        if (this.versionMeetsMinimum(4, 1, 1) || this.protocolVersion > 9 && (this.serverCapabilities & 0x200) != 0) {
            this.serverCharsetIndex = buf.readByte() & 0xFF;
            this.serverStatus = buf.readInt();
            this.checkTransactionState(0);
            this.serverCapabilities |= buf.readInt() << 16;
            if ((this.serverCapabilities & 0x80000) != 0) {
                this.authPluginDataLength = buf.readByte() & 0xFF;
            } else {
                buf.readByte();
            }
            buf.setPosition(buf.getPosition() + 10);
            if ((this.serverCapabilities & 0x8000) != 0) {
                StringBuilder newSeed;
                String seedPart2;
                if (this.authPluginDataLength > 0) {
                    seedPart2 = buf.readString("ASCII", this.getExceptionInterceptor(), this.authPluginDataLength - 8);
                    newSeed = new StringBuilder(this.authPluginDataLength);
                } else {
                    seedPart2 = buf.readString("ASCII", this.getExceptionInterceptor());
                    newSeed = new StringBuilder(20);
                }
                newSeed.append(this.seed);
                newSeed.append(seedPart2);
                this.seed = newSeed.toString();
            }
        }
        if ((this.serverCapabilities & 0x20) != 0 && this.connection.getUseCompression()) {
            this.clientParam |= 0x20L;
        }
        boolean bl = this.useConnectWithDb = database != null && database.length() > 0 && !this.connection.getCreateDatabaseIfNotExist();
        if (this.useConnectWithDb) {
            this.clientParam |= 8L;
        }
        if (this.versionMeetsMinimum(5, 7, 0) && !this.connection.getUseSSL() && !this.connection.isUseSSLExplicit()) {
            this.connection.setUseSSL(true);
            this.connection.setVerifyServerCertificate(false);
            this.connection.getLog().logWarn(Messages.getString("MysqlIO.SSLWarning"));
        }
        if ((this.serverCapabilities & 0x800) == 0 && this.connection.getUseSSL()) {
            if (this.connection.getRequireSSL()) {
                this.connection.close();
                this.forceClose();
                throw SQLError.createSQLException(Messages.getString("MysqlIO.15"), "08001", this.getExceptionInterceptor());
            }
            this.connection.setUseSSL(false);
        }
        if ((this.serverCapabilities & 4) != 0) {
            this.clientParam |= 4L;
            this.hasLongColumnInfo = true;
        }
        if (!this.connection.getUseAffectedRows()) {
            this.clientParam |= 2L;
        }
        if (this.connection.getAllowLoadLocalInfile()) {
            this.clientParam |= 0x80L;
        }
        if (this.isInteractiveClient) {
            this.clientParam |= 0x400L;
        }
        if ((this.serverCapabilities & 0x800000) != 0) {
            // empty if block
        }
        if ((this.serverCapabilities & 0x1000000) != 0) {
            this.clientParam |= 0x1000000L;
        }
        if ((this.serverCapabilities & 0x80000) != 0) {
            this.proceedHandshakeWithPluggableAuthentication(user, password, database, buf);
            return;
        }
        this.clientParam = this.protocolVersion > 9 ? (this.clientParam |= 1L) : (this.clientParam &= 0xFFFFFFFFFFFFFFFEL);
        if (this.versionMeetsMinimum(4, 1, 0) || this.protocolVersion > 9 && (this.serverCapabilities & 0x4000) != 0) {
            if (this.versionMeetsMinimum(4, 1, 1) || this.protocolVersion > 9 && (this.serverCapabilities & 0x200) != 0) {
                this.clientParam |= 0x200L;
                this.has41NewNewProt = true;
                this.clientParam |= 0x2000L;
                this.clientParam |= 0x20000L;
                if (this.connection.getAllowMultiQueries()) {
                    this.clientParam |= 0x10000L;
                }
            } else {
                this.clientParam |= 0x4000L;
                this.has41NewNewProt = false;
            }
            this.use41Extensions = true;
        }
        int passwordLength = 16;
        int userLength = user != null ? user.length() : 0;
        int databaseLength = database != null ? database.length() : 0;
        int packLength = (userLength + passwordLength + databaseLength) * 3 + 7 + 4 + 33;
        Buffer packet = null;
        if (!this.connection.getUseSSL()) {
            if ((this.serverCapabilities & 0x8000) != 0) {
                this.clientParam |= 0x8000L;
                if (this.versionMeetsMinimum(4, 1, 1) || this.protocolVersion > 9 && (this.serverCapabilities & 0x200) != 0) {
                    this.secureAuth411(null, packLength, user, password, database, true);
                } else {
                    this.secureAuth(null, packLength, user, password, database, true);
                }
            } else {
                packet = new Buffer(packLength);
                if ((this.clientParam & 0x4000L) != 0L) {
                    if (this.versionMeetsMinimum(4, 1, 1) || this.protocolVersion > 9 && (this.serverCapabilities & 0x200) != 0) {
                        packet.writeLong(this.clientParam);
                        packet.writeLong(this.maxThreeBytes);
                        packet.writeByte((byte)8);
                        packet.writeBytesNoNull(new byte[23]);
                    } else {
                        packet.writeLong(this.clientParam);
                        packet.writeLong(this.maxThreeBytes);
                    }
                } else {
                    packet.writeInt((int)this.clientParam);
                    packet.writeLongInt(this.maxThreeBytes);
                }
                packet.writeString(user, CODE_PAGE_1252, this.connection);
                if (this.protocolVersion > 9) {
                    packet.writeString(Util.newCrypt(password, this.seed, this.connection.getPasswordCharacterEncoding()), CODE_PAGE_1252, this.connection);
                } else {
                    packet.writeString(Util.oldCrypt(password, this.seed), CODE_PAGE_1252, this.connection);
                }
                if (this.useConnectWithDb) {
                    packet.writeString(database, CODE_PAGE_1252, this.connection);
                }
                this.send(packet, packet.getPosition());
            }
        } else {
            this.negotiateSSLConnection(user, password, database, packLength);
            if ((this.serverCapabilities & 0x8000) != 0) {
                if (this.versionMeetsMinimum(4, 1, 1)) {
                    this.secureAuth411(null, packLength, user, password, database, true);
                } else {
                    this.secureAuth411(null, packLength, user, password, database, true);
                }
            } else {
                packet = new Buffer(packLength);
                if (this.use41Extensions) {
                    packet.writeLong(this.clientParam);
                    packet.writeLong(this.maxThreeBytes);
                } else {
                    packet.writeInt((int)this.clientParam);
                    packet.writeLongInt(this.maxThreeBytes);
                }
                packet.writeString(user);
                if (this.protocolVersion > 9) {
                    packet.writeString(Util.newCrypt(password, this.seed, this.connection.getPasswordCharacterEncoding()));
                } else {
                    packet.writeString(Util.oldCrypt(password, this.seed));
                }
                if ((this.serverCapabilities & 8) != 0 && database != null && database.length() > 0) {
                    packet.writeString(database);
                }
                this.send(packet, packet.getPosition());
            }
        }
        if (!this.versionMeetsMinimum(4, 1, 1) || this.protocolVersion <= 9 || (this.serverCapabilities & 0x200) == 0) {
            this.checkErrorPacket();
        }
        if ((this.serverCapabilities & 0x20) != 0 && this.connection.getUseCompression() && !(this.mysqlInput instanceof CompressedInputStream)) {
            this.deflater = new Deflater();
            this.useCompression = true;
            this.mysqlInput = new CompressedInputStream(this.connection, this.mysqlInput);
        }
        if (!this.useConnectWithDb) {
            this.changeDatabaseTo(database);
        }
        try {
            this.mysqlConnection = this.socketFactory.afterHandshake();
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
    }

    private void loadAuthenticationPlugins() throws SQLException {
        String authenticationPluginClasses;
        this.clientDefaultAuthenticationPlugin = this.connection.getDefaultAuthenticationPlugin();
        if (this.clientDefaultAuthenticationPlugin == null || "".equals(this.clientDefaultAuthenticationPlugin.trim())) {
            throw SQLError.createSQLException(Messages.getString("Connection.BadDefaultAuthenticationPlugin", new Object[]{this.clientDefaultAuthenticationPlugin}), this.getExceptionInterceptor());
        }
        String disabledPlugins = this.connection.getDisabledAuthenticationPlugins();
        if (disabledPlugins != null && !"".equals(disabledPlugins)) {
            this.disabledAuthenticationPlugins = new ArrayList<String>();
            List<String> pluginsToDisable = StringUtils.split(disabledPlugins, ",", true);
            Iterator<String> iter = pluginsToDisable.iterator();
            while (iter.hasNext()) {
                this.disabledAuthenticationPlugins.add(iter.next());
            }
        }
        this.authenticationPlugins = new HashMap<String, AuthenticationPlugin>();
        AuthenticationPlugin plugin = new MysqlOldPasswordPlugin();
        plugin.init(this.connection, this.connection.getProperties());
        boolean defaultIsFound = this.addAuthenticationPlugin(plugin);
        plugin = new MysqlNativePasswordPlugin();
        plugin.init(this.connection, this.connection.getProperties());
        if (this.addAuthenticationPlugin(plugin)) {
            defaultIsFound = true;
        }
        plugin = new MysqlClearPasswordPlugin();
        plugin.init(this.connection, this.connection.getProperties());
        if (this.addAuthenticationPlugin(plugin)) {
            defaultIsFound = true;
        }
        plugin = new Sha256PasswordPlugin();
        plugin.init(this.connection, this.connection.getProperties());
        if (this.addAuthenticationPlugin(plugin)) {
            defaultIsFound = true;
        }
        if ((authenticationPluginClasses = this.connection.getAuthenticationPlugins()) != null && !"".equals(authenticationPluginClasses)) {
            List<Extension> plugins = Util.loadExtensions(this.connection, this.connection.getProperties(), authenticationPluginClasses, "Connection.BadAuthenticationPlugin", this.getExceptionInterceptor());
            for (Extension object : plugins) {
                plugin = (AuthenticationPlugin)object;
                if (!this.addAuthenticationPlugin(plugin)) continue;
                defaultIsFound = true;
            }
        }
        if (!defaultIsFound) {
            throw SQLError.createSQLException(Messages.getString("Connection.DefaultAuthenticationPluginIsNotListed", new Object[]{this.clientDefaultAuthenticationPlugin}), this.getExceptionInterceptor());
        }
    }

    private boolean addAuthenticationPlugin(AuthenticationPlugin plugin) throws SQLException {
        boolean disabledByMechanism;
        boolean isDefault = false;
        String pluginClassName = plugin.getClass().getName();
        String pluginProtocolName = plugin.getProtocolPluginName();
        boolean disabledByClassName = this.disabledAuthenticationPlugins != null && this.disabledAuthenticationPlugins.contains(pluginClassName);
        boolean bl = disabledByMechanism = this.disabledAuthenticationPlugins != null && this.disabledAuthenticationPlugins.contains(pluginProtocolName);
        if (disabledByClassName || disabledByMechanism) {
            if (this.clientDefaultAuthenticationPlugin.equals(pluginClassName)) {
                throw SQLError.createSQLException(Messages.getString("Connection.BadDisabledAuthenticationPlugin", new Object[]{disabledByClassName ? pluginClassName : pluginProtocolName}), this.getExceptionInterceptor());
            }
        } else {
            this.authenticationPlugins.put(pluginProtocolName, plugin);
            if (this.clientDefaultAuthenticationPlugin.equals(pluginClassName)) {
                this.clientDefaultAuthenticationPluginName = pluginProtocolName;
                isDefault = true;
            }
        }
        return isDefault;
    }

    private AuthenticationPlugin getAuthenticationPlugin(String pluginName) throws SQLException {
        AuthenticationPlugin plugin = this.authenticationPlugins.get(pluginName);
        if (plugin != null && !plugin.isReusable()) {
            try {
                plugin = (AuthenticationPlugin)plugin.getClass().newInstance();
                plugin.init(this.connection, this.connection.getProperties());
            }
            catch (Throwable t) {
                SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.BadAuthenticationPlugin", new Object[]{plugin.getClass().getName()}), this.getExceptionInterceptor());
                sqlEx.initCause(t);
                throw sqlEx;
            }
        }
        return plugin;
    }

    private void checkConfidentiality(AuthenticationPlugin plugin) throws SQLException {
        if (plugin.requiresConfidentiality() && !this.isSSLEstablished()) {
            throw SQLError.createSQLException(Messages.getString("Connection.AuthenticationPluginRequiresSSL", new Object[]{plugin.getProtocolPluginName()}), this.getExceptionInterceptor());
        }
    }

    private void proceedHandshakeWithPluggableAuthentication(String user, String password, String database, Buffer challenge) throws SQLException {
        if (this.authenticationPlugins == null) {
            this.loadAuthenticationPlugins();
        }
        boolean skipPassword = false;
        int passwordLength = 16;
        int userLength = user != null ? user.length() : 0;
        int databaseLength = database != null ? database.length() : 0;
        int packLength = (userLength + passwordLength + databaseLength) * 3 + 7 + 4 + 33;
        AuthenticationPlugin plugin = null;
        Buffer fromServer = null;
        ArrayList<Buffer> toServer = new ArrayList<Buffer>();
        boolean done = false;
        Buffer last_sent = null;
        boolean old_raw_challenge = false;
        int counter = 100;
        while (0 < counter--) {
            String enc;
            String pluginName;
            if (!done) {
                if (challenge != null) {
                    if (challenge.isOKPacket()) {
                        throw SQLError.createSQLException(Messages.getString("Connection.UnexpectedAuthenticationApproval", new Object[]{plugin.getProtocolPluginName()}), this.getExceptionInterceptor());
                    }
                    this.clientParam |= 0xAA201L;
                    if (this.connection.getAllowMultiQueries()) {
                        this.clientParam |= 0x10000L;
                    }
                    if ((this.serverCapabilities & 0x400000) != 0 && !this.connection.getDisconnectOnExpiredPasswords()) {
                        this.clientParam |= 0x400000L;
                    }
                    if ((this.serverCapabilities & 0x100000) != 0 && !NONE.equals(this.connection.getConnectionAttributes())) {
                        this.clientParam |= 0x100000L;
                    }
                    if ((this.serverCapabilities & 0x200000) != 0) {
                        this.clientParam |= 0x200000L;
                    }
                    this.has41NewNewProt = true;
                    this.use41Extensions = true;
                    if (this.connection.getUseSSL()) {
                        this.negotiateSSLConnection(user, password, database, packLength);
                    }
                    pluginName = null;
                    if ((this.serverCapabilities & 0x80000) != 0) {
                        pluginName = !this.versionMeetsMinimum(5, 5, 10) || this.versionMeetsMinimum(5, 6, 0) && !this.versionMeetsMinimum(5, 6, 2) ? challenge.readString("ASCII", this.getExceptionInterceptor(), this.authPluginDataLength) : challenge.readString("ASCII", this.getExceptionInterceptor());
                    }
                    if ((plugin = this.getAuthenticationPlugin(pluginName)) == null) {
                        plugin = this.getAuthenticationPlugin(this.clientDefaultAuthenticationPluginName);
                    } else if (pluginName.equals(Sha256PasswordPlugin.PLUGIN_NAME) && !this.isSSLEstablished() && this.connection.getServerRSAPublicKeyFile() == null && !this.connection.getAllowPublicKeyRetrieval()) {
                        plugin = this.getAuthenticationPlugin(this.clientDefaultAuthenticationPluginName);
                        skipPassword = !this.clientDefaultAuthenticationPluginName.equals(pluginName);
                    }
                    this.serverDefaultAuthenticationPluginName = plugin.getProtocolPluginName();
                    this.checkConfidentiality(plugin);
                    fromServer = new Buffer(StringUtils.getBytes(this.seed));
                } else {
                    plugin = this.getAuthenticationPlugin(this.serverDefaultAuthenticationPluginName == null ? this.clientDefaultAuthenticationPluginName : this.serverDefaultAuthenticationPluginName);
                    this.checkConfidentiality(plugin);
                    fromServer = new Buffer(StringUtils.getBytes(this.seed));
                }
            } else {
                challenge = this.checkErrorPacket();
                old_raw_challenge = false;
                this.packetSequence = (byte)(this.packetSequence + 1);
                this.compressedPacketSequence = (byte)(this.compressedPacketSequence + 1);
                if (plugin == null) {
                    plugin = this.getAuthenticationPlugin(this.serverDefaultAuthenticationPluginName != null ? this.serverDefaultAuthenticationPluginName : this.clientDefaultAuthenticationPluginName);
                }
                if (challenge.isOKPacket()) {
                    plugin.destroy();
                    break;
                }
                if (challenge.isAuthMethodSwitchRequestPacket()) {
                    skipPassword = false;
                    pluginName = challenge.readString("ASCII", this.getExceptionInterceptor());
                    if (!plugin.getProtocolPluginName().equals(pluginName)) {
                        plugin.destroy();
                        plugin = this.getAuthenticationPlugin(pluginName);
                        if (plugin == null) {
                            throw SQLError.createSQLException(Messages.getString("Connection.BadAuthenticationPlugin", new Object[]{pluginName}), this.getExceptionInterceptor());
                        }
                    }
                    this.checkConfidentiality(plugin);
                    fromServer = new Buffer(StringUtils.getBytes(challenge.readString("ASCII", this.getExceptionInterceptor())));
                } else if (this.versionMeetsMinimum(5, 5, 16)) {
                    fromServer = new Buffer(challenge.getBytes(challenge.getPosition(), challenge.getBufLength() - challenge.getPosition()));
                } else {
                    old_raw_challenge = true;
                    fromServer = new Buffer(challenge.getBytes(challenge.getPosition() - 1, challenge.getBufLength() - challenge.getPosition() + 1));
                }
            }
            try {
                plugin.setAuthenticationParameters(user, skipPassword ? null : password);
                done = plugin.nextAuthenticationStep(fromServer, toServer);
            }
            catch (SQLException e) {
                throw SQLError.createSQLException(e.getMessage(), e.getSQLState(), e, this.getExceptionInterceptor());
            }
            if (toServer.size() <= 0) continue;
            if (challenge == null) {
                enc = this.getEncodingForHandshake();
                last_sent = new Buffer(packLength + 1);
                last_sent.writeByte((byte)17);
                last_sent.writeString(user, enc, this.connection);
                if (toServer.get(0).getBufLength() < 256) {
                    last_sent.writeByte((byte)toServer.get(0).getBufLength());
                    last_sent.writeBytesNoNull(toServer.get(0).getByteBuffer(), 0, toServer.get(0).getBufLength());
                } else {
                    last_sent.writeByte((byte)0);
                }
                if (this.useConnectWithDb) {
                    last_sent.writeString(database, enc, this.connection);
                } else {
                    last_sent.writeByte((byte)0);
                }
                this.appendCharsetByteForHandshake(last_sent, enc);
                last_sent.writeByte((byte)0);
                if ((this.serverCapabilities & 0x80000) != 0) {
                    last_sent.writeString(plugin.getProtocolPluginName(), enc, this.connection);
                }
                if ((this.clientParam & 0x100000L) != 0L) {
                    this.sendConnectionAttributes(last_sent, enc, this.connection);
                    last_sent.writeByte((byte)0);
                }
                this.send(last_sent, last_sent.getPosition());
                continue;
            }
            if (challenge.isAuthMethodSwitchRequestPacket()) {
                last_sent = new Buffer(toServer.get(0).getBufLength() + 4);
                last_sent.writeBytesNoNull(toServer.get(0).getByteBuffer(), 0, toServer.get(0).getBufLength());
                this.send(last_sent, last_sent.getPosition());
                continue;
            }
            if (challenge.isRawPacket() || old_raw_challenge) {
                for (Buffer buffer : toServer) {
                    last_sent = new Buffer(buffer.getBufLength() + 4);
                    last_sent.writeBytesNoNull(buffer.getByteBuffer(), 0, toServer.get(0).getBufLength());
                    this.send(last_sent, last_sent.getPosition());
                }
                continue;
            }
            enc = this.getEncodingForHandshake();
            last_sent = new Buffer(packLength);
            last_sent.writeLong(this.clientParam);
            last_sent.writeLong(this.maxThreeBytes);
            this.appendCharsetByteForHandshake(last_sent, enc);
            last_sent.writeBytesNoNull(new byte[23]);
            last_sent.writeString(user, enc, this.connection);
            if ((this.serverCapabilities & 0x200000) != 0) {
                last_sent.writeLenBytes(toServer.get(0).getBytes(toServer.get(0).getBufLength()));
            } else {
                last_sent.writeByte((byte)toServer.get(0).getBufLength());
                last_sent.writeBytesNoNull(toServer.get(0).getByteBuffer(), 0, toServer.get(0).getBufLength());
            }
            if (this.useConnectWithDb) {
                last_sent.writeString(database, enc, this.connection);
            } else {
                last_sent.writeByte((byte)0);
            }
            if ((this.serverCapabilities & 0x80000) != 0) {
                last_sent.writeString(plugin.getProtocolPluginName(), enc, this.connection);
            }
            if ((this.clientParam & 0x100000L) != 0L) {
                this.sendConnectionAttributes(last_sent, enc, this.connection);
            }
            this.send(last_sent, last_sent.getPosition());
        }
        if (counter == 0) {
            throw SQLError.createSQLException(Messages.getString("CommunicationsException.TooManyAuthenticationPluginNegotiations"), this.getExceptionInterceptor());
        }
        if ((this.serverCapabilities & 0x20) != 0 && this.connection.getUseCompression() && !(this.mysqlInput instanceof CompressedInputStream)) {
            this.deflater = new Deflater();
            this.useCompression = true;
            this.mysqlInput = new CompressedInputStream(this.connection, this.mysqlInput);
        }
        if (!this.useConnectWithDb) {
            this.changeDatabaseTo(database);
        }
        try {
            this.mysqlConnection = this.socketFactory.afterHandshake();
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
    }

    private Properties getConnectionAttributesAsProperties(String atts) throws SQLException {
        Properties props = new Properties();
        if (atts != null) {
            String[] pairs;
            for (String pair : pairs = atts.split(",")) {
                int keyEnd = pair.indexOf(":");
                if (keyEnd <= 0 || keyEnd + 1 >= pair.length()) continue;
                props.setProperty(pair.substring(0, keyEnd), pair.substring(keyEnd + 1));
            }
        }
        props.setProperty("_client_name", "MySQL Connector Java");
        props.setProperty("_client_version", "5.1.40");
        props.setProperty("_runtime_vendor", NonRegisteringDriver.RUNTIME_VENDOR);
        props.setProperty("_runtime_version", NonRegisteringDriver.RUNTIME_VERSION);
        props.setProperty("_client_license", "GPL");
        return props;
    }

    private void sendConnectionAttributes(Buffer buf, String enc, MySQLConnection conn) throws SQLException {
        String atts = conn.getConnectionAttributes();
        Buffer lb = new Buffer(100);
        try {
            Properties props = this.getConnectionAttributesAsProperties(atts);
            for (Object key : props.keySet()) {
                lb.writeLenString((String)key, enc, conn.getServerCharset(), null, conn.parserKnowsUnicode(), conn);
                lb.writeLenString(props.getProperty((String)key), enc, conn.getServerCharset(), null, conn.parserKnowsUnicode(), conn);
            }
        }
        catch (UnsupportedEncodingException e) {
            // empty catch block
        }
        buf.writeByte((byte)(lb.getPosition() - 4));
        buf.writeBytesNoNull(lb.getByteBuffer(), 4, lb.getBufLength() - 4);
    }

    private void changeDatabaseTo(String database) throws SQLException {
        if (database == null || database.length() == 0) {
            return;
        }
        try {
            this.sendCommand(2, database, null, false, null, 0);
        }
        catch (Exception ex) {
            if (this.connection.getCreateDatabaseIfNotExist()) {
                this.sendCommand(3, "CREATE DATABASE IF NOT EXISTS " + database, null, false, null, 0);
                this.sendCommand(2, database, null, false, null, 0);
            }
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ex, this.getExceptionInterceptor());
        }
    }

    final ResultSetRow nextRow(Field[] fields, int columnCount, boolean isBinaryEncoded, int resultSetConcurrency, boolean useBufferRowIfPossible, boolean useBufferRowExplicit, boolean canReuseRowPacketForBufferRow, Buffer existingRowPacket) throws SQLException {
        if (this.useDirectRowUnpack && existingRowPacket == null && !isBinaryEncoded && !useBufferRowIfPossible && !useBufferRowExplicit) {
            return this.nextRowFast(fields, columnCount, isBinaryEncoded, resultSetConcurrency, useBufferRowIfPossible, useBufferRowExplicit, canReuseRowPacketForBufferRow);
        }
        Buffer rowPacket = null;
        if (existingRowPacket == null) {
            rowPacket = this.checkErrorPacket();
            if (!useBufferRowExplicit && useBufferRowIfPossible && rowPacket.getBufLength() > this.useBufferRowSizeThreshold) {
                useBufferRowExplicit = true;
            }
        } else {
            rowPacket = existingRowPacket;
            this.checkErrorPacket(existingRowPacket);
        }
        if (!isBinaryEncoded) {
            rowPacket.setPosition(rowPacket.getPosition() - 1);
            if (!(!this.isEOFDeprecated() && rowPacket.isEOFPacket() || this.isEOFDeprecated() && rowPacket.isResultSetOKPacket())) {
                if (resultSetConcurrency == 1008 || !useBufferRowIfPossible && !useBufferRowExplicit) {
                    byte[][] rowData = new byte[columnCount][];
                    for (int i = 0; i < columnCount; ++i) {
                        rowData[i] = rowPacket.readLenByteArray(0);
                    }
                    return new ByteArrayRow(rowData, this.getExceptionInterceptor());
                }
                if (!canReuseRowPacketForBufferRow) {
                    this.reusablePacket = new Buffer(rowPacket.getBufLength());
                }
                return new BufferRow(rowPacket, fields, false, this.getExceptionInterceptor());
            }
            this.readServerStatusForResultSets(rowPacket);
            return null;
        }
        if (!(!this.isEOFDeprecated() && rowPacket.isEOFPacket() || this.isEOFDeprecated() && rowPacket.isResultSetOKPacket())) {
            if (resultSetConcurrency == 1008 || !useBufferRowIfPossible && !useBufferRowExplicit) {
                return this.unpackBinaryResultSetRow(fields, rowPacket, resultSetConcurrency);
            }
            if (!canReuseRowPacketForBufferRow) {
                this.reusablePacket = new Buffer(rowPacket.getBufLength());
            }
            return new BufferRow(rowPacket, fields, true, this.getExceptionInterceptor());
        }
        rowPacket.setPosition(rowPacket.getPosition() - 1);
        this.readServerStatusForResultSets(rowPacket);
        return null;
    }

    final ResultSetRow nextRowFast(Field[] fields, int columnCount, boolean isBinaryEncoded, int resultSetConcurrency, boolean useBufferRowIfPossible, boolean useBufferRowExplicit, boolean canReuseRowPacket) throws SQLException {
        try {
            int lengthRead = this.readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
            if (lengthRead < 4) {
                this.forceClose();
                throw new RuntimeException(Messages.getString("MysqlIO.43"));
            }
            int packetLength = (this.packetHeaderBuf[0] & 0xFF) + ((this.packetHeaderBuf[1] & 0xFF) << 8) + ((this.packetHeaderBuf[2] & 0xFF) << 16);
            if (packetLength == this.maxThreeBytes) {
                this.reuseAndReadPacket(this.reusablePacket, packetLength);
                return this.nextRow(fields, columnCount, isBinaryEncoded, resultSetConcurrency, useBufferRowIfPossible, useBufferRowExplicit, canReuseRowPacket, this.reusablePacket);
            }
            if (packetLength > this.useBufferRowSizeThreshold) {
                this.reuseAndReadPacket(this.reusablePacket, packetLength);
                return this.nextRow(fields, columnCount, isBinaryEncoded, resultSetConcurrency, true, true, false, this.reusablePacket);
            }
            int remaining = packetLength;
            boolean firstTime = true;
            Object rowData = null;
            for (int i = 0; i < columnCount; ++i) {
                int sw = this.mysqlInput.read() & 0xFF;
                --remaining;
                if (firstTime) {
                    if (sw == 255) {
                        Buffer errorPacket = new Buffer(packetLength + 4);
                        errorPacket.setPosition(0);
                        errorPacket.writeByte(this.packetHeaderBuf[0]);
                        errorPacket.writeByte(this.packetHeaderBuf[1]);
                        errorPacket.writeByte(this.packetHeaderBuf[2]);
                        errorPacket.writeByte((byte)1);
                        errorPacket.writeByte((byte)sw);
                        this.readFully(this.mysqlInput, errorPacket.getByteBuffer(), 5, packetLength - 1);
                        errorPacket.setPosition(4);
                        this.checkErrorPacket(errorPacket);
                    }
                    if (sw == 254 && packetLength < 0xFFFFFF) {
                        if (this.use41Extensions) {
                            if (this.isEOFDeprecated()) {
                                remaining -= this.skipLengthEncodedInteger(this.mysqlInput);
                                remaining -= this.skipLengthEncodedInteger(this.mysqlInput);
                                this.oldServerStatus = this.serverStatus;
                                this.serverStatus = this.mysqlInput.read() & 0xFF | (this.mysqlInput.read() & 0xFF) << 8;
                                this.checkTransactionState(this.oldServerStatus);
                                remaining -= 2;
                                this.warningCount = this.mysqlInput.read() & 0xFF | (this.mysqlInput.read() & 0xFF) << 8;
                                remaining -= 2;
                                if (this.warningCount > 0) {
                                    this.hadWarnings = true;
                                }
                            } else {
                                this.warningCount = this.mysqlInput.read() & 0xFF | (this.mysqlInput.read() & 0xFF) << 8;
                                remaining -= 2;
                                if (this.warningCount > 0) {
                                    this.hadWarnings = true;
                                }
                                this.oldServerStatus = this.serverStatus;
                                this.serverStatus = this.mysqlInput.read() & 0xFF | (this.mysqlInput.read() & 0xFF) << 8;
                                this.checkTransactionState(this.oldServerStatus);
                                remaining -= 2;
                            }
                            this.setServerSlowQueryFlags();
                            if (remaining > 0) {
                                this.skipFully(this.mysqlInput, remaining);
                            }
                        }
                        return null;
                    }
                    rowData = new byte[columnCount][];
                    firstTime = false;
                }
                int len = 0;
                switch (sw) {
                    case 251: {
                        len = -1;
                        break;
                    }
                    case 252: {
                        len = this.mysqlInput.read() & 0xFF | (this.mysqlInput.read() & 0xFF) << 8;
                        remaining -= 2;
                        break;
                    }
                    case 253: {
                        len = this.mysqlInput.read() & 0xFF | (this.mysqlInput.read() & 0xFF) << 8 | (this.mysqlInput.read() & 0xFF) << 16;
                        remaining -= 3;
                        break;
                    }
                    case 254: {
                        len = (int)((long)(this.mysqlInput.read() & 0xFF) | (long)(this.mysqlInput.read() & 0xFF) << 8 | (long)(this.mysqlInput.read() & 0xFF) << 16 | (long)(this.mysqlInput.read() & 0xFF) << 24 | (long)(this.mysqlInput.read() & 0xFF) << 32 | (long)(this.mysqlInput.read() & 0xFF) << 40 | (long)(this.mysqlInput.read() & 0xFF) << 48 | (long)(this.mysqlInput.read() & 0xFF) << 56);
                        remaining -= 8;
                        break;
                    }
                    default: {
                        len = sw;
                    }
                }
                if (len == -1) {
                    rowData[i] = null;
                    continue;
                }
                if (len == 0) {
                    rowData[i] = Constants.EMPTY_BYTE_ARRAY;
                    continue;
                }
                rowData[i] = new byte[len];
                int bytesRead = this.readFully(this.mysqlInput, rowData[i], 0, len);
                if (bytesRead != len) {
                    throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, new IOException(Messages.getString("MysqlIO.43")), this.getExceptionInterceptor());
                }
                remaining -= bytesRead;
            }
            if (remaining > 0) {
                this.skipFully(this.mysqlInput, remaining);
            }
            return new ByteArrayRow((byte[][])rowData, this.getExceptionInterceptor());
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void quit() throws SQLException {
        try {
            try {
                if (!this.mysqlConnection.isClosed()) {
                    try {
                        this.mysqlConnection.shutdownInput();
                    }
                    catch (UnsupportedOperationException ex) {}
                }
            }
            catch (IOException ioEx) {
                this.connection.getLog().logWarn("Caught while disconnecting...", ioEx);
            }
            Buffer packet = new Buffer(6);
            this.packetSequence = (byte)-1;
            this.compressedPacketSequence = (byte)-1;
            packet.writeByte((byte)1);
            this.send(packet, packet.getPosition());
            Object var3_4 = null;
            this.forceClose();
        }
        catch (Throwable throwable) {
            Object var3_5 = null;
            this.forceClose();
            throw throwable;
        }
    }

    Buffer getSharedSendPacket() {
        if (this.sharedSendPacket == null) {
            this.sharedSendPacket = new Buffer(1024);
        }
        return this.sharedSendPacket;
    }

    void closeStreamer(RowData streamer) throws SQLException {
        if (this.streamingData == null) {
            throw SQLError.createSQLException(Messages.getString("MysqlIO.17") + streamer + Messages.getString("MysqlIO.18"), this.getExceptionInterceptor());
        }
        if (streamer != this.streamingData) {
            throw SQLError.createSQLException(Messages.getString("MysqlIO.19") + streamer + Messages.getString("MysqlIO.20") + Messages.getString("MysqlIO.21") + Messages.getString("MysqlIO.22"), this.getExceptionInterceptor());
        }
        this.streamingData = null;
    }

    boolean tackOnMoreStreamingResults(ResultSetImpl addingTo) throws SQLException {
        if ((this.serverStatus & 8) != 0) {
            boolean moreRowSetsExist = true;
            ResultSetImpl currentResultSet = addingTo;
            boolean firstTime = true;
            while (moreRowSetsExist && (firstTime || !currentResultSet.reallyResult())) {
                firstTime = false;
                Buffer fieldPacket = this.checkErrorPacket();
                fieldPacket.setPosition(0);
                java.sql.Statement owningStatement = addingTo.getStatement();
                int maxRows = owningStatement.getMaxRows();
                ResultSetImpl newResultSet = this.readResultsForQueryOrUpdate((StatementImpl)owningStatement, maxRows, owningStatement.getResultSetType(), owningStatement.getResultSetConcurrency(), true, owningStatement.getConnection().getCatalog(), fieldPacket, addingTo.isBinaryEncoded, -1L, null);
                currentResultSet.setNextResultSet(newResultSet);
                currentResultSet = newResultSet;
                boolean bl = moreRowSetsExist = (this.serverStatus & 8) != 0;
                if (currentResultSet.reallyResult() || moreRowSetsExist) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    ResultSetImpl readAllResults(StatementImpl callingStatement, int maxRows, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Buffer resultPacket, boolean isBinaryEncoded, long preSentColumnCount, Field[] metadataFromCache) throws SQLException {
        boolean serverHasMoreResults;
        ResultSetImpl topLevelResultSet;
        resultPacket.setPosition(resultPacket.getPosition() - 1);
        ResultSetImpl currentResultSet = topLevelResultSet = this.readResultsForQueryOrUpdate(callingStatement, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, resultPacket, isBinaryEncoded, preSentColumnCount, metadataFromCache);
        boolean checkForMoreResults = (this.clientParam & 0x20000L) != 0L;
        boolean bl = serverHasMoreResults = (this.serverStatus & 8) != 0;
        if (serverHasMoreResults && streamResults) {
            if (topLevelResultSet.getUpdateCount() != -1L) {
                this.tackOnMoreStreamingResults(topLevelResultSet);
            }
            this.reclaimLargeReusablePacket();
            return topLevelResultSet;
        }
        boolean moreRowSetsExist = checkForMoreResults & serverHasMoreResults;
        while (moreRowSetsExist) {
            Buffer fieldPacket = this.checkErrorPacket();
            fieldPacket.setPosition(0);
            ResultSetImpl newResultSet = this.readResultsForQueryOrUpdate(callingStatement, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, fieldPacket, isBinaryEncoded, preSentColumnCount, metadataFromCache);
            currentResultSet.setNextResultSet(newResultSet);
            currentResultSet = newResultSet;
            moreRowSetsExist = (this.serverStatus & 8) != 0;
        }
        if (!streamResults) {
            this.clearInputStream();
        }
        this.reclaimLargeReusablePacket();
        return topLevelResultSet;
    }

    void resetMaxBuf() {
        this.maxAllowedPacket = this.connection.getMaxAllowedPacket();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    final Buffer sendCommand(int command, String extraData, Buffer queryPacket, boolean skipCheck, String extraDataCharEncoding, int timeoutMillis) throws SQLException {
        Buffer buffer;
        ++this.commandCount;
        this.enablePacketDebug = this.connection.getEnablePacketDebug();
        this.readPacketSequence = 0;
        int oldTimeout = 0;
        if (timeoutMillis != 0) {
            try {
                oldTimeout = this.mysqlConnection.getSoTimeout();
                this.mysqlConnection.setSoTimeout(timeoutMillis);
            }
            catch (SocketException e) {
                throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e, this.getExceptionInterceptor());
            }
        }
        try {
            try {
                block25: {
                    int bytesLeft;
                    this.checkForOutstandingStreamingData();
                    this.oldServerStatus = this.serverStatus;
                    this.serverStatus = 0;
                    this.hadWarnings = false;
                    this.warningCount = 0;
                    this.queryNoIndexUsed = false;
                    this.queryBadIndexUsed = false;
                    this.serverQueryWasSlow = false;
                    if (this.useCompression && (bytesLeft = this.mysqlInput.available()) > 0) {
                        this.mysqlInput.skip(bytesLeft);
                    }
                    try {
                        this.clearInputStream();
                        if (queryPacket == null) {
                            int packLength = 8 + (extraData != null ? extraData.length() : 0) + 2;
                            if (this.sendPacket == null) {
                                this.sendPacket = new Buffer(packLength);
                            }
                            this.packetSequence = (byte)-1;
                            this.compressedPacketSequence = (byte)-1;
                            this.readPacketSequence = 0;
                            this.checkPacketSequence = true;
                            this.sendPacket.clear();
                            this.sendPacket.writeByte((byte)command);
                            if (command == 2 || command == 5 || command == 6 || command == 3 || command == 22) {
                                if (extraDataCharEncoding == null) {
                                    this.sendPacket.writeStringNoNull(extraData);
                                } else {
                                    this.sendPacket.writeStringNoNull(extraData, extraDataCharEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.connection);
                                }
                            } else if (command == 12) {
                                long id = Long.parseLong(extraData);
                                this.sendPacket.writeLong(id);
                            }
                            this.send(this.sendPacket, this.sendPacket.getPosition());
                            break block25;
                        }
                        this.packetSequence = (byte)-1;
                        this.compressedPacketSequence = (byte)-1;
                        this.send(queryPacket, queryPacket.getPosition());
                    }
                    catch (SQLException sqlEx) {
                        throw sqlEx;
                    }
                    catch (Exception ex) {
                        throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ex, this.getExceptionInterceptor());
                    }
                }
                Buffer returnPacket = null;
                if (!skipCheck) {
                    if (command == 23 || command == 26) {
                        this.readPacketSequence = 0;
                        this.packetSequenceReset = true;
                    }
                    returnPacket = this.checkErrorPacket(command);
                }
                buffer = returnPacket;
                Object var12_17 = null;
                if (timeoutMillis == 0) return buffer;
            }
            catch (IOException ioEx) {
                this.preserveOldTransactionState();
                throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
            }
            catch (SQLException e) {
                this.preserveOldTransactionState();
                throw e;
            }
        }
        catch (Throwable throwable) {
            Object var12_18 = null;
            if (timeoutMillis == 0) throw throwable;
            try {
                this.mysqlConnection.setSoTimeout(oldTimeout);
                throw throwable;
            }
            catch (SocketException e) {
                throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e, this.getExceptionInterceptor());
            }
        }
        try {}
        catch (SocketException e) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e, this.getExceptionInterceptor());
        }
        this.mysqlConnection.setSoTimeout(oldTimeout);
        return buffer;
    }

    protected boolean shouldIntercept() {
        return this.statementInterceptors != null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final ResultSetInternalMethods sqlQueryDirect(StatementImpl callingStatement, String query, String characterEncoding, Buffer queryPacket, int maxRows, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Field[] cachedMetadata) throws Exception {
        block46: {
            ResultSetInternalMethods interceptedResults;
            ++this.statementExecutionDepth;
            if (this.statementInterceptors == null || (interceptedResults = this.invokeStatementInterceptorsPre(query, callingStatement, false)) == null) break block46;
            ResultSetInternalMethods resultSetInternalMethods = interceptedResults;
            Object var31_16 = null;
            --this.statementExecutionDepth;
            return resultSetInternalMethods;
        }
        try {
            ResultSetInternalMethods interceptedResults;
            long queryStartTime = 0L;
            long queryEndTime = 0L;
            String statementComment = this.connection.getStatementComment();
            if (this.connection.getIncludeThreadNamesAsStatementComment()) {
                statementComment = (statementComment != null ? statementComment + ", " : "") + "java thread: " + Thread.currentThread().getName();
            }
            if (query != null) {
                int packLength = 5 + query.length() * 3 + 2;
                byte[] commentAsBytes = null;
                if (statementComment != null) {
                    commentAsBytes = StringUtils.getBytes(statementComment, null, characterEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
                    packLength += commentAsBytes.length;
                    packLength += 6;
                }
                if (this.sendPacket == null) {
                    this.sendPacket = new Buffer(packLength);
                } else {
                    this.sendPacket.clear();
                }
                this.sendPacket.writeByte((byte)3);
                if (commentAsBytes != null) {
                    this.sendPacket.writeBytesNoNull(Constants.SLASH_STAR_SPACE_AS_BYTES);
                    this.sendPacket.writeBytesNoNull(commentAsBytes);
                    this.sendPacket.writeBytesNoNull(Constants.SPACE_STAR_SLASH_SPACE_AS_BYTES);
                }
                if (characterEncoding != null) {
                    if (this.platformDbCharsetMatches) {
                        this.sendPacket.writeStringNoNull(query, characterEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.connection);
                    } else if (StringUtils.startsWithIgnoreCaseAndWs(query, "LOAD DATA")) {
                        this.sendPacket.writeBytesNoNull(StringUtils.getBytes(query));
                    } else {
                        this.sendPacket.writeStringNoNull(query, characterEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.connection);
                    }
                } else {
                    this.sendPacket.writeStringNoNull(query);
                }
                queryPacket = this.sendPacket;
            }
            byte[] queryBuf = null;
            int oldPacketPosition = 0;
            if (this.needToGrabQueryFromPacket) {
                queryBuf = queryPacket.getByteBuffer();
                oldPacketPosition = queryPacket.getPosition();
                queryStartTime = this.getCurrentTimeNanosOrMillis();
            }
            if (this.autoGenerateTestcaseScript) {
                String testcaseQuery = null;
                testcaseQuery = query != null ? (statementComment != null ? "/* " + statementComment + " */ " + query : query) : StringUtils.toString(queryBuf, 5, oldPacketPosition - 5);
                StringBuilder debugBuf = new StringBuilder(testcaseQuery.length() + 32);
                this.connection.generateConnectionCommentBlock(debugBuf);
                debugBuf.append(testcaseQuery);
                debugBuf.append(';');
                this.connection.dumpTestcaseQuery(debugBuf.toString());
            }
            Buffer resultPacket = this.sendCommand(3, null, queryPacket, false, null, 0);
            long fetchBeginTime = 0L;
            long fetchEndTime = 0L;
            String profileQueryToLog = null;
            boolean queryWasSlow = false;
            if (this.profileSql || this.logSlowQueries) {
                queryEndTime = this.getCurrentTimeNanosOrMillis();
                boolean shouldExtractQuery = false;
                if (this.profileSql) {
                    shouldExtractQuery = true;
                } else if (this.logSlowQueries) {
                    long queryTime = queryEndTime - queryStartTime;
                    boolean logSlow = false;
                    if (!this.useAutoSlowLog) {
                        logSlow = queryTime > (long)this.connection.getSlowQueryThresholdMillis();
                    } else {
                        logSlow = this.connection.isAbonormallyLongQuery(queryTime);
                        this.connection.reportQueryTime(queryTime);
                    }
                    if (logSlow) {
                        shouldExtractQuery = true;
                        queryWasSlow = true;
                    }
                }
                if (shouldExtractQuery) {
                    boolean truncated = false;
                    int extractPosition = oldPacketPosition;
                    if (oldPacketPosition > this.connection.getMaxQuerySizeToLog()) {
                        extractPosition = this.connection.getMaxQuerySizeToLog() + 5;
                        truncated = true;
                    }
                    profileQueryToLog = StringUtils.toString(queryBuf, 5, extractPosition - 5);
                    if (truncated) {
                        profileQueryToLog = profileQueryToLog + Messages.getString("MysqlIO.25");
                    }
                }
                fetchBeginTime = queryEndTime;
            }
            ResultSetInternalMethods rs = this.readAllResults(callingStatement, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, resultPacket, false, -1L, cachedMetadata);
            if (queryWasSlow && !this.serverQueryWasSlow) {
                StringBuilder mesgBuf = new StringBuilder(48 + profileQueryToLog.length());
                mesgBuf.append(Messages.getString("MysqlIO.SlowQuery", new Object[]{String.valueOf(this.useAutoSlowLog ? " 95% of all queries " : Long.valueOf(this.slowQueryThreshold)), this.queryTimingUnits, queryEndTime - queryStartTime}));
                mesgBuf.append(profileQueryToLog);
                ProfilerEventHandler eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
                eventSink.consumeEvent(new ProfilerEvent(6, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, rs.resultId, System.currentTimeMillis(), (int)(queryEndTime - queryStartTime), this.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), mesgBuf.toString()));
                if (this.connection.getExplainSlowQueries()) {
                    if (oldPacketPosition < 0x100000) {
                        this.explainSlowQuery(queryPacket.getBytes(5, oldPacketPosition - 5), profileQueryToLog);
                    } else {
                        this.connection.getLog().logWarn(Messages.getString("MysqlIO.28") + 0x100000 + Messages.getString("MysqlIO.29"));
                    }
                }
            }
            if (this.logSlowQueries) {
                ProfilerEventHandler eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
                if (this.queryBadIndexUsed && this.profileSql) {
                    eventSink.consumeEvent(new ProfilerEvent(6, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, rs.resultId, System.currentTimeMillis(), queryEndTime - queryStartTime, this.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), Messages.getString("MysqlIO.33") + profileQueryToLog));
                }
                if (this.queryNoIndexUsed && this.profileSql) {
                    eventSink.consumeEvent(new ProfilerEvent(6, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, rs.resultId, System.currentTimeMillis(), queryEndTime - queryStartTime, this.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), Messages.getString("MysqlIO.35") + profileQueryToLog));
                }
                if (this.serverQueryWasSlow && this.profileSql) {
                    eventSink.consumeEvent(new ProfilerEvent(6, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, rs.resultId, System.currentTimeMillis(), queryEndTime - queryStartTime, this.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), Messages.getString("MysqlIO.ServerSlowQuery") + profileQueryToLog));
                }
            }
            if (this.profileSql) {
                fetchEndTime = this.getCurrentTimeNanosOrMillis();
                ProfilerEventHandler eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
                eventSink.consumeEvent(new ProfilerEvent(3, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, rs.resultId, System.currentTimeMillis(), queryEndTime - queryStartTime, this.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), profileQueryToLog));
                eventSink.consumeEvent(new ProfilerEvent(5, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, rs.resultId, System.currentTimeMillis(), fetchEndTime - fetchBeginTime, this.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), null));
            }
            if (this.hadWarnings) {
                this.scanForAndThrowDataTruncation();
            }
            if (this.statementInterceptors != null && (interceptedResults = this.invokeStatementInterceptorsPost(query, callingStatement, rs, false, null)) != null) {
                rs = interceptedResults;
            }
            ResultSetImpl resultSetImpl = rs;
            Object var31_17 = null;
            --this.statementExecutionDepth;
            return resultSetImpl;
        }
        catch (SQLException sqlEx) {
            try {
                if (this.statementInterceptors != null) {
                    this.invokeStatementInterceptorsPost(query, callingStatement, null, false, sqlEx);
                }
                if (callingStatement != null) {
                    Object object = callingStatement.cancelTimeoutMutex;
                    synchronized (object) {
                        if (callingStatement.wasCancelled) {
                            SQLException cause = null;
                            cause = callingStatement.wasCancelledByTimeout ? new MySQLTimeoutException() : new MySQLStatementCancelledException();
                            callingStatement.resetCancelledState();
                            throw cause;
                        }
                    }
                }
                throw sqlEx;
            }
            catch (Throwable throwable) {
                Object var31_18 = null;
                --this.statementExecutionDepth;
                throw throwable;
            }
        }
    }

    ResultSetInternalMethods invokeStatementInterceptorsPre(String sql, Statement interceptedStatement, boolean forceExecute) throws SQLException {
        ResultSetInternalMethods previousResultSet = null;
        int s = this.statementInterceptors.size();
        for (int i = 0; i < s; ++i) {
            String sqlToInterceptor;
            ResultSetInternalMethods interceptedResultSet;
            boolean shouldExecute;
            StatementInterceptorV2 interceptor = this.statementInterceptors.get(i);
            boolean executeTopLevelOnly = interceptor.executeTopLevelOnly();
            boolean bl = shouldExecute = executeTopLevelOnly && (this.statementExecutionDepth == 1 || forceExecute) || !executeTopLevelOnly;
            if (!shouldExecute || (interceptedResultSet = interceptor.preProcess(sqlToInterceptor = sql, interceptedStatement, this.connection)) == null) continue;
            previousResultSet = interceptedResultSet;
        }
        return previousResultSet;
    }

    ResultSetInternalMethods invokeStatementInterceptorsPost(String sql, Statement interceptedStatement, ResultSetInternalMethods originalResultSet, boolean forceExecute, SQLException statementException) throws SQLException {
        int s = this.statementInterceptors.size();
        for (int i = 0; i < s; ++i) {
            String sqlToInterceptor;
            ResultSetInternalMethods interceptedResultSet;
            boolean shouldExecute;
            StatementInterceptorV2 interceptor = this.statementInterceptors.get(i);
            boolean executeTopLevelOnly = interceptor.executeTopLevelOnly();
            boolean bl = shouldExecute = executeTopLevelOnly && (this.statementExecutionDepth == 1 || forceExecute) || !executeTopLevelOnly;
            if (!shouldExecute || (interceptedResultSet = interceptor.postProcess(sqlToInterceptor = sql, interceptedStatement, originalResultSet, this.connection, this.warningCount, this.queryNoIndexUsed, this.queryBadIndexUsed, statementException)) == null) continue;
            originalResultSet = interceptedResultSet;
        }
        return originalResultSet;
    }

    private void calculateSlowQueryThreshold() {
        this.slowQueryThreshold = this.connection.getSlowQueryThresholdMillis();
        if (this.connection.getUseNanosForElapsedTime()) {
            long nanosThreshold = this.connection.getSlowQueryThresholdNanos();
            this.slowQueryThreshold = nanosThreshold != 0L ? nanosThreshold : (this.slowQueryThreshold *= 1000000L);
        }
    }

    protected long getCurrentTimeNanosOrMillis() {
        if (this.useNanosForElapsedTime) {
            return TimeUtil.getCurrentTimeNanosOrMillis();
        }
        return System.currentTimeMillis();
    }

    String getHost() {
        return this.host;
    }

    boolean isVersion(int major, int minor, int subminor) {
        return major == this.getServerMajorVersion() && minor == this.getServerMinorVersion() && subminor == this.getServerSubMinorVersion();
    }

    boolean versionMeetsMinimum(int major, int minor, int subminor) {
        if (this.getServerMajorVersion() >= major) {
            if (this.getServerMajorVersion() == major) {
                if (this.getServerMinorVersion() >= minor) {
                    if (this.getServerMinorVersion() == minor) {
                        return this.getServerSubMinorVersion() >= subminor;
                    }
                    return true;
                }
                return false;
            }
            return true;
        }
        return false;
    }

    private static final String getPacketDumpToLog(Buffer packetToDump, int packetLength) {
        if (packetLength < 1024) {
            return packetToDump.dump(packetLength);
        }
        StringBuilder packetDumpBuf = new StringBuilder(4096);
        packetDumpBuf.append(packetToDump.dump(1024));
        packetDumpBuf.append(Messages.getString("MysqlIO.36"));
        packetDumpBuf.append(1024);
        packetDumpBuf.append(Messages.getString("MysqlIO.37"));
        return packetDumpBuf.toString();
    }

    private final int readFully(InputStream in, byte[] b, int off, int len) throws IOException {
        int n;
        int count;
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        for (n = 0; n < len; n += count) {
            count = in.read(b, off + n, len - n);
            if (count >= 0) continue;
            throw new EOFException(Messages.getString("MysqlIO.EOF", new Object[]{len, n}));
        }
        return n;
    }

    private final long skipFully(InputStream in, long len) throws IOException {
        long n;
        long count;
        if (len < 0L) {
            throw new IOException("Negative skip length not allowed");
        }
        for (n = 0L; n < len; n += count) {
            count = in.skip(len - n);
            if (count >= 0L) continue;
            throw new EOFException(Messages.getString("MysqlIO.EOF", new Object[]{len, n}));
        }
        return n;
    }

    private final int skipLengthEncodedInteger(InputStream in) throws IOException {
        int sw = in.read() & 0xFF;
        switch (sw) {
            case 252: {
                return (int)this.skipFully(in, 2L) + 1;
            }
            case 253: {
                return (int)this.skipFully(in, 3L) + 1;
            }
            case 254: {
                return (int)this.skipFully(in, 8L) + 1;
            }
        }
        return 1;
    }

    protected final ResultSetImpl readResultsForQueryOrUpdate(StatementImpl callingStatement, int maxRows, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Buffer resultPacket, boolean isBinaryEncoded, long preSentColumnCount, Field[] metadataFromCache) throws SQLException {
        long columnCount = resultPacket.readFieldLength();
        if (columnCount == 0L) {
            return this.buildResultSetWithUpdates(callingStatement, resultPacket);
        }
        if (columnCount == -1L) {
            String charEncoding = null;
            if (this.connection.getUseUnicode()) {
                charEncoding = this.connection.getEncoding();
            }
            String fileName = null;
            fileName = this.platformDbCharsetMatches ? (charEncoding != null ? resultPacket.readString(charEncoding, this.getExceptionInterceptor()) : resultPacket.readString()) : resultPacket.readString();
            return this.sendFileToServer(callingStatement, fileName);
        }
        ResultSetImpl results = this.getResultSet(callingStatement, columnCount, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, isBinaryEncoded, metadataFromCache);
        return results;
    }

    private int alignPacketSize(int a, int l) {
        return a + l - 1 & ~(l - 1);
    }

    private ResultSetImpl buildResultSetWithRows(StatementImpl callingStatement, String catalog, Field[] fields, RowData rows, int resultSetType, int resultSetConcurrency, boolean isBinaryEncoded) throws SQLException {
        ResultSetImpl rs = null;
        switch (resultSetConcurrency) {
            case 1007: {
                rs = ResultSetImpl.getInstance(catalog, fields, rows, this.connection, callingStatement, false);
                if (!isBinaryEncoded) break;
                rs.setBinaryEncoded();
                break;
            }
            case 1008: {
                rs = ResultSetImpl.getInstance(catalog, fields, rows, this.connection, callingStatement, true);
                break;
            }
            default: {
                return ResultSetImpl.getInstance(catalog, fields, rows, this.connection, callingStatement, false);
            }
        }
        rs.setResultSetType(resultSetType);
        rs.setResultSetConcurrency(resultSetConcurrency);
        return rs;
    }

    private ResultSetImpl buildResultSetWithUpdates(StatementImpl callingStatement, Buffer resultPacket) throws SQLException {
        long updateCount = -1L;
        long updateID = -1L;
        String info = null;
        try {
            if (this.useNewUpdateCounts) {
                updateCount = resultPacket.newReadLength();
                updateID = resultPacket.newReadLength();
            } else {
                updateCount = resultPacket.readLength();
                updateID = resultPacket.readLength();
            }
            if (this.use41Extensions) {
                this.serverStatus = resultPacket.readInt();
                this.checkTransactionState(this.oldServerStatus);
                this.warningCount = resultPacket.readInt();
                if (this.warningCount > 0) {
                    this.hadWarnings = true;
                }
                resultPacket.readByte();
                this.setServerSlowQueryFlags();
            }
            if (this.connection.isReadInfoMsgEnabled()) {
                info = resultPacket.readString(this.connection.getErrorMessageEncoding(), this.getExceptionInterceptor());
            }
        }
        catch (Exception ex) {
            SQLException sqlEx = SQLError.createSQLException(SQLError.get("S1000"), "S1000", -1, this.getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
        ResultSetImpl updateRs = ResultSetImpl.getInstance(updateCount, updateID, this.connection, callingStatement);
        if (info != null) {
            updateRs.setServerInfo(info);
        }
        return updateRs;
    }

    private void setServerSlowQueryFlags() {
        this.queryBadIndexUsed = (this.serverStatus & 0x10) != 0;
        this.queryNoIndexUsed = (this.serverStatus & 0x20) != 0;
        this.serverQueryWasSlow = (this.serverStatus & 0x800) != 0;
    }

    private void checkForOutstandingStreamingData() throws SQLException {
        if (this.streamingData != null) {
            boolean shouldClobber = this.connection.getClobberStreamingResults();
            if (!shouldClobber) {
                throw SQLError.createSQLException(Messages.getString("MysqlIO.39") + this.streamingData + Messages.getString("MysqlIO.40") + Messages.getString("MysqlIO.41") + Messages.getString("MysqlIO.42"), this.getExceptionInterceptor());
            }
            this.streamingData.getOwner().realClose(false);
            this.clearInputStream();
        }
    }

    private Buffer compressPacket(Buffer packet, int offset, int packetLen) throws SQLException {
        int compressedLength = packetLen;
        int uncompressedLength = 0;
        byte[] compressedBytes = null;
        int offsetWrite = offset;
        if (packetLen < 50) {
            compressedBytes = packet.getByteBuffer();
        } else {
            byte[] bytesToCompress = packet.getByteBuffer();
            compressedBytes = new byte[bytesToCompress.length * 2];
            if (this.deflater == null) {
                this.deflater = new Deflater();
            }
            this.deflater.reset();
            this.deflater.setInput(bytesToCompress, offset, packetLen);
            this.deflater.finish();
            compressedLength = this.deflater.deflate(compressedBytes);
            if (compressedLength > packetLen) {
                compressedBytes = packet.getByteBuffer();
                compressedLength = packetLen;
            } else {
                uncompressedLength = packetLen;
                offsetWrite = 0;
            }
        }
        Buffer compressedPacket = new Buffer(7 + compressedLength);
        compressedPacket.setPosition(0);
        compressedPacket.writeLongInt(compressedLength);
        compressedPacket.writeByte(this.compressedPacketSequence);
        compressedPacket.writeLongInt(uncompressedLength);
        compressedPacket.writeBytesNoNull(compressedBytes, offsetWrite, compressedLength);
        return compressedPacket;
    }

    private final void readServerStatusForResultSets(Buffer rowPacket) throws SQLException {
        if (this.use41Extensions) {
            rowPacket.readByte();
            if (this.isEOFDeprecated()) {
                rowPacket.newReadLength();
                rowPacket.newReadLength();
                this.oldServerStatus = this.serverStatus;
                this.serverStatus = rowPacket.readInt();
                this.checkTransactionState(this.oldServerStatus);
                this.warningCount = rowPacket.readInt();
                if (this.warningCount > 0) {
                    this.hadWarnings = true;
                }
                rowPacket.readByte();
                if (this.connection.isReadInfoMsgEnabled()) {
                    rowPacket.readString(this.connection.getErrorMessageEncoding(), this.getExceptionInterceptor());
                }
            } else {
                this.warningCount = rowPacket.readInt();
                if (this.warningCount > 0) {
                    this.hadWarnings = true;
                }
                this.oldServerStatus = this.serverStatus;
                this.serverStatus = rowPacket.readInt();
                this.checkTransactionState(this.oldServerStatus);
            }
            this.setServerSlowQueryFlags();
        }
    }

    private SocketFactory createSocketFactory() throws SQLException {
        try {
            if (this.socketFactoryClassName == null) {
                throw SQLError.createSQLException(Messages.getString("MysqlIO.75"), "08001", this.getExceptionInterceptor());
            }
            return (SocketFactory)Class.forName(this.socketFactoryClassName).newInstance();
        }
        catch (Exception ex) {
            SQLException sqlEx = SQLError.createSQLException(Messages.getString("MysqlIO.76") + this.socketFactoryClassName + Messages.getString("MysqlIO.77"), "08001", this.getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }

    private void enqueuePacketForDebugging(boolean isPacketBeingSent, boolean isPacketReused, int sendLength, byte[] header, Buffer packet) throws SQLException {
        if (this.packetDebugRingBuffer.size() + 1 > this.connection.getPacketDebugBufferSize()) {
            this.packetDebugRingBuffer.removeFirst();
        }
        StringBuilder packetDump = null;
        if (!isPacketBeingSent) {
            int bytesToDump = Math.min(1024, packet.getBufLength());
            Buffer packetToDump = new Buffer(4 + bytesToDump);
            packetToDump.setPosition(0);
            packetToDump.writeBytesNoNull(header);
            packetToDump.writeBytesNoNull(packet.getBytes(0, bytesToDump));
            String packetPayload = packetToDump.dump(bytesToDump);
            packetDump = new StringBuilder(96 + packetPayload.length());
            packetDump.append("Server ");
            packetDump.append(isPacketReused ? "(re-used) " : "(new) ");
            packetDump.append(packet.toSuperString());
            packetDump.append(" --------------------> Client\n");
            packetDump.append("\nPacket payload:\n\n");
            packetDump.append(packetPayload);
            if (bytesToDump == 1024) {
                packetDump.append("\nNote: Packet of " + packet.getBufLength() + " bytes truncated to " + 1024 + " bytes.\n");
            }
        } else {
            int bytesToDump = Math.min(1024, sendLength);
            String packetPayload = packet.dump(bytesToDump);
            packetDump = new StringBuilder(68 + packetPayload.length());
            packetDump.append("Client ");
            packetDump.append(packet.toSuperString());
            packetDump.append("--------------------> Server\n");
            packetDump.append("\nPacket payload:\n\n");
            packetDump.append(packetPayload);
            if (bytesToDump == 1024) {
                packetDump.append("\nNote: Packet of " + sendLength + " bytes truncated to " + 1024 + " bytes.\n");
            }
        }
        this.packetDebugRingBuffer.addLast(packetDump);
    }

    private RowData readSingleRowSet(long columnCount, int maxRows, int resultSetConcurrency, boolean isBinaryEncoded, Field[] fields) throws SQLException {
        ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        boolean useBufferRowExplicit = MysqlIO.useBufferRowExplicit(fields);
        ResultSetRow row = this.nextRow(fields, (int)columnCount, isBinaryEncoded, resultSetConcurrency, false, useBufferRowExplicit, false, null);
        int rowCount = 0;
        if (row != null) {
            rows.add(row);
            rowCount = 1;
        }
        while (row != null) {
            row = this.nextRow(fields, (int)columnCount, isBinaryEncoded, resultSetConcurrency, false, useBufferRowExplicit, false, null);
            if (row == null || maxRows != -1 && rowCount >= maxRows) continue;
            rows.add(row);
            ++rowCount;
        }
        RowDataStatic rowData = new RowDataStatic(rows);
        return rowData;
    }

    public static boolean useBufferRowExplicit(Field[] fields) {
        if (fields == null) {
            return false;
        }
        for (int i = 0; i < fields.length; ++i) {
            switch (fields[i].getSQLType()) {
                case -4: 
                case -1: 
                case 2004: 
                case 2005: {
                    return true;
                }
            }
        }
        return false;
    }

    private void reclaimLargeReusablePacket() {
        if (this.reusablePacket != null && this.reusablePacket.getCapacity() > 0x100000) {
            this.reusablePacket = new Buffer(1024);
        }
    }

    private final Buffer reuseAndReadPacket(Buffer reuse) throws SQLException {
        return this.reuseAndReadPacket(reuse, -1);
    }

    private final Buffer reuseAndReadPacket(Buffer reuse, int existingPacketLength) throws SQLException {
        try {
            reuse.setWasMultiPacket(false);
            int packetLength = 0;
            if (existingPacketLength == -1) {
                int lengthRead = this.readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
                if (lengthRead < 4) {
                    this.forceClose();
                    throw new IOException(Messages.getString("MysqlIO.43"));
                }
                packetLength = (this.packetHeaderBuf[0] & 0xFF) + ((this.packetHeaderBuf[1] & 0xFF) << 8) + ((this.packetHeaderBuf[2] & 0xFF) << 16);
            } else {
                packetLength = existingPacketLength;
            }
            if (this.traceProtocol) {
                StringBuilder traceMessageBuf = new StringBuilder();
                traceMessageBuf.append(Messages.getString("MysqlIO.44"));
                traceMessageBuf.append(packetLength);
                traceMessageBuf.append(Messages.getString("MysqlIO.45"));
                traceMessageBuf.append(StringUtils.dumpAsHex(this.packetHeaderBuf, 4));
                this.connection.getLog().logTrace(traceMessageBuf.toString());
            }
            byte multiPacketSeq = this.packetHeaderBuf[3];
            if (!this.packetSequenceReset) {
                if (this.enablePacketDebug && this.checkPacketSequence) {
                    this.checkPacketSequencing(multiPacketSeq);
                }
            } else {
                this.packetSequenceReset = false;
            }
            this.readPacketSequence = multiPacketSeq;
            reuse.setPosition(0);
            if (reuse.getByteBuffer().length <= packetLength) {
                reuse.setByteBuffer(new byte[packetLength + 1]);
            }
            reuse.setBufLength(packetLength);
            int numBytesRead = this.readFully(this.mysqlInput, reuse.getByteBuffer(), 0, packetLength);
            if (numBytesRead != packetLength) {
                throw new IOException("Short read, expected " + packetLength + " bytes, only read " + numBytesRead);
            }
            if (this.traceProtocol) {
                StringBuilder traceMessageBuf = new StringBuilder();
                traceMessageBuf.append(Messages.getString("MysqlIO.46"));
                traceMessageBuf.append(MysqlIO.getPacketDumpToLog(reuse, packetLength));
                this.connection.getLog().logTrace(traceMessageBuf.toString());
            }
            if (this.enablePacketDebug) {
                this.enqueuePacketForDebugging(false, true, 0, this.packetHeaderBuf, reuse);
            }
            boolean isMultiPacket = false;
            if (packetLength == this.maxThreeBytes) {
                reuse.setPosition(this.maxThreeBytes);
                isMultiPacket = true;
                packetLength = this.readRemainingMultiPackets(reuse, multiPacketSeq);
            }
            if (!isMultiPacket) {
                reuse.getByteBuffer()[packetLength] = 0;
            }
            if (this.connection.getMaintainTimeStats()) {
                this.lastPacketReceivedTimeMs = System.currentTimeMillis();
            }
            return reuse;
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
        catch (OutOfMemoryError oom) {
            try {
                this.clearInputStream();
            }
            catch (Exception ex) {
                // empty catch block
            }
            try {
                this.connection.realClose(false, false, true, oom);
            }
            catch (Exception ex) {
                // empty catch block
            }
            throw oom;
        }
    }

    private int readRemainingMultiPackets(Buffer reuse, byte multiPacketSeq) throws IOException, SQLException {
        int packetLength = -1;
        Buffer multiPacket = null;
        do {
            int lengthRead;
            if ((lengthRead = this.readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4)) < 4) {
                this.forceClose();
                throw new IOException(Messages.getString("MysqlIO.47"));
            }
            packetLength = (this.packetHeaderBuf[0] & 0xFF) + ((this.packetHeaderBuf[1] & 0xFF) << 8) + ((this.packetHeaderBuf[2] & 0xFF) << 16);
            if (multiPacket == null) {
                multiPacket = new Buffer(packetLength);
            }
            if (!this.useNewLargePackets && packetLength == 1) {
                this.clearInputStream();
                break;
            }
            if ((multiPacketSeq = (byte)(multiPacketSeq + 1)) != this.packetHeaderBuf[3]) {
                throw new IOException(Messages.getString("MysqlIO.49"));
            }
            multiPacket.setPosition(0);
            multiPacket.setBufLength(packetLength);
            byte[] byteBuf = multiPacket.getByteBuffer();
            int lengthToWrite = packetLength;
            int bytesRead = this.readFully(this.mysqlInput, byteBuf, 0, packetLength);
            if (bytesRead != lengthToWrite) {
                throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, SQLError.createSQLException(Messages.getString("MysqlIO.50") + lengthToWrite + Messages.getString("MysqlIO.51") + bytesRead + ".", this.getExceptionInterceptor()), this.getExceptionInterceptor());
            }
            reuse.writeBytesNoNull(byteBuf, 0, lengthToWrite);
        } while (packetLength == this.maxThreeBytes);
        reuse.setPosition(0);
        reuse.setWasMultiPacket(true);
        return packetLength;
    }

    private void checkPacketSequencing(byte multiPacketSeq) throws SQLException {
        if (multiPacketSeq == -128 && this.readPacketSequence != 127) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, new IOException("Packets out of order, expected packet # -128, but received packet # " + multiPacketSeq), this.getExceptionInterceptor());
        }
        if (this.readPacketSequence == -1 && multiPacketSeq != 0) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, new IOException("Packets out of order, expected packet # -1, but received packet # " + multiPacketSeq), this.getExceptionInterceptor());
        }
        if (multiPacketSeq != -128 && this.readPacketSequence != -1 && multiPacketSeq != this.readPacketSequence + 1) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, new IOException("Packets out of order, expected packet # " + (this.readPacketSequence + 1) + ", but received packet # " + multiPacketSeq), this.getExceptionInterceptor());
        }
    }

    void enableMultiQueries() throws SQLException {
        Buffer buf = this.getSharedSendPacket();
        buf.clear();
        buf.writeByte((byte)27);
        buf.writeInt(0);
        this.sendCommand(27, null, buf, false, null, 0);
    }

    void disableMultiQueries() throws SQLException {
        Buffer buf = this.getSharedSendPacket();
        buf.clear();
        buf.writeByte((byte)27);
        buf.writeInt(1);
        this.sendCommand(27, null, buf, false, null, 0);
    }

    private final void send(Buffer packet, int packetLen) throws SQLException {
        try {
            if (this.maxAllowedPacket > 0 && packetLen > this.maxAllowedPacket) {
                throw new PacketTooBigException(packetLen, this.maxAllowedPacket);
            }
            if (this.serverMajorVersion >= 4 && (packetLen - 4 >= this.maxThreeBytes || this.useCompression && packetLen - 4 >= this.maxThreeBytes - 3)) {
                this.sendSplitPackets(packet, packetLen);
            } else {
                this.packetSequence = (byte)(this.packetSequence + 1);
                Buffer packetToSend = packet;
                packetToSend.setPosition(0);
                packetToSend.writeLongInt(packetLen - 4);
                packetToSend.writeByte(this.packetSequence);
                if (this.useCompression) {
                    this.compressedPacketSequence = (byte)(this.compressedPacketSequence + 1);
                    int originalPacketLen = packetLen;
                    packetToSend = this.compressPacket(packetToSend, 0, packetLen);
                    packetLen = packetToSend.getPosition();
                    if (this.traceProtocol) {
                        StringBuilder traceMessageBuf = new StringBuilder();
                        traceMessageBuf.append(Messages.getString("MysqlIO.57"));
                        traceMessageBuf.append(MysqlIO.getPacketDumpToLog(packetToSend, packetLen));
                        traceMessageBuf.append(Messages.getString("MysqlIO.58"));
                        traceMessageBuf.append(MysqlIO.getPacketDumpToLog(packet, originalPacketLen));
                        this.connection.getLog().logTrace(traceMessageBuf.toString());
                    }
                } else if (this.traceProtocol) {
                    StringBuilder traceMessageBuf = new StringBuilder();
                    traceMessageBuf.append(Messages.getString("MysqlIO.59"));
                    traceMessageBuf.append("host: '");
                    traceMessageBuf.append(this.host);
                    traceMessageBuf.append("' threadId: '");
                    traceMessageBuf.append(this.threadId);
                    traceMessageBuf.append("'\n");
                    traceMessageBuf.append(packetToSend.dump(packetLen));
                    this.connection.getLog().logTrace(traceMessageBuf.toString());
                }
                this.mysqlOutput.write(packetToSend.getByteBuffer(), 0, packetLen);
                this.mysqlOutput.flush();
            }
            if (this.enablePacketDebug) {
                this.enqueuePacketForDebugging(true, false, packetLen + 5, this.packetHeaderBuf, packet);
            }
            if (packet == this.sharedSendPacket) {
                this.reclaimLargeSharedSendPacket();
            }
            if (this.connection.getMaintainTimeStats()) {
                this.lastPacketSentTimeMs = System.currentTimeMillis();
            }
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private final ResultSetImpl sendFileToServer(StatementImpl callingStatement, String fileName) throws SQLException {
        Buffer filePacket;
        block27: {
            block24: {
                if (this.useCompression) {
                    this.compressedPacketSequence = (byte)(this.compressedPacketSequence + 1);
                }
                filePacket = this.loadFileBufRef == null ? null : this.loadFileBufRef.get();
                int bigPacketLength = Math.min(this.connection.getMaxAllowedPacket() - 12, this.alignPacketSize(this.connection.getMaxAllowedPacket() - 16, 4096) - 12);
                int oneMeg = 0x100000;
                int smallerPacketSizeAligned = Math.min(oneMeg - 12, this.alignPacketSize(oneMeg - 16, 4096) - 12);
                int packetLength = Math.min(smallerPacketSizeAligned, bigPacketLength);
                if (filePacket == null) {
                    try {
                        filePacket = new Buffer(packetLength + 4);
                        this.loadFileBufRef = new SoftReference<Buffer>(filePacket);
                    }
                    catch (OutOfMemoryError oom) {
                        throw SQLError.createSQLException("Could not allocate packet of " + packetLength + " bytes required for LOAD DATA LOCAL INFILE operation." + " Try increasing max heap allocation for JVM or decreasing server variable 'max_allowed_packet'", "S1001", this.getExceptionInterceptor());
                    }
                }
                filePacket.clear();
                this.send(filePacket, 0);
                byte[] fileBuf = new byte[packetLength];
                BufferedInputStream fileIn = null;
                try {
                    try {
                        if (!this.connection.getAllowLoadLocalInfile()) {
                            throw SQLError.createSQLException(Messages.getString("MysqlIO.LoadDataLocalNotAllowed"), "S1000", this.getExceptionInterceptor());
                        }
                        InputStream hookedStream = null;
                        if (callingStatement != null) {
                            hookedStream = callingStatement.getLocalInfileInputStream();
                        }
                        if (hookedStream != null) {
                            fileIn = new BufferedInputStream(hookedStream);
                        } else if (!this.connection.getAllowUrlInLocalInfile()) {
                            fileIn = new BufferedInputStream(new FileInputStream(fileName));
                        } else if (fileName.indexOf(58) != -1) {
                            try {
                                URL urlFromFileName = new URL(fileName);
                                fileIn = new BufferedInputStream(urlFromFileName.openStream());
                            }
                            catch (MalformedURLException badUrlEx) {
                                fileIn = new BufferedInputStream(new FileInputStream(fileName));
                            }
                        } else {
                            fileIn = new BufferedInputStream(new FileInputStream(fileName));
                        }
                        int bytesRead = 0;
                        while ((bytesRead = fileIn.read(fileBuf)) != -1) {
                            filePacket.clear();
                            filePacket.writeBytesNoNull(fileBuf, 0, bytesRead);
                            this.send(filePacket, filePacket.getPosition());
                        }
                        Object var13_17 = null;
                        if (fileIn == null) break block24;
                    }
                    catch (IOException ioEx) {
                        StringBuilder messageBuf = new StringBuilder(Messages.getString("MysqlIO.60"));
                        if (fileName != null && !this.connection.getParanoid()) {
                            messageBuf.append("'");
                            messageBuf.append(fileName);
                            messageBuf.append("'");
                        }
                        messageBuf.append(Messages.getString("MysqlIO.63"));
                        if (this.connection.getParanoid()) throw SQLError.createSQLException(messageBuf.toString(), "S1009", this.getExceptionInterceptor());
                        messageBuf.append(Messages.getString("MysqlIO.64"));
                        messageBuf.append(Util.stackTraceToString(ioEx));
                        throw SQLError.createSQLException(messageBuf.toString(), "S1009", this.getExceptionInterceptor());
                    }
                }
                catch (Throwable throwable) {
                    Object var13_18 = null;
                    if (fileIn == null) {
                        filePacket.clear();
                        this.send(filePacket, filePacket.getPosition());
                        this.checkErrorPacket();
                        throw throwable;
                    }
                    try {
                        fileIn.close();
                    }
                    catch (Exception ex) {
                        SQLException sqlEx = SQLError.createSQLException(Messages.getString("MysqlIO.65"), "S1000", ex, this.getExceptionInterceptor());
                        throw sqlEx;
                    }
                    fileIn = null;
                    throw throwable;
                }
                try {}
                catch (Exception ex) {
                    SQLException sqlEx = SQLError.createSQLException(Messages.getString("MysqlIO.65"), "S1000", ex, this.getExceptionInterceptor());
                    throw sqlEx;
                }
                fileIn.close();
                fileIn = null;
                break block27;
            }
            filePacket.clear();
            this.send(filePacket, filePacket.getPosition());
            this.checkErrorPacket();
        }
        filePacket.clear();
        this.send(filePacket, filePacket.getPosition());
        Buffer resultPacket = this.checkErrorPacket();
        return this.buildResultSetWithUpdates(callingStatement, resultPacket);
    }

    private Buffer checkErrorPacket(int command) throws SQLException {
        Buffer resultPacket = null;
        this.serverStatus = 0;
        try {
            resultPacket = this.reuseAndReadPacket(this.reusablePacket);
        }
        catch (SQLException sqlEx) {
            throw sqlEx;
        }
        catch (Exception fallThru) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, fallThru, this.getExceptionInterceptor());
        }
        this.checkErrorPacket(resultPacket);
        return resultPacket;
    }

    private void checkErrorPacket(Buffer resultPacket) throws SQLException {
        byte statusCode = resultPacket.readByte();
        if (statusCode == -1) {
            int errno = 2000;
            if (this.protocolVersion > 9) {
                errno = resultPacket.readInt();
                String xOpen = null;
                String serverErrorMessage = resultPacket.readString(this.connection.getErrorMessageEncoding(), this.getExceptionInterceptor());
                if (serverErrorMessage.charAt(0) == '#') {
                    if (serverErrorMessage.length() > 6) {
                        xOpen = serverErrorMessage.substring(1, 6);
                        serverErrorMessage = serverErrorMessage.substring(6);
                        if (xOpen.equals("HY000")) {
                            xOpen = SQLError.mysqlToSqlState(errno, this.connection.getUseSqlStateCodes());
                        }
                    } else {
                        xOpen = SQLError.mysqlToSqlState(errno, this.connection.getUseSqlStateCodes());
                    }
                } else {
                    xOpen = SQLError.mysqlToSqlState(errno, this.connection.getUseSqlStateCodes());
                }
                this.clearInputStream();
                StringBuilder errorBuf = new StringBuilder();
                String xOpenErrorMessage = SQLError.get(xOpen);
                if (!this.connection.getUseOnlyServerErrorMessages() && xOpenErrorMessage != null) {
                    errorBuf.append(xOpenErrorMessage);
                    errorBuf.append(Messages.getString("MysqlIO.68"));
                }
                errorBuf.append(serverErrorMessage);
                if (!this.connection.getUseOnlyServerErrorMessages() && xOpenErrorMessage != null) {
                    errorBuf.append("\"");
                }
                this.appendDeadlockStatusInformation(xOpen, errorBuf);
                if (xOpen != null && xOpen.startsWith("22")) {
                    throw new MysqlDataTruncation(errorBuf.toString(), 0, true, false, 0, 0, errno);
                }
                throw SQLError.createSQLException(errorBuf.toString(), xOpen, errno, false, this.getExceptionInterceptor(), this.connection);
            }
            String serverErrorMessage = resultPacket.readString(this.connection.getErrorMessageEncoding(), this.getExceptionInterceptor());
            this.clearInputStream();
            if (serverErrorMessage.indexOf(Messages.getString("MysqlIO.70")) != -1) {
                throw SQLError.createSQLException(SQLError.get("S0022") + ", " + serverErrorMessage, "S0022", -1, false, this.getExceptionInterceptor(), this.connection);
            }
            StringBuilder errorBuf = new StringBuilder(Messages.getString("MysqlIO.72"));
            errorBuf.append(serverErrorMessage);
            errorBuf.append("\"");
            throw SQLError.createSQLException(SQLError.get("S1000") + ", " + errorBuf.toString(), "S1000", -1, false, this.getExceptionInterceptor(), this.connection);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void appendDeadlockStatusInformation(String xOpen, StringBuilder errorBuf) throws SQLException {
        block19: {
            if (this.connection.getIncludeInnodbStatusInDeadlockExceptions() && xOpen != null && (xOpen.startsWith("40") || xOpen.startsWith("41")) && this.streamingData == null) {
                ResultSet rs = null;
                try {
                    Object var6_4;
                    try {
                        rs = this.sqlQueryDirect(null, "SHOW ENGINE INNODB STATUS", this.connection.getEncoding(), null, -1, 1003, 1007, false, this.connection.getCatalog(), null);
                        if (rs.next()) {
                            errorBuf.append("\n\n");
                            errorBuf.append(rs.getString("Status"));
                        } else {
                            errorBuf.append("\n\n");
                            errorBuf.append(Messages.getString("MysqlIO.NoInnoDBStatusFound"));
                        }
                    }
                    catch (Exception ex) {
                        errorBuf.append("\n\n");
                        errorBuf.append(Messages.getString("MysqlIO.InnoDBStatusFailed"));
                        errorBuf.append("\n\n");
                        errorBuf.append(Util.stackTraceToString(ex));
                        var6_4 = null;
                        if (rs != null) {
                            rs.close();
                        }
                        break block19;
                    }
                    var6_4 = null;
                    if (rs == null) break block19;
                }
                catch (Throwable throwable) {
                    Object var6_5 = null;
                    if (rs != null) {
                        rs.close();
                    }
                    throw throwable;
                }
                rs.close();
            }
        }
        if (this.connection.getIncludeThreadDumpInDeadlockExceptions()) {
            errorBuf.append("\n\n*** Java threads running at time of deadlock ***\n\n");
            ThreadMXBean threadMBean = ManagementFactory.getThreadMXBean();
            long[] threadIds = threadMBean.getAllThreadIds();
            ThreadInfo[] threads = threadMBean.getThreadInfo(threadIds, Integer.MAX_VALUE);
            ArrayList<ThreadInfo> activeThreads = new ArrayList<ThreadInfo>();
            for (ThreadInfo info : threads) {
                if (info == null) continue;
                activeThreads.add(info);
            }
            for (ThreadInfo threadInfo : activeThreads) {
                StackTraceElement[] stackTrace;
                errorBuf.append('\"');
                errorBuf.append(threadInfo.getThreadName());
                errorBuf.append("\" tid=");
                errorBuf.append(threadInfo.getThreadId());
                errorBuf.append(" ");
                errorBuf.append((Object)threadInfo.getThreadState());
                if (threadInfo.getLockName() != null) {
                    errorBuf.append(" on lock=" + threadInfo.getLockName());
                }
                if (threadInfo.isSuspended()) {
                    errorBuf.append(" (suspended)");
                }
                if (threadInfo.isInNative()) {
                    errorBuf.append(" (running in native)");
                }
                if ((stackTrace = threadInfo.getStackTrace()).length > 0) {
                    errorBuf.append(" in ");
                    errorBuf.append(stackTrace[0].getClassName());
                    errorBuf.append(".");
                    errorBuf.append(stackTrace[0].getMethodName());
                    errorBuf.append("()");
                }
                errorBuf.append("\n");
                if (threadInfo.getLockOwnerName() != null) {
                    errorBuf.append("\t owned by " + threadInfo.getLockOwnerName() + " Id=" + threadInfo.getLockOwnerId());
                    errorBuf.append("\n");
                }
                for (int j = 0; j < stackTrace.length; ++j) {
                    StackTraceElement ste = stackTrace[j];
                    errorBuf.append("\tat " + ste.toString());
                    errorBuf.append("\n");
                }
            }
        }
    }

    private final void sendSplitPackets(Buffer packet, int packetLen) throws SQLException {
        try {
            int len;
            Buffer toCompress;
            Buffer packetToSend = this.splitBufRef == null ? null : this.splitBufRef.get();
            Buffer buffer = toCompress = !this.useCompression || this.compressBufRef == null ? null : this.compressBufRef.get();
            if (packetToSend == null) {
                packetToSend = new Buffer(this.maxThreeBytes + 4);
                this.splitBufRef = new SoftReference<Buffer>(packetToSend);
            }
            if (this.useCompression) {
                int cbuflen = packetLen + (packetLen / this.maxThreeBytes + 1) * 4;
                if (toCompress == null) {
                    toCompress = new Buffer(cbuflen);
                    this.compressBufRef = new SoftReference<Buffer>(toCompress);
                } else if (toCompress.getBufLength() < cbuflen) {
                    toCompress.setPosition(toCompress.getBufLength());
                    toCompress.ensureCapacity(cbuflen - toCompress.getBufLength());
                }
            }
            int splitSize = this.maxThreeBytes;
            int originalPacketPos = 4;
            byte[] origPacketBytes = packet.getByteBuffer();
            int toCompressPosition = 0;
            for (len = packetLen - 4; len >= 0; len -= this.maxThreeBytes) {
                this.packetSequence = (byte)(this.packetSequence + 1);
                if (len < splitSize) {
                    splitSize = len;
                }
                packetToSend.setPosition(0);
                packetToSend.writeLongInt(splitSize);
                packetToSend.writeByte(this.packetSequence);
                if (len > 0) {
                    System.arraycopy(origPacketBytes, originalPacketPos, packetToSend.getByteBuffer(), 4, splitSize);
                }
                if (this.useCompression) {
                    System.arraycopy(packetToSend.getByteBuffer(), 0, toCompress.getByteBuffer(), toCompressPosition, 4 + splitSize);
                    toCompressPosition += 4 + splitSize;
                } else {
                    this.mysqlOutput.write(packetToSend.getByteBuffer(), 0, 4 + splitSize);
                    this.mysqlOutput.flush();
                }
                originalPacketPos += splitSize;
            }
            if (this.useCompression) {
                len = toCompressPosition;
                toCompressPosition = 0;
                splitSize = this.maxThreeBytes - 3;
                while (len >= 0) {
                    this.compressedPacketSequence = (byte)(this.compressedPacketSequence + 1);
                    if (len < splitSize) {
                        splitSize = len;
                    }
                    Buffer compressedPacketToSend = this.compressPacket(toCompress, toCompressPosition, splitSize);
                    packetLen = compressedPacketToSend.getPosition();
                    this.mysqlOutput.write(compressedPacketToSend.getByteBuffer(), 0, packetLen);
                    this.mysqlOutput.flush();
                    toCompressPosition += splitSize;
                    len -= this.maxThreeBytes - 3;
                }
            }
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
    }

    private void reclaimLargeSharedSendPacket() {
        if (this.sharedSendPacket != null && this.sharedSendPacket.getCapacity() > 0x100000) {
            this.sharedSendPacket = new Buffer(1024);
        }
    }

    boolean hadWarnings() {
        return this.hadWarnings;
    }

    void scanForAndThrowDataTruncation() throws SQLException {
        if (this.streamingData == null && this.versionMeetsMinimum(4, 1, 0) && this.connection.getJdbcCompliantTruncation() && this.warningCount > 0) {
            SQLError.convertShowWarningsToSQLWarnings(this.connection, this.warningCount, true);
        }
    }

    private void secureAuth(Buffer packet, int packLength, String user, String password, String database, boolean writeClientParams) throws SQLException {
        if (packet == null) {
            packet = new Buffer(packLength);
        }
        if (writeClientParams) {
            if (this.use41Extensions) {
                if (this.versionMeetsMinimum(4, 1, 1)) {
                    packet.writeLong(this.clientParam);
                    packet.writeLong(this.maxThreeBytes);
                    packet.writeByte((byte)8);
                    packet.writeBytesNoNull(new byte[23]);
                } else {
                    packet.writeLong(this.clientParam);
                    packet.writeLong(this.maxThreeBytes);
                }
            } else {
                packet.writeInt((int)this.clientParam);
                packet.writeLongInt(this.maxThreeBytes);
            }
        }
        packet.writeString(user, CODE_PAGE_1252, this.connection);
        if (password.length() != 0) {
            packet.writeString(FALSE_SCRAMBLE, CODE_PAGE_1252, this.connection);
        } else {
            packet.writeString("", CODE_PAGE_1252, this.connection);
        }
        if (this.useConnectWithDb) {
            packet.writeString(database, CODE_PAGE_1252, this.connection);
        }
        this.send(packet, packet.getPosition());
        if (password.length() > 0) {
            Buffer b = this.readPacket();
            b.setPosition(0);
            byte[] replyAsBytes = b.getByteBuffer();
            if (replyAsBytes.length == 24 && replyAsBytes[0] != 0) {
                if (replyAsBytes[0] != 42) {
                    try {
                        byte[] buff = Security.passwordHashStage1(password);
                        byte[] passwordHash = new byte[buff.length];
                        System.arraycopy(buff, 0, passwordHash, 0, buff.length);
                        passwordHash = Security.passwordHashStage2(passwordHash, replyAsBytes);
                        byte[] packetDataAfterSalt = new byte[replyAsBytes.length - 4];
                        System.arraycopy(replyAsBytes, 4, packetDataAfterSalt, 0, replyAsBytes.length - 4);
                        byte[] mysqlScrambleBuff = new byte[20];
                        Security.xorString(packetDataAfterSalt, mysqlScrambleBuff, passwordHash, 20);
                        Security.xorString(mysqlScrambleBuff, buff, buff, 20);
                        Buffer packet2 = new Buffer(25);
                        packet2.writeBytesNoNull(buff);
                        this.packetSequence = (byte)(this.packetSequence + 1);
                        this.send(packet2, 24);
                    }
                    catch (NoSuchAlgorithmException nse) {
                        throw SQLError.createSQLException(Messages.getString("MysqlIO.91") + Messages.getString("MysqlIO.92"), "S1000", this.getExceptionInterceptor());
                    }
                }
                try {
                    byte[] passwordHash = Security.createKeyFromOldPassword(password);
                    byte[] netReadPos4 = new byte[replyAsBytes.length - 4];
                    System.arraycopy(replyAsBytes, 4, netReadPos4, 0, replyAsBytes.length - 4);
                    byte[] mysqlScrambleBuff = new byte[20];
                    Security.xorString(netReadPos4, mysqlScrambleBuff, passwordHash, 20);
                    String scrambledPassword = Util.scramble(StringUtils.toString(mysqlScrambleBuff), password);
                    Buffer packet2 = new Buffer(packLength);
                    packet2.writeString(scrambledPassword, CODE_PAGE_1252, this.connection);
                    this.packetSequence = (byte)(this.packetSequence + 1);
                    this.send(packet2, 24);
                }
                catch (NoSuchAlgorithmException nse) {
                    throw SQLError.createSQLException(Messages.getString("MysqlIO.91") + Messages.getString("MysqlIO.92"), "S1000", this.getExceptionInterceptor());
                }
            }
        }
    }

    void secureAuth411(Buffer packet, int packLength, String user, String password, String database, boolean writeClientParams) throws SQLException {
        String enc = this.getEncodingForHandshake();
        if (packet == null) {
            packet = new Buffer(packLength);
        }
        if (writeClientParams) {
            if (this.use41Extensions) {
                if (this.versionMeetsMinimum(4, 1, 1)) {
                    packet.writeLong(this.clientParam);
                    packet.writeLong(this.maxThreeBytes);
                    this.appendCharsetByteForHandshake(packet, enc);
                    packet.writeBytesNoNull(new byte[23]);
                } else {
                    packet.writeLong(this.clientParam);
                    packet.writeLong(this.maxThreeBytes);
                }
            } else {
                packet.writeInt((int)this.clientParam);
                packet.writeLongInt(this.maxThreeBytes);
            }
        }
        if (user != null) {
            packet.writeString(user, enc, this.connection);
        }
        if (password.length() != 0) {
            packet.writeByte((byte)20);
            try {
                packet.writeBytesNoNull(Security.scramble411(password, this.seed, this.connection.getPasswordCharacterEncoding()));
            }
            catch (NoSuchAlgorithmException nse) {
                throw SQLError.createSQLException(Messages.getString("MysqlIO.91") + Messages.getString("MysqlIO.92"), "S1000", this.getExceptionInterceptor());
            }
            catch (UnsupportedEncodingException e) {
                throw SQLError.createSQLException(Messages.getString("MysqlIO.91") + Messages.getString("MysqlIO.92"), "S1000", this.getExceptionInterceptor());
            }
        } else {
            packet.writeByte((byte)0);
        }
        if (this.useConnectWithDb) {
            packet.writeString(database, enc, this.connection);
        } else {
            packet.writeByte((byte)0);
        }
        if ((this.serverCapabilities & 0x100000) != 0) {
            this.sendConnectionAttributes(packet, enc, this.connection);
        }
        this.send(packet, packet.getPosition());
        byte by = this.packetSequence;
        this.packetSequence = (byte)(by + 1);
        byte savePacketSequence = by;
        Buffer reply = this.checkErrorPacket();
        if (reply.isAuthMethodSwitchRequestPacket()) {
            this.packetSequence = savePacketSequence = (byte)(savePacketSequence + 1);
            packet.clear();
            String seed323 = this.seed.substring(0, 8);
            packet.writeString(Util.newCrypt(password, seed323, this.connection.getPasswordCharacterEncoding()));
            this.send(packet, packet.getPosition());
            this.checkErrorPacket();
        }
    }

    private final ResultSetRow unpackBinaryResultSetRow(Field[] fields, Buffer binaryData, int resultSetConcurrency) throws SQLException {
        int numFields = fields.length;
        byte[][] unpackedRowData = new byte[numFields][];
        int nullCount = (numFields + 9) / 8;
        int nullMaskPos = binaryData.getPosition();
        binaryData.setPosition(nullMaskPos + nullCount);
        int bit = 4;
        for (int i = 0; i < numFields; ++i) {
            if ((binaryData.readByte(nullMaskPos) & bit) != 0) {
                unpackedRowData[i] = null;
            } else if (resultSetConcurrency != 1008) {
                this.extractNativeEncodedColumn(binaryData, fields, i, unpackedRowData);
            } else {
                this.unpackNativeEncodedColumn(binaryData, fields, i, unpackedRowData);
            }
            if (((bit <<= 1) & 0xFF) != 0) continue;
            bit = 1;
            ++nullMaskPos;
        }
        return new ByteArrayRow(unpackedRowData, this.getExceptionInterceptor());
    }

    private final void extractNativeEncodedColumn(Buffer binaryData, Field[] fields, int columnIndex, byte[][] unpackedRowData) throws SQLException {
        Field curField = fields[columnIndex];
        switch (curField.getMysqlType()) {
            case 6: {
                break;
            }
            case 1: {
                unpackedRowData[columnIndex] = new byte[]{binaryData.readByte()};
                break;
            }
            case 2: 
            case 13: {
                unpackedRowData[columnIndex] = binaryData.getBytes(2);
                break;
            }
            case 3: 
            case 9: {
                unpackedRowData[columnIndex] = binaryData.getBytes(4);
                break;
            }
            case 8: {
                unpackedRowData[columnIndex] = binaryData.getBytes(8);
                break;
            }
            case 4: {
                unpackedRowData[columnIndex] = binaryData.getBytes(4);
                break;
            }
            case 5: {
                unpackedRowData[columnIndex] = binaryData.getBytes(8);
                break;
            }
            case 11: {
                int length = (int)binaryData.readFieldLength();
                unpackedRowData[columnIndex] = binaryData.getBytes(length);
                break;
            }
            case 10: {
                int length = (int)binaryData.readFieldLength();
                unpackedRowData[columnIndex] = binaryData.getBytes(length);
                break;
            }
            case 7: 
            case 12: {
                int length = (int)binaryData.readFieldLength();
                unpackedRowData[columnIndex] = binaryData.getBytes(length);
                break;
            }
            case 0: 
            case 15: 
            case 16: 
            case 245: 
            case 246: 
            case 249: 
            case 250: 
            case 251: 
            case 252: 
            case 253: 
            case 254: 
            case 255: {
                unpackedRowData[columnIndex] = binaryData.readLenByteArray(0);
                break;
            }
            default: {
                throw SQLError.createSQLException(Messages.getString("MysqlIO.97") + curField.getMysqlType() + Messages.getString("MysqlIO.98") + columnIndex + Messages.getString("MysqlIO.99") + fields.length + Messages.getString("MysqlIO.100"), "S1000", this.getExceptionInterceptor());
            }
        }
    }

    private final void unpackNativeEncodedColumn(Buffer binaryData, Field[] fields, int columnIndex, byte[][] unpackedRowData) throws SQLException {
        Field curField = fields[columnIndex];
        switch (curField.getMysqlType()) {
            case 6: {
                break;
            }
            case 1: {
                byte tinyVal = binaryData.readByte();
                if (!curField.isUnsigned()) {
                    unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(tinyVal));
                    break;
                }
                short unsignedTinyVal = (short)(tinyVal & 0xFF);
                unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(unsignedTinyVal));
                break;
            }
            case 2: 
            case 13: {
                short shortVal = (short)binaryData.readInt();
                if (!curField.isUnsigned()) {
                    unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(shortVal));
                    break;
                }
                int unsignedShortVal = shortVal & 0xFFFF;
                unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(unsignedShortVal));
                break;
            }
            case 3: 
            case 9: {
                int intVal = (int)binaryData.readLong();
                if (!curField.isUnsigned()) {
                    unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(intVal));
                    break;
                }
                long longVal = (long)intVal & 0xFFFFFFFFL;
                unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(longVal));
                break;
            }
            case 8: {
                long longVal = binaryData.readLongLong();
                if (!curField.isUnsigned()) {
                    unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(longVal));
                    break;
                }
                BigInteger asBigInteger = ResultSetImpl.convertLongToUlong(longVal);
                unpackedRowData[columnIndex] = StringUtils.getBytes(asBigInteger.toString());
                break;
            }
            case 4: {
                float floatVal = Float.intBitsToFloat(binaryData.readIntAsLong());
                unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(floatVal));
                break;
            }
            case 5: {
                double doubleVal = Double.longBitsToDouble(binaryData.readLongLong());
                unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(doubleVal));
                break;
            }
            case 11: {
                int length = (int)binaryData.readFieldLength();
                int hour = 0;
                byte minute = 0;
                byte seconds = 0;
                if (length != 0) {
                    binaryData.readByte();
                    binaryData.readLong();
                    hour = binaryData.readByte();
                    minute = binaryData.readByte();
                    seconds = binaryData.readByte();
                    if (length > 8) {
                        binaryData.readLong();
                    }
                }
                byte[] timeAsBytes = new byte[]{(byte)Character.forDigit(hour / 10, 10), (byte)Character.forDigit(hour % 10, 10), 58, (byte)Character.forDigit(minute / 10, 10), (byte)Character.forDigit(minute % 10, 10), 58, (byte)Character.forDigit(seconds / 10, 10), (byte)Character.forDigit(seconds % 10, 10)};
                unpackedRowData[columnIndex] = timeAsBytes;
                break;
            }
            case 10: {
                int length = (int)binaryData.readFieldLength();
                int year = 0;
                byte month = 0;
                byte day = 0;
                boolean hour = false;
                boolean minute = false;
                boolean seconds = false;
                if (length != 0) {
                    year = binaryData.readInt();
                    month = binaryData.readByte();
                    day = binaryData.readByte();
                }
                if (year == 0 && month == 0 && day == 0) {
                    if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior())) {
                        unpackedRowData[columnIndex] = null;
                        break;
                    }
                    if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
                        throw SQLError.createSQLException("Value '0000-00-00' can not be represented as java.sql.Date", "S1009", this.getExceptionInterceptor());
                    }
                    year = 1;
                    month = 1;
                    day = 1;
                }
                byte[] dateAsBytes = new byte[10];
                dateAsBytes[0] = (byte)Character.forDigit(year / 1000, 10);
                int after1000 = year % 1000;
                dateAsBytes[1] = (byte)Character.forDigit(after1000 / 100, 10);
                int after100 = after1000 % 100;
                dateAsBytes[2] = (byte)Character.forDigit(after100 / 10, 10);
                dateAsBytes[3] = (byte)Character.forDigit(after100 % 10, 10);
                dateAsBytes[4] = 45;
                dateAsBytes[5] = (byte)Character.forDigit(month / 10, 10);
                dateAsBytes[6] = (byte)Character.forDigit(month % 10, 10);
                dateAsBytes[7] = 45;
                dateAsBytes[8] = (byte)Character.forDigit(day / 10, 10);
                dateAsBytes[9] = (byte)Character.forDigit(day % 10, 10);
                unpackedRowData[columnIndex] = dateAsBytes;
                break;
            }
            case 7: 
            case 12: {
                int length = (int)binaryData.readFieldLength();
                int year = 0;
                byte month = 0;
                byte day = 0;
                byte hour = 0;
                byte minute = 0;
                byte seconds = 0;
                int nanos = 0;
                if (length != 0) {
                    year = binaryData.readInt();
                    month = binaryData.readByte();
                    day = binaryData.readByte();
                    if (length > 4) {
                        hour = binaryData.readByte();
                        minute = binaryData.readByte();
                        seconds = binaryData.readByte();
                    }
                }
                if (year == 0 && month == 0 && day == 0) {
                    if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior())) {
                        unpackedRowData[columnIndex] = null;
                        break;
                    }
                    if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
                        throw SQLError.createSQLException("Value '0000-00-00' can not be represented as java.sql.Timestamp", "S1009", this.getExceptionInterceptor());
                    }
                    year = 1;
                    month = 1;
                    day = 1;
                }
                int stringLength = 19;
                byte[] nanosAsBytes = StringUtils.getBytes(Integer.toString(nanos));
                byte[] datetimeAsBytes = new byte[stringLength += 1 + nanosAsBytes.length];
                datetimeAsBytes[0] = (byte)Character.forDigit(year / 1000, 10);
                int after1000 = year % 1000;
                datetimeAsBytes[1] = (byte)Character.forDigit(after1000 / 100, 10);
                int after100 = after1000 % 100;
                datetimeAsBytes[2] = (byte)Character.forDigit(after100 / 10, 10);
                datetimeAsBytes[3] = (byte)Character.forDigit(after100 % 10, 10);
                datetimeAsBytes[4] = 45;
                datetimeAsBytes[5] = (byte)Character.forDigit(month / 10, 10);
                datetimeAsBytes[6] = (byte)Character.forDigit(month % 10, 10);
                datetimeAsBytes[7] = 45;
                datetimeAsBytes[8] = (byte)Character.forDigit(day / 10, 10);
                datetimeAsBytes[9] = (byte)Character.forDigit(day % 10, 10);
                datetimeAsBytes[10] = 32;
                datetimeAsBytes[11] = (byte)Character.forDigit(hour / 10, 10);
                datetimeAsBytes[12] = (byte)Character.forDigit(hour % 10, 10);
                datetimeAsBytes[13] = 58;
                datetimeAsBytes[14] = (byte)Character.forDigit(minute / 10, 10);
                datetimeAsBytes[15] = (byte)Character.forDigit(minute % 10, 10);
                datetimeAsBytes[16] = 58;
                datetimeAsBytes[17] = (byte)Character.forDigit(seconds / 10, 10);
                datetimeAsBytes[18] = (byte)Character.forDigit(seconds % 10, 10);
                datetimeAsBytes[19] = 46;
                int nanosOffset = 20;
                System.arraycopy(nanosAsBytes, 0, datetimeAsBytes, 20, nanosAsBytes.length);
                unpackedRowData[columnIndex] = datetimeAsBytes;
                break;
            }
            case 0: 
            case 15: 
            case 16: 
            case 245: 
            case 246: 
            case 249: 
            case 250: 
            case 251: 
            case 252: 
            case 253: 
            case 254: {
                unpackedRowData[columnIndex] = binaryData.readLenByteArray(0);
                break;
            }
            default: {
                throw SQLError.createSQLException(Messages.getString("MysqlIO.97") + curField.getMysqlType() + Messages.getString("MysqlIO.98") + columnIndex + Messages.getString("MysqlIO.99") + fields.length + Messages.getString("MysqlIO.100"), "S1000", this.getExceptionInterceptor());
            }
        }
    }

    private void negotiateSSLConnection(String user, String password, String database, int packLength) throws SQLException {
        if (!ExportControlled.enabled()) {
            throw new ConnectionFeatureNotAvailableException(this.connection, this.lastPacketSentTimeMs, null);
        }
        if ((this.serverCapabilities & 0x8000) != 0) {
            this.clientParam |= 0x8000L;
        }
        this.clientParam |= 0x800L;
        Buffer packet = new Buffer(packLength);
        if (this.use41Extensions) {
            packet.writeLong(this.clientParam);
            packet.writeLong(this.maxThreeBytes);
            this.appendCharsetByteForHandshake(packet, this.getEncodingForHandshake());
            packet.writeBytesNoNull(new byte[23]);
        } else {
            packet.writeInt((int)this.clientParam);
        }
        this.send(packet, packet.getPosition());
        ExportControlled.transformSocketToSSLSocket(this);
    }

    public boolean isSSLEstablished() {
        return ExportControlled.enabled() && ExportControlled.isSSLEstablished(this);
    }

    protected int getServerStatus() {
        return this.serverStatus;
    }

    protected List<ResultSetRow> fetchRowsViaCursor(List<ResultSetRow> fetchedRows, long statementId, Field[] columnTypes, int fetchSize, boolean useBufferRowExplicit) throws SQLException {
        if (fetchedRows == null) {
            fetchedRows = new ArrayList<ResultSetRow>(fetchSize);
        } else {
            fetchedRows.clear();
        }
        this.sharedSendPacket.clear();
        this.sharedSendPacket.writeByte((byte)28);
        this.sharedSendPacket.writeLong(statementId);
        this.sharedSendPacket.writeLong(fetchSize);
        this.sendCommand(28, null, this.sharedSendPacket, true, null, 0);
        ResultSetRow row = null;
        while ((row = this.nextRow(columnTypes, columnTypes.length, true, 1007, false, useBufferRowExplicit, false, null)) != null) {
            fetchedRows.add(row);
        }
        return fetchedRows;
    }

    protected long getThreadId() {
        return this.threadId;
    }

    protected boolean useNanosForElapsedTime() {
        return this.useNanosForElapsedTime;
    }

    protected long getSlowQueryThreshold() {
        return this.slowQueryThreshold;
    }

    protected String getQueryTimingUnits() {
        return this.queryTimingUnits;
    }

    protected int getCommandCount() {
        return this.commandCount;
    }

    private void checkTransactionState(int oldStatus) throws SQLException {
        boolean previouslyInTrans = (oldStatus & 1) != 0;
        boolean currentlyInTrans = this.inTransactionOnServer();
        if (previouslyInTrans && !currentlyInTrans) {
            this.connection.transactionCompleted();
        } else if (!previouslyInTrans && currentlyInTrans) {
            this.connection.transactionBegun();
        }
    }

    private void preserveOldTransactionState() {
        this.serverStatus |= this.oldServerStatus & 1;
    }

    protected void setStatementInterceptors(List<StatementInterceptorV2> statementInterceptors) {
        this.statementInterceptors = statementInterceptors.isEmpty() ? null : statementInterceptors;
    }

    protected ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }

    protected void setSocketTimeout(int milliseconds) throws SQLException {
        try {
            this.mysqlConnection.setSoTimeout(milliseconds);
        }
        catch (SocketException e) {
            SQLException sqlEx = SQLError.createSQLException("Invalid socket timeout value or state", "S1009", this.getExceptionInterceptor());
            sqlEx.initCause(e);
            throw sqlEx;
        }
    }

    protected void releaseResources() {
        if (this.deflater != null) {
            this.deflater.end();
            this.deflater = null;
        }
    }

    String getEncodingForHandshake() {
        String enc = this.connection.getEncoding();
        if (enc == null) {
            enc = "UTF-8";
        }
        return enc;
    }

    private void appendCharsetByteForHandshake(Buffer packet, String enc) throws SQLException {
        int charsetIndex = 0;
        if (enc != null) {
            charsetIndex = CharsetMapping.getCollationIndexForJavaEncoding(enc, this.connection);
        }
        if (charsetIndex == 0) {
            charsetIndex = 33;
        }
        if (charsetIndex > 255) {
            throw SQLError.createSQLException("Invalid character set index for encoding: " + enc, "S1009", this.getExceptionInterceptor());
        }
        packet.writeByte((byte)charsetIndex);
    }

    public boolean isEOFDeprecated() {
        return (this.clientParam & 0x1000000L) != 0L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static {
        OutputStreamWriter outWriter = null;
        try {
            outWriter = new OutputStreamWriter(new ByteArrayOutputStream());
            jvmPlatformCharset = outWriter.getEncoding();
            Object var2_1 = null;
        }
        catch (Throwable throwable) {
            Object var2_2 = null;
            try {
                if (outWriter != null) {
                    outWriter.close();
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
            throw throwable;
        }
        try {
            if (outWriter != null) {
                outWriter.close();
            }
        }
        catch (IOException iOException) {}
    }
}


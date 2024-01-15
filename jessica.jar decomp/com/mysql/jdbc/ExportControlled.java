/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.MysqlIO;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.SocketFactory;
import com.mysql.jdbc.SocketMetadata;
import com.mysql.jdbc.StringUtils;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.util.Base64Decoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class ExportControlled {
    private static final String SQL_STATE_BAD_SSL_PARAMS = "08000";

    protected static boolean enabled() {
        return true;
    }

    protected static void transformSocketToSSLSocket(MysqlIO mysqlIO) throws SQLException {
        StandardSSLSocketFactory sslFact = new StandardSSLSocketFactory(ExportControlled.getSSLSocketFactoryDefaultOrConfigured(mysqlIO), mysqlIO.socketFactory, mysqlIO.mysqlConnection);
        try {
            String[] stringArray;
            mysqlIO.mysqlConnection = sslFact.connect(mysqlIO.host, mysqlIO.port, null);
            ArrayList<String> allowedProtocols = new ArrayList<String>();
            List<String> supportedProtocols = Arrays.asList(((SSLSocket)mysqlIO.mysqlConnection).getSupportedProtocols());
            if (mysqlIO.versionMeetsMinimum(5, 6, 0) && Util.isEnterpriseEdition(mysqlIO.getServerVersion())) {
                String[] stringArray2 = new String[3];
                stringArray2[0] = "TLSv1.2";
                stringArray2[1] = "TLSv1.1";
                stringArray = stringArray2;
                stringArray2[2] = "TLSv1";
            } else {
                String[] stringArray3 = new String[2];
                stringArray3[0] = "TLSv1.1";
                stringArray = stringArray3;
                stringArray3[1] = "TLSv1";
            }
            for (String protocol : stringArray) {
                if (!supportedProtocols.contains(protocol)) continue;
                allowedProtocols.add(protocol);
            }
            ((SSLSocket)mysqlIO.mysqlConnection).setEnabledProtocols(allowedProtocols.toArray(new String[0]));
            String enabledSSLCipherSuites = mysqlIO.connection.getEnabledSSLCipherSuites();
            boolean overrideCiphers = enabledSSLCipherSuites != null && enabledSSLCipherSuites.length() > 0;
            ArrayList<String> allowedCiphers = null;
            if (overrideCiphers) {
                allowedCiphers = new ArrayList<String>();
                List<String> availableCiphers = Arrays.asList(((SSLSocket)mysqlIO.mysqlConnection).getEnabledCipherSuites());
                for (String cipher : enabledSSLCipherSuites.split("\\s*,\\s*")) {
                    if (!availableCiphers.contains(cipher)) continue;
                    allowedCiphers.add(cipher);
                }
            } else {
                boolean disableDHAlgorithm = false;
                if (mysqlIO.versionMeetsMinimum(5, 5, 45) && !mysqlIO.versionMeetsMinimum(5, 6, 0) || mysqlIO.versionMeetsMinimum(5, 6, 26) && !mysqlIO.versionMeetsMinimum(5, 7, 0) || mysqlIO.versionMeetsMinimum(5, 7, 6)) {
                    if (Util.getJVMVersion() < 8) {
                        disableDHAlgorithm = true;
                    }
                } else if (Util.getJVMVersion() >= 8) {
                    disableDHAlgorithm = true;
                }
                if (disableDHAlgorithm) {
                    allowedCiphers = new ArrayList();
                    for (String cipher : ((SSLSocket)mysqlIO.mysqlConnection).getEnabledCipherSuites()) {
                        if (disableDHAlgorithm && (cipher.indexOf("_DHE_") > -1 || cipher.indexOf("_DH_") > -1)) continue;
                        allowedCiphers.add(cipher);
                    }
                }
            }
            if (allowedCiphers != null) {
                ((SSLSocket)mysqlIO.mysqlConnection).setEnabledCipherSuites(allowedCiphers.toArray(new String[0]));
            }
            ((SSLSocket)mysqlIO.mysqlConnection).startHandshake();
            mysqlIO.mysqlInput = mysqlIO.connection.getUseUnbufferedInput() ? mysqlIO.mysqlConnection.getInputStream() : new BufferedInputStream(mysqlIO.mysqlConnection.getInputStream(), 16384);
            mysqlIO.mysqlOutput = new BufferedOutputStream(mysqlIO.mysqlConnection.getOutputStream(), 16384);
            mysqlIO.mysqlOutput.flush();
            mysqlIO.socketFactory = sslFact;
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(mysqlIO.connection, mysqlIO.getLastPacketSentTimeMs(), mysqlIO.getLastPacketReceivedTimeMs(), ioEx, mysqlIO.getExceptionInterceptor());
        }
    }

    private ExportControlled() {
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static SSLSocketFactory getSSLSocketFactoryDefaultOrConfigured(MysqlIO mysqlIO) throws SQLException {
        KeyManagerFactory kmf;
        TrustManagerFactory tmf;
        String clientCertificateKeyStoreUrl;
        block36: {
            char[] password;
            URL ksURL;
            InputStream ksIS;
            String trustCertificateKeyStorePassword;
            String trustCertificateKeyStoreType;
            String trustCertificateKeyStoreUrl;
            block35: {
                clientCertificateKeyStoreUrl = mysqlIO.connection.getClientCertificateKeyStoreUrl();
                trustCertificateKeyStoreUrl = mysqlIO.connection.getTrustCertificateKeyStoreUrl();
                String clientCertificateKeyStoreType = mysqlIO.connection.getClientCertificateKeyStoreType();
                String clientCertificateKeyStorePassword = mysqlIO.connection.getClientCertificateKeyStorePassword();
                trustCertificateKeyStoreType = mysqlIO.connection.getTrustCertificateKeyStoreType();
                trustCertificateKeyStorePassword = mysqlIO.connection.getTrustCertificateKeyStorePassword();
                if (StringUtils.isNullOrEmpty(clientCertificateKeyStoreUrl) && StringUtils.isNullOrEmpty(trustCertificateKeyStoreUrl) && mysqlIO.connection.getVerifyServerCertificate()) {
                    return (SSLSocketFactory)SSLSocketFactory.getDefault();
                }
                tmf = null;
                kmf = null;
                try {
                    tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                }
                catch (NoSuchAlgorithmException nsae) {
                    throw SQLError.createSQLException("Default algorithm definitions for TrustManager and/or KeyManager are invalid.  Check java security properties file.", SQL_STATE_BAD_SSL_PARAMS, 0, false, mysqlIO.getExceptionInterceptor());
                }
                if (!StringUtils.isNullOrEmpty(clientCertificateKeyStoreUrl)) {
                    ksIS = null;
                    try {
                        try {
                            if (!StringUtils.isNullOrEmpty(clientCertificateKeyStoreType)) {
                                KeyStore clientKeyStore = KeyStore.getInstance(clientCertificateKeyStoreType);
                                ksURL = new URL(clientCertificateKeyStoreUrl);
                                password = clientCertificateKeyStorePassword == null ? new char[]{} : clientCertificateKeyStorePassword.toCharArray();
                                ksIS = ksURL.openStream();
                                clientKeyStore.load(ksIS, password);
                                kmf.init(clientKeyStore, password);
                            }
                        }
                        catch (UnrecoverableKeyException uke) {
                            throw SQLError.createSQLException("Could not recover keys from client keystore.  Check password?", SQL_STATE_BAD_SSL_PARAMS, 0, false, mysqlIO.getExceptionInterceptor());
                        }
                        catch (NoSuchAlgorithmException nsae) {
                            throw SQLError.createSQLException("Unsupported keystore algorithm [" + nsae.getMessage() + "]", SQL_STATE_BAD_SSL_PARAMS, 0, false, mysqlIO.getExceptionInterceptor());
                        }
                        catch (KeyStoreException kse) {
                            throw SQLError.createSQLException("Could not create KeyStore instance [" + kse.getMessage() + "]", SQL_STATE_BAD_SSL_PARAMS, 0, false, mysqlIO.getExceptionInterceptor());
                        }
                        catch (CertificateException nsae) {
                            throw SQLError.createSQLException("Could not load client" + clientCertificateKeyStoreType + " keystore from " + clientCertificateKeyStoreUrl, mysqlIO.getExceptionInterceptor());
                        }
                        catch (MalformedURLException mue) {
                            throw SQLError.createSQLException(clientCertificateKeyStoreUrl + " does not appear to be a valid URL.", SQL_STATE_BAD_SSL_PARAMS, 0, false, mysqlIO.getExceptionInterceptor());
                        }
                        catch (IOException ioe) {
                            SQLException sqlEx = SQLError.createSQLException("Cannot open " + clientCertificateKeyStoreUrl + " [" + ioe.getMessage() + "]", SQL_STATE_BAD_SSL_PARAMS, 0, false, mysqlIO.getExceptionInterceptor());
                            sqlEx.initCause(ioe);
                            throw sqlEx;
                        }
                        Object var14_28 = null;
                        if (ksIS == null) break block35;
                    }
                    catch (Throwable throwable) {
                        Object var14_29 = null;
                        if (ksIS == null) throw throwable;
                        try {
                            ksIS.close();
                            throw throwable;
                        }
                        catch (IOException e) {
                            // empty catch block
                        }
                        throw throwable;
                    }
                    try {}
                    catch (IOException e) {}
                    ksIS.close();
                }
            }
            if (!StringUtils.isNullOrEmpty(trustCertificateKeyStoreUrl)) {
                ksIS = null;
                try {
                    try {
                        if (!StringUtils.isNullOrEmpty(trustCertificateKeyStoreType)) {
                            KeyStore trustKeyStore = KeyStore.getInstance(trustCertificateKeyStoreType);
                            ksURL = new URL(trustCertificateKeyStoreUrl);
                            password = trustCertificateKeyStorePassword == null ? new char[]{} : trustCertificateKeyStorePassword.toCharArray();
                            ksIS = ksURL.openStream();
                            trustKeyStore.load(ksIS, password);
                            tmf.init(trustKeyStore);
                        }
                    }
                    catch (NoSuchAlgorithmException nsae) {
                        throw SQLError.createSQLException("Unsupported keystore algorithm [" + nsae.getMessage() + "]", SQL_STATE_BAD_SSL_PARAMS, 0, false, mysqlIO.getExceptionInterceptor());
                    }
                    catch (KeyStoreException kse) {
                        throw SQLError.createSQLException("Could not create KeyStore instance [" + kse.getMessage() + "]", SQL_STATE_BAD_SSL_PARAMS, 0, false, mysqlIO.getExceptionInterceptor());
                    }
                    catch (CertificateException nsae) {
                        throw SQLError.createSQLException("Could not load trust" + trustCertificateKeyStoreType + " keystore from " + trustCertificateKeyStoreUrl, SQL_STATE_BAD_SSL_PARAMS, 0, false, mysqlIO.getExceptionInterceptor());
                    }
                    catch (MalformedURLException mue) {
                        throw SQLError.createSQLException(trustCertificateKeyStoreUrl + " does not appear to be a valid URL.", SQL_STATE_BAD_SSL_PARAMS, 0, false, mysqlIO.getExceptionInterceptor());
                    }
                    catch (IOException ioe) {
                        SQLException sqlEx = SQLError.createSQLException("Cannot open " + trustCertificateKeyStoreUrl + " [" + ioe.getMessage() + "]", SQL_STATE_BAD_SSL_PARAMS, 0, false, mysqlIO.getExceptionInterceptor());
                        sqlEx.initCause(ioe);
                        throw sqlEx;
                    }
                    Object var17_33 = null;
                    if (ksIS == null) break block36;
                }
                catch (Throwable throwable) {
                    Object var17_34 = null;
                    if (ksIS == null) throw throwable;
                    try {
                        ksIS.close();
                        throw throwable;
                    }
                    catch (IOException e) {
                        // empty catch block
                    }
                    throw throwable;
                }
                try {}
                catch (IOException e) {}
                ksIS.close();
            }
        }
        SSLContext sslContext = null;
        try {
            TrustManager[] trustManagerArray;
            sslContext = SSLContext.getInstance("TLS");
            KeyManager[] keyManagerArray = StringUtils.isNullOrEmpty(clientCertificateKeyStoreUrl) ? null : kmf.getKeyManagers();
            if (mysqlIO.connection.getVerifyServerCertificate()) {
                trustManagerArray = tmf.getTrustManagers();
            } else {
                X509TrustManager[] x509TrustManagerArray = new X509TrustManager[1];
                trustManagerArray = x509TrustManagerArray;
                x509TrustManagerArray[0] = new X509TrustManager(){

                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                };
            }
            sslContext.init(keyManagerArray, trustManagerArray, null);
            return sslContext.getSocketFactory();
        }
        catch (NoSuchAlgorithmException nsae) {
            throw SQLError.createSQLException("TLS is not a valid SSL protocol.", SQL_STATE_BAD_SSL_PARAMS, 0, false, mysqlIO.getExceptionInterceptor());
        }
        catch (KeyManagementException kme) {
            throw SQLError.createSQLException("KeyManagementException: " + kme.getMessage(), SQL_STATE_BAD_SSL_PARAMS, 0, false, mysqlIO.getExceptionInterceptor());
        }
    }

    public static boolean isSSLEstablished(MysqlIO mysqlIO) {
        return SSLSocket.class.isAssignableFrom(mysqlIO.mysqlConnection.getClass());
    }

    public static RSAPublicKey decodeRSAPublicKey(String key, ExceptionInterceptor interceptor) throws SQLException {
        try {
            if (key == null) {
                throw new SQLException("key parameter is null");
            }
            int offset = key.indexOf("\n") + 1;
            int len = key.indexOf("-----END PUBLIC KEY-----") - offset;
            byte[] certificateData = Base64Decoder.decode(key.getBytes(), offset, len);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(certificateData);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey)kf.generatePublic(spec);
        }
        catch (Exception ex) {
            throw SQLError.createSQLException("Unable to decode public key", "S1009", ex, interceptor);
        }
    }

    public static byte[] encryptWithRSAPublicKey(byte[] source, RSAPublicKey key, ExceptionInterceptor interceptor) throws SQLException {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(1, key);
            return cipher.doFinal(source);
        }
        catch (Exception ex) {
            throw SQLError.createSQLException(ex.getMessage(), "S1009", ex, interceptor);
        }
    }

    public static class StandardSSLSocketFactory
    implements SocketFactory,
    SocketMetadata {
        private SSLSocket rawSocket = null;
        private final SSLSocketFactory sslFact;
        private final SocketFactory existingSocketFactory;
        private final Socket existingSocket;

        public StandardSSLSocketFactory(SSLSocketFactory sslFact, SocketFactory existingSocketFactory, Socket existingSocket) {
            this.sslFact = sslFact;
            this.existingSocketFactory = existingSocketFactory;
            this.existingSocket = existingSocket;
        }

        public Socket afterHandshake() throws SocketException, IOException {
            this.existingSocketFactory.afterHandshake();
            return this.rawSocket;
        }

        public Socket beforeHandshake() throws SocketException, IOException {
            return this.rawSocket;
        }

        public Socket connect(String host, int portNumber, Properties props) throws SocketException, IOException {
            this.rawSocket = (SSLSocket)this.sslFact.createSocket(this.existingSocket, host, portNumber, true);
            return this.rawSocket;
        }

        public boolean isLocallyConnected(ConnectionImpl conn) throws SQLException {
            return SocketMetadata.Helper.isLocallyConnected(conn);
        }
    }
}


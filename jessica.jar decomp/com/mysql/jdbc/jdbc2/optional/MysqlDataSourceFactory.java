/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class MysqlDataSourceFactory
implements ObjectFactory {
    protected static final String DATA_SOURCE_CLASS_NAME = "com.mysql.jdbc.jdbc2.optional.MysqlDataSource";
    protected static final String POOL_DATA_SOURCE_CLASS_NAME = "com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource";
    protected static final String XA_DATA_SOURCE_CLASS_NAME = "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource";

    @Override
    public Object getObjectInstance(Object refObj, Name nm, Context ctx, Hashtable<?, ?> env) throws Exception {
        Reference ref = (Reference)refObj;
        String className = ref.getClassName();
        if (className != null && (className.equals(DATA_SOURCE_CLASS_NAME) || className.equals(POOL_DATA_SOURCE_CLASS_NAME) || className.equals(XA_DATA_SOURCE_CLASS_NAME))) {
            String explicitUrlAsString;
            String databaseName;
            String serverName;
            String password;
            MysqlDataSource dataSource = null;
            try {
                dataSource = (MysqlDataSource)Class.forName(className).newInstance();
            }
            catch (Exception ex) {
                throw new RuntimeException("Unable to create DataSource of class '" + className + "', reason: " + ex.toString());
            }
            int portNumber = 3306;
            String portNumberAsString = this.nullSafeRefAddrStringGet("port", ref);
            if (portNumberAsString != null) {
                portNumber = Integer.parseInt(portNumberAsString);
            }
            dataSource.setPort(portNumber);
            String user = this.nullSafeRefAddrStringGet("user", ref);
            if (user != null) {
                dataSource.setUser(user);
            }
            if ((password = this.nullSafeRefAddrStringGet("password", ref)) != null) {
                dataSource.setPassword(password);
            }
            if ((serverName = this.nullSafeRefAddrStringGet("serverName", ref)) != null) {
                dataSource.setServerName(serverName);
            }
            if ((databaseName = this.nullSafeRefAddrStringGet("databaseName", ref)) != null) {
                dataSource.setDatabaseName(databaseName);
            }
            if ((explicitUrlAsString = this.nullSafeRefAddrStringGet("explicitUrl", ref)) != null && Boolean.valueOf(explicitUrlAsString).booleanValue()) {
                dataSource.setUrl(this.nullSafeRefAddrStringGet("url", ref));
            }
            dataSource.setPropertiesViaRef(ref);
            return dataSource;
        }
        return null;
    }

    private String nullSafeRefAddrStringGet(String referenceName, Reference ref) {
        RefAddr refAddr = ref.get(referenceName);
        String asString = refAddr != null ? (String)refAddr.getContent() : null;
        return asString;
    }
}


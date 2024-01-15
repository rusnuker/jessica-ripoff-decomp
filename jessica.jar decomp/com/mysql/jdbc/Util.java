/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.Extension;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MultiHostConnectionProxy;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.StringUtils;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Util {
    private static Util enclosingInstance;
    private static boolean isJdbc4;
    private static boolean isJdbc42;
    private static int jvmVersion;
    private static int jvmUpdateNumber;
    private static boolean isColdFusion;
    private static final ConcurrentMap<Class<?>, Boolean> isJdbcInterfaceCache;
    private static final String MYSQL_JDBC_PACKAGE_ROOT;
    private static final ConcurrentMap<Class<?>, Class<?>[]> implementedInterfacesCache;

    public static boolean isJdbc4() {
        return isJdbc4;
    }

    public static boolean isJdbc42() {
        return isJdbc42;
    }

    public static int getJVMVersion() {
        return jvmVersion;
    }

    public static boolean jvmMeetsMinimum(int version, int updateNumber) {
        return Util.getJVMVersion() > version || Util.getJVMVersion() == version && Util.getJVMUpdateNumber() >= updateNumber;
    }

    public static int getJVMUpdateNumber() {
        return jvmUpdateNumber;
    }

    public static boolean isColdFusion() {
        return isColdFusion;
    }

    public static boolean isCommunityEdition(String serverVersion) {
        return !Util.isEnterpriseEdition(serverVersion);
    }

    public static boolean isEnterpriseEdition(String serverVersion) {
        return serverVersion.contains("enterprise") || serverVersion.contains("commercial") || serverVersion.contains("advanced");
    }

    public static String newCrypt(String password, String seed, String encoding) {
        byte b;
        double d;
        int i;
        if (password == null || password.length() == 0) {
            return password;
        }
        long[] pw = Util.newHash(seed.getBytes());
        long[] msg = Util.hashPre41Password(password, encoding);
        long max = 0x3FFFFFFFL;
        long seed1 = (pw[0] ^ msg[0]) % max;
        long seed2 = (pw[1] ^ msg[1]) % max;
        char[] chars = new char[seed.length()];
        for (i = 0; i < seed.length(); ++i) {
            seed1 = (seed1 * 3L + seed2) % max;
            seed2 = (seed1 + seed2 + 33L) % max;
            d = (double)seed1 / (double)max;
            b = (byte)Math.floor(d * 31.0 + 64.0);
            chars[i] = (char)b;
        }
        seed1 = (seed1 * 3L + seed2) % max;
        seed2 = (seed1 + seed2 + 33L) % max;
        d = (double)seed1 / (double)max;
        b = (byte)Math.floor(d * 31.0);
        i = 0;
        while (i < seed.length()) {
            int n = i++;
            chars[n] = (char)(chars[n] ^ (char)b);
        }
        return new String(chars);
    }

    public static long[] hashPre41Password(String password, String encoding) {
        try {
            return Util.newHash(password.replaceAll("\\s", "").getBytes(encoding));
        }
        catch (UnsupportedEncodingException e) {
            return new long[0];
        }
    }

    public static long[] hashPre41Password(String password) {
        return Util.hashPre41Password(password, Charset.defaultCharset().name());
    }

    static long[] newHash(byte[] password) {
        long nr = 1345345333L;
        long add = 7L;
        long nr2 = 305419889L;
        for (byte b : password) {
            long tmp = 0xFF & b;
            nr ^= ((nr & 0x3FL) + add) * tmp + (nr << 8);
            nr2 += nr2 << 8 ^ nr;
            add += tmp;
        }
        long[] result = new long[]{nr & Integer.MAX_VALUE, nr2 & Integer.MAX_VALUE};
        return result;
    }

    public static String oldCrypt(String password, String seed) {
        long max = 0x1FFFFFFL;
        if (password == null || password.length() == 0) {
            return password;
        }
        long hp = Util.oldHash(seed);
        long hm = Util.oldHash(password);
        long nr = hp ^ hm;
        long s1 = nr %= max;
        long s2 = nr / 2L;
        char[] chars = new char[seed.length()];
        for (int i = 0; i < seed.length(); ++i) {
            s1 = (s1 * 3L + s2) % max;
            s2 = (s1 + s2 + 33L) % max;
            double d = (double)s1 / (double)max;
            byte b = (byte)Math.floor(d * 31.0 + 64.0);
            chars[i] = (char)b;
        }
        return new String(chars);
    }

    static long oldHash(String password) {
        long nr = 1345345333L;
        long nr2 = 7L;
        for (int i = 0; i < password.length(); ++i) {
            if (password.charAt(i) == ' ' || password.charAt(i) == '\t') continue;
            long tmp = password.charAt(i);
            nr ^= ((nr & 0x3FL) + nr2) * tmp + (nr << 8);
            nr2 += tmp;
        }
        return nr & Integer.MAX_VALUE;
    }

    private static RandStructcture randomInit(long seed1, long seed2) {
        RandStructcture randStruct = enclosingInstance.new RandStructcture();
        randStruct.maxValue = 0x3FFFFFFFL;
        randStruct.maxValueDbl = randStruct.maxValue;
        randStruct.seed1 = seed1 % randStruct.maxValue;
        randStruct.seed2 = seed2 % randStruct.maxValue;
        return randStruct;
    }

    public static Object readObject(ResultSet resultSet, int index) throws Exception {
        ObjectInputStream objIn = new ObjectInputStream(resultSet.getBinaryStream(index));
        Object obj = objIn.readObject();
        objIn.close();
        return obj;
    }

    private static double rnd(RandStructcture randStruct) {
        randStruct.seed1 = (randStruct.seed1 * 3L + randStruct.seed2) % randStruct.maxValue;
        randStruct.seed2 = (randStruct.seed1 + randStruct.seed2 + 33L) % randStruct.maxValue;
        return (double)randStruct.seed1 / randStruct.maxValueDbl;
    }

    public static String scramble(String message, String password) {
        byte[] to = new byte[8];
        String val = "";
        message = message.substring(0, 8);
        if (password != null && password.length() > 0) {
            long[] hashPass = Util.hashPre41Password(password);
            long[] hashMessage = Util.newHash(message.getBytes());
            RandStructcture randStruct = Util.randomInit(hashPass[0] ^ hashMessage[0], hashPass[1] ^ hashMessage[1]);
            int msgPos = 0;
            int msgLength = message.length();
            int toPos = 0;
            while (msgPos++ < msgLength) {
                to[toPos++] = (byte)(Math.floor(Util.rnd(randStruct) * 31.0) + 64.0);
            }
            byte extra = (byte)Math.floor(Util.rnd(randStruct) * 31.0);
            int i = 0;
            while (i < to.length) {
                int n = i++;
                to[n] = (byte)(to[n] ^ extra);
            }
            val = StringUtils.toString(to);
        }
        return val;
    }

    public static String stackTraceToString(Throwable ex) {
        StringBuilder traceBuf = new StringBuilder();
        traceBuf.append(Messages.getString("Util.1"));
        if (ex != null) {
            traceBuf.append(ex.getClass().getName());
            String message = ex.getMessage();
            if (message != null) {
                traceBuf.append(Messages.getString("Util.2"));
                traceBuf.append(message);
            }
            StringWriter out = new StringWriter();
            PrintWriter printOut = new PrintWriter(out);
            ex.printStackTrace(printOut);
            traceBuf.append(Messages.getString("Util.3"));
            traceBuf.append(out.toString());
        }
        traceBuf.append(Messages.getString("Util.4"));
        return traceBuf.toString();
    }

    public static Object getInstance(String className, Class<?>[] argTypes, Object[] args, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            return Util.handleNewInstance(Class.forName(className).getConstructor(argTypes), args, exceptionInterceptor);
        }
        catch (SecurityException e) {
            throw SQLError.createSQLException("Can't instantiate required class", "S1000", e, exceptionInterceptor);
        }
        catch (NoSuchMethodException e) {
            throw SQLError.createSQLException("Can't instantiate required class", "S1000", e, exceptionInterceptor);
        }
        catch (ClassNotFoundException e) {
            throw SQLError.createSQLException("Can't instantiate required class", "S1000", e, exceptionInterceptor);
        }
    }

    public static final Object handleNewInstance(Constructor<?> ctor, Object[] args, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            return ctor.newInstance(args);
        }
        catch (IllegalArgumentException e) {
            throw SQLError.createSQLException("Can't instantiate required class", "S1000", e, exceptionInterceptor);
        }
        catch (InstantiationException e) {
            throw SQLError.createSQLException("Can't instantiate required class", "S1000", e, exceptionInterceptor);
        }
        catch (IllegalAccessException e) {
            throw SQLError.createSQLException("Can't instantiate required class", "S1000", e, exceptionInterceptor);
        }
        catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof SQLException) {
                throw (SQLException)target;
            }
            if (target instanceof ExceptionInInitializerError) {
                target = ((ExceptionInInitializerError)target).getException();
            }
            throw SQLError.createSQLException(target.toString(), "S1000", target, exceptionInterceptor);
        }
    }

    public static boolean interfaceExists(String hostname) {
        try {
            Class<?> networkInterfaceClass = Class.forName("java.net.NetworkInterface");
            return networkInterfaceClass.getMethod("getByName", null).invoke(networkInterfaceClass, hostname) != null;
        }
        catch (Throwable t) {
            return false;
        }
    }

    public static void resultSetToMap(Map mappedValues, ResultSet rs) throws SQLException {
        while (rs.next()) {
            mappedValues.put(rs.getObject(1), rs.getObject(2));
        }
    }

    public static void resultSetToMap(Map mappedValues, ResultSet rs, int key, int value) throws SQLException {
        while (rs.next()) {
            mappedValues.put(rs.getObject(key), rs.getObject(value));
        }
    }

    public static void resultSetToMap(Map mappedValues, ResultSet rs, String key, String value) throws SQLException {
        while (rs.next()) {
            mappedValues.put(rs.getObject(key), rs.getObject(value));
        }
    }

    public static Map<Object, Object> calculateDifferences(Map<?, ?> map1, Map<?, ?> map2) {
        HashMap<Object, Object> diffMap = new HashMap<Object, Object>();
        for (Map.Entry<?, ?> entry : map1.entrySet()) {
            Object key = entry.getKey();
            Number value1 = null;
            Number value2 = null;
            if (entry.getValue() instanceof Number) {
                value1 = (Number)entry.getValue();
                value2 = (Number)map2.get(key);
            } else {
                try {
                    value1 = new Double(entry.getValue().toString());
                    value2 = new Double(map2.get(key).toString());
                }
                catch (NumberFormatException nfe) {
                    continue;
                }
            }
            if (value1.equals(value2)) continue;
            if (value1 instanceof Byte) {
                diffMap.put(key, (byte)((Byte)value2 - (Byte)value1));
                continue;
            }
            if (value1 instanceof Short) {
                diffMap.put(key, (short)((Short)value2 - (Short)value1));
                continue;
            }
            if (value1 instanceof Integer) {
                diffMap.put(key, (Integer)value2 - (Integer)value1);
                continue;
            }
            if (value1 instanceof Long) {
                diffMap.put(key, (Long)value2 - (Long)value1);
                continue;
            }
            if (value1 instanceof Float) {
                diffMap.put(key, Float.valueOf(((Float)value2).floatValue() - ((Float)value1).floatValue()));
                continue;
            }
            if (value1 instanceof Double) {
                diffMap.put(key, ((Double)value2).shortValue() - ((Double)value1).shortValue());
                continue;
            }
            if (value1 instanceof BigDecimal) {
                diffMap.put(key, ((BigDecimal)value2).subtract((BigDecimal)value1));
                continue;
            }
            if (!(value1 instanceof BigInteger)) continue;
            diffMap.put(key, ((BigInteger)value2).subtract((BigInteger)value1));
        }
        return diffMap;
    }

    public static List<Extension> loadExtensions(Connection conn, Properties props, String extensionClassNames, String errorMessageKey, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        LinkedList<Extension> extensionList = new LinkedList<Extension>();
        List<String> interceptorsToCreate = StringUtils.split(extensionClassNames, ",", true);
        String className = null;
        try {
            int s = interceptorsToCreate.size();
            for (int i = 0; i < s; ++i) {
                className = interceptorsToCreate.get(i);
                Extension extensionInstance = (Extension)Class.forName(className).newInstance();
                extensionInstance.init(conn, props);
                extensionList.add(extensionInstance);
            }
        }
        catch (Throwable t) {
            SQLException sqlEx = SQLError.createSQLException(Messages.getString(errorMessageKey, new Object[]{className}), exceptionInterceptor);
            sqlEx.initCause(t);
            throw sqlEx;
        }
        return extensionList;
    }

    public static boolean isJdbcInterface(Class<?> clazz) {
        if (isJdbcInterfaceCache.containsKey(clazz)) {
            return (Boolean)isJdbcInterfaceCache.get(clazz);
        }
        if (clazz.isInterface()) {
            try {
                if (Util.isJdbcPackage(clazz.getPackage().getName())) {
                    isJdbcInterfaceCache.putIfAbsent(clazz, true);
                    return true;
                }
            }
            catch (Exception ex) {
                // empty catch block
            }
        }
        for (Class<?> iface : clazz.getInterfaces()) {
            if (!Util.isJdbcInterface(iface)) continue;
            isJdbcInterfaceCache.putIfAbsent(clazz, true);
            return true;
        }
        if (clazz.getSuperclass() != null && Util.isJdbcInterface(clazz.getSuperclass())) {
            isJdbcInterfaceCache.putIfAbsent(clazz, true);
            return true;
        }
        isJdbcInterfaceCache.putIfAbsent(clazz, false);
        return false;
    }

    public static boolean isJdbcPackage(String packageName) {
        return packageName != null && (packageName.startsWith("java.sql") || packageName.startsWith("javax.sql") || packageName.startsWith(MYSQL_JDBC_PACKAGE_ROOT));
    }

    public static Class<?>[] getImplementedInterfaces(Class<?> clazz) {
        Class[] implementedInterfaces = (Class[])implementedInterfacesCache.get(clazz);
        if (implementedInterfaces != null) {
            return implementedInterfaces;
        }
        LinkedHashSet interfaces = new LinkedHashSet();
        Class<?> superClass = clazz;
        do {
            Collections.addAll(interfaces, superClass.getInterfaces());
        } while ((superClass = superClass.getSuperclass()) != null);
        implementedInterfaces = interfaces.toArray(new Class[interfaces.size()]);
        Class[] oldValue = implementedInterfacesCache.putIfAbsent(clazz, implementedInterfaces);
        if (oldValue != null) {
            implementedInterfaces = oldValue;
        }
        return implementedInterfaces;
    }

    public static long secondsSinceMillis(long timeInMillis) {
        return (System.currentTimeMillis() - timeInMillis) / 1000L;
    }

    public static int truncateAndConvertToInt(long longValue) {
        return longValue > Integer.MAX_VALUE ? Integer.MAX_VALUE : (longValue < Integer.MIN_VALUE ? Integer.MIN_VALUE : (int)longValue);
    }

    public static int[] truncateAndConvertToInt(long[] longArray) {
        int[] intArray = new int[longArray.length];
        for (int i = 0; i < longArray.length; ++i) {
            intArray[i] = longArray[i] > Integer.MAX_VALUE ? Integer.MAX_VALUE : (longArray[i] < Integer.MIN_VALUE ? Integer.MIN_VALUE : (int)longArray[i]);
        }
        return intArray;
    }

    static {
        String loadedFrom;
        enclosingInstance = new Util();
        jvmVersion = -1;
        jvmUpdateNumber = -1;
        isColdFusion = false;
        try {
            Class.forName("java.sql.NClob");
            isJdbc4 = true;
        }
        catch (ClassNotFoundException e) {
            isJdbc4 = false;
        }
        try {
            Class.forName("java.sql.JDBCType");
            isJdbc42 = true;
        }
        catch (Throwable t) {
            isJdbc42 = false;
        }
        String jvmVersionString = System.getProperty("java.version");
        int startPos = jvmVersionString.indexOf(46);
        int endPos = startPos + 1;
        if (startPos != -1) {
            while (Character.isDigit(jvmVersionString.charAt(endPos)) && ++endPos < jvmVersionString.length()) {
            }
        }
        jvmVersion = endPos > ++startPos ? Integer.parseInt(jvmVersionString.substring(startPos, endPos)) : (isJdbc42 ? 8 : (isJdbc4 ? 6 : 5));
        startPos = jvmVersionString.indexOf("_");
        endPos = startPos + 1;
        if (startPos != -1) {
            while (Character.isDigit(jvmVersionString.charAt(endPos)) && ++endPos < jvmVersionString.length()) {
            }
        }
        if (endPos > ++startPos) {
            jvmUpdateNumber = Integer.parseInt(jvmVersionString.substring(startPos, endPos));
        }
        isColdFusion = (loadedFrom = Util.stackTraceToString(new Throwable())) != null ? loadedFrom.indexOf("coldfusion") != -1 : false;
        isJdbcInterfaceCache = new ConcurrentHashMap();
        String packageName = MultiHostConnectionProxy.class.getPackage().getName();
        MYSQL_JDBC_PACKAGE_ROOT = packageName.substring(0, packageName.indexOf("jdbc") + 4);
        implementedInterfacesCache = new ConcurrentHashMap();
    }

    class RandStructcture {
        long maxValue;
        double maxValueDbl;
        long seed1;
        long seed2;

        RandStructcture() {
        }
    }
}


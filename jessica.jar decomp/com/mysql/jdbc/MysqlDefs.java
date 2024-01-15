/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import java.util.HashMap;
import java.util.Map;

public final class MysqlDefs {
    static final int COM_BINLOG_DUMP = 18;
    static final int COM_CHANGE_USER = 17;
    static final int COM_CLOSE_STATEMENT = 25;
    static final int COM_CONNECT_OUT = 20;
    static final int COM_END = 29;
    static final int COM_EXECUTE = 23;
    static final int COM_FETCH = 28;
    static final int COM_LONG_DATA = 24;
    static final int COM_PREPARE = 22;
    static final int COM_REGISTER_SLAVE = 21;
    static final int COM_RESET_STMT = 26;
    static final int COM_SET_OPTION = 27;
    static final int COM_TABLE_DUMP = 19;
    static final int CONNECT = 11;
    static final int CREATE_DB = 5;
    static final int DEBUG = 13;
    static final int DELAYED_INSERT = 16;
    static final int DROP_DB = 6;
    static final int FIELD_LIST = 4;
    static final int FIELD_TYPE_BIT = 16;
    public static final int FIELD_TYPE_BLOB = 252;
    static final int FIELD_TYPE_DATE = 10;
    static final int FIELD_TYPE_DATETIME = 12;
    static final int FIELD_TYPE_DECIMAL = 0;
    static final int FIELD_TYPE_DOUBLE = 5;
    static final int FIELD_TYPE_ENUM = 247;
    static final int FIELD_TYPE_FLOAT = 4;
    static final int FIELD_TYPE_GEOMETRY = 255;
    static final int FIELD_TYPE_INT24 = 9;
    static final int FIELD_TYPE_LONG = 3;
    static final int FIELD_TYPE_LONG_BLOB = 251;
    static final int FIELD_TYPE_LONGLONG = 8;
    static final int FIELD_TYPE_MEDIUM_BLOB = 250;
    static final int FIELD_TYPE_NEW_DECIMAL = 246;
    static final int FIELD_TYPE_NEWDATE = 14;
    static final int FIELD_TYPE_NULL = 6;
    static final int FIELD_TYPE_SET = 248;
    static final int FIELD_TYPE_SHORT = 2;
    static final int FIELD_TYPE_STRING = 254;
    static final int FIELD_TYPE_TIME = 11;
    static final int FIELD_TYPE_TIMESTAMP = 7;
    static final int FIELD_TYPE_TINY = 1;
    static final int FIELD_TYPE_TINY_BLOB = 249;
    static final int FIELD_TYPE_VAR_STRING = 253;
    static final int FIELD_TYPE_VARCHAR = 15;
    static final int FIELD_TYPE_YEAR = 13;
    static final int FIELD_TYPE_JSON = 245;
    static final int INIT_DB = 2;
    static final long LENGTH_BLOB = 65535L;
    static final long LENGTH_LONGBLOB = 0xFFFFFFFFL;
    static final long LENGTH_MEDIUMBLOB = 0xFFFFFFL;
    static final long LENGTH_TINYBLOB = 255L;
    static final int MAX_ROWS = 50000000;
    public static final int NO_CHARSET_INFO = -1;
    static final byte OPEN_CURSOR_FLAG = 1;
    static final int PING = 14;
    static final int PROCESS_INFO = 10;
    static final int PROCESS_KILL = 12;
    static final int QUERY = 3;
    static final int QUIT = 1;
    static final int RELOAD = 7;
    static final int SHUTDOWN = 8;
    static final int SLEEP = 0;
    static final int STATISTICS = 9;
    static final int TIME = 15;
    private static Map<String, Integer> mysqlToJdbcTypesMap = new HashMap<String, Integer>();

    static int mysqlToJavaType(int mysqlType) {
        int jdbcType;
        switch (mysqlType) {
            case 0: 
            case 246: {
                jdbcType = 3;
                break;
            }
            case 1: {
                jdbcType = -6;
                break;
            }
            case 2: {
                jdbcType = 5;
                break;
            }
            case 3: {
                jdbcType = 4;
                break;
            }
            case 4: {
                jdbcType = 7;
                break;
            }
            case 5: {
                jdbcType = 8;
                break;
            }
            case 6: {
                jdbcType = 0;
                break;
            }
            case 7: {
                jdbcType = 93;
                break;
            }
            case 8: {
                jdbcType = -5;
                break;
            }
            case 9: {
                jdbcType = 4;
                break;
            }
            case 10: {
                jdbcType = 91;
                break;
            }
            case 11: {
                jdbcType = 92;
                break;
            }
            case 12: {
                jdbcType = 93;
                break;
            }
            case 13: {
                jdbcType = 91;
                break;
            }
            case 14: {
                jdbcType = 91;
                break;
            }
            case 247: {
                jdbcType = 1;
                break;
            }
            case 248: {
                jdbcType = 1;
                break;
            }
            case 249: {
                jdbcType = -3;
                break;
            }
            case 250: {
                jdbcType = -4;
                break;
            }
            case 251: {
                jdbcType = -4;
                break;
            }
            case 252: {
                jdbcType = -4;
                break;
            }
            case 15: 
            case 253: {
                jdbcType = 12;
                break;
            }
            case 245: 
            case 254: {
                jdbcType = 1;
                break;
            }
            case 255: {
                jdbcType = -2;
                break;
            }
            case 16: {
                jdbcType = -7;
                break;
            }
            default: {
                jdbcType = 12;
            }
        }
        return jdbcType;
    }

    static int mysqlToJavaType(String mysqlType) {
        if (mysqlType.equalsIgnoreCase("BIT")) {
            return MysqlDefs.mysqlToJavaType(16);
        }
        if (mysqlType.equalsIgnoreCase("TINYINT")) {
            return MysqlDefs.mysqlToJavaType(1);
        }
        if (mysqlType.equalsIgnoreCase("SMALLINT")) {
            return MysqlDefs.mysqlToJavaType(2);
        }
        if (mysqlType.equalsIgnoreCase("MEDIUMINT")) {
            return MysqlDefs.mysqlToJavaType(9);
        }
        if (mysqlType.equalsIgnoreCase("INT") || mysqlType.equalsIgnoreCase("INTEGER")) {
            return MysqlDefs.mysqlToJavaType(3);
        }
        if (mysqlType.equalsIgnoreCase("BIGINT")) {
            return MysqlDefs.mysqlToJavaType(8);
        }
        if (mysqlType.equalsIgnoreCase("INT24")) {
            return MysqlDefs.mysqlToJavaType(9);
        }
        if (mysqlType.equalsIgnoreCase("REAL")) {
            return MysqlDefs.mysqlToJavaType(5);
        }
        if (mysqlType.equalsIgnoreCase("FLOAT")) {
            return MysqlDefs.mysqlToJavaType(4);
        }
        if (mysqlType.equalsIgnoreCase("DECIMAL")) {
            return MysqlDefs.mysqlToJavaType(0);
        }
        if (mysqlType.equalsIgnoreCase("NUMERIC")) {
            return MysqlDefs.mysqlToJavaType(0);
        }
        if (mysqlType.equalsIgnoreCase("DOUBLE")) {
            return MysqlDefs.mysqlToJavaType(5);
        }
        if (mysqlType.equalsIgnoreCase("CHAR")) {
            return MysqlDefs.mysqlToJavaType(254);
        }
        if (mysqlType.equalsIgnoreCase("VARCHAR")) {
            return MysqlDefs.mysqlToJavaType(253);
        }
        if (mysqlType.equalsIgnoreCase("DATE")) {
            return MysqlDefs.mysqlToJavaType(10);
        }
        if (mysqlType.equalsIgnoreCase("TIME")) {
            return MysqlDefs.mysqlToJavaType(11);
        }
        if (mysqlType.equalsIgnoreCase("YEAR")) {
            return MysqlDefs.mysqlToJavaType(13);
        }
        if (mysqlType.equalsIgnoreCase("TIMESTAMP")) {
            return MysqlDefs.mysqlToJavaType(7);
        }
        if (mysqlType.equalsIgnoreCase("DATETIME")) {
            return MysqlDefs.mysqlToJavaType(12);
        }
        if (mysqlType.equalsIgnoreCase("TINYBLOB")) {
            return -2;
        }
        if (mysqlType.equalsIgnoreCase("BLOB")) {
            return -4;
        }
        if (mysqlType.equalsIgnoreCase("MEDIUMBLOB")) {
            return -4;
        }
        if (mysqlType.equalsIgnoreCase("LONGBLOB")) {
            return -4;
        }
        if (mysqlType.equalsIgnoreCase("TINYTEXT")) {
            return 12;
        }
        if (mysqlType.equalsIgnoreCase("TEXT")) {
            return -1;
        }
        if (mysqlType.equalsIgnoreCase("MEDIUMTEXT")) {
            return -1;
        }
        if (mysqlType.equalsIgnoreCase("LONGTEXT")) {
            return -1;
        }
        if (mysqlType.equalsIgnoreCase("ENUM")) {
            return MysqlDefs.mysqlToJavaType(247);
        }
        if (mysqlType.equalsIgnoreCase("SET")) {
            return MysqlDefs.mysqlToJavaType(248);
        }
        if (mysqlType.equalsIgnoreCase("GEOMETRY")) {
            return MysqlDefs.mysqlToJavaType(255);
        }
        if (mysqlType.equalsIgnoreCase("BINARY")) {
            return -2;
        }
        if (mysqlType.equalsIgnoreCase("VARBINARY")) {
            return -3;
        }
        if (mysqlType.equalsIgnoreCase("BIT")) {
            return MysqlDefs.mysqlToJavaType(16);
        }
        if (mysqlType.equalsIgnoreCase("JSON")) {
            return MysqlDefs.mysqlToJavaType(245);
        }
        return 1111;
    }

    public static String typeToName(int mysqlType) {
        switch (mysqlType) {
            case 0: {
                return "FIELD_TYPE_DECIMAL";
            }
            case 1: {
                return "FIELD_TYPE_TINY";
            }
            case 2: {
                return "FIELD_TYPE_SHORT";
            }
            case 3: {
                return "FIELD_TYPE_LONG";
            }
            case 4: {
                return "FIELD_TYPE_FLOAT";
            }
            case 5: {
                return "FIELD_TYPE_DOUBLE";
            }
            case 6: {
                return "FIELD_TYPE_NULL";
            }
            case 7: {
                return "FIELD_TYPE_TIMESTAMP";
            }
            case 8: {
                return "FIELD_TYPE_LONGLONG";
            }
            case 9: {
                return "FIELD_TYPE_INT24";
            }
            case 16: {
                return "FIELD_TYPE_BIT";
            }
            case 10: {
                return "FIELD_TYPE_DATE";
            }
            case 11: {
                return "FIELD_TYPE_TIME";
            }
            case 12: {
                return "FIELD_TYPE_DATETIME";
            }
            case 13: {
                return "FIELD_TYPE_YEAR";
            }
            case 14: {
                return "FIELD_TYPE_NEWDATE";
            }
            case 247: {
                return "FIELD_TYPE_ENUM";
            }
            case 248: {
                return "FIELD_TYPE_SET";
            }
            case 249: {
                return "FIELD_TYPE_TINY_BLOB";
            }
            case 250: {
                return "FIELD_TYPE_MEDIUM_BLOB";
            }
            case 251: {
                return "FIELD_TYPE_LONG_BLOB";
            }
            case 252: {
                return "FIELD_TYPE_BLOB";
            }
            case 253: {
                return "FIELD_TYPE_VAR_STRING";
            }
            case 254: {
                return "FIELD_TYPE_STRING";
            }
            case 15: {
                return "FIELD_TYPE_VARCHAR";
            }
            case 255: {
                return "FIELD_TYPE_GEOMETRY";
            }
            case 245: {
                return "FIELD_TYPE_JSON";
            }
        }
        return " Unknown MySQL Type # " + mysqlType;
    }

    static final void appendJdbcTypeMappingQuery(StringBuilder buf, String mysqlTypeColumnName) {
        buf.append("CASE ");
        HashMap<String, Integer> typesMap = new HashMap<String, Integer>();
        typesMap.putAll(mysqlToJdbcTypesMap);
        typesMap.put("BINARY", -2);
        typesMap.put("VARBINARY", -3);
        for (String mysqlTypeName : typesMap.keySet()) {
            buf.append(" WHEN UPPER(");
            buf.append(mysqlTypeColumnName);
            buf.append(")='");
            buf.append(mysqlTypeName);
            buf.append("' THEN ");
            buf.append(typesMap.get(mysqlTypeName));
            if (!mysqlTypeName.equalsIgnoreCase("DOUBLE") && !mysqlTypeName.equalsIgnoreCase("FLOAT") && !mysqlTypeName.equalsIgnoreCase("DECIMAL") && !mysqlTypeName.equalsIgnoreCase("NUMERIC")) continue;
            buf.append(" WHEN ");
            buf.append(mysqlTypeColumnName);
            buf.append("='");
            buf.append(mysqlTypeName);
            buf.append(" UNSIGNED' THEN ");
            buf.append(typesMap.get(mysqlTypeName));
        }
        buf.append(" ELSE ");
        buf.append(1111);
        buf.append(" END ");
    }

    static {
        mysqlToJdbcTypesMap.put("BIT", MysqlDefs.mysqlToJavaType(16));
        mysqlToJdbcTypesMap.put("TINYINT", MysqlDefs.mysqlToJavaType(1));
        mysqlToJdbcTypesMap.put("SMALLINT", MysqlDefs.mysqlToJavaType(2));
        mysqlToJdbcTypesMap.put("MEDIUMINT", MysqlDefs.mysqlToJavaType(9));
        mysqlToJdbcTypesMap.put("INT", MysqlDefs.mysqlToJavaType(3));
        mysqlToJdbcTypesMap.put("INTEGER", MysqlDefs.mysqlToJavaType(3));
        mysqlToJdbcTypesMap.put("BIGINT", MysqlDefs.mysqlToJavaType(8));
        mysqlToJdbcTypesMap.put("INT24", MysqlDefs.mysqlToJavaType(9));
        mysqlToJdbcTypesMap.put("REAL", MysqlDefs.mysqlToJavaType(5));
        mysqlToJdbcTypesMap.put("FLOAT", MysqlDefs.mysqlToJavaType(4));
        mysqlToJdbcTypesMap.put("DECIMAL", MysqlDefs.mysqlToJavaType(0));
        mysqlToJdbcTypesMap.put("NUMERIC", MysqlDefs.mysqlToJavaType(0));
        mysqlToJdbcTypesMap.put("DOUBLE", MysqlDefs.mysqlToJavaType(5));
        mysqlToJdbcTypesMap.put("CHAR", MysqlDefs.mysqlToJavaType(254));
        mysqlToJdbcTypesMap.put("VARCHAR", MysqlDefs.mysqlToJavaType(253));
        mysqlToJdbcTypesMap.put("DATE", MysqlDefs.mysqlToJavaType(10));
        mysqlToJdbcTypesMap.put("TIME", MysqlDefs.mysqlToJavaType(11));
        mysqlToJdbcTypesMap.put("YEAR", MysqlDefs.mysqlToJavaType(13));
        mysqlToJdbcTypesMap.put("TIMESTAMP", MysqlDefs.mysqlToJavaType(7));
        mysqlToJdbcTypesMap.put("DATETIME", MysqlDefs.mysqlToJavaType(12));
        mysqlToJdbcTypesMap.put("TINYBLOB", -2);
        mysqlToJdbcTypesMap.put("BLOB", -4);
        mysqlToJdbcTypesMap.put("MEDIUMBLOB", -4);
        mysqlToJdbcTypesMap.put("LONGBLOB", -4);
        mysqlToJdbcTypesMap.put("TINYTEXT", 12);
        mysqlToJdbcTypesMap.put("TEXT", -1);
        mysqlToJdbcTypesMap.put("MEDIUMTEXT", -1);
        mysqlToJdbcTypesMap.put("LONGTEXT", -1);
        mysqlToJdbcTypesMap.put("ENUM", MysqlDefs.mysqlToJavaType(247));
        mysqlToJdbcTypesMap.put("SET", MysqlDefs.mysqlToJavaType(248));
        mysqlToJdbcTypesMap.put("GEOMETRY", MysqlDefs.mysqlToJavaType(255));
        mysqlToJdbcTypesMap.put("JSON", MysqlDefs.mysqlToJavaType(245));
    }
}


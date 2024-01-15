/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.xmlrpc.base;

import com.mysql.fabric.xmlrpc.base.Array;
import com.mysql.fabric.xmlrpc.base.Struct;
import java.util.Arrays;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class Value {
    public static final byte TYPE_i4 = 0;
    public static final byte TYPE_int = 1;
    public static final byte TYPE_boolean = 2;
    public static final byte TYPE_string = 3;
    public static final byte TYPE_double = 4;
    public static final byte TYPE_dateTime_iso8601 = 5;
    public static final byte TYPE_base64 = 6;
    public static final byte TYPE_struct = 7;
    public static final byte TYPE_array = 8;
    protected Object objValue = "";
    protected byte objType = (byte)3;
    private DatatypeFactory dtf = null;

    public Value() {
    }

    public Value(int value) {
        this.setInt(value);
    }

    public Value(String value) {
        this.setString(value);
    }

    public Value(boolean value) {
        this.setBoolean(value);
    }

    public Value(double value) {
        this.setDouble(value);
    }

    public Value(GregorianCalendar value) throws DatatypeConfigurationException {
        this.setDateTime(value);
    }

    public Value(byte[] value) {
        this.setBase64(value);
    }

    public Value(Struct value) {
        this.setStruct(value);
    }

    public Value(Array value) {
        this.setArray(value);
    }

    public Object getValue() {
        return this.objValue;
    }

    public byte getType() {
        return this.objType;
    }

    public void setInt(int value) {
        this.objValue = value;
        this.objType = 1;
    }

    public void setInt(String value) {
        this.objValue = Integer.valueOf(value);
        this.objType = 1;
    }

    public void setString(String value) {
        this.objValue = value;
        this.objType = (byte)3;
    }

    public void appendString(String value) {
        this.objValue = this.objValue != null ? this.objValue + value : value;
        this.objType = (byte)3;
    }

    public void setBoolean(boolean value) {
        this.objValue = value;
        this.objType = (byte)2;
    }

    public void setBoolean(String value) {
        this.objValue = value.trim().equals("1") || value.trim().equalsIgnoreCase("true") ? Boolean.valueOf(true) : Boolean.valueOf(false);
        this.objType = (byte)2;
    }

    public void setDouble(double value) {
        this.objValue = value;
        this.objType = (byte)4;
    }

    public void setDouble(String value) {
        this.objValue = Double.valueOf(value);
        this.objType = (byte)4;
    }

    public void setDateTime(GregorianCalendar value) throws DatatypeConfigurationException {
        if (this.dtf == null) {
            this.dtf = DatatypeFactory.newInstance();
        }
        this.objValue = this.dtf.newXMLGregorianCalendar(value);
        this.objType = (byte)5;
    }

    public void setDateTime(String value) throws DatatypeConfigurationException {
        if (this.dtf == null) {
            this.dtf = DatatypeFactory.newInstance();
        }
        this.objValue = this.dtf.newXMLGregorianCalendar(value);
        this.objType = (byte)5;
    }

    public void setBase64(byte[] value) {
        this.objValue = value;
        this.objType = (byte)6;
    }

    public void setStruct(Struct value) {
        this.objValue = value;
        this.objType = (byte)7;
    }

    public void setArray(Array value) {
        this.objValue = value;
        this.objType = (byte)8;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("<value>");
        switch (this.objType) {
            case 0: {
                sb.append("<i4>" + ((Integer)this.objValue).toString() + "</i4>");
                break;
            }
            case 1: {
                sb.append("<int>" + ((Integer)this.objValue).toString() + "</int>");
                break;
            }
            case 2: {
                sb.append("<boolean>" + ((Boolean)this.objValue != false ? 1 : 0) + "</boolean>");
                break;
            }
            case 4: {
                sb.append("<double>" + ((Double)this.objValue).toString() + "</double>");
                break;
            }
            case 5: {
                sb.append("<dateTime.iso8601>" + ((XMLGregorianCalendar)this.objValue).toString() + "</dateTime.iso8601>");
                break;
            }
            case 6: {
                sb.append("<base64>" + Arrays.toString((byte[])this.objValue) + "</base64>");
                break;
            }
            case 7: {
                sb.append(((Struct)this.objValue).toString());
                break;
            }
            case 8: {
                sb.append(((Array)this.objValue).toString());
                break;
            }
            default: {
                sb.append("<string>" + this.escapeXMLChars(this.objValue.toString()) + "</string>");
            }
        }
        sb.append("</value>");
        return sb.toString();
    }

    private String escapeXMLChars(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        block5: for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch (c) {
                case '&': {
                    sb.append("&amp;");
                    continue block5;
                }
                case '<': {
                    sb.append("&lt;");
                    continue block5;
                }
                case '>': {
                    sb.append("&gt;");
                    continue block5;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}


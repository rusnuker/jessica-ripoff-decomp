/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Constants;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.SingleByteCharsetConverter;
import com.mysql.jdbc.StringUtils;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.sql.SQLException;

public class Buffer {
    static final int MAX_BYTES_TO_DUMP = 512;
    static final int NO_LENGTH_LIMIT = -1;
    static final long NULL_LENGTH = -1L;
    private int bufLength = 0;
    private byte[] byteBuffer;
    private int position = 0;
    protected boolean wasMultiPacket = false;
    public static final short TYPE_ID_ERROR = 255;
    public static final short TYPE_ID_EOF = 254;
    public static final short TYPE_ID_AUTH_SWITCH = 254;
    public static final short TYPE_ID_LOCAL_INFILE = 251;
    public static final short TYPE_ID_OK = 0;

    public Buffer(byte[] buf) {
        this.byteBuffer = buf;
        this.setBufLength(buf.length);
    }

    Buffer(int size) {
        this.byteBuffer = new byte[size];
        this.setBufLength(this.byteBuffer.length);
        this.position = 4;
    }

    final void clear() {
        this.position = 4;
    }

    final void dump() {
        this.dump(this.getBufLength());
    }

    final String dump(int numBytes) {
        return StringUtils.dumpAsHex(this.getBytes(0, numBytes > this.getBufLength() ? this.getBufLength() : numBytes), numBytes > this.getBufLength() ? this.getBufLength() : numBytes);
    }

    final String dumpClampedBytes(int numBytes) {
        int numBytesToDump = numBytes < 512 ? numBytes : 512;
        String dumped = StringUtils.dumpAsHex(this.getBytes(0, numBytesToDump > this.getBufLength() ? this.getBufLength() : numBytesToDump), numBytesToDump > this.getBufLength() ? this.getBufLength() : numBytesToDump);
        if (numBytesToDump < numBytes) {
            return dumped + " ....(packet exceeds max. dump length)";
        }
        return dumped;
    }

    final void dumpHeader() {
        for (int i = 0; i < 4; ++i) {
            String hexVal = Integer.toHexString(this.readByte(i) & 0xFF);
            if (hexVal.length() == 1) {
                hexVal = "0" + hexVal;
            }
            System.out.print(hexVal + " ");
        }
    }

    final void dumpNBytes(int start, int nBytes) {
        StringBuilder asciiBuf = new StringBuilder();
        for (int i = start; i < start + nBytes && i < this.getBufLength(); ++i) {
            String hexVal = Integer.toHexString(this.readByte(i) & 0xFF);
            if (hexVal.length() == 1) {
                hexVal = "0" + hexVal;
            }
            System.out.print(hexVal + " ");
            if (this.readByte(i) > 32 && this.readByte(i) < 127) {
                asciiBuf.append((char)this.readByte(i));
            } else {
                asciiBuf.append(".");
            }
            asciiBuf.append(" ");
        }
        System.out.println("    " + asciiBuf.toString());
    }

    final void ensureCapacity(int additionalData) throws SQLException {
        if (this.position + additionalData > this.getBufLength()) {
            if (this.position + additionalData < this.byteBuffer.length) {
                this.setBufLength(this.byteBuffer.length);
            } else {
                int newLength = (int)((double)this.byteBuffer.length * 1.25);
                if (newLength < this.byteBuffer.length + additionalData) {
                    newLength = this.byteBuffer.length + (int)((double)additionalData * 1.25);
                }
                if (newLength < this.byteBuffer.length) {
                    newLength = this.byteBuffer.length + additionalData;
                }
                byte[] newBytes = new byte[newLength];
                System.arraycopy(this.byteBuffer, 0, newBytes, 0, this.byteBuffer.length);
                this.byteBuffer = newBytes;
                this.setBufLength(this.byteBuffer.length);
            }
        }
    }

    public int fastSkipLenString() {
        long len = this.readFieldLength();
        this.position = (int)((long)this.position + len);
        return (int)len;
    }

    public void fastSkipLenByteArray() {
        long len = this.readFieldLength();
        if (len == -1L || len == 0L) {
            return;
        }
        this.position = (int)((long)this.position + len);
    }

    protected final byte[] getBufferSource() {
        return this.byteBuffer;
    }

    public int getBufLength() {
        return this.bufLength;
    }

    public byte[] getByteBuffer() {
        return this.byteBuffer;
    }

    final byte[] getBytes(int len) {
        byte[] b = new byte[len];
        System.arraycopy(this.byteBuffer, this.position, b, 0, len);
        this.position += len;
        return b;
    }

    byte[] getBytes(int offset, int len) {
        byte[] dest = new byte[len];
        System.arraycopy(this.byteBuffer, offset, dest, 0, len);
        return dest;
    }

    int getCapacity() {
        return this.byteBuffer.length;
    }

    public ByteBuffer getNioBuffer() {
        throw new IllegalArgumentException(Messages.getString("ByteArrayBuffer.0"));
    }

    public int getPosition() {
        return this.position;
    }

    final boolean isEOFPacket() {
        return (this.byteBuffer[0] & 0xFF) == 254 && this.getBufLength() <= 5;
    }

    final boolean isAuthMethodSwitchRequestPacket() {
        return (this.byteBuffer[0] & 0xFF) == 254;
    }

    final boolean isOKPacket() {
        return (this.byteBuffer[0] & 0xFF) == 0;
    }

    final boolean isResultSetOKPacket() {
        return (this.byteBuffer[0] & 0xFF) == 254 && this.getBufLength() < 0xFFFFFF;
    }

    final boolean isRawPacket() {
        return (this.byteBuffer[0] & 0xFF) == 1;
    }

    final long newReadLength() {
        int sw = this.byteBuffer[this.position++] & 0xFF;
        switch (sw) {
            case 251: {
                return 0L;
            }
            case 252: {
                return this.readInt();
            }
            case 253: {
                return this.readLongInt();
            }
            case 254: {
                return this.readLongLong();
            }
        }
        return sw;
    }

    final byte readByte() {
        return this.byteBuffer[this.position++];
    }

    final byte readByte(int readAt) {
        return this.byteBuffer[readAt];
    }

    final long readFieldLength() {
        int sw = this.byteBuffer[this.position++] & 0xFF;
        switch (sw) {
            case 251: {
                return -1L;
            }
            case 252: {
                return this.readInt();
            }
            case 253: {
                return this.readLongInt();
            }
            case 254: {
                return this.readLongLong();
            }
        }
        return sw;
    }

    final int readInt() {
        byte[] b = this.byteBuffer;
        return b[this.position++] & 0xFF | (b[this.position++] & 0xFF) << 8;
    }

    final int readIntAsLong() {
        byte[] b = this.byteBuffer;
        return b[this.position++] & 0xFF | (b[this.position++] & 0xFF) << 8 | (b[this.position++] & 0xFF) << 16 | (b[this.position++] & 0xFF) << 24;
    }

    final byte[] readLenByteArray(int offset) {
        long len = this.readFieldLength();
        if (len == -1L) {
            return null;
        }
        if (len == 0L) {
            return Constants.EMPTY_BYTE_ARRAY;
        }
        this.position += offset;
        return this.getBytes((int)len);
    }

    final long readLength() {
        int sw = this.byteBuffer[this.position++] & 0xFF;
        switch (sw) {
            case 251: {
                return 0L;
            }
            case 252: {
                return this.readInt();
            }
            case 253: {
                return this.readLongInt();
            }
            case 254: {
                return this.readLong();
            }
        }
        return sw;
    }

    final long readLong() {
        byte[] b = this.byteBuffer;
        return (long)b[this.position++] & 0xFFL | ((long)b[this.position++] & 0xFFL) << 8 | (long)(b[this.position++] & 0xFF) << 16 | (long)(b[this.position++] & 0xFF) << 24;
    }

    final int readLongInt() {
        byte[] b = this.byteBuffer;
        return b[this.position++] & 0xFF | (b[this.position++] & 0xFF) << 8 | (b[this.position++] & 0xFF) << 16;
    }

    final long readLongLong() {
        byte[] b = this.byteBuffer;
        return (long)(b[this.position++] & 0xFF) | (long)(b[this.position++] & 0xFF) << 8 | (long)(b[this.position++] & 0xFF) << 16 | (long)(b[this.position++] & 0xFF) << 24 | (long)(b[this.position++] & 0xFF) << 32 | (long)(b[this.position++] & 0xFF) << 40 | (long)(b[this.position++] & 0xFF) << 48 | (long)(b[this.position++] & 0xFF) << 56;
    }

    final int readnBytes() {
        int sw = this.byteBuffer[this.position++] & 0xFF;
        switch (sw) {
            case 1: {
                return this.byteBuffer[this.position++] & 0xFF;
            }
            case 2: {
                return this.readInt();
            }
            case 3: {
                return this.readLongInt();
            }
            case 4: {
                return (int)this.readLong();
            }
        }
        return 255;
    }

    public final String readString() {
        int len = 0;
        int maxLen = this.getBufLength();
        for (int i = this.position; i < maxLen && this.byteBuffer[i] != 0; ++i) {
            ++len;
        }
        String s = StringUtils.toString(this.byteBuffer, this.position, len);
        this.position += len + 1;
        return s;
    }

    final String readString(String encoding, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        int len = 0;
        int maxLen = this.getBufLength();
        for (int i = this.position; i < maxLen && this.byteBuffer[i] != 0; ++i) {
            ++len;
        }
        try {
            String string = StringUtils.toString(this.byteBuffer, this.position, len, encoding);
            Object var8_8 = null;
            this.position += len + 1;
            return string;
        }
        catch (UnsupportedEncodingException uEE) {
            try {
                throw SQLError.createSQLException(Messages.getString("ByteArrayBuffer.1") + encoding + "'", "S1009", exceptionInterceptor);
            }
            catch (Throwable throwable) {
                Object var8_9 = null;
                this.position += len + 1;
                throw throwable;
            }
        }
    }

    final String readString(String encoding, ExceptionInterceptor exceptionInterceptor, int expectedLength) throws SQLException {
        if (this.position + expectedLength > this.getBufLength()) {
            throw SQLError.createSQLException(Messages.getString("ByteArrayBuffer.2"), "S1009", exceptionInterceptor);
        }
        try {
            String string = StringUtils.toString(this.byteBuffer, this.position, expectedLength, encoding);
            Object var6_6 = null;
            this.position += expectedLength;
            return string;
        }
        catch (UnsupportedEncodingException uEE) {
            try {
                throw SQLError.createSQLException(Messages.getString("ByteArrayBuffer.1") + encoding + "'", "S1009", exceptionInterceptor);
            }
            catch (Throwable throwable) {
                Object var6_7 = null;
                this.position += expectedLength;
                throw throwable;
            }
        }
    }

    public void setBufLength(int bufLengthToSet) {
        this.bufLength = bufLengthToSet;
    }

    public void setByteBuffer(byte[] byteBufferToSet) {
        this.byteBuffer = byteBufferToSet;
    }

    public void setPosition(int positionToSet) {
        this.position = positionToSet;
    }

    public void setWasMultiPacket(boolean flag) {
        this.wasMultiPacket = flag;
    }

    public String toString() {
        return this.dumpClampedBytes(this.getPosition());
    }

    public String toSuperString() {
        return super.toString();
    }

    public boolean wasMultiPacket() {
        return this.wasMultiPacket;
    }

    public final void writeByte(byte b) throws SQLException {
        this.ensureCapacity(1);
        this.byteBuffer[this.position++] = b;
    }

    public final void writeBytesNoNull(byte[] bytes) throws SQLException {
        int len = bytes.length;
        this.ensureCapacity(len);
        System.arraycopy(bytes, 0, this.byteBuffer, this.position, len);
        this.position += len;
    }

    final void writeBytesNoNull(byte[] bytes, int offset, int length) throws SQLException {
        this.ensureCapacity(length);
        System.arraycopy(bytes, offset, this.byteBuffer, this.position, length);
        this.position += length;
    }

    final void writeDouble(double d) throws SQLException {
        long l = Double.doubleToLongBits(d);
        this.writeLongLong(l);
    }

    final void writeFieldLength(long length) throws SQLException {
        if (length < 251L) {
            this.writeByte((byte)length);
        } else if (length < 65536L) {
            this.ensureCapacity(3);
            this.writeByte((byte)-4);
            this.writeInt((int)length);
        } else if (length < 0x1000000L) {
            this.ensureCapacity(4);
            this.writeByte((byte)-3);
            this.writeLongInt((int)length);
        } else {
            this.ensureCapacity(9);
            this.writeByte((byte)-2);
            this.writeLongLong(length);
        }
    }

    final void writeFloat(float f) throws SQLException {
        this.ensureCapacity(4);
        int i = Float.floatToIntBits(f);
        byte[] b = this.byteBuffer;
        b[this.position++] = (byte)(i & 0xFF);
        b[this.position++] = (byte)(i >>> 8);
        b[this.position++] = (byte)(i >>> 16);
        b[this.position++] = (byte)(i >>> 24);
    }

    final void writeInt(int i) throws SQLException {
        this.ensureCapacity(2);
        byte[] b = this.byteBuffer;
        b[this.position++] = (byte)(i & 0xFF);
        b[this.position++] = (byte)(i >>> 8);
    }

    final void writeLenBytes(byte[] b) throws SQLException {
        int len = b.length;
        this.ensureCapacity(len + 9);
        this.writeFieldLength(len);
        System.arraycopy(b, 0, this.byteBuffer, this.position, len);
        this.position += len;
    }

    final void writeLenString(String s, String encoding, String serverEncoding, SingleByteCharsetConverter converter, boolean parserKnowsUnicode, MySQLConnection conn) throws UnsupportedEncodingException, SQLException {
        byte[] b = null;
        b = converter != null ? converter.toBytes(s) : StringUtils.getBytes(s, encoding, serverEncoding, parserKnowsUnicode, conn, conn.getExceptionInterceptor());
        int len = b.length;
        this.ensureCapacity(len + 9);
        this.writeFieldLength(len);
        System.arraycopy(b, 0, this.byteBuffer, this.position, len);
        this.position += len;
    }

    final void writeLong(long i) throws SQLException {
        this.ensureCapacity(4);
        byte[] b = this.byteBuffer;
        b[this.position++] = (byte)(i & 0xFFL);
        b[this.position++] = (byte)(i >>> 8);
        b[this.position++] = (byte)(i >>> 16);
        b[this.position++] = (byte)(i >>> 24);
    }

    final void writeLongInt(int i) throws SQLException {
        this.ensureCapacity(3);
        byte[] b = this.byteBuffer;
        b[this.position++] = (byte)(i & 0xFF);
        b[this.position++] = (byte)(i >>> 8);
        b[this.position++] = (byte)(i >>> 16);
    }

    final void writeLongLong(long i) throws SQLException {
        this.ensureCapacity(8);
        byte[] b = this.byteBuffer;
        b[this.position++] = (byte)(i & 0xFFL);
        b[this.position++] = (byte)(i >>> 8);
        b[this.position++] = (byte)(i >>> 16);
        b[this.position++] = (byte)(i >>> 24);
        b[this.position++] = (byte)(i >>> 32);
        b[this.position++] = (byte)(i >>> 40);
        b[this.position++] = (byte)(i >>> 48);
        b[this.position++] = (byte)(i >>> 56);
    }

    final void writeString(String s) throws SQLException {
        this.ensureCapacity(s.length() * 3 + 1);
        this.writeStringNoNull(s);
        this.byteBuffer[this.position++] = 0;
    }

    final void writeString(String s, String encoding, MySQLConnection conn) throws SQLException {
        this.ensureCapacity(s.length() * 3 + 1);
        try {
            this.writeStringNoNull(s, encoding, encoding, false, conn);
        }
        catch (UnsupportedEncodingException ue) {
            throw new SQLException(ue.toString(), "S1000");
        }
        this.byteBuffer[this.position++] = 0;
    }

    final void writeStringNoNull(String s) throws SQLException {
        int len = s.length();
        this.ensureCapacity(len * 3);
        System.arraycopy(StringUtils.getBytes(s), 0, this.byteBuffer, this.position, len);
        this.position += len;
    }

    final void writeStringNoNull(String s, String encoding, String serverEncoding, boolean parserKnowsUnicode, MySQLConnection conn) throws UnsupportedEncodingException, SQLException {
        byte[] b = StringUtils.getBytes(s, encoding, serverEncoding, parserKnowsUnicode, conn, conn.getExceptionInterceptor());
        int len = b.length;
        this.ensureCapacity(len);
        System.arraycopy(b, 0, this.byteBuffer, this.position, len);
        this.position += len;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.profiler;

import com.mysql.jdbc.StringUtils;
import java.util.Date;

public class ProfilerEvent {
    public static final byte TYPE_WARN = 0;
    public static final byte TYPE_OBJECT_CREATION = 1;
    public static final byte TYPE_PREPARE = 2;
    public static final byte TYPE_QUERY = 3;
    public static final byte TYPE_EXECUTE = 4;
    public static final byte TYPE_FETCH = 5;
    public static final byte TYPE_SLOW_QUERY = 6;
    protected byte eventType;
    protected long connectionId;
    protected int statementId;
    protected int resultSetId;
    protected long eventCreationTime;
    protected long eventDuration;
    protected String durationUnits;
    protected int hostNameIndex;
    protected String hostName;
    protected int catalogIndex;
    protected String catalog;
    protected int eventCreationPointIndex;
    protected String eventCreationPointDesc;
    protected String message;

    public ProfilerEvent(byte eventType, String hostName, String catalog, long connectionId, int statementId, int resultSetId, long eventCreationTime, long eventDuration, String durationUnits, String eventCreationPointDesc, String eventCreationPoint, String message) {
        this.eventType = eventType;
        this.connectionId = connectionId;
        this.statementId = statementId;
        this.resultSetId = resultSetId;
        this.eventCreationTime = eventCreationTime;
        this.eventDuration = eventDuration;
        this.durationUnits = durationUnits;
        this.eventCreationPointDesc = eventCreationPointDesc;
        this.message = message;
    }

    public String getEventCreationPointAsString() {
        return this.eventCreationPointDesc;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(32);
        switch (this.eventType) {
            case 4: {
                buf.append("EXECUTE");
                break;
            }
            case 5: {
                buf.append("FETCH");
                break;
            }
            case 1: {
                buf.append("CONSTRUCT");
                break;
            }
            case 2: {
                buf.append("PREPARE");
                break;
            }
            case 3: {
                buf.append("QUERY");
                break;
            }
            case 0: {
                buf.append("WARN");
                break;
            }
            case 6: {
                buf.append("SLOW QUERY");
                break;
            }
            default: {
                buf.append("UNKNOWN");
            }
        }
        buf.append(" created: ");
        buf.append(new Date(this.eventCreationTime));
        buf.append(" duration: ");
        buf.append(this.eventDuration);
        buf.append(" connection: ");
        buf.append(this.connectionId);
        buf.append(" statement: ");
        buf.append(this.statementId);
        buf.append(" resultset: ");
        buf.append(this.resultSetId);
        if (this.message != null) {
            buf.append(" message: ");
            buf.append(this.message);
        }
        if (this.eventCreationPointDesc != null) {
            buf.append("\n\nEvent Created at:\n");
            buf.append(this.eventCreationPointDesc);
        }
        return buf.toString();
    }

    public static ProfilerEvent unpack(byte[] buf) throws Exception {
        int pos = 0;
        byte eventType = buf[pos++];
        long connectionId = ProfilerEvent.readInt(buf, pos);
        int statementId = ProfilerEvent.readInt(buf, pos += 8);
        int resultSetId = ProfilerEvent.readInt(buf, pos += 4);
        long eventCreationTime = ProfilerEvent.readLong(buf, pos += 4);
        long eventDuration = ProfilerEvent.readLong(buf, pos += 8);
        byte[] eventDurationUnits = ProfilerEvent.readBytes(buf, pos += 4);
        pos += 4;
        if (eventDurationUnits != null) {
            pos += eventDurationUnits.length;
        }
        ProfilerEvent.readInt(buf, pos);
        byte[] eventCreationAsBytes = ProfilerEvent.readBytes(buf, pos += 4);
        pos += 4;
        if (eventCreationAsBytes != null) {
            pos += eventCreationAsBytes.length;
        }
        byte[] message = ProfilerEvent.readBytes(buf, pos);
        pos += 4;
        if (message != null) {
            pos += message.length;
        }
        return new ProfilerEvent(eventType, "", "", connectionId, statementId, resultSetId, eventCreationTime, eventDuration, StringUtils.toString(eventDurationUnits, "ISO8859_1"), StringUtils.toString(eventCreationAsBytes, "ISO8859_1"), null, StringUtils.toString(message, "ISO8859_1"));
    }

    public byte[] pack() throws Exception {
        int len = 29;
        byte[] eventCreationAsBytes = null;
        this.getEventCreationPointAsString();
        if (this.eventCreationPointDesc != null) {
            eventCreationAsBytes = StringUtils.getBytes(this.eventCreationPointDesc, "ISO8859_1");
            len += 4 + eventCreationAsBytes.length;
        } else {
            len += 4;
        }
        byte[] messageAsBytes = null;
        if (this.message != null) {
            messageAsBytes = StringUtils.getBytes(this.message, "ISO8859_1");
            len += 4 + messageAsBytes.length;
        } else {
            len += 4;
        }
        byte[] durationUnitsAsBytes = null;
        if (this.durationUnits != null) {
            durationUnitsAsBytes = StringUtils.getBytes(this.durationUnits, "ISO8859_1");
            len += 4 + durationUnitsAsBytes.length;
        } else {
            len += 4;
            durationUnitsAsBytes = StringUtils.getBytes("", "ISO8859_1");
        }
        byte[] buf = new byte[len];
        int pos = 0;
        buf[pos++] = this.eventType;
        pos = ProfilerEvent.writeLong(this.connectionId, buf, pos);
        pos = ProfilerEvent.writeInt(this.statementId, buf, pos);
        pos = ProfilerEvent.writeInt(this.resultSetId, buf, pos);
        pos = ProfilerEvent.writeLong(this.eventCreationTime, buf, pos);
        pos = ProfilerEvent.writeLong(this.eventDuration, buf, pos);
        pos = ProfilerEvent.writeBytes(durationUnitsAsBytes, buf, pos);
        pos = ProfilerEvent.writeInt(this.eventCreationPointIndex, buf, pos);
        pos = eventCreationAsBytes != null ? ProfilerEvent.writeBytes(eventCreationAsBytes, buf, pos) : ProfilerEvent.writeInt(0, buf, pos);
        pos = messageAsBytes != null ? ProfilerEvent.writeBytes(messageAsBytes, buf, pos) : ProfilerEvent.writeInt(0, buf, pos);
        return buf;
    }

    private static int writeInt(int i, byte[] buf, int pos) {
        buf[pos++] = (byte)(i & 0xFF);
        buf[pos++] = (byte)(i >>> 8);
        buf[pos++] = (byte)(i >>> 16);
        buf[pos++] = (byte)(i >>> 24);
        return pos;
    }

    private static int writeLong(long l, byte[] buf, int pos) {
        buf[pos++] = (byte)(l & 0xFFL);
        buf[pos++] = (byte)(l >>> 8);
        buf[pos++] = (byte)(l >>> 16);
        buf[pos++] = (byte)(l >>> 24);
        buf[pos++] = (byte)(l >>> 32);
        buf[pos++] = (byte)(l >>> 40);
        buf[pos++] = (byte)(l >>> 48);
        buf[pos++] = (byte)(l >>> 56);
        return pos;
    }

    private static int writeBytes(byte[] msg, byte[] buf, int pos) {
        pos = ProfilerEvent.writeInt(msg.length, buf, pos);
        System.arraycopy(msg, 0, buf, pos, msg.length);
        return pos + msg.length;
    }

    private static int readInt(byte[] buf, int pos) {
        return buf[pos++] & 0xFF | (buf[pos++] & 0xFF) << 8 | (buf[pos++] & 0xFF) << 16 | (buf[pos++] & 0xFF) << 24;
    }

    private static long readLong(byte[] buf, int pos) {
        return (long)(buf[pos++] & 0xFF) | (long)(buf[pos++] & 0xFF) << 8 | (long)(buf[pos++] & 0xFF) << 16 | (long)(buf[pos++] & 0xFF) << 24 | (long)(buf[pos++] & 0xFF) << 32 | (long)(buf[pos++] & 0xFF) << 40 | (long)(buf[pos++] & 0xFF) << 48 | (long)(buf[pos++] & 0xFF) << 56;
    }

    private static byte[] readBytes(byte[] buf, int pos) {
        int length = ProfilerEvent.readInt(buf, pos);
        byte[] msg = new byte[length];
        System.arraycopy(buf, pos += 4, msg, 0, length);
        return msg;
    }

    public String getCatalog() {
        return this.catalog;
    }

    public long getConnectionId() {
        return this.connectionId;
    }

    public long getEventCreationTime() {
        return this.eventCreationTime;
    }

    public long getEventDuration() {
        return this.eventDuration;
    }

    public String getDurationUnits() {
        return this.durationUnits;
    }

    public byte getEventType() {
        return this.eventType;
    }

    public int getResultSetId() {
        return this.resultSetId;
    }

    public int getStatementId() {
        return this.statementId;
    }

    public String getMessage() {
        return this.message;
    }
}


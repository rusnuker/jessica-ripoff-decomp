/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.Util;
import java.util.Date;

public class SftpATTRS {
    static final int S_ISUID = 2048;
    static final int S_ISGID = 1024;
    static final int S_ISVTX = 512;
    static final int S_IRUSR = 256;
    static final int S_IWUSR = 128;
    static final int S_IXUSR = 64;
    static final int S_IREAD = 256;
    static final int S_IWRITE = 128;
    static final int S_IEXEC = 64;
    static final int S_IRGRP = 32;
    static final int S_IWGRP = 16;
    static final int S_IXGRP = 8;
    static final int S_IROTH = 4;
    static final int S_IWOTH = 2;
    static final int S_IXOTH = 1;
    private static final int pmask = 4095;
    public static final int SSH_FILEXFER_ATTR_SIZE = 1;
    public static final int SSH_FILEXFER_ATTR_UIDGID = 2;
    public static final int SSH_FILEXFER_ATTR_PERMISSIONS = 4;
    public static final int SSH_FILEXFER_ATTR_ACMODTIME = 8;
    public static final int SSH_FILEXFER_ATTR_EXTENDED = Integer.MIN_VALUE;
    static final int S_IFMT = 61440;
    static final int S_IFIFO = 4096;
    static final int S_IFCHR = 8192;
    static final int S_IFDIR = 16384;
    static final int S_IFBLK = 24576;
    static final int S_IFREG = 32768;
    static final int S_IFLNK = 40960;
    static final int S_IFSOCK = 49152;
    int flags = 0;
    long size;
    int uid;
    int gid;
    int permissions;
    int atime;
    int mtime;
    String[] extended = null;

    public String getPermissionsString() {
        StringBuffer buf = new StringBuffer(10);
        if (this.isDir()) {
            buf.append('d');
        } else if (this.isLink()) {
            buf.append('l');
        } else {
            buf.append('-');
        }
        if ((this.permissions & 0x100) != 0) {
            buf.append('r');
        } else {
            buf.append('-');
        }
        if ((this.permissions & 0x80) != 0) {
            buf.append('w');
        } else {
            buf.append('-');
        }
        if ((this.permissions & 0x800) != 0) {
            buf.append('s');
        } else if ((this.permissions & 0x40) != 0) {
            buf.append('x');
        } else {
            buf.append('-');
        }
        if ((this.permissions & 0x20) != 0) {
            buf.append('r');
        } else {
            buf.append('-');
        }
        if ((this.permissions & 0x10) != 0) {
            buf.append('w');
        } else {
            buf.append('-');
        }
        if ((this.permissions & 0x400) != 0) {
            buf.append('s');
        } else if ((this.permissions & 8) != 0) {
            buf.append('x');
        } else {
            buf.append('-');
        }
        if ((this.permissions & 4) != 0) {
            buf.append('r');
        } else {
            buf.append('-');
        }
        if ((this.permissions & 2) != 0) {
            buf.append('w');
        } else {
            buf.append('-');
        }
        if ((this.permissions & 1) != 0) {
            buf.append('x');
        } else {
            buf.append('-');
        }
        return buf.toString();
    }

    public String getAtimeString() {
        Date date = new Date((long)this.atime * 1000L);
        return date.toString();
    }

    public String getMtimeString() {
        Date date = new Date((long)this.mtime * 1000L);
        return date.toString();
    }

    private SftpATTRS() {
    }

    static SftpATTRS getATTR(Buffer buf) {
        int count;
        SftpATTRS attr = new SftpATTRS();
        attr.flags = buf.getInt();
        if ((attr.flags & 1) != 0) {
            attr.size = buf.getLong();
        }
        if ((attr.flags & 2) != 0) {
            attr.uid = buf.getInt();
            attr.gid = buf.getInt();
        }
        if ((attr.flags & 4) != 0) {
            attr.permissions = buf.getInt();
        }
        if ((attr.flags & 8) != 0) {
            attr.atime = buf.getInt();
        }
        if ((attr.flags & 8) != 0) {
            attr.mtime = buf.getInt();
        }
        if ((attr.flags & Integer.MIN_VALUE) != 0 && (count = buf.getInt()) > 0) {
            attr.extended = new String[count * 2];
            for (int i = 0; i < count; ++i) {
                attr.extended[i * 2] = Util.byte2str(buf.getString());
                attr.extended[i * 2 + 1] = Util.byte2str(buf.getString());
            }
        }
        return attr;
    }

    int length() {
        int len = 4;
        if ((this.flags & 1) != 0) {
            len += 8;
        }
        if ((this.flags & 2) != 0) {
            len += 8;
        }
        if ((this.flags & 4) != 0) {
            len += 4;
        }
        if ((this.flags & 8) != 0) {
            len += 8;
        }
        if ((this.flags & Integer.MIN_VALUE) != 0) {
            len += 4;
            int count = this.extended.length / 2;
            if (count > 0) {
                for (int i = 0; i < count; ++i) {
                    len += 4;
                    len += this.extended[i * 2].length();
                    len += 4;
                    len += this.extended[i * 2 + 1].length();
                }
            }
        }
        return len;
    }

    void dump(Buffer buf) {
        int count;
        buf.putInt(this.flags);
        if ((this.flags & 1) != 0) {
            buf.putLong(this.size);
        }
        if ((this.flags & 2) != 0) {
            buf.putInt(this.uid);
            buf.putInt(this.gid);
        }
        if ((this.flags & 4) != 0) {
            buf.putInt(this.permissions);
        }
        if ((this.flags & 8) != 0) {
            buf.putInt(this.atime);
        }
        if ((this.flags & 8) != 0) {
            buf.putInt(this.mtime);
        }
        if ((this.flags & Integer.MIN_VALUE) != 0 && (count = this.extended.length / 2) > 0) {
            for (int i = 0; i < count; ++i) {
                buf.putString(Util.str2byte(this.extended[i * 2]));
                buf.putString(Util.str2byte(this.extended[i * 2 + 1]));
            }
        }
    }

    void setFLAGS(int flags) {
        this.flags = flags;
    }

    public void setSIZE(long size) {
        this.flags |= 1;
        this.size = size;
    }

    public void setUIDGID(int uid, int gid) {
        this.flags |= 2;
        this.uid = uid;
        this.gid = gid;
    }

    public void setACMODTIME(int atime, int mtime) {
        this.flags |= 8;
        this.atime = atime;
        this.mtime = mtime;
    }

    public void setPERMISSIONS(int permissions) {
        this.flags |= 4;
        this.permissions = permissions = this.permissions & 0xFFFFF000 | permissions & 0xFFF;
    }

    private boolean isType(int mask) {
        return (this.flags & 4) != 0 && (this.permissions & 0xF000) == mask;
    }

    public boolean isReg() {
        return this.isType(32768);
    }

    public boolean isDir() {
        return this.isType(16384);
    }

    public boolean isChr() {
        return this.isType(8192);
    }

    public boolean isBlk() {
        return this.isType(24576);
    }

    public boolean isFifo() {
        return this.isType(4096);
    }

    public boolean isLink() {
        return this.isType(40960);
    }

    public boolean isSock() {
        return this.isType(49152);
    }

    public int getFlags() {
        return this.flags;
    }

    public long getSize() {
        return this.size;
    }

    public int getUId() {
        return this.uid;
    }

    public int getGId() {
        return this.gid;
    }

    public int getPermissions() {
        return this.permissions;
    }

    public int getATime() {
        return this.atime;
    }

    public int getMTime() {
        return this.mtime;
    }

    public String[] getExtended() {
        return this.extended;
    }

    public String toString() {
        return this.getPermissionsString() + " " + this.getUId() + " " + this.getGId() + " " + this.getSize() + " " + this.getMtimeString();
    }
}


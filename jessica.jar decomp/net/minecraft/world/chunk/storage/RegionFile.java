/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.world.chunk.storage;

import com.google.common.collect.Lists;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import javax.annotation.Nullable;
import net.minecraft.server.MinecraftServer;

public class RegionFile {
    private static final byte[] EMPTY_SECTOR = new byte[4096];
    private final File fileName;
    private RandomAccessFile dataFile;
    private final int[] offsets = new int[1024];
    private final int[] chunkTimestamps = new int[1024];
    private List<Boolean> sectorFree;
    private int sizeDelta;
    private long lastModified;

    public RegionFile(File fileNameIn) {
        this.fileName = fileNameIn;
        this.sizeDelta = 0;
        try {
            if (fileNameIn.exists()) {
                this.lastModified = fileNameIn.lastModified();
            }
            this.dataFile = new RandomAccessFile(fileNameIn, "rw");
            if (this.dataFile.length() < 4096L) {
                this.dataFile.write(EMPTY_SECTOR);
                this.dataFile.write(EMPTY_SECTOR);
                this.sizeDelta += 8192;
            }
            if ((this.dataFile.length() & 0xFFFL) != 0L) {
                int i = 0;
                while ((long)i < (this.dataFile.length() & 0xFFFL)) {
                    this.dataFile.write(0);
                    ++i;
                }
            }
            int i1 = (int)this.dataFile.length() / 4096;
            this.sectorFree = Lists.newArrayListWithCapacity((int)i1);
            int j = 0;
            while (j < i1) {
                this.sectorFree.add(true);
                ++j;
            }
            this.sectorFree.set(0, false);
            this.sectorFree.set(1, false);
            this.dataFile.seek(0L);
            int j1 = 0;
            while (j1 < 1024) {
                int k;
                this.offsets[j1] = k = this.dataFile.readInt();
                if (k != 0 && (k >> 8) + (k & 0xFF) <= this.sectorFree.size()) {
                    int l = 0;
                    while (l < (k & 0xFF)) {
                        this.sectorFree.set((k >> 8) + l, false);
                        ++l;
                    }
                }
                ++j1;
            }
            int k1 = 0;
            while (k1 < 1024) {
                int l1;
                this.chunkTimestamps[k1] = l1 = this.dataFile.readInt();
                ++k1;
            }
        }
        catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    @Nullable
    public synchronized DataInputStream getChunkDataInputStream(int x, int z) {
        int l;
        block12: {
            block11: {
                int k;
                int j;
                block10: {
                    int i;
                    block9: {
                        if (this.outOfBounds(x, z)) {
                            return null;
                        }
                        try {
                            i = this.getOffset(x, z);
                            if (i != 0) break block9;
                            return null;
                        }
                        catch (IOException var9) {
                            return null;
                        }
                    }
                    j = i >> 8;
                    k = i & 0xFF;
                    if (j + k <= this.sectorFree.size()) break block10;
                    return null;
                }
                this.dataFile.seek(j * 4096);
                l = this.dataFile.readInt();
                if (l <= 4096 * k) break block11;
                return null;
            }
            if (l > 0) break block12;
            return null;
        }
        byte b0 = this.dataFile.readByte();
        if (b0 == 1) {
            byte[] abyte1 = new byte[l - 1];
            this.dataFile.read(abyte1);
            return new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(abyte1))));
        }
        if (b0 == 2) {
            byte[] abyte = new byte[l - 1];
            this.dataFile.read(abyte);
            return new DataInputStream(new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(abyte))));
        }
        return null;
    }

    @Nullable
    public DataOutputStream getChunkDataOutputStream(int x, int z) {
        return this.outOfBounds(x, z) ? null : new DataOutputStream(new BufferedOutputStream(new DeflaterOutputStream(new ChunkBuffer(x, z))));
    }

    protected synchronized void write(int x, int z, byte[] data, int length) {
        try {
            int i = this.getOffset(x, z);
            int j = i >> 8;
            int k = i & 0xFF;
            int l = (length + 5) / 4096 + 1;
            if (l >= 256) {
                return;
            }
            if (j != 0 && k == l) {
                this.write(j, data, length);
            } else {
                int i1 = 0;
                while (i1 < k) {
                    this.sectorFree.set(j + i1, true);
                    ++i1;
                }
                int l1 = this.sectorFree.indexOf(true);
                int j1 = 0;
                if (l1 != -1) {
                    int k1 = l1;
                    while (k1 < this.sectorFree.size()) {
                        if (j1 != 0) {
                            j1 = this.sectorFree.get(k1).booleanValue() ? ++j1 : 0;
                        } else if (this.sectorFree.get(k1).booleanValue()) {
                            l1 = k1;
                            j1 = 1;
                        }
                        if (j1 >= l) break;
                        ++k1;
                    }
                }
                if (j1 >= l) {
                    j = l1;
                    this.setOffset(x, z, l1 << 8 | l);
                    int j2 = 0;
                    while (j2 < l) {
                        this.sectorFree.set(j + j2, false);
                        ++j2;
                    }
                    this.write(j, data, length);
                } else {
                    this.dataFile.seek(this.dataFile.length());
                    j = this.sectorFree.size();
                    int i2 = 0;
                    while (i2 < l) {
                        this.dataFile.write(EMPTY_SECTOR);
                        this.sectorFree.add(false);
                        ++i2;
                    }
                    this.sizeDelta += 4096 * l;
                    this.write(j, data, length);
                    this.setOffset(x, z, j << 8 | l);
                }
            }
            this.setChunkTimestamp(x, z, (int)(MinecraftServer.getCurrentTimeMillis() / 1000L));
        }
        catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    private void write(int sectorNumber, byte[] data, int length) throws IOException {
        this.dataFile.seek(sectorNumber * 4096);
        this.dataFile.writeInt(length + 1);
        this.dataFile.writeByte(2);
        this.dataFile.write(data, 0, length);
    }

    private boolean outOfBounds(int x, int z) {
        return x < 0 || x >= 32 || z < 0 || z >= 32;
    }

    private int getOffset(int x, int z) {
        return this.offsets[x + z * 32];
    }

    public boolean isChunkSaved(int x, int z) {
        return this.getOffset(x, z) != 0;
    }

    private void setOffset(int x, int z, int offset) throws IOException {
        this.offsets[x + z * 32] = offset;
        this.dataFile.seek((x + z * 32) * 4);
        this.dataFile.writeInt(offset);
    }

    private void setChunkTimestamp(int x, int z, int timestamp) throws IOException {
        this.chunkTimestamps[x + z * 32] = timestamp;
        this.dataFile.seek(4096 + (x + z * 32) * 4);
        this.dataFile.writeInt(timestamp);
    }

    public void close() throws IOException {
        if (this.dataFile != null) {
            this.dataFile.close();
        }
    }

    class ChunkBuffer
    extends ByteArrayOutputStream {
        private final int chunkX;
        private final int chunkZ;

        public ChunkBuffer(int x, int z) {
            super(8096);
            this.chunkX = x;
            this.chunkZ = z;
        }

        @Override
        public void close() throws IOException {
            RegionFile.this.write(this.chunkX, this.chunkZ, this.buf, this.count);
        }
    }
}


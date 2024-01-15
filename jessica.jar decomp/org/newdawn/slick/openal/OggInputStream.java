/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.jcraft.jogg.Packet
 *  com.jcraft.jogg.Page
 *  com.jcraft.jogg.StreamState
 *  com.jcraft.jogg.SyncState
 *  com.jcraft.jorbis.Block
 *  com.jcraft.jorbis.Comment
 *  com.jcraft.jorbis.DspState
 *  com.jcraft.jorbis.Info
 *  org.lwjgl.BufferUtils
 */
package org.newdawn.slick.openal;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.openal.AudioInputStream;
import org.newdawn.slick.util.Log;

public class OggInputStream
extends InputStream
implements AudioInputStream {
    private int convsize = 16384;
    private byte[] convbuffer = new byte[this.convsize];
    private InputStream input;
    private Info oggInfo = new Info();
    private boolean endOfStream;
    private SyncState syncState = new SyncState();
    private StreamState streamState = new StreamState();
    private Page page = new Page();
    private Packet packet = new Packet();
    private Comment comment = new Comment();
    private DspState dspState = new DspState();
    private Block vorbisBlock = new Block(this.dspState);
    byte[] buffer;
    int bytes = 0;
    boolean bigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);
    boolean endOfBitStream = true;
    boolean inited = false;
    private int readIndex;
    private ByteBuffer pcmBuffer = BufferUtils.createByteBuffer((int)2048000);
    private int total;

    public OggInputStream(InputStream input) throws IOException {
        this.input = input;
        this.total = input.available();
        this.init();
    }

    public int getLength() {
        return this.total;
    }

    @Override
    public int getChannels() {
        return this.oggInfo.channels;
    }

    @Override
    public int getRate() {
        return this.oggInfo.rate;
    }

    private void init() throws IOException {
        this.initVorbis();
        this.readPCM();
    }

    @Override
    public int available() {
        return this.endOfStream ? 0 : 1;
    }

    private void initVorbis() {
        this.syncState.init();
    }

    /*
     * Unable to fully structure code
     */
    private boolean getPageAndPacket() {
        block16: {
            index = this.syncState.buffer(4096);
            this.buffer = this.syncState.data;
            if (this.buffer == null) {
                this.endOfStream = true;
                return false;
            }
            try {
                this.bytes = this.input.read(this.buffer, index, 4096);
            }
            catch (Exception e) {
                Log.error("Failure reading in vorbis");
                Log.error(e);
                this.endOfStream = true;
                return false;
            }
            this.syncState.wrote(this.bytes);
            if (this.syncState.pageout(this.page) != 1) {
                if (this.bytes < 4096) {
                    return false;
                }
                Log.error("Input does not appear to be an Ogg bitstream.");
                this.endOfStream = true;
                return false;
            }
            this.streamState.init(this.page.serialno());
            this.oggInfo.init();
            this.comment.init();
            if (this.streamState.pagein(this.page) < 0) {
                Log.error("Error reading first page of Ogg bitstream data.");
                this.endOfStream = true;
                return false;
            }
            if (this.streamState.packetout(this.packet) != 1) {
                Log.error("Error reading initial header packet.");
                this.endOfStream = true;
                return false;
            }
            if (this.oggInfo.synthesis_headerin(this.comment, this.packet) < 0) {
                Log.error("This Ogg bitstream does not contain Vorbis audio data.");
                this.endOfStream = true;
                return false;
            }
            i = 0;
            break block16;
            while ((result = this.syncState.pageout(this.page)) != 0) {
                if (result == 1) {
                    this.streamState.pagein(this.page);
                    while (i < 2) {
                        result = this.streamState.packetout(this.packet);
                        if (result == 0) break;
                        if (result == -1) {
                            Log.error("Corrupt secondary header.  Exiting.");
                            this.endOfStream = true;
                            return false;
                        }
                        this.oggInfo.synthesis_headerin(this.comment, this.packet);
                        ++i;
                    }
                }
lbl54:
                // 5 sources

                ** while (i >= 2)
lbl55:
                // 1 sources

            }
lbl56:
            // 2 sources

            index = this.syncState.buffer(4096);
            this.buffer = this.syncState.data;
            try {
                this.bytes = this.input.read(this.buffer, index, 4096);
            }
            catch (Exception e) {
                Log.error("Failed to read Vorbis: ");
                Log.error(e);
                this.endOfStream = true;
                return false;
            }
            if (this.bytes == 0 && i < 2) {
                Log.error("End of file before finding all Vorbis headers!");
                this.endOfStream = true;
                return false;
            }
            this.syncState.wrote(this.bytes);
        }
        if (i < 2) ** GOTO lbl54
        this.convsize = 4096 / this.oggInfo.channels;
        this.dspState.synthesis_init(this.oggInfo);
        this.vorbisBlock.init(this.dspState);
        return true;
    }

    /*
     * Unable to fully structure code
     */
    private void readPCM() throws IOException {
        wrote = false;
        while (true) {
            block27: {
                if (this.endOfBitStream) {
                    if (!this.getPageAndPacket()) break;
                    this.endOfBitStream = false;
                }
                if (!this.inited) {
                    this.inited = true;
                    return;
                }
                _pcm = new float[1][][];
                _index = new int[this.oggInfo.channels];
                break block27;
                while ((result = this.syncState.pageout(this.page)) != 0) {
                    if (result == -1) {
                        Log.error("Corrupt or missing data in bitstream; continuing...");
                    } else {
                        this.streamState.pagein(this.page);
                        while ((result = this.streamState.packetout(this.packet)) != 0) {
                            if (result == -1) continue;
                            if (this.vorbisBlock.synthesis(this.packet) == 0) {
                                this.dspState.synthesis_blockin(this.vorbisBlock);
                            }
                            while ((samples = this.dspState.synthesis_pcmout((float[][][])_pcm, _index)) > 0) {
                                pcm = _pcm[0];
                                bout = samples < this.convsize ? samples : this.convsize;
                                i = 0;
                                while (i < this.oggInfo.channels) {
                                    ptr = i * 2;
                                    mono = _index[i];
                                    j = 0;
                                    while (j < bout) {
                                        val = (int)((double)pcm[i][mono + j] * 32767.0);
                                        if (val > 32767) {
                                            val = 32767;
                                        }
                                        if (val < -32768) {
                                            val = -32768;
                                        }
                                        if (val < 0) {
                                            val |= 32768;
                                        }
                                        if (this.bigEndian) {
                                            this.convbuffer[ptr] = (byte)(val >>> 8);
                                            this.convbuffer[ptr + 1] = (byte)val;
                                        } else {
                                            this.convbuffer[ptr] = (byte)val;
                                            this.convbuffer[ptr + 1] = (byte)(val >>> 8);
                                        }
                                        ptr += 2 * this.oggInfo.channels;
                                        ++j;
                                    }
                                    ++i;
                                }
                                bytesToWrite = 2 * this.oggInfo.channels * bout;
                                if (bytesToWrite >= this.pcmBuffer.remaining()) {
                                    Log.warn("Read block from OGG that was too big to be buffered: " + bytesToWrite);
                                } else {
                                    this.pcmBuffer.put(this.convbuffer, 0, bytesToWrite);
                                }
                                wrote = true;
                                this.dspState.synthesis_read(bout);
                            }
                        }
                        if (this.page.eos() != 0) {
                            this.endOfBitStream = true;
                        }
                        if (!this.endOfBitStream && wrote) {
                            return;
                        }
                    }
lbl65:
                    // 4 sources

                    ** while (this.endOfBitStream)
lbl66:
                    // 1 sources

                }
lbl67:
                // 2 sources

                if (!this.endOfBitStream) {
                    this.bytes = 0;
                    index = this.syncState.buffer(4096);
                    if (index >= 0) {
                        this.buffer = this.syncState.data;
                        try {
                            this.bytes = this.input.read(this.buffer, index, 4096);
                        }
                        catch (Exception e) {
                            Log.error("Failure during vorbis decoding");
                            Log.error(e);
                            this.endOfStream = true;
                            return;
                        }
                    } else {
                        this.bytes = 0;
                    }
                    this.syncState.wrote(this.bytes);
                    if (this.bytes == 0) {
                        this.endOfBitStream = true;
                    }
                }
            }
            if (!this.endOfBitStream) ** GOTO lbl65
            this.streamState.clear();
            this.vorbisBlock.clear();
            this.dspState.clear();
            this.oggInfo.clear();
        }
        this.syncState.clear();
        this.endOfStream = true;
    }

    @Override
    public int read() throws IOException {
        if (this.readIndex >= this.pcmBuffer.position()) {
            this.pcmBuffer.clear();
            this.readPCM();
            this.readIndex = 0;
        }
        if (this.readIndex >= this.pcmBuffer.position()) {
            return -1;
        }
        int value = this.pcmBuffer.get(this.readIndex);
        if (value < 0) {
            value += 256;
        }
        ++this.readIndex;
        return value;
    }

    @Override
    public boolean atEnd() {
        return this.endOfStream && this.readIndex >= this.pcmBuffer.position();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int i = 0;
        while (i < len) {
            block5: {
                block6: {
                    try {
                        int value = this.read();
                        if (value >= 0) {
                            b[i] = (byte)value;
                            break block5;
                        }
                        if (i != 0) break block6;
                        return -1;
                    }
                    catch (IOException e) {
                        Log.error(e);
                        return i;
                    }
                }
                return i;
            }
            ++i;
        }
        return len;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public void close() throws IOException {
    }
}


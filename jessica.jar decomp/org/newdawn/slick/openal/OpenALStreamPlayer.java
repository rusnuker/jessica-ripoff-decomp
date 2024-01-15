/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.openal.AL10
 *  org.lwjgl.openal.OpenALException
 */
package org.newdawn.slick.openal;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.OpenALException;
import org.newdawn.slick.openal.AudioInputStream;
import org.newdawn.slick.openal.OggInputStream;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;

public class OpenALStreamPlayer {
    public static final int BUFFER_COUNT = 3;
    private static final int sectionSize = 81920;
    private byte[] buffer = new byte[81920];
    private IntBuffer bufferNames;
    private ByteBuffer bufferData = BufferUtils.createByteBuffer((int)81920);
    private IntBuffer unqueued = BufferUtils.createIntBuffer((int)1);
    private int source;
    private int remainingBufferCount;
    private boolean loop;
    private boolean done = true;
    private AudioInputStream audio;
    private String ref;
    private URL url;
    private float pitch;
    private float positionOffset;

    public OpenALStreamPlayer(int source, String ref) {
        this.source = source;
        this.ref = ref;
        this.bufferNames = BufferUtils.createIntBuffer((int)3);
        AL10.alGenBuffers((IntBuffer)this.bufferNames);
    }

    public OpenALStreamPlayer(int source, URL url) {
        this.source = source;
        this.url = url;
        this.bufferNames = BufferUtils.createIntBuffer((int)3);
        AL10.alGenBuffers((IntBuffer)this.bufferNames);
    }

    private void initStreams() throws IOException {
        if (this.audio != null) {
            this.audio.close();
        }
        OggInputStream audio = this.url != null ? new OggInputStream(this.url.openStream()) : new OggInputStream(ResourceLoader.getResourceAsStream(this.ref));
        this.audio = audio;
        this.positionOffset = 0.0f;
    }

    public String getSource() {
        return this.url == null ? this.ref : this.url.toString();
    }

    private void removeBuffers() {
        IntBuffer buffer = BufferUtils.createIntBuffer((int)1);
        int queued = AL10.alGetSourcei((int)this.source, (int)4117);
        while (queued > 0) {
            AL10.alSourceUnqueueBuffers((int)this.source, (IntBuffer)buffer);
            --queued;
        }
    }

    public void play(boolean loop) throws IOException {
        this.loop = loop;
        this.initStreams();
        this.done = false;
        AL10.alSourceStop((int)this.source);
        this.removeBuffers();
        this.startPlayback();
    }

    public void setup(float pitch) {
        this.pitch = pitch;
    }

    public boolean done() {
        return this.done;
    }

    public void update() {
        if (this.done) {
            return;
        }
        float sampleRate = this.audio.getRate();
        float sampleSize = this.audio.getChannels() > 1 ? 4.0f : 2.0f;
        int processed = AL10.alGetSourcei((int)this.source, (int)4118);
        while (processed > 0) {
            this.unqueued.clear();
            AL10.alSourceUnqueueBuffers((int)this.source, (IntBuffer)this.unqueued);
            int bufferIndex = this.unqueued.get(0);
            float bufferLength = (float)AL10.alGetBufferi((int)bufferIndex, (int)8196) / sampleSize / sampleRate;
            this.positionOffset += bufferLength;
            if (this.stream(bufferIndex)) {
                AL10.alSourceQueueBuffers((int)this.source, (IntBuffer)this.unqueued);
            } else {
                --this.remainingBufferCount;
                if (this.remainingBufferCount == 0) {
                    this.done = true;
                }
            }
            --processed;
        }
        int state = AL10.alGetSourcei((int)this.source, (int)4112);
        if (state != 4114) {
            AL10.alSourcePlay((int)this.source);
        }
    }

    public boolean stream(int bufferId) {
        block7: {
            block6: {
                try {
                    int count = this.audio.read(this.buffer);
                    if (count == -1) break block6;
                    this.bufferData.clear();
                    this.bufferData.put(this.buffer, 0, count);
                    this.bufferData.flip();
                    int format = this.audio.getChannels() > 1 ? 4355 : 4353;
                    try {
                        AL10.alBufferData((int)bufferId, (int)format, (ByteBuffer)this.bufferData, (int)this.audio.getRate());
                        break block7;
                    }
                    catch (OpenALException e) {
                        Log.error("Failed to loop buffer: " + bufferId + " " + format + " " + count + " " + this.audio.getRate(), e);
                        return false;
                    }
                }
                catch (IOException e) {
                    Log.error(e);
                    return false;
                }
            }
            if (this.loop) {
                this.initStreams();
                this.stream(bufferId);
                break block7;
            }
            this.done = true;
            return false;
        }
        return true;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean setPosition(float position) {
        try {
            if (this.getPosition() > position) {
                this.initStreams();
            }
            float sampleRate = this.audio.getRate();
            float sampleSize = this.audio.getChannels() > 1 ? 4.0f : 2.0f;
            while (true) {
                if (!(this.positionOffset < position)) {
                    this.startPlayback();
                    return true;
                }
                int count = this.audio.read(this.buffer);
                if (count == -1) break;
                float bufferLength = (float)count / sampleSize / sampleRate;
                this.positionOffset += bufferLength;
            }
            if (this.loop) {
                this.initStreams();
                return false;
            }
            this.done = true;
            return false;
        }
        catch (IOException e) {
            Log.error(e);
            return false;
        }
    }

    private void startPlayback() {
        AL10.alSourcei((int)this.source, (int)4103, (int)0);
        AL10.alSourcef((int)this.source, (int)4099, (float)this.pitch);
        this.remainingBufferCount = 3;
        int i = 0;
        while (i < 3) {
            this.stream(this.bufferNames.get(i));
            ++i;
        }
        AL10.alSourceQueueBuffers((int)this.source, (IntBuffer)this.bufferNames);
        AL10.alSourcePlay((int)this.source);
    }

    public float getPosition() {
        return this.positionOffset + AL10.alGetSourcef((int)this.source, (int)4132);
    }
}


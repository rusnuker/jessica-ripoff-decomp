/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.BufferUtils
 */
package org.newdawn.slick;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.Image;
import org.newdawn.slick.opengl.ImageData;

public class ImageBuffer
implements ImageData {
    private int width;
    private int height;
    private int texWidth;
    private int texHeight;
    private byte[] rawData;

    public ImageBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.texWidth = this.get2Fold(width);
        this.texHeight = this.get2Fold(height);
        this.rawData = new byte[this.texWidth * this.texHeight * 4];
    }

    public byte[] getRGBA() {
        return this.rawData;
    }

    @Override
    public int getDepth() {
        return 32;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getTexHeight() {
        return this.texHeight;
    }

    @Override
    public int getTexWidth() {
        return this.texWidth;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public ByteBuffer getImageBufferData() {
        ByteBuffer scratch = BufferUtils.createByteBuffer((int)this.rawData.length);
        scratch.put(this.rawData);
        scratch.flip();
        return scratch;
    }

    public void setRGBA(int x, int y, int r, int g, int b, int a) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            throw new RuntimeException("Specified location: " + x + "," + y + " outside of image");
        }
        int ofs = (x + y * this.texWidth) * 4;
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            this.rawData[ofs] = (byte)b;
            this.rawData[ofs + 1] = (byte)g;
            this.rawData[ofs + 2] = (byte)r;
            this.rawData[ofs + 3] = (byte)a;
        } else {
            this.rawData[ofs] = (byte)r;
            this.rawData[ofs + 1] = (byte)g;
            this.rawData[ofs + 2] = (byte)b;
            this.rawData[ofs + 3] = (byte)a;
        }
    }

    public Image getImage() {
        return new Image(this);
    }

    public Image getImage(int filter) {
        return new Image(this, filter);
    }

    private int get2Fold(int fold) {
        int ret = 2;
        while (ret < fold) {
            ret *= 2;
        }
        return ret;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.tests;

import java.nio.ByteOrder;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.SlickException;

public class ImageBufferEndianTest
extends BasicGame {
    private ImageBuffer redImageBuffer;
    private ImageBuffer blueImageBuffer;
    private Image fromRed;
    private Image fromBlue;
    private String endian;

    public ImageBufferEndianTest() {
        super("ImageBuffer Endian Test");
    }

    public static void main(String[] args) {
        try {
            AppGameContainer container = new AppGameContainer(new ImageBufferEndianTest());
            container.setDisplayMode(800, 600, false);
            container.start();
        }
        catch (SlickException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        g.setColor(Color.white);
        g.drawString("Endianness is " + this.endian, 10.0f, 100.0f);
        g.drawString("Image below should be red", 10.0f, 200.0f);
        g.drawImage(this.fromRed, 10.0f, 220.0f);
        g.drawString("Image below should be blue", 410.0f, 200.0f);
        g.drawImage(this.fromBlue, 410.0f, 220.0f);
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        this.endian = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN ? "Big endian" : (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? "Little endian" : "no idea");
        this.redImageBuffer = new ImageBuffer(100, 100);
        this.fillImageBufferWithColor(this.redImageBuffer, Color.red, 100, 100);
        this.blueImageBuffer = new ImageBuffer(100, 100);
        this.fillImageBufferWithColor(this.blueImageBuffer, Color.blue, 100, 100);
        this.fromRed = this.redImageBuffer.getImage();
        this.fromBlue = this.blueImageBuffer.getImage();
    }

    private void fillImageBufferWithColor(ImageBuffer buffer, Color c, int width, int height) {
        int x = 0;
        while (x < width) {
            int y = 0;
            while (y < height) {
                buffer.setRGBA(x, y, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
                ++y;
            }
            ++x;
        }
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {
    }
}


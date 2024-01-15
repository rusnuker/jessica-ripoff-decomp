/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.tests;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.SlickException;

public class ImageBufferTest
extends BasicGame {
    private Image image;

    public ImageBufferTest() {
        super("Image Buffer Test");
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        ImageBuffer buffer = new ImageBuffer(320, 200);
        int x = 0;
        while (x < 320) {
            int y = 0;
            while (y < 200) {
                if (y == 20) {
                    buffer.setRGBA(x, y, 255, 255, 255, 255);
                } else {
                    buffer.setRGBA(x, y, x, y, 0, 255);
                }
                ++y;
            }
            ++x;
        }
        this.image = buffer.getImage();
    }

    @Override
    public void render(GameContainer container, Graphics g) {
        this.image.draw(50.0f, 50.0f);
    }

    @Override
    public void update(GameContainer container, int delta) {
    }

    @Override
    public void keyPressed(int key, char c) {
        if (key == 1) {
            System.exit(0);
        }
    }

    public static void main(String[] argv) {
        try {
            AppGameContainer container = new AppGameContainer(new ImageBufferTest());
            container.setDisplayMode(800, 600, false);
            container.start();
        }
        catch (SlickException e) {
            e.printStackTrace();
        }
    }
}


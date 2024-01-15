/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.tests;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

public class TransformTest
extends BasicGame {
    private float scale = 1.0f;
    private boolean scaleUp;
    private boolean scaleDown;

    public TransformTest() {
        super("Transform Test");
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        container.setTargetFrameRate(100);
    }

    @Override
    public void render(GameContainer contiainer, Graphics g) {
        g.translate(320.0f, 240.0f);
        g.scale(this.scale, this.scale);
        g.setColor(Color.red);
        int x = 0;
        while (x < 10) {
            int y = 0;
            while (y < 10) {
                g.fillRect(-500 + x * 100, -500 + y * 100, 80.0f, 80.0f);
                ++y;
            }
            ++x;
        }
        g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
        g.fillRect(-320.0f, -240.0f, 640.0f, 480.0f);
        g.setColor(Color.white);
        g.drawRect(-320.0f, -240.0f, 640.0f, 480.0f);
    }

    @Override
    public void update(GameContainer container, int delta) {
        if (this.scaleUp) {
            this.scale += (float)delta * 0.001f;
        }
        if (this.scaleDown) {
            this.scale -= (float)delta * 0.001f;
        }
    }

    @Override
    public void keyPressed(int key, char c) {
        if (key == 1) {
            System.exit(0);
        }
        if (key == 16) {
            this.scaleUp = true;
        }
        if (key == 30) {
            this.scaleDown = true;
        }
    }

    @Override
    public void keyReleased(int key, char c) {
        if (key == 16) {
            this.scaleUp = false;
        }
        if (key == 30) {
            this.scaleDown = false;
        }
    }

    public static void main(String[] argv) {
        try {
            AppGameContainer container = new AppGameContainer(new TransformTest());
            container.setDisplayMode(640, 480, false);
            container.start();
        }
        catch (SlickException e) {
            e.printStackTrace();
        }
    }
}


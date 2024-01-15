/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.tests;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.openal.SoundStore;

public class SoundPositionTest
extends BasicGame {
    private GameContainer myContainer;
    private Music music;
    private int[] engines = new int[3];

    public SoundPositionTest() {
        super("Music Position Test");
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        SoundStore.get().setMaxSources(32);
        this.myContainer = container;
        this.music = new Music("testdata/kirby.ogg", true);
        this.music.play();
    }

    @Override
    public void render(GameContainer container, Graphics g) {
        g.setColor(Color.white);
        g.drawString("Position: " + this.music.getPosition(), 100.0f, 100.0f);
        g.drawString("Space - Pause/Resume", 100.0f, 130.0f);
        g.drawString("Right Arrow - Advance 5 seconds", 100.0f, 145.0f);
    }

    @Override
    public void update(GameContainer container, int delta) {
    }

    @Override
    public void keyPressed(int key, char c) {
        if (key == 57) {
            if (this.music.playing()) {
                this.music.pause();
            } else {
                this.music.resume();
            }
        }
        if (key == 205) {
            this.music.setPosition(this.music.getPosition() + 5.0f);
        }
    }

    public static void main(String[] argv) {
        try {
            AppGameContainer container = new AppGameContainer(new SoundPositionTest());
            container.setDisplayMode(800, 600, false);
            container.start();
        }
        catch (SlickException e) {
            e.printStackTrace();
        }
    }
}


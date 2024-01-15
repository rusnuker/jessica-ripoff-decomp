/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.darkstorm.minecraft.gui.GuiManager;
import org.darkstorm.minecraft.gui.component.Frame;
import org.darkstorm.minecraft.gui.theme.Theme;

public abstract class AbstractGuiManager
implements GuiManager {
    private final List<Frame> frames = new CopyOnWriteArrayList<Frame>();
    private Theme theme;

    @Override
    public abstract void setup();

    @Override
    public void addFrame(Frame frame) {
        frame.setTheme(this.theme);
        this.frames.add(0, frame);
    }

    @Override
    public void removeFrame(Frame frame) {
        this.frames.remove(frame);
    }

    @Override
    public Frame[] getFrames() {
        return this.frames.toArray(new Frame[this.frames.size()]);
    }

    @Override
    public void bringForward(Frame frame) {
        if (this.frames.remove(frame)) {
            this.frames.add(0, frame);
        }
    }

    @Override
    public Theme getTheme() {
        return this.theme;
    }

    @Override
    public void setTheme(Theme theme) {
        this.theme = theme;
        for (Frame frame : this.frames) {
            frame.setTheme(theme);
        }
        this.resizeComponents();
    }

    protected abstract void resizeComponents();

    @Override
    public void render() {
        Frame[] frames = this.getFrames();
        int i = frames.length - 1;
        while (i >= 0) {
            frames[i].render();
            --i;
        }
    }

    @Override
    public void renderPinned() {
        Frame[] frames = this.getFrames();
        int i = frames.length - 1;
        while (i >= 0) {
            if (frames[i].isPinned()) {
                frames[i].render();
            }
            --i;
        }
    }

    @Override
    public void update() {
        Frame[] frames = this.getFrames();
        int i = frames.length - 1;
        while (i >= 0) {
            frames[i].update();
            --i;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui;

import org.darkstorm.minecraft.gui.component.Frame;
import org.darkstorm.minecraft.gui.theme.Theme;

public interface GuiManager {
    public void setup();

    public void addFrame(Frame var1);

    public void removeFrame(Frame var1);

    public Frame[] getFrames();

    public void bringForward(Frame var1);

    public Theme getTheme();

    public void setTheme(Theme var1);

    public void render();

    public void renderPinned();

    public void update();
}


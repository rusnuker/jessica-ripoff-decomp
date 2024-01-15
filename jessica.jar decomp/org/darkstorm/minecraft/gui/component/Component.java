/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import org.darkstorm.minecraft.gui.component.Container;
import org.darkstorm.minecraft.gui.theme.Theme;

public interface Component {
    public Theme getTheme();

    public void setTheme(Theme var1);

    public void render();

    public void update();

    public int getX();

    public int getY();

    public int getWidth();

    public int getHeight();

    public void setX(int var1);

    public void setY(int var1);

    public void setWidth(int var1);

    public void setHeight(int var1);

    public Point getLocation();

    public Dimension getSize();

    public Rectangle getArea();

    public Container getParent();

    public Color getBackgroundColor();

    public Color getForegroundColor();

    public void setBackgroundColor(Color var1);

    public void setForegroundColor(Color var1);

    public void setParent(Container var1);

    public void onMousePress(int var1, int var2, int var3);

    public void onMouseRelease(int var1, int var2, int var3);

    public void resize();

    public boolean isVisible();

    public void setVisible(boolean var1);

    public boolean isEnabled();

    public void setEnabled(boolean var1);
}


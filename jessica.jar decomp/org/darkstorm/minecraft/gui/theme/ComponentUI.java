/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.theme;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import org.darkstorm.minecraft.gui.component.Component;
import org.darkstorm.minecraft.gui.component.Container;

public interface ComponentUI {
    public void render(Component var1);

    public Rectangle getChildRenderArea(Container var1);

    public Dimension getDefaultSize(Component var1);

    public Color getDefaultBackgroundColor(Component var1);

    public Color getDefaultForegroundColor(Component var1);

    public Rectangle[] getInteractableRegions(Component var1);

    public void handleInteraction(Component var1, Point var2, int var3);

    public void handleUpdate(Component var1);
}


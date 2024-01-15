/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.layout;

import java.awt.Dimension;
import java.awt.Rectangle;
import org.darkstorm.minecraft.gui.layout.Constraint;
import org.darkstorm.minecraft.gui.layout.LayoutManager;

public class BasicLayoutManager
implements LayoutManager {
    @Override
    public void reposition(Rectangle area, Rectangle[] componentAreas, Constraint[][] constraints) {
        int offset = 0;
        Rectangle[] rectangleArray = componentAreas;
        int n = componentAreas.length;
        int n2 = 0;
        while (n2 < n) {
            Rectangle componentArea = rectangleArray[n2];
            if (componentArea == null) {
                throw new NullPointerException();
            }
            componentArea.x = area.x;
            componentArea.y = area.y + offset;
            offset += componentArea.height;
            ++n2;
        }
    }

    @Override
    public Dimension getOptimalPositionedSize(Rectangle[] componentAreas, Constraint[][] constraints) {
        int width = 0;
        int height = 0;
        Rectangle[] rectangleArray = componentAreas;
        int n = componentAreas.length;
        int n2 = 0;
        while (n2 < n) {
            Rectangle component = rectangleArray[n2];
            if (component == null) {
                throw new NullPointerException();
            }
            height += component.height;
            width = Math.max(width, component.width);
            ++n2;
        }
        return new Dimension(width, height);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.layout;

import java.awt.Dimension;
import java.awt.Rectangle;
import org.darkstorm.minecraft.gui.layout.Constraint;

public interface LayoutManager {
    public void reposition(Rectangle var1, Rectangle[] var2, Constraint[][] var3);

    public Dimension getOptimalPositionedSize(Rectangle[] var1, Constraint[][] var2);
}


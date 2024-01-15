/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component;

import org.darkstorm.minecraft.gui.component.TextComponent;

public interface Label
extends TextComponent {
    public TextAlignment getHorizontalAlignment();

    public TextAlignment getVerticalAlignment();

    public void setHorizontalAlignment(TextAlignment var1);

    public void setVerticalAlignment(TextAlignment var1);

    public static enum TextAlignment {
        CENTER,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM;

    }
}


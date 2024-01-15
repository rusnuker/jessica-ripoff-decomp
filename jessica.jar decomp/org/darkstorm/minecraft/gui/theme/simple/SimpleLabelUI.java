/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
package org.darkstorm.minecraft.gui.theme.simple;

import java.awt.Color;
import java.awt.Dimension;
import org.darkstorm.minecraft.gui.component.Label;
import org.darkstorm.minecraft.gui.theme.AbstractComponentUI;
import org.darkstorm.minecraft.gui.theme.simple.SimpleTheme;
import org.darkstorm.minecraft.gui.util.RenderUtil;
import org.lwjgl.opengl.GL11;

public class SimpleLabelUI
extends AbstractComponentUI<Label> {
    private final SimpleTheme theme;

    SimpleLabelUI(SimpleTheme theme) {
        super(Label.class);
        this.theme = theme;
        this.foreground = Color.WHITE;
        this.background = new Color(128, 128, 128, 128);
    }

    @Override
    protected void renderComponent(Label label) {
        this.translateComponent(label, false);
        int x = 0;
        int y = 0;
        switch (label.getHorizontalAlignment()) {
            case CENTER: {
                x += label.getWidth() / 2 - this.theme.getFontRenderer().getStringWidth(label.getText()) / 2;
                break;
            }
            case RIGHT: {
                x += label.getWidth() - this.theme.getFontRenderer().getStringWidth(label.getText()) - 2;
                break;
            }
            default: {
                x += 2;
            }
        }
        switch (label.getVerticalAlignment()) {
            case TOP: {
                y += 2;
                break;
            }
            case BOTTOM: {
                y += label.getHeight() - this.theme.getFontRenderer().FONT_HEIGHT - 2;
                break;
            }
            default: {
                y += label.getHeight() / 2 - this.theme.getFontRenderer().FONT_HEIGHT / 2;
            }
        }
        GL11.glEnable((int)3042);
        GL11.glEnable((int)3553);
        GL11.glDisable((int)2884);
        this.theme.getFontRenderer().drawString(label.getText(), x, y, RenderUtil.toRGBA(label.getForegroundColor()));
        GL11.glEnable((int)2884);
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        this.translateComponent(label, true);
    }

    @Override
    protected Dimension getDefaultComponentSize(Label component) {
        return new Dimension(this.theme.getFontRenderer().getStringWidth(component.getText()) + 4, this.theme.getFontRenderer().FONT_HEIGHT + 4);
    }
}


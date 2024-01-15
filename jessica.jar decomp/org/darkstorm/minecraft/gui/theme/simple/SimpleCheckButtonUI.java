/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Mouse
 *  org.lwjgl.opengl.GL11
 */
package org.darkstorm.minecraft.gui.theme.simple;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import org.darkstorm.minecraft.gui.component.CheckButton;
import org.darkstorm.minecraft.gui.component.Container;
import org.darkstorm.minecraft.gui.theme.AbstractComponentUI;
import org.darkstorm.minecraft.gui.theme.simple.SimpleTheme;
import org.darkstorm.minecraft.gui.util.RenderUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class SimpleCheckButtonUI
extends AbstractComponentUI<CheckButton> {
    private final SimpleTheme theme;

    SimpleCheckButtonUI(SimpleTheme theme) {
        super(CheckButton.class);
        this.theme = theme;
        this.foreground = Color.WHITE;
        this.background = new Color(128, 128, 128, 128);
    }

    @Override
    protected void renderComponent(CheckButton button) {
        this.translateComponent(button, false);
        Rectangle area = button.getArea();
        GL11.glEnable((int)3042);
        GL11.glDisable((int)2884);
        GL11.glDisable((int)3553);
        RenderUtil.setColor(button.getBackgroundColor());
        int size = area.height;
        GL11.glBegin((int)7);
        GL11.glVertex2d((double)2.0, (double)2.0);
        GL11.glVertex2d((double)(size + 2), (double)2.0);
        GL11.glVertex2d((double)(size + 2), (double)(size + 2));
        GL11.glVertex2d((double)2.0, (double)(size + 2));
        GL11.glEnd();
        if (button.isSelected()) {
            GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)1.0f);
            GL11.glBegin((int)7);
            GL11.glVertex2d((double)3.0, (double)3.5);
            GL11.glVertex2d((double)((double)size + 0.5), (double)3.5);
            GL11.glVertex2d((double)((double)size + 0.5), (double)(size + 1));
            GL11.glVertex2d((double)3.0, (double)(size + 1));
            GL11.glEnd();
        }
        GL11.glLineWidth((float)1.0f);
        GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        GL11.glBegin((int)2);
        GL11.glVertex2d((double)2.0, (double)2.0);
        GL11.glVertex2d((double)(size + 2), (double)2.0);
        GL11.glVertex2d((double)(size + 2), (double)(size + 2));
        GL11.glVertex2d((double)1.5, (double)(size + 2));
        GL11.glEnd();
        Point mouse = RenderUtil.calculateMouseLocation();
        Container parent = button.getParent();
        while (parent != null) {
            mouse.x -= parent.getX();
            mouse.y -= parent.getY();
            parent = parent.getParent();
        }
        if (area.contains(mouse)) {
            GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)(Mouse.isButtonDown((int)0) ? 0.5f : 0.3f));
            GL11.glBegin((int)7);
            GL11.glVertex2d((double)0.0, (double)0.0);
            GL11.glVertex2d((double)area.width, (double)0.0);
            GL11.glVertex2d((double)area.width, (double)(area.height + 4));
            GL11.glVertex2d((double)0.0, (double)(area.height + 4));
            GL11.glEnd();
        }
        GL11.glEnable((int)3553);
        String text = button.getText();
        this.theme.getFontRenderer().drawString(text, size + 4, area.height / 2 - this.theme.getFontRenderer().FONT_HEIGHT / 2, RenderUtil.toRGBA(button.getForegroundColor()));
        GL11.glEnable((int)2884);
        GL11.glDisable((int)3042);
        this.translateComponent(button, true);
    }

    @Override
    protected Dimension getDefaultComponentSize(CheckButton component) {
        return new Dimension(this.theme.getFontRenderer().getStringWidth(component.getText()) + this.theme.getFontRenderer().FONT_HEIGHT + 6, this.theme.getFontRenderer().FONT_HEIGHT + 4);
    }

    @Override
    protected Rectangle[] getInteractableComponentRegions(CheckButton component) {
        return new Rectangle[]{new Rectangle(0, 0, component.getWidth(), component.getHeight())};
    }

    @Override
    protected void handleComponentInteraction(CheckButton component, Point location, int button) {
        if (location.x <= component.getWidth() && location.y <= component.getHeight() && button == 0) {
            component.press();
        }
    }
}


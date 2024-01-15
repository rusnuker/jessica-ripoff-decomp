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
import org.darkstorm.minecraft.gui.component.ComboBox;
import org.darkstorm.minecraft.gui.component.Container;
import org.darkstorm.minecraft.gui.theme.AbstractComponentUI;
import org.darkstorm.minecraft.gui.theme.simple.SimpleTheme;
import org.darkstorm.minecraft.gui.util.RenderUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class SimpleComboBoxUI
extends AbstractComponentUI<ComboBox> {
    private final SimpleTheme theme;

    SimpleComboBoxUI(SimpleTheme theme) {
        super(ComboBox.class);
        this.theme = theme;
        this.foreground = Color.WHITE;
        this.background = new Color(128, 128, 128, 192);
    }

    @Override
    protected void renderComponent(ComboBox component) {
        this.translateComponent(component, false);
        Rectangle area = component.getArea();
        GL11.glEnable((int)3042);
        GL11.glDisable((int)2884);
        GL11.glDisable((int)3553);
        int maxWidth = 0;
        String[] stringArray = component.getElements();
        int n = stringArray.length;
        int n2 = 0;
        while (n2 < n) {
            String element = stringArray[n2];
            maxWidth = Math.max(maxWidth, this.theme.getFontRenderer().getStringWidth(element));
            ++n2;
        }
        int extendedHeight = 0;
        if (component.isSelected()) {
            String[] elements = component.getElements();
            int i = 0;
            while (i < elements.length - 1) {
                extendedHeight += this.theme.getFontRenderer().FONT_HEIGHT + 2;
                ++i;
            }
            extendedHeight += 2;
        }
        RenderUtil.setColor(component.getBackgroundColor());
        GL11.glBegin((int)7);
        GL11.glVertex2d((double)0.0, (double)0.0);
        GL11.glVertex2d((double)area.width, (double)0.0);
        GL11.glVertex2d((double)area.width, (double)(area.height + extendedHeight));
        GL11.glVertex2d((double)0.0, (double)(area.height + extendedHeight));
        GL11.glEnd();
        Point mouse = RenderUtil.calculateMouseLocation();
        Container parent = component.getParent();
        while (parent != null) {
            mouse.x -= parent.getX();
            mouse.y -= parent.getY();
            parent = parent.getParent();
        }
        GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)(Mouse.isButtonDown((int)0) ? 0.5f : 0.3f));
        if (area.contains(mouse)) {
            GL11.glBegin((int)7);
            GL11.glVertex2d((double)0.0, (double)0.0);
            GL11.glVertex2d((double)area.width, (double)0.0);
            GL11.glVertex2d((double)area.width, (double)area.height);
            GL11.glVertex2d((double)0.0, (double)area.height);
            GL11.glEnd();
        } else if (component.isSelected() && mouse.x >= area.x && mouse.x <= area.x + area.width) {
            int offset = component.getHeight();
            String[] elements = component.getElements();
            int i = 0;
            while (i < elements.length) {
                if (i != component.getSelectedIndex()) {
                    int height = this.theme.getFontRenderer().FONT_HEIGHT + 2;
                    if (!(component.getSelectedIndex() == 0 ? i != 1 : i != 0) || (component.getSelectedIndex() == elements.length - 1 ? i == elements.length - 2 : i == elements.length - 1)) {
                        ++height;
                    }
                    if (mouse.y >= area.y + offset && mouse.y <= area.y + offset + height) {
                        GL11.glBegin((int)7);
                        GL11.glVertex2d((double)0.0, (double)offset);
                        GL11.glVertex2d((double)0.0, (double)(offset + height));
                        GL11.glVertex2d((double)area.width, (double)(offset + height));
                        GL11.glVertex2d((double)area.width, (double)offset);
                        GL11.glEnd();
                        break;
                    }
                    offset += height;
                }
                ++i;
            }
        }
        int height = this.theme.getFontRenderer().FONT_HEIGHT + 4;
        GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)0.3f);
        GL11.glBegin((int)4);
        if (component.isSelected()) {
            GL11.glVertex2d((double)((double)(maxWidth + 4) + (double)height / 2.0), (double)((double)height / 3.0));
            GL11.glVertex2d((double)((double)(maxWidth + 4) + (double)height / 3.0), (double)(2.0 * (double)height / 3.0));
            GL11.glVertex2d((double)((double)(maxWidth + 4) + 2.0 * (double)height / 3.0), (double)(2.0 * (double)height / 3.0));
        } else {
            GL11.glVertex2d((double)((double)(maxWidth + 4) + (double)height / 3.0), (double)((double)height / 3.0));
            GL11.glVertex2d((double)((double)(maxWidth + 4) + 2.0 * (double)height / 3.0), (double)((double)height / 3.0));
            GL11.glVertex2d((double)((double)(maxWidth + 4) + (double)height / 2.0), (double)(2.0 * (double)height / 3.0));
        }
        GL11.glEnd();
        GL11.glLineWidth((float)1.0f);
        GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        if (component.isSelected()) {
            GL11.glBegin((int)1);
            GL11.glVertex2d((double)2.0, (double)area.height);
            GL11.glVertex2d((double)(area.width - 2), (double)area.height);
            GL11.glEnd();
        }
        GL11.glBegin((int)1);
        GL11.glVertex2d((double)(maxWidth + 4), (double)2.0);
        GL11.glVertex2d((double)(maxWidth + 4), (double)(area.height - 2));
        GL11.glEnd();
        GL11.glBegin((int)2);
        if (component.isSelected()) {
            GL11.glVertex2d((double)((double)(maxWidth + 4) + (double)height / 2.0), (double)((double)height / 3.0));
            GL11.glVertex2d((double)((double)(maxWidth + 4) + (double)height / 3.0), (double)(2.0 * (double)height / 3.0));
            GL11.glVertex2d((double)((double)(maxWidth + 4) + 2.0 * (double)height / 3.0), (double)(2.0 * (double)height / 3.0));
        } else {
            GL11.glVertex2d((double)((double)(maxWidth + 4) + (double)height / 3.0), (double)((double)height / 3.0));
            GL11.glVertex2d((double)((double)(maxWidth + 4) + 2.0 * (double)height / 3.0), (double)((double)height / 3.0));
            GL11.glVertex2d((double)((double)(maxWidth + 4) + (double)height / 2.0), (double)(2.0 * (double)height / 3.0));
        }
        GL11.glEnd();
        GL11.glEnable((int)3553);
        String text = component.getSelectedElement();
        this.theme.getFontRenderer().drawString(text, 2, area.height / 2 - this.theme.getFontRenderer().FONT_HEIGHT / 2, RenderUtil.toRGBA(component.getForegroundColor()));
        if (component.isSelected()) {
            int offset = area.height + 2;
            String[] elements = component.getElements();
            int i = 0;
            while (i < elements.length) {
                if (i != component.getSelectedIndex()) {
                    this.theme.getFontRenderer().drawString(elements[i], 2, offset, RenderUtil.toRGBA(component.getForegroundColor()));
                    offset += this.theme.getFontRenderer().FONT_HEIGHT + 2;
                }
                ++i;
            }
        }
        GL11.glEnable((int)2884);
        GL11.glDisable((int)3042);
        this.translateComponent(component, true);
    }

    @Override
    protected Dimension getDefaultComponentSize(ComboBox component) {
        int maxWidth = 0;
        String[] stringArray = component.getElements();
        int n = stringArray.length;
        int n2 = 0;
        while (n2 < n) {
            String element = stringArray[n2];
            maxWidth = Math.max(maxWidth, this.theme.getFontRenderer().getStringWidth(element));
            ++n2;
        }
        return new Dimension(maxWidth + 8 + this.theme.getFontRenderer().FONT_HEIGHT, this.theme.getFontRenderer().FONT_HEIGHT + 4);
    }

    @Override
    protected Rectangle[] getInteractableComponentRegions(ComboBox component) {
        int height = component.getHeight();
        if (component.isSelected()) {
            String[] elements = component.getElements();
            int i = 0;
            while (i < elements.length) {
                height += this.theme.getFontRenderer().FONT_HEIGHT + 2;
                ++i;
            }
            height += 2;
        }
        return new Rectangle[]{new Rectangle(0, 0, component.getWidth(), height)};
    }

    @Override
    protected void handleComponentInteraction(ComboBox component, Point location, int button) {
        if (button != 0) {
            return;
        }
        if (location.x <= component.getWidth() && location.y <= component.getHeight()) {
            component.setSelected(!component.isSelected());
        } else if (location.x <= component.getWidth() && component.isSelected()) {
            int offset = component.getHeight() + 2;
            String[] elements = component.getElements();
            int i = 0;
            while (i < elements.length) {
                if (i != component.getSelectedIndex()) {
                    if (location.y >= offset && location.y <= offset + this.theme.getFontRenderer().FONT_HEIGHT) {
                        component.setSelectedIndex(i);
                        component.setSelected(false);
                        break;
                    }
                    offset += this.theme.getFontRenderer().FONT_HEIGHT + 2;
                }
                ++i;
            }
        }
    }
}


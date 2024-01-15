/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
package org.darkstorm.minecraft.gui.theme.simple;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import org.darkstorm.minecraft.gui.component.Component;
import org.darkstorm.minecraft.gui.component.Container;
import org.darkstorm.minecraft.gui.component.Frame;
import org.darkstorm.minecraft.gui.layout.Constraint;
import org.darkstorm.minecraft.gui.theme.AbstractComponentUI;
import org.darkstorm.minecraft.gui.theme.simple.SimpleTheme;
import org.darkstorm.minecraft.gui.util.RenderUtil;
import org.lwjgl.opengl.GL11;

public class SimpleFrameUI
extends AbstractComponentUI<Frame> {
    private final SimpleTheme theme;

    SimpleFrameUI(SimpleTheme theme) {
        super(Frame.class);
        this.theme = theme;
        this.foreground = Color.WHITE;
        this.background = new Color(40, 35, 36, 60);
    }

    @Override
    protected void renderComponent(Frame component) {
        Rectangle area = new Rectangle(component.getArea());
        int fontHeight = this.theme.getFontRenderer().FONT_HEIGHT;
        this.translateComponent(component, false);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)2884);
        GL11.glDisable((int)3553);
        GL11.glBlendFunc((int)770, (int)771);
        if (component.isMinimized()) {
            area.height = fontHeight + 4;
        }
        RenderUtil.setColor(component.getBackgroundColor());
        GL11.glBegin((int)7);
        GL11.glVertex2d((double)0.0, (double)0.0);
        GL11.glVertex2d((double)area.width, (double)0.0);
        GL11.glVertex2d((double)area.width, (double)area.height);
        GL11.glVertex2d((double)0.0, (double)area.height);
        GL11.glEnd();
        int offset = component.getWidth() - 2;
        Point mouse = RenderUtil.calculateMouseLocation();
        Container parent = component;
        while (parent != null) {
            mouse.x -= parent.getX();
            mouse.y -= parent.getY();
            parent = parent.getParent();
        }
        boolean[] checks = new boolean[]{component.isClosable(), component.isPinnable(), component.isMinimizable()};
        boolean[] blArray = new boolean[3];
        blArray[1] = component.isPinned();
        blArray[2] = component.isMinimized();
        boolean[] overlays = blArray;
        int i = 0;
        while (i < checks.length) {
            if (checks[i]) {
                RenderUtil.setColor(component.getBackgroundColor());
                GL11.glBegin((int)7);
                GL11.glVertex2d((double)(offset - fontHeight), (double)2.0);
                GL11.glVertex2d((double)offset, (double)2.0);
                GL11.glVertex2d((double)offset, (double)(fontHeight + 2));
                GL11.glVertex2d((double)(offset - fontHeight), (double)(fontHeight + 2));
                GL11.glEnd();
                if (overlays[i]) {
                    GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)0.5f);
                    GL11.glBegin((int)7);
                    GL11.glVertex2d((double)(offset - fontHeight), (double)2.0);
                    GL11.glVertex2d((double)offset, (double)2.0);
                    GL11.glVertex2d((double)offset, (double)(fontHeight + 2));
                    GL11.glVertex2d((double)(offset - fontHeight), (double)(fontHeight + 2));
                    GL11.glEnd();
                }
                if (mouse.x >= offset - fontHeight && mouse.x <= offset && mouse.y >= 2 && mouse.y <= fontHeight + 2) {
                    GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)0.3f);
                    GL11.glBegin((int)7);
                    GL11.glVertex2d((double)(offset - fontHeight), (double)2.0);
                    GL11.glVertex2d((double)offset, (double)2.0);
                    GL11.glVertex2d((double)offset, (double)(fontHeight + 2));
                    GL11.glVertex2d((double)(offset - fontHeight), (double)(fontHeight + 2));
                    GL11.glEnd();
                }
                GL11.glLineWidth((float)1.0f);
                GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)1.0f);
                GL11.glBegin((int)2);
                GL11.glVertex2d((double)(offset - fontHeight), (double)2.0);
                GL11.glVertex2d((double)offset, (double)2.0);
                GL11.glVertex2d((double)offset, (double)(fontHeight + 2));
                GL11.glVertex2d((double)((double)(offset - fontHeight) - 0.5), (double)(fontHeight + 2));
                GL11.glEnd();
                offset -= fontHeight + 2;
            }
            ++i;
        }
        GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        GL11.glLineWidth((float)1.0f);
        GL11.glBegin((int)1);
        GL11.glVertex2d((double)2.0, (double)(this.theme.getFontRenderer().FONT_HEIGHT + 4));
        GL11.glVertex2d((double)(area.width - 2), (double)(this.theme.getFontRenderer().FONT_HEIGHT + 4));
        GL11.glEnd();
        GL11.glEnable((int)3553);
        this.theme.getFontRenderer().drawString(component.getTitle(), 2, 2, RenderUtil.toRGBA(component.getForegroundColor()));
        GL11.glEnable((int)2884);
        GL11.glDisable((int)3042);
        this.translateComponent(component, true);
    }

    @Override
    protected Rectangle getContainerChildRenderArea(Frame container) {
        Rectangle area = new Rectangle(container.getArea());
        area.x = 2;
        area.y = this.theme.getFontRenderer().FONT_HEIGHT + 6;
        area.width -= 4;
        area.height -= this.theme.getFontRenderer().FONT_HEIGHT + 8;
        return area;
    }

    @Override
    protected Dimension getDefaultComponentSize(Frame component) {
        Component[] children = component.getChildren();
        Rectangle[] areas = new Rectangle[children.length];
        Constraint[][] constraints = new Constraint[children.length][];
        int i = 0;
        while (i < children.length) {
            Component child = children[i];
            Dimension size = child.getTheme().getUIForComponent(child).getDefaultSize(child);
            areas[i] = new Rectangle(0, 0, size.width, size.height);
            constraints[i] = component.getConstraints(child);
            ++i;
        }
        Dimension size = component.getLayoutManager().getOptimalPositionedSize(areas, constraints);
        size.width += 4;
        size.height += this.theme.getFontRenderer().FONT_HEIGHT + 8;
        return size;
    }

    @Override
    protected Rectangle[] getInteractableComponentRegions(Frame component) {
        return new Rectangle[]{new Rectangle(0, 0, component.getWidth(), this.theme.getFontRenderer().FONT_HEIGHT + 4)};
    }

    @Override
    protected void handleComponentInteraction(Frame component, Point location, int button) {
        if (button != 0) {
            return;
        }
        int offset = component.getWidth() - 2;
        int textHeight = this.theme.getFontRenderer().FONT_HEIGHT;
        if (component.isClosable()) {
            if (location.x >= offset - textHeight && location.x <= offset && location.y >= 2 && location.y <= textHeight + 2) {
                component.close();
                return;
            }
            offset -= textHeight + 2;
        }
        if (component.isPinnable()) {
            if (location.x >= offset - textHeight && location.x <= offset && location.y >= 2 && location.y <= textHeight + 2) {
                component.setPinned(!component.isPinned());
                return;
            }
            offset -= textHeight + 2;
        }
        if (component.isMinimizable()) {
            if (location.x >= offset - textHeight && location.x <= offset && location.y >= 2 && location.y <= textHeight + 2) {
                component.setMinimized(!component.isMinimized());
                return;
            }
            offset -= textHeight + 2;
        }
        if (location.x >= 0 && location.x <= offset && location.y >= 0 && location.y <= textHeight + 4) {
            component.setDragging(true);
            return;
        }
    }
}


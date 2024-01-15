/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
package org.darkstorm.minecraft.gui.theme.simple;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import org.darkstorm.minecraft.gui.component.Component;
import org.darkstorm.minecraft.gui.component.Panel;
import org.darkstorm.minecraft.gui.layout.Constraint;
import org.darkstorm.minecraft.gui.theme.AbstractComponentUI;
import org.darkstorm.minecraft.gui.theme.simple.SimpleTheme;
import org.darkstorm.minecraft.gui.util.RenderUtil;
import org.lwjgl.opengl.GL11;

public class SimplePanelUI
extends AbstractComponentUI<Panel> {
    private final SimpleTheme theme;

    SimplePanelUI(SimpleTheme theme) {
        super(Panel.class);
        this.theme = theme;
        this.foreground = Color.WHITE;
        this.background = new Color(40, 35, 36, 28);
    }

    @Override
    protected void renderComponent(Panel component) {
        if (component.getParent() != null) {
            return;
        }
        Rectangle area = component.getArea();
        this.translateComponent(component, false);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glDisable((int)2884);
        GL11.glBlendFunc((int)770, (int)771);
        RenderUtil.setColor(component.getBackgroundColor());
        GL11.glBegin((int)7);
        GL11.glVertex2d((double)0.0, (double)0.0);
        GL11.glVertex2d((double)area.width, (double)0.0);
        GL11.glVertex2d((double)area.width, (double)area.height);
        GL11.glVertex2d((double)0.0, (double)area.height);
        GL11.glEnd();
        GL11.glEnable((int)2884);
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        this.translateComponent(component, true);
    }

    @Override
    protected Dimension getDefaultComponentSize(Panel component) {
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
        return component.getLayoutManager().getOptimalPositionedSize(areas, constraints);
    }
}


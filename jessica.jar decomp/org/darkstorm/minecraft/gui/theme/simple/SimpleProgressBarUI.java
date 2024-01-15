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
import net.minecraft.client.gui.FontRenderer;
import org.darkstorm.minecraft.gui.component.ProgressBar;
import org.darkstorm.minecraft.gui.theme.AbstractComponentUI;
import org.darkstorm.minecraft.gui.theme.simple.SimpleTheme;
import org.darkstorm.minecraft.gui.util.RenderUtil;
import org.lwjgl.opengl.GL11;

public class SimpleProgressBarUI
extends AbstractComponentUI<ProgressBar> {
    private SimpleTheme theme;

    public SimpleProgressBarUI(SimpleTheme theme) {
        super(ProgressBar.class);
        this.theme = theme;
        this.foreground = Color.LIGHT_GRAY;
        this.background = new Color(128, 128, 128, 192);
    }

    @Override
    protected void renderComponent(ProgressBar component) {
        Rectangle area = component.getArea();
        int fontSize = this.theme.getFontRenderer().FONT_HEIGHT;
        FontRenderer fontRenderer = this.theme.getFontRenderer();
        this.translateComponent(component, false);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)2884);
        GL11.glDisable((int)3553);
        RenderUtil.setColor(component.getBackgroundColor());
        GL11.glLineWidth((float)0.9f);
        GL11.glBegin((int)2);
        GL11.glVertex2d((double)0.0, (double)0.0);
        GL11.glVertex2d((double)area.width, (double)0.0);
        GL11.glVertex2d((double)area.width, (double)area.height);
        GL11.glVertex2d((double)0.0, (double)area.height);
        GL11.glEnd();
        double barPercentage = (component.getValue() - component.getMinimumValue()) / (component.getMaximumValue() - component.getMinimumValue());
        RenderUtil.setColor(component.getForegroundColor());
        GL11.glBegin((int)7);
        GL11.glVertex2d((double)0.0, (double)0.0);
        GL11.glVertex2d((double)((double)area.width * barPercentage), (double)0.0);
        GL11.glVertex2d((double)((double)area.width * barPercentage), (double)area.height);
        GL11.glVertex2d((double)0.0, (double)area.height);
        GL11.glEnd();
        GL11.glEnable((int)3553);
        String content = null;
        switch (component.getValueDisplay()) {
            case DECIMAL: {
                content = String.format("%,.3f", component.getValue());
                break;
            }
            case INTEGER: {
                content = String.format("%,d", Math.round(component.getValue()));
                break;
            }
            case PERCENTAGE: {
                int percent = (int)Math.round((component.getValue() - component.getMinimumValue()) / (component.getMaximumValue() - component.getMinimumValue()) * 100.0);
                content = String.format("%d%%", percent);
            }
        }
        if (content != null) {
            GL11.glBlendFunc((int)775, (int)769);
            fontRenderer.drawString(content, component.getWidth() / 2 - fontRenderer.getStringWidth(content) / 2, component.getHeight() / 2 - fontSize / 2, RenderUtil.toRGBA(component.getForegroundColor()));
            GL11.glBlendFunc((int)770, (int)771);
        }
        GL11.glEnable((int)2884);
        GL11.glDisable((int)3042);
        this.translateComponent(component, true);
    }

    @Override
    protected Dimension getDefaultComponentSize(ProgressBar component) {
        return new Dimension(100, 4 + this.theme.getFontRenderer().FONT_HEIGHT);
    }

    @Override
    protected Rectangle[] getInteractableComponentRegions(ProgressBar component) {
        return new Rectangle[0];
    }
}


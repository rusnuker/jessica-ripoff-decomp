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
import net.minecraft.client.gui.FontRenderer;
import org.darkstorm.minecraft.gui.component.Container;
import org.darkstorm.minecraft.gui.component.Slider;
import org.darkstorm.minecraft.gui.theme.AbstractComponentUI;
import org.darkstorm.minecraft.gui.theme.simple.SimpleTheme;
import org.darkstorm.minecraft.gui.util.RenderUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class SimpleSliderUI
extends AbstractComponentUI<Slider> {
    private SimpleTheme theme;

    public SimpleSliderUI(SimpleTheme theme) {
        super(Slider.class);
        this.theme = theme;
        this.foreground = Color.cyan;
        this.background = new Color(64, 64, 128, 192);
    }

    @Override
    protected void renderComponent(Slider component) {
        this.translateComponent(component, false);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)2884);
        Rectangle area = component.getArea();
        int fontSize = this.theme.getFontRenderer().FONT_HEIGHT;
        FontRenderer fontRenderer = this.theme.getFontRenderer();
        fontRenderer.drawString(component.getText(), 0, 0, RenderUtil.toRGBA(component.getForegroundColor()));
        String content = null;
        switch (component.getValueDisplay()) {
            case DECIMAL: {
                content = String.format("%,.2f", component.getValue());
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
            String suffix = component.getContentSuffix();
            if (suffix != null && !suffix.trim().isEmpty()) {
                content = content.concat(" ").concat(suffix);
            }
            fontRenderer.drawString(content, component.getWidth() - fontRenderer.getStringWidth(content), 0, RenderUtil.toRGBA(component.getForegroundColor()));
        }
        GL11.glDisable((int)3553);
        RenderUtil.setColor(component.getBackgroundColor());
        GL11.glLineWidth((float)0.9f);
        GL11.glBegin((int)2);
        GL11.glVertex2d((double)0.0, (double)((double)fontSize + 2.0));
        GL11.glVertex2d((double)area.width, (double)((double)fontSize + 2.0));
        GL11.glVertex2d((double)area.width, (double)area.height);
        GL11.glVertex2d((double)0.0, (double)area.height);
        GL11.glEnd();
        double sliderPercentage = (component.getValue() - component.getMinimumValue()) / (component.getMaximumValue() - component.getMinimumValue());
        RenderUtil.setColor(component.getForegroundColor());
        GL11.glBegin((int)7);
        GL11.glVertex2d((double)0.0, (double)((double)fontSize + 2.0));
        GL11.glVertex2d((double)((double)area.width * sliderPercentage), (double)((double)fontSize + 2.0));
        GL11.glVertex2d((double)((double)area.width * sliderPercentage), (double)area.height);
        GL11.glVertex2d((double)0.0, (double)area.height);
        GL11.glEnd();
        GL11.glEnable((int)3553);
        this.translateComponent(component, true);
    }

    @Override
    protected Dimension getDefaultComponentSize(Slider component) {
        return new Dimension(100, 8 + this.theme.getFontRenderer().FONT_HEIGHT);
    }

    @Override
    protected Rectangle[] getInteractableComponentRegions(Slider component) {
        return new Rectangle[]{new Rectangle(0, this.theme.getFontRenderer().FONT_HEIGHT + 2, component.getWidth(), component.getHeight() - this.theme.getFontRenderer().FONT_HEIGHT)};
    }

    @Override
    protected void handleComponentInteraction(Slider component, Point location, int button) {
        if (this.getInteractableComponentRegions(component)[0].contains(location) && button == 0) {
            if (Mouse.isButtonDown((int)button) && !component.isValueChanging()) {
                component.setValueChanging(true);
            } else if (!Mouse.isButtonDown((int)button) && component.isValueChanging()) {
                component.setValueChanging(false);
            }
        }
    }

    @Override
    protected void handleComponentUpdate(Slider component) {
        if (component.isValueChanging()) {
            if (!Mouse.isButtonDown((int)0)) {
                component.setValueChanging(false);
                return;
            }
            Point mouse = RenderUtil.calculateMouseLocation();
            Container parent = component.getParent();
            if (parent != null) {
                mouse.translate(-parent.getX(), -parent.getY());
            }
            double percent = (double)mouse.x / (double)component.getWidth();
            double value = component.getMinimumValue() + percent * (component.getMaximumValue() - component.getMinimumValue());
            component.setValue(value);
        }
    }
}


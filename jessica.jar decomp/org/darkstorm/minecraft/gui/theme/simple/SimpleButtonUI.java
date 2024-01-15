/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Mouse
 *  org.lwjgl.opengl.GL11
 */
package org.darkstorm.minecraft.gui.theme.simple;

import com.mysql.fabric.Wrapper;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import org.darkstorm.minecraft.gui.component.Button;
import org.darkstorm.minecraft.gui.component.Container;
import org.darkstorm.minecraft.gui.theme.AbstractComponentUI;
import org.darkstorm.minecraft.gui.theme.simple.SimpleTheme;
import org.darkstorm.minecraft.gui.util.RenderUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Image;

public class SimpleButtonUI
extends AbstractComponentUI<Button> {
    private final SimpleTheme theme;
    Image im_button;

    SimpleButtonUI(SimpleTheme theme) {
        super(Button.class);
        this.theme = theme;
        this.foreground = Color.WHITE;
        this.background = new Color(40, 35, 36, 35);
    }

    @Override
    protected void renderComponent(Button button) {
        try {
            if (this.im_button == null) {
                this.im_button = new Image(new File(Wrapper.mc().mcDataDir, "MrBeast").getAbsolutePath());
            }
            this.translateComponent(button, false);
            Rectangle area = button.getArea();
            GL11.glEnable((int)3042);
            GL11.glDisable((int)2884);
            GL11.glDisable((int)3553);
            RenderUtil.setColor(button.getBackgroundColor());
            if (button.getBackgroundColor().equals(new Color(2, 110, 231, 60))) {
                this.im_button.draw(0.0f, -18.0f, 66.0f, 45.0f, new org.newdawn.slick.Color(110, 4, 21));
            } else {
                this.im_button.draw(0.0f, -18.0f, 66.0f, 45.0f, new org.newdawn.slick.Color(2, 110, 231));
            }
            Point mouse = RenderUtil.calculateMouseLocation();
            Container parent = button.getParent();
            while (parent != null) {
                mouse.x -= parent.getX();
                mouse.y -= parent.getY();
                parent = parent.getParent();
            }
            if (area.contains(mouse)) {
                this.im_button.draw(0.0f, 0.0f, 66.0f, 15.0f, Mouse.isButtonDown((int)0) ? new org.newdawn.slick.Color(0.0f, 0.0f, 0.0f, 0.5f) : new org.newdawn.slick.Color(0.0f, 0.0f, 0.0f, 0.3f));
            }
            GL11.glEnable((int)3553);
            String text = button.getText();
            this.theme.getFontRenderer().drawString(text, area.width / 2 - this.theme.getFontRenderer().getStringWidth(text) / 2, area.height / 2 - this.theme.getFontRenderer().FONT_HEIGHT / 2, RenderUtil.toRGBA(button.getForegroundColor()));
            GL11.glEnable((int)2884);
            GL11.glDisable((int)3042);
            this.translateComponent(button, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Dimension getDefaultComponentSize(Button component) {
        return new Dimension(this.theme.getFontRenderer().getStringWidth(component.getText()) + 2, this.theme.getFontRenderer().FONT_HEIGHT + 2);
    }

    @Override
    protected Rectangle[] getInteractableComponentRegions(Button component) {
        return new Rectangle[]{new Rectangle(0, 0, component.getWidth(), component.getHeight())};
    }

    @Override
    protected void handleComponentInteraction(Button component, Point location, int button) {
        if (location.x <= component.getWidth() && location.y <= component.getHeight() && button == 0) {
            component.press();
        }
    }
}


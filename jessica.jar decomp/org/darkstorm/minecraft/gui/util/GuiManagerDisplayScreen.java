/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.util;

import java.awt.Rectangle;
import java.io.IOException;
import net.minecraft.client.gui.GuiScreen;
import org.darkstorm.minecraft.gui.GuiManager;
import org.darkstorm.minecraft.gui.component.Component;
import org.darkstorm.minecraft.gui.component.Frame;

public class GuiManagerDisplayScreen
extends GuiScreen {
    private final GuiManager guiManager;

    public GuiManagerDisplayScreen(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        int n;
        int n2;
        Object[] objectArray;
        Frame frame;
        super.mouseClicked(x, y, button);
        Frame[] frameArray = this.guiManager.getFrames();
        int n3 = frameArray.length;
        int n4 = 0;
        while (n4 < n3) {
            frame = frameArray[n4];
            if (frame.isVisible() && !frame.isMinimized() && !frame.getArea().contains(x, y)) {
                objectArray = frame.getChildren();
                n2 = objectArray.length;
                n = 0;
                while (n < n2) {
                    Component component = objectArray[n];
                    Rectangle[] rectangleArray = component.getTheme().getUIForComponent(component).getInteractableRegions(component);
                    int n5 = rectangleArray.length;
                    int n6 = 0;
                    while (n6 < n5) {
                        Rectangle area = rectangleArray[n6];
                        if (area.contains(x - frame.getX() - component.getX(), y - frame.getY() - component.getY())) {
                            frame.onMousePress(x - frame.getX(), y - frame.getY(), button);
                            this.guiManager.bringForward(frame);
                            return;
                        }
                        ++n6;
                    }
                    ++n;
                }
            }
            ++n4;
        }
        frameArray = this.guiManager.getFrames();
        n3 = frameArray.length;
        n4 = 0;
        while (n4 < n3) {
            frame = frameArray[n4];
            if (frame.isVisible()) {
                if (!frame.isMinimized() && frame.getArea().contains(x, y)) {
                    frame.onMousePress(x - frame.getX(), y - frame.getY(), button);
                    this.guiManager.bringForward(frame);
                    break;
                }
                if (frame.isMinimized()) {
                    objectArray = frame.getTheme().getUIForComponent(frame).getInteractableRegions(frame);
                    n2 = objectArray.length;
                    n = 0;
                    while (n < n2) {
                        Object area = objectArray[n];
                        if (((Rectangle)area).contains(x - frame.getX(), y - frame.getY())) {
                            frame.onMousePress(x - frame.getX(), y - frame.getY(), button);
                            this.guiManager.bringForward(frame);
                            return;
                        }
                        ++n;
                    }
                }
            }
            ++n4;
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        int n;
        int n2;
        Object[] objectArray;
        Frame frame;
        super.mouseReleased(x, y, button);
        Frame[] frameArray = this.guiManager.getFrames();
        int n3 = frameArray.length;
        int n4 = 0;
        while (n4 < n3) {
            frame = frameArray[n4];
            if (frame.isVisible() && !frame.isMinimized() && !frame.getArea().contains(x, y)) {
                objectArray = frame.getChildren();
                n2 = objectArray.length;
                n = 0;
                while (n < n2) {
                    Component component = objectArray[n];
                    Rectangle[] rectangleArray = component.getTheme().getUIForComponent(component).getInteractableRegions(component);
                    int n5 = rectangleArray.length;
                    int n6 = 0;
                    while (n6 < n5) {
                        Rectangle area = rectangleArray[n6];
                        if (area.contains(x - frame.getX() - component.getX(), y - frame.getY() - component.getY())) {
                            frame.onMouseRelease(x - frame.getX(), y - frame.getY(), button);
                            this.guiManager.bringForward(frame);
                            return;
                        }
                        ++n6;
                    }
                    ++n;
                }
            }
            ++n4;
        }
        frameArray = this.guiManager.getFrames();
        n3 = frameArray.length;
        n4 = 0;
        while (n4 < n3) {
            frame = frameArray[n4];
            if (frame.isVisible()) {
                if (!frame.isMinimized() && frame.getArea().contains(x, y)) {
                    frame.onMouseRelease(x - frame.getX(), y - frame.getY(), button);
                    this.guiManager.bringForward(frame);
                    break;
                }
                if (frame.isMinimized()) {
                    objectArray = frame.getTheme().getUIForComponent(frame).getInteractableRegions(frame);
                    n2 = objectArray.length;
                    n = 0;
                    while (n < n2) {
                        Object area = objectArray[n];
                        if (((Rectangle)area).contains(x - frame.getX(), y - frame.getY())) {
                            frame.onMouseRelease(x - frame.getX(), y - frame.getY(), button);
                            this.guiManager.bringForward(frame);
                            return;
                        }
                        ++n;
                    }
                }
            }
            ++n4;
        }
    }

    @Override
    public void drawScreen(int par2, int par3, float par4) {
        this.guiManager.render();
        super.drawScreen(par2, par3, par4);
    }
}


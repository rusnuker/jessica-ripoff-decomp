/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import org.darkstorm.minecraft.gui.AbstractGuiManager;
import org.darkstorm.minecraft.gui.component.BoundedRangeComponent;
import org.darkstorm.minecraft.gui.component.Button;
import org.darkstorm.minecraft.gui.component.ComboBox;
import org.darkstorm.minecraft.gui.component.Component;
import org.darkstorm.minecraft.gui.component.Frame;
import org.darkstorm.minecraft.gui.component.basic.BasicButton;
import org.darkstorm.minecraft.gui.component.basic.BasicCheckButton;
import org.darkstorm.minecraft.gui.component.basic.BasicComboBox;
import org.darkstorm.minecraft.gui.component.basic.BasicFrame;
import org.darkstorm.minecraft.gui.component.basic.BasicLabel;
import org.darkstorm.minecraft.gui.component.basic.BasicProgressBar;
import org.darkstorm.minecraft.gui.component.basic.BasicSlider;
import org.darkstorm.minecraft.gui.layout.Constraint;
import org.darkstorm.minecraft.gui.listener.ButtonListener;
import org.darkstorm.minecraft.gui.listener.ComboBoxListener;
import org.darkstorm.minecraft.gui.theme.Theme;
import org.darkstorm.minecraft.gui.theme.simple.SimpleTheme;

public final class ExampleGuiManager
extends AbstractGuiManager {
    private final AtomicBoolean setup = new AtomicBoolean();

    @Override
    public void setup() {
        if (!this.setup.compareAndSet(false, true)) {
            return;
        }
        this.createTestFrame();
        this.resizeComponents();
        Minecraft minecraft = Minecraft.getMinecraft();
        Dimension maxSize = this.recalculateSizes();
        int offsetX = 5;
        int offsetY = 5;
        int scale = minecraft.gameSettings.guiScale;
        if (scale == 0) {
            scale = 1000;
        }
        int scaleFactor = 0;
        while (scaleFactor < scale && minecraft.displayWidth / (scaleFactor + 1) >= 320 && minecraft.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        Frame[] frameArray = this.getFrames();
        int n = frameArray.length;
        int n2 = 0;
        while (n2 < n) {
            Frame frame = frameArray[n2];
            frame.setX(offsetX);
            frame.setY(offsetY);
            if ((offsetX += maxSize.width + 5) + maxSize.width + 5 > minecraft.displayWidth / scaleFactor) {
                offsetX = 5;
                offsetY += maxSize.height + 5;
            }
            ++n2;
        }
    }

    private void createTestFrame() {
        Theme theme = this.getTheme();
        BasicFrame testFrame = new BasicFrame("Frame");
        testFrame.setTheme(theme);
        testFrame.add(new BasicLabel("TEST LOL"), new Constraint[0]);
        testFrame.add(new BasicLabel("TEST 23423"), new Constraint[0]);
        testFrame.add(new BasicLabel("TE123123123ST LOL"), new Constraint[0]);
        testFrame.add(new BasicLabel("31243 LO3242L432"), new Constraint[0]);
        BasicButton testButton = new BasicButton("Duplicate this frame!");
        testButton.addButtonListener(new ButtonListener(){

            @Override
            public void onButtonPress(Button button) {
                ExampleGuiManager.this.createTestFrame();
            }
        });
        testFrame.add(new BasicCheckButton("This is a checkbox"), new Constraint[0]);
        testFrame.add(testButton, new Constraint[0]);
        BasicComboBox comboBox = new BasicComboBox("Simple theme", "Other theme", "Other theme 2");
        comboBox.addComboBoxListener(new ComboBoxListener(){

            @Override
            public void onComboBoxSelectionChanged(ComboBox comboBox) {
                SimpleTheme theme;
                switch (comboBox.getSelectedIndex()) {
                    case 0: {
                        theme = new SimpleTheme();
                        break;
                    }
                    default: {
                        return;
                    }
                }
                ExampleGuiManager.this.setTheme(theme);
            }
        });
        testFrame.add(comboBox, new Constraint[0]);
        BasicSlider slider = new BasicSlider("Test");
        slider.setContentSuffix("things");
        slider.setValueDisplay(BoundedRangeComponent.ValueDisplay.INTEGER);
        testFrame.add(slider, new Constraint[0]);
        testFrame.add(new BasicProgressBar(50.0, 0.0, 100.0, 1.0, BoundedRangeComponent.ValueDisplay.PERCENTAGE), new Constraint[0]);
        testFrame.setX(50);
        testFrame.setY(50);
        Dimension defaultDimension = theme.getUIForComponent(testFrame).getDefaultSize(testFrame);
        testFrame.setWidth(defaultDimension.width);
        testFrame.setHeight(defaultDimension.height);
        testFrame.layoutChildren();
        testFrame.setVisible(true);
        testFrame.setMinimized(true);
        this.addFrame(testFrame);
    }

    @Override
    protected void resizeComponents() {
        Theme theme = this.getTheme();
        Frame[] frames = this.getFrames();
        BasicButton enable = new BasicButton("Enable");
        BasicButton disable = new BasicButton("Disable");
        Dimension enableSize = theme.getUIForComponent(enable).getDefaultSize(enable);
        Dimension disableSize = theme.getUIForComponent(disable).getDefaultSize(disable);
        int buttonWidth = Math.max(enableSize.width, disableSize.width);
        int buttonHeight = Math.max(enableSize.height, disableSize.height);
        Frame[] frameArray = frames;
        int n = frames.length;
        int n2 = 0;
        while (n2 < n) {
            Frame frame = frameArray[n2];
            if (frame instanceof ModuleFrame) {
                Component[] componentArray = frame.getChildren();
                int n3 = componentArray.length;
                int n4 = 0;
                while (n4 < n3) {
                    Component component = componentArray[n4];
                    if (component instanceof Button) {
                        component.setWidth(buttonWidth);
                        component.setHeight(buttonHeight);
                    }
                    ++n4;
                }
            }
            ++n2;
        }
        this.recalculateSizes();
    }

    private Dimension recalculateSizes() {
        Frame frame;
        Frame[] frames = this.getFrames();
        int maxWidth = 0;
        int maxHeight = 0;
        Frame[] frameArray = frames;
        int n = frames.length;
        int n2 = 0;
        while (n2 < n) {
            frame = frameArray[n2];
            Dimension defaultDimension = frame.getTheme().getUIForComponent(frame).getDefaultSize(frame);
            maxWidth = Math.max(maxWidth, defaultDimension.width);
            frame.setHeight(defaultDimension.height);
            if (frame.isMinimized()) {
                Rectangle[] rectangleArray = frame.getTheme().getUIForComponent(frame).getInteractableRegions(frame);
                int n3 = rectangleArray.length;
                int n4 = 0;
                while (n4 < n3) {
                    Rectangle area = rectangleArray[n4];
                    maxHeight = Math.max(maxHeight, area.height);
                    ++n4;
                }
            } else {
                maxHeight = Math.max(maxHeight, defaultDimension.height);
            }
            ++n2;
        }
        frameArray = frames;
        n = frames.length;
        n2 = 0;
        while (n2 < n) {
            frame = frameArray[n2];
            frame.setWidth(maxWidth);
            frame.layoutChildren();
            ++n2;
        }
        return new Dimension(maxWidth, maxHeight);
    }

    private class ModuleFrame
    extends BasicFrame {
        private ModuleFrame() {
        }

        private ModuleFrame(String title) {
            super(title);
        }
    }
}


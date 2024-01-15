/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.fabric.Category;
import com.mysql.fabric.Module;
import com.mysql.fabric.Wrapper;
import com.mysql.jdbc.jdbc2.optional.GuiManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.command.KillauraSettings;
import net.minecraft.init.NoSlowdown_One_Million_Dollars;
import net.minecraft.init.Reach_One_Million_Dollars;
import org.darkstorm.minecraft.gui.AbstractGuiManager;
import org.darkstorm.minecraft.gui.component.BoundedRangeComponent;
import org.darkstorm.minecraft.gui.component.Button;
import org.darkstorm.minecraft.gui.component.Component;
import org.darkstorm.minecraft.gui.component.Frame;
import org.darkstorm.minecraft.gui.component.Slider;
import org.darkstorm.minecraft.gui.component.basic.BasicButton;
import org.darkstorm.minecraft.gui.component.basic.BasicCheckButton;
import org.darkstorm.minecraft.gui.component.basic.BasicSlider;
import org.darkstorm.minecraft.gui.layout.GridLayoutManager;
import org.darkstorm.minecraft.gui.listener.ButtonListener;
import org.darkstorm.minecraft.gui.listener.SliderListener;
import org.darkstorm.minecraft.gui.theme.Theme;
import org.darkstorm.minecraft.gui.theme.simple.SimpleTheme;

public final class GuiManager_Nirvana
extends AbstractGuiManager {
    private final AtomicBoolean setup = new AtomicBoolean();

    @Override
    public void setup() {
        if (!this.setup.compareAndSet(false, true)) {
            return;
        }
        HashMap<Category, GuiManager.ModuleFrame_Nirvana> categoryFrames = new HashMap<Category, GuiManager.ModuleFrame_Nirvana>();
        for (Module module : Wrapper.getModules().values()) {
            GuiManager.ModuleFrame_Nirvana frame = (GuiManager.ModuleFrame_Nirvana)categoryFrames.get((Object)module.getCategory());
            if (frame == null) {
                String name = module.getCategory().name().toLowerCase();
                name = String.valueOf(Character.toUpperCase(name.charAt(0))) + name.substring(1);
                frame = new GuiManager.ModuleFrame_Nirvana(this, name, null);
                frame.setTheme(new SimpleTheme());
                frame.setLayoutManager(new GridLayoutManager(1, 0));
                frame.setVisible(true);
                frame.setClosable(false);
                frame.setMinimized(true);
                frame.setWidth(100000000);
                frame.setHeight(100000000);
                this.addFrame(frame);
                categoryFrames.put(module.getCategory(), frame);
            }
            Module updateModule = module;
            BasicButton button = new BasicButton(this, module.getName(), updateModule){
                private boolean color;
                private Color c;
                final /* synthetic */ GuiManager_Nirvana this$0;
                private final /* synthetic */ Module val$updateModule;
                {
                    this.this$0 = guiManager_Nirvana;
                    this.val$updateModule = module;
                    super($anonymous0);
                    this.color = true;
                    this.c = null;
                }

                public void update() {
                    if (this.color) {
                        this.c = this.getBackgroundColor();
                        this.color = false;
                    }
                    if (this.val$updateModule.isToggled()) {
                        this.setBackgroundColor(new Color(2, 110, 231, 60));
                    } else {
                        this.setBackgroundColor(this.c);
                    }
                    this.setText(this.val$updateModule.getName());
                }
            };
            button.addButtonListener(new ButtonListener(this, updateModule){
                final /* synthetic */ GuiManager_Nirvana this$0;
                private final /* synthetic */ Module val$updateModule;
                {
                    this.this$0 = guiManager_Nirvana;
                    this.val$updateModule = module;
                }

                public void onButtonPress(Button button) {
                    this.val$updateModule.toggle();
                    button.setText(this.val$updateModule.isToggled() ? String.valueOf(this.val$updateModule.getName()) + " ON" : String.valueOf(this.val$updateModule.getName()) + " OFF");
                }
            });
            frame.add(button, GridLayoutManager.HorizontalGridConstraint.FILL);
        }
        this.killauraSettings();
        this.values();
        Minecraft minecraft = Minecraft.getMinecraft();
        Dimension maxSize = this.recalculateSizes();
        int offsetX = 5;
        int offsetY = 5;
        int scale = minecraft.gameSettings.guiScale;
        if (scale == 0) {
            scale = 700;
        }
        int scaleFactor = 0;
        while (scaleFactor < scale && minecraft.displayWidth / (scaleFactor + 1) >= 320 && minecraft.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        Frame[] arrayOfFrame = this.getFrames();
        int j = arrayOfFrame.length;
        int i = 0;
        while (i < j) {
            Frame frame = arrayOfFrame[i];
            frame.setX(offsetX);
            frame.setY(offsetY);
            if ((offsetX += maxSize.width + 5) + maxSize.width + 5 > minecraft.displayWidth / scaleFactor) {
                offsetX = 5;
                offsetY += maxSize.height + 5;
            }
            ++i;
        }
    }

    public void killauraSettings() {
        GuiManager.ModuleFrame_Nirvana frameKS = new GuiManager.ModuleFrame_Nirvana(this, "Killaura Settings", null);
        frameKS.setTheme(new SimpleTheme());
        frameKS.setLayoutManager(new GridLayoutManager(1, 0));
        frameKS.setVisible(true);
        frameKS.setClosable(false);
        frameKS.setMinimized(true);
        frameKS.setWidth(100000000);
        frameKS.setHeight(100000000);
        this.addFrame(frameKS);
        BasicCheckButton checkButtonKSPlayers = new BasicCheckButton(this, "Players"){
            final /* synthetic */ GuiManager_Nirvana this$0;
            {
                this.this$0 = guiManager_Nirvana;
                super($anonymous0);
            }

            public void update() {
            }
        };
        if (KillauraSettings.players) {
            checkButtonKSPlayers.setSelected(true);
        } else {
            checkButtonKSPlayers.setSelected(false);
        }
        checkButtonKSPlayers.addButtonListener(new ButtonListener(this){
            final /* synthetic */ GuiManager_Nirvana this$0;
            {
                this.this$0 = guiManager_Nirvana;
            }

            public void onButtonPress(Button button) {
                KillauraSettings.players = !KillauraSettings.players;
                Wrapper.getFiles().saveValues();
            }
        });
        BasicCheckButton checkButtonKSMobs = new BasicCheckButton(this, "Mobs"){
            final /* synthetic */ GuiManager_Nirvana this$0;
            {
                this.this$0 = guiManager_Nirvana;
                super($anonymous0);
            }

            public void update() {
            }
        };
        if (KillauraSettings.mobs) {
            checkButtonKSMobs.setSelected(true);
        } else {
            checkButtonKSMobs.setSelected(false);
        }
        checkButtonKSMobs.addButtonListener(new ButtonListener(this){
            final /* synthetic */ GuiManager_Nirvana this$0;
            {
                this.this$0 = guiManager_Nirvana;
            }

            public void onButtonPress(Button button) {
                KillauraSettings.mobs = !KillauraSettings.mobs;
                Wrapper.getFiles().saveValues();
            }
        });
        BasicCheckButton checkButtonKSTeam = new BasicCheckButton(this, "Team"){
            final /* synthetic */ GuiManager_Nirvana this$0;
            {
                this.this$0 = guiManager_Nirvana;
                super($anonymous0);
            }

            public void update() {
            }
        };
        if (KillauraSettings.team) {
            checkButtonKSTeam.setSelected(true);
        } else {
            checkButtonKSTeam.setSelected(false);
        }
        checkButtonKSTeam.addButtonListener(new ButtonListener(this){
            final /* synthetic */ GuiManager_Nirvana this$0;
            {
                this.this$0 = guiManager_Nirvana;
            }

            public void onButtonPress(Button button) {
                KillauraSettings.team = !KillauraSettings.team;
                Wrapper.getFiles().saveValues();
            }
        });
        BasicSlider sliderKSRange = new BasicSlider("Range", KillauraSettings.range, 1.0, 10.0, 0.1, BoundedRangeComponent.ValueDisplay.DECIMAL);
        sliderKSRange.setValueDisplay(BoundedRangeComponent.ValueDisplay.DECIMAL);
        sliderKSRange.addSliderListener(new SliderListener(this, sliderKSRange){
            final /* synthetic */ GuiManager_Nirvana this$0;
            private final /* synthetic */ BasicSlider val$sliderKSRange;
            {
                this.this$0 = guiManager_Nirvana;
                this.val$sliderKSRange = basicSlider;
            }

            public void onSliderValueChanged(Slider slider) {
                KillauraSettings.range = this.val$sliderKSRange.getValue();
                Wrapper.getFiles().saveValues();
            }
        });
        BasicSlider sliderKSSpeed = new BasicSlider("Killaura Speed", KillauraSettings.KillauraSpeed, 1.0, 50.0, 0.1, BoundedRangeComponent.ValueDisplay.DECIMAL);
        sliderKSSpeed.setValueDisplay(BoundedRangeComponent.ValueDisplay.DECIMAL);
        sliderKSSpeed.addSliderListener(new SliderListener(this, sliderKSSpeed){
            final /* synthetic */ GuiManager_Nirvana this$0;
            private final /* synthetic */ BasicSlider val$sliderKSSpeed;
            {
                this.this$0 = guiManager_Nirvana;
                this.val$sliderKSSpeed = basicSlider;
            }

            public void onSliderValueChanged(Slider slider) {
                KillauraSettings.KillauraSpeed = (float)this.val$sliderKSSpeed.getValue();
                Wrapper.getFiles().saveValues();
            }
        });
        BasicSlider sliderKSFOV = new BasicSlider("FOV", KillauraSettings.FOV, 1.0, 180.0, 1.0, BoundedRangeComponent.ValueDisplay.INTEGER);
        sliderKSFOV.setValueDisplay(BoundedRangeComponent.ValueDisplay.DECIMAL);
        sliderKSFOV.addSliderListener(new SliderListener(this, sliderKSFOV){
            final /* synthetic */ GuiManager_Nirvana this$0;
            private final /* synthetic */ BasicSlider val$sliderKSFOV;
            {
                this.this$0 = guiManager_Nirvana;
                this.val$sliderKSFOV = basicSlider;
            }

            public void onSliderValueChanged(Slider slider) {
                KillauraSettings.FOV = this.val$sliderKSFOV.getValue();
                Wrapper.getFiles().saveValues();
            }
        });
        BasicSlider sliderKSYaw = new BasicSlider("Yaw", KillauraSettings.yaw, 15.0, 180.0, 1.0, BoundedRangeComponent.ValueDisplay.INTEGER);
        sliderKSYaw.setValueDisplay(BoundedRangeComponent.ValueDisplay.DECIMAL);
        sliderKSYaw.addSliderListener(new SliderListener(this, sliderKSYaw){
            final /* synthetic */ GuiManager_Nirvana this$0;
            private final /* synthetic */ BasicSlider val$sliderKSYaw;
            {
                this.this$0 = guiManager_Nirvana;
                this.val$sliderKSYaw = basicSlider;
            }

            public void onSliderValueChanged(Slider slider) {
                KillauraSettings.yaw = (float)this.val$sliderKSYaw.getValue();
                Wrapper.getFiles().saveValues();
            }
        });
        BasicSlider sliderKSPitch = new BasicSlider("Pitch", KillauraSettings.pitch, 15.0, 180.0, 1.0, BoundedRangeComponent.ValueDisplay.INTEGER);
        sliderKSPitch.setValueDisplay(BoundedRangeComponent.ValueDisplay.DECIMAL);
        sliderKSPitch.addSliderListener(new SliderListener(this, sliderKSPitch){
            final /* synthetic */ GuiManager_Nirvana this$0;
            private final /* synthetic */ BasicSlider val$sliderKSPitch;
            {
                this.this$0 = guiManager_Nirvana;
                this.val$sliderKSPitch = basicSlider;
            }

            public void onSliderValueChanged(Slider slider) {
                KillauraSettings.pitch = (float)this.val$sliderKSPitch.getValue();
                Wrapper.getFiles().saveValues();
            }
        });
        frameKS.add(checkButtonKSPlayers, GridLayoutManager.HorizontalGridConstraint.FILL);
        frameKS.add(checkButtonKSMobs, GridLayoutManager.HorizontalGridConstraint.FILL);
        frameKS.add(checkButtonKSTeam, GridLayoutManager.HorizontalGridConstraint.FILL);
        frameKS.add(sliderKSRange, GridLayoutManager.HorizontalGridConstraint.FILL);
        frameKS.add(sliderKSSpeed, GridLayoutManager.HorizontalGridConstraint.FILL);
        frameKS.add(sliderKSFOV, GridLayoutManager.HorizontalGridConstraint.FILL);
        frameKS.add(sliderKSYaw, GridLayoutManager.HorizontalGridConstraint.FILL);
        frameKS.add(sliderKSPitch, GridLayoutManager.HorizontalGridConstraint.FILL);
    }

    public void values() {
        GuiManager.ModuleFrame_Nirvana frameKS = new GuiManager.ModuleFrame_Nirvana(this, "Values", null);
        frameKS.setTheme(new SimpleTheme());
        frameKS.setLayoutManager(new GridLayoutManager(1, 0));
        frameKS.setVisible(true);
        frameKS.setClosable(false);
        frameKS.setMinimized(true);
        frameKS.setWidth(100000000);
        frameKS.setHeight(100000000);
        this.addFrame(frameKS);
        BasicSlider sliderReach = new BasicSlider("Reach", Reach_One_Million_Dollars.reach, 1.0, 9.0, 0.1, BoundedRangeComponent.ValueDisplay.DECIMAL);
        sliderReach.setValueDisplay(BoundedRangeComponent.ValueDisplay.DECIMAL);
        sliderReach.addSliderListener(new SliderListener(this, sliderReach){
            final /* synthetic */ GuiManager_Nirvana this$0;
            private final /* synthetic */ BasicSlider val$sliderReach;
            {
                this.this$0 = guiManager_Nirvana;
                this.val$sliderReach = basicSlider;
            }

            public void onSliderValueChanged(Slider slider) {
                Reach_One_Million_Dollars.reach = (float)this.val$sliderReach.getValue();
                Wrapper.getFiles().saveValues();
            }
        });
        BasicSlider sliderNoSlowdown = new BasicSlider("NoSlowdown", NoSlowdown_One_Million_Dollars.value, 0.0, 100.0, 1.0, BoundedRangeComponent.ValueDisplay.INTEGER);
        sliderNoSlowdown.setValueDisplay(BoundedRangeComponent.ValueDisplay.DECIMAL);
        sliderNoSlowdown.addSliderListener(new SliderListener(this, sliderNoSlowdown){
            final /* synthetic */ GuiManager_Nirvana this$0;
            private final /* synthetic */ BasicSlider val$sliderNoSlowdown;
            {
                this.this$0 = guiManager_Nirvana;
                this.val$sliderNoSlowdown = basicSlider;
            }

            public void onSliderValueChanged(Slider slider) {
                NoSlowdown_One_Million_Dollars.value = (float)this.val$sliderNoSlowdown.getValue();
                Wrapper.getFiles().saveValues();
            }
        });
        frameKS.add(sliderReach, GridLayoutManager.HorizontalGridConstraint.FILL);
        frameKS.add(sliderNoSlowdown, GridLayoutManager.HorizontalGridConstraint.FILL);
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
            if (frame instanceof GuiManager.ModuleFrame_Nirvana) {
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
}


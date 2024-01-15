/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component;

import org.darkstorm.minecraft.gui.component.BoundedRangeComponent;
import org.darkstorm.minecraft.gui.component.Component;
import org.darkstorm.minecraft.gui.component.TextComponent;
import org.darkstorm.minecraft.gui.listener.SliderListener;

public interface Slider
extends Component,
TextComponent,
BoundedRangeComponent {
    public String getContentSuffix();

    public boolean isValueChanging();

    public void setContentSuffix(String var1);

    public void setValueChanging(boolean var1);

    public void addSliderListener(SliderListener var1);

    public void removeSliderListener(SliderListener var1);
}


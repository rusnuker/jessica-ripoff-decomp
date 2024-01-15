/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component.basic;

import org.darkstorm.minecraft.gui.component.AbstractComponent;
import org.darkstorm.minecraft.gui.component.BoundedRangeComponent;
import org.darkstorm.minecraft.gui.component.Slider;
import org.darkstorm.minecraft.gui.listener.ComponentListener;
import org.darkstorm.minecraft.gui.listener.SliderListener;

public class BasicSlider
extends AbstractComponent
implements Slider {
    private String text;
    private String suffix;
    private double value;
    private double minimum;
    private double maximum;
    private double increment;
    private BoundedRangeComponent.ValueDisplay display;
    private boolean changing = false;
    private double startValue;

    public BasicSlider() {
        this("");
    }

    public BasicSlider(String text) {
        this(text, 0.0);
    }

    public BasicSlider(String text, double value) {
        this(text, value, 0.0, 100.0);
    }

    public BasicSlider(String text, double value, double minimum, double maximum) {
        this(text, value, minimum, maximum, 1);
    }

    public BasicSlider(String text, double value, double minimum, double maximum, int increment) {
        this(text, value, minimum, maximum, increment, BoundedRangeComponent.ValueDisplay.DECIMAL);
    }

    public BasicSlider(String text, double value, double minimum, double maximum, double increment, BoundedRangeComponent.ValueDisplay display) {
        this.text = text != null ? text : "";
        this.minimum = Math.max(0.0, Math.min(minimum, maximum));
        this.maximum = Math.max(0.0, Math.max(minimum, maximum));
        value = Math.max(minimum, Math.min(maximum, value));
        this.value = value - (double)Math.round(value % increment / increment) * increment;
        this.increment = Math.min(maximum, Math.max(5.0E-4, increment));
        this.display = display != null ? display : BoundedRangeComponent.ValueDisplay.DECIMAL;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public void setText(String text) {
        this.text = text != null ? text : "";
    }

    @Override
    public double getValue() {
        return this.value;
    }

    @Override
    public double getMinimumValue() {
        return this.minimum;
    }

    @Override
    public double getMaximumValue() {
        return this.maximum;
    }

    @Override
    public double getIncrement() {
        return this.increment;
    }

    @Override
    public BoundedRangeComponent.ValueDisplay getValueDisplay() {
        return this.display;
    }

    @Override
    public boolean isValueChanging() {
        return this.changing;
    }

    @Override
    public String getContentSuffix() {
        return this.suffix;
    }

    @Override
    public void setValue(double value) {
        double oldValue = this.value;
        value = Math.max(this.minimum, Math.min(this.maximum, value));
        this.value = value - (double)Math.round(value % this.increment / this.increment) * this.increment;
        if (!this.changing && oldValue != this.value) {
            this.fireChange();
        }
    }

    @Override
    public void setMinimumValue(double minimum) {
        this.minimum = Math.max(0.0, Math.min(this.maximum, minimum));
        this.setValue(this.value);
    }

    @Override
    public void setMaximumValue(double maximum) {
        this.maximum = Math.max(maximum, this.minimum);
        this.setValue(this.value);
    }

    @Override
    public void setIncrement(double increment) {
        this.increment = Math.min(this.maximum, Math.max(5.0E-4, increment));
        this.setValue(this.value);
    }

    @Override
    public void setValueDisplay(BoundedRangeComponent.ValueDisplay display) {
        this.display = display != null ? display : BoundedRangeComponent.ValueDisplay.DECIMAL;
    }

    @Override
    public void setValueChanging(boolean changing) {
        if (changing != this.changing) {
            this.changing = changing;
            if (changing) {
                this.startValue = this.value;
            } else if (this.startValue != this.value) {
                this.fireChange();
            }
        }
    }

    @Override
    public void setContentSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public void addSliderListener(SliderListener listener) {
        this.addListener(listener);
    }

    @Override
    public void removeSliderListener(SliderListener listener) {
        this.removeListener(listener);
    }

    private void fireChange() {
        ComponentListener[] componentListenerArray = this.getListeners();
        int n = componentListenerArray.length;
        int n2 = 0;
        while (n2 < n) {
            ComponentListener listener = componentListenerArray[n2];
            if (listener instanceof SliderListener) {
                ((SliderListener)listener).onSliderValueChanged(this);
            }
            ++n2;
        }
    }
}


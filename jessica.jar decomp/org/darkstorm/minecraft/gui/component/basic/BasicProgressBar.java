/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component.basic;

import org.darkstorm.minecraft.gui.component.AbstractComponent;
import org.darkstorm.minecraft.gui.component.BoundedRangeComponent;
import org.darkstorm.minecraft.gui.component.ProgressBar;

public class BasicProgressBar
extends AbstractComponent
implements ProgressBar {
    private double value;
    private double minimum;
    private double maximum;
    private double increment;
    private BoundedRangeComponent.ValueDisplay display;
    private boolean indeterminate;

    public BasicProgressBar() {
        this(0.0);
    }

    public BasicProgressBar(double value) {
        this(value, 0.0, 100.0);
    }

    public BasicProgressBar(double value, double minimum, double maximum) {
        this(value, minimum, maximum, 1);
    }

    public BasicProgressBar(double value, double minimum, double maximum, int increment) {
        this(value, minimum, maximum, increment, BoundedRangeComponent.ValueDisplay.NONE);
    }

    public BasicProgressBar(double value, double minimum, double maximum, double increment, BoundedRangeComponent.ValueDisplay display) {
        this.minimum = Math.max(0.0, Math.min(minimum, maximum));
        this.maximum = Math.max(0.0, Math.max(minimum, maximum));
        value = Math.max(minimum, Math.min(maximum, value));
        this.value = value - (double)Math.round(value % increment / increment) * increment;
        this.increment = Math.min(maximum, Math.max(5.0E-4, increment));
        this.display = display != null ? display : BoundedRangeComponent.ValueDisplay.NONE;
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
    public boolean isIndeterminate() {
        return this.indeterminate;
    }

    @Override
    public void setValue(double value) {
        value = Math.max(this.minimum, Math.min(this.maximum, value));
        this.value = value - (double)Math.round(value % this.increment / this.increment) * this.increment;
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
        this.display = display != null ? display : BoundedRangeComponent.ValueDisplay.NONE;
    }

    @Override
    public void setIndeterminate(boolean indeterminate) {
        this.indeterminate = indeterminate;
    }
}


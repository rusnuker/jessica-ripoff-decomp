/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component;

import org.darkstorm.minecraft.gui.component.Component;

public interface BoundedRangeComponent
extends Component {
    public double getValue();

    public double getMinimumValue();

    public double getMaximumValue();

    public double getIncrement();

    public ValueDisplay getValueDisplay();

    public void setValue(double var1);

    public void setMinimumValue(double var1);

    public void setMaximumValue(double var1);

    public void setIncrement(double var1);

    public void setValueDisplay(ValueDisplay var1);

    public static enum ValueDisplay {
        DECIMAL,
        INTEGER,
        PERCENTAGE,
        NONE;

    }
}


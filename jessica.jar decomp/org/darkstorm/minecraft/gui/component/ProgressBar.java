/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component;

import org.darkstorm.minecraft.gui.component.BoundedRangeComponent;
import org.darkstorm.minecraft.gui.component.Component;

public interface ProgressBar
extends Component,
BoundedRangeComponent {
    public boolean isIndeterminate();

    public void setIndeterminate(boolean var1);
}


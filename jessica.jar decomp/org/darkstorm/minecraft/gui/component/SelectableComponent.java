/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component;

import org.darkstorm.minecraft.gui.component.Component;
import org.darkstorm.minecraft.gui.listener.SelectableComponentListener;

public interface SelectableComponent
extends Component {
    public boolean isSelected();

    public void setSelected(boolean var1);

    public void addSelectableComponentListener(SelectableComponentListener var1);

    public void removeSelectableComponentListener(SelectableComponentListener var1);
}


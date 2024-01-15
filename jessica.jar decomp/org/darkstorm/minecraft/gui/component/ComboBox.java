/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component;

import org.darkstorm.minecraft.gui.component.Component;
import org.darkstorm.minecraft.gui.component.SelectableComponent;
import org.darkstorm.minecraft.gui.listener.ComboBoxListener;

public interface ComboBox
extends Component,
SelectableComponent {
    public String[] getElements();

    public void setElements(String ... var1);

    public int getSelectedIndex();

    public void setSelectedIndex(int var1);

    public String getSelectedElement();

    public void addComboBoxListener(ComboBoxListener var1);

    public void removeComboBoxListener(ComboBoxListener var1);
}


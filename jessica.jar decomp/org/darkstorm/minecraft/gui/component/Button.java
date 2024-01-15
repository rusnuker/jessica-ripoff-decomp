/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component;

import org.darkstorm.minecraft.gui.component.ButtonGroup;
import org.darkstorm.minecraft.gui.component.Component;
import org.darkstorm.minecraft.gui.component.TextComponent;
import org.darkstorm.minecraft.gui.listener.ButtonListener;

public interface Button
extends Component,
TextComponent {
    public void press();

    public void addButtonListener(ButtonListener var1);

    public void removeButtonListener(ButtonListener var1);

    public ButtonGroup getGroup();

    public void setGroup(ButtonGroup var1);
}


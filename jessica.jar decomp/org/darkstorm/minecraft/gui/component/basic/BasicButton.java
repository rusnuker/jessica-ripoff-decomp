/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component.basic;

import org.darkstorm.minecraft.gui.component.AbstractComponent;
import org.darkstorm.minecraft.gui.component.Button;
import org.darkstorm.minecraft.gui.component.ButtonGroup;
import org.darkstorm.minecraft.gui.listener.ButtonListener;
import org.darkstorm.minecraft.gui.listener.ComponentListener;

public class BasicButton
extends AbstractComponent
implements Button {
    protected String text = "";
    protected ButtonGroup group;

    public BasicButton() {
    }

    public BasicButton(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void press() {
        ComponentListener[] componentListenerArray = this.getListeners();
        int n = componentListenerArray.length;
        int n2 = 0;
        while (n2 < n) {
            ComponentListener listener = componentListenerArray[n2];
            ((ButtonListener)listener).onButtonPress(this);
            ++n2;
        }
    }

    @Override
    public void addButtonListener(ButtonListener listener) {
        this.addListener(listener);
    }

    @Override
    public void removeButtonListener(ButtonListener listener) {
        this.removeListener(listener);
    }

    @Override
    public ButtonGroup getGroup() {
        return this.group;
    }

    @Override
    public void setGroup(ButtonGroup group) {
        this.group = group;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component.basic;

import org.darkstorm.minecraft.gui.component.Button;
import org.darkstorm.minecraft.gui.component.RadioButton;
import org.darkstorm.minecraft.gui.component.basic.BasicButton;
import org.darkstorm.minecraft.gui.listener.ComponentListener;
import org.darkstorm.minecraft.gui.listener.SelectableComponentListener;

public class BasicRadioButton
extends BasicButton
implements RadioButton {
    private boolean selected = false;

    public BasicRadioButton() {
    }

    public BasicRadioButton(String text) {
        this.text = text;
    }

    @Override
    public void press() {
        this.selected = true;
        Button[] buttonArray = this.getGroup().getButtons();
        int n = buttonArray.length;
        int n2 = 0;
        while (n2 < n) {
            Button button = buttonArray[n2];
            if (!this.equals(button) && button instanceof RadioButton) {
                ((RadioButton)button).setSelected(false);
            }
            ++n2;
        }
        super.press();
    }

    @Override
    public boolean isSelected() {
        return this.selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
        ComponentListener[] componentListenerArray = this.getListeners();
        int n = componentListenerArray.length;
        int n2 = 0;
        while (n2 < n) {
            ComponentListener listener = componentListenerArray[n2];
            if (listener instanceof SelectableComponentListener) {
                try {
                    ((SelectableComponentListener)listener).onSelectedStateChanged(this);
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            ++n2;
        }
    }

    @Override
    public void addSelectableComponentListener(SelectableComponentListener listener) {
        this.addListener(listener);
    }

    @Override
    public void removeSelectableComponentListener(SelectableComponentListener listener) {
        this.removeListener(listener);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component.basic;

import org.darkstorm.minecraft.gui.component.CheckButton;
import org.darkstorm.minecraft.gui.component.basic.BasicButton;
import org.darkstorm.minecraft.gui.listener.ComponentListener;
import org.darkstorm.minecraft.gui.listener.SelectableComponentListener;

public class BasicCheckButton
extends BasicButton
implements CheckButton {
    private boolean selected = false;

    public BasicCheckButton() {
    }

    public BasicCheckButton(String text) {
        this.text = text;
    }

    @Override
    public void press() {
        this.selected = !this.selected;
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


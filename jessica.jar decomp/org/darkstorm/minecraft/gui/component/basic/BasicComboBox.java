/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component.basic;

import org.darkstorm.minecraft.gui.component.AbstractComponent;
import org.darkstorm.minecraft.gui.component.ComboBox;
import org.darkstorm.minecraft.gui.listener.ComboBoxListener;
import org.darkstorm.minecraft.gui.listener.ComponentListener;
import org.darkstorm.minecraft.gui.listener.SelectableComponentListener;

public class BasicComboBox
extends AbstractComponent
implements ComboBox {
    private String[] elements;
    private int selectedIndex;
    private boolean selected;

    public BasicComboBox() {
        this.elements = new String[0];
    }

    public BasicComboBox(String ... elements) {
        this.elements = elements;
    }

    @Override
    public String[] getElements() {
        return this.elements;
    }

    @Override
    public void setElements(String ... elements) {
        this.selectedIndex = 0;
        this.elements = elements;
    }

    @Override
    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    @Override
    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        ComponentListener[] componentListenerArray = this.getListeners();
        int n = componentListenerArray.length;
        int n2 = 0;
        while (n2 < n) {
            ComponentListener listener = componentListenerArray[n2];
            if (listener instanceof ComboBoxListener) {
                try {
                    ((ComboBoxListener)listener).onComboBoxSelectionChanged(this);
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            ++n2;
        }
    }

    @Override
    public String getSelectedElement() {
        return this.elements[this.selectedIndex];
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
    public void addComboBoxListener(ComboBoxListener listener) {
        this.addListener(listener);
    }

    @Override
    public void removeComboBoxListener(ComboBoxListener listener) {
        this.removeListener(listener);
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


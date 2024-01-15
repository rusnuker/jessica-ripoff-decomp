/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component.basic;

import java.util.ArrayList;
import java.util.List;
import org.darkstorm.minecraft.gui.component.Button;
import org.darkstorm.minecraft.gui.component.ButtonGroup;

public class BasicButtonGroup
implements ButtonGroup {
    private List<Button> buttons = new ArrayList<Button>();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addButton(Button button) {
        if (button == null) {
            throw new NullPointerException();
        }
        List<Button> list = this.buttons;
        synchronized (list) {
            this.buttons.add(button);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeButton(Button button) {
        if (button == null) {
            throw new NullPointerException();
        }
        List<Button> list = this.buttons;
        synchronized (list) {
            this.buttons.remove(button);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Button[] getButtons() {
        List<Button> list = this.buttons;
        synchronized (list) {
            return this.buttons.toArray(new Button[this.buttons.size()]);
        }
    }
}


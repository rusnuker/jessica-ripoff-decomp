/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.listener;

import org.darkstorm.minecraft.gui.component.SelectableComponent;
import org.darkstorm.minecraft.gui.listener.ComponentListener;

public interface SelectableComponentListener
extends ComponentListener {
    public void onSelectedStateChanged(SelectableComponent var1);
}


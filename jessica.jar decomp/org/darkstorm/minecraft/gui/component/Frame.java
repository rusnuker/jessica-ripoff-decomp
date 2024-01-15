/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component;

import org.darkstorm.minecraft.gui.component.Container;
import org.darkstorm.minecraft.gui.component.DraggableComponent;

public interface Frame
extends Container,
DraggableComponent {
    public String getTitle();

    public void setTitle(String var1);

    public boolean isPinned();

    public void setPinned(boolean var1);

    public boolean isPinnable();

    public void setPinnable(boolean var1);

    public boolean isMinimized();

    public void setMinimized(boolean var1);

    public boolean isMinimizable();

    public void setMinimizable(boolean var1);

    public void close();

    public boolean isClosable();

    public void setClosable(boolean var1);
}


/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component;

import org.darkstorm.minecraft.gui.component.Component;
import org.darkstorm.minecraft.gui.layout.Constraint;
import org.darkstorm.minecraft.gui.layout.LayoutManager;

public interface Container
extends Component {
    public LayoutManager getLayoutManager();

    public void setLayoutManager(LayoutManager var1);

    public Component[] getChildren();

    public void add(Component var1, Constraint ... var2);

    public Constraint[] getConstraints(Component var1);

    public Component getChildAt(int var1, int var2);

    public boolean hasChild(Component var1);

    public boolean remove(Component var1);

    public void layoutChildren();
}


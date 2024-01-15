/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.theme;

import java.util.HashMap;
import java.util.Map;
import org.darkstorm.minecraft.gui.component.Component;
import org.darkstorm.minecraft.gui.theme.AbstractComponentUI;
import org.darkstorm.minecraft.gui.theme.ComponentUI;
import org.darkstorm.minecraft.gui.theme.Theme;

public abstract class AbstractTheme
implements Theme {
    protected final Map<Class<? extends Component>, ComponentUI> uis = new HashMap<Class<? extends Component>, ComponentUI>();

    protected void installUI(AbstractComponentUI<?> ui) {
        this.uis.put(ui.handledComponentClass, ui);
    }

    @Override
    public ComponentUI getUIForComponent(Component component) {
        if (component == null || !(component instanceof Component)) {
            throw new IllegalArgumentException();
        }
        return this.getComponentUIForClass(component.getClass());
    }

    public ComponentUI getComponentUIForClass(Class<? extends Component> componentClass) {
        Class<?>[] classArray = componentClass.getInterfaces();
        int n = classArray.length;
        int n2 = 0;
        while (n2 < n) {
            Class<?> componentInterface = classArray[n2];
            ComponentUI ui = this.uis.get(componentInterface);
            if (ui != null) {
                return ui;
            }
            ++n2;
        }
        if (componentClass.getSuperclass().equals(Component.class)) {
            return this.uis.get(componentClass);
        }
        if (!Component.class.isAssignableFrom(componentClass.getSuperclass())) {
            return null;
        }
        return this.getComponentUIForClass(componentClass.getSuperclass());
    }
}


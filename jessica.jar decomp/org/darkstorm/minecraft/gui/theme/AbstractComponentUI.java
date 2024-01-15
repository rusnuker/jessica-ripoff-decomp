/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
package org.darkstorm.minecraft.gui.theme;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import org.darkstorm.minecraft.gui.component.Component;
import org.darkstorm.minecraft.gui.component.Container;
import org.darkstorm.minecraft.gui.theme.ComponentUI;
import org.lwjgl.opengl.GL11;

public abstract class AbstractComponentUI<T extends Component>
implements ComponentUI {
    protected final Class<T> handledComponentClass;
    protected Color foreground;
    protected Color background;

    public AbstractComponentUI(Class<T> handledComponentClass) {
        this.handledComponentClass = handledComponentClass;
    }

    @Override
    public void render(Component component) {
        if (component == null) {
            throw new NullPointerException();
        }
        if (!this.handledComponentClass.isInstance(component)) {
            throw new IllegalArgumentException();
        }
        if (!component.isVisible()) {
            return;
        }
        this.renderComponent((Component)this.handledComponentClass.cast(component));
    }

    protected abstract void renderComponent(T var1);

    @Override
    public Rectangle getChildRenderArea(Container container) {
        if (!Container.class.isAssignableFrom(this.handledComponentClass)) {
            throw new UnsupportedOperationException();
        }
        if (container == null) {
            throw new NullPointerException();
        }
        if (!this.handledComponentClass.isInstance(container)) {
            throw new IllegalArgumentException();
        }
        return this.getContainerChildRenderArea((Component)this.handledComponentClass.cast(container));
    }

    protected Rectangle getContainerChildRenderArea(T container) {
        return new Rectangle(new Point(0, 0), container.getSize());
    }

    @Override
    public Dimension getDefaultSize(Component component) {
        if (component == null) {
            throw new NullPointerException();
        }
        if (!this.handledComponentClass.isInstance(component)) {
            throw new IllegalArgumentException();
        }
        return this.getDefaultComponentSize((Component)this.handledComponentClass.cast(component));
    }

    protected abstract Dimension getDefaultComponentSize(T var1);

    protected void translateComponent(Component component, boolean reverse) {
        Container parent = component.getParent();
        while (parent != null) {
            GL11.glTranslated((double)((reverse ? -1 : 1) * parent.getX()), (double)((reverse ? -1 : 1) * parent.getY()), (double)0.0);
            parent = parent.getParent();
        }
        GL11.glTranslated((double)((reverse ? -1 : 1) * component.getX()), (double)((reverse ? -1 : 1) * component.getY()), (double)0.0);
    }

    @Override
    public Color getDefaultBackgroundColor(Component component) {
        if (component == null) {
            throw new NullPointerException();
        }
        if (!this.handledComponentClass.isInstance(component)) {
            throw new IllegalArgumentException();
        }
        return this.getBackgroundColor((Component)this.handledComponentClass.cast(component));
    }

    protected Color getBackgroundColor(T component) {
        return this.background;
    }

    @Override
    public Color getDefaultForegroundColor(Component component) {
        if (component == null) {
            throw new NullPointerException();
        }
        if (!this.handledComponentClass.isInstance(component)) {
            throw new IllegalArgumentException();
        }
        return this.getForegroundColor((Component)this.handledComponentClass.cast(component));
    }

    protected Color getForegroundColor(T component) {
        return this.foreground;
    }

    @Override
    public Rectangle[] getInteractableRegions(Component component) {
        if (component == null) {
            throw new NullPointerException();
        }
        if (!this.handledComponentClass.isInstance(component)) {
            throw new IllegalArgumentException();
        }
        return this.getInteractableComponentRegions((Component)this.handledComponentClass.cast(component));
    }

    protected Rectangle[] getInteractableComponentRegions(T component) {
        return new Rectangle[0];
    }

    @Override
    public void handleInteraction(Component component, Point location, int button) {
        if (component == null) {
            throw new NullPointerException();
        }
        if (!this.handledComponentClass.isInstance(component)) {
            throw new IllegalArgumentException();
        }
        this.handleComponentInteraction((Component)this.handledComponentClass.cast(component), location, button);
    }

    protected void handleComponentInteraction(T component, Point location, int button) {
    }

    @Override
    public void handleUpdate(Component component) {
        if (component == null) {
            throw new NullPointerException();
        }
        if (!this.handledComponentClass.isInstance(component)) {
            throw new IllegalArgumentException();
        }
        this.handleComponentUpdate((Component)this.handledComponentClass.cast(component));
    }

    protected void handleComponentUpdate(T component) {
    }
}


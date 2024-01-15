/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component;

import java.awt.Rectangle;
import java.util.LinkedHashMap;
import java.util.Map;
import org.darkstorm.minecraft.gui.component.AbstractComponent;
import org.darkstorm.minecraft.gui.component.Component;
import org.darkstorm.minecraft.gui.component.Container;
import org.darkstorm.minecraft.gui.layout.BasicLayoutManager;
import org.darkstorm.minecraft.gui.layout.Constraint;
import org.darkstorm.minecraft.gui.layout.LayoutManager;
import org.darkstorm.minecraft.gui.theme.Theme;

public abstract class AbstractContainer
extends AbstractComponent
implements Container {
    private final Map<Component, Constraint[]> children = new LinkedHashMap<Component, Constraint[]>();
    private LayoutManager layoutManager = new BasicLayoutManager();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void render() {
        super.render();
        Map<Component, Constraint[]> map = this.children;
        synchronized (map) {
            for (Component child : this.children.keySet()) {
                child.render();
            }
        }
    }

    @Override
    public LayoutManager getLayoutManager() {
        return this.layoutManager;
    }

    @Override
    public void setLayoutManager(LayoutManager layoutManager) {
        if (layoutManager == null) {
            layoutManager = new BasicLayoutManager();
        }
        this.layoutManager = layoutManager;
        this.layoutChildren();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Component[] getChildren() {
        Map<Component, Constraint[]> map = this.children;
        synchronized (map) {
            return this.children.keySet().toArray(new Component[this.children.size()]);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void add(Component child, Constraint ... constraints) {
        Map<Component, Constraint[]> map = this.children;
        synchronized (map) {
            Container parent = child.getParent();
            if (parent != null && parent.hasChild(child)) {
                parent.remove(child);
            }
            this.children.put(child, constraints);
            if (!this.enabled) {
                child.setEnabled(false);
            }
            if (!this.visible) {
                child.setVisible(false);
            }
            child.setParent(this);
            child.setTheme(this.getTheme());
            this.layoutChildren();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Constraint[] getConstraints(Component child) {
        if (child == null) {
            throw new NullPointerException();
        }
        Map<Component, Constraint[]> map = this.children;
        synchronized (map) {
            Constraint[] constraints = this.children.get(child);
            return constraints != null ? constraints : new Constraint[]{};
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Component getChildAt(int x, int y) {
        Map<Component, Constraint[]> map = this.children;
        synchronized (map) {
            for (Component child : this.children.keySet()) {
                if (!child.getArea().contains(x, y)) continue;
                return child;
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean remove(Component child) {
        Map<Component, Constraint[]> map = this.children;
        synchronized (map) {
            block4: {
                if (this.children.remove(child) == null) break block4;
                this.layoutChildren();
                return true;
            }
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean hasChild(Component child) {
        Map<Component, Constraint[]> map = this.children;
        synchronized (map) {
            return this.children.get(child) != null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setTheme(Theme theme) {
        super.setTheme(theme);
        Map<Component, Constraint[]> map = this.children;
        synchronized (map) {
            for (Component child : this.children.keySet()) {
                child.setTheme(theme);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void layoutChildren() {
        Map<Component, Constraint[]> map = this.children;
        synchronized (map) {
            Component[] components = this.children.keySet().toArray(new Component[this.children.size()]);
            Rectangle[] areas = new Rectangle[components.length];
            int i = 0;
            while (i < components.length) {
                areas[i] = components[i].getArea();
                ++i;
            }
            Constraint[][] allConstraints = (Constraint[][])this.children.values().toArray((T[])new Constraint[this.children.size()][]);
            if (this.getTheme() != null) {
                this.layoutManager.reposition(this.ui.getChildRenderArea(this), areas, allConstraints);
            }
            Component[] componentArray = components;
            int n = components.length;
            int n2 = 0;
            while (n2 < n) {
                Component child = componentArray[n2];
                if (child instanceof Container) {
                    ((Container)child).layoutChildren();
                }
                ++n2;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void onMousePress(int x, int y, int button) {
        super.onMousePress(x, y, button);
        Map<Component, Constraint[]> map = this.children;
        synchronized (map) {
            for (Component child : this.children.keySet()) {
                if (!child.isVisible() || child.getArea().contains(x, y)) continue;
                Rectangle[] rectangleArray = child.getTheme().getUIForComponent(child).getInteractableRegions(child);
                int n = rectangleArray.length;
                int n2 = 0;
                while (n2 < n) {
                    Rectangle area = rectangleArray[n2];
                    if (area.contains(x - child.getX(), y - child.getY())) {
                        child.onMousePress(x - child.getX(), y - child.getY(), button);
                        return;
                    }
                    ++n2;
                }
            }
            for (Component child : this.children.keySet()) {
                if (!child.isVisible() || !child.getArea().contains(x, y)) continue;
                child.onMousePress(x - child.getX(), y - child.getY(), button);
                return;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void onMouseRelease(int x, int y, int button) {
        super.onMouseRelease(x, y, button);
        Map<Component, Constraint[]> map = this.children;
        synchronized (map) {
            for (Component child : this.children.keySet()) {
                if (!child.isVisible() || child.getArea().contains(x, y)) continue;
                Rectangle[] rectangleArray = child.getTheme().getUIForComponent(child).getInteractableRegions(child);
                int n = rectangleArray.length;
                int n2 = 0;
                while (n2 < n) {
                    Rectangle area = rectangleArray[n2];
                    if (area.contains(x - child.getX(), y - child.getY())) {
                        child.onMouseRelease(x - child.getX(), y - child.getY(), button);
                        return;
                    }
                    ++n2;
                }
            }
            for (Component child : this.children.keySet()) {
                if (!child.isVisible() || !child.getArea().contains(x, y)) continue;
                child.onMouseRelease(x - child.getX(), y - child.getY(), button);
                return;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        enabled = this.isEnabled();
        Map<Component, Constraint[]> map = this.children;
        synchronized (map) {
            for (Component child : this.children.keySet()) {
                child.setEnabled(enabled);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        visible = this.isVisible();
        Map<Component, Constraint[]> map = this.children;
        synchronized (map) {
            for (Component child : this.children.keySet()) {
                child.setVisible(visible);
            }
        }
    }

    @Override
    public void update() {
        Component[] componentArray = this.getChildren();
        int n = componentArray.length;
        int n2 = 0;
        while (n2 < n) {
            Component child = componentArray[n2];
            child.update();
            ++n2;
        }
    }
}


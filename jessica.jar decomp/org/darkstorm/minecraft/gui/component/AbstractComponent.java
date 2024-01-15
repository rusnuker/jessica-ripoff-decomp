/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.darkstorm.minecraft.gui.component.Component;
import org.darkstorm.minecraft.gui.component.Container;
import org.darkstorm.minecraft.gui.component.basic.BasicFrame;
import org.darkstorm.minecraft.gui.listener.ComponentListener;
import org.darkstorm.minecraft.gui.theme.ComponentUI;
import org.darkstorm.minecraft.gui.theme.Theme;

public abstract class AbstractComponent
implements Component {
    private Container parent = null;
    private Theme theme;
    protected Rectangle area = new Rectangle(0, 0, 0, 0);
    protected ComponentUI ui;
    protected Color foreground;
    protected Color background;
    protected boolean enabled = true;
    protected boolean visible = true;
    private List<ComponentListener> listeners = new CopyOnWriteArrayList<ComponentListener>();

    @Override
    public void render() {
        if (this.ui == null) {
            return;
        }
        this.ui.render(this);
    }

    @Override
    public void update() {
        if (this.ui == null) {
            return;
        }
        this.ui.handleUpdate(this);
    }

    protected ComponentUI getUI() {
        return this.theme.getUIForComponent(this);
    }

    @Override
    public void onMousePress(int x, int y, int button) {
        if (this.ui != null) {
            Rectangle[] rectangleArray = this.ui.getInteractableRegions(this);
            int n = rectangleArray.length;
            int n2 = 0;
            while (n2 < n) {
                Rectangle area = rectangleArray[n2];
                if (area.contains(x, y)) {
                    this.ui.handleInteraction(this, new Point(x, y), button);
                    break;
                }
                ++n2;
            }
        }
    }

    @Override
    public void onMouseRelease(int x, int y, int button) {
    }

    @Override
    public Theme getTheme() {
        return this.theme;
    }

    @Override
    public void setTheme(Theme theme) {
        boolean changeArea;
        Dimension defaultSize;
        Theme oldTheme = this.theme;
        this.theme = theme;
        if (theme == null) {
            this.ui = null;
            this.foreground = null;
            this.background = null;
            return;
        }
        this.ui = this.getUI();
        if (oldTheme != null) {
            defaultSize = oldTheme.getUIForComponent(this).getDefaultSize(this);
            changeArea = this.area.width == defaultSize.width && this.area.height == defaultSize.height;
        } else {
            changeArea = this.area.equals(new Rectangle(0, 0, 0, 0));
        }
        if (changeArea) {
            defaultSize = this.ui.getDefaultSize(this);
            this.area = new Rectangle(this.area.x, this.area.y, defaultSize.width, defaultSize.height);
        }
        this.foreground = this.ui.getDefaultForegroundColor(this);
        this.background = this.ui.getDefaultBackgroundColor(this);
    }

    @Override
    public int getX() {
        return this.area.x;
    }

    @Override
    public int getY() {
        return this.area.y;
    }

    @Override
    public int getWidth() {
        if (this instanceof BasicFrame) {
            return 70;
        }
        return this.area.width;
    }

    @Override
    public int getHeight() {
        return this.area.height;
    }

    @Override
    public void setX(int x) {
        this.area.x = x;
    }

    @Override
    public void setY(int y) {
        this.area.y = y;
    }

    @Override
    public void setWidth(int width) {
        this.area.width = this instanceof BasicFrame ? 70 : width;
    }

    @Override
    public void setHeight(int height) {
        this.area.height = height;
    }

    @Override
    public Color getBackgroundColor() {
        return this.background;
    }

    @Override
    public Color getForegroundColor() {
        return this.foreground;
    }

    @Override
    public void setBackgroundColor(Color color) {
        this.background = color;
    }

    @Override
    public void setForegroundColor(Color color) {
        this.foreground = color;
    }

    @Override
    public Point getLocation() {
        return this.area.getLocation();
    }

    @Override
    public Dimension getSize() {
        return this.area.getSize();
    }

    @Override
    public Rectangle getArea() {
        return this.area;
    }

    @Override
    public Container getParent() {
        return this.parent;
    }

    @Override
    public void setParent(Container parent) {
        if (!parent.hasChild(this) || this.parent != null && this.parent.hasChild(this)) {
            throw new IllegalArgumentException();
        }
        this.parent = parent;
    }

    @Override
    public void resize() {
        Dimension defaultDimension = this.ui.getDefaultSize(this);
        this.setWidth(defaultDimension.width);
        this.setHeight(defaultDimension.height);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = this.parent != null && !this.parent.isEnabled() ? false : enabled;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = this.parent != null && !this.parent.isVisible() ? false : visible;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void addListener(ComponentListener listener) {
        List<ComponentListener> list = this.listeners;
        synchronized (list) {
            this.listeners.add(listener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void removeListener(ComponentListener listener) {
        List<ComponentListener> list = this.listeners;
        synchronized (list) {
            this.listeners.remove(listener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected ComponentListener[] getListeners() {
        List<ComponentListener> list = this.listeners;
        synchronized (list) {
            return this.listeners.toArray(new ComponentListener[this.listeners.size()]);
        }
    }
}


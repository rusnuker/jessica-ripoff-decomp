/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Mouse
 */
package org.darkstorm.minecraft.gui.component.basic;

import java.awt.Point;
import org.darkstorm.minecraft.gui.component.AbstractContainer;
import org.darkstorm.minecraft.gui.component.Frame;
import org.darkstorm.minecraft.gui.util.RenderUtil;
import org.lwjgl.input.Mouse;

public class BasicFrame
extends AbstractContainer
implements Frame {
    private String title;
    private Point dragOffset;
    private boolean pinned;
    private boolean pinnable = true;
    private boolean minimized;
    private boolean minimizable = true;
    private boolean closable = true;

    @Override
    public void render() {
        if (this.isDragging()) {
            if (Mouse.isButtonDown((int)0)) {
                Point mouseLocation = RenderUtil.calculateMouseLocation();
                this.setX(mouseLocation.x - this.dragOffset.x);
                this.setY(mouseLocation.y - this.dragOffset.y);
            } else {
                this.setDragging(false);
            }
        }
        if (this.minimized) {
            if (this.ui != null) {
                this.ui.render(this);
            }
        } else {
            super.render();
        }
    }

    public BasicFrame() {
        this("");
    }

    public BasicFrame(String title) {
        this.setVisible(false);
        this.title = title;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean isDragging() {
        return this.dragOffset != null;
    }

    @Override
    public void setDragging(boolean dragging) {
        if (dragging) {
            Point mouseLocation = RenderUtil.calculateMouseLocation();
            this.dragOffset = new Point(mouseLocation.x - this.getX(), mouseLocation.y - this.getY());
        } else {
            this.dragOffset = null;
        }
    }

    @Override
    public boolean isPinned() {
        return this.pinned;
    }

    @Override
    public void setPinned(boolean pinned) {
        if (!this.pinnable) {
            pinned = false;
        }
        this.pinned = pinned;
    }

    @Override
    public boolean isPinnable() {
        return this.pinnable;
    }

    @Override
    public void setPinnable(boolean pinnable) {
        if (!pinnable) {
            this.pinned = false;
        }
        this.pinnable = pinnable;
    }

    @Override
    public boolean isMinimized() {
        return this.minimized;
    }

    @Override
    public void setMinimized(boolean minimized) {
        if (!this.minimizable) {
            minimized = false;
        }
        this.minimized = minimized;
    }

    @Override
    public boolean isMinimizable() {
        return this.minimizable;
    }

    @Override
    public void setMinimizable(boolean minimizable) {
        if (!minimizable) {
            this.minimized = false;
        }
        this.minimizable = minimizable;
    }

    @Override
    public void close() {
        if (this.closable) {
            this.setVisible(false);
        }
    }

    @Override
    public boolean isClosable() {
        return this.closable;
    }

    @Override
    public void setClosable(boolean closable) {
        this.closable = closable;
    }
}


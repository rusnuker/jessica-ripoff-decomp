/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.component.basic;

import org.darkstorm.minecraft.gui.component.AbstractComponent;
import org.darkstorm.minecraft.gui.component.Label;

public class BasicLabel
extends AbstractComponent
implements Label {
    protected String text;
    protected Label.TextAlignment horizontalAlignment = Label.TextAlignment.LEFT;
    protected Label.TextAlignment verticalAlignment = Label.TextAlignment.CENTER;

    public BasicLabel() {
    }

    public BasicLabel(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Label.TextAlignment getHorizontalAlignment() {
        return this.horizontalAlignment;
    }

    @Override
    public Label.TextAlignment getVerticalAlignment() {
        return this.verticalAlignment;
    }

    @Override
    public void setHorizontalAlignment(Label.TextAlignment alignment) {
        this.horizontalAlignment = alignment;
    }

    @Override
    public void setVerticalAlignment(Label.TextAlignment alignment) {
        this.verticalAlignment = alignment;
    }
}


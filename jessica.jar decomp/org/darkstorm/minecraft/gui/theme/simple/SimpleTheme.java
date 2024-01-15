/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.theme.simple;

import java.awt.Font;
import net.minecraft.client.gui.FontRenderer;
import org.darkstorm.minecraft.gui.font.UnicodeFontRenderer;
import org.darkstorm.minecraft.gui.theme.AbstractTheme;
import org.darkstorm.minecraft.gui.theme.simple.SimpleButtonUI;
import org.darkstorm.minecraft.gui.theme.simple.SimpleCheckButtonUI;
import org.darkstorm.minecraft.gui.theme.simple.SimpleComboBoxUI;
import org.darkstorm.minecraft.gui.theme.simple.SimpleFrameUI;
import org.darkstorm.minecraft.gui.theme.simple.SimpleLabelUI;
import org.darkstorm.minecraft.gui.theme.simple.SimplePanelUI;
import org.darkstorm.minecraft.gui.theme.simple.SimpleProgressBarUI;
import org.darkstorm.minecraft.gui.theme.simple.SimpleSliderUI;

public class SimpleTheme
extends AbstractTheme {
    private final FontRenderer fontRenderer = new UnicodeFontRenderer(new Font("Trebuchet MS", 0, 15));

    public SimpleTheme() {
        this.installUI(new SimpleFrameUI(this));
        this.installUI(new SimplePanelUI(this));
        this.installUI(new SimpleLabelUI(this));
        this.installUI(new SimpleButtonUI(this));
        this.installUI(new SimpleCheckButtonUI(this));
        this.installUI(new SimpleComboBoxUI(this));
        this.installUI(new SimpleSliderUI(this));
        this.installUI(new SimpleProgressBarUI(this));
    }

    public FontRenderer getFontRenderer() {
        return this.fontRenderer;
    }
}


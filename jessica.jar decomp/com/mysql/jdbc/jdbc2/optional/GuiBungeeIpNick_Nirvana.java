/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Charsets
 *  org.lwjgl.input.Keyboard
 */
package com.mysql.jdbc.jdbc2.optional;

import com.google.common.base.Charsets;
import com.mysql.fabric.Wrapper;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.datafix.HackPack;
import org.lwjgl.input.Keyboard;

public class GuiBungeeIpNick_Nirvana
extends GuiScreen {
    private GuiScreen parentScreen;
    private GuiTextField usernameTextField;
    private GuiTextField usernameTextField2;
    private String error;

    public GuiBungeeIpNick_Nirvana(GuiScreen parentScreen2) {
        this.parentScreen = parentScreen2;
    }

    @Override
    public void updateScreen() {
        this.usernameTextField.updateCursorCounter();
        this.usernameTextField2.updateCursorCounter();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents((boolean)false);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (!guibutton.enabled) {
            return;
        }
        if (guibutton.id == 1) {
            this.mc.displayGuiScreen(this.parentScreen);
        } else if (guibutton.id == 0) {
            Wrapper.getHackPack();
            HackPack.setFakeUUID(UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.usernameTextField2.getText()).getBytes(Charsets.UTF_8)).toString());
            Wrapper.getHackPack();
            HackPack.setFakeIP(this.usernameTextField.getText());
        }
        this.mc.displayGuiScreen(this.parentScreen);
    }

    @Override
    protected void keyTyped(char c, int i) {
        this.usernameTextField.textboxKeyTyped(c, i);
        this.usernameTextField2.textboxKeyTyped(c, i);
        if (c == '\t' && this.usernameTextField.isFocused()) {
            this.usernameTextField.setFocused(false);
        }
        if (c == '\r') {
            this.actionPerformed((GuiButton)this.buttonList.get(0));
        }
    }

    @Override
    protected void mouseClicked(int i, int j, int k) throws IOException {
        super.mouseClicked(i, j, k);
        this.usernameTextField.mouseClicked(i, j, k);
        this.usernameTextField2.mouseClicked(i, j, k);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents((boolean)true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + 12, "Done"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + 12, "Cancel"));
        this.usernameTextField = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 100, 116, 200, 20);
        this.usernameTextField2 = new GuiTextField(3, this.fontRendererObj, this.width / 2 - 100, 96, 200, 20);
        this.usernameTextField.setMaxStringLength(500);
        this.usernameTextField2.setMaxStringLength(500);
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        this.drawDefaultBackground();
        this.usernameTextField.drawTextBox();
        this.drawCenteredString(this.fontRendererObj, "\u2191 Nick, \u2193 IP.", this.width / 2, this.height / 4 - 60 + 20, 0xFFFFFF);
        this.usernameTextField2.drawTextBox();
        super.drawScreen(i, j, f);
    }
}


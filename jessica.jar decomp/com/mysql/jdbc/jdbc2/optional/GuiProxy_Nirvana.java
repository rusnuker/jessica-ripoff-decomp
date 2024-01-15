/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 */
package com.mysql.jdbc.jdbc2.optional;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public class GuiProxy_Nirvana
extends GuiScreen {
    private GuiScreen parentScreen;
    private GuiTextField ipPortTextField;
    public static String strIpPort = "";

    public GuiProxy_Nirvana(GuiScreen guiscreen) {
        this.parentScreen = guiscreen;
    }

    @Override
    public void updateScreen() {
        this.ipPortTextField.updateCursorCounter();
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
            try {
                strIpPort = this.ipPortTextField.getText();
                if (strIpPort.equals("")) {
                    System.setProperty("proxySet", "false");
                } else {
                    System.setProperty("proxySet", "true");
                    System.setProperty("socksProxyHost", strIpPort.split(":")[0]);
                    System.setProperty("socksProxyPort", strIpPort.split(":")[1]);
                }
            }
            catch (Exception e) {
                System.setProperty("proxySet", "false");
                strIpPort = "";
            }
        }
        this.mc.displayGuiScreen(this.parentScreen);
    }

    @Override
    protected void keyTyped(char c, int i) {
        this.ipPortTextField.textboxKeyTyped(c, i);
        if (c == '\t' && this.ipPortTextField.isFocused()) {
            this.ipPortTextField.setFocused(false);
        }
        if (c == '\r') {
            this.actionPerformed((GuiButton)this.buttonList.get(0));
        }
    }

    @Override
    protected void mouseClicked(int i, int j, int k) throws IOException {
        super.mouseClicked(i, j, k);
        this.ipPortTextField.mouseClicked(i, j, k);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents((boolean)true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + 12, "Done"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + 12, "Cancel"));
        this.ipPortTextField = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 100, 116, 200, 20);
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Proxy", this.width / 2, this.height / 4 - 60 + 20, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "IP:Port (Socks 4/5)", this.width / 2 - 100, 104, 0xA0A0A0);
        this.ipPortTextField.drawTextBox();
        super.drawScreen(i, j, f);
    }
}


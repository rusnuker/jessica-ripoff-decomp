/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 */
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.jdbc2.optional.GuiBungeeIpNick_Nirvana;
import com.mysql.jdbc.jdbc2.optional.GuiHttpGetBrute_Nirvana;
import com.mysql.jdbc.jdbc2.optional.GuiHttpPostBrute_Nirvana;
import com.mysql.jdbc.jdbc2.optional.GuiOfflineName_Nirvana;
import com.mysql.jdbc.jdbc2.optional.GuiProxy_Nirvana;
import com.mysql.jdbc.jdbc2.optional.GuiSpoofUUID_Nirvana;
import com.mysql.jdbc.jdbc2.optional.GuiSubdomainBrute_Nirvana;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

public class GuiHackTools_Nirvana
extends GuiScreen {
    private GuiScreen parentScreen;

    public GuiHackTools_Nirvana(GuiScreen guiscreen) {
        this.parentScreen = guiscreen;
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
        if (guibutton.id == 0) {
            this.mc.displayGuiScreen(this.parentScreen);
        } else if (guibutton.id == 1) {
            GuiSubdomainBrute_Nirvana ps = new GuiSubdomainBrute_Nirvana();
            ps.setVisible(true);
        } else if (guibutton.id == 2) {
            this.mc.displayGuiScreen(new GuiOfflineName_Nirvana(this.parentScreen));
        } else if (guibutton.id == 3) {
            this.mc.displayGuiScreen(new GuiBungeeIpNick_Nirvana(this.parentScreen));
        } else if (guibutton.id == 4) {
            GuiHttpPostBrute_Nirvana ps = new GuiHttpPostBrute_Nirvana();
            ps.setVisible(true);
        } else if (guibutton.id == 5) {
            GuiHttpGetBrute_Nirvana ps = new GuiHttpGetBrute_Nirvana();
            ps.setVisible(true);
        } else if (guibutton.id == 7) {
            this.mc.displayGuiScreen(new GuiSpoofUUID_Nirvana(this.parentScreen));
        } else if (guibutton.id == 6) {
            this.mc.displayGuiScreen(new GuiProxy_Nirvana(this.parentScreen));
        }
    }

    @Override
    protected void mouseClicked(int i, int j, int k) throws IOException {
        super.mouseClicked(i, j, k);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents((boolean)true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(6, this.width / 2 - 100, this.height / 4 + -48 + 12, "Proxy"));
        this.buttonList.add(new GuiButton(5, this.width / 2 - 100, this.height / 4 + -24 + 12, "HTTP/HTTPS GET Method Brute"));
        this.buttonList.add(new GuiButton(7, this.width / 2 - 100, this.height / 4 + 0 + 12, "Spoof UUID"));
        this.buttonList.add(new GuiButton(4, this.width / 2 - 100, this.height / 4 + 24 + 12, "HTTP/HTTPS POST Method Accounts Brute"));
        this.buttonList.add(new GuiButton(3, this.width / 2 - 100, this.height / 4 + 48 + 12, "Bungee Offline UUID Spoof"));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 100, this.height / 4 + 72 + 12, "Offline Name"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96 + 12, "Subdomain Brute"));
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120 + 12, "Cancel"));
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        this.drawDefaultBackground();
        super.drawScreen(i, j, f);
    }
}


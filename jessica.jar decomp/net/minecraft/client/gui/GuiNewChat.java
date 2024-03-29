/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.mysql.fabric.ChatCommands;
import com.mysql.fabric.Wrapper;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.datafix.FileManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiNewChat
extends Gui {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft mc;
    private final List<String> sentMessages = Lists.newArrayList();
    public final List<ChatLine> chatLines = Lists.newArrayList();
    private final List<ChatLine> drawnChatLines = Lists.newArrayList();
    private int scrollPos;
    private boolean isScrolled;

    public GuiNewChat(Minecraft mcIn) {
        this.mc = mcIn;
    }

    public void drawChat(int updateCounter) {
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int i = this.getLineCount();
            int j = this.drawnChatLines.size();
            float f = this.mc.gameSettings.chatOpacity * 0.9f + 0.1f;
            if (j > 0) {
                boolean flag = false;
                if (this.getChatOpen()) {
                    flag = true;
                }
                float f1 = this.getChatScale();
                int k = MathHelper.ceil((float)this.getChatWidth() / f1);
                GlStateManager.pushMatrix();
                GlStateManager.translate(2.0f, 8.0f, 0.0f);
                GlStateManager.scale(f1, f1, 1.0f);
                int l = 0;
                int i1 = 0;
                while (i1 + this.scrollPos < this.drawnChatLines.size() && i1 < i) {
                    int j1;
                    ChatLine chatline = this.drawnChatLines.get(i1 + this.scrollPos);
                    if (chatline != null && ((j1 = updateCounter - chatline.getUpdatedCounter()) < 200 || flag)) {
                        double d0 = (double)j1 / 200.0;
                        d0 = 1.0 - d0;
                        d0 *= 10.0;
                        d0 = MathHelper.clamp(d0, 0.0, 1.0);
                        d0 *= d0;
                        int l1 = (int)(255.0 * d0);
                        if (flag) {
                            l1 = 255;
                        }
                        l1 = (int)((float)l1 * f);
                        ++l;
                        if (l1 > 3) {
                            boolean i2 = false;
                            int j2 = -i1 * 9;
                            GuiNewChat.drawRect(-2, j2 - 9, 0 + k + 4, j2, l1 / 2 << 24);
                            String s = chatline.getChatComponent().getFormattedText();
                            GlStateManager.enableBlend();
                            this.mc.fontRendererObj.drawStringWithShadow(s, 0.0f, j2 - 8, 0xFFFFFF + (l1 << 24));
                            GlStateManager.disableAlpha();
                            GlStateManager.disableBlend();
                        }
                    }
                    ++i1;
                }
                if (flag) {
                    int k2 = this.mc.fontRendererObj.FONT_HEIGHT;
                    GlStateManager.translate(-3.0f, 0.0f, 0.0f);
                    int l2 = j * k2 + j;
                    int i3 = l * k2 + l;
                    int j3 = this.scrollPos * i3 / j;
                    int k1 = i3 * i3 / l2;
                    if (l2 != i3) {
                        int k3 = j3 > 0 ? 170 : 96;
                        int l3 = this.isScrolled ? 0xCC3333 : 0x3333AA;
                        GuiNewChat.drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
                        GuiNewChat.drawRect(2, -j3, 1, -j3 - k1, 0xCCCCCC + (k3 << 24));
                    }
                }
                GlStateManager.popMatrix();
            }
        }
    }

    public void clearChatMessages(boolean p_146231_1_) {
        this.drawnChatLines.clear();
        this.chatLines.clear();
        if (p_146231_1_) {
            this.sentMessages.clear();
        }
    }

    public void printChatMessage(ITextComponent chatComponent) {
        this.printChatMessageWithOptionalDeletion(chatComponent, 0);
    }

    public void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId) {
        if (ChatCommands.isGetData) {
            Wrapper.getFiles();
            File txt = new File(String.valueOf(FileManager.getClientDir()) + "SVS getter data\\" + ChatCommands.fileData);
            try {
                String name = chatComponent.getUnformattedText().split("Unable to execute \"")[1].split("\" as no matching commands were found.")[0];
                Wrapper.getFiles();
                FileManager.write(txt, name);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        this.setChatLine(chatComponent, chatLineId, this.mc.ingameGUI.getUpdateCounter(), false);
        LOGGER.info("[CHAT] {}", (Object)chatComponent.getUnformattedText().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
    }

    private void setChatLine(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly) {
        if (chatLineId != 0) {
            this.deleteChatLine(chatLineId);
        }
        int i = MathHelper.floor((float)this.getChatWidth() / this.getChatScale());
        List<ITextComponent> list = GuiUtilRenderComponents.splitText(chatComponent, i, this.mc.fontRendererObj, false, false);
        boolean flag = this.getChatOpen();
        for (ITextComponent itextcomponent : list) {
            if (flag && this.scrollPos > 0) {
                this.isScrolled = true;
                this.scroll(1);
            }
            this.drawnChatLines.add(0, new ChatLine(updateCounter, itextcomponent, chatLineId));
        }
        while (this.drawnChatLines.size() > 100) {
            this.drawnChatLines.remove(this.drawnChatLines.size() - 1);
        }
        if (!displayOnly) {
            this.chatLines.add(0, new ChatLine(updateCounter, chatComponent, chatLineId));
            while (this.chatLines.size() > 100) {
                this.chatLines.remove(this.chatLines.size() - 1);
            }
        }
    }

    public void refreshChat() {
        this.drawnChatLines.clear();
        this.resetScroll();
        int i = this.chatLines.size() - 1;
        while (i >= 0) {
            ChatLine chatline = this.chatLines.get(i);
            this.setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
            --i;
        }
    }

    public List<String> getSentMessages() {
        return this.sentMessages;
    }

    public void addToSentMessages(String message) {
        if (this.sentMessages.isEmpty() || !this.sentMessages.get(this.sentMessages.size() - 1).equals(message)) {
            this.sentMessages.add(message);
        }
    }

    public void resetScroll() {
        this.scrollPos = 0;
        this.isScrolled = false;
    }

    public void scroll(int amount) {
        this.scrollPos += amount;
        int i = this.drawnChatLines.size();
        if (this.scrollPos > i - this.getLineCount()) {
            this.scrollPos = i - this.getLineCount();
        }
        if (this.scrollPos <= 0) {
            this.scrollPos = 0;
            this.isScrolled = false;
        }
    }

    @Nullable
    public ITextComponent getChatComponent(int mouseX, int mouseY) {
        if (!this.getChatOpen()) {
            return null;
        }
        ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        int i = scaledresolution.getScaleFactor();
        float f = this.getChatScale();
        int j = mouseX / i - 2;
        int k = mouseY / i - 40;
        j = MathHelper.floor((float)j / f);
        k = MathHelper.floor((float)k / f);
        if (j >= 0 && k >= 0) {
            int l = Math.min(this.getLineCount(), this.drawnChatLines.size());
            if (j <= MathHelper.floor((float)this.getChatWidth() / this.getChatScale()) && k < this.mc.fontRendererObj.FONT_HEIGHT * l + l) {
                int i1 = k / this.mc.fontRendererObj.FONT_HEIGHT + this.scrollPos;
                if (i1 >= 0 && i1 < this.drawnChatLines.size()) {
                    ChatLine chatline = this.drawnChatLines.get(i1);
                    int j1 = 0;
                    for (ITextComponent itextcomponent : chatline.getChatComponent()) {
                        if (!(itextcomponent instanceof TextComponentString) || (j1 += this.mc.fontRendererObj.getStringWidth(GuiUtilRenderComponents.removeTextColorsIfConfigured(((TextComponentString)itextcomponent).getText(), false))) <= j) continue;
                        return itextcomponent;
                    }
                }
                return null;
            }
            return null;
        }
        return null;
    }

    public boolean getChatOpen() {
        return this.mc.currentScreen instanceof GuiChat;
    }

    public void deleteChatLine(int id) {
        Iterator<ChatLine> iterator = this.drawnChatLines.iterator();
        while (iterator.hasNext()) {
            ChatLine chatline = iterator.next();
            if (chatline.getChatLineID() != id) continue;
            iterator.remove();
        }
        iterator = this.chatLines.iterator();
        while (iterator.hasNext()) {
            ChatLine chatline1 = iterator.next();
            if (chatline1.getChatLineID() != id) continue;
            iterator.remove();
            break;
        }
    }

    public int getChatWidth() {
        return GuiNewChat.calculateChatboxWidth(this.mc.gameSettings.chatWidth);
    }

    public int getChatHeight() {
        return GuiNewChat.calculateChatboxHeight(this.getChatOpen() ? this.mc.gameSettings.chatHeightFocused : this.mc.gameSettings.chatHeightUnfocused);
    }

    public float getChatScale() {
        return this.mc.gameSettings.chatScale;
    }

    public static int calculateChatboxWidth(float scale) {
        int i = 320;
        int j = 40;
        return MathHelper.floor(scale * 280.0f + 40.0f);
    }

    public static int calculateChatboxHeight(float scale) {
        int i = 180;
        int j = 20;
        return MathHelper.floor(scale * 160.0f + 20.0f);
    }

    public int getLineCount() {
        return this.getChatHeight() / 9;
    }
}


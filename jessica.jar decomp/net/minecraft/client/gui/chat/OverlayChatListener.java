/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.IChatListener;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;

public class OverlayChatListener
implements IChatListener {
    private final Minecraft field_192577_a;

    public OverlayChatListener(Minecraft p_i47394_1_) {
        this.field_192577_a = p_i47394_1_;
    }

    @Override
    public void func_192576_a(ChatType p_192576_1_, ITextComponent p_192576_2_) {
        this.field_192577_a.ingameGUI.setRecordPlaying(p_192576_2_, false);
    }
}


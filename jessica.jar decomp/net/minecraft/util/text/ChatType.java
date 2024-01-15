/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.text;

public enum ChatType {
    CHAT(0),
    SYSTEM(1),
    GAME_INFO(2);

    private final byte field_192588_d;

    private ChatType(byte p_i47429_3_) {
        this.field_192588_d = p_i47429_3_;
    }

    public byte func_192583_a() {
        return this.field_192588_d;
    }

    public static ChatType func_192582_a(byte p_192582_0_) {
        ChatType[] chatTypeArray = ChatType.values();
        int n = chatTypeArray.length;
        int n2 = 0;
        while (n2 < n) {
            ChatType chattype = chatTypeArray[n2];
            if (p_192582_0_ == chattype.field_192588_d) {
                return chattype;
            }
            ++n2;
        }
        return CHAT;
    }
}


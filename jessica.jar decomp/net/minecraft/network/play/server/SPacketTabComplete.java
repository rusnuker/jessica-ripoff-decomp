/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketTabComplete
implements Packet<INetHandlerPlayClient> {
    private String[] matches;

    public SPacketTabComplete() {
    }

    public SPacketTabComplete(String[] matchesIn) {
        this.matches = matchesIn;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.matches = new String[buf.readVarIntFromBuffer()];
        int i = 0;
        while (i < this.matches.length) {
            this.matches[i] = buf.readStringFromBuffer(Short.MAX_VALUE);
            ++i;
        }
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(this.matches.length);
        String[] stringArray = this.matches;
        int n = this.matches.length;
        int n2 = 0;
        while (n2 < n) {
            String s = stringArray[n2];
            buf.writeString(s);
            ++n2;
        }
    }

    @Override
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleTabComplete(this);
    }

    public String[] getMatches() {
        return this.matches;
    }
}


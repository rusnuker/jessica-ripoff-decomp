/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketDestroyEntities
implements Packet<INetHandlerPlayClient> {
    private int[] entityIDs;

    public SPacketDestroyEntities() {
    }

    public SPacketDestroyEntities(int ... entityIdsIn) {
        this.entityIDs = entityIdsIn;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityIDs = new int[buf.readVarIntFromBuffer()];
        int i = 0;
        while (i < this.entityIDs.length) {
            this.entityIDs[i] = buf.readVarIntFromBuffer();
            ++i;
        }
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(this.entityIDs.length);
        int[] nArray = this.entityIDs;
        int n = this.entityIDs.length;
        int n2 = 0;
        while (n2 < n) {
            int i = nArray[n2];
            buf.writeVarIntToBuffer(i);
            ++n2;
        }
    }

    @Override
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleDestroyEntities(this);
    }

    public int[] getEntityIDs() {
        return this.entityIDs;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.World;

public class SPacketEntityStatus
implements Packet<INetHandlerPlayClient> {
    private int entityId;
    private byte logicOpcode;

    public SPacketEntityStatus() {
    }

    public SPacketEntityStatus(Entity entityIn, byte opcodeIn) {
        this.entityId = entityIn.getEntityId();
        this.logicOpcode = opcodeIn;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityId = buf.readInt();
        this.logicOpcode = buf.readByte();
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeInt(this.entityId);
        buf.writeByte(this.logicOpcode);
    }

    @Override
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleEntityStatus(this);
    }

    public Entity getEntity(World worldIn) {
        return worldIn.getEntityByID(this.entityId);
    }

    public byte getOpCode() {
        return this.logicOpcode;
    }
}


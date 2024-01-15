/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.handshake.client;

import com.mysql.fabric.Wrapper;
import java.io.IOException;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;
import net.minecraft.util.datafix.HackPack;

public class C00Handshake
implements Packet<INetHandlerHandshakeServer> {
    private int protocolVersion;
    private String ip;
    private int port;
    private EnumConnectionState requestedState;

    public C00Handshake() {
    }

    public C00Handshake(String p_i47613_1_, int p_i47613_2_, EnumConnectionState p_i47613_3_) {
        this.protocolVersion = 340;
        Wrapper.getHackPack();
        if (!HackPack.getFakeIp().isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder(String.valueOf(p_i47613_1_)).append("\u0000");
            Wrapper.getHackPack();
            StringBuilder stringBuilder2 = stringBuilder.append(HackPack.getFakeIp()).append("\u0000");
            Wrapper.getHackPack();
            this.ip = stringBuilder2.append(HackPack.getFakeUUID().replace("-", "")).toString();
        } else {
            this.ip = p_i47613_1_;
        }
        this.port = p_i47613_2_;
        this.requestedState = p_i47613_3_;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.protocolVersion = buf.readVarIntFromBuffer();
        this.ip = buf.readStringFromBuffer(255);
        this.port = buf.readUnsignedShort();
        this.requestedState = EnumConnectionState.getById(buf.readVarIntFromBuffer());
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(this.protocolVersion);
        buf.writeString(this.ip);
        buf.writeShort(this.port);
        buf.writeVarIntToBuffer(this.requestedState.getId());
    }

    @Override
    public void processPacket(INetHandlerHandshakeServer handler) {
        handler.processHandshake(this);
    }

    public EnumConnectionState getRequestedState() {
        return this.requestedState;
    }

    public int getProtocolVersion() {
        return this.protocolVersion;
    }
}


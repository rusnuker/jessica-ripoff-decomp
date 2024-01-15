/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.network.play.server;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;

public class SPacketStatistics
implements Packet<INetHandlerPlayClient> {
    private Map<StatBase, Integer> statisticMap;

    public SPacketStatistics() {
    }

    public SPacketStatistics(Map<StatBase, Integer> statisticMapIn) {
        this.statisticMap = statisticMapIn;
    }

    @Override
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleStatistics(this);
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        int i = buf.readVarIntFromBuffer();
        this.statisticMap = Maps.newHashMap();
        int j = 0;
        while (j < i) {
            StatBase statbase = StatList.getOneShotStat(buf.readStringFromBuffer(Short.MAX_VALUE));
            int k = buf.readVarIntFromBuffer();
            if (statbase != null) {
                this.statisticMap.put(statbase, k);
            }
            ++j;
        }
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(this.statisticMap.size());
        for (Map.Entry<StatBase, Integer> entry : this.statisticMap.entrySet()) {
            buf.writeString(entry.getKey().statId);
            buf.writeVarIntToBuffer(entry.getValue());
        }
    }

    public Map<StatBase, Integer> getStatisticMap() {
        return this.statisticMap;
    }
}


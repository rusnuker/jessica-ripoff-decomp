/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.init;

import com.mysql.fabric.Category;
import com.mysql.fabric.Module;
import com.mysql.fabric.Wrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.Packet;

public class Blink_One_Million_Dollars
extends Module {
    public static List<Packet> packets = new ArrayList<Packet>();

    public Blink_One_Million_Dollars() {
        super("Blink", Category.Player);
    }

    @Override
    public void onDisable() {
        for (Packet packet : packets) {
            Wrapper.sendPacketBypass(packet);
        }
        packets.clear();
    }
}


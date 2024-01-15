/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.io.FileUtils
 *  org.lwjgl.opengl.Display
 */
package com.mysql.fabric;

import com.mysql.fabric.ChatCommands;
import com.mysql.fabric.Module;
import com.mysql.jdbc.jdbc2.optional.GuiManager_Nirvana;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.AAC_One_Million_Dollars;
import net.minecraft.init.AirJump_One_Million_Dollars;
import net.minecraft.init.AntiKnockback_One_Million_Dollars;
import net.minecraft.init.AutoBlock_One_Million_Dollars;
import net.minecraft.init.BedExploit_One_Million_Dollars;
import net.minecraft.init.Blink_One_Million_Dollars;
import net.minecraft.init.BunnyHop_One_Million_Dollars;
import net.minecraft.init.ChestStealer_One_Million_Dollars;
import net.minecraft.init.FakeCreative_One_Million_Dollars;
import net.minecraft.init.Glide_One_Million_Dollars;
import net.minecraft.init.HardCombat_One_Million_Dollars;
import net.minecraft.init.HasteEffect_One_Million_Dollars;
import net.minecraft.init.JumpEffect2_One_Million_Dollars;
import net.minecraft.init.JumpEffect3_One_Million_Dollars;
import net.minecraft.init.JumpEffect_One_Million_Dollars;
import net.minecraft.init.Killaura18_One_Million_Dollars;
import net.minecraft.init.LongJump_One_Million_Dollars;
import net.minecraft.init.NCP_One_Million_Dollars;
import net.minecraft.init.NoSlowdown_One_Million_Dollars;
import net.minecraft.init.PlayerESP_One_Million_Dollars;
import net.minecraft.init.Reach_One_Million_Dollars;
import net.minecraft.init.ScaffoldAAC_One_Million_Dollars;
import net.minecraft.init.Spartan_One_Million_Dollars;
import net.minecraft.init.Sprint_One_Million_Dollars;
import net.minecraft.network.Packet;
import net.minecraft.util.datafix.Binds;
import net.minecraft.util.datafix.FileManager;
import net.minecraft.util.datafix.FriendManager;
import net.minecraft.util.datafix.HackPack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.model.MoveEvent;
import org.apache.commons.io.FileUtils;
import org.darkstorm.minecraft.gui.theme.simple.SimpleTheme;
import org.lwjgl.opengl.Display;

public class Wrapper {
    private static Binds binds;
    private static HackPack hackpack;
    private static GuiManager_Nirvana guiManager;
    private static FileManager filemanager;
    private static ChatCommands chathandler;
    private static FriendManager friendmanager;
    public static Wrapper instance;
    private static TreeMap<String, Module> modules;
    private static String version;
    private static String clientname;

    static {
        version = "1.12.2";
        clientname = "MrBeast";
    }

    public Wrapper() {
        binds = new Binds();
        hackpack = new HackPack();
        filemanager = new FileManager();
        chathandler = new ChatCommands();
        modules = new TreeMap();
        friendmanager = new FriendManager();
        Wrapper.addMod(new NCP_One_Million_Dollars());
        Wrapper.addMod(new AAC_One_Million_Dollars());
        Wrapper.addMod(new Spartan_One_Million_Dollars());
        Wrapper.addMod(new HardCombat_One_Million_Dollars());
        Wrapper.addMod(new Killaura18_One_Million_Dollars());
        Wrapper.addMod(new Reach_One_Million_Dollars());
        Wrapper.addMod(new AutoBlock_One_Million_Dollars());
        Wrapper.addMod(new AntiKnockback_One_Million_Dollars());
        Wrapper.addMod(new ChestStealer_One_Million_Dollars());
        Wrapper.addMod(new ScaffoldAAC_One_Million_Dollars());
        Wrapper.addMod(new Sprint_One_Million_Dollars());
        Wrapper.addMod(new BunnyHop_One_Million_Dollars());
        Wrapper.addMod(new Glide_One_Million_Dollars());
        Wrapper.addMod(new NoSlowdown_One_Million_Dollars());
        Wrapper.addMod(new Blink_One_Million_Dollars());
        Wrapper.addMod(new AirJump_One_Million_Dollars());
        Wrapper.addMod(new LongJump_One_Million_Dollars());
        Wrapper.addMod(new HasteEffect_One_Million_Dollars());
        Wrapper.addMod(new JumpEffect_One_Million_Dollars());
        Wrapper.addMod(new JumpEffect2_One_Million_Dollars());
        Wrapper.addMod(new JumpEffect3_One_Million_Dollars());
        Wrapper.addMod(new FakeCreative_One_Million_Dollars());
        Wrapper.addMod(new BedExploit_One_Million_Dollars());
        Wrapper.addMod(new PlayerESP_One_Million_Dollars());
        try {
            filemanager.init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        File bbutton = new File(Wrapper.mc().mcDataDir, "MrBeast");
        if (!bbutton.exists()) {
            URL source = this.getClass().getResource("/assets/minecraft/jessica/button.png");
            File dest = new File(Wrapper.mc().mcDataDir, "MrBeast");
            try {
                FileUtils.copyURLToFile((URL)source, (File)dest);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        guiManager = new GuiManager_Nirvana();
        guiManager.setTheme(new SimpleTheme());
        guiManager.setup();
        Display.setTitle((String)(String.valueOf(Wrapper.getClientName()) + " " + Wrapper.getVesrion()));
    }

    public static Binds getBinds() {
        return binds;
    }

    public static Minecraft mc() {
        return Minecraft.getMinecraft();
    }

    public static WorldClient world() {
        return Wrapper.mc().world;
    }

    public static String getVesrion() {
        return version;
    }

    public static String currentDate() {
        return new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
    }

    public static HackPack getHackPack() {
        return hackpack;
    }

    public static FileManager getFiles() {
        return filemanager;
    }

    public static String getClientName() {
        return clientname;
    }

    public static EntityPlayerSP player() {
        return Wrapper.mc().player;
    }

    public static void sendPacket(Packet p) {
        Wrapper.player().connection.sendPacket(p);
    }

    public static void sendPacketBypass(Packet p) {
        Wrapper.player().connection.sendPacket(p);
    }

    public static GuiManager_Nirvana getGuiManager() {
        return guiManager;
    }

    public static FriendManager getFriends() {
        return friendmanager;
    }

    public static ChatCommands getChatHandler() {
        return chathandler;
    }

    public static void msg(String s, boolean prefix) {
        s = String.valueOf(prefix ? "&f&l[&4&l" + Wrapper.getClientName() + "&f&l] &r" : "") + s;
        Wrapper.player().addChatMessage(new TextComponentTranslation(s.replace("&", "\u00a7"), new Object[0]));
    }

    private static void addMod(Module m) {
        if (modules.get(m.getAlias()) == null) {
            modules.put(m.getAlias(), m);
        }
    }

    public static Module getModule(String alias) {
        return Wrapper.getModules().get(alias.toLowerCase().replace(" ", ""));
    }

    public static TreeMap<String, Module> getModules() {
        return modules;
    }

    public static void onUpdate() {
        for (Module m : Wrapper.getModules().values()) {
            if (!m.isToggled()) continue;
            m.onUpdate();
        }
    }

    public static void onRender(float partialTicks) {
        for (Module m : Wrapper.getModules().values()) {
            if (!m.isToggled()) continue;
            m.onRender(partialTicks);
        }
    }

    public static void onGetPacket(Packet<?> packet) {
        for (Module m : Wrapper.getModules().values()) {
            if (!m.isToggled()) continue;
            m.onGetPacket(packet);
        }
    }

    public static void onMotion(MoveEvent e) {
        for (Module m : Wrapper.getModules().values()) {
            if (!m.isToggled()) continue;
            m.onMotion(e);
        }
    }
}


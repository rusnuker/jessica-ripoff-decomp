/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ArrayListMultimap
 *  com.google.common.collect.Multimap
 *  org.lwjgl.input.Keyboard
 */
package net.minecraft.util.datafix;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mysql.fabric.UIRenderer;
import com.mysql.fabric.Wrapper;
import org.darkstorm.minecraft.gui.util.GuiManagerDisplayScreen;
import org.lwjgl.input.Keyboard;

public class Binds {
    private static boolean[] keyStates = new boolean[256];
    public static Multimap<Integer, String> binds = ArrayListMultimap.create();

    public static void makeBinds() {
        for (Integer key : binds.keySet()) {
            if (!Binds.checkKey(key)) continue;
            for (String s : binds.get((Object)key)) {
                Wrapper.player().sendChatMessage(s);
                try {
                    Thread.sleep(15L);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (Binds.checkKey(54) && !(Wrapper.mc().currentScreen instanceof GuiManagerDisplayScreen)) {
            Wrapper.mc().displayGuiScreen(new GuiManagerDisplayScreen(Wrapper.getGuiManager()));
            UIRenderer.renderAndUpdateFrames();
        }
    }

    private static int getKey(String key) {
        return Keyboard.getKeyIndex((String)key);
    }

    private static boolean checkKey(int i) {
        if (Wrapper.mc().currentScreen != null) {
            return false;
        }
        if (Keyboard.isKeyDown((int)i) != keyStates[i]) {
            Binds.keyStates[i] = !keyStates[i];
            return Binds.keyStates[i];
        }
        return false;
    }

    public static void addBind(String key, String msg) {
        binds.put((Object)Binds.getKey(key.toUpperCase()), (Object)msg);
        Wrapper.msg("&aMessage \"" + msg + "\" binded on key " + key.toUpperCase() + ".", true);
    }

    public static void addBindSave(String key, String msg) {
        binds.put((Object)Binds.getKey(key.toUpperCase()), (Object)msg);
    }

    public static void delBind(String key) {
        if (!binds.containsKey((Object)Binds.getKey(key.toUpperCase()))) {
            Wrapper.msg("&cThis key not contains messages.", true);
            return;
        }
        binds.removeAll((Object)Binds.getKey(key.toUpperCase()));
        Wrapper.msg("&aDeleted messages from key " + key.toUpperCase() + ".", true);
    }
}


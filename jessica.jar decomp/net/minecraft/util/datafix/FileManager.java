/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  org.lwjgl.input.Keyboard
 */
package net.minecraft.util.datafix;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mysql.fabric.ChatCommands;
import com.mysql.fabric.Module;
import com.mysql.fabric.Wrapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.command.KillauraSettings;
import net.minecraft.init.NoSlowdown_One_Million_Dollars;
import net.minecraft.init.Reach_One_Million_Dollars;
import net.minecraft.util.datafix.Binds;
import net.minecraft.util.datafix.FriendManager;
import net.minecraft.util.datafix.JsonUtils;
import org.lwjgl.input.Keyboard;

public class FileManager {
    public File dir;
    public File modules;
    public File binds;
    public File friends;
    public File values;

    public FileManager() {
        this.dir = new File(Wrapper.mc().mcDataDir, "MrBeast");
        this.modules = new File(this.dir, "Modules.json");
        this.binds = new File(this.dir, "Binds.json");
        this.friends = new File(this.dir, "Friends.json");
        this.values = new File(this.dir, "Values.json");
        if (!this.dir.exists()) {
            this.dir.mkdir();
        }
    }

    public void init() throws Exception {
        this.loadModules();
        this.saveModules();
        this.loadBinds();
        this.saveBinds();
        this.loadFriends();
        this.saveFriends();
        this.loadValues();
        this.saveValues();
    }

    public void saveModules() {
        try {
            JsonObject json = new JsonObject();
            for (Module mod : Wrapper.getModules().values()) {
                JsonObject jsonMod = new JsonObject();
                jsonMod.addProperty("Toggled", Boolean.valueOf(mod.isToggled()));
                json.add(mod.getName(), (JsonElement)jsonMod);
            }
            PrintWriter save = new PrintWriter(new FileWriter(this.modules));
            save.println(JsonUtils.prettyGson.toJson((JsonElement)json));
            save.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void loadModules() {
        try {
            if (!this.modules.exists()) {
                this.modules.createNewFile();
            }
            BufferedReader load = new BufferedReader(new FileReader(this.modules));
            JsonObject json = (JsonObject)JsonUtils.jsonParser.parse((Reader)load);
            load.close();
            for (Map.Entry entry : json.entrySet()) {
                Module module = Wrapper.getModule((String)entry.getKey());
                if (module == null) continue;
                JsonObject jsonModule = (JsonObject)entry.getValue();
                boolean enabled = jsonModule.get("Toggled").getAsBoolean();
                module.setToggled(enabled);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void saveBinds() {
        try {
            JsonObject json = new JsonObject();
            Wrapper.getBinds();
            for (Integer key : Binds.binds.keySet()) {
                JsonObject jsonMod = new JsonObject();
                Wrapper.getBinds();
                for (String s : Binds.binds.get((Object)key)) {
                    jsonMod.addProperty(String.valueOf(new Random().nextInt(99999999)), s);
                    json.add(key.toString(), (JsonElement)jsonMod);
                }
            }
            PrintWriter save = new PrintWriter(new FileWriter(this.binds));
            save.println(JsonUtils.prettyGson.toJson((JsonElement)json));
            save.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadBinds() {
        try {
            if (!this.binds.exists()) {
                this.binds.createNewFile();
            }
            BufferedReader load = new BufferedReader(new FileReader(this.binds));
            JsonObject json = (JsonObject)JsonUtils.jsonParser.parse((Reader)load);
            load.close();
            for (Map.Entry entry : json.entrySet()) {
                JsonObject jsonModule = (JsonObject)entry.getValue();
                Set entrySet = jsonModule.entrySet();
                for (Map.Entry s : entrySet) {
                    JsonElement jsonModule2 = (JsonElement)s.getValue();
                    String message = jsonModule2.getAsString();
                    Wrapper.getBinds();
                    Binds.addBindSave(Keyboard.getKeyName((int)Integer.parseInt((String)entry.getKey())), message);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFriends() {
        try {
            BufferedReader load;
            ArrayList friends;
            if (!this.friends.exists()) {
                this.friends.createNewFile();
            }
            if ((friends = (ArrayList)JsonUtils.gson.fromJson((Reader)(load = new BufferedReader(new FileReader(this.friends))), ArrayList.class)) != null) {
                FriendManager.setFriends(friends);
            }
            load.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void saveFriends() {
        try {
            PrintWriter save = new PrintWriter(new FileWriter(this.friends));
            save.println(JsonUtils.prettyGson.toJson(FriendManager.getFriends()));
            save.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void write(File file, String text) {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            } else if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file, true);
            fw.write(String.valueOf(text) + System.getProperty("line.separator"));
            fw.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void saveValues() {
        try {
            JsonObject jsonMod = new JsonObject();
            jsonMod.addProperty("Killaura Players", Boolean.valueOf(KillauraSettings.players));
            jsonMod.addProperty("Killaura Mobs", Boolean.valueOf(KillauraSettings.mobs));
            jsonMod.addProperty("Killaura Team", Boolean.valueOf(KillauraSettings.team));
            jsonMod.addProperty("Killaura Range", (Number)KillauraSettings.range);
            jsonMod.addProperty("Killaura Speed", (Number)Float.valueOf(KillauraSettings.KillauraSpeed));
            jsonMod.addProperty("Killaura FOV", (Number)KillauraSettings.FOV);
            jsonMod.addProperty("Killaura Yaw", (Number)Float.valueOf(KillauraSettings.yaw));
            jsonMod.addProperty("Killaura Pitch", (Number)Float.valueOf(KillauraSettings.pitch));
            jsonMod.addProperty("Reach Value", (Number)Reach_One_Million_Dollars.reach);
            jsonMod.addProperty("NoSlowdown Value", (Number)Float.valueOf(NoSlowdown_One_Million_Dollars.value));
            jsonMod.addProperty("DelayList Value", (Number)ChatCommands.delayList);
            jsonMod.addProperty("FileList Value", ChatCommands.fileList);
            PrintWriter save = new PrintWriter(new FileWriter(this.values));
            save.println(JsonUtils.prettyGson.toJson((JsonElement)jsonMod));
            save.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadValues() {
        try {
            if (!this.values.exists()) {
                this.values.createNewFile();
            }
            BufferedReader load = new BufferedReader(new FileReader(this.values));
            JsonObject jsonModule = (JsonObject)JsonUtils.jsonParser.parse((Reader)load);
            KillauraSettings.players = jsonModule.get("Killaura Players").getAsBoolean();
            KillauraSettings.mobs = jsonModule.get("Killaura Mobs").getAsBoolean();
            KillauraSettings.throughWalls = jsonModule.get("Killaura Team").getAsBoolean();
            KillauraSettings.range = jsonModule.get("Killaura Range").getAsDouble();
            KillauraSettings.KillauraSpeed = jsonModule.get("Killaura Speed").getAsFloat();
            KillauraSettings.FOV = jsonModule.get("Killaura FOV").getAsDouble();
            KillauraSettings.yaw = jsonModule.get("Killaura Yaw").getAsFloat();
            KillauraSettings.pitch = jsonModule.get("Killaura Pitch").getAsFloat();
            Reach_One_Million_Dollars.reach = jsonModule.get("Reach Value").getAsDouble();
            NoSlowdown_One_Million_Dollars.value = jsonModule.get("NoSlowdown Value").getAsFloat();
            ChatCommands.delayList = jsonModule.get("DelayList Value").getAsInt();
            ChatCommands.fileList = jsonModule.get("FileList Value").getAsString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getClientDir() {
        String dir = Wrapper.mc().mcDataDir + "MrBeast";
        File filedir = new File(dir);
        if (!filedir.exists()) {
            filedir.mkdirs();
        }
        return dir;
    }
}


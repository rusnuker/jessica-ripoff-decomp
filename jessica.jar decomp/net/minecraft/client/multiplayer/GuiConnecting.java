/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package net.minecraft.client.multiplayer;

import com.mysql.jdbc.jdbc2.optional.GuiProxy_Nirvana;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.CPacketLoginStart;
import net.minecraft.util.datafix.ProxyServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiConnecting
extends GuiScreen {
    private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);
    private static final Logger LOGGER = LogManager.getLogger();
    public static NetworkManager networkManager2;
    private NetworkManager networkManager;
    private boolean cancel;
    private final GuiScreen previousGuiScreen;

    public GuiConnecting(GuiScreen parent, Minecraft mcIn, ServerData serverDataIn) {
        this.mc = mcIn;
        this.previousGuiScreen = parent;
        ServerAddress serveraddress = ServerAddress.fromString(serverDataIn.serverIP);
        mcIn.loadWorld(null);
        mcIn.setServerData(serverDataIn);
        this.connect(serveraddress.getIP(), serveraddress.getPort());
    }

    public GuiConnecting(GuiScreen parent, Minecraft mcIn, String hostName, int port) {
        this.mc = mcIn;
        this.previousGuiScreen = parent;
        mcIn.loadWorld(null);
        this.connect(hostName, port);
    }

    private void connect(String ip2, int port2) {
        final String ip3 = ip2;
        final int port3 = port2;
        if (!GuiProxy_Nirvana.strIpPort.equals("")) {
            try {
                if (ProxyServer.client != null && ProxyServer.server != null) {
                    ProxyServer.client.close();
                    ProxyServer.server.close();
                    ProxyServer.ss.close();
                    ProxyServer.client = null;
                    ProxyServer.server = null;
                    ProxyServer.ss = null;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            new Thread(){

                @Override
                public void run() {
                    try {
                        ProxyServer.proxyServer(ip3, port3);
                    }
                    catch (Exception e) {
                        try {
                            ProxyServer.client.close();
                            ProxyServer.server.close();
                            ProxyServer.ss.close();
                            ProxyServer.client = null;
                            ProxyServer.server = null;
                            ProxyServer.ss = null;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
            }.start();
            ip2 = "127.0.0.1";
            port2 = 61245;
        }
        final String ip = ip2;
        final int port = port2;
        LOGGER.info("Connecting to {}, {}", (Object)ip, (Object)port);
        new Thread("Server Connector #" + CONNECTION_ID.incrementAndGet()){

            @Override
            public void run() {
                InetAddress inetaddress = null;
                try {
                    if (GuiConnecting.this.cancel) {
                        return;
                    }
                    inetaddress = InetAddress.getByName(ip);
                    GuiConnecting.this.networkManager = NetworkManager.createNetworkManagerAndConnect(inetaddress, port, ((GuiConnecting)GuiConnecting.this).mc.gameSettings.isUsingNativeTransport());
                    GuiConnecting.this.networkManager.setNetHandler(new NetHandlerLoginClient(GuiConnecting.this.networkManager, GuiConnecting.this.mc, GuiConnecting.this.previousGuiScreen));
                    GuiConnecting.this.networkManager.sendPacket(new C00Handshake(ip3, port3, EnumConnectionState.LOGIN));
                    GuiConnecting.this.networkManager.sendPacket(new CPacketLoginStart(GuiConnecting.this.mc.getSession().getProfile()));
                }
                catch (UnknownHostException unknownhostexception) {
                    if (GuiConnecting.this.cancel) {
                        return;
                    }
                    LOGGER.error("Couldn't connect to server", (Throwable)unknownhostexception);
                    GuiConnecting.this.mc.displayGuiScreen(new GuiDisconnected(GuiConnecting.this.previousGuiScreen, "connect.failed", new TextComponentTranslation("disconnect.genericReason", "Unknown host")));
                }
                catch (Exception exception) {
                    if (GuiConnecting.this.cancel) {
                        return;
                    }
                    LOGGER.error("Couldn't connect to server", (Throwable)exception);
                    String s = exception.toString();
                    if (inetaddress != null) {
                        String s1 = inetaddress + ":" + port;
                        s = s.replaceAll(s1, "");
                    }
                    GuiConnecting.this.mc.displayGuiScreen(new GuiDisconnected(GuiConnecting.this.previousGuiScreen, "connect.failed", new TextComponentTranslation("disconnect.genericReason", s)));
                }
            }
        }.start();
    }

    @Override
    public void updateScreen() {
        if (this.networkManager != null) {
            if (this.networkManager.isChannelOpen()) {
                this.networkManager.processReceivedPackets();
            } else {
                this.networkManager.checkDisconnected();
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120 + 12, I18n.format("gui.cancel", new Object[0])));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.cancel = true;
            if (this.networkManager != null) {
                this.networkManager.closeChannel(new TextComponentString("Aborted"));
            }
            this.mc.displayGuiScreen(this.previousGuiScreen);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        if (this.networkManager == null) {
            this.drawCenteredString(this.fontRendererObj, I18n.format("connect.connecting", new Object[0]), this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        } else {
            this.drawCenteredString(this.fontRendererObj, I18n.format("connect.authorizing", new Object[0]), this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}


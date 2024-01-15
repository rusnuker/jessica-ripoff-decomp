/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.fabric.Module;
import com.mysql.fabric.UIRenderer;
import com.mysql.fabric.Wrapper;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.Binds;
import org.darkstorm.minecraft.gui.font.UnicodeFontRenderer;
import org.lwjgl.opengl.GL11;

public class GuiIngameHook_Nirvana
extends GuiIngame {
    private ResourceLocation jsLogo = new ResourceLocation("com/mysql/fabric/jssc.png");
    private FontRenderer fontRenderer = new UnicodeFontRenderer(new Font("Trebuchet MS", 1, 19));
    public static int responseTime = 0;
    private int color = 0;
    private boolean colorBool = false;

    public GuiIngameHook_Nirvana(Minecraft mcIn) {
        super(mcIn);
    }

    @Override
    public void renderGameOverlay(float partialTicks) {
        super.renderGameOverlay(partialTicks);
        int count = 0;
        ArrayList<Module> enable = new ArrayList<Module>();
        for (Module m : Wrapper.getModules().values()) {
            if (!m.isToggled()) continue;
            enable.add(m);
        }
        Comparator<Module> cp = new Comparator<Module>(this){
            final /* synthetic */ GuiIngameHook_Nirvana this$0;
            {
                this.this$0 = guiIngameHook_Nirvana;
            }

            public int compare(Module b1, Module b2) {
                return Integer.compare(GuiIngameHook_Nirvana.access$0(this.this$0).getStringWidth(b1.getName()), GuiIngameHook_Nirvana.access$0(this.this$0).getStringWidth(b2.getName()));
            }
        };
        enable.sort(cp.reversed());
        try {
            this.fontRenderer.drawString(String.valueOf(Wrapper.getClientName()) + " " + Wrapper.getVesrion(), 2, 2, new Color(210, 0, 0).getRGB());
        }
        catch (Exception exception) {
            // empty catch block
        }
        for (Module m : enable) {
            int j = this.fontRenderer.FONT_HEIGHT;
            int k = this.fontRenderer.getStringWidth(m.getName());
            int l = new ScaledResolution(Wrapper.mc()).getScaledWidth() - 2 - k;
            int i1 = 2 + j * count;
            try {
                this.fontRenderer.drawString(m.getName(), l, i1, new Color(210, 20, 20).getRGB());
            }
            catch (Exception exception) {
                // empty catch block
            }
            ++count;
        }
        GL11.glPushMatrix();
        GL11.glTranslatef((float)50.0f, (float)50.0f, (float)0.0f);
        this.drawGradientRect(-48, -48, -48, -48, -16777216, Integer.MIN_VALUE);
        GL11.glPopMatrix();
        ScaledResolution scaledresolution = new ScaledResolution(Wrapper.mc());
        Wrapper.mc().entityRenderer.setupOverlayRendering();
        GlStateManager.enableBlend();
        Wrapper.getBinds();
        Binds.makeBinds();
        UIRenderer.renderAndUpdateFrames();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.jsLogo);
        int x = 1;
        int y = 2;
        int w = 0;
        int h = 0;
        float fw = 0.0f;
        float fh = 0.0f;
        float u = 0.0f;
        float v = 0.0f;
        Gui.drawModalRectWithCustomSizedTexture(x, y, u, v, w, h, fw, fh);
    }

    static /* synthetic */ FontRenderer access$0(GuiIngameHook_Nirvana guiIngameHook_Nirvana) {
        return guiIngameHook_Nirvana.fontRenderer;
    }
}


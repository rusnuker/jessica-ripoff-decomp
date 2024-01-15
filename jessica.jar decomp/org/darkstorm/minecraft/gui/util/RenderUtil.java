/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Mouse
 *  org.lwjgl.opengl.GL11
 */
package org.darkstorm.minecraft.gui.util;

import java.awt.Color;
import java.awt.Point;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class RenderUtil {
    public static void scissorBox(int x, int y, int xend, int yend) {
        int width = xend - x;
        int height = yend - y;
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int factor = sr.getScaleFactor();
        int bottomY = Minecraft.getMinecraft().currentScreen.height - yend;
        GL11.glScissor((int)(x * factor), (int)(bottomY * factor), (int)(width * factor), (int)(height * factor));
    }

    public static void setupLineSmooth() {
        GL11.glEnable((int)3042);
        GL11.glDisable((int)2896);
        GL11.glDisable((int)2929);
        GL11.glEnable((int)2848);
        GL11.glDisable((int)3553);
        GL11.glHint((int)3154, (int)4354);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)32925);
        GL11.glEnable((int)32926);
        GL11.glShadeModel((int)7425);
    }

    public static void drawLine(double startX, double startY, double startZ, double endX, double endY, double endZ, float thickness) {
        GL11.glPushMatrix();
        RenderUtil.setupLineSmooth();
        GL11.glLineWidth((float)thickness);
        GL11.glBegin((int)1);
        GL11.glVertex3d((double)startX, (double)startY, (double)startZ);
        GL11.glVertex3d((double)endX, (double)endY, (double)endZ);
        GL11.glEnd();
        GL11.glDisable((int)3042);
        GL11.glEnable((int)3553);
        GL11.glDisable((int)2848);
        GL11.glEnable((int)2896);
        GL11.glEnable((int)2929);
        GL11.glDisable((int)32925);
        GL11.glDisable((int)32926);
        GL11.glPopMatrix();
    }

    public static void drawTexturedModalRect(int textureId, int posX, int posY, int width, int height) {
        double halfHeight = height / 2;
        double halfWidth = width / 2;
        GL11.glDisable((int)2884);
        GL11.glBindTexture((int)3553, (int)textureId);
        GL11.glPushMatrix();
        GL11.glTranslated((double)((double)posX + halfWidth), (double)((double)posY + halfHeight), (double)0.0);
        GL11.glScalef((float)width, (float)height, (float)0.0f);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glBegin((int)4);
        GL11.glNormal3f((float)0.0f, (float)0.0f, (float)1.0f);
        GL11.glTexCoord2f((float)1.0f, (float)1.0f);
        GL11.glVertex2d((double)1.0, (double)1.0);
        GL11.glTexCoord2f((float)0.0f, (float)1.0f);
        GL11.glVertex2d((double)-1.0, (double)1.0);
        GL11.glTexCoord2f((float)0.0f, (float)0.0f);
        GL11.glVertex2d((double)-1.0, (double)-1.0);
        GL11.glTexCoord2f((float)0.0f, (float)0.0f);
        GL11.glVertex2d((double)-1.0, (double)-1.0);
        GL11.glTexCoord2f((float)1.0f, (float)0.0f);
        GL11.glVertex2d((double)1.0, (double)-1.0);
        GL11.glTexCoord2f((float)1.0f, (float)1.0f);
        GL11.glVertex2d((double)1.0, (double)1.0);
        GL11.glEnd();
        GL11.glDisable((int)3042);
        GL11.glBindTexture((int)3553, (int)0);
        GL11.glPopMatrix();
    }

    public static int interpolateColor(int rgba1, int rgba2, float percent) {
        int r1 = rgba1 & 0xFF;
        int g1 = rgba1 >> 8 & 0xFF;
        int b1 = rgba1 >> 16 & 0xFF;
        int a1 = rgba1 >> 24 & 0xFF;
        int r2 = rgba2 & 0xFF;
        int g2 = rgba2 >> 8 & 0xFF;
        int b2 = rgba2 >> 16 & 0xFF;
        int a2 = rgba2 >> 24 & 0xFF;
        int r = (int)(r1 < r2 ? (float)r1 + (float)(r2 - r1) * percent : (float)r2 + (float)(r1 - r2) * percent);
        int g = (int)(g1 < g2 ? (float)g1 + (float)(g2 - g1) * percent : (float)g2 + (float)(g1 - g2) * percent);
        int b = (int)(b1 < b2 ? (float)b1 + (float)(b2 - b1) * percent : (float)b2 + (float)(b1 - b2) * percent);
        int a = (int)(a1 < a2 ? (float)a1 + (float)(a2 - a1) * percent : (float)a2 + (float)(a1 - a2) * percent);
        return r | g << 8 | b << 16 | a << 24;
    }

    public static void setColor(Color c) {
        GL11.glColor4f((float)((float)c.getRed() / 255.0f), (float)((float)c.getGreen() / 255.0f), (float)((float)c.getBlue() / 255.0f), (float)((float)c.getAlpha() / 255.0f));
    }

    public static Color toColor(int rgba) {
        int r = rgba & 0xFF;
        int g = rgba >> 8 & 0xFF;
        int b = rgba >> 16 & 0xFF;
        int a = rgba >> 24 & 0xFF;
        return new Color(r, g, b, a);
    }

    public static int toRGBA(Color c) {
        return c.getRed() | c.getGreen() << 8 | c.getBlue() << 16 | c.getAlpha() << 24;
    }

    public static void setColor(int rgba) {
        int r = rgba & 0xFF;
        int g = rgba >> 8 & 0xFF;
        int b = rgba >> 16 & 0xFF;
        int a = rgba >> 24 & 0xFF;
        GL11.glColor4b((byte)((byte)r), (byte)((byte)g), (byte)((byte)b), (byte)((byte)a));
    }

    public static Point calculateMouseLocation() {
        Minecraft minecraft = Minecraft.getMinecraft();
        int scale = minecraft.gameSettings.guiScale;
        if (scale == 0) {
            scale = 1000;
        }
        int scaleFactor = 0;
        while (scaleFactor < scale && minecraft.displayWidth / (scaleFactor + 1) >= 320 && minecraft.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        return new Point(Mouse.getX() / scaleFactor, minecraft.displayHeight / scaleFactor - Mouse.getY() / scaleFactor - 1);
    }
}


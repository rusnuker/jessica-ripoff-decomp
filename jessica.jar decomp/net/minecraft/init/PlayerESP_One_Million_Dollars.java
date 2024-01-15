/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
package net.minecraft.init;

import com.mysql.fabric.Category;
import com.mysql.fabric.Module;
import com.mysql.fabric.Wrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.datafix.FriendManager;
import net.minecraft.util.datafix.RenderUtils;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

public class PlayerESP_One_Million_Dollars
extends Module {
    private int playerBox;

    public PlayerESP_One_Million_Dollars() {
        super("PlayerESP", Category.Render);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        GL11.glDeleteLists((int)this.playerBox, (int)1);
        this.playerBox = 0;
    }

    @Override
    public void onRender(double partialTicks) {
        this.playerBox = GL11.glGenLists((int)1);
        GL11.glNewList((int)this.playerBox, (int)4864);
        AxisAlignedBB bb = new AxisAlignedBB(-0.5, 0.0, -0.5, 0.5, 1.0, 0.5);
        RenderUtils.drawOutlinedBox(bb);
        GL11.glEndList();
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)2848);
        GL11.glLineWidth((float)2.0f);
        GL11.glDisable((int)3553);
        GL11.glDisable((int)2929);
        GL11.glPushMatrix();
        GL11.glTranslated((double)(-Wrapper.mc().getRenderManager().renderPosX), (double)(-Wrapper.mc().getRenderManager().renderPosY), (double)(-Wrapper.mc().getRenderManager().renderPosZ));
        this.renderBoxes(partialTicks);
        GL11.glPopMatrix();
        GL11.glEnable((int)2929);
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glDisable((int)2848);
    }

    private void renderBoxes(double partialTicks) {
        for (EntityPlayer e : Wrapper.world().playerEntities) {
            if (e == Wrapper.player()) continue;
            GL11.glPushMatrix();
            GL11.glTranslated((double)(e.prevPosX + (e.posX - e.prevPosX) * partialTicks), (double)(e.prevPosY + (e.posY - e.prevPosY) * partialTicks), (double)(e.prevPosZ + (e.posZ - e.prevPosZ) * partialTicks));
            GL11.glScaled((double)((double)e.width + 0.1), (double)((double)e.height + 0.1), (double)((double)e.width + 0.1));
            Wrapper.getFriends();
            if (FriendManager.isFriend(e.getName())) {
                GL11.glColor4f((float)0.0f, (float)0.0f, (float)1.0f, (float)0.5f);
            } else {
                GL11.glColor4f((float)1.0f, (float)0.0f, (float)0.0f, (float)0.5f);
            }
            GL11.glCallList((int)this.playerBox);
            GL11.glPopMatrix();
        }
    }
}


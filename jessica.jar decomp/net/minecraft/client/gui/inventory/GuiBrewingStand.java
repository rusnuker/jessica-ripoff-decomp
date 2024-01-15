/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.inventory;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class GuiBrewingStand
extends GuiContainer {
    private static final ResourceLocation BREWING_STAND_GUI_TEXTURES = new ResourceLocation("textures/gui/container/brewing_stand.png");
    private static final int[] BUBBLELENGTHS;
    private final InventoryPlayer playerInventory;
    private final IInventory tileBrewingStand;

    static {
        int[] nArray = new int[7];
        nArray[0] = 29;
        nArray[1] = 24;
        nArray[2] = 20;
        nArray[3] = 16;
        nArray[4] = 11;
        nArray[5] = 6;
        BUBBLELENGTHS = nArray;
    }

    public GuiBrewingStand(InventoryPlayer playerInv, IInventory p_i45506_2_) {
        super(new ContainerBrewingStand(playerInv, p_i45506_2_));
        this.playerInventory = playerInv;
        this.tileBrewingStand = p_i45506_2_;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.func_191948_b(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = this.tileBrewingStand.getDisplayName().getUnformattedText();
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6, 0x404040);
        this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int i1;
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.mc.getTextureManager().bindTexture(BREWING_STAND_GUI_TEXTURES);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        int k = this.tileBrewingStand.getField(1);
        int l = MathHelper.clamp((18 * k + 20 - 1) / 20, 0, 18);
        if (l > 0) {
            this.drawTexturedModalRect(i + 60, j + 44, 176, 29, l, 4);
        }
        if ((i1 = this.tileBrewingStand.getField(0)) > 0) {
            int j1 = (int)(28.0f * (1.0f - (float)i1 / 400.0f));
            if (j1 > 0) {
                this.drawTexturedModalRect(i + 97, j + 16, 176, 0, 9, j1);
            }
            if ((j1 = BUBBLELENGTHS[i1 / 2 % 7]) > 0) {
                this.drawTexturedModalRect(i + 63, j + 14 + 29 - j1, 185, 29 - j1, 12, j1);
            }
        }
    }
}


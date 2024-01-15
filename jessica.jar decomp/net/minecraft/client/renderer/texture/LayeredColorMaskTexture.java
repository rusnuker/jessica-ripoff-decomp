/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.io.IOUtils
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package net.minecraft.client.renderer.texture;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LayeredColorMaskTexture
extends AbstractTexture {
    private static final Logger LOG = LogManager.getLogger();
    private final ResourceLocation textureLocation;
    private final List<String> listTextures;
    private final List<EnumDyeColor> listDyeColors;

    public LayeredColorMaskTexture(ResourceLocation textureLocationIn, List<String> p_i46101_2_, List<EnumDyeColor> p_i46101_3_) {
        this.textureLocation = textureLocationIn;
        this.listTextures = p_i46101_2_;
        this.listDyeColors = p_i46101_3_;
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void loadTexture(IResourceManager resourceManager) throws IOException {
        this.deleteGlTexture();
        iresource = null;
        try {
            iresource = resourceManager.getResource(this.textureLocation);
            bufferedimage1 = TextureUtil.readBufferedImage(iresource.getInputStream());
            i = bufferedimage1.getType();
            if (i == 0) {
                i = 6;
            }
            bufferedimage = new BufferedImage(bufferedimage1.getWidth(), bufferedimage1.getHeight(), i);
            graphics = bufferedimage.getGraphics();
            graphics.drawImage(bufferedimage1, 0, 0, null);
            j = 0;
lbl14:
            // 2 sources

            while (true) {
                if (j >= 17 || j >= this.listTextures.size() || j >= this.listDyeColors.size()) {
                }
                ** GOTO lbl-1000
                break;
            }
        }
        catch (IOException ioexception) {
            LayeredColorMaskTexture.LOG.error("Couldn't load layered image", (Throwable)ioexception);
            IOUtils.closeQuietly((Closeable)iresource);
            return;
        }
        catch (Throwable var19_20) {
            IOUtils.closeQuietly(iresource);
            throw var19_20;
        }
        IOUtils.closeQuietly((Closeable)iresource);
        TextureUtil.uploadTextureImage(this.getGlTextureId(), bufferedimage);
        return;
lbl-1000:
        // 1 sources

        {
            iresource1 = null;
            try {
                s = this.listTextures.get(j);
                k = this.listDyeColors.get(j).func_193350_e();
                if (s != null && (bufferedimage2 = TextureUtil.readBufferedImage((iresource1 = resourceManager.getResource(new ResourceLocation(s))).getInputStream())).getWidth() == bufferedimage.getWidth() && bufferedimage2.getHeight() == bufferedimage.getHeight() && bufferedimage2.getType() == 6) {
                    l = 0;
                    while (l < bufferedimage2.getHeight()) {
                        i1 = 0;
                        while (i1 < bufferedimage2.getWidth()) {
                            j1 = bufferedimage2.getRGB(i1, l);
                            if ((j1 & -16777216) != 0) {
                                k1 = (j1 & 0xFF0000) << 8 & -16777216;
                                l1 = bufferedimage1.getRGB(i1, l);
                                i2 = MathHelper.multiplyColor(l1, k) & 0xFFFFFF;
                                bufferedimage2.setRGB(i1, l, k1 | i2);
                            }
                            ++i1;
                        }
                        ++l;
                    }
                    bufferedimage.getGraphics().drawImage(bufferedimage2, 0, 0, null);
                }
            }
            finally {
                IOUtils.closeQuietly(iresource1);
            }
            ++j;
            ** continue;
        }
    }
}


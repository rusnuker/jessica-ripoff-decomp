/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.imageout;

import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.imageout.ImageWriter;

public class ImageIOWriter
implements ImageWriter {
    @Override
    public void saveImage(Image image, String format, OutputStream output, boolean hasAlpha) throws IOException {
        ComponentColorModel cm;
        PixelInterleavedSampleModel sampleModel;
        int[] offsets;
        int len = 4 * image.getWidth() * image.getHeight();
        if (!hasAlpha) {
            len = 3 * image.getWidth() * image.getHeight();
        }
        ByteBuffer out = ByteBuffer.allocate(len);
        int y = 0;
        while (y < image.getHeight()) {
            int x = 0;
            while (x < image.getWidth()) {
                Color c = image.getColor(x, y);
                out.put((byte)(c.r * 255.0f));
                out.put((byte)(c.g * 255.0f));
                out.put((byte)(c.b * 255.0f));
                if (hasAlpha) {
                    out.put((byte)(c.a * 255.0f));
                }
                ++x;
            }
            ++y;
        }
        DataBufferByte dataBuffer = new DataBufferByte(out.array(), len);
        if (hasAlpha) {
            int[] nArray = new int[4];
            nArray[1] = 1;
            nArray[2] = 2;
            nArray[3] = 3;
            offsets = nArray;
            sampleModel = new PixelInterleavedSampleModel(0, image.getWidth(), image.getHeight(), 4, 4 * image.getWidth(), offsets);
            cm = new ComponentColorModel(ColorSpace.getInstance(1000), new int[]{8, 8, 8, 8}, true, false, 3, 0);
        } else {
            int[] nArray = new int[3];
            nArray[1] = 1;
            nArray[2] = 2;
            offsets = nArray;
            sampleModel = new PixelInterleavedSampleModel(0, image.getWidth(), image.getHeight(), 3, 3 * image.getWidth(), offsets);
            int[] nArray2 = new int[4];
            nArray2[0] = 8;
            nArray2[1] = 8;
            nArray2[2] = 8;
            cm = new ComponentColorModel(ColorSpace.getInstance(1000), nArray2, false, false, 1, 0);
        }
        WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));
        BufferedImage img = new BufferedImage(cm, raster, false, null);
        ImageIO.write((RenderedImage)img, format, output);
    }
}


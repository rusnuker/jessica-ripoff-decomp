/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.imageout;

import java.util.HashMap;
import javax.imageio.ImageIO;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.imageout.ImageIOWriter;
import org.newdawn.slick.imageout.ImageWriter;
import org.newdawn.slick.imageout.TGAWriter;

public class ImageWriterFactory {
    private static HashMap writers = new HashMap();

    static {
        String[] formats = ImageIO.getWriterFormatNames();
        ImageIOWriter writer = new ImageIOWriter();
        int i = 0;
        while (i < formats.length) {
            ImageWriterFactory.registerWriter(formats[i], writer);
            ++i;
        }
        TGAWriter tga = new TGAWriter();
        ImageWriterFactory.registerWriter("tga", tga);
    }

    public static void registerWriter(String format, ImageWriter writer) {
        writers.put(format, writer);
    }

    public static String[] getSupportedFormats() {
        return writers.keySet().toArray(new String[0]);
    }

    public static ImageWriter getWriterForFormat(String format) throws SlickException {
        ImageWriter writer = (ImageWriter)writers.get(format);
        if (writer != null) {
            return writer;
        }
        writer = (ImageWriter)writers.get(format.toLowerCase());
        if (writer != null) {
            return writer;
        }
        writer = (ImageWriter)writers.get(format.toUpperCase());
        if (writer != null) {
            return writer;
        }
        throw new SlickException("No image writer available for: " + format);
    }
}


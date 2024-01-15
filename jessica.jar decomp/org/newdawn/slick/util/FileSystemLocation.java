/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.newdawn.slick.util.ResourceLocation;

public class FileSystemLocation
implements ResourceLocation {
    private File root;

    public FileSystemLocation(File root) {
        this.root = root;
    }

    @Override
    public URL getResource(String ref) {
        File file;
        block4: {
            try {
                file = new File(this.root, ref);
                if (!file.exists()) {
                    file = new File(ref);
                }
                if (file.exists()) break block4;
                return null;
            }
            catch (IOException e) {
                return null;
            }
        }
        return file.toURI().toURL();
    }

    @Override
    public InputStream getResourceAsStream(String ref) {
        try {
            File file = new File(this.root, ref);
            if (!file.exists()) {
                file = new File(ref);
            }
            return new FileInputStream(file);
        }
        catch (IOException e) {
            return null;
        }
    }
}


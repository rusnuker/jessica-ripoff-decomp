/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric;

import com.mysql.fabric.Wrapper;
import org.darkstorm.minecraft.gui.component.Frame;
import org.darkstorm.minecraft.gui.util.GuiManagerDisplayScreen;

public class UIRenderer {
    public static void renderAndUpdateFrames() {
        Frame f;
        Frame[] frameArray = Wrapper.getGuiManager().getFrames();
        int n = frameArray.length;
        int n2 = 0;
        while (n2 < n) {
            f = frameArray[n2];
            f.update();
            ++n2;
        }
        frameArray = Wrapper.getGuiManager().getFrames();
        n = frameArray.length;
        n2 = 0;
        while (n2 < n) {
            f = frameArray[n2];
            if (f.isPinned() || Wrapper.mc().currentScreen instanceof GuiManagerDisplayScreen) {
                f.render();
            }
            ++n2;
        }
    }
}


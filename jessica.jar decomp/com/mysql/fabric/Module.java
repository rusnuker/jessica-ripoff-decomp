/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric;

import com.mysql.fabric.Category;
import com.mysql.fabric.Wrapper;
import net.minecraft.network.Packet;
import net.minecraftforge.common.model.MoveEvent;

public class Module {
    public double hoverOpacity = 0.0;
    private String name;
    protected boolean toggled;
    private Category category;

    public Module(String m, Category c) {
        this.name = m;
        this.category = c;
        this.toggled = false;
    }

    public void toggle() {
        boolean bl = this.toggled = !this.toggled;
        if (this.toggled) {
            this.onEnable();
        } else {
            this.onDisable();
        }
        try {
            Wrapper.getFiles().saveModules();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onUpdate() {
    }

    public void onGetPacket(Packet<?> packet) {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isToggled() {
        return this.toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
        try {
            Wrapper.getFiles().saveModules();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public Category getCategory() {
        return this.category;
    }

    public String getAlias() {
        return this.getName().toLowerCase().replace(" ", "");
    }

    public void onMotion(MoveEvent e) {
    }

    public void onRender(double partialTicks) {
    }
}


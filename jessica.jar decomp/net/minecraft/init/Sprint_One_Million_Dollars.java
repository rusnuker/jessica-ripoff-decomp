/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.init;

import com.mysql.fabric.Category;
import com.mysql.fabric.Module;
import com.mysql.fabric.Wrapper;

public class Sprint_One_Million_Dollars
extends Module {
    public Sprint_One_Million_Dollars() {
        super("Sprint", Category.Player);
    }

    @Override
    public void onUpdate() {
        Wrapper.player().setSprinting(true);
        super.onUpdate();
    }
}


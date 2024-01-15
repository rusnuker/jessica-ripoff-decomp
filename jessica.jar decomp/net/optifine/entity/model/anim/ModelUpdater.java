/*
 * Decompiled with CFR 0.152.
 */
package net.optifine.entity.model.anim;

import net.optifine.entity.model.anim.IModelResolver;
import net.optifine.entity.model.anim.ModelVariableUpdater;

public class ModelUpdater {
    private ModelVariableUpdater[] modelVariableUpdaters;

    public ModelUpdater(ModelVariableUpdater[] modelVariableUpdaters) {
        this.modelVariableUpdaters = modelVariableUpdaters;
    }

    public void update() {
        int i = 0;
        while (i < this.modelVariableUpdaters.length) {
            ModelVariableUpdater modelvariableupdater = this.modelVariableUpdaters[i];
            modelvariableupdater.update();
            ++i;
        }
    }

    public boolean initialize(IModelResolver mr) {
        int i = 0;
        while (i < this.modelVariableUpdaters.length) {
            ModelVariableUpdater modelvariableupdater = this.modelVariableUpdaters[i];
            if (!modelvariableupdater.initialize(mr)) {
                return false;
            }
            ++i;
        }
        return true;
    }
}


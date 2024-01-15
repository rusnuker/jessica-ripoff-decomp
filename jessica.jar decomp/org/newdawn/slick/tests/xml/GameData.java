/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.tests.xml;

import java.util.ArrayList;
import org.newdawn.slick.tests.xml.Entity;

public class GameData {
    private ArrayList entities = new ArrayList();

    private void add(Entity entity) {
        this.entities.add(entity);
    }

    public void dump(String prefix) {
        System.out.println(String.valueOf(prefix) + "GameData");
        int i = 0;
        while (i < this.entities.size()) {
            ((Entity)this.entities.get(i)).dump(String.valueOf(prefix) + "\t");
            ++i;
        }
    }
}


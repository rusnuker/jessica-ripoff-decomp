/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.tests.xml;

import java.util.ArrayList;
import org.newdawn.slick.tests.xml.Item;

public class ItemContainer
extends Item {
    private ArrayList items = new ArrayList();

    private void add(Item item) {
        this.items.add(item);
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setCondition(int condition) {
        this.condition = condition;
    }

    @Override
    public void dump(String prefix) {
        System.out.println(String.valueOf(prefix) + "Item Container " + this.name + "," + this.condition);
        int i = 0;
        while (i < this.items.size()) {
            ((Item)this.items.get(i)).dump(String.valueOf(prefix) + "\t");
            ++i;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.tests.xml;

public class Item {
    protected String name;
    protected int condition;

    public void dump(String prefix) {
        System.out.println(String.valueOf(prefix) + "Item " + this.name + "," + this.condition);
    }
}


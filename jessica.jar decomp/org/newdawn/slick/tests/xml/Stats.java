/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.tests.xml;

public class Stats {
    private int hp;
    private int mp;
    private float age;
    private int exp;

    public void dump(String prefix) {
        System.out.println(String.valueOf(prefix) + "Stats " + this.hp + "," + this.mp + "," + this.age + "," + this.exp);
    }
}


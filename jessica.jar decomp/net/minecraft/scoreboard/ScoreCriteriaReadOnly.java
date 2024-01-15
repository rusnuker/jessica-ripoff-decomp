/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.scoreboard;

import net.minecraft.scoreboard.ScoreCriteria;

public class ScoreCriteriaReadOnly
extends ScoreCriteria {
    public ScoreCriteriaReadOnly(String name) {
        super(name);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}


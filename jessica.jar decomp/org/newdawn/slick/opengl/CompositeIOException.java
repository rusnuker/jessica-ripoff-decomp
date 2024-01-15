/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.opengl;

import java.io.IOException;
import java.util.ArrayList;

public class CompositeIOException
extends IOException {
    private ArrayList exceptions = new ArrayList();

    public void addException(Exception e) {
        this.exceptions.add(e);
    }

    @Override
    public String getMessage() {
        String msg = "Composite Exception: \n";
        int i = 0;
        while (i < this.exceptions.size()) {
            msg = String.valueOf(msg) + "\t" + ((IOException)this.exceptions.get(i)).getMessage() + "\n";
            ++i;
        }
        return msg;
    }
}


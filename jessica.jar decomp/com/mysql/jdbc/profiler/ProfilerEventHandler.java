/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.profiler;

import com.mysql.jdbc.Extension;
import com.mysql.jdbc.profiler.ProfilerEvent;

public interface ProfilerEventHandler
extends Extension {
    public void consumeEvent(ProfilerEvent var1);
}


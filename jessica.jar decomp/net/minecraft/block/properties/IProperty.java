/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Optional
 */
package net.minecraft.block.properties;

import com.google.common.base.Optional;
import java.util.Collection;

public interface IProperty<T extends Comparable<T>> {
    public String getName();

    public Collection<T> getAllowedValues();

    public Class<T> getValueClass();

    public Optional<T> parseValue(String var1);

    public String getName(T var1);
}


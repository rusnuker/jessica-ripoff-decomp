/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Function
 *  com.google.common.base.MoreObjects
 *  com.google.common.base.Optional
 *  com.google.common.base.Predicate
 *  com.google.common.base.Predicates
 *  com.google.common.base.Splitter
 *  com.google.common.collect.Iterables
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.multipart.ICondition;

public class ConditionPropertyValue
implements ICondition {
    private static final Splitter SPLITTER = Splitter.on((char)'|').omitEmptyStrings();
    private final String key;
    private final String value;

    public ConditionPropertyValue(String keyIn, String valueIn) {
        this.key = keyIn;
        this.value = valueIn;
    }

    @Override
    public Predicate<IBlockState> getPredicate(BlockStateContainer blockState) {
        List list;
        boolean flag;
        final IProperty<?> iproperty = blockState.getProperty(this.key);
        if (iproperty == null) {
            throw new RuntimeException(String.valueOf(this.toString()) + ": Definition: " + blockState + " has no property: " + this.key);
        }
        String s = this.value;
        boolean bl = flag = !s.isEmpty() && s.charAt(0) == '!';
        if (flag) {
            s = s.substring(1);
        }
        if ((list = SPLITTER.splitToList((CharSequence)s)).isEmpty()) {
            throw new RuntimeException(String.valueOf(this.toString()) + ": has an empty value: " + this.value);
        }
        Predicate predicate = list.size() == 1 ? this.makePredicate(iproperty, s) : Predicates.or((Iterable)Iterables.transform((Iterable)list, (Function)new Function<String, Predicate<IBlockState>>(){

            @Nullable
            public Predicate<IBlockState> apply(@Nullable String p_apply_1_) {
                return ConditionPropertyValue.this.makePredicate(iproperty, p_apply_1_);
            }
        }));
        return flag ? Predicates.not((Predicate)predicate) : predicate;
    }

    private Predicate<IBlockState> makePredicate(final IProperty<?> property, String valueIn) {
        final Optional<?> optional = property.parseValue(valueIn);
        if (!optional.isPresent()) {
            throw new RuntimeException(String.valueOf(this.toString()) + ": has an unknown value: " + this.value);
        }
        return new Predicate<IBlockState>(){

            public boolean apply(@Nullable IBlockState p_apply_1_) {
                return p_apply_1_ != null && p_apply_1_.getValue(property).equals(optional.get());
            }
        };
    }

    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("key", (Object)this.key).add("value", (Object)this.value).toString();
    }
}


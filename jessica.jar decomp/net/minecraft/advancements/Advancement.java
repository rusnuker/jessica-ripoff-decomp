/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Sets
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonSyntaxException
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.commons.lang3.ArrayUtils;

public class Advancement {
    private final Advancement field_192076_a;
    private final DisplayInfo field_192077_b;
    private final AdvancementRewards field_192078_c;
    private final ResourceLocation field_192079_d;
    private final Map<String, Criterion> field_192080_e;
    private final String[][] field_192081_f;
    private final Set<Advancement> field_192082_g = Sets.newLinkedHashSet();
    private final ITextComponent field_193125_h;

    public Advancement(ResourceLocation p_i47472_1_, @Nullable Advancement p_i47472_2_, @Nullable DisplayInfo p_i47472_3_, AdvancementRewards p_i47472_4_, Map<String, Criterion> p_i47472_5_, String[][] p_i47472_6_) {
        this.field_192079_d = p_i47472_1_;
        this.field_192077_b = p_i47472_3_;
        this.field_192080_e = ImmutableMap.copyOf(p_i47472_5_);
        this.field_192076_a = p_i47472_2_;
        this.field_192078_c = p_i47472_4_;
        this.field_192081_f = p_i47472_6_;
        if (p_i47472_2_ != null) {
            p_i47472_2_.func_192071_a(this);
        }
        if (p_i47472_3_ == null) {
            this.field_193125_h = new TextComponentString(p_i47472_1_.toString());
        } else {
            this.field_193125_h = new TextComponentString("[");
            this.field_193125_h.getStyle().setColor(p_i47472_3_.func_192291_d().func_193229_c());
            ITextComponent itextcomponent = p_i47472_3_.func_192297_a().createCopy();
            TextComponentString itextcomponent1 = new TextComponentString("");
            ITextComponent itextcomponent2 = itextcomponent.createCopy();
            itextcomponent2.getStyle().setColor(p_i47472_3_.func_192291_d().func_193229_c());
            itextcomponent1.appendSibling(itextcomponent2);
            itextcomponent1.appendText("\n");
            itextcomponent1.appendSibling(p_i47472_3_.func_193222_b());
            itextcomponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, itextcomponent1));
            this.field_193125_h.appendSibling(itextcomponent);
            this.field_193125_h.appendText("]");
        }
    }

    public Builder func_192075_a() {
        return new Builder(this.field_192076_a == null ? null : this.field_192076_a.func_192067_g(), this.field_192077_b, this.field_192078_c, this.field_192080_e, this.field_192081_f);
    }

    @Nullable
    public Advancement func_192070_b() {
        return this.field_192076_a;
    }

    @Nullable
    public DisplayInfo func_192068_c() {
        return this.field_192077_b;
    }

    public AdvancementRewards func_192072_d() {
        return this.field_192078_c;
    }

    public String toString() {
        return "SimpleAdvancement{id=" + this.func_192067_g() + ", parent=" + (this.field_192076_a == null ? "null" : this.field_192076_a.func_192067_g()) + ", display=" + this.field_192077_b + ", rewards=" + this.field_192078_c + ", criteria=" + this.field_192080_e + ", requirements=" + Arrays.deepToString((Object[])this.field_192081_f) + '}';
    }

    public Iterable<Advancement> func_192069_e() {
        return this.field_192082_g;
    }

    public Map<String, Criterion> func_192073_f() {
        return this.field_192080_e;
    }

    public int func_193124_g() {
        return this.field_192081_f.length;
    }

    public void func_192071_a(Advancement p_192071_1_) {
        this.field_192082_g.add(p_192071_1_);
    }

    public ResourceLocation func_192067_g() {
        return this.field_192079_d;
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        }
        if (!(p_equals_1_ instanceof Advancement)) {
            return false;
        }
        Advancement advancement = (Advancement)p_equals_1_;
        return this.field_192079_d.equals(advancement.field_192079_d);
    }

    public int hashCode() {
        return this.field_192079_d.hashCode();
    }

    public String[][] func_192074_h() {
        return this.field_192081_f;
    }

    public ITextComponent func_193123_j() {
        return this.field_193125_h;
    }

    public static class Builder {
        private final ResourceLocation field_192061_a;
        private Advancement field_192062_b;
        private final DisplayInfo field_192063_c;
        private final AdvancementRewards field_192064_d;
        private final Map<String, Criterion> field_192065_e;
        private final String[][] field_192066_f;

        Builder(@Nullable ResourceLocation p_i47414_1_, @Nullable DisplayInfo p_i47414_2_, AdvancementRewards p_i47414_3_, Map<String, Criterion> p_i47414_4_, String[][] p_i47414_5_) {
            this.field_192061_a = p_i47414_1_;
            this.field_192063_c = p_i47414_2_;
            this.field_192064_d = p_i47414_3_;
            this.field_192065_e = p_i47414_4_;
            this.field_192066_f = p_i47414_5_;
        }

        public boolean func_192058_a(Function<ResourceLocation, Advancement> p_192058_1_) {
            if (this.field_192061_a == null) {
                return true;
            }
            this.field_192062_b = p_192058_1_.apply(this.field_192061_a);
            return this.field_192062_b != null;
        }

        public Advancement func_192056_a(ResourceLocation p_192056_1_) {
            return new Advancement(p_192056_1_, this.field_192062_b, this.field_192063_c, this.field_192064_d, this.field_192065_e, this.field_192066_f);
        }

        public void func_192057_a(PacketBuffer p_192057_1_) {
            if (this.field_192061_a == null) {
                p_192057_1_.writeBoolean(false);
            } else {
                p_192057_1_.writeBoolean(true);
                p_192057_1_.func_192572_a(this.field_192061_a);
            }
            if (this.field_192063_c == null) {
                p_192057_1_.writeBoolean(false);
            } else {
                p_192057_1_.writeBoolean(true);
                this.field_192063_c.func_192290_a(p_192057_1_);
            }
            Criterion.func_192141_a(this.field_192065_e, p_192057_1_);
            p_192057_1_.writeVarIntToBuffer(this.field_192066_f.length);
            String[][] stringArray = this.field_192066_f;
            int n = this.field_192066_f.length;
            int n2 = 0;
            while (n2 < n) {
                String[] astring = stringArray[n2];
                p_192057_1_.writeVarIntToBuffer(astring.length);
                String[] stringArray2 = astring;
                int n3 = astring.length;
                int n4 = 0;
                while (n4 < n3) {
                    String s = stringArray2[n4];
                    p_192057_1_.writeString(s);
                    ++n4;
                }
                ++n2;
            }
        }

        public String toString() {
            return "Task Advancement{parentId=" + this.field_192061_a + ", display=" + this.field_192063_c + ", rewards=" + this.field_192064_d + ", criteria=" + this.field_192065_e + ", requirements=" + Arrays.deepToString((Object[])this.field_192066_f) + '}';
        }

        public static Builder func_192059_a(JsonObject p_192059_0_, JsonDeserializationContext p_192059_1_) {
            int n;
            ResourceLocation resourcelocation = p_192059_0_.has("parent") ? new ResourceLocation(JsonUtils.getString(p_192059_0_, "parent")) : null;
            DisplayInfo displayinfo = p_192059_0_.has("display") ? DisplayInfo.func_192294_a(JsonUtils.getJsonObject(p_192059_0_, "display"), p_192059_1_) : null;
            AdvancementRewards advancementrewards = JsonUtils.deserializeClass(p_192059_0_, "rewards", AdvancementRewards.field_192114_a, p_192059_1_, AdvancementRewards.class);
            Map<String, Criterion> map = Criterion.func_192144_b(JsonUtils.getJsonObject(p_192059_0_, "criteria"), p_192059_1_);
            if (map.isEmpty()) {
                throw new JsonSyntaxException("Advancement criteria cannot be empty");
            }
            JsonArray jsonarray = JsonUtils.getJsonArray(p_192059_0_, "requirements", new JsonArray());
            String[][] astring = new String[jsonarray.size()][];
            int i = 0;
            while (i < jsonarray.size()) {
                JsonArray jsonarray1 = JsonUtils.getJsonArray(jsonarray.get(i), "requirements[" + i + "]");
                astring[i] = new String[jsonarray1.size()];
                int j = 0;
                while (j < jsonarray1.size()) {
                    astring[i][j] = JsonUtils.getString(jsonarray1.get(j), "requirements[" + i + "][" + j + "]");
                    ++j;
                }
                ++i;
            }
            if (astring.length == 0) {
                astring = new String[map.size()][];
                int k = 0;
                for (String s2 : map.keySet()) {
                    astring[k++] = new String[]{s2};
                }
            }
            String[][] stringArray = astring;
            int j = astring.length;
            int n2 = 0;
            while (n2 < j) {
                String[] astring1 = stringArray[n2];
                if (astring1.length == 0 && map.isEmpty()) {
                    throw new JsonSyntaxException("Requirement entry cannot be empty");
                }
                String[] stringArray2 = astring1;
                int n3 = astring1.length;
                n = 0;
                while (n < n3) {
                    String s = stringArray2[n];
                    if (!map.containsKey(s)) {
                        throw new JsonSyntaxException("Unknown required criterion '" + s + "'");
                    }
                    ++n;
                }
                ++n2;
            }
            for (String s1 : map.keySet()) {
                boolean flag = false;
                String[][] stringArray3 = astring;
                n = astring.length;
                int n4 = 0;
                while (n4 < n) {
                    Object[] astring2 = stringArray3[n4];
                    if (ArrayUtils.contains((Object[])astring2, (Object)s1)) {
                        flag = true;
                        break;
                    }
                    ++n4;
                }
                if (flag) continue;
                throw new JsonSyntaxException("Criterion '" + s1 + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required.");
            }
            return new Builder(resourcelocation, displayinfo, advancementrewards, map, astring);
        }

        public static Builder func_192060_b(PacketBuffer p_192060_0_) throws IOException {
            ResourceLocation resourcelocation = p_192060_0_.readBoolean() ? p_192060_0_.func_192575_l() : null;
            DisplayInfo displayinfo = p_192060_0_.readBoolean() ? DisplayInfo.func_192295_b(p_192060_0_) : null;
            Map<String, Criterion> map = Criterion.func_192142_c(p_192060_0_);
            String[][] astring = new String[p_192060_0_.readVarIntFromBuffer()][];
            int i = 0;
            while (i < astring.length) {
                astring[i] = new String[p_192060_0_.readVarIntFromBuffer()];
                int j = 0;
                while (j < astring[i].length) {
                    astring[i][j] = p_192060_0_.readStringFromBuffer(Short.MAX_VALUE);
                    ++j;
                }
                ++i;
            }
            return new Builder(resourcelocation, displayinfo, AdvancementRewards.field_192114_a, map, astring);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.util;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;

public enum SoundCategory {
    MASTER("master"),
    MUSIC("music"),
    RECORDS("record"),
    WEATHER("weather"),
    BLOCKS("block"),
    HOSTILE("hostile"),
    NEUTRAL("neutral"),
    PLAYERS("player"),
    AMBIENT("ambient"),
    VOICE("voice");

    private static final Map<String, SoundCategory> SOUND_CATEGORIES;
    private final String name;

    static {
        SOUND_CATEGORIES = Maps.newHashMap();
        SoundCategory[] soundCategoryArray = SoundCategory.values();
        int n = soundCategoryArray.length;
        int n2 = 0;
        while (n2 < n) {
            SoundCategory soundcategory = soundCategoryArray[n2];
            if (SOUND_CATEGORIES.containsKey(soundcategory.getName())) {
                throw new Error("Clash in Sound Category name pools! Cannot insert " + (Object)((Object)soundcategory));
            }
            SOUND_CATEGORIES.put(soundcategory.getName(), soundcategory);
            ++n2;
        }
    }

    private SoundCategory(String nameIn) {
        this.name = nameIn;
    }

    public String getName() {
        return this.name;
    }

    public static SoundCategory getByName(String categoryName) {
        return SOUND_CATEGORIES.get(categoryName);
    }

    public static Set<String> getSoundCategoryNames() {
        return SOUND_CATEGORIES.keySet();
    }
}


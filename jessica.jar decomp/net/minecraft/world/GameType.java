/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world;

import com.mysql.fabric.Wrapper;
import net.minecraft.entity.player.PlayerCapabilities;

public enum GameType {
    NOT_SET(-1, "", ""),
    SURVIVAL(0, "survival", "s"),
    CREATIVE(1, "creative", "c"),
    ADVENTURE(2, "adventure", "a"),
    SPECTATOR(3, "spectator", "sp");

    int id;
    String name;
    String shortName;

    private GameType(int idIn, String nameIn, String shortNameIn) {
        this.id = idIn;
        this.name = nameIn;
        this.shortName = shortNameIn;
    }

    public int getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void configurePlayerCapabilities(PlayerCapabilities capabilities) {
        if (this == CREATIVE) {
            capabilities.allowFlying = true;
            capabilities.isCreativeMode = true;
            capabilities.disableDamage = true;
        } else if (this == SPECTATOR) {
            capabilities.allowFlying = true;
            capabilities.isCreativeMode = false;
            capabilities.disableDamage = true;
            capabilities.isFlying = true;
        } else {
            capabilities.allowFlying = false;
            capabilities.isCreativeMode = false;
            capabilities.disableDamage = false;
            capabilities.isFlying = false;
        }
        capabilities.allowEdit = !this.isAdventure();
    }

    public boolean isAdventure() {
        return this == ADVENTURE || this == SPECTATOR;
    }

    public boolean isCreative() {
        if (Wrapper.getModule("FakeCreative").isToggled()) {
            return this == SURVIVAL || this == ADVENTURE || this == SPECTATOR;
        }
        return this == CREATIVE;
    }

    public boolean isSurvivalOrAdventure() {
        if (Wrapper.getModule("FakeCreative").isToggled()) {
            return this == CREATIVE;
        }
        return this == SURVIVAL || this == ADVENTURE;
    }

    public static GameType getByID(int idIn) {
        return GameType.parseGameTypeWithDefault(idIn, SURVIVAL);
    }

    public static GameType parseGameTypeWithDefault(int targetId, GameType fallback) {
        GameType[] gameTypeArray = GameType.values();
        int n = gameTypeArray.length;
        int n2 = 0;
        while (n2 < n) {
            GameType gametype = gameTypeArray[n2];
            if (gametype.id == targetId) {
                return gametype;
            }
            ++n2;
        }
        return fallback;
    }

    public static GameType getByName(String gamemodeName) {
        return GameType.parseGameTypeWithDefault(gamemodeName, SURVIVAL);
    }

    public static GameType parseGameTypeWithDefault(String targetName, GameType fallback) {
        GameType[] gameTypeArray = GameType.values();
        int n = gameTypeArray.length;
        int n2 = 0;
        while (n2 < n) {
            GameType gametype = gameTypeArray[n2];
            if (gametype.name.equals(targetName) || gametype.shortName.equals(targetName)) {
                return gametype;
            }
            ++n2;
        }
        return fallback;
    }
}


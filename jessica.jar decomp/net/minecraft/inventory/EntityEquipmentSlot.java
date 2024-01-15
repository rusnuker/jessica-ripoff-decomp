/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.inventory;

public enum EntityEquipmentSlot {
    MAINHAND(Type.HAND, 0, 0, "mainhand"),
    OFFHAND(Type.HAND, 1, 5, "offhand"),
    FEET(Type.ARMOR, 0, 1, "feet"),
    LEGS(Type.ARMOR, 1, 2, "legs"),
    CHEST(Type.ARMOR, 2, 3, "chest"),
    HEAD(Type.ARMOR, 3, 4, "head");

    private final Type slotType;
    private final int index;
    private final int slotIndex;
    private final String name;

    private EntityEquipmentSlot(Type slotTypeIn, int indexIn, int slotIndexIn, String nameIn) {
        this.slotType = slotTypeIn;
        this.index = indexIn;
        this.slotIndex = slotIndexIn;
        this.name = nameIn;
    }

    public Type getSlotType() {
        return this.slotType;
    }

    public int getIndex() {
        return this.index;
    }

    public int getSlotIndex() {
        return this.slotIndex;
    }

    public String getName() {
        return this.name;
    }

    public static EntityEquipmentSlot fromString(String targetName) {
        EntityEquipmentSlot[] entityEquipmentSlotArray = EntityEquipmentSlot.values();
        int n = entityEquipmentSlotArray.length;
        int n2 = 0;
        while (n2 < n) {
            EntityEquipmentSlot entityequipmentslot = entityEquipmentSlotArray[n2];
            if (entityequipmentslot.getName().equals(targetName)) {
                return entityequipmentslot;
            }
            ++n2;
        }
        throw new IllegalArgumentException("Invalid slot '" + targetName + "'");
    }

    public static enum Type {
        HAND,
        ARMOR;

    }
}


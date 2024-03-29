/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package net.minecraft.entity;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SharedMonsterAttributes {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final IAttribute MAX_HEALTH = new RangedAttribute(null, "generic.maxHealth", 20.0, 0.0, 1024.0).setDescription("Max Health").setShouldWatch(true);
    public static final IAttribute FOLLOW_RANGE = new RangedAttribute(null, "generic.followRange", 32.0, 0.0, 2048.0).setDescription("Follow Range");
    public static final IAttribute KNOCKBACK_RESISTANCE = new RangedAttribute(null, "generic.knockbackResistance", 0.0, 0.0, 1.0).setDescription("Knockback Resistance");
    public static final IAttribute MOVEMENT_SPEED = new RangedAttribute(null, "generic.movementSpeed", 0.7f, 0.0, 1024.0).setDescription("Movement Speed").setShouldWatch(true);
    public static final IAttribute field_193334_e = new RangedAttribute(null, "generic.flyingSpeed", 0.4f, 0.0, 1024.0).setDescription("Flying Speed").setShouldWatch(true);
    public static final IAttribute ATTACK_DAMAGE = new RangedAttribute(null, "generic.attackDamage", 2.0, 0.0, 2048.0);
    public static final IAttribute ATTACK_SPEED = new RangedAttribute(null, "generic.attackSpeed", 4.0, 0.0, 1024.0).setShouldWatch(true);
    public static final IAttribute ARMOR = new RangedAttribute(null, "generic.armor", 0.0, 0.0, 30.0).setShouldWatch(true);
    public static final IAttribute ARMOR_TOUGHNESS = new RangedAttribute(null, "generic.armorToughness", 0.0, 0.0, 20.0).setShouldWatch(true);
    public static final IAttribute LUCK = new RangedAttribute(null, "generic.luck", 0.0, -1024.0, 1024.0).setShouldWatch(true);

    public static NBTTagList writeBaseAttributeMapToNBT(AbstractAttributeMap map) {
        NBTTagList nbttaglist = new NBTTagList();
        for (IAttributeInstance iattributeinstance : map.getAllAttributes()) {
            nbttaglist.appendTag(SharedMonsterAttributes.writeAttributeInstanceToNBT(iattributeinstance));
        }
        return nbttaglist;
    }

    private static NBTTagCompound writeAttributeInstanceToNBT(IAttributeInstance instance) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        IAttribute iattribute = instance.getAttribute();
        nbttagcompound.setString("Name", iattribute.getAttributeUnlocalizedName());
        nbttagcompound.setDouble("Base", instance.getBaseValue());
        Collection<AttributeModifier> collection = instance.getModifiers();
        if (collection != null && !collection.isEmpty()) {
            NBTTagList nbttaglist = new NBTTagList();
            for (AttributeModifier attributemodifier : collection) {
                if (!attributemodifier.isSaved()) continue;
                nbttaglist.appendTag(SharedMonsterAttributes.writeAttributeModifierToNBT(attributemodifier));
            }
            nbttagcompound.setTag("Modifiers", nbttaglist);
        }
        return nbttagcompound;
    }

    public static NBTTagCompound writeAttributeModifierToNBT(AttributeModifier modifier) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setString("Name", modifier.getName());
        nbttagcompound.setDouble("Amount", modifier.getAmount());
        nbttagcompound.setInteger("Operation", modifier.getOperation());
        nbttagcompound.setUniqueId("UUID", modifier.getID());
        return nbttagcompound;
    }

    public static void setAttributeModifiers(AbstractAttributeMap map, NBTTagList list) {
        int i = 0;
        while (i < list.tagCount()) {
            NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
            IAttributeInstance iattributeinstance = map.getAttributeInstanceByName(nbttagcompound.getString("Name"));
            if (iattributeinstance == null) {
                LOGGER.warn("Ignoring unknown attribute '{}'", (Object)nbttagcompound.getString("Name"));
            } else {
                SharedMonsterAttributes.applyModifiersToAttributeInstance(iattributeinstance, nbttagcompound);
            }
            ++i;
        }
    }

    private static void applyModifiersToAttributeInstance(IAttributeInstance instance, NBTTagCompound compound) {
        instance.setBaseValue(compound.getDouble("Base"));
        if (compound.hasKey("Modifiers", 9)) {
            NBTTagList nbttaglist = compound.getTagList("Modifiers", 10);
            int i = 0;
            while (i < nbttaglist.tagCount()) {
                AttributeModifier attributemodifier = SharedMonsterAttributes.readAttributeModifierFromNBT(nbttaglist.getCompoundTagAt(i));
                if (attributemodifier != null) {
                    AttributeModifier attributemodifier1 = instance.getModifier(attributemodifier.getID());
                    if (attributemodifier1 != null) {
                        instance.removeModifier(attributemodifier1);
                    }
                    instance.applyModifier(attributemodifier);
                }
                ++i;
            }
        }
    }

    @Nullable
    public static AttributeModifier readAttributeModifierFromNBT(NBTTagCompound compound) {
        UUID uuid = compound.getUniqueId("UUID");
        try {
            return new AttributeModifier(uuid, compound.getString("Name"), compound.getDouble("Amount"), compound.getInteger("Operation"));
        }
        catch (Exception exception) {
            LOGGER.warn("Unable to create attribute: {}", (Object)exception.getMessage());
            return null;
        }
    }
}


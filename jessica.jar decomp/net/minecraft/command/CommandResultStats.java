/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.command;

import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class CommandResultStats {
    private static final int NUM_RESULT_TYPES = Type.values().length;
    private static final String[] STRING_RESULT_TYPES = new String[NUM_RESULT_TYPES];
    private String[] entitiesID = STRING_RESULT_TYPES;
    private String[] objectives = STRING_RESULT_TYPES;

    public void setCommandStatForSender(MinecraftServer server, final ICommandSender sender, Type typeIn, int p_184932_4_) {
        String s = this.entitiesID[typeIn.getTypeID()];
        if (s != null) {
            Scoreboard scoreboard;
            ScoreObjective scoreobjective;
            String s1;
            ICommandSender icommandsender = new ICommandSender(){

                @Override
                public String getName() {
                    return sender.getName();
                }

                @Override
                public ITextComponent getDisplayName() {
                    return sender.getDisplayName();
                }

                @Override
                public void addChatMessage(ITextComponent component) {
                    sender.addChatMessage(component);
                }

                @Override
                public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
                    return true;
                }

                @Override
                public BlockPos getPosition() {
                    return sender.getPosition();
                }

                @Override
                public Vec3d getPositionVector() {
                    return sender.getPositionVector();
                }

                @Override
                public World getEntityWorld() {
                    return sender.getEntityWorld();
                }

                @Override
                public Entity getCommandSenderEntity() {
                    return sender.getCommandSenderEntity();
                }

                @Override
                public boolean sendCommandFeedback() {
                    return sender.sendCommandFeedback();
                }

                @Override
                public void setCommandStat(Type type, int amount) {
                    sender.setCommandStat(type, amount);
                }

                @Override
                public MinecraftServer getServer() {
                    return sender.getServer();
                }
            };
            try {
                s1 = CommandBase.getEntityName(server, icommandsender, s);
            }
            catch (CommandException var12) {
                return;
            }
            String s2 = this.objectives[typeIn.getTypeID()];
            if (s2 != null && (scoreobjective = (scoreboard = sender.getEntityWorld().getScoreboard()).getObjective(s2)) != null && scoreboard.entityHasObjective(s1, scoreobjective)) {
                Score score = scoreboard.getOrCreateScore(s1, scoreobjective);
                score.setScorePoints(p_184932_4_);
            }
        }
    }

    public void readStatsFromNBT(NBTTagCompound tagcompound) {
        if (tagcompound.hasKey("CommandStats", 10)) {
            NBTTagCompound nbttagcompound = tagcompound.getCompoundTag("CommandStats");
            Type[] typeArray = Type.values();
            int n = typeArray.length;
            int n2 = 0;
            while (n2 < n) {
                Type commandresultstats$type = typeArray[n2];
                String s = String.valueOf(commandresultstats$type.getTypeName()) + "Name";
                String s1 = String.valueOf(commandresultstats$type.getTypeName()) + "Objective";
                if (nbttagcompound.hasKey(s, 8) && nbttagcompound.hasKey(s1, 8)) {
                    String s2 = nbttagcompound.getString(s);
                    String s3 = nbttagcompound.getString(s1);
                    CommandResultStats.setScoreBoardStat(this, commandresultstats$type, s2, s3);
                }
                ++n2;
            }
        }
    }

    public void writeStatsToNBT(NBTTagCompound tagcompound) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        Type[] typeArray = Type.values();
        int n = typeArray.length;
        int n2 = 0;
        while (n2 < n) {
            Type commandresultstats$type = typeArray[n2];
            String s = this.entitiesID[commandresultstats$type.getTypeID()];
            String s1 = this.objectives[commandresultstats$type.getTypeID()];
            if (s != null && s1 != null) {
                nbttagcompound.setString(String.valueOf(commandresultstats$type.getTypeName()) + "Name", s);
                nbttagcompound.setString(String.valueOf(commandresultstats$type.getTypeName()) + "Objective", s1);
            }
            ++n2;
        }
        if (!nbttagcompound.hasNoTags()) {
            tagcompound.setTag("CommandStats", nbttagcompound);
        }
    }

    public static void setScoreBoardStat(CommandResultStats stats, Type resultType, @Nullable String entityID, @Nullable String objectiveName) {
        if (entityID != null && !entityID.isEmpty() && objectiveName != null && !objectiveName.isEmpty()) {
            if (stats.entitiesID == STRING_RESULT_TYPES || stats.objectives == STRING_RESULT_TYPES) {
                stats.entitiesID = new String[NUM_RESULT_TYPES];
                stats.objectives = new String[NUM_RESULT_TYPES];
            }
            stats.entitiesID[resultType.getTypeID()] = entityID;
            stats.objectives[resultType.getTypeID()] = objectiveName;
        } else {
            CommandResultStats.removeScoreBoardStat(stats, resultType);
        }
    }

    private static void removeScoreBoardStat(CommandResultStats resultStatsIn, Type resultTypeIn) {
        if (resultStatsIn.entitiesID != STRING_RESULT_TYPES && resultStatsIn.objectives != STRING_RESULT_TYPES) {
            resultStatsIn.entitiesID[resultTypeIn.getTypeID()] = null;
            resultStatsIn.objectives[resultTypeIn.getTypeID()] = null;
            boolean flag = true;
            Type[] typeArray = Type.values();
            int n = typeArray.length;
            int n2 = 0;
            while (n2 < n) {
                Type commandresultstats$type = typeArray[n2];
                if (resultStatsIn.entitiesID[commandresultstats$type.getTypeID()] != null && resultStatsIn.objectives[commandresultstats$type.getTypeID()] != null) {
                    flag = false;
                    break;
                }
                ++n2;
            }
            if (flag) {
                resultStatsIn.entitiesID = STRING_RESULT_TYPES;
                resultStatsIn.objectives = STRING_RESULT_TYPES;
            }
        }
    }

    public void addAllStats(CommandResultStats resultStatsIn) {
        Type[] typeArray = Type.values();
        int n = typeArray.length;
        int n2 = 0;
        while (n2 < n) {
            Type commandresultstats$type = typeArray[n2];
            CommandResultStats.setScoreBoardStat(this, commandresultstats$type, resultStatsIn.entitiesID[commandresultstats$type.getTypeID()], resultStatsIn.objectives[commandresultstats$type.getTypeID()]);
            ++n2;
        }
    }

    public static enum Type {
        SUCCESS_COUNT(0, "SuccessCount"),
        AFFECTED_BLOCKS(1, "AffectedBlocks"),
        AFFECTED_ENTITIES(2, "AffectedEntities"),
        AFFECTED_ITEMS(3, "AffectedItems"),
        QUERY_RESULT(4, "QueryResult");

        final int typeID;
        final String typeName;

        private Type(int id, String name) {
            this.typeID = id;
            this.typeName = name;
        }

        public int getTypeID() {
            return this.typeID;
        }

        public String getTypeName() {
            return this.typeName;
        }

        public static String[] getTypeNames() {
            String[] astring = new String[Type.values().length];
            int i = 0;
            Type[] typeArray = Type.values();
            int n = typeArray.length;
            int n2 = 0;
            while (n2 < n) {
                Type commandresultstats$type = typeArray[n2];
                astring[i++] = commandresultstats$type.getTypeName();
                ++n2;
            }
            return astring;
        }

        @Nullable
        public static Type getTypeByName(String name) {
            Type[] typeArray = Type.values();
            int n = typeArray.length;
            int n2 = 0;
            while (n2 < n) {
                Type commandresultstats$type = typeArray[n2];
                if (commandresultstats$type.getTypeName().equals(name)) {
                    return commandresultstats$type;
                }
                ++n2;
            }
            return null;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.command.server;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;

public class CommandSaveAll
extends CommandBase {
    @Override
    public String getCommandName() {
        return "save-all";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.save.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        sender.addChatMessage(new TextComponentTranslation("commands.save.start", new Object[0]));
        if (server.getPlayerList() != null) {
            server.getPlayerList().saveAllPlayerData();
        }
        try {
            int i = 0;
            while (i < server.worldServers.length) {
                if (server.worldServers[i] != null) {
                    WorldServer worldserver = server.worldServers[i];
                    boolean flag = worldserver.disableLevelSaving;
                    worldserver.disableLevelSaving = false;
                    worldserver.saveAllChunks(true, null);
                    worldserver.disableLevelSaving = flag;
                }
                ++i;
            }
            if (args.length > 0 && "flush".equals(args[0])) {
                sender.addChatMessage(new TextComponentTranslation("commands.save.flushStart", new Object[0]));
                int j = 0;
                while (j < server.worldServers.length) {
                    if (server.worldServers[j] != null) {
                        WorldServer worldserver1 = server.worldServers[j];
                        boolean flag1 = worldserver1.disableLevelSaving;
                        worldserver1.disableLevelSaving = false;
                        worldserver1.saveChunkData();
                        worldserver1.disableLevelSaving = flag1;
                    }
                    ++j;
                }
                sender.addChatMessage(new TextComponentTranslation("commands.save.flushEnd", new Object[0]));
            }
        }
        catch (MinecraftException minecraftexception) {
            CommandSaveAll.notifyCommandListener(sender, (ICommand)this, "commands.save.failed", minecraftexception.getMessage());
            return;
        }
        CommandSaveAll.notifyCommandListener(sender, (ICommand)this, "commands.save.success", new Object[0]);
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        return args.length == 1 ? CommandSaveAll.getListOfStringsMatchingLastWord(args, "flush") : Collections.emptyList();
    }
}


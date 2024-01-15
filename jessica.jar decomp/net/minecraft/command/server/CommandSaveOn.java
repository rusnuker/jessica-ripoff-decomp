/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public class CommandSaveOn
extends CommandBase {
    @Override
    public String getCommandName() {
        return "save-on";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.save-on.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        boolean flag = false;
        int i = 0;
        while (i < server.worldServers.length) {
            if (server.worldServers[i] != null) {
                WorldServer worldserver = server.worldServers[i];
                if (worldserver.disableLevelSaving) {
                    worldserver.disableLevelSaving = false;
                    flag = true;
                }
            }
            ++i;
        }
        if (!flag) {
            throw new CommandException("commands.save-on.alreadyOn", new Object[0]);
        }
        CommandSaveOn.notifyCommandListener(sender, (ICommand)this, "commands.save.enabled", new Object[0]);
    }
}


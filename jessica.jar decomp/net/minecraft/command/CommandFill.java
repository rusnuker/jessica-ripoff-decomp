/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.command;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandFill
extends CommandBase {
    @Override
    public String getCommandName() {
        return "fill";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.fill.usage";
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        block24: {
            block18: {
                if (args.length < 7) {
                    throw new WrongUsageException("commands.fill.usage", new Object[0]);
                }
                sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
                blockpos = CommandFill.parseBlockPos(sender, args, 0, false);
                blockpos1 = CommandFill.parseBlockPos(sender, args, 3, false);
                block = CommandBase.getBlockByText(sender, args[6]);
                iblockstate = args.length >= 8 ? CommandFill.func_190794_a(block, args[7]) : block.getDefaultState();
                blockpos2 = new BlockPos(Math.min(blockpos.getX(), blockpos1.getX()), Math.min(blockpos.getY(), blockpos1.getY()), Math.min(blockpos.getZ(), blockpos1.getZ()));
                blockpos3 = new BlockPos(Math.max(blockpos.getX(), blockpos1.getX()), Math.max(blockpos.getY(), blockpos1.getY()), Math.max(blockpos.getZ(), blockpos1.getZ()));
                i = (blockpos3.getX() - blockpos2.getX() + 1) * (blockpos3.getY() - blockpos2.getY() + 1) * (blockpos3.getZ() - blockpos2.getZ() + 1);
                if (i > 32768) {
                    throw new CommandException("commands.fill.tooManyBlocks", new Object[]{i, 32768});
                }
                if (blockpos2.getY() < 0 || blockpos3.getY() >= 256) break block18;
                world = sender.getEntityWorld();
                j = blockpos2.getZ();
                while (j <= blockpos3.getZ()) {
                    k = blockpos2.getX();
                    while (k <= blockpos3.getX()) {
                        if (!world.isBlockLoaded(new BlockPos(k, blockpos3.getY() - blockpos2.getY(), j))) {
                            throw new CommandException("commands.fill.outOfWorld", new Object[0]);
                        }
                        k += 16;
                    }
                    j += 16;
                }
                nbttagcompound = new NBTTagCompound();
                flag = false;
                if (args.length >= 10 && block.hasTileEntity()) {
                    s = CommandFill.buildString(args, 9);
                    try {
                        nbttagcompound = JsonToNBT.getTagFromJson(s);
                        flag = true;
                    }
                    catch (NBTException nbtexception) {
                        throw new CommandException("commands.fill.tagError", new Object[]{nbtexception.getMessage()});
                    }
                }
                list = Lists.newArrayList();
                i = 0;
                l = blockpos2.getZ();
                while (l <= blockpos3.getZ()) {
                    i1 = blockpos2.getY();
                    while (i1 <= blockpos3.getY()) {
                        j1 = blockpos2.getX();
                        while (j1 <= blockpos3.getX()) {
                            block23: {
                                block19: {
                                    block20: {
                                        block22: {
                                            block21: {
                                                blockpos4 = new BlockPos(j1, i1, l);
                                                if (args.length < 9) break block19;
                                                if ("outline".equals(args[8]) || "hollow".equals(args[8])) break block20;
                                                if (!"destroy".equals(args[8])) break block21;
                                                world.destroyBlock(blockpos4, true);
                                                break block19;
                                            }
                                            if (!"keep".equals(args[8])) break block22;
                                            if (world.isAirBlock(blockpos4)) break block19;
                                            break block23;
                                        }
                                        if (!"replace".equals(args[8]) || block.hasTileEntity() || args.length <= 9) break block19;
                                        block1 = CommandBase.getBlockByText(sender, args[9]);
                                        if (world.getBlockState(blockpos4).getBlock() == block1 && (args.length <= 10 || "-1".equals(args[10]) || "*".equals(args[10]) || CommandBase.func_190791_b(block1, args[10]).apply((Object)world.getBlockState(blockpos4)))) break block19;
                                        break block23;
                                    }
                                    if (j1 != blockpos2.getX() && j1 != blockpos3.getX() && i1 != blockpos2.getY() && i1 != blockpos3.getY() && l != blockpos2.getZ() && l != blockpos3.getZ()) {
                                        if ("hollow".equals(args[8])) {
                                            world.setBlockState(blockpos4, Blocks.AIR.getDefaultState(), 2);
                                            list.add(blockpos4);
                                            ** GOTO lbl81
                                        } else {
                                            ** GOTO lbl68
                                        }
                                    }
                                    break block19;
lbl68:
                                    // 2 sources

                                    break block23;
                                }
                                if ((tileentity1 = world.getTileEntity(blockpos4)) != null && tileentity1 instanceof IInventory) {
                                    ((IInventory)tileentity1).clear();
                                }
                                if (world.setBlockState(blockpos4, iblockstate, 2)) {
                                    list.add(blockpos4);
                                    ++i;
                                    if (flag && (tileentity = world.getTileEntity(blockpos4)) != null) {
                                        nbttagcompound.setInteger("x", blockpos4.getX());
                                        nbttagcompound.setInteger("y", blockpos4.getY());
                                        nbttagcompound.setInteger("z", blockpos4.getZ());
                                        tileentity.readFromNBT(nbttagcompound);
                                    }
                                }
                            }
                            ++j1;
                        }
                        ++i1;
                    }
                    ++l;
                }
                for (BlockPos blockpos5 : list) {
                    block2 = world.getBlockState(blockpos5).getBlock();
                    world.notifyNeighborsRespectDebug(blockpos5, block2, false);
                }
                if (i <= 0) {
                    throw new CommandException("commands.fill.failed", new Object[0]);
                }
                break block24;
            }
            throw new CommandException("commands.fill.outOfWorld", new Object[0]);
        }
        sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, i);
        CommandFill.notifyCommandListener(sender, (ICommand)this, "commands.fill.success", new Object[]{i});
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length > 0 && args.length <= 3) {
            return CommandFill.getTabCompletionCoordinate(args, 0, pos);
        }
        if (args.length > 3 && args.length <= 6) {
            return CommandFill.getTabCompletionCoordinate(args, 3, pos);
        }
        if (args.length == 7) {
            return CommandFill.getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys());
        }
        if (args.length == 9) {
            return CommandFill.getListOfStringsMatchingLastWord(args, "replace", "destroy", "keep", "hollow", "outline");
        }
        return args.length == 10 && "replace".equals(args[8]) ? CommandFill.getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys()) : Collections.emptyList();
    }
}


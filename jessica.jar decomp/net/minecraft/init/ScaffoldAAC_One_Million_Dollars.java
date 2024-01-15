/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.init;

import com.mysql.fabric.Category;
import com.mysql.fabric.Module;
import com.mysql.fabric.Wrapper;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.datafix.BlockData;
import net.minecraft.util.datafix.BlockUtils;
import net.minecraft.util.datafix.RotationUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class ScaffoldAAC_One_Million_Dollars
extends Module {
    public BlockData blockData = null;
    public BlockData blockData1 = null;
    public BlockData blockData2 = null;
    public BlockData blockData3 = null;
    public BlockData blockData4 = null;
    public BlockData blockData5 = null;
    private boolean sneak = false;
    public static boolean noSneak = false;
    private List<Block> blacklist = Arrays.asList(Blocks.AIR, Blocks.WATER, Blocks.FLOWING_WATER, Blocks.LAVA, Blocks.FLOWING_LAVA);
    private BlockData blockdata;
    private int delay;
    private boolean isSprint = false;
    public static float yaw = 0.0f;
    public static float pitch = 0.0f;
    public static float yawHelper = -9999.0f;
    public static float pitchHelper = -9999.0f;
    public static boolean sneaking = false;
    public static boolean sneakingSpartan = false;

    public ScaffoldAAC_One_Million_Dollars() {
        super("Scaffold", Category.Player);
    }

    public Object[] arrayFacing() {
        if (Wrapper.player().getHorizontalFacing() == EnumFacing.SOUTH) {
            return new Object[]{Float.valueOf(180.65749f), new BlockPos(Wrapper.player().posX, Wrapper.player().posY - 1.0, Wrapper.player().posZ - 0.265)};
        }
        if (Wrapper.player().getHorizontalFacing() == EnumFacing.NORTH) {
            return new Object[]{Float.valueOf(360.3425f), new BlockPos(Wrapper.player().posX, Wrapper.player().posY - 1.0, Wrapper.player().posZ + 0.265)};
        }
        if (Wrapper.player().getHorizontalFacing() == EnumFacing.EAST) {
            return new Object[]{Float.valueOf(450.3425f), new BlockPos(Wrapper.player().posX - 0.265, Wrapper.player().posY - 1.0, Wrapper.player().posZ)};
        }
        return new Object[]{Float.valueOf(269.6575f), new BlockPos(Wrapper.player().posX + 0.265, Wrapper.player().posY - 1.0, Wrapper.player().posZ)};
    }

    public BlockData getBlockData(BlockPos pos) {
        if (!this.blacklist.contains(Wrapper.world().getBlockState(pos.add(0, -1, 0)).getBlock())) {
            return new BlockData(pos.add(0, -1, 0), EnumFacing.UP);
        }
        if (!this.blacklist.contains(Wrapper.world().getBlockState(pos.add(-1, 0, 0)).getBlock())) {
            return new BlockData(pos.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (!this.blacklist.contains(Wrapper.world().getBlockState(pos.add(1, 0, 0)).getBlock())) {
            return new BlockData(pos.add(1, 0, 0), EnumFacing.WEST);
        }
        if (!this.blacklist.contains(Wrapper.world().getBlockState(pos.add(0, 0, -1)).getBlock())) {
            return new BlockData(pos.add(0, 0, -1), EnumFacing.SOUTH);
        }
        if (!this.blacklist.contains(Wrapper.world().getBlockState(pos.add(0, 0, 1)).getBlock())) {
            return new BlockData(pos.add(0, 0, 1), EnumFacing.NORTH);
        }
        return null;
    }

    @Override
    public void onEnable() {
        this.isSprint = Wrapper.getModule("Sprint").isToggled();
        if (this.isSprint) {
            try {
                Thread.sleep(10L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            Wrapper.getModule("Sprint").setToggled(false);
            Wrapper.player().setSprinting(false);
        }
        RotationUtils.set(((Float)this.arrayFacing()[0]).floatValue(), 81.70434f);
        Wrapper.sendPacket(new CPacketPlayer.Rotation(Wrapper.player().rotationYaw, Wrapper.player().rotationPitch, Wrapper.player().onGround));
    }

    @Override
    public void onDisable() {
        if (this.isSprint) {
            try {
                Thread.sleep(10L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            Wrapper.getModule("Sprint").setToggled(true);
        }
        Wrapper.sendPacketBypass(new CPacketPlayer.Rotation(Wrapper.player().rotationYaw, Wrapper.player().rotationPitch, Wrapper.player().onGround));
    }

    @Override
    public void onUpdate() {
        if (Wrapper.getModule("AAC").isToggled() || Wrapper.getModule("Spartan").isToggled() || Wrapper.getModule("HardCombat").isToggled()) {
            this.Scaffold_AAC();
        } else {
            this.Scaffold_NCP();
        }
        super.onUpdate();
    }

    private void Scaffold_AAC() {
        try {
            BlockPos blocka = new BlockPos(Wrapper.player().posX, Wrapper.player().posY - 1.0, Wrapper.player().posZ);
            BlockPos blockna = new BlockPos(Wrapper.player().posX, Wrapper.player().posY - 2.0, Wrapper.player().posZ);
            if (Wrapper.player().onGround) {
                RotationUtils.set(((Float)this.arrayFacing()[0]).floatValue(), 81.70434f);
                BlockPos block = (BlockPos)this.arrayFacing()[1];
                this.blockData1 = this.getBlockData(block);
                if (Wrapper.world().getBlockState(block).getBlock() == Blocks.AIR && sneaking) {
                    RayTraceResult.Type cfr_ignored_0 = Wrapper.mc().objectMouseOver.typeOfHit;
                    if (Wrapper.mc().objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                        try {
                            Wrapper.player().motionX = 0.0;
                            Wrapper.player().motionZ = 0.0;
                            int newSlot = -1;
                            int i = 0;
                            while (i < 9) {
                                ItemStack stack = Wrapper.player().inventory.getStackInSlot(i);
                                if (stack != null && stack.getItem() instanceof ItemBlock && Block.getBlockFromItem(stack.getItem()).getDefaultState().getBlock().isFullBlock(Block.getBlockFromItem(stack.getItem()).getDefaultState())) {
                                    newSlot = i;
                                    break;
                                }
                                ++i;
                            }
                            if (newSlot == -1) {
                                return;
                            }
                            int oldSlot = Wrapper.player().inventory.currentItem;
                            Wrapper.player().inventory.currentItem = newSlot;
                            Wrapper.mc().playerController.processRightClickBlock(Wrapper.player(), Wrapper.world(), this.blockData1.position, this.blockData1.face, new Vec3d(this.blockData1.position.getX(), this.blockData1.position.getY(), this.blockData1.position.getZ()), EnumHand.MAIN_HAND);
                            Wrapper.player().swingArm(EnumHand.MAIN_HAND);
                            Wrapper.player().inventory.currentItem = oldSlot;
                        }
                        catch (Exception exception) {}
                    }
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void Scaffold_NCP() {
        BlockPos belowPlayer = new BlockPos(Wrapper.player()).down();
        if (!Wrapper.world().getBlockState(belowPlayer).getBlock().getMaterial(Wrapper.world().getBlockState(belowPlayer).getBlock().getDefaultState()).isReplaceable()) {
            return;
        }
        int newSlot = -1;
        int i = 0;
        while (i < 9) {
            ItemStack stack = Wrapper.player().inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock && Block.getBlockFromItem(stack.getItem()).getDefaultState().getBlock().isFullBlock(Wrapper.world().getBlockState(belowPlayer).getBlock().getDefaultState())) {
                newSlot = i;
                break;
            }
            ++i;
        }
        if (newSlot == -1) {
            return;
        }
        int oldSlot = Wrapper.player().inventory.currentItem;
        Wrapper.player().inventory.currentItem = newSlot;
        BlockUtils.placeBlockScaffold(belowPlayer);
        Wrapper.player().inventory.currentItem = oldSlot;
    }

    private void sneakOn() {
        new Thread(this){
            final /* synthetic */ ScaffoldAAC_One_Million_Dollars this$0;
            {
                this.this$0 = scaffoldAAC_One_Million_Dollars;
            }

            public void run() {
                try {
                    Wrapper.mc().gameSettings.keyBindSneak.pressed = true;
                    ScaffoldAAC_One_Million_Dollars.sneakingSpartan = true;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }.start();
    }

    public static void sneakSlow() {
        new Thread(){

            public void run() {
                try {
                    Thread.sleep(10L);
                    ScaffoldAAC_One_Million_Dollars.sneaking = true;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }.start();
    }
}


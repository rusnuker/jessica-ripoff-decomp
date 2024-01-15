/*
 * Decompiled with CFR 0.152.
 */
package shadersmod.client;

import optifine.MatchBlock;

public class BlockAlias {
    private int blockId;
    private MatchBlock[] matchBlocks;

    public BlockAlias(int blockId, MatchBlock[] matchBlocks) {
        this.blockId = blockId;
        this.matchBlocks = matchBlocks;
    }

    public int getBlockId() {
        return this.blockId;
    }

    public boolean matches(int id, int metadata) {
        int i = 0;
        while (i < this.matchBlocks.length) {
            MatchBlock matchblock = this.matchBlocks[i];
            if (matchblock.matches(id, metadata)) {
                return true;
            }
            ++i;
        }
        return false;
    }

    public int[] getMatchBlockIds() {
        int[] aint = new int[this.matchBlocks.length];
        int i = 0;
        while (i < aint.length) {
            aint[i] = this.matchBlocks[i].getBlockId();
            ++i;
        }
        return aint;
    }
}


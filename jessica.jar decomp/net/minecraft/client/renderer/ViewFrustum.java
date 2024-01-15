/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ViewFrustum {
    protected final RenderGlobal renderGlobal;
    protected final World world;
    protected int countChunksY;
    protected int countChunksX;
    protected int countChunksZ;
    public RenderChunk[] renderChunks;

    public ViewFrustum(World worldIn, int renderDistanceChunks, RenderGlobal renderGlobalIn, IRenderChunkFactory renderChunkFactory) {
        this.renderGlobal = renderGlobalIn;
        this.world = worldIn;
        this.setCountChunksXYZ(renderDistanceChunks);
        this.createRenderChunks(renderChunkFactory);
    }

    protected void createRenderChunks(IRenderChunkFactory renderChunkFactory) {
        int i = this.countChunksX * this.countChunksY * this.countChunksZ;
        this.renderChunks = new RenderChunk[i];
        int j = 0;
        int k = 0;
        while (k < this.countChunksX) {
            int l = 0;
            while (l < this.countChunksY) {
                int i1 = 0;
                while (i1 < this.countChunksZ) {
                    int j1 = (i1 * this.countChunksY + l) * this.countChunksX + k;
                    this.renderChunks[j1] = renderChunkFactory.create(this.world, this.renderGlobal, j++);
                    this.renderChunks[j1].setPosition(k * 16, l * 16, i1 * 16);
                    ++i1;
                }
                ++l;
            }
            ++k;
        }
    }

    public void deleteGlResources() {
        RenderChunk[] renderChunkArray = this.renderChunks;
        int n = this.renderChunks.length;
        int n2 = 0;
        while (n2 < n) {
            RenderChunk renderchunk = renderChunkArray[n2];
            renderchunk.deleteGlResources();
            ++n2;
        }
    }

    protected void setCountChunksXYZ(int renderDistanceChunks) {
        int i;
        this.countChunksX = i = renderDistanceChunks * 2 + 1;
        this.countChunksY = 16;
        this.countChunksZ = i;
    }

    public void updateChunkPositions(double viewEntityX, double viewEntityZ) {
        int i = MathHelper.floor(viewEntityX) - 8;
        int j = MathHelper.floor(viewEntityZ) - 8;
        int k = this.countChunksX * 16;
        int l = 0;
        while (l < this.countChunksX) {
            int i1 = this.getBaseCoordinate(i, k, l);
            int j1 = 0;
            while (j1 < this.countChunksZ) {
                int k1 = this.getBaseCoordinate(j, k, j1);
                int l1 = 0;
                while (l1 < this.countChunksY) {
                    int i2 = l1 * 16;
                    RenderChunk renderchunk = this.renderChunks[(j1 * this.countChunksY + l1) * this.countChunksX + l];
                    renderchunk.setPosition(i1, i2, k1);
                    ++l1;
                }
                ++j1;
            }
            ++l;
        }
    }

    private int getBaseCoordinate(int p_178157_1_, int p_178157_2_, int p_178157_3_) {
        int i = p_178157_3_ * 16;
        int j = i - p_178157_1_ + p_178157_2_ / 2;
        if (j < 0) {
            j -= p_178157_2_ - 1;
        }
        return i - j / p_178157_2_ * p_178157_2_;
    }

    public void markBlocksForUpdate(int p_187474_1_, int p_187474_2_, int p_187474_3_, int p_187474_4_, int p_187474_5_, int p_187474_6_, boolean p_187474_7_) {
        int i = MathHelper.intFloorDiv(p_187474_1_, 16);
        int j = MathHelper.intFloorDiv(p_187474_2_, 16);
        int k = MathHelper.intFloorDiv(p_187474_3_, 16);
        int l = MathHelper.intFloorDiv(p_187474_4_, 16);
        int i1 = MathHelper.intFloorDiv(p_187474_5_, 16);
        int j1 = MathHelper.intFloorDiv(p_187474_6_, 16);
        int k1 = i;
        while (k1 <= l) {
            int l1 = k1 % this.countChunksX;
            if (l1 < 0) {
                l1 += this.countChunksX;
            }
            int i2 = j;
            while (i2 <= i1) {
                int j2 = i2 % this.countChunksY;
                if (j2 < 0) {
                    j2 += this.countChunksY;
                }
                int k2 = k;
                while (k2 <= j1) {
                    int l2 = k2 % this.countChunksZ;
                    if (l2 < 0) {
                        l2 += this.countChunksZ;
                    }
                    int i3 = (l2 * this.countChunksY + j2) * this.countChunksX + l1;
                    RenderChunk renderchunk = this.renderChunks[i3];
                    renderchunk.setNeedsUpdate(p_187474_7_);
                    ++k2;
                }
                ++i2;
            }
            ++k1;
        }
    }

    @Nullable
    public RenderChunk getRenderChunk(BlockPos pos) {
        int i = pos.getX() >> 4;
        int j = pos.getY() >> 4;
        int k = pos.getZ() >> 4;
        if (j >= 0 && j < this.countChunksY) {
            if ((i %= this.countChunksX) < 0) {
                i += this.countChunksX;
            }
            if ((k %= this.countChunksZ) < 0) {
                k += this.countChunksZ;
            }
            int l = (k * this.countChunksY + j) * this.countChunksX + i;
            return this.renderChunks[l];
        }
        return null;
    }
}


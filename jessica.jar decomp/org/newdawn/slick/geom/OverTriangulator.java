/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.geom;

import org.newdawn.slick.geom.Triangulator;

public class OverTriangulator
implements Triangulator {
    private float[][] triangles;

    public OverTriangulator(Triangulator tris) {
        this.triangles = new float[tris.getTriangleCount() * 6 * 3][2];
        int tcount = 0;
        int i = 0;
        while (i < tris.getTriangleCount()) {
            float[] pt2;
            float[] pt1;
            float cx = 0.0f;
            float cy = 0.0f;
            int p = 0;
            while (p < 3) {
                float[] pt = tris.getTrianglePoint(i, p);
                cx += pt[0];
                cy += pt[1];
                ++p;
            }
            cx /= 3.0f;
            cy /= 3.0f;
            p = 0;
            while (p < 3) {
                int n = p + 1;
                if (n > 2) {
                    n = 0;
                }
                pt1 = tris.getTrianglePoint(i, p);
                pt2 = tris.getTrianglePoint(i, n);
                pt1[0] = (pt1[0] + pt2[0]) / 2.0f;
                pt1[1] = (pt1[1] + pt2[1]) / 2.0f;
                this.triangles[tcount * 3 + 0][0] = cx;
                this.triangles[tcount * 3 + 0][1] = cy;
                this.triangles[tcount * 3 + 1][0] = pt1[0];
                this.triangles[tcount * 3 + 1][1] = pt1[1];
                this.triangles[tcount * 3 + 2][0] = pt2[0];
                this.triangles[tcount * 3 + 2][1] = pt2[1];
                ++tcount;
                ++p;
            }
            p = 0;
            while (p < 3) {
                int n = p + 1;
                if (n > 2) {
                    n = 0;
                }
                pt1 = tris.getTrianglePoint(i, p);
                pt2 = tris.getTrianglePoint(i, n);
                pt2[0] = (pt1[0] + pt2[0]) / 2.0f;
                pt2[1] = (pt1[1] + pt2[1]) / 2.0f;
                this.triangles[tcount * 3 + 0][0] = cx;
                this.triangles[tcount * 3 + 0][1] = cy;
                this.triangles[tcount * 3 + 1][0] = pt1[0];
                this.triangles[tcount * 3 + 1][1] = pt1[1];
                this.triangles[tcount * 3 + 2][0] = pt2[0];
                this.triangles[tcount * 3 + 2][1] = pt2[1];
                ++tcount;
                ++p;
            }
            ++i;
        }
    }

    @Override
    public void addPolyPoint(float x, float y) {
    }

    @Override
    public int getTriangleCount() {
        return this.triangles.length / 3;
    }

    @Override
    public float[] getTrianglePoint(int tri, int i) {
        float[] pt = this.triangles[tri * 3 + i];
        return new float[]{pt[0], pt[1]};
    }

    @Override
    public void startHole() {
    }

    @Override
    public boolean triangulate() {
        return true;
    }
}


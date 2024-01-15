/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.geom;

import java.io.Serializable;
import org.newdawn.slick.geom.GeomUtil;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.NeatTriangulator;
import org.newdawn.slick.geom.OverTriangulator;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.geom.Triangulator;
import org.newdawn.slick.geom.Vector2f;

public abstract class Shape
implements Serializable {
    protected float[] points;
    protected float[] center;
    protected float x;
    protected float y;
    protected float maxX;
    protected float maxY;
    protected float minX;
    protected float minY;
    protected float boundingCircleRadius;
    protected boolean pointsDirty = true;
    protected transient Triangulator tris;
    protected boolean trianglesDirty;

    public void setLocation(float x, float y) {
        this.setX(x);
        this.setY(y);
    }

    public abstract Shape transform(Transform var1);

    protected abstract void createPoints();

    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        if (x != this.x) {
            float dx = x - this.x;
            this.x = x;
            if (this.points == null || this.center == null) {
                this.checkPoints();
            }
            int i = 0;
            while (i < this.points.length / 2) {
                int n = i * 2;
                this.points[n] = this.points[n] + dx;
                ++i;
            }
            this.center[0] = this.center[0] + dx;
            x += dx;
            this.maxX += dx;
            this.minX += dx;
            this.trianglesDirty = true;
        }
    }

    public void setY(float y) {
        if (y != this.y) {
            float dy = y - this.y;
            this.y = y;
            if (this.points == null || this.center == null) {
                this.checkPoints();
            }
            int i = 0;
            while (i < this.points.length / 2) {
                int n = i * 2 + 1;
                this.points[n] = this.points[n] + dy;
                ++i;
            }
            this.center[1] = this.center[1] + dy;
            y += dy;
            this.maxY += dy;
            this.minY += dy;
            this.trianglesDirty = true;
        }
    }

    public float getY() {
        return this.y;
    }

    public Vector2f getLocation() {
        return new Vector2f(this.getX(), this.getY());
    }

    public void setLocation(Vector2f loc) {
        this.setX(loc.x);
        this.setY(loc.y);
    }

    public float getCenterX() {
        this.checkPoints();
        return this.center[0];
    }

    public void setCenterX(float centerX) {
        if (this.points == null || this.center == null) {
            this.checkPoints();
        }
        float xDiff = centerX - this.getCenterX();
        this.setX(this.x + xDiff);
    }

    public float getCenterY() {
        this.checkPoints();
        return this.center[1];
    }

    public void setCenterY(float centerY) {
        if (this.points == null || this.center == null) {
            this.checkPoints();
        }
        float yDiff = centerY - this.getCenterY();
        this.setY(this.y + yDiff);
    }

    public float getMaxX() {
        this.checkPoints();
        return this.maxX;
    }

    public float getMaxY() {
        this.checkPoints();
        return this.maxY;
    }

    public float getMinX() {
        this.checkPoints();
        return this.minX;
    }

    public float getMinY() {
        this.checkPoints();
        return this.minY;
    }

    public float getBoundingCircleRadius() {
        this.checkPoints();
        return this.boundingCircleRadius;
    }

    public float[] getCenter() {
        this.checkPoints();
        return this.center;
    }

    public float[] getPoints() {
        this.checkPoints();
        return this.points;
    }

    public int getPointCount() {
        this.checkPoints();
        return this.points.length / 2;
    }

    public float[] getPoint(int index) {
        this.checkPoints();
        float[] result = new float[]{this.points[index * 2], this.points[index * 2 + 1]};
        return result;
    }

    public float[] getNormal(int index) {
        float[] current = this.getPoint(index);
        float[] prev = this.getPoint(index - 1 < 0 ? this.getPointCount() - 1 : index - 1);
        float[] next = this.getPoint(index + 1 >= this.getPointCount() ? 0 : index + 1);
        float[] t1 = this.getNormal(prev, current);
        float[] t2 = this.getNormal(current, next);
        if (index == 0 && !this.closed()) {
            return t2;
        }
        if (index == this.getPointCount() - 1 && !this.closed()) {
            return t1;
        }
        float tx = (t1[0] + t2[0]) / 2.0f;
        float ty = (t1[1] + t2[1]) / 2.0f;
        float len = (float)Math.sqrt(tx * tx + ty * ty);
        return new float[]{tx / len, ty / len};
    }

    public boolean contains(Shape other) {
        if (other.intersects(this)) {
            return false;
        }
        int i = 0;
        while (i < other.getPointCount()) {
            float[] pt = other.getPoint(i);
            if (!this.contains(pt[0], pt[1])) {
                return false;
            }
            ++i;
        }
        return true;
    }

    private float[] getNormal(float[] start, float[] end) {
        float dx = start[0] - end[0];
        float dy = start[1] - end[1];
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        return new float[]{-(dy /= len), dx /= len};
    }

    public boolean includes(float x, float y) {
        if (this.points.length == 0) {
            return false;
        }
        this.checkPoints();
        Line testLine = new Line(0.0f, 0.0f, 0.0f, 0.0f);
        Vector2f pt = new Vector2f(x, y);
        int i = 0;
        while (i < this.points.length) {
            int n = i + 2;
            if (n >= this.points.length) {
                n = 0;
            }
            testLine.set(this.points[i], this.points[i + 1], this.points[n], this.points[n + 1]);
            if (testLine.on(pt)) {
                return true;
            }
            i += 2;
        }
        return false;
    }

    public int indexOf(float x, float y) {
        int i = 0;
        while (i < this.points.length) {
            if (this.points[i] == x && this.points[i + 1] == y) {
                return i / 2;
            }
            i += 2;
        }
        return -1;
    }

    public boolean contains(float x, float y) {
        this.checkPoints();
        if (this.points.length == 0) {
            return false;
        }
        boolean result = false;
        int npoints = this.points.length;
        float xold = this.points[npoints - 2];
        float yold = this.points[npoints - 1];
        int i = 0;
        while (i < npoints) {
            float y2;
            float y1;
            float x2;
            float x1;
            float xnew = this.points[i];
            float ynew = this.points[i + 1];
            if (xnew > xold) {
                x1 = xold;
                x2 = xnew;
                y1 = yold;
                y2 = ynew;
            } else {
                x1 = xnew;
                x2 = xold;
                y1 = ynew;
                y2 = yold;
            }
            if (xnew < x == x <= xold && ((double)y - (double)y1) * (double)(x2 - x1) < ((double)y2 - (double)y1) * (double)(x - x1)) {
                result = !result;
            }
            xold = xnew;
            yold = ynew;
            i += 2;
        }
        return result;
    }

    public boolean intersects(Shape shape) {
        this.checkPoints();
        boolean result = false;
        float[] points = this.getPoints();
        float[] thatPoints = shape.getPoints();
        int length = points.length;
        int thatLength = thatPoints.length;
        if (!this.closed()) {
            length -= 2;
        }
        if (!shape.closed()) {
            thatLength -= 2;
        }
        int i = 0;
        while (i < length) {
            int iNext = i + 2;
            if (iNext >= points.length) {
                iNext = 0;
            }
            int j = 0;
            while (j < thatLength) {
                int jNext = j + 2;
                if (jNext >= thatPoints.length) {
                    jNext = 0;
                }
                double unknownA = ((double)(points[iNext] - points[i]) * (double)(thatPoints[j + 1] - points[i + 1]) - (double)((points[iNext + 1] - points[i + 1]) * (thatPoints[j] - points[i]))) / (double)((points[iNext + 1] - points[i + 1]) * (thatPoints[jNext] - thatPoints[j]) - (points[iNext] - points[i]) * (thatPoints[jNext + 1] - thatPoints[j + 1]));
                double unknownB = ((double)(thatPoints[jNext] - thatPoints[j]) * (double)(thatPoints[j + 1] - points[i + 1]) - (double)((thatPoints[jNext + 1] - thatPoints[j + 1]) * (thatPoints[j] - points[i]))) / (double)((points[iNext + 1] - points[i + 1]) * (thatPoints[jNext] - thatPoints[j]) - (points[iNext] - points[i]) * (thatPoints[jNext + 1] - thatPoints[j + 1]));
                if (unknownA >= 0.0 && unknownA <= 1.0 && unknownB >= 0.0 && unknownB <= 1.0) {
                    result = true;
                    break;
                }
                j += 2;
            }
            if (result) break;
            i += 2;
        }
        return result;
    }

    public boolean hasVertex(float x, float y) {
        if (this.points.length == 0) {
            return false;
        }
        this.checkPoints();
        int i = 0;
        while (i < this.points.length) {
            if (this.points[i] == x && this.points[i + 1] == y) {
                return true;
            }
            i += 2;
        }
        return false;
    }

    protected void findCenter() {
        this.center = new float[]{0.0f, 0.0f};
        int length = this.points.length;
        int i = 0;
        while (i < length) {
            this.center[0] = this.center[0] + this.points[i];
            this.center[1] = this.center[1] + this.points[i + 1];
            i += 2;
        }
        this.center[0] = this.center[0] / (float)(length / 2);
        this.center[1] = this.center[1] / (float)(length / 2);
    }

    protected void calculateRadius() {
        this.boundingCircleRadius = 0.0f;
        int i = 0;
        while (i < this.points.length) {
            float temp = (this.points[i] - this.center[0]) * (this.points[i] - this.center[0]) + (this.points[i + 1] - this.center[1]) * (this.points[i + 1] - this.center[1]);
            this.boundingCircleRadius = this.boundingCircleRadius > temp ? this.boundingCircleRadius : temp;
            i += 2;
        }
        this.boundingCircleRadius = (float)Math.sqrt(this.boundingCircleRadius);
    }

    protected void calculateTriangles() {
        if (!this.trianglesDirty && this.tris != null) {
            return;
        }
        if (this.points.length >= 6) {
            boolean clockwise = true;
            float area = 0.0f;
            int i = 0;
            while (i < this.points.length / 2 - 1) {
                float x1 = this.points[i * 2];
                float y1 = this.points[i * 2 + 1];
                float x2 = this.points[i * 2 + 2];
                float y2 = this.points[i * 2 + 3];
                area += x1 * y2 - y1 * x2;
                ++i;
            }
            clockwise = (area /= 2.0f) > 0.0f;
            this.tris = new NeatTriangulator();
            i = 0;
            while (i < this.points.length) {
                this.tris.addPolyPoint(this.points[i], this.points[i + 1]);
                i += 2;
            }
            this.tris.triangulate();
        }
        this.trianglesDirty = false;
    }

    public void increaseTriangulation() {
        this.checkPoints();
        this.calculateTriangles();
        this.tris = new OverTriangulator(this.tris);
    }

    public Triangulator getTriangles() {
        this.checkPoints();
        this.calculateTriangles();
        return this.tris;
    }

    protected final void checkPoints() {
        if (this.pointsDirty) {
            this.createPoints();
            this.findCenter();
            this.calculateRadius();
            if (this.points.length > 0) {
                this.maxX = this.points[0];
                this.maxY = this.points[1];
                this.minX = this.points[0];
                this.minY = this.points[1];
                int i = 0;
                while (i < this.points.length / 2) {
                    this.maxX = Math.max(this.points[i * 2], this.maxX);
                    this.maxY = Math.max(this.points[i * 2 + 1], this.maxY);
                    this.minX = Math.min(this.points[i * 2], this.minX);
                    this.minY = Math.min(this.points[i * 2 + 1], this.minY);
                    ++i;
                }
            }
            this.pointsDirty = false;
            this.trianglesDirty = true;
        }
    }

    public void preCache() {
        this.checkPoints();
        this.getTriangles();
    }

    public boolean closed() {
        return true;
    }

    public Shape prune() {
        Polygon result = new Polygon();
        int i = 0;
        while (i < this.getPointCount()) {
            float len2;
            float len1;
            int next = i + 1 >= this.getPointCount() ? 0 : i + 1;
            int prev = i - 1 < 0 ? this.getPointCount() - 1 : i - 1;
            float dx1 = this.getPoint(i)[0] - this.getPoint(prev)[0];
            float dy1 = this.getPoint(i)[1] - this.getPoint(prev)[1];
            float dx2 = this.getPoint(next)[0] - this.getPoint(i)[0];
            float dy2 = this.getPoint(next)[1] - this.getPoint(i)[1];
            if ((dx1 /= (len1 = (float)Math.sqrt(dx1 * dx1 + dy1 * dy1))) != (dx2 /= (len2 = (float)Math.sqrt(dx2 * dx2 + dy2 * dy2))) || (dy1 /= len1) != (dy2 /= len2)) {
                result.addPoint(this.getPoint(i)[0], this.getPoint(i)[1]);
            }
            ++i;
        }
        return result;
    }

    public Shape[] subtract(Shape other) {
        return new GeomUtil().subtract(this, other);
    }

    public Shape[] union(Shape other) {
        return new GeomUtil().union(this, other);
    }

    public float getWidth() {
        return this.maxX - this.minX;
    }

    public float getHeight() {
        return this.maxY - this.minY;
    }
}


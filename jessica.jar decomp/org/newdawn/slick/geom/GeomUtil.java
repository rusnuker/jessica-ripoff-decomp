/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.geom;

import java.util.ArrayList;
import org.newdawn.slick.geom.GeomUtilListener;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.geom.Vector2f;

public class GeomUtil {
    public float EPSILON = 1.0E-4f;
    public float EDGE_SCALE = 1.0f;
    public int MAX_POINTS = 10000;
    public GeomUtilListener listener;

    public Shape[] subtract(Shape target, Shape missing) {
        target = target.transform(new Transform());
        missing = missing.transform(new Transform());
        int count = 0;
        int i = 0;
        while (i < target.getPointCount()) {
            if (missing.contains(target.getPoint(i)[0], target.getPoint(i)[1])) {
                ++count;
            }
            ++i;
        }
        if (count == target.getPointCount()) {
            return new Shape[0];
        }
        if (!target.intersects(missing)) {
            return new Shape[]{target};
        }
        int found = 0;
        int i2 = 0;
        while (i2 < missing.getPointCount()) {
            if (target.contains(missing.getPoint(i2)[0], missing.getPoint(i2)[1]) && !this.onPath(target, missing.getPoint(i2)[0], missing.getPoint(i2)[1])) {
                ++found;
            }
            ++i2;
        }
        i2 = 0;
        while (i2 < target.getPointCount()) {
            if (missing.contains(target.getPoint(i2)[0], target.getPoint(i2)[1]) && !this.onPath(missing, target.getPoint(i2)[0], target.getPoint(i2)[1])) {
                ++found;
            }
            ++i2;
        }
        if (found < 1) {
            return new Shape[]{target};
        }
        return this.combine(target, missing, true);
    }

    private boolean onPath(Shape path, float x, float y) {
        int i = 0;
        while (i < path.getPointCount() + 1) {
            int n = GeomUtil.rationalPoint(path, i + 1);
            Line line = this.getLine(path, GeomUtil.rationalPoint(path, i), n);
            Vector2f vector2f = new Vector2f(x, y);
            if (line.distance(vector2f) < this.EPSILON * 100.0f) {
                return true;
            }
            ++i;
        }
        return false;
    }

    public void setListener(GeomUtilListener listener) {
        this.listener = listener;
    }

    public Shape[] union(Shape target, Shape other) {
        if (!(target = target.transform(new Transform())).intersects(other = other.transform(new Transform()))) {
            return new Shape[]{target, other};
        }
        boolean touches = false;
        int buttCount = 0;
        int i = 0;
        while (i < target.getPointCount()) {
            if (other.contains(target.getPoint(i)[0], target.getPoint(i)[1]) && !other.hasVertex(target.getPoint(i)[0], target.getPoint(i)[1])) {
                touches = true;
                break;
            }
            if (other.hasVertex(target.getPoint(i)[0], target.getPoint(i)[1])) {
                ++buttCount;
            }
            ++i;
        }
        i = 0;
        while (i < other.getPointCount()) {
            if (target.contains(other.getPoint(i)[0], other.getPoint(i)[1]) && !target.hasVertex(other.getPoint(i)[0], other.getPoint(i)[1])) {
                touches = true;
                break;
            }
            ++i;
        }
        if (!touches && buttCount < 2) {
            return new Shape[]{target, other};
        }
        return this.combine(target, other, false);
    }

    private Shape[] combine(Shape target, Shape other, boolean subtract) {
        if (subtract) {
            float[] point;
            ArrayList<Shape> shapes = new ArrayList<Shape>();
            ArrayList<Vector2f> used = new ArrayList<Vector2f>();
            int i = 0;
            while (i < target.getPointCount()) {
                point = target.getPoint(i);
                if (other.contains(point[0], point[1])) {
                    used.add(new Vector2f(point[0], point[1]));
                    if (this.listener != null) {
                        this.listener.pointExcluded(point[0], point[1]);
                    }
                }
                ++i;
            }
            i = 0;
            while (i < target.getPointCount()) {
                point = target.getPoint(i);
                Vector2f pt = new Vector2f(point[0], point[1]);
                if (!used.contains(pt)) {
                    Shape result = this.combineSingle(target, other, true, i);
                    shapes.add(result);
                    int j = 0;
                    while (j < result.getPointCount()) {
                        float[] kpoint = result.getPoint(j);
                        Vector2f kpt = new Vector2f(kpoint[0], kpoint[1]);
                        used.add(kpt);
                        ++j;
                    }
                }
                ++i;
            }
            return shapes.toArray(new Shape[0]);
        }
        int i = 0;
        while (i < target.getPointCount()) {
            if (!other.contains(target.getPoint(i)[0], target.getPoint(i)[1]) && !other.hasVertex(target.getPoint(i)[0], target.getPoint(i)[1])) {
                Shape shape = this.combineSingle(target, other, false, i);
                return new Shape[]{shape};
            }
            ++i;
        }
        return new Shape[]{other};
    }

    private Shape combineSingle(Shape target, Shape missing, boolean subtract, int start) {
        Shape current = target;
        Shape other = missing;
        int point = start;
        int dir = 1;
        Polygon poly = new Polygon();
        boolean first = true;
        int loop = 0;
        float px = current.getPoint(point)[0];
        float py = current.getPoint(point)[1];
        while (!poly.hasVertex(px, py) || first || current != target) {
            Line line;
            HitResult hit;
            first = false;
            if (++loop > this.MAX_POINTS) break;
            poly.addPoint(px, py);
            if (this.listener != null) {
                this.listener.pointUsed(px, py);
            }
            if ((hit = this.intersect(other, line = this.getLine(current, px, py, GeomUtil.rationalPoint(current, point + dir)))) != null) {
                Shape temp;
                Line hitLine = hit.line;
                Vector2f pt = hit.pt;
                px = pt.x;
                py = pt.y;
                if (this.listener != null) {
                    this.listener.pointIntersected(px, py);
                }
                if (other.hasVertex(px, py)) {
                    point = other.indexOf(pt.x, pt.y);
                    dir = 1;
                    px = pt.x;
                    py = pt.y;
                    Shape temp2 = current;
                    current = other;
                    other = temp2;
                    continue;
                }
                float dx = hitLine.getDX() / hitLine.length();
                float dy = hitLine.getDY() / hitLine.length();
                if (current.contains(pt.x + (dx *= this.EDGE_SCALE), pt.y + (dy *= this.EDGE_SCALE))) {
                    if (subtract) {
                        if (current == missing) {
                            point = hit.p2;
                            dir = -1;
                        } else {
                            point = hit.p1;
                            dir = 1;
                        }
                    } else if (current == target) {
                        point = hit.p2;
                        dir = -1;
                    } else {
                        point = hit.p2;
                        dir = -1;
                    }
                    temp = current;
                    current = other;
                    other = temp;
                    continue;
                }
                if (current.contains(pt.x - dx, pt.y - dy)) {
                    if (subtract) {
                        if (current == target) {
                            point = hit.p2;
                            dir = -1;
                        } else {
                            point = hit.p1;
                            dir = 1;
                        }
                    } else if (current == missing) {
                        point = hit.p1;
                        dir = 1;
                    } else {
                        point = hit.p1;
                        dir = 1;
                    }
                    temp = current;
                    current = other;
                    other = temp;
                    continue;
                }
                if (subtract) break;
                point = hit.p1;
                dir = 1;
                temp = current;
                current = other;
                other = temp;
                point = GeomUtil.rationalPoint(current, point + dir);
                px = current.getPoint(point)[0];
                py = current.getPoint(point)[1];
                continue;
            }
            point = GeomUtil.rationalPoint(current, point + dir);
            px = current.getPoint(point)[0];
            py = current.getPoint(point)[1];
        }
        poly.addPoint(px, py);
        if (this.listener != null) {
            this.listener.pointUsed(px, py);
        }
        return poly;
    }

    public HitResult intersect(Shape shape, Line line) {
        float distance = Float.MAX_VALUE;
        HitResult hit = null;
        int i = 0;
        while (i < shape.getPointCount()) {
            float newDis;
            int next = GeomUtil.rationalPoint(shape, i + 1);
            Line local = this.getLine(shape, i, next);
            Vector2f pt = line.intersect(local, true);
            if (pt != null && (newDis = pt.distance(line.getStart())) < distance && newDis > this.EPSILON) {
                hit = new HitResult();
                hit.pt = pt;
                hit.line = local;
                hit.p1 = i;
                hit.p2 = next;
                distance = newDis;
            }
            ++i;
        }
        return hit;
    }

    public static int rationalPoint(Shape shape, int p) {
        while (p < 0) {
            p += shape.getPointCount();
        }
        while (p >= shape.getPointCount()) {
            p -= shape.getPointCount();
        }
        return p;
    }

    public Line getLine(Shape shape, int s, int e) {
        float[] start = shape.getPoint(s);
        float[] end = shape.getPoint(e);
        Line line = new Line(start[0], start[1], end[0], end[1]);
        return line;
    }

    public Line getLine(Shape shape, float sx, float sy, int e) {
        float[] end = shape.getPoint(e);
        Line line = new Line(sx, sy, end[0], end[1]);
        return line;
    }

    public class HitResult {
        public Line line;
        public int p1;
        public int p2;
        public Vector2f pt;
    }
}


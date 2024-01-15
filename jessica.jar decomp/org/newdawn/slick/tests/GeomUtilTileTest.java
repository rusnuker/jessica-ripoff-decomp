/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.tests;

import java.util.ArrayList;
import java.util.HashSet;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.GeomUtil;
import org.newdawn.slick.geom.GeomUtilListener;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Vector2f;

public class GeomUtilTileTest
extends BasicGame
implements GeomUtilListener {
    private Shape source;
    private Shape cut;
    private Shape[] result;
    private GeomUtil util = new GeomUtil();
    private ArrayList original = new ArrayList();
    private ArrayList combined = new ArrayList();
    private ArrayList intersections = new ArrayList();
    private ArrayList used = new ArrayList();
    private ArrayList[][] quadSpace;
    private Shape[][] quadSpaceShapes;

    public GeomUtilTileTest() {
        super("GeomUtilTileTest");
    }

    private void generateSpace(ArrayList shapes, float minx, float miny, float maxx, float maxy, int segments) {
        this.quadSpace = new ArrayList[segments][segments];
        this.quadSpaceShapes = new Shape[segments][segments];
        float dx = (maxx - minx) / (float)segments;
        float dy = (maxy - miny) / (float)segments;
        int x = 0;
        while (x < segments) {
            int y = 0;
            while (y < segments) {
                this.quadSpace[x][y] = new ArrayList();
                Polygon segmentPolygon = new Polygon();
                segmentPolygon.addPoint(minx + dx * (float)x, miny + dy * (float)y);
                segmentPolygon.addPoint(minx + dx * (float)x + dx, miny + dy * (float)y);
                segmentPolygon.addPoint(minx + dx * (float)x + dx, miny + dy * (float)y + dy);
                segmentPolygon.addPoint(minx + dx * (float)x, miny + dy * (float)y + dy);
                int i = 0;
                while (i < shapes.size()) {
                    Shape shape = (Shape)shapes.get(i);
                    if (this.collides(shape, segmentPolygon)) {
                        this.quadSpace[x][y].add(shape);
                    }
                    ++i;
                }
                this.quadSpaceShapes[x][y] = segmentPolygon;
                ++y;
            }
            ++x;
        }
    }

    private void removeFromQuadSpace(Shape shape) {
        int segments = this.quadSpace.length;
        int x = 0;
        while (x < segments) {
            int y = 0;
            while (y < segments) {
                this.quadSpace[x][y].remove(shape);
                ++y;
            }
            ++x;
        }
    }

    private void addToQuadSpace(Shape shape) {
        int segments = this.quadSpace.length;
        int x = 0;
        while (x < segments) {
            int y = 0;
            while (y < segments) {
                if (this.collides(shape, this.quadSpaceShapes[x][y])) {
                    this.quadSpace[x][y].add(shape);
                }
                ++y;
            }
            ++x;
        }
    }

    public void init() {
        int size = 10;
        int[][] nArrayArray = new int[10][];
        int[] nArray = new int[10];
        nArray[7] = 3;
        nArrayArray[0] = nArray;
        int[] nArray2 = new int[10];
        nArray2[1] = 1;
        nArray2[2] = 1;
        nArray2[3] = 1;
        nArray2[6] = 1;
        nArray2[7] = 1;
        nArray2[8] = 1;
        nArrayArray[1] = nArray2;
        int[] nArray3 = new int[10];
        nArray3[1] = 1;
        nArray3[2] = 1;
        nArray3[6] = 5;
        nArray3[7] = 1;
        nArray3[8] = 6;
        nArrayArray[2] = nArray3;
        int[] nArray4 = new int[10];
        nArray4[1] = 1;
        nArray4[2] = 2;
        nArray4[6] = 4;
        nArray4[7] = 1;
        nArray4[8] = 1;
        nArrayArray[3] = nArray4;
        int[] nArray5 = new int[10];
        nArray5[1] = 1;
        nArray5[2] = 1;
        nArray5[6] = 1;
        nArray5[7] = 1;
        nArrayArray[4] = nArray5;
        int[] nArray6 = new int[10];
        nArray6[4] = 3;
        nArray6[6] = 1;
        nArray6[7] = 1;
        nArrayArray[5] = nArray6;
        int[] nArray7 = new int[10];
        nArray7[3] = 1;
        nArray7[4] = 1;
        nArray7[8] = 1;
        nArrayArray[6] = nArray7;
        int[] nArray8 = new int[10];
        nArray8[3] = 1;
        nArray8[4] = 1;
        nArrayArray[7] = nArray8;
        nArrayArray[8] = new int[10];
        nArrayArray[9] = new int[10];
        int[][] map = nArrayArray;
        int x = 0;
        while (x < map[0].length) {
            int y = 0;
            while (y < map.length) {
                if (map[y][x] != 0) {
                    switch (map[y][x]) {
                        case 1: {
                            Polygon p2 = new Polygon();
                            p2.addPoint(x * 32, y * 32);
                            p2.addPoint(x * 32 + 32, y * 32);
                            p2.addPoint(x * 32 + 32, y * 32 + 32);
                            p2.addPoint(x * 32, y * 32 + 32);
                            this.original.add(p2);
                            break;
                        }
                        case 2: {
                            Polygon poly = new Polygon();
                            poly.addPoint(x * 32, y * 32);
                            poly.addPoint(x * 32 + 32, y * 32);
                            poly.addPoint(x * 32, y * 32 + 32);
                            this.original.add(poly);
                            break;
                        }
                        case 3: {
                            Circle ellipse = new Circle((float)(x * 32 + 16), (float)(y * 32 + 32), 16.0f, 16);
                            this.original.add(ellipse);
                            break;
                        }
                        case 4: {
                            Polygon p = new Polygon();
                            p.addPoint(x * 32 + 32, y * 32);
                            p.addPoint(x * 32 + 32, y * 32 + 32);
                            p.addPoint(x * 32, y * 32 + 32);
                            this.original.add(p);
                            break;
                        }
                        case 5: {
                            Polygon p3 = new Polygon();
                            p3.addPoint(x * 32, y * 32);
                            p3.addPoint(x * 32 + 32, y * 32);
                            p3.addPoint(x * 32 + 32, y * 32 + 32);
                            this.original.add(p3);
                            break;
                        }
                        case 6: {
                            Polygon p4 = new Polygon();
                            p4.addPoint(x * 32, y * 32);
                            p4.addPoint(x * 32 + 32, y * 32);
                            p4.addPoint(x * 32, y * 32 + 32);
                            this.original.add(p4);
                        }
                    }
                }
                ++y;
            }
            ++x;
        }
        long before = System.currentTimeMillis();
        this.generateSpace(this.original, 0.0f, 0.0f, (size + 1) * 32, (size + 1) * 32, 8);
        this.combined = this.combineQuadSpace();
        long after = System.currentTimeMillis();
        System.out.println("Combine took: " + (after - before));
        System.out.println("Combine result: " + this.combined.size());
    }

    private ArrayList combineQuadSpace() {
        boolean updated = true;
        while (updated) {
            updated = false;
            int x = 0;
            while (x < this.quadSpace.length) {
                int y = 0;
                while (y < this.quadSpace.length) {
                    ArrayList shapes = this.quadSpace[x][y];
                    int before = shapes.size();
                    this.combine(shapes);
                    int after = shapes.size();
                    updated |= before != after;
                    ++y;
                }
                ++x;
            }
        }
        HashSet result = new HashSet();
        int x = 0;
        while (x < this.quadSpace.length) {
            int y = 0;
            while (y < this.quadSpace.length) {
                result.addAll(this.quadSpace[x][y]);
                ++y;
            }
            ++x;
        }
        return new ArrayList(result);
    }

    private ArrayList combine(ArrayList shapes) {
        ArrayList last = shapes;
        ArrayList current = shapes;
        boolean first = true;
        while (current.size() != last.size() || first) {
            first = false;
            last = current;
            current = this.combineImpl(current);
        }
        ArrayList<Shape> pruned = new ArrayList<Shape>();
        int i = 0;
        while (i < current.size()) {
            pruned.add(((Shape)current.get(i)).prune());
            ++i;
        }
        return pruned;
    }

    private ArrayList combineImpl(ArrayList shapes) {
        ArrayList result = new ArrayList(shapes);
        if (this.quadSpace != null) {
            result = shapes;
        }
        int i = 0;
        while (i < shapes.size()) {
            Shape first = (Shape)shapes.get(i);
            int j = i + 1;
            while (j < shapes.size()) {
                Shape[] joined;
                Shape second = (Shape)shapes.get(j);
                if (first.intersects(second) && (joined = this.util.union(first, second)).length == 1) {
                    if (this.quadSpace != null) {
                        this.removeFromQuadSpace(first);
                        this.removeFromQuadSpace(second);
                        this.addToQuadSpace(joined[0]);
                    } else {
                        result.remove(first);
                        result.remove(second);
                        result.add(joined[0]);
                    }
                    return result;
                }
                ++j;
            }
            ++i;
        }
        return result;
    }

    public boolean collides(Shape shape1, Shape shape2) {
        float[] pt;
        if (shape1.intersects(shape2)) {
            return true;
        }
        int i = 0;
        while (i < shape1.getPointCount()) {
            pt = shape1.getPoint(i);
            if (shape2.contains(pt[0], pt[1])) {
                return true;
            }
            ++i;
        }
        i = 0;
        while (i < shape2.getPointCount()) {
            pt = shape2.getPoint(i);
            if (shape1.contains(pt[0], pt[1])) {
                return true;
            }
            ++i;
        }
        return false;
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        this.util.setListener(this);
        this.init();
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        Shape shape;
        g.setColor(Color.green);
        int i = 0;
        while (i < this.original.size()) {
            shape = (Shape)this.original.get(i);
            g.draw(shape);
            ++i;
        }
        g.setColor(Color.white);
        if (this.quadSpaceShapes != null) {
            g.draw(this.quadSpaceShapes[0][0]);
        }
        g.translate(0.0f, 320.0f);
        i = 0;
        while (i < this.combined.size()) {
            g.setColor(Color.white);
            shape = (Shape)this.combined.get(i);
            g.draw(shape);
            int j = 0;
            while (j < shape.getPointCount()) {
                g.setColor(Color.yellow);
                float[] pt = shape.getPoint(j);
                g.fillOval(pt[0] - 1.0f, pt[1] - 1.0f, 3.0f, 3.0f);
                ++j;
            }
            ++i;
        }
    }

    public static void main(String[] argv) {
        try {
            AppGameContainer container = new AppGameContainer(new GeomUtilTileTest());
            container.setDisplayMode(800, 600, false);
            container.start();
        }
        catch (SlickException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pointExcluded(float x, float y) {
    }

    @Override
    public void pointIntersected(float x, float y) {
        this.intersections.add(new Vector2f(x, y));
    }

    @Override
    public void pointUsed(float x, float y) {
        this.used.add(new Vector2f(x, y));
    }
}


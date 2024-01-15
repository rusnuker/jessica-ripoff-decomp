/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.util.pathfinding.navmesh;

import java.util.ArrayList;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;
import org.newdawn.slick.util.pathfinding.navmesh.NavMesh;
import org.newdawn.slick.util.pathfinding.navmesh.Space;

public class NavMeshBuilder
implements PathFindingContext {
    private int sx;
    private int sy;
    private float smallestSpace = 0.2f;
    private boolean tileBased;

    public NavMesh build(TileBasedMap map) {
        return this.build(map, true);
    }

    public NavMesh build(TileBasedMap map, boolean tileBased) {
        this.tileBased = tileBased;
        ArrayList<Space> spaces = new ArrayList<Space>();
        if (tileBased) {
            int x = 0;
            while (x < map.getWidthInTiles()) {
                int y = 0;
                while (y < map.getHeightInTiles()) {
                    if (!map.blocked(this, x, y)) {
                        spaces.add(new Space(x, y, 1.0f, 1.0f));
                    }
                    ++y;
                }
                ++x;
            }
        } else {
            Space space = new Space(0.0f, 0.0f, map.getWidthInTiles(), map.getHeightInTiles());
            this.subsection(map, space, spaces);
        }
        while (this.mergeSpaces(spaces)) {
        }
        this.linkSpaces(spaces);
        return new NavMesh(spaces);
    }

    private boolean mergeSpaces(ArrayList spaces) {
        int source = 0;
        while (source < spaces.size()) {
            Space a = (Space)spaces.get(source);
            int target = source + 1;
            while (target < spaces.size()) {
                Space b = (Space)spaces.get(target);
                if (a.canMerge(b)) {
                    spaces.remove(a);
                    spaces.remove(b);
                    spaces.add(a.merge(b));
                    return true;
                }
                ++target;
            }
            ++source;
        }
        return false;
    }

    private void linkSpaces(ArrayList spaces) {
        int source = 0;
        while (source < spaces.size()) {
            Space a = (Space)spaces.get(source);
            int target = source + 1;
            while (target < spaces.size()) {
                Space b = (Space)spaces.get(target);
                if (a.hasJoinedEdge(b)) {
                    a.link(b);
                    b.link(a);
                }
                ++target;
            }
            ++source;
        }
    }

    public boolean clear(TileBasedMap map, Space space) {
        if (this.tileBased) {
            return true;
        }
        float x = 0.0f;
        boolean donex = false;
        while (x < space.getWidth()) {
            float y = 0.0f;
            boolean doney = false;
            while (y < space.getHeight()) {
                this.sx = (int)(space.getX() + x);
                this.sy = (int)(space.getY() + y);
                if (map.blocked(this, this.sx, this.sy)) {
                    return false;
                }
                if (!((y += 0.1f) > space.getHeight()) || doney) continue;
                y = space.getHeight();
                doney = true;
            }
            if (!((x += 0.1f) > space.getWidth()) || donex) continue;
            x = space.getWidth();
            donex = true;
        }
        return true;
    }

    private void subsection(TileBasedMap map, Space space, ArrayList spaces) {
        if (!this.clear(map, space)) {
            float width2 = space.getWidth() / 2.0f;
            float height2 = space.getHeight() / 2.0f;
            if (width2 < this.smallestSpace && height2 < this.smallestSpace) {
                return;
            }
            this.subsection(map, new Space(space.getX(), space.getY(), width2, height2), spaces);
            this.subsection(map, new Space(space.getX(), space.getY() + height2, width2, height2), spaces);
            this.subsection(map, new Space(space.getX() + width2, space.getY(), width2, height2), spaces);
            this.subsection(map, new Space(space.getX() + width2, space.getY() + height2, width2, height2), spaces);
        } else {
            spaces.add(space);
        }
    }

    @Override
    public Mover getMover() {
        return null;
    }

    @Override
    public int getSearchDistance() {
        return 0;
    }

    @Override
    public int getSourceX() {
        return this.sx;
    }

    @Override
    public int getSourceY() {
        return this.sy;
    }
}


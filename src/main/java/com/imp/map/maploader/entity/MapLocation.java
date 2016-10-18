package com.imp.map.maploader.entity;

import java.util.List;

public abstract class MapLocation {

    public final static int VAL_UNKNOWN = -1;

    public int row = VAL_UNKNOWN;
    public int col = VAL_UNKNOWN;
    final public int x;
    final public int y;
    final public int zoom;
    public final double xofs;
    public final double yofs;
    public final int size;

    public MapLocation(int row, int col, int x, int y, int zoom, int size, double xofs, double yofs) {
        this.row = row;
        this.col = col;
        this.x = x;
        this.y = y;
        this.zoom = zoom;
        this.xofs = xofs;
        this.yofs = yofs;
        this.size = size;
    }

    public MapLocation(MapLocation src) {
        this.row = src.row;
        this.col = src.col;
        this.x = src.x;
        this.y = src.y;
        this.zoom = src.zoom;
        this.xofs = src.xofs;
        this.yofs = src.yofs;
        this.size = src.size;
    }

    public int tileOffsetX() {
        return (int) (xofs * size);
    }

    public int tileOffsetY() {
        return (int) (yofs * size);
    }

    public int mapOffsetX() {
        return col * size + tileOffsetX();
    }

    public int mapOffsetY() {
        return row * size + tileOffsetY();
    }

    public boolean sameGeoLocation(MapLocation loc) {
        return loc.zoom == zoom && loc.x == x && loc.y == y;
    }

    public MapLocation setPixPosition(int row, int col) {
        this.row = row;
        this.col = col;
        return this;
    }

    public boolean hasPixLocation() {
        return row != VAL_UNKNOWN && col != VAL_UNKNOWN;
    }

    public MapLocation locateTile(List<? extends MapLocation> map) {
        if (map != null && !map.isEmpty()) {
            for (MapLocation t : map) {
                if (t != null && sameGeoLocation(t)) {
                    col = t.col;
                    row = t.row;
                    break;
                }
            }
        }
        return this;
    }

}

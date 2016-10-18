package com.imp.map.maploader.entity;

public class Tile extends MapLocation {

    public final static int DEF_TILESIZE = 256;

    protected boolean newRow = false;

    public Tile(int zoom, int xtile, int ytile, int size) {
        this(zoom, xtile, ytile, size, 0, 0);
    }

    public Tile(int zoom, int xtile, int ytile, double xofs, double yofs) {
        this(zoom, xtile, ytile, DEF_TILESIZE, xofs, yofs);
    }

    public Tile(int zoom, int xtile, int ytile, int size, double xofs, double yofs) {
        super(VAL_UNKNOWN, VAL_UNKNOWN, xtile, ytile, zoom, size, xofs, yofs);
    }

    @Override
    public String toString() {
        return ("" + zoom + "/" + x + "/" + y);
    }
}

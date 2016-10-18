package com.imp.map.maploader.entity;

import java.awt.image.BufferedImage;

public class TileImage extends MapLocation {

    final public boolean empty;
    final public BufferedImage image;

    public TileImage(BufferedImage bufimg, Tile tile) {
        super(tile);
        this.image = bufimg;
        empty = image == null;
    }
}

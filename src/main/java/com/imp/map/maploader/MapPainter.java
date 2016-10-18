package com.imp.map.maploader;


import com.imp.map.maploader.entity.GeoPoint;
import com.imp.map.maploader.entity.Tile;
import com.imp.map.maploader.entity.TileImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

public abstract class MapPainter {

    protected int offsetX = 0;
    protected int offsetY = 0;

    public GeoPoint center() {
        return null;
    }

    public BufferedImage picture() {
        return null;
    }

    protected void draw(Graphics2D parent, MapLoader ml, List<TileImage> map) {
        if (map == null || map.isEmpty() || picture() == null) {
            return;
        }
        int tileSize = map.get(0).size;
        int x0, y0;
        if (center() != null) {
            Tile t = (Tile) ml.getTile(center(), tileSize, false).locateTile(map);
            x0 = t.mapOffsetX();
            y0 = t.mapOffsetY();
        } else {
            x0 = 0;
            y0 = 0;
        }

        BufferedImage img = picture();
        offsetX = -img.getWidth() / 2;
        offsetY = -img.getWidth() / 2;
        parent.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        parent.drawImage(img, x0 + offsetX, y0 + offsetY, null);
    }
}

package com.imp.map.maploader;

import com.imp.map.maploader.entity.GeoPoint;
import com.imp.map.maploader.entity.TileImage;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

public class OsmRenderer {

    final MapLoader ml;
    List<TileImage> til = null;

    protected int cols = 0;
    protected int rows = 0;
    protected GeoPoint lastCenter = null;
    protected BufferedImage out = null;

    public OsmRenderer() {
        this(MapLoader.DEF_TILE_SERVER, MapLoader.DEF_TIMEOUT);
    }

    public OsmRenderer(String tileUrl) {
        this(tileUrl, MapLoader.DEF_TIMEOUT);
    }

    public OsmRenderer(String tileUrl, int timeoutms) {
        ml = new MapLoader(tileUrl, timeoutms);
    }

    public OsmRenderer prepareBackground(final double lat, final double lon, final int zoom, final int width, final int height) {
        return prepareBackground(new GeoPoint(lat, lon, zoom), width, height);
    }

    public OsmRenderer prepareBackground(final GeoPoint center, final int width, final int height) {
        if (lastCenter != null && GeoPoint.same(center, lastCenter)) {
            // pass
        } else {
            out = null;
            til = ml.getImages(center, width, height);
            cols = ml.lastCols;
            rows = ml.lastRows;
        }
        lastCenter = center;
        out = ml.getBackgroundImage(til);
        return this;
    }

    public OsmRenderer paintOver(MapPainter p) {
        if (ml.ready() && p != null) {
            p.draw((Graphics2D) out.getGraphics(), ml, til);
        }
        return this;
    }

    public synchronized OsmRenderer crop() {
        if (ml.ready()) {
            out = ml.getFinalImage(out, lastCenter, til);
        }
        return this;
    }

    public BufferedImage get() {
        return out;
    }

    public byte[] getBytes() throws IOException {
        byte[] imageInByte;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(out, "png", baos);
            baos.flush();
            imageInByte = baos.toByteArray();
        }
        return imageInByte;
    }

}

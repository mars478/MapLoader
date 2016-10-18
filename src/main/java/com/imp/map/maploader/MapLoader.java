package com.imp.map.maploader;

import com.imp.map.maploader.entity.GeoPoint;
import com.imp.map.maploader.entity.Tile;
import static com.imp.map.maploader.entity.Tile.DEF_TILESIZE;
import com.imp.map.maploader.entity.TileImage;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;

public class MapLoader implements Serializable {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MapLoader.class);

    public final static int DEF_TIMEOUT = 3000;
    public final static String DEF_TILE_SERVER = "http://tile.openstreetmap.org/";
    protected String tile_server;

    protected int lastCols = 0;
    protected int lastRows = 0;
    protected int lastWidth = 0;
    protected int lastHeight = 0;

    protected final int timeout;
    protected final int tileSize;

    public MapLoader() {
        this(DEF_TILE_SERVER, DEF_TIMEOUT);
    }

    public MapLoader(String tileUrl) {
        this(tileUrl, DEF_TIMEOUT);
    }

    public MapLoader(String tileUrl, int timeoutms) {
        if (StringUtils.isBlank(tileUrl)) {
            throw new IllegalArgumentException("MapLoader: empty tile url found");
        }
        if (!StringUtils.endsWith(tileUrl, "/")) {
            tileUrl = tileUrl + '/';
        }
        tile_server = tileUrl;
        timeout = timeoutms;
        tileSize = DEF_TILESIZE;
    }

    protected Tile getTileCenter(@Nonnull GeoPoint gp, final int tileSize) {
        return getTile(gp, tileSize, true);
    }

    protected Tile getTile(@Nonnull GeoPoint gp, final int tileSize, boolean center) {
        final double lon = gp.lon;
        final double lat = gp.lat;
        final int zoom = gp.zoom;

        double xd = (lon + 180) / 360 * (1 << zoom);
        double yd = (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom);

        int xtile = (int) Math.floor(xd);
        int ytile = (int) Math.floor(yd);
        if (xtile < 0) {
            xtile = 0;
        }
        if (xtile >= (1 << zoom)) {
            xtile = ((1 << zoom) - 1);
        }
        if (ytile < 0) {
            ytile = 0;
        }
        if (ytile >= (1 << zoom)) {
            ytile = ((1 << zoom) - 1);
        }

        return new Tile(zoom, xtile, ytile, tileSize, xd % 1, yd % 1);
    }

    @Nonnull
    protected List<Tile> getTilesNumbersArea(@Nonnull final GeoPoint center, final int width, final int height) {
        final int verticalSteps = (int) Math.ceil((double) height / (double) tileSize);
        final int horizontalSteps = (int) Math.ceil((double) width / (double) tileSize);
        List<Tile> ret = new LinkedList<>();
        final int vs = verticalSteps % 2 == 1 ? (verticalSteps / 2 + 1) : verticalSteps / 2;
        final int hs = horizontalSteps % 2 == 1 ? (horizontalSteps / 2 + 1) : horizontalSteps / 2;

        lastRows = 0;
        lastCols = 0;
        this.lastWidth = width;
        this.lastHeight = height;

        int r = 0, c = 0;
        final Tile ct = getTileCenter(center, tileSize); // centerTile
        final int initialY = ct.y - vs;
        for (int x = ct.x - hs; x <= ct.x + hs; x++) {
            c = 0;
            for (int y = initialY; y <= ct.y + vs; y++) {
                ret.add((Tile) new Tile(ct.zoom, x, y, tileSize).setPixPosition(c, r));
                c++;
            }
            r++;
        }

        lastRows = r;
        lastCols = c;

        return ret;
    }

    protected TileImage loadImage(Tile tile) throws IOException {
        URL url = new URL(tile_server + tile + ".png");
        return new TileImage(ImageIO.read(url), tile);
    }

    protected List<TileImage> getImages(@Nonnull final GeoPoint center, final int width, final int height) {
        List<Tile> tiles = getTilesNumbersArea(center, width, height);
        final List<TileImage> ret = new LinkedList<>();

        try {
            final long start = System.currentTimeMillis();
            ExecutorService taskExecutor = Executors.newFixedThreadPool(2);
            List<Callable<Boolean>> tasks = new LinkedList<>();
            for (Tile t : tiles) {
                tasks.add((Callable<Boolean>) () -> {
                    TileImage cur = null;
                    long delta = System.currentTimeMillis() - start;
                    if (delta < timeout) {
                        try {
                            cur = loadImage(t);
                        } catch (IOException ex) {
                            log.error(ExceptionUtils.getStackTrace(ex));
                        }
                    }
                    if (cur == null) {
                        cur = new TileImage(null, t); // pass
                    }
                    synchronized (ret) {
                        ret.add(cur);
                    }
                    return cur != null;
                });
            }

            taskExecutor.invokeAll(tasks);
        } catch (InterruptedException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
        }

        return ret;
    }

    public BufferedImage getBackgroundImage(List<TileImage> til) {
        if (til == null || til.isEmpty()) {
            return null;
        }

        BufferedImage out = new BufferedImage(lastRows * tileSize, lastCols * tileSize, BufferedImage.TYPE_INT_ARGB);
        int x, y;
        Graphics2D g = out.createGraphics();

        AsyncPainter async = new AsyncPainter(til.size());
        for (TileImage ti : til) {
            if (!ti.empty) {
                x = ti.col * DEF_TILESIZE;
                y = ti.row * DEF_TILESIZE;

                if (g.drawImage(ti.image, x, y, async)) {
                    async.counterDecrement();
                }
            }
        }

        async.lockReady();
        return out;
    }

    public BufferedImage getFinalImage(BufferedImage background, @Nonnull final GeoPoint center, List<TileImage> til) {
        Tile t = (Tile) getTile(center, tileSize, false).locateTile(til);
        int x0 = t.mapOffsetX();
        int y0 = t.mapOffsetY();
        return background.getSubimage(x0 - lastWidth / 2, y0 - lastHeight / 2, lastWidth, lastHeight);
    }

    public boolean ready() {
        return lastCols != 0 && lastRows != 0 && lastWidth != 0 && lastHeight != 0;
    }

    public int getWidth() {
        return lastWidth;
    }

    public int getHeight() {
        return lastHeight;
    }

}

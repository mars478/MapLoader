package com.imp.map.maploader;

import com.imp.map.maploader.entity.GeoPoint;
import static java.lang.Math.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class MarkerPainter extends MapPainter {

    final double x;
    final double y;
    final int azimuth;
    final int zoom;
    final GeoPoint center;

    public MarkerPainter(double x, double y, int azimuth, int zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
        this.azimuth = azimuth;
        this.center = new GeoPoint(x, y, zoom);
    }

    @Override
    public GeoPoint center() {
        return center;
    }

    @Override
    public BufferedImage picture() {
        int side = 60; //px
        BufferedImage b = new BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB);
        Graphics g = b.getGraphics();
        g.setColor(Color.RED);

        int x0 = 30;
        int y0 = 30;
        PolyTriangle p = new PolyTriangle(x0, y0, azimuth);
        if (azimuth != -1) {
            g.fillPolygon(p.x, p.y, p.SIZE);
        } else {
            final int s = p.SCALE;
            final int s2 = s * 2;
            g.fillOval((int) x0 - s, (int) y0 - s, s2, s2);
        }

        return b;
    }

    class PolyTriangle {

        public final static int SCALE = 14;
        public final static int SIZE = 3;
        public final int[] x;
        public final int[] y;

        @SuppressWarnings("LocalVariableHidesMemberVariable")
        public PolyTriangle(int x0, int y0, int azimuth) {
            final int k = SCALE;
            final double a = -azimuth / 57.324;

            int[] x = new int[SIZE];
            int[] y = new int[SIZE];

            x[0] = (int) round(x0 - 2 * k * sin(a));
            y[0] = (int) round(y0 - 2 * k * cos(a));

            x[1] = (int) round(x0 + k * (cos(a) + 2 * sin(a)));
            y[1] = (int) round(y0 - k * (sin(a) - 2 * cos(a)));

            x[2] = (int) round(x0 + k * (2 * sin(a) - cos(a)));
            y[2] = (int) round(y0 + k * (sin(a) + 2 * cos(a)));

            this.x = x;
            this.y = y;
        }
    }
}

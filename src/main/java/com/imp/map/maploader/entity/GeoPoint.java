package com.imp.map.maploader.entity;

public class GeoPoint {

    public final double lat;
    public final double lon;
    public final int zoom;

    public GeoPoint(double lat, double lon, int zoom) {
        this.lat = lat;
        this.lon = lon;
        this.zoom = zoom;
    }

    public static boolean same(GeoPoint p1, GeoPoint p2) {
        return p1 != null && p2 != null && p1.lat == p2.lat && p1.lon == p2.lon && p1.zoom == p2.zoom;
    }
}

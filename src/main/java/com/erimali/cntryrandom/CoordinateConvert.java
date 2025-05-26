package com.erimali.cntryrandom;
import java.awt.geom.Point2D;

public class CoordinateConvert {
    private final int mapWidth;
    private final int mapHeight;
    private final double centerX;
    private final double centerY;

    public CoordinateConvert(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.centerX = mapWidth / 2.0;
        this.centerY = mapHeight / 2.0;
    }

    public double pixelToLongitude(double x) {
        return (x - centerX) / centerX * 180.0;
    }

    public double pixelToLatitude(double y) {
        return -(y - centerY) / centerY * 90.0;
    }

    public double longitudeToPixelX(double lon) {
        return (lon / 180.0) * centerX + centerX;
    }

    public double latitudeToPixelY(double lat) {
        return (-lat / 90.0) * centerY + centerY;
    }

    public Point2D.Double latLonToPoint(double lat, double lon) {
        double x = longitudeToPixelX(lon);
        double y = latitudeToPixelY(lat);
        return new Point2D.Double(x, y);
    }

    public String latLonString(double x, double y) {
        double lat = pixelToLatitude(y);
        double lon = pixelToLongitude(x);
        return String.format("Lat: %.2f°, Lon: %.2f°", lat, lon);
    }

    // DMS = degrees, minutes, second
    private String toDMS(double decimal, boolean isLat) {
        decimal = Math.abs(decimal);
        int degrees = (int) decimal;
        double fractional = (decimal - degrees) * 60;
        int minutes = (int) fractional;
        double seconds = (fractional - minutes) * 60;
        String direction;
        if (isLat) {
            direction = (decimal >= 0) ? "N" : "S";
        } else {
            direction = (decimal >= 0) ? "E" : "W";
        }

        return String.format("%d°%d'%d\"%s", degrees, minutes, (int) seconds, direction);
    }

    public String latLonStringDMS(double x, double y) {
        double lat = pixelToLatitude(y);
        double lon = pixelToLongitude(x);
        return String.format("%s, %s", toDMS(lat, true), toDMS(lon, false));
    }


}

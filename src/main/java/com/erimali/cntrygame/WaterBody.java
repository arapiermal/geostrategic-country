package com.erimali.cntrygame;

import javafx.geometry.Point2D;

import java.util.List;

//Mixed between Logic and GUI
public class WaterBody implements DijkstraCalculable {

    public enum WaterBodyType {
        OCEAN("Ocean"),
        SEA("Sea"),
        ;
        //if not final could be changed for translation (!)
        private final String desc;

        WaterBodyType(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }
    }

    private int waterBodyId;
    private WaterBodyType type;
    private String name;
    private double area;
    private double centerX;//or represented by Point(x,y) in world map;
    private double centerY;
    private transient Point2D point;
    //private List<WaterBody> neighbours;//or set...


    public WaterBody(WaterBodyType type, String name, double area) {
        this.type = type;
        this.name = name;
        this.area = area;
    }

    public WaterBody(int waterBodyId, WaterBodyType type, String name, double area) {
        this.waterBodyId = waterBodyId;
        this.type = type;
        this.name = name;
        this.area = area;
    }

    //when loading saveGame (if WaterBody[] not transient(?))
    public Point2D setAndGetPoint2D() {
        point = new Point2D(centerX, centerY);
        return point;
    }

    public Point2D setAndGetPoint2D(double centerX, double centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
        point = new Point2D(centerX, centerY);
        return point;
    }

    public void setPoint2D(Point2D point) {
        this.centerX = point.getX();
        this.centerY = point.getY();
        this.point = point;
    }

    public Point2D makePointBetweenProvinces(SVGProvince[] provinces, int... provIds) {
        if (provIds.length == 0)
            return null;
        double totalX = 0.0;
        double totalY = 0.0;
        for (int i : provIds) {
            SVGProvince temp = provinces[i];
            totalX += temp.getCenterX();
            totalY += temp.getCenterY();
        }
        double avgX = totalX / provIds.length;
        double avgY = totalY / provIds.length;
        return setAndGetPoint2D(avgX, avgY);
    }

    public int getWaterBodyId() {
        return waterBodyId;
    }

    public boolean equals(WaterBody o) {
        return type.equals(o.type) && name.equalsIgnoreCase(o.name);
    }

    @Override
    public String toString() {
        return name + " " + type.toString();
    }

    @Override
    public double getCenterX() {
        return centerX;
    }

    @Override
    public double getCenterY() {
        return centerY;
    }

    @Override
    public double getDistance(DijkstraCalculable dijkstraCalculable) {
        double distX = centerX - dijkstraCalculable.getCenterX();
        double distY = centerY - dijkstraCalculable.getCenterY();
        return Math.sqrt(distX * distX + distY * distY);
    }

    public WaterBodyType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public double getArea() {
        return area;
    }

    public Point2D getPoint() {
        return point;
    }

    public void setWaterBodyId(int waterBodyId) {
        this.waterBodyId = waterBodyId;
    }

}

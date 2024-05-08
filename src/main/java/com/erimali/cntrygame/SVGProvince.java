package com.erimali.cntrygame;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.SVGPath;


public class SVGProvince extends SVGPath {
    //private static int CURRPROVINCEID = 0;
    private double provX, provY;
    private int ownerId;
    private int provId;
    private int occupierId;
    //cache the color as well ?
    private RadialGradient radialGradient; // Cache

    //private AdmDiv admDiv;

    public SVGProvince(int ownerId, int provId) {
        this.provId = provId;
        this.ownerId = ownerId;
        this.occupierId = -1;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getProvId() {
        return provId;
    }

    public void setProvId(int provId) {
        this.provId = provId;
    }

    public void updateXY() {
        double minX0 = this.getBoundsInLocal().getMinX();
        double minY0 = this.getBoundsInLocal().getMinY();
        double maxX0 = this.getBoundsInLocal().getMaxX();
        double maxY0 = this.getBoundsInLocal().getMaxY();
        this.provX = (minX0 + maxX0) / 2;
        this.provY = (minY0 + maxY0) / 2;

    }

    public double getProvX() {
        return provX;
    }

    public double getProvY() {
        return provY;
    }


    //ellipse like
    public double getAvgRadius() {
        return (getBoundsInLocal().getWidth() + getBoundsInLocal().getHeight()) / 4;
    }

    //Not accurate
    public double calcAvgArea(double mapW, double mapH) {
        double radiusX = getBoundsInLocal().getWidth() / 2;
        double radiusY = getBoundsInLocal().getHeight() / 2;
        double area = Math.PI * radiusX * radiusY;
        return area * (mapW * mapH) / (getBoundsInLocal().getWidth() * getBoundsInLocal().getHeight());
    }

    public void setFillExtra(Paint owner, Paint occupier) {
        if (owner instanceof Color && occupier instanceof Color) {
            //linear gradient javafx.scene.paint.CycleMethod.REPEAT
            if (radialGradient == null) {
                radialGradient = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true,
                        javafx.scene.paint.CycleMethod.NO_CYCLE, new Stop(0, (Color) occupier), new Stop(1, (Color) owner));
            }
            setFill(radialGradient);
        } else {
            setFill(owner);
        }
    }

    public boolean isOccupied() {
        return occupierId >= 0;
    }

    public void setOccupierId(int occupierId) {
        this.occupierId = occupierId;
        radialGradient = null;
    }

    public int getOccupierId() {
        return occupierId;
    }

    public double getDistance(SVGProvince s1) {
        double distX = provX - s1.provX;
        double distY = provY - s1.provY;
        return Math.sqrt(distX * distX + distY * distY);
    }
}


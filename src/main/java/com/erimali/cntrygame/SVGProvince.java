package com.erimali.cntrygame;

import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;


public class SVGProvince extends SVGPath implements DijkstraCalculable {
    //private static int CURRPROVINCEID = 0;
    private double provX, provY;
    private int ownerId;
    private int provId;
    private int occupierId;
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
        double minX = getBoundsInLocal().getMinX();
        double minY = getBoundsInLocal().getMinY();
        double maxX = getBoundsInLocal().getMaxX();
        double maxY = getBoundsInLocal().getMaxY();
        this.provX = (minX + maxX) / 2;
        this.provY = (minY + maxY) / 2;
        // Dealing with provinces which appear in both the East and West of the globe because of wrapping (example: Alaska)
        if (getBoundsInLocal().getWidth() > WorldMap.getDefMapWidth() / 2) {
            this.provX = maxX - 32;
            //TESTING.print(maxX, getId());
        }
    }

    @Override
    public double getCenterX() {
        return provX;
    }

    @Override
    public double getCenterY() {
        return provY;
    }


    public double getAvgRadius() {
        return (getBoundsInLocal().getWidth() + getBoundsInLocal().getHeight()) / 4;
    }

    //ellipse like
    //Not accurate
    public double calcAvgArea(double mapW, double mapH) {
        double radiusX = getBoundsInLocal().getWidth() / 2;
        double radiusY = getBoundsInLocal().getHeight() / 2;
        double area = Math.PI * radiusX * radiusY;
        return area * (mapW * mapH) / (getBoundsInLocal().getWidth() * getBoundsInLocal().getHeight());
    }

    public void setFillExtra(Paint owner, Paint occupier) {
        if (owner instanceof Color ownerColor && occupier instanceof Color occupierColor) {
            //linear gradient javafx.scene.paint.CycleMethod.REPEAT
            if (radialGradient == null) {
                radialGradient = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true,
                        javafx.scene.paint.CycleMethod.NO_CYCLE, new Stop(0, occupierColor), new Stop(1, ownerColor));
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
        radialGradient = null; // if WorldMap is known ... you can do the coloring here... careful with mapMode...
    }

    public int getOccupierId() {
        return occupierId;
    }

    public double getDistance(DijkstraCalculable dijkstraCalculable) {
        double distX = provX - dijkstraCalculable.getCenterX();
        double distY = provY - dijkstraCalculable.getCenterY();
        return Math.sqrt(distX * distX + distY * distY);
    }

    public boolean isProbNeighbour(SVGProvince o) {
        Bounds main = getBoundsInLocal();
        Bounds other = o.getBoundsInLocal();
        if (main.getMaxX() <= other.getMinX() ||
                main.getMinX() >= other.getMaxX() ||
                main.getMaxY() <= other.getMinY() ||
                main.getMinY() >= other.getMaxY()) {
            return false;
        }
        return true; // Overlap
    }
}


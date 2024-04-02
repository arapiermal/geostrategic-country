package com.erimali.cntrygame;

import javafx.scene.shape.SVGPath;


public class SVGProvince extends SVGPath {
    //private static int CURRPROVINCEID = 0;
    private double provX, provY;
    private int ownerId;
    private int provId;

    //private AdmDiv admDiv;

    public SVGProvince(int ownerId, int provId) {
        this.provId = provId;
        this.ownerId = ownerId;
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

}

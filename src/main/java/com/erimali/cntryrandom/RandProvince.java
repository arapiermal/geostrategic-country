package com.erimali.cntryrandom;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.List;

public class RandProvince extends GeoPolZone {
    private static int IDS = 0;
    private Color color;
    private int ownerId;

    public RandProvince(Point2D mainPoint, List<Point2D> boundary){
        this.mainPoint = mainPoint;
        this.boundary = boundary;
        this.provId = IDS++;
    }

    public boolean isCoastal() {
        return neighbours.stream().anyMatch(n -> !n.aboveSeaLevel());
    }

    public void setOwnerId(int ownerId){
        this.ownerId = ownerId;
    }

    public int getOwnerId(){
        return ownerId;
    }

    public void setColor(Color color){
        this.color = color;
    }

    public Color getColor(){
        return color;
    }

    public static void resetCountingIDS(){
        IDS = 0;
    }
}

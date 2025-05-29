package com.erimali.cntryrandom;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.List;

public class RandWaterBody extends GeoPolZone {
    // Starting id of water bodies is 1073741823, to differentiate from actual provinces
    private static final int DEFAULT_IDS = Integer.MAX_VALUE / 2;
    private static int IDS = DEFAULT_IDS;

    public RandWaterBody(Point2D mainPoint, List<Point2D> boundary){
        this.mainPoint = mainPoint;
        this.boundary = boundary;
        this.provId =  IDS++;
    }

    public Color getColor(){
        return Color.LIGHTSKYBLUE;
    }
    public static void resetCountingIDS(){
        IDS = DEFAULT_IDS;
    }
}

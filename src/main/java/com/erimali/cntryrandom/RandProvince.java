package com.erimali.cntryrandom;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.List;

public class RandProvince extends GeoPolZone {
    private static int IDS = 0;
    private RandCountry country;

    public RandProvince(Point2D mainPoint, List<Point2D> boundary) {
        this.mainPoint = mainPoint;
        this.boundary = boundary;
        this.provId = IDS++;
    }

    public boolean isCoastal() {
        return !neighWaters.isEmpty();
    }

    public void setCountry(RandCountry country) {
        this.country = country;
    }

    public RandCountry getCountry() {
        return country;
    }


    public Color getColor() {
        if (country == null)
            return Color.LIGHTGRAY;

        return country.getColor();
    }

    public static void resetCountingIDS() {
        IDS = 0;
    }

    public String getName() {
        if (name == null) {
            return "Prov" + provId;
        }
        return name;
    }

    public int getOwnerId() {
        if (country == null)
            return -1;

        return country.getId();
    }
}

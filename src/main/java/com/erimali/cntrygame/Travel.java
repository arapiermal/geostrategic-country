package com.erimali.cntrygame;

import com.erimali.cntrymilitary.MilUnit;

import java.util.List;

public class Travel {

    TravelAmbients ambient;
    // km / h speed for Units?
    List<MilUnit> travellers; //unnecessary if Transport Units have that inside?
    double totalDistance;
    double distanceTravelled;

    public Travel(List<MilUnit> travellers, TravelAmbients ambient, double distance) {
        this.travellers = travellers;
        this.ambient = ambient;
        this.totalDistance = distance;
    }

    public boolean travel(double distance) {
        distanceTravelled += distance;
        if (distanceTravelled >= totalDistance)
            return true; // destroy Travel
        else
            return false;
    }

    public static double getDistanceFromLatLng(double lat1, double lng1, double lat2, double lng2, boolean miles) {
        double r = 6371; // radius of the earth in km
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double latDiff = lat2Rad - lat1Rad;
        double lngDiff = Math.toRadians(lng2 - lng1);

        double a = Math.sin(latDiff / 2.0) * Math.sin(latDiff / 2.0)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(lngDiff / 2.0) * Math.sin(lngDiff / 2.0);
        double d = 2 * r * Math.asin(Math.sqrt(a));

        if (miles) {
            return d * 0.621371; // return miles
        } else {
            return d; // return km
        }
    }
    // minigames?
    // technologies?
    // rockets?

}

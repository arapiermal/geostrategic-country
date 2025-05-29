package com.erimali.cntryrandom;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

import java.util.List;

//Could be "extends Pane"
public class RandMapFX {

    public static Pane createPaneFX(RandWorldMap randWorldMap){
        return createPaneFX(randWorldMap.getMapWidth(), randWorldMap.getMapHeight(), randWorldMap.getZones());
    }

    public static Pane createPaneFX(double mapWidth, double mapHeight, List<GeoPolZone> zones){
        Pane pane = new Pane();
        pane.setPrefWidth(mapWidth);
        pane.setPrefHeight(mapHeight);
        for (GeoPolZone zone : zones) {
            List<Point2D> cell = zone.getBoundary();
            Polygon polygon = new Polygon();
            for (Point2D point : cell) {
                polygon.getPoints().addAll(point.getX(), point.getY());
            }

            polygon.setFill(zone.getColor());
            polygon.setStroke(Color.BLACK);

            pane.getChildren().add(polygon);
        }

        for (GeoPolZone zone : zones) {
            Point2D site = zone.getMainPoint();
            Circle circle = new Circle(site.getX(), site.getY(), 3, Color.BLACK);
            pane.getChildren().add(circle);
        }

        return pane;
    }

}

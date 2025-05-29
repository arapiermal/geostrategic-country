package com.erimali.cntryrandom;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class PerlinMapFX {
    static Color deepWater = Color.rgb(0, 0, 100);       // Dark Blue
    static Color shallowWater = Color.rgb(0, 100, 200);  // Medium Blue
    static Color beach = Color.rgb(240, 230, 140);       // Khaki/Beige
    static Color plains = Color.rgb(50, 205, 50);        // LimeGreen
    static Color forest = Color.rgb(34, 139, 34);        // ForestGreen
    static Color hills = Color.rgb(139, 119, 101);       // Tan/Light Brown for hills
    static Color mountains = Color.rgb(139, 69, 19);       // SaddleBrown
    static Color snowCaps = Color.rgb(245, 245, 245);      // WhiteSmoke
    
    public static ImageView genImageViewPerlin(RandWorldMap randWorldMap){
        int mapWidth = (int) randWorldMap.getMapWidth();
        int mapHeight = (int) randWorldMap.getMapHeight();
        PerlinNoiseElevationGen generator = randWorldMap.getPerlinNoise();
        WritableImage mapImage = new WritableImage(mapWidth, mapHeight);
        PixelWriter pixelWriter = mapImage.getPixelWriter();
        double waterLevel = GeoPolZone.getSeaLevel();
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                double elevation = generator.islandNoise(x, y);

                Color colorToSet = getColor(elevation, waterLevel);
                pixelWriter.setColor(x, y, colorToSet);
            }
        }

        return new ImageView(mapImage);
        
    }

    private static Color getColor(double elevation, double waterLevel) {
        Color colorToSet;

        if (elevation > 0.55) {
            colorToSet = snowCaps;
        } else if (elevation > 0.35) {
            colorToSet = mountains;
        } else if (elevation > 0.20) {
            colorToSet = hills;
        } else if (elevation > 0.10) {
            colorToSet = forest;
        } else if (elevation > waterLevel + 0.02) { // Land just above water (plains)
            colorToSet = plains;
        } else if (elevation > waterLevel - 0.05) { // Beach just above/at waterLevel
            colorToSet = beach;
        } else if (elevation > waterLevel - 0.3) { // Shallow water
            colorToSet = shallowWater;
        } else { // Deep water
            colorToSet = deepWater;
        }
        return colorToSet;
    }

}
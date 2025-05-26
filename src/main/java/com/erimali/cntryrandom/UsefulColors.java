package com.erimali.cntryrandom;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UsefulColors {
    private static Set<Color> colors = new HashSet<>();

    public static void loadUsefulColors(File file){
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                for (int i = 1; i < parts.length; i++) {
                    String hex = parts[i].trim();
                    if (hex.startsWith("#")) {
                        try {
                            Color color = Color.web(hex);
                            colors.add(color);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid color code: " + hex);
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Color getRandomColor(){
        return null;
    }
    public static Color getRandomColor(List<Color> nearby){
        return null;
    }
}

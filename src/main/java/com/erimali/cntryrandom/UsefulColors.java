package com.erimali.cntryrandom;

import javafx.scene.paint.Color;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class UsefulColors {
    private static Set<Color> colors = new HashSet<>();

    public static void loadUsefulColorsResources() {
        try {
            InputStream inputStream = UsefulColors.class.getResourceAsStream("colorCodesFlags.csv");
            if (inputStream == null) {
                System.err.println("Could not find colorCodesFlags.csv in resources.");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    parseAddColor(line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void loadUsefulColors(){

        File file = new File("src/main/resources/colorCodesFlags.csv");
        if(!file.exists()){
            System.err.println("Could not find colorCodesFlags.csv.");
            return;
        }
        loadUsefulColors(file);
    }
    public static void loadUsefulColors(File file){
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                parseAddColor(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void parseAddColor(String line){
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
    public static Color getRandomColor(){
        return colors.stream().skip(new Random().nextInt(colors.size())).findFirst().orElse(null);
    }
    public static Color getRandomColor(List<Color> nearby){
        return null;
    }
}

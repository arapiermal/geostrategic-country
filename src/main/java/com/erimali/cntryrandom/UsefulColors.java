package com.erimali.cntryrandom;

import javafx.scene.paint.Color;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

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

    public static void loadUsefulColors() {

        File file = new File("src/main/resources/colorCodesFlags.csv");
        if (!file.exists()) {
            System.err.println("Could not find colorCodesFlags.csv.");
            return;
        }
        loadUsefulColors(file);
    }

    public static void loadUsefulColors(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                parseAddColor(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void parseAddColor(String line) {
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

    public static Color getRandomColor() {
        return colors.stream().skip(new Random().nextInt(colors.size())).findFirst().orElse(null);
    }

    public static Color getRandomColor(List<Color> nearby) {
        return null;
    }

    public static void colorCountries(List<RandCountry> countries) {
        double similarityThreshold = 0.25;

        // Convert and shuffle the color list for fair distribution
        List<Color> availableColors = new ArrayList<>(colors);
        Collections.shuffle(availableColors);

        // Welsh Powell Graph colouring: sort countries by most neighbors to minimize conflicts
        countries.sort((a, b) -> b.getNeighbours().size() - a.getNeighbours().size());

        for (RandCountry country : countries) {
            Set<Color> neighborColors = new HashSet<>();
            for (RandCountry neighbor : country.getNeighbours()) {
                if (neighbor.getColor() != null) {
                    neighborColors.add(neighbor.getColor());
                }
            }

            Color assignedColor = null;

            Iterator<Color> iterator = availableColors.iterator();
            while (iterator.hasNext()) {
                Color candidate = iterator.next();
                boolean tooSimilar = false;
                for (Color used : neighborColors) {
                    if (UsefulColors.isTooSimilar(candidate, used, similarityThreshold)) {
                        tooSimilar = true;
                        break;
                    }
                }
                if (!tooSimilar) {
                    assignedColor = candidate;
                    iterator.remove(); // Use once
                    break;
                }
            }

            // Fallback if no more distinct color is available
            if (assignedColor == null) {
                System.err.println("Warning: No unique color available. Falling back.");
                for (Color fallback : colors) {
                    boolean ok = true;
                    for (Color used : neighborColors) {
                        if (UsefulColors.isTooSimilar(fallback, used, similarityThreshold)) {
                            ok = false;
                            break;
                        }
                    }
                    if (ok) {
                        assignedColor = fallback;
                        break;
                    }
                }
                if (assignedColor == null) {
                    assignedColor = Color.BLACK; // last resort
                }
            }

            country.setColor(assignedColor);
        }
    }

    public static boolean isTooSimilar(Color c1, Color c2, double threshold) {
        double dr = c1.getRed() - c2.getRed();
        double dg = c1.getGreen() - c2.getGreen();
        double db = c1.getBlue() - c2.getBlue();
        double distance = Math.sqrt(dr * dr + dg * dg + db * db);
        return distance < threshold;
    }
}

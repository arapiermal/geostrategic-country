package com.erimali.cntryrandom;

import com.erimali.cntrygame.CountryArray;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class SVGSave {
    // Backup method if no GeoPolZone
    public static String generateSVG(double mapWidth, double mapHeight, List<List<Point2D>> cells, List<Point2D> sites) {
        StringBuilder svg = new StringBuilder();
        svg.append("<svg width='").append(mapWidth).append("' height='").append(mapHeight)
                .append("' xmlns='http://www.w3.org/2000/svg'>\n");

        Random rand = new Random();

        // Draw each Voronoi cell
        for (int i = 0; i < cells.size(); i++) {
            List<Point2D> cell = cells.get(i);
            // Build SVG path from polygon vertices
            StringBuilder pathData = new StringBuilder();
            if (!cell.isEmpty()) {
                pathData.append("M ").append((int) cell.getFirst().getX())
                        .append(" ").append((int) cell.getFirst().getY()).append(" ");
                for (int j = 1; j < cell.size(); j++) {
                    Point2D p = cell.get(j);
                    pathData.append("L ").append((int) p.getX())
                            .append(" ").append((int) p.getY()).append(" ");
                }
                pathData.append("Z");
            }
            // Fill with random color
            String fill = String.format("rgb(%d,%d,%d)",
                    rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
            svg.append("<path d='").append(pathData).append("' fill='").append(fill)
                    .append("' stroke='black' stroke-width='1'/>\n");


        }

        // Optional
        for (Point2D site : sites) {
            svg.append("<circle cx='").append((int) site.getX()).append("' cy='")
                    .append((int) site.getY()).append("' r='3' fill='black'/>\n");
        }

        svg.append("</svg>");
        return svg.toString();
    }

    public static String generateSVG(double mapWidth, double mapHeight, List<GeoPolZone> zones) {
        StringBuilder svg = new StringBuilder();
        svg.append("<svg width='").append(mapWidth).append("' height='").append(mapHeight)
                .append("' xmlns='http://www.w3.org/2000/svg'>\n");

        for (int i = 0; i < zones.size(); i++) {
            GeoPolZone zone = zones.get(i);
            List<Point2D> cell = zone.getBoundary();

            StringBuilder pathData = new StringBuilder();
            if (!cell.isEmpty()) {
                pathData.append("M ").append((int) cell.getFirst().getX())
                        .append(" ").append((int) cell.getFirst().getY()).append(" ");
                for (int j = 1; j < cell.size(); j++) {
                    Point2D p = cell.get(j);
                    pathData.append("L ").append((int) p.getX())
                            .append(" ").append((int) p.getY()).append(" ");
                }
                pathData.append("Z");
            }


            Color color = zone.getColor();
            String name = zone.getName();
            if(zone instanceof RandProvince prov) {
                String countryId = CountryArray.getIndexISO2(prov.getOwnerId());
                name += "_" + countryId;
            }
            String fill = String.format("rgb(%d,%d,%d)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
            svg.append("<path ").append("id='").append(name).append("' d='").append(pathData).append("' fill='").append(fill)
                    .append("' stroke='black' stroke-width='1'/>\n");


        }

        // Optional
        for (GeoPolZone zone : zones) {
            Point2D site = zone.getMainPoint();
            svg.append("<circle cx='").append((int) site.getX()).append("' cy='")
                    .append((int) site.getY()).append("' r='3' fill='black'/>\n");
        }

        svg.append("</svg>");
        return svg.toString();
    }


    // Save SVG string to a file
    public static void saveStringToPath(String filename, String svgContent) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(svgContent);
            System.out.println("SVG map saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error writing SVG file: " + e.getMessage());
        }
    }
}

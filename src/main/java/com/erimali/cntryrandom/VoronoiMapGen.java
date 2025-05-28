package com.erimali.cntryrandom;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

import javafx.geometry.Point2D;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VoronoiMapGen {
    // Size of the output image and number of sites (provinces)
    private int mapWidth;
    private int mapHeight;
    private int totalProvs;
    private int padding = 50;  // Padding around the sites for the bounding box
    private Random rand;
    private List<Point2D> sites;
    private List<List<Point2D>> cells;
    //private List<VProvince> provinces;
    //private List<VCountry> countries;


    public VoronoiMapGen(int totalProvs, int mapWidth, int mapHeight){
        this.rand = new Random();
        this.totalProvs = totalProvs;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.sites = generateRandomSites(totalProvs, mapWidth, mapHeight);
        this.cells = computeVoronoiCells(sites);
    }
    // a*x + b*y + c = 0
    static class Line {
        double a, b, c;

        public Line(double a, double b, double c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        // Evaluate the line equation at point p: if positive, the point lies on one side; negative on the other.
        double evaluate(Point2D p) {
            return a * p.getX() + b * p.getY() + c;
        }

        // Given two sites, compute the perpendicular bisector of the segment between them.
        // The bisector’s normal vector is (dx, dy) where dx = (x2 - x1) and dy = (y2 - y1).
        static Line perpendicularBisector(Point2D p1, Point2D p2) {
            double midX = (p1.getX() + p2.getX()) / 2.0;
            double midY = (p1.getY() + p2.getY()) / 2.0;
            double dx = p2.getX() - p1.getX();
            double dy = p2.getY() - p1.getY();
            // The bisector line: dx*(x - midX) + dy*(y - midY) = 0
            // -> dx*x + dy*y - (dx*midX + dy*midY) = 0
            double c = -(dx * midX + dy * midY);
            return new Line(dx, dy, c);
        }
    }

    // Clip a convex polygon with the half-plane defined by the line L.
    // We want to keep the side that contains our reference site (determined by refValue = L.evaluate(site)).
    private List<Point2D> clipPolygon(List<Point2D> polygon, Line L, double refValue) {
        List<Point2D> output = new ArrayList<>();
        int n = polygon.size();

        for (int i = 0; i < n; i++) {
            Point2D current = polygon.get(i);
            Point2D next = polygon.get((i + 1) % n);

            double currentEval = L.evaluate(current);
            double nextEval = L.evaluate(next);

            // A point is considered "inside" if its evaluation has the same sign as our reference.
            boolean currentInside = currentEval * refValue >= 0;
            boolean nextInside = nextEval * refValue >= 0;

            // If current point is inside, add it.
            if (currentInside) {
                output.add(current);
            }

            // If the edge crosses the line (i.e. one point is inside and the other outside), add the intersection.
            if (currentInside != nextInside) {
                // Find intersection using parameter t along edge: current + t*(next-current)
                double t = currentEval / (currentEval - nextEval);
                double ix = current.getX() + t * (next.getX() - current.getX());
                double iy = current.getY() + t * (next.getY() - current.getY());
                output.add(new Point2D(ix, iy));
            }
        }
        return output;
    }

    // Generate a padded bounding box that will serve as the initial polygon for every Voronoi cell.
    private List<Point2D> createBoundingBox(List<Point2D> sites) {
        double xl = Double.MAX_VALUE, xr = -Double.MAX_VALUE;
        double yb = Double.MAX_VALUE, yt = -Double.MAX_VALUE;

        // Find extremes among the sites.
        for (Point2D p : sites) {
            if (p.getX() < xl) xl = p.getX();
            if (p.getX() > xr) xr = p.getX();
            if (p.getY() < yb) yb = p.getY();
            if (p.getY() > yt) yt = p.getY();
        }

        // Add padding.
        xl -= padding;
        xr += padding;
        yb -= padding;
        yt += padding;

        // Construct a rectangle (counter-clockwise order).
        List<Point2D> box = new ArrayList<>();
        box.add(new Point2D(xl, yt));
        box.add(new Point2D(xr, yt));
        box.add(new Point2D(xr, yb));
        box.add(new Point2D(xl, yb));
        return box;
    }

    // Generate a list of random sites within the given width and height.
    private List<Point2D> generateRandomSites(int count, int width, int height) {
        List<Point2D> sites = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double x = rand.nextInt(width);
            double y = rand.nextInt(height);
            sites.add(new Point2D(x, y));
        }
        return sites;
    }

    // Build the Voronoi diagram as a list of cells (each cell is a list of points defining its polygon).
    private List<List<Point2D>> computeVoronoiCells(List<Point2D> sites) {
        List<List<Point2D>> cells = new ArrayList<>();
        // Create a bounding box that covers all sites (with padding)
        List<Point2D> boundingBox = createBoundingBox(sites);

        for (Point2D site : sites) {
            // Start with the full bounding box.
            List<Point2D> cell = new ArrayList<>(boundingBox);

            // Clip the cell with the half-plane defined by each other site.
            for (Point2D other : sites) {
                if (site.equals(other))
                    continue;

                // Compute the perpendicular bisector between the current site and the other site.
                Line bisector = Line.perpendicularBisector(site, other);
                // Determine which side of the bisector to keep:
                // Evaluate the bisector function at the current site.
                double refValue = bisector.evaluate(site);
                // Clip the polygon (cell) with this half-plane.
                cell = clipPolygon(cell, bisector, refValue);

                // If the cell becomes empty, we can break early.
                if (cell.isEmpty()) {
                    break;
                }
            }
            cells.add(cell);
        }
        return cells;
    }

    // Generate an SVG string from the Voronoi cells.
    private String generateSVG(List<List<Point2D>> cells, List<Point2D> sites) {
        StringBuilder svg = new StringBuilder();
        svg.append("<svg width='").append(mapWidth).append("' height='").append(mapHeight)
                .append("' xmlns='http://www.w3.org/2000/svg'>\n");

        Random rand = new Random();

        // Draw each Voronoi cell.
        for (int i = 0; i < cells.size(); i++) {
            List<Point2D> cell = cells.get(i);
            // Build the SVG path from polygon vertices.
            StringBuilder pathData = new StringBuilder();
            if (!cell.isEmpty()) {
                pathData.append("M ").append((int) cell.get(0).getX())
                        .append(" ").append((int) cell.get(0).getY()).append(" ");
                for (int j = 1; j < cell.size(); j++) {
                    Point2D p = cell.get(j);
                    pathData.append("L ").append((int) p.getX())
                            .append(" ").append((int) p.getY()).append(" ");
                }
                pathData.append("Z");
            }
            // Fill with a random color.
            String fill = String.format("rgb(%d,%d,%d)",
                    rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
            svg.append("<path d='").append(pathData).append("' fill='").append(fill)
                    .append("' stroke='black' stroke-width='1'/>\n");
        }

        // Optionally, draw the site points.
        for (Point2D site : sites) {
            svg.append("<circle cx='").append((int) site.getX()).append("' cy='")
                    .append((int) site.getY()).append("' r='3' fill='black'/>\n");
        }

        svg.append("</svg>");
        return svg.toString();
    }

    // Save the SVG string to a file.
    static void saveSVG(String filename, String svgContent) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(svgContent);
            System.out.println("SVG map saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error writing SVG file: " + e.getMessage());
        }
    }

    public Pane createPaneFX(){
        Pane pane = new Pane();
        List<Point2D> sites = generateRandomSites(totalProvs, mapWidth, mapHeight);
        List<List<Point2D>> cells = computeVoronoiCells(sites);
        // Draw the Voronoi cells as JavaFX Polygons
        for (List<Point2D> cell : cells) {
            Polygon polygon = new Polygon();
            for (Point2D point : cell) {
                polygon.getPoints().addAll(point.getX(), point.getY());
            }

            // Random fill color
            Color fillColor = Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
            polygon.setFill(fillColor);
            polygon.setStroke(Color.BLACK);

            pane.getChildren().add(polygon);
        }

        // Draw site points
        for (Point2D site : sites) {
            Circle circle = new Circle(site.getX(), site.getY(), 3, Color.BLACK);
            pane.getChildren().add(circle);
        }

        return pane;
    }

    public void saveToSVG(String path){
        saveSVG(path, generateSVG(cells, sites));
    }
}

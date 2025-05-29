package com.erimali.cntryrandom;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// More efficient than brute force, but points are randomly generated within the rectangular grid
public class JitteredGridVoronoi implements Voronoi {
    private final int rows, cols;
    private final double width, height, jitterFactor;
    private final Point2D[][] gridPoints;
    private List<Point2D> sites;
    private List<List<Point2D>> cells;
    private final double cellWidth, cellHeight;

    public JitteredGridVoronoi(int rows, int cols, double width, double height, double jitterFactor) {
        this.rows = rows;
        this.cols = cols;
        this.width = width;
        this.height = height;
        this.jitterFactor = jitterFactor;
        this.gridPoints = new Point2D[rows][cols];
        this.sites = new ArrayList<>();
        this.cells = new ArrayList<>();
        this.cellWidth = width / cols;
        this.cellHeight = height / rows;

        generateJitteredGrid();
        generateVoronoiCells();
    }


    private void generateJitteredGrid() {
        Random rand = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double x = j * cellWidth + rand.nextDouble() * cellWidth * jitterFactor;
                double y = i * cellHeight + rand.nextDouble() * cellHeight * jitterFactor;
                Point2D point = new Point2D(x, y);
                gridPoints[i][j] = point;
                sites.add(point);
            }
        }
    }

    public static int[] calcRowsColsFromTotalProv(int totalProv, double mapWidth, double mapHeight) {
        double aspectRatio = mapWidth / mapHeight;

        // Estimate columns based on square root and aspect ratio
        int cols = (int) Math.sqrt(totalProv * aspectRatio);
        int rows = (int) Math.ceil((double) totalProv / cols);

        return new int[]{rows, cols};
    }

    @Override
    public List<Point2D> getSites() {
        return sites;
    }

    @Override
    public List<List<Point2D>> getVoronoiCells() {
        return cells;
    }

    @Override
    public void setSites(List<Point2D> sites) {
        if (this.sites != sites && this.sites.size() == sites.size()) {
            this.sites = sites;
            int k = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    gridPoints[i][j] = sites.get(k++);
                }
            }
        }

    }

    @Override
    public void generateVoronoiCells() {
        cells.clear();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Point2D site = gridPoints[i][j];
                List<Point2D> cell = getInitialBoundingBox();

                for (int di = -1; di <= 1; di++) {
                    for (int dj = -1; dj <= 1; dj++) {
                        if (di == 0 && dj == 0) continue;
                        int ni = i + di;
                        int nj = j + dj;
                        if (ni >= 0 && ni < rows && nj >= 0 && nj < cols) {
                            Point2D neighbor = gridPoints[ni][nj];
                            cell = clipPolygon(cell, site, neighbor);
                            if (cell.isEmpty()) break;
                        }
                    }
                }

                cells.add(cell);
            }
        }
    }

    private List<Point2D> getInitialBoundingBox() {
        List<Point2D> box = new ArrayList<>();
        box.add(new Point2D(0, 0));
        box.add(new Point2D(width, 0));
        box.add(new Point2D(width, height));
        box.add(new Point2D(0, height));
        return box;
    }

    // Clip polygon with a bisector from site1 and site2
    private List<Point2D> clipPolygon(List<Point2D> poly, Point2D site, Point2D neighbor) {
        List<Point2D> result = new ArrayList<>();
        double mx = (site.getX() + neighbor.getX()) / 2;
        double my = (site.getY() + neighbor.getY()) / 2;
        double dx = neighbor.getY() - site.getY();
        double dy = site.getX() - neighbor.getX();

        Point2D normal = new Point2D(dx, dy);

        for (int i = 0; i < poly.size(); i++) {
            Point2D a = poly.get(i);
            Point2D b = poly.get((i + 1) % poly.size());
            boolean aInside = isLeft(a, normal, mx, my);
            boolean bInside = isLeft(b, normal, mx, my);

            if (aInside && bInside) {
                result.add(b);
            } else if (aInside != bInside) {
                Point2D intersect = getIntersection(a, b, mx, my, normal);
                if (intersect != null) result.add(intersect);
                if (bInside) result.add(b);
            }
        }
        return result;
    }

    private boolean isLeft(Point2D p, Point2D normal, double mx, double my) {
        return (p.getX() - mx) * normal.getX() + (p.getY() - my) * normal.getY() >= 0;
    }

    private Point2D getIntersection(Point2D a, Point2D b, double mx, double my, Point2D normal) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double t = ((mx - a.getX()) * normal.getX() + (my - a.getY()) * normal.getY()) /
                (dx * normal.getX() + dy * normal.getY());
        if (t >= 0 && t <= 1) {
            return new Point2D(a.getX() + t * dx, a.getY() + t * dy);
        }
        return null;
    }
}
